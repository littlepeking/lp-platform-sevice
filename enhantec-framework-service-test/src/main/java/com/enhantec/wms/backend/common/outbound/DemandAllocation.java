package com.enhantec.wms.backend.common.outbound;

import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

public class DemandAllocation {

    public static List<HashMap<String, String>> findByOrderLineNumber(Context context, Connection conn, String orderkey, String orderLineNumber, boolean checkExist) {

        String sql = "SELECT * FROM DEMANDALLOCATION WHERE ORDERKEY = ? and  ORDERLINENUMBER = ? ORDER BY DEMANDKEY ASC ";

        List<HashMap<String,String>> records= DBHelper.executeQuery(context, conn, sql, new Object[]{ orderkey,  orderLineNumber});

        if(checkExist && records.size()==0) ExceptionHelper.throwRfFulfillLogicException("未找到订单行号为"+orderkey+orderLineNumber+"的需求分配明细");

        return records;
    }

    public static HashMap<String, String> findByKey(Context context, Connection conn, String demandKey, boolean checkExist) {

        String sql = "SELECT * FROM DEMANDALLOCATION WHERE DEMANDKEY = ? ";

        return DBHelper.getRecord(context, conn, sql, new Object[]{ demandKey},"需求分配明细",checkExist);

    }

}
