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
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.inventory.VLotAttribute;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.IdGenerationHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
public class WMSCoreOperations {

    public void move(String fromId, String toId, String fromLoc, String toLoc, String lot,
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
                ( fromQtyPickedChg.compareTo(fromIdQtyPicked) != 0) || qtyToBeMoved.compareTo(fromIdQty) != 0 ) throw new EHApplicationException("移动已拣货的容器时，不允许移动部分数量");
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
                DBHelper.insert( "LOTXLOCXID", lotxlocxid);

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

        if (!autoShip) {

            // 自动发运不增加目标数量
            if (!toIdInvExist) {
                Map<String, Object> id = new HashMap<>();
                id.put("ADDWHO", username);
                id.put("EDITWHO", username);
                id.put("ID", toId);
                id.put("QTY", qtyToBeMoved);
                id.put("STATUS", isToIdHold);
                id.put("PACKKEY", packKey);
                DBHelper.insert( "ID", id);

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
                    DBHelper.insert("INVENTORYHOLD", invHoldRec);

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
                DBHelper.insert( "SKUXLOC", skuxloc);
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
        if(saveItrn){
            int itrnKey = IdGenerationHelper.getNCounter("ITRNKEY");

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
            DBHelper.insert("ITRN", itrn);
        }

    }

    public ServiceDataMap pick(String pickDetailKey,String toId, String toLoc,BigDecimal uomQtyToBePicked, String uom, boolean allowShortPick,boolean reduceOpenQtyAfterShortPick, boolean allowOverPick, boolean autoShip){

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

        if(toId == null) throw new EHApplicationException("拣货至容器号不允许为空");

        String toLocationType = DBHelper.getStringValue("SELECT LOCATIONTYPE FROM LOC WHERE LOC=?", new String[]{toLoc});
        if (!toLocationType.equals("PICKTO")) throw new EHApplicationException("拣货目标库位不存在或类型错误");


        Map<String, String> orderHashMap = Orders.findByOrderKey(pdHashMap.get("ORDERKEY"),true);
        Map<String, String> orderDetailHashMap = Orders.findOrderDetailByKey(pdHashMap.get("ORDERKEY"),pdHashMap.get("ORDERDETAILKEY"),true);

        if (!orderDetailHashMap.get("IDREQUIRED").equals(fromId)) throw new EHApplicationException("已指定容器条码("+orderDetailHashMap.get("IDREQUIRED")+"),请按要求拣货");

        ////////////////////////
        // 检查物料是否冻结,(核对单据类型与冻结类型对应表) HOLDALLOCATIONMATRIX

        List<Object> holdReasonList = DBHelper.getValueList("SELECT STATUSCODE FROM HOLDALLOCATIONMATRIX WHERE ORDERTYPE = ?", new Object[]{orderHashMap.get("ORDERTYPE")},"冻结状态");

        Map<String,String> fromIdHashMap = LotxLocxId.findWithoutCheckIDNotes(pdHashMap.get("ID"),true);

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

        if(UtilHelper.isEmpty(toId)){
            if(isFullLpnPick){
                toId = fromId;
            }else {
                toId = IdGenerationHelper.generateIDByKeyName("CASEID",10);
            }
        }

        BigDecimal qtyPickDetailAlloc =  new BigDecimal(fromIdHashMap.get("QTY"));

        if(qtyPickDetailAlloc.compareTo(new BigDecimal(fromIdHashMap.get("QTYALLOCATED")))>0){
            throw new EHApplicationException("WMS数据异常:拣货明细待拣货数量" + qtyPickDetailAlloc + "大于和当前拣货容器" + fromId + "的分配量"+ fromIdHashMap.get("QTYALLOCATED"));
        }

        boolean shortPick = qtyToBePicked.compareTo(qtyPickDetailAlloc) < 0;
        boolean overPick = qtyToBePicked.compareTo(qtyPickDetailAlloc) > 0;

        if (shortPick && !allowShortPick) throw new EHApplicationException("系统配置不允许短拣,拣货失败");
        if (overPick && !allowOverPick) throw new EHApplicationException("系统配置不允许超拣,拣货失败");

        BigDecimal qtyAllocChg = qtyToBePicked.subtract(qtyPickDetailAlloc);

        BigDecimal qtyAvailable = DBHelper.getDecimalValue("SELECT QTY-QTYALLOCATED-QTYPICKED FROM LOTXLOCXID WHERE LOT=? AND LOC=? AND ID=?", new Object[]{lot,fromLoc,fromId });
        if (qtyAllocChg.compareTo(qtyAvailable)>0) throw new EHApplicationException("当前容器可超拣数量为"+qtyAvailable.toString()+",不满足超拣需求");



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
        DBHelper.executeUpdate("UPDATE PICKDETAIL SET QTY = QTY + ?, ADJUSTEDQTY = ADJUSTEDQTY + ?, EDITWHO=?,EDITDATE=?,STATUS=?,LOC=?,ID=?,DROPID=? WHERE PICKDETAILKEY=?"
                    , new Object[]{qtyAllocChg, qtyAllocChg, username,currentDate, autoShip ? 9 : 5 ,toLoc, toId, toId, pickDetailKey});

        DBHelper.executeUpdate("UPDATE TASKDETAIL SET QTY = QTY + ?, UOMQTY = UOMQTY + ? , UOM = ?, EDITWHO=?,EDITDATE=?,STATUS=?,TOLOC=?, TOID = ? WHERE STATUS < 9 AND  PICKDETAILKEY = ?"
                , new Object[]{qtyAllocChg, UOM.Std2UOMQty(packKey, uom, qtyAllocChg), uom, username,currentDate, 9, toLoc, toId, pickDetailKey});

        Map<String,String> odHashMap=DBHelper.getRecord("SELECT STATUS,OPENQTY - QTYPICKED AS QTY FROM ORDERDETAIL  WHERE ORDERKEY=? AND ORDERLINENUMBER=?"
                    ,  new String[]{pdHashMap.get("ORDERKEY"),pdHashMap.get("ORDERLINENUMBER")},"订单行");

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

        updateOrderDetailStatus(orderKey,orderLineNumber);
        updateOrderStatus(orderKey);


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

        DBHelper.insert("ITRN", itrn);

        ServiceDataMap serviceDataMap = new ServiceDataMap();
        serviceDataMap.setAttribValue("itrnkey", itrnKey);
        serviceDataMap.setAttribValue("toId", toId);
        return serviceDataMap;

    }




    public ServiceDataMap getOrderDetailStatus(String orderkey, String orderLineNumber) {
        try {

            String newStatus;

            Integer pickstotal = null;
            Integer released = null;
            Integer inpicking = null;
            Integer packed = null;
            Integer instaged = null;
            Integer loaded = null;
            Integer dpreleased = null;
            Integer dpinpicking = null;
            String orderdetailstatus = null;
            String maxcode = null;
            double SHIPPEDQTY = 0.0D;
            double OPENQTY = 0.0D;
            double QTYPREALLOCATED = 0.0D;
            double QTYALLOCATED = 0.0D;
            double QTYPICKED = 0.0D;
            int ODSTATUS = 0;
            int ISSUBSTITUTE = 0;
            String wpReleased = null;
            int qqRowCount = 0;
            //
            String pickdetailstatus = null;
            String locType = null;
            String dropid = null;
            String waveKey = null;
            int ordreleased = 0;
            int inpick = 0;
            int ordpacked = 0;
            int ordinstaged = 0;
            int ordloaded = 0;

            List<Map<String,String>>  res = DBHelper.executeQuery( " SELECT a.status, b.locationtype, a.dropid, a.wavekey FROM PICKDETAIL a, LOC b WHERE b.Loc = a.Loc AND a.OrderKey = ? AND a.OrderLineNumber = ?",
                    new Object[]{ orderkey, orderLineNumber});

            for(Map<String,String> r : res) {
                pickdetailstatus = r.get("status");
                locType = r.get("locationtype");
                dropid = r.get("dropid");
                waveKey = r.get("wavekey");
                ++ordreleased;
            }

            if (pickdetailstatus.equals("2") || pickdetailstatus.equals("3")) {
                ++inpick;
            }

            if (pickdetailstatus.equals("6")) {
                ++ordpacked;
            }

            if (locType.equals("STAGED")) {
                ++ordinstaged;
            }

            if (pickdetailstatus.equals("8")) {
                ++ordloaded;
            }


            pickstotal= qqRowCount;
            released=ordreleased;
            inpicking=inpick;
            packed=ordpacked;
            instaged=ordinstaged;
            loaded=ordloaded;


            Map<String,Object>  resStatus = DBHelper.getRawRecord(" SELECT SUM ( CASE WHEN b.status = '0' AND a.wavekey <> ' ' THEN 1 ELSE 0 END ) RELEASED, SUM ( CASE WHEN b.status >= '2' AND b.status <= '3' THEN 1 ELSE 0 END ) INPICKING FROM DEMANDALLOCATION a, TASKDETAIL b WHERE b.Sourcekey = a.DemandKey AND b.SourceType = 'DP' AND b.status = '0' AND a.OrderKey = ? AND a.OrderLineNumber = ?",
                    new Object[]{orderkey, orderLineNumber});

            dpreleased = (int) resStatus.get("RELEASED");
            dpinpicking = (int) resStatus.get("RELEASED");

            Map<String,Object>  resOrderLine = DBHelper.getRawRecord(" SELECT SHIPPEDQTY, OPENQTY, QTYPREALLOCATED, QTYALLOCATED, QTYPICKED, ISSUBSTITUTE, WPReleased, STATUS FROM OrderDetail WHERE OrderKey = ? AND OrderLineNumber = ?",
                    new Object[]{ orderkey, orderLineNumber});

            SHIPPEDQTY = (double) resOrderLine.get("SHIPPEDQTY");
            OPENQTY =  (double) resOrderLine.get("OPENQTY");
            QTYPREALLOCATED =  (double) resOrderLine.get("QTYPREALLOCATED");
            QTYALLOCATED = (double) resOrderLine.get("QTYALLOCATED");
            QTYPICKED =  (double) resOrderLine.get("QTYPICKED");
            ISSUBSTITUTE =  (int) resOrderLine.get("ISSUBSTITUTE");
            wpReleased =  (String) resOrderLine.get("WPReleased");
            ODSTATUS = (int) resOrderLine.get("STATUS");


            if (pickstotal== null) {
                pickstotal = 0;
            }

            if (released== null) {
                released = 0;
            }

            if (inpicking== null) {
                inpicking = 0;
            }

            if (packed== null) {
                packed = 0;
            } else if (QTYPICKED == 0.0D) {
                packed = 0;
            }

            if (instaged== null) {
                instaged = 0;
            }

            if (loaded== null) {
                loaded = 0;
            }

            if (dpreleased== null) {
                dpreleased = 0;
            }

            if (dpinpicking== null) {
                dpinpicking = 0;
            }


            if (SHIPPEDQTY > 0.0D && OPENQTY == 0.0D) {
                newStatus = "95";
            } else if (SHIPPEDQTY > 0.0D && OPENQTY > 0.0D && OPENQTY - QTYPREALLOCATED - QTYALLOCATED == 0.0D && (ODSTATUS >= 22 && ODSTATUS <= 29 || released > 0)) {
                newStatus = "27";
            } else if (SHIPPEDQTY > 0.0D && OPENQTY > 0.0D && OPENQTY - QTYPREALLOCATED - QTYALLOCATED == 0.0D) {
                newStatus = "92";
            } else if (loaded == pickstotal && pickstotal > 0 && QTYPICKED > 0.0D && OPENQTY - QTYPICKED == 0.0D) {
                newStatus = "88";
            } else if (SHIPPEDQTY > 0.0D && loaded > 0) {
                newStatus = "92";
            } else if (loaded > 0 && QTYPICKED > 0.0D && OPENQTY - QTYPICKED == 0.0D) {
                newStatus = "82";
            } else if (instaged == pickstotal && instaged > 0 && SHIPPEDQTY == 0.0D && OPENQTY - QTYPICKED == 0.0D && OPENQTY > 0.0D && QTYPICKED > 0.0D) {
                newStatus = "75";
            } else if (packed == pickstotal && packed > 0 && QTYPICKED == OPENQTY) {
                newStatus = "68";
            } else if (SHIPPEDQTY > 0.0D && packed > 0) {
                newStatus = "92";
            } else if (packed > 0) {
                newStatus = "61";
            } else if (SHIPPEDQTY > 0.0D && OPENQTY - QTYPICKED == 0.0D && OPENQTY > 0.0D && QTYPICKED > 0.0D) {
                newStatus = "57";
            } else if (SHIPPEDQTY == 0.0D && OPENQTY - QTYPICKED == 0.0D && OPENQTY > 0.0D && QTYPICKED > 0.0D) {
                newStatus = "55";
            } else if (SHIPPEDQTY > 0.0D && OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED >= 0.0D && QTYALLOCATED >= 0.0D && QTYPICKED > 0.0D) {
                newStatus = "53";
            } else if (SHIPPEDQTY != 0.0D || !(OPENQTY > 0.0D) || OPENQTY - QTYALLOCATED - QTYPICKED != 0.0D || !(QTYALLOCATED > 0.0D) || !(QTYPICKED > 0.0D) || (ODSTATUS < 22 || ODSTATUS > 29) && released <= 0) {
                if (SHIPPEDQTY == 0.0D && OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED == 0.0D && QTYALLOCATED > 0.0D && QTYPICKED > 0.0D) {
                    newStatus = "52";
                } else if (inpicking <= 0 && dpinpicking <= 0) {
                    if (SHIPPEDQTY != 0.0D || !(OPENQTY > 0.0D) || OPENQTY - QTYALLOCATED != 0.0D || released <= 0 && dpreleased <= 0) {
                        if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYPICKED > 0.0D && QTYALLOCATED > 0.0D && (ODSTATUS >= 22 && ODSTATUS <= 29 || released > 0)) {
                            newStatus = "25";
                        } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYALLOCATED > 0.0D && SHIPPEDQTY == 0.0D && (released > 0 || dpreleased > 0) && QTYPICKED == 0.0D) {
                            newStatus = "22";
                        } else if (SHIPPEDQTY == 0.0D && OPENQTY == 0.0D && QTYPICKED == 0.0D && ISSUBSTITUTE > 0) {
                            newStatus = "18";
                        } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED == 0.0D) {
                            newStatus = "17";
                        } else if (!(SHIPPEDQTY > 0.0D) || !(OPENQTY - QTYALLOCATED > 0.0D) || QTYPICKED != 0.0D || (ODSTATUS < 22 || ODSTATUS > 29) && released <= 0) {
                            if (SHIPPEDQTY > 0.0D && OPENQTY - QTYALLOCATED > 0.0D && QTYPICKED == 0.0D) {
                                newStatus = "16";
                            } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYPICKED > 0.0D && QTYALLOCATED >= 0.0D && (ODSTATUS >= 22 && ODSTATUS <= 29 || ODSTATUS == 51 || released > 0)) {
                                newStatus = "25";
                            } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYPICKED > 0.0D && QTYALLOCATED >= 0.0D) {
                                newStatus = "15";
                            } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYALLOCATED > 0.0D) {
                                newStatus = "14";
                            } else if (OPENQTY > 0.0D && OPENQTY - QTYPREALLOCATED == 0.0D && wpReleased != null && wpReleased.equals("1")) {
                                newStatus = "13";
                            } else if (OPENQTY > 0.0D && OPENQTY - QTYPREALLOCATED == 0.0D) {
                                newStatus = "12";
                            } else if (OPENQTY > 0.0D && QTYPREALLOCATED > 0.0D) {
                                newStatus = "11";
                            } else if (OPENQTY > 0.0D && QTYPREALLOCATED + QTYALLOCATED + QTYPICKED + SHIPPEDQTY == 0.0D) {
                                newStatus = "09";
                            } else if (SHIPPEDQTY + OPENQTY == 0.0D) {
                                newStatus = "95";
                            } else if (OPENQTY - QTYPREALLOCATED - QTYALLOCATED - QTYPICKED < 0.0D) {
                                newStatus = "-2";
                            } else {
                                newStatus = "-1";
                            }
                        } else {
                            newStatus = "27";
                        }
                    } else {
                        newStatus = "29";
                    }
                } else {
                    newStatus = "51";
                }
            } else {
                newStatus = "25";
            }

            int qqRowCount2 = 0;

            String storerkey = null;
            String sku = null;

            Map<String,String> resOrderLine2 = DBHelper.getRecord(" SELECT Status, Storerkey, SKU FROM OrderDetail WHERE OrderKey = ? AND OrderLineNumber = ?",
                    new Object[]{ orderkey,orderLineNumber});

            orderdetailstatus = resOrderLine2.get("Status");
            storerkey =  resOrderLine2.get("Storerkey");
            sku =  resOrderLine2.get("SKU");

            Map<String,String> resOrderstatussetup = DBHelper.getRecord(" SELECT MAX ( Code ) code FROM Orderstatussetup WHERE Orderflag = '1' AND Detailflag = '1' AND Enabled = '1' AND Code <= ?",
                    new Object[]{newStatus});
            maxcode = resOrderstatussetup.get("code");


            if (maxcode != null && maxcode.equals("09") && orderdetailstatus != null && orderdetailstatus.compareToIgnoreCase("02") >= 0 && orderdetailstatus != null && orderdetailstatus.compareToIgnoreCase("08") <= 0) {
                newStatus = orderdetailstatus;
            } else if (orderdetailstatus == null || !orderdetailstatus.equalsIgnoreCase("68") || !newStatus.equalsIgnoreCase("55") && !newStatus.equalsIgnoreCase("61")) {
                newStatus = maxcode;
            } else {
                newStatus = maxcode;
                double qtyPacked = 0.0D;
                double qtyOrdered = 0.0D;
                int qqRowCount6 = 0;

                PreparedStatement qqPrepStmt6 = null;
                ResultSet qqResultSet6 = null;

                Map<String,Object> resPackoutdetail = DBHelper.getRawRecord(" SELECT SUM(qtypicked) qtypicked FROM Packoutdetail WHERE Orderkey = ? AND Storerkey =? AND SKU = ?",
                        new Object[]{orderkey,storerkey,sku} );

                qtyPacked = (double) resPackoutdetail.get("qtypicked");


                if (qtyPacked > 0.0D) {

                    Map<String,Object> resOrderDetail = DBHelper.getRawRecord(" SELECT sum(Openqty+SHIPPEDQTY) qty FROM Orderdetail WHERE Orderkey = ? AND Storerkey =? AND SKU = ? ",
                            new Object[]{orderkey,storerkey,sku} );

                    qtyOrdered = (Integer) resOrderDetail.get("qty");

                    if (qtyPacked == qtyOrdered) {
                        newStatus = orderdetailstatus;
                    }
                }
            }


            if ("95".equals(newStatus)) {
                String isOrderRequireClose = DBHelper.getStringValue("SELECT REQUIREORDERCLOSE  FROM ORDERS WHERE OrderKey = ?",
                        new Object[]{orderkey});

                if (isOrderRequireClose != null && "1".equals(isOrderRequireClose)) {
                    newStatus = "94";
                }
            }

            ServiceDataMap serviceDataMap = new ServiceDataMap();
            serviceDataMap.setAttribValue("newStatus", newStatus);

            return serviceDataMap;
        } finally {
            //logger.debug("leaving getOrderDetailStatus( String pOrderkey, TextData pOrderLineNumber, DBSession context, String pNewStatus, String pDetailHistoryFlag)");
        }
    }

    public ServiceDataMap getOrderStatus(String orderkey) {

            String  oldStatus = DBHelper.getStringValue("SELECT Status FROM Orders WHERE OrderKey = ?"
                    ,new  Object[]{orderkey});

            String maxOrderDetailStatus = null;
            String maxCode = null;
            int orderDetailCount = 0;
            String maxStatus = null;
            String minStatus = null;
            String newStatus = "NA";

            Map<String,Object>  res = DBHelper.getRawRecord( " SELECT COUNT ( * ) count, MAX ( Status ) maxStatus, MIN ( Status ) minStatus FROM Orderdetail WHERE Orderkey = ? AND Status <> '18' and ( openqty>0 or shippedqty>0 or qtypreallocated>0 or qtyallocated>0 or qtypicked>0 )",
                    new Object[]{ orderkey});

            orderDetailCount = (Integer) res.get("count");
            maxStatus = (String) res.get("maxStatus");
            minStatus = (String) res.get("minStatus");



            if (maxStatus != null && maxStatus.equals("99") && minStatus != null && minStatus.equals("99")) {
                maxOrderDetailStatus = "99";
            } else if (maxStatus != null && maxStatus.equals("98") && minStatus != null && minStatus.equals("98")) {
                maxOrderDetailStatus = "98";
            } else if (maxStatus != null && maxStatus.equals("97") && minStatus != null && minStatus.equals("97")) {
                maxOrderDetailStatus = "97";
            } else if (maxStatus != null && maxStatus.equals("96") && minStatus != null && minStatus.equals("96")) {
                maxOrderDetailStatus = "96";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("95") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("99") <= 0 && minStatus != null && minStatus.equals("95")) {
                maxOrderDetailStatus = "95";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("94") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("99") <= 0 && minStatus != null && minStatus.equals("94")) {
                maxOrderDetailStatus = "94";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("92") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("95") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("95") < 0) {
                maxOrderDetailStatus = "92";
            } else if (maxStatus != null && maxStatus.equals("27") && minStatus != null && minStatus.compareToIgnoreCase("27") <= 0) {
                maxOrderDetailStatus = "92";
            } else if (maxStatus != null && maxStatus.equals("16") && minStatus != null && minStatus.compareToIgnoreCase("16") <= 0) {
                maxOrderDetailStatus = "92";
            } else if (maxStatus != null && maxStatus.equals("53") && minStatus != null && minStatus.compareToIgnoreCase("53") <= 0) {
                maxOrderDetailStatus = "92";
            } else if (maxStatus != null && maxStatus.equals("57") && minStatus != null && minStatus.compareToIgnoreCase("57") <= 0) {
                maxOrderDetailStatus = "92";
            } else if (maxStatus != null && maxStatus.equals("88") && minStatus != null && minStatus.equals("88")) {
                maxOrderDetailStatus = "88";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("82") >= 0 && minStatus != null && minStatus.compareToIgnoreCase("88") < 0) {
                maxOrderDetailStatus = "82";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("75") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("78") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("52") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("17") >= 0) {
                maxOrderDetailStatus = "52";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("75") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("78") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("15") <= 0) {
                maxOrderDetailStatus = "52";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("75") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("78") <= 0 && minStatus != null && minStatus.equals("55")) {
                maxOrderDetailStatus = "55";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("75") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("78") <= 0 && minStatus != null && minStatus.equals("68")) {
                maxOrderDetailStatus = "68";
            } else if (maxStatus != null && maxStatus.equals("75") && minStatus != null && minStatus.equals("75")) {
                maxOrderDetailStatus = "75";
            } else if (maxStatus != null && maxStatus.equals("68") && minStatus != null && minStatus.equals("68")) {
                maxOrderDetailStatus = "68";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("61") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("68") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("61") <= 0) {
                maxOrderDetailStatus = "61";
            } else if (maxStatus != null && maxStatus.equals("55") && minStatus != null && minStatus.equals("55")) {
                maxOrderDetailStatus = "55";
            } else if (maxStatus != null && maxStatus.equals("25") && minStatus != null && minStatus.compareToIgnoreCase("25") <= 0) {
                maxOrderDetailStatus = "52";
            } else if (maxStatus != null && maxStatus.equals("15") && minStatus != null && minStatus.compareToIgnoreCase("15") <= 0) {
                maxOrderDetailStatus = "52";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("52") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("55") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("55") < 0 && this.doesOrderDetailStatusExists(orderkey, "27")) {
                maxOrderDetailStatus = "92";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("52") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("55") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("55") < 0) {
                maxOrderDetailStatus = "52";
            } else if (maxStatus != null && maxStatus.equals("29") && minStatus != null && minStatus.equals("29")) {
                maxOrderDetailStatus = "29";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("27") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0 && (this.doesOrderDetailStatusExists(orderkey, "92") || this.doesOrderDetailStatusExists(orderkey, "27") || this.doesOrderDetailStatusExists(orderkey, "16"))) {
                maxOrderDetailStatus = "92";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("25") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0 && (this.doesOrderDetailStatusExists(orderkey, "52") || this.doesOrderDetailStatusExists(orderkey, "25") || this.doesOrderDetailStatusExists(orderkey, "15"))) {
                maxOrderDetailStatus = "52";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("22") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0 && this.doesOrderDetailStatusExists(orderkey, "16")) {
                maxOrderDetailStatus = "92";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("22") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0 && this.doesOrderDetailStatusExists(orderkey, "15")) {
                maxOrderDetailStatus = "52";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("22") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0) {
                maxOrderDetailStatus = "22";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("29") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("51") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("51") <= 0) {
                maxOrderDetailStatus = "29";
            } else if (maxStatus != null && maxStatus.equals("17") && minStatus != null && minStatus.equals("17")) {
                maxOrderDetailStatus = "17";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("14") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("17") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("17") < 0) {
                maxOrderDetailStatus = "14";
            } else if (maxStatus != null && maxStatus.equals("13") && minStatus != null && minStatus.equals("13")) {
                maxOrderDetailStatus = "13";
            } else if (maxStatus != null && maxStatus.equals("12") && minStatus != null && minStatus.equals("12")) {
                maxOrderDetailStatus = "12";
            } else if (maxStatus != null && maxStatus.equals("12") && minStatus != null && minStatus.compareToIgnoreCase("12") < 0) {
                maxOrderDetailStatus = "11";
            } else if (maxStatus != null && maxStatus.equals("11") && minStatus != null && minStatus.equals("11")) {
                maxOrderDetailStatus = "11";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("09") <= 0 && this.doesOrderDetailStatusExists(orderkey, "06")) {
                maxOrderDetailStatus = "06";
            } else if (maxStatus != null && maxStatus.equals("08") && minStatus != null && minStatus.equals("08")) {
                maxOrderDetailStatus = "08";
            } else if (maxStatus != null && maxStatus.equals("04") && minStatus != null && minStatus.equals("04")) {
                maxOrderDetailStatus = "04";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("04") <= 0 && minStatus != null && minStatus.equals("02")) {
                maxOrderDetailStatus = "02";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("09") <= 0) {
                maxOrderDetailStatus = "09";
            } else {
                maxOrderDetailStatus = "-1";
            }

            if (orderDetailCount == 0) {

                Map<String,Object> orderDtsInfo = DBHelper.getRawRecord(" SELECT COUNT ( * ) COUNT, MIN ( STATUS ) STATUS FROM ORDERDETAIL WHERE ORDERKEY = ? AND STATUS <> '18' ",
                        new Object[]{ orderkey});

                int ordDtZeroQtyCount = (Integer) orderDtsInfo.get("COUNT");
                String zeroQtyMinStatus = (String) orderDtsInfo.get("STATUS");

                if (ordDtZeroQtyCount != 0) {

                    newStatus = zeroQtyMinStatus;

                } else {

                    newStatus = "00";

                }

            } else {

                maxCode = DBHelper.getStringValue( " SELECT MAX (CODE) CODE FROM ORDERSTATUSSETUP WHERE ORDERFLAG = '1' AND HEADERFLAG = '1' AND ENABLED = '1' AND CODE <= ? ",
                        new Object[]{maxOrderDetailStatus});

                if (maxCode != null && maxCode.equals("09") && oldStatus != null && oldStatus.compareToIgnoreCase("02") >= 0 && oldStatus != null && oldStatus.compareToIgnoreCase("08") <= 0 && oldStatus != null && oldStatus.compareToIgnoreCase("06") != 0) {
                    newStatus = oldStatus;
                } else if (maxCode != null && maxCode.compareToIgnoreCase("02") >= 0 && maxCode != null && maxCode.compareToIgnoreCase("08") <= 0 && oldStatus != null && oldStatus.compareToIgnoreCase("96") >= 0) {
                    newStatus = oldStatus;
                } else {
                    newStatus = maxCode;
                }
            }

            if (newStatus != null && newStatus.equals("NA")) {
                newStatus = "00";
            }

            ServiceDataMap serviceDataMap = new ServiceDataMap();
            serviceDataMap.setAttribValue("status", oldStatus);
            serviceDataMap.setAttribValue("newStatus", newStatus);

            return serviceDataMap;

    }


    private boolean doesOrderDetailStatusExists(String orderKey, String statusKey) {

        List res = DBHelper.executeQuery("Select *  From ORDERDETAIL Where OrderKey = ? AND STATUS = ?",
                new Object[]{ orderKey,statusKey});

        return res.size()>0;
    }

    public void updateOrderStatus(String orderKey) {

        ServiceDataMap orderStatusParam = getOrderStatus(orderKey);

        String userid = EHContextHelper.getUser().getUsername();

        DBHelper.executeUpdate( "UPDATE ORDERS SET Status = ? , EditWho = ?, EditDate = ? WHERE Orderkey = ? "
                , new Object[]{
                        orderStatusParam.getString("newStatus"),
                        userid,
                        EHDateTimeHelper.getCurrentDate(),
                        orderKey
                });
    }

    public void updateOrderDetailStatus(String orderKey, String orderLineNumber) {

        ServiceDataMap orderDetailStatusParam = getOrderDetailStatus(orderKey, orderLineNumber);
        String newOrderDetailStatus = orderDetailStatusParam.getString("newStatus");

        String userid = EHContextHelper.getUser().getUsername();

        DBHelper.executeUpdate( "UPDATE ORDERDETAIL SET STATUS = ? , EDITWHO = ?, EDITDATE = ? WHERE ORDERKEY = ? AND ORDERLINENUMBER = ?"
                , new Object[]{
                        newOrderDetailStatus,
                        userid,
                        EHDateTimeHelper.getCurrentDate(),
                        orderKey,
                        orderLineNumber}
        );
    }

    /**
     * 分配库存
     * @param orderKey
     * @param orderLineNumber
     * @param lot
     * @param loc
     * @param id
     * @return pickDetailKey
     */
    public String addPickDetail(String orderKey, String orderLineNumber, String lot, String loc, String id, String packKey, String uom, BigDecimal uomQty) {

        BigDecimal qty = UOM.UOMQty2StdQty(packKey,uom, uomQty);

        String username = EHContextHelper.getUser().getUsername();
        LocalDateTime currentDate = EHDateTimeHelper.getCurrentDate();

        DBHelper.executeUpdate("UPDATE ORDERDETAIL SET EDITWHO=?,EDITDATE=?,STATUS=?,OPENQTY = OPENQTY + ?,  QTYALLOCATED=QTYALLOCATED+? WHERE ORDERKEY=? AND ORDERLINENUMBER=?"
                , new Object[] {username,currentDate,qty,orderKey,orderLineNumber});

        DBHelper.executeUpdate("UPDATE LOTXLOCXID SET QTYALLOCATED=QTYALLOCATED+?,EDITWHO=?,EDITDATE=? WHERE LOT=? AND LOC=? AND ID=?" , new Object[]{qty,username,currentDate,lot,loc,id});

        Map<String,Object> lotHashMap = VLotAttribute.findByLot(lot, true);

        String storerKey = lotHashMap.get("STORERKEY").toString();
        String sku = lotHashMap.get("SKU").toString();

        DBHelper.executeUpdate("UPDATE SKUXLOC SET QTYALLOCATED=QTYALLOCATED+?,EDITWHO=?,EDITDATE=? WHERE  LOC=?  AND STORERKEY=? AND SKU=?", new Object[]{qty,username,"@date",loc,storerKey,sku});

        DBHelper.executeUpdate("UPDATE LOT SET QTYALLOCATED=QTYALLOCATED+?,EDITWHO=?,EDITDATE=? WHERE LOT=?", new Object[]{qty,username,currentDate,lot});

        String pickDetailKey = IdGenerationHelper.generateIDByKeyName("PICKDETAILKEY",10);
        String cartonId = IdGenerationHelper.generateIDByKeyName("CARTONID",10);

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

        Map<String, String> locHashMap = DBHelper.getRecord("SELECT A.LOGICALLOCATION,A.PUTAWAYZONE FROM LOC A,PUTAWAYZONE B WHERE A.PUTAWAYZONE=B.PUTAWAYZONE AND A.LOC=?"
                ,new Object[] {loc},"库位");

        pdHashMap.put("DOOR", locHashMap.get("PUTAWAYZONE"));
        pdHashMap.put("ROUTE", locHashMap.get("LOGICALLOCATION"));

        DBHelper.insert("PICKDETAIL", pdHashMap);

        return pickDetailKey;
    }


    /**
     * 取消分配库存
     * @param pickDetailKey
     */
    public void deletePickDetail(String pickDetailKey) {




    }

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
