package com.enhantec.wms.backend.outbound.picking;


public class ClusterPickingConfirmV2  {
//
//
//    /**
//     * JOHN 20201115
//     *
//     --注册方法
//     DELETE FROM SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME= 'EHClusterPickingConfirm';
//     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
//     values ('EHClusterPickingConfirm', 'com.enhantec.sce.outbound.order.picking', 'enhantec', 'ClusterPickingConfirm', 'TRUE', 'JOHN',  'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,taskdetailkey,grosswgt,netwgt,tarewgt,uom,toid,snlist,esignaturekey,printer','0.10','0');
//-
//     */
//    //TMTPKP1S3 paramters:
//    //sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,
//    // ttm,taskdetailkey,storerkey,sku,fromloc,fromchkdigit,fromid,toloc,tochkdigit,toid,lot,qty,caseid,packkey,uom,reason,cartongroup,cartontype,transactionkey,position
//    //example: TTM,0000000139,KL,0000000086,10-411,,NB20111004001,PICKTO,,,0000000086,,0000000125,SKU001,EA,1,STD,SMALL,,,
//
//
//    private static final ILogger SCE_LOGGER = SCELoggerFactory.getInstance("ClusterPickingConfirm.class");
//
//    @Override
//    public void execute(ProcessData processData) {
//
//         = ()processData.getInputDataMap();
//        EXEDataObjectprocessData.getInputDataMap() = ((EXEDataObject) ((Object) context.theEXEDataObjectStack.stackList.get(1)));
//
//        String taskdetailkey =processData.getInputDataMap().getString("taskdetailkey");
//        String userid = EHContextHelper.getUser().getUsername();
//
//        String uom =processData.getInputDataMap().getString("uom");
//        String grossWgt =processData.getInputDataMap().getString("grosswgt");
//        String netWgt =processData.getInputDataMap().getString("netwgt");
//        String tareWgt =processData.getInputDataMap().getString("tarewgt");
//        String toId =processData.getInputDataMap().getString("toid");//唯一码拣货至箱号
//        String snListStr =processData.getInputDataMap().getString("snlist");//唯一码列表
//
//        String esignaturekey =processData.getInputDataMap().getString("esignaturekey");
//        String printer =processData.getInputDataMap().getString("printer");
//
//
//
//        try {
//
//            BigDecimal grossWgtDecimal = str2Decimal(grossWgt,"毛重",false);
//            BigDecimal uomQtyTobePicked = str2Decimal(netWgt,"净重",false);
//            BigDecimal tareWgtDecimal = str2Decimal(tareWgt,"皮重",false);
//
//            String[] snList = null;
//            if(!UtilHelper.isEmpty(snListStr)){
//                snList = snListStr.split(";;;");
//            }else {
//                snList = new String[]{};
//            }
//
//            if(snList.length!= 0 && snList.length != Integer.parseInt(netWgt)){
//                ExceptionHelper.throwRfFulfillLogicException("提交的唯一码的个数和净重/数量不一致");
//            }
//
//            //context.theSQLMgr.transactionBegin();
//
//
//
//            if (UtilHelper.isEmpty(taskdetailkey)) ExceptionHelper.throwRfFulfillLogicException("拣货任务号不允许为空");
//
//            HashMap<String, String> taskDetailRecord = getTaskInfo( taskdetailkey,);
//
//            String orderKey = taskDetailRecord.get("ORDERKEY");
//            String orderLineNumber = taskDetailRecord.get("ORDERLINENUMBER");
//            String pickdetailKey = taskDetailRecord.get("PICKDETAILKEY");
//            String orderType = taskDetailRecord.get("TYPE");
//
//            HashMap<String,String>  fromIdHashMap = LotxLocxId.findById(taskDetailRecord.get("FROMID"),true);
//
//             BigDecimal taskQty = new BigDecimal(taskDetailRecord.get("QTY"));
//             BigDecimal lpnQty = new BigDecimal(fromIdHashMap.get("QTY"));
//             BigDecimal availQty = new BigDecimal(fromIdHashMap.get("AVAILABLEQTY"));
//
//             String stdUom = UOM.getStdUOM( fromIdHashMap.get("PACKKEY"));
//
//             BigDecimal stdGrossWgtDecimal = UOM.UOMQty2StdQty( fromIdHashMap.get("PACKKEY"), uom, grossWgtDecimal);
//             BigDecimal stdTareWgtDecimal = UOM.UOMQty2StdQty( fromIdHashMap.get("PACKKEY"), uom, tareWgtDecimal);
//             BigDecimal stdQtyTobePicked = UOM.UOMQty2StdQty( fromIdHashMap.get("PACKKEY"), uom, uomQtyTobePicked);
//
//             if(!CDOrderType.isAllowOverPick(orderType) && UtilHelper.decimalCompare(stdQtyTobePicked,taskQty)>0)
//                 ExceptionHelper.throwRfFulfillLogicException("不允许超额拣货");
//
//             ///对于固体物料，如果存在非整桶拣货的情况，拆分拣货明细后再做拣货。
//             //其他物料走正常的超拣短拣逻辑
//             //注意：固体物料虽然受短拣影响进行任务拆分，但和是否进行整容器发货是没有关系的。超拣和短拣都可能不是整容器发货。
//             //超拣短拣判断的是拣货量是否等于任务量，是否整托盘是看拣货量是否等于当前的LPN量
//
//             boolean isShortPick = stdQtyTobePicked.compareTo(taskQty) < 0;
//             if (CDOrderType.isSplitTaskAfterShortPick(orderType) && isShortPick) {
//                 //如果触发固体拣货的短拣，即当拣货量小于任务量，系统自动重置当前任务量为实际拣货量(避免后面再次触发正常流程的短拣逻辑)，同时分拆当前拣货明细和任务
//                 taskQty = stdQtyTobePicked;
//                 splitPickDetailAndTask( taskdetailkey, stdQtyTobePicked);
//                 //reload task detail as task already changed.
//                 taskDetailRecord = getTaskInfo( taskdetailkey,);
//
//             }
//
//             HashMap<String,String> result = PickUtil.doRandomPick( orderKey,orderLineNumber, fromIdHashMap,toId, grossWgt, tareWgt, netWgt, uom,taskQty, snList,esignaturekey,printer);
//
//             toId = result.get("TOID");
//             String printLabel = result.get("PRINT");
//
//             String reasonCode = PickUtil.getPickReason(taskQty, stdQtyTobePicked);
//
//             if(CDOrderType.isReduceOrderQtyAfterShortPick(orderType)) {
//
//                 if ("SHORT".equals(reasonCode)) {
//
//                     DBHelper.executeUpdate( "UPDATE ORDERDETAIL SET OpenQty = OpenQty - ?, EditWho = ?, EditDate = ? WHERE Orderkey = ? AND OrderLineNumber = ?"
//                             , new Object[]{
//                                     taskQty.subtract(stdQtyTobePicked).toPlainString()),
//                                     userid),
//                                     UtilHelper.getCurrentDate(),
//                                     orderKey),
//                                     orderLineNumber}
//                     );
//
//                     OutboundHelper.GetOrderDetailStatusOutputParam orderDetailStatusParam = getOrderDetailStatus( orderKey, orderLineNumber), null, null);
//                     String newOrderDetailStatus = orderDetailStatusParam.pNewStatus;
//
//
//                     DBHelper.executeUpdate( "UPDATE ORDERDETAIL SET Status = ? , EditWho = ?, EditDate = ? WHERE Orderkey = ? AND OrderLineNumber = ?"
//                             , new Object[]{
//                                     newOrderDetailStatus),
//                                     userid),
//                                     UtilHelper.getCurrentDate(),
//                                     orderKey),
//                                     orderLineNumber}
//                     );
//
//                     OutboundHelper.GetOrderStatusOutputParam orderStatusParam = getOrderStatus( orderKey, null, null, null);
//                     String newOrderStatus = orderStatusParam.pNewStatus;
//
//                     DBHelper.executeUpdate( "UPDATE ORDERS SET Status = ? , EditWho = ?, EditDate = ? WHERE Orderkey = ? "
//                             , new Object[]{
//                                     newOrderStatus),
//                                     userid),
//                                     UtilHelper.getCurrentDate(),
//                                     orderKey)
//                             });
//                 }
//
//
//                 ((SsaWrappedConnection) conn).getConnection().commit();
//             }
//
//
//             Udtrn UDTRN = new Udtrn();
//             UDTRN.EsignatureKey = esignaturekey;
//             UDTRN.FROMTYPE = "订单任务拣货";
//             UDTRN.FROMTABLENAME = "TASKDETAIL";
//             UDTRN.FROMKEY = taskdetailkey;
//             UDTRN.FROMKEY = taskdetailkey;
//             UDTRN.FROMKEY1 = taskDetailRecord.get("ORDERKEY");
//             UDTRN.FROMKEY2 = taskDetailRecord.get("PICKDETAILKEY");
//             UDTRN.FROMKEY3 = taskDetailRecord.get("FROMID");
//             UDTRN.TITLE01 = "订单号";
//             UDTRN.CONTENT01 = taskDetailRecord.get("ORDERKEY");
//             UDTRN.TITLE02 = "拣货明细号";
//             UDTRN.CONTENT02 = taskDetailRecord.get("PICKDETAILKEY");
//             UDTRN.TITLE03 = "物料代码";
//             UDTRN.CONTENT03 = taskDetailRecord.get("SKU");
//             UDTRN.TITLE04 = "拣货容器号";
//             UDTRN.CONTENT04 = taskDetailRecord.get("FROMID");
//             UDTRN.TITLE05 = "拣货到容器号";
//             UDTRN.CONTENT05 = toId;
//             UDTRN.TITLE06 = "预期拣货数量";
//             UDTRN.CONTENT06 = taskQty.toPlainString();
//             UDTRN.TITLE07 = "实际拣货数量";
//             UDTRN.CONTENT07 = stdQtyTobePicked.toPlainString();
//
//
//             //
//             UDTRN.Insert( EHContextHelper.getUser().getUsername());
//             ((SsaWrappedConnection) conn).getConnection().commit();
//
//
//
//            EXEDataObject outDO = (EXEDataObject) context.theEXEDataObjectStack.stackList.get(1);
//
//            HashMap<String, String> taskDetailHashMap = TaskDetail.findById( taskdetailkey, true);
//            outDO.setAttribValue("status", taskDetailHashMap.get("STATUS"));
//            outDO.setAttribValue("PRINTLABEL",printLabel);
//
//        }catch (Exception e)
//        {
//            try
//            {
//                ((SsaWrappedConnection) conn).rollback();
//
//            }	catch (Exception e1) {}
//
//            if ( e instanceof FulfillLogicException)
//                throw (FulfillLogicException)e;
//            else
//                throw new FulfillLogicException().setup( 2, 2, 26, 9999, FObject.NX(this, e.getMessage()),
//                            FulfillException.D_PARAM_SETUP_PPARAM1,FulfillException.D_PARAM_SETUP_PPARAM2,FulfillException.D_PARAM_SETUP_PPARAM3,FulfillException.D_PARAM_SETUP_PPARAM4,FulfillException.D_PARAM_SETUP_PPARAM5,
//                            FulfillException.D_PARAM_SETUP_PPARAM6,FulfillException.D_PARAM_SETUP_PPARAM7,FulfillException.D_PARAM_SETUP_PPARAM8,FulfillException.D_PARAM_SETUP_PPARAM9);
//
//        }finally {
//            
//        }
//
//      
//    }
//
//    public void splitPickDetailAndTask( String taskdetailkey, BigDecimal stdQtyTobePicked) throws SQLException, SsaException {
//
//        PreparedStatement qqPrepStmt = null;
//
//        try {
//            //context.theSQLMgr.transactionBegin();
//
//            //((SSADBSession)context).createTransaction();
//            //DatabaseTransaction trans = ((SSADBSession)context).createStandaloneTransaction();
//            context.theSQLMgr.transBeginIndependent();
//            //******************
//            // MUST GET CONNECTION AFTER transBeginIndependent, OTHTERWISE THE TRANSACTION WILL NOT DO REAL COMMIT AFTER transCommitIndependent()
//            
//            //*****************
//
//            HashMap<String, String> originalTaskDetailInfo = TaskDetail.findById( taskdetailkey, true);
//            HashMap<String, String> originalPickDetailInfo = PickDetail.findByPickDetailKey( originalTaskDetailInfo.get("PICKDETAILKEY"), true);
//
//            DBHelper.executeUpdate( "UPDATE PICKDETAIL SET QTY = ? , UOMQTY = ? WHERE PICKDETAILKEY = ?",
//                    new Object[]{
//                            //CANNOT use decimalData as qty will become 0 if it is very small like 0.001
//                            stdQtyTobePicked.toPlainString()),
//                            stdQtyTobePicked.toPlainString()),
//                            originalTaskDetailInfo.get("PICKDETAILKEY"))
//                    });
//
//            DBHelper.executeUpdate( "UPDATE TASKDETAIL SET QTY = ? , UOMQTY = ? WHERE TASKDETAILKEY = ?",
//                    new Object[]{
//                            stdQtyTobePicked.toPlainString()),
//                            stdQtyTobePicked.toPlainString()),
//                            taskdetailkey)
//                    });
//
//            qqPrepStmt = DBHelper.executeUpdate(" INSERT INTO PICKDETAIL ( PickDetailKey, CaseID, PickHeaderkey, OrderKey, OrderLineNumber, Lot, Storerkey, Sku, PackKey, UOM, UOMQty, Qty, Loc, ToLoc, ID, CartonGroup, CartonType, DoReplenish, ReplenishZone, DoCartonize, PickMethod, AddWho, EditWho, SeqNo, StatusRequired,fromloc, SelectedCartonType, SelectedCartonID, grosswgt, netwgt, tarewgt, PickContPlacement, status, wavekey) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,? )");
//
//            BigDecimal taskQty = new BigDecimal(originalTaskDetailInfo.get("QTY"));
//            BigDecimal qtyDiff = taskQty.subtract(stdQtyTobePicked);
//
//
//            String pickDetailKey = KeyGen.getKey("PICKDETAILKEY", 10, context).getValue();
//            String caseId = KeyGen.getKey("CARTONID", 10, context).getValue();
//
//            new Object[]{ pickDetailKey);
//            DBHelper.setValue(qqPrepStmt, 2, caseId);
//            DBHelper.setValue(qqPrepStmt, 3, " ");
//            DBHelper.setValue(qqPrepStmt, 4, originalPickDetailInfo.get("ORDERKEY"));
//            DBHelper.setValue(qqPrepStmt, 5, originalPickDetailInfo.get("ORDERLINENUMBER"));
//            DBHelper.setValue(qqPrepStmt, 6, originalPickDetailInfo.get("LOT"));
//            DBHelper.setValue(qqPrepStmt, 7, originalPickDetailInfo.get("STORERKEY"));
//            DBHelper.setValue(qqPrepStmt, 8, originalPickDetailInfo.get("SKU"));
//            DBHelper.setValue(qqPrepStmt, 9, originalPickDetailInfo.get("PACKKEY"));
//            //As current allocation strategy is the only allocate using standard UOM, so here set uomQty without need UOM conversion
//            DBHelper.setValue(qqPrepStmt, 10, originalPickDetailInfo.get("UOM"));
//            DBHelper.setValue(qqPrepStmt, 11, qtyDiff.toPlainString());
//            DBHelper.setValue(qqPrepStmt, 12, qtyDiff.toPlainString());
//            DBHelper.setValue(qqPrepStmt, 13, originalPickDetailInfo.get("LOC"));
//            DBHelper.setValue(qqPrepStmt, 14, originalPickDetailInfo.get("TOLOC"));
//            DBHelper.setValue(qqPrepStmt, 15, originalPickDetailInfo.get("ID"));
//            DBHelper.setValue(qqPrepStmt, 16, originalPickDetailInfo.get("CARTONGROUP"));
//            DBHelper.setValue(qqPrepStmt, 17, originalPickDetailInfo.get("CARTONTYPE"));
//            DBHelper.setValue(qqPrepStmt, 18, originalPickDetailInfo.get("DOREPLENISH"));
//            DBHelper.setValue(qqPrepStmt, 19, originalPickDetailInfo.get("REPLENISHZONE"));
//            DBHelper.setValue(qqPrepStmt, 20, originalPickDetailInfo.get("DOCARTONIZE"));
//            DBHelper.setValue(qqPrepStmt, 21, originalPickDetailInfo.get("PICKMETHOD"));
//            DBHelper.setValue(qqPrepStmt, 22, EHContextHelper.getUser().getUsername());
//            DBHelper.setValue(qqPrepStmt, 23, EHContextHelper.getUser().getUsername());
//            DBHelper.setValue(qqPrepStmt, 24, 99999);
//            DBHelper.setValue(qqPrepStmt, 25, originalPickDetailInfo.get("STATUSREQUIRED"));
//            DBHelper.setValue(qqPrepStmt, 26, originalPickDetailInfo.get("FROMLOC"));
//            DBHelper.setValue(qqPrepStmt, 27, originalPickDetailInfo.get("SELECTEDCARTONTYPE"));
//            DBHelper.setValue(qqPrepStmt, 28, caseId);
//            DBHelper.setValue(qqPrepStmt, 29, originalPickDetailInfo.get("GROSSWGT"));
//            DBHelper.setValue(qqPrepStmt, 30, originalPickDetailInfo.get("NETWGT"));
//            DBHelper.setValue(qqPrepStmt, 31, originalPickDetailInfo.get("TAREWGT"));
//            DBHelper.setValue(qqPrepStmt, 32, "0");//PICKCONTPLACEMENT
//            DBHelper.setValue(qqPrepStmt, 33, "1"); //status: released
//            DBHelper.setValue(qqPrepStmt, 34, originalPickDetailInfo.get("WAVEKEY"));
//            qqPrepStmt.executeUpdate();
//
//
//            qqPrepStmt = DBHelper.executeUpdate(" INSERT INTO TASKDETAIL ( TaskDetailKey, TaskType, StorerKey, Sku, Lot, UOM, UOMQTY, Qty, FromLoc, LogicalFromLoc, FromID, ToLoc, ToId, SourceType, SourceKey, WaveKey, CaseId, OrderKey, OrderLineNumber, PickDetailKey, PickMethod, AddWho, EditWho, Door, Route, Stop, Putawayzone,STATUS,USERKEY,REASONKEY,STARTTIME,ENDTIME, PICKCONTPLACEMENT ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,? )");
//            String newTaskDetailKey = KeyGen.getKey("TASKDETAILKEY", 10, context).getValue();
//            new Object[]{ newTaskDetailKey);
//            DBHelper.setValue(qqPrepStmt, 2, "PK");
//            DBHelper.setValue(qqPrepStmt, 3, originalTaskDetailInfo.get("STORERKEY"));
//            DBHelper.setValue(qqPrepStmt, 4, originalTaskDetailInfo.get("SKU"));
//            DBHelper.setValue(qqPrepStmt, 5, originalTaskDetailInfo.get("LOT"));
//            DBHelper.setValue(qqPrepStmt, 6, originalTaskDetailInfo.get("UOM"));
//            //As current allocation strategy is the only allocate using standard UOM, so here set uomQty without need UOM conversion
//            DBHelper.setValue(qqPrepStmt, 7, qtyDiff.toPlainString());
//            DBHelper.setValue(qqPrepStmt, 8, qtyDiff.toPlainString());
//            DBHelper.setValue(qqPrepStmt, 9, originalTaskDetailInfo.get("FROMLOC"));
//            DBHelper.setValue(qqPrepStmt, 10, originalTaskDetailInfo.get("LOGICALFROMLOC"));
//            DBHelper.setValue(qqPrepStmt, 11, originalTaskDetailInfo.get("FROMID"));
//            DBHelper.setValue(qqPrepStmt, 12, originalTaskDetailInfo.get("TOLOC"));
//            DBHelper.setValue(qqPrepStmt, 13, originalTaskDetailInfo.get("TOID"));
//            DBHelper.setValue(qqPrepStmt, 14, originalTaskDetailInfo.get("SOURCETYPE"));
//            DBHelper.setValue(qqPrepStmt, 15, pickDetailKey);
//            DBHelper.setValue(qqPrepStmt, 16, originalTaskDetailInfo.get("WAVEKEY"));
//            DBHelper.setValue(qqPrepStmt, 17, caseId);
//            DBHelper.setValue(qqPrepStmt, 18, originalTaskDetailInfo.get("ORDERKEY"));
//            DBHelper.setValue(qqPrepStmt, 19, originalTaskDetailInfo.get("ORDERLINENUMBER"));
//            DBHelper.setValue(qqPrepStmt, 20, pickDetailKey);
//            DBHelper.setValue(qqPrepStmt, 21, originalTaskDetailInfo.get("PICKMETHOD"));
//            DBHelper.setValue(qqPrepStmt, 22, EHContextHelper.getUser().getUsername());
//            DBHelper.setValue(qqPrepStmt, 23, EHContextHelper.getUser().getUsername());
//            DBHelper.setValue(qqPrepStmt, 24, originalTaskDetailInfo.get("DOOR"));
//            DBHelper.setValue(qqPrepStmt, 25, originalTaskDetailInfo.get("ROUTE"));
//            DBHelper.setValue(qqPrepStmt, 26, originalTaskDetailInfo.get("STOP"));
//            DBHelper.setValue(qqPrepStmt, 27, originalTaskDetailInfo.get("PUTAWAYZONE"));
//            DBHelper.setValue(qqPrepStmt, 28, "0"); //status
//            DBHelper.setValue(qqPrepStmt, 29, " ");//USERKEY设为空，否则在状态为0时获取不到任务
//            DBHelper.setValue(qqPrepStmt, 30, " ");//reasonkey
//            java.sql.Date currentDate = UtilHelper.getCurrentDate();
//            DBHelper.setValue(qqPrepStmt, 31, currentDate);
//            DBHelper.setValue(qqPrepStmt, 32, currentDate);
//            DBHelper.setValue(qqPrepStmt, 33, "0");//PICKCONTPLACEMENT 将新建的分拆拣货任务的任务优先级设为最高，下一次拣货优先进行
//            qqPrepStmt.executeUpdate();
//
//
//            //context.theSQLMgr.transactionCommit();
//            //trans.commit();
//            context.theSQLMgr.transCommitIndependent();
//
//
//        } catch (SQLException qqSQLException) {
//            SQLException e1=new SQLException(qqSQLException.getMessage());
//            throw new DBResourceException(e1);
//        } finally {
//            context.releaseStatement(qqPrepStmt);
//            context.releaseConnection(qqConnection);
//        }
//    }
//
//
//    private HashMap<String, String> getTaskInfo( String taskdetailkey,) {
//        return DBHelper.getRecord(
//                "SELECT O.TYPE, TD.STATUS, TD.LOT,TD.QTY,TD.SKU,TD.STORERKEY,TD.FROMLOC,TD.FROMID,TD.TOID,TD.TOLOC,TD.CASEID,TD.UOM,P.CARTONGROUP ,P.CARTONTYPE ,P.PACKKEY, P.ORDERKEY,P.ORDERLINENUMBER, P.PICKDETAILKEY " +
//                        " FROM TASKDETAIL TD, PICKDETAIL P,ORDERS O " +
//                        " WHERE TD.PICKDETAILKEY = P.PICKDETAILKEY AND O.ORDERKEY = TD.ORDERKEY AND TASKDETAILKEY = ? ",
//                new Object[]{taskdetailkey}, "拣货和任务明细");
//    }
//
//
//    public OutboundHelper.GetOrderDetailStatusOutputParam getOrderDetailStatus( String pOrderkey, TextData pOrderLineNumber, String pNewStatus, String pDetailHistoryFlag) {
//        OutboundHelper.GetOrderDetailStatusOutputParam var296;
//        try {
//            DBSession pSession =  context;
//            //logger.debug("starting getOrderDetailStatus( String pOrderkey, TextData pOrderLineNumber, DBSession pSession, String pNewStatus, String pDetailHistoryFlag)");
//            Integer pickstotal = null;
//            Integer released = null;
//            Integer inpicking = null;
//            Integer packed = null;
//            Integer instaged = null;
//            Integer loaded = null;
//            Integer dpreleased = null;
//            Integer dpinpicking = null;
//            String orderdetailstatus = null;
//            String maxcode = null;
//            double SHIPPEDQTY = 0.0D;
//            double OPENQTY = 0.0D;
//            double QTYPREALLOCATED = 0.0D;
//            double QTYALLOCATED = 0.0D;
//            double QTYPICKED = 0.0D;
//            int ODSTATUS = 0;
//            int ISSUBSTITUTE = 0;
//            String wpReleased = null;
//            int qqRowCount = 0;
//            //
//            PreparedStatement qqPrepStmt = null;
//            ResultSet qqResultSet = null;
//            String pickdetailstatus = null;
//            String locType = null;
//            String dropid = null;
//            String waveKey = null;
//            int ordreleased = 0;
//            int inpick = 0;
//            int ordpacked = 0;
//            int ordinstaged = 0;
//            int ordloaded = 0;
//
//            try {
//                qqPrepStmt = DBHelper.executeUpdate(" SELECT a.status, b.locationtype, a.dropid, a.wavekey FROM PICKDETAIL a, LOC b WHERE b.Loc = a.Loc AND a.OrderKey = ? AND a.OrderLineNumber = ?");
//                new Object[]{ pOrderkey);
//                DBHelper.setValue(qqPrepStmt, 2, pOrderLineNumber);
//
//                for(qqResultSet = qqPrepStmt.executeQuery(); qqResultSet.next(); ++qqRowCount) {
//                    pickdetailstatus = DBHelper.getValue(qqResultSet, 1, pickdetailstatus);
//                    locType = DBHelper.getValue(qqResultSet, 2, locType);
//                    dropid = DBHelper.getValue(qqResultSet, 3, dropid);
//                    waveKey = DBHelper.getValue(qqResultSet, 4, waveKey);
//                    if (pickdetailstatus.equals("1") && waveKey != " ") {
//                        ++ordreleased;
//                    }
//
//                    if (pickdetailstatus.equals("2") || pickdetailstatus.equals("3")) {
//                        ++inpick;
//                    }
//
//                    if (pickdetailstatus.equals("6")) {
//                        ++ordpacked;
//                    }
//
//                    if (locType.equals("STAGED")) {
//                        ++ordinstaged;
//                    }
//
//                    if (pickdetailstatus.equals("8")) {
//                        ++ordloaded;
//                    }
//                }
//
//                pickstotal.setValue(qqRowCount);
//                released.setValue(ordreleased);
//                inpicking.setValue(inpick);
//                packed.setValue(ordpacked);
//                instaged.setValue(ordinstaged);
//                loaded.setValue(ordloaded);
//            } catch (SQLException var277) {
//                throw new DBResourceException(var277);
//            } finally {
//                pSession.releaseStatement(qqPrepStmt);
//                pSession.releaseResultSet(qqResultSet);
//                pSession.releaseConnection(qqConnection);
//            }
//
//            boolean var42 = false;
//
//            PreparedStatement qqPrepStmt5 = null;
//            ResultSet qqResultSet5 = null;
//
//            try {
//                qqPrepStmt5 = DBHelper.executeUpdate(" SELECT SUM ( CASE WHEN b.status = '0' AND a.wavekey <> ' ' THEN 1 ELSE 0 END ) RELEASED, SUM ( CASE WHEN b.status >= '2' AND b.status <= '3' THEN 1 ELSE 0 END ) INPICKING FROM DEMANDALLOCATION a, TASKDETAIL b WHERE b.Sourcekey = a.DemandKey AND b.SourceType = 'DP' AND b.status = '0' AND a.OrderKey = ? AND a.OrderLineNumber = ?");
//                DBHelper.setValue(qqPrepStmt5, 1, pOrderkey);
//                DBHelper.setValue(qqPrepStmt5, 2, pOrderLineNumber);
//                qqResultSet5 = qqPrepStmt5.executeQuery();
//                if (qqResultSet5.next()) {
//                    dpreleased = DBHelper.getValue(qqResultSet5, 1, dpreleased);
//                    dpinpicking = DBHelper.getValue(qqResultSet5, 2, dpinpicking);
//                }
//            } catch (SQLException var279) {
//                throw new DBResourceException(var279);
//            } finally {
//                pSession.releaseStatement(qqPrepStmt5);
//                pSession.releaseResultSet(qqResultSet5);
//                pSession.releaseConnection(qqConnection);
//            }
//
//            byte qqRowCount1 = 0;
//
//            PreparedStatement qqPrepStmt1 = null;
//            ResultSet qqResultSet1 = null;
//
//            try {
//                qqPrepStmt1 = DBHelper.executeUpdate(" SELECT SHIPPEDQTY, OPENQTY, QTYPREALLOCATED, QTYALLOCATED, QTYPICKED, ISSUBSTITUTE, WPReleased, STATUS FROM OrderDetail WHERE OrderKey = ? AND OrderLineNumber = ?");
//                DBHelper.setValue(qqPrepStmt1, 1, pOrderkey);
//                DBHelper.setValue(qqPrepStmt1, 2, pOrderLineNumber);
//                qqResultSet1 = qqPrepStmt1.executeQuery();
//                if (qqResultSet1.next()) {
//                    SHIPPEDQTY = DBHelper.getValue(qqResultSet1, 1, SHIPPEDQTY);
//                    OPENQTY = DBHelper.getValue(qqResultSet1, 2, OPENQTY);
//                    QTYPREALLOCATED = DBHelper.getValue(qqResultSet1, 3, QTYPREALLOCATED);
//                    QTYALLOCATED = DBHelper.getValue(qqResultSet1, 4, QTYALLOCATED);
//                    QTYPICKED = DBHelper.getValue(qqResultSet1, 5, QTYPICKED);
//                    ISSUBSTITUTE = DBHelper.getValue(qqResultSet1, 6, ISSUBSTITUTE);
//                    wpReleased = DBHelper.getValue(qqResultSet1, 7, wpReleased);
//                    ODSTATUS = DBHelper.getValue(qqResultSet1, 8, ODSTATUS);
//                    int var286 = qqRowCount1 + 1;
//                }
//            } catch (SQLException var273) {
//                throw new DBResourceException(var273);
//            } finally {
//                pSession.releaseStatement(qqPrepStmt1);
//                pSession.releaseResultSet(qqResultSet1);
//                pSession.releaseConnection(qqConnection);
//            }
//
//            if (pickstotal== null) {
//                pickstotal.setValue(0);
//            }
//
//            if (released== null) {
//                released.setValue(0);
//            }
//
//            if (inpicking== null) {
//                inpicking.setValue(0);
//            }
//
//            if (packed== null) {
//                packed.setValue(0);
//            } else if (QTYPICKED == 0.0D) {
//                packed.setValue(0);
//            }
//
//            if (instaged== null) {
//                instaged.setValue(0);
//            }
//
//            if (loaded== null) {
//                loaded.setValue(0);
//            }
//
//            if (dpreleased== null) {
//                dpreleased.setValue(0);
//            }
//
//            if (dpinpicking== null) {
//                dpinpicking.setValue(0);
//            }
//
//            if (SHIPPEDQTY > 0.0D && OPENQTY == 0.0D) {
//                pNewStatus = "95";
//            } else if (SHIPPEDQTY > 0.0D && OPENQTY > 0.0D && OPENQTY - QTYPREALLOCATED - QTYALLOCATED == 0.0D && (ODSTATUS >= 22 && ODSTATUS <= 29 || released > 0)) {
//                pNewStatus = "27";
//            } else if (SHIPPEDQTY > 0.0D && OPENQTY > 0.0D && OPENQTY - QTYPREALLOCATED - QTYALLOCATED == 0.0D) {
//                pNewStatus = "92";
//            } else if (loaded == pickstotal && pickstotal > 0 && QTYPICKED > 0.0D && OPENQTY - QTYPICKED == 0.0D) {
//                pNewStatus = "88";
//            } else if (SHIPPEDQTY > 0.0D && loaded > 0) {
//                pNewStatus = "92";
//            } else if (loaded > 0 && QTYPICKED > 0.0D && OPENQTY - QTYPICKED == 0.0D) {
//                pNewStatus = "82";
//            } else if (instaged == pickstotal && instaged > 0 && SHIPPEDQTY == 0.0D && OPENQTY - QTYPICKED == 0.0D && OPENQTY > 0.0D && QTYPICKED > 0.0D) {
//                pNewStatus = "75";
//            } else if (packed == pickstotal && packed > 0 && QTYPICKED == OPENQTY) {
//                pNewStatus = "68";
//            } else if (SHIPPEDQTY > 0.0D && packed > 0) {
//                pNewStatus = "92";
//            } else if (packed > 0) {
//                pNewStatus = "61";
//            } else if (SHIPPEDQTY > 0.0D && OPENQTY - QTYPICKED == 0.0D && OPENQTY > 0.0D && QTYPICKED > 0.0D) {
//                pNewStatus = "57";
//            } else if (SHIPPEDQTY == 0.0D && OPENQTY - QTYPICKED == 0.0D && OPENQTY > 0.0D && QTYPICKED > 0.0D) {
//                pNewStatus = "55";
//            } else if (SHIPPEDQTY > 0.0D && OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED >= 0.0D && QTYALLOCATED >= 0.0D && QTYPICKED > 0.0D) {
//                pNewStatus = "53";
//            } else if (SHIPPEDQTY != 0.0D || !(OPENQTY > 0.0D) || OPENQTY - QTYALLOCATED - QTYPICKED != 0.0D || !(QTYALLOCATED > 0.0D) || !(QTYPICKED > 0.0D) || (ODSTATUS < 22 || ODSTATUS > 29) && released <= 0) {
//                if (SHIPPEDQTY == 0.0D && OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED == 0.0D && QTYALLOCATED > 0.0D && QTYPICKED > 0.0D) {
//                    pNewStatus = "52";
//                } else if (inpicking <= 0 && dpinpicking <= 0) {
//                    if (SHIPPEDQTY != 0.0D || !(OPENQTY > 0.0D) || OPENQTY - QTYALLOCATED != 0.0D || released <= 0 && dpreleased <= 0) {
//                        if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYPICKED > 0.0D && QTYALLOCATED > 0.0D && (ODSTATUS >= 22 && ODSTATUS <= 29 || released > 0)) {
//                            pNewStatus = "25";
//                        } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYALLOCATED > 0.0D && SHIPPEDQTY == 0.0D && (released > 0 || dpreleased > 0) && QTYPICKED == 0.0D) {
//                            pNewStatus = "22";
//                        } else if (SHIPPEDQTY == 0.0D && OPENQTY == 0.0D && QTYPICKED == 0.0D && ISSUBSTITUTE > 0) {
//                            pNewStatus = "18";
//                        } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED == 0.0D) {
//                            pNewStatus = "17";
//                        } else if (!(SHIPPEDQTY > 0.0D) || !(OPENQTY - QTYALLOCATED > 0.0D) || QTYPICKED != 0.0D || (ODSTATUS < 22 || ODSTATUS > 29) && released <= 0) {
//                            if (SHIPPEDQTY > 0.0D && OPENQTY - QTYALLOCATED > 0.0D && QTYPICKED == 0.0D) {
//                                pNewStatus = "16";
//                            } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYPICKED > 0.0D && QTYALLOCATED >= 0.0D && (ODSTATUS >= 22 && ODSTATUS <= 29 || ODSTATUS == 51 || released > 0)) {
//                                pNewStatus = "25";
//                            } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYPICKED > 0.0D && QTYALLOCATED >= 0.0D) {
//                                pNewStatus = "15";
//                            } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYALLOCATED > 0.0D) {
//                                pNewStatus = "14";
//                            } else if (OPENQTY > 0.0D && OPENQTY - QTYPREALLOCATED == 0.0D && wpReleased != null && wpReleased.equals("1")) {
//                                pNewStatus = "13";
//                            } else if (OPENQTY > 0.0D && OPENQTY - QTYPREALLOCATED == 0.0D) {
//                                pNewStatus = "12";
//                            } else if (OPENQTY > 0.0D && QTYPREALLOCATED > 0.0D) {
//                                pNewStatus = "11";
//                            } else if (OPENQTY > 0.0D && QTYPREALLOCATED + QTYALLOCATED + QTYPICKED + SHIPPEDQTY == 0.0D) {
//                                pNewStatus = "09";
//                            } else if (SHIPPEDQTY + OPENQTY == 0.0D) {
//                                pNewStatus = "95";
//                            } else if (OPENQTY - QTYPREALLOCATED - QTYALLOCATED - QTYPICKED < 0.0D) {
//                                pNewStatus = "-2";
//                            } else {
//                                pNewStatus = "-1";
//                            }
//                        } else {
//                            pNewStatus = "27";
//                        }
//                    } else {
//                        pNewStatus = "29";
//                    }
//                } else {
//                    pNewStatus = "51";
//                }
//            } else {
//                pNewStatus = "25";
//            }
//
//            int qqRowCount2 = 0;
//
//            PreparedStatement qqPrepStmt2 = null;
//            ResultSet qqResultSet2 = null;
//            String storerkey = null;
//            String sku = null;
//
//            int var287;
//            try {
//                qqPrepStmt2 = DBHelper.executeUpdate(" SELECT Status, Storerkey, SKU FROM OrderDetail WHERE OrderKey = ? AND OrderLineNumber = ?");
//                DBHelper.setValue(qqPrepStmt2, 1, pOrderkey);
//                DBHelper.setValue(qqPrepStmt2, 2, pOrderLineNumber);
//                qqResultSet2 = qqPrepStmt2.executeQuery();
//                if (qqResultSet2.next()) {
//                    orderdetailstatus = DBHelper.getValue(qqResultSet2, 1, orderdetailstatus);
//                    storerkey = DBHelper.getValue(qqResultSet2, 2, storerkey);
//                    sku = DBHelper.getValue(qqResultSet2, 3, sku);
//                    var287 = qqRowCount2 + 1;
//                }
//            } catch (SQLException var281) {
//                throw new DBResourceException(var281);
//            } finally {
//                pSession.releaseStatement(qqPrepStmt2);
//                pSession.releaseResultSet(qqResultSet2);
//                pSession.releaseConnection(qqConnection);
//            }
//
//            byte qqRowCount3 = 0;
//
//            PreparedStatement qqPrepStmt3 = null;
//            ResultSet qqResultSet3 = null;
//
//            try {
//                qqPrepStmt3 = DBHelper.executeUpdate(" SELECT MAX ( Code ) FROM Orderstatussetup WHERE Orderflag = '1' AND Detailflag = '1' AND Enabled = '1' AND Code <= ?");
//                DBHelper.setValue(qqPrepStmt3, 1, pNewStatus);
//                qqResultSet3 = qqPrepStmt3.executeQuery();
//                if (qqResultSet3.next()) {
//                    maxcode = DBHelper.getValue(qqResultSet3, 1, maxcode);
//                    int var288 = qqRowCount3 + 1;
//                }
//            } catch (SQLException var271) {
//                throw new DBResourceException(var271);
//            } finally {
//                pSession.releaseStatement(qqPrepStmt3);
//                pSession.releaseResultSet(qqResultSet3);
//                pSession.releaseConnection(qqConnection);
//            }
//
//            if (maxcode != null && maxcode.equals("09") && orderdetailstatus != null && orderdetailstatus.compareToIgnoreCase("02") >= 0 && orderdetailstatus != null && orderdetailstatus.compareToIgnoreCase("08") <= 0) {
//                pNewStatus = orderdetailstatus;
//            } else if (orderdetailstatus == null || !orderdetailstatus.equalsIgnoreCase("68") || !pNewStatus.equalsIgnoreCase("55") && !pNewStatus.equalsIgnoreCase("61")) {
//                pNewStatus = maxcode;
//            } else {
//                pNewStatus = maxcode;
//                double qtyPacked = 0.0D;
//                double qtyOrdered = 0.0D;
//                int qqRowCount6 = 0;
//
//                PreparedStatement qqPrepStmt6 = null;
//                ResultSet qqResultSet6 = null;
//
//                try {
//                    qqPrepStmt6 = DBHelper.executeUpdate(" SELECT SUM(qtypicked) FROM Packoutdetail WHERE Orderkey = ? AND Storerkey =? AND SKU = ?");
//                    DBHelper.setValue(qqPrepStmt6, 1, pOrderkey);
//                    DBHelper.setValue(qqPrepStmt6, 2, storerkey);
//                    DBHelper.setValue(qqPrepStmt6, 3, sku);
//                    qqResultSet6 = qqPrepStmt6.executeQuery();
//                    if (qqResultSet6.next()) {
//                        qtyPacked = qqResultSet6.getDouble(1);
//                        int var292 = qqRowCount6 + 1;
//                    }
//                } catch (SQLException var269) {
//                    throw new DBResourceException(var269);
//                } finally {
//                    pSession.releaseStatement(qqPrepStmt6);
//                    pSession.releaseResultSet(qqResultSet6);
//                    pSession.releaseConnection(qqConnection);
//                }
//
//                if (qtyPacked > 0.0D) {
//                    int qqRowCount7 = 0;
//
//                    PreparedStatement qqPrepStmt7 = null;
//                    ResultSet qqResultSet7 = null;
//
//                    try {
//                        qqPrepStmt7 = DBHelper.executeUpdate(" SELECT sum(Openqty+SHIPPEDQTY) FROM Orderdetail WHERE Orderkey = ? AND Storerkey =? AND SKU = ? ");
//                        DBHelper.setValue(qqPrepStmt7, 1, pOrderkey);
//                        DBHelper.setValue(qqPrepStmt7, 2, storerkey);
//                        DBHelper.setValue(qqPrepStmt7, 3, sku);
//                        qqResultSet7 = qqPrepStmt7.executeQuery();
//                        if (qqResultSet7.next()) {
//                            qtyOrdered = DBHelper.getValue(qqResultSet7, 1, qtyOrdered);
//                            int var297 = qqRowCount7 + 1;
//                        }
//                    } catch (SQLException var283) {
//                        throw new DBResourceException(var283);
//                    } finally {
//                        pSession.releaseStatement(qqPrepStmt7);
//                        pSession.releaseResultSet(qqResultSet7);
//                        pSession.releaseConnection(qqConnection);
//                    }
//
//                    if (qtyPacked == qtyOrdered) {
//                        pNewStatus = orderdetailstatus;
//                    }
//                }
//            }
//
//            int qqRowCount4 = 0;
//
//            PreparedStatement qqPrepStmt4 = null;
//            ResultSet qqResultSet4 = null;
//
//            try {
//                qqPrepStmt4 = DBHelper.executeUpdate(" SELECT DetailHistoryFlag FROM ORDERSTATUSSETUP WHERE Code = ?");
//                DBHelper.setValue(qqPrepStmt4, 1, pNewStatus);
//                qqResultSet4 = qqPrepStmt4.executeQuery();
//                if (qqResultSet4.next()) {
//                    pDetailHistoryFlag = DBHelper.getValue(qqResultSet4, 1, pDetailHistoryFlag);
//                    int var290 = qqRowCount4 + 1;
//                }
//            } catch (SQLException var267) {
//                throw new DBResourceException(var267);
//            } finally {
//                pSession.releaseStatement(qqPrepStmt4);
//                pSession.releaseResultSet(qqResultSet4);
//                pSession.releaseConnection(qqConnection);
//            }
//
//            if ("95".equals(pNewStatus)) {
//                qqRowCount2 = 0;
//                qqPrepStmt2 = null;
//                qqResultSet2 = null;
//                String isOrderRequireClose = null;
//
//                try {
//                    qqPrepStmt2 = DBHelper.executeUpdate(" SELECT REQUIREORDERCLOSE  FROM ORDERS WHERE OrderKey = ? ");
//                    qqPrepStmt2.setString(1, pOrderkey);
//                    qqResultSet2 = qqPrepStmt2.executeQuery();
//                    if (qqResultSet2.next()) {
//                        isOrderRequireClose = qqResultSet2.getString(1);
//                        var287 = qqRowCount2 + 1;
//                    }
//                } catch (SQLException var275) {
//                    throw new DBResourceException(var275);
//                } finally {
//                    pSession.releaseStatement(qqPrepStmt2);
//                    pSession.releaseResultSet(qqResultSet2);
//                    pSession.releaseConnection(qqConnection);
//                }
//
//                if (isOrderRequireClose != null && "1".equals(isOrderRequireClose)) {
//                    pNewStatus = "94";
//                }
//            }
//
//            OutboundHelper.GetOrderDetailStatusOutputParam qqGetOrderDetailStatusOutputParam = new OutboundHelper.GetOrderDetailStatusOutputParam();
//            qqGetOrderDetailStatusOutputParam.pNewStatus = pNewStatus;
//            qqGetOrderDetailStatusOutputParam.pDetailHistoryFlag = pDetailHistoryFlag;
//            var296 = qqGetOrderDetailStatusOutputParam;
//        } finally {
//            //logger.debug("leaving getOrderDetailStatus( String pOrderkey, TextData pOrderLineNumber, DBSession pSession, String pNewStatus, String pDetailHistoryFlag)");
//        }
//
//        return var296;
//    }
//
//    public OutboundHelper.GetOrderStatusOutputParam getOrderStatus(String pOrderkey, String pStatus, String pNewStatus, String pLogHistory) {
//        OutboundHelper.GetOrderStatusOutputParam var108;
//        try {
//            DBSession pSession = context;
//            //logger.debug("starting getOrderStatus( String pOrderkey, DBSession pSession, String pStatus, String pNewStatus, String pLogHistory)");
//            String maxOrderDetailStatus = null;
//            String maxCode = null;
//            int orderDetailCount = 0;
//            String maxStatus = null;
//            String minStatus = null;
//            pNewStatus = "NA";
//            int qqRowCount = 0;
//            //
//            PreparedStatement qqPrepStmt = null;
//            ResultSet qqResultSet = null;
//
//            try {
//                qqPrepStmt = DBHelper.executeUpdate(" SELECT COUNT ( * ), MAX ( Status ), MIN ( Status ) FROM Orderdetail WHERE Orderkey = ? AND Status <> '18' and ( openqty>0 or shippedqty>0 or qtypreallocated>0 or qtyallocated>0 or qtypicked>0 )");
//                new Object[]{ pOrderkey);
//                qqResultSet = qqPrepStmt.executeQuery();
//                if (qqResultSet.next()) {
//                    orderDetailCount = DBHelper.getValue(qqResultSet, 1, orderDetailCount);
//                    maxStatus = DBHelper.getValue(qqResultSet, 2, maxStatus);
//                    minStatus = DBHelper.getValue(qqResultSet, 3, minStatus);
//                    ++qqRowCount;
//                }
//            } catch (SQLException var95) {
//                throw new DBResourceException(var95);
//            } finally {
//                pSession.releaseStatement(qqPrepStmt);
//                pSession.releaseResultSet(qqResultSet);
//                pSession.releaseConnection(qqConnection);
//            }
//
//            if (maxStatus != null && maxStatus.equals("99") && minStatus != null && minStatus.equals("99")) {
//                maxOrderDetailStatus = "99";
//            } else if (maxStatus != null && maxStatus.equals("98") && minStatus != null && minStatus.equals("98")) {
//                maxOrderDetailStatus = "98";
//            } else if (maxStatus != null && maxStatus.equals("97") && minStatus != null && minStatus.equals("97")) {
//                maxOrderDetailStatus = "97";
//            } else if (maxStatus != null && maxStatus.equals("96") && minStatus != null && minStatus.equals("96")) {
//                maxOrderDetailStatus = "96";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("95") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("99") <= 0 && minStatus != null && minStatus.equals("95")) {
//                maxOrderDetailStatus = "95";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("94") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("99") <= 0 && minStatus != null && minStatus.equals("94")) {
//                maxOrderDetailStatus = "94";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("92") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("95") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("95") < 0) {
//                maxOrderDetailStatus = "92";
//            } else if (maxStatus != null && maxStatus.equals("27") && minStatus != null && minStatus.compareToIgnoreCase("27") <= 0) {
//                maxOrderDetailStatus = "92";
//            } else if (maxStatus != null && maxStatus.equals("16") && minStatus != null && minStatus.compareToIgnoreCase("16") <= 0) {
//                maxOrderDetailStatus = "92";
//            } else if (maxStatus != null && maxStatus.equals("53") && minStatus != null && minStatus.compareToIgnoreCase("53") <= 0) {
//                maxOrderDetailStatus = "92";
//            } else if (maxStatus != null && maxStatus.equals("57") && minStatus != null && minStatus.compareToIgnoreCase("57") <= 0) {
//                maxOrderDetailStatus = "92";
//            } else if (maxStatus != null && maxStatus.equals("88") && minStatus != null && minStatus.equals("88")) {
//                maxOrderDetailStatus = "88";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("82") >= 0 && minStatus != null && minStatus.compareToIgnoreCase("88") < 0) {
//                maxOrderDetailStatus = "82";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("75") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("78") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("52") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("17") >= 0) {
//                maxOrderDetailStatus = "52";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("75") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("78") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("15") <= 0) {
//                maxOrderDetailStatus = "52";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("75") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("78") <= 0 && minStatus != null && minStatus.equals("55")) {
//                maxOrderDetailStatus = "55";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("75") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("78") <= 0 && minStatus != null && minStatus.equals("68")) {
//                maxOrderDetailStatus = "68";
//            } else if (maxStatus != null && maxStatus.equals("75") && minStatus != null && minStatus.equals("75")) {
//                maxOrderDetailStatus = "75";
//            } else if (maxStatus != null && maxStatus.equals("68") && minStatus != null && minStatus.equals("68")) {
//                maxOrderDetailStatus = "68";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("61") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("68") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("61") <= 0) {
//                maxOrderDetailStatus = "61";
//            } else if (maxStatus != null && maxStatus.equals("55") && minStatus != null && minStatus.equals("55")) {
//                maxOrderDetailStatus = "55";
//            } else if (maxStatus != null && maxStatus.equals("25") && minStatus != null && minStatus.compareToIgnoreCase("25") <= 0) {
//                maxOrderDetailStatus = "52";
//            } else if (maxStatus != null && maxStatus.equals("15") && minStatus != null && minStatus.compareToIgnoreCase("15") <= 0) {
//                maxOrderDetailStatus = "52";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("52") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("55") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("55") < 0 && this.doesOrderDetailStatusExists(pOrderkey, "27")) {
//                maxOrderDetailStatus = "92";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("52") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("55") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("55") < 0) {
//                maxOrderDetailStatus = "52";
//            } else if (maxStatus != null && maxStatus.equals("29") && minStatus != null && minStatus.equals("29")) {
//                maxOrderDetailStatus = "29";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("27") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0 && (this.doesOrderDetailStatusExists(pOrderkey, "92") || this.doesOrderDetailStatusExists(pOrderkey, "27") || this.doesOrderDetailStatusExists(pOrderkey, "16"))) {
//                maxOrderDetailStatus = "92";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("25") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0 && (this.doesOrderDetailStatusExists(pOrderkey, "52") || this.doesOrderDetailStatusExists(pOrderkey, "25") || this.doesOrderDetailStatusExists(pOrderkey, "15"))) {
//                maxOrderDetailStatus = "52";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("22") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0 && this.doesOrderDetailStatusExists(pOrderkey, "16")) {
//                maxOrderDetailStatus = "92";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("22") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0 && this.doesOrderDetailStatusExists(pOrderkey, "15")) {
//                maxOrderDetailStatus = "52";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("22") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0) {
//                maxOrderDetailStatus = "22";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("29") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("51") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("51") <= 0) {
//                maxOrderDetailStatus = "29";
//            } else if (maxStatus != null && maxStatus.equals("17") && minStatus != null && minStatus.equals("17")) {
//                maxOrderDetailStatus = "17";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("14") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("17") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("17") < 0) {
//                maxOrderDetailStatus = "14";
//            } else if (maxStatus != null && maxStatus.equals("13") && minStatus != null && minStatus.equals("13")) {
//                maxOrderDetailStatus = "13";
//            } else if (maxStatus != null && maxStatus.equals("12") && minStatus != null && minStatus.equals("12")) {
//                maxOrderDetailStatus = "12";
//            } else if (maxStatus != null && maxStatus.equals("12") && minStatus != null && minStatus.compareToIgnoreCase("12") < 0) {
//                maxOrderDetailStatus = "11";
//            } else if (maxStatus != null && maxStatus.equals("11") && minStatus != null && minStatus.equals("11")) {
//                maxOrderDetailStatus = "11";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("09") <= 0 && this.doesOrderDetailStatusExists(pOrderkey, "06")) {
//                maxOrderDetailStatus = "06";
//            } else if (maxStatus != null && maxStatus.equals("08") && minStatus != null && minStatus.equals("08")) {
//                maxOrderDetailStatus = "08";
//            } else if (maxStatus != null && maxStatus.equals("04") && minStatus != null && minStatus.equals("04")) {
//                maxOrderDetailStatus = "04";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("04") <= 0 && minStatus != null && minStatus.equals("02")) {
//                maxOrderDetailStatus = "02";
//            } else if (maxStatus != null && maxStatus.compareToIgnoreCase("09") <= 0) {
//                maxOrderDetailStatus = "09";
//            } else {
//                maxOrderDetailStatus = "-1";
//            }
//
//            byte qqRowCount1;
//            PreparedStatement qqPrepStmt1;
//            ResultSet qqResultSet1;
//            int ordDtZeroQtyCount;
//            if (orderDetailCount == 0) {
//                ordDtZeroQtyCount = 0;
//                String zeroQtyMinStatus = null;
//
//                try {
//                    qqPrepStmt = DBHelper.executeUpdate(" SELECT COUNT ( * ), MIN ( Status ) FROM Orderdetail WHERE Orderkey = ? AND Status <> '18' ");
//                    new Object[]{ pOrderkey);
//                    qqResultSet = qqPrepStmt.executeQuery();
//                    if (qqResultSet.next()) {
//                        ordDtZeroQtyCount = DBHelper.getValue(qqResultSet, 1, ordDtZeroQtyCount);
//                        zeroQtyMinStatus = DBHelper.getValue(qqResultSet, 2, zeroQtyMinStatus);
//                        ++qqRowCount;
//                    }
//                } catch (SQLException var97) {
//                    throw new DBResourceException(var97);
//                } finally {
//                    pSession.releaseStatement(qqPrepStmt);
//                    pSession.releaseResultSet(qqResultSet);
//                    pSession.releaseConnection(qqConnection);
//                }
//
//                if (ordDtZeroQtyCount != 0) {
//                    pNewStatus = zeroQtyMinStatus;
//                } else {
//                    pNewStatus = "00";
//                }
//            } else {
//                qqRowCount1 = 0;
//                qqPrepStmt1 = null;
//                qqResultSet1 = null;
//
//                try {
//                    qqPrepStmt1 = DBHelper.executeUpdate(" SELECT Status FROM Orders WHERE OrderKey = ?");
//                    DBHelper.setValue(qqPrepStmt1, 1, pOrderkey);
//                    qqResultSet1 = qqPrepStmt1.executeQuery();
//                    if (qqResultSet1.next()) {
//                        pStatus = DBHelper.getValue(qqResultSet1, 1, pStatus);
//                        ordDtZeroQtyCount = qqRowCount1 + 1;
//                    }
//                } catch (SQLException var99) {
//                    throw new DBResourceException(var99);
//                } finally {
//                    pSession.releaseStatement(qqPrepStmt1);
//                    pSession.releaseResultSet(qqResultSet1);
//                    pSession.releaseConnection(qqConnection);
//                }
//
//                byte qqRowCount2 = 0;
//
//                PreparedStatement qqPrepStmt2 = null;
//                ResultSet qqResultSet2 = null;
//
//                try {
//                    qqPrepStmt2 = DBHelper.executeUpdate(" SELECT MAX ( Code ) FROM Orderstatussetup WHERE Orderflag = '1' AND Headerflag = '1' AND Enabled = '1' AND Code <= ?");
//                    DBHelper.setValue(qqPrepStmt2, 1, maxOrderDetailStatus);
//                    qqResultSet2 = qqPrepStmt2.executeQuery();
//                    if (qqResultSet2.next()) {
//                        maxCode = DBHelper.getValue(qqResultSet2, 1, maxCode);
//                        int var106 = qqRowCount2 + 1;
//                    }
//                } catch (SQLException var93) {
//                    throw new DBResourceException(var93);
//                } finally {
//                    pSession.releaseStatement(qqPrepStmt2);
//                    pSession.releaseResultSet(qqResultSet2);
//                    pSession.releaseConnection(qqConnection);
//                }
//
//                if (maxCode != null && maxCode.equals("09") && pStatus != null && pStatus.compareToIgnoreCase("02") >= 0 && pStatus != null && pStatus.compareToIgnoreCase("08") <= 0 && pStatus != null && pStatus.compareToIgnoreCase("06") != 0) {
//                    pNewStatus = pStatus;
//                } else if (maxCode != null && maxCode.compareToIgnoreCase("02") >= 0 && maxCode != null && maxCode.compareToIgnoreCase("08") <= 0 && pStatus != null && pStatus.compareToIgnoreCase("96") >= 0) {
//                    pNewStatus = pStatus;
//                } else {
//                    pNewStatus = maxCode;
//                }
//            }
//
//            if (pNewStatus != null && pNewStatus.equals("NA")) {
//                pNewStatus = "00";
//            }
//
//            qqRowCount1 = 0;
//            qqPrepStmt1 = null;
//            qqResultSet1 = null;
//
//            try {
//                qqPrepStmt1 = DBHelper.executeUpdate(" SELECT Headerhistoryflag FROM ORDERSTATUSSETUP WHERE Code = ?");
//                DBHelper.setValue(qqPrepStmt1, 1, pNewStatus);
//                qqResultSet1 = qqPrepStmt1.executeQuery();
//                if (qqResultSet1.next()) {
//                    pLogHistory = DBHelper.getValue(qqResultSet1, 1, pLogHistory);
//                    ordDtZeroQtyCount = qqRowCount1 + 1;
//                }
//            } catch (SQLException var101) {
//                throw new DBResourceException(var101);
//            } finally {
//                pSession.releaseStatement(qqPrepStmt1);
//                pSession.releaseResultSet(qqResultSet1);
//                pSession.releaseConnection(qqConnection);
//            }
//
//            OutboundHelper.GetOrderStatusOutputParam qqGetOrderStatusOutputParam = new OutboundHelper.GetOrderStatusOutputParam();
//            qqGetOrderStatusOutputParam.pStatus = pStatus;
//            qqGetOrderStatusOutputParam.pNewStatus = pNewStatus;
//            qqGetOrderStatusOutputParam.pLogHistory = pLogHistory;
//            var108 = qqGetOrderStatusOutputParam;
//        } finally {
//            //logger.debug("leaving getOrderStatus( String pOrderkey, DBSession pSession, String pStatus, String pNewStatus, String pLogHistory)");
//        }
//
//        return var108;
//    }
//
//
//    public boolean doesOrderDetailStatusExists(String orderKey, String statusKey) {
//        boolean var20;
//        try {
//            DBSession pSession =  context;
//            //logger.debug("starting doesOrderDetailStatusExists(DBSession pSession, String orderKey, String statusKey)");
//
//            PreparedStatement qqPrepStmt = null;
//            ResultSet qqResultSet = null;
//            boolean doesOrderDetailStatusExists = false;
//
//            try {
//                qqPrepStmt = DBHelper.executeUpdate("Select *  From ORDERDETAIL Where OrderKey = ? AND STATUS = ?");
//                qqPrepStmt.setString(1, orderKey);
//                qqPrepStmt.setString(2, statusKey);
//                qqResultSet = qqPrepStmt.executeQuery();
//                if (qqResultSet.next()) {
//                    doesOrderDetailStatusExists = true;
//                }
//            } catch (SQLException var17) {
//                SQLException qqSQLException = var17;
//                throw DBExceptionHelper.createException(var17);
//            } finally {
//                pSession.releaseStatement(qqPrepStmt);
//                pSession.releaseConnection(qqConnection);
//                pSession.releaseResultSet(qqResultSet);
//            }
//
//            var20 = doesOrderDetailStatusExists;
//        } finally {
//            //logger.debug("leaving doesOrderDetailStatusExists(DBSession pSession, String orderKey, String statusKey)");
//        }
//
//        return var20;
//    }

}
