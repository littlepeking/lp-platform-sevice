package com.enhantec.wms.backend.outbound.picking;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.outbound.PickDetail;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.PreparedStatement;
import java.util.Map;
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

public class LpnCreateSODelete extends WMSBaseService {

//    private static final long serialVersionUID = 1L;
//
//    private static ILogger logger = SCELoggerFactory.getInstance(LpnCreateSODelete.class);

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {
//        EXEDataObjectprocessData.getInputDataMap() = (EXEDataObject)context.theEXEDataObjectStack.stackList.get(1);
        String userid = EHContextHelper.getUser().getUsername();


        try{
            String orderKey = serviceDataHolder.getInputDataAsMap().getString("orderKey");
            String orderLineNumber = serviceDataHolder.getInputDataAsMap().getString("orderLineNumber");
            String lpn = serviceDataHolder.getInputDataAsMap().getString("lpn");
            String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString( "ESIGNATUREKEY");

            String status = DBHelper.getStringValue("select status from orders where orderKey = ?",
                    new String[]{orderKey},"");
            if(LegecyUtilHelper.isNull(status)) throw new Exception("未找到发货订单"+orderKey);
//            if(status.compareTo("09")>0) throw new Exception("发货订单"+orderKey+"已经关闭或发货，不能继续操作");
            if(status.equals("95")) throw new Exception("发货订单"+orderKey+"已经关闭或发货，不能继续操作");

            Map<String,String> orderDetailInfo= DBHelper.getRecord(
                    "SELECT IDREQUIRED, EXTERNLINENO,SKU,OPENQTY,SUSR2,SUSR3 FROM ORDERDETAIL WHERE ORDERKEY=? AND ORDERLINENUMBER=? AND STATUS in ('02','04','06','09','55') ",
                    new Object[]{orderKey, orderLineNumber},"订单明细行",false);
            if (null == orderDetailInfo || orderDetailInfo.isEmpty()) throw new Exception("未找到出库单明细行或该明细行的状态已不允许删除");

            /**
             * 删除拣货明细
             */
            List<Map<String, String>> pickDetailList = PickDetail.findByOrderKeyAndOrderLineNumber(
                    orderKey,
                    orderLineNumber, true);

            PreparedStatement qqPrepStmt;

            String pickdetailkey = pickDetailList.get(0).get("PICKDETAILKEY");
            ServiceDataMap thePickDO = new ServiceDataMap();
//            thePickDO.clearDO();
//            thePickDO.setConstraintItem("pickdetailkey", pickdetailkey);
//            thePickDO.setWhereClause(" WHERE PickDetailKey = :pickdetailkey");
//            context.theEXEDataObjectStack.push(thePickDO);
//            logger.info("Calling TrPickDetail.preUpdateFire()");
//            context.theSQLMgr.searchTriggerLibrary("PickDetail")).preDeleteFire();
//            connection = context.getConnection();
            DBHelper.executeUpdate(" DELETE FROM PICKDETAIL WHERE PickDetailKey = ?",
                  new Object[]{pickdetailkey});
//            context.theSQLMgr.searchTriggerLibrary("PickDetail")).postDeleteFire();


            Map<String,String> idHashMap = IDNotes.findById(orderDetailInfo.get("IDREQUIRED"), true);

            DBHelper.executeUpdate("DELETE ORDERDETAIL WHERE ORDERKEY = ? and ORDERLINENUMBER=?",
                    new Object[]{orderKey, orderLineNumber});

            String linesCount = DBHelper.getStringValue("SELECT COUNT(1) FROM ORDERDETAIL WHERE ORDERKEY = ? ",
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
            udtrn.Insert(userid);

            ServiceDataMap theOutDO = new ServiceDataMap();
//            theOutDO.clearDO();
//            theOutDO.setRow(theOutDO.createRow());

            theOutDO.setAttribValue("linesCount",linesCount);

            serviceDataHolder.setOutputData(theOutDO);
            serviceDataHolder.setReturnCode(1);
//            context.theEXEDataObjectStack.push(theOutDO);
//
        }catch (Exception e){
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());
        }
    }
}
