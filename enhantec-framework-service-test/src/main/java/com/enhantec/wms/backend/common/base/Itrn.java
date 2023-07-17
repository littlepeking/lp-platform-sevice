package com.enhantec.wms.backend.common.base;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.*;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.HashMap;
public class Itrn {

    public static  HashMap<String,String> findByPickDetailKey( String pickDetailKey, boolean checkExist) throws FulfillLogicException {

        if(UtilHelper.isEmpty(pickDetailKey)) ExceptionHelper.throwRfFulfillLogicException("拣货明细ID不能为空");

        HashMap<String,String>  itrnRecord = DBHelper.getRecord("select * from ITRN where TRANTYPE ='MV' and SOURCETYPE ='PICKING' and SOURCEKEY = ?",
                new Object[]{pickDetailKey},"交易记录");
        if(checkExist && itrnRecord == null) ExceptionHelper.throwRfFulfillLogicException("交易记录不存在");

        return itrnRecord;
    }


}
