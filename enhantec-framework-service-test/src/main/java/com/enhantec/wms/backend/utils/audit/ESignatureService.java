package com.enhantec.wms.backend.utils.audit;

import com.enhantec.wms.backend.framework.WMSBaseService;import com.enhantec.framework.common.utils.EHContextHelper;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.*;

import java.util.Map;
import java.util.HashMap;

import static com.enhantec.wms.backend.utils.audit.AuthService.authenticate;


public class ESignatureService extends WMSBaseService
{



    /**
     --注册方法 负责用户名密码验证和插入电子签名记录
     DELETE FROM wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHEsignature';
     insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHEsignature', 'com.enhantec.sce.utils.audit', 'enhantec', 'ESignatureService', 'TRUE',  'JOHN', 'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,USERNAME,PASSWORD,REASON,NOTES','0.10','0');

     */


    private static final long serialVersionUID = 1L;

    public ESignatureService()
    {
    }


    public void execute(ServiceDataHolder serviceDataHolder)
    {


        String userid = EHContextHelper.getUser().getUsername();
        String eSignatureKey=null;


        try
        {
            String user = serviceDataHolder.getInputDataAsMap().getString( "USERNAME");
            //john 根据复核功能需要，取消当前用户限制
//            if (!userid.toUpperCase().equals(user.toUpperCase()))
//                ExceptionHelper.throwRfFulfillLogicException("签名用户不是当前用户");
            String PASSWORD = serviceDataHolder.getInputDataAsMap().getString( "PASSWORD");
            String REASON = serviceDataHolder.getInputDataAsMap().getString( "REASON");//签名原因
            String  NOTES = serviceDataHolder.getInputDataAsMap().getString( "NOTES");//备注
//
            eSignatureKey = doSignature( user,PASSWORD, REASON, NOTES);

        }
        catch (Exception e)
        {
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException( e.getMessage());
        }finally {
            
        }


        ServiceDataMap theOutDO = new ServiceDataMap();
        theOutDO.setAttribValue("EsignatureKey", eSignatureKey);
        serviceDataHolder.setReturnCode(1);
        serviceDataHolder.setOutputData(theOutDO);


    }

    public static String doSignature( String userid, String password, String reason, String notes) throws Exception {

        authenticate(userid, password);

        Map<String,String> Fields=new HashMap<String,String>();
        String eSignatureKey = String.valueOf(IdGenerationHelper.getNCounter("ESIGNATURE"));
        Fields.put("SERIALKEY", eSignatureKey);
        Fields.put("SIGN", userid);
        Fields.put("REASON", reason);
        Fields.put("NOTES", notes);

        LegacyDBHelper.ExecInsert( "Esignature", Fields);
        return eSignatureKey;
    }
    /**
     * @Author: Allan
     * @Date: 2021/6/16
     * @Description: 根据ESIGNATUREKEY获User
     */
    public static String getUserByEsignaturkey(String ESIGNATUREKEY) throws Exception{
        if(ESIGNATUREKEY.indexOf(':')==-1){
            return DBHelper.getValue("select SIGN FROM ESIGNATURE where SERIALKEY =?",
                    new Object[]{ESIGNATUREKEY},String.class,null);
        }else {
            String[] eSignatureKeys = ESIGNATUREKEY.split(":");
            String User1=getUserByEsignaturkey(eSignatureKeys[0]);
            String User2=getUserByEsignaturkey(eSignatureKeys[1]);
            return User1+":"+User2;
        }

    }



}