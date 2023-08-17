package com.enhantec.wms.backend.utils.audit;


import com.enhantec.wms.backend.framework.WMSBaseService;import com.enhantec.framework.common.utils.EHContextHelper;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.common.UtilHelper;

/**
 --注册方法 只负责插入审计历史记录，不负责验证
 DELETE FROM wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHEsignature';
 insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHEsignature', 'com.enhantec.sce.utils.audit', 'enhantec', 'ESignatureService', 'TRUE',  'JOHN', 'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,USERNAME,PASSWORD,REASON,NOTES','0.10','0');

 */


public class AuditService extends WMSBaseService {


    @Deprecated
    public void execute(ServiceDataHolder serviceDataHolder)
    {
        //John
        //暂时留出接口，未实现完成，目前后台服务直接使用静态方法doAudit
        //对于要记录BIO变化的前台服务，直接使用UIPreAudit和UIPostAudit

        /*
        EXEDataObjectprocessData.getInputDataMap() = (EXEDataObject)context.theEXEDataObjectStack.stackList.get(1);
        String userid = EHContextHelper.getUser().getUsername();
        String eSignatureKey=null;

        
        try
        {
            String USERNAME =processData.getInputDataMap().getString( "USERNAME");
            String PASSWORD =processData.getInputDataMap().getString( "PASSWORD");
            String REASON =processData.getInputDataMap().getString( "REASON");//签名原因
            String NOTES =processData.getInputDataMap().getString( "NOTES");//备注
            // todo implement
            UDTRN uDTRN = new UDTRN();

            doAudit(USERNAME,PASSWORD,REASON,NOTES,uDTRN);

        }
        catch (Exception e)
        {


            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException( e.getMessage());
        }



        EXEDataObject theOutDO = new EXEDataObject();
        theOutDO.clearDO();
        theOutDO.setRow(theOutDO.createRow());

        theOutDO.setAttribValue("EsignatureKey"), eSignatureKey));
        theOutDO.setReturnCode(1);
        context.theEXEDataObjectStack.push(theOutDO);


         */


    }

    public static void doAudit( Udtrn uDTRN) throws Exception {
        //Add audit record
        String userid = EHContextHelper.getUser().getUsername();
        uDTRN.Insert( userid);

    }

    @Deprecated
    public static void doAudit( String username, String password, String reason, String notes, Udtrn uDTRN) throws Exception {

        //Do esignature
        //String userid = EHContextHelper.getUser().getUsername();
        if (!UtilHelper.isEmpty(username) && !UtilHelper.isEmpty(password)){
            //john 根据复核功能需要，取消当前用户限制
//            if(!userid.toUpperCase().equals(username.toUpperCase()))
//                ExceptionHelper.throwRfFulfillLogicException("签名用户不是当前用户");
//
            uDTRN.EsignatureKey = ESignatureService.doSignature( username,password, reason, notes);
        }

        //Add audit record
        uDTRN.Insert( username);

    }



}
