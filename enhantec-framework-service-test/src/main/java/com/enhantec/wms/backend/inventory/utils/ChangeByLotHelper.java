package com.enhantec.wms.backend.inventory.utils;

import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;

import java.sql.Connection;
import java.util.HashMap;

public class ChangeByLotHelper {
    public static void checkSkuAttributeIsMatch(String fromSku, String toSku, Context context){
        HashMap<String,String> fromSkuHashMap = SKU.findById(context,fromSku,true);
        HashMap<String,String> toSkuHashMap = SKU.findById(context,toSku,true);
        if (!fromSkuHashMap.get("PACKKEY").equalsIgnoreCase(toSkuHashMap.get("PACKKEY")))
            ExceptionHelper.throwRfFulfillLogicException("物料代码"+fromSku+"与物料代码"+toSku+"包装不一致无法转换");
        if (!fromSkuHashMap.get("SNUM_ENDTOEND").equalsIgnoreCase(toSkuHashMap.get("SNUM_ENDTOEND")))
            ExceptionHelper.throwRfFulfillLogicException("物料代码"+fromSku+"与物料代码"+toSku+"是否唯一码管理不一致无法转换");
        if (!fromSkuHashMap.get("EXT_UDF_STR3").equalsIgnoreCase(toSkuHashMap.get("EXT_UDF_STR3")))
            ExceptionHelper.throwRfFulfillLogicException("物料代码"+fromSku+"与物料代码"+toSku+"JDE编码不一致无法转换");
    }
}
