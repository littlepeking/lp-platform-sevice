package com.enhantec.wms.backend.outbound.picking;

import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.SerialInventory;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.outbound.DemandAllocation;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.common.*;

import java.util.Map;
import java.util.List;


/**
 --注册方法
 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME = 'EHDynamicPickSuggestion';
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHDynamicPickSuggestion', 'com.enhantec.sce.outbound.order.picking', 'enhantec', 'DynamicPickSuggestion','TRUE','JOHN','JOHN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ORDERKEY,ORDERLINENUMBER','0.10','0');
 **/


public class DynamicPickSuggestion extends WMSBaseService {



    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {



        try {

            String orderKey = serviceDataHolder.getInputDataAsMap().getString("ORDERKEY");
            String orderLineNumber = serviceDataHolder.getInputDataAsMap().getString("ORDERLINENUMBER");




            Map<String,String> orderInfo = Orders.findByOrderKey(orderKey,true);
            Map<String,String> orderDetailInfo = Orders.findOrderDetailByKey(orderKey, orderLineNumber,true);
            Map<String,String> orderTypeInfo = CodeLookup.getCodeLookupByKey("ORDERTYPE",orderInfo.get("TYPE"));

            String sku = orderDetailInfo.get("SKU");

            boolean isSerialControl = SKU.isSerialControl(sku);

            Map<String,String> skuInfo = SKU.findById(sku,true);

            String mainUOM = UOM.getStdUOM( skuInfo.get("PACKKEY"));



            List<Map<String,String>> demandAllocationList = DemandAllocation.findByOrderLineNumber( orderKey,orderLineNumber, false);
//
//            if(demandAllocationList.size()==0){
//                //ExceptionHelper.throwRfFulfillLogicException("当前订单分配的全部拣货任务已完成");
//
//                EXEDataObject theOutDO = SocketHelper.list2ExeSocketData(new ArrayList<>());
//
//                context.theEXEDataObjectStack.push(theOutDO);
//    
//            }

            String demandKey = demandAllocationList.get(0).get("DEMANDKEY");
            String daLottable01 = demandAllocationList.get(0).get("LOTTABLE01");
            String daELottable09 = demandAllocationList.get(0).get("ELOTTABLE09");
            String daELottable03 = demandAllocationList.get(0).get("ELOTTABLE03");
            String daQtyAllocated = demandAllocationList.get(0).get("QTYALLOCATED");


            StringBuffer querySqlSB = new StringBuffer();

            //相同生产批次ELOTTABLE09但不同收货批次LOTTABLE06会有多行，导致建议的记录重复，需要做DISTINCT。
            querySqlSB.append("SELECT DISTINCT l.ID,l.LOC,vl.LOTTABLE06 , vl.ELOTTABLE09, i.ISOPENED, CASE WHEN i.ISOPENED = 1 THEN 'Y' ELSE '' END ISOPENED, l.QTY, p.PACKUOM3 as STDUOM, l.QTY LPNQTY, l.QTY-l.QTYALLOCATED-l.QTYPICKED AVAILABLEQTY" +
                    " FROM LOTXLOCXID l , idnotes i, V_LOTATTRIBUTE vl, LOC c, DEMANDALLOCATION d, PACK p " +
                    " WHERE l.id = i.id and l.LOT = vl.LOT and p.PACKKEY = vl.LOTTABLE01 " +
                    " and l.QTY > 0 and l.STATUS <>'HOLD'" +   //暂不支持冻结分配的动态拣货
                    "");
            querySqlSB.append(" AND l.LOC = c.LOC AND c.LOC<>'PICKTO' ");

            Map<String,String> zoneInfo = CodeLookup.getCodeLookupByKey( "SYSSET","PACKZONE");

            if(!UtilHelper.isEmpty(zoneInfo.get("UDF1"))) {
                //如果设置了分装区则过滤掉分装区的库位
                querySqlSB.append( " AND c.PUTAWAYZONE <> '" + zoneInfo.get("UDF1") + "' " );
            }

            querySqlSB.append(" AND l.SKU = '" + skuInfo.get("SKU") +"' ");

            //TODO:增加项目号为需求分配属性，不应该在拣货建议时才匹配项目号

//            //取样单和留样出库单不看项目号（取样出库单段暂时保留，但是动态拣货的订单类型不应该存在取样单，留样出库单应该可以）
//            if(!orderInfo.get("TYPE").equals(ORDER_TYPE_SAMPLE) && !"Y".equalsIgnoreCase((orderTypeInfo.get("UDF6")))) {
//                String projectCode = orderInfo.get("NOTES");
//                if (!UtilHelper.isEmpty(projectCode)) {
//                    querySqlSB.append(" AND ('" + projectCode + "' IN (SELECT value FROM STRING_SPLIT(i.PROJECTCODE, ',')) OR LOWER(i.PROJECTCODE) = '"+DefaultProjectCode+"') ");
//                } else {
//                    querySqlSB.append(" AND ( LOWER(i.PROJECTCODE) = '"+DefaultProjectCode+"' ) ");
//                }
//            }


            querySqlSB.append(" AND vl.LOTTABLE01 = '" + daLottable01 +"' ");
            if(!UtilHelper.isEmpty(daELottable09)){
                querySqlSB.append(" AND vl.ELOTTABLE09 = '" + daELottable09 +"' ");
            }else {
                querySqlSB.append(" AND (vl.ELOTTABLE09 = '' OR vl.ELOTTABLE09 IS NULL) ");
            }
            //为了避免批属性模糊导致分配结果重叠的问题，质量状态分配后即锁定
            //如限制性放行允许使用CONDIREL和RELEASE的，当动态分配后，即锁定具体的质量状态，避免匹配条件模糊导致后期分配结果出现重叠,因此下面的逻辑已经改为直接匹配一个状态
            querySqlSB.append(PickUtil.getQualityStatusSqlFilterStr(orderTypeInfo, daELottable03));

            String orderByClause = " ORDER BY vl.LOTTABLE06 ASC, i.ISOPENED DESC, l.LOC ASC, l.ID ASC ";

            querySqlSB.append(orderByClause);

            List<Map<String,String>> list = DBHelper.executeQuery( querySqlSB.toString(), new Object[]{});

            if(list.size()==0) ExceptionHelper.throwRfFulfillLogicException("存在需求分配记录，但未找到可供拣货的容器，该问题可能由于在分配后库存状态发生变化导致。请在工作台删除需求分配记录后重新分配后再次尝试。");

            //添加建议关联的需求分配主信息

            list.stream().forEach(x->{
                x.put("DEMANDKEY",demandKey);
                x.put("SKU",sku);
                x.put("SKUDESCR",skuInfo.get("DESCR"));
                x.put("LOT01ALLOCATED",daLottable01);
                x.put("ELOT09ALLOCATED",daELottable09);
                x.put("ELOT03ALLOCATED",daELottable03);
                x.put("QTYALLOCATED",daQtyAllocated);
                //为零头库存增加唯一码信息
                if(isSerialControl && UtilHelper.decimalStrCompare(x.get("LPNQTY"),"1")==0){
                    List<Map<String,String>>  snList = SerialInventory.findByLpn(x.get("ID"),true);
                    if(snList.size()!=1) ExceptionHelper.throwRfFulfillLogicException("箱号"+x.get("ID")+"的库存数量为1，但对应的唯一码库存的数量为"+snList.size()+ "，请联系管理员");
                    x.put("SERIALNUMBER",snList.get(0).get("SERIALNUMBERLONG"));
                }else {
                    x.put("SERIALNUMBER","");
                }
            });
//
//            EXEDataObject theOutDO = SocketHelper.list2ExeSocketData(list);
//
//            context.theEXEDataObjectStack.push(theOutDO);


            serviceDataHolder.setOutputData(list);

        } catch (Exception e) {
            ExceptionHelper.throwRfFulfillLogicException(e.getMessage());
        }finally {
            
        }

    }



}
