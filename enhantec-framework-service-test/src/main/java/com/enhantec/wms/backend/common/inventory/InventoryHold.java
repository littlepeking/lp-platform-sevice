/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 *
 *******************************************************************************/

package com.enhantec.wms.backend.common.inventory;

import com.enhantec.wms.backend.utils.common.DBHelper;

import java.util.List;

public class InventoryHold {

    public static List<String> getHoldReasonsById( String id) {
        return (List<String>) DBHelper.getValueList("SELECT STATUS FROM INVENTORYHOLD WHERE ID=? AND HOLD=1 "
                , new Object[]{id}, "容器冻结原因");
    }

    public static List<String> getHoldReasonsByLoc( String loc) {
        return (List<String>) DBHelper.getValueList("SELECT STATUS FROM INVENTORYHOLD WHERE LOC=? AND HOLD=1 "
                , new Object[]{loc}, "库位冻结原因");
    }

    public static List<String> getHoldReasonsByLot( String lot) {
        return (List<String>) DBHelper.getValueList("SELECT STATUS FROM INVENTORYHOLD WHERE LOT=? AND HOLD=1 "
                , new Object[]{lot}, "批次冻结原因");
    }

    public static List<String> getIdHoldReasons(String lot, String loc, String id) {
        return (List<String>) DBHelper.getValueList("SELECT STATUS FROM INVENTORYHOLD WHERE (LOT=? OR LOC=? OR ID=?) AND HOLD=1 "
                , new Object[]{lot, loc, id}, "冻结原因");
    }

    public static List<String> getOrderTypeHoldReasons(String orderType) {
       return DBHelper.getValueList("SELECT STATUSCODE FROM HOLDALLOCATIONMATRIX WHERE ORDERTYPE=? ORDER BY SEQUENCE ", new Object[] {orderType},"订单冻结分配原因");
    }



}
