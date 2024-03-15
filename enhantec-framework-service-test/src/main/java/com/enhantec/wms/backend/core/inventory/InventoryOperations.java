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

package com.enhantec.wms.backend.core.inventory;

import com.enhantec.framework.common.exception.EHApplicationException;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.framework.common.utils.EHDateTimeHelper;
import com.enhantec.wms.backend.common.base.Loc;
import com.enhantec.wms.backend.common.inventory.InventoryHold;
import com.enhantec.wms.backend.common.inventory.Lot;
import com.enhantec.wms.backend.common.inventory.LotAttribute;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.common.outbound.PickDetail;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.IdGenerationHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
@AllArgsConstructor
public class InventoryOperations {

    static public String HOLD = "HOLD";
    static public String OK = "OK";


    /**
     * 移动
     * @param fromId 来源ID
     * @param toId  目标ID
     * @param fromLoc 来源库位
     * @param toLoc  目标库位
     * @param fromLot 来源批次
     * @param toLot 目标批次
     * @param qtyToBeMoved  待移动量
     * @param fromQtyAllocChg fromId此次需要减少的分配量(目前用于拣货时扣量使用)
     * @param fromQtyPickedChg fromId此次需要减少的拣货量(仅用于整体移动拣货至ID使用，仅留出接口，移动拣货至ID的功能待开发)
     * @param toQtyAllocChg toId此次需要增加的分配量（目前用于移动带分配量的整容器）
     * @param toQtyPickedChg toId此次需要增加的拣货量（目前用于拣货增加拣货量使用）
     * @param saveItrn 是否保持交易记录
     * @return 返回交易记录ID和目标ID
     */
    public ServiceDataMap move(String fromId, String toId, String fromLoc, String toLoc, String fromLot,String toLot,
                               BigDecimal qtyToBeMoved, BigDecimal fromQtyAllocChg, BigDecimal fromQtyPickedChg,
                               BigDecimal toQtyAllocChg, BigDecimal toQtyPickedChg, boolean saveItrn){
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

        if(fromLoc.equals(toLoc) && fromId.equals(toId) && fromLot.equals(toLot) ) throw new EHApplicationException("容器已在目标库位，不需要移动");

        Loc.findById(fromLoc,true);
        if(!fromLoc.equals(toLoc)) Loc.findById(toLoc,true);
        Map<String,Object> fromLotAttrHashMap = LotAttribute.findWithEntByLot(fromLot,true);
        Map<String,Object> toLotAttrHashMap = LotAttribute.findWithEntByLot(toLot,true);
        if(UtilHelper.isEmpty(fromId)) ExceptionHelper.throwRfFulfillLogicException("移动的源容器号不能为空");


        Map<String, String> lotxLocxFromIdHashMap = DBHelper.getRecord( "SELECT A.STORERKEY, A.SKU, A.QTY, A.QTYALLOCATED, A.QTYPICKED "
                + " FROM LOTXLOCXID A WHERE LOT=? AND LOC=? AND ID=?", new Object[]{fromLot, fromLoc, fromId},"库存",false); // AND STATUS=?    ,fromStatus
        if (lotxLocxFromIdHashMap == null) throw new EHApplicationException("未找到可用库存");

        Map<String,String> locMap = DBHelper.getRecord( "SELECT LOCATIONTYPE FROM LOC WHERE LOC=?", new Object[]{toLoc}, "库位");

        boolean isFromIdHold = DBHelper.getCount("SELECT COUNT(1) FROM INVENTORYHOLD WHERE HOLD = 1 AND ID = ? ", new Object[]{fromId}) > 0;

        boolean isFromLocHold = DBHelper.getCount("SELECT COUNT(1) FROM INVENTORYHOLD WHERE HOLD = 1 AND LOC = ? ", new Object[]{fromLoc}) > 0;

        boolean isToLocHold = DBHelper.getCount("SELECT COUNT(1) FROM INVENTORYHOLD WHERE HOLD = 1 AND LOC = ? ", new Object[]{toLoc}) > 0;

        boolean isFromLotHold = DBHelper.getCount("SELECT COUNT(1) FROM INVENTORYHOLD WHERE HOLD = 1 AND LOT = ? ", new Object[]{fromLot}) > 0;

        boolean isToLotHold = DBHelper.getCount("SELECT COUNT(1) FROM INVENTORYHOLD WHERE HOLD = 1 AND LOT = ? ", new Object[]{toLot}) > 0;


        String fromLotxLocxIdHoldStatus = isFromIdHold || isFromLocHold || isFromLotHold ? "HOLD":"OK";

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

        String toLotxLocxIdHoldStatus = isToIdHold || isToLocHold || isToLotHold ? "HOLD":"OK";

//        long toCount1 = DBHelper.getCount( "SELECT COUNT(1) FROM LOC WHERE LOCATIONFLAG=? AND LOC=?", new Object[]{"HOLD", toLoc});
//        if (toCount1 > 0) toStatus = "HOLD";

        String storerKey = lotxLocxFromIdHashMap.get("STORERKEY");
        String sku = lotxLocxFromIdHashMap.get("SKU");
        BigDecimal fromIdQty = new BigDecimal(lotxLocxFromIdHashMap.get("QTY"));
        BigDecimal fromIdQtyAllocated = new BigDecimal(lotxLocxFromIdHashMap.get("QTYALLOCATED"));
        BigDecimal fromIdQtyPicked = new BigDecimal(lotxLocxFromIdHashMap.get("QTYPICKED"));
        if (fromIdQty.compareTo(qtyToBeMoved) < 0) throw new EHApplicationException("待移动的数量"+qtyToBeMoved+"大于容器的当前库存数量"+fromIdQty);
        //fromQtyAllocChg>0代表此次为拣货操作，需要减少分配数量，因此要保证分配量大于此次拣货的变化量
        if (fromQtyAllocChg.compareTo(BigDecimal.ZERO) > 0 && fromIdQtyAllocated.compareTo(fromQtyAllocChg) < 0) throw new EHApplicationException("库存分配量不足");
        //fromQtyPickedChg>0代表此次为发运或者移动整容器操作，此时不允许发运或移动部分拣货量，只允许整体操作容器。
        if (fromQtyPickedChg.compareTo(BigDecimal.ZERO) > 0 &&
                ( fromQtyPickedChg.compareTo(fromIdQtyPicked) != 0 || qtyToBeMoved.compareTo(fromIdQty) != 0 )) throw new EHApplicationException("移动已拣货的容器时，不允许移动部分数量");
        BigDecimal fromIdAvailQty = fromIdQty.subtract(fromIdQtyAllocated).subtract(fromIdQtyPicked);
        // qtyToBeMoved-fromQtyAllocChg代表抛去这次拣货量外，还需要多少可用库存满足移动的要求。
        //举例:当前id的分配量归属于多条拣货明细，当拣货明细1进行拣货时，只会把它自己的拣货量传入fromQtyAllocChg，因此通过下述计算expectedAvailQtyToBeMoved就会得出除原拣货明细要求外的额外需要移动的拣货数量。此数量不能大于当前id的可用量，否则会影响其他拣货明细。
        //根据上述校验的条件要求，对于存在拣货量的id移动存在两种场景：
        // 1.当fromQtyPickedChg=0时为仅移动可用量，此时 qtyToBeMoved == expectedAvailQtyToBeMoved <= fromIdAvailQty
        // 2.当fromQtyPickedChg>0时为移动整个容器，此时 qtyToBeMoved == fromIdQty 且 expectedAvailQtyToBeMoved == fromIdAvailQty
        BigDecimal expectedAvailQtyToBeMoved = qtyToBeMoved.subtract(fromQtyAllocChg).subtract(fromQtyPickedChg);
        if (fromIdAvailQty.compareTo(expectedAvailQtyToBeMoved) < 0) throw new EHApplicationException("容器"+fromIdAvailQty+"可用库存不足,移动失败");

        //如果目标批次和源批次不同（批属性转移的情况）：
        // 1、 如为正常库存：则源批次下的可移动量要小于该批次正常库存的可用量(不包含冻结库存的可用量)-源批次下动态拣货的分配量
        // 2、 如为冻结库存：则源批次下的可移动量要小于该批次冻结库存的可用量
        if(!fromLot.equals(toLot)) {
            Map<String, Object> lotMap = Lot.findById(fromLot);

            BigDecimal qtyUnHoldAvailByLot = (BigDecimal) lotMap.get("QTYAVAIL");
            BigDecimal qtyOnHoldAvailByLot = (BigDecimal) lotMap.get("QTYONHOLD");

            if(HOLD.equals(fromLotxLocxIdHoldStatus) && qtyOnHoldAvailByLot.compareTo(expectedAvailQtyToBeMoved) < 0
            || OK.equals(fromLotxLocxIdHoldStatus) && qtyUnHoldAvailByLot.compareTo(expectedAvailQtyToBeMoved) < 0
            ) {
                throw new EHApplicationException("待移动的批次" + fromLot + "可用库存不足，移动失败");
            }

        }


        String username = EHContextHelper.getUser().getUsername();
        LocalDateTime currentDate = EHDateTimeHelper.getCurrentDate();

        //------------------------------------------------------------------------------------
        //  LOTXLOCXID
        //------------------------------------------------------------------------------------
        DBHelper.executeUpdate( "UPDATE LOTXLOCXID SET QTY=QTY-?, QTYALLOCATED=QTYALLOCATED-?, QTYPICKED=QTYPICKED-?, EDITWHO=?, EDITDATE=? WHERE LOT=? AND LOC=? AND ID=?"
                , new Object[]{qtyToBeMoved, fromQtyAllocChg, fromQtyPickedChg, username, currentDate, fromLot, fromLoc, fromId});
        //------------------------------------------------------------------------------------
        if (DBHelper.getCount("SELECT COUNT(1) FROM LOTXLOCXID WHERE LOT=? AND LOC=? AND ID=?", new Object[]{toLot, toLoc, toId}) == 0) {

            Map<String, Object> lotxlocxid = new HashMap<>();
            lotxlocxid.put("STORERKEY", storerKey);
            lotxlocxid.put("SKU", sku);
            lotxlocxid.put("LOT", toLot);
            lotxlocxid.put("LOC", toLoc);
            lotxlocxid.put("ID", toId);
            lotxlocxid.put("QTY", qtyToBeMoved);
            lotxlocxid.put("QTYALLOCATED", toQtyAllocChg);
            lotxlocxid.put("QTYPICKED", toQtyPickedChg);
            lotxlocxid.put("STATUS", toLotxLocxIdHoldStatus);
            lotxlocxid.put("ADDWHO", username);
            lotxlocxid.put("EDITWHO", username);
            DBHelper.executeInsert("LOTXLOCXID", lotxlocxid);

        } else
            DBHelper.executeUpdate("UPDATE LOTXLOCXID SET QTY=QTY+?, QTYALLOCATED=QTYALLOCATED+?, QTYPICKED=QTYPICKED+?, STATUS = ?, EDITWHO=?, EDITDATE=? WHERE LOT=? AND LOC=? AND ID=?"
                    , new Object[]{qtyToBeMoved, toQtyAllocChg, toQtyPickedChg, toLotxLocxIdHoldStatus, username, currentDate, toLot, toLoc, toId});

        //更新后检查LOTXLOCXID的数量，如果为0直接删除
        DBHelper.executeUpdate("DELETE FROM LOTXLOCXID WHERE LOT = ? AND LOC = ? AND ID = ? AND QTY = 0 ", new Object[]{fromLot, fromLoc, fromId});

        //------------------------------------------------------------------------------------
        //  ID
        //------------------------------------------------------------------------------------
        String packKey = DBHelper.getStringValue( "SELECT PACKKEY FROM IDNOTES WHERE ID=?", new Object[]{fromId}, "包装");

        DBHelper.executeUpdate( "UPDATE ID SET QTY=QTY-?, EDITWHO=?, EDITDATE=? WHERE ID=?", new Object[]{qtyToBeMoved, username, currentDate, fromId});

        if (!toIdInvExist) {
            Map<String, Object> id = new HashMap<>();
            id.put("ADDWHO", username);
            id.put("EDITWHO", username);
            id.put("ID", toId);
            id.put("QTY", qtyToBeMoved);
            id.put("STATUS", isToIdHold);
            id.put("PACKKEY", packKey);
            DBHelper.executeInsert("ID", id);

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
            DBHelper.executeUpdate("UPDATE ID SET QTY=QTY+?, EDITWHO=?, EDITDATE=? WHERE ID=?"
                    , new Object[]{qtyToBeMoved, username, currentDate, toId});


        //更新后检查FROMID的数量，如果为0直接删除
        DBHelper.executeUpdate("DELETE FROM ID WHERE ID = ? AND QTY = 0 ", new Object[]{fromId});

        //------------------------------------------------------------------------------------
        //  SKUXLOC
        //------------------------------------------------------------------------------------
        if (DBHelper.getCount( "SELECT COUNT(1) FROM SKUXLOC  WHERE LOC=?  AND STORERKEY=? AND SKU=?", new Object[]{fromLoc, storerKey, sku}) == 0)
            throw new EHApplicationException("SKUXLOC表未找到数据");
        DBHelper.executeUpdate( "UPDATE SKUXLOC SET QTY=QTY-?, QTYALLOCATED=QTYALLOCATED-?, QTYPICKED=QTYPICKED-?, EDITWHO=?, EDITDATE=? WHERE  LOC=?  AND STORERKEY=? AND SKU=?"
                , new Object[]{qtyToBeMoved, fromQtyAllocChg, fromQtyPickedChg, username, currentDate, fromLoc, storerKey, sku});

        if (DBHelper.getCount("SELECT COUNT(1) FROM SKUXLOC  WHERE  LOC=?  AND STORERKEY=? AND SKU=?", new Object[]{toLoc, storerKey, sku}) == 0) {
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
            DBHelper.executeInsert("SKUXLOC", skuxloc);
        } else
            DBHelper.executeUpdate("UPDATE SKUXLOC SET QTY=QTY+?, QTYALLOCATED=QTYALLOCATED+?, QTYPICKED=QTYPICKED+?, EDITWHO=?, EDITDATE=? WHERE  LOC=?  AND STORERKEY=? AND SKU=?"
                    , new Object[]{qtyToBeMoved, toQtyAllocChg, toQtyPickedChg, username, currentDate, toLoc, storerKey, sku});


        //更新后检查SKUXLOC数量，如果为0直接删除
        DBHelper.executeUpdate("DELETE FROM SKUXLOC WHERE STORERKEY = ? AND SKU = ? AND LOC = ? AND QTY = 0 ", new Object[]{storerKey,sku,fromLoc});


        //------------------------------------------------------------------------------------
        //  LOT
        //------------------------------------------------------------------------------------

        BigDecimal fromQtyHoldChg = fromLotxLocxIdHoldStatus.equals(HOLD) ? qtyToBeMoved : BigDecimal.ZERO;
        BigDecimal toQtyHoldChg = toLotxLocxIdHoldStatus.equals(HOLD) ? qtyToBeMoved : BigDecimal.ZERO;

        if (!fromLot.equals(toLot)
                || (fromQtyHoldChg.compareTo(toQtyHoldChg) != 0)
                || (fromQtyAllocChg.compareTo(toQtyAllocChg) != 0)
                || (fromQtyPickedChg.compareTo(toQtyPickedChg) != 0))
        {
            DBHelper.executeUpdate("UPDATE LOT SET QTY= QTY-?, QTYALLOCATED=QTYALLOCATED-?, QTYPICKED=QTYPICKED-?, QTYONHOLD = QTYONHOLD-?, EDITWHO=?, EDITDATE=? WHERE LOT=?"
                    , new Object[]{qtyToBeMoved, fromQtyAllocChg, fromQtyPickedChg, fromQtyHoldChg, username, currentDate, fromLot});

            if (DBHelper.getCount("SELECT COUNT(1) FROM LOT  WHERE  LOT=? ", new Object[]{toLot}) == 0) {
                Map<String, Object> lotHashMap = new HashMap<>();
                lotHashMap.put("ADDWHO", username);
                lotHashMap.put("EDITWHO", username);
                lotHashMap.put("STORERKEY", storerKey);
                lotHashMap.put("SKU", sku);
                lotHashMap.put("LOT", toLot);
                lotHashMap.put("QTY", qtyToBeMoved);
                lotHashMap.put("QTYALLOCATED", toQtyAllocChg);
                lotHashMap.put("QTYPICKED", toQtyPickedChg);
                lotHashMap.put("QTYONHOLD", toQtyHoldChg);
                lotHashMap.put("STATUS", isToLotHold? "HOLD":"OK");
                DBHelper.executeInsert("LOT", lotHashMap);
            } else

            DBHelper.executeUpdate("UPDATE LOT SET QTY= QTY+?, QTYALLOCATED=QTYALLOCATED+?, QTYPICKED=QTYPICKED+?, QTYONHOLD = QTYONHOLD+?, EDITWHO=?, EDITDATE=? WHERE LOT=?"
                    , new Object[]{qtyToBeMoved, toQtyAllocChg, toQtyPickedChg, toQtyHoldChg, username, currentDate, toLot});
        }


        //更新后检查LOT，如果为0直接删除
        DBHelper.executeUpdate("DELETE FROM LOT WHERE LOT = ? AND QTY = 0 ", new Object[]{fromLot});

        //交易记录
        Integer itrnKey = null;

        if(saveItrn){

            itrnKey = IdGenerationHelper.getNCounter("ITRNKEY");
            Map<String,Object> itrn = new HashMap<>();

            itrn.put("ADDWHO", username);
            itrn.put("EDITWHO", username);
            itrn.put("ITRNKEY", itrnKey);
            itrn.put("ITRNSYSID", "0");
            itrn.put("TRANTYPE", fromLot.equals(toLot) ? "MV":"IT");
            itrn.put("STORERKEY", storerKey);
            itrn.put("SKU", sku);
            itrn.put("LOT", toLot);
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

            itrn.put("LOTTABLE01", toLotAttrHashMap.get("LOTTABLE01"));
            itrn.put("LOTTABLE02", toLotAttrHashMap.get("LOTTABLE02"));
            itrn.put("LOTTABLE03", toLotAttrHashMap.get("LOTTABLE03"));
            itrn.put("LOTTABLE04", EHDateTimeHelper.getLocalDateStr((LocalDateTime) toLotAttrHashMap.get("LOTTABLE04")));
            itrn.put("LOTTABLE05", EHDateTimeHelper.getLocalDateStr((LocalDateTime) toLotAttrHashMap.get("LOTTABLE05")));
            itrn.put("LOTTABLE06", toLotAttrHashMap.get("LOTTABLE06"));
            itrn.put("LOTTABLE07", toLotAttrHashMap.get("LOTTABLE07"));
            itrn.put("LOTTABLE08", toLotAttrHashMap.get("LOTTABLE08"));
            itrn.put("LOTTABLE09", toLotAttrHashMap.get("LOTTABLE09"));
            itrn.put("LOTTABLE10", toLotAttrHashMap.get("LOTTABLE10"));
            itrn.put("LOTTABLE11", EHDateTimeHelper.getLocalDateStr((LocalDateTime) toLotAttrHashMap.get("LOTTABLE11")));
            itrn.put("LOTTABLE12", EHDateTimeHelper.getLocalDateStr((LocalDateTime) toLotAttrHashMap.get("LOTTABLE12")));


            //TODO 增加如下字段给批属性变更使用
//            if(!fromLot.equals(toLot)) {

//            itrn.put("FROMLOT", fromLot);
//            itrn.put("TOLOT", toLot);
//            itrn.put("FROMLOTTABLE01", fromLotAttrHashMap.get("LOTTABLE01"));
//            itrn.put("FROMLOTTABLE02", fromLotAttrHashMap.get("LOTTABLE02"));
//            itrn.put("FROMLOTTABLE03", fromLotAttrHashMap.get("LOTTABLE03"));
//            itrn.put("FROMLOTTABLE04", EHDateTimeHelper.getLocalDateStr((LocalDateTime) fromLotAttrHashMap.get("LOTTABLE04")));
//            itrn.put("FROMLOTTABLE05", EHDateTimeHelper.getLocalDateStr((LocalDateTime) fromLotAttrHashMap.get("LOTTABLE05")));
//            itrn.put("FROMLOTTABLE06", fromLotAttrHashMap.get("LOTTABLE06"));
//            itrn.put("FROMLOTTABLE07", fromLotAttrHashMap.get("LOTTABLE07"));
//            itrn.put("FROMLOTTABLE08", fromLotAttrHashMap.get("LOTTABLE08"));
//            itrn.put("FROMLOTTABLE09", fromLotAttrHashMap.get("LOTTABLE09"));
//            itrn.put("FROMLOTTABLE10", fromLotAttrHashMap.get("LOTTABLE10"));
//            itrn.put("FROMLOTTABLE11", EHDateTimeHelper.getLocalDateStr((LocalDateTime) fromLotAttrHashMap.get("LOTTABLE11")));
//            itrn.put("FROMLOTTABLE12", EHDateTimeHelper.getLocalDateStr((LocalDateTime) fromLotAttrHashMap.get("LOTTABLE12")));
//
//            itrn.put("TOLOTTABLE01", toLotAttrHashMap.get("LOTTABLE01"));
//            itrn.put("TOLOTTABLE02", toLotAttrHashMap.get("LOTTABLE02"));
//            itrn.put("TOLOTTABLE03", toLotAttrHashMap.get("LOTTABLE03"));
//            itrn.put("TOLOTTABLE04", EHDateTimeHelper.getLocalDateStr((LocalDateTime) toLotAttrHashMap.get("LOTTABLE04")));
//            itrn.put("TOLOTTABLE05", EHDateTimeHelper.getLocalDateStr((LocalDateTime) toLotAttrHashMap.get("LOTTABLE05")));
//            itrn.put("TOLOTTABLE06", toLotAttrHashMap.get("LOTTABLE06"));
//            itrn.put("TOLOTTABLE07", toLotAttrHashMap.get("LOTTABLE07"));
//            itrn.put("TOLOTTABLE08", toLotAttrHashMap.get("LOTTABLE08"));
//            itrn.put("TOLOTTABLE09", toLotAttrHashMap.get("LOTTABLE09"));
//            itrn.put("TOLOTTABLE10", toLotAttrHashMap.get("LOTTABLE10"));
//            itrn.put("TOLOTTABLE11", EHDateTimeHelper.getLocalDateStr((LocalDateTime) toLotAttrHashMap.get("LOTTABLE11")));
//            itrn.put("TOLOTTABLE12", EHDateTimeHelper.getLocalDateStr((LocalDateTime) toLotAttrHashMap.get("LOTTABLE12")));
//            }


//---------------------------------
            DBHelper.executeInsert("ITRN", itrn);
        }


        ServiceDataMap serviceDataMap = new ServiceDataMap();
        serviceDataMap.setAttribValue("itrnKey", itrnKey);
        serviceDataMap.setAttribValue("toId", toId);

        return serviceDataMap;

    }


    private String getInventoryHoldKey() {
        return IdGenerationHelper.getNCounterStrWithLength("INVENTORYHOLDKEY", 10);
    }

    public void holdById(String id, String reasonCode){

        if(InventoryHold.getHoldReasonsById(id).contains(reasonCode)){
            throw new EHApplicationException("容器"+id+"已存在相同冻结代码"+reasonCode+"的记录，不允许重复冻结");
        }


        List<Map<String,String>> lotxLocxIdList = LotxLocxId.findMultiLotIdWithoutIDNotes(id);

        if (lotxLocxIdList.size() == 0) throw new EHApplicationException("未找到库存容器"+id);


        increaseLotxLocxIdListHoldQty(lotxLocxIdList);


        Map<String,String> holdIdRecord = DBHelper.getRecord("SELECT * FROM INVENTORYHOLD WHERE STATUS = ? AND ID = ? ", new Object[]{reasonCode, id},"容器冻结记录");

        if(holdIdRecord != null){

            DBHelper.executeUpdate( "UPDATE INVENTORYHOLD SET EDITWHO=?,EDITDATE=?,HOLD=?,DATEON=?, WHOON=?"
                            + " WHERE ID = ? AND STATUS = ?"
                    , new Object[] {
                            EHContextHelper.getUser().getUsername(),
                            LocalDateTime.now(),
                            "1",
                            LocalDateTime.now(),
                            EHContextHelper.getUser().getUsername(),
                            id, reasonCode});

        }else {

            DBHelper.executeInsert("INVENTORYHOLD", new HashMap<>() {{
                put("INVENTORYHOLDKEY", getInventoryHoldKey());
                put("STATUS", reasonCode);
                put("HOLD", 1);
                put("ID", id);
                put("DATEON", LocalDateTime.now());
                put("WHOON", EHContextHelper.getUser().getUsername());
            }});

        }
    }

    public void holdByLot(String lot, String reasonCode){

        //确认批次已存在
        LotAttribute.findWithEntByLot(lot, true);

        List<Map<String,String>> lotxLocxIdList = LotxLocxId.findByLot(lot);
        //暂不做如下校验，原因：虑在lot存在的情况下，即使没有库存该批次也应该添加lot的冻结记录，如果后续有新的库存，应该自动变为冻结状态。
        //if (lotxLocxIdList.size() == 0) throw new EHApplicationException("未找到批次号为"+lot+"的库存容器");

        //lot表存在预分配量，证明该批次下存在对正常库存的分配量，则不允许冻结。（这里不用关心这个正常库存是否分没分配到id级别，因为按批次冻结不允许有任何正常库存已被分配）
        Map<String,Object> lotHashMap = Lot.findById(lot);

        List<String> reasonCodeListByLot = InventoryHold.getHoldReasonsByLot(lot);

        if(reasonCodeListByLot.contains(reasonCode)){
            throw new EHApplicationException("批次"+lot+"已存在相同冻结代码"+reasonCode+"的记录，不允许重复冻结");
        }else if(reasonCodeListByLot.size() == 0){

            if(lotHashMap!=null && lotHashMap.get("QTYPREALLOCATED") !=null &&
                    new BigDecimal(lotHashMap.get("QTYPREALLOCATED").toString()).compareTo(BigDecimal.ZERO)>0)
                throw new EHApplicationException("批次"+lot+"存在预分配量，不允许冻结");

            increaseLotxLocxIdListHoldQty(lotxLocxIdList);

            DBHelper.executeUpdate( "UPDATE LOT SET STATUS = ?, EDITWHO = ?,EDITDATE = ? WHERE LOT = ? "
                    , new Object[] {
                            "HOLD",
                            EHContextHelper.getUser().getUsername(),
                            LocalDateTime.now(),
                            lot});

        }else {
            //如果该批次已经有其他冻结记录，说明该批次下的所有库存分配量和拣货量都为冻结分配或者拣货的库存，因此可以使用其他任意原因再次冻结,无需修改库存。
            //Do Nothing
        }

        Map<String,String> holdLotRecord = DBHelper.getRecord("SELECT * FROM INVENTORYHOLD WHERE STATUS = ? AND LOT = ? ", new Object[]{reasonCode, lot},"批次冻结记录");

        if(holdLotRecord!=null){

            DBHelper.executeUpdate( "UPDATE INVENTORYHOLD SET EDITWHO=?,EDITDATE=?,HOLD=?,DATEON=?, WHOON=?"
                            + " WHERE LOT = ? AND STATUS = ?"
                    , new Object[] {
                            EHContextHelper.getUser().getUsername(),
                            LocalDateTime.now(),
                            "1",
                            LocalDateTime.now(),
                            EHContextHelper.getUser().getUsername(),
                           lot, reasonCode});

        }else {

            DBHelper.executeInsert("INVENTORYHOLD", new HashMap<>() {{
                put("INVENTORYHOLDKEY", getInventoryHoldKey());
                put("STATUS", reasonCode);
                put("HOLD", 1);
                put("LOT", lot);
                put("DATEON", LocalDateTime.now());
                put("WHOON", EHContextHelper.getUser().getUsername());
            }});

        }

    }

    public void holdByLoc(String loc, String reasonCode){

        //确认库位已存在
        Loc.findById(loc, true);

        List<Map<String,String>> lotxLocxIdList = LotxLocxId.findByLoc(loc);


        List<String> reasonCodeListByLoc = InventoryHold.getHoldReasonsByLoc(loc);

        if(reasonCodeListByLoc.contains(reasonCode)){
            throw new EHApplicationException("库位"+loc+"已存在相同冻结代码"+reasonCode+"的记录，不允许重复冻结");
        }else if(reasonCodeListByLoc.size() == 0){

            increaseLotxLocxIdListHoldQty(lotxLocxIdList);

        }else {
            //如果该库位已经有其他冻结记录，说明该库位下的所有库存分配量和拣货量都为冻结分配或者拣货的库存，因此可以使用其他任意原因再次冻结,无需修改库存。
            //Do Nothing
        }


        Map<String,String> holdLocRecord = DBHelper.getRecord("SELECT * FROM INVENTORYHOLD WHERE STATUS = ? AND LOC = ? ", new Object[]{reasonCode, loc},"库位冻结记录");

        if(holdLocRecord != null){

            DBHelper.executeUpdate( "UPDATE INVENTORYHOLD SET EDITWHO = ?,EDITDATE = ?,HOLD = ?,DATEON = ?, WHOON = ?"
                            + " WHERE LOC = ? AND STATUS = ?"
                    , new Object[] {
                            EHContextHelper.getUser().getUsername(),
                            LocalDateTime.now(),
                            "1",
                            LocalDateTime.now(),
                            EHContextHelper.getUser().getUsername(),
                            loc, reasonCode});

        }else {

            DBHelper.executeInsert("INVENTORYHOLD", new HashMap<>() {{
                put("INVENTORYHOLDKEY", getInventoryHoldKey());
                put("STATUS", reasonCode);
                put("HOLD", 1);
                put("LOC", loc);
                put("DATEON", LocalDateTime.now());
                put("WHOON", EHContextHelper.getUser().getUsername());
            }});

        }


    }

    /***
     * 增加LotxLocxIdList的冻结数量
     * 这里允许有多个不同ID，或相同ID下的多批次库存
     * 本方法仅对未冻结的ID做数量上的调整，冻结记录单独在调用方法中添加。
     * @param lotxLocxIdList
     */
    private void increaseLotxLocxIdListHoldQty(List<Map<String,String>> lotxLocxIdList){

        for (Map<String, String> lotxLocxIdHashMap : lotxLocxIdList) {

            increaseLotxLocxIdHoldQty(lotxLocxIdHashMap);

        }

    }

    /**
     * 本方法仅对未冻结的ID做数量上的调整，冻结记录单独在调用方法中添加。
     * @param lotxLocxIdHashMap
     */

    private void increaseLotxLocxIdHoldQty(Map<String, String> lotxLocxIdHashMap) {

        String username = EHContextHelper.getUser().getUsername();

        LocalDateTime currentDate = EHDateTimeHelper.getCurrentDate();

        String id = lotxLocxIdHashMap.get("ID");

        //当ID已为冻结状态现又需要被其他原因冻结，则无需修改库存信息，直接返回。
        if(HOLD.equals(lotxLocxIdHashMap.get("STATUS"))) return;

        //当id分配量或者拣货量大于0时，该id不允许被冻结。
        if(new BigDecimal(lotxLocxIdHashMap.get("QTYPICKED")).compareTo(BigDecimal.ZERO)>0 ||
                new BigDecimal(lotxLocxIdHashMap.get("QTYALLOCATED")).compareTo(BigDecimal.ZERO)>0){
            throw new EHApplicationException("容器"+id+"存在分配量或拣货量，不允许冻结");
        }

        //当前lot中正常库存的可用量qty-qtypicked-qtyallocated-qtypreallocated-qtyonhold（hold状态库存的可用量）是否大于当前待冻结的id的数量，如果大于则允许冻结，否则不允许
        BigDecimal qtyUnholdAvail = DBHelper.getDecimalValue("SELECT QTY-QTYPICKED-QTYALLOCATED-QTYPREALLOCATED-QTYONHOLD FROM LOT WHERE LOT = ? ", new Object[]{lotxLocxIdHashMap.get("LOT")});

        if(qtyUnholdAvail.compareTo(new BigDecimal(lotxLocxIdHashMap.get("QTY"))) <0) throw new EHApplicationException("待冻结容器"+id+"的数量为"+lotxLocxIdHashMap.get("QTY")+"大于当前批次可锁定的数量"+ qtyUnholdAvail);

        DBHelper.executeUpdate("UPDATE LOTXLOCXID SET STATUS=?,EDITWHO=?,EDITDATE=? WHERE LOT=? AND LOC=? AND ID=?"
                    , new Object[]{"HOLD", username, currentDate, lotxLocxIdHashMap.get("LOT"), lotxLocxIdHashMap.get("LOC"), id});

        DBHelper.executeUpdate("UPDATE LOT SET QTYONHOLD = QTYONHOLD + ?,EDITWHO=?,EDITDATE=? WHERE LOT=?"
                    , new Object[]{  lotxLocxIdHashMap.get("QTY"), username, currentDate, lotxLocxIdHashMap.get("LOT")});

        DBHelper.executeUpdate("UPDATE ID SET STATUS=?,EDITWHO=?,EDITDATE=? WHERE ID=?"
                , new Object[]{"HOLD", username, currentDate, id});


    }


    /**
     *  根据ID解冻
     * @param id
     * @param reasonCode
     * @param isDelete
     */
    public void unholdById(String id, String reasonCode, boolean isDelete){

        //仅ID HOLD冻结记录
        List<String> holdReasonListById = InventoryHold.getHoldReasonsById(id);

        if(!holdReasonListById.contains(reasonCode)) throw new EHApplicationException("未找到待解冻容器"+id+"且冻结原因为"+reasonCode+"的冻结记录");

        List<Map<String,String>>  lotxLocxIdHashMapList = LotxLocxId.findMultiLotIdWithoutIDNotes(id);

        releaseByLotxLocxId(lotxLocxIdHashMapList,reasonCode);

        //更新或删除INVENTORYHOLD
        if(isDelete) {

            DBHelper.executeUpdate("DELETE FROM INVENTORYHOLD WHERE ID = ? AND STATUS = ? ", new Object[]{id, reasonCode});

        }else {

            DBHelper.executeUpdate("UPDATE INVENTORYHOLD SET EDITWHO=?,EDITDATE=?,HOLD=?,DATEOFF=?, WHOOFF=?"
                            + " WHERE ID = ? AND STATUS = ?"
                    , new Object[] {
                            EHContextHelper.getUser().getUsername(),
                            LocalDateTime.now(),
                            "0",
                            LocalDateTime.now(),
                            EHContextHelper.getUser().getUsername(),
                            id, reasonCode});

        }

    }


    /**
     *  根据LOT解冻
     * @param lot
     * @param reasonCode
     * @param isDelete
     */
    public void unholdByLot(String lot, String reasonCode, boolean isDelete){

        //仅LOT HOLD冻结记录
        List<String> holdReasonListByLot = InventoryHold.getHoldReasonsByLot(lot);

        if(!holdReasonListByLot.contains(reasonCode)) throw new EHApplicationException("未找到待解冻批次"+lot+"且冻结原因为"+reasonCode+"的冻结记录");

        List<Map<String,String>>  lotxLocxIdHashMapList = LotxLocxId.findByLot(lot);

        releaseByLotxLocxId(lotxLocxIdHashMapList,reasonCode);

        //如果当前lot不存在除当前reasonCode外的其他冻结记录，则在解冻后将lot状态置为OK
        if (holdReasonListByLot.size()==1){

            DBHelper.executeUpdate( "UPDATE LOT SET STATUS = ?, EDITWHO = ?, EDITDATE = ? WHERE LOT = ? "
                    , new Object[] {
                            "OK",
                            EHContextHelper.getUser().getUsername(),
                            LocalDateTime.now(),
                            lot});
        }

        //更新或删除INVENTORYHOLD
        if(isDelete) {

            DBHelper.executeUpdate("DELETE FROM INVENTORYHOLD WHERE LOT = ? AND STATUS = ? ", new Object[]{lot, reasonCode});

        }else {

            DBHelper.executeUpdate("UPDATE INVENTORYHOLD SET EDITWHO=?,EDITDATE=?,HOLD=?,DATEOFF=?, WHOOFF=?"
                            + " WHERE LOT = ? AND STATUS = ?"
                    , new Object[] {
                            EHContextHelper.getUser().getUsername(),
                            LocalDateTime.now(),
                            "0",
                            LocalDateTime.now(),
                            EHContextHelper.getUser().getUsername(),
                            lot, reasonCode});

        }

    }


    /**
     *  根据LOC解冻
     * @param loc
     * @param reasonCode
     * @param isDelete
     */
    public void unholdByLoc(String loc, String reasonCode, boolean isDelete){

        //仅LOC HOLD冻结记录
        List<String> holdReasonListByLoc = InventoryHold.getHoldReasonsByLoc(loc);

        if(!holdReasonListByLoc.contains(reasonCode)) throw new EHApplicationException("未找到待解冻库位"+loc+"且冻结原因为"+reasonCode+"的冻结记录");

        List<Map<String,String>>  lotxLocxIdHashMapList = LotxLocxId.findByLoc(loc);

        releaseByLotxLocxId(lotxLocxIdHashMapList,reasonCode);

        //更新或删除INVENTORY HOLD RECORD
        if(isDelete) {

            DBHelper.executeUpdate("DELETE FROM INVENTORYHOLD WHERE LOC = ? AND STATUS = ? ", new Object[]{loc, reasonCode});

        }else {

            DBHelper.executeUpdate("UPDATE INVENTORYHOLD SET EDITWHO=?,EDITDATE=?,HOLD=?,DATEOFF=?, WHOOFF=?"
                            + " WHERE LOC = ? AND STATUS = ?"
                    , new Object[] {
                            EHContextHelper.getUser().getUsername(),
                            LocalDateTime.now(),
                            "0",
                            LocalDateTime.now(),
                            EHContextHelper.getUser().getUsername(),
                            loc, reasonCode});

        }

    }

    /**
     * 检查并根据需要解冻所有库存列表中的冻结数量和状态
     * @param lotxLocxIdHashMapList
     * @param reasonCode
     */
    private void releaseByLotxLocxId(List<Map<String, String>> lotxLocxIdHashMapList, String reasonCode){

        String username = EHContextHelper.getUser().getUsername();
        LocalDateTime currentDate = EHDateTimeHelper.getCurrentDate();

        for (Map<String,String> lotxLocxIdHashMap: lotxLocxIdHashMapList) {

            if(checkReleaseIdHoldByReasonCode(lotxLocxIdHashMap, reasonCode)&& lotxLocxIdHashMap.get("STATUS").equals("HOLD")){

                DBHelper.executeUpdate("UPDATE LOTXLOCXID SET STATUS=?,EDITWHO=?,EDITDATE=? WHERE LOT=? AND LOC=? AND ID=?"
                        , new Object[]{"OK", username, currentDate, lotxLocxIdHashMap.get("LOT"), lotxLocxIdHashMap.get("LOC"), lotxLocxIdHashMap.get("ID")});

                DBHelper.executeUpdate("UPDATE LOT SET QTYONHOLD = QTYONHOLD - ?,EDITWHO=?,EDITDATE=? WHERE LOT=?"
                        , new Object[]{  lotxLocxIdHashMap.get("QTY"), username, currentDate, lotxLocxIdHashMap.get("LOT")});

                DBHelper.executeUpdate("UPDATE ID SET STATUS=?,EDITWHO=?,EDITDATE=? WHERE ID=?"
                        , new Object[]{"OK", username, currentDate, lotxLocxIdHashMap.get("ID")});

            }

        }

    }

    /**
     * a)如果id已存在分配量或拣货量，则要查看该id是否存在其他关联冻结记录能够支持其关联订单的冻结分配要求，否则不允许解冻。
     * b)如果id不存在分配量和拣货量，则可以直接解除冻结
     * @param lotxLocxIdHashMap
     * @param reasonCode
     * @return 是否将库存状态从冻结重置为正常
     */

    private boolean checkReleaseIdHoldByReasonCode(Map<String, String> lotxLocxIdHashMap, String reasonCode){

        /**
         * a)如果id已存在分配量或拣货量，则同根据批次解冻的方式类似，要查看该id是否有相同冻结原因下存在按批次或者库位冻结的记录，如果存在记录则该id允许解冻，否则不允许解冻。
         * b)如果id不存在分配量和拣货量，则可以直接解除该冻结原因
         */

        String id = lotxLocxIdHashMap.get("ID");

        //查询id上所有冻结维度上能关联到的冻结原因，如果在lot，loc，id上不同维度上用相同原因冻结了多次，则list中也应包含多次。
        List<String> holdReasonListById = InventoryHold.getIdHoldReasons(lotxLocxIdHashMap.get("LOT"),lotxLocxIdHashMap.get("LOC"),id);

        //在当前id上去掉一条相同的冻结原因，再次查看是否满足订单要求
        for (int i=0;i<holdReasonListById.size();i++){
            if(holdReasonListById.get(i).equals(reasonCode)){
                holdReasonListById.remove(i);
                break;
            }
        }

        if(new BigDecimal(lotxLocxIdHashMap.get("QTYPICKED")).compareTo(BigDecimal.ZERO)>0 ||
                new BigDecimal(lotxLocxIdHashMap.get("QTYALLOCATED")).compareTo(BigDecimal.ZERO)>0){

            List<Map<String, String>> pickDetails = PickDetail.findByUnshippedId(id);

            Set<String> orderKeySet = pickDetails.stream().map(pd-> pd.get("ORDERKEY")).collect(Collectors.toSet());

            for (String orderKey : orderKeySet) {

                Map<String, String> orderInfo = Orders.findByOrderKey(orderKey, true);

                //查询订单的冻结要求
                List<String> holdReasonListByOrder = InventoryHold.getOrderTypeHoldReasons(orderInfo.get("TYPE"));

                holdReasonListByOrder.retainAll(holdReasonListById);
                if(holdReasonListByOrder.size()<=0) throw new EHApplicationException("冻结原因"+reasonCode+"解冻后，将不能满足订单"+orderKey+"的冻结要求，解冻失败");

            }

            return false;

        }else{
            //如果移出掉后未有其他冻结原因与库存记录关联，则应解冻为正常。
            return holdReasonListById.size() == 0;

        }

    }


    /**
     *  批属性转移
     *  转移后，id关联的lot冻结状态要参考目标lot的冻结状态。
     * @param lot
     * @param id
     * @param overrideLotAttributeList 此参数为待更新的批属性列表，如果不传则默认使用原值。如需全量更新，需传全量属性列表
     */
    public void internalTransfer(String id, String lot, HashMap<String,Object> overrideLotAttributeList) {

        if(UtilHelper.isEmpty(id)) throw new EHApplicationException("容器号不允许为空");
        if(UtilHelper.isEmpty(lot)) throw new EHApplicationException("WMS源批次号不允许为空");
        if(overrideLotAttributeList==null || overrideLotAttributeList.size()==0) throw new EHApplicationException("未提供待修改的批属性信息列表");

        Map<String,Object> lotxLocxIdMap = LotxLocxId.find(id,lot,true);

        String loc = lotxLocxIdMap.get("LOC").toString();
        BigDecimal qty = (BigDecimal) lotxLocxIdMap.get("QTY");

        if(((BigDecimal)lotxLocxIdMap.get("QTYPICKED")).compareTo(BigDecimal.ZERO)>0 ||
                ((BigDecimal)lotxLocxIdMap.get("QTYALLOCATED")).compareTo(BigDecimal.ZERO)>0){
            throw new EHApplicationException("容器"+id+"存在分配量或拣货量，不允许内部转移");
        }

        LinkedHashMap<String, Object> toLotMap = new LinkedHashMap<>();

        Map<String,Object> fromLotMap = LotAttribute.findByLot(lot,true);

        fromLotMap.entrySet().forEach(e ->{
                    if(e.getKey().startsWith("LOTTABLE"))  toLotMap.put(e.getKey(), e.getValue());
        });


        boolean lotInfoChanged = false;
//
//        //override the changed lot attributes
//        List<String> lotColumns = new ArrayList<>(){{
//            add("LOTTABLE04");
//            add("LOTTABLE05");
//            add("LOTTABLE11");
//            add("LOTTABLE12");
//        }};

        for (Map.Entry<String, Object> attribute : overrideLotAttributeList.entrySet()) {

            if (!toLotMap.containsKey(attribute.getKey()))
                throw new EHApplicationException("批属性字段" + attribute.getKey() + "不存在");

            Object overrideValue;

            if(toLotMap.get(attribute.getKey()) instanceof LocalDateTime) {


                if (attribute.getValue() instanceof LocalDateTime) {

                    overrideValue = attribute.getValue();

                } else if (attribute.getValue() instanceof Number) {

                    overrideValue = EHDateTimeHelper.timeStamp2LocalDateTime(attribute.getValue());

                } else {

                    throw new EHApplicationException("批属性字段" + attribute.getKey() + "的数据类型无法转换为时间格式");

                }

            }else {

                overrideValue = attribute.getValue();

            }

            if(!overrideValue.equals(toLotMap.get(attribute.getKey()))){
                toLotMap.put(attribute.getKey(), overrideValue);
                lotInfoChanged = true;
            }

        }

        if(!lotInfoChanged) throw new EHApplicationException("未修改批属性，无需进行转移");

        //因为lot是lotattribute表的主键，因此对于不同的storerkey，即使物料和批属性组合相同也会产生不同的lot，所以这里要根据storerKey来过滤
        toLotMap.put("STORERKEY",lotxLocxIdMap.get("STORERKEY"));
        toLotMap.put("SKU",lotxLocxIdMap.get("SKU"));


        StringBuffer sqlSB = new StringBuffer(" SELECT * FROM LOTATTRIBUTE WHERE 1=1 ");

        toLotMap.entrySet().forEach(e-> sqlSB.append(" AND " + e.getKey() + " = ? "));

        Map<String,Object> targetLotMap = DBHelper.getRawRecord(sqlSB.toString(), toLotMap.values().toArray(),"批属性记录",false);

        String toLot;

        if(targetLotMap == null){
            //TODO 对于生成的新批次暂不考虑冻结是否与原批次一致，因为新批次需要复制原批次的冻结记录，因此暂保持与其他wms系统行为一致，暂不做处理
            toLot = IdGenerationHelper.getNCounterStrWithLength("LOT",10);

            toLotMap.put("LOT", toLot);

            DBHelper.executeInsert("LOTATTRIBUTE",toLotMap);
        }else {

            toLot = targetLotMap.get("LOT").toString();

        }

        move(id, id, loc, loc, lot, toLot, qty, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, true);


    }

}
