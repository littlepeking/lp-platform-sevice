
package com.enhantec.wms.backend.inbound.po;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;

import java.util.Map;

public class ClosePoByUI extends WMSBaseService {


    /**
     * --注册方法
     delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHClosePoByUI'
     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHClosePoByUI', 'com.enhantec.sce.inbound.po', 'enhantec', 'ClosePoByUI', 'TRUE', 'JOHN', 'JOHN'
     , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,POKEY,ESIGNATUREKEY','0.10','0');
     */


    private static final long serialVersionUID = 1L;

    public ClosePoByUI() {
    }



    public void execute(ServiceDataHolder serviceDataHolder) {

        String userid = EHContextHelper.getUser().getUsername();


        try {



            String poKey = serviceDataHolder.getInputDataAsMap().getString("POKEY");
            String esignatureKey = serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");
            String SQL="SELECT * FROM WMS_PO WHERE  POKEY = ?  ";
            Map<String, String>  record = DBHelper.getRecord( SQL, new Object[]{ poKey},"变更单");
            if( record == null ) ExceptionHelper.throwRfFulfillLogicException("调拨单为"+poKey+"未找到");
            
            String user = DBHelper.getValue( "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                    esignatureKey
            }, String.class, "关闭人");
            DBHelper.executeUpdate( "UPDATE WMS_PO SET STATUS = ? WHERE POKEY = ? ",
                    new Object[]{"11",poKey});
            DBHelper.executeUpdate( "UPDATE WMS_PO_DETAIL SET STATUS = ? WHERE POKEY = ? ",
                    new Object[]{"11",poKey});
            Udtrn UDTRN = new Udtrn();

            UDTRN.EsignatureKey = esignatureKey;


            UDTRN.FROMTYPE =  "关闭调拨单";
            UDTRN.FROMTABLENAME = "WMS_PO";
            UDTRN.FROMKEY = poKey;
            UDTRN.FROMKEY1 = "";
            UDTRN.FROMKEY2 = "";
            UDTRN.FROMKEY3 = "";
            UDTRN.TITLE01 = "调拨单号";
            UDTRN.CONTENT01 = poKey;
            UDTRN.TITLE02 = "操作人";
            UDTRN.CONTENT02 = user;
            UDTRN.Insert( userid);

          

        }catch (Exception e){
            
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());

        }finally {
            
        }
    }
}