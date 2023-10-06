package com.enhantec.wms.backend.inbound.asn;

import com.alibaba.fastjson.JSONObject;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.*;
import com.enhantec.wms.backend.common.base.code.CDReceiptType;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.WMSBaseService;import com.enhantec.framework.common.utils.EHContextHelper;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;
import com.enhantec.wms.backend.utils.print.Labels;
import com.enhantec.wms.backend.utils.print.PrintHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * --注册方法
 DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHReturnWithASNAdd'
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHReturnWithASNAdd', 'com.enhantec.sce.inbound.asn', 'enhantec', 'ReturnWithASNAdd', 'TRUE', 'john', 'john',
 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,LPN,ORIGINLPN,RECEIPTKEY,RECEIPTLINENUMBER,RECTYPE,SKU,GROSSWGT,NETWGT,TAREWGT,REGROSSWGT,UOM,SNLIST,ISOPENED,ESIGNATUREKEY,PRINTER','0.10','0');
 */
public class ReturnWithASNAdd extends WMSBaseService {
    private static final long serialVersionUID = 1L;
    //默认收货库位
    private static final String DEFAULT_RECEIPT_LOC = "STAGE";

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {


        String lpn = serviceDataHolder.getInputDataAsMap().getString("LPN");//绑定的新LPN
        String originLpn = serviceDataHolder.getInputDataAsMap().getString("ORIGINLPN");//扫描的LPN
        String receiptKey = serviceDataHolder.getInputDataAsMap().getString("RECEIPTKEY");
        String originalReceiptLineNumber = serviceDataHolder.getInputDataAsMap().getString("RECEIPTLINENUMBER");
        String receiptType= serviceDataHolder.getInputDataAsMap().getString("RECTYPE");
        String sku = serviceDataHolder.getInputDataAsMap().getString("SKU");
        String netWgt = serviceDataHolder.getInputDataAsMap().getString("NETWGT");
        String grossWgt = serviceDataHolder.getInputDataAsMap().getString("GROSSWGT");
        String tareWgt = serviceDataHolder.getInputDataAsMap().getString("TAREWGT");
        String reGrossWgt = serviceDataHolder.getInputDataAsMap().getString("REGROSSWGT");//复称重量
        String uom = serviceDataHolder.getInputDataAsMap().getString("UOM");
        String snListStr = serviceDataHolder.getInputDataAsMap().getString("SNLIST");
        String isOpened= serviceDataHolder.getInputDataAsMap().getString("ISOPENED");
        String esignatureKey= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");
        String printer = serviceDataHolder.getInputDataAsMap().getString("PRINTER");

        String toLoc = DEFAULT_RECEIPT_LOC;

        try{

            if (UtilHelper.isEmpty(grossWgt)) throw new Exception("未成功获取毛重数据");
            if (UtilHelper.isEmpty(tareWgt)) throw new Exception("未成功获取皮重数据");
            if (UtilHelper.isEmpty(netWgt)) throw new Exception("未成功获取净重数据");

            Map<String, String> receiptHashMap = buildReceiptHeader(receiptKey,receiptType);


            if(!UtilHelper.isEmpty(originalReceiptLineNumber)) {
                Map<String, String> originalReceiptDetailHashMap = Receipt.findReceiptDetailByLineNumber( receiptKey, originalReceiptLineNumber, true);
                if(!(new BigDecimal(originalReceiptDetailHashMap.get("QTYRECEIVED")).compareTo(new BigDecimal(0)) == 0))
                    ExceptionHelper.throwRfFulfillLogicException("收货指令行的已收货数量必须为0");
                toLoc = originalReceiptDetailHashMap.get("TOLOC");
            }
            //退货入库在没有指令行的情况下，如果收货类型配置了默认收货库位，会使用配置的收货库位。
            String toLocConf = CodeLookup.getCodeLookupByKey( "RECEIPTYPE", receiptType).get("UDF9");
            if(!UtilHelper.isEmpty(toLocConf)){
                toLoc = toLocConf;
            }
            Map<String, String> skuMap = SKU.findById( sku, true);

            String packKey = skuMap.get("PACKKEY");

            //插入收货行的应为STD UOM,但是RF传入的是UOM QTY,需要转换
            BigDecimal grossWgtStdQty = UOM.UOMQty2StdQty(packKey, uom, new BigDecimal(grossWgt));
            BigDecimal tareWgtStdQQty = UOM.UOMQty2StdQty(packKey, uom, new BigDecimal(tareWgt));
            BigDecimal netWgtStdQQty = UOM.UOMQty2StdQty(packKey, uom, new BigDecimal(netWgt));

            ArrayList<String> snArray = new ArrayList<>();
            ArrayList<String> snWightArray = new ArrayList<>();
            ArrayList<String> snUomArray = new ArrayList<>();

            if(!UtilHelper.isEmpty(snListStr)){
                String snUom = skuMap.get("SNUOM");
                JSONObject snJsonObject = JSONObject.parseObject(snListStr);
                for (String sn : snJsonObject.keySet()) {
                    snArray.add(sn);
                    snWightArray.add(snJsonObject.getString(sn));
                    snUomArray.add(snUom);
                }
            }

            Map<String,String> insertedReceiptDetail = Receipt.insertReceiptDetailByReturnLpn( receiptHashMap, originalReceiptLineNumber,sku,lpn,originLpn, toLoc,isOpened
                    ,netWgtStdQQty.toPlainString(),grossWgtStdQty.toPlainString(),tareWgtStdQQty.toPlainString(),uom,reGrossWgt, snArray.toArray(new String[snArray.size()]),snWightArray.toArray(new String[snWightArray.size()]), snUomArray.toArray(new String[snUomArray.size()]));
            /**
             * 唯一码控制的打印LPN和SN标签
             * 是否自动生成箱号并打印还是扫描输入（该配置仅用于唯一码退货，按批次的容器条码收货直接生成子容器号）
             * 该设置仅在isBindingNewLpn生效时有效，如果不启用isBindingNewLpn，扫描每个唯一码生成的LPN则直接使用数字流水码，以和唯一码的箱号、批次管理的容器条码的生成规则进行区分
             *
             * 非唯一码控制的打印LPN标签
             * 标签显示重量且发生重量变化的时候打印
             * TODO:完善标签是否显示LPN重量配置，目前只是拣货打印剩余量标签使用。
             *  应改为在任何情况下当LPN数量发生变化的时候，需要重新打印物料标签。
             *  适用于所有收发货和库存数量调整的逻辑。
             */
            boolean printLable = false;
            if(SKU.isSerialControl(sku)) {
                if (CDReceiptType.isBindAndAutoGenerateLpn( sku, receiptType)) {
                    PrintHelper.printLPNByReceiptLineNumber(
                            insertedReceiptDetail.get("RECEIPTKEY"),
                            insertedReceiptDetail.get("RECEIPTLINENUMBER"),
                            Labels.LPN_UI,
                            printer, "1", "ASN退货标签");
                    printLable = true;
                }
                if(CDSysSet.snLabelWgt()) {
                    for (int i = 0; i < snArray.size(); i++) {
                        String sn = snArray.get(i);
                        String newSnWeight = snWightArray.get(i);
                        String oldSnWeight = SNHistory.findBySkuAndSN( insertedReceiptDetail.get("SKU"), sn, true).get("SNWEIGHT");
                        if(UtilHelper.decimalStrCompare(newSnWeight,oldSnWeight) != 0){
                            PrintHelper.printSnByReceiptLineNumberAndSn(
                                    insertedReceiptDetail.get("RECEIPTKEY"),
                                    insertedReceiptDetail.get("RECEIPTLINENUMBER"),
                                    sn, Labels.SN_UI,
                                    printer, "1", "ASN退货唯一码标签");
                            printLable = true;
                        }
                    }
                }
            } else if (CDSysSet.enableLabelWgt()) {
                Map<String, String> idNotesHistory = IDNotesHistory.findLastShippedRecordById( insertedReceiptDetail.get("TOID"), true);
                String originalNetWgt = idNotesHistory.get("ORIGINALNETWGT");
                String expectedNetWgt = insertedReceiptDetail.get("QTYEXPECTED");

                if(UtilHelper.decimalStrCompare(originalNetWgt,expectedNetWgt) != 0){
                    PrintHelper.printLPNByReceiptLineNumber(
                            insertedReceiptDetail.get("RECEIPTKEY"),
                            insertedReceiptDetail.get("RECEIPTLINENUMBER"),
                            Labels.LPN_UI,
                            printer, "1", "ASN退货标签");
                    printLable = true;
                }
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
            udtrn.insert(EHContextHelper.getUser().getUsername());

            ServiceDataMap theOutDO = new ServiceDataMap();
            //扫描单个唯一码情况，返回唯一码
            if(UtilHelper.isEmpty(insertedReceiptDetail.get("SERIALNUMBER"))) {
                theOutDO.setAttribValue("TOID", insertedReceiptDetail.get("TOID"));
            }else {
                theOutDO.setAttribValue("TOID", insertedReceiptDetail.get("SERIALNUMBER"));
            }
            theOutDO.setAttribValue("RECEIPTKEY", insertedReceiptDetail.get("RECEIPTKEY"));
            theOutDO.setAttribValue("EXTERNRECEIPTKEY", insertedReceiptDetail.get("EXTERNRECEIPTKEY"));
            theOutDO.setAttribValue("PRINTLABEL",printLable);
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

    private Map<String,String> buildReceiptHeader( String receiptKey, String receiptType) throws Exception {

        Map<String, String> receiptHashMap = null;

        String exReceiptKey = "";

        String userid = EHContextHelper.getUser().getUsername();

        if(UtilHelper.isEmpty(receiptKey)) {
            receiptKey = LegacyDBHelper.GetNCounterBill( "RECEIPT");
            Map<String,String> newReceiptHashMap = new HashMap<String,String>();
            newReceiptHashMap.put("ADDWHO", userid);
            newReceiptHashMap.put("EDITWHO", userid);
            newReceiptHashMap.put("RECEIPTKEY", receiptKey);
            //生成退货单号
            exReceiptKey = IdGenerationHelper.generateID( "RET", 10);
            newReceiptHashMap.put("EXTERNRECEIPTKEY", exReceiptKey);
            newReceiptHashMap.put("STATUS", "0");
            newReceiptHashMap.put("ALLOWAUTORECEIPT", "0");
            newReceiptHashMap.put("TYPE", receiptType);
            if(CDReceiptType.isAutoReceiving( receiptType)) {
                newReceiptHashMap.put("ISCONFIRMED", "2");
                newReceiptHashMap.put("ISCONFIRMEDUSER", userid);
                newReceiptHashMap.put("ISCONFIRMEDUSER2",userid);

            }else {
                newReceiptHashMap.put("ISCONFIRMED", "0"); //完成收货信息采集后，进行签名后改为2。
            }

            String STORERKEY = DBHelper.getStringValue( "select udf1 from codelkup where listname=? and code=?", new String[]{"SYSSET", "STORERKEY"}, "");
            newReceiptHashMap.put("STORERKEY", STORERKEY);
            LegacyDBHelper.ExecInsert( "RECEIPT", newReceiptHashMap);

            receiptHashMap = newReceiptHashMap;

        }else {
            receiptHashMap = Receipt.findByReceiptKey(receiptKey,true);

            //无ASN收货对于扫描直接收货的情况,收货后应该允许继续收货，因此去掉发运状态的限制，收货完成后关闭ASN后不允许继续收货
            if( //receipt.get("STATUS").equals("9") ||
                    receiptHashMap.get("STATUS").equals("11") || receiptHashMap.get("STATUS").equals("15") || receiptHashMap.get("STATUS").equals("20")){
                throw new Exception("收货单已关闭,不允许操作");
            }

        }

        return receiptHashMap;

    }

}