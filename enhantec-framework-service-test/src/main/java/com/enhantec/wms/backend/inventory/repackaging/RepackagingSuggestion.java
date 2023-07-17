package com.enhantec.wms.backend.inventory.repackaging;

import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.outbound.picking.PickUtil;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.HashMap;
import java.util.List;

/**
 --注册方法
 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME = 'EHRepackagingSuggestion';
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHRepackagingSuggestion', 'com.enhantec.sce.inventory.repackaging', 'enhantec', 'RepackagingSuggestion','TRUE','JOHN','JOHN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ORDERKEY,ORDERLINENUMBER','0.10','0');
 **/


public class RepackagingSuggestion extends LegacyBaseService {

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {
        

        try {

            String orderKey = serviceDataHolder.getInputDataAsMap().getString("ORDERKEY");
            String orderLineNumber = serviceDataHolder.getInputDataAsMap().getString("ORDERLINENUMBER");

 

            HashMap<String,String> zoneInfo = CodeLookup.getCodeLookupByKey( "SYSSET","PACKZONE");
            if(UtilHelper.isEmpty(zoneInfo.get("UDF1"))) ExceptionHelper.throwRfFulfillLogicException("仓库分装区配置未进行设置");
            //List<HashMap<String,String>> packLocs = Loc.findByZone(zoneInfo.get("UDF1"),false);
            HashMap<String,String> orderInfo = Orders.findByOrderKey(orderKey,true);
            HashMap<String,String> orderDetailInfo = Orders.findOrderDetailByKey(orderKey, orderLineNumber,true);
            HashMap<String,String> orderTypeInfo = CodeLookup.getCodeLookupByKey("ORDERTYPE",orderInfo.get("TYPE"));

            HashMap<String,String> skuInfo = SKU.findById(orderDetailInfo.get("SKU"),true);

            String mainUOM = UOM.getStdUOM( skuInfo.get("PACKKEY"));

            StringBuffer querySqlSB = new StringBuffer();

            querySqlSB.append("SELECT l.ID,l.LOC, vl.LOTTABLE06 RECEIPTLOT, CASE WHEN i.ISOPENED = 1 THEN 'Y' ELSE '' END ISOPENED, l.QTY, '"+ mainUOM +"' as UOM " +
                    " FROM LOTXLOCXID l , idnotes i, V_LOTATTRIBUTE vl, LOC c " +
                    " WHERE l.id = i.id and l.LOT = vl.LOT and  l.QTY > 0 and l.QTYPICKED = 0 and l.QTYALLOCATED = 0 and l.STATUS <>'HOLD'");

            querySqlSB.append(" AND l.LOC = c.LOC AND c.PUTAWAYZONE <> '"+ zoneInfo.get("UDF1") +"' AND c.LOC<>'PICKTO' ");


            querySqlSB.append(" AND l.SKU = '" + skuInfo.get("SKU") +"' ");

            String defaultProjectCode = CDSysSet.getDefaultProjectCode();

            //取样单和留样出库单不看项目号（这段暂时保留，但是分装的订单类型不应该是取样单或留样出库单，应该用不到）
            if(!orderInfo.get("TYPE").equals(CDSysSet.getSampleOrderType()) && !"Y".equalsIgnoreCase((orderTypeInfo.get("UDF6")))) {
                String projectCode = orderInfo.get("NOTES");
                if (!UtilHelper.isEmpty(projectCode)) {
                    //有项目号库存使用项目号库存，找不到则使用公共项目号库存
                    querySqlSB.append(" AND ('" + projectCode + "' IN (SELECT value FROM STRING_SPLIT(i.PROJECTCODE, ',')) OR i.PROJECTCODE = '"+defaultProjectCode+"') ");
                } else {
                    querySqlSB.append(" AND  i.PROJECTCODE = '"+defaultProjectCode+"' ");
                }
            }

            if("Y".equalsIgnoreCase(orderTypeInfo.get("UDF6"))){
                querySqlSB.append(" AND vl.LOTTABLE01 = '" + skuInfo.get("SUSR6") +"' ");  //取样包装
            }else{
                querySqlSB.append(" AND vl.LOTTABLE01 = '" + skuInfo.get("PACKKEY") +"' ");  //标准包装
            }

            if(!UtilHelper.isEmpty(orderDetailInfo.get("LOTTABLE02"))) {
                querySqlSB.append(" AND vl.LOTTABLE02 = '" + orderDetailInfo.get("LOTTABLE02")+"' ");
            }
            if(!UtilHelper.isEmpty(orderDetailInfo.get("LOTTABLE03"))) {
                querySqlSB.append(" AND vl.LOTTABLE03 = '" + orderDetailInfo.get("LOTTABLE03")+"' ");
            }
            if(!UtilHelper.isEmpty(orderDetailInfo.get("LOTTABLE06"))) {
                querySqlSB.append(" AND vl.LOTTABLE06 = '" + orderDetailInfo.get("LOTTABLE06")+"' ");
            }
            if(!UtilHelper.isEmpty(orderDetailInfo.get("LOTTABLE07"))) {
                querySqlSB.append(" AND vl.LOTTABLE07 = '" + orderDetailInfo.get("LOTTABLE07")+"' ");
            }
            if(!UtilHelper.isEmpty(orderDetailInfo.get("LOTTABLE08"))) {
                querySqlSB.append(" AND vl.LOTTABLE08 = '" + orderDetailInfo.get("LOTTABLE08")+"' ");
            }
            if(!UtilHelper.isEmpty(orderDetailInfo.get("LOTTABLE09"))) {
                querySqlSB.append(" AND vl.LOTTABLE09 = '" + orderDetailInfo.get("LOTTABLE09")+"' ");
            }
            if(!UtilHelper.isEmpty(orderDetailInfo.get("LOTTABLE10"))) {
                querySqlSB.append(" AND vl.LOTTABLE10 = '" + orderDetailInfo.get("LOTTABLE10")+"' ");
            }

            if(!UtilHelper.isEmpty(orderDetailInfo.get("ELOTTABLE01"))) {
                querySqlSB.append(" AND vl.ELOTTABLE01 = '" + orderDetailInfo.get("ELOTTABLE01")+"' ");
            }
            if(!UtilHelper.isEmpty(orderDetailInfo.get("ELOTTABLE02"))) {
                querySqlSB.append(" AND vl.ELOTTABLE02 = '" + orderDetailInfo.get("ELOTTABLE02")+"' ");
            }

            querySqlSB.append(PickUtil.getQualityStatusSqlFilterStr(orderTypeInfo, orderDetailInfo.get("ELOTTABLE03")));

            if(!UtilHelper.isEmpty(orderDetailInfo.get("ELOTTABLE06"))) {
                querySqlSB.append(" AND vl.ELOTTABLE06 = '" + orderDetailInfo.get("ELOTTABLE06")+"' ");
            }
            if(!UtilHelper.isEmpty(orderDetailInfo.get("ELOTTABLE07"))) {
                querySqlSB.append(" AND vl.ELOTTABLE07 = '" + orderDetailInfo.get("ELOTTABLE07")+"' ");
            }
            if(!UtilHelper.isEmpty(orderDetailInfo.get("ELOTTABLE08"))) {
                querySqlSB.append(" AND vl.ELOTTABLE08 = '" + orderDetailInfo.get("ELOTTABLE08")+"' ");
            }
            if(!UtilHelper.isEmpty(orderDetailInfo.get("ELOTTABLE09"))) {
                querySqlSB.append(" AND vl.ELOTTABLE09 = '" + orderDetailInfo.get("ELOTTABLE09")+"' ");
            }
            if(!UtilHelper.isEmpty(orderDetailInfo.get("ELOTTABLE10"))) {
                querySqlSB.append(" AND vl.ELOTTABLE10 = '" + orderDetailInfo.get("ELOTTABLE10")+"' ");
            }

            //有项目号优先
            String orderByClause = " ORDER BY CASE WHEN i.PROJECTCODE IS NULL OR i.PROJECTCODE = '"+defaultProjectCode+ "' THEN 1 ELSE 0 END ASC, " +
                    "CASE WHEN vl.ELOTTABLE11 IS NULL THEN 1 ELSE 0 END ASC, vl.ELOTTABLE11 ASC,"+
                    PickUtil.getQualityStatusSqlOrderByStr(orderTypeInfo,orderDetailInfo.get("ELOTTABLE03"))
                    +" vl.LOTTABLE06 ASC, i.ISOPENED DESC, l.LOC, l.ID ASC";


            querySqlSB.append(orderByClause);

            List<HashMap<String, String>> list = DBHelper.executeQuery( querySqlSB.toString(), new Object[]{});

            serviceDataHolder.setReturnCode(1);
            serviceDataHolder.setOutputData(list);

        } catch (Exception e) {
            ExceptionHelper.throwRfFulfillLogicException(e.getMessage());
        }finally {
            
        }
    }

}
