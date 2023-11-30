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

package com.enhantec.wms.backend.core.outbound.allocations.strategyCode;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.framework.common.utils.EHDateTimeHelper;
import com.enhantec.wms.backend.common.Const;
import com.enhantec.wms.backend.common.inventory.InventoryHold;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.core.WMSServiceNames;
import com.enhantec.wms.backend.core.outbound.OutboundUtilHelper;
import com.enhantec.wms.backend.core.outbound.allocations.AllocInfo;
import com.enhantec.wms.backend.core.outbound.allocations.AllocationExecutor;
import com.enhantec.wms.backend.core.outbound.allocations.PreAllocationService;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.IdGenerationHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(WMSServiceNames.OUTBOUND_ALLOCATION_PICK_CODE_A01)
@AllArgsConstructor
/**
 * 订单分配的基础类，后续功能需要对orderLineAllocInfo进行逐步扩展
 */
public class A01 implements AllocationExecutor {

    private final PreAllocationService preAllocationService;


    /**
     * 按容器分配（用于正常库存和冻结库存的任务拣货）
     * @param allocInfo
     * @return 订单号是否全部分配完成
     */
    @Override
    public boolean allocate(AllocInfo allocInfo){

        List<Map<String,Object>>  candidateLotInfoList = preAllocationService.getCandidateLotInfo(allocInfo.getOrderKey(), allocInfo.getOrderLineNumber());

        for(Map<String, Object> candidateLotInfo : candidateLotInfoList)
        {
            String lot = candidateLotInfo.get("LOT").toString();
            BigDecimal qtyUnHoldAvail = (BigDecimal)candidateLotInfo.get("QTYUNHOLDAVAIL");
            BigDecimal qtyOnHoldAvail = (BigDecimal)candidateLotInfo.get("QTYONHOLDAVAIL");
            List<String> orderHoldReasons = InventoryHold.getOrderTypeHoldReasons(allocInfo.getOrderType());

            //如果订单为非冻结分配类型的订单，则可先根据批次正常可用库存数量做校验，来决定是否有必要进一步分配明细容器库存
            if(orderHoldReasons.isEmpty() || orderHoldReasons.size()==1 && orderHoldReasons.get(0).equals("OK")){
                if(qtyUnHoldAvail.compareTo(BigDecimal.ZERO)<=0){
                    //无可用正常库存
                    continue;
                }else {
                    //使用正常库存分配的情况（使用此分支的目的是为了提高SQL检索效率，不需要加载冻结库存的数据）
                    List<Map<String, Object>> idInfoListByLot = findIdAllocCandidatesByLotAndRequiredId(lot, allocInfo.getIdRequired(), false);

                    for(Map<String, Object> idInfoByLot : idInfoListByLot){

                        if(allocateIdInv(allocInfo, idInfoByLot)) break;

                    }
                }
            }
            //如果订单冻结原因中不包含OK，则说明订单只需要分配冻结库存，此时可先根据当前批次冻结的可用库存数量做校验，来决定是否有必要进一步分配明细容器库存
            else if(!orderHoldReasons.contains("OK")){
                //无可用冻结库存
                if(qtyOnHoldAvail.compareTo(BigDecimal.ZERO)<=0){
                    continue;
                }else{
                    //冻结分配且不使用正常库存的情况（使用此分支的目的是为了提高SQL检索效率，不需要加载正常库存的数据）
                    List<Map<String, Object>> holdIdInfoListByLot = findIdAllocCandidatesByLotAndRequiredId(lot, allocInfo.getIdRequired(),true);

                    for(Map<String, Object> holdIdInfoByLot : holdIdInfoListByLot){
                        //STATUS=HOLD
                        List<String> idHoldReasons = InventoryHold.getIdHoldReasons(holdIdInfoByLot.get("LOT").toString(),holdIdInfoByLot.get("LOC").toString(),holdIdInfoByLot.get("ID").toString());

                        if(UtilHelper.hasIntersection(idHoldReasons,orderHoldReasons)) {

                            if(allocateIdInv(allocInfo, holdIdInfoByLot)) break;

                        }
                    }
                }
            }else {
                //当前批次存在可分配的库存，且允许分配任何正常库存或者满足任一冻结原因的明细库存
                List<Map<String, Object>> idInfoListByLot = findIdAllocCandidatesByLotAndRequiredId(lot, allocInfo.getIdRequired(),null);

                for(Map<String,Object> idInfo : idInfoListByLot){

                    //此时如果id为正常库存则可以直接分配，因为此分支已包含了OK的冻结原因
                    if("OK".equals(idInfo.get("STATUS"))){

                        if(allocateIdInv(allocInfo, idInfo)) break;

                    }else {
                        //STATUS=HOLD
                        List<String> idHoldReasons = InventoryHold.getIdHoldReasons(idInfo.get("LOT").toString(),idInfo.get("LOC").toString(),idInfo.get("ID").toString());
                        if(UtilHelper.hasIntersection(idHoldReasons,orderHoldReasons)){

                            if(allocateIdInv(allocInfo, idInfo)) break;

                        }
                    }
                }
            }
        }

        OutboundUtilHelper.updateOrderDetailStatus(allocInfo.getOrderKey(), allocInfo.getOrderLineNumber());
        OutboundUtilHelper.updateOrderStatus(allocInfo.getOrderKey());

        return allocInfo.getQtyToBeAllocate().compareTo(BigDecimal.ZERO) <= 0;
    }


    /**
     * 按最小计量单位分配
     * @param allocInfo 订单行分配请求参数
     * @param idInfoByLot 当前分配的容器ID库存信息
     * @return 订单行是否全部分配完成
     */
    private boolean allocateIdInv(AllocInfo allocInfo, Map<String, Object> idInfoByLot) {

        String username = EHContextHelper.getUser().getUsername();
        LocalDateTime currentDate = EHDateTimeHelper.getCurrentDate();

        BigDecimal qtyIdAllocated = BigDecimal.ZERO;

        BigDecimal qtyIdAvail = (BigDecimal) idInfoByLot.get("QTYAVAIL");
        if (qtyIdAvail.compareTo(BigDecimal.ZERO)>0){

            if(qtyIdAvail.compareTo(allocInfo.getQtyToBeAllocate())<0 /*|| orderLineAllocInfo.isFullLpnAlloc() TODO isFullLpnAlloc待增加配置于分配策略明细中*/){
                qtyIdAllocated = qtyIdAvail;
            }else if(qtyIdAvail.compareTo(allocInfo.getQtyToBeAllocate())>=0){
                qtyIdAllocated = allocInfo.getQtyToBeAllocate();
            }

            DBHelper.executeUpdate("UPDATE LOTXLOCXID SET QTYALLOCATED = QTYALLOCATED + ?,EDITWHO = ?,EDITDATE = ? WHERE LOT = ? AND LOC = ? AND ID  = ? AND QTY > 0", new Object[]{ qtyIdAllocated, username, currentDate, idInfoByLot.get("LOT"), idInfoByLot.get("LOC"), idInfoByLot.get("ID")});

            //PACKKEY和UOM代表了当前分配使用的包装和计量单位，如包装或计量单位不同则不允许合并到之前已存在的拣货明细。
            //默认相同的UOM允许合并记录，当前策略为按最小单位UOM=6分配并合并，不支持按计量单位分段拣货
            //TODO 新增按计量单位分段的分配策略
            //如果UOM=6,为最小单位分配，标准数量单位为1，如EA
            //如果UOM=1,为当前物料默认包装下容器的标准数量分配，如PL
            //如果UOM=2,为当前物料默认包装下箱的标准数量分配，如CS

            Map<String, Object> orderDetailInfo = Orders.findOrderDetail(allocInfo.getOrderKey(), allocInfo.getOrderLineNumber());

            Map<String, Object> pickDetailInfo = DBHelper.getRawRecord("SELECT * FROM PICKDETAIL WHERE STATUS IN (0,1) AND ORDERKEY = ? AND ORDERLINENUMBER = ? AND PACKKEY = ? AND UOM = ? AND LOT = ? AND FROMLOC = ? AND ID = ?",
                    new Object[]{ allocInfo.getOrderKey(), allocInfo.getOrderLineNumber(), orderDetailInfo.get("PACKKEY"),"6", idInfoByLot.get("LOT"), idInfoByLot.get("LOC"), idInfoByLot.get("ID")},"拣货明细",false);

            if (pickDetailInfo!=null) {

                DBHelper.executeUpdate("UPDATE PICKDETAIL SET QTY = QTY + ?, EDITWHO = ?, EDITDATE = ? WHERE STATUS IN (0,1) AND ORDERKEY = ? AND ORDERLINENUMBER = ? AND PACKKEY = ? AND UOM = ? AND LOT = ? AND FROMLOC = ? AND ID = ?",
                        new Object[]{qtyIdAllocated, username, currentDate, allocInfo.getOrderKey(), allocInfo.getOrderLineNumber(), orderDetailInfo.get("PACKKEY"),"6", idInfoByLot.get("LOT"), idInfoByLot.get("LOC"), idInfoByLot.get("ID")});

                if(pickDetailInfo.get("STATUS").equals("1")){
                    DBHelper.executeUpdate("UPDATE TASKDETAIL SET QTY = QTY + ?, UOMQTY = UOMQTY + ?, EDITWHO = ?, EDITDATE = ? WHERE PICKDETAILKEY = ? ",
                            new Object[]{qtyIdAllocated, qtyIdAllocated, username, currentDate, pickDetailInfo.get("PICKDETAILKEY")});
                }

            }else {
                Map<String, Object> pickDetailHashMap = new HashMap<>();
                pickDetailHashMap.put("PICKDETAILKEY", IdGenerationHelper.getNCounterStrWithLength("PICKDETAILKEY", 10));
                pickDetailHashMap.put("WHSEID", EHContextHelper.getCurrentOrgId());
                pickDetailHashMap.put("ORDERKEY", allocInfo.getOrderKey());
                pickDetailHashMap.put("ORDERLINENUMBER", allocInfo.getOrderLineNumber());
                pickDetailHashMap.put("PACKKEY", orderDetailInfo.get("PACKKEY"));
                pickDetailHashMap.put("UOM", "6");
                pickDetailHashMap.put("LOT", idInfoByLot.get("LOT"));
                pickDetailHashMap.put("FROMLOC", idInfoByLot.get("LOC"));
                pickDetailHashMap.put("LOC", idInfoByLot.get("LOC"));
                pickDetailHashMap.put("TOLOC", "PICKTO");
                pickDetailHashMap.put("ID", idInfoByLot.get("ID"));
                pickDetailHashMap.put("STATUS", "0");
                pickDetailHashMap.put("QTY", qtyIdAllocated);
                pickDetailHashMap.put("ADDWHO", username);
                pickDetailHashMap.put("ADDDATE", currentDate);
                pickDetailHashMap.put("EDITWHO", username);
                pickDetailHashMap.put("EDITDATE", currentDate);
                DBHelper.executeInsert("PICKDETAIL", pickDetailHashMap);
            }
            //here QTYONHOLD means QTYONHOLDAVAIL
            DBHelper.executeUpdate("UPDATE LOT SET QTYALLOCATED = QTYALLOCATED + ?, QTYONHOLD = QTYONHOLD - ?, EDITWHO = ?,EDITDATE = ? WHERE LOT = ?",
                    new Object[]{qtyIdAllocated, "HOLD".equals(idInfoByLot.get("STATUS")) ? qtyIdAllocated : 0, username, currentDate, idInfoByLot.get("LOT")});
        }

        allocInfo.setQtyToBeAllocate(allocInfo.getQtyToBeAllocate().subtract(qtyIdAllocated));

        updateAllocStatus(allocInfo, qtyIdAllocated);

        return allocInfo.getResult().getAllocStatus() == AllocInfo.AllocStatus.fullyAllocated;

    }

    private void updateAllocStatus(AllocInfo allocInfo, BigDecimal qtyIdAllocated) {

        if(allocInfo.getQtyToBeAllocate().compareTo(BigDecimal.ZERO) <= 0){

            allocInfo.getResult().setAllocStatus(AllocInfo.AllocStatus.fullyAllocated);

        }else if(allocInfo.getResult().getAllocStatus() != AllocInfo.AllocStatus.partialAllocated && qtyIdAllocated.compareTo(BigDecimal.ZERO)>0){

            allocInfo.getResult().setAllocStatus(AllocInfo.AllocStatus.partialAllocated);

        }
    }


    /**
     * 用于分配容器时，查询当前lot下的所有的容器明细库存
     * 这里获取容器列表时，暂时没有对容器的数量进行排序，根据实际项目如需要先分配整容器，则可以增加条件 ORDER BY A.QTY-A.QTYPICKED-A.QTYALLOCATED DESC, 反之ASC则代表优先出零散的容器。
     * @param lot
     * @return
     */
    public List<Map<String, Object>> findIdAllocCandidatesByLotAndRequiredId(String lot, String id, Boolean hold) {

        String sql = "select A.ID,A.LOT,A.LOC,A.ID,A.STORERKEY,A.SKU,A.QTY,A.QTYALLOCATED,A.QTYPICKED,A.QTY-A.QTYPICKED-A.QTYALLOCATED QTYAVAIL, A.STATUS "
                + ",B.LOTTABLE01,B.LOTTABLE02,B.ELOTTABLE02,B.ELOTTABLE03"
                + ",FORMAT(B.LOTTABLE04,'"+ Const.DateTimeFormat+"') AS LOTTABLE04"
                + ",FORMAT(B.ELOTTABLE05,'"+Const.DateTimeFormat+"') as ELOTTABLE05"
                + ",B.ELOTTABLE06,B.LOTTABLE06,B.ELOTTABLE07,B.ELOTTABLE08,B.ELOTTABLE09,B.LOTTABLE10"
                + ",FORMAT(B.ELOTTABLE11,'"+Const.DateTimeFormat+"') as ELOTTABLE11"
                + ",FORMAT(B.ELOTTABLE12,'"+Const.DateTimeFormat+"') as ELOTTABLE12"
                + " FROM LOTXLOCXID A,V_LOTATTRIBUTE B "
                + " WHERE A.LOT=B.LOT AND A.QTY-QTYALLOCATED-QTYPICKED>0 AND A.LOT = ? ";


        List params = new ArrayList();
        params.add(lot);

        if(!UtilHelper.isEmpty(id)){
            sql += " AND A.ID = ? ";
            params.add(id);
        }

        if(hold != null){
            sql +=  hold ? " AND A.STATUS = 'HOLD' " : " AND A.STATUS = 'OK' ";
        }

        List<Map<String,Object>> idList = DBHelper.executeQueryRawData(sql, params.toArray());
        return idList;
    }



}
