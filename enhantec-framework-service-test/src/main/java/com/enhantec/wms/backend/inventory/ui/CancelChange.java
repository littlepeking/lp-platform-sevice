
package com.enhantec.wms.backend.inventory.ui;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;

import java.util.Map;

public class CancelChange extends WMSBaseService {


    /**
     * --??
     delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHCancelChange'
     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHCancelChange', 'com.enhantec.sce.inventory.ui', 'enhantec', 'CancelChange', 'TRUE', 'JOHN', 'JOHN'
     , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,Changekey,ESIGNATUREKEY','0.10','0');
     */

//此类借用更改项目号功能来实现生基更改物料代码功能
    private static final long serialVersionUID = 1L;

    public CancelChange() {
    }

    public void execute(ServiceDataHolder serviceDataHolder) {
        String userid = EHContextHelper.getUser().getUsername();


        try {



            String changekey = serviceDataHolder.getInputDataAsMap().getString("CHANGEKEY");
            String esignatureKey = serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");
            String SQL="SELECT * FROM ENCHGPROJECTCODE WHERE  CHANGEKEY = ?  ";
            Map<String, String> record = DBHelper.getRecord( SQL, new Object[]{ changekey},"变更单");
            if( record == null ) ExceptionHelper.throwRfFulfillLogicException("变更单为"+changekey+"未找到");
            if ("5".equalsIgnoreCase(record.get("STATUS"))) throw new Exception("已经执行不可取消");
            DBHelper.executeUpdate( "UPDATE ENCHGPROJECTCODE SET STATUS = 6 WHERE CHANGEKEY = ?", new String[]{changekey});
            DBHelper.executeUpdate( "UPDATE ENCHGPROJECTCODEDETAIL SET STATUS = 6 WHERE CHANGEKEY = ?", new String[]{changekey});
          

        }catch (Exception e){
            
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());

        }finally {
            
        }
    }

}