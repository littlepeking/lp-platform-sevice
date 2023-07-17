package com.enhantec.wms.backend.common.base;

import com.enhantec.wms.backend.common.Const;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.*;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.HashMap;
import java.util.List;

public class IDNotesHistory {


    public static HashMap<String, String> findLastShippedRecordById( String lpn, boolean checkExist) {

        HashMap<String,String> lpnHisInfo= DBHelper.getRecord(
                " SELECT TOP 1 " + Const.commonSkuFieldsWithAlias+
                        ",ID.SERIALKEY, ID.ID, ID.BARRELNUMBER, ID.TOTALBARREL, ID.BARRELDESCR BARRELDESCR, ID.PACKKEY, ID.UOM," +
                        "ID.ORIGINALGROSSWGT, ID.ORIGINALTAREWGT, ID.ORIGINALNETWGT,ID.REGROSSWGT,ID.PROJECTCODE,ID.ORDERKEY, " +
                        "ID.ISOPENED,ID.RETURNTIMES,ID.LASTSHIPPEDLOC,ID.PRODLOTEXPECTED," + Const.CommonLottableFields +
                        " FROM IDNOTESHISTORY ID, SKU S, V_LOTATTRIBUTE ELOT" +
                        " WHERE ID.SKU = S.SKU  AND  ID.LOT = ELOT.LOT AND ID =? ORDER BY ID.SERIALKEY DESC "
                , new Object[]{lpn},"容器条码/箱号的历史库存",checkExist);

        return lpnHisInfo;
    }


    public static List<HashMap<String,String>> findLastShippedSNsById( String lpn, boolean checkExist) throws FulfillLogicException {

        if(UtilHelper.isEmpty(lpn)) ExceptionHelper.throwRfFulfillLogicException("箱号不能为空");
        HashMap<String,String>  idLastHisRecord = IDNotesHistory.findLastShippedRecordById(lpn, true);
        List<HashMap<String,String>>  snList = DBHelper.executeQuery("SELECT * FROM SNHISTORY where IDSERIALKEY = ? ", new Object[]{idLastHisRecord.get("SERIALKEY")});
        if(checkExist && snList.size() == 0) ExceptionHelper.throwRfFulfillLogicException("未找到箱号"+lpn+"下的唯一码历史库存");

        return snList;
    }



}
