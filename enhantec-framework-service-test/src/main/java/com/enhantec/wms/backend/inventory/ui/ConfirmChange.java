
package com.enhantec.wms.backend.inventory.ui;

import com.enhantec.wms.backend.framework.LegacyBaseService;import com.enhantec.framework.common.utils.EHContextHelper;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.inventory.utils.ChangeByLotHelper;
import com.enhantec.wms.backend.utils.common.*;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.Map;
import java.util.List;

public class ConfirmChange extends LegacyBaseService {


    /**
     * --注册方法
     delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHConfirmChange'
     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHConfirmChange', 'com.enhantec.sce.inventory.ui', 'enhantec', 'ConfirmChange', 'TRUE', 'JOHN', 'JOHN'
     , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,Changekey,ESIGNATUREKEY','0.10','0');
     */

//此类借用更改项目号功能来实现生基更改物料代码功能
    private static final long serialVersionUID = 1L;

    public ConfirmChange() {
    }

    public void execute(ServiceDataHolder serviceDataHolder) {
        String userid = EHContextHelper.getUser().getUsername();


        try {



            String changekey = serviceDataHolder.getInputDataAsMap().getString("CHANGEKEY");
            String esignatureKey = serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");
            String SQL="SELECT * FROM ENCHGPROJECTCODE WHERE  CHANGEKEY = ?  ";
            Map<String, String>  record = DBHelper.getRecord( SQL, new Object[]{ changekey},"变更单");
            if( record == null ) ExceptionHelper.throwRfFulfillLogicException("变更单为"+changekey+"未找到");

            List<Map<String,String>> ehchangeDetailList = DBHelper.executeQuery( "select  e.FROMSKU,e.TOSKU from  ENCHGPROJECTCODEDETAIL e " +
                    "      where  e.CHANGEKEY = ?", new Object[]{
                    changekey});
            for (Map<String,String> ehchangedetailHash:ehchangeDetailList) {
                ChangeByLotHelper.checkSkuAttributeIsMatch(ehchangedetailHash.get("FROMSKU"),ehchangedetailHash.get("TOSKU"));
            }

            String isConfirmedUser = DBHelper.getValue( "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                    esignatureKey
            }, String.class, "复核人");
            String status = record.get("STATUS");
            switch (status){
                case "1":
                    updateConfirmUserByChangeKey(changekey,isConfirmedUser,"ISCONFIRMEDUSER1","2");
                    break;
                case "2":
                    updateConfirmUserByChangeKey(changekey,isConfirmedUser,"ISCONFIRMEDUSER2","3");
                    break;

            }


        }catch (Exception e){
            
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());

        }finally {
            
        }
    }
    private static void checkConfirmUser(String changeKey, String confirmUser){
        String SQL = "select * from ENCHGPROJECTCODE where ISCONFIRMEDUSER1=?  " +
                "and changekey=?";
        Map<String, String>  record = DBHelper.getRecord(SQL, new Object[]{ confirmUser,changeKey},"变更单");
        if( record != null ) ExceptionHelper.throwRfFulfillLogicException("变更单为"+changeKey+"复核人，确认人不能为同一人");
    }
    private static void updateConfirmUserByChangeKey(String changeKey, String confirmUser, String field,String status){

        checkConfirmUser(changeKey,confirmUser);
        DBHelper.executeUpdate( "UPDATE ENCHGPROJECTCODE SET "+field+" = ? , status = ? WHERE changeKey = ? ",
                new Object[]{confirmUser,status,changeKey});
        DBHelper.executeUpdate( "UPDATE ENCHGPROJECTCODEDETAIL SET status = ? WHERE changeKey = ? ",
                new Object[]{status,changeKey});

    }
}