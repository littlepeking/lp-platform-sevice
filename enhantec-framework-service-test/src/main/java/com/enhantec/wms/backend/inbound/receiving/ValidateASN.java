
package com.enhantec.wms.backend.inbound.receiving;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.inbound.asn.utils.ReceiptValidationHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;
import com.enhantec.wms.backend.utils.common.UtilHelper;

import java.util.Map;

public class ValidateASN extends WMSBaseService {


    /**
     * --注册方法
     delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHValidateASN'
     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHValidateASN', 'com.enhantec.sce.inbound.receiving', 'enhantec', 'ValidateASN', 'TRUE', 'JOHN', 'JOHN'
     , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,RECEIPTKEY,ESIGNATUREKEY','0.10','0');
     */


    private static final long serialVersionUID = 1L;

    public ValidateASN() {
    }

    public void execute(ServiceDataHolder serviceDataHolder) {
        String userid = EHContextHelper.getUser().getUsername();


        try {



            String receiptKey = serviceDataHolder.getInputDataAsMap().getString("RECEIPTKEY");
            String esignatureKey = serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

            Map<String, String>  receiptInfo =  Receipt.findByReceiptKey(receiptKey,true);

            if(UtilHelper.isEmpty(receiptInfo.get("EXTERNRECEIPTKEY"))) ExceptionHelper.throwRfFulfillLogicException("外部单号不允许为空");

            ReceiptValidationHelper.validateASN( receiptKey);

          

        }catch (Exception e){
            
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());

        }finally {
            
        }
    }

}