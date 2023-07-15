
package com.enhantec.wms.backend.inbound.receiving;

import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;

import java.sql.Connection;
import java.util.HashMap;

public class UnconfirmASN extends LegacyBaseService {


    /**
     * --注册方法
     delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHUnconfirmASN'
     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHUnconfirmASN', 'com.enhantec.sce.inbound.receiving', 'enhantec', 'UnconfirmASN', 'TRUE', 'JOHN', 'JOHN'
     , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,RECEIPTKEY,ESIGNATUREKEY','0.10','0');
     */


    private static final long serialVersionUID = 1L;

    public UnconfirmASN() {
    }

    public void execute(ServiceDataHolder serviceDataHolder) {
        String userid = context.getUserID();


        try {



            String receiptKey = serviceDataHolder.getInputDataAsMap().getString("RECEIPTKEY");
            String esignatureKey = serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

            HashMap<String, String>  receiptInfo =  Receipt.findByReceiptKey(context,receiptKey,true);

            if(!receiptInfo.get("STATUS").equals("0")) ExceptionHelper.throwRfFulfillLogicException("非新建状态的ASN不允许取消确认");

            String isFromInterface = CodeLookup.getCodeLookupValue(context,"RECEIPTYPE",receiptInfo.get("TYPE"),"UDF4","收货类型");

            if("Y".equalsIgnoreCase(isFromInterface)) ExceptionHelper.throwRfFulfillLogicException("接口发送的收货指令不允许取消确认");

            DBHelper.executeUpdate(context, "UPDATE RECEIPT SET ISCONFIRMEDUSER ='', ISCONFIRMEDUSER2 = '', ISCONFIRMED = 0  WHERE RECEIPTKEY = ? ",
                    new Object[]{ receiptKey });


            Udtrn UDTRN=new Udtrn();
            UDTRN.EsignatureKey=esignatureKey;
            UDTRN.FROMTYPE="取消确认ASN";
            UDTRN.FROMTABLENAME="RECEIPT";
            UDTRN.FROMKEY=receiptKey;
            UDTRN.FROMKEY1="";
            UDTRN.FROMKEY2="";
            UDTRN.FROMKEY3="";
            UDTRN.TITLE01="ASN单号";    UDTRN.CONTENT01=receiptKey;
            UDTRN.TITLE02="确认状态";    UDTRN.CONTENT02="N";
            UDTRN.Insert(context, userid);




          

        }catch (Exception e){
            
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());

        }finally {
            
        }
    }
}