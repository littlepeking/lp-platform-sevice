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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
@AllArgsConstructor
public class InventoryOperations {

    static public String HOLD = "HOLD";
    static public String OK = "OK";


    public ServiceDataMap move(String fromId, String toId, String fromLoc, String toLoc, String lot,
                               BigDecimal qtyToBeMoved, BigDecimal fromQtyAllocChg, BigDecimal fromQtyPickedChg,
                               BigDecimal toQtyAllocChg, BigDecimal toQtyPickedChg, boolean autoShip, boolean saveItrn){
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
        LotAttribute.findByLot(lot,true);
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
            Map<String,Object> lotHashMap = LotAttribute.findByLot(lot, true);

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


    private String getInventoryHoldKey() {
        return IdGenerationHelper.getNCounterStrWithLength("INVENTORYHOLDKEY", 10);
    }

    public void holdById(String id, String reasonCode){

        Map<String,String> holdIdRecord = DBHelper.getRecord("SELECT * FROM INVENTORYHOLD WHERE STATUS = ? AND ID = ? ", new Object[]{reasonCode, id},"容器冻结记录");

        boolean isIdHold = holdIdRecord != null && "1".equals(holdIdRecord.get("HOLD"));

        if(!isIdHold){

            throw new EHApplicationException("容器"+id+"未被冻结原因"+reasonCode+"冻结，不允许解冻");

        }

        List<Map<String,String>> lotxLocxIdList = LotxLocxId.findMultiLotIdWithoutIDNotes(id);

        if (lotxLocxIdList.size() == 0) throw new EHApplicationException("未找到库存容器"+id);

        if(InventoryHold.getHoldReasonsById(id).contains(reasonCode)){
            throw new EHApplicationException("容器"+id+"已存在相同冻结代码"+reasonCode+"的记录，不允许重复冻结");
        }

        increaseLotxLocxIdListHoldQty(lotxLocxIdList);

        if(holdIdRecord != null){

            DBHelper.executeUpdate( "UPDATE INVENTORYHOLD SET EDITWHO=?,EDITDATE=?,HOLD=?,DATEON=?, WHOON=?"
                            + " WHERE ID = ? STATUS = ?"
                    , new Object[] {
                            EHContextHelper.getUser().getUsername(),
                            LocalDateTime.now(),
                            "1",
                            LocalDateTime.now().toString(),
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

        Map<String,String> holdLotRecord = DBHelper.getRecord("SELECT * FROM INVENTORYHOLD WHERE STATUS = ? AND LOT = ? ", new Object[]{reasonCode, lot},"批次冻结记录");

        boolean isLotHold = holdLotRecord != null && "1".equals(holdLotRecord.get("HOLD"));

        if(!isLotHold){

            throw new EHApplicationException("批次"+lot+"未被冻结原因"+reasonCode+"冻结，不允许解冻");

        }

        //确认批次已存在
        LotAttribute.findByLot(lot, true);

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

        }else {
            //如果该批次已经有其他冻结记录，说明该批次下的所有库存分配量和拣货量都为冻结分配或者拣货的库存，因此可以使用其他任意原因再次冻结,无需修改库存。
            //Do Nothing
        }

        if(holdLotRecord!=null){

            DBHelper.executeUpdate( "UPDATE INVENTORYHOLD SET EDITWHO=?,EDITDATE=?,HOLD=?,DATEON=?, WHOON=?"
                            + " WHERE LOT = ? STATUS = ?"
                    , new Object[] {
                            EHContextHelper.getUser().getUsername(),
                            LocalDateTime.now(),
                            "1",
                            LocalDateTime.now().toString(),
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

        Map<String,String> holdLocRecord = DBHelper.getRecord("SELECT * FROM INVENTORYHOLD WHERE STATUS = ? AND LOC = ? ", new Object[]{reasonCode, loc},"库位冻结记录");

        boolean isLocHold = holdLocRecord != null && "1".equals(holdLocRecord.get("HOLD"));

        if(!isLocHold){

            throw new EHApplicationException("库位"+loc+"未被冻结原因"+reasonCode+"冻结，不允许解冻");

        }


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

        if(holdLocRecord != null){

            DBHelper.executeUpdate( "UPDATE INVENTORYHOLD SET EDITWHO=?,EDITDATE=?,HOLD=?,DATEON=?, WHOON=?"
                            + " WHERE LOC = ? STATUS = ?"
                    , new Object[] {
                            EHContextHelper.getUser().getUsername(),
                            LocalDateTime.now(),
                            "1",
                            LocalDateTime.now().toString(),
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

        //当前lot中正常库存的可用量qty-qtypicked-qtyallocated-qtypreallocated-qtyhold（hold状态库存的可用量）是否大于当前待冻结的id的数量，如果大于则允许冻结，否则不允许
        BigDecimal qtyUnholdAvail = DBHelper.getDecimalValue("SELECT QTY-QTYPICKED-QTYALLOCATED-QTYPREALLOCATED-QTYHOLD FROM LOT WHERE LOT = ? ", new Object[]{lotxLocxIdHashMap.get("LOT")});

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
    public void releaseById(String id, String reasonCode, boolean isDelete){

        //仅ID HOLD冻结记录
        List<String> holdReasonListById = InventoryHold.getHoldReasonsById(id);

        if(!holdReasonListById.contains(reasonCode)) throw new EHApplicationException("未找到待解冻容器"+id+"的冻结记录");

        List<Map<String,String>>  lotxLocxIdHashMapList = LotxLocxId.findMultiLotIdWithoutIDNotes(id);

        releaseByLotxLocxId(lotxLocxIdHashMapList,reasonCode);

        //更新或删除INVENTORYHOLD
        if(isDelete) {

            DBHelper.executeUpdate("DELETE FROM INVENTORYHOLD WHERE ID = ? AND STATUS = ? ", new Object[]{id, reasonCode});

        }else {

            DBHelper.executeUpdate("UPDATE INVENTORYHOLD SET EDITWHO=?,EDITDATE=?,HOLD=?,DATEOFF=?, WHOOFF=?"
                            + " WHERE ID = ? STATUS = ?"
                    , new Object[] {
                            EHContextHelper.getUser().getUsername(),
                            LocalDateTime.now(),
                            "0",
                            LocalDateTime.now().toString(),
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
    public void releaseByLot(String lot, String reasonCode, boolean isDelete){

        //仅LOT HOLD冻结记录
        List<String> holdReasonListByLot = InventoryHold.getHoldReasonsByLot(lot);

        if(!holdReasonListByLot.contains(reasonCode)) throw new EHApplicationException("未找到待解冻批次"+lot+"的冻结记录");

        List<Map<String,String>>  lotxLocxIdHashMapList = LotxLocxId.findByLot(lot);

        releaseByLotxLocxId(lotxLocxIdHashMapList,reasonCode);

        //更新或删除INVENTORYHOLD
        if(isDelete) {

            DBHelper.executeUpdate("DELETE FROM INVENTORYHOLD WHERE LOT = ? AND STATUS = ? ", new Object[]{lot, reasonCode});

        }else {

            DBHelper.executeUpdate("UPDATE INVENTORYHOLD SET EDITWHO=?,EDITDATE=?,HOLD=?,DATEOFF=?, WHOOFF=?"
                            + " WHERE LOT = ? STATUS = ?"
                    , new Object[] {
                            EHContextHelper.getUser().getUsername(),
                            LocalDateTime.now(),
                            "0",
                            LocalDateTime.now().toString(),
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
    public void releaseByLoc(String loc, String reasonCode, boolean isDelete){

        //仅LOC HOLD冻结记录
        List<String> holdReasonListByLoc = InventoryHold.getHoldReasonsByLoc(loc);

        if(!holdReasonListByLoc.contains(reasonCode)) throw new EHApplicationException("未找到待解冻库位"+loc+"的冻结记录");

        List<Map<String,String>>  lotxLocxIdHashMapList = LotxLocxId.findByLoc(loc);

        releaseByLotxLocxId(lotxLocxIdHashMapList,reasonCode);

        //更新或删除INVENTORYHOLD
        if(isDelete) {

            DBHelper.executeUpdate("DELETE FROM INVENTORYHOLD WHERE LOC = ? AND STATUS = ? ", new Object[]{loc, reasonCode});

        }else {

            DBHelper.executeUpdate("UPDATE INVENTORYHOLD SET EDITWHO=?,EDITDATE=?,HOLD=?,DATEOFF=?, WHOOFF=?"
                            + " WHERE LOC = ? STATUS = ?"
                    , new Object[] {
                            EHContextHelper.getUser().getUsername(),
                            LocalDateTime.now(),
                            "0",
                            LocalDateTime.now().toString(),
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

            if(checkReleaseIdHoldByReasonCode(lotxLocxIdHashMap, reasonCode)){

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
                if(holdReasonListByOrder.size()<0) throw new EHApplicationException("冻结原因"+reasonCode+"解冻后，将不能满足订单"+orderKey+"的冻结要求，解冻失败");

            }

            return false;

        }else{
            //如果移出掉后未有其他冻结原因与库存记录关联，则应解冻为正常。
            return holdReasonListById.size() == 0;

        }

    }


}
