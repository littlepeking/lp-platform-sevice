package com.enhantec.wms.backend.utils.audit;


import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;


/**
 --注册方法  只负责用户名密码验证，不负责插入任何电子签名或者审计记录
 DELETE FROM wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHAuthService';
 insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHAuthService', 'com.enhantec.sce.utils.audit', 'enhantec', 'AuthService', 'TRUE',  'JOHN', 'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,USERNAME,PASSWORD,REASON,NOTES','0.10','0');

 */


public class AuthService extends WMSBaseService {

    public void execute(ServiceDataHolder serviceDataHolder)
    {
        String userid = EHContextHelper.getUser().getUsername();

        try
        {
            String user = serviceDataHolder.getInputDataAsMap().getString( "USERNAME");
            //john 根据复核功能需要，取消当前用户限制
//            if (!userid.toUpperCase().equals(user.toUpperCase()))
//                ExceptionHelper.throwRfFulfillLogicException("签名用户不是当前用户");
            String password = serviceDataHolder.getInputDataAsMap().getString( "PASSWORD");
            //String REASON =processData.getInputDataMap().getString( "REASON");//签名原因
            //String  NOTES =processData.getInputDataMap().getString( "NOTES");//备注
            authenticate(user, password);

        }
        catch (Exception e)
        {
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());
        }


        ServiceDataMap theOutDO = new ServiceDataMap();
        theOutDO.setAttribValue("Authenticated", "true");
        serviceDataHolder.setReturnCode(1);
        serviceDataHolder.setOutputData(theOutDO);

      

    }


    public static void authenticate(String user, String password) {
//
//        Enterprise ent = SsaGenericApplication.getEnterprise("OA", "EpnyAuthenticationService");
//        String fullyQualifiedname= null;
//        EpnyUserContext epnyUserCtx =null;
//
//        if (user.indexOf("@") > -1) {
//            user = user.substring(0, user.indexOf("@"));
//        }
//
//        try {
//
//            EpnyAuthenticationService authnService = (EpnyAuthenticationService) EjbHelper.getSession(ent.getProvider(), null, null, ent.getFactory() ,ent.getJndiName(), EpnyAuthenticationServiceHome.class);
//            epnyUserCtx = authnService.getCredentials(user, password, null);
//            fullyQualifiedname = epnyUserCtx.getUserName();
//
//            authnService.remove();
//        } catch (com.epiphany.shr.util.exceptions.EpiSecurityException e) {
//            Map<String, String> userDetails=new HashMap<String, String>();
//
//            userDetails.put("callerID", fullyQualifiedname);
//            //userDetails.put("component", component);
//
//            try {
//                Enterprise enterprise = SsaGenericApplication.getEnterprise("UserAudit", "EpnyUserAuditService");
//
//                EpnyUserAuditService userAuditService = (EpnyUserAuditService) EjbHelper.getSession(enterprise.getProvider(), null, null, enterprise.getFactory() ,enterprise.getJndiName(), EpnyUserAuditServiceHome.class);
//                userAuditService.setLoginFailuresData(user, userDetails);
//                userAuditService.remove();
//            } catch (com.ssaglobal.SsaException e1) {
//                ApplicationInterface.throwWSError(FrameworkConstants.SP_ER_USER, EXEConstantsConstants.EF_LE_INVALIDDATA, EXEConstantsConstants.EF_MS_RF, 2620, "Unable to connect to OA Server", null);
//
//            } catch (Exception x) {
//                ApplicationInterface.throwWSError(FrameworkConstants.SP_ER_USER, EXEConstantsConstants.EF_LE_INVALIDDATA, EXEConstantsConstants.EF_MS_RF, 1, x.getMessage(), null);
//            }
//
//            ApplicationInterface.throwWSError(FrameworkConstants.SP_ER_USER, EXEConstantsConstants.EF_LE_INVALIDDATA, EXEConstantsConstants.EF_MS_RF, 1, "用户名密码不匹配", null);
//
//        } catch (com.ssaglobal.SsaException e) {
//            ApplicationInterface.throwWSError(FrameworkConstants.SP_ER_USER, EXEConstantsConstants.EF_LE_INVALIDDATA, EXEConstantsConstants.EF_MS_RF, 2620, "Unable to connect to OA Server", null);
//
//        } catch(RemoteException re) {
//            System.out.println("*****##4$RemoteException****");
//            System.out.println("*****="+re.getCause().getMessage());
//            System.out.println("*****##4$#****");
//            ApplicationInterface.throwWSError(FrameworkConstants.SP_ER_USER, EXEConstantsConstants.EF_LE_INVALIDDATA, EXEConstantsConstants.EF_MS_RF, 1, re.getCause().getMessage(), null);
//
//        } catch (Exception x) {
//            ApplicationInterface.throwWSError(FrameworkConstants.SP_ER_USER, EXEConstantsConstants.EF_LE_INVALIDDATA, EXEConstantsConstants.EF_MS_RF, 1, x.getMessage(), null);
//        }
//
    }




}
