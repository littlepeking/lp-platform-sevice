package com.enhantec.wms.backend.utils.print;

import com.alibaba.fastjson.JSON;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.LotxId;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

public class PrintHelper {

    public static void printLPNByIDNotes(Context context, Connection conn, String id, String labelName, String printer, String copies, String notes) throws Exception {

        HashMap<String, String> idNotes =  IDNotes.findById(context,conn,id,true);

        HashMap<String,String> printParams = new HashMap<>();
        printParams.put("IDNOTES.ID",id);
        List<HashMap<String,String>> printData = PrintUtil.getData(context,conn,labelName,printParams);

        String labelSuffix = getLabelSuffixBySku(context,conn, idNotes.get("SKU"));

        String labelTemplateBaseName = LabelConfig.getLabelTemplatePrefix(context,conn,labelName);

        PrintUtil.printLabel(context,conn,printer, labelTemplateBaseName+labelSuffix,labelName,copies,notes,printData,id);

    }

    public static void removePrintTaskByIDNotes(Context context, Connection conn,String labelName, String id) throws Exception {
        DBHelper.executeUpdate(context,conn,"DELETE FROM PRINT_TASK WHERE LABELNAME = ? AND [KEY] = ? AND PRINTSTATUS = -1 ", new Object[]{
                labelName,
                id
        });
    }

    public static void removePrintTaskByReceiptDetail(Context context, Connection connection, String labelName, String receiptKey, String receiptLineNumber) throws Exception {
        DBHelper.executeUpdate(context, connection,
                "DELETE FROM PRINT_TASK WHERE LABELNAME = ? AND [KEY] = ? AND PRINTSTATUS = -1 ",
                new Object[]{
                       labelName,
                       receiptKey + receiptLineNumber
                });
    }

    public static void printLPNByReceiptKey(Context context, Connection conn, String receiptKey,String labelName, String printer, String copies,String notes) throws Exception {

        List<HashMap<String, String>> receiptDetails = Receipt.findReceiptDetails(context,conn,receiptKey ,true);

        HashMap<String, String> printParams = new HashMap<>();

        printParams.put("RECEIPT.RECEIPTKEY", receiptKey);

        List<HashMap<String,String>> printData = PrintUtil.getData(context,conn,labelName,printParams);
        //采购收货的SKU相同
        String labelSuffix = getLabelSuffixBySku(context,conn, receiptDetails.get(0).get("SKU"));

        String labelTemplateBaseName = LabelConfig.getLabelTemplatePrefix(context,conn,labelName);

        PrintUtil.printLabel(context,conn,printer, labelTemplateBaseName+labelSuffix,labelName,copies,notes,printData,receiptKey);

    }

    public static void printLPNByReceiptLineNumber(Context context, Connection conn, String receiptKey,String receiptLineNumber ,String labelName, String printer, String copies,String notes) throws Exception {

        HashMap<String, String> receiptDetail =  Receipt.findReceiptDetailById(context,conn,receiptKey, receiptLineNumber ,true);

        HashMap<String,String> printParams = new HashMap<>();
        printParams.put("RECEIPTDETAIL.RECEIPTKEY",receiptKey);
        printParams.put("RECEIPTDETAIL.RECEIPTLINENUMBER",receiptLineNumber);
        List<HashMap<String,String>> printData = PrintUtil.getData(context,conn,labelName,printParams);

        String labelSuffix = getLabelSuffixBySku(context,conn, receiptDetail.get("SKU"));

        String labelTemplateBaseName = LabelConfig.getLabelTemplatePrefix(context,conn,labelName);

        PrintUtil.printLabel(context,conn,printer, labelTemplateBaseName+labelSuffix,labelName,copies,notes,printData,receiptKey+receiptLineNumber);

    }

    public static void printSamplingLpnLabel(Context context, Connection conn, String orderKey,String labelName, String printer, String copies,String notes) throws Exception {

        List<HashMap<String,String>>  orderDetails = Orders.findOrderDetailsByOrderKey(context, conn, orderKey, true);

        HashMap<String,String> printParams = new HashMap<>();
        printParams.put("ORDERS.ORDERKEY",orderKey);
        List<HashMap<String,String>> printData = PrintUtil.getData(context,conn,labelName,printParams);

        //取样单上的SKU和收货批次相同，取第一个即可
        String labelSuffix = getLabelSuffixBySku(context,conn, orderDetails.get(0).get("SKU"));

        String labelTemplateBaseName = LabelConfig.getLabelTemplatePrefix(context,conn,labelName);

        PrintUtil.printLabel(context,conn,printer, labelTemplateBaseName+labelSuffix,labelName,copies,notes,printData,orderKey);

    }

    /**
     * 这里只拼接SKU类型后缀，打印时根据打印机ID拼接对应的纸张类型
     * @param context
     * @param conn
     * @param sku
     * @return
     * @throws Exception
     */
    private static String getLabelSuffixBySku(Context context, Connection conn, String sku) throws Exception {


        HashMap<String,String> skuHashMap = SKU.findById(context,conn,sku,true);

        String labelSuffix ="";

        if(!UtilHelper.isEmpty(skuHashMap.get("BUSR4"))) {

            HashMap<String, String> codeLkup = CodeLookup.getCodeLookupByKey(context, conn, "SKUTYPE1", skuHashMap.get("BUSR4"));

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
    public static void printSnByIdnotes(Context context,Connection connection,String id, String labelName, String printer, String printCopies,String notes) throws Exception {
        HashMap<String, String> idNotes =  IDNotes.findById(context,connection,id,true);

        HashMap<String,String> printParams = new HashMap<>();
        printParams.put("IDNOTES.ID",id);
        List<HashMap<String,String>> printData = PrintUtil.getData(context,connection,labelName,printParams);

        String labelSuffix = getLabelSuffixBySku(context,connection, idNotes.get("SKU"));

        String labelTemplateBaseName = LabelConfig.getLabelTemplatePrefix(context,connection,labelName);

        PrintUtil.printLabel(context,connection,printer, labelTemplateBaseName+labelSuffix,labelName,printCopies,notes,printData,id);
    }

    /**
     * 根据LPN打印最后一次收货的唯一码信息
     */
    public static void rePrintSnByLPN(Context context,Connection connection,String id,String labelName,String printer,String printCopies,String notes)throws Exception{
        HashMap<String, String> lastReceiptDetailByLPN = Receipt.findLastReceiptDetailByLPN(context, connection, id, true);

        HashMap<String,String> printParams = new HashMap<>();
        printParams.put("RECEIPTDETAIL.TOID",id);
        printParams.put("RECEIPTDETAIL.RECEIPTKEY",lastReceiptDetailByLPN.get("RECEIPTKEY"));
        List<HashMap<String,String>> printData = PrintUtil.getData(context,connection,labelName,printParams);

        String labelSuffix = getLabelSuffixBySku(context,connection, lastReceiptDetailByLPN.get("SKU"));

        String labelTemplateBaseName = LabelConfig.getLabelTemplatePrefix(context,connection,labelName);

        PrintUtil.printLabel(context,connection,printer, labelTemplateBaseName+labelSuffix,labelName,printCopies,notes,printData,id);
    }

    /**
     * 根据入库明细打印sn标签
     */
    public static void printSnByReceiptLineNumber(Context context,Connection connection,String receiptKey,String receiptLineNumber,String labelName,String printer,String printCopies,String notes)throws Exception{
        HashMap<String, String> receiptDetail =  Receipt.findReceiptDetailById(context,connection,receiptKey, receiptLineNumber ,true);

        HashMap<String,String> printParams = new HashMap<>();
        printParams.put("RECEIPTDETAIL.RECEIPTKEY",receiptKey);
        printParams.put("RECEIPTDETAIL.RECEIPTLINENUMBER",receiptLineNumber);
        List<HashMap<String,String>> printData = PrintUtil.getData(context,connection,labelName,printParams);

        String labelSuffix = getLabelSuffixBySku(context,connection, receiptDetail.get("SKU"));

        String labelTemplateBaseName = LabelConfig.getLabelTemplatePrefix(context,connection,labelName);

        PrintUtil.printLabel(context,connection,printer, labelTemplateBaseName+labelSuffix,labelName,printCopies,notes,printData,receiptKey+","+receiptLineNumber);

    }

    /**
     * 根据收货行和唯一码打印唯一码收货标签
     */
    public static void printSnByReceiptLineNumberAndSn(Context context, Connection connection,String receiptKey,String receiptLineNumber,String serialNumber,String labelName,String printer,String printCopies,String notes)throws Exception{
        HashMap<String, String> receiptDetail =  Receipt.findReceiptDetailById(context,connection,receiptKey, receiptLineNumber ,true);
        LotxId.findDetailByReceiptLineAndSn(context,connection,receiptKey,receiptLineNumber,serialNumber,true);

        HashMap<String,String> printParams = new HashMap<>();
        printParams.put("RECEIPTDETAIL.RECEIPTKEY",receiptKey);
        printParams.put("RECEIPTDETAIL.RECEIPTLINENUMBER",receiptLineNumber);
        printParams.put("LOTXIDDETAIL.SERIALNUMBERLONG",serialNumber);
        printParams.put("LOTXIDDETAIL.IOFLAG","I");
        List<HashMap<String,String>> printData = PrintUtil.getData(context,connection,labelName,printParams);

        String labelSuffix = getLabelSuffixBySku(context,connection, receiptDetail.get("SKU"));

        String labelTemplateBaseName = LabelConfig.getLabelTemplatePrefix(context,connection,labelName);

        PrintUtil.printLabel(context,connection,printer, labelTemplateBaseName+labelSuffix,labelName,printCopies,notes,printData,receiptKey+receiptLineNumber);

    }


    /**
     * 根据容器条码打印标签，如果任务已存在且在缓存打印列表中，则更新任务
     */
    public static void printOrUpdateTaskLPNByIDNotes(Context context, Connection conn, String id,String labelName, String printer, String copies,String notes) throws Exception{
        Integer existPrintTaskCount = DBHelper.getValue(context, conn,
                "SELECT COUNT(*) FROM PRINT_TASK WHERE [KEY] = ? AND PRINTSTATUS = '-1' AND LABELNAME = ?",
                new Object[]{id,labelName}, Integer.class, "");
        if(existPrintTaskCount > 0){
            updateTaskInfoById(context,conn,labelName,id);
        }else{
            printLPNByIDNotes(context,conn,id,labelName,printer,copies,notes);
        }
    }

    private static void updateTaskInfoById(Context context,Connection connection,String labelName,String id){
        HashMap<String, String> idNotes =  IDNotes.findById(context,connection,id,true);

        HashMap<String,String> printParams = new HashMap<>();
        printParams.put("IDNOTES.ID",id);
        List<HashMap<String,String>> printData = PrintUtil.getData(context,connection,labelName,printParams);

        StringBuffer printDataString = new StringBuffer();
        printDataString.append("[");
        String mapJson = JSON.toJSONString(printData);
        printDataString.append(mapJson);
        printDataString.append("]");

        DBHelper.executeUpdate(context,connection,
                "UPDATE PRINT_TASK SET PRINTDATA = ? WHERE [KEY] = ? AND PRINTSTATUS = '-1' AND LABELNAME = ?",
                new Object[]{printDataString.toString(),id,labelName});
    }

}
