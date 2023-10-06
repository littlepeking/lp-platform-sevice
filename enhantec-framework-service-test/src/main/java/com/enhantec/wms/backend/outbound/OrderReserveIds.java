package com.enhantec.wms.backend.outbound;

import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;

import com.enhantec.framework.common.utils.EHContextHelper;


/**
 * JOHN 20201119按日期 获取采购批次号
 --注册方法

 DELETE FROM wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHOrderReserveIds'
 insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHOrderReserveIds', 'com.enhantec.sce.outbound.order', 'enhantec', 'OrderReserveIds', 'TRUE', 'JOHN',  'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server, PROJECTCODE,IDS','0.10','0');


 */



public class OrderReserveIds extends WMSBaseService {


    private static final long serialVersionUID = 1L;

    public OrderReserveIds() {
    }

    public void execute(ServiceDataHolder serviceDataHolder) {



        String userid = EHContextHelper.getUser().getUsername();
        try
        {

            String projectId = serviceDataHolder.getInputDataAsMap().getString("PROJECTCODE");
            String ids = serviceDataHolder.getInputDataAsMap().getString( "IDS");

            String[] idArray = ids.split(",");

            String idStr = "'" + String.join("','",idArray)+ "'" ;

            DBHelper.executeUpdate( "UPDATE IDNOTES SET PROJECTCODE = ? WHERE ID IN ("+idStr+")", new String[]{projectId});

            Udtrn UDTRN=new Udtrn();
            //UDTRN.EsignatureKey=ESIGNATUREKEY;
            UDTRN.FROMTYPE="预留项目库存";
            UDTRN.FROMTABLENAME="IDNOTES";
            UDTRN.FROMKEY=projectId;
            UDTRN.TITLE01="项目号"; UDTRN.CONTENT01=projectId;
            UDTRN.TITLE02="容器条码列表"; UDTRN.CONTENT01=ids;
            UDTRN.insert( userid);
            //--------------------------------------------------------------


            ServiceDataMap theOutDO = new ServiceDataMap();
            serviceDataHolder.setOutputData(theOutDO);
            serviceDataHolder.setReturnCode(1);

        }
        catch (Exception e)
        {
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException( e.getMessage());
        }finally {
            
        }
    }
}