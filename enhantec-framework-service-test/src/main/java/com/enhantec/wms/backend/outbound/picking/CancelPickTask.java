package com.enhantec.wms.backend.outbound.picking;

import com.enhantec.wms.backend.framework.LegacyBaseService;import com.enhantec.wms.backend.framework.Context;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.common.outbound.PickDetail;
import com.enhantec.wms.backend.common.task.TaskDetail;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * JOHN 20200208
 *
 --注册方法
 DELETE FROM SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME= 'EHCancelPickTask';
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHCancelPickTask', 'com.enhantec.sce.outbound.order.picking', 'enhantec', 'CancelPickTask', 'TRUE', 'JOHN',  'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,taskdetailkey','0.10','0');

 */
public class CancelPickTask  extends LegacyBaseService {
//
//    private static ILogger logger = SCELoggerFactory.getInstance(CancelPickTask.class);


    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {
        try {
            //todo:

            String taskDetailKey = serviceDataHolder.getInputDataAsMap().getString("TASKDETAILKEY");

            HashMap<String, String> taskDetailInfo = TaskDetail.findById(context,taskDetailKey,true);

            if(!taskDetailInfo.get("STATUS").equals("0") && !taskDetailInfo.get("STATUS").equals("3"))
                ExceptionHelper.throwRfFulfillLogicException("待删除的任务状态必须为未定或处理中");

            cancelPickDetail(context, taskDetailInfo);

            throw new RuntimeException("not implement");


        } catch (Exception e) {
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());

        } finally {

        }


    }

    public void cancelPickDetail(Context context, HashMap<String,String> taskDetailInfo) throws SQLException {


            HashMap<String, String> pickDetailInfo = PickDetail.findByPickDetailKey(context, taskDetailInfo.get("PICKDETAILKEY"), true);

            DBHelper.executeUpdate(context,
                        "UPDATE PICKDETAIL SET STATUS = 0 WHERE PICKDETAILKEY = ?",
                new Object[]{ pickDetailInfo.get("PICKDETAILKEY")});

//            EXEDataObject thePickDO = new EXEDataObject();
//            thePickDO.clearDO();
//            thePickDO.setConstraintItem("pickdetailkey", taskDetailInfo.get("PICKDETAILKEY"));
//            thePickDO.setWhereClause(" WHERE PickDetailKey = :pickdetailkey");
//            context.theEXEDataObjectStack.push(thePickDO);
//            logger.info("Calling TrPickDetail.preUpdateFire()");
//            context.theSQLMgr.searchTriggerLibrary("PickDetail")).preDeleteFire(context);
//            
//            qqPrepStmt = DBHelper.executeUpdate(" DELETE FROM PICKDETAIL WHERE PickDetailKey = ?");
//            new Object[]{ pickDetailInfo.get("PICKDETAILKEY"));
//            qqPrepStmt.executeUpdate();
//            context.theSQLMgr.searchTriggerLibrary("PickDetail")).postDeleteFire(context);

    }

}