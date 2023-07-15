package com.enhantec.wms.backend.common.base;

import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

public class Loc {

    public static  HashMap<String,String> findById(Context context, String loc, boolean checkExist) throws FulfillLogicException {

        if(UtilHelper.isEmpty(loc)) ExceptionHelper.throwRfFulfillLogicException("库位不能为空");

        HashMap<String,String>  locRecord = DBHelper.getRecord(context,"select * from loc where loc = ?", new Object[]{ loc},"库位");
        if(checkExist && locRecord == null) ExceptionHelper.throwRfFulfillLogicException("库位"+loc+"不存在");

        return locRecord;
    }

    public static List<HashMap<String,String>> findByZone(Context context, String putawayZone, boolean checkExist) throws FulfillLogicException {

        if(UtilHelper.isEmpty(putawayZone)) ExceptionHelper.throwRfFulfillLogicException("库区代码不能为空");

        List<HashMap<String,String>> locs = DBHelper.executeQuery(context,"select * from loc where putawayzone = ?", new Object[]{putawayZone});
        if(checkExist && locs.size() == 0) ExceptionHelper.throwRfFulfillLogicException("在区"+putawayZone+"中未找到库位");

        return locs;
    }


}
