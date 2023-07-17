package com.enhantec.wms.backend.outbound;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.outbound.allocation.OrderProcessingP1S1;
import com.enhantec.wms.backend.utils.common.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class OutboundUtils {

    public static void checkQtyIsAvailableInLotxLocxId( String ID, BigDecimal qty, BigDecimal allocatedQty) {
        HashMap<String, String> lotxLocxIdHashMap =  LotxLocxId.findById( ID,true);

        BigDecimal availQty = new BigDecimal(lotxLocxIdHashMap.get("AVAILABLEQTY"));

        BigDecimal maxAllowPickQty = allocatedQty.add(availQty);
        if(qty.compareTo(maxAllowPickQty)>0)
            ExceptionHelper.throwRfFulfillLogicException(ID+"的拣货数量"+ UtilHelper.trimZerosAndToStr(qty)+"大于最大可拣货量"+UtilHelper.trimZerosAndToStr(availQty));
    }

    public static void checkQtyIsAvailableInIDNotes( String ID, BigDecimal qty) {
        HashMap<String, String> idNotesHashMap = IDNotes.findById( ID,true);

        BigDecimal idNotesQty = new BigDecimal(idNotesHashMap.get("NETWGT"));

        if( idNotesQty.compareTo(qty)<0) ExceptionHelper.throwRfFulfillLogicException(ID+"的拣货数量"+ UtilHelper.trimZerosAndToStr(qty)+"大于标签量"+UtilHelper.trimZerosAndToStr(idNotesQty));
    }


    public static void allocateAndShip( String orderKey) throws Exception {
        allocateAndShip(orderKey,true);
    }
    public static void allocateAndShip( String orderKey, boolean shipWithIDNotes) throws Exception {
        //Start allocation & ship
        if(shipWithIDNotes) {

            //同步减少IDNOTES的库存
            List<HashMap<String,String>> Details = DBHelper.executeQuery( "select ORDERLINENUMBER,STORERKEY,SKU,IDREQUIRED,LOTTABLE06,SUSR1,SUSR3,STATUS,PACKKEY, UOM"
                            + ",ORIGINALQTY,OPENQTY,SHIPPEDQTY,QTYPREALLOCATED,QTYALLOCATED,QTYPICKED,UOM,PACKKEY "
                            + " from orderdetail where orderkey=? order by orderlinenumber", new String[]{orderKey});

            for (int iDetail = 0; iDetail < Details.size(); iDetail++) {

                HashMap<String,String> mDetail = Details.get(iDetail);
                String id = mDetail.get("IDREQUIRED");
                String openQty = mDetail.get("ORIGINALQTY");

                IDNotes.decreaseWgtByIdWithAvailQtyCheck( id, openQty, "分装数量");
            }
        }

            //减少系统库存
        ServiceDataHolder allocateDO = new ServiceDataHolder();
        ServiceDataMap data = new ServiceDataMap();
        data.setAttribValue("orderKey", orderKey);
        data.setAttribValue("osKey", "");
        data.setAttribValue("doCarton", "Y");
        data.setAttribValue("doRoute", "N");
        data.setAttribValue("tblPrefix", "");
        data.setAttribValue("preallocateOnly", "N");
        allocateDO.setInputData(data);

        OrderProcessingP1S1 allocateProcess = new OrderProcessingP1S1();
        allocateProcess.execute(allocateDO);

        EXEDataObject shipDO = new EXEDataObject();
        shipDO.setAttribValue("orderkey", orderKey);
        shipDO.setAttribValue("TransactionStarted", "true");
        //todo
//        context.theEXEDataObjectStack.push(shipDO);
//        Process shipProcess = context.searchObjectLibrary("NSPMASSSHIPORDERS"));
//        shipProcess.execute();
//        context.theEXEDataObjectStack.pop();

        String status = DBHelper.getValue( "select status from orders where orderkey = ? ", new Object[]{orderKey}, String.class, "订单");

        if (!"95".equals(status))
            ExceptionHelper.throwRfFulfillLogicException("订单" + orderKey + "发运失败，请确认可用库存量大于订单量");

    }

    /**
     * 只用来做结算变成取消的订单状态判断方法
     * @param orderStatus
     * @return
     */
    public static boolean isOrderCanBeCancelled(String orderStatus){

        //增加98和99的原因是允许两种取消状态来回切换，避免输入错误后无法修改的情况

        return  orderStatus.equals("00") || //空订单
                orderStatus.equals("02") || //外部创建
                orderStatus.equals("04") || //内部创建
                orderStatus.equals("06") || //未分配
                orderStatus.equals("09") || //未开始
                orderStatus.equals("98") || //外部取消
                orderStatus.equals("99"); //内部取消

    }


    /**
     * 修改订单行状态为 98: '外部取消' / 99: "内部取消"

     * @param orderkey
     * @return
     */
    public static void cancelOrder( String orderkey, boolean isExternalCancel){

        String status = isExternalCancel ? "98":"99";

        DBHelper.executeUpdate("UPDATE ORDERDETAIL SET STATUS = ? WHERE ORDERKEY = ? ",new Object[]{ status,orderkey});
        DBHelper.executeUpdate("UPDATE ORDERS SET STATUS = ? WHERE ORDERKEY = ?",new Object[]{ status,orderkey});

    }

}
