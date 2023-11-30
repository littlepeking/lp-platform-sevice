/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 *
 *******************************************************************************/

package com.enhantec.wms.backend.core;

import com.enhantec.framework.common.exception.EHApplicationException;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.framework.common.utils.EHDateTimeHelper;
import com.enhantec.wms.backend.common.base.Loc;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.inventory.InventoryHold;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.inventory.VLotAttribute;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.common.outbound.PickDetail;
import com.enhantec.wms.backend.core.outbound.OutboundUtilHelper;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.IdGenerationHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
public class WMSOperations {

    public ServiceDataMap move(String fromId, String toId, String fromLoc, String toLoc, String lot,
                            BigDecimal qtyToBeMoved, BigDecimal fromQtyAllocChg, BigDecimal fromQtyPickedChg,
                            BigDecimal toQtyAllocChg, BigDecimal toQtyPickedChg, boolean autoShip,boolean saveItrn){
//        if (autoShip) {
//            if (qty.compareTo(toQtyPickedChg) != 0) throw new EHApplicationException("来源数量与目标拣货量不一致");
//            if (qty.compareTo(fromQtyAllocChg) != 0) throw new EHApplicationException("来源数量与来源分配量不一致");
//            if (fromQtyPickedChg.compareTo(BigDecimal.ZERO) != 0) throw new EHApplicationException("来源拣货量不能有值");
//            if (toQtyAllocChg.compareTo(BigDecimal.ZERO) != 0) throw new EHApplicationException("目标分配量不能有值");
//        }

//        //todo 考虑是否去掉如下逻辑，因为超拣和短拣可能会导致数量关系变化
//        BigDecimal from = fromQtyAllocChg.add(fromQtyPickedChg);
//        BigDecimal to = toQtyAllocChg.add(toQtyPickedChg);
//        if (from.compareTo(to) != 0) throw new EHApplicationException("来源分配拣货量与目标分配拣货量不一致");

        if(fromLoc.equals(toLoc) && fromId.equals(toId)) throw new EHApplicationException("容器已在目标库位，不需要移动");

        Loc.findById(fromLoc,true);
        if(!fromLoc.equals(toLoc)) Loc.findById(toLoc,true);
        VLotAttribute.findByLot(lot,true);
        if(UtilHelper.isEmpty(fromId)) ExceptionHelper.throwRfFulfillLogicException("移动的源容器号不能为空");


        Map<String, String> lotxLocxFromIdHashMap = DBHelper.getRecord( "SELECT A.STORERKEY, A.SKU, A.QTY, A.QTYALLOCATED, A.QTYPICKED "
                + " FROM LOTXLOCXID A WHERE LOT=? AND LOC=? AND ID=?", new Object[]{lot, fromLoc, fromId},"库存",false); // AND STATUS=?    ,fromStatus
        if (lotxLocxFromIdHashMap == null) throw new EHApplicationException("未找到可用库存");

        Map<String,String> locMap = DBHelper.getRecord( "SELECT LOCATIONTYPE FROM LOC WHERE LOC=?", new Object[]{toLoc}, "库位");

        boolean isFromIdHold = DBHelper.getCount("SELECT COUNT(1) FROM INVENTORYHOLD WHERE HOLD = 1 AND ID = ? ", new Object[]{fromId}) > 0;

        boolean isFromLocHold = DBHelper.getCount("SELECT COUNT(1) FROM INVENTORYHOLD WHERE HOLD = 1 AND LOC = ? ", new Object[]{fromLoc}) > 0;

        boolean isToLocHold = DBHelper.getCount("SELECT COUNT(1) FROM INVENTORYHOLD WHERE HOLD = 1 AND LOC = ? ", new Object[]{toLoc}) > 0;

        boolean isLotHold = DBHelper.getCount("SELECT COUNT(1) FROM INVENTORYHOLD WHERE HOLD = 1 AND LOT = ? ", new Object[]{lot}) > 0;

        String fromLotxLocxIdHoldStatus = isFromIdHold || isFromLocHold || isLotHold ? "HOLD":"OK";

//        long fromCount1 = DBHelper.getCount( "SELECT COUNT(1) FROM LOC WHERE LOCATIONFLAG=? AND LOC=?", new Object[]{"HOLD", fromLoc});
//        if (fromCount1 > 0) fromStatus = "HOLD";

        boolean toIdInvExist = DBHelper.getCount("SELECT COUNT(1) FROM LOTXLOCXID WHERE QTY > 0 AND ID=?", new Object[]{toId}) > 0;
        //如果目标容器不存在使用原容器、库位、批次冻结状态来计算，否则使用已存在的状态。
        if(!toIdInvExist){
            //如库存不存在，先清除toId的空库存记录和相关的INVENTORYHOLD冻结记录，为后续移动冻结的LPN到新的没库存的LPN做准备，保证冻结的库存在移动后仍被冻结。
            DBHelper.executeUpdate("DELETE FROM LOTXLOCXID WHERE QTY = 0 AND ID = ? ", new Object[]{toId});
            DBHelper.executeUpdate("DELETE FROM ID WHERE ID = ? ", new Object[]{toId});
            DBHelper.executeUpdate("DELETE FROM INVENTORYHOLD WHERE ID = ? ", new Object[]{toId});
        }

        //目标LPN本身是否被hold
        boolean isToIdHold = !toIdInvExist ? isFromIdHold : DBHelper.getCount("SELECT COUNT(1) FROM INVENTORYHOLD WHERE HOLD = 1 AND ID = ? ", new Object[]{toId}) > 0;

        String toLotxLocxIdHoldStatus = isToIdHold || isToLocHold || isLotHold ? "HOLD":"OK";


//        long toCount1 = DBHelper.getCount( "SELECT COUNT(1) FROM LOC WHERE LOCATIONFLAG=? AND LOC=?", new Object[]{"HOLD", toLoc});
//        if (toCount1 > 0) toStatus = "HOLD";

        String storerKey = lotxLocxFromIdHashMap.get("STORERKEY");
        String sku = lotxLocxFromIdHashMap.get("SKU");
        BigDecimal fromIdQty = new BigDecimal(lotxLocxFromIdHashMap.get("QTY"));
        BigDecimal fromIdQtyAllocated = new BigDecimal(lotxLocxFromIdHashMap.get("QTYALLOCATED"));
        BigDecimal fromIdQtyPicked = new BigDecimal(lotxLocxFromIdHashMap.get("QTYPICKED"));
        if (fromIdQty.compareTo(qtyToBeMoved) < 0) throw new EHApplicationException("待移动的数量"+qtyToBeMoved+"大于容器的当前库存数量"+fromIdQty);
        if (fromIdQtyAllocated.compareTo(fromQtyAllocChg) < 0) throw new EHApplicationException("库存分配量不足");
        //LOTXLOCXID中可以减少部分分配量（比如拣货时），但不存在部分移动拣货量的情况，但允许整体移动拣货至容器。
        if (fromQtyPickedChg.compareTo(BigDecimal.ZERO) > 0 &&
                ( fromQtyPickedChg.compareTo(fromIdQtyPicked) != 0 || qtyToBeMoved.compareTo(fromIdQty) != 0 )) throw new EHApplicationException("移动已拣货的容器时，不允许移动部分数量");
        BigDecimal fromIdAvailQty = fromIdQty.subtract(fromIdQtyAllocated).subtract(fromIdQtyPicked);
        BigDecimal expectedAvailQtyToBeMoved= qtyToBeMoved.subtract(fromQtyAllocChg).subtract(fromQtyPickedChg);
        if (fromIdAvailQty.compareTo(expectedAvailQtyToBeMoved) < 0) throw new EHApplicationException("可用库存不足,请检查容器是否被分配或拣货");


        String username = EHContextHelper.getUser().getUsername();
        LocalDateTime currentDate = EHDateTimeHelper.getCurrentDate();

        //------------------------------------------------------------------------------------
        //  LOTXLOCXID
        //------------------------------------------------------------------------------------
        DBHelper.executeUpdate( "UPDATE LOTXLOCXID SET QTY=QTY-?, QTYALLOCATED=QTYALLOCATED-?, QTYPICKED=QTYPICKED-?, EDITWHO=?, EDITDATE=? WHERE LOT=? AND LOC=? AND ID=?"
                , new Object[]{qtyToBeMoved, fromQtyAllocChg, fromQtyPickedChg, username, currentDate, lot, fromLoc, fromId});
        //------------------------------------------------------------------------------------
        if (!autoShip) {
            // 自动发运不增加目标数量
            if (DBHelper.getCount( "SELECT COUNT(1) FROM LOTXLOCXID WHERE LOT=? AND LOC=? AND ID=?", new Object[]{lot, toLoc, toId}) == 0) {

                Map<String, Object> lotxlocxid = new HashMap<>();
                lotxlocxid.put("STORERKEY", storerKey);
                lotxlocxid.put("SKU", sku);
                lotxlocxid.put("LOT", lot);
                lotxlocxid.put("LOC", toLoc);
                lotxlocxid.put("ID", toId);
                lotxlocxid.put("QTY", qtyToBeMoved);
                lotxlocxid.put("QTYALLOCATED", toQtyAllocChg);
                lotxlocxid.put("QTYPICKED", toQtyPickedChg);
                lotxlocxid.put("STATUS", toLotxLocxIdHoldStatus);
                lotxlocxid.put("ADDWHO", username);
                lotxlocxid.put("EDITWHO", username);
                DBHelper.executeInsert( "LOTXLOCXID", lotxlocxid);

            } else
                //TODO: 暂不对原LPN和新LPN冻结状态的一致性做校验，冻结状态暂以目标容器当前状态为准。相关逻辑可现在项目代码中实现，后续有必要再考虑抽离到核心逻辑。
                DBHelper.executeUpdate( "UPDATE LOTXLOCXID SET QTY=QTY+?, QTYALLOCATED=QTYALLOCATED+?, QTYPICKED=QTYPICKED+?, EDITWHO=?, EDITDATE=? WHERE LOT=? AND LOC=? AND ID=?"
                        , new Object[]{qtyToBeMoved, toQtyAllocChg, toQtyPickedChg, username, currentDate, lot, toLoc, toId});
        }

        //更新后检查LOTXLOCXID的数量，如果为0直接删除
        DBHelper.executeUpdate("DELETE FROM LOTXLOCXID WHERE LOT = ? AND LOC = ? AND ID = ? AND QTY = 0 ", new Object[]{lot, fromLoc, fromId});

        //------------------------------------------------------------------------------------
        //  ID
        //------------------------------------------------------------------------------------
        String packKey = DBHelper.getStringValue( "SELECT PACKKEY FROM IDNOTES WHERE ID=?", new Object[]{fromId}, "包装");

        DBHelper.executeUpdate( "UPDATE ID SET QTY=QTY-?, EDITWHO=?, EDITDATE=? WHERE ID=?", new Object[]{qtyToBeMoved, username, currentDate, fromId});

        // 自动发运不增加目标数量
        if (!autoShip) {
            if (!toIdInvExist) {
                Map<String, Object> id = new HashMap<>();
                id.put("ADDWHO", username);
                id.put("EDITWHO", username);
                id.put("ID", toId);
                id.put("QTY", qtyToBeMoved);
                id.put("STATUS", isToIdHold);
                id.put("PACKKEY", packKey);
                DBHelper.executeInsert( "ID", id);

                //当目标LPN为新记录时，也要复制关联的冻结记录。
                List<Map<String, Object>> fromIdInvHoldList = DBHelper.executeQueryRawData("SELECT * FROM INVENTORYHOLD WHERE HOLD = 1 AND ID = ? ", new Object[]{fromId});

                fromIdInvHoldList.forEach(invHoldRecord -> {

                    Map<String, Object> invHoldRec = new HashMap<>();
                    invHoldRec.put("INVENTORYHOLDKEY", IdGenerationHelper.getNCounterStrWithLength("INVENTORYHOLDKEY", 10));
                    invHoldRec.put("WHSEID", EHContextHelper.getCurrentOrgId());
                    invHoldRec.put("LOT", "");
                    invHoldRec.put("LOC", "");
                    invHoldRec.put("ID", toId);
                    invHoldRec.put("HOLD", 1);
                    invHoldRec.put("STATUS", invHoldRecord.get("STATUS"));
                    invHoldRec.put("DATEON", currentDate);
                    invHoldRec.put("WHOON", username);
                    invHoldRec.put("ADDWHO", username);
                    invHoldRec.put("EDITWHO", username);
                    DBHelper.executeInsert("INVENTORYHOLD", invHoldRec);

                });

            } else
                DBHelper.executeUpdate( "UPDATE ID SET QTY=QTY+?, EDITWHO=?, EDITDATE=? WHERE ID=?"
                        , new Object[]{qtyToBeMoved, username, currentDate, toId});
        }

        //更新后检查FROMID的数量，如果为0直接删除
        DBHelper.executeUpdate("DELETE FROM ID WHERE ID = ? AND QTY = 0 ", new Object[]{fromId});

        //------------------------------------------------------------------------------------
        //  SKUXLOC
        //------------------------------------------------------------------------------------
        if (DBHelper.getCount( "SELECT COUNT(1) FROM SKUXLOC  WHERE LOC=?  AND STORERKEY=? AND SKU=?", new Object[]{fromLoc, storerKey, sku}) == 0)
            throw new EHApplicationException("SKUXLOC表未找到数据");
        DBHelper.executeUpdate( "UPDATE SKUXLOC SET QTY=QTY-?, QTYALLOCATED=QTYALLOCATED-?, QTYPICKED=QTYPICKED-?, EDITWHO=?, EDITDATE=? WHERE  LOC=?  AND STORERKEY=? AND SKU=?"
                , new Object[]{qtyToBeMoved, fromQtyAllocChg, fromQtyPickedChg, username, currentDate, fromLoc, storerKey, sku});

        if (!autoShip) {
            // 自动发运不增加目标数量
            if (DBHelper.getCount( "SELECT COUNT(1) FROM SKUXLOC  WHERE  LOC=?  AND STORERKEY=? AND SKU=?", new Object[]{toLoc, storerKey, sku}) == 0) {
                Map<String, Object> skuxloc = new HashMap<>();
                skuxloc.put("ADDWHO", username);
                skuxloc.put("EDITWHO", username);
                skuxloc.put("STORERKEY", storerKey);
                skuxloc.put("SKU", sku);
                skuxloc.put("LOC", toLoc);
                skuxloc.put("QTY", qtyToBeMoved);
                skuxloc.put("QTYALLOCATED", toQtyAllocChg);
                skuxloc.put("QTYPICKED", toQtyPickedChg);
                skuxloc.put("LOCATIONTYPE", locMap.get("LOCATIONTYPE"));
                DBHelper.executeInsert( "SKUXLOC", skuxloc);
            } else
                DBHelper.executeUpdate( "UPDATE SKUXLOC SET QTY=QTY+?, QTYALLOCATED=QTYALLOCATED+?, QTYPICKED=QTYPICKED+?, EDITWHO=?, EDITDATE=? WHERE  LOC=?  AND STORERKEY=? AND SKU=?"
                        , new Object[]{qtyToBeMoved, toQtyAllocChg, toQtyPickedChg, username, currentDate, toLoc, storerKey, sku});
        }

        //更新后检查SKUXLOC数量，如果为0直接删除
        DBHelper.executeUpdate("DELETE FROM SKUXLOC WHERE STORERKEY = ? AND SKU = ? AND LOC = ? AND QTY = 0 ", new Object[]{storerKey,sku,fromLoc});


        //------------------------------------------------------------------------------------
        //  LOT
        //------------------------------------------------------------------------------------
        if (autoShip) {
            DBHelper.executeUpdate( "UPDATE LOT SET QTY=QTY-?, QTYALLOCATED=QTYALLOCATED-?, EDITWHO=?, EDITDATE=? WHERE LOT=?"
                    , new Object[]{qtyToBeMoved, qtyToBeMoved, username, currentDate, lot});
        } else {
            //LOT总量不会变化，分配量和拣货量需要根据实际情况调整
            if ((fromQtyAllocChg.compareTo(BigDecimal.ZERO) != 0) || (toQtyAllocChg.compareTo(BigDecimal.ZERO) != 0) || (fromQtyPickedChg.compareTo(BigDecimal.ZERO) != 0) || (toQtyPickedChg.compareTo(BigDecimal.ZERO) != 0)) {
                DBHelper.executeUpdate( "UPDATE LOT SET QTYALLOCATED=QTYALLOCATED-?+?, QTYPICKED=QTYPICKED-?+?, EDITWHO=?, EDITDATE=? WHERE LOT=?"
                        , new Object[]{fromQtyAllocChg, toQtyAllocChg, fromQtyPickedChg, toQtyPickedChg, username, currentDate, lot});
            }
            if (!fromLotxLocxIdHoldStatus.equals(toLotxLocxIdHoldStatus)) {
//                BigDecimal qtyOnHold = DBHelper.getDecimalValue( "SELECT SUM(QTY) FROM LOTXLOCXID WHERE STATUS = 'HOLD' AND LOT = ?", new Object[]{lot});
//                DBHelper.executeUpdate( "UPDATE LOT SET QTYONHOLD = ?, EDITWHO = ?, EDITDATE = ? WHERE LOT = ?"
//                        , new Object[]{qtyOnHold, username, currentDate, lot});

                BigDecimal qtyLotHoldChange;
                if("HOLD".equals(fromLotxLocxIdHoldStatus)){
                    //HOLD=>OK
                    qtyLotHoldChange = qtyToBeMoved.negate();
                }else {
                    //OK=>HOLD
                    qtyLotHoldChange = qtyToBeMoved;
                }
                DBHelper.executeUpdate("UPDATE LOT SET QTYONHOLD = QTYONHOLD + ?,EDITWHO=?,EDITDATE=? WHERE LOT=?", new Object[]{qtyLotHoldChange,username,currentDate,lot});

            }
        }


        //更新后检查LOT，如果为0直接删除
        DBHelper.executeUpdate("DELETE FROM LOT WHERE STORERKEY = ? AND LOT = ? AND QTY = 0 ", new Object[]{storerKey,lot});

        //交易记录

        Integer itrnKey = null;

        if(saveItrn){
            itrnKey = IdGenerationHelper.getNCounter("ITRNKEY");

            Map<String,Object> itrn = new HashMap<String,Object>();
            itrn.put("ADDWHO", username);
            itrn.put("EDITWHO", username);
            itrn.put("ITRNKEY", itrnKey);
            itrn.put("ITRNSYSID", "0");
            itrn.put("TRANTYPE", "MV");
            itrn.put("STORERKEY", storerKey);
            itrn.put("SKU", lot);
            itrn.put("LOT", lot);
            itrn.put("FROMLOC", fromLoc);
            itrn.put("FROMID", fromId);
            itrn.put("TOLOC", toLoc);
            itrn.put("TOID", toId);
            itrn.put("SOURCETYPE", "PICKING");
            itrn.put("QTY", qtyToBeMoved);
            itrn.put("STATUS","OK");
            itrn.put("UOMCALC", "0");
            itrn.put("INTRANSIT", "1");
//----------------------------------
            Map<String,Object> lotHashMap = VLotAttribute.findByLot(lot, true);

            itrn.put("LOTTABLE01", lotHashMap.get("LOTTABLE01"));
            itrn.put("LOTTABLE02", lotHashMap.get("LOTTABLE02"));
            itrn.put("LOTTABLE03", lotHashMap.get("LOTTABLE03"));
            itrn.put("LOTTABLE04", EHDateTimeHelper.getLocalDateStr((LocalDateTime) lotHashMap.get("LOTTABLE04")));
            itrn.put("LOTTABLE05", EHDateTimeHelper.getLocalDateStr((LocalDateTime) lotHashMap.get("LOTTABLE05")));
            itrn.put("LOTTABLE06", lotHashMap.get("LOTTABLE06"));
            itrn.put("LOTTABLE07", lotHashMap.get("LOTTABLE07"));
            itrn.put("LOTTABLE08", lotHashMap.get("LOTTABLE08"));
            itrn.put("LOTTABLE09", lotHashMap.get("LOTTABLE09"));
            itrn.put("LOTTABLE10", lotHashMap.get("LOTTABLE10"));
            itrn.put("LOTTABLE11", EHDateTimeHelper.getLocalDateStr((LocalDateTime) lotHashMap.get("LOTTABLE11")));
            itrn.put("LOTTABLE12", EHDateTimeHelper.getLocalDateStr((LocalDateTime) lotHashMap.get("LOTTABLE12")));
//---------------------------------
            DBHelper.executeInsert("ITRN", itrn);
        }


        ServiceDataMap serviceDataMap = new ServiceDataMap();
        serviceDataMap.setAttribValue("itrnKey", itrnKey);
        serviceDataMap.setAttribValue("toId", toId);

        return serviceDataMap;

    }

    public ServiceDataMap pick(String pickDetailKey,String toId, String toLoc,BigDecimal uomQtyToBePicked, String uom, boolean allowShortPick,boolean reduceOpenQtyAfterShortPick, boolean allowOverPick, boolean autoShip){

        //TODO 检查toId是否存在，如存在则继续检查该容器是否在PICKTO类型的库位并且该容器的数量必须等于拣货量，如不满足则不允许拣货至该容器。

        Map<String,String> pdHashMap = DBHelper.getRecord(
                "SELECT ORDERKEY,ORDERLINENUMBER,PICKDETAILKEY,QTY,STORERKEY,SKU,ID,LOC,LOT,STATUS,PACKKEY,UOM FROM PICKDETAIL where PICKDETAILKEY=?"
                , new String[]{pickDetailKey},"拣货明细");
        if (pdHashMap == null) throw new EHApplicationException("未找到拣货明细");

        String storerKey = pdHashMap.get("STORERKEY");
        String sku = pdHashMap.get("SKU");
        String packKey = pdHashMap.get("PACKKEY");
        String fromLoc = pdHashMap.get("LOC");
        String fromId = pdHashMap.get("ID");
        String lot = pdHashMap.get("LOT");
        String orderKey = pdHashMap.get("ORDERKEY");
        String orderLineNumber = pdHashMap.get("ORDERLINENUMBER");
        if(UtilHelper.isEmpty(toLoc)) toLoc = "PICKTO";


        int pdStatus = Integer.parseInt(pdHashMap.get("STATUS"));
        if (pdStatus < 0) throw new EHApplicationException("拣货明细当前状态异常,不允许拣货");
        if (pdStatus >= 5) throw new EHApplicationException("拣货明细当前状态显示已拣货,不允许拣货");
        //--检查订单状态-------------------------------
        String iOrderStatus = DBHelper.getStringValue("SELECT STATUS FROM ORDERS WHERE ORDERKEY=?", new Object[]{pdHashMap.get("ORDERKEY")}, "订单");
        if (iOrderStatus.equals("12")) throw new EHApplicationException("出库单状态预分配,不允许拣货");
        if (Integer.parseInt(iOrderStatus)<0) throw new EHApplicationException("出库单当前状态异常,不允许拣货");
        if (Integer.parseInt(iOrderStatus)>=95) throw new EHApplicationException("出库单当前状态已完成出库,不允许拣货");
        //--检查订单行状态-------------------------------
        String iOrderLineStatus = DBHelper.getStringValue("SELECT STATUS FROM ORDERDETAIL WHERE ORDERKEY = ? AND ORDERLINENUMBER = ? ", new Object[]{pdHashMap.get("ORDERKEY"), pdHashMap.get("ORDERLINENUMBER")}, "订单行");
        if (Integer.parseInt(iOrderLineStatus)<0) throw new EHApplicationException("出库单明细行当前状态异常,不允许拣货");
        if (Integer.parseInt(iOrderLineStatus)>=95) throw new EHApplicationException("出库单明细行当前状态已完成出库,不允许拣货");

        String toLocationType = DBHelper.getStringValue("SELECT LOCATIONTYPE FROM LOC WHERE LOC=?", new String[]{toLoc});
        if (!toLocationType.equals("PICKTO")) throw new EHApplicationException("拣货目标库位不存在或类型错误");


        Map<String, String> orderHashMap = Orders.findByOrderKey(pdHashMap.get("ORDERKEY"),true);
        Map<String, String> orderDetailHashMap = Orders.findOrderDetailByKey(pdHashMap.get("ORDERKEY"),pdHashMap.get("ORDERLINENUMBER"),true);

        if (!UtilHelper.isEmpty(orderDetailHashMap.get("IDREQUIRED")) && !orderDetailHashMap.get("IDREQUIRED").equals(fromId)) throw new EHApplicationException("已指定容器条码("+orderDetailHashMap.get("IDREQUIRED")+"),请按要求拣货");

        ////////////////////////
        // 检查物料是否冻结,(核对单据类型与冻结类型对应表) HOLDALLOCATIONMATRIX

        List<Object> holdReasonList = DBHelper.getValueList("SELECT STATUSCODE FROM HOLDALLOCATIONMATRIX WHERE ORDERTYPE = ?", new Object[]{orderHashMap.get("ORDERTYPE")},"冻结状态");

        Map<String,String> fromIdHashMap = LotxLocxId.findByLotAndId(lot, fromId,true);

        if (!fromIdHashMap.get("STATUS").equals("OK"))
        {
            if (holdReasonList.size() == 0) throw new EHApplicationException("当前订单类型不允许拣选冻结物料");
            boolean isMatch = false;
            for (Object holdReason: holdReasonList) {
                isMatch = DBHelper.getCount("SELECT COUNT(1) FROM INVENTORYHOLD WHERE STATUS=? AND HOLD = ? AND ( ID = ? OR LOC = ? OR LOT = ? )"
                        , new String[]{holdReason.toString(),"1",fromId,fromLoc,lot}) > 0;
                if(isMatch) break;
            }
            if (!isMatch) throw new EHApplicationException("期望拣选的物料冻结原因与订单要求不符");
        } else {
            //fromIdHashMap.get("STATUS")=="OK"
            if(holdReasonList.size()!=0 && !holdReasonList.contains("OK")) throw new EHApplicationException("当前订单类型不允许拣选非冻结的物料");
        }
        ////////////////////////

        BigDecimal qtyToBePicked = UOM.UOMQty2StdQty(packKey,uom, uomQtyToBePicked);

        boolean isFullLpnPick = qtyToBePicked.compareTo(new BigDecimal(fromIdHashMap.get("QTY"))) == 0;

//        if(toId == null) throw new EHApplicationException("拣货至容器号不允许为空");

        if(UtilHelper.isEmpty(toId)){
            if(isFullLpnPick){
                toId = fromId;
            }else {
                toId = IdGenerationHelper.generateIDByKeyName("CASEID",10);
            }
        }

        BigDecimal qtyPickDetailAlloc =  new BigDecimal(pdHashMap.get("QTY"));

        if(qtyPickDetailAlloc.compareTo(new BigDecimal(fromIdHashMap.get("QTYALLOCATED")))>0){
            throw new EHApplicationException("WMS数据异常:拣货明细待拣货数量" + qtyPickDetailAlloc + "大于和当前拣货容器" + fromId + "的分配量"+ fromIdHashMap.get("QTYALLOCATED"));
        }

        boolean shortPick = qtyToBePicked.compareTo(qtyPickDetailAlloc) < 0;
        boolean overPick = qtyToBePicked.compareTo(qtyPickDetailAlloc) > 0;

        if (shortPick && !allowShortPick) throw new EHApplicationException("系统配置不允许短拣,拣货失败");
        if (overPick && !allowOverPick) throw new EHApplicationException("系统配置不允许超拣,拣货失败");

        BigDecimal qtyAllocChg = qtyToBePicked.subtract(qtyPickDetailAlloc);

        BigDecimal qtyAvailable = DBHelper.getDecimalValue("SELECT QTY-QTYALLOCATED-QTYPICKED FROM LOTXLOCXID WHERE LOT=? AND LOC=? AND ID=?", new Object[]{lot,fromLoc,fromId });
        if (qtyAllocChg.compareTo(qtyAvailable)>0) throw new EHApplicationException("当前容器的最大可超拣数量为"+qtyAvailable.toString()+",不满足超拣需求");

        String username = EHContextHelper.getUser().getUsername();
        LocalDateTime currentDate = EHDateTimeHelper.getCurrentDate();

        //这里不需要调整库存分配量，在move时直接调整
//        if(qtyAllocChg.compareTo(BigDecimal.ZERO)!=0) {
//
//
//            DBHelper.executeUpdate("UPDATE LOTXLOCXID SET QTYALLOCATED = QTYALLOCATED + ? WHERE LOT = ? AND LOC = ? AND ID = ?"
//                    , new Object[]{qtyChg, lot, fromLoc, fromId});
//            DBHelper.executeUpdate("UPDATE SKUXLOC SET QTYALLOCATED = QTYALLOCATED + ? WHERE STORERKEY = ? AND LOC = ? AND  SKU = ?"
//                    , new Object[]{qtyChg, storerKey, fromLoc, sku});
//            DBHelper.executeUpdate("UPDATE LOT SET QTYALLOCATED=QTYALLOCATED+? WHERE LOT=?"
//                    , new Object[]{qtyChg, lot});
//
//            DBHelper.executeUpdate("UPDATE PICKDETAIL SET QTY = QTY + ?, ADJUSTEDQTY = ADJUSTEDQTY + ? WHERE PICKDETAILKEY = ?"
//                    , new Object[]{qtyAllocChg, qtyAllocChg, pickDetailKey});
//
//            DBHelper.executeUpdate("UPDATE TASKDETAIL SET QTY = QTY + ?, UOMQTY = UOMQTY + ? , uom = ?, WHERE STATUS < 9 AND PICKDETAILKEY = ? "
//                    , new Object[]{qtyAllocChg, UOM.Std2UOMQty(packKey, uom, qtyAllocChg), uom, pickDetailKey});
//
//            //todo 当短拣时，是否允许调整订单量
//            DBHelper.executeUpdate("UPDATE ORDERDETAIL SET OPENQTY = OPENQTY + ?, QTYALLOCATED = QTYALLOCATED + ?,ADJUSTEDQTY = ADJUSTEDQTY + ? WHERE ORDERKEY = ? AND ORDERLINENUMBER = ?"
//                    , new Object[]{qtyAllocChg, qtyAllocChg, qtyAllocChg, orderKey, orderLineNumber});
//
//        }

        //执行拣货移动
        move(fromId, toId, fromLoc, toLoc, lot, qtyToBePicked, qtyPickDetailAlloc, BigDecimal.ZERO,BigDecimal.ZERO,qtyToBePicked,autoShip,false);

        //完成拣货调整拣货明细和订单量
        DBHelper.executeUpdate("UPDATE PICKDETAIL SET QTY = QTY + ?, EDITWHO=?,EDITDATE=?,STATUS=?,LOC=?,ID=?,DROPID=? WHERE PICKDETAILKEY=?"
                    , new Object[]{qtyAllocChg, username,currentDate, autoShip ? 9 : 5 ,toLoc, toId, toId, pickDetailKey});

        DBHelper.executeUpdate("UPDATE TASKDETAIL SET QTY = QTY + ?, UOMQTY = UOMQTY + ? , UOM = ?, EDITWHO=?,EDITDATE=?,STATUS=?,TOLOC=?, TOID = ? WHERE STATUS < 9 AND  PICKDETAILKEY = ?"
                , new Object[]{qtyAllocChg, UOM.Std2UOMQty(packKey, uom, qtyAllocChg), uom, username,currentDate, 9, toLoc, toId, pickDetailKey});

//        Map<String,String> odHashMap=DBHelper.getRecord("SELECT STATUS,OPENQTY - QTYPICKED AS QTY FROM ORDERDETAIL  WHERE ORDERKEY=? AND ORDERLINENUMBER=?"
//                    ,  new String[]{pdHashMap.get("ORDERKEY"),pdHashMap.get("ORDERLINENUMBER")},"订单行");

//        String odStatus = autoShip ? "95": "55";
//
//        String  qtyUnpicked = UtilHelper.decimalStrSubtract(orderDetailHashMap.get("OPENQTY"), orderDetailHashMap.get("QTYPICKED"));
//
//        if (new BigDecimal(qtyUnpicked).compareTo(qtyPickDetailAlloc)!=0) odStatus = autoShip ? "92" : "25";
//
//        //以最大的状态值显示，如有部分发运和部分拣货两条拣货明细的情况，应显示92，而不是25
//        if (odHashMap.get("STATUS").compareTo(odStatus)>0) odStatus = odHashMap.get("STATUS");

//        if (autoShip)
//        {
//
//            if(shortPick && !reduceOpenQtyAfterShortPick){
//                DBHelper.executeUpdate("UPDATE ORDERDETAIL SET EDITWHO=?,EDITDATE=?,ACTUALSHIPDATE=?,STATUS=?,QTYALLOCATED=QTYALLOCATED-?,OPENQTY=OPENQTY-?,ADJUSTEDQTY = ADJUSTEDQTY + ? ,SHIPPEDQTY=SHIPPEDQTY+? WHERE ORDERKEY=? AND ORDERLINENUMBER=?"
//                        , new Object[]{username, currentDate, currentDate, odStatus, qtyPickDetailAlloc, qtyToBePicked, 0, qtyToBePicked, orderKey, orderLineNumber});
//            }else {
//                DBHelper.executeUpdate("UPDATE ORDERDETAIL SET EDITWHO=?,EDITDATE=?,ACTUALSHIPDATE=?,STATUS=?,QTYALLOCATED=QTYALLOCATED-?,OPENQTY=OPENQTY-?,ADJUSTEDQTY = ADJUSTEDQTY + ? ,SHIPPEDQTY=SHIPPEDQTY+? WHERE ORDERKEY=? AND ORDERLINENUMBER=?"
//                        , new Object[]{username, currentDate, currentDate, odStatus, qtyPickDetailAlloc, qtyPickDetailAlloc, qtyAllocChg, qtyToBePicked, orderKey, orderLineNumber});
//            }
//        }
//        else
//        {
//            if(shortPick && !reduceOpenQtyAfterShortPick){
//                DBHelper.executeUpdate("UPDATE ORDERDETAIL SET EDITWHO=?,EDITDATE=?,STATUS=?,OPENQTY = OPENQTY + ?,QTYALLOCATED=QTYALLOCATED-?,QTYPICKED=QTYPICKED+? , ADJUSTEDQTY = ADJUSTEDQTY + ? WHERE ORDERKEY=? AND ORDERLINENUMBER=?"
//                        , new Object[]{username, currentDate, odStatus, 0, qtyPickDetailAlloc, qtyToBePicked, orderKey,0, orderLineNumber});
//            }else {
//                DBHelper.executeUpdate("UPDATE ORDERDETAIL SET EDITWHO=?,EDITDATE=?,STATUS=?,OPENQTY = OPENQTY + ?,QTYALLOCATED=QTYALLOCATED-?,QTYPICKED=QTYPICKED+? , ADJUSTEDQTY = ADJUSTEDQTY + ? WHERE ORDERKEY=? AND ORDERLINENUMBER=?"
//                        , new Object[]{username, currentDate, odStatus, qtyAllocChg, qtyPickDetailAlloc, qtyToBePicked, qtyAllocChg, orderKey, orderLineNumber});
//            }
//        }

//        String orderStatus = autoShip ? "95": "55";

//        long odUnfulfilledCnt = DBHelper.getCount("SELECT COUNT(1) FROM ORDERDETAIL WHERE  ORDERKEY = ? AND STATUS < ? ", new String[]{orderKey, autoShip ? "95" : "55"});
//        if (odUnfulfilledCnt > 0)  orderStatus = autoShip ? "92" : "25";

//        if (autoShip)
//            DBHelper.executeUpdate("UPDATE ORDERS SET EDITWHO = ?, EDITDATE = ?, ACTUALSHIPDATE = ?, STATUS = ? WHERE ORDERKEY = ? "
//                    , new Object[]{username,currentDate,currentDate,orderStatus,orderKey});
//        else
//            DBHelper.executeUpdate("UPDATE ORDERS SET EDITWHO = ?, EDITDATE = ?, STATUS = ? WHERE ORDERKEY = ? "
//                    , new Object[]{username,currentDate, orderStatus,orderKey});

        if (autoShip)
        {

            if(shortPick && !reduceOpenQtyAfterShortPick){
                DBHelper.executeUpdate("UPDATE ORDERDETAIL SET EDITWHO=?,EDITDATE=?,ACTUALSHIPDATE=?,QTYALLOCATED=QTYALLOCATED-?,OPENQTY=OPENQTY-?,ADJUSTEDQTY = ADJUSTEDQTY + ? ,SHIPPEDQTY=SHIPPEDQTY+? WHERE ORDERKEY=? AND ORDERLINENUMBER=?"
                        , new Object[]{username, currentDate, currentDate, qtyPickDetailAlloc, qtyToBePicked, 0, qtyToBePicked, orderKey, orderLineNumber});
            }else {
                DBHelper.executeUpdate("UPDATE ORDERDETAIL SET EDITWHO=?,EDITDATE=?,ACTUALSHIPDATE=?,QTYALLOCATED=QTYALLOCATED-?,OPENQTY=OPENQTY-?,ADJUSTEDQTY = ADJUSTEDQTY + ? ,SHIPPEDQTY=SHIPPEDQTY+? WHERE ORDERKEY=? AND ORDERLINENUMBER=?"
                        , new Object[]{username, currentDate, currentDate, qtyPickDetailAlloc, qtyPickDetailAlloc, qtyAllocChg, qtyToBePicked, orderKey, orderLineNumber});
            }
        }
        else
        {
            if(shortPick && !reduceOpenQtyAfterShortPick){
                DBHelper.executeUpdate("UPDATE ORDERDETAIL SET EDITWHO=?,EDITDATE=?,OPENQTY = OPENQTY + ?,QTYALLOCATED=QTYALLOCATED-?,QTYPICKED=QTYPICKED+? , ADJUSTEDQTY = ADJUSTEDQTY + ? WHERE ORDERKEY=? AND ORDERLINENUMBER=?"
                        , new Object[]{username, currentDate, 0, qtyPickDetailAlloc, qtyToBePicked, orderKey,0, orderLineNumber});
            }else {
                DBHelper.executeUpdate("UPDATE ORDERDETAIL SET EDITWHO=?,EDITDATE=?,OPENQTY = OPENQTY + ?,QTYALLOCATED=QTYALLOCATED-?,QTYPICKED=QTYPICKED+? , ADJUSTEDQTY = ADJUSTEDQTY + ? WHERE ORDERKEY=? AND ORDERLINENUMBER=?"
                        , new Object[]{username, currentDate, qtyAllocChg, qtyPickDetailAlloc, qtyToBePicked, qtyAllocChg, orderKey, orderLineNumber});
            }
        }

        if (autoShip) DBHelper.executeUpdate("UPDATE ORDERS SET ACTUALSHIPDATE = ? WHERE ORDERKEY = ? " , new Object[]{currentDate,orderKey});

        OutboundUtilHelper.updateOrderDetailStatus(orderKey,orderLineNumber);
        OutboundUtilHelper.updateOrderStatus(orderKey);


        //交易记录

        int itrnKey = IdGenerationHelper.getNCounter("ITRNKEY");

        Map<String,Object> itrn = new HashMap<String,Object>();
        itrn.put("ADDWHO", username);
        itrn.put("EDITWHO", username);
        itrn.put("ITRNKEY", itrnKey);
        itrn.put("ITRNSYSID", "0");
        if (autoShip)
            itrn.put("TRANTYPE", "WD");
        else
            itrn.put("TRANTYPE", "MV");

        itrn.put("STORERKEY", storerKey);
        itrn.put("SKU", lot);
        itrn.put("LOT", lot);
        itrn.put("FROMLOC", fromLoc);
        itrn.put("FROMID", fromId);
        itrn.put("TOLOC", toLoc);
        itrn.put("TOID", toId);
        if (autoShip)
        {
            itrn.put("SOURCEKEY", orderKey+orderLineNumber);
            itrn.put("SOURCETYPE", "ntrPickDetailUpdate");
            itrn.put("QTY", "-"+qtyToBePicked);
            itrn.put("UOMQTY", "-"+uomQtyToBePicked);
        }
        else
        {
            itrn.put("SourceKey", pickDetailKey);
            itrn.put("SOURCETYPE", "PICKING");
            itrn.put("QTY", qtyToBePicked);
            itrn.put("UOMQTY", qtyToBePicked);
        }
        itrn.put("STATUS","OK");

        itrn.put("PACKKEY", packKey);
        itrn.put("UOM", uom);
        itrn.put("UOMCALC", "0");
        itrn.put("INTRANSIT", "1");

        Map<String,Object> lotHashMap = VLotAttribute.findByLot(lot, true);

        itrn.put("LOTTABLE01", lotHashMap.get("LOTTABLE01"));
        itrn.put("LOTTABLE02", lotHashMap.get("LOTTABLE02"));
        itrn.put("LOTTABLE03", lotHashMap.get("LOTTABLE03"));
        itrn.put("LOTTABLE04", EHDateTimeHelper.getLocalDateStr((LocalDateTime) lotHashMap.get("LOTTABLE04")));
        itrn.put("LOTTABLE05", EHDateTimeHelper.getLocalDateStr((LocalDateTime) lotHashMap.get("LOTTABLE05")));
        itrn.put("LOTTABLE06", lotHashMap.get("LOTTABLE06"));
        itrn.put("LOTTABLE07", lotHashMap.get("LOTTABLE07"));
        itrn.put("LOTTABLE08", lotHashMap.get("LOTTABLE08"));
        itrn.put("LOTTABLE09", lotHashMap.get("LOTTABLE09"));
        itrn.put("LOTTABLE10", lotHashMap.get("LOTTABLE10"));
        itrn.put("LOTTABLE11", EHDateTimeHelper.getLocalDateStr((LocalDateTime) lotHashMap.get("LOTTABLE11")));
        itrn.put("LOTTABLE12", EHDateTimeHelper.getLocalDateStr((LocalDateTime) lotHashMap.get("LOTTABLE12")));

        DBHelper.executeInsert("ITRN", itrn);

        ServiceDataMap serviceDataMap = new ServiceDataMap();
        serviceDataMap.setAttribValue("itrnKey", itrnKey);
        serviceDataMap.setAttribValue("toId", toId);

        return serviceDataMap;

    }




    /**
     * 添加拣货明细
     * @param orderKey
     * @param orderLineNumber
     * @param lot
     * @param id
     * @return pickDetailKey
     */
    public ServiceDataMap addPickDetail(String orderKey, String orderLineNumber, String lot, String id, String packKey, String uom, BigDecimal uomQty) {

        //TODO 校验拣货至ID未被其他容器使用

        BigDecimal qty = UOM.UOMQty2StdQty(packKey,uom, uomQty);

        String username = EHContextHelper.getUser().getUsername();
        LocalDateTime currentDate = EHDateTimeHelper.getCurrentDate();

        Map<String, String> lotxLocxIdHashMap = LotxLocxId.findByLotAndId(lot,id, true);

        String loc = lotxLocxIdHashMap.get("LOC");

        String qtyAvailableInLLI = UtilHelper.decimalStrSubtract(lotxLocxIdHashMap.get("QTY"), UtilHelper.decimalStrAdd(lotxLocxIdHashMap.get("QTYALLOCATED"), lotxLocxIdHashMap.get("QTYPICKED")));

        if(new BigDecimal(qtyAvailableInLLI).compareTo(qty)<0) throw new EHApplicationException("拣货明细添加失败:当前容器的库存可用数量小于待添加的拣货明细数量");

        Map<String, String> orderDetailHashMap = Orders.findOrderDetailByKey(orderKey,orderLineNumber,true);

        String qtyPickAndAlloc = UtilHelper.decimalStrAdd(orderDetailHashMap.get("QTYALLOCATED"), orderDetailHashMap.get("QTYPICKED"));

        if(UtilHelper.decimalStrCompare(qtyPickAndAlloc, orderDetailHashMap.get("OPENQTY")) == 0) throw new EHApplicationException("当前分配和拣货数量已经达到订单行的要求，不允许超量拣货");

        DBHelper.executeUpdate("UPDATE ORDERDETAIL SET EDITWHO = ?,EDITDATE = ?, QTYALLOCATED = QTYALLOCATED + ? WHERE ORDERKEY = ? AND ORDERLINENUMBER = ? "
                , new Object[] {username,currentDate,qty,orderKey,orderLineNumber});

        //处理超分配的情况（仅允许最后一个容器超分配）
        DBHelper.executeUpdate("UPDATE ORDERDETAIL SET OPENQTY = QTYALLOCATED + QTYPICKED WHERE OPENQTY < QTYALLOCATED + QTYPICKED AND ORDERKEY = ? AND ORDERLINENUMBER = ? "
                , new Object[] {username,currentDate,qty,orderKey,orderLineNumber});

        DBHelper.executeUpdate("UPDATE LOTXLOCXID SET QTYALLOCATED = QTYALLOCATED + ?, EDITWHO = ?, EDITDATE = ? WHERE LOT = ? AND LOC = ? AND ID = ? " , new Object[]{qty,username,currentDate,lot,loc,id});

        Map<String,Object> lotHashMap = VLotAttribute.findByLot(lot, true);

        String storerKey = lotHashMap.get("STORERKEY").toString();
        String sku = lotHashMap.get("SKU").toString();

        DBHelper.executeUpdate("UPDATE SKUXLOC SET QTYALLOCATED = QTYALLOCATED + ?,EDITWHO = ?,EDITDATE = ? WHERE LOC = ? AND STORERKEY = ? AND SKU = ?", new Object[]{qty,username,currentDate,loc,storerKey,sku});

        DBHelper.executeUpdate("UPDATE LOT SET QTYALLOCATED = QTYALLOCATED + ?,EDITWHO = ?,EDITDATE = ? WHERE LOT = ?", new Object[]{qty,username,currentDate,lot});

        String pickDetailKey = IdGenerationHelper.generateIDByKeyName("PICKDETAILKEY",10);
        String cartonId = IdGenerationHelper.generateIDByKeyName("CARTONID",10);

        String waveKey = buildWave(orderKey);


        ///////////
        Map<String,Object> pdHashMap = new HashMap<>();
        pdHashMap.put("ADDWHO", username);
        pdHashMap.put("EDITWHO", username);
        pdHashMap.put("PICKHEADERKEY", " ");
        pdHashMap.put("PICKDETAILKEY", pickDetailKey);
        pdHashMap.put("ORDERKEY", orderKey);
        pdHashMap.put("ORDERLINENUMBER", orderLineNumber);
        pdHashMap.put("STORERKEY", storerKey);
        pdHashMap.put("SKU", sku);
        pdHashMap.put("PACKKEY", packKey);
        pdHashMap.put("UOM", UOM.getUOMCode(packKey,uom));
        pdHashMap.put("QTY", qty);
        pdHashMap.put("UOMQTY", uomQty);
        pdHashMap.put("STATUS", "0");
        pdHashMap.put("LOT", lot);
        pdHashMap.put("FROMLOC", loc);
        pdHashMap.put("LOC", loc);
        pdHashMap.put("ID", id);
        pdHashMap.put("STATUSREQUIRED", "OK");
        pdHashMap.put("CARTONGROUP", "STD");
        pdHashMap.put("CARTONTYPE", "SMALL");
        pdHashMap.put("CASEID", cartonId);
        pdHashMap.put("TOLOC", "PICKTO");//TODO 后期改成根据通道门来设置
        pdHashMap.put("WAVEKEY",waveKey);
        pdHashMap.put("PICKMETHOD", 1);

        Map<String, String> locHashMap = DBHelper.getRecord("SELECT A.LOGICALLOCATION,A.PUTAWAYZONE FROM LOC A,PUTAWAYZONE B WHERE A.PUTAWAYZONE=B.PUTAWAYZONE AND A.LOC=?"
                ,new Object[] {loc},"库位");

//        pdHashMap.put("DOOR", locHashMap.get("PUTAWAYZONE"));
//        pdHashMap.put("ROUTE", locHashMap.get("LOGICALLOCATION"));

        DBHelper.executeInsert("PICKDETAIL", pdHashMap);

        OutboundUtilHelper.updateOrderDetailStatus(orderKey,orderLineNumber);
        OutboundUtilHelper.updateOrderStatus(orderKey);

        ServiceDataMap serviceDataMap = new ServiceDataMap();
        serviceDataMap.setAttribValue("pickDetailKey", pickDetailKey);

        return serviceDataMap;

    }

    /**
     * 删除拣货明细
     * @param pickDetailKey
     * @return pickDetailKey
     */
    public ServiceDataMap removePickDetail(String pickDetailKey) {

        String username = EHContextHelper.getUser().getUsername();
        LocalDateTime currentDate = EHDateTimeHelper.getCurrentDate();

        Map<String, String> pickDetailMap = PickDetail.findByPickDetailKey(pickDetailKey,true);

        String orderKey = pickDetailMap.get("ORDERKEY");
        String orderLineNumber = pickDetailMap.get("ORDERLINENUMBER");
        String lot = pickDetailMap.get("LOT");
        String loc = pickDetailMap.get("LOC");
        String id = pickDetailMap.get("ID");
        String status = pickDetailMap.get("STATUS");
        BigDecimal qty = new BigDecimal(pickDetailMap.get("QTY"));

        BigDecimal qtyAllocatedChg;
        BigDecimal qtyPickedChg;

        if(UtilHelper.decimalStrCompare(status,"5")<0){
            qtyAllocatedChg = qty;
            qtyPickedChg = BigDecimal.ZERO;
        }else if(status.equals("5")){
            qtyAllocatedChg = BigDecimal.ZERO;
            qtyPickedChg = qty;
        }else{
            throw new EHApplicationException("当前状态不允许删除拣货明细");
        }


        DBHelper.executeUpdate("UPDATE ORDERDETAIL SET EDITWHO = ?,EDITDATE = ?, QTYALLOCATED = QTYALLOCATED - ?, QTYPICKED = QTYPICKED - ? WHERE ORDERKEY = ? AND ORDERLINENUMBER = ? "
                , new Object[] {username,currentDate,qtyAllocatedChg, qtyPickedChg, orderKey, orderLineNumber});

        DBHelper.executeUpdate("UPDATE LOTXLOCXID SET QTYALLOCATED = QTYALLOCATED - ?, QTYPICKED = QTYPICKED - ? , EDITWHO = ?, EDITDATE = ? WHERE LOT = ? AND LOC = ? AND ID = ? "
                , new Object[]{qtyAllocatedChg, qtyPickedChg,username,currentDate,lot,loc,id});

        Map<String,Object> lotHashMap = VLotAttribute.findByLot(lot, true);

        String storerKey = lotHashMap.get("STORERKEY").toString();
        String sku = lotHashMap.get("SKU").toString();

        DBHelper.executeUpdate("UPDATE SKUXLOC SET QTYALLOCATED = QTYALLOCATED - ?, QTYPICKED = QTYPICKED - ? ,EDITWHO = ?,EDITDATE = ? WHERE LOC = ? AND STORERKEY = ? AND SKU = ?",
                new Object[]{qtyAllocatedChg, qtyPickedChg, username, currentDate, loc, storerKey, sku});

        DBHelper.executeUpdate("UPDATE LOT SET QTYALLOCATED = QTYALLOCATED - ?, QTYPICKED = QTYPICKED - ? ,EDITWHO = ?,EDITDATE = ? WHERE LOT = ?",
                new Object[]{qtyAllocatedChg, qtyPickedChg, username, currentDate, lot});

        DBHelper.executeUpdate("DELETE FROM PICKDETAIL WHERE PICKDETAILKEY = ?", new Object[]{pickDetailKey});
        DBHelper.executeUpdate("DELETE FROM TASKDETAIL WHERE PICKDETAILKEY = ?", new Object[]{pickDetailKey});

        OutboundUtilHelper.updateOrderDetailStatus(orderKey,orderLineNumber);
        OutboundUtilHelper.updateOrderStatus(orderKey);

        ServiceDataMap serviceDataMap = new ServiceDataMap();
        serviceDataMap.setAttribValue("pickDetailKey", pickDetailKey);

        return serviceDataMap;

    }

    //用于满足Legacy WMS发放任务的规则,创建wave
    private String buildWave(String orderKey) {

        String username = EHContextHelper.getUser().getUsername();

        String waveKey = null;
        String waveDetailKey = null;

        //如果存在拣货明细，使用当前使用的wavekey，否则创建新的。
        String currentWaveKey = DBHelper.getStringValue(
                "SELECT MAX(WAVEKEY) FROM PICKDETAIL WHERE ORDERKEY = ? ", new Object[]{ orderKey });

        if( currentWaveKey != null ){
            waveKey = currentWaveKey;

            waveDetailKey = DBHelper.getStringValue(
                    "SELECT WAVEDETAILKEY FROM WAVEDETAIL WHERE WAVEKEY = ? AND ORDERKEY = ? ", new Object[]{ waveKey, orderKey });

            if(waveDetailKey == null) {

                waveDetailKey = IdGenerationHelper.fillStringWithZero(Integer.parseInt(DBHelper.getStringValue(
                        "SELECT MAX(WAVEDETAILKEY)+1 FROM WAVEDETAIL WHERE WAVEKEY = ? AND ORDERKEY = ? ", new Object[]{waveKey, orderKey})), 10);

                DBHelper.executeUpdate("INSERT INTO WAVEDETAIL ( WAVEKEY,WAVEDETAILKEY,ORDERKEY,ADDWHO,EDITWHO) " +
                        "VALUES ( ?, ?, ?, ?, ?)", new Object[]{waveKey, waveDetailKey, orderKey, username, username});

            }

        }else {
            waveKey = IdGenerationHelper.getNCounterStrWithLength("WAVEKEY", 10);
            waveDetailKey = "0000000001";

            DBHelper.executeUpdate(
                    "INSERT INTO WAVE ( WAVEKEY, DESCR, WAVETYPE, STATUS, DISPATCHPALLETPICKMETHOD, DISPATCHCASEPICKMETHOD, DISPATCHPIECEPICKMETHOD,BATCHFLAG,AUTOBATCH,INPROCESS,ADDWHO,EDITWHO) "+
                            " VALUES ( ?, ?, '0', '0', '1', '3', '3', '0', '0', '0', ?, ?)", new Object[]{waveKey, "", username, username});


            DBHelper.executeUpdate("INSERT INTO WAVEDETAIL ( WAVEKEY,WAVEDETAILKEY,ORDERKEY,ADDWHO,EDITWHO) " +
                    "VALUES ( ?, ?, ?, ?, ?)", new Object[]{waveKey, waveDetailKey, orderKey, username, username});

        }

        return waveKey;
    }

    /**
     * 按拣货至容器发运
     */
    public void shipById(String id)
    {
        List<Map<String,String>> LotxLocxIdList = LotxLocxId.findMultiLotIdWithoutIDNotes(id);

        if (LotxLocxIdList.size() == 0) throw new EHApplicationException("未找到可发运的容器");

        LotxLocxIdList.forEach(e->{
            if(!Loc.findById(e.get("LOC"),true).get("LOCATIONTYPE").equals("PICKTO"))
            throw new EHApplicationException("当前容器存在于非拣货至库位，不允许发运");
        });

        List<Map<String,String>> pickDetailList = new ArrayList<>();

        for(Map<String,String> lotxLocxIdHashMap : LotxLocxIdList){

            if(UtilHelper.decimalStrCompare(lotxLocxIdHashMap.get("QTY"),lotxLocxIdHashMap.get("QTYPICKED"))!=0) throw new EHApplicationException("当前容器中存在非拣货状态的库存，不允许发运");

            //取得当前容器关联的拣货明细
            List<Map<String,String>> tempPickedPDDetails = DBHelper.executeQuery("SELECT * FROM PICKDETAIL WHERE ID=? AND LOC = ? AND LOT = ? AND STATUS>=5 AND STATUS<9", new Object[] {id, lotxLocxIdHashMap.get("LOC"),lotxLocxIdHashMap.get("LOT")});

            BigDecimal tempTotalPickedQty = BigDecimal.ZERO;
            for(Map<String,String> tempPickedPDDetail : tempPickedPDDetails){
                tempTotalPickedQty = tempTotalPickedQty.add(new BigDecimal(tempPickedPDDetail.get("QTY")));
            }

            if(tempTotalPickedQty.compareTo(new BigDecimal(lotxLocxIdHashMap.get("QTY")))!=0)
                throw new EHApplicationException("当前容器数量和拣货明细中的数量不匹配，请检查库存量和拣货明细量是否匹配");

            pickDetailList.addAll(tempPickedPDDetails);

        }

        HashSet<String> updatedOrderDetailKeys = new HashSet<>();

        pickDetailList.forEach(pdHashMap -> {
            shipByPickDetail(pdHashMap);
            updatedOrderDetailKeys.add(pdHashMap.get("ORDERLINENUMBER"));
        } );

        //一个待发运容器只能对应一个订单但可能用于多个订单行的落放ID
        updatedOrderDetailKeys.forEach(odKey -> OutboundUtilHelper.updateOrderDetailStatus(pickDetailList.get(0).get("ORDERKEY"), odKey));
        OutboundUtilHelper.updateOrderStatus(pickDetailList.get(0).get("ORDERKEY"));

    }



    /**
     * 按订单号发运
     * @param orderKey
     */
    public void shipByOrder(String orderKey, boolean allowPartialShip)
    {
        //16	部分分配/部分运送
        //27    部分发放/部分发货
        //53	部分拣选/部分运送
        //57	已全部拣货/部分运送
        //92	部分运送
        //95	出货全部完成

        if(!allowPartialShip){
            long unPickFinishedCount = DBHelper.getCount("SELECT COUNT(*) FROM ORDERDETAIL WHERE ORDERKEY=? AND STATUS<55", new Object[] {orderKey});
            if(unPickFinishedCount>0) throw new EHApplicationException("存在未拣货完成的订单明细行，不允许发运");
        }

        List<Map<String,String>> pickDetails = DBHelper.executeQuery("SELECT * FROM PICKDETAIL WHERE ORDERKEY=? AND STATUS>=5 AND STATUS<9", new Object[] {orderKey});
        if (pickDetails == null) throw new EHApplicationException("未找到待发运的拣货明细");

        pickDetails.forEach(pdHashMap ->  shipByPickDetail(pdHashMap));

        String SQL="SELECT * FROM ORDERDETAIL WHERE ORDERKEY = ? AND STATUS < 95";
        List<Map<String,String>> orderDetails = DBHelper.executeQuery( SQL, new Object[]{ orderKey});

        orderDetails.forEach( od-> OutboundUtilHelper.updateOrderDetailStatus(orderKey, od.get("ORDERLINENUMBER")));
        OutboundUtilHelper.updateOrderStatus(orderKey);

    }

    private void shipByPickDetail(Map<String, String> pdHashMap) {

        if(!Loc.findById(pdHashMap.get("LOC"),true).get("LOCATIONTYPE").equals("PICKTO"))
                throw new EHApplicationException("当前容器存在于非拣货至库位，不允许发运");

        String username = EHContextHelper.getUser().getUsername();
        LocalDateTime currentDate = EHDateTimeHelper.getCurrentDate();

        Map<String,String> LotxLocxIdHashMap = LotxLocxId.findByLotAndId(pdHashMap.get("LOT"), pdHashMap.get("ID"),true);
        Map<String,Object> lotHashMap = VLotAttribute.findByLot(pdHashMap.get("LOT"),true);
        //------------------------------------------------------------------------------------
        DBHelper.executeUpdate("UPDATE LOTXLOCXID SET QTY=QTY-?,QTYPICKED=QTYPICKED-?,EDITWHO=?,EDITDATE=? WHERE LOT=? AND LOC=? AND ID=?"
                , new Object[]{pdHashMap.get("QTY"), pdHashMap.get("QTY"), username, currentDate, pdHashMap.get("LOT"), pdHashMap.get("LOC"), pdHashMap.get("ID")});
        //------------------------------------------------------------------------------------

        boolean isLpnHold = LotxLocxIdHashMap.get("STATUS").equals("HOLD");

        DBHelper.executeUpdate( "UPDATE LOT SET QTY=QTY - ?,QTYPICKED = QTYPICKED - ?,EDITWHO = ?,EDITDATE = ?,QTYONHOLD = QTYONHOLD - ? WHERE LOT = ?"
                , new Object[]{pdHashMap.get("QTY"), pdHashMap.get("QTY"), username, currentDate,isLpnHold ? pdHashMap.get("QTY") : 0, pdHashMap.get("LOT")});
        //------------------------------------------------------------------------------------
        DBHelper.executeUpdate("UPDATE ID SET QTY = QTY - ? , EDITWHO = ?,EDITDATE = ? WHERE ID = ?"
                , new Object[]{pdHashMap.get("QTY"), username, currentDate, pdHashMap.get("ID")});
        //------------------------------------------------------------------------------------
        DBHelper.executeUpdate( "UPDATE SKUXLOC SET QTY = QTY - ?, QTYPICKED = QTYPICKED - ?,EDITWHO = ?, EDITDATE = ? WHERE LOC = ? AND STORERKEY = ? AND SKU = ?"
                , new Object[]{pdHashMap.get("QTY"), pdHashMap.get("QTY"), username, currentDate, pdHashMap.get("LOC"), pdHashMap.get("STORERKEY"), pdHashMap.get("SKU")});
        //------------------------------------------------------------------------------------
        DBHelper.executeUpdate( "UPDATE PICKDETAIL SET STATUS=?,EDITWHO=?,EDITDATE=? WHERE PICKDETAILKEY=?"
                , new Object[]{"9", username, currentDate, pdHashMap.get("PICKDETAILKEY")});

        DBHelper.executeUpdate( "UPDATE ORDERDETAIL SET EDITWHO=?,EDITDATE=?,ACTUALSHIPDATE=?,OPENQTY=OPENQTY-?,QTYPICKED=QTYPICKED-?,SHIPPEDQTY=SHIPPEDQTY+? WHERE ORDERKEY=? AND ORDERLINENUMBER=?"
                , new Object[]{username,currentDate,currentDate,pdHashMap.get("QTY"),pdHashMap.get("QTY"),pdHashMap.get("QTY"),pdHashMap.get("ORDERKEY"),pdHashMap.get("ORDERLINENUMBER")});

        int itrnKey = IdGenerationHelper.getNCounter("ITRNKEY");
        Map<String,Object> itrn = new HashMap<>();
        itrn.put("ADDWHO", username);
        itrn.put("EDITWHO", username);
        itrn.put("ITRNKEY", itrnKey);
        itrn.put("ITRNSYSID", "0");
        itrn.put("TRANTYPE", "WD");
        itrn.put("STORERKEY", pdHashMap.get("STORERKEY"));
        itrn.put("SKU", pdHashMap.get("SKU"));
        itrn.put("LOT", pdHashMap.get("LOT"));
        itrn.put("FROMLOC", " ");
        itrn.put("FROMID", pdHashMap.get("ID"));
        itrn.put("TOLOC", pdHashMap.get("LOC"));
        itrn.put("TOID", pdHashMap.get("ID"));
        itrn.put("SOURCEKEY", pdHashMap.get("ORDERKEY")+ pdHashMap.get("ORDERLINENUMBER"));
        itrn.put("SOURCETYPE", "ShipByPickDetail");
        itrn.put("QTY", "-"+ pdHashMap.get("QTY"));
        itrn.put("UOMQTY", "-"+ pdHashMap.get("QTY"));
        itrn.put("PACKKEY", pdHashMap.get("PACKKEY"));
        itrn.put("UOM", "EA");
        itrn.put("UOMCALC", "0");
        itrn.put("INTRANSIT", "1");
        itrn.put("STATUS", "OK");
        itrn.put("LOTTABLE01", lotHashMap.get("LOTTABLE01"));
        itrn.put("LOTTABLE02", lotHashMap.get("LOTTABLE02"));
        itrn.put("LOTTABLE03", lotHashMap.get("LOTTABLE03"));
        itrn.put("LOTTABLE04", EHDateTimeHelper.getLocalDateStr((LocalDateTime) lotHashMap.get("LOTTABLE04")));
        itrn.put("LOTTABLE05", EHDateTimeHelper.getLocalDateStr((LocalDateTime) lotHashMap.get("LOTTABLE05")));
        itrn.put("LOTTABLE06", lotHashMap.get("LOTTABLE06"));
        itrn.put("LOTTABLE07", lotHashMap.get("LOTTABLE07"));
        itrn.put("LOTTABLE08", lotHashMap.get("LOTTABLE08"));
        itrn.put("LOTTABLE09", lotHashMap.get("LOTTABLE09"));
        itrn.put("LOTTABLE10", lotHashMap.get("LOTTABLE10"));
        itrn.put("LOTTABLE11", EHDateTimeHelper.getLocalDateStr((LocalDateTime) lotHashMap.get("LOTTABLE11")));
        itrn.put("LOTTABLE12", EHDateTimeHelper.getLocalDateStr((LocalDateTime) lotHashMap.get("LOTTABLE12")));
        //---------------------------------
        DBHelper.executeInsert("ITRN", itrn);
    }


    //TODO 低优先级，待实现
    public void switchLpn(String taskDetailKey, String lot, String loc, String id){


        //Unallocate时要考虑删除关联的任务明细。
        //allocate时要看目标容器是否存在分配量，如存在则要看相关的分配量是否已经在拣货中，如在拣货，则不允许交换托盘。


        //逻辑：先unallocate oldId和newId 再 allocate oldId和newId

        //update task detail

        String username = EHContextHelper.getUser().getUsername();
        LocalDateTime currentDate = EHDateTimeHelper.getCurrentDate();

        DBHelper.executeUpdate("UPDATE TASKMANAGERSKIPTASKS SET LOT = ?, LOC = ?, ID = ?, EDITWHO=?,EDITDATE=? WHERE TASKDETAILKEY = ? ",
                new Object[] {lot,loc,id,username, currentDate, taskDetailKey});

    }






}
