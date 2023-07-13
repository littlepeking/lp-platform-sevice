package com.enhantec.wms.backend.common.base;

import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.util.HashMap;

public class SKU {

    public static  HashMap<String,String> findById(Context context, Connection conn, String sku, boolean checkExist) throws FulfillLogicException {

        if(UtilHelper.isEmpty(sku)) ExceptionHelper.throwRfFulfillLogicException("物料代码不能为空");

        HashMap<String,String>  record = DBHelper.getRecord(context,conn,"select * from sku where sku = ?", new Object[]{sku},"物料代码");
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("物料代码"+sku+"不存在");

        return record;
    }



    //是否唯一码管理
    public static boolean isSerialControl(Context context, Connection conn, String sku) {

        HashMap<String,String> skuHashMap = SKU.findById(context, conn, sku,true);

        return "1".equals(skuHashMap.get("SNUM_ENDTOEND"));

    }

    public static boolean isBindAndNOTAutoGenerateLpn(Context context, Connection conn, String sku) throws Exception {

        if(!SKU.isSerialControl(context, conn,sku)){
            //批次管理物料自动生成容器条码
            return false;
        }else {
            // 1.手动生成箱号
            // 2.自动生成箱号
            // 3.不生成箱号（每个SN都使用独立的流水码箱号）
            if("1".equals(CDSysSet.getSNGenerateLpnType(context, conn))){
                return true;
            }else {
                return false;
            }
        }

    }

    public static boolean isBindAndAutoGenerateLpn(Context context, Connection conn, String sku) throws Exception {

        if(!SKU.isSerialControl(context, conn,sku)){
            //批次管理物料自动生成容器条码
            return false;
        }else {
            // 1.手动生成箱号
            // 2.自动生成箱号
            // 3.不生成箱号（每个SN都使用独立的流水码箱号）
            if("2".equals(CDSysSet.getSNGenerateLpnType(context, conn))){
                return true;
            }else {
                return false;
            }
        }

    }

    public static boolean isBindingLpn(Context context, Connection conn, String sku){

        if(!SKU.isSerialControl(context, conn,sku)){
            //批次管理物料自动生成容器条码
            return false;
        }else {
            // 1.手动生成箱号
            // 2.自动生成箱号
            // 3.不生成箱号（每个SN都使用独立的流水码箱号）
            if("1".equals(CDSysSet.getSNGenerateLpnType(context, conn)) || "2".equals(CDSysSet.getSNGenerateLpnType(context, conn))){
                return true;
            }else {
                return false;
            }
        }

    }


}
