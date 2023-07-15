package com.enhantec.wms.backend.common.base;

import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.DBHelper;

import java.sql.Connection;
import java.util.HashMap;

public class Zone {

    public static HashMap<String, String> findById(Context context, String putawayzone, boolean checkExist) throws Exception {
        HashMap<String,String> putawayzoneRecord= DBHelper.getRecord(context
                , "select * from putawayzone where id = ? " , new Object[]{putawayzone},"库区"+putawayzone+"不存在",checkExist);

        return putawayzoneRecord;
    }

}
