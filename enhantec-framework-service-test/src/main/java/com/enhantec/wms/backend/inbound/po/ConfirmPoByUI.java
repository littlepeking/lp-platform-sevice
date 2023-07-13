
package com.enhantec.wms.backend.inbound.po;
import com.enhantec.wms.backend.framework.LegacyBaseService;import com.enhantec.wms.backend.framework.Context;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.util.HashMap;

public class ConfirmPoByUI extends LegacyBaseService {
    /**
     * --注册方法
     delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='ConfirmPoByUI'
     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHConfirmPoByUI', 'com.enhantec.sce.inbound.po', 'enhantec', 'ConfirmPoByUI', 'TRUE', 'JOHN', 'JOHN'
     , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,POKEY,ESIGNATUREKEY','0.10','0');
     */
    private static final long serialVersionUID = 1L;
    public ConfirmPoByUI() {
    }
    private static void checkConfirmUser(String poKey, String confirmUser, Context context, Connection conn){
        String SQL = "select * from WMS_PO where ISCONFIRMEDUSER1=?  " +
                "and POKEY=?";
        HashMap<String, String>  record = DBHelper.getRecord(context, conn, SQL, new Object[]{ confirmUser,poKey},"变更单");
        if( record != null ) ExceptionHelper.throwRfFulfillLogicException("调拨单为"+poKey+"复核人，确认人不能为同一人");
    }
    private static void updateConfirmUserByChangeKey(String poKey, String confirmUser, Context context, Connection conn,String field,String confirmStatus){

        checkConfirmUser(poKey,confirmUser,context,conn);
        DBHelper.executeUpdate(context, conn, "UPDATE WMS_PO SET "+field+" = ? , CONFIRMSTATUS = ? WHERE POKEY = ? ",
                new Object[]{confirmUser,confirmStatus,poKey});

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
            String isConfirmedUser = DBHelper.getValue(context, conn, "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                    esignatureKey
            }, String.class, "复核人");
            String status = record.get("CONFIRMSTATUS");
            switch (status){
                case "0":
                    updateConfirmUserByChangeKey(poKey,isConfirmedUser,context,conn,"ISCONFIRMEDUSER1","1");
                    break;
                case "1":
                    updateConfirmUserByChangeKey(poKey,isConfirmedUser,context,conn,"ISCONFIRMEDUSER2","2");
                    break;
                case "2":
                    ExceptionHelper.throwRfFulfillLogicException("调拨单已复核人，不可重复复核");

            }
            Udtrn UDTRN = new Udtrn();
            if (record.get("CONFIRMSTATUS").equals("0")) {
                UDTRN.EsignatureKey = esignatureKey;
            } else {//复核
                UDTRN.EsignatureKey1 = esignatureKey;
            }
            UDTRN.FROMTYPE = record.get("CONFIRMSTATUS").equals("0") ? "确认调拨单" : "复核调拨单";
            UDTRN.FROMTABLENAME = "WMS_PO";
            UDTRN.FROMKEY = poKey;
            UDTRN.FROMKEY1 = "";
            UDTRN.FROMKEY2 = "";
            UDTRN.FROMKEY3 = "";
            UDTRN.TITLE01 = "调拨单号";
            UDTRN.CONTENT01 = poKey;
            UDTRN.TITLE02 = "操作人";
            UDTRN.CONTENT02 = isConfirmedUser
            ;
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