package com.enhantec.wms.backend.outbound.ship;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.*;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.common.outbound.PickDetail;
import com.enhantec.wms.backend.framework.LegacyBaseService;import com.enhantec.wms.backend.framework.Context;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.inbound.asn.utils.ReceiptUtilHelper;
import com.enhantec.wms.backend.outbound.utils.OrderValidationHelper;
import com.enhantec.wms.backend.outbound.utils.ToWHAsnBuilder;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import static com.enhantec.wms.backend.utils.common.UtilHelper.trimZerosAndToStr;


/**
 --注册方法

 DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHShipByOrder'
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHShipByOrder', 'com.enhantec.sce.outbound.order.ship', 'enhantec', 'ShipByOrder', 'TRUE', 'JOHN',  'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,orderkey,esignaturekey','0.10','0');


 */

public class ShipByOrder extends LegacyBaseService {
//    private static ILogger log = SCELoggerFactory.getInstance(ShipByOrder.class);

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {

        Context context = null;
        Connection conn = null;

        try {
//            EXEDataObjectprocessData.getInputDataMap() = (EXEDataObject) context.theEXEDataObjectStack.stackList.get(1);

            String userid = context.getUserID();

            conn = context.getConnection();

            String orderKey = serviceDataHolder.getInputDataAsMap().getString("orderkey");

            HashMap<String, String> orderInfo = Orders.findByOrderKey(context, conn, orderKey, true);
            String PRODLOTEXPECTED =orderInfo.get("PRODLOTEXPECTED");

            if (CDSysSet.isShipByConfirm(context,conn)){
                if( !"2".equals(orderInfo.get("ISCONFIRMED"))) ExceptionHelper.throwRfFulfillLogicException("订单未复核不允许发运");
            }else {
                //如不进行复核确认则需要在发运时执行原确认动作的逻辑
                OrderValidationHelper.checkOrderTypeAndQualityStatusMatch4Alloc(context,conn,orderKey);
            }

            OrderValidationHelper.validateFieldsBeforeShip(context,conn,orderKey);

            /*  55拣货项完整   57已全部拣货/部分运送   92部分运送   95出货全部完成*/

            //checkOrderTypeAndQualityStatusMatch4Ship(context,conn,orderKey);

            if (orderInfo.get("STATUS").equals("95")) {
                ExceptionHelper.throwRfFulfillLogicException("不能重复发运订单");
            }
//            if (!(orderInfo.get("STATUS").equals("55")
//                    || orderInfo.get("STATUS").equals("57")
//                    || orderInfo.get("STATUS").equals("92"))) {
//                ExceptionHelper.throwRfFulfillLogicException("订单未拣货完成不能发运");
//            }

            String unAllocatedODCount = LegacyDBHelper.GetValue(context, conn, "select count(1) unAllocatedODCount from orderdetail where OPENQTY - QTYPICKED - QTYALLOCATED > 0 and orderkey=?", new String[]{orderKey}, "0");
            String unPickedCount = LegacyDBHelper.GetValue(context, conn, "select count(1) unPickedCount from pickdetail where ORDERKEY=? and status<5", new String[]{orderKey}, "0");

            if (!unAllocatedODCount.equals("0")
                    || !unPickedCount.equals("0")) throw new Exception("请确认订单数量已分配和拣货完毕");

            List<HashMap<String, String>> pickDetails = PickDetail.findByOrderKey(context, conn, orderKey, true);
            String esignatureKey = serviceDataHolder.getInputDataAsMap().getString("esignaturekey");
            HashMap<String, String> orderTypeConf = CodeLookup.getCodeLookupByKey(context, conn, "ORDERTYPE", orderInfo.get("TYPE"));
            //是否转仓出库类型
            if("Y".equalsIgnoreCase(orderTypeConf.get("EXT_UDF_STR5"))){
                String toWarehouse = orderInfo.get("TOWAREHOUSE");
                if(UtilHelper.isEmpty(toWarehouse)){
                    throw new Exception("未填写目标仓库，请检查订单数据");
                }else{
                    if(toWarehouse.equalsIgnoreCase(conn.getSchema())){
                        throw new Exception("目标仓库不能与本仓库一致");
                    }
                    generateTargetWarehouseAsn(context,conn,pickDetails,toWarehouse,
                            orderInfo,esignatureKey);
                }
            }


            for(HashMap<String, String> pickDetail: pickDetails){
                if(!pickDetail.get("STATUS").equals("9")) {
                    if (!UtilHelper.isEmpty(PRODLOTEXPECTED)){
                        //如果orders目标收货批次不为空 将目标收货批次放在idnotes
                        DBHelper.executeUpdate(context,conn,"update IDNOTES SET PRODLOTEXPECTED = ? WHERE ID = ? ", new Object[]{PRODLOTEXPECTED , pickDetail.get("ID")});
                    }
                    HashMap<String, String> shippedIdNotesHashMap = IDNotes.decreaseWgtById(context, conn, new BigDecimal(pickDetail.get("QTY")), pickDetail.get("ID"));
                    if(UtilHelper.decimalStrCompare(shippedIdNotesHashMap.get("NETWGT"), "0")==0) {
                        // ExceptionHelper.throwRfFulfillLogicException("待发运的容器条码/箱号" + pickDetail.get("ID") + "发运时扣库存异常(不为零)，发运失败");
                        //校验发运的IDNOTES库存余额应为0并移至历史表，否则报错
                        IDNotes.archiveIDNotes(context, conn, shippedIdNotesHashMap);
                    }
                }

            }

            serviceDataHolder.getInputDataAsMap().setAttribValue("TransactionStarted", "true");
//            Process shipProcess = context.searchObjectLibrary("NSPMASSSHIPORDERS"));
//            shipProcess.execute(context);
//            context.theEXEDataObjectStack.pop();

            HashMap<String, String> updatedOrderInfo = Orders.findByOrderKey(context, conn, orderKey, true);
            if (!"95".equals(updatedOrderInfo.get("STATUS")))
                ExceptionHelper.throwRfFulfillLogicException("发运失败，请检查订单数据是否正确");


            Udtrn UDTRN = new Udtrn();

            if(esignatureKey.indexOf(':')==-1){
                UDTRN.EsignatureKey=esignatureKey;
            }else {
                //复核
                String[] eSignatureKeys = esignatureKey.split(":");
                UDTRN.EsignatureKey=eSignatureKeys[0];
                UDTRN.EsignatureKey1=eSignatureKeys[1];
            }

            UDTRN.FROMTYPE = "按订单发运";
            UDTRN.FROMTABLENAME = "ORDERS";
            UDTRN.FROMKEY = orderInfo.get("ORDERKEY");
            UDTRN.FROMKEY1 = "";
            UDTRN.FROMKEY2 = "";
            UDTRN.TITLE01 = "订单号";
            UDTRN.CONTENT01 = orderInfo.get("ORDERKEY");

            conn = context.getConnection();

            UDTRN.Insert(context, conn, context.getUserID());

            //生基采购退货还po单客户化 创建单据时控制一个出库单仅出一个批次 直接查询拣货数量 归还po数量
            HashMap<String, String> orderTypeEntry = CodeLookup.getCodeLookupByKey(context, conn, "ORDERTYPE", orderInfo.get("TYPE"));
            if ("Y".equalsIgnoreCase(orderTypeEntry.get("EXT_UDF_STR4"))) {
                String pokey=orderInfo.get("BUYERPO");
                String poLine=orderInfo.get("BUYERPOLINE");
                for(HashMap<String, String> pickDetail: pickDetails) {
                    String pickqty =pickDetail.get("QTY");
                    HashMap<String, String> skuHash = SKU.findById(context, conn, pickDetail.get("SKU"),true);
                    BigDecimal updateqty = ReceiptUtilHelper.stdQty2PoWgt(context,conn,skuHash.get("SNAVGWGT"),new BigDecimal(pickqty),pickDetail.get("SKU"));
                    LegacyDBHelper.ExecSql(context, conn
                            , "Update WMS_PO_DETAIL set RECEIVEDQTY=ISNULL(RECEIVEDQTY,0)-? where POKEY=? and POLINENUMBER=?"
                            , new String[]{trimZerosAndToStr(updateqty),pokey,poLine});

                }
            }


        }catch (Exception e)
        {
           try
           {
               context.releaseConnection(conn);
           }catch (Exception e1) {

           }
           if ( e instanceof FulfillLogicException)
               throw (FulfillLogicException)e;
           else
               throw new FulfillLogicException(e.getMessage());
        }finally {
            try	{	context.releaseConnection(conn); }	catch (Exception e1) {		}
        }
    }

    /**
     * 仓库间转移
     */
    private void generateTargetWarehouseAsn(Context context, Connection connection, List<HashMap<String,String>> pickDetails, String toWarehouseName,HashMap<String,String> orderInfo,String esignatureKey)throws Exception{
        String fromWarehouseName = connection.getSchema();
        HashMap<String,String> fromWarehouseConf = CodeLookup.getCodeLookupByKey(context, connection, "WHTRANFER", fromWarehouseName.toUpperCase());
        HashMap<String,String> toWarehouseConf = CodeLookup.getCodeLookupByKey(context, connection, "WHTRANFER", toWarehouseName);


        boolean gmpToGmp = "Y".equalsIgnoreCase(fromWarehouseConf.get("UDF1")) && "Y".equalsIgnoreCase(toWarehouseConf.get("UDF1"));
        boolean gmpToNonGmp = "Y".equalsIgnoreCase(fromWarehouseConf.get("UDF1")) && "N".equalsIgnoreCase(toWarehouseConf.get("UDF1"));
        boolean nonGmpToGmp = "N".equalsIgnoreCase(fromWarehouseConf.get("UDF1")) && "Y".equalsIgnoreCase(toWarehouseConf.get("UDF1"));
        boolean nonGmpToNonGmp = "N".equalsIgnoreCase(fromWarehouseConf.get("UDF1")) && "N".equalsIgnoreCase(toWarehouseConf.get("UDF1"));

        String receiptKey = null;

        int seq = 0;
        ToWHAsnBuilder toWHAsnBuilder = new ToWHAsnBuilder(context, connection, toWarehouseName,getToWarehouseReceiptType(context,connection,orderInfo.get("TYPE"),toWarehouseName));
        for (HashMap<String, String> pickDetail : pickDetails) {
            HashMap<String, String> fromSkuMap = SKU.findById(context, connection, pickDetail.get("SKU"), true);
            String toSKU = pickDetail.get("SKU");
            if(gmpToNonGmp){
                toSKU = fromSkuMap.get("EXT_UDF_STR3");//GMP转NON-GMP使用JDE编码
            }
            if(nonGmpToGmp){
                String sql = "SELECT SKU FROM " + orderInfo.get("TOWAREHOUSE") + ".SKU WHERE SKU = ? AND EXT_UDF_STR3 = ?";
                List<HashMap<String, String>> toWareHouseSku = DBHelper.executeQuery(context, connection, sql,
                        new Object[]{orderInfo.get("TOGMPSKU"), pickDetail.get("SKU")});
                if (null == toWareHouseSku) {
                    ExceptionHelper.throwRfFulfillLogicException("目标GMP仓库编码信息不存在");
                }
                toSKU = orderInfo.get("TOGMPSKU");
            }
            HashMap<String, String> toSkuMap = DBHelper.getRecord(context, connection,
                    "SELECT * FROM " + toWarehouseName + ".SKU WHERE SKU = ?",
                    new Object[]{toSKU}, "仓库"+toWarehouseConf.get("DESCRIPTION")+"物料代码为"+toSKU+"的物料", true);
            if(!fromSkuMap.get("PACKKEY").equals(toSkuMap.get("PACKKEY"))){
                throw new Exception("仓库"+fromWarehouseConf.get("DESCRIPTION")+"的物料"+fromSkuMap.get("SKU")+
                        "与仓库"+toWarehouseConf.get("DESCRIPTION")+"的物料"+toSKU+"包装不一致，不允许转仓");
            }

            if ((SKU.isSerialControl(context, connection, pickDetail.get("SKU")) && !"1".equals(toSkuMap.get("SNUM_ENDTOEND")))
                    || (!SKU.isSerialControl(context, connection, pickDetail.get("SKU")) && "1".equals(toSkuMap.get("SNUM_ENDTOEND")))) {
                throw new Exception("转仓前物料"+pickDetail.get("SKU")+"与转仓后物料"+toSKU+"唯一码管理方式不同，不允许转仓");
            }

            if(null == receiptKey) {
                String isConfirmedUser1 = "";
                String isConfirmedUser2 = "";
                if(esignatureKey.indexOf(":") == -1) {
                    isConfirmedUser1 = DBHelper.getValue(context, connection,
                            "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ",
                            new Object[]{esignatureKey}, String.class, "确认人");
                }else{
                    String[] eSignatureKeys = esignatureKey.split(":");
                    String eSignatureKey1=eSignatureKeys[0];
                    String eSignatureKey2=eSignatureKeys[1];
                    isConfirmedUser1 = DBHelper.getValue(context, connection, "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                            eSignatureKey1
                    }, String.class, "确认人");

                    isConfirmedUser2 = DBHelper.getValue(context, connection, "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                            eSignatureKey2
                    }, String.class, "复核人");
                }

                receiptKey = toWHAsnBuilder.buildReceiptHeadInfo(isConfirmedUser1,isConfirmedUser2);
            }
            HashMap<String, String> fromIdNotesHashMap = LotxLocxId.findById(context, connection, pickDetail.get("ID"), true);
            String receiptLineNumber = toWHAsnBuilder.buildReceiptDetailInfo(receiptKey,toSKU,++seq,fromIdNotesHashMap);
            if(SKU.isSerialControl(context,connection,fromIdNotesHashMap.get("SKU"))){
                List<HashMap<String, String>> pickLotXIdDetail = LotxId.findDetailsByPickDetailKey(context, connection, pickDetail.get("PICKDETAILKEY"), true);
                String [] snList = new String[pickLotXIdDetail.size()];
                String [] snWgtList = new String[pickLotXIdDetail.size()];
                String [] snUomList = new String[pickLotXIdDetail.size()];
                for (int i = 0; i < pickLotXIdDetail.size(); i++) {
                    HashMap<String, String> serialNumberList = SerialInventory.findBySkuAndSN(context, connection, fromIdNotesHashMap.get("SKU"),
                            pickLotXIdDetail.get(i).get("OOTHER1"), true);
                    snList[i] = serialNumberList.get("SERIALNUMBER");
                    snWgtList[i] = serialNumberList.get("NETWEIGHT");
                    snUomList[i] = serialNumberList.get("DATA2");
                }
                toWHAsnBuilder.buildReceiptLotxIdInfo(toSKU,pickDetail.get("ID"),receiptKey,receiptLineNumber,
                        snList,snWgtList,snUomList);
            }

        }
    }
    private String getToWarehouseReceiptType(Context context,Connection connection,String orderType,String toWarehouse) throws Exception{
        HashMap<String, String> toWareHouseConf = DBHelper.getRecord(context, connection,
                "SELECT * FROM CODELKUP WHERE LISTNAME = 'TOWHCONF' AND UDF1 = ? AND UDF2 = ?",
                new Object[]{toWarehouse, orderType}, "", false);
        if(null == toWareHouseConf || toWareHouseConf.isEmpty()){
            toWareHouseConf = DBHelper.getRecord(context,connection,
                    "SELECT * FROM CODELKUP WHERE LISTNAME = 'TOWHCONF' AND UDF1 = ?",
                            new Object[]{toWarehouse},
                            "",false);
        }
        if(null == toWareHouseConf || toWareHouseConf.isEmpty()){
            toWareHouseConf = DBHelper.getRecord(context,connection,
                    "SELECT * FROM CODELKUP WHERE LISTNAME = 'TOWHCONF' AND UDF2 = ?",
                    new Object[]{orderType},
                    "",false);
        }
        if(null == toWareHouseConf || toWareHouseConf.isEmpty()){
            toWareHouseConf = DBHelper.getRecord(context,connection,
                    "SELECT * FROM CODELKUP WHERE LISTNAME = 'TOWHCONF' " +
                            "AND (UDF1 IS NULL OR UDF1 = '') AND (UDF2 IS NULL OR UDF2 = '')",
                    new Object[]{},
                    "",false);
        }
        if(null == toWareHouseConf || toWareHouseConf.isEmpty()){
            throw new Exception("未找到转仓出库功能转移至仓库的入库类型的配置");
        }
        if(UtilHelper.isEmpty(toWareHouseConf.get("UDF3"))){
           throw new Exception("转仓出库功能配置的入库类型为空，请检查配置");
        }
        return toWareHouseConf.get("UDF3");


    }

}
