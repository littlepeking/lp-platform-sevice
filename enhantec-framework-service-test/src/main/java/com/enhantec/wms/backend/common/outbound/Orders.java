package com.enhantec.wms.backend.common.outbound;

import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.framework.UserInfo;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

public class Orders {

    public static HashMap<String, String> findByOrderKey(Context context, String orderKey, boolean checkExist) {

        String SQL="select * from orders where orderkey = ?";

        HashMap<String,String> record= DBHelper.getRecord(context, SQL, new Object[]{ orderKey},"订单");
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("未找到订单"+orderKey);
        return record;
    }

    public static HashMap<String, String> findByOrderKey(UserInfo userInfo, String orderKey, boolean checkExist) {

        String SQL="select * from orders where orderkey = ?";

        HashMap<String,String> record= DBHelper.getRecord(userInfo, SQL, new Object[]{ orderKey},"订单",checkExist);
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("未找到订单"+orderKey);
        return record;
    }

    public static List<HashMap<String,String>> findOrderDetailsByOrderKey(Context context, String orderKey, boolean checkExist) {

        String SQL="select * from orderdetail where orderkey = ?";
        List<HashMap<String,String>> list= DBHelper.executeQuery(context, SQL, new Object[]{ orderKey});
        if(checkExist && list.size() == 0) ExceptionHelper.throwRfFulfillLogicException("未找到订单号为"+orderKey+"的订单明细");
        return list;

    }

    public static HashMap<String,String> findOrderDetailByKey(Context context, String orderKey,String orderLineNumber, boolean checkExist) {

        String SQL="select * from orderdetail where orderkey = ? and orderlinenumber = ?";
        HashMap<String,String> rec= DBHelper.getRecord(context, SQL,
                new Object[]{ orderKey , orderLineNumber},
                "订单行"+orderKey+orderLineNumber,checkExist);
        return rec;

    }

}
