package com.enhantec.wms.backend.common.base;

import com.enhantec.wms.backend.common.Const;
import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.DBHelper;
import java.sql.Connection;
import java.util.HashMap;

public class SNHistory {

    public static HashMap<String, String> findBySkuAndSN(Context context, String sku, String serialNumber, boolean checkExist) {

        HashMap<String,String> lpnHisInfo= DBHelper.getRecord(context,
                " SELECT TOP 1 " + Const.commonSkuFieldsWithAlias+
                        ",ID.ID, ID.BARRELNUMBER, ID.TOTALBARREL, ID.BARRELDESCR BARRELDESCR, ID.PACKKEY, ID.UOM," +
                        "ID.ORIGINALGROSSWGT, ID.ORIGINALTAREWGT, ID.ORIGINALNETWGT,ID.REGROSSWGT,ID.PROJECTCODE, " +
                        "ID.ISOPENED,ID.RETURNTIMES,ID.LASTSHIPPEDLOC,ID.PRODLOTEXPECTED," + Const.CommonLottableFields +
                        ", SN.NETWEIGHT SNWEIGHT,ID.ORDERKEY "+
                        " FROM IDNOTESHISTORY ID,SNHISTORY SN, SKU S, V_LOTATTRIBUTE ELOT" +
                        " WHERE SN.IDSERIALKEY = ID.SERIALKEY AND ID.SKU = S.SKU AND ID.LOT = ELOT.LOT AND SN.SKU = ? AND SN.SERIALNUMBER = ? ORDER BY SN.SERIALKEY DESC "
                , new Object[]{sku,serialNumber},"唯一码的历史库存",checkExist);

        return lpnHisInfo;
    }

//
//    public static void deleteBySkuAndLpn(Context context, String sku, String lpn) throws FulfillLogicException {
//
//        if(UtilHelper.isEmpty(sku)) ExceptionHelper.throwRfFulfillLogicException("物料代码不能为空");
//        if(UtilHelper.isEmpty(lpn)) ExceptionHelper.throwRfFulfillLogicException("箱号不能为空");
//
//        DBHelper.executeUpdate(context,"DELETE FROM SerialInventoryHistory where sku = ? and id = ?", new Object[]{sku),lpn});
//
//    }

}
