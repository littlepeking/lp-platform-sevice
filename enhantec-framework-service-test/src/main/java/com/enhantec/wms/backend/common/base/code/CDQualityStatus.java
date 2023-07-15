package com.enhantec.wms.backend.common.base.code;

import com.enhantec.wms.backend.common.Const;
import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.SKU;

import java.sql.Connection;

public class CDQualityStatus {

    public static String findByOrderType(Context context, String type, String sku){
        return CodeLookup.getCodeLookupByKey(context,"ORDERTYPE",type).get("UDF5");
    }

    /**
     * 1.如果为退货入库类型并且配置了质量状态为SAMEASBEFORE，则使用原质量状态
     * 2.如果配置了具体的质量状态，则直接使用
     * 3.如果没有配置质量状态则看SKU的待检标志
     * @param context

     * @param type
     * @param sku
     * @return
     */
    public static String findByReceiptType(Context context,String type,String sku,String preStatus){

            String defaultReceiptQualityStatus = CodeLookup.getCodeLookupByKey(context,"RECEIPTYPE",type).get("UDF7");

            if (UtilHelper.isEmpty(defaultReceiptQualityStatus)){

                String skuIsQualityCheck = SKU.findById(context,sku,true).get("BUSR3");

                return "1".equals(skuIsQualityCheck) ? Const.QUALITYSTATUS_QUARANTINE :  Const.QUALITYSTATUS_RELEASE;

            }else{

                if(CDReceiptType.isReturnType(context,type) && defaultReceiptQualityStatus.equals("SAMEASBEFORE")) {

                    if(UtilHelper.isEmpty(preStatus)) ExceptionHelper.throwRfFulfillLogicException("根据配置，退货待收货行应使用原质量状态，但当前收货行中提供的质量状态为空");

                    return preStatus;

                }else {

                    return defaultReceiptQualityStatus;

                }
            }

    }
}
