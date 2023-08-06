package com.enhantec.wms.backend.common.outbound;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;

import com.enhantec.framework.common.utils.EHContextHelper;
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

        String SQL="select * from orderdetail where orderkey = ?";
        List<Map<String,String>> list= DBHelper.executeQuery( SQL, new Object[]{ orderKey});
        if(checkExist && list.size() == 0) ExceptionHelper.throwRfFulfillLogicException("未找到订单号为"+orderKey+"的订单明细");
        return list;

    }

    public static Map<String,String> findOrderDetailByKey( String orderKey,String orderLineNumber, boolean checkExist) {

        String SQL="select * from orderdetail where orderkey = ? and orderlinenumber = ?";
        Map<String,String> rec= DBHelper.getRecord( SQL,
                new Object[]{ orderKey , orderLineNumber},
                "订单行"+orderKey+orderLineNumber,checkExist);
        return rec;

    }

}
