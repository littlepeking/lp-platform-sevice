package com.enhantec.wms.backend.inventory.utils;

import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.enhantec.wms.backend.utils.common.UtilHelper.getString;
public class InventoryValidationHelper {

    public static void validateLocMix(Context context, String fromId, String fromLoc, String toLoc) throws Exception {

        HashMap<String,String> locHashMap = Loc.findById(context, toLoc,true);

        String toPutawayZone = locHashMap.get("PUTAWAYZONE");

        List<HashMap<String,String>> mixedzones = CodeLookup.getCodeLookupList(context, "MIXEDZONES");

        if(mixedzones!=null){
            boolean isMixedZone = mixedzones.stream().filter(e->e.get("CODE").equals(toPutawayZone)).count()>0;
            //非否定校验列表区内不进行检查
            if(isMixedZone) return;
        }

        //校验物料在上架策略关联的上架库区中
        HashMap<String,String> lotxLocxIdHashMap = LotxLocxId.findFullAvailInvById(context,fromId,"未找到可用于上架的容器条码");

        if(lotxLocxIdHashMap.get("LOC").equals(toLoc)) ExceptionHelper.throwRfFulfillLogicException("当前容器提条码已在目标库位上，无需处理");

        HashMap<String,String> skuHashMap = SKU.findById(context,lotxLocxIdHashMap.get("SKU"),true);
        String putawayStrategyKey = skuHashMap.get("PUTAWAYSTRATEGYKEY");

        List<HashMap<String,String>> zones = DBHelper.executeQuery(context,
                "SELECT DISTINCT ZONE FROM PUTAWAYSTRATEGYDETAIL P " +
                        " WHERE P.ZONE IS NOT NULL AND P.ZONE <>'' AND P.PUTAWAYSTRATEGYKEY = ? ",new Object[]{
                        putawayStrategyKey
                });

        if (!UtilHelper.isEmpty(skuHashMap.get("PUTAWAYZONE")))
            zones.add(new HashMap<String, String>(){{
                put("ZONE",skuHashMap.get("PUTAWAYZONE"));
             }}) ;

        HashMap<String,String> locInfo = Loc.findById(context,toLoc,true);

        if(!locInfo.get("PUTAWAYZONE").equals("DOCK") && !zones.stream().anyMatch(x->x.get("ZONE").equals(locInfo.get("PUTAWAYZONE"))))
            ExceptionHelper.throwRfFulfillLogicException("目标库位不在被允许的上架区列表中");

        //安全五项检查
        /*
                1、	待验和合格的物料不能放在同一托盘（库位）上。lottable03 质量状态
                2、	固体和液体应分开存放。sku.itemcharacteristic2
                3、	酸碱分开放置。sku.itemcharacteristic1
                4、	原料、中间体、产品按入库单据类型区分，分开放置。sku. busr4
         */

        List<HashMap<String,String>> sourceRes = DBHelper.executeQuery(
                context,
                " SELECT s.sku, " +
                        "s.itemcharacteristic1, " +
                        "s.itemcharacteristic2, " +
                        "s.busr4,  " +
                        "la.elottable03 " +
                        "FROM lotxlocxid l, sku s, v_lotattribute la " +
                        "WHERE l.sku = s.sku  AND l.lot = la.lot AND qty>0" +
                        "AND l.loc = ? AND l.id = ? ",
                Arrays.asList(fromLoc, fromId)
        );

        if(sourceRes.size()==0) ExceptionHelper.throwRfFulfillLogicException("未找到库存");


        List<HashMap<String,String>> targetRes = DBHelper.executeQuery(
                context,
                " SELECT s.sku," +
                        "s.itemcharacteristic1, " +
                        "s.itemcharacteristic2, " +
                        "s.busr4, " +
                        "la.elottable03 " +
                        "FROM lotxlocxid l, sku s, v_lotattribute la " +
                        "WHERE l.sku = s.sku  AND l.lot = la.lot AND qty>0" +
                        "AND l.loc = ?",
                Arrays.asList(toLoc)
        );

        if(sourceRes.size()==0) return;// 库存不存在，校验通过

        targetRes.stream().forEach(t->{
            sourceRes.stream().forEach(s->{
                if(!UtilHelper.equals(getString(s.get("itemcharacteristic1")),getString(t.get("itemcharacteristic1")))){
                    ExceptionHelper.throwRfFulfillLogicException("酸性物料和碱性物料不可混放");
                }
                if(!UtilHelper.equals(getString(s.get("itemcharacteristic2")),getString(t.get("itemcharacteristic2")))){
                    ExceptionHelper.throwRfFulfillLogicException("固液状态不同，不可混放");
                }
                if(!UtilHelper.equals(getString(s.get("elottable03")),getString(t.get("elottable03")))){
                    ExceptionHelper.throwRfFulfillLogicException("质量状态不同，不允许混放");
                }
                if(!UtilHelper.equals(getString(s.get("busr4")),getString(t.get("busr4")))){
                    ExceptionHelper.throwRfFulfillLogicException("不同物料类别（原料、中间体、产品）不可混放");
                }

            });
        });


    }
    public static void validateLotQty(Context context, String lottable06){
        String Sql="select QTYPICKED,QTYALLOCATED,QTYONHOLD  from LOT l ,V_LOTATTRIBUTE vl" +
                " where l.LOT=vl.LOT and vl.lottable06=? ";
        HashMap<String,String> record= DBHelper.getRecord(context, Sql, new Object[]{  lottable06},"批次库存可用量");
        if(record == null) ExceptionHelper.throwRfFulfillLogicException("批次"+lottable06+"不存在");
        if (UtilHelper.decimalStrCompare(record.get("QTYPICKED"),"0")!=0){
            ExceptionHelper.throwRfFulfillLogicException("批次"+lottable06+"拣货量不为0，无法操作");
        }
        if (UtilHelper.decimalStrCompare(record.get("QTYALLOCATED"),"0")!=0){
            ExceptionHelper.throwRfFulfillLogicException("批次"+lottable06+"分配量不为0，无法操作");
        }
        if (UtilHelper.decimalStrCompare(record.get("QTYONHOLD"),"0")!=0){
            ExceptionHelper.throwRfFulfillLogicException("批次"+lottable06+"冻结量不为0，无法操作");
        }
    }
}
