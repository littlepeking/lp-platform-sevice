package com.enhantec.wms.backend.outbound.picking;

import com.enhantec.wms.backend.common.outbound.PickDetail;
import com.enhantec.wms.backend.common.task.TaskDetail;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.ServiceHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.base.code.CDOrderType;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.outbound.OutboundUtils;
import com.enhantec.wms.backend.utils.common.*;
import com.enhantec.wms.backend.utils.print.Labels;
import com.enhantec.wms.backend.utils.print.PrintHelper;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import static com.enhantec.wms.backend.utils.common.UtilHelper.str2Decimal;
import static com.enhantec.wms.backend.outbound.OutboundUtils.checkQtyIsAvailableInLotxLocxId;
import static com.enhantec.wms.backend.outbound.picking.PickUtil.checkIfSplitTimesOverLimit;


public class ClusterPickingConfirm extends WMSBaseService {

    /**
     * JOHN 20201115
     *
     --注册方法
     DELETE FROM SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME= 'EHClusterPickingConfirm';
     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHClusterPickingConfirm', 'com.enhantec.sce.outbound.order.picking', 'enhantec', 'ClusterPickingConfirm', 'TRUE', 'JOHN',  'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,taskdetailkey,grosswgt,netwgt,tarewgt,uom,toid,snlist,esignaturekey,printer','0.10','0');
-
     */
    //TMTPKP1S3 paramters:
    //sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,
    // ttm,taskdetailkey,storerkey,sku,fromloc,fromchkdigit,fromid,toloc,tochkdigit,toid,lot,qty,caseid,packkey,uom,reason,cartongroup,cartontype,transactionkey,position
    //example: TTM,0000000139,KL,0000000086,10-411,,NB20111004001,PICKTO,,,0000000086,,0000000125,SKU001,EA,1,STD,SMALL,,,

//
//    private static final ILogger SCE_LOGGER = SCELoggerFactory.getInstance("ClusterPickingConfirm.class");


    private static final Map<String, Object> keyLocks = new ConcurrentHashMap<>();

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {


        String userid = EHContextHelper.getUser().getUsername();
        String taskdetailkey = serviceDataHolder.getInputDataAsMap().getString("taskdetailkey");
        String uom = serviceDataHolder.getInputDataAsMap().getString("uom");
        String grossWgt = serviceDataHolder.getInputDataAsMap().getString("grosswgt");
        String netwgt = serviceDataHolder.getInputDataAsMap().getString("netwgt");
        String tarewgt = serviceDataHolder.getInputDataAsMap().getString("tarewgt");
        String toId = serviceDataHolder.getInputDataAsMap().getString("toid");//唯一码拣货至箱号
        String snListStr = serviceDataHolder.getInputDataAsMap().getString("snlist");//唯一码列表

        String esignaturekey = serviceDataHolder.getInputDataAsMap().getString("esignaturekey");
        String printer = serviceDataHolder.getInputDataAsMap().getString("printer");



        try {
            boolean printLabel = false;

            if (UtilHelper.isEmpty(taskdetailkey)) ExceptionHelper.throwRfFulfillLogicException("拣货任务号不允许为空");

            //context.theSQLMgr.transactionBegin();


            synchronized (keyLocks.computeIfAbsent(taskdetailkey, k -> new Object())) {

                Map<String, String> taskDetailRecord = getTaskInfo( taskdetailkey);

                String orderKey = taskDetailRecord.get("ORDERKEY");
                String orderLineNumber = taskDetailRecord.get("ORDERLINENUMBER");
                String pickdetailKey = taskDetailRecord.get("PICKDETAILKEY");
                String orderType = taskDetailRecord.get("TYPE");

                Map<String,String>  fromIdHashMap = LotxLocxId.findById(taskDetailRecord.get("FROMID"),true);

                //检查状态，避免重复提交造成的死锁。
                if(!taskDetailRecord.get("STATUS").equals("9")) {

                    BigDecimal grossWgtDecimal = str2Decimal(grossWgt,"毛重",false);
                    BigDecimal uomQtyTobePicked = str2Decimal(netwgt,"净重",false);
                    BigDecimal tareWgtDecimal = str2Decimal(tarewgt,"皮重",false);
                    if (grossWgtDecimal.subtract(uomQtyTobePicked).compareTo(tareWgtDecimal) != 0) {
                        ExceptionHelper.throwRfFulfillLogicException("输入的毛皮净重不匹配");
                    }

                    //String uomCode = UOM.getUOMCode( lotxLocxIdHashMap.get("PACKKEY"), uom);

                    BigDecimal taskQty = new BigDecimal(taskDetailRecord.get("QTY"));
                    BigDecimal lpnQty = new BigDecimal(fromIdHashMap.get("QTY"));
                    BigDecimal availQty = new BigDecimal(fromIdHashMap.get("AVAILABLEQTY"));

                    String stdUom = UOM.getStdUOM( fromIdHashMap.get("PACKKEY"));

                    BigDecimal stdGrossWgtDecimal = UOM.UOMQty2StdQty( fromIdHashMap.get("PACKKEY"), uom, grossWgtDecimal);
                    BigDecimal stdTareWgtDecimal = UOM.UOMQty2StdQty( fromIdHashMap.get("PACKKEY"), uom, tareWgtDecimal);
                    BigDecimal stdQtyTobePicked = UOM.UOMQty2StdQty( fromIdHashMap.get("PACKKEY"), uom, uomQtyTobePicked);


                    String[] snList;
                    if(!UtilHelper.isEmpty(snListStr)){
                        snList = snListStr.split(";;;");
                    }else {
                        snList = new String[]{};
                    }

                    if(snList.length!= 0 && snList.length != stdQtyTobePicked.intValue()){
                        ExceptionHelper.throwRfFulfillLogicException("提交的唯一码的个数和净重/数量不一致");
                    }


                    if(!CDOrderType.isAllowOverPick(orderType) && UtilHelper.decimalCompare(stdQtyTobePicked,taskQty)>0)
                        ExceptionHelper.throwRfFulfillLogicException("不允许超额拣货");

                    OutboundUtils.checkQtyIsAvailableInIDNotes( taskDetailRecord.get("FROMID"), str2Decimal(stdQtyTobePicked, "拣货量", false));

                    checkQtyIsAvailableInLotxLocxId( taskDetailRecord.get("FROMID"), str2Decimal(stdQtyTobePicked, "拣货量", false), taskQty);

                    boolean isPickFullLPN = stdQtyTobePicked.compareTo(lpnQty) == 0;

                    if (!isPickFullLPN) checkIfSplitTimesOverLimit( taskDetailRecord.get("FROMID"));

                    ///对于固体物料，如果存在非整桶拣货的情况，拆分拣货明细后再做拣货。
                    //其他物料走正常的超拣短拣逻辑
                    //注意：固体物料虽然受短拣影响进行任务拆分，但和是否进行整容器发货是没有关系的。超拣和短拣都可能不是整容器发货。
                    //超拣短拣判断的是拣货量是否等于任务量，是否整托盘是看拣货量是否等于当前的LPN量
                    Map<String, String> skuHashMap = SKU.findById( taskDetailRecord.get("SKU"), true);

                    boolean isShortPick = stdQtyTobePicked.compareTo(taskQty) < 0;
                    if (CDOrderType.isSplitTaskAfterShortPick(orderType) && isShortPick) {
                        //如果触发固体拣货的短拣，即当拣货量小于任务量，系统自动重置当前任务量为实际拣货量(避免后面再次触发正常流程的短拣逻辑)，同时分拆当前拣货明细和任务
                        taskQty = stdQtyTobePicked;
                        splitPickDetailAndTask( taskdetailkey, stdQtyTobePicked);
                        //reload task detail as task already changed.
                        taskDetailRecord = getTaskInfo( taskdetailkey);

                    }

                    //populatingprocessData.getInputDataMap()
                     taskDetailRecord.entrySet().stream().forEach(e -> {

                         if(!"toid".equalsIgnoreCase(e.getKey()) &&
                            !"grosswgt".equalsIgnoreCase(e.getKey()) &&
                            !"netwgt".equalsIgnoreCase(e.getKey()) &&
                            !"uom".equalsIgnoreCase(e.getKey()) &&
                            !"tarewgt".equalsIgnoreCase(e.getKey())) serviceDataHolder.getInputDataAsMap().setAttribValue(e.getKey(), e.getValue());
                     });

                    ///////////////////
                    //正常的拣货逻辑

                    //非整箱或者是整箱但拣货到LPN和原箱号不同且不为空，则需要拆分
                    if (!isPickFullLPN
                            || (isPickFullLPN && !UtilHelper.isEmpty(toId) && !taskDetailRecord.get("FROMID").equals(toId))) {

                        toId = IDNotes.splitWgtById( stdGrossWgtDecimal, stdQtyTobePicked, stdTareWgtDecimal, grossWgt, netwgt, tarewgt, uom, taskDetailRecord.get("FROMID"),toId, taskDetailRecord.get("ORDERKEY"),false);

                        Map<String,String> fieldsToBeUpdate = new HashMap<>();
                        fieldsToBeUpdate.put("LASTSHIPPEDLOC", taskDetailRecord.get("FROMLOC")); //该ID最后一次的拣货自库位
//                        fieldsToBeUpdate.put("LASTLOC", fromIdHashMap.get("LOC")); //该ID的上一个库位
                        fieldsToBeUpdate.put("LASTID", fromIdHashMap.get("ID")); //该ID的上一个ID
                        IDNotes.update( toId, fieldsToBeUpdate);
                        List<String> notPrintLpnLabelOrderTypes = CDSysSet.getNotPrintLpnLabelOrderTypes();
                        if(IDNotes.isLpnOrBoxId(toId)) {
                            if(null == notPrintLpnLabelOrderTypes || !notPrintLpnLabelOrderTypes.contains(orderType)) {
                                printLabel = true;
                                PrintHelper.printLPNByIDNotes( toId, Labels.LPN, printer, "1", "拣货标签");
                            }
                        }
                        //如果该ID的标签由于固体多次分装的情况发生，已经存在，则应删除已存在的物料剩余量标签的打印任务（因为再次分装后，原待打印的物料剩余量标签的数量已经不正确），并新建打印任务
                        if(CDSysSet.enableLabelWgt()) {
                            printLabel = true;
                            PrintHelper.removePrintTaskByIDNotes( Labels.LPN_UI_SY, taskDetailRecord.get("FROMID"));
                            PrintHelper.printLPNByIDNotes( taskDetailRecord.get("FROMID"), Labels.LPN_UI_SY, printer, "1", "物料剩余量标签");
                        }

                    } else {
                        //(FULL LPN) AND ((NEW LPN IS NULL) OR (NEW LPN = OLD LPN))

                        //更新标签信息
                        Map<String,String> fieldsToBeUpdate = new HashMap<String,String>();

                        fieldsToBeUpdate.put("GROSSWGTLABEL", grossWgt);//原毛重标签量
                        fieldsToBeUpdate.put("TAREWGTLABEL", tarewgt);//原皮重标签量
                        fieldsToBeUpdate.put("NETWGTLABEL", netwgt);//原净重标签量
                        fieldsToBeUpdate.put("UOMLABEL", uom);//采集读取的计量单位
                        fieldsToBeUpdate.put("LASTSHIPPEDLOC", taskDetailRecord.get("FROMLOC")); //该ID最后一次的拣货自库位
//                        fieldsToBeUpdate.put("LASTLOC", fromIdHashMap.get("LOC")); //该ID的上一个库位
                        fieldsToBeUpdate.put("LASTID", fromIdHashMap.get("ID")); //该ID的上一个ID
                        fieldsToBeUpdate.put("ORDERKEY",orderKey);
                        IDNotes.update( taskDetailRecord.get("FROMID"), fieldsToBeUpdate);

                        if (CDOrderType.isSplitTaskAfterShortPick(orderType) && CDSysSet.enableLabelWgt()) {

                            //删除由于固体分装产生的物料剩余量标签的打印任务，因为本次拣货将该容器所有数量全部拣出，不需要再打印剩余量标签
                            PrintHelper.removePrintTaskByIDNotes( Labels.LPN_UI_SY, taskDetailRecord.get("FROMID"));
                            //对于固体分装，即使是整容器发货，也必须打印拣货标签
                            //PrintHelper.printLPNByIDNotes( taskDetailRecord.get("FROMID"), Labels.LPN, printer, "1", "拣货标签");

                        }

                        toId = taskDetailRecord.get("FROMID");
                    }

                    //use std qty directly
                    serviceDataHolder.getInputDataAsMap().setAttribValue("toid", toId);
                    serviceDataHolder.getInputDataAsMap().setAttribValue("qty", stdQtyTobePicked.toPlainString());
                    //processData.getInputDataMap().setAttribValue("uom", uomCode);
                    serviceDataHolder.getInputDataAsMap().setAttribValue("uom", "6");
                    String reasonCode = PickUtil.getPickReason(taskQty, stdQtyTobePicked);

                    serviceDataHolder.getInputDataAsMap().setAttribValue("reason", reasonCode);
                    serviceDataHolder.getInputDataAsMap().setAttribValue("fromchkdigit", "");
                    serviceDataHolder.getInputDataAsMap().setAttribValue("tochkdigit", "");
                    serviceDataHolder.getInputDataAsMap().setAttribValue("transactionkey", "");
                    serviceDataHolder.getInputDataAsMap().setAttribValue("position", "");

                    //((SSADBSession)context).setInsideTransaction(false);
                    //((SSADBSession)context).createTransaction();
//                    DatabaseTransaction trans = ((SSADBSession) context).createStandaloneTransaction();
                    //context.theSQLMgr.transBeginIndependent();

                    //执行原生拣货
                    ServiceDataMap res = ServiceHelper.executeService( "NSPRFTPK01C", serviceDataHolder);

                    //唯一码LOTXID拣货逻辑(使用NSPRFTPK01C进行整容器拣货时，不需要手工更新唯一码库存)
                    if(!isPickFullLPN) {
                        PickUtil.pickSerialNumber( orderKey, orderLineNumber, pickdetailKey, fromIdHashMap, toId, snList, res.getString("itrnkey"));
                    }
                    //////////////////////////////////
                    //更新拣货明细的自定义字段1为开封，用于反馈赋码系统时确定是否使用箱号还是唯一码
                    Map<String,String> toIDNotesHashMap = IDNotes.findById( toId,true);
                    DBHelper.executeUpdate( "UPDATE PICKDETAIL SET PDUDF1 = ? WHERE PICKDETAILKEY = ? "
                            , new Object[]{
                                    toIDNotesHashMap.get("ISOPENED"),
                                    pickdetailKey}
                    );


                    if(CDOrderType.isReduceOrderQtyAfterShortPick(orderType)) {

                        if ("SHORT".equals(reasonCode)) {

                            DBHelper.executeUpdate( "UPDATE ORDERDETAIL SET OpenQty = OpenQty - ?, EditWho = ?, EditDate = ? WHERE Orderkey = ? AND OrderLineNumber = ?"
                                    , new Object[]{
                                            taskQty.subtract(stdQtyTobePicked).toPlainString(),
                                            userid,
                                            new Date(Calendar.getInstance().getTimeInMillis()),
                                            orderKey,
                                            orderLineNumber}
                            );

                            ServiceDataMap orderDetailStatusParam = getOrderDetailStatus( orderKey, orderLineNumber, null, null);
                            String newOrderDetailStatus = orderDetailStatusParam.getString("newStatus");


                            DBHelper.executeUpdate( "UPDATE ORDERDETAIL SET Status = ? , EditWho = ?, EditDate = ? WHERE Orderkey = ? AND OrderLineNumber = ?"
                                    , new Object[]{
                                            newOrderDetailStatus,
                                            userid,
                                            UtilHelper.getCurrentSqlDate(),
                                            orderKey,
                                            orderLineNumber}
                            );

                            ServiceDataMap orderStatusParam = getOrderStatus( orderKey, null, null, null);

                            DBHelper.executeUpdate( "UPDATE ORDERS SET Status = ? , EditWho = ?, EditDate = ? WHERE Orderkey = ? "
                                    , new Object[]{
                                            orderStatusParam.getString("newStatus"),
                                            userid,
                                            UtilHelper.getCurrentSqlDate(),
                                            orderKey
                                    });
                        }

                    }
                    //根据拣货LPN内拣货后是否有开封SN更改标记
                    if (toId.equalsIgnoreCase(fromIdHashMap.get("ID"))){//非整LPN
                        ChangeOpenSnMarksHelper.changeOpenSnMarksBYLpn(skuHashMap.get("SKU"),toId,fromIdHashMap.get("ID"));
                    }else {
                        ChangeOpenSnMarksHelper.changeOpenSnMarksBYLpn(skuHashMap.get("SKU"),toId);
                    }
                    Udtrn UDTRN = new Udtrn();
                    if(!UtilHelper.isEmpty(esignaturekey)){
                        String[] split = esignaturekey.split(":");
                        if(split.length > 1) {
                            UDTRN.EsignatureKey = split[0];
                            UDTRN.EsignatureKey1 = split[1];
                        }
                    }else {
                            UDTRN.EsignatureKey = esignaturekey;
                    }
                    UDTRN.FROMTYPE = "订单任务拣货";
                    UDTRN.FROMTABLENAME = "TASKDETAIL";
                    UDTRN.FROMKEY = taskdetailkey;
                    UDTRN.FROMKEY = taskdetailkey;
                    UDTRN.FROMKEY1 = taskDetailRecord.get("ORDERKEY");
                    UDTRN.FROMKEY2 = taskDetailRecord.get("PICKDETAILKEY");
                    UDTRN.FROMKEY3 = taskDetailRecord.get("FROMID");
                    UDTRN.TITLE01 = "订单号";
                    UDTRN.CONTENT01 = taskDetailRecord.get("ORDERKEY");
                    UDTRN.TITLE02 = "拣货明细号";
                    UDTRN.CONTENT02 = taskDetailRecord.get("PICKDETAILKEY");
                    UDTRN.TITLE03 = "物料代码";
                    UDTRN.CONTENT03 = taskDetailRecord.get("SKU");
                    UDTRN.TITLE04 = "拣货容器号";
                    UDTRN.CONTENT04 = taskDetailRecord.get("FROMID");
                    UDTRN.TITLE05 = "拣货到容器号";
                    UDTRN.CONTENT05 = toId;
                    UDTRN.TITLE06 = "预期拣货数量";
                    UDTRN.CONTENT06 = taskQty.toPlainString();
                    UDTRN.TITLE07 = "实际拣货数量";
                    UDTRN.CONTENT07 = stdQtyTobePicked.toPlainString();


                    //
                    UDTRN.Insert( EHContextHelper.getUser().getUsername());
//                    ((SsaWrappedConnection) conn).getConnection().commit();

                }
            }

            ServiceDataMap outDO = new ServiceDataMap();

            Map<String, String> taskDetailHashMap = TaskDetail.findById( taskdetailkey, true);
            outDO.setAttribValue("status", taskDetailHashMap.get("STATUS"));
            outDO.setAttribValue("PRINTLABEL",printLabel);
            serviceDataHolder.setOutputData(outDO);

        }catch (Exception e){
            //出错回滚非原生逻辑未提交的部分
//            try { conn.rollback(); }	catch (Exception e1) {}
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());

        } finally {
        }


    }

    public void splitPickDetailAndTask( String taskdetailkey, BigDecimal stdQtyTobePicked) throws SQLException {

        PreparedStatement qqPrepStmt = null;

            //context.theSQLMgr.transactionBegin();

            //((SSADBSession)context).createTransaction();
            //DatabaseTransaction trans = ((SSADBSession)context).createStandaloneTransaction();
//            context.theSQLMgr.transBeginIndependent();
            //******************
            // MUST GET CONNECTION AFTER transBeginIndependent, OTHTERWISE THE TRANSACTION WILL NOT DO REAL COMMIT AFTER transCommitIndependent()
            
            //*****************

            Map<String, String> originalTaskDetailInfo = TaskDetail.findById( taskdetailkey, true);
            Map<String, String> originalPickDetailInfo = PickDetail.findByPickDetailKey( originalTaskDetailInfo.get("PICKDETAILKEY"), true);

            DBHelper.executeUpdate( "UPDATE PICKDETAIL SET QTY = ? , UOMQTY = ? WHERE PICKDETAILKEY = ?",
                    new Object[]{
                            //CANNOT use decimalData as qty will become 0 if it is very small like 0.001
                            stdQtyTobePicked.toPlainString(),
                            stdQtyTobePicked.toPlainString(),
                            originalTaskDetailInfo.get("PICKDETAILKEY")
                    });

            DBHelper.executeUpdate( "UPDATE TASKDETAIL SET QTY = ? , UOMQTY = ? WHERE TASKDETAILKEY = ?",
                    new Object[]{
                            stdQtyTobePicked.toPlainString(),
                            stdQtyTobePicked.toPlainString(),
                            taskdetailkey
                    });



        BigDecimal taskQty = new BigDecimal(originalTaskDetailInfo.get("QTY"));
        BigDecimal qtyDiff = taskQty.subtract(stdQtyTobePicked);


        String pickDetailKey = IdGenerationHelper.getNCounterStrWithLength("PICKDETAILKEY", 10);
        String caseId = IdGenerationHelper.getNCounterStrWithLength("CARTONID", 10);


        DBHelper.executeUpdate(" INSERT INTO PICKDETAIL ( PickDetailKey, CaseID, PickHeaderkey, OrderKey, OrderLineNumber, Lot, Storerkey, Sku, PackKey, UOM, UOMQty, Qty, Loc, ToLoc, ID, CartonGroup, CartonType, DoReplenish, ReplenishZone, DoCartonize, PickMethod, AddWho, EditWho, SeqNo, StatusRequired,fromloc, SelectedCartonType, SelectedCartonID, grosswgt, netwgt, tarewgt, PickContPlacement, status, wavekey) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,? )",

            new Object[]{ pickDetailKey
            , caseId
            , " "
            , originalPickDetailInfo.get("ORDERKEY")
            , originalPickDetailInfo.get("ORDERLINENUMBER")
            , originalPickDetailInfo.get("LOT")
            , originalPickDetailInfo.get("STORERKEY")
            , originalPickDetailInfo.get("SKU")
            , originalPickDetailInfo.get("PACKKEY")
            //As current allocation strategy is the only allocate using standard UOM, so here set uomQty without need UOM conversion
            , originalPickDetailInfo.get("UOM")
            , qtyDiff.toPlainString()
            , qtyDiff.toPlainString()
            , originalPickDetailInfo.get("LOC")
            , originalPickDetailInfo.get("TOLOC")
            , originalPickDetailInfo.get("ID")
            , originalPickDetailInfo.get("CARTONGROUP")
            , originalPickDetailInfo.get("CARTONTYPE")
            , originalPickDetailInfo.get("DOREPLENISH")
            , originalPickDetailInfo.get("REPLENISHZONE")
            , originalPickDetailInfo.get("DOCARTONIZE")
            , originalPickDetailInfo.get("PICKMETHOD")
            , EHContextHelper.getUser().getUsername()
            , EHContextHelper.getUser().getUsername()
            , 99999
            , originalPickDetailInfo.get("STATUSREQUIRED")
            , originalPickDetailInfo.get("FROMLOC")
            , originalPickDetailInfo.get("SELECTEDCARTONTYPE")
            , caseId
            , originalPickDetailInfo.get("GROSSWGT")
            , originalPickDetailInfo.get("NETWGT")
            , originalPickDetailInfo.get("TAREWGT")
            , "0"//PICKCONTPLACEMENT
            , "1" //status: released
            , originalPickDetailInfo.get("WAVEKEY")});

        String newTaskDetailKey = IdGenerationHelper.getNCounterStrWithLength("TASKDETAILKEY", 10);

            java.sql.Date currentDate = UtilHelper.getCurrentSqlDate();
            DBHelper.executeUpdate(" INSERT INTO TASKDETAIL ( TaskDetailKey, TaskType, StorerKey, Sku, Lot, UOM, UOMQTY, Qty, FromLoc, LogicalFromLoc, FromID, ToLoc, ToId, SourceType, SourceKey, WaveKey, CaseId, OrderKey, OrderLineNumber, PickDetailKey, PickMethod, AddWho, EditWho, Door, Route, Stop, Putawayzone,STATUS,USERKEY,REASONKEY,STARTTIME,ENDTIME, PICKCONTPLACEMENT ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,? )",

            new Object[]{ newTaskDetailKey
            , "PK"
            , originalTaskDetailInfo.get("STORERKEY")
            , originalTaskDetailInfo.get("SKU")
            , originalTaskDetailInfo.get("LOT")
            , originalTaskDetailInfo.get("UOM")
            //As current allocation strategy is the only allocate using standard UOM, so here set uomQty without need UOM conversion
            , qtyDiff.toPlainString()
            , qtyDiff.toPlainString()
            , originalTaskDetailInfo.get("FROMLOC")
            , originalTaskDetailInfo.get("LOGICALFROMLOC")
            , originalTaskDetailInfo.get("FROMID")
            , originalTaskDetailInfo.get("TOLOC")
            , originalTaskDetailInfo.get("TOID")
            , originalTaskDetailInfo.get("SOURCETYPE")
            , pickDetailKey
            , originalTaskDetailInfo.get("WAVEKEY")
            , caseId
            , originalTaskDetailInfo.get("ORDERKEY")
            , originalTaskDetailInfo.get("ORDERLINENUMBER")
            , pickDetailKey
            , originalTaskDetailInfo.get("PICKMETHOD")
            , EHContextHelper.getUser().getUsername()
            , EHContextHelper.getUser().getUsername()
            , originalTaskDetailInfo.get("DOOR")
            , originalTaskDetailInfo.get("ROUTE")
            , originalTaskDetailInfo.get("STOP")
            , originalTaskDetailInfo.get("PUTAWAYZONE")
            , "0" //status
            , " "//USERKEY设为空，否则在状态为0时获取不到任务
            , " "//reasonkey

            , currentDate
            , currentDate
            , "0"});//PICKCONTPLACEMENT 将新建的分拆拣货任务的任务优先级设为最高，下一次拣货优先进行

            //context.theSQLMgr.transactionCommit();
            //trans.commit();
//            context.theSQLMgr.transCommitIndependent();

    }


    private Map<String, String> getTaskInfo( String taskdetailkey) {
        return DBHelper.getRecord(
                "SELECT O.TYPE, TD.STATUS, TD.LOT,TD.QTY,TD.SKU,TD.STORERKEY,TD.FROMLOC,TD.FROMID,TD.TOID,TD.TOLOC,TD.CASEID,TD.UOM,P.CARTONGROUP ,P.CARTONTYPE ,P.PACKKEY, P.ORDERKEY,P.ORDERLINENUMBER, P.PICKDETAILKEY " +
                        " FROM TASKDETAIL TD, PICKDETAIL P,ORDERS O " +
                        " WHERE TD.PICKDETAILKEY = P.PICKDETAILKEY AND O.ORDERKEY = TD.ORDERKEY AND TASKDETAILKEY = ? ",
                new Object[]{taskdetailkey}, "拣货和任务明细");
    }


    public ServiceDataMap getOrderDetailStatus( String pOrderkey, String pOrderLineNumber, String pNewStatus, String pDetailHistoryFlag) {
        try {
            //logger.debug("starting getOrderDetailStatus( String pOrderkey, TextData pOrderLineNumber, DBSession context, String pNewStatus, String pDetailHistoryFlag)");
            Integer pickstotal = null;
            Integer released = null;
            Integer inpicking = null;
            Integer packed = null;
            Integer instaged = null;
            Integer loaded = null;
            Integer dpreleased = null;
            Integer dpinpicking = null;
            String orderdetailstatus = null;
            String maxcode = null;
            double SHIPPEDQTY = 0.0D;
            double OPENQTY = 0.0D;
            double QTYPREALLOCATED = 0.0D;
            double QTYALLOCATED = 0.0D;
            double QTYPICKED = 0.0D;
            int ODSTATUS = 0;
            int ISSUBSTITUTE = 0;
            String wpReleased = null;
            int qqRowCount = 0;
            //
            String pickdetailstatus = null;
            String locType = null;
            String dropid = null;
            String waveKey = null;
            int ordreleased = 0;
            int inpick = 0;
            int ordpacked = 0;
            int ordinstaged = 0;
            int ordloaded = 0;

            List<Map<String,String>>  res = DBHelper.executeQuery( " SELECT a.status, b.locationtype, a.dropid, a.wavekey FROM PICKDETAIL a, LOC b WHERE b.Loc = a.Loc AND a.OrderKey = ? AND a.OrderLineNumber = ?",
                new Object[]{ pOrderkey, pOrderLineNumber});

                    for(Map<String,String> r : res) {
                        pickdetailstatus = r.get("status");
                        locType = r.get("locationtype");
                        dropid = r.get("dropid");
                        waveKey = r.get("wavekey");
                            ++ordreleased;
                    }

                    if (pickdetailstatus.equals("2") || pickdetailstatus.equals("3")) {
                        ++inpick;
                    }

                    if (pickdetailstatus.equals("6")) {
                        ++ordpacked;
                    }

                    if (locType.equals("STAGED")) {
                        ++ordinstaged;
                    }

                    if (pickdetailstatus.equals("8")) {
                        ++ordloaded;
                    }


                pickstotal= qqRowCount;
                released=ordreleased;
                inpicking=inpick;
                packed=ordpacked;
                instaged=ordinstaged;
                loaded=ordloaded;


            Map<String,Object>  resStatus = DBHelper.getRawRecord(" SELECT SUM ( CASE WHEN b.status = '0' AND a.wavekey <> ' ' THEN 1 ELSE 0 END ) RELEASED, SUM ( CASE WHEN b.status >= '2' AND b.status <= '3' THEN 1 ELSE 0 END ) INPICKING FROM DEMANDALLOCATION a, TASKDETAIL b WHERE b.Sourcekey = a.DemandKey AND b.SourceType = 'DP' AND b.status = '0' AND a.OrderKey = ? AND a.OrderLineNumber = ?",
                new Object[]{pOrderkey, pOrderLineNumber});

            dpreleased = (int) resStatus.get("RELEASED");
            dpinpicking = (int) resStatus.get("RELEASED");

            Map<String,Object>  resOrderLine = DBHelper.getRawRecord(" SELECT SHIPPEDQTY, OPENQTY, QTYPREALLOCATED, QTYALLOCATED, QTYPICKED, ISSUBSTITUTE, WPReleased, STATUS FROM OrderDetail WHERE OrderKey = ? AND OrderLineNumber = ?",
               new Object[]{ pOrderkey, pOrderLineNumber});

                    SHIPPEDQTY = (double) resOrderLine.get("SHIPPEDQTY");
                    OPENQTY =  (double) resOrderLine.get("OPENQTY");
                    QTYPREALLOCATED =  (double) resOrderLine.get("QTYPREALLOCATED");
                    QTYALLOCATED = (double) resOrderLine.get("QTYALLOCATED");
                    QTYPICKED =  (double) resOrderLine.get("QTYPICKED");
                    ISSUBSTITUTE =  (int) resOrderLine.get("ISSUBSTITUTE");
                    wpReleased =  (String) resOrderLine.get("WPReleased");
                    ODSTATUS = (int) resOrderLine.get("STATUS");


            if (pickstotal== null) {
                pickstotal = 0;
            }

            if (released== null) {
                released = 0;
            }

            if (inpicking== null) {
                inpicking = 0;
            }

            if (packed== null) {
                packed = 0;
            } else if (QTYPICKED == 0.0D) {
                packed = 0;
            }

            if (instaged== null) {
                instaged = 0;
            }

            if (loaded== null) {
                loaded = 0;
            }

            if (dpreleased== null) {
                dpreleased = 0;
            }

            if (dpinpicking== null) {
                dpinpicking = 0;
            }

            if (SHIPPEDQTY > 0.0D && OPENQTY == 0.0D) {
                pNewStatus = "95";
            } else if (SHIPPEDQTY > 0.0D && OPENQTY > 0.0D && OPENQTY - QTYPREALLOCATED - QTYALLOCATED == 0.0D && (ODSTATUS >= 22 && ODSTATUS <= 29 || released > 0)) {
                pNewStatus = "27";
            } else if (SHIPPEDQTY > 0.0D && OPENQTY > 0.0D && OPENQTY - QTYPREALLOCATED - QTYALLOCATED == 0.0D) {
                pNewStatus = "92";
            } else if (loaded == pickstotal && pickstotal > 0 && QTYPICKED > 0.0D && OPENQTY - QTYPICKED == 0.0D) {
                pNewStatus = "88";
            } else if (SHIPPEDQTY > 0.0D && loaded > 0) {
                pNewStatus = "92";
            } else if (loaded > 0 && QTYPICKED > 0.0D && OPENQTY - QTYPICKED == 0.0D) {
                pNewStatus = "82";
            } else if (instaged == pickstotal && instaged > 0 && SHIPPEDQTY == 0.0D && OPENQTY - QTYPICKED == 0.0D && OPENQTY > 0.0D && QTYPICKED > 0.0D) {
                pNewStatus = "75";
            } else if (packed == pickstotal && packed > 0 && QTYPICKED == OPENQTY) {
                pNewStatus = "68";
            } else if (SHIPPEDQTY > 0.0D && packed > 0) {
                pNewStatus = "92";
            } else if (packed > 0) {
                pNewStatus = "61";
            } else if (SHIPPEDQTY > 0.0D && OPENQTY - QTYPICKED == 0.0D && OPENQTY > 0.0D && QTYPICKED > 0.0D) {
                pNewStatus = "57";
            } else if (SHIPPEDQTY == 0.0D && OPENQTY - QTYPICKED == 0.0D && OPENQTY > 0.0D && QTYPICKED > 0.0D) {
                pNewStatus = "55";
            } else if (SHIPPEDQTY > 0.0D && OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED >= 0.0D && QTYALLOCATED >= 0.0D && QTYPICKED > 0.0D) {
                pNewStatus = "53";
            } else if (SHIPPEDQTY != 0.0D || !(OPENQTY > 0.0D) || OPENQTY - QTYALLOCATED - QTYPICKED != 0.0D || !(QTYALLOCATED > 0.0D) || !(QTYPICKED > 0.0D) || (ODSTATUS < 22 || ODSTATUS > 29) && released <= 0) {
                if (SHIPPEDQTY == 0.0D && OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED == 0.0D && QTYALLOCATED > 0.0D && QTYPICKED > 0.0D) {
                    pNewStatus = "52";
                } else if (inpicking <= 0 && dpinpicking <= 0) {
                    if (SHIPPEDQTY != 0.0D || !(OPENQTY > 0.0D) || OPENQTY - QTYALLOCATED != 0.0D || released <= 0 && dpreleased <= 0) {
                        if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYPICKED > 0.0D && QTYALLOCATED > 0.0D && (ODSTATUS >= 22 && ODSTATUS <= 29 || released > 0)) {
                            pNewStatus = "25";
                        } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYALLOCATED > 0.0D && SHIPPEDQTY == 0.0D && (released > 0 || dpreleased > 0) && QTYPICKED == 0.0D) {
                            pNewStatus = "22";
                        } else if (SHIPPEDQTY == 0.0D && OPENQTY == 0.0D && QTYPICKED == 0.0D && ISSUBSTITUTE > 0) {
                            pNewStatus = "18";
                        } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED == 0.0D) {
                            pNewStatus = "17";
                        } else if (!(SHIPPEDQTY > 0.0D) || !(OPENQTY - QTYALLOCATED > 0.0D) || QTYPICKED != 0.0D || (ODSTATUS < 22 || ODSTATUS > 29) && released <= 0) {
                            if (SHIPPEDQTY > 0.0D && OPENQTY - QTYALLOCATED > 0.0D && QTYPICKED == 0.0D) {
                                pNewStatus = "16";
                            } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYPICKED > 0.0D && QTYALLOCATED >= 0.0D && (ODSTATUS >= 22 && ODSTATUS <= 29 || ODSTATUS == 51 || released > 0)) {
                                pNewStatus = "25";
                            } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYPICKED > 0.0D && QTYALLOCATED >= 0.0D) {
                                pNewStatus = "15";
                            } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYALLOCATED > 0.0D) {
                                pNewStatus = "14";
                            } else if (OPENQTY > 0.0D && OPENQTY - QTYPREALLOCATED == 0.0D && wpReleased != null && wpReleased.equals("1")) {
                                pNewStatus = "13";
                            } else if (OPENQTY > 0.0D && OPENQTY - QTYPREALLOCATED == 0.0D) {
                                pNewStatus = "12";
                            } else if (OPENQTY > 0.0D && QTYPREALLOCATED > 0.0D) {
                                pNewStatus = "11";
                            } else if (OPENQTY > 0.0D && QTYPREALLOCATED + QTYALLOCATED + QTYPICKED + SHIPPEDQTY == 0.0D) {
                                pNewStatus = "09";
                            } else if (SHIPPEDQTY + OPENQTY == 0.0D) {
                                pNewStatus = "95";
                            } else if (OPENQTY - QTYPREALLOCATED - QTYALLOCATED - QTYPICKED < 0.0D) {
                                pNewStatus = "-2";
                            } else {
                                pNewStatus = "-1";
                            }
                        } else {
                            pNewStatus = "27";
                        }
                    } else {
                        pNewStatus = "29";
                    }
                } else {
                    pNewStatus = "51";
                }
            } else {
                pNewStatus = "25";
            }

            int qqRowCount2 = 0;

            String storerkey = null;
            String sku = null;

            Map<String,String> resOrderLine2 = DBHelper.getRecord(" SELECT Status, Storerkey, SKU FROM OrderDetail WHERE OrderKey = ? AND OrderLineNumber = ?",
                new Object[]{ pOrderkey,pOrderLineNumber});

                    orderdetailstatus = resOrderLine2.get("Status");
                    storerkey =  resOrderLine2.get("Storerkey");
                    sku =  resOrderLine2.get("SKU");

            Map<String,String> resOrderstatussetup = DBHelper.getRecord(" SELECT MAX ( Code ) code FROM Orderstatussetup WHERE Orderflag = '1' AND Detailflag = '1' AND Enabled = '1' AND Code <= ?",
                new Object[]{pNewStatus});
            maxcode = resOrderstatussetup.get("code");


            if (maxcode != null && maxcode.equals("09") && orderdetailstatus != null && orderdetailstatus.compareToIgnoreCase("02") >= 0 && orderdetailstatus != null && orderdetailstatus.compareToIgnoreCase("08") <= 0) {
                pNewStatus = orderdetailstatus;
            } else if (orderdetailstatus == null || !orderdetailstatus.equalsIgnoreCase("68") || !pNewStatus.equalsIgnoreCase("55") && !pNewStatus.equalsIgnoreCase("61")) {
                pNewStatus = maxcode;
            } else {
                pNewStatus = maxcode;
                double qtyPacked = 0.0D;
                double qtyOrdered = 0.0D;
                int qqRowCount6 = 0;
                
                PreparedStatement qqPrepStmt6 = null;
                ResultSet qqResultSet6 = null;

                Map<String,Object> resPackoutdetail = DBHelper.getRawRecord(" SELECT SUM(qtypicked) qtypicked FROM Packoutdetail WHERE Orderkey = ? AND Storerkey =? AND SKU = ?",
                    new Object[]{pOrderkey,storerkey,sku} );

                qtyPacked = (double) resPackoutdetail.get("qtypicked");


                if (qtyPacked > 0.0D) {

                    Map<String,Object> resOrderDetail = DBHelper.getRawRecord(" SELECT sum(Openqty+SHIPPEDQTY) qty FROM Orderdetail WHERE Orderkey = ? AND Storerkey =? AND SKU = ? ",
                            new Object[]{pOrderkey,storerkey,sku} );

                    qtyOrdered = (Integer) resOrderDetail.get("qty");

                    if (qtyPacked == qtyOrdered) {
                        pNewStatus = orderdetailstatus;
                    }
                }
            }

            pDetailHistoryFlag = DBHelper.getValue(" SELECT DetailHistoryFlag FROM ORDERSTATUSSETUP WHERE Code = ?",
                        new Object[]{pNewStatus});


            if ("95".equals(pNewStatus)) {
                String isOrderRequireClose = DBHelper.getValue("SELECT REQUIREORDERCLOSE  FROM ORDERS WHERE OrderKey = ?",
                        new Object[]{pOrderkey});

                if (isOrderRequireClose != null && "1".equals(isOrderRequireClose)) {
                    pNewStatus = "94";
                }
            }

            ServiceDataMap serviceDataMap = new ServiceDataMap();
            serviceDataMap.setAttribValue("newStatus", pNewStatus);
            serviceDataMap.setAttribValue("detailHistoryFlagtatus", pDetailHistoryFlag);

         return serviceDataMap;
        } finally {
            //logger.debug("leaving getOrderDetailStatus( String pOrderkey, TextData pOrderLineNumber, DBSession context, String pNewStatus, String pDetailHistoryFlag)");
        }
    }

    public ServiceDataMap getOrderStatus(String pOrderkey, String pStatus, String pNewStatus, String pLogHistory) {
        try {
            //logger.debug("starting getOrderStatus( String pOrderkey, DBSession context, String pStatus, String pNewStatus, String pLogHistory)");
            String maxOrderDetailStatus = null;
            String maxCode = null;
            int orderDetailCount = 0;
            String maxStatus = null;
            String minStatus = null;
            pNewStatus = "NA";


            Map<String,Object>  res = DBHelper.getRawRecord( " SELECT COUNT ( * ) count, MAX ( Status ) maxStatus, MIN ( Status ) minStatus FROM Orderdetail WHERE Orderkey = ? AND Status <> '18' and ( openqty>0 or shippedqty>0 or qtypreallocated>0 or qtyallocated>0 or qtypicked>0 )",
                new Object[]{ pOrderkey});

            orderDetailCount = (Integer) res.get("count");
            maxStatus = (String) res.get("maxStatus");
            minStatus = (String) res.get("minStatus");



            if (maxStatus != null && maxStatus.equals("99") && minStatus != null && minStatus.equals("99")) {
                maxOrderDetailStatus = "99";
            } else if (maxStatus != null && maxStatus.equals("98") && minStatus != null && minStatus.equals("98")) {
                maxOrderDetailStatus = "98";
            } else if (maxStatus != null && maxStatus.equals("97") && minStatus != null && minStatus.equals("97")) {
                maxOrderDetailStatus = "97";
            } else if (maxStatus != null && maxStatus.equals("96") && minStatus != null && minStatus.equals("96")) {
                maxOrderDetailStatus = "96";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("95") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("99") <= 0 && minStatus != null && minStatus.equals("95")) {
                maxOrderDetailStatus = "95";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("94") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("99") <= 0 && minStatus != null && minStatus.equals("94")) {
                maxOrderDetailStatus = "94";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("92") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("95") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("95") < 0) {
                maxOrderDetailStatus = "92";
            } else if (maxStatus != null && maxStatus.equals("27") && minStatus != null && minStatus.compareToIgnoreCase("27") <= 0) {
                maxOrderDetailStatus = "92";
            } else if (maxStatus != null && maxStatus.equals("16") && minStatus != null && minStatus.compareToIgnoreCase("16") <= 0) {
                maxOrderDetailStatus = "92";
            } else if (maxStatus != null && maxStatus.equals("53") && minStatus != null && minStatus.compareToIgnoreCase("53") <= 0) {
                maxOrderDetailStatus = "92";
            } else if (maxStatus != null && maxStatus.equals("57") && minStatus != null && minStatus.compareToIgnoreCase("57") <= 0) {
                maxOrderDetailStatus = "92";
            } else if (maxStatus != null && maxStatus.equals("88") && minStatus != null && minStatus.equals("88")) {
                maxOrderDetailStatus = "88";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("82") >= 0 && minStatus != null && minStatus.compareToIgnoreCase("88") < 0) {
                maxOrderDetailStatus = "82";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("75") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("78") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("52") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("17") >= 0) {
                maxOrderDetailStatus = "52";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("75") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("78") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("15") <= 0) {
                maxOrderDetailStatus = "52";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("75") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("78") <= 0 && minStatus != null && minStatus.equals("55")) {
                maxOrderDetailStatus = "55";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("75") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("78") <= 0 && minStatus != null && minStatus.equals("68")) {
                maxOrderDetailStatus = "68";
            } else if (maxStatus != null && maxStatus.equals("75") && minStatus != null && minStatus.equals("75")) {
                maxOrderDetailStatus = "75";
            } else if (maxStatus != null && maxStatus.equals("68") && minStatus != null && minStatus.equals("68")) {
                maxOrderDetailStatus = "68";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("61") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("68") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("61") <= 0) {
                maxOrderDetailStatus = "61";
            } else if (maxStatus != null && maxStatus.equals("55") && minStatus != null && minStatus.equals("55")) {
                maxOrderDetailStatus = "55";
            } else if (maxStatus != null && maxStatus.equals("25") && minStatus != null && minStatus.compareToIgnoreCase("25") <= 0) {
                maxOrderDetailStatus = "52";
            } else if (maxStatus != null && maxStatus.equals("15") && minStatus != null && minStatus.compareToIgnoreCase("15") <= 0) {
                maxOrderDetailStatus = "52";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("52") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("55") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("55") < 0 && this.doesOrderDetailStatusExists(pOrderkey, "27")) {
                maxOrderDetailStatus = "92";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("52") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("55") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("55") < 0) {
                maxOrderDetailStatus = "52";
            } else if (maxStatus != null && maxStatus.equals("29") && minStatus != null && minStatus.equals("29")) {
                maxOrderDetailStatus = "29";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("27") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0 && (this.doesOrderDetailStatusExists(pOrderkey, "92") || this.doesOrderDetailStatusExists(pOrderkey, "27") || this.doesOrderDetailStatusExists(pOrderkey, "16"))) {
                maxOrderDetailStatus = "92";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("25") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0 && (this.doesOrderDetailStatusExists(pOrderkey, "52") || this.doesOrderDetailStatusExists(pOrderkey, "25") || this.doesOrderDetailStatusExists(pOrderkey, "15"))) {
                maxOrderDetailStatus = "52";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("22") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0 && this.doesOrderDetailStatusExists(pOrderkey, "16")) {
                maxOrderDetailStatus = "92";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("22") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0 && this.doesOrderDetailStatusExists(pOrderkey, "15")) {
                maxOrderDetailStatus = "52";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("22") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0) {
                maxOrderDetailStatus = "22";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("29") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("51") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("51") <= 0) {
                maxOrderDetailStatus = "29";
            } else if (maxStatus != null && maxStatus.equals("17") && minStatus != null && minStatus.equals("17")) {
                maxOrderDetailStatus = "17";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("14") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("17") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("17") < 0) {
                maxOrderDetailStatus = "14";
            } else if (maxStatus != null && maxStatus.equals("13") && minStatus != null && minStatus.equals("13")) {
                maxOrderDetailStatus = "13";
            } else if (maxStatus != null && maxStatus.equals("12") && minStatus != null && minStatus.equals("12")) {
                maxOrderDetailStatus = "12";
            } else if (maxStatus != null && maxStatus.equals("12") && minStatus != null && minStatus.compareToIgnoreCase("12") < 0) {
                maxOrderDetailStatus = "11";
            } else if (maxStatus != null && maxStatus.equals("11") && minStatus != null && minStatus.equals("11")) {
                maxOrderDetailStatus = "11";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("09") <= 0 && this.doesOrderDetailStatusExists(pOrderkey, "06")) {
                maxOrderDetailStatus = "06";
            } else if (maxStatus != null && maxStatus.equals("08") && minStatus != null && minStatus.equals("08")) {
                maxOrderDetailStatus = "08";
            } else if (maxStatus != null && maxStatus.equals("04") && minStatus != null && minStatus.equals("04")) {
                maxOrderDetailStatus = "04";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("04") <= 0 && minStatus != null && minStatus.equals("02")) {
                maxOrderDetailStatus = "02";
            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("09") <= 0) {
                maxOrderDetailStatus = "09";
            } else {
                maxOrderDetailStatus = "-1";
            }

            int ordDtZeroQtyCount;
            if (orderDetailCount == 0) {
                ordDtZeroQtyCount = 0;
                String zeroQtyMinStatus = null;

                Map<String,Object> res1 = DBHelper.getRawRecord(" SELECT COUNT ( * ) COUNT, MIN ( Status ) STATUS FROM Orderdetail WHERE Orderkey = ? AND Status <> '18' ",
                    new Object[]{ pOrderkey});

                        ordDtZeroQtyCount = (Integer) res1.get("COUNT");
                        zeroQtyMinStatus =  (String) res1.get("STATUS");

                if (ordDtZeroQtyCount != 0) {
                    pNewStatus = zeroQtyMinStatus;
                } else {
                    pNewStatus = "00";
                }
            } else {
               Map<String,String>  res1 = DBHelper.getRecord("SELECT Status FROM Orders WHERE OrderKey = ?"
                       ,new  Object[]{pOrderkey});

               pStatus =res1.get("Status");


                Map<String,String>  res2 = DBHelper.getRecord( " SELECT MAX (code) code FROM Orderstatussetup WHERE Orderflag = '1' AND Headerflag = '1' AND Enabled = '1' AND Code <= ?",
                new Object[]{maxOrderDetailStatus});

                maxCode = res2.get("code");


                if (maxCode != null && maxCode.equals("09") && pStatus != null && pStatus.compareToIgnoreCase("02") >= 0 && pStatus != null && pStatus.compareToIgnoreCase("08") <= 0 && pStatus != null && pStatus.compareToIgnoreCase("06") != 0) {
                    pNewStatus = pStatus;
                } else if (maxCode != null && maxCode.compareToIgnoreCase("02") >= 0 && maxCode != null && maxCode.compareToIgnoreCase("08") <= 0 && pStatus != null && pStatus.compareToIgnoreCase("96") >= 0) {
                    pNewStatus = pStatus;
                } else {
                    pNewStatus = maxCode;
                }
            }

            if (pNewStatus != null && pNewStatus.equals("NA")) {
                pNewStatus = "00";
            }

            pLogHistory = DBHelper.getValue( " SELECT Headerhistoryflag FROM ORDERSTATUSSETUP WHERE Code = ?",new Object[]{ pNewStatus});

            ServiceDataMap serviceDataMap = new ServiceDataMap();
            serviceDataMap.setAttribValue("status", pStatus);
            serviceDataMap.setAttribValue("newStatus", pNewStatus);
            serviceDataMap.setAttribValue("LogHistory", pLogHistory);

            return serviceDataMap;

        } finally {
            //logger.debug("leaving getOrderStatus( String pOrderkey, DBSession context, String pStatus, String pNewStatus, String pLogHistory)");
        }


    }


    public boolean doesOrderDetailStatusExists(String orderKey, String statusKey) {

        List res = DBHelper.executeQuery("Select *  From ORDERDETAIL Where OrderKey = ? AND STATUS = ?",
                new Object[]{ orderKey,statusKey});

        return res.size()>0;
    }

}