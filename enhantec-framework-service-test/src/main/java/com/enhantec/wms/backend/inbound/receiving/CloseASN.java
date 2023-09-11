package com.enhantec.wms.backend.inbound.receiving;

import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.inbound.asn.utils.ReceiptUtilHelper;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.math.BigDecimal;
import com.enhantec.framework.common.utils.EHContextHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class CloseASN extends WMSBaseService {


    /**
     * --注册方法
      delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHCloseASN'
      insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
      values ('EHCloseASN', 'com.enhantec.sce.inbound.receiving', 'enhantec', 'CloseASN', 'TRUE', 'JOHN', 'JOHN'
      , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,RECEIPTKEY,ESIGNATUREKEY','0.10','0');
     */


    private static final long serialVersionUID = 1L;

    public CloseASN() {
    }

    public void execute(ServiceDataHolder serviceDataHolder) {
        String userid = EHContextHelper.getUser().getUsername();


//        EHContextHelper.switchOrgId("wh01");
//        DBHelper.executeQueryRawData("select * from t1",new ArrayList<>());
//        EHContextHelper.switchOrgId("1565464895783219202");
//        DBHelper.executeQueryRawData("select * from t1",new ArrayList<>());
//
//        DBHelper.executeQueryRawDataByOrgId("wh01","select * from t1",new ArrayList<>());
//
//        DBHelper.executeQueryRawData("select * from t1",new ArrayList<>());



        try {



            String receiptKey = serviceDataHolder.getInputDataAsMap().getString("RECEIPTKEY");
            String esignatureKey = serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");
            String allowAbnormalClose = serviceDataHolder.getInputDataAsMap().getString("ALLOWABNORMALCLOSE");

            Map<String, String>  receiptInfo = Receipt.findByReceiptKey(receiptKey,true);

            //鉴于使用场景很少暂时禁用异常关闭功能，以方便RF使用通用的关闭逻辑
            // if(receiptInfo.get("STATUS").equals("0")&& !("true").equalsIgnoreCase(allowAbnormalClose))
            // ExceptionHelper.throwRfFulfillLogicException("不允许正常关闭未收货的ASN单");

            String notes = DBHelper.getStringValue("SELECT NOTES FROM Esignature WHERE SERIALKEY = ?",new Object[]{
                    esignatureKey},"电子签名");

            ServiceHelper.executeService("closeASNService",
                    new ServiceDataHolder(
                            new ServiceDataMap(
                            new HashMap<String,Object>(){{
                        put("RECEIPTKEY",receiptKey);}}
                            )
                    )
            );

            Map<String, String> receiptHashmap = Receipt.findByReceiptKey(receiptKey,true);

            if(!UtilHelper.isEmpty(notes)) {
                DBHelper.executeUpdate( "UPDATE RECEIPT " +
                        "SET NOTES = ? " +
                        "WHERE RECEIPTKEY = ? ", new Object[]{
                        notes,
                        receiptKey
                });
            }



            //CSS在接口处理是否回退采购单数量的逻辑，业务不做处理。


            if(receiptInfo.get("TYPE").equals(CDSysSet.getPOReceiptType())) {
                //TODO:ASN支持不同SKU的多个指令行，暂不允许多个相同SKU的指令行在一个ASN单的情况。

                //考虑有ASN收货存在指令行，去掉HAVING SUM(QTYEXPECTED-QTYRECEIVED)>0 子句，同时在收货过程中指令明细行的的预计收货数量会自动置为0，保证SUM(QTYEXPECTED-QTYRECEIVED)回滚的数量匹配。
                //ORIGINALLINENUMBER IS NO NULL时为非指令行，忽略预期量
                List<Map<String, String>> rollbackSkuQtyHashMap = DBHelper.executeQuery(
                        "SELECT EXTERNRECEIPTKEY, SKU, SUM(CASE WHEN ORIGINALLINENUMBER IS NOT NULL THEN 0 ELSE QTYEXPECTED END - QTYRECEIVED) AS ROLLBACKQTY FROM RECEIPTDETAIL " +
                                " WHERE RECEIPTKEY =? GROUP BY EXTERNRECEIPTKEY, SKU ", new Object[]{
                                receiptKey
                        });

                for (Map<String, String> rollbackSkuQty : rollbackSkuQtyHashMap) {
                    String conversion = SKU.findById( rollbackSkuQty.get("SKU"), true).get("SNAVGWGT");
                    BigDecimal totalRollbackQty = ReceiptUtilHelper.stdQty2PoWgt(conversion,new BigDecimal(rollbackSkuQty.get("ROLLBACKQTY")),rollbackSkuQty.get("SKU"));

                    List<Map<String, String>> preReceiptCheckList = DBHelper.executeQuery(
                            "SELECT SERIALKEY,FROMKEY, FROMLINENO,POUSEDQTY FROM PRERECEIPTCHECK WHERE RECEIPTLOT = ? ORDER BY FROMKEY DESC, FROMLINENO DESC", new Object[]{
                                    rollbackSkuQty.get("EXTERNRECEIPTKEY")
                            });

                    for (Map<String, String> preReceiptCheckRec : preReceiptCheckList) {
                        if (totalRollbackQty.compareTo(BigDecimal.ZERO) > 0) {

                            BigDecimal currentPOUsedQty = new BigDecimal(preReceiptCheckRec.get("POUSEDQTY"));
                            BigDecimal currentPORollbackQty = totalRollbackQty.compareTo(currentPOUsedQty) >= 0 ? currentPOUsedQty : totalRollbackQty;
                            String newPOLineStatus = UtilHelper.decimalCompare(currentPOUsedQty,currentPORollbackQty)==0 ? "0": "5";

                            //回退WMS_PO_DETAIL表
                            DBHelper.executeUpdate(
                                    "UPDATE WMS_PO_DETAIL SET RECEIVEDQTY = RECEIVEDQTY - ?, STATUS = ? WHERE  POKEY = ?  AND POLINENUMBER = ? ", new Object[]{
                                            currentPORollbackQty,
                                            newPOLineStatus,
                                            preReceiptCheckRec.get("FROMKEY"),
                                            preReceiptCheckRec.get("FROMLINENO")
                                    });

                            //回退收货检查表
                            DBHelper.executeUpdate(
                                    "UPDATE PRERECEIPTCHECK SET POUSEDQTY = POUSEDQTY - ? WHERE SERIALKEY = ? ", new Object[]{
                                           currentPORollbackQty,
                                            preReceiptCheckRec.get("SERIALKEY")
                                    });
                            totalRollbackQty = totalRollbackQty.subtract(currentPORollbackQty);


                        }
                    }


                }
            }





            Udtrn UDTRN=new Udtrn();
            UDTRN.EsignatureKey=esignatureKey;
            UDTRN.FROMTYPE= "true".equalsIgnoreCase(allowAbnormalClose) ? "异常关闭ASN":"关闭ASN";
            UDTRN.FROMTABLENAME="RECEIPT";
            UDTRN.FROMKEY=receiptKey;
            UDTRN.FROMKEY1="";
            UDTRN.FROMKEY2="";
            UDTRN.FROMKEY3="";
            UDTRN.TITLE01="ASN单号";    UDTRN.CONTENT01=receiptKey;
            UDTRN.TITLE02="状态";    UDTRN.CONTENT02="11";
            UDTRN.Insert( userid);




          

        }catch (Exception e){
            
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());

        }finally {
            
        }
    }
}