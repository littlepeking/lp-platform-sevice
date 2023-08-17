package com.enhantec.wms.backend.inventory.repackaging;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;

/**
 --注册方法

 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME = 'EHRepackagingMaintainLeftQty';
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHRepackagingMaintainLeftQty', 'com.enhantec.sce.inventory.repackaging', 'enhantec', 'MaintainLeftLpnQty','TRUE','JOHN','JOHN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,TYPE,RECEIPTKEY,ID,LEFTQTY,UOM,ESIGNATUREKEY','0.10','0');

 **/

public class MaintainLeftLpnQty extends WMSBaseService {

    private static final long serialVersionUID = 1L;

    public MaintainLeftLpnQty()
    {

    }

    public void execute(ServiceDataHolder serviceDataHolder)
    {
        String userid = EHContextHelper.getUser().getUsername();



        try
        {
            final String type= serviceDataHolder.getInputDataAsMap().getString("TYPE");
            final String receiptKey = serviceDataHolder.getInputDataAsMap().getString("RECEIPTKEY");
            final String id= serviceDataHolder.getInputDataAsMap().getString("ID");
            final String leftQty= serviceDataHolder.getInputDataAsMap().getString("LEFTQTY");
            final String uom= serviceDataHolder.getInputDataAsMap().getString("UOM");
            final String esignatureKey= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

            if (UtilHelper.isEmpty(type)) throw new Exception("类型不能为空");
            if (UtilHelper.isEmpty(receiptKey)) throw new Exception("分装入库单号不能为空");

            Map<String,String> receiptHashMap =  Receipt.findByReceiptKey(receiptKey,true);

            /*
                RECEIPT.SUSR2 分装单关联的收货批次号
                RECEIPT.SUSR3 分装间
                RECEIPT.SUSR4 分装单关联的领料出库订单号+行号
                RECEIPT.SUSR5 分装余料信息列表，格式： 容器号1|剩余数量；容器号2|剩余数量;

             */
            String orderKeyStr = receiptHashMap.get("SUSR4");
            String orderKey = orderKeyStr.substring(0,10);
            String orderLineNumber = orderKeyStr.substring(10);
            boolean isInRepackProcess = RepackgingUtils.isInRepackProcess(orderKey,orderLineNumber);

            List<Map<String,String>> leftLpnList = RepackgingUtils.getLeftLPNListFromStr(receiptHashMap.get("SUSR5"));

            String LeftLpnStr = "";


            if(UtilHelper.equals(type,"ADD")) {

                if(isInRepackProcess) ExceptionHelper.throwRfFulfillLogicException("分装进行中，不允许进行修改");

                if (UtilHelper.isEmpty(id)) throw new Exception("余料容器条码不能为空");
                if (UtilHelper.isEmpty(leftQty) || UtilHelper.decimalStrCompare(leftQty,"0")<=0) throw new Exception("余料数量必须大于0");
                if (UtilHelper.isEmpty(uom)) throw new Exception("余料计量单位不能为空");

                Optional<Map<String, String>> optionalLeftLpnInfo = leftLpnList.stream().filter(x->x.get("ID").equals(id)).findFirst();


                Map<String,String> idHashMap = LotxLocxId.findAvailInvByLocId(receiptHashMap.get("SUSR3"),id,true,true);

                if(!UtilHelper.equals(idHashMap.get("LOTTABLE06"),receiptHashMap.get("SUSR2")))
                    ExceptionHelper.throwRfFulfillLogicException("所选容器的批次非当前分装批次"+receiptHashMap.get("SUSR2"));

                Map<String,String> skuHashMap = SKU.findById(idHashMap.get("SKU"),true);

                //检查分装间是否存在该容器且库存量>余料数量

                BigDecimal stdLeftQty = UOM.UOMQty2StdQty(skuHashMap.get("PACKKEY"),uom,UtilHelper.str2Decimal(leftQty,"余料数量",false));

                if(UtilHelper.decimalStrCompare(idHashMap.get("QTY"),stdLeftQty.toPlainString())<0){
                    ExceptionHelper.throwRfFulfillLogicException("容器余料数量不能大于当前库存数量");
                }

                if(optionalLeftLpnInfo.isPresent()) {
                    Map<String,String> leftLpnInfo = optionalLeftLpnInfo.get();
                    leftLpnInfo.put("LEFTQTY",leftQty);
                    leftLpnInfo.put("UOM",uom);
                }else {
                    Map<String,String> leftLpnInfo = new HashMap<>();
                    leftLpnInfo.put("ID",id);
                    leftLpnInfo.put("LEFTQTY",leftQty);
                    leftLpnInfo.put("UOM",uom);
                    leftLpnList.add(leftLpnInfo);
                }

                Udtrn UDTRN=new Udtrn();

                UDTRN.EsignatureKey=esignatureKey;
                UDTRN.FROMTYPE = "添加分装余料记录";
                UDTRN.FROMTABLENAME = "RECEIPT";
                UDTRN.FROMKEY = receiptKey;
                UDTRN.FROMKEY1LABEL = "分装入库单号";
                UDTRN.FROMKEY1 = receiptKey;

                UDTRN.FROMKEY2LABEL = "余料容器号";
                UDTRN.FROMKEY2 = id;
                UDTRN.TITLE01 = "数量";
                UDTRN.CONTENT01 = leftQty;
                UDTRN.TITLE02 = "单位";
                UDTRN.CONTENT02 = uom;

                UDTRN.Insert( userid);

            }else if(UtilHelper.equals(type,"REMOVE")) {

                if(isInRepackProcess) ExceptionHelper.throwRfFulfillLogicException("分装进行中，不允许进行修改");

                if (UtilHelper.isEmpty(id)) throw new Exception("余料容器条码不能为空");

                Optional<Map<String, String>> optionalLeftLpnInfo = leftLpnList.stream().filter(x->x.get("ID").equals(id)).findFirst();

                if(optionalLeftLpnInfo.isPresent()) {
                    Map<String,String> LpnInfo =  optionalLeftLpnInfo.get();
                    leftLpnList.remove(LpnInfo);
                }else {
                    ExceptionHelper.throwRfFulfillLogicException("未找到要删除的余料标签"+id);
                }

                Udtrn UDTRN=new Udtrn();
                UDTRN.EsignatureKey=esignatureKey;
                UDTRN.FROMTYPE = "删除分装余料记录";
                UDTRN.FROMTABLENAME = "RECEIPT";
                UDTRN.FROMKEY = receiptKey;
                UDTRN.FROMKEY1LABEL = "分装入库单号";
                UDTRN.FROMKEY1 = receiptKey;
                UDTRN.FROMKEY2LABEL = "余料容器号";
                UDTRN.FROMKEY2 = id;
                UDTRN.Insert( userid);

            }else{
                //query

            }

            LeftLpnStr = buildLeftLpnInfoStr(leftLpnList);

            DBHelper.executeUpdate("UPDATE RECEIPT SET SUSR5 = ? WHERE RECEIPTKEY = ? ",
                    new Object[]{
                            LeftLpnStr,
                            receiptKey,
                        });

            BigDecimal totalLeftQty = new BigDecimal(0);



            if(leftLpnList.size()>0) {

                Map<String,String> idHashMap = LotxLocxId.findById(leftLpnList.get(0).get("ID"),true);

                Map<String,String> skuHashMap = SKU.findById(idHashMap.get("SKU"),true);

                for (Map<String, String> leftLpnInfo : leftLpnList) {
                    BigDecimal lpnQty = UOM.UOMQty2StdQty( skuHashMap.get("PACKKEY"), leftLpnInfo.get("UOM"), new BigDecimal(leftLpnInfo.get("LEFTQTY")));
                    totalLeftQty = totalLeftQty.add(lpnQty);
                }
            }




            ServiceDataMap theOutDO = new ServiceDataMap();
            theOutDO.setAttribValue("TOTALLEFTQTY",UtilHelper.trimZerosAndToStr(totalLeftQty.toPlainString()));
            theOutDO.setAttribValue("TOTALLEFTLPNCOUNT",String.valueOf(leftLpnList.size()));
            serviceDataHolder.setReturnCode(1);
            serviceDataHolder.setOutputData(theOutDO);


          
        }
        catch (Exception e)
        {
            
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());
        }finally {
            
        }

    }

    private String buildLeftLpnInfoStr(List<Map<String, String>> leftLpnList) {

        StringBuffer leftLpnSB = new StringBuffer();

        for(Map<String,String> lpnInfo : leftLpnList){
            leftLpnSB.append(lpnInfo.get("ID")).append("|")
                    .append(lpnInfo.get("LEFTQTY")).append("|")
                    .append(lpnInfo.get("UOM")).append(";");

        }
        return leftLpnSB.toString();
    }




}
