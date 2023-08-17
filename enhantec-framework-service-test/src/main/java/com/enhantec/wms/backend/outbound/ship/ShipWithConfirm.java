package com.enhantec.wms.backend.outbound.ship;

import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.common.ServiceHelper;

/**
 * 注册方法
DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHShipWithConfirm'
insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
values ('EHShipWithConfirm', 'com.enhantec.sce.outbound.order.ship', 'enhantec', 'ShipWithConfirm', 'TRUE', 'JOHN',  'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,orderkey,esignaturekey','0.10','0');
 */
public class ShipWithConfirm extends WMSBaseService {

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {

        ServiceHelper.executeService( "EHConfirmSO", serviceDataHolder);
        ServiceHelper.executeService("EHShipByOrder", serviceDataHolder);
    }
}
