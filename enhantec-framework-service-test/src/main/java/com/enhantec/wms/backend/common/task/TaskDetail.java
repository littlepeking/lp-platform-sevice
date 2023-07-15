package com.enhantec.wms.backend.common.task;

import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

public class TaskDetail {

    public static HashMap<String, String> findById(Context context, String id, boolean checkExist) {

        String SQL="select * from taskdetail where TASKDETAILKEY = ?";

        HashMap<String,String> record= DBHelper.getRecord(context, SQL, new Object[]{ id},"任务");
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("未找到任务"+id);
        return record;
    }


    public static List<HashMap<String, String>> findByOrderKey(Context context, String orderKey, boolean checkExist) {

        String sql="select * from taskdetail where ORDERKEY = ?";

        return DBHelper.executeQuery(context, sql, new Object[]{ orderKey});

    }

}
