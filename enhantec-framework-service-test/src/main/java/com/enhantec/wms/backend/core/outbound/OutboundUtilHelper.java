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

package com.enhantec.wms.backend.core.outbound;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.framework.common.utils.EHDateTimeHelper;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.DBHelper;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class OutboundUtilHelper {

    public static ServiceDataMap computeOrderDetailStatus(String orderkey, String orderLineNumber) {
        try {

            String newStatus;

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

            List<Map<String,String>> res = DBHelper.executeQuery( " SELECT A.STATUS, B.LOCATIONTYPE, A.DROPID, A.WAVEKEY FROM PICKDETAIL A, LOC B WHERE B.LOC = A.LOC AND A.ORDERKEY = ? AND A.ORDERLINENUMBER = ?",
                    new Object[]{ orderkey, orderLineNumber});

            for(Map<String,String> r : res) {
                pickdetailstatus = r.get("STATUS");
                locType = r.get("LOCATIONTYPE");
                dropid = r.get("DROPID");
                waveKey = r.get("WAVEKEY");

                if (((pickdetailstatus.equals("1")) && (waveKey != " "))) {
                    ordreleased = ordreleased + 1;
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
            }


            pickstotal= qqRowCount;
            released=ordreleased;
            inpicking=inpick;
            packed=ordpacked;
            instaged=ordinstaged;
            loaded=ordloaded;


            Map<String,Object>  resStatus = DBHelper.getRawRecord(" SELECT SUM ( CASE WHEN b.status = '0' AND a.wavekey <> ' ' THEN 1 ELSE 0 END ) RELEASED, SUM ( CASE WHEN b.status >= '2' AND b.status <= '3' THEN 1 ELSE 0 END ) INPICKING FROM DEMANDALLOCATION a, TASKDETAIL b WHERE b.Sourcekey = a.DemandKey AND b.SourceType = 'DP' AND b.status = '0' AND a.OrderKey = ? AND a.OrderLineNumber = ? ",
                    new Object[]{orderkey, orderLineNumber});

            dpreleased = (int) (resStatus.get("RELEASED")  == null? 0: resStatus.get("RELEASED"));
            dpinpicking = (int) (resStatus.get("INPICKING")  == null? 0: resStatus.get("INPICKING"));

            Map<String,Object>  resOrderLine = DBHelper.getRawRecord(" SELECT SHIPPEDQTY, OPENQTY, QTYPREALLOCATED, QTYALLOCATED, QTYPICKED, ISSUBSTITUTE, WPRELEASED, STATUS FROM ORDERDETAIL WHERE ORDERKEY = ? AND ORDERLINENUMBER = ?",
                    new Object[]{ orderkey, orderLineNumber});

            SHIPPEDQTY = ((BigDecimal) resOrderLine.get("SHIPPEDQTY")).doubleValue();
            OPENQTY =   ((BigDecimal) resOrderLine.get("OPENQTY")).doubleValue();
            QTYPREALLOCATED =   ((BigDecimal) resOrderLine.get("QTYPREALLOCATED")).doubleValue();
            QTYALLOCATED =  ((BigDecimal) resOrderLine.get("QTYALLOCATED")).doubleValue();
            QTYPICKED =   ((BigDecimal) resOrderLine.get("QTYPICKED")).doubleValue();
            ISSUBSTITUTE =  (int) resOrderLine.get("ISSUBSTITUTE");
            wpReleased =  (String) resOrderLine.get("WPRELEASED");
            ODSTATUS = Integer.parseInt(resOrderLine.get("STATUS").toString());


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
                newStatus = "95";
            } else if (SHIPPEDQTY > 0.0D && OPENQTY > 0.0D && OPENQTY - QTYPREALLOCATED - QTYALLOCATED == 0.0D && (ODSTATUS >= 22 && ODSTATUS <= 29 || released > 0)) {
                newStatus = "27";
            } else if (SHIPPEDQTY > 0.0D && OPENQTY > 0.0D && OPENQTY - QTYPREALLOCATED - QTYALLOCATED == 0.0D) {
                newStatus = "92";
            } else if (loaded == pickstotal && pickstotal > 0 && QTYPICKED > 0.0D && OPENQTY - QTYPICKED == 0.0D) {
                newStatus = "88";
            } else if (SHIPPEDQTY > 0.0D && loaded > 0) {
                newStatus = "92";
            } else if (loaded > 0 && QTYPICKED > 0.0D && OPENQTY - QTYPICKED == 0.0D) {
                newStatus = "82";
            } else if (instaged == pickstotal && instaged > 0 && SHIPPEDQTY == 0.0D && OPENQTY - QTYPICKED == 0.0D && OPENQTY > 0.0D && QTYPICKED > 0.0D) {
                newStatus = "75";
            } else if (packed == pickstotal && packed > 0 && QTYPICKED == OPENQTY) {
                newStatus = "68";
            } else if (SHIPPEDQTY > 0.0D && packed > 0) {
                newStatus = "92";
            } else if (packed > 0) {
                newStatus = "61";
            } else if (SHIPPEDQTY > 0.0D && OPENQTY - QTYPICKED == 0.0D && OPENQTY > 0.0D && QTYPICKED > 0.0D) {
                newStatus = "57";
            } else if (SHIPPEDQTY == 0.0D && OPENQTY - QTYPICKED == 0.0D && OPENQTY > 0.0D && QTYPICKED > 0.0D) {
                newStatus = "55";
            } else if (SHIPPEDQTY > 0.0D && OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED >= 0.0D && QTYALLOCATED >= 0.0D && QTYPICKED > 0.0D) {
                newStatus = "53";
            } else if (SHIPPEDQTY != 0.0D || !(OPENQTY > 0.0D) || OPENQTY - QTYALLOCATED - QTYPICKED != 0.0D || !(QTYALLOCATED > 0.0D) || !(QTYPICKED > 0.0D) || (ODSTATUS < 22 || ODSTATUS > 29) && released <= 0) {
                if (SHIPPEDQTY == 0.0D && OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED == 0.0D && QTYALLOCATED > 0.0D && QTYPICKED > 0.0D) {
                    newStatus = "52";
                } else if (inpicking <= 0 && dpinpicking <= 0) {
                    if (SHIPPEDQTY != 0.0D || !(OPENQTY > 0.0D) || OPENQTY - QTYALLOCATED != 0.0D || released <= 0 && dpreleased <= 0) {
                        if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYPICKED > 0.0D && QTYALLOCATED > 0.0D && (ODSTATUS >= 22 && ODSTATUS <= 29 || released > 0)) {
                            newStatus = "25";
                        } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYALLOCATED > 0.0D && SHIPPEDQTY == 0.0D && (released > 0 || dpreleased > 0) && QTYPICKED == 0.0D) {
                            newStatus = "22";
                        } else if (SHIPPEDQTY == 0.0D && OPENQTY == 0.0D && QTYPICKED == 0.0D && ISSUBSTITUTE > 0) {
                            newStatus = "18";
                        } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED == 0.0D) {
                            newStatus = "17";
                        } else if (!(SHIPPEDQTY > 0.0D) || !(OPENQTY - QTYALLOCATED > 0.0D) || QTYPICKED != 0.0D || (ODSTATUS < 22 || ODSTATUS > 29) && released <= 0) {
                            if (SHIPPEDQTY > 0.0D && OPENQTY - QTYALLOCATED > 0.0D && QTYPICKED == 0.0D) {
                                newStatus = "16";
                            } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYPICKED > 0.0D && QTYALLOCATED >= 0.0D && (ODSTATUS >= 22 && ODSTATUS <= 29 || ODSTATUS == 51 || released > 0)) {
                                newStatus = "25";
                            } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYPICKED > 0.0D && QTYALLOCATED >= 0.0D) {
                                newStatus = "15";
                            } else if (OPENQTY > 0.0D && OPENQTY - QTYALLOCATED - QTYPICKED > 0.0D && QTYALLOCATED > 0.0D) {
                                newStatus = "14";
                            } else if (OPENQTY > 0.0D && OPENQTY - QTYPREALLOCATED == 0.0D && wpReleased != null && wpReleased.equals("1")) {
                                newStatus = "13";
                            } else if (OPENQTY > 0.0D && OPENQTY - QTYPREALLOCATED == 0.0D) {
                                newStatus = "12";
                            } else if (OPENQTY > 0.0D && QTYPREALLOCATED > 0.0D) {
                                newStatus = "11";
                            } else if (OPENQTY > 0.0D && QTYPREALLOCATED + QTYALLOCATED + QTYPICKED + SHIPPEDQTY == 0.0D) {
                                newStatus = "09";
                            } else if (SHIPPEDQTY + OPENQTY == 0.0D) {
                                newStatus = "95";
                            } else if (OPENQTY - QTYPREALLOCATED - QTYALLOCATED - QTYPICKED < 0.0D) {
                                newStatus = "-2";
                            } else {
                                newStatus = "-1";
                            }
                        } else {
                            newStatus = "27";
                        }
                    } else {
                        newStatus = "29";
                    }
                } else {
                    newStatus = "51";
                }
            } else {
                newStatus = "25";
            }

            int qqRowCount2 = 0;

            String storerkey = null;
            String sku = null;

            Map<String,String> resOrderLine2 = DBHelper.getRecord(" SELECT Status, Storerkey, SKU FROM OrderDetail WHERE OrderKey = ? AND OrderLineNumber = ?",
                    new Object[]{ orderkey,orderLineNumber});

            orderdetailstatus = resOrderLine2.get("STATUS");
            storerkey =  resOrderLine2.get("STORERKEY");
            sku =  resOrderLine2.get("SKU");

            Map<String,String> resOrderstatussetup = DBHelper.getRecord(" SELECT MAX ( Code ) code FROM Orderstatussetup WHERE Orderflag = '1' AND Detailflag = '1' AND Enabled = '1' AND Code <= ?",
                    new Object[]{newStatus});
            maxcode = resOrderstatussetup.get("CODE");


            if (maxcode != null && maxcode.equals("09") && orderdetailstatus != null && orderdetailstatus.compareToIgnoreCase("02") >= 0 && orderdetailstatus != null && orderdetailstatus.compareToIgnoreCase("08") <= 0) {
                newStatus = orderdetailstatus;
            } else if (orderdetailstatus == null || !orderdetailstatus.equalsIgnoreCase("68") || !newStatus.equalsIgnoreCase("55") && !newStatus.equalsIgnoreCase("61")) {
                newStatus = maxcode;
            } else {
                newStatus = maxcode;
                double qtyPacked = 0.0D;
                double qtyOrdered = 0.0D;
                int qqRowCount6 = 0;

                PreparedStatement qqPrepStmt6 = null;
                ResultSet qqResultSet6 = null;

                Map<String,Object> resPackoutdetail = DBHelper.getRawRecord(" SELECT SUM(qtypicked) qtypicked FROM Packoutdetail WHERE Orderkey = ? AND Storerkey =? AND SKU = ?",
                        new Object[]{orderkey,storerkey,sku} );

                qtyPacked = ((BigDecimal) resPackoutdetail.get("QTYPICKED")).doubleValue();


                if (qtyPacked > 0.0D) {

                    Map<String,Object> resOrderDetail = DBHelper.getRawRecord(" SELECT sum(Openqty+SHIPPEDQTY) qty FROM Orderdetail WHERE Orderkey = ? AND Storerkey =? AND SKU = ? ",
                            new Object[]{orderkey,storerkey,sku} );

                    qtyOrdered = (Integer) resOrderDetail.get("QTY");

                    if (qtyPacked == qtyOrdered) {
                        newStatus = orderdetailstatus;
                    }
                }
            }


            if ("95".equals(newStatus)) {
                String isOrderRequireClose = DBHelper.getStringValue("SELECT REQUIREORDERCLOSE  FROM ORDERS WHERE OrderKey = ?",
                        new Object[]{orderkey});

                if (isOrderRequireClose != null && "1".equals(isOrderRequireClose)) {
                    newStatus = "94";
                }
            }

            ServiceDataMap serviceDataMap = new ServiceDataMap();
            serviceDataMap.setAttribValue("NEWSTATUS", newStatus);

            return serviceDataMap;
        } finally {
            //logger.debug("leaving getOrderDetailStatus( String pOrderkey, TextData pOrderLineNumber, DBSession context, String pNewStatus, String pDetailHistoryFlag)");
        }
    }

    public static ServiceDataMap computeOrderStatus(String orderkey) {

        String  oldStatus = DBHelper.getStringValue("SELECT Status FROM Orders WHERE OrderKey = ?"
                ,new  Object[]{orderkey});

        String maxOrderDetailStatus = null;
        String maxCode = null;
        int orderDetailCount = 0;
        String maxStatus = null;
        String minStatus = null;
        String newStatus = "NA";

        Map<String,Object>  res = DBHelper.getRawRecord( " SELECT COUNT ( * ) count, MAX ( Status ) maxStatus, MIN ( Status ) minStatus FROM Orderdetail WHERE Orderkey = ? AND Status <> '18' and ( openqty>0 or shippedqty>0 or qtypreallocated>0 or qtyallocated>0 or qtypicked>0 )",
                new Object[]{ orderkey});

        orderDetailCount = (Integer) res.get("COUNT");
        maxStatus = (String) res.get("MAXSTATUS");
        minStatus = (String) res.get("MINSTATUS");



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
        } else if (maxStatus != null && maxStatus.compareToIgnoreCase("52") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("55") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("55") < 0 && doesOrderDetailStatusExists(orderkey, "27")) {
            maxOrderDetailStatus = "92";
        } else if (maxStatus != null && maxStatus.compareToIgnoreCase("52") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("55") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("55") < 0) {
            maxOrderDetailStatus = "52";
        } else if (maxStatus != null && maxStatus.equals("29") && minStatus != null && minStatus.equals("29")) {
            maxOrderDetailStatus = "29";
        } else if (maxStatus != null && maxStatus.compareToIgnoreCase("27") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0 && (doesOrderDetailStatusExists(orderkey, "92") || doesOrderDetailStatusExists(orderkey, "27") || doesOrderDetailStatusExists(orderkey, "16"))) {
            maxOrderDetailStatus = "92";
        } else if (maxStatus != null && maxStatus.compareToIgnoreCase("25") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0 && (doesOrderDetailStatusExists(orderkey, "52") || doesOrderDetailStatusExists(orderkey, "25") || doesOrderDetailStatusExists(orderkey, "15"))) {
            maxOrderDetailStatus = "52";
        } else if (maxStatus != null && maxStatus.compareToIgnoreCase("22") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0 && (doesOrderDetailStatusExists(orderkey, "16"))) {
            maxOrderDetailStatus = "92";
        } else if (maxStatus != null && maxStatus.compareToIgnoreCase("22") >= 0 && maxStatus != null && maxStatus.compareToIgnoreCase("29") <= 0 && minStatus != null && minStatus.compareToIgnoreCase("29") < 0 && doesOrderDetailStatusExists(orderkey, "15")) {
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
        } else if (maxStatus != null && maxStatus.compareToIgnoreCase("09") <= 0 && doesOrderDetailStatusExists(orderkey, "06")) {
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

        if (orderDetailCount == 0) {

            Map<String,Object> orderDtsInfo = DBHelper.getRawRecord(" SELECT COUNT ( * ) COUNT, MIN ( STATUS ) STATUS FROM ORDERDETAIL WHERE ORDERKEY = ? AND STATUS <> '18' ",
                    new Object[]{ orderkey});

            int ordDtZeroQtyCount = (Integer) orderDtsInfo.get("COUNT");
            String zeroQtyMinStatus = (String) orderDtsInfo.get("STATUS");

            if (ordDtZeroQtyCount != 0) {

                newStatus = zeroQtyMinStatus;

            } else {

                newStatus = "00";

            }

        } else {

            maxCode = DBHelper.getStringValue( " SELECT MAX (CODE) CODE FROM ORDERSTATUSSETUP WHERE ORDERFLAG = '1' AND HEADERFLAG = '1' AND ENABLED = '1' AND CODE <= ? ",
                    new Object[]{maxOrderDetailStatus});

            if (maxCode != null && maxCode.equals("09") && oldStatus != null && oldStatus.compareToIgnoreCase("02") >= 0 && oldStatus != null && oldStatus.compareToIgnoreCase("08") <= 0 && oldStatus != null && oldStatus.compareToIgnoreCase("06") != 0) {
                newStatus = oldStatus;
            } else if (maxCode != null && maxCode.compareToIgnoreCase("02") >= 0 && maxCode != null && maxCode.compareToIgnoreCase("08") <= 0 && oldStatus != null && oldStatus.compareToIgnoreCase("96") >= 0) {
                newStatus = oldStatus;
            } else {
                newStatus = maxCode;
            }
        }

        if (newStatus != null && newStatus.equals("NA")) {
            newStatus = "00";
        }

        ServiceDataMap serviceDataMap = new ServiceDataMap();
        serviceDataMap.setAttribValue("STATUS", oldStatus);
        serviceDataMap.setAttribValue("NEWSTATUS", newStatus);

        return serviceDataMap;

    }


    private static boolean doesOrderDetailStatusExists(String orderKey, String statusKey) {

        List res = DBHelper.executeQuery("Select *  From ORDERDETAIL Where OrderKey = ? AND STATUS = ?",
                new Object[]{ orderKey,statusKey});

        return res.size()>0;
    }

    public static void updateOrderStatus(String orderKey) {

        ServiceDataMap orderStatusRes = computeOrderStatus(orderKey);
        String oldStatus = orderStatusRes.getString("STATUS");
        String newStatus = orderStatusRes.getString("NEWSTATUS");
        if(!oldStatus.equals(newStatus)){

            String userid = EHContextHelper.getUser().getUsername();
            LocalDateTime currentDate = EHDateTimeHelper.getCurrentDate();

            DBHelper.executeUpdate( "UPDATE ORDERS SET STATUS = ? , EDITWHO = ?, EDITDATE = ? WHERE ORDERKEY = ? "
                    , new Object[]{
                            orderStatusRes.getString("NEWSTATUS"),
                            userid,
                            EHDateTimeHelper.getCurrentDate(),
                            orderKey
                    });

            if (newStatus.equals("95"))
            {
                DBHelper.executeUpdate("UPDATE ORDERS SET ACTUALSHIPDATE = ? WHERE ORDERKEY = ?", new Object[]{currentDate,orderKey});
            }

        }


    }

    public static void updateOrderDetailStatus(String orderKey, String orderLineNumber) {

        ServiceDataMap orderDetailStatusParam = computeOrderDetailStatus(orderKey, orderLineNumber);
        String newOrderDetailStatus = orderDetailStatusParam.getString("NEWSTATUS");

        String userid = EHContextHelper.getUser().getUsername();

        DBHelper.executeUpdate( "UPDATE ORDERDETAIL SET STATUS = ? , EDITWHO = ?, EDITDATE = ? WHERE ORDERKEY = ? AND ORDERLINENUMBER = ?"
                , new Object[]{
                        newOrderDetailStatus,
                        userid,
                        EHDateTimeHelper.getCurrentDate(),
                        orderKey,
                        orderLineNumber}
        );
    }



}
