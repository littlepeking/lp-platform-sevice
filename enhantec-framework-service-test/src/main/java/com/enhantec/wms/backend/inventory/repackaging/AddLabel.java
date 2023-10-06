package com.enhantec.wms.backend.inventory.repackaging;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;
import com.enhantec.wms.backend.utils.print.Labels;
import com.enhantec.wms.backend.utils.print.PrintHelper;

import java.math.BigDecimal;
import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.Map;
import java.util.HashMap;

import static com.enhantec.wms.backend.utils.common.UtilHelper.trimZerosAndToStr;


/**
 --注册方法

 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME = 'EHRepackagingAddLabel';
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHRepackagingAddLabel', 'com.enhantec.sce.inventory.repackaging', 'enhantec', 'AddLabel','TRUE','JOHN','JOHN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ORDERKEY,ORDERLINENUMBER,LOTTABLE06,NETWGT,GROSSWGT,TAREWGT,UOM,PRINTER,ESIGNATUREKEY','0.10','0');

 **/

public class AddLabel extends WMSBaseService {

    private static final long serialVersionUID = 1L;

    public AddLabel()
    {

    }

    public void execute(ServiceDataHolder serviceDataHolder)
    {
        String userid = EHContextHelper.getUser().getUsername();




        try
        {

            final String orderKey = serviceDataHolder.getInputDataAsMap().getString("ORDERKEY");
            final String orderLineNumber = serviceDataHolder.getInputDataAsMap().getString("ORDERLINENUMBER");
            final String lottable06= serviceDataHolder.getInputDataAsMap().getString("LOTTABLE06");
            final String netWgt= serviceDataHolder.getInputDataAsMap().getString("NETWGT");
            final String grossWgt= serviceDataHolder.getInputDataAsMap().getString("GROSSWGT");
            final String tareWgt= serviceDataHolder.getInputDataAsMap().getString("TAREWGT");
            final String uom= serviceDataHolder.getInputDataAsMap().getString("UOM");
            final String printer= serviceDataHolder.getInputDataAsMap().getString("PRINTER");
            final String esignatureKey= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

            if (UtilHelper.isEmpty(orderKey)) throw new Exception("订单号不能为空");
            if (UtilHelper.isEmpty(orderLineNumber)) throw new Exception("订单行号不能为空");
            if (UtilHelper.isEmpty(lottable06)) throw new Exception("批号不能为空");
            if (UtilHelper.isEmpty(uom)) throw new Exception("单位不能为空");


            boolean isInRepackProcess = RepackgingUtils.isInRepackProcess(orderKey,orderLineNumber);
            if(isInRepackProcess) ExceptionHelper.throwRfFulfillLogicException("分装进行中，不允许进行修改");


            BigDecimal grossWgtDecimal = UtilHelper.str2Decimal(grossWgt,"毛重",false);
            BigDecimal netWgtDecimal = UtilHelper.str2Decimal(netWgt,"净重",false);
            BigDecimal tareWgtDecimal = UtilHelper.str2Decimal(tareWgt,"皮重",false);
            if (grossWgtDecimal.subtract(netWgtDecimal).compareTo(tareWgtDecimal) != 0) {
                ExceptionHelper.throwRfFulfillLogicException("输入的毛皮净重不匹配");
            }

            Map<String,String> orderDetailHashMap = Orders.findOrderDetailByKey(orderKey,orderLineNumber,true);
            Map<String,String>  skuHashMap = SKU.findById(orderDetailHashMap.get("SKU"),true);

            BigDecimal stdGrossWgtDecimal = UOM.UOMQty2StdQty( skuHashMap.get("PACKKEY"), uom, grossWgtDecimal);
            BigDecimal stdTareWgtDecimal = UOM.UOMQty2StdQty( skuHashMap.get("PACKKEY"), uom, tareWgtDecimal);
            BigDecimal stdNetWgtDecimal = UOM.UOMQty2StdQty( skuHashMap.get("PACKKEY"), uom, netWgtDecimal);


            String currentRepackReceiptKey = orderDetailHashMap.get("SUSR1");//当前分装入库单号
            String currentPackLoc = orderDetailHashMap.get("SUSR3");//分装间

            if(UtilHelper.isEmpty(currentPackLoc)) ExceptionHelper.throwRfFulfillLogicException("数据异常，找不到订单行关联的分装间");

            String storerKey= DBHelper.getStringValue( "select udf1 from codelkup where listname=? and code=?", new String[]{"SYSSET","STORERKEY"}, "");
            String repackReceiptType= DBHelper.getStringValue( "select udf1 from codelkup where listname=? and code=?", new String[]{"SYSSET","REPACKRECT"}, "");
            // String repackOrderType= XtSql.GetValue( "select udf1 from codelkup where listname=? and code=?", new String[]{"SYSSET","REPACKORDT"}, "");
            if(UtilHelper.isEmpty(repackReceiptType)) ExceptionHelper.throwRfFulfillLogicException("分装入库单类型代码未设置");

            if(UtilHelper.isEmpty(currentRepackReceiptKey)){

                currentRepackReceiptKey= LegacyDBHelper.GetNCounterBill( "RECEIPT");

                Map<String,String> RECEIPT=new HashMap<String,String>();
                RECEIPT.put("ADDWHO", userid);
                RECEIPT.put("EDITWHO", userid);
                RECEIPT.put("RECEIPTKEY", currentRepackReceiptKey);
                //生成分装单号
                String repackExternReceiptKey = IdGenerationHelper.generateID( orderKey+"F",2);
                RECEIPT.put("EXTERNRECEIPTKEY", repackExternReceiptKey);
                RECEIPT.put("STATUS", "0");
                RECEIPT.put("ALLOWAUTORECEIPT", "0");
                RECEIPT.put("TYPE", repackReceiptType);
                RECEIPT.put("ISCONFIRMED", "2"); //分装ASN默认为已确认并且不允许取消确认
                RECEIPT.put("STORERKEY", storerKey);
                RECEIPT.put("SUSR2", lottable06);//当前ASN的分装批次号
                RECEIPT.put("SUSR3", currentPackLoc);//分装间
                RECEIPT.put("SUSR4", orderKey+orderLineNumber);//分装单关联的领料出库订单号+行号
                LegacyDBHelper.ExecInsert( "RECEIPT", RECEIPT);

                DBHelper.executeUpdate("UPDATE ORDERDETAIL SET SUSR1 = ? WHERE ORDERKEY = ? AND ORDERLINENUMBER = ? ",new Object[]{
                     currentRepackReceiptKey,  orderKey ,  orderLineNumber });

            }else{
                Map<String,String> receiptHashMap =  Receipt.findByReceiptKey(currentRepackReceiptKey,true);
                //RECEIPT.SUSR2 当前进行的分装批次号
                if(!UtilHelper.equals(receiptHashMap.get("SUSR2"),lottable06)) ExceptionHelper.throwRfFulfillLogicException("当前分装进行中的批次为"+receiptHashMap.get("SUSR2")+"，请先完成该批次分装");

            }

            //分装入库单行号
            Object preReceiptLineNumberObj = DBHelper.getStringValue("SELECT MAX(RECEIPTLINENUMBER) FROM RECEIPTDETAIL WHERE RECEIPTKEY = ?",
                new Object[]{currentRepackReceiptKey},"");

            int receiptLineNumberInt = preReceiptLineNumberObj == null ? 1 : Integer.parseInt(preReceiptLineNumberObj.toString())+1;

            String receiptLineNumber = receiptLineNumberInt+"";

            while(receiptLineNumber.length()<5) receiptLineNumber="0"+receiptLineNumber;

			/*
			    批属性01	包装
				批属性02	存货类型
				E批属性03	状态
				批属性04	入库日期
				E批属性05	复验日期
				批属性06	批号
				E批属性07	规格
				E批属性08	供应商
				E批属性09 供应商批次
				批属性10 采购编码
				E批属性11	有效期
				E批属性12	原材料-取样日期/成品-生产日期
			*/

            Map<String,String> lpnInfo=DBHelper.getRecord(
                    " SELECT TOP 1 s.SKU, s.DESCR SKUDESCR,s.PACKKEY, s.COMMODITYCLASS STORAGECONDITIONS,id.TAREWGT,id.ISOPENED, " +
                            "id.BARRELNUMBER, id.TOTALBARREL, id.barreldescr BARRELDESCR, " +
                            "id.ORIGINALGROSSWGT, id.ORIGINALTAREWGT, id.ORIGINALNETWGT, id.PROJECTCODE, " +
                            "id.GROSSWGT, id.TAREWGT, id.NETWGT, " +
                            "elot.LOTTABLE01," +  "elot.LOTTABLE02," +  "elot.ELOTTABLE02," +
                            "elot.ELOTTABLE03," +   "elot.LOTTABLE04," +
                            "elot.ELOTTABLE05," +  "elot.ELOTTABLE06," +   "elot.LOTTABLE06," +
                            "elot.ELOTTABLE07," +   "elot.ELOTTABLE08," +
                            "elot.ELOTTABLE09," +   "elot.LOTTABLE10," +
                            "elot.ELOTTABLE11," +   "elot.ELOTTABLE12," +
                            "id.RETURNTIMES FROM idnotes id, sku s, v_lotattribute elot, lotxlocxid l " +
                            " WHERE l.sku = s.sku " +
                            " and l.id = id.id " +
                            " and l.lot = elot.lot " +
                            " and l.QTY > 0 " +
                            " and id.NETWGT > 0 " +
                            " and elot.LOTTABLE06 = ? "
                    , new Object[]{lottable06},"收货批次备货容器");
            if (lpnInfo == null) throw new Exception("未找到该批次的备货容器");

            String suppilerName = " ";
            if(!UtilHelper.isEmpty(lpnInfo.get("ELOTTABLE08"))) {
                Map<String, String> supplierInfo = DBHelper.getRecord(
                        "SELECT * FROM STORER WHERE TYPE = '5' AND STORERKEY = ? "
                        , new Object[]{lpnInfo.get("ELOTTABLE08")},"供应商信息");
                if (supplierInfo == null) throw new Exception("未找到供应商" + lpnInfo.get("ELOTTABLE08") + "");

                suppilerName = supplierInfo.get("COMPANY");

            }

            //分装条码自动生成，生成规则：批号+F+001，分装物料批次号+F+3位流水。
            String newLpn = IdGenerationHelper.generateLpn( lottable06+"F");

            Map<String,String> receiptDetail=new HashMap<String,String>();
            receiptDetail.put("STORERKEY", storerKey);
            receiptDetail.put("SKU", lpnInfo.get("SKU"));
            receiptDetail.put("RECEIPTKEY", currentRepackReceiptKey);
            receiptDetail.put("TYPE", repackReceiptType);
            receiptDetail.put("RECEIPTLINENUMBER", receiptLineNumber);
            receiptDetail.put("EXTERNLINENO", "WMS"+receiptLineNumber);
            receiptDetail.put("GROSSWGTEXPECTED", trimZerosAndToStr(stdGrossWgtDecimal));
            receiptDetail.put("TAREWGTEXPECTED",  trimZerosAndToStr(stdTareWgtDecimal));
            receiptDetail.put("QTYEXPECTED", trimZerosAndToStr(stdNetWgtDecimal));
            receiptDetail.put("BARRELNUMBER", newLpn.substring(newLpn.length()-4)); //桶号
            receiptDetail.put("TOTALBARRELNUMBER", lpnInfo.get("TOTALBARREL")); //总桶数
            receiptDetail.put("SUSR2", newLpn.substring(newLpn.length()-4)); //桶号
            receiptDetail.put("SUSR3", lpnInfo.get("ELOTTABLE09")); //供应商批号
            receiptDetail.put("SUSR4", lpnInfo.get("ELOTTABLE08")); //供应商
            receiptDetail.put("SUSR5", suppilerName); //供应商名称
            receiptDetail.put("SUSR6", lpnInfo.get("PROJECTCODE"));
            receiptDetail.put("SUSR7",  "1");//是否开封
            receiptDetail.put("SUSR8",  grossWgt);//标签毛重量
            receiptDetail.put("SUSR9",  netWgt);//标签净重量
            receiptDetail.put("SUSR10",  tareWgt);//标签皮重量
            //receiptDetail.put("SUSR11",  uom);//不需要增加标签计量单位，收货行的计量单位即为用户实际选择的计量单位
            receiptDetail.put("ADDWHO", userid);
            receiptDetail.put("EDITWHO", userid);
            receiptDetail.put("UOM",uom);
            receiptDetail.put("PACKKEY",lpnInfo.get("PACKKEY"));
            receiptDetail.put("TOLOC", currentPackLoc);
            receiptDetail.put("TOID", newLpn);
            receiptDetail.put("CONDITIONCODE", "OK");
            receiptDetail.put("LOTTABLE01",  lpnInfo.get("LOTTABLE01"));
            receiptDetail.put("LOTTABLE02",  lpnInfo.get("LOTTABLE02"));//存货类型
            receiptDetail.put("ELOTTABLE03",  lpnInfo.get("ELOTTABLE03"));
            receiptDetail.put("LOTTABLE04", lpnInfo.get("LOTTABLE04"));
            receiptDetail.put("ELOTTABLE05", lpnInfo.get("ELOTTABLE05"));
            receiptDetail.put("LOTTABLE06", lottable06);
            receiptDetail.put("ELOTTABLE07", lpnInfo.get("ELOTTABLE07"));
            receiptDetail.put("ELOTTABLE08", lpnInfo.get("ELOTTABLE08"));
            receiptDetail.put("ELOTTABLE09", lpnInfo.get("ELOTTABLE09"));
            receiptDetail.put("LOTTABLE10", lpnInfo.get("LOTTABLE10")); //采购编码
            receiptDetail.put("ELOTTABLE11", lpnInfo.get("ELOTTABLE11"));
            receiptDetail.put("ELOTTABLE12", lpnInfo.get("ELOTTABLE12"));

            LegacyDBHelper.ExecInsert( "RECEIPTDETAIL", receiptDetail);

            //打印分装标签
            PrintHelper.printLPNByReceiptLineNumber(
                        receiptDetail.get("RECEIPTKEY"),
                        receiptDetail.get("RECEIPTLINENUMBER"),
                        Labels.LPN_REPACK,
                        printer, "1", "打印分装标签");

            Udtrn UDTRN=new Udtrn();

            if(esignatureKey.indexOf(':')==-1){
                UDTRN.EsignatureKey=esignatureKey;
            }else {
                //复核
                String[] eSignatureKeys = esignatureKey.split(":");
                UDTRN.EsignatureKey=eSignatureKeys[0];
                UDTRN.EsignatureKey1=eSignatureKeys[1];
            }

            UDTRN.FROMTYPE="生成分装标签";
            UDTRN.FROMTABLENAME="RECEIPT";
            UDTRN.FROMKEY=currentRepackReceiptKey;
            UDTRN.FROMKEY1LABEL="分装入库单号";
            UDTRN.FROMKEY1= currentRepackReceiptKey;
            UDTRN.FROMKEY2LABEL="分装单行号";
            UDTRN.FROMKEY2=receiptLineNumber;
            UDTRN.FROMKEY3="";
            UDTRN.TITLE01="原订单号";    UDTRN.CONTENT01=orderKey;
            UDTRN.TITLE02="原订单行号";    UDTRN.CONTENT02=orderLineNumber;
            UDTRN.TITLE03="分装间";    UDTRN.CONTENT03=currentPackLoc;
            UDTRN.TITLE04="标签容器号";    UDTRN.CONTENT04=newLpn;
            UDTRN.TITLE05="毛重/数量";    UDTRN.CONTENT05=grossWgt;
            UDTRN.TITLE06="净重/数量";    UDTRN.CONTENT06=netWgt;
            UDTRN.TITLE07="皮重/数量";    UDTRN.CONTENT07=tareWgt;


            UDTRN.insert( userid);


            ServiceDataMap theOutDO = new ServiceDataMap();
            theOutDO.setAttribValue("NEWLPN",newLpn);
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

}
