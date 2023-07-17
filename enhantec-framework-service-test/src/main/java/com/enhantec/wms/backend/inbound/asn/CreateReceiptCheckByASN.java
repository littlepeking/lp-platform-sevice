package com.enhantec.wms.backend.inbound.asn;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;

import java.util.HashMap;


public class CreateReceiptCheckByASN extends com.enhantec.wms.backend.framework.LegacyBaseService
{



    /**
     *  生成ASN检查记录
     --注册方法

     DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE COMPOSITE='CreateReceiptCheckByASN';
     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('CreateReceiptCheckByASN', 'com.enhantec.sce.inbound.po', 'enhantec', 'CreateReceiptCheckByASN', 'TRUE',  'JOHN',  'ALLAN' , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server, TRANSCHECK,PACKCHECK,checkresult,RECEIPTKEY,ESIGNATUREKEY','0.10','0');

     */


    private static final long serialVersionUID = 1L;

    public void execute(com.enhantec.wms.backend.framework.ServiceDataHolder processData)
    {
        String userid = EHContextHelper.getUser().getUsername();

        



        try
        {
            String TRANSCHECK=processData.getInputDataAsMap().getString("TRANSCHECK");//运输条件
            String PACKCHECK=processData.getInputDataAsMap().getString("PACKCHECK");//包装状况
            String checkresult = processData.getInputDataAsMap().getString("checkresult");
            String esignaturekey = processData.getInputDataAsMap().getString("ESIGNATUREKEY");
            String receiptkey = processData.getInputDataAsMap().getString("RECEIPTKEY");


                HashMap<String,String> PRERECEIPTCHECK=new HashMap<String,String>();
                PRERECEIPTCHECK.put("WHSEID", "@user");
                PRERECEIPTCHECK.put("ADDWHO", userid);
                PRERECEIPTCHECK.put("EDITWHO", userid);
                PRERECEIPTCHECK.put("FROMTYPE","2" );
                PRERECEIPTCHECK.put("FROMKEY", receiptkey);
                PRERECEIPTCHECK.put("TRANSCHECK", TRANSCHECK);
                PRERECEIPTCHECK.put("PACKCHECK", PACKCHECK);
                PRERECEIPTCHECK.put("checkresult", checkresult);
                LegacyDBHelper.ExecInsert( "PRERECEIPTCHECK", PRERECEIPTCHECK);

            String[] eSignatureKeys = esignaturekey.split(":");
            String eSignatureKey1=eSignatureKeys[0];
            String eSignatureKey2=eSignatureKeys[1];
            String isConfirmedUser1 = DBHelper.getValue( "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                    eSignatureKey1
            }, String.class, "确认人");

            String isConfirmedUser2 = DBHelper.getValue( "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                    eSignatureKey2
            }, String.class, "复核人");

                Udtrn UDTRN=new Udtrn();
                UDTRN.FROMTYPE="ASN收货检查";
                UDTRN.FROMTABLENAME="PRERECEIPTCHECK";
                UDTRN.FROMKEY=receiptkey;
                UDTRN.FROMKEY3="";
                UDTRN.TITLE01="运输温度检查";    UDTRN.CONTENT01=TRANSCHECK;
                UDTRN.TITLE03="包装&标签检查";    UDTRN.CONTENT03=PACKCHECK;
                UDTRN.TITLE04 = "确认人";
                UDTRN.CONTENT04 = isConfirmedUser1;
                UDTRN.TITLE05 = "复核人";
                UDTRN.CONTENT05 = isConfirmedUser2;
                UDTRN.Insert( userid);

                String ASNReceiptCheckStatus = CodeLookup.getCodeLookupValue("ASNCHKRES",checkresult,"UDF1","检查结果");
            DBHelper.executeUpdate("update receipt set RECEIPTCHECKSTATUS=? where receiptkey=?"
                    ,new String[]{ASNReceiptCheckStatus,receiptkey});


            ServiceDataMap theOutDO = new ServiceDataMap();
            processData.setReturnCode(1);
            processData.setOutputData(theOutDO);


        }
        catch (Exception e)
        {
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());
        }


    }

}