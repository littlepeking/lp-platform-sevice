package com.enhantec.wms.backend.outbound.picking;

import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.*;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.inventory.utils.InventoryHelper;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import com.enhantec.wms.backend.common.KeyGen;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.outbound.OutboundUtils;
import com.enhantec.wms.backend.outbound.utils.OrderValidationHelper;
import com.enhantec.wms.backend.utils.common.*;
import com.enhantec.wms.backend.utils.print.Labels;
import com.enhantec.wms.backend.utils.print.PrintHelper;

import java.math.BigDecimal;
import com.enhantec.framework.common.utils.EHContextHelper;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PickUtil {

    public static String getPickReason(BigDecimal taskQty, BigDecimal stdQtyTobePicked) {
        String reason ="";
        if(stdQtyTobePicked.compareTo(taskQty)<0) reason ="SHORT";
        else if(stdQtyTobePicked.compareTo(taskQty)>0) reason ="OVERPICK";

        return reason;

    }

    public static String getQualityStatusSqlFilterStr(Map<String, String> orderTypeInfo, String qualityStatus) {


        if(!UtilHelper.isEmpty(qualityStatus)) {
            return " and vl.ELOTTABLE03 = '" + qualityStatus + "'";
        }else {

            if (!UtilHelper.isEmpty(orderTypeInfo.get("UDF5"))) {

                String[] statuses = orderTypeInfo.get("UDF5").split(",");


                if(statuses.length==1){
                   return  " and vl.ELOTTABLE03 = '" + statuses[0] + "'";
                }else {

                    StringBuffer statusSB = new StringBuffer(" and vl.ELOTTABLE03 in ('").append(statuses[0]);

                    for(int i=1;i<statuses.length;i++){

                        statusSB.append("','").append(statuses[i]);
                    }

                    statusSB.append("') ");

                    return statusSB.toString();

                }
            }

            return "";

        }


/*        String sql ="";

        if(!UtilHelper.isEmpty(qualityStatus)) {
            // 匹配箱的质量状态和容器的质量状态eLottable03
            sql =  " and (" +
                    "      ('RELEASE' = '" + qualityStatus + "' and vl.ELOTTABLE03 in ('RELEASE','NA') and i.QUALITYSTATUS in ('RELEASE','NA')) or " +
                    "      ('REJECT' = '" + qualityStatus + "' and ( vl.ELOTTABLE03 = 'REJECT' or i.QUALITYSTATUS ='REJECT'  )) or " +
                    "      ('CONDIREL' = '" + qualityStatus + "' and vl.ELOTTABLE03 in ('CONDIREL') and i.QUALITYSTATUS in ('NA','RELEASE','CONDIREL' )) or " +
                    "      ('CONDIREL' = '" + qualityStatus + "' and vl.ELOTTABLE03 in ('RELEASE','NA') and i.QUALITYSTATUS = 'CONDIREL' ) or " +
                    "      ('QUARANTINE' = '" + qualityStatus + "' and vl.ELOTTABLE03 = 'QUARANTINE' and i.QUALITYSTATUS in ('NA','RELEASE','CONDIREL','QUARANTINE'))  or " +
                    "      ('QUARANTINE' = '" + qualityStatus + "' and vl.ELOTTABLE03 in ('NA','RELEASE','CONDIREL') and  i.QUALITYSTATUS in ('QUARANTINE')) " +
                    "     ) ";
        }

        return sql;
*/

    }





    public static String getQualityStatusSqlOrderByStr(Map<String, String> orderTypeInfo, String qualityStatus) {


        if (!UtilHelper.isEmpty(qualityStatus)) {
            return "";
        } else {

            if (!UtilHelper.isEmpty(orderTypeInfo.get("UDF5"))) {

                String[] statuses = orderTypeInfo.get("UDF5").split(",");

                if (statuses.length == 1) {
                    return ""; //一个状态时不需要排序
                } else {
                    StringBuffer statusSB = new StringBuffer(" CASE ");

                    for (int i = 0; i < statuses.length; i++) {

                        statusSB.append(" WHEN vl.ELOTTABLE03  = '").append(statuses[i]).append("' THEN ").append(i);
                    }

                    statusSB.append(" ELSE 999 END ASC, ");

                    return statusSB.toString();

                }
            }

            return "";

        }
    }

        @Deprecated
    public static String computeQualityStatus1(String lotQualityStatus, String idQualityStatus) throws Exception {

        if(("RELEASE".equals(lotQualityStatus) || "NA".equals(lotQualityStatus)) && ("RELEASE".equals(idQualityStatus) || "NA".equals(idQualityStatus))){
            return "RELEASE";
        }else if("REJECT".equals(lotQualityStatus) || "REJECT".equals(idQualityStatus)){
            return "REJECT";
        }else if("CONDIREL".equals(lotQualityStatus) && ("RELEASE".equals(lotQualityStatus)|| "CONDIREL".equals(idQualityStatus)|| "NA".equals(idQualityStatus))){
            return "CONDIREL";
        }else if(("RELEASE".equals(lotQualityStatus)|| "NA".equals(lotQualityStatus)) && "CONDIREL".equals(idQualityStatus)){
            return "CONDIREL";
        }else if("QUARANTINE".equals(lotQualityStatus) && ("QUARANTINE".equals(idQualityStatus)|| "RELEASE".equals(idQualityStatus)|| "CONDIREL".equals(idQualityStatus)|| "NA".equals(idQualityStatus))){
            return "QUARANTINE";
        }else if(("RELEASE".equals(lotQualityStatus) || "CONDIREL".equals(lotQualityStatus)|| "NA".equals(lotQualityStatus)) && ("QUARANTINE".equals(idQualityStatus))){
            return "QUARANTINE";
        }

        throw new Exception("未找到容器质量状态"+lotQualityStatus+"和批次质量状态"+idQualityStatus+"的计算组合");

    }

    public static void checkIfSplitTimesOverLimit( String fromid) {

        Map<String,String> record = DBHelper.getRecord("select KEYCOUNT FROM NCOUNTER n where n.KEYNAME = ? ",  new Object[]{ fromid }, "NCOUNTER");

        if( record!=null &&  Integer.parseInt(record.get("KEYCOUNT"))>=26) ExceptionHelper.throwRfFulfillLogicException("同一容器分拆最多允许26次");
    }


    /**
     * 手工完成唯一码拣货（使用NSPRFTPK01C进行整容器拣货时，无需执行此操作）


     * @param orderKey
     * @param orderLineNumber
     * @param pickdetailKey
     * @param fromIdHashMap
     * @param toId
     * @param snList
     * @param itrnKey
     * @throws Exception
     */
    public static void pickSerialNumber( String orderKey, String orderLineNumber, String pickdetailKey, Map<String, String> fromIdHashMap, String toId, String[] snList, String itrnKey) throws Exception {

        String userid = EHContextHelper.getUser().getUsername();

        if (snList.length != 0) {

            String lotxIdKey = LotxId.buildLotxIHeaderInfo(  fromIdHashMap.get("SKU"), "O", toId, orderKey, orderLineNumber, pickdetailKey, fromIdHashMap.get("LOT"));

            for (int i = 0; i < snList.length; i++) {
                LotxId.buildLotxIdDetailInfo( lotxIdKey, fromIdHashMap.get("SKU"), "O", fromIdHashMap.get("ID"), toId, orderKey, orderLineNumber, pickdetailKey, fromIdHashMap.get("LOT"), snList[i], i + 1);
            }

            DBHelper.executeUpdate( "UPDATE LOTXIDHEADER SET  STATUS ='9' WHERE LOTXIDKEY = ? ", new Object[]{lotxIdKey});
            for (int i = 0; i < snList.length; i++) {
                DBHelper.executeUpdate( "UPDATE SERIALINVENTORY SET LOC = ?, ID = ? , LOT = ? WHERE  SERIALNUMBERLONG = ? AND SKU = ? ", new Object[]{
                        "PICKTO", toId, fromIdHashMap.get("LOT"), snList[i], fromIdHashMap.get("SKU")});

                String itrnSerialKey = KeyGen.getKey("ITRNSERIALKEY", 2, 10);

                Map<String,String> serialItrn = new HashMap<>();
                serialItrn.put("SERIALNUMBERLONG", snList[i]);
                serialItrn.put("SERIALNUMBER", snList[i]);
                serialItrn.put("ITRNSERIALKEY", itrnSerialKey);
                serialItrn.put("ITRNKEY", itrnKey);
                serialItrn.put("STORERKEY", fromIdHashMap.get("STORERKEY"));
                serialItrn.put("SKU", fromIdHashMap.get("SKU"));
                serialItrn.put("LOT", fromIdHashMap.get("LOT"));
                serialItrn.put("LOC", "PICKTO");
                serialItrn.put("ID", toId);
                serialItrn.put("QTY", "1");
                serialItrn.put("GROSSWEIGHT", "0");
                serialItrn.put("NETWEIGHT", "0");
                serialItrn.put("DATA2", "");
                serialItrn.put("DATA3", "");
                serialItrn.put("DATA4", "");
                serialItrn.put("DATA5", "");
                serialItrn.put("Trantype", "MV");
                serialItrn.put("ADDWHO", userid);
                serialItrn.put("EDITWHO", userid);
                LegacyDBHelper.ExecInsert( "ITRNSERIAL", serialItrn);

            }
        }
    }

    /**
     *
     * @return
     *  key:TOID,value:拣货至容器号
     *  key:PRINT,value:是否打印标签
     * @throws Exception
     */
    public static Map<String,String> doRandomPick( String orderKey, String orderLineNumber, Map<String, String> lotxLocxIdHashMap, String toId, String grossUomWgt, String tareUomWgt, String netUomWgt, String uom, BigDecimal stdQtyAllocated, String[] snList, String esignatureKey, String printer) throws Exception {

        String fromId = lotxLocxIdHashMap.get("ID");
        String userid = EHContextHelper.getUser().getUsername();



        BigDecimal grossUomWgtDecimal = UtilHelper.str2Decimal(grossUomWgt,"毛重",false);
        BigDecimal uomQtyTobePicked = UtilHelper.str2Decimal(netUomWgt,"净重",false);
        BigDecimal tareUomWgtDecimal = UtilHelper.str2Decimal(tareUomWgt,"皮重",false);
        if (grossUomWgtDecimal.subtract(uomQtyTobePicked).compareTo(tareUomWgtDecimal) != 0) {
            ExceptionHelper.throwRfFulfillLogicException("输入的毛皮净重不匹配");
        }

        BigDecimal lpnQty = new BigDecimal(lotxLocxIdHashMap.get("QTY"));
        BigDecimal availQty = new BigDecimal(lotxLocxIdHashMap.get("AVAILABLEQTY"));


        //String stdUom = UOM.getStdUOM( lotxLocxIdHashMap.get("PACKKEY"));

        BigDecimal stdGrossWgtDecimal = UOM.UOMQty2StdQty( lotxLocxIdHashMap.get("PACKKEY"), uom, grossUomWgtDecimal);
        BigDecimal stdTareWgtDecimal = UOM.UOMQty2StdQty( lotxLocxIdHashMap.get("PACKKEY"), uom, tareUomWgtDecimal);
        BigDecimal stdQtyTobePicked = UOM.UOMQty2StdQty( lotxLocxIdHashMap.get("PACKKEY"), uom, uomQtyTobePicked);


        OutboundUtils.checkQtyIsAvailableInLotxLocxId( fromId,stdQtyTobePicked,stdQtyAllocated);
        OutboundUtils.checkQtyIsAvailableInIDNotes( fromId, stdQtyTobePicked);

        boolean isPickFullLPN = stdQtyTobePicked.compareTo(lpnQty) == 0;

        if (!isPickFullLPN) PickUtil.checkIfSplitTimesOverLimit( fromId);

        boolean printLabel = false;

        ///////////////////
        //正常的拣货逻辑

        if (!isPickFullLPN) {
            //动态拣货TOID参数应传入空，系统会自动生成LPN号（批次管理物料生成子容器号，唯一码管理物料生成流水码箱号）
            toId = IDNotes.splitWgtById( stdGrossWgtDecimal, stdQtyTobePicked, stdTareWgtDecimal, grossUomWgt, netUomWgt, tareUomWgt, uom, fromId,toId, orderKey,false);

            Map<String,String> fieldsToBeUpdate = new HashMap<>();
            fieldsToBeUpdate.put("LASTSHIPPEDLOC", lotxLocxIdHashMap.get("LOC")); //该ID最后一次的拣货自库位
//            fieldsToBeUpdate.put("LASTLOC", lotxLocxIdHashMap.get("LOC")); //该ID的上一个库位
            fieldsToBeUpdate.put("LASTID", lotxLocxIdHashMap.get("ID")); //该ID的上一个ID
            IDNotes.update( toId, fieldsToBeUpdate);
            if(IDNotes.isLpnOrBoxId(toId)) {
                Map<String, String> orderInfo = Orders.findByOrderKey( orderKey, true);
                List<String> notPrintLpnLabelOrderTypes = CDSysSet.getNotPrintLpnLabelOrderTypes();
                if(null == notPrintLpnLabelOrderTypes || !notPrintLpnLabelOrderTypes.contains(orderInfo.get("TYPE"))) {
                    printLabel = true;
                    PrintHelper.printLPNByIDNotes( toId, Labels.LPN, printer, "1", "非整桶拣货标签");
                }
            }
            //如果该ID的标签由于固体多次分装的情况发生，已经存在，则应删除已存在的物料剩余量标签的打印任务（因为再次分装后，原待打印的物料剩余量标签的数量已经不正确），并新建打印任务
            if(CDSysSet.enableLabelWgt()) {
                printLabel = true;
                PrintHelper.removePrintTaskByIDNotes( Labels.LPN_UI_SY, fromId);
                PrintHelper.printLPNByIDNotes( fromId, Labels.LPN_UI_SY, printer, "1", "物料剩余量标签");
            }


        } else {
            //(FULL LPN) AND ((NEW LPN IS NULL) OR (NEW LPN = OLD LPN))
            toId = fromId;

            //更新标签信息
            Map<String,String> fieldsToBeUpdate = new HashMap<String,String>();

            fieldsToBeUpdate.put("GROSSWGTLABEL", grossUomWgt);//原毛重标签量
            fieldsToBeUpdate.put("TAREWGTLABEL", tareUomWgt);//原皮重标签量
            fieldsToBeUpdate.put("NETWGTLABEL", netUomWgt);//原净重标签量
            fieldsToBeUpdate.put("UOMLABEL", uom);//采集读取的计量单位
            fieldsToBeUpdate.put("LASTSHIPPEDLOC", lotxLocxIdHashMap.get("LOC")); //该ID最后一次的拣货自库位
//            fieldsToBeUpdate.put("LASTLOC", lotxLocxIdHashMap.get("LOC")); //该ID的上一个库位
            fieldsToBeUpdate.put("LASTID", lotxLocxIdHashMap.get("ID")); //该ID的上一个ID
            fieldsToBeUpdate.put("ORDERKEY", orderKey);
            IDNotes.update( toId, fieldsToBeUpdate);
        }

        Map<String, String> outHashMap = doPickByAddPickDetail(orderKey, orderLineNumber, fromId, toId, stdQtyTobePicked);

        String pickDetailKey =outHashMap.get("PICKDETAILKEY");
        String itrnKey =outHashMap.get("ITRNKEY");


        if(SKU.isSerialControl(lotxLocxIdHashMap.get("SKU"))) {
            //ADD PICKDETAIL的方式进行整箱拣货，需要手工填充序列号库存
            if(snList.length == 0) {
                List<Map<String, String>> snListHashMap = SerialInventory.findByLpn( fromId, true);
                snList = snListHashMap.stream().map(x -> x.get("SERIALNUMBERLONG")).toArray(String[]::new);
            }

            PickUtil.pickSerialNumber( orderKey, orderLineNumber, pickDetailKey, lotxLocxIdHashMap, toId, snList, itrnKey);

        }

        /////////

        Udtrn UDTRN = new Udtrn();

        if(esignatureKey.indexOf(':')==-1){
            //取样自动生成
            UDTRN.EsignatureKey=esignatureKey;
        }else {
            //复核
            String[] eSignatureKeys = esignatureKey.split(":");
            UDTRN.EsignatureKey=eSignatureKeys[0];
            UDTRN.EsignatureKey1=eSignatureKeys[1];
        }

        UDTRN.FROMTYPE = "拣货";
        UDTRN.FROMTABLENAME = "ORDERDETAIL";
        UDTRN.FROMKEY = orderKey + orderLineNumber;
        UDTRN.FROMKEY1 = fromId;
        UDTRN.FROMKEY2 = toId;
        UDTRN.TITLE01 = "出库单号及行号";
        UDTRN.CONTENT01 = orderKey + orderLineNumber;
        UDTRN.TITLE02 = "来源容器号";
        UDTRN.CONTENT02 = fromId;
        UDTRN.TITLE03 = "拣货到容器号";
        UDTRN.CONTENT03 = toId;
        UDTRN.TITLE04 = "拣货批次";
        UDTRN.CONTENT04 = lotxLocxIdHashMap.get("ELOTTABLE09");
        UDTRN.TITLE05 = "拣货包装";
        UDTRN.CONTENT05 = lotxLocxIdHashMap.get("ELOTTABLE01");
        UDTRN.TITLE06 = "质量状态";
        UDTRN.CONTENT06 = lotxLocxIdHashMap.get("ELOTTABLE03");
        UDTRN.TITLE07 = "拣货数量";
        UDTRN.CONTENT07 = stdQtyTobePicked.toPlainString();
        UDTRN.Insert( userid);

        Map<String, String> result = new HashMap<>();
        result.put("TOID", toId);
        result.put("PRINT", String.valueOf(printLabel));

        return result;
    }

    public static Map<String, String> doPickByAddPickDetail( String orderKey, String orderLineNumber, String idToBePicked, String toId, BigDecimal stdQtyToBePicked) throws SQLException {

        ServiceDataMap thePickDO = new ServiceDataMap();

        Map<String,String> lotxLocxIdInfo = LotxLocxId.findById(idToBePicked,true);

        //检查容器条码和订单冻结分配状态匹配
        PickUtil.checkIdHoldStatusMatchOrderType(orderKey, lotxLocxIdInfo);
        //检查容器条码和订单质量状态匹配
        OrderValidationHelper.checkIdQualityStatusMatchOrderType(orderKey,lotxLocxIdInfo);

        String pickDetailKey = IdGenerationHelper.getNextKey("PICKDETAILKEY");
//        thePickDO.clearDO();
//        thePickDO.setConstraintItem("pickdetailkey", pickDetailKey);
//        thePickDO.setWhereClause(" WHERE PickDetailKey = :pickdetailkey");
////        context.theEXEDataObjectStack.push(thePickDO);
//        context.theSQLMgr.searchTriggerLibrary("PickDetail")).preInsertFire();


        String caseId = IdGenerationHelper.getNextKey("CARTONID");


        double grosswgt = 0.0D;
        double netwgt = 0.0D;
        double tarewgt = 0.0D;

        String stdUOM = UOM.getStdUOM( lotxLocxIdInfo.get("PACKKEY"));
        //right now just provide stduom, it should be always 6
        String uom = UOM.getUOMCode( lotxLocxIdInfo.get("PACKKEY"), stdUOM);

        //支持冻结库存的拣货
        String statusRequired = lotxLocxIdInfo.get("STATUS").equals("OK")? "OK": InventoryHelper.getHoldStatus4Pick(lotxLocxIdInfo);
        //更新拣货明细的自定义字段1为开封，用于反馈赋码系统时确定是否使用箱号还是唯一码
        Map<String,String> idNotesHashMap = IDNotes.findById(toId,true);

        DBHelper.executeUpdate(
                " INSERT INTO PICKDETAIL ( PickDetailKey, CaseID, PickHeaderkey, OrderKey, OrderLineNumber, Lot, Storerkey, Sku, PackKey, UOM, UOMQty, Qty, Loc, ToLoc, ID, CartonGroup, CartonType, DoReplenish, ReplenishZone, DoCartonize, PickMethod, AddWho, EditWho, SeqNo, StatusRequired,fromloc, SelectedCartonType, SelectedCartonID, grosswgt, netwgt, tarewgt, PickContPlacement, status, PDUDF1 ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,? )",
                new Object[]{
                        pickDetailKey,
                        caseId,
                        " ",
                        orderKey,
                        orderLineNumber,
                        lotxLocxIdInfo.get("LOT"),
                        lotxLocxIdInfo.get("STORERKEY"),
                        lotxLocxIdInfo.get("SKU"),
                        lotxLocxIdInfo.get("PACKKEY"),
                        uom,
                        stdQtyToBePicked,
                        stdQtyToBePicked,
                        lotxLocxIdInfo.get("LOC"),
                        "",
                        lotxLocxIdInfo.get("ID"),
                        "STD",//carton group
                        "CASE",//carton type
                        "N",//DOREPLENISH
                        " ",//REPLENISHZONE
                        "N",//DOCARTONIZE
                        "3",//PICKMETHOD 1:定向/3辅助
                        EHContextHelper.getUser().getUsername(),
                        EHContextHelper.getUser().getUsername(),
                        99999,
                        statusRequired,
                        lotxLocxIdInfo.get("LOC"),
                        "CASE",//SELECTEDCARTONTYPE
                        caseId,
                        grosswgt,
                        netwgt,
                        tarewgt,
                        "0",//PICKCONTPLACEMENT 拣货优先级
                        "0", //sta
                        idNotesHashMap.get("ISOPENED")// tus
                });


//
//        EXEDataObject triggerDO = new EXEDataObject();
//        triggerDO.setAttribValue("qty",
//                stdQtyToBePicked.toPlainString());
//        triggerDO.setAttribValue("sku",
//                lotxLocxIdInfo.get("SKU"));
//        triggerDO.setAttribValue("storerkey",
//                lotxLocxIdInfo.get("STORERKEY"));
//
//        triggerDO.setAttribValue("fromloc",
//                lotxLocxIdInfo.get("LOC"));
//        triggerDO.setAttribValue("loc",
//                lotxLocxIdInfo.get("LOC"));
//        triggerDO.setAttribValue("pickdetailkey",
//                pickDetailKey);
//        triggerDO.setAttribValue("orderkey",
//                orderKey);
//        triggerDO.setAttribValue("orderlinenumber",
//                orderLineNumber);
//        triggerDO.setAttribValue("lot",
//                lotxLocxIdInfo.get("LOT"));
//        triggerDO.setAttribValue("id", lotxLocxIdInfo.get("ID"));
//        triggerDO.setAttribValue("pickmethod",
//                "3");
//        triggerDO.setAttribValue("uom",
//                uom);
//        triggerDO.setAttribValue("PACKKEY",
//                lotxLocxIdInfo.get("PACKKEY"));
//
//        triggerDO.setAttribValue("statusRequired", statusRequired);
//
//        context.theSQLMgr.searchTriggerLibrary("PickDetail")).postInsertFire();
//

                //todo:
////        //更新拣货明细为已拣货
//        context.theSQLMgr.searchTriggerLibrary("PickDetail")).preUpdateFire();
        DBHelper.executeUpdate(
                "UPDATE PICKDETAIL SET DROPID = ?, LOC = ?, TOLOC = ?, STATUS = ? WHERE PICKDETAILKEY = ? ",
                new Object[]{toId, "PICKTO","PICKTO", "5", pickDetailKey});

//        EXEDataObject pdUpdateTriggerDO = new EXEDataObject();
//        // pdUpdateTriggerDO.setAttribValue("ID"), toId));
//        pdUpdateTriggerDO.setAttribValue("DROPID", toId);
//        pdUpdateTriggerDO.setAttribValue("TOLOC", "PICKTO");
//        pdUpdateTriggerDO.setAttribValue("LOC", "PICKTO");
//        pdUpdateTriggerDO.setAttribValue("FROMLOC", lotxLocxIdInfo.get("LOC"));
//        pdUpdateTriggerDO.setAttribValue("STATUS", "5");
//        pdUpdateTriggerDO.setAttribValue("statusRequired", statusRequired);
//
//        context.theEXEDataObjectStack.push(pdUpdateTriggerDO);
//        context.theSQLMgr.searchTriggerLibrary("PickDetail")).postUpdateFire();
//
//        EXEDataObject outDO = (EXEDataObject) context.theEXEDataObjectStack.stackList.get(1);


        HashMap <String,String> outHashMap = new HashMap<>();
        outHashMap.put("PICKDETAILKEY",pickDetailKey);
       //todo: outHashMap.put("ITRNKEY",outDO.getString("itrnkey"));
        return outHashMap;

    }


    public static void checkIdHoldStatusMatchOrderType( String orderKey, Map<String,String> lotxLocxIdInfo){

        Map<String, String> orderHashMap = Orders.findByOrderKey( orderKey, true);

        List<Map<String, String>> holdStatusHashMapList = DBHelper.executeQuery( "SELECT STATUSCODE from HOLDALLOCATIONMATRIX WHERE ORDERTYPE=?", new Object[]{
                orderHashMap.get("TYPE")
        });

        if(holdStatusHashMapList.size()>0) {


            List<String> orderTypeHoldStatusList = holdStatusHashMapList.stream().map(x -> x.get("STATUSCODE")).collect(Collectors.toList());


                List<String> holdStatuses = InventoryHelper.getHoldStatuses( lotxLocxIdInfo);

                if (!holdStatuses.stream().anyMatch(x -> orderTypeHoldStatusList.stream().anyMatch(y -> y.equals(x))))
                    ExceptionHelper.throwRfFulfillLogicException("当前容器条码的冻结状态不符合订单拣货要求");

        }else {
            //非冻结分配的出库类型可以出OK的库存
            if ("OK".equals(lotxLocxIdInfo.get("STATUS"))) {
                return;
            } else {
                ExceptionHelper.throwRfFulfillLogicException("当前容器条码/箱号已经冻结，不允许拣货");
            }
        }

    }


}
