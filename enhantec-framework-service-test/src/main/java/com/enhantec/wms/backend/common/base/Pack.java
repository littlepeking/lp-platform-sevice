package com.enhantec.wms.backend.common.base;

import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.util.HashMap;

public class Pack {

    public static  HashMap<String,String> findById(Context context, String packKey, boolean checkExist) throws FulfillLogicException {

        if(UtilHelper.isEmpty(packKey)) ExceptionHelper.throwRfFulfillLogicException("包装代码不能为空");

        HashMap<String,String>  packRecord = DBHelper.getRecord(context,"select * from pack where packKey = ?", new Object[]{packKey},"包装");
        if(checkExist && packRecord == null) ExceptionHelper.throwRfFulfillLogicException("包装"+packKey+"不存在");

        return packRecord;
    }

}
