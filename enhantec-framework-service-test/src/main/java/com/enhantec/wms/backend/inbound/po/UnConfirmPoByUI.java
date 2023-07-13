
package com.enhantec.wms.backend.inbound.po;

import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;

import java.sql.Connection;
import java.util.HashMap;

public class UnConfirmPoByUI extends LegacyBaseService {


    /**
     * --注册方法
     delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHUnConfirmPoByUI'
     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHUnConfirmPoByUI', 'com.enhantec.sce.inbound.po', 'enhantec', 'UnConfirmPoByUI', 'TRUE', 'JOHN', 'JOHN'
     , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,POKEY,ESIGNATUREKEY','0.10','0');
     */


    private static final long serialVersionUID = 1L;

    public UnConfirmPoByUI() {
    }



    public void execute(ServiceDataHolder serviceDataHolder) {

        String userid = context.getUserID();
        Connection conn = null;

        try {

            conn = context.getConnection();

            String poKey = serviceDataHolder.getInputDataAsMap().getString("POKEY");
            String esignatureKey = serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");
            String SQL="SELECT * FROM WMS_PO WHERE  POKEY = ?  ";
            HashMap<String, String>  record = DBHelper.getRecord(context, conn, SQL, new Object[]{ poKey},"变更单");
            if( record == null ) ExceptionHelper.throwRfFulfillLogicException("调拨单为"+poKey+"未找到");
            
            String user = DBHelper.getValue(context, conn, "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                    esignatureKey
            }, String.class, "复核人");
            DBHelper.executeUpdate(context, conn, "UPDATE WMS_PO SET CONFIRMSTATUS = ?,ISCONFIRMEDUSER1 ='',ISCONFIRMEDUSER2 = '' WHERE POKEY = ? ",
                    new Object[]{"0",poKey});
            Udtrn UDTRN = new Udtrn();

                UDTRN.EsignatureKey = esignatureKey;

            UDTRN.FROMTYPE =  "取消确认调拨单";
            UDTRN.FROMTABLENAME = "WMS_PO";
            UDTRN.FROMKEY = poKey;
            UDTRN.FROMKEY1 = "";
            UDTRN.FROMKEY2 = "";
            UDTRN.FROMKEY3 = "";
            UDTRN.TITLE01 = "调拨单号";
            UDTRN.CONTENT01 = poKey;
            UDTRN.TITLE02 = "操作人";
            UDTRN.CONTENT02 = user;
            UDTRN.Insert(context, conn, userid);

          

        }catch (Exception e){
            try	{	context.releaseConnection(conn); }	catch (Exception e1) {		}
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());

        }finally {
            try	{	context.releaseConnection(conn); }	catch (Exception e1) {		}
        }
    }
}