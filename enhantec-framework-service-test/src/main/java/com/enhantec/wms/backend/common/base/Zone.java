package com.enhantec.wms.backend.common.base;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.DBHelper;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.HashMap;

public class Zone {

    public static HashMap<String, String> findById( String putawayzone, boolean checkExist) throws Exception {
        HashMap<String,String> putawayzoneRecord= DBHelper.getRecord( "select * from putawayzone where id = ? " , new Object[]{putawayzone},"库区"+putawayzone+"不存在",checkExist);

        return putawayzoneRecord;
    }

}
