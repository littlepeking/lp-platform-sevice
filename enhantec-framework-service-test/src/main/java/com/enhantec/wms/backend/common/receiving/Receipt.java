package com.enhantec.wms.backend.common.receiving;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.common.IdGenerationHelper;
import com.enhantec.wms.backend.utils.common.ServiceHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import com.enhantec.wms.backend.common.Const;
import com.enhantec.wms.backend.common.base.*;
import com.enhantec.wms.backend.common.base.code.CDReceiptType;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import static com.enhantec.wms.backend.utils.common.UtilHelper.nvl;
import static com.enhantec.wms.backend.common.base.LotxId.buildReceiptLotxIdInfo;
public class Receipt {

    public static HashMap<String, String> findByReceiptKey(Context context, String receiptKey, boolean checkExist) {

        String SQL="SELECT * FROM RECEIPT WHERE  receiptKey = ?  ";
        HashMap<String, String>  record = DBHelper.getRecord(context, SQL, new Object[]{ receiptKey},"收货单");
        if(checkExist && record == null ) ExceptionHelper.throwRfFulfillLogicException("收货单为"+receiptKey+"未找到");
        return record;
    }

    public static HashMap<String, String> findReceiptDetailByLPN(Context context,String receiptKey, String lpn, boolean checkExist) {

        String SQL="SELECT * FROM RECEIPTDETAIL WHERE RECEIPTKEY = ?  AND TOID = ?";
        HashMap<String,String> record= DBHelper.getRecord(context, SQL, new Object[]{receiptKey, lpn},"收货明细");
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("容器条码为"+lpn+"的收货明细未找到");
        return record;
    }

    /**
     * 查询最后被收货的LPN的收货单信息
     */
    public static HashMap<String,String> findLastReceiptDetailByLPN(Context context,String lpn,boolean checkExist){
        String sql = "SELECT TOP 1 * FROM RECEIPTDETAIL WHERE TOID = ? ORDER BY EDITDATE DESC";
        HashMap<String, String> record = DBHelper.getRecord(context, sql, new Object[]{lpn}, "收货明细");
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("容器条码为"+lpn+"的收货明细未找到");
        return record;
    }


    public static HashMap<String, String> findReceiptDetailById(Context context, String receiptKey, String receiptLineNumber, boolean checkExist) {

        String SQL="SELECT * FROM RECEIPTDETAIL WHERE  receiptKey = ? and receiptLineNumber = ? ";
        HashMap<String,String> record= DBHelper.getRecord(context, SQL, new Object[]{ receiptKey,receiptLineNumber},"收货明细");
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("收货单为"+receiptKey+receiptLineNumber+"的收货明细未找到");
        return record;
    }

    public static List<HashMap<String, String>> findReceiptDetails(Context context, String receiptKey, boolean checkExist) {

        String SQL="SELECT * FROM RECEIPTDETAIL WHERE  receiptKey = ?  ";
        List<HashMap<String, String>>  list = DBHelper.executeQuery(context, SQL, new Object[]{ receiptKey});
        if(checkExist && list.size() == 0 ) ExceptionHelper.throwRfFulfillLogicException("收货单为"+receiptKey+"的收货明细未找到");
        return list;
    }

    /**
     * 更新ASN出库单的状态
     * @param context

     * @param receiptKey
     * @param status 0-新建，5-收货中，9-收货完成，11-已结。
     */
    public static void updateReceiptStatus(Context context,String receiptKey, String status){
        String sql = "UPDATE RECEIPT SET STATUS = '"+status+"' WHERE RECEIPTKEY = ?";
        DBHelper.executeUpdate(context,sql,new Object[]{receiptKey});
    }

    /**
     * 更新ASN出库单行的状态
     * @param context

     * @param receiptKey
     * @param receiptLineNumber
     * @param status status 0-新建，5-收货中，9-收货完成，11-已结。
     */
    public static void updateReceiptDetailStatus(Context context,String receiptKey,String receiptLineNumber,String status){
        String sql = "UPDATE RECEIPTDETAIL SET STATUS = '"+status+"' WHERE RECEIPTKEY = ? AND RECEIPTLINENUMBER = ?";
        DBHelper.executeUpdate(context,sql,new Object[]{receiptKey,receiptLineNumber});
    }

    /**
     * 插入退货待收货明细行
     * 当前业务支持如下4个场景：
     * 1.多个唯一码绑定箱号: lpn和snList 有值
     * 2.扫描唯一码直接收货：snList 有值
     * 3.唯一码物料整箱退库：originLpn 有值
     * 4.批次管理的物料退库：originLpn 有值
     * @throws Exception
     */
    public static HashMap<String,String> insertReceiptDetailByReturnLpn(
            Context context, HashMap<String,String> receiptHashMap, String receiptLinuNumber,
            String sku, String newLpn, String originLpn, String loc, String isOpened,
            String netStdWgt, String grossStdWgt, String tareStdWgt, String uom, String regrossWgt,
            String[] snList, String[] snWightList, String[] snUomList)throws Exception {

        //只有唯一码的情况，绑定的newLpn才有值，校验其是否符合收货要求
        if(!UtilHelper.isEmpty(newLpn)) {

            HashMap<String, String> receiptDetailInfo = DBHelper.getRecord(context,
                    "SELECT RECEIPTKEY FROM RECEIPTDETAIL WHERE STATUS = 0 AND RECEIPTKEY = ? AND TOID = ? "
                    , new Object[]{receiptHashMap.get("RECEIPTPKEY"), newLpn}, "收货明细");
            if (receiptDetailInfo != null) {
                throw new Exception("容器条码" + newLpn + "已存在于收货单" + receiptHashMap.get("RECEIPTPKEY") + "的待收货明细中,不允许重复添加");
            }

            HashMap<String, String> lpnInfo = LotxLocxId.findById(context, newLpn, false);
            if (lpnInfo != null) ExceptionHelper.throwRfFulfillLogicException("当前容器条码仍有库存，不允许退货");


            if (SKU.isSerialControl(context, sku)) {
                if (!IDNotes.isBoxId(context, newLpn))
                    ExceptionHelper.throwRfFulfillLogicException("绑定的条码不符合唯一码管理物料的箱号规则");
            } else {
                if (!IDNotes.isLpn(context, newLpn))
                    ExceptionHelper.throwRfFulfillLogicException("绑定的条码不符合按批次管理的容器条码规则");
            }
        }

        //根据不同的业务场景校验数据是否合法，同时如果LPN为空则生成新的LPN。

        //LPN的最后一次历史库存信息（注意：唯一码查询出的是之前所属的所属箱信息，数量为箱内的总数量）
        HashMap<String,String>  lpnHisInfo = null;

        //不绑定箱号时，扫描单个唯一码直接收货，为每个唯一码一个箱子流水号，同时在收货单行上记录该唯一码，目的是为了方便RF和界面查询。
        String singleSerialNumber  = "";

        //唯一码管理的物料，只有两种可能：1.整箱退货：传入originLpn，唯一码退货：传入snLIST
        if(!UtilHelper.isEmpty(originLpn) && snList.length>0) ExceptionHelper.throwRfFulfillLogicException("按箱号收货和按唯一码收货中只能选择一种");

        if(!UtilHelper.isEmpty(originLpn) &&
                (!IDNotes.isBoxId(context,originLpn) && !IDNotes.isLpn(context,originLpn)))
            ExceptionHelper.throwRfFulfillLogicException("待收货的箱号不符合唯一码管理物料的箱号规则");
        /**
         * 唯一码收货绑定新箱号(自动输入都可)
         */
        boolean needGenerateBoxId = false;
        if(SKU.isSerialControl(context,sku) && CDReceiptType.isBindingNewLpn(context,sku,receiptHashMap.get("TYPE"))) {

            if(!UtilHelper.isEmpty(originLpn)){
                //唯一码物料整箱退库,不允许绑定
                    ExceptionHelper.throwRfFulfillLogicException("唯一码管理的物料绑定新箱号退货，必须扫描唯一码");
            }else {
                if(snList.length==0) ExceptionHelper.throwRfFulfillLogicException("扫描唯一码绑定箱号收货，唯一码不能为空");
                //多个唯一码绑定箱号,此时ORIGINLPN一定为空，数据在SNLIST中。如果LPN箱号为空，则需创建新箱号
                if(UtilHelper.isEmpty(newLpn)) {
                    if(CDReceiptType.isBindAndAutoGenerateLpn(context,sku,receiptHashMap.get("TYPE"))) {
                        needGenerateBoxId = true;
                    }else {
                        ExceptionHelper.throwRfFulfillLogicException("当前收货类型的收货绑定至的箱号不能为空");
                    }
                }

                //唯一码查询出的是之前所属的所属箱信息，数量为箱内的总数量
                lpnHisInfo = SNHistory.findBySkuAndSN(context,sku,snList[0],true);
                lpnHisInfo.put("ISOPENED","1");//根据唯一码进行收货，说明箱子已经拆封

            }
            //整箱退货
        }else if(SKU.isSerialControl(context,sku) && !CDReceiptType.isBindingNewLpn(context,sku,receiptHashMap.get("TYPE"))) {

            if(!UtilHelper.isEmpty(originLpn)){
                //唯一码物料整箱退库,箱号不变，但需要填充SN明细行
                lpnHisInfo = IDNotesHistory.findLastShippedRecordById(context,originLpn,true);
                List<HashMap<String, String>> lastShippedSNsById = IDNotesHistory.findLastShippedSNsById(context, originLpn, true);
                snList = lastShippedSNsById.stream().map(x -> x.get("SERIALNUMBER")).toArray(String[]::new);
                snWightList = lastShippedSNsById.stream().map(x -> x.get("NETWEIGHT")).toArray(String[]::new);
                snUomList = lastShippedSNsById.stream().map(x -> x.get("SNUOM")).toArray(String[]::new);
                newLpn = originLpn;
            }else {
                //扫描唯一码直接收货，创建内部流水号LPN箱号
                if(snList.length!=1) ExceptionHelper.throwRfFulfillLogicException("直接扫描唯一码收货，唯一码必须存在且仅存在一个");
                lpnHisInfo = SNHistory.findBySkuAndSN(context,sku, snList[0],true);
                lpnHisInfo.put("ISOPENED","1");//根据唯一码进行收货，说明箱子已经拆封
                singleSerialNumber = snList[0];

                newLpn = IdGenerationHelper.generateIDByKeyName(context,context.getUserID(), "WMSBOX",10);
            }

        }else{
            //按批次管理LPN退货（即普通的LPN）
            if(!UtilHelper.isEmpty(originLpn)) {
                //是否是R3类型收货且当前有库存则认为是现有库存增量
                boolean useExistInventory = isAllowReceivingToExistingInv(context, receiptHashMap, sku, originLpn);

                if(useExistInventory){
                    //使用LPN所在库位
                    lpnHisInfo = LotxLocxId.findById(context,originLpn,true);
                    loc = lpnHisInfo.get("LOC");
                }else{
                    //正常退库，库存必须为0
                    HashMap<String, String> lpnInfo = LotxLocxId.findById(context, newLpn, false);
                    if (lpnInfo != null) ExceptionHelper.throwRfFulfillLogicException("当前容器条码仍有库存，不允许退货");

                    lpnHisInfo = IDNotesHistory.findLastShippedRecordById(context, originLpn, true);
                }

                newLpn = originLpn;
                isOpened = UtilHelper.decimalStrCompare(lpnHisInfo.get("ORIGINALNETWGT"),netStdWgt)==0 ? "0":"1";
                lpnHisInfo.put("ISOPENED",isOpened);
            }else{
                ExceptionHelper.throwRfFulfillLogicException("按批次管理的物料原始容器条码不能为空");
            }

        }
        //根据退货批次生成类型，生成新的退货收货批次信息
        HashMap<String, String> newReceiptLotHashMap = buildReturnLotInfo(context, receiptHashMap, lpnHisInfo);
        //
        if(needGenerateBoxId){
            newLpn = IdGenerationHelper.createBoxId(context,newReceiptLotHashMap.get("LOTTABLE06"));
        }
        //防止重复添加相同的LPN导致无法使用按容器条码收货功能
        String receiptDetailSql="SELECT A.RECEIPTKEY " +
                " FROM RECEIPT A,RECEIPTDETAIL B " +
                "WHERE A.RECEIPTKEY=B.RECEIPTKEY AND B.QTYEXPECTED>0 AND A.STATUS IN ('0','5') AND B.STATUS='0' AND B.TOID=? ";
        String receiptKey = DBHelper.getValue(context, receiptDetailSql, new Object[]{newLpn}, "", false);
        if(!UtilHelper.isEmpty(receiptKey)){
            throw new Exception("容器条码/箱号"+newLpn+"在收货单"+receiptKey+"中已存在待收货明细");
        }

        String userId = context.getUserID();

        HashMap<String,String> newReceiptDetail = new LinkedHashMap<>();

        String storerKey = java.lang.String.valueOf(DBHelper.getValue(context, "select UDF1 from Codelkup where ListName=? and Code=?",
                new Object[]{"SYSSET","STORERKEY"}, "默认货主"));
        newReceiptDetail.put("STORERKEY", storerKey);
        newReceiptDetail.put("TYPE", receiptHashMap.get("TYPE"));
        newReceiptDetail.put("ORIGINALLINENUMBER", receiptLinuNumber);//存储关联的收货单汇总指令行的行号
        newReceiptDetail.put("RECEIPTKEY", receiptHashMap.get("RECEIPTKEY"));
        newReceiptDetail.put("EXTERNRECEIPTKEY", receiptHashMap.get("EXTERNRECEIPTKEY"));
        newReceiptDetail.put("SKU", sku);
        newReceiptDetail.put("TOID", newLpn);
        newReceiptDetail.put("SERIALNUMBER", singleSerialNumber);
        newReceiptDetail.put("TAREWGTEXPECTED", tareStdWgt);
        newReceiptDetail.put("TOLOC", loc);
        if(SKU.isSerialControl(context,sku) && CDReceiptType.isBindingNewLpn(context,sku,receiptHashMap.get("TYPE"))) {
            isOpened = "0"; //CSS返工重新组箱后认为是整箱。
        }else {
            isOpened = "1".equals(isOpened) || "1".equals(lpnHisInfo.get("ISOPENED")) ? "1" : "0";
        }
        newReceiptDetail.put("SUSR7", isOpened);//是否开封
        newReceiptDetail.put("SUSR10", lpnHisInfo.get("LOTTABLE06"));//原收货批次
        newReceiptDetail.put("SUSR11", UtilHelper.intStrAdd(lpnHisInfo.get("RETURNTIMES"),isOpened));//非整桶退货次数
        newReceiptDetail.put("SUSR6", lpnHisInfo.get("PROJECTCODE"));//项目号
        newReceiptDetail.put("BARRELNUMBER", lpnHisInfo.get("BARRELNUMBER")); //桶号
        newReceiptDetail.put("TOTALBARRELNUMBER", lpnHisInfo.get("TOTALBARREL")); //总桶数
        newReceiptDetail.put("REGROSSWGT", regrossWgt);
        newReceiptDetail.put("UOM", uom);
        newReceiptDetail.put("GROSSWGTEXPECTED", grossStdWgt);
        newReceiptDetail.put("TAREWGTEXPECTED", tareStdWgt);
        newReceiptDetail.put("QTYEXPECTED", netStdWgt);
        newReceiptDetail.put("PACKKEY", lpnHisInfo.get("PACKKEY"));
        newReceiptDetail.put("LOTTABLE01", lpnHisInfo.get("LOTTABLE01"));
        newReceiptDetail.put("LOTTABLE02", lpnHisInfo.get("LOTTABLE02"));
        newReceiptDetail.put("LOTTABLE03", lpnHisInfo.get("LOTTABLE03"));
        newReceiptDetail.put("LOTTABLE04", lpnHisInfo.get("LOTTABLE04"));
        newReceiptDetail.put("LOTTABLE05", lpnHisInfo.get("LOTTABLE05"));
        newReceiptDetail.put("LOTTABLE06", newReceiptLotHashMap.get("LOTTABLE06"));
        newReceiptDetail.put("LOTTABLE07", lpnHisInfo.get("LOTTABLE07"));
        newReceiptDetail.put("LOTTABLE08", lpnHisInfo.get("LOTTABLE08"));
        newReceiptDetail.put("LOTTABLE09", lpnHisInfo.get("LOTTABLE09"));
        newReceiptDetail.put("LOTTABLE10", lpnHisInfo.get("LOTTABLE10"));
        newReceiptDetail.put("LOTTABLE11", lpnHisInfo.get("LOTTABLE11"));
        newReceiptDetail.put("LOTTABLE12", lpnHisInfo.get("LOTTABLE12"));
        newReceiptDetail.put("ELOTTABLE01",lpnHisInfo.get("ELOTTABLE01"));
        newReceiptDetail.put("ELOTTABLE02",lpnHisInfo.get("ELOTTABLE02"));//保税状态
        newReceiptDetail.put("ELOTTABLE03",lpnHisInfo.get("ELOTTABLE03"));//质量状态
        newReceiptDetail.put("ELOTTABLE04",lpnHisInfo.get("ELOTTABLE04"));//停止发运期（css）
        newReceiptDetail.put("ELOTTABLE05",lpnHisInfo.get("ELOTTABLE05"));
        newReceiptDetail.put("ELOTTABLE06",lpnHisInfo.get("ELOTTABLE06"));
        newReceiptDetail.put("ELOTTABLE07",newReceiptLotHashMap.get("ELOTTABLE07"));
        newReceiptDetail.put("ELOTTABLE08",lpnHisInfo.get("ELOTTABLE08"));
        newReceiptDetail.put("ELOTTABLE09",lpnHisInfo.get("ELOTTABLE09"));
        newReceiptDetail.put("ELOTTABLE10",lpnHisInfo.get("ELOTTABLE10"));
        newReceiptDetail.put("ELOTTABLE11", lpnHisInfo.get("ELOTTABLE11"));//有效期
        newReceiptDetail.put("ELOTTABLE12", lpnHisInfo.get("ELOTTABLE12"));///生产日期
        newReceiptDetail.put("LASTSHIPPEDLOC",lpnHisInfo.get("LASTSHIPPEDLOC"));//原库位
        newReceiptDetail.put("PROJECTCODEBYORDERKEY",getProjectCodeByOrderKey(context,lpnHisInfo.get("ORDERKEY")));//原出库项目号）
//        newReceiptDetail.put("LASTLOC", lpnHisInfo.get("LOC")); //该ID的上一个库位
        newReceiptDetail.put("LASTID", lpnHisInfo.get("ID")); //该ID的上一个ID
        newReceiptDetail.put("PRODLOTEXPECTED",lpnHisInfo.get("PRODLOTEXPECTED"));//原领料出库目标生产批次

        newReceiptDetail.put("ADDWHO", userId);
        newReceiptDetail.put("EDITWHO", userId);
        newReceiptDetail.put("CONDITIONCODE", "OK");
        newReceiptDetail.put("QTYRECEIVED", "0");

        String thisReceiptLineNumber = DBHelper.getValue(context, "SELECT CASE WHEN MAX(RECEIPTLINENUMBER) IS NULL THEN 0 ELSE MAX(RECEIPTLINENUMBER) END FROM RECEIPTDETAIL WHERE RECEIPTKEY = ?",
                new Object[]{receiptHashMap.get("RECEIPTKEY")}, "").toString();
        String newReceiptLineNumber = LegecyUtilHelper.To_Char(new Integer(thisReceiptLineNumber) + 1, 5);
        newReceiptDetail.put("RECEIPTLINENUMBER", newReceiptLineNumber);
        newReceiptDetail.put("EXTERNLINENO", "WMS" + newReceiptLineNumber);
        LegacyDBHelper.ExecInsert(context, "RECEIPTDETAIL", newReceiptDetail);

        buildReceiptLotxIdInfo(context, newReceiptDetail.get("SKU"),"I",newReceiptDetail.get("TOID"),newReceiptDetail.get("RECEIPTKEY"),newReceiptDetail.get("RECEIPTLINENUMBER"), snList, snWightList, snUomList);


        //只打印开封标签
//        if(!UtilHelper.isEmpty(printer)) {
//            if ("1".equals(isOpened)) {
//                PrintHelper.printLPNByReceiptLineNumber(
//                        context,
//                        newReceiptDetail.get("RECEIPTKEY"),
//                        newReceiptDetail.get("RECEIPTLINENUMBER"),
//                        Labels.LPN_TK,
//                        printer, "1", "打印退库LPN标签");
//            }
//        }

        return newReceiptDetail;
    }

    private static boolean isAllowReceivingToExistingInv(Context context, HashMap<String, String> receiptHashMap, String sku, String originLpn) {

        boolean useExistInventory = false;
        if(!SKU.isSerialControl(context, sku) &&CDReceiptType.isReturnTypeWithInventory(context, receiptHashMap.get("TYPE")) ){

            HashMap<String, String> lotxLocxIdById = LotxLocxId.findById(context, originLpn, false);
            if(lotxLocxIdById != null){
                useExistInventory = true;
            }
        }
        return useExistInventory;
    }

    //生成退货批属性信息
    private static HashMap<String,String> buildReturnLotInfo(Context context, HashMap<String,String> receiptHashMap, HashMap<String,String> lpnHisInfo) throws Exception {

       HashMap<String,String> newReceiptLotHashMap = new HashMap<>();

       //RECEIPTDETAIL.SUSR10 退货LPN的原收货批次，用于收货时供相同批次的LPN检索已生成的子批号，避免子批次重复生成。
       HashMap<String,String> receiptLottableInfoHashMap = DBHelper.getRecord(context,"SELECT TOP 1 * FROM RECEIPTDETAIL WHERE RECEIPTKEY = ? AND SUSR10 = ? ",
                new Object[]{receiptHashMap.get("RECEIPTKEY"), lpnHisInfo.get("LOTTABLE06")},"",false);

       if(receiptLottableInfoHashMap != null){
           newReceiptLotHashMap.put("LOTTABLE06",receiptLottableInfoHashMap.get("LOTTABLE06"));
           newReceiptLotHashMap.put("ELOTTABLE07",receiptLottableInfoHashMap.get("ELOTTABLE07"));
       }else{
           String returnLotGenerateType = CDReceiptType.getReturnLotGenerateType(context, receiptHashMap.get("TYPE"));

           if(UtilHelper.isEmpty(returnLotGenerateType) || "0".equals(returnLotGenerateType)) {
               //使用原收货批次号,无需修改批次
               newReceiptLotHashMap.put("LOTTABLE06",lpnHisInfo.get("LOTTABLE06"));
               newReceiptLotHashMap.put("ELOTTABLE07",lpnHisInfo.get("ELOTTABLE07"));
           }else if("1".equals(returnLotGenerateType)) {
               //当容器开封时，退货的批次号为原批次号+R(A-Z)
               String newRetLot = IdGenerationHelper.createSubReceiptLot(context, lpnHisInfo.get("LOTTABLE06"), "R");
               newReceiptLotHashMap.put("LOTTABLE06", newRetLot);
               newReceiptLotHashMap.put("ELOTTABLE07",lpnHisInfo.get("ELOTTABLE07"));
           }else if("2".equals(returnLotGenerateType)) {
               //退货的批次号直接从系统获取下一新的收货批次号，且Elottable07生产批号+R(A-Z)
               String newRetLot = IdGenerationHelper.createReceiptLot(context, lpnHisInfo.get("SKU"));
               //Elottable07生产批号+R(A-Z)
               String newELottable07 = IdGenerationHelper.createSubReceiptLot(context, lpnHisInfo.get("ELOTTABLE07"), "R");
               newReceiptLotHashMap.put("LOTTABLE06", newRetLot);
               newReceiptLotHashMap.put("ELOTTABLE07",newELottable07);

           }
       }
        return newReceiptLotHashMap;
    }

    /**
     * 插入待收货明细行
     * @throws Exception
     */
    public static HashMap<String,String> insertReceiptDetailByOriginalLine(Context context, HashMap<String,String> originalReceiptLineHashMap,String lpn,String loc, String isOpened, String netStdWgt, String grossStdWgt, String tareStdWgt, String uom, String regrossWgt,String[] snList)throws Exception {

        String receiptLot = originalReceiptLineHashMap.get("LOTTABLE06");
        if(UtilHelper.isEmpty(receiptLot)) {
            ExceptionHelper.throwRfFulfillLogicException("收货指令行的收货批次不能为空，可能是外部导入ASN时未生成，请联系管理员");
//            receiptLot = IdGenerationHelper.createReceiptLot(context, originalReceiptLineHashMap.get("SKU"));
//            DBHelper.executeUpdate(context,"UPDATE RECEIPTDETAIL SET LOTTABLE06 = ? WHERE RECEIPTKEY = ? AND RECEIPTLINENUMBER = ?",
//                    new Object[]{
//                                    receiptLot),
//                                    originalReceiptLineHashMap.get("RECEIPTKEY")),
//                                    originalReceiptLineHashMap.get("RECEIPTLINENUMBER"))
//                            });
        }

        if(UtilHelper.isEmpty(lpn)){
            if(SKU.isSerialControl(context,originalReceiptLineHashMap.get("SKU"))) {
                lpn = IdGenerationHelper.createBoxId(context,receiptLot);
            }else {
                 lpn = IdGenerationHelper.generateLpn(context, receiptLot);
            }

        }else {
            if(SKU.isSerialControl(context,originalReceiptLineHashMap.get("SKU"))) {
                if (!IDNotes.isBoxId(context, lpn))
                    ExceptionHelper.throwRfFulfillLogicException("绑定的条码不符合唯一码管理物料的箱号规则");
            }else {
                if (!IDNotes.isLpn(context, lpn))
                    ExceptionHelper.throwRfFulfillLogicException("绑定的条码不符合按批次管理的容器条码规则");
            }
        }

        String userId = context.getUserID();

        HashMap<String,String> newReceiptDetail = new LinkedHashMap<>();

        HashMap<String, String> receiptDetailInfo = DBHelper.getRecord(context,
                "SELECT RECEIPTKEY FROM RECEIPTDETAIL WHERE STATUS = 0 AND TOID = ? "
                , new Object[]{lpn}, "收货明细");
        if (receiptDetailInfo != null) {
            throw new Exception("容器条码" + lpn + "已存在于收货单" + receiptDetailInfo.get("RECEIPTKEY") + "中,不允许重复添加");
        }

        newReceiptDetail.put("STORERKEY", originalReceiptLineHashMap.get("STORERKEY"));
        newReceiptDetail.put("TYPE", originalReceiptLineHashMap.get("TYPE"));
        newReceiptDetail.put("ORIGINALLINENUMBER", originalReceiptLineHashMap.get("RECEIPTLINENUMBER"));//存储关联的收货单汇总指令行的行号
        newReceiptDetail.put("RECEIPTKEY", originalReceiptLineHashMap.get("RECEIPTKEY"));
        newReceiptDetail.put("EXTERNRECEIPTKEY", originalReceiptLineHashMap.get("EXTERNRECEIPTKEY"));
        newReceiptDetail.put("SKU", originalReceiptLineHashMap.get("SKU"));

        newReceiptDetail.put("TOID", lpn);
        newReceiptDetail.put("TAREWGTEXPECTED", tareStdWgt);
        newReceiptDetail.put("TOLOC", loc);
        newReceiptDetail.put("SUSR7", isOpened);//是否开封
        newReceiptDetail.put("SUSR6", originalReceiptLineHashMap.get("SUSR6"));

        String maxBarrelNumber = DBHelper.getValue(context, "SELECT CASE WHEN MAX(BARRELNUMBER) IS NULL THEN 0 ELSE MAX(BARRELNUMBER) END FROM RECEIPTDETAIL WHERE ORIGINALLINENUMBER IS NOT NULL AND RECEIPTKEY = ? ",
                new Object[]{originalReceiptLineHashMap.get("RECEIPTKEY")}, "").toString();
        String nextBarrelNumber = LegecyUtilHelper.To_Char(new Integer(maxBarrelNumber) + 1, 3);
        newReceiptDetail.put("BARRELNUMBER", nextBarrelNumber); //桶号
        newReceiptDetail.put("TOTALBARRELNUMBER", nextBarrelNumber); //总桶数

        //新增收货明细时，更新所有当前已存在的收货明细的总桶数
        DBHelper.executeUpdate(context, "UPDATE RECEIPTDETAIL SET TOTALBARRELNUMBER = ? WHERE RECEIPTKEY = ? ", new Object[]{
                nextBarrelNumber, originalReceiptLineHashMap.get("RECEIPTKEY")
        });

        newReceiptDetail.put("REGROSSWGT", regrossWgt);
        newReceiptDetail.put("UOM", uom);
        newReceiptDetail.put("GROSSWGTEXPECTED", grossStdWgt);
        newReceiptDetail.put("TAREWGTEXPECTED", tareStdWgt);
        newReceiptDetail.put("QTYEXPECTED", netStdWgt);
        newReceiptDetail.put("PACKKEY", originalReceiptLineHashMap.get("PACKKEY"));
        newReceiptDetail.put("LOTTABLE01", nvl(originalReceiptLineHashMap.get("LOTTABLE01"), " "));
        newReceiptDetail.put("LOTTABLE02", nvl(originalReceiptLineHashMap.get("LOTTABLE02"), " "));
        newReceiptDetail.put("LOTTABLE03", nvl(originalReceiptLineHashMap.get("LOTTABLE03"), " "));
        newReceiptDetail.put("LOTTABLE04", originalReceiptLineHashMap.get("LOTTABLE04"));
        newReceiptDetail.put("LOTTABLE05", originalReceiptLineHashMap.get("LOTTABLE05"));

        newReceiptDetail.put("LOTTABLE06", receiptLot);

        newReceiptDetail.put("LOTTABLE07", nvl(originalReceiptLineHashMap.get("LOTTABLE07"), " "));
        newReceiptDetail.put("LOTTABLE08", nvl(originalReceiptLineHashMap.get("LOTTABLE08"), " "));
        newReceiptDetail.put("LOTTABLE09", nvl(originalReceiptLineHashMap.get("LOTTABLE09"), " "));
        newReceiptDetail.put("LOTTABLE10", nvl(originalReceiptLineHashMap.get("LOTTABLE10"), " "));
        newReceiptDetail.put("LOTTABLE11", originalReceiptLineHashMap.get("LOTTABLE11"));
        newReceiptDetail.put("LOTTABLE12", originalReceiptLineHashMap.get("LOTTABLE12"));
        newReceiptDetail.put("ELOTTABLE01", nvl(originalReceiptLineHashMap.get("ELOTTABLE01"), " "));//质量状态
        newReceiptDetail.put("ELOTTABLE02", nvl(originalReceiptLineHashMap.get("ELOTTABLE02"), " "));//保税状态
        newReceiptDetail.put("ELOTTABLE03", nvl(originalReceiptLineHashMap.get("ELOTTABLE03"), " "));//质量状态
        newReceiptDetail.put("ELOTTABLE04", originalReceiptLineHashMap.get("ELOTTABLE04"));//停止发运期（css）
        newReceiptDetail.put("ELOTTABLE05", originalReceiptLineHashMap.get("ELOTTABLE05"));
        newReceiptDetail.put("ELOTTABLE06", nvl(originalReceiptLineHashMap.get("ELOTTABLE06"), " "));
        newReceiptDetail.put("ELOTTABLE07", nvl(originalReceiptLineHashMap.get("ELOTTABLE07"), " "));
        newReceiptDetail.put("ELOTTABLE08", nvl(originalReceiptLineHashMap.get("ELOTTABLE08"), " "));
        newReceiptDetail.put("ELOTTABLE09", nvl(originalReceiptLineHashMap.get("ELOTTABLE09"), " "));
        newReceiptDetail.put("ELOTTABLE10", nvl(originalReceiptLineHashMap.get("ELOTTABLE10"), " "));
        newReceiptDetail.put("ELOTTABLE11", originalReceiptLineHashMap.get("ELOTTABLE11"));//有效期
        newReceiptDetail.put("ELOTTABLE12", originalReceiptLineHashMap.get("ELOTTABLE12"));///生产日期

        newReceiptDetail.put("ADDWHO", userId);
        newReceiptDetail.put("EDITWHO", userId);
        newReceiptDetail.put("CONDITIONCODE", "OK");
        newReceiptDetail.put("QTYRECEIVED", "0");

        String thisReceiptLineNumber = DBHelper.getValue(context, "SELECT CASE WHEN MAX(RECEIPTLINENUMBER) IS NULL THEN 0 ELSE MAX(RECEIPTLINENUMBER) END FROM RECEIPTDETAIL WHERE RECEIPTKEY = ?",
                new Object[]{originalReceiptLineHashMap.get("RECEIPTKEY")}, "").toString();
        String newReceiptLineNumber = LegecyUtilHelper.To_Char(new Integer(thisReceiptLineNumber) + 1, 5);
        newReceiptDetail.put("RECEIPTLINENUMBER", newReceiptLineNumber);
        newReceiptDetail.put("EXTERNLINENO", "WMS" + newReceiptLineNumber);
        LegacyDBHelper.ExecInsert(context, "RECEIPTDETAIL", newReceiptDetail);

        buildReceiptLotxIdInfo(context, newReceiptDetail.get("SKU"),"I",newReceiptDetail.get("TOID"),newReceiptDetail.get("RECEIPTKEY"),newReceiptDetail.get("RECEIPTLINENUMBER"), snList);


        return newReceiptDetail;
    }






    /**
     * 删除待收货明细行
     * @param context

     * @param receiptKey
     * @param receiptLineNumber
     * @throws Exception
     */
    public static void deleteReceiptDetail(Context context,String receiptKey,String receiptLineNumber)throws Exception{

        HashMap<String,String> receiptDetailInfo = Receipt.findReceiptDetailById(context,receiptKey,receiptLineNumber,true);

        if(!receiptDetailInfo.get("STATUS").equals("0"))  ExceptionHelper.throwRfFulfillLogicException("当前收货单行已经收货，不允许删除");

        DBHelper.executeUpdate(context,"DELETE FROM RECEIPTDETAIL WHERE RECEIPTKEY = ? AND RECEIPTLINENUMBER = ? ",
        new Object[]{receiptDetailInfo.get("RECEIPTKEY"), receiptDetailInfo.get("RECEIPTLINENUMBER")});

        if(SKU.isSerialControl(context,receiptDetailInfo.get("SKU"))) {
            DBHelper.executeUpdate(context, "DELETE FROM LOTXIDHEADER WHERE SOURCEKEY = ? AND SOURCELINENUMBER = ? AND IOFLAG ='I'",
                    new Object[]{receiptDetailInfo.get("RECEIPTKEY"), receiptDetailInfo.get("RECEIPTLINENUMBER")});

            DBHelper.executeUpdate(context, "DELETE FROM LOTXIDDETAIL WHERE SOURCEKEY = ? AND SOURCELINENUMBER = ? AND IOFLAG ='I'",
                    new Object[]{receiptDetailInfo.get("RECEIPTKEY"), receiptDetailInfo.get("RECEIPTLINENUMBER")});
        }

        HashMap<String,String> receiptTypeHashMap = CodeLookup.getCodeLookupByKey(context, "RECEIPTYPE", receiptDetailInfo.get("TYPE"));
        if(Const.RECEIPT_RF_TYPE_WITH_ASN.equalsIgnoreCase(receiptTypeHashMap.get("UDF5"))) {
            refreshReceiptBarrelNumber(context, receiptKey, receiptDetailInfo.get("ORIGINALLINENUMBER"));
        }
    }

    private static void refreshReceiptBarrelNumber(Context context, String receiptKey,String originalLineNumber){

        List<HashMap<String,String>> receiptDetails = DBHelper.executeQuery(
                context,"SELECT RECEIPTLINENUMBER FROM RECEIPTDETAIL WHERE RECEIPTKEY = ? AND ORIGINALLINENUMBER = ? ",
                new Object[]{receiptKey, originalLineNumber}
                );

        int currentBarrelNumber = 0;

        if(receiptDetails!=null && receiptDetails.size()>0){
            String totalBarrelNumber = LegecyUtilHelper.To_Char(receiptDetails.size(), 3);;
            for(int i =0;i<receiptDetails.size();i++){
                String nextBarrelNumber = LegecyUtilHelper.To_Char(++currentBarrelNumber, 3);
                //更新所有收货明细的总桶数
                DBHelper.executeUpdate(context,"UPDATE RECEIPTDETAIL SET TOTALBARRELNUMBER = ?, BARRELNUMBER=? WHERE RECEIPTKEY = ? AND  RECEIPTLINENUMBER = ? ",
                  new Object[]{
                            totalBarrelNumber, nextBarrelNumber,receiptKey,receiptDetails.get(i).get("RECEIPTLINENUMBER")
                });
            }
        }

    }

    public static void execReceiptDetailReceiving(Context context, ServiceDataHolder serviceDataHolder, HashMap<String,String> insertedReceiptDetail, String grossWgt, String tareWgt, String netWgt) throws Exception {
        //调用API执行收货。
        serviceDataHolder.getInputDataAsMap().setAttribValue("LPN", insertedReceiptDetail.get("TOID"));//箱号
        serviceDataHolder.getInputDataAsMap().setAttribValue("RECEIPTKEY", insertedReceiptDetail.get("RECEIPTKEY"));//ASN收货单号
        serviceDataHolder.getInputDataAsMap().setAttribValue("LOC", insertedReceiptDetail.get("TOLOC"));
        //收货时需要传入UOMQTY，非STD QTY
        serviceDataHolder.getInputDataAsMap().setAttribValue("GROSSWGTRECEIVED", grossWgt);//毛量
        serviceDataHolder.getInputDataAsMap().setAttribValue("TAREWGTRECEIVED", tareWgt);//皮重
        serviceDataHolder.getInputDataAsMap().setAttribValue("NETWGTRECEIVED", netWgt);//净重
        /////
        serviceDataHolder.getInputDataAsMap().setAttribValue("REGROSSWGT", "");//复称重量
        serviceDataHolder.getInputDataAsMap().setAttribValue("ESIGNATUREKEY", "PL");//ESIGNATUREKEY
        ServiceHelper.executeService(context, "ReceivingWithSignature", serviceDataHolder);


        HashMap<String,String> receiptTypeHashMap = CodeLookup.getCodeLookupByKey(context, "RECEIPTYPE", insertedReceiptDetail.get("TYPE"));

        //有汇总指令的ASN收货要更新桶号信息
        if(Const.RECEIPT_RF_TYPE_WITH_ASN.equalsIgnoreCase(receiptTypeHashMap.get("UDF5"))) {

            //更新所有已收货行的总桶数
            DBHelper.executeUpdate(context, "UPDATE IDNOTES SET TOTALBARREL = ?, BARRELDESCR = BARRELNUMBER +' / '+ ? WHERE ORIGINRECEIPTKEY = ? ", new Object[]{
                    insertedReceiptDetail.get("TOTALBARREL"),
                    insertedReceiptDetail.get("TOTALBARREL"),
                    insertedReceiptDetail.get("ORIGINRECEIPTKEY")
            });

        }
    }


    /**
     * 处理收货单汇总行的状态。
     */
    public static void processReceiptStatus(Context context,String receiptKey){

        //如果明细收货行收了汇总收货行的货，明细收货行应显示为已接收，更新汇总收货行的状态为5（收货中）。
        List<HashMap<String, String>> records = DBHelper.executeQuery(context,
                "SELECT ORIGINALLINENUMBER,SUM(QTYRECEIVED) QTYRECEIVED,MIN(STATUS) AS STATUS FROM RECEIPTDETAIL WHERE RECEIPTKEY = ? AND ORIGINALLINENUMBER IS NOT NULL GROUP BY ORIGINALLINENUMBER ",
                new Object[]{receiptKey});
        for (HashMap<String, String> record : records) {
            //当明细收货行>=汇总行的数量时，汇总行应更新已接收
            String qtyExpected = DBHelper.getValue(context, "SELECT QTYEXPECTED FROM RECEIPTDETAIL WHERE RECEIPTKEY = ? AND RECEIPTLINENUMBER = ?",
                    new Object[]{receiptKey, record.get("ORIGINALLINENUMBER")}, String.class, "");
            //收货量已达到汇总行要求并且该汇总行下没有未收货的收货行
            if(UtilHelper.decimalStrCompare(qtyExpected,record.get("QTYRECEIVED")) <= 0 && !record.get("STATUS").equals("0")){
                Receipt.updateReceiptDetailStatus(context,receiptKey,record.get("ORIGINALLINENUMBER"),"9");
            }else{
                Receipt.updateReceiptDetailStatus(context,receiptKey,record.get("ORIGINALLINENUMBER"),"5");
            }
        }

        //这里更新的是收货单单头的状态。
        HashMap<String, String> record = DBHelper.getRecord(context,
                "SELECT SUM(CASE WHEN (STATUS = '0' OR STATUS = '5') THEN 1 ELSE 0 END) AS DONE , SUM (CASE WHEN STATUS = '9' THEN 1 ELSE 0 END) AS NOTSTART FROM RECEIPTDETAIL WHERE RECEIPTKEY = ?",
                new Object[]{receiptKey}, "");
        //该出库单所有单行的STATUS为0的count，即单行为新建状态的行数
        boolean done = "0".equals(record.get("DONE"));
        //该出库单所有单行的STATUS为9的count，即单行为收货完成状态的行数
        boolean notStart = "0".equals(record.get("NOTSTART"));
        String status = "";
        if(done){
            //Status为新建状态的行为0的话，说明单子收货完成
            status = "9";
        }else if(notStart){
            //Status为已完成状态的行为0的话，说明单子未收货
            status = "0";
        }else {
            status = "5";
        }
        Receipt.updateReceiptStatus(context,receiptKey,status);

    }

    public static String getProjectCodeByOrderKey(Context context,String orderKey){
        if (UtilHelper.isEmpty(orderKey))return " ";
        String projectCodeByOrderKey = DBHelper.getValue(context, "SELECT NOTES FROM ORDERS WHERE ORDERKEY = ? ",
                new Object[]{orderKey}, String.class, "");
        return projectCodeByOrderKey;
    }
}
