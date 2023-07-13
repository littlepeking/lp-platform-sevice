package com.enhantec.wms.backend.common.outbound;

import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

public class PickDetail {

    public static HashMap<String, String> findByPickDetailKey(Context context, Connection conn, String id, boolean checkExist) {

        String SQL="select * from pickdetail where pickdetailkey = ?";

        HashMap<String,String> record= DBHelper.getRecord(context, conn, SQL, new Object[]{ id},"拣货明细");
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("未找到拣货明细"+id);
        return record;
    }

    public static HashMap<String, String> findPickedLpn(Context context, Connection conn, String lpn, boolean checkExist) {

        String SQL="select * from pickdetail where status = 5 and id = ?";

        HashMap<String,String> record= DBHelper.getRecord(context, conn, SQL, new Object[]{ lpn},"拣货明细");
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("未找到容器条码为"+lpn+"的拣货明细");
        return record;
    }

    public static List<HashMap<String, String>> findByOrderKey(Context context, Connection conn, String orderkey, boolean checkExist) {

        String SQL="select * from pickdetail where orderkey = ? ";

        List<HashMap<String,String>> records= DBHelper.executeQuery(context, conn, SQL, new Object[]{ orderkey});
        if(checkExist && records.size()==0) ExceptionHelper.throwRfFulfillLogicException("未找到订单号为"+orderkey+"的拣货明细");

        return records;
    }


    public static List<HashMap<String,String>> findByOrderKeyAndOrderLineNumber(Context context,Connection connection,String orderKey,String orderLineNumber,boolean checkExist){
        String sql = "SELECT * FROM PICKDETAIL WHERE ORDERKEY = ? AND ORDERLINENUMBER = ?";
        List<HashMap<String, String>> records = DBHelper.executeQuery(context, connection, sql, new Object[]{orderKey, orderLineNumber});
        if(checkExist && records.size() == 0){
            ExceptionHelper.throwRfFulfillLogicException("未找到订单号为"+orderKey+",订单行号为"+orderLineNumber+"的拣货明细");
        }
        return records;
    }

}
