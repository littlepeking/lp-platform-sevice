package com.enhantec.wms.backend.common.base;

import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

public class SerialInventory {

    public static  HashMap<String,String> findBySkuAndSN(Context context, String sku, String sn, boolean checkExist) throws FulfillLogicException {

        if(UtilHelper.isEmpty(sku)) ExceptionHelper.throwRfFulfillLogicException("���ϴ��벻��Ϊ��");
        if(UtilHelper.isEmpty(sn)) ExceptionHelper.throwRfFulfillLogicException("Ψһ�벻��Ϊ��");


        HashMap<String,String>  locRecord = DBHelper.getRecord(context,"select * from SerialInventory where sku = ? and SerialNumber = ?", new Object[]{sku,sn},"Ψһ����");
        if(checkExist && locRecord == null) ExceptionHelper.throwRfFulfillLogicException("����:"+sku+" Ψһ��:"+sn+" �ڿ���в�����");

        return locRecord;
    }

    public static List<HashMap<String,String>> findByLpn(Context context, String lpn, boolean checkExist) throws FulfillLogicException {

        if(UtilHelper.isEmpty(lpn)) ExceptionHelper.throwRfFulfillLogicException("��Ų���Ϊ��");

        List<HashMap<String,String>> snList = DBHelper.executeQuery(context,"select * from SerialInventory where id = ?", new Object[]{lpn});
        if(checkExist && snList.size() == 0) ExceptionHelper.throwRfFulfillLogicException("���:"+lpn+" �ڿ���в�����");

        return snList;
    }


}
