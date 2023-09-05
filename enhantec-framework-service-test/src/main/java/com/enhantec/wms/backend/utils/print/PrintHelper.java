package com.enhantec.wms.backend.utils.print;

import com.alibaba.fastjson.JSON;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.LotxId;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class PrintHelper {

    public static void printLPNByIDNotes( String id, String labelName, String printer, String copies, String notes) throws Exception {

        Map<String, String> idNotes =  IDNotes.findById(id,true);

        Map<String,String> printParams = new HashMap<>();
        printParams.put("IDNOTES.ID",id);
        List<Map<String,String>> printData = PrintUtil.getData(labelName,printParams);

        String labelSuffix = getLabelSuffixBySku( idNotes.get("SKU"));

        String labelTemplateBaseName = LabelConfig.getLabelTemplatePrefix(labelName);

        PrintUtil.printLabel(printer, labelTemplateBaseName+labelSuffix,labelName,copies,notes,printData,id);

    }

    public static void removePrintTaskByIDNotes(String labelName, String id) throws Exception {
        DBHelper.executeUpdate("DELETE FROM PRINT_TASK WHERE LABELNAME = ? AND [KEY] = ? AND PRINTSTATUS = -1 ", new Object[]{
                labelName,
                id
        });
    }

    public static void removePrintTaskByReceiptDetail( String labelName, String receiptKey, String receiptLineNumber) throws Exception {
        DBHelper.executeUpdate(
                "DELETE FROM PRINT_TASK WHERE LABELNAME = ? AND [KEY] = ? AND PRINTSTATUS = -1 ",
                new Object[]{
                       labelName,
                       receiptKey + receiptLineNumber
                });
    }

    public static void printLPNByReceiptKey( String receiptKey,String labelName, String printer, String copies,String notes) throws Exception {

        List<Map<String, String>> receiptDetails = Receipt.findReceiptDetails(receiptKey ,true);

        Map<String, String> printParams = new HashMap<>();

        printParams.put("RECEIPT.RECEIPTKEY", receiptKey);

        List<Map<String,String>> printData = PrintUtil.getData(labelName,printParams);
        //采购收货的SKU相同
        String labelSuffix = getLabelSuffixBySku( receiptDetails.get(0).get("SKU"));

        String labelTemplateBaseName = LabelConfig.getLabelTemplatePrefix(labelName);

        PrintUtil.printLabel(printer, labelTemplateBaseName+labelSuffix,labelName,copies,notes,printData,receiptKey);

    }

    public static void printLPNByReceiptLineNumber( String receiptKey,String receiptLineNumber ,String labelName, String printer, String copies,String notes) throws Exception {

        Map<String, String> receiptDetail =  Receipt.findReceiptDetailByLineNumber(receiptKey, receiptLineNumber ,true);

        Map<String,String> printParams = new HashMap<>();
        printParams.put("RECEIPTDETAIL.RECEIPTKEY",receiptKey);
        printParams.put("RECEIPTDETAIL.RECEIPTLINENUMBER",receiptLineNumber);
        List<Map<String,String>> printData = PrintUtil.getData(labelName,printParams);

        String labelSuffix = getLabelSuffixBySku( receiptDetail.get("SKU"));

        String labelTemplateBaseName = LabelConfig.getLabelTemplatePrefix(labelName);

        PrintUtil.printLabel(printer, labelTemplateBaseName+labelSuffix,labelName,copies,notes,printData,receiptKey+receiptLineNumber);

    }

    public static void printSamplingLpnLabel( String orderKey,String labelName, String printer, String copies,String notes) throws Exception {

        List<Map<String,String>>  orderDetails = Orders.findOrderDetailsByOrderKey( orderKey, true);

        Map<String,String> printParams = new HashMap<>();
        printParams.put("ORDERS.ORDERKEY",orderKey);
        List<Map<String,String>> printData = PrintUtil.getData(labelName,printParams);

        //取样单上的SKU和收货批次相同，取第一个即可
        String labelSuffix = getLabelSuffixBySku( orderDetails.get(0).get("SKU"));

        String labelTemplateBaseName = LabelConfig.getLabelTemplatePrefix(labelName);

        PrintUtil.printLabel(printer, labelTemplateBaseName+labelSuffix,labelName,copies,notes,printData,orderKey);

    }

    /**
     * 这里只拼接SKU类型后缀，打印时根据打印机ID拼接对应的纸张类型


     * @param sku
     * @return
     * @throws Exception
     */
    private static String getLabelSuffixBySku( String sku) throws Exception {


        Map<String,String> skuHashMap = SKU.findById(sku,true);

        String labelSuffix ="";

        if(!UtilHelper.isEmpty(skuHashMap.get("BUSR4"))) {

            Map<String, String> codeLkup = CodeLookup.getCodeLookupByKey( "SKUTYPE1", skuHashMap.get("BUSR4"));

            labelSuffix = UtilHelper.isEmpty(codeLkup.get("UDF1")) ? "" : "_" + codeLkup.get("UDF1");

        }else{
            ExceptionHelper.throwRfFulfillLogicException("标签打印:物料类型不允许为空");
            //labelSuffix += "_DEFAULT";
        }

        return labelSuffix;
    }

    /**
     * 根据箱号打印唯一码标签
     */
    public static void printSnByIdnotes(String id, String labelName, String printer, String printCopies,String notes) throws Exception {
        Map<String, String> idNotes =  IDNotes.findById(id,true);

        Map<String,String> printParams = new HashMap<>();
        printParams.put("IDNOTES.ID",id);
        List<Map<String,String>> printData = PrintUtil.getData(labelName,printParams);

        String labelSuffix = getLabelSuffixBySku( idNotes.get("SKU"));

        String labelTemplateBaseName = LabelConfig.getLabelTemplatePrefix(labelName);

        PrintUtil.printLabel(printer, labelTemplateBaseName+labelSuffix,labelName,printCopies,notes,printData,id);
    }

    /**
     * 根据LPN打印最后一次收货的唯一码信息
     */
    public static void rePrintSnByLPN(String id,String labelName,String printer,String printCopies,String notes)throws Exception{
        Map<String, String> lastReceiptDetailByLPN = Receipt.findLastReceiptDetailByLPN( id, true);

        Map<String,String> printParams = new HashMap<>();
        printParams.put("RECEIPTDETAIL.TOID",id);
        printParams.put("RECEIPTDETAIL.RECEIPTKEY",lastReceiptDetailByLPN.get("RECEIPTKEY"));
        List<Map<String,String>> printData = PrintUtil.getData(labelName,printParams);

        String labelSuffix = getLabelSuffixBySku( lastReceiptDetailByLPN.get("SKU"));

        String labelTemplateBaseName = LabelConfig.getLabelTemplatePrefix(labelName);

        PrintUtil.printLabel(printer, labelTemplateBaseName+labelSuffix,labelName,printCopies,notes,printData,id);
    }

    /**
     * 根据入库明细打印sn标签
     */
    public static void printSnByReceiptLineNumber(String receiptKey,String receiptLineNumber,String labelName,String printer,String printCopies,String notes)throws Exception{
        Map<String, String> receiptDetail =  Receipt.findReceiptDetailByLineNumber(receiptKey, receiptLineNumber ,true);

        Map<String,String> printParams = new HashMap<>();
        printParams.put("RECEIPTDETAIL.RECEIPTKEY",receiptKey);
        printParams.put("RECEIPTDETAIL.RECEIPTLINENUMBER",receiptLineNumber);
        List<Map<String,String>> printData = PrintUtil.getData(labelName,printParams);

        String labelSuffix = getLabelSuffixBySku( receiptDetail.get("SKU"));

        String labelTemplateBaseName = LabelConfig.getLabelTemplatePrefix(labelName);

        PrintUtil.printLabel(printer, labelTemplateBaseName+labelSuffix,labelName,printCopies,notes,printData,receiptKey+","+receiptLineNumber);

    }

    /**
     * 根据收货行和唯一码打印唯一码收货标签
     */
    public static void printSnByReceiptLineNumberAndSn(String receiptKey,String receiptLineNumber,String serialNumber,String labelName,String printer,String printCopies,String notes)throws Exception{
        Map<String, String> receiptDetail =  Receipt.findReceiptDetailByLineNumber(receiptKey, receiptLineNumber ,true);
        LotxId.findDetailByReceiptLineAndSn(receiptKey,receiptLineNumber,serialNumber,true);

        Map<String,String> printParams = new HashMap<>();
        printParams.put("RECEIPTDETAIL.RECEIPTKEY",receiptKey);
        printParams.put("RECEIPTDETAIL.RECEIPTLINENUMBER",receiptLineNumber);
        printParams.put("LOTXIDDETAIL.SERIALNUMBERLONG",serialNumber);
        printParams.put("LOTXIDDETAIL.IOFLAG","I");
        List<Map<String,String>> printData = PrintUtil.getData(labelName,printParams);

        String labelSuffix = getLabelSuffixBySku( receiptDetail.get("SKU"));

        String labelTemplateBaseName = LabelConfig.getLabelTemplatePrefix(labelName);

        PrintUtil.printLabel(printer, labelTemplateBaseName+labelSuffix,labelName,printCopies,notes,printData,receiptKey+receiptLineNumber);

    }


    /**
     * 根据容器条码打印标签，如果任务已存在且在缓存打印列表中，则更新任务
     */
    public static void printOrUpdateTaskLPNByIDNotes( String id,String labelName, String printer, String copies,String notes) throws Exception{
        Integer existPrintTaskCount = DBHelper.getValue( "SELECT COUNT(*) FROM PRINT_TASK WHERE [KEY] = ? AND PRINTSTATUS = '-1' AND LABELNAME = ?",
                new Object[]{id,labelName}, Integer.class, "");
        if(existPrintTaskCount > 0){
            updateTaskInfoById(labelName,id);
        }else{
            printLPNByIDNotes(id,labelName,printer,copies,notes);
        }
    }

    private static void updateTaskInfoById(String labelName,String id){
        Map<String, String> idNotes =  IDNotes.findById(id,true);

        Map<String,String> printParams = new HashMap<>();
        printParams.put("IDNOTES.ID",id);
        List<Map<String,String>> printData = PrintUtil.getData(labelName,printParams);

        StringBuffer printDataString = new StringBuffer();
        printDataString.append("[");
        String mapJson = JSON.toJSONString(printData);
        printDataString.append(mapJson);
        printDataString.append("]");

        DBHelper.executeUpdate(
                "UPDATE PRINT_TASK SET PRINTDATA = ? WHERE [KEY] = ? AND PRINTSTATUS = '-1' AND LABELNAME = ?",
                new Object[]{printDataString.toString(),id,labelName});
    }

}
