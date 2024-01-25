package com.enhantec.wms.backend.common.outbound;

import com.enhantec.framework.common.exception.EHApplicationException;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;

import com.enhantec.framework.common.utils.EHContextHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.List;

public class Orders {

    public static Map<String, String> findByOrderKey( String orderKey, boolean checkExist) {

        String SQL="select * from orders where orderkey = ?";

        Map<String,String> record= DBHelper.getRecord( SQL, new Object[]{ orderKey},"订单");
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("未找到订单"+orderKey);
        return record;
    }


    public static List<Map<String,String>> findOrderDetailsByOrderKey( String orderKey, boolean checkExist) {

        String SQL="SELECT * FROM ORDERDETAIL WHERE ORDERKEY = ? ORDER BY ORDERLINENUMBER ";
        List<Map<String,String>> list= DBHelper.executeQuery( SQL, new Object[]{ orderKey});
        if(checkExist && list.size() == 0) ExceptionHelper.throwRfFulfillLogicException("未找到订单号为"+orderKey+"的订单明细");
        return list;

    }

    public static Map<String,String> findOrderDetailByKey( String orderKey,String orderLineNumber, boolean checkExist) {

        String SQL="select * from orderdetail where orderkey = ? and orderlinenumber = ?";
        Map<String,String> rec= DBHelper.getRecord( SQL,
                new Object[]{ orderKey , orderLineNumber},
                "订单行"+orderKey+" "+orderLineNumber,checkExist);
        return rec;

    }

    public static Map<String, Object> findOrderDetail(String orderKey, String orderLineNumber) {
        Map<String,Object> orderDetailMap = DBHelper.getRawRecord(
                "SELECT A.STORERKEY, A.TYPE, B.SKU, B.PACKKEY,B.UOM, B.IDREQUIRED, B.ORIGINALQTY, B.OPENQTY, B.QTYPREALLOCATED, B.QTYALLOCATED, B.QTYPICKED, B.SHIPPEDQTY, B.STATUS, B.NEWALLOCATIONSTRATEGY, " +
                        " B.LOTTABLE01, B.LOTTABLE02, B.LOTTABLE03, B.LOTTABLE04, B.LOTTABLE05, B.LOTTABLE06, B.LOTTABLE07, B.LOTTABLE08, B.LOTTABLE09, B.LOTTABLE10, B.LOTTABLE11, B.LOTTABLE12"
                        + " FROM ORDERS A, ORDERDETAIL B"
                        + " WHERE A.ORDERKEY=B.ORDERKEY AND B.ORDERKEY=? AND B.ORDERLINENUMBER=?", new String[] {orderKey, orderLineNumber},"订单行",false);

        if (orderDetailMap == null) throw new EHApplicationException("未找到出库单明细行("+orderKey+","+orderLineNumber+")");

        return orderDetailMap;
    }

}
