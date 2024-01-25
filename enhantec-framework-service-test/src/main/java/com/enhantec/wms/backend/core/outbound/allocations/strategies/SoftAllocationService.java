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

package com.enhantec.wms.backend.core.outbound.allocations.strategies;

import com.enhantec.framework.common.exception.EHApplicationException;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.framework.common.utils.EHDateTimeHelper;
import com.enhantec.wms.backend.common.inventory.InventoryHold;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.core.outbound.OutboundUtilHelper;
import com.enhantec.wms.backend.core.outbound.allocations.AllocationExecutor;
import com.enhantec.wms.backend.core.outbound.allocations.OrderDetailAllocInfo;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SoftAllocationService implements AllocationExecutor {


    private String applyInvFilterCondition(String sql, ArrayList<Object> params, String attributeName, Object attributeValue) {

        String sqlToBeAppend = "";

        if (!UtilHelper.isEmpty(attributeValue))
        {
            if(attributeValue instanceof String){
                String[] orderSelectedLots = attributeValue.toString().split(",");
                sqlToBeAppend += " AND " + attributeName + " IN (";
                for(String orderSelectedLot : orderSelectedLots)
                {
                    if(!UtilHelper.isEmpty(orderSelectedLot)) sqlToBeAppend += " ?,"; params.add(orderSelectedLot);
                }
                sqlToBeAppend = sqlToBeAppend.substring(0, sqlToBeAppend.length() - 1)+")";
            }else  if(attributeValue instanceof LocalDateTime) {
                sqlToBeAppend +=" AND " + attributeName + " = ? ";
                params.add(attributeValue);
            }else {
                throw new EHApplicationException("分配失败：不支持对该数据类型进行过滤");
            }
            sql = sql + sqlToBeAppend;

        }
        return sql;
    }


    /**
     * 获取能够匹配所有批属性要求的批次列表(不区分是否冻结或不冻结的库存）
     * @param orderKey
     * @param orderLineNumber
     * @return
     */
    public List<Map<String,Object>> getCandidateLotInfo(String orderKey, String orderLineNumber){

        Map<String, Object> orderDetailMap = Orders.findOrderDetail(orderKey, orderLineNumber);

        String sql="SELECT L.LOT,L.QTY-L.QTYPREALLOCATED-L.QTYALLOCATED-L.QTYPICKED-L.QTYONHOLD AS QTYUNHOLDAVAIL, L.QTYONHOLD AS QTYONHOLDAVAIL FROM LOT L, LOTATTRIBUTE LA WHERE QTY-QTYPREALLOCATED-QTYALLOCATED-QTYPICKED>0 AND L.LOT = LA.LOT ";


        ArrayList<Object> params = new ArrayList<>();

        sql+=" AND L.STORERKEY=?"; params.add(orderDetailMap.get("STORERKEY").toString());
        sql+=" AND L.SKU=?"; params.add(orderDetailMap.get("SKU").toString());

        sql = applyInvFilterCondition(sql, params, "LOTTABLE01", orderDetailMap.get("LOTTABLE01"));
        sql = applyInvFilterCondition(sql, params, "LOTTABLE02", orderDetailMap.get("LOTTABLE02"));
        sql = applyInvFilterCondition(sql, params, "LOTTABLE03", orderDetailMap.get("LOTTABLE03"));
        sql = applyInvFilterCondition(sql, params, "LOTTABLE04", orderDetailMap.get("LOTTABLE04"));
        sql = applyInvFilterCondition(sql, params, "LOTTABLE05", orderDetailMap.get("LOTTABLE05"));
        sql = applyInvFilterCondition(sql, params, "LOTTABLE06", orderDetailMap.get("LOTTABLE06"));
        sql = applyInvFilterCondition(sql, params, "LOTTABLE07", orderDetailMap.get("LOTTABLE07"));
        sql = applyInvFilterCondition(sql, params, "LOTTABLE08", orderDetailMap.get("LOTTABLE08"));
        sql = applyInvFilterCondition(sql, params, "LOTTABLE09", orderDetailMap.get("LOTTABLE09"));
        sql = applyInvFilterCondition(sql, params, "LOTTABLE10", orderDetailMap.get("LOTTABLE10"));
        sql = applyInvFilterCondition(sql, params, "LOTTABLE11", orderDetailMap.get("LOTTABLE11"));
        sql = applyInvFilterCondition(sql, params, "LOTTABLE12", orderDetailMap.get("LOTTABLE12"));


        if (!UtilHelper.isEmpty(orderDetailMap.get("IDREQUIRED")))
        {
            List<String> idFilteredLotList = DBHelper.getValueList("SELECT LOT FROM LOTXLOCXID WHERE ID=? AND QTY>0", new Object[] {orderDetailMap.get("IDREQUIRED").toString()},"指定容器批次");
            if (idFilteredLotList.size() == 0){
                return new ArrayList<>();
                //throw new EHApplicationException("未找到指定容器条码("+orderDetailMap.get("IDREQUIRED")+")对应库存");
            }

            sql+=" AND LOT in ( ";

            for(String idFilteredLot : idFilteredLotList)
            {
                if(!UtilHelper.isEmpty(idFilteredLot)) sql += " ?, "; params.add(idFilteredLot);
            }
            sql = sql.substring(0, sql.length() - 1)+")";
        }

        sql+=" ORDER BY LOTTABLE04, LOT ";

        List<Map<String,Object>> filteredLotList = DBHelper.executeQueryRawData(sql, params);

        return filteredLotList;

    }


    /**
     * 正常库存按批次分配（用于正常库存按批次分配和动态拣货的分配）
     * @param orderDetailAllocInfo
     * @return
     */
    public void allocate(OrderDetailAllocInfo orderDetailAllocInfo){

        List<String> orderHoldReasons = InventoryHold.getOrderTypeHoldReasons(orderDetailAllocInfo.getOrderType());

        //动态拣货跳过需要冻结分配的订单行
        if(orderHoldReasons.stream().map(e-> !e.equals("OK")).count()>0) return;

        String username = EHContextHelper.getUser().getUsername();
        LocalDateTime currentDate = EHDateTimeHelper.getCurrentDate();

        List<String> allocLotList = new ArrayList<>();


        List<Map<String,Object>> candidateLotInfoList = getCandidateLotInfo(orderDetailAllocInfo.getOrderKey(),orderDetailAllocInfo.getOrderLineNumber());

        for(Map<String, Object> candidateLotInfo : candidateLotInfoList)
        {
            BigDecimal qtyLotAlloc = null; //当前批次可分配量

            String lot = candidateLotInfo.get("LOT").toString();
            BigDecimal qtyUnHoldAvail = (BigDecimal)candidateLotInfo.get("QTYUNHOLDAVAIL");

            if (qtyUnHoldAvail.compareTo(BigDecimal.ZERO)>0){

                if(qtyUnHoldAvail.compareTo(orderDetailAllocInfo.getQtyToBeAllocate())>=0){

                    qtyLotAlloc = orderDetailAllocInfo.getQtyToBeAllocate();
                    orderDetailAllocInfo.setQtyToBeAllocate(BigDecimal.ZERO);

                }else if(qtyUnHoldAvail.compareTo(orderDetailAllocInfo.getQtyToBeAllocate())<0){

                    qtyLotAlloc = qtyUnHoldAvail;
                    orderDetailAllocInfo.setQtyToBeAllocate(orderDetailAllocInfo.getQtyToBeAllocate().subtract(qtyUnHoldAvail));

                }

                DBHelper.executeUpdate("UPDATE LOT SET QTYPREALLOCATED = QTYPREALLOCATED + ?,EDITWHO = ?,EDITDATE = ? WHERE LOT = ?", new Object[]{qtyLotAlloc,username,currentDate,lot});

                if(!DBHelper.executeUpdate("UPDATE PREALLOCATEPICKDETAIL SET QTY = QTY + ?, EDITWHO = ?, EDITDATE = ? WHERE ORDERKEY = ? AND ORDERLINENUMBER = ? AND LOT = ?" , new Object[]{qtyLotAlloc, username,currentDate,orderDetailAllocInfo.getOrderKey(),orderDetailAllocInfo.getOrderLineNumber(), lot})){
                    DBHelper.executeUpdate("INSERT INTO PREALLOCATEPICKDETAIL (ORDERKEY, ORDERLINENUMBER, LOT, QTY, ADDWHO, ADDDATE, EDITWHO, EDITDATE) VALUES (?,?,?,?,?,?,?,?)" , new Object[]{ orderDetailAllocInfo.getOrderKey(), orderDetailAllocInfo.getOrderLineNumber(), lot,qtyLotAlloc, username,currentDate,username,currentDate});
                }

                DBHelper.executeUpdate("UPDATE ORDERDETAIL SET QTYPREALLOCATED = QTYPREALLOCATED + ?,EDITWHO = ?,EDITDATE = ? WHERE ORDERKEY = ? AND ORDERLINENUMBER = ? ", new Object[]{qtyLotAlloc, username, currentDate, orderDetailAllocInfo.getOrderKey(), orderDetailAllocInfo.getOrderLineNumber()});


                allocLotList.add(lot);
            }

            if(orderDetailAllocInfo.getQtyToBeAllocate().compareTo(BigDecimal.ZERO) == 0){
                break;
            }

        }

        OutboundUtilHelper.updateOrderDetailStatus(orderDetailAllocInfo.getOrderKey(),orderDetailAllocInfo.getOrderLineNumber());
        OutboundUtilHelper.updateOrderStatus(orderDetailAllocInfo.getOrderKey());

    }

}
