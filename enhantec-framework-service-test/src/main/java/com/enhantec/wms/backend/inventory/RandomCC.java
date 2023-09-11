package com.enhantec.wms.backend.inventory;


import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.IdGenerationHelper;

import java.math.BigDecimal;

/**
 --注册方法
 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME = 'StochasticInv';
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHRandomCC', 'com.enhantec.sce.inventory', 'enhantec', 'RandomCC','TRUE','ALLAN','ALLAN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,LPN,LOC,BARRELDESCR,SKU,LOT,NETWGT,NOTES,CCKEY,WHSEID,STORERKEY,CCQTY,EXISTIN,PACKKEY,UOM','0.10','0');
 **/


public class RandomCC extends WMSBaseService {

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {

        String storerKey= DBHelper.getStringValue("SELECT UDF1 FROM CODELKUP WHERE LISTNAME=? AND CODE=?",new Object[]{
                "SYSSET","STORERKEY"},"");

        try {

            String rfccKey = serviceDataHolder.getInputDataAsMap().getString("CCKEY");
            if ("".equals(rfccKey)) {
                try {
                    String cckey = IdGenerationHelper.generateID( "", "M", 9);
                    DBHelper.executeUpdate( "insert into CC(whseid,cckey,storerkey,SKU,LOC,ADDWHO,EDITWHO,status)\n" +
                            "           values(?,?,?,?,?,?,?,'9')", new Object[]{serviceDataHolder.getInputDataAsMap().getString("whseid"),
                            cckey,
                            storerKey,
                            "N/A",
                            serviceDataHolder.getInputDataAsMap().getString("LOC"),
                            serviceDataHolder.getInputDataAsMap().getString("userid"),
                            serviceDataHolder.getInputDataAsMap().getString("userid")});
                    serviceDataHolder.getInputDataAsMap().setAttribValue("cckey", cckey);
                } catch (Exception e) {
                    ExceptionHelper.throwRfFulfillLogicException(e.getMessage());
                }
            } else if ("1".equals(serviceDataHolder.getInputDataAsMap().getString("EXISTIN"))) {
                try {
                    String ccdetilkey = IdGenerationHelper.generateID( "", "MD", 8);
                    String packkey = serviceDataHolder.getInputDataAsMap().getString("PACKKEY");
                    String uom = serviceDataHolder.getInputDataAsMap().getString("UOM");
                    BigDecimal CCQTY = UOM.UOMQty2StdQty( packkey, uom, new BigDecimal(serviceDataHolder.getInputDataAsMap().getString("CCQTY")));
                    BigDecimal Qty = CCQTY.subtract(new BigDecimal((serviceDataHolder.getInputDataAsMap().getString("NETWGT"))));
                    //LPN,LOC,BARRELDESCR,SKU,LOT,NETWGT,NOTES,CCKEY,WHSEID,STORERKEY,CCQTY,EXISTIN,PACKKEY,,UOM
                    DBHelper.executeUpdate( "insert into CCDETAIL " +
                            "(WHSEID,CCKEY,CCDETAILKEY,STORERKEY,SKU,LOT,LOC,ID,QTY,SYSQTY,ADJQTY,ADDWHO,EDITWHO,status)\n" +
                            "          values(?,?,?,?,?,?,?,?,?,?,?,?,?,'9')", new Object[]{serviceDataHolder.getInputDataAsMap().getString("WHSEID"),
                            serviceDataHolder.getInputDataAsMap().getString("CCKEY"),
                            ccdetilkey,
                            storerKey,
                    serviceDataHolder.getInputDataAsMap().getString("SKU"),
                    serviceDataHolder.getInputDataAsMap().getString("LOT"),
                    serviceDataHolder.getInputDataAsMap().getString("LOC"),
                    serviceDataHolder.getInputDataAsMap().getString("LPN"),
                    serviceDataHolder.getInputDataAsMap().getString("CCQTY"),
                    serviceDataHolder.getInputDataAsMap().getString("NETWGT"),
                            Qty.toString(),
                    serviceDataHolder.getInputDataAsMap().getString("userid"),
                    serviceDataHolder.getInputDataAsMap().getString("userid")});
                } catch (Exception e) {
                    ExceptionHelper.throwRfFulfillLogicException(e.getMessage());
                }
            } else if ("0".equals(serviceDataHolder.getInputDataAsMap().getString("EXISTIN"))) {
                try {
                    String ccdetilkey = IdGenerationHelper.generateID( "", "MD", 8);
                    BigDecimal Qty = new BigDecimal(serviceDataHolder.getInputDataAsMap().getString("CCQTY")).subtract(new BigDecimal(serviceDataHolder.getInputDataAsMap().getString("NETWGT")));
                    //LPN,LOC,BARRELDESCR,SKU,LOT,NETWGT,NOTES,CCKEY,WHSEID,STORERKEY,CCQTY,EXISTIN
                    DBHelper.executeUpdate( "insert into CCDETAIL " +
                            "(WHSEID,CCKEY,CCDETAILKEY,ID,QTY,SYSQTY,ADJQTY,ADDWHO,EDITWHO,status,NOTES)\n" +
                            "     values(?,?,?,?,?,?,?,?,?,'9',?)", new Object[]{
                            serviceDataHolder.getInputDataAsMap().getString("WHSEID"),
                            serviceDataHolder.getInputDataAsMap().getString("CCKEY"),
                            ccdetilkey,
                    serviceDataHolder.getInputDataAsMap().getString("LPN"),
                    serviceDataHolder.getInputDataAsMap().getString("CCQTY"),
                    serviceDataHolder.getInputDataAsMap().getString("NETWGT"),
                            Qty.toString(),
                    serviceDataHolder.getInputDataAsMap().getString("userid"),
                    serviceDataHolder.getInputDataAsMap().getString("userid"),
                    serviceDataHolder.getInputDataAsMap().getString("NOTES")});

                } catch (Exception e) {
                    ExceptionHelper.throwRfFulfillLogicException(e.getMessage());
                }
            }
        }finally {
            
        }

    }

}