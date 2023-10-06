package com.enhantec.wms.backend.common.base;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.DBResourceException;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;

import java.math.BigDecimal;
import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.Map;
import java.util.Map;
import java.util.Optional;

public class UOM {

    public static BigDecimal UOMQty2StdQty( String packKey, String uom, BigDecimal uomQty) {

        //设置scale=5，否则重量为0时会变成0E-10的数值
        return  getConversionRate2Std( packKey,uom).multiply(uomQty).setScale(5);

    }

    public static BigDecimal Std2UOMQty( String packKey, String uom, BigDecimal stdQty) {

        return  stdQty.divide(getConversionRate2Std( packKey,uom));

    }


    public static BigDecimal getConversionRate2Std( String packKey, String uom) {

        Map<String, String> uomHashMap = getUOMHashMap( packKey);

        if(uomHashMap.size()==0||uomHashMap==null) ExceptionHelper.throwRfFulfillLogicException("未发现包装"+packKey);


        Optional<Map.Entry<String,String>> optionalEntry = uomHashMap.entrySet().stream().filter(e->uom.equals(e.getValue())).findFirst();

        if(!optionalEntry.isPresent()) ExceptionHelper.throwRfFulfillLogicException("未在包装中找到计量单位"+uom);

        Map.Entry<String,String> entry =optionalEntry.get();

        String uomNum = entry.getKey().substring(entry.getKey().length()-1);

        String conversionRate = uomHashMap.get("PACKQTY"+uomNum);

        return new BigDecimal(conversionRate);

    }

    public static Map<String, String> getUOMHashMap( String packKey) throws DBResourceException {
        Map<String,String> uomHashMap = DBHelper.getRecord(
                 "select p.QTY as PACKQTY3, p.INNERPACK as PACKQTY2,p.CASECNT as PACKQTY1,p.PALLET as PACKQTY4,p.[CUBE] as PACKQTY5,p.GROSSWGT as PACKQTY6,p.NETWGT as PACKQTY7,p.OTHERUNIT1 as PACKQTY8,p.OTHERUNIT2 as PACKQTY9, " +
                        "p.PACKUOM1, p.PACKUOM2, p.PACKUOM3, p.PACKUOM4, p.PACKUOM5, p.PACKUOM6, p.PACKUOM7, p.PACKUOM8, p.PACKUOM9 from pack p where p.PACKKEY = ?"
                , new Object[]{packKey},"计量单位");
        return uomHashMap;
    }

    public static String getStdUOM(String packKey) throws DBResourceException {
        Map<String,String> uomHashMap= getUOMHashMap( packKey);
        return uomHashMap.get("PACKUOM3");
    }

    public static String getUOMCode(String packKey, String uom) {

        Map<String,String>  packHashMap =Pack.findById(packKey,true);

        String uomCode = null;
        if(packHashMap.get("PACKUOM3").equalsIgnoreCase(uom)){
            uomCode = "6";
        }else if(packHashMap.get("PACKUOM1").equalsIgnoreCase(uom)){
            uomCode = "2";
        }else if(packHashMap.get("PACKUOM4").equalsIgnoreCase(uom)){
            uomCode = "1";
        }else if(packHashMap.get("PACKUOM2").equalsIgnoreCase(uom)){
            uomCode = "3";
        }
        if(uomCode == null) ExceptionHelper.throwRfFulfillLogicException("未找到UOM"+uom+"到UOM code的转换关系");
        return uomCode;
    }
}
