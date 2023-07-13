package com.enhantec.wms.backend.common.base;

import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.util.HashMap;
public class Itrn {

    public static  HashMap<String,String> findByPickDetailKey(Context context, Connection conn, String pickDetailKey, boolean checkExist) throws FulfillLogicException {

        if(UtilHelper.isEmpty(pickDetailKey)) ExceptionHelper.throwRfFulfillLogicException("拣货明细ID不能为空");

        HashMap<String,String>  itrnRecord = DBHelper.getRecord(context,conn,"select * from ITRN where TRANTYPE ='MV' and SOURCETYPE ='PICKING' and SOURCEKEY = ?",
                new Object[]{pickDetailKey},"交易记录");
        if(checkExist && itrnRecord == null) ExceptionHelper.throwRfFulfillLogicException("交易记录不存在");

        return itrnRecord;
    }


}
