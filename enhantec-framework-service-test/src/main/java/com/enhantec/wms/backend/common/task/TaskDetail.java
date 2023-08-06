package com.enhantec.wms.backend.common.task;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.Map;
import java.util.List;

public class TaskDetail {

    public static Map<String, String> findById( String id, boolean checkExist) {

        String SQL="select * from taskdetail where TASKDETAILKEY = ?";

        Map<String,String> record= DBHelper.getRecord( SQL, new Object[]{ id},"任务");
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("未找到任务"+id);
        return record;
    }


    public static List<Map<String, String>> findByOrderKey( String orderKey, boolean checkExist) {

        String sql="select * from taskdetail where ORDERKEY = ?";

        return DBHelper.executeQuery( sql, new Object[]{ orderKey});

    }

}
