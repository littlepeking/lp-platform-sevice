package com.enhantec.wms.backend.utils.common;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.framework.Context;

import java.sql.Connection;

public class ChangeOpenSnMarksHelper {

 public static void changeOpenSnMarksBYLpn(Context context, String sku , String... ids) throws Exception {
     String openSnMarks = "0";
     if(SKU.isSerialControl(context,sku)){
         for (String id:ids) {
             String getExistOpenSnLpn = "select COUNT(*) from idnotes i ,SERIALINVENTORY s ,SKU s2 " +
                     "where i.id =s.ID and s.NETWEIGHT < s2.SNAVGWGT and i.sku = s2.SKU and i.id=?";
             String existOpenSnLpn = DBHelper.getValue(context, getExistOpenSnLpn,new String[]{id},"");
             if (!UtilHelper.isEmpty(existOpenSnLpn)&&Integer.parseInt(existOpenSnLpn)>0){
                 openSnMarks="1";
             }
             DBHelper.executeUpdate(context,
                     "UPDATE  idnotes set  existopensn = ? where id = ?"
                     , new Object[]{
                             openSnMarks,
                             id
                     });
         }
     }
 }









}
