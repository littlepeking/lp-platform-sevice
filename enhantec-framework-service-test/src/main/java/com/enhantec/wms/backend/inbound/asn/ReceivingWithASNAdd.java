package com.enhantec.wms.backend.inbound.asn;

import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.base.code.CDReceiptType;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import com.enhantec.wms.backend.utils.print.Labels;
import com.enhantec.wms.backend.utils.print.PrintHelper;

import java.math.BigDecimal;
import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.Map;

/**
 *
 * --注册方法
 DELETE FROM wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHReceivingWithASNAdd'
 insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHReceivingWithASNAdd', 'com.enhantec.sce.inbound.asn', 'enhantec', 'ReceivingWithASNAdd', 'TRUE', 'john', 'john',
 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,LPN,RECEIPTKEY,RECEIPTLINENUMBER,GROSSWGT,NETWGT,TAREWGT,UOM,SNLIST,PRINTER','0.10','0');
 */
public class ReceivingWithASNAdd extends WMSBaseService {
    private static final long serialVersionUID = 1L;

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {


        String lpn = serviceDataHolder.getInputDataAsMap().getString("LPN");
        String receiptKey = serviceDataHolder.getInputDataAsMap().getString("RECEIPTKEY");
        String originalReceiptLineNumber = serviceDataHolder.getInputDataAsMap().getString("RECEIPTLINENUMBER");
        String netWgt = serviceDataHolder.getInputDataAsMap().getString("NETWGT");
        String grossWgt = serviceDataHolder.getInputDataAsMap().getString("GROSSWGT");
        String tareWgt = serviceDataHolder.getInputDataAsMap().getString("TAREWGT");
        String uom = serviceDataHolder.getInputDataAsMap().getString("UOM");
        String snListStr = serviceDataHolder.getInputDataAsMap().getString("SNLIST");
        String printer = serviceDataHolder.getInputDataAsMap().getString("PRINTER");

        String userId = EHContextHelper.getUser().getUsername();

        try{

            Map<String,String> receiptHashMap = Receipt.findByReceiptKey(receiptKey,true);

            if (receiptHashMap.get("STATUS").equals("9") || receiptHashMap.get("STATUS").equals("11")
                    || receiptHashMap.get("STATUS").equals("15") || receiptHashMap.get("STATUS").equals("20")
            )  throw new Exception("收货单行已收货完成,不允许修改");

            Map<String,String> originalReceiptDetailHashMap = Receipt.findReceiptDetailById(receiptKey,originalReceiptLineNumber,true);

            if(!(new BigDecimal(originalReceiptDetailHashMap.get("QTYRECEIVED")).compareTo(new BigDecimal(0)) == 0)) ExceptionHelper.throwRfFulfillLogicException("收货指令行的已收货数量必须为0");

            //插入收货行的应为STD UOM,但是RF传入的是UOM QTY,需要转换
            BigDecimal grossWgtStdQty = UOM.UOMQty2StdQty(originalReceiptDetailHashMap.get("PACKKEY"), uom, new BigDecimal(grossWgt));
            BigDecimal tareWgtStdQQty = UOM.UOMQty2StdQty(originalReceiptDetailHashMap.get("PACKKEY"), uom, new BigDecimal(tareWgt));
            BigDecimal netWgtStdQQty = UOM.UOMQty2StdQty(originalReceiptDetailHashMap.get("PACKKEY"), uom, new BigDecimal(netWgt));

            String[] snList = null;
            if(!UtilHelper.isEmpty(snListStr)){
                snList = snListStr.split(";;;");
            }

            Map<String,String> insertedReceiptDetail = Receipt.insertReceiptDetailByOriginalLine(originalReceiptDetailHashMap,lpn,originalReceiptDetailHashMap.get("TOLOC"),"0"
                    ,netWgtStdQQty.toPlainString(),grossWgtStdQty.toPlainString(),tareWgtStdQQty.toPlainString(),uom,"", snList);

            if(!SKU.isSerialControl(insertedReceiptDetail.get("SKU")) ||
                    CDReceiptType.isBindAndAutoGenerateLpn( insertedReceiptDetail.get("SKU"), receiptHashMap.get("TYPE"))) {
                        PrintHelper.printLPNByReceiptLineNumber(
                                insertedReceiptDetail.get("RECEIPTKEY"),
                                insertedReceiptDetail.get("RECEIPTLINENUMBER"),
                                Labels.LPN_UI,
                                printer, "1", "ASN收货标签");

            }

            if(CDReceiptType.isAutoReceiving( receiptHashMap.get("TYPE"))) {
                Receipt.execReceiptDetailReceiving( serviceDataHolder, insertedReceiptDetail, grossWgt, tareWgt, netWgt);
            }

            Udtrn udtrn = new Udtrn();
            udtrn.FROMTYPE="添加ASN收货明细";
            udtrn.FROMTABLENAME="RECEIPT";
            udtrn.FROMKEY=receiptKey;
            udtrn.FROMKEY1= receiptHashMap.get("TYPE");
            udtrn.FROMKEY1LABEL="收货类型";
            udtrn.FROMKEY2="";
            udtrn.FROMKEY3="";
            udtrn.TITLE01="ASN单号";  udtrn.CONTENT01=receiptKey;
            udtrn.TITLE02="行号"; udtrn.CONTENT02=insertedReceiptDetail.get("RECEIPTLINENUMBER");
            udtrn.TITLE03="容器号/箱号"; udtrn.CONTENT03=lpn;
            udtrn.TITLE04="数量"; udtrn.CONTENT04=netWgt;
            udtrn.TITLE05="单位"; udtrn.CONTENT05=uom;
            udtrn.Insert(userId);



            ServiceDataMap theOutDO = new ServiceDataMap();
            theOutDO.setAttribValue("TOID", insertedReceiptDetail.get("TOID"));
            serviceDataHolder.setReturnCode(1);
            serviceDataHolder.setOutputData(theOutDO);

        }catch (Exception e){
            try
            {
            }	catch (Exception e1) {		}
            if ( e instanceof FulfillLogicException) {
                throw (FulfillLogicException)e;
            }
            else {
                throw new FulfillLogicException(e.getMessage());
            }
        }

    }

}