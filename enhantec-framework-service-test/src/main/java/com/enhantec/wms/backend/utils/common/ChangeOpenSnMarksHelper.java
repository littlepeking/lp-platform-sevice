package com.enhantec.wms.backend.utils.common;
import com.enhantec.wms.backend.common.base.SKU;

public class ChangeOpenSnMarksHelper {

 public static void changeOpenSnMarksBYLpn( String sku , String... ids) {
     String openSnMarks = "0";
     if(SKU.isSerialControl(sku)){
         for (String id:ids) {
             String getExistOpenSnLpn = "select COUNT(*) from idnotes i ,SERIALINVENTORY s ,SKU s2 " +
                     "where i.id =s.ID and s.NETWEIGHT < s2.SNAVGWGT and i.sku = s2.SKU and i.id=?";
             String existOpenSnLpn = DBHelper.getStringValue( getExistOpenSnLpn,new String[]{id},"");
             if (!UtilHelper.isEmpty(existOpenSnLpn)&&Integer.parseInt(existOpenSnLpn)>0){
                 openSnMarks="1";
             }
             DBHelper.executeUpdate(
                     "UPDATE  idnotes set  existopensn = ? where id = ?"
                     , new Object[]{
                             openSnMarks,
                             id
                     });
         }
     }
 }









}
