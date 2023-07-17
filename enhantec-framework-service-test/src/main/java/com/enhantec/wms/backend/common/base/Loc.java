package com.enhantec.wms.backend.common.base;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.*;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.HashMap;
import java.util.List;

public class Loc {

    public static  HashMap<String,String> findById( String loc, boolean checkExist) throws FulfillLogicException {

        if(UtilHelper.isEmpty(loc)) ExceptionHelper.throwRfFulfillLogicException("库位不能为空");

        HashMap<String,String>  locRecord = DBHelper.getRecord("select * from loc where loc = ?", new Object[]{ loc},"库位");
        if(checkExist && locRecord == null) ExceptionHelper.throwRfFulfillLogicException("库位"+loc+"不存在");

        return locRecord;
    }

    public static List<HashMap<String,String>> findByZone( String putawayZone, boolean checkExist) throws FulfillLogicException {

        if(UtilHelper.isEmpty(putawayZone)) ExceptionHelper.throwRfFulfillLogicException("库区代码不能为空");

        List<HashMap<String,String>> locs = DBHelper.executeQuery("select * from loc where putawayzone = ?", new Object[]{putawayZone});
        if(checkExist && locs.size() == 0) ExceptionHelper.throwRfFulfillLogicException("在区"+putawayZone+"中未找到库位");

        return locs;
    }


}
