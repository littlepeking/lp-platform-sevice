package com.enhantec.wms.backend.outbound.picking;

import com.enhantec.wms.backend.common.KeyGen;
import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.inventory.VLotAttribute;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.common.outbound.PickDetail;
import com.enhantec.wms.backend.common.task.TaskDetail;
import com.enhantec.wms.backend.framework.LegacyBaseService;import com.enhantec.wms.backend.framework.Context;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.UserInfo;
import com.enhantec.wms.backend.utils.common.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

import static com.enhantec.wms.backend.utils.common.UtilHelper.trimZerosAndToStr;

/**
 * JOHN 20201115
 *
 --注册方法
 DELETE FROM wmsadmin.sproceduremap where THEPROCNAME= 'EHSwapPickDetailLPN';
 insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHSwapPickDetailLPN', 'com.enhantec.sce.outbound.order', 'enhantec', 'SwapPickDetailLPN', 'TRUE', 'JOHN',  'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,TASKDETAILKEY,NEWID','0.10','0');

 */

public class SwapPickDetailLPN extends LegacyBaseService {
//
//    private static ILogger logger = SCELoggerFactory.getInstance(SwapPickDetailLPN.class);

    UserInfo ourUser = new UserInfo();

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {
        try {
//            ourUser = new UserInfo();
//            ourUser.mySqlMgr = pContext.theSQLMgr;
//            ourUser.mySession = ourUser.mySqlMgr.getDBSession();
//            ourUser.name = pContext.getUserID();

//
//            EXEDataObjectprocessData.getInputDataMap() = ((EXEDataObject) ((Object) pContext.theEXEDataObjectStack.stackList.get(1)));

            String taskDetailKey = serviceDataHolder.getInputDataAsMap().getString("TASKDETAILKEY");
            String newId = serviceDataHolder.getInputDataAsMap().getString("NEWID");

            HashMap<String, String> lotxLocxIdInfo = LotxLocxId.findAvailInvById(context,null,newId,false,true);

            HashMap<String, String> taskDetailInfo = TaskDetail.findById(context,null,taskDetailKey,true);


            swapLpnValidate(context, lotxLocxIdInfo, taskDetailInfo);


            String newTaskDetailKey = swapPickDetail(context, taskDetailInfo, newId);

            EXEDataObject outDO = new EXEDataObject();

            HashMap<String,String>  skuHashMap = SKU.findById(context, null,taskDetailInfo.get("SKU"),true);
            HashMap<String,Object>  lotHashMap = VLotAttribute.findByLot(context, null,taskDetailInfo.get("LOT"),true);
            HashMap<String,String>  idNotesHashMap = IDNotes.findById(context, null,newId,true);
            HashMap<String,String>  taskDetailHashMap = TaskDetail.findById(context, null,newTaskDetailKey,true);
            HashMap<String,String>  lotxLocxIdHashMap = LotxLocxId.findById(context, null,newId,true);
            String  stdUom = UOM.getStdUOM(context, null,idNotesHashMap.get("PACKKEY"));

            outDO.setAttribValue("taskdetailkey",newTaskDetailKey);
            outDO.setAttribValue("skudescr",skuHashMap.get("DESCR"));
            outDO.setAttribValue("lottable06",lotHashMap.get("LOTTABLE06").toString());
            outDO.setAttribValue("barreldescr",idNotesHashMap.get("BARRELDESCR"));
            outDO.setAttribValue("status",taskDetailHashMap.get("STATUS"));
            outDO.setAttribValue("orderkey",taskDetailHashMap.get("ORDERKEY"));
            outDO.setAttribValue("fromloc",taskDetailHashMap.get("FROMLOC"));
            outDO.setAttribValue("sku",taskDetailHashMap.get("SKU"));
            outDO.setAttribValue("qty",taskDetailHashMap.get("QTY"));
            outDO.setAttribValue("lpnqty",lotxLocxIdInfo.get("QTY"));
            outDO.setAttribValue("fromid",taskDetailHashMap.get("FROMID"));
            outDO.setAttribValue("stdUom",stdUom);
            outDO.setAttribValue("availQty",lotxLocxIdHashMap.get("AVAILABLEQTY"));
            outDO.setAttribValue("packkey",idNotesHashMap.get("PACKKEY"));




            serviceDataHolder.setOutputData(outDO);



        } catch (SQLException var101) {
            throw new DBResourceException(var101);
        } finally {

        }


    }

    public void swapLpnValidate(Context context, HashMap<String, String> newLotxlocxidInfo, HashMap<String, String> taskDetailInfo) {

        HashMap<String, String> orderDetailInfo = Orders.findOrderDetailByKey(context, null, taskDetailInfo.get("ORDERKEY"),taskDetailInfo.get("ORDERLINENUMBER"), true);
        if(!UtilHelper.isEmpty(orderDetailInfo.get("SERIALNUMBER"))) ExceptionHelper.throwRfFulfillLogicException("指定唯一码出库时，不允许切换容器");

        if(!trimZerosAndToStr(newLotxlocxidInfo.get("AVAILABLEQTY")).equals(
                trimZerosAndToStr(newLotxlocxidInfo.get("QTY"))
        )) ExceptionHelper.throwRfFulfillLogicException("目标容器条码已被分配或拣货，不允许置换");

        if(new BigDecimal(taskDetailInfo.get("QTY")).compareTo(new BigDecimal(newLotxlocxidInfo.get("AVAILABLEQTY")))>0)
         ExceptionHelper.throwRfFulfillLogicException("目标容器可用量"+trimZerosAndToStr(newLotxlocxidInfo.get("AVAILABLEQTY"))
                 +"小于当前分配量"+trimZerosAndToStr(taskDetailInfo.get("QTY")));

        HashMap<String, String> lotxlocxidInfo = LotxLocxId.findById(context, null, taskDetailInfo.get("FROMID"), true);


        			/*
					批属性01
					批属性02	质量等级
					批属性03	状态
					批属性04	入库日期
					批属性05	复验日期
					批属性06	批号
					批属性07	规格
					批属性08	供应商
					批属性09 供应商批次
					批属性10 采购编码
					批属性11	有效期
					批属性12	原材料-取样日期/成品-生产日期
				*/
        if(!UtilHelper.equals(lotxlocxidInfo.get("PROJECTCODE"), newLotxlocxidInfo.get("PROJECTCODE")))
            ExceptionHelper.throwRfFulfillLogicException("不允许置换不同项目号的容器");

        if(!UtilHelper.equals(newLotxlocxidInfo.get("LOTTABLE01"), lotxlocxidInfo.get("LOTTABLE01"))){
            ExceptionHelper.throwRfFulfillLogicException("不允许置换不同包装的容器");
        }else if(!UtilHelper.equals(newLotxlocxidInfo.get("LOTTABLE02"), lotxlocxidInfo.get("LOTTABLE02"))){
            ExceptionHelper.throwRfFulfillLogicException("不允许置换不同存货类型的容器");
        }else if(!UtilHelper.equals(newLotxlocxidInfo.get("LOTTABLE06"), lotxlocxidInfo.get("LOTTABLE06"))){
            ExceptionHelper.throwRfFulfillLogicException("不允许置换不同收货批次的容器");
        }else if(!UtilHelper.equals(newLotxlocxidInfo.get("ELOTTABLE01"), lotxlocxidInfo.get("ELOTTABLE01"))){
            ExceptionHelper.throwRfFulfillLogicException("不允许置换不同的保税账册号");
        }else if(!UtilHelper.equals(newLotxlocxidInfo.get("ELOTTABLE02"), lotxlocxidInfo.get("ELOTTABLE02"))){
            ExceptionHelper.throwRfFulfillLogicException("不允许置换不同保税状态的容器");
        }else if(!UtilHelper.equals(newLotxlocxidInfo.get("ELOTTABLE03"), lotxlocxidInfo.get("ELOTTABLE03"))){
            ExceptionHelper.throwRfFulfillLogicException("不允许置换不同质量状态的容器");
        }else if(!UtilHelper.equals(newLotxlocxidInfo.get("ELOTTABLE07"), lotxlocxidInfo.get("ELOTTABLE07"))){
            ExceptionHelper.throwRfFulfillLogicException("不允许置换不同型号的容器");
        }else if(!UtilHelper.equals(newLotxlocxidInfo.get("ELOTTABLE08"), lotxlocxidInfo.get("ELOTTABLE08"))){
            ExceptionHelper.throwRfFulfillLogicException("不允许置换不同供应商的容器");
        }else if(!UtilHelper.equals(newLotxlocxidInfo.get("ELOTTABLE09"), lotxlocxidInfo.get("ELOTTABLE09"))){
            ExceptionHelper.throwRfFulfillLogicException("不允许置换不同供应商批次的容器");
        }else if(!UtilHelper.equals(newLotxlocxidInfo.get("ELOTTABLE11"), lotxlocxidInfo.get("ELOTTABLE11"))){
            ExceptionHelper.throwRfFulfillLogicException("不允许置换不同有效期的容器");
        }else if(!UtilHelper.equals(newLotxlocxidInfo.get("ELOTTABLE12"), lotxlocxidInfo.get("ELOTTABLE12"))){
            ExceptionHelper.throwRfFulfillLogicException("不允许置换不同生产日期的容器");
        }



    }

    public String swapPickDetail(Context context, HashMap<String,String> taskDetailInfo,String newId) throws SQLException {


        Connection qqConnection = null;

        try {
              HashMap<String, String> pickDetailInfo = PickDetail.findByPickDetailKey(context, null, taskDetailInfo.get("PICKDETAILKEY"), true);
              HashMap<String, String> lotxLocxIdInfo = LotxLocxId.findAvailInvById(context, null, newId,false, true);


            int qqRowCount = 0;

            PreparedStatement qqPrepStmt = null;

            try {
//                context.theSQLMgr.transactionBegin();
                qqConnection = context.getConnection();
                qqPrepStmt = qqConnection.prepareStatement(
                        "UPDATE PICKDETAIL SET STATUS = 0 WHERE PICKDETAILKEY = ?");
                DBHelper.setValue(qqPrepStmt, 1, taskDetailInfo.get("PICKDETAILKEY"));
                qqRowCount = qqRowCount + qqPrepStmt.executeUpdate();
            } catch (SQLException qqSQLException) {
                throw new DBResourceException(qqSQLException);
            } finally {
                try {context.releaseStatement(qqPrepStmt);}catch (Exception e) { }
                try {context.releaseConnection(qqConnection);}catch (Exception e) { }

            }
//              context.theSQLMgr.transactionCommit();
//
//              context.theSQLMgr.transactionBegin();
//              EXEDataObject thePickDO = new EXEDataObject();
//              thePickDO.clearDO();
//              thePickDO.setConstraintItem("pickdetailkey", taskDetailInfo.get("PICKDETAILKEY"));
//              thePickDO.setWhereClause(" WHERE PickDetailKey = :pickdetailkey");
//              context.theEXEDataObjectStack.push(thePickDO);
//              logger.info("Calling TrPickDetail.preUpdateFire()");
//              context.theSQLMgr.searchTriggerLibrary("PickDetail")).preDeleteFire(context);
//              qqConnection = context.getConnection();
//              qqPrepStmt = qqConnection.prepareStatement(" DELETE FROM PICKDETAIL WHERE PickDetailKey = ?");
//              DBHelper.setValue(qqPrepStmt, 1, pickDetailInfo.get("PICKDETAILKEY"));
//              qqPrepStmt.executeUpdate();



              String pickDetailKey = KeyGen.getKey( context,"PICKDETAILKEY", 10);
//              thePickDO.clearDO();
//              thePickDO.setConstraintItem("pickdetailkey", pickDetailKey);
//              thePickDO.setWhereClause(" WHERE PickDetailKey = :pickdetailkey");
//              context.theEXEDataObjectStack.push(thePickDO);
//              context.theSQLMgr.searchTriggerLibrary("PickDetail")).preInsertFire(context);
              qqConnection = context.getConnection();
              qqPrepStmt = qqConnection.prepareStatement(" INSERT INTO PICKDETAIL ( PickDetailKey, CaseID, PickHeaderkey, OrderKey, OrderLineNumber, Lot, Storerkey, Sku, PackKey, UOM, UOMQty, Qty, Loc, ToLoc, ID, CartonGroup, CartonType, DoReplenish, ReplenishZone, DoCartonize, PickMethod, AddWho, EditWho, SeqNo, StatusRequired,fromloc, SelectedCartonType, SelectedCartonID, grosswgt, netwgt, tarewgt, PickContPlacement, status ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,? )");

              String caseId = KeyGen.getKey(context, "CARTONID", 10);

              double grosswgt = 0.0D;
              double netwgt = 0.0D;
              double tarewgt = 0.0D;
              DBHelper.setValue(qqPrepStmt, 1, pickDetailKey);
              DBHelper.setValue(qqPrepStmt, 2, caseId);
              DBHelper.setValue(qqPrepStmt, 3, " ");
              DBHelper.setValue(qqPrepStmt, 4, pickDetailInfo.get("ORDERKEY"));
              DBHelper.setValue(qqPrepStmt, 5, pickDetailInfo.get("ORDERLINENUMBER"));
              DBHelper.setValue(qqPrepStmt, 6, lotxLocxIdInfo.get("LOT"));
              DBHelper.setValue(qqPrepStmt, 7, lotxLocxIdInfo.get("STORERKEY"));
              DBHelper.setValue(qqPrepStmt, 8, lotxLocxIdInfo.get("SKU"));
              DBHelper.setValue(qqPrepStmt, 9, lotxLocxIdInfo.get("PACKKEY"));

              String stdUOM = UOM.getStdUOM(context, null, lotxLocxIdInfo.get("PACKKEY"));
              //right now just provide stduom, it should be always 6
              String uom = UOM.getUOMCode(context, lotxLocxIdInfo.get("PACKKEY"), stdUOM);
              DBHelper.setValue(qqPrepStmt, 10, uom);
              DBHelper.setValue(qqPrepStmt, 11, taskDetailInfo.get("QTY"));
              DBHelper.setValue(qqPrepStmt, 12, taskDetailInfo.get("QTY"));
              DBHelper.setValue(qqPrepStmt, 13, lotxLocxIdInfo.get("LOC"));
              DBHelper.setValue(qqPrepStmt, 14, pickDetailInfo.get("TOLOC"));
              DBHelper.setValue(qqPrepStmt, 15, lotxLocxIdInfo.get("ID"));
              DBHelper.setValue(qqPrepStmt, 16, pickDetailInfo.get("CARTONGROUP"));
              DBHelper.setValue(qqPrepStmt, 17, pickDetailInfo.get("CARTONTYPE"));
              DBHelper.setValue(qqPrepStmt, 18, pickDetailInfo.get("DOREPLENISH"));
              DBHelper.setValue(qqPrepStmt, 19, pickDetailInfo.get("REPLENISHZONE"));
              DBHelper.setValue(qqPrepStmt, 20, pickDetailInfo.get("DOCARTONIZE"));
              DBHelper.setValue(qqPrepStmt, 21, pickDetailInfo.get("PICKMETHOD"));
              DBHelper.setValue(qqPrepStmt, 22,context.getUserID());
              DBHelper.setValue(qqPrepStmt, 23, context.getUserID());
              DBHelper.setValue(qqPrepStmt, 24, 99999);
              DBHelper.setValue(qqPrepStmt, 25, pickDetailInfo.get("STATUSREQUIRED"));
              DBHelper.setValue(qqPrepStmt, 26, lotxLocxIdInfo.get("LOC"));
              DBHelper.setValue(qqPrepStmt, 27, pickDetailInfo.get("CARTONTYPE"));
              DBHelper.setValue(qqPrepStmt, 28, caseId);
              DBHelper.setValue(qqPrepStmt, 29, grosswgt);
              DBHelper.setValue(qqPrepStmt, 30, netwgt);
              DBHelper.setValue(qqPrepStmt, 31, tarewgt);
              DBHelper.setValue(qqPrepStmt, 32, pickDetailInfo.get("PICKCONTPLACEMENT"));
              DBHelper.setValue(qqPrepStmt, 33, "1"); //status: released
              qqPrepStmt.executeUpdate();

              EXEDataObject triggerDO = new EXEDataObject();
              triggerDO.setAttribValue("qty",
                      taskDetailInfo.get("QTY"));
              triggerDO.setAttribValue("sku",
                      lotxLocxIdInfo.get("SKU"));
              triggerDO.setAttribValue("storerkey",
                      lotxLocxIdInfo.get("STORERKEY"));
              triggerDO.setAttribValue("loc",
                      lotxLocxIdInfo.get("LOC"));
              triggerDO.setAttribValue("pickdetailkey",
                      pickDetailKey);
              triggerDO.setAttribValue("orderkey",
                      pickDetailInfo.get("ORDERKEY"));
              triggerDO.setAttribValue("orderlinenumber",
                      pickDetailInfo.get("ORDERLINENUMBER"));
              triggerDO.setAttribValue("lot",
                      lotxLocxIdInfo.get("LOT"));
              triggerDO.setAttribValue("id", lotxLocxIdInfo.get("ID"));
              triggerDO.setAttribValue("pickmethod",
                      pickDetailInfo.get("PICKMETHOD"));
              triggerDO.setAttribValue("uom",
                          uom);
              triggerDO.setAttribValue("PACKKEY",
                      lotxLocxIdInfo.get("PACKKEY"));
//
//              context.theEXEDataObjectStack.push(triggerDO);
//
//              context.theSQLMgr.searchTriggerLibrary("PickDetail")).postInsertFire(context);
//
//              context.theSQLMgr.searchTriggerLibrary("TaskDetail")).preInsertFire(context);

              HashMap<String, String> locInfo = Loc.findById(context, null, lotxLocxIdInfo.get("LOC"), true);
              qqConnection = context.getConnection();
              qqPrepStmt = qqConnection.prepareStatement(" INSERT INTO TASKDETAIL ( TaskDetailKey, TaskType, StorerKey, Sku, Lot, UOM, UOMQTY, Qty, FromLoc, LogicalFromLoc, FromID, ToLoc, ToId, SourceType, SourceKey, WaveKey, CaseId, OrderKey, OrderLineNumber, PickDetailKey, PickMethod, AddWho, EditWho, Door, Route, Stop, Putawayzone,STATUS,USERKEY,REASONKEY,STARTTIME,ENDTIME ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,? )");
              String newTaskDetailKey = KeyGen.getKey(context,"TASKDETAILKEY", 10);
              DBHelper.setValue(qqPrepStmt, 1, newTaskDetailKey);
              DBHelper.setValue(qqPrepStmt, 2, "PK");
              DBHelper.setValue(qqPrepStmt, 3, lotxLocxIdInfo.get("STORERKEY"));
              DBHelper.setValue(qqPrepStmt, 4, lotxLocxIdInfo.get("SKU"));
              DBHelper.setValue(qqPrepStmt, 5, lotxLocxIdInfo.get("LOT"));
              DBHelper.setValue(qqPrepStmt, 6, uom);
              DBHelper.setValue(qqPrepStmt, 7, taskDetailInfo.get("QTY"));
              DBHelper.setValue(qqPrepStmt, 8, taskDetailInfo.get("QTY"));
              DBHelper.setValue(qqPrepStmt, 9, lotxLocxIdInfo.get("LOC"));
              DBHelper.setValue(qqPrepStmt, 10, locInfo.get("LOGICALLOCATION"));
              DBHelper.setValue(qqPrepStmt, 11, lotxLocxIdInfo.get("ID"));
              DBHelper.setValue(qqPrepStmt, 12, pickDetailInfo.get("TOLOC"));
              DBHelper.setValue(qqPrepStmt, 13, lotxLocxIdInfo.get("ID"));
              DBHelper.setValue(qqPrepStmt, 14, "PICKDETAIL");
              DBHelper.setValue(qqPrepStmt, 15, pickDetailKey);
              DBHelper.setValue(qqPrepStmt, 16, pickDetailInfo.get("WAVEKEY"));
              DBHelper.setValue(qqPrepStmt, 17, caseId);
              DBHelper.setValue(qqPrepStmt, 18, pickDetailInfo.get("ORDERKEY"));
              DBHelper.setValue(qqPrepStmt, 19, pickDetailInfo.get("ORDERLINENUMBER"));
              DBHelper.setValue(qqPrepStmt, 20, pickDetailKey);
              DBHelper.setValue(qqPrepStmt, 21, pickDetailInfo.get("PICKMETHOD"));
              DBHelper.setValue(qqPrepStmt, 22, context.getUserID());
              DBHelper.setValue(qqPrepStmt, 23, context.getUserID());
              DBHelper.setValue(qqPrepStmt, 24, pickDetailInfo.get("DOOR"));
              DBHelper.setValue(qqPrepStmt, 25, pickDetailInfo.get("ROUTE"));
              DBHelper.setValue(qqPrepStmt, 26, pickDetailInfo.get("STOP"));
              DBHelper.setValue(qqPrepStmt, 27, locInfo.get("PUTAWAYZONE"));
              //Add following fields for occupy the task immediately
              DBHelper.setValue(qqPrepStmt, 28, "3"); //status
              DBHelper.setValue(qqPrepStmt, 29, context.getUserID());
              DBHelper.setValue(qqPrepStmt, 30, " ");//reasonekey
              java.sql.Date currentDate = UtilHelper.getCurrentSqlDate();
              DBHelper.setValue(qqPrepStmt, 31, currentDate);
              DBHelper.setValue(qqPrepStmt, 32, currentDate);
              qqPrepStmt.executeUpdate();


//              triggerDO = new EXEDataObject();
//              triggerDO.setConstraintItem("taskdetailkey"), newTaskDetailKey));
//              triggerDO.setAttribValue("taskdetailkey"), newTaskDetailKey));
//
//              context.theEXEDataObjectStack.push(triggerDO);
//              context.theSQLMgr.searchTriggerLibrary("TaskDetail")).postInsertFire(context);
//              context.theSQLMgr.transactionCommit();
              return newTaskDetailKey;
        }catch (Exception e){
//            context.theSQLMgr.transactionAbort();
            throw e;
        }
        finally{
            try	{	context.releaseConnection(qqConnection); }	catch (Exception e1) {		}
        }
    }

}
