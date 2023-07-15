package com.enhantec.wms.backend.outbound.picking;

import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.common.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class ResetPendingTasks extends LegacyBaseService {

    /**
     * JOHN 20201115
     *
     --注册方法
     DELETE FROM wmsadmin.sproceduremap where THEPROCNAME= 'EHResetPendingTasks';
     insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHResetPendingTasks', 'com.enhantec.sce.outbound.order.picking', 'enhantec', 'ResetPendingTasks', 'TRUE', 'JOHN',  'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server','0.10','0');

     */


    //TMEVCP02P1S1 paramters:
    //sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ttM,area,sequence,continue,arg1,arg2,arg3,arg4,arg5,arg6,arg7,arg8,arg9,arg10,arg11,arg12,arg13,arg14,arg15,arg16,arg17,arg18,arg19,arg20,arg21,arg22,arg23,arg24,arg25,arg26,arg27,arg28,arg29,arg30,ioflag,taskkey
    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {

        resetPendingTasks();

    }


    public void resetPendingTasks() {
            Date currentDateTime = UtilHelper.getCurrentSqlDate();
            /**
             * 用户获取到拣货任务时，会将该任务的userKey更新为该用户，status更新为3(拣货中)
             * 此时除该用户以外的其它用户永远没有机会重新获取到该任务。
             * 为了使用户获取后又放弃的任务，其它用户还能获取继续使用。
             * 所以这里需要更新跳过时间到期的任务，更新当前用户正在操作且没有进行跳过的任务。
             * 在TASKMANAGERSKIPTASKS表中且未到期的任务状态不能进行变更，否则会造成重复拣货或者任务状态不对的bug
             */
            DBHelper.executeUpdate(context,
                        "UPDATE TASKDETAIL SET status = '0', userkey = ' ', ReleaseDate = NULL " +
                                "WHERE TaskDetailKey IN ( SELECT TaskDetailKey FROM TASKMANAGERSKIPTASKS WHERE ReleaseDate <= ? ) " +
                                "or (userkey = ? and status = '3' AND TaskDetailKey NOT IN ( SELECT TaskDetailKey FROM TASKMANAGERSKIPTASKS WHERE ReleaseDate > ? )) ",
                Arrays.asList(currentDateTime,context.getUserID(),currentDateTime ));

            DBHelper.executeUpdate(context," DELETE FROM TASKMANAGERSKIPTASKS WHERE ReleaseDate <= ?", Arrays.asList(currentDateTime));

    }

}
