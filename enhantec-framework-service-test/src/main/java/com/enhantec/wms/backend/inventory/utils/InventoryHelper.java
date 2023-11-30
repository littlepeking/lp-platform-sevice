package com.enhantec.wms.backend.inventory.utils;

import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.*;
import com.enhantec.wms.backend.utils.print.Labels;
import com.enhantec.wms.backend.core.WMSServiceNames;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.enhantec.wms.backend.utils.common.UtilHelper.trimZerosAndToStr;
import static com.enhantec.wms.backend.utils.print.PrintHelper.printOrUpdateTaskLPNByIDNotes;

public class InventoryHelper {

    /**
     * @return
     *  key:TOID,value:拆分至容器
     *  key:PRINT,value:是否需要重新打印标签
     * @throws Exception
     */
    public static Map<String,String> doMove( String OPNAME, Map<String, String> fromIdHashMap, List<String> snList, String toId, String fromLoc, String toLoc, String toBeMovedNetWgt, String toBeMovedGrossWgt, String toBeMovedTareWgt, String tobeMovedUOM, String printer, boolean isSplitLpn) throws Exception {

        String fromId = fromIdHashMap.get("ID");

        BigDecimal idQty = new BigDecimal(fromIdHashMap.get("QTY"));
        BigDecimal allocatedQty = new BigDecimal(fromIdHashMap.get("QTYALLOCATED"));
        BigDecimal pickedQty = new BigDecimal(fromIdHashMap.get("QTYPICKED"));
        BigDecimal availableQty = idQty.subtract(allocatedQty).subtract(pickedQty);

        if(UtilHelper.isEmpty(tobeMovedUOM)){
            tobeMovedUOM = fromIdHashMap.get("UOM");
        }
        if(UtilHelper.isEmpty(toBeMovedNetWgt)){
            toBeMovedNetWgt = snList.size()>0 ? String.valueOf(snList.size()) : fromIdHashMap.get("QTY");
        }
        if(UtilHelper.isEmpty(toBeMovedGrossWgt)){
            toBeMovedGrossWgt = toBeMovedNetWgt;
        }

        if(UtilHelper.isEmpty(toBeMovedTareWgt)){
            toBeMovedTareWgt = "0";
        }

        BigDecimal deltaGrossWgt = UOM.UOMQty2StdQty( fromIdHashMap.get("PACKKEY"), tobeMovedUOM, new BigDecimal(toBeMovedGrossWgt));
        BigDecimal deltaNetWgt = UOM.UOMQty2StdQty( fromIdHashMap.get("PACKKEY"), tobeMovedUOM, new BigDecimal(toBeMovedNetWgt));
        BigDecimal deltaTareWgt = UOM.UOMQty2StdQty( fromIdHashMap.get("PACKKEY"), tobeMovedUOM, new BigDecimal(toBeMovedTareWgt));

        if (availableQty.compareTo(deltaNetWgt) == -1)
            ExceptionHelper.throwRfFulfillLogicException(OPNAME +"数量" + trimZerosAndToStr(deltaNetWgt) + "大于当前库存可用量" + trimZerosAndToStr(trimZerosAndToStr(availableQty)));

        String status =fromIdHashMap.get("STATUS");

        Loc.findById( toLoc,true);

        if(fromId.equals(toId) && fromLoc.equals(toLoc)) {
            ExceptionHelper.throwRfFulfillLogicException("库位和容器条码均相同，无需进行操作");
        }

        if(fromId.equals(toId) && !fromLoc.equals(toLoc)) {
            //否定校验
            InventoryValidationHelper.validateLocMix( toId, fromLoc, toLoc);
        }
        /**
         * fromId原始容器，拆分/合并后依旧有重量且启用标签重量，打印fromId原始容器标签
         */
        boolean printFromIdLabel = false;
        if(UtilHelper.decimalStrCompare(fromIdHashMap.get("NETWGT"),toBeMovedNetWgt) > 0
                && CDSysSet.enableLabelWgt()){
            printFromIdLabel = true;
        }
        boolean printToIdLabel = false;
        /**
         * toId为空，说明需要创建新容器，打印新容器的标签
         * toId不为空
         * fromId == toId，移动，不需要打印标签
         * fromId != toId且启用重量，打印标签(当toId已经指定的情况下，这时候认为已经存在toId的标签，无需重新打印)
         */
        if(UtilHelper.isEmpty(toId)){
            printToIdLabel = true;
        }else{
            if(!fromId.equals(toId) && CDSysSet.enableLabelWgt()){
                printToIdLabel = true;
            }
        }

        //fromid==toid为移动操作，不需要拆分合并容器，只需要移动库存和唯一码
        if(!fromId.equals(toId)){
            //update existing IDNOTES and insert new record to IDNOTES
            toId = IDNotes.splitWgtById(deltaGrossWgt,deltaNetWgt,deltaTareWgt,toBeMovedGrossWgt,toBeMovedNetWgt,toBeMovedTareWgt,tobeMovedUOM,fromId,toId,"", isSplitLpn);
        }

        if(snList.size()>0) {
            for (String sn: snList) {
                Map<String,String> serialMove = new HashMap<>();
                serialMove.put("WHSEID", "@user");
                serialMove.put("STORERKEY", CDSysSet.getStorerKey());
                serialMove.put("SKU", fromIdHashMap.get("SKU"));
                serialMove.put("LOT", fromIdHashMap.get("LOT"));
                serialMove.put("ID", fromId);
                serialMove.put("LOC", fromIdHashMap.get("LOC"));
                serialMove.put("ADDWHO", EHContextHelper.getUser().getUsername());
                serialMove.put("EDITWHO", EHContextHelper.getUser().getUsername());
                serialMove.put("ADDDATE", LocalDateTime.now().toString());
                serialMove.put("EDITDATE", LocalDateTime.now().toString());
                serialMove.put("SERIALNUMBER",sn);
                LegacyDBHelper.ExecInsert( "SERIALMOVE", serialMove);
            }
        }

        ServiceDataMap moveDO = buildParams( fromIdHashMap.get("STORERKEY"), fromId, toId,  toLoc, deltaNetWgt);

        ServiceHelper.executeService(WMSServiceNames.INV_SINGLE_LOT_ID_MOVE,new ServiceDataHolder(moveDO));

        if (!toId.equalsIgnoreCase(fromId)){//拆分SN时 不一致
            ChangeOpenSnMarksHelper.changeOpenSnMarksBYLpn(fromIdHashMap.get("SKU"),toId,fromId);
        }
        boolean printLabel = false;
        //print lpn
        if(printFromIdLabel && IDNotes.isLpnOrBoxId(fromId)){
            printLabel = true;
            printOrUpdateTaskLPNByIDNotes(fromId, Labels.LPN_UI_SY,printer,"","打印拆分容器余量标签");
        }
        if(printToIdLabel && IDNotes.isLpnOrBoxId(toId)){
            printLabel = true;
            printOrUpdateTaskLPNByIDNotes(toId, Labels.LPN_UI_SY,printer,"","打印拆分至容器余量标签");
        }

        Map<String, String> result = new HashMap<>();
        result.put("TOID",toId);
        result.put("PRINT",String.valueOf(printLabel));
        return result;

    }

    private static ServiceDataMap buildParams( String storerKey, String fromId, String toId, String toLoc, BigDecimal qtyToBeMoved) {
        ServiceDataMap moveDO = new ServiceDataMap();
        moveDO.setAttribValue("storerKey",storerKey);
//        moveDO.setAttribValue("Sku",SKU);
//        moveDO.setAttribValue("Lot",LOT);
//        moveDO.setAttribValue("FromLoc",FROMLOC);
        moveDO.setAttribValue("fromId",fromId);
        moveDO.setAttribValue("toLoc",toLoc);
        moveDO.setAttribValue("toId",toId);
        moveDO.setAttribValue("qty",qtyToBeMoved);

        return  moveDO;
    }


    //Logic referred from InventoryMoveExecuteMovesAction
    private static boolean isOnlyLocOnHold( String loc, String lot, String ID)  {
        boolean locOnHold = false;
        if(!"".equalsIgnoreCase(loc)){
            String locQuery = "SELECT count(1) TOTALNUM FROM INVENTORYHOLD WHERE loc = ? AND Hold = '1'";
            Map<String,String> record=DBHelper.getRecord( locQuery, new Object[]{ loc},"库存冻结");

            if(!record.get("TOTALNUM").equals("0")){
                locOnHold = true;
            }
        }
        boolean lotOnHold = false;
        if(!"".equalsIgnoreCase(lot)){
            String lotQuery = "SELECT count(1) TOTALNUM FROM INVENTORYHOLD WHERE lot = ? AND Hold = '1'";
            Map<String,String> record=DBHelper.getRecord( lotQuery, new Object[]{ lot},"库存冻结");

            if(!record.get("TOTALNUM").equals("0")){
                lotOnHold = true;
            }

        }


        boolean idOnHold = false;
        if(!"".equalsIgnoreCase(ID)){
            String idQuery = "SELECT count(1) TOTALNUM FROM INVENTORYHOLD WHERE id = ? AND Hold = '1'";
            Map<String,String> record=DBHelper.getRecord( idQuery, new Object[]{ID},"库存冻结");
            if(!record.get("TOTALNUM").equals("0")){
                idOnHold = true;
            }
        }

        if(locOnHold && !lotOnHold && !idOnHold){//means only loc on hold
            return true;
        }
        return false;
    }

    public static String getHoldStatus4Pick( Map<String,String> lotxLocxIdInfo)  {

        List<String> holdStatuses = getHoldStatuses(lotxLocxIdInfo);

        if(holdStatuses.size()>0){
            return holdStatuses.get(0);
        }else {
            return "OK";
        }
    }


    public static List<String> getHoldStatuses( Map<String,String> lotxLocxIdInfo)  {

        String statusQuery = "SELECT STATUS FROM INVENTORYHOLD WHERE HOLD = 1 AND STATUS <> 'OK' AND (LOC =? OR ID = ? OR LOT = ?) ";
        List<Map<String,String>>  statusList = DBHelper.executeQuery( statusQuery, new Object[]{
                lotxLocxIdInfo.get("LOC"),
                lotxLocxIdInfo.get("ID"),
                lotxLocxIdInfo.get("LOT")});

        List<String> holdStatuses = statusList.stream().map(x->x.get("STATUS")).collect(Collectors.toList());
        if(holdStatuses.size() == 0){
            holdStatuses.add("OK");
        }
        return holdStatuses;
    }
    public static void checkLocQuantityLimit( String loc) throws Exception {
        String locNumQuery = "select COUNT(*) from LOTXLOCXID l,idnotes i where l.LOC=? and i.id=l.ID and l.QTY>0";
        String  locNum = DBHelper.getStringValue( locNumQuery, new Object[]{loc},"库位LPN数量");
        Map<String,String> locHash= Loc.findById(loc,true);
        String stackLimit = UtilHelper.isEmpty(locHash.get("STACKLIMIT"))?"0":locHash.get("STACKLIMIT");
        String footPrint = UtilHelper.isEmpty(locHash.get("FOOTPRINT"))?"0":locHash.get("FOOTPRINT");
        String maxLocQuantity =  (new BigDecimal(stackLimit).multiply(new BigDecimal(footPrint))).toPlainString();
        if (!UtilHelper.isEmpty(maxLocQuantity)&&!"0".equalsIgnoreCase(maxLocQuantity)){
            if (Integer.parseInt(locNum) >= Integer.parseInt(maxLocQuantity))
                throw new Exception("超出库位最大容器存放量");
        }

    }

}
