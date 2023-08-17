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
import com.enhantec.wms.backend.framework.WMSBaseService;import com.enhantec.framework.common.utils.EHContextHelper;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.*;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import static com.enhantec.wms.backend.utils.common.UtilHelper.trimZerosAndToStr;

/**
 * JOHN 20201115
 *
 --注册方法
 DELETE FROM wmsadmin.sproceduremap where THEPROCNAME= 'EHSwapPickDetailLPN';
 insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHSwapPickDetailLPN', 'com.enhantec.sce.outbound.order', 'enhantec', 'SwapPickDetailLPN', 'TRUE', 'JOHN',  'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,TASKDETAILKEY,NEWID','0.10','0');

 */

public class SwapPickDetailLPN extends WMSBaseService {
//
//    private static ILogger logger = SCELoggerFactory.getInstance(SwapPickDetailLPN.class);

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {
        try {
//            ourUser = new UserInfo();
//            ourUser.mySqlMgr = pContext.theSQLMgr;
//            ourUser.mySession = ourUser.mySqlMgr.getDBSession();
//            ourUser.name = pEHContextHelper.getUser().getUsername();

//
//            EXEDataObjectprocessData.getInputDataMap() = ((EXEDataObject) ((Object) pContext.theEXEDataObjectStack.stackList.get(1)));

            String taskDetailKey = serviceDataHolder.getInputDataAsMap().getString("TASKDETAILKEY");
            String newId = serviceDataHolder.getInputDataAsMap().getString("NEWID");

            Map<String, String> lotxLocxIdInfo = LotxLocxId.findAvailInvById(newId,false,true);

            Map<String, String> taskDetailInfo = TaskDetail.findById(taskDetailKey,true);


            swapLpnValidate( lotxLocxIdInfo, taskDetailInfo);


            String newTaskDetailKey = swapPickDetail( taskDetailInfo, newId);

            ServiceDataMap outDO = new ServiceDataMap();

            Map<String,String>  skuHashMap = SKU.findById(taskDetailInfo.get("SKU"),true);
            Map<String,Object>  lotHashMap = VLotAttribute.findByLot(taskDetailInfo.get("LOT"),true);
            Map<String,String>  idNotesHashMap = IDNotes.findById(newId,true);
            Map<String,String>  taskDetailHashMap = TaskDetail.findById(newTaskDetailKey,true);
            Map<String,String>  lotxLocxIdHashMap = LotxLocxId.findById(newId,true);
            String  stdUom = UOM.getStdUOM(idNotesHashMap.get("PACKKEY"));

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

    public void swapLpnValidate( Map<String, String> newLotxlocxidInfo, Map<String, String> taskDetailInfo) {

        Map<String, String> orderDetailInfo = Orders.findOrderDetailByKey( taskDetailInfo.get("ORDERKEY"),taskDetailInfo.get("ORDERLINENUMBER"), true);
        if(!UtilHelper.isEmpty(orderDetailInfo.get("SERIALNUMBER"))) ExceptionHelper.throwRfFulfillLogicException("指定唯一码出库时，不允许切换容器");

        if(!trimZerosAndToStr(newLotxlocxidInfo.get("AVAILABLEQTY")).equals(
                trimZerosAndToStr(newLotxlocxidInfo.get("QTY"))
        )) ExceptionHelper.throwRfFulfillLogicException("目标容器条码已被分配或拣货，不允许置换");

        if(new BigDecimal(taskDetailInfo.get("QTY")).compareTo(new BigDecimal(newLotxlocxidInfo.get("AVAILABLEQTY")))>0)
         ExceptionHelper.throwRfFulfillLogicException("目标容器可用量"+trimZerosAndToStr(newLotxlocxidInfo.get("AVAILABLEQTY"))
                 +"小于当前分配量"+trimZerosAndToStr(taskDetailInfo.get("QTY")));

        Map<String, String> lotxlocxidInfo = LotxLocxId.findById( taskDetailInfo.get("FROMID"), true);


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

    public String swapPickDetail( Map<String,String> taskDetailInfo,String newId) throws SQLException {




        try {
              Map<String, String> pickDetailInfo = PickDetail.findByPickDetailKey( taskDetailInfo.get("PICKDETAILKEY"), true);
              Map<String, String> lotxLocxIdInfo = LotxLocxId.findAvailInvById( newId,false, true);


            int qqRowCount = 0;

            PreparedStatement qqPrepStmt = null;

            DBHelper.executeUpdate(
                        "UPDATE PICKDETAIL SET STATUS = 0 WHERE PICKDETAILKEY = ?",new Object[]{
                                taskDetailInfo.get("PICKDETAILKEY")
            });

//              context.theSQLMgr.transactionCommit();
//
//              context.theSQLMgr.transactionBegin();
//              EXEDataObject thePickDO = new EXEDataObject();
//              thePickDO.clearDO();
//              thePickDO.setConstraintItem("pickdetailkey", taskDetailInfo.get("PICKDETAILKEY"));
//              thePickDO.setWhereClause(" WHERE PickDetailKey = :pickdetailkey");
//              context.theEXEDataObjectStack.push(thePickDO);
//              logger.info("Calling TrPickDetail.preUpdateFire()");
//              context.theSQLMgr.searchTriggerLibrary("PickDetail")).preDeleteFire();
//              
//              qqPrepStmt = DBHelper.executeUpdate(" DELETE FROM PICKDETAIL WHERE PickDetailKey = ?");
//              new Object[]{ pickDetailInfo.get("PICKDETAILKEY"));
//              qqPrepStmt.executeUpdate();



              String pickDetailKey = KeyGen.getKey( "PICKDETAILKEY", 10);
//              thePickDO.clearDO();
//              thePickDO.setConstraintItem("pickdetailkey", pickDetailKey);
//              thePickDO.setWhereClause(" WHERE PickDetailKey = :pickdetailkey");
//              context.theEXEDataObjectStack.push(thePickDO);
//              context.theSQLMgr.searchTriggerLibrary("PickDetail")).preInsertFire();

            String caseId = KeyGen.getKey( "CARTONID", 10);

            double grosswgt = 0.0D;
            double netwgt = 0.0D;
            double tarewgt = 0.0D;

            String stdUOM = UOM.getStdUOM( lotxLocxIdInfo.get("PACKKEY"));
            //right now just provide stduom, it should be always 6
            String uom = UOM.getUOMCode( lotxLocxIdInfo.get("PACKKEY"), stdUOM);

            DBHelper.executeUpdate(
                    " INSERT INTO PICKDETAIL ( PickDetailKey, CaseID, PickHeaderkey, OrderKey, OrderLineNumber, Lot, Storerkey, Sku, PackKey, UOM, UOMQty, Qty, Loc, ToLoc, ID, CartonGroup, CartonType, DoReplenish, ReplenishZone, DoCartonize, PickMethod, AddWho, EditWho, SeqNo, StatusRequired,fromloc, SelectedCartonType, SelectedCartonID, grosswgt, netwgt, tarewgt, PickContPlacement, status ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,? )",new Object[]{
                           new Object[]{

                                   pickDetailKey,
                                   caseId,
                                   " ",
                                   pickDetailInfo.get("ORDERKEY"),
                                   pickDetailInfo.get("ORDERLINENUMBER"),
                                   lotxLocxIdInfo.get("LOT"),
                                   lotxLocxIdInfo.get("STORERKEY"),
                                   lotxLocxIdInfo.get("SKU"),
                                   lotxLocxIdInfo.get("PACKKEY")
                                   , uom
                                   , taskDetailInfo.get("QTY")
                                   , taskDetailInfo.get("QTY")
                                   , lotxLocxIdInfo.get("LOC")
                                   , pickDetailInfo.get("TOLOC")
                                   , lotxLocxIdInfo.get("ID")
                                   , pickDetailInfo.get("CARTONGROUP")
                                   , pickDetailInfo.get("CARTONTYPE")
                                   , pickDetailInfo.get("DOREPLENISH")
                                   , pickDetailInfo.get("REPLENISHZONE")
                                   , pickDetailInfo.get("DOCARTONIZE")
                                   , pickDetailInfo.get("PICKMETHOD")
                                   ,EHContextHelper.getUser().getUsername()
                                   , EHContextHelper.getUser().getUsername()
                                   , 99999
                                   , pickDetailInfo.get("STATUSREQUIRED")
                                   , lotxLocxIdInfo.get("LOC")
                                   , pickDetailInfo.get("CARTONTYPE")
                                   , caseId
                                   , grosswgt
                                   , netwgt
                                   , tarewgt
                                   , pickDetailInfo.get("PICKCONTPLACEMENT")
                                   , "1", //status: released
                           }
                    });

//
//              EXEDataObject triggerDO = new EXEDataObject();
//              triggerDO.setAttribValue("qty",
//                      taskDetailInfo.get("QTY"));
//              triggerDO.setAttribValue("sku",
//                      lotxLocxIdInfo.get("SKU"));
//              triggerDO.setAttribValue("storerkey",
//                      lotxLocxIdInfo.get("STORERKEY"));
//              triggerDO.setAttribValue("loc",
//                      lotxLocxIdInfo.get("LOC"));
//              triggerDO.setAttribValue("pickdetailkey",
//                      pickDetailKey);
//              triggerDO.setAttribValue("orderkey",
//                      pickDetailInfo.get("ORDERKEY"));
//              triggerDO.setAttribValue("orderlinenumber",
//                      pickDetailInfo.get("ORDERLINENUMBER"));
//              triggerDO.setAttribValue("lot",
//                      lotxLocxIdInfo.get("LOT"));
//              triggerDO.setAttribValue("id", lotxLocxIdInfo.get("ID"));
//              triggerDO.setAttribValue("pickmethod",
//                      pickDetailInfo.get("PICKMETHOD"));
//              triggerDO.setAttribValue("uom",
//                          uom);
//              triggerDO.setAttribValue("PACKKEY",
//                      lotxLocxIdInfo.get("PACKKEY"));
////
//              context.theEXEDataObjectStack.push(triggerDO);
//
//              context.theSQLMgr.searchTriggerLibrary("PickDetail")).postInsertFire();
//
//              context.theSQLMgr.searchTriggerLibrary("TaskDetail")).preInsertFire();

              Map<String, String> locInfo = Loc.findById( lotxLocxIdInfo.get("LOC"), true);

              String newTaskDetailKey = KeyGen.getKey("TASKDETAILKEY", 10);

              java.sql.Date currentDate = UtilHelper.getCurrentSqlDate();

              DBHelper.executeUpdate(" INSERT INTO TASKDETAIL ( TaskDetailKey, TaskType, StorerKey, Sku, Lot, UOM, UOMQTY, Qty, FromLoc, LogicalFromLoc, FromID, ToLoc, ToId, SourceType, SourceKey, WaveKey, CaseId, OrderKey, OrderLineNumber, PickDetailKey, PickMethod, AddWho, EditWho, Door, Route, Stop, Putawayzone,STATUS,USERKEY,REASONKEY,STARTTIME,ENDTIME ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,? )",
                new Object[]{
               newTaskDetailKey
              , "PK"
              , lotxLocxIdInfo.get("STORERKEY")
              , lotxLocxIdInfo.get("SKU")
              , lotxLocxIdInfo.get("LOT")
              , uom
              , taskDetailInfo.get("QTY")
              , taskDetailInfo.get("QTY")
              , lotxLocxIdInfo.get("LOC")
              , locInfo.get("LOGICALLOCATION")
              , lotxLocxIdInfo.get("ID")
              , pickDetailInfo.get("TOLOC")
              , lotxLocxIdInfo.get("ID")
              , "PICKDETAIL"
              , pickDetailKey
              , pickDetailInfo.get("WAVEKEY")
              , caseId
              , pickDetailInfo.get("ORDERKEY")
              , pickDetailInfo.get("ORDERLINENUMBER")
              , pickDetailKey
              , pickDetailInfo.get("PICKMETHOD")
              , EHContextHelper.getUser().getUsername()
              , EHContextHelper.getUser().getUsername()
              , pickDetailInfo.get("DOOR")
              , pickDetailInfo.get("ROUTE")
              , pickDetailInfo.get("STOP")
              , locInfo.get("PUTAWAYZONE")
              //Add following fields for occupy the task immediately
              , "3" //status
              , EHContextHelper.getUser().getUsername()
              , " "//reasonekey

              , currentDate
              , currentDate});


//              triggerDO = new EXEDataObject();
//              triggerDO.setConstraintItem("taskdetailkey"), newTaskDetailKey));
//              triggerDO.setAttribValue("taskdetailkey"), newTaskDetailKey));
//
//              context.theEXEDataObjectStack.push(triggerDO);
//              context.theSQLMgr.searchTriggerLibrary("TaskDetail")).postInsertFire();
//              context.theSQLMgr.transactionCommit();
              return newTaskDetailKey;
        }catch (Exception e){
//            context.theSQLMgr.transactionAbort();
            throw e;
        }
        finally{
            try	{}	catch (Exception e1) {		}
        }
    }

}
