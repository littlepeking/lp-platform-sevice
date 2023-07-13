package com.enhantec.wms.backend.utils.common;

public class AuthHelper {

    public static void authenticate(String user, String password) {
//
//        Enterprise ent = SsaGenericApplication.getEnterprise("OA", "EpnyAuthenticationService");
//        String fullyQualifiedname= null;
//        EpnyUserContext epnyUserCtx =null;

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
//            HashMap<String, String> userDetails=new HashMap<String, String>();
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
