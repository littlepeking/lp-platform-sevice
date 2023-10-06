package com.enhantec.wms.backend.inbound.receiving;

import com.enhantec.wms.backend.common.WMSServiceNames;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.Const;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.base.code.CDQualityStatus;
import com.enhantec.wms.backend.common.base.code.CDReceiptType;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.inventory.VLotAttribute;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.WMSBaseService;import com.enhantec.framework.common.utils.EHContextHelper;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.inbound.asn.utils.ReceiptValidationHelper;
import com.enhantec.wms.backend.inventory.utils.InventoryHelper;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;
import com.enhantec.wms.backend.core.inbound.ReceivingByIdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * --注册方法
 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='ReceivingWithSignature'
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values('ReceivingWithSignature', 'com.enhantec.sce.inbound.receiving', 'enhantec', 'ReceivingWithSignature', 'TRUE',  'JOHN',  '按LPN收货并签名' , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,LPN,RECEIPTKEY,LOC,GROSSWGTRECEIVED,NETWGTRECEIVED,TAREWGTRECEIVED,REGROSSWGT,ESIGNATUREKEY','0.10','0');
 */

/*
 * 批次逻辑：
 * 1.如果收货批次存在，则校验所有静态的批属性要一致，对于需要变化的批属性（如质量状态，复验期、有效期等）不进行校验，直接沿用当前库存中的批次信息。
 * 2.如果批次不存在，新增收货批次。
 *
 * 标签信息表逻辑：
 * 1.标签表中只保留有库存的标签，已出库的标签已移至IDNOTESHISTORY
 * 2.如果标签不存在，则新增，同时在收货后要记录WMS LOT.
 *
 * 目前暂不支持在本功能中直接扫描唯一码收货，因为并没有应用的场景，目前支持的唯一码物料收货场景：
 * 扫描绑定后的LPN箱号收货（CSS）
 * 在有ASN的收货功能中创建收货明细并自动收货(CS)
 *
 */

@Service(WMSServiceNames.ReceivingWithSignature)
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
public class ReceivingWithSignature extends WMSBaseService {

    private final ReceivingByIdService receivingByIdService;

    public void execute(ServiceDataHolder serviceDataHolder) {

        String userid = EHContextHelper.getUser().getUsername();
//        context.theSQLMgr.transactionBegin();
        
        try {

            String RECEIPTKEY = serviceDataHolder.getInputDataAsMap().getString("RECEIPTKEY");
            if(UtilHelper.isEmpty(RECEIPTKEY)) ExceptionHelper.throwRfFulfillLogicException("RECEIPTKEY不能为空");
            String LPN = serviceDataHolder.getInputDataAsMap().getString("LPN");
            if(UtilHelper.isEmpty(LPN)) ExceptionHelper.throwRfFulfillLogicException("容器条码不能为空");

            Map<String, String> receiptDetailInfo =  Receipt.findReceiptDetailByLPN( RECEIPTKEY,LPN,true);

            String LOC = receiptDetailInfo.get("TOLOC");

            String GROSSWGTRECEIVED = serviceDataHolder.getInputDataAsMap().getString("GROSSWGTRECEIVED");
            String TAREWGTRECEIVED = serviceDataHolder.getInputDataAsMap().getString("TAREWGTRECEIVED");
            String NETWGTRECEIVED = serviceDataHolder.getInputDataAsMap().getString("NETWGTRECEIVED");
            String REGROSSWGT = serviceDataHolder.getInputDataAsMap().getString("REGROSSWGT");/*复秤重量*/
            String ESIGNATUREKEY = serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

            try {
                ReceiptValidationHelper.validateASN(RECEIPTKEY);

                //检查待收货行的是否唯一码已存在于库存
                ReceiptValidationHelper.checkSerialNumberExistInInv(receiptDetailInfo);
                InventoryHelper.checkLocQuantityLimit(LOC);
            }catch (Exception e){
                ExceptionHelper.throwRfFulfillLogicException(
                        "当前收货单数据校验失败，请检查收货单信息是否正确，错误信息：\n"+
                        e.getMessage());
            }

            Map<String, String> result = this.receivingByLpn(serviceDataHolder, userid, ESIGNATUREKEY, RECEIPTKEY, LPN, LOC, GROSSWGTRECEIVED, TAREWGTRECEIVED, NETWGTRECEIVED, REGROSSWGT);

            ServiceDataMap theOutDO = new ServiceDataMap();
            theOutDO.setAttribValue("LPN", LPN);
            theOutDO.setAttribValue("TOTAL", result.get("TOTAL"));
            theOutDO.setAttribValue("RECALL", result.get("RECALL"));

            serviceDataHolder.setReturnCode(1);
            serviceDataHolder.setOutputData(theOutDO);
          

        } catch (Exception e) {
            if (e instanceof FulfillLogicException)
                throw (FulfillLogicException) e;
            else
                throw new FulfillLogicException(e.getMessage());
        } finally {
        }
    }

    private Map<String,String> receivingByLpn(ServiceDataHolder serviceDataHolder, String userid, String ESIGNATUREKEY, String RECEIPTKEY, String LPN, String LOC, String GROSSWGTRECEIVED, String TAREWGTRECEIVED, String NETWGTRECEIVED, String REGROSSWGT) throws Exception{
        //如果批量，ESIGNATUREKEY 为空字符串，用来后面验证是不是同一单同一批的最后一桶
        boolean eachReceivingall = "PL".equals(ESIGNATUREKEY);/*Receivingtype区分是否批量收入；*/
        if (eachReceivingall) ESIGNATUREKEY = "";

        Map<String, String> receiptInfo = Receipt.findByReceiptKey( RECEIPTKEY, true);
        //执行前对数据的校验
        //入库收货检查 生基
        ReceiptValidationHelper.checkASNReceiptCheckStatus(receiptInfo);
        this.CheckIfReceiptConfirmed(receiptInfo);
        this.CheckReceivingLocExists(LOC);

        this.checkIdNotes(LPN,receiptInfo,LOC);

        /*MASK  12/26/2020 11:00:00*/
        //查询带有lpn，且状态为未收货，预期量>0的收货明细行
        String sql =
                "SELECT RECEIPTKEY,RECEIPTLINENUMBER, STORERKEY,TOID,SERIALNUMBER, STATUS,TYPE,TOLOC,SKU,QTYEXPECTED,UOM,PACKKEY,CONDITIONCODE"
                        + ",LOTTABLE01,LOTTABLE02,ELOTTABLE02,ELOTTABLE03,FORMAT(LOTTABLE04,'" + LegacyDBHelper.DateTimeFormat + "') AS LOTTABLE04,FORMAT(ELOTTABLE05,'" + LegacyDBHelper.DateTimeFormat + "') AS ELOTTABLE05,ELOTTABLE06,LOTTABLE06,ELOTTABLE07,ELOTTABLE08,ELOTTABLE09,LOTTABLE10,FORMAT(ELOTTABLE11,'" + LegacyDBHelper.DateTimeFormat + "') AS ELOTTABLE11,FORMAT(ELOTTABLE12,'" + LegacyDBHelper.DateTimeFormat + "') AS ELOTTABLE12 , ELOTTABLE14,ELOTTABLE21 , ELOTTABLE10 "
                        + ",SUSR1,SUSR2,SUSR3,SUSR4,SUSR5"
                        + ",SUSR6,SUSR7,SUSR8,SUSR9,SUSR10"
                        + ",SUSR11,SUSR12,SUSR13,SUSR14,SUSR15"
                        + ",SUSR16,SUSR17,SUSR18,SUSR19,SUSR20,BARRELNUMBER,TOTALBARRELNUMBER,MEMO,LASTSHIPPEDLOC,PRODLOTEXPECTED "
                        + " FROM RECEIPTDETAIL WHERE RECEIPTKEY=? AND TOID=? AND QTYEXPECTED>0 AND STATUS='0' ORDER BY STATUS DESC";
        Map<String, String> receiptDetailInfo = DBHelper.getRecord( sql,
                new Object[]{RECEIPTKEY, LPN}, "待收货明细行", true);

        //更新Elottable信息。
        this.processELottableInfo(receiptDetailInfo);

        String stdUom = UOM.getStdUOM( receiptDetailInfo.get("PACKKEY"));
        String cntLastLpn = DBHelper.getStringValue( "SELECT COUNT(1) FROM RECEIPTDETAIL WHERE RECEIPTKEY=? AND TOID<>? AND QTYEXPECTED>0 AND STATUS=?"
                , new String[]{RECEIPTKEY, LPN, "0"}, "0");
        //转换单位。
        BigDecimal grossStdQty = UOM.UOMQty2StdQty( receiptDetailInfo.get("PACKKEY"), receiptDetailInfo.get("UOM"),
                UtilHelper.isEmpty(GROSSWGTRECEIVED) ? new BigDecimal("0") : new BigDecimal(GROSSWGTRECEIVED));
        BigDecimal tareStdQty = UOM.UOMQty2StdQty( receiptDetailInfo.get("PACKKEY"), receiptDetailInfo.get("UOM"),
                UtilHelper.isEmpty(TAREWGTRECEIVED) ? new BigDecimal("0") : new BigDecimal(TAREWGTRECEIVED));
        BigDecimal netStdQty = UOM.UOMQty2StdQty( receiptDetailInfo.get("PACKKEY"), receiptDetailInfo.get("UOM"),
                UtilHelper.isEmpty(NETWGTRECEIVED) ? new BigDecimal("0") : new BigDecimal(NETWGTRECEIVED));

        DBHelper.executeUpdate( "update receiptdetail set lottable01 = ? where receiptkey=? and toid=?",
                new String[]{receiptDetailInfo.get("PACKKEY"), RECEIPTKEY, LPN});

        //调用系统API进行收货操作.
        receivingByIdService.execute(new ServiceDataHolder(new ServiceDataMap(
                new HashMap(){{
                    put("RECEIPTKEY",RECEIPTKEY);
                    put("LPN",LPN);
                    put("QTY",netStdQty);
                }}
        )));
        //新增idnotes
        this.populateIdNotes(LPN,userid,GROSSWGTRECEIVED,TAREWGTRECEIVED,NETWGTRECEIVED,REGROSSWGT,stdUom,receiptDetailInfo);


        //判断lpn内是否有开封唯一码 有 则标记
        ChangeOpenSnMarksHelper.changeOpenSnMarksBYLpn(receiptDetailInfo.get("SKU"),LPN);
        DBHelper.executeUpdate( "update receiptdetail set STATUS = 9, GROSSWGTRECEIVED=? , TAREWGTRECEIVED = ?,REGROSSWGT =? where receiptkey=? and toid=?", new String[]{GROSSWGTRECEIVED, TAREWGTRECEIVED, REGROSSWGT, RECEIPTKEY, LPN});

        String receivingFuncType = CDReceiptType.getReceivingFuncType(receiptInfo.get("TYPE"));
        //有汇总指令的ASN收货要更新ASN状态并更新当前行的预期量为0
        if(Const.RECEIPT_RF_TYPE_WITH_ASN.equalsIgnoreCase(receivingFuncType)
                || Const.RECEIPT_RF_TYPE_RETURN_WITH_ASN.equalsIgnoreCase(receivingFuncType)) {

            //收货完成，把预期量改成0
            DBHelper.executeUpdate(
                    "UPDATE RECEIPTDETAIL SET QTYEXPECTED = 0 WHERE TOID = ? AND RECEIPTKEY = ?",
                    new Object[]{LPN, RECEIPTKEY});
            //处理收货单汇总行状态和单头状态.
            Receipt.processReceiptStatus( RECEIPTKEY);
        }else {
            //根据是否是最后一箱，来更新订单状态。
            if (cntLastLpn.equals("0")) {
                DBHelper.executeUpdate( "update receipt set STATUS = 9 where receiptkey=? ", new String[]{RECEIPTKEY});
            }
        }

        String sku = DBHelper.getStringValue( "select sku from receiptdetail where receiptkey=? and toid=?", new Object[]{RECEIPTKEY, LPN});
        String cnt = DBHelper.getStringValue( "select count(1) as C1 from receiptdetail where receiptkey=? and sku=?  AND TOID IS NOT NULL AND TOID <>'' "
                , new String[]{RECEIPTKEY, sku}, "");
        String cnt1 = DBHelper.getStringValue( "select count(1) as C1 from receiptdetail where receiptkey=? and sku=? AND TOID IS NOT NULL AND TOID <>'' and status>0"
                , new String[]{RECEIPTKEY, sku}, "");
        String TOTAL = cnt1 + " / " + cnt;
        String RECALL = "N";
        if (cnt1.equals(cnt)) RECALL = "Y";
        //填写记录日志
        this.addUDTRN(userid,ESIGNATUREKEY,RECEIPTKEY,LPN,LOC,NETWGTRECEIVED,receiptDetailInfo);


        Map<String, String> result = new HashMap<>();
        result.put("TOTAL",TOTAL);
        result.put("RECALL",RECALL);

        return result;

    }

    private void addUDTRN(String userid,String ESIGNATUREKEY,String RECEIPTKEY,String LPN,String LOC,String NETWGTRECEIVED,Map<String,String> receiptDetailInfo)throws Exception{

        Udtrn UDTRN = new Udtrn();
        if(!UtilHelper.isEmpty(ESIGNATUREKEY)){
            String[] split = ESIGNATUREKEY.split(":");
            if(split.length > 1){
                UDTRN.EsignatureKey = split[0];
                UDTRN.EsignatureKey1 = split[1];
            }else {
                UDTRN.EsignatureKey = ESIGNATUREKEY;
            }
        }
        UDTRN.FROMTYPE = "收货-按LPN";
        UDTRN.FROMTABLENAME = "RECEIPTDETAIL";
        UDTRN.FROMKEY = RECEIPTKEY;
        UDTRN.FROMKEY1 = LPN;
        UDTRN.FROMKEY2 = "";
        UDTRN.FROMKEY3 = "";
        UDTRN.TITLE01 = "ASN单号";
        UDTRN.CONTENT01 = RECEIPTKEY;
        UDTRN.TITLE02 = "容器条码";
        UDTRN.CONTENT02 = LPN;
        UDTRN.TITLE04 = "收货库位";
        UDTRN.CONTENT04 = LOC;
        UDTRN.TITLE05 = "物料";
        UDTRN.CONTENT05 = LegecyUtilHelper.Nz(receiptDetailInfo.get("SKU"), "");
        UDTRN.TITLE06 = "数量";
        UDTRN.CONTENT06 = LegecyUtilHelper.Nz(NETWGTRECEIVED, "");
        UDTRN.TITLE07 = "质量等级";
        UDTRN.CONTENT07 = LegecyUtilHelper.Nz(receiptDetailInfo.get("ELOTTABLE02"), "");
        UDTRN.TITLE08 = "质量状态";
        UDTRN.CONTENT08 = LegecyUtilHelper.Nz(receiptDetailInfo.get("ELOTTABLE03"), "");
        UDTRN.TITLE09 = "收货日期";
        UDTRN.CONTENT09 = LegecyUtilHelper.Nz(receiptDetailInfo.get("LOTTABLE04"), "");
        UDTRN.TITLE10 = "复验日期";
        UDTRN.CONTENT10 = LegecyUtilHelper.Nz(receiptDetailInfo.get("ELOTTABLE05"), "");
        UDTRN.TITLE11 = "批属性6";
        UDTRN.CONTENT11 = LegecyUtilHelper.Nz(receiptDetailInfo.get("LOTTABLE06"), "");
        UDTRN.TITLE12 = "型号";
        UDTRN.CONTENT12 = LegecyUtilHelper.Nz(receiptDetailInfo.get("ELOTTABLE07"), "");
        UDTRN.TITLE13 = "供应商";
        UDTRN.CONTENT13 = LegecyUtilHelper.Nz(receiptDetailInfo.get("ELOTTABLE08"), "");
        UDTRN.TITLE14 = "供应商批次";
        UDTRN.CONTENT14 = LegecyUtilHelper.Nz(receiptDetailInfo.get("ELOTTABLE09"), "");
        UDTRN.TITLE16 = "有效期";
        UDTRN.CONTENT16 = LegecyUtilHelper.Nz(receiptDetailInfo.get("ELOTTABLE11"), "");
        UDTRN.TITLE17 = "成品生产日期";
        UDTRN.CONTENT17 = LegecyUtilHelper.Nz(receiptDetailInfo.get("ELOTTABLE12"), "");
        UDTRN.TITLE18 = "存货类型";
        UDTRN.CONTENT18 = LegecyUtilHelper.Nz(receiptDetailInfo.get("LOTTABLE02"), "");
        UDTRN.insert( userid);
    }

    /**
     * 更新或插入idnotes表
     * @param LPN 容器条码
     * @param userid
     * @param GROSSWGTRECEIVED 毛重
     * @param TAREWGTRECEIVED 皮重
     * @param NETWGTRECEIVED 净重
     * @param REGROSSWGT 复称重量
     * @param stdUom 单位
     * @param receiptDetailInfo 收货行信息
     * @throws Exception
     */
    private void populateIdNotes(String LPN, String userid, String GROSSWGTRECEIVED, String TAREWGTRECEIVED, String NETWGTRECEIVED, String REGROSSWGT, String stdUom, Map<String,String> receiptDetailInfo)throws Exception{
        /*IDNOTES需要记录所属的WMS LOT*/
        String receivedLot = LotxLocxId.findWithoutCheckIDNotes( LPN, true).get("LOT");

        /*查找IDNOTES是否存在*/
        Map<String,String> idnotesRecord = IDNotes.findById( LPN, false);

        if (idnotesRecord != null) {
            //ExceptionHelper.throwRfFulfillLogicException("库存中已有收货标签"+LPN+"的库存，收货失败");
            //恢复对已有lpn进行增量收货的功能，更新idnotes表
            Map<String,String> updateFields = new HashMap<>();
            updateFields.put("EditWho", userid);
            updateFields.put("GROSSWGT", UtilHelper.decimalStrAdd(idnotesRecord.get("GROSSWGT"),GROSSWGTRECEIVED));/*毛重*/
            updateFields.put("TAREWGT", UtilHelper.decimalStrAdd(idnotesRecord.get("TAREWGT"),TAREWGTRECEIVED));/*皮重*/
            updateFields.put("NETWGT", UtilHelper.decimalStrAdd(idnotesRecord.get("NETWGT"),NETWGTRECEIVED));/*净重*/
            updateFields.put("ORIGINALGROSSWGT", UtilHelper.decimalStrAdd(idnotesRecord.get("ORIGINALGROSSWGT"),GROSSWGTRECEIVED));/*毛重*/
            updateFields.put("ORIGINALTAREWGT", UtilHelper.decimalStrAdd(idnotesRecord.get("ORIGINALTAREWGT"),TAREWGTRECEIVED));/*皮重*/
            updateFields.put("ORIGINALNETWGT", UtilHelper.decimalStrAdd(idnotesRecord.get("ORIGINALNETWGT"),NETWGTRECEIVED));/*净重*/
            IDNotes.update(LPN,updateFields);
        }else {
            //为零记录已归档至归档表，所有收货或者退货记录均需重新插入IDNOTES表
            Map<String,String> IDNOTES = new HashMap<>();
            IDNOTES.put("AddWho", userid);
            IDNOTES.put("EditWho", userid);
            IDNOTES.put("ID", LPN);/*LPN*/
            IDNOTES.put("GROSSWGT", GROSSWGTRECEIVED);/*毛重*/
            IDNOTES.put("TAREWGT", TAREWGTRECEIVED);/*皮重*/
            IDNOTES.put("NETWGT", NETWGTRECEIVED);/*净重*/
            IDNOTES.put("ORIGINALGROSSWGT", GROSSWGTRECEIVED);/*毛重*/
            IDNOTES.put("ORIGINALTAREWGT", TAREWGTRECEIVED);/*皮重*/
            IDNOTES.put("ORIGINALNETWGT", NETWGTRECEIVED);/*净重*/
            IDNOTES.put("GROSSWGTLABEL", receiptDetailInfo.get("SUSR8"));/*标签毛重(仅分装使用，正常收货目前没有采集标签量，只有采集毛重的复秤重量)*/
            IDNOTES.put("NETWGTLABEL", receiptDetailInfo.get("SUSR9"));/*标签净重(仅分装使用，正常收货目前没有采集标签量）*/
            IDNOTES.put("TAREWGTLABEL", receiptDetailInfo.get("SUSR10"));/*标签皮重(仅分装使用，正常收货目前没有采集标签量）*/
            IDNOTES.put("UOMLABEL", receiptDetailInfo.get("UOM"));/*标签单位(仅分装使用，正常收货目前没有采集标签量）*/
            IDNOTES.put("REGROSSWGT", REGROSSWGT);/*复秤重量*/
            IDNOTES.put("STORERKEY", receiptDetailInfo.get("STORERKEY"));
            IDNOTES.put("SKU", receiptDetailInfo.get("SKU"));/*物料代码*/
            IDNOTES.put("UOM", stdUom);/*主计量单位*/
            IDNOTES.put("PACKKEY", receiptDetailInfo.get("PACKKEY"));/*包装  写入的目的是在退库创建ASN单时能够取到当时出库的包装*/
            IDNOTES.put("BARRELNUMBER", receiptDetailInfo.get("BARRELNUMBER"));/*桶号*/
            IDNOTES.put("TOTALBARREL", receiptDetailInfo.get("TOTALBARRELNUMBER"));/*总桶号*/
            IDNOTES.put("BARRELDESCR", receiptDetailInfo.get("BARRELNUMBER") + " / " + receiptDetailInfo.get("TOTALBARRELNUMBER"));/*桶描述*/
            IDNOTES.put("ORIGINRECEIPTKEY", receiptDetailInfo.get("RECEIPTKEY"));/*原始收货单号*/
            IDNOTES.put("ORIGINRECEIPTLINENUMBER", receiptDetailInfo.get("RECEIPTLINENUMBER"));/*原始收货单行号*/

            IDNOTES.put("LOT", receivedLot);
            String projectCode = UtilHelper.isEmpty(receiptDetailInfo.get("SUSR6")) ? CDSysSet.getDefaultProjectCode() : receiptDetailInfo.get("SUSR6").trim();
            IDNOTES.put("PROJECTCODE", projectCode);
            IDNOTES.put("PROJECTID", receiptDetailInfo.get("SUSR12"));
            IDNOTES.put("ISOPENED", UtilHelper.isEmpty(receiptDetailInfo.get("SUSR7")) ? "0" : receiptDetailInfo.get("SUSR7"));
            IDNOTES.put("MEMO", receiptDetailInfo.get("MEMO"));//备注
            IDNOTES.put("RETURNTIMES", receiptDetailInfo.get("SUSR11"));//非整桶退货次数在退货单中已计算完毕，直接复制
            IDNOTES.put("LASTSHIPPEDLOC",receiptDetailInfo.get("LASTSHIPPEDLOC"));//原库位信息
//            IDNOTES.put("LASTLOC",receiptDetailInfo.get("LASTLOC"));//上一次的所在库位
            IDNOTES.put("LASTID",receiptDetailInfo.get("LASTID"));//上一次的所在容器
            IDNOTES.put("PRODLOTEXPECTED",receiptDetailInfo.get("PRODLOTEXPECTED"));//原领料出库目标生产批次

            LegacyDBHelper.ExecInsert( "IDNOTES", IDNOTES);
        }

    }

    /**
     * 收货前对数据的校验
     *  1. 校验库位是否存在。
     *  2. 校验收货单是否复核。
     */
    private void CheckIfReceiptConfirmed(Map<String,String> receiptInfo) throws Exception{

        //检验收货单状态，是否复核
        if (!receiptInfo.get("ISCONFIRMED").equals("2"))
            ExceptionHelper.throwRfFulfillLogicException("收货单" + receiptInfo.get("RECEIPTKEY") + "未复核,不允许收货");



    }

    private void CheckReceivingLocExists(String loc) throws Exception{
        //查询库位在系统中是否存在
        Map<String,String> locRecord = DBHelper.getRecord( "select LOCATIONHANDLING,STATUS from LOC where LOC=?", new String[]{loc},"",false);
        if (locRecord == null)
            throw new Exception("收货库位在系统中不存在");
    }

    /**
     *  校验库存是否存在，及收货方式是否是退货入库,如果是退货入库暂不进行后台历史数据的核对校验，以提高收货性能。

     * @param lpn
     * @param receiptInfo
     */
    private void checkIdNotes( String lpn, Map<String,String> receiptInfo,String loc){
//        Map<String, String> idnotesRecord = IDNotes.findById( lpn, false);
        Map<String, String> idnotesRecord = LotxLocxId.findById( lpn, false);
        if (idnotesRecord != null) {
            if(CDReceiptType.isReturnTypeWithInventory(receiptInfo.get("TYPE"))){
                if(!idnotesRecord.get("LOC").equals(loc)){
                    ExceptionHelper.throwRfFulfillLogicException("增量收货只允许收货到库存所在库位:"+idnotesRecord.get("LOC"));
                }
            }else{
                ExceptionHelper.throwRfFulfillLogicException("此容器条码在库内仍有库存，不允许重复收货");
            }
            //如果idnotes有库存并且系统中有库存，就抛出异常不允许收货,已重构IDNOTES发运后的记录至IDNOTESHISTORY表中，IDNOTES表中的记录均有库存，无需再比较数量。
            //为了提高收货性能，收货时不再看该ID的历史库存。
            // if (!UtilHelper.trimZerosAndToStr(idnotesRecord.get("NETWGT")).equals("0") || lotxLocxIdRecord != null) {
//            ExceptionHelper.throwRfFulfillLogicException("此容器条码在库内仍有库存，不允许重复收货");
            //}

        }

    }


    /**
     * 根据收货批次，处理ELottable的信息，增加或校验


     * @param receiptDetailInfo
     * @throws Exception
     */
    private void processELottableInfo(Map<String,String> receiptDetailInfo) throws Exception {
        //根据收货批号获取该批次Elottable的信息。
        Map<String, String> receiptLotInfo = VLotAttribute.getEnterpriseReceiptLotInfo( receiptDetailInfo.get("LOTTABLE06"));

        if (receiptLotInfo != null) {
            //如果收货批次相同，但是物料代码不同，提示批次被占用。
            if (!receiptDetailInfo.get("SKU").equals(receiptLotInfo.get("SKU")))
                ExceptionHelper.throwRfFulfillLogicException(
                        "收货批次" + receiptDetailInfo.get("LOTTABLE06") + "已被物料" + receiptLotInfo.get("SKU") + "使用，不允许重复使用"
                );

            List<Map<String,String>> skuLotConfList = CodeLookup.getCodeLookupList("SKULOTCONF");

            Map<String,String> skuHashMap = SKU.findById(receiptDetailInfo.get("SKU"),true);

            if(skuLotConfList!=null && skuLotConfList.size()>0) {
                //BUSR4 物料类型
                skuLotConfList = skuLotConfList.stream().filter(
                        e -> UtilHelper.equals(e.get("UDF1"), skuHashMap.get("BUSR4"))).collect(Collectors.toList());

                checkElotFieldsIsMatch(receiptDetailInfo,receiptLotInfo, skuLotConfList);

            }
            //如果相同收货批次在库存中已经存在，则使用现有数据覆盖RECEIPTDETAIL上提供的质量状态、复测期和有效期，避免出现相同批次不同质量状态、复测期和有效期的情况。
            /*因为目前收货批次、复测期、有效期已经抽取到ELOTATTRIBUTE表中，当相同批号出现时直接使用已存在的批次号即可，该批次号使用的质量状态、复测期、有效期会自动通过ELOTATTRIBUTE表进行关联，不需要像原始版本一样需要手工更新LOTATTRIBUTE表。*/
        } else {
            //如果没有收货批次信息，则插入新的收货批次信息。信息来源为收货单
            Map<String,String> eLot = new HashMap<>();
            eLot.put("STORERKEY", receiptDetailInfo.get("STORERKEY"));
            eLot.put("SKU", receiptDetailInfo.get("SKU"));
            eLot.put("ELOT", receiptDetailInfo.get("LOTTABLE06"));
            for (int i = 1; i <= 24; i++) {
                String num = UtilHelper.addPrefixZeros4Number(i, 2);
                eLot.put("ELOTTABLE" + num, UtilHelper.trim(receiptDetailInfo.get("ELOTTABLE" + num)));
            }
            //更新保税状态、账册号、产地信息
            Map<String, String> receiptHashMap = DBHelper.getRecord( "select ELOTTABLE01,ELOTTABLE02,TYPE,ELOTTABLE22,ELOTTABLE23,ELOTTABLE24,ELOTTABLE18 " +
                            "from RECEIPT where RECEIPTKEY=? ",
                    new Object[]{receiptDetailInfo.get("RECEIPTKEY")}, "收货单", true);
            //退货沿用原保税状态和产地信息。第一次收货，行上如为空，则取收货单头上的信息。
            if(UtilHelper.isEmpty(eLot.get("ELOTTABLE01"))) eLot.put("ELOTTABLE01",UtilHelper.nvl(receiptHashMap.get("ELOTTABLE01"),""));
            if(UtilHelper.isEmpty(eLot.get("ELOTTABLE02"))) eLot.put("ELOTTABLE02",UtilHelper.nvl(receiptHashMap.get("ELOTTABLE02"),""));
            if(UtilHelper.isEmpty(eLot.get("ELOTTABLE18"))) eLot.put("ELOTTABLE18",UtilHelper.nvl(receiptHashMap.get("ELOTTABLE18"),""));
            if(UtilHelper.isEmpty(eLot.get("ELOTTABLE23"))) eLot.put("ELOTTABLE23",UtilHelper.nvl(receiptHashMap.get("ELOTTABLE23"),""));
            if(UtilHelper.isEmpty(eLot.get("ELOTTABLE24"))) eLot.put("ELOTTABLE24",UtilHelper.nvl(receiptHashMap.get("ELOTTABLE24"),""));
            if(UtilHelper.isEmpty(eLot.get("ELOTTABLE22"))) eLot.put("ELOTTABLE22",UtilHelper.nvl(receiptHashMap.get("ELOTTABLE22"),""));
            if(UtilHelper.isEmpty(eLot.get("ELOTTABLE20"))) eLot.put("ELOTTABLE20",UtilHelper.nvl(receiptDetailInfo.get("SUSR5"),""));

            String qualityStatus ="";
            //根据配置获取质量状态
            if(receiptHashMap.get("TYPE").equalsIgnoreCase(CDSysSet.getPOReceiptType())){
                 qualityStatus = receiptDetailInfo.get("ELOTTABLE03");
            }else {
                 qualityStatus = CDQualityStatus.findByReceiptType(receiptHashMap.get("TYPE"),receiptDetailInfo.get("SKU"),receiptDetailInfo.get("ELOTTABLE03"));
            }
            eLot.put("ELOTTABLE03",qualityStatus);

            //ELOTTABLE09记录收货批次，用于参与动态分配。
            // 对于按批次管理的物料，ELOTTABLE09可自动赋值为LOTTABLE06，
            //对于唯一码管理的物料直接使用receiptdetail传入的生产批次ELOTTABLE09。
            //后期可以考虑将动态拣货字段抽象成灵活的配置。

            //ELOTTABLE09在CS用于供应商批号，这里暂不做自动赋值。如果动态拣货，需要按照供应商批号动态拣货，或者考虑把供应商批号移至ELOTTABLE07.
            //boolean isSkuSerialControl = SKU.isSerialControl(receiptDetailInfo.get("SKU"));
            //            if(!isSkuSerialControl){
            //                eLot.put("ELOTTABLE09", UtilHelper.trim(receiptDetailInfo.get("LOTTABLE06")));
            //            }

            eLot.put("ELOTTABLE13", "0");/*复测次数默认设为0*/
            LegacyDBHelper.ExecInsert( "ENTERPRISE.ELOTATTRIBUTE", eLot);
        }
    }

    private void checkElotFieldsIsMatch(Map<String, String> receiptDetailInfo,Map<String, String>  receiptLotInfo, List<Map<String, String>> skuLotConfList) {
        //String num = UtilHelper.addPrefixZeros4Number(i, 2);
        //String lottableFieldName = "ELOTTABLE" + num;

        //因为保税状态从ASN头上取值，取消ELOTTABLE02对保税状态的校验
        //checkElotFieldIsMatch(receiptDetailInfo,receiptLotInfo,skuLotConfList,"ELOTTABLE02");
        checkElotFieldIsMatch(receiptDetailInfo,receiptLotInfo,skuLotConfList,"ELOTTABLE07");
        checkElotFieldIsMatch(receiptDetailInfo,receiptLotInfo,skuLotConfList,"ELOTTABLE08");
        checkElotFieldIsMatch(receiptDetailInfo,receiptLotInfo,skuLotConfList,"ELOTTABLE09");
        checkElotFieldIsMatch(receiptDetailInfo,receiptLotInfo,skuLotConfList,"ELOTTABLE12");
    }

    private void checkElotFieldIsMatch(Map<String, String> receiptDetailInfo,Map<String, String>  receiptLotInfo, List<Map<String, String>> skuLotConfList, String lottableFieldName){

        if(!UtilHelper.equals(receiptDetailInfo.get(lottableFieldName),receiptLotInfo.get(lottableFieldName))) {

            Optional<Map<String, String>> lottableConf = skuLotConfList.stream().filter(x -> UtilHelper.equals(x.get("UDF1"), lottableFieldName)).findFirst();

            String fieldDisplayName = lottableFieldName;
            if(lottableConf.isPresent()){
                String label = lottableConf.get().get("UDF2");
                if(!UtilHelper.isEmpty(label)) fieldDisplayName = label;
            }

            ExceptionHelper.throwRfFulfillLogicException("收货行中的'" + fieldDisplayName + "'批属性和当前库存中的不一致，请检查收货数据");
        }
    }


}

