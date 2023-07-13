package com.enhantec.wms.backend.outbound.picking;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.outbound.PickDetail;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;

/**
 * dz
 *
 *
  --注册方法
 DELETE FROM wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHLpnCreateSODelete'
 insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHLpnCreateSODelete', 'com.enhantec.sce.outbound.order.picking', 'enhantec', 'LpnCreateSODelete', 'TRUE', 'dz', 'dz',
 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,orderKey,orderLineNumber,ESIGNATUREKEY','0.10','0');

 */

public class LpnCreateSODelete extends LegacyBaseService {

//    private static final long serialVersionUID = 1L;
//
//    private static ILogger logger = SCELoggerFactory.getInstance(LpnCreateSODelete.class);

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {
//        EXEDataObjectprocessData.getInputDataMap() = (EXEDataObject)context.theEXEDataObjectStack.stackList.get(1);
        String userid = context.getUserID();
        Connection connection = context.getConnection();

        try{
            String orderKey = serviceDataHolder.getInputDataAsMap().getString("orderKey");
            String orderLineNumber = serviceDataHolder.getInputDataAsMap().getString("orderLineNumber");
            String lpn = serviceDataHolder.getInputDataAsMap().getString("lpn");
            String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString( "ESIGNATUREKEY");

            String status = LegacyDBHelper.GetValue(context,connection,
                    "select status from orders where orderKey = ?",
                    new String[]{orderKey},"");
            if(LegecyUtilHelper.isNull(status)) throw new Exception("未找到发货订单"+orderKey);
//            if(status.compareTo("09")>0) throw new Exception("发货订单"+orderKey+"已经关闭或发货，不能继续操作");
            if(status.equals("95")) throw new Exception("发货订单"+orderKey+"已经关闭或发货，不能继续操作");

            HashMap<String,String> orderDetailInfo= DBHelper.getRecord(context, connection,
                    "SELECT IDREQUIRED, EXTERNLINENO,SKU,OPENQTY,SUSR2,SUSR3 FROM ORDERDETAIL WHERE ORDERKEY=? AND ORDERLINENUMBER=? AND STATUS in ('02','04','06','09','55') ",
                    new Object[]{orderKey, orderLineNumber},"订单明细行",false);
            if (null == orderDetailInfo || orderDetailInfo.isEmpty()) throw new Exception("未找到出库单明细行或该明细行的状态已不允许删除");

            /**
             * 删除拣货明细
             */
            List<HashMap<String, String>> pickDetailList = PickDetail.findByOrderKeyAndOrderLineNumber(
                    context, connection, orderKey,
                    orderLineNumber, true);

            PreparedStatement qqPrepStmt;

            String pickdetailkey = pickDetailList.get(0).get("PICKDETAILKEY");
            EXEDataObject thePickDO = new EXEDataObject();
//            thePickDO.clearDO();
//            thePickDO.setConstraintItem("pickdetailkey", pickdetailkey);
//            thePickDO.setWhereClause(" WHERE PickDetailKey = :pickdetailkey");
//            context.theEXEDataObjectStack.push(thePickDO);
//            logger.info("Calling TrPickDetail.preUpdateFire()");
//            context.theSQLMgr.searchTriggerLibrary("PickDetail")).preDeleteFire(context);
//            connection = context.getConnection();
            qqPrepStmt = connection.prepareStatement(" DELETE FROM PICKDETAIL WHERE PickDetailKey = ?");
            DBHelper.setValue(qqPrepStmt, 1, pickdetailkey);
            qqPrepStmt.executeUpdate();
//            context.theSQLMgr.searchTriggerLibrary("PickDetail")).postDeleteFire(context);


            HashMap<String,String> idHashMap = IDNotes.findById(context,connection,orderDetailInfo.get("IDREQUIRED"), true);

            DBHelper.executeUpdate(context, connection,"DELETE ORDERDETAIL WHERE ORDERKEY = ? and ORDERLINENUMBER=?",
                    new Object[]{orderKey, orderLineNumber});

            String linesCount = DBHelper.getValue(context, connection,
                    "SELECT COUNT(1) FROM ORDERDETAIL WHERE ORDERKEY = ? ",
                    new Object[]{orderKey},"订单行数").toString();


            Udtrn udtrn = new Udtrn();
            udtrn.EsignatureKey = ESIGNATUREKEY;
            udtrn.FROMTYPE = "扫描容器直接出库-删除明细";
            udtrn.FROMTABLENAME = "ORDERDETAIL";
            udtrn.FROMKEY = orderKey;
            udtrn.FROMKEY1 = orderKey;
            udtrn.FROMKEY2 = orderLineNumber;
            udtrn.TITLE01 = "出库单号"; udtrn.CONTENT01 = orderKey;
            udtrn.TITLE02 = "出库单行号"; udtrn.CONTENT02 = orderLineNumber;
            udtrn.TITLE03 = "物料代码"; udtrn.CONTENT03 = orderDetailInfo.get("SKU");
            udtrn.TITLE04="容器条码"; udtrn.CONTENT04=orderDetailInfo.get("IDREQUIRED");
            udtrn.TITLE05="唯一码"; udtrn.CONTENT05=idHashMap.get("SERIALNUMBER");
            udtrn.TITLE06 = "出库数量"; udtrn.CONTENT06=orderDetailInfo.get("OPENQTY");
            udtrn.Insert(context,connection,userid);

            EXEDataObject theOutDO = new EXEDataObject();
//            theOutDO.clearDO();
//            theOutDO.setRow(theOutDO.createRow());

            theOutDO.setAttribValue("linesCount",linesCount);

            serviceDataHolder.setOutputData(theOutDO);
            serviceDataHolder.setReturnCode(1);
//            context.theEXEDataObjectStack.push(theOutDO);
//
        }catch (Exception e){
            try
            {
                context.releaseConnection(connection);
            }	catch (Exception e1) {		}
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());
        }
    }
}
