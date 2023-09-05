package com.enhantec.wms.backend.inventory.repackaging;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;

import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.WMSBaseService;import com.enhantec.framework.common.utils.EHContextHelper;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.outbound.OutboundUtils;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 --注册方法

 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME = 'EHRepackaging';
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHRepackaging', 'com.enhantec.sce.inventory.repackaging', 'enhantec', 'Repackaging','TRUE','JOHN','JOHN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ORDERKEY,ORDERLINENUMBER,ESIGNATUREKEY','0.10','0');


 **/

public class Repackaging extends WMSBaseService {

    private static final long serialVersionUID = 1L;

    public Repackaging()
    {
    }

    public void execute(ServiceDataHolder serviceDataHolder)
    {
        String userid = EHContextHelper.getUser().getUsername();



        try
        {

//            context.theSQLMgr.transactionBegin();

            final String orderKey = serviceDataHolder.getInputDataAsMap().getString("ORDERKEY");
            final String orderLineNumber = serviceDataHolder.getInputDataAsMap().getString("ORDERLINENUMBER");
            final String esignatureKey= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

            if (UtilHelper.isEmpty(orderKey)) throw new Exception("订单号不能为空");
            if (UtilHelper.isEmpty(orderLineNumber)) throw new Exception("订单行号不能为空");
            if (UtilHelper.isEmpty(esignatureKey)) throw new Exception("电子签名不能为空");


            Map<String,String> orderHashMap = Orders.findByOrderKey(orderKey,true);
            Map<String,String> orderDetailHashMap = Orders.findOrderDetailByKey(orderKey,orderLineNumber,true);

            String repackReceiptKey = orderDetailHashMap.get("SUSR1");

            String rePackOrderKey = orderDetailHashMap.get("SUSR2");

            if(UtilHelper.isEmpty(repackReceiptKey)) ExceptionHelper.throwRfFulfillLogicException("找不到该订单行正在进行的分装收货单，请先生成分装签");

            Map<String,String> receiptHashMap = Receipt.findByReceiptKey( repackReceiptKey,true);

            String lottable06 =receiptHashMap.get("SUSR2");

            String sku = orderDetailHashMap.get("SKU");
            String packKey = orderDetailHashMap.get("PACKKEY");

            String stdUOM = UOM.getStdUOM(packKey);

            //如果repackOrderKey不为空，说明单据已经创建但未自动完成后续操作，需要手工执行。
            if(UtilHelper.isEmpty(rePackOrderKey)) {

                String receiptDetailsSql = "SELECT * FROM RECEIPTDETAIL WHERE STATUS = '0' AND receiptKey = ?  ";
                List<Map<String, String>> receiptDetailList = DBHelper.executeQuery( receiptDetailsSql, new Object[]{repackReceiptKey});
                if (receiptDetailList.size() == 0)
                    ExceptionHelper.throwRfFulfillLogicException("未找到收货单" + repackReceiptKey + "下待收货的分装标签");

                String currentPackLoc = orderDetailHashMap.get("SUSR3");

                String STORERKEY = orderDetailHashMap.get("STORERKEY");
                //String repackReceiptType= XtSql.GetValue( "select udf1 from codelkup where listname=? and code=?", new String[]{"SYSSET","REPACKRECT"}, "");
                String repackOrderType = DBHelper.getValue( "select udf1 from codelkup where listname=? and code=?", new String[]{"SYSSET", "REPACKORDT"}, "");
                if (UtilHelper.isEmpty(repackOrderType)) ExceptionHelper.throwRfFulfillLogicException("分装出库单类型代码未设置");

                String projectCode = orderHashMap.get("NOTES");
                rePackOrderKey = LegacyDBHelper.GetNCounterBill( "ORDER");

                Map<String,String> repackOrderHashMap = new HashMap<>();
                repackOrderHashMap.put("AddWho", userid);
                repackOrderHashMap.put("EditWho", userid);
                repackOrderHashMap.put("type", repackOrderType);
                repackOrderHashMap.put("ohtype", repackOrderType);
                repackOrderHashMap.put("SUSR4", receiptHashMap.get("SUSR4"));//关联的领料订单行+行号
                repackOrderHashMap.put("status", "06");
                repackOrderHashMap.put("orderkey", rePackOrderKey);
                repackOrderHashMap.put("externorderkey", receiptHashMap.get("EXTERNRECEIPTKEY"));
                repackOrderHashMap.put("REFERENCENUM", lottable06);
                repackOrderHashMap.put("storerkey", STORERKEY);
                repackOrderHashMap.put("notes", projectCode);
                repackOrderHashMap.put("ISCONFIRMED", "2");
                LegacyDBHelper.ExecInsert( "orders", repackOrderHashMap);
                //记录正在进行的分装出库单号，如果一切执行正常，SUSR1分装入库单号,SUSR2分装出库单号会被自动清空。
                DBHelper.executeUpdate( "UPDATE ORDERDETAIL SET SUSR2 = ? WHERE ORDERKEY = ? AND ORDERLINENUMBER = ? ", new Object[]{
                        rePackOrderKey, orderKey, orderLineNumber});

                //add order lines
                List<Map<String, String>> leftLpnList = RepackgingUtils.getLeftLPNListFromStr(receiptHashMap.get("SUSR5"));

                //添加完全消耗的备货LPN
                List<Map<String, String>> allPreparedIds = DBHelper.executeQuery(
                        "SELECT a.ID, QTY FROM LOTXLOCXID a, v_lotattribute b WHERE  a.lot=b.lot " +
                                " and a.LOC = ? AND a.SKU = ? AND b.LOTTABLE06 = ? AND a.QTY>0",
                        new Object[]{currentPackLoc, sku, lottable06});

                for (Map<String, String> idRec : allPreparedIds) {

                    Boolean existInLeftLpnList = leftLpnList.stream().anyMatch(x -> x.get("ID").equals(idRec.get("ID")));

                    if (!existInLeftLpnList) {
                        leftLpnList.add(new HashMap<String, String>() {{
                            put("ID", idRec.get("ID"));
                            put("UOM", stdUOM);
                            put("LEFTQTY", "0");
                        }});
                    }

                }


                //////////////

                BigDecimal totalUsedQty = BigDecimal.ZERO;

                if (leftLpnList.size() > 0) {

                    for (Map<String, String> leftLpnInfo : leftLpnList) {

                        //////////
                        //校验容器的可用量应大于分装量
                        Map<String, String> idHashMap = LotxLocxId.findAvailInvById( leftLpnInfo.get("ID"), true, true);
                        BigDecimal leftLpnStdQty = UOM.UOMQty2StdQty( packKey, leftLpnInfo.get("UOM"), new BigDecimal(leftLpnInfo.get("LEFTQTY")));
                        BigDecimal idUsedStdQty = new BigDecimal(idHashMap.get("QTY")).subtract(leftLpnStdQty);


                        BigDecimal idAvailableQty = new BigDecimal(idHashMap.get("AVAILABLEQTY"));

                        if (idUsedStdQty.compareTo(idAvailableQty) > 0)
                            ExceptionHelper.throwRfFulfillLogicException("容器" + leftLpnInfo.get("ID") + "的分装量不能大于可用量");

                        if (!UtilHelper.equals(idHashMap.get("STATUS"),"OK"))
                            ExceptionHelper.throwRfFulfillLogicException("容器" + leftLpnInfo.get("ID") + "为冻结状态，请将其移出分装间后再进行分装");


                        /////

                        if (idUsedStdQty.compareTo(BigDecimal.ZERO) > 0) {

                            ///////生成分装出库单行号
                            Object preOrderLineNumberObj = DBHelper.getValue( "SELECT MAX(ORDERLINENUMBER) FROM ORDERDETAIL WHERE ORDERKEY = ?",
                                    new Object[]{rePackOrderKey}, "");

                            int orderLineNumberInt = preOrderLineNumberObj == null ? 1 : Integer.parseInt(preOrderLineNumberObj.toString()) + 1;

                            String repackOrderLineNumber = orderLineNumberInt + "";

                            while (repackOrderLineNumber.length() < 5)
                                repackOrderLineNumber = "0" + repackOrderLineNumber;
                            ///

                            Map<String,String> repackOrderDetail = new HashMap<String,String>();
                            repackOrderDetail.put("ADDWHO", userid);
                            repackOrderDetail.put("EDITWHO", userid);
                            repackOrderDetail.put("STATUS", "04");
                            repackOrderDetail.put("ORDERKEY", rePackOrderKey);
                            repackOrderDetail.put("EXTERNORDERKEY", orderHashMap.get("EXTERNORDERKEY"));
                            repackOrderDetail.put("ORDERLINENUMBER", repackOrderLineNumber);
                            repackOrderDetail.put("EXTERNLINENO", "WMS"+repackOrderLineNumber);
                            //OrderDetail.put("EXTERNLINENO", PROJECT);
                            repackOrderDetail.put("STORERKEY", STORERKEY);
                            repackOrderDetail.put("SKU", sku);
                            repackOrderDetail.put("ORIGINALQTY", String.valueOf(idUsedStdQty));
                            repackOrderDetail.put("OPENQTY", String.valueOf(idUsedStdQty));
                            repackOrderDetail.put("PACKKEY", packKey);
                            repackOrderDetail.put("UOM", leftLpnInfo.get("UOM"));
                            //orderDetail.put("SUSR4", idHashMap.get("BARRELDESCR")); //桶号
                            repackOrderDetail.put("IDREQUIRED", leftLpnInfo.get("ID"));
                            repackOrderDetail.put("LOTTABLE06", lottable06);
                            repackOrderDetail.put("NEWALLOCATIONSTRATEGY", "N21"); //分配策略:匹配数量，然后最佳适配
                            LegacyDBHelper.ExecInsert( "orderdetail", repackOrderDetail);

                            totalUsedQty = totalUsedQty.add(idUsedStdQty);
                        }

                    }
                }

                if((totalUsedQty.compareTo(BigDecimal.ZERO)==0)) ExceptionHelper.throwRfFulfillLogicException("未找到待分装的容器，分装消耗量必须大于0");


                BigDecimal totalReceivedQty = receiptDetailList.stream().map(x -> new BigDecimal(x.get("QTYEXPECTED"))).reduce(BigDecimal.ZERO, BigDecimal::add);

                //如果分装间已分装量>订单行最大待分装量（订单行未结数量-订单行分配量-订单行拣货量），则增加该订单行的需求数量已容纳分装多余的量
                String tempQty = UtilHelper.decimalStrSubtract(orderDetailHashMap.get("OPENQTY"),orderDetailHashMap.get("QTYALLOCATED"));
                String repackQtyExpected = UtilHelper.decimalStrSubtract(tempQty,orderDetailHashMap.get("QTYPICKED"));
                String needIncreasedOpenQty = UtilHelper.decimalStrSubtract(totalReceivedQty.toPlainString(), repackQtyExpected);

                if(UtilHelper.decimalStrCompare(needIncreasedOpenQty,"0")>0){

                    DBHelper.executeUpdate( "UPDATE ORDERDETAIL SET OpenQty = OpenQty + ?, EditWho = ?, EditDate = ? WHERE Orderkey = ? AND OrderLineNumber = ?"
                            , new Object[]{
                                    needIncreasedOpenQty,
                                    userid,
                                    UtilHelper.getCurrentSqlDate(),
                                    orderKey,
                                    orderLineNumber}
                    );

                }

                Udtrn UDTRN = new Udtrn();

                if(esignatureKey.indexOf(':')==-1){
                    //取样自动生成
                    UDTRN.EsignatureKey=esignatureKey;
                }else {
                    //复核
                    String[] eSignatureKeys = esignatureKey.split(":");
                    UDTRN.EsignatureKey=eSignatureKeys[0];
                    UDTRN.EsignatureKey1=eSignatureKeys[1];
                }

                UDTRN.FROMTYPE = "执行分装";
                UDTRN.FROMTABLENAME = "ORDERDETAIL";
                UDTRN.FROMKEY = currentPackLoc;
                UDTRN.FROMKEY1 = orderKey + orderLineNumber;
                UDTRN.FROMKEY2 = repackReceiptKey;
                UDTRN.FROMKEY3 = rePackOrderKey;
                UDTRN.TITLE01 = "分装批次";
                UDTRN.CONTENT01 = lottable06;
                UDTRN.TITLE02 = "出库单号及行号";
                UDTRN.CONTENT02 = orderKey + orderLineNumber;
                UDTRN.TITLE03 = "分装入库单号";
                UDTRN.CONTENT03 = repackReceiptKey;
                UDTRN.TITLE04 = "分装入库数量";
                UDTRN.CONTENT04 = UtilHelper.trimZerosAndToStr(totalReceivedQty);
                UDTRN.TITLE05 = "分装出库单号";
                UDTRN.CONTENT05 = rePackOrderKey;
                UDTRN.TITLE06 = "分装出库数量";
                UDTRN.CONTENT06 = UtilHelper.trimZerosAndToStr(totalUsedQty);
                UDTRN.TITLE07 = "损耗量";
                UDTRN.CONTENT07 = UtilHelper.decimalStrSubtract(totalUsedQty.toPlainString(), totalReceivedQty.toPlainString());
                UDTRN.Insert( userid);

               // context.theSQLMgr.transactionCommit();


                //*****创建分装出库单和入库单完成,开始自动收发货*******

                try {

//                    context.theSQLMgr.transactionBegin();
        
                    //分装出库单发货
                    OutboundUtils.allocateAndShip( rePackOrderKey);
                    //发货后立刻提交事务，保证当前分配和发运操作的业务数据完整性。
//                    context.theSQLMgr.transactionCommit();

                } catch (Exception e) {
//                    context.theSQLMgr.transactionAbort();
                    ExceptionHelper.throwRfFulfillLogicException("分装出入库单创建成功，但分装出库单发运未成功，请检查单据并手工执行后续操作后重试");
                }


                //分装入库单收货

                try {
//                    context.theSQLMgr.transactionBegin();

        
                    ServiceHelper.executeService( "EHReceiveAll",
                            new ServiceDataHolder(new ServiceDataMap(
                            new HashMap<String,Object>() {{
                        put("RECEIPTKEY", repackReceiptKey);
                        put("ESIGNATUREKEY","");
                    }})));


                    Map<String,String> receivedReceiptHashMap = Receipt.findByReceiptKey( repackReceiptKey,true);

                    if (!receivedReceiptHashMap.get("STATUS").equals("9")){
                        ExceptionHelper.throwRfFulfillLogicException("已执行过分装，但未自动完成。分装出库单" + rePackOrderKey + "已完成，但分装入库单"+repackReceiptKey+"收货未能完成，请手工进行后续分装入库单收货、领料单分配及拣货操作后重试");
                    }else {

                        DBHelper.executeUpdate( "UPDATE RECEIPT SET STATUS = 11 WHERE RECEIPTKEY = ?", new Object[]{repackReceiptKey});
                        DBHelper.executeUpdate( "UPDATE RECEIPTDETAIL SET STATUS = 11 WHERE RECEIPTKEY = ? AND STATUS = 9 ", new Object[]{repackReceiptKey});

                    }
//                    context.theSQLMgr.transactionCommit();

                } catch (Exception e) {
//                    context.theSQLMgr.transactionAbort();
                    ExceptionHelper.throwRfFulfillLogicException("分装出库单发运已完成，但分装入库单收货失败，请检查单据并手工执行后续操作后重试");
                }

                //领料单自动拣货
                try {
//                    context.theSQLMgr.transactionBegin();
                    //自动分配分装收货单的库存到原领料单并自动拣货
                    addPickDetails2Order( orderDetailHashMap, receiptDetailList);

//                    context.theSQLMgr.transactionCommit();


                } catch (Exception e) {
//                    context.theSQLMgr.transactionAbort();
                    ExceptionHelper.throwRfFulfillLogicException("分装出入库单收货、发货均已完成，但未能自动拣货至领料单，请检查单据并手工执行后续操作后重试");
                }
            }else{

                Map<String,String> rePackOrderHashMap = Orders.findByOrderKey(rePackOrderKey,true);

                if(!rePackOrderHashMap.get("STATUS").equals("95")){
                    ExceptionHelper.throwRfFulfillLogicException("已执行过分装，但未自动完成。请手工对分装出库单"+rePackOrderKey+"进行发货和对分装入库单"+repackReceiptKey+"进行收货，最后对关联的领料单进行分配及拣货后重试");
                }

                if (!receiptHashMap.get("STATUS").equals("9") && !receiptHashMap.get("STATUS").equals("11")){
                        ExceptionHelper.throwRfFulfillLogicException("已执行过分装，但未自动完成。当前分装出库单" + rePackOrderKey + "已完成，但分装入库单"+repackReceiptKey+"收货并自动拣货至领料单未能完成，请手工进行后续分装入库单收货、领料单分配及拣货操作后重试");
                }

            }

            //清空正在分装的出入库单号和已分装完成的单据列表:
            // SUSR1 分装入库单号
            // SUSR2 分装出库单号
            // SUSR4 已分装完成的单据列表, 格式:  收货批次1|分装入库单号1|分装出库单号1 ; 收货批次2|分装入库单号2分装出库单号2;
            DBHelper.executeUpdate("UPDATE ORDERDETAIL SET SUSR1 = null, SUSR2 = null, SUSR4 = CONCAT(SUSR4, ?) WHERE ORDERKEY = ? AND ORDERLINENUMBER = ? ",new Object[]{
                    lottable06+"|"+repackReceiptKey+"|"+rePackOrderKey+";",  orderKey ,  orderLineNumber });

          
        }
        catch (Exception e)
        {
//            context.theSQLMgr.transactionAbort();
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());
        }finally {
            
        }

    }

    private void addPickDetails2Order( Map<String, String> orderDetailHashMap, List<Map<String, String>> receiptDetailList) throws SQLException {

        ServiceDataMap thePickDO = new ServiceDataMap();

        for(Map<String,String> receiptDetail: receiptDetailList) {

            Map<String,String> lotxLocxIdInfo = LotxLocxId.findById(receiptDetail.get("TOID"),true);


            String pickDetailKey = IdGenerationHelper.getNCounterStrWithLength("PICKDETAILKEY", 10);

//            thePickDO.clearDO();
//            thePickDO.setConstraintItem("pickdetailkey", pickDetailKey);
//            thePickDO.setWhereClause(" WHERE PickDetailKey = :pickdetailkey");
//            context.theEXEDataObjectStack.push(thePickDO);
//            context.theSQLMgr.searchTriggerLibrary("PickDetail")).preInsertFire();

            String caseId = IdGenerationHelper.getNCounterStrWithLength("CARTONID", 10);

            double grosswgt = 0.0D;
            double netwgt = 0.0D;
            double tarewgt = 0.0D;
            String stdUOM = UOM.getStdUOM( lotxLocxIdInfo.get("PACKKEY"));
            //right now just provide stduom, it should be always 6
            String uom = UOM.getUOMCode( lotxLocxIdInfo.get("PACKKEY"), stdUOM);

            DBHelper.executeUpdate(" INSERT INTO PICKDETAIL ( PickDetailKey, CaseID, PickHeaderkey, OrderKey, OrderLineNumber, Lot, Storerkey, Sku, PackKey, UOM, UOMQty, Qty, Loc, ToLoc, ID, CartonGroup, CartonType, DoReplenish, ReplenishZone, DoCartonize, PickMethod, AddWho, EditWho, SeqNo, StatusRequired,fromloc, SelectedCartonType, SelectedCartonID, grosswgt, netwgt, tarewgt, PickContPlacement, status ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,? )",
            new Object[]{ pickDetailKey
            , caseId
            , " "
            , orderDetailHashMap.get("ORDERKEY")
            , orderDetailHashMap.get("ORDERLINENUMBER")
            , lotxLocxIdInfo.get("LOT")
            , lotxLocxIdInfo.get("STORERKEY")
            , lotxLocxIdInfo.get("SKU")
            , lotxLocxIdInfo.get("PACKKEY")
            , uom
            , lotxLocxIdInfo.get("QTY")
            , lotxLocxIdInfo.get("QTY")
            , lotxLocxIdInfo.get("LOC")
            , ""
            , lotxLocxIdInfo.get("ID")
            , "STD"//carton group
            , "CASE"//carton type
            , "N"//DOREPLENISH
            , " "//REPLENISHZONE
            , "N"//DOCARTONIZE
            , "3"//PICKMETHOD 1:定向/3辅助
            , EHContextHelper.getUser().getUsername()
            , EHContextHelper.getUser().getUsername()
            , 99999
            , "OK"//STATUSREQUIRED
            , lotxLocxIdInfo.get("LOC")
            , "CASE"//SELECTEDCARTONTYPE
            , caseId
            , grosswgt
            , netwgt
            , tarewgt
            , "0"//PICKCONTPLACEMENT 拣货优先级
            , "0" }//status
          );


            //todo
//            EXEDataObject triggerDO = new EXEDataObject();
//            triggerDO.setAttribValue("qty"),
//                    lotxLocxIdInfo.get("QTY")));
//            triggerDO.setAttribValue("sku"),
//                    lotxLocxIdInfo.get("SKU")));
//            triggerDO.setAttribValue("storerkey"),
//                    lotxLocxIdInfo.get("STORERKEY")));
//
//            triggerDO.setAttribValue("fromloc"),
//                    lotxLocxIdInfo.get("LOC")));
//            triggerDO.setAttribValue("loc"),
//                    lotxLocxIdInfo.get("LOC")));
//            triggerDO.setAttribValue("pickdetailkey"),
//                    pickDetailKey));
//            triggerDO.setAttribValue("orderkey"),
//                    orderDetailHashMap.get("ORDERKEY")));
//            triggerDO.setAttribValue("orderlinenumber"),
//                    orderDetailHashMap.get("ORDERLINENUMBER")));
//            triggerDO.setAttribValue("lot"),
//                    lotxLocxIdInfo.get("LOT")));
//            triggerDO.setAttribValue("id"), lotxLocxIdInfo.get("ID")));
//            triggerDO.setAttribValue("pickmethod"),
//                    "3"));
//            triggerDO.setAttribValue("uom"),
//                    uom));
//            triggerDO.setAttribValue("PACKKEY"),
//                    lotxLocxIdInfo.get("PACKKEY")));
//
//            context.theEXEDataObjectStack.push(triggerDO);
//
//            context.theSQLMgr.searchTriggerLibrary("PickDetail")).postInsertFire();

            //更新拣货明细为已拣货
//            context.theSQLMgr.searchTriggerLibrary("PickDetail")).preUpdateFire();
            DBHelper.executeQuery("UPDATE PICKDETAIL SET LOC = ?, TOLOC = ?, STATUS = ? WHERE PICKDETAILKEY = ? ",
            new Object[]{ "PICKTO"
            , "PICKTO"
            , "5" //status: 拣货完成
            , pickDetailKey});

            //todo
//            EXEDataObject pdUpdateTriggerDO = new EXEDataObject();
//            pdUpdateTriggerDO.setAttribValue("TOLOC"), "PICKTO"));
//            pdUpdateTriggerDO.setAttribValue("LOC"), "PICKTO"));
//            pdUpdateTriggerDO.setAttribValue("FROMLOC"), lotxLocxIdInfo.get("LOC")));
//            pdUpdateTriggerDO.setAttribValue("STATUS"), "5"));
//            context.theEXEDataObjectStack.push(pdUpdateTriggerDO);
//            context.theSQLMgr.searchTriggerLibrary("PickDetail")).postUpdateFire();

        }

    }

}