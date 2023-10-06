package com.enhantec.wms.backend.common.base;

import com.enhantec.framework.common.exception.EHApplicationException;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;
import com.enhantec.wms.backend.common.base.code.CDOrderType;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.utils.common.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

import static com.enhantec.wms.backend.utils.common.UtilHelper.str2Decimal;
import static com.enhantec.wms.backend.utils.common.UtilHelper.trimZerosAndToStr;
import static com.enhantec.wms.backend.outbound.OutboundUtils.checkQtyIsAvailableInIDNotes;
import static com.enhantec.wms.backend.outbound.OutboundUtils.checkQtyIsAvailableInLotxLocxId;

public class IDNotes {

    public static void decreaseWgtByIdWithAvailQtyCheck( String id, String openQty, String errorName) {

        checkQtyIsAvailableInIDNotes( id, str2Decimal(openQty, errorName, false));

        checkQtyIsAvailableInLotxLocxId( id, str2Decimal(openQty, errorName, false), BigDecimal.ZERO);

        decreaseWgtById( new BigDecimal(openQty),  id);

    }

    public static Map<String,String> decreaseWgtById( BigDecimal netWgt, String id) {

        Map<String,String> currentIDNotes = DBHelper.getRecordByConditions("IDNOTES", new HashMap<String,Object>(){{
            put("ID",id);
        }},"容器标签数据");

        if(currentIDNotes==null) ExceptionHelper.throwRfFulfillLogicException("未到找容器条码"+id);

        if(UtilHelper.decimalStrCompare(currentIDNotes.get("NETWGT"),netWgt.toPlainString())==-1) ExceptionHelper.throwRfFulfillLogicException("容器标签"+id+"减少的净重"+ trimZerosAndToStr(netWgt) +"不允许大于总净重" +currentIDNotes.get("NETWGT") );
        //对于发运整容器的情况，开封标记应该为0
        String isOpened = UtilHelper.decimalStrCompare(currentIDNotes.get("NETWGT"),netWgt.toPlainString())==0 ? "0":"1";
        isOpened = "1".equals(currentIDNotes.get("ISOPENED")) || "1".equals(isOpened) ? "1":"0" ;

        /**
         * 这里label的毛重和净重使用的是实际的毛重净重
         */
        List<Object> params = new ArrayList<>();
        params.add(isOpened);
        params.add(trimZerosAndToStr(netWgt));
        params.add(trimZerosAndToStr(netWgt));
        params.add(trimZerosAndToStr(netWgt));
        params.add(trimZerosAndToStr(netWgt));
        params.add(EHContextHelper.getUser().getUsername());
        params.add( new Date(Calendar.getInstance().getTimeInMillis()));
        params.add(id);
        DBHelper.executeUpdate("update IDNOTES SET ISOPENED = ?, " +
                "grosswgt = grosswgt - ? , netwgt = netwgt - ?, " +
                "grosswgtlabel = grosswgt - ? , netwgtlabel = netwgt - ?, "+
                "EDITWHO = ?, EDITDATE = ? WHERE ID = ? ", params);

        return IDNotes.findById( id, true);
    }

    //分装的情况下要更新原始重量，发运操作没有分拆容器所以不需要执行此方法
    public static Map<String,String> syncOriginalWgtById( String id) {

        Map<String,String> currentIDNotes = DBHelper.getRecordByConditions("IDNOTES", new HashMap<String,Object>(){{
            put("ID",id);
        }},"容器标签数据");

        if(currentIDNotes==null) ExceptionHelper.throwRfFulfillLogicException("未到找容器条码"+id);

        List<Object> params = new ArrayList<>();
        params.add(EHContextHelper.getUser().getUsername());
        params.add( new Date(Calendar.getInstance().getTimeInMillis()));
        params.add(id);
        DBHelper.executeUpdate("UPDATE IDNOTES SET ORIGINALGROSSWGT = GROSSWGT , ORIGINALTAREWGT = TAREWGT,ORIGINALNETWGT = NETWGT, EDITWHO = ?, EDITDATE = ? WHERE ID = ? ", params);

        return DBHelper.getRecordByConditions("IDNOTES", new HashMap<String,Object>(){{
            put("ID",id);
        }},"容器标签数据");
    }

    /**
     * 分拆容器：
     * 本方法只在拣货数量和原容器数量不相同，或者批次相同但目标箱号不相同的情况下使用。
     * 注意：批次管理物料的容器不允许指定目标容器条码分拆，因为后期无法溯源。


     * @param grossWgt
     * @param netWgt
     * @param tareWgt
     * @param grossWgtLabel
     * @param netWgtLabel
     * @param tareWgtLabel
     * @param uomLabel
     * @param id
     * @param toid
     * @param orderKey
     * @return
     * @throws Exception
     */
    public static String splitWgtById( BigDecimal grossWgt, BigDecimal netWgt, BigDecimal tareWgt, String grossWgtLabel, String netWgtLabel, String tareWgtLabel, String uomLabel,  String id, String toid, String orderKey, boolean isSplitLpn) {

        Map<String,String> fromIDNotes = IDNotes.findById( id,true);

        Map<String,String> decreasedFromIdNotes = decreaseWgtById( netWgt, id);
        syncOriginalWgtById( id);

        String barrelNumber ="";

        //String newLpn = IdGenerationHelper.createLpnFromExistingLpn(id);
        //增加逻辑，如果拆分后的至容器在库存中，则更新idnotes。

        if(!UtilHelper.isEmpty(orderKey) && !SKU.isSerialControl(fromIDNotes.get("SKU")) && !UtilHelper.isEmpty(toid)) {
            //批号管理的物料，因为需要通过子桶来溯源，必须通过系统自己生成子桶的标签，不允许移动部分桶的数量至另一个桶或者选择拣货的目的桶，只允许分拆，不然桶号就没法根据规则生成了。
            ExceptionHelper.throwRfFulfillLogicException("批次管理的物料不允许指定拣货至容器条码，只允许系统自动生成");
        }

        if(!UtilHelper.isEmpty(toid)) {

            Map<String, String> toIdLotxLocxIdHashMap = LotxLocxId.findById( toid, false);

            if (toIdLotxLocxIdHashMap != null) {

                if(!UtilHelper.isEmpty(orderKey)) {
                    if (UtilHelper.decimalStrCompare(toIdLotxLocxIdHashMap.get("AVAILABLEQTY"), BigDecimal.ZERO.toPlainString()) > 0)
                        ExceptionHelper.throwRfFulfillLogicException("该箱号存在可用库存，不能作为拣货至箱号");
                }

                Map<String, String> toIdNotes = IDNotes.findById( toid, false);

                if (toIdNotes != null && !toIdNotes.get("LOT").equals(decreasedFromIdNotes.get("LOT"))) {
                    if(!UtilHelper.isEmpty(orderKey)) {
                        throw new EHApplicationException("拣货至容器条码/箱号" + toid + "的收货批次和" + id + "的批次不匹配，不允许合箱拣货");
                    }else{
                        throw new EHApplicationException("合并至容器条码/箱号" + toid + "的收货批次和" + id + "的批次不匹配，不允许合箱");
                    }
                }

                Map<String,String> updateFields = new HashMap<>();
                updateFields.put("ORIGINALGROSSWGT", UtilHelper.decimalStrAdd(toIdNotes.get("ORIGINALGROSSWGT"), grossWgt));//原始毛重
                updateFields.put("ORIGINALTAREWGT", UtilHelper.decimalStrAdd(toIdNotes.get("ORIGINALTAREWGT"), tareWgt));//原始皮重
                updateFields.put("ORIGINALNETWGT", UtilHelper.decimalStrAdd(toIdNotes.get("ORIGINALNETWGT"), netWgt));//原始净重
                updateFields.put("GROSSWGT", UtilHelper.decimalStrAdd(toIdNotes.get("GROSSWGT"), grossWgt));//毛重
                updateFields.put("TAREWGT", UtilHelper.decimalStrAdd(toIdNotes.get("TAREWGT"), tareWgt));//皮重
                updateFields.put("NETWGT", UtilHelper.decimalStrAdd(toIdNotes.get("NETWGT"), netWgt));//净重
                updateFields.put("GROSSWGTLABEL", UtilHelper.decimalStrAdd(toIdNotes.get("GROSSWGT"), grossWgt));
                updateFields.put("TAREWGTLABEL", UtilHelper.decimalStrAdd(toIdNotes.get("TAREWGT"), tareWgt));
                updateFields.put("NETWGTLABEL", UtilHelper.decimalStrAdd(toIdNotes.get("NETWGT"), netWgt));
//                updateFields.put("UOMLABEL", "");
                updateFields.put("ISOPENED", "1");
                Map<String,String> whereFields = new HashMap<>();
                whereFields.put("ID", toid);
                LegacyDBHelper.execUpdate( "idnotes", updateFields, whereFields);
                if(UtilHelper.decimalStrCompare(decreasedFromIdNotes.get("NETWGT"),"0") == 0) {
                    IDNotes.archiveIDNotes( decreasedFromIdNotes);
                }

                return toid;
            }else {
                //检查箱号，容器条码的合法性
                if(SKU.isSerialControl(fromIDNotes.get("SKU"))){
                    if(!IDNotes.isBoxId(toid))
                        ExceptionHelper.throwRfFulfillLogicException("绑定的条码不符合唯一码管理物料的箱号规则");
                }else{
                    if(!IDNotes.isLpn(toid)){
                        ExceptionHelper.throwRfFulfillLogicException("绑定的条码不符合批次管理物料的LPN规则");
                        if(fromIDNotes.get("NETWGT").equals(netWgt)){
                            barrelNumber = fromIDNotes.get("BARRELNUMBER");
                        }else{
                            //指定拆分到一个新的LPN的时候，容器条码桶号仍使用原桶号（此时库存里会有两个桶号相同的容器），后期可根据项目实际情况去调整
                            barrelNumber = fromIDNotes.get("BARRELNUMBER");
                        }
                    }
                }
            }
        }

        //toid为空说明需要新创建，不为空说明为合法的唯一码箱号/LPN，可直接使用
        if(UtilHelper.isEmpty(toid)) {

            //  订单号不为空：拣货
            //  订单号为空：移动
            if (!UtilHelper.isEmpty(orderKey)) {

                Map<String,String> orderHashMap = Orders.findByOrderKey(orderKey,true);

                //批次号管理物料不会使用流水号作为LPN，因为无法追溯。这里也暂不关心是否自动生成LPN的配置，只要为空都自动生成。
                if(!SKU.isSerialControl( fromIDNotes.get("SKU"))) {
                    toid = IdGenerationHelper.createLpnOrBoxIdFromExistingLpn( id);
                    barrelNumber = IdGenerationHelper.getBarrelNumberFromLpn( toid);
                }else if(SKU.isSerialControl( fromIDNotes.get("SKU")) && CDOrderType.isBindAndAutoGenerateLpn( fromIDNotes.get("SKU"), orderHashMap.get("TYPE"))) {
                    toid = IdGenerationHelper.createLpnOrBoxIdFromExistingLpn(id);
                    barrelNumber = fromIDNotes.get("BARRELNUMBER");
                } else if (SKU.isSerialControl( fromIDNotes.get("SKU")) &&CDOrderType.isBindAndNOTAutoGenerateLpn( fromIDNotes.get("SKU"), orderHashMap.get("TYPE"))) {
                    ExceptionHelper.throwRfFulfillLogicException("拣货至箱号不允许为空");//该配置目前只支持任务拣货，暂不支持动态拣货和扫描LPN拣货，这两种拣货将设置改为'不需要绑定新箱号'。
                } else {
                    //对于不绑定箱号的情况使用流水码箱号，因为用户不关心拆箱后的箱号。
                    //问题:在多个唯一码不绑定箱号拣货的情况下，多个sn会绑定到一个流水箱号的拣货明细上，考虑到灵活性今后可改为每个SN单独生成一个拣货明细。因为如果需要删除拣货明细，目前会删除该流水箱号下的所有SN。
                    //解决方案：目前前台对于不绑定箱号的情况，扫描唯一码会直接提交当前唯一码给后台进行拣货操作，此时ORDERTYPE UDF2应该选择3短拣后拣货明细自动拆分为两条,以保证当前任务剩下的待拣货数量可以持续完成。
                    //带来的问题：由于短拣配置是在订单类型级别配置，因此对于非唯一码管理的物料也必须是拆分任务的方式，需要在RF手工删除多余的任务，但这个问题对现场操作影响不大，暂不改为订单类型+是否唯一码进行短拣配置。
                    toid = IdGenerationHelper.generateIDByKeyName( "WMSBOX", 11);
                    barrelNumber = fromIDNotes.get("BARRELNUMBER");
                }
            } else {
                //移动操作加上SYSSET对是否拣货到箱号是否允许为空的配置校验，如允许则自动生成箱号，否则报错
                //移动操作需要绑定箱号必为外部箱号，因为流水号不需要绑定。
                if(SKU.isSerialControl( fromIDNotes.get("SKU"))) {
                    if(isSplitLpn) {
                        if(CDSysSet.mustProvideToIdIfSplitSN()){
                            ExceptionHelper.throwRfFulfillLogicException("唯一码管理的物料必须提供分拆至箱号");
                        }else {
                            //isSplitLpn标志仅用于判断移动或容器分拆操作的情况，ORDERKEY不为空则该标志位无效。
                            toid = IdGenerationHelper.createLpnOrBoxIdFromExistingLpn(id);
                        }
                    }else {
                        //如果直接移动唯一码，则箱号使用流水码
                        toid = IdGenerationHelper.generateIDByKeyName( "WMSBOX", 10);
                    }
                    barrelNumber = fromIDNotes.get("BARRELNUMBER");//箱号暂时赋值为原箱号，因为对唯一码意义并不大
                }else {//批次管理物料
                    toid = IdGenerationHelper.createLpnOrBoxIdFromExistingLpn( id);
                    barrelNumber = IdGenerationHelper.getBarrelNumberFromLpn( toid);
                }

            }

        }

        //add New IDNotes
        String userid = EHContextHelper.getUser().getUsername();
        Map<String,String> IDNOTES = new HashMap<String,String>();
        IDNOTES.put("AddWho", userid);
        IDNOTES.put("EditWho", userid);
        IDNOTES.put("STORERKEY", decreasedFromIdNotes.get("STORERKEY"));
        IDNOTES.put("SKU", decreasedFromIdNotes.get("SKU"));        //物料代码
        IDNOTES.put("UOM", decreasedFromIdNotes.get("UOM"));//	  	计量单位
        IDNOTES.put("PACKKEY", decreasedFromIdNotes.get("PACKKEY"));//	  包装
        IDNOTES.put("ORIGINALGROSSWGT", trimZerosAndToStr(grossWgt));//	原始毛重
        IDNOTES.put("ORIGINALTAREWGT", trimZerosAndToStr(tareWgt));// 原始皮重
        IDNOTES.put("ORIGINALNETWGT", trimZerosAndToStr(netWgt));//	 原始净重
        IDNOTES.put("GROSSWGT", trimZerosAndToStr(grossWgt));//	毛重
        IDNOTES.put("TAREWGT", trimZerosAndToStr(tareWgt));// 皮重
        IDNOTES.put("NETWGT", trimZerosAndToStr(netWgt));//	 净重
        IDNOTES.put("GROSSWGTLABEL", grossWgtLabel);//原毛重标签量
        IDNOTES.put("TAREWGTLABEL", tareWgtLabel);//原皮重标签量
        IDNOTES.put("NETWGTLABEL", netWgtLabel);//原净重标签量
        IDNOTES.put("UOMLABEL", uomLabel);//采集读取的计量单位
        IDNOTES.put("ID", toid);//LPN
        IDNOTES.put("BARRELNUMBER", barrelNumber);//桶号
        IDNOTES.put("TOTALBARREL", decreasedFromIdNotes.get("TOTALBARREL"));//	  总桶号
        IDNOTES.put("BARRELDESCR", barrelNumber + " / " + decreasedFromIdNotes.get("TOTALBARREL"));//桶描述
        IDNOTES.put("LOT", decreasedFromIdNotes.get("LOT"));
        IDNOTES.put("PROJECTCODE", decreasedFromIdNotes.get("PROJECTCODE"));
        IDNOTES.put("ORDERKEY", orderKey);
        IDNOTES.put("ISOPENED", "1");
        IDNOTES.put("MEMO", decreasedFromIdNotes.get("MEMO"));//备注
        IDNOTES.put("FROMID",id);
//        IDNOTES.put("FROMLOC",decreasedFromIdNotes.get("FROMLOC"));
        LegacyDBHelper.ExecInsert( "IDNOTES", IDNOTES);

        if(UtilHelper.decimalStrCompare(decreasedFromIdNotes.get("NETWGT"),"0") == 0) {
            IDNotes.archiveIDNotes( decreasedFromIdNotes);
        }

        return toid;
    }

    public static void update( String id, Map<String,String> updateFields) {

        if(UtilHelper.isEmpty(id)) throw new EHApplicationException("待更新的容器号不能为空");

        String userid = EHContextHelper.getUser().getUsername();

        updateFields.put("EditWho", userid);

        LegacyDBHelper.execUpdate( "IDNOTES",updateFields, new HashMap<String,String>(){{put("ID",id);}});
    }

    public static Map<String,String> findAvailInvById(String id,boolean checkExist) throws DBResourceException {


        if(UtilHelper.isEmpty(id)) ExceptionHelper.throwRfFulfillLogicException("ID不能为空");

        Map<String,String> record = DBHelper.getRecord("select * from idnotes where NETWGT > 0 AND id=?", new Object[]{id},"容器标签");
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("未找到容器标签的库存");

        return record;



    }

    //目前IDNOTES表中的数据NETWGT都大于0，出现为0的自动移至历史数据。
    //TODO:移动拆分容器数量为0时，归档IDNOTES
    public static Map<String,String> findById(String id,boolean checkExist) throws DBResourceException {


        if(UtilHelper.isEmpty(id)) ExceptionHelper.throwRfFulfillLogicException("ID不能为空");

        Map<String,String>  record = DBHelper.getRecord("select * from idnotes where id=?", new Object[]{id},"容器标签");
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("容器标签"+id+"不存在");

        return record;

    }

    public static void delete(String id) throws DBResourceException {

        if(UtilHelper.isEmpty(id)) ExceptionHelper.throwRfFulfillLogicException("ID不能为空");

        String netWgt = findById(id,true).get("NETWGT");

         if(UtilHelper.decimalStrCompare(netWgt,"0")!=0) ExceptionHelper.throwRfFulfillLogicException("容器标签数量不为零，不允许删除");
         DBHelper.executeUpdate("delete from idnotes where id=? ", new Object[]{id});

    }

    /**
     * 判断传进来的数据是否是唯一码使用的箱号
     * @param id
     * @return 容器条码false/箱号true
     */
    public static boolean isBoxId( String id) {
        List<String> enableWarehouses = System.getEnableWarehouses();
        for (String enableWarehouse : enableWarehouses) {
            String boxPrefix = CDSysSet.getBoxPrefix(enableWarehouse).get("UDF1");
            String lotPrefix = CDSysSet.getLotPrefix(enableWarehouse).get("UDF1");
            if(id.startsWith(boxPrefix+lotPrefix)){
                return true;
            }
        }
        return false;
    }
    public static boolean isLpnOrBoxId(String id){
        return isLpn(id) || isBoxId(id);
    }

    /**
     * 判断传进来的数据是否是按批次管理的容器条码
     * @param id
     * @return 容器条码false/箱号true
     */
    public static boolean isLpn( String id) {
        List<String> enableWarehouses = System.getEnableWarehouses();
        for (String enableWarehouse : enableWarehouses) {
            String lpnPrefix = CDSysSet.getLpnPrefix(enableWarehouse).get("UDF1");
            String lotPrefix = CDSysSet.getLotPrefix(enableWarehouse).get("UDF1");
            if(id.startsWith(lpnPrefix+lotPrefix)){
                return true;
            }
        }
        return false;
    }

//    public static Map<String,String> ship( String id) throws Exception {
//
//        findById(id,true);
//        List<Object> params = new ArrayList<>();
//        params.add(id));
//        DBHelper.executeUpdate("update IDNOTES SET grosswgt = tarewgt , netwgt = 0 WHERE ID = ? ", params);
//        return DBHelper.getRecordByConditions("IDNOTES", new HashMap<String,Object>(){{
//            put("ID",id));
//        }});
//
//    }


    public static void archiveIDNotes( Map<String, String> shippedIdNotesHashMap) {
        Map<String,String> idNotesHistory=new HashMap<>();
        idNotesHistory.put("ADDWHO", EHContextHelper.getUser().getUsername());
        idNotesHistory.put("EDITWHO", EHContextHelper.getUser().getUsername());
        idNotesHistory.put("ADDDATE", LocalDateTime.now().toString());
        idNotesHistory.put("EDITDATE", LocalDateTime.now().toString());
        String yyyyMMStr = new SimpleDateFormat("yyyyMM").format(new Date(Calendar.getInstance().getTimeInMillis()));
        idNotesHistory.put("ADDMONTH", yyyyMMStr);
        idNotesHistory.put("BARRELDESCR", shippedIdNotesHashMap.get("BARRELDESCR"));
        idNotesHistory.put("BARRELNUMBER", shippedIdNotesHashMap.get("BARRELNUMBER"));
        idNotesHistory.put("FROMID", shippedIdNotesHashMap.get("FROMID"));
        idNotesHistory.put("GROSSWGT", shippedIdNotesHashMap.get("GROSSWGT"));
        idNotesHistory.put("ID", shippedIdNotesHashMap.get("ID"));
        idNotesHistory.put("INSPECTIONDATE", shippedIdNotesHashMap.get("INSPECTIONDATE"));
        idNotesHistory.put("ISOPENED", shippedIdNotesHashMap.get("ISOPENED"));
        idNotesHistory.put("LASTSHIPPEDLOC", shippedIdNotesHashMap.get("LASTSHIPPEDLOC"));
//        idNotesHistory.put("LASTLOC", shippedIdNotesHashMap.get("LOC")); //该ID的上一个库位
        idNotesHistory.put("LASTID", shippedIdNotesHashMap.get("ID")); //该ID的上一个ID
        idNotesHistory.put("LOCBEFOREPACK", shippedIdNotesHashMap.get("LOCBEFOREPACK"));
        idNotesHistory.put("LOT", shippedIdNotesHashMap.get("LOT"));
        idNotesHistory.put("MEMO", shippedIdNotesHashMap.get("MEMO"));
        idNotesHistory.put("NETWGT", shippedIdNotesHashMap.get("NETWGT"));
        idNotesHistory.put("ORDERKEY", shippedIdNotesHashMap.get("ORDERKEY"));
        idNotesHistory.put("ORIGINALGROSSWGT", shippedIdNotesHashMap.get("ORIGINALGROSSWGT"));
        idNotesHistory.put("ORIGINALNETWGT", shippedIdNotesHashMap.get("ORIGINALNETWGT"));
        idNotesHistory.put("ORIGINALTAREWGT", shippedIdNotesHashMap.get("ORIGINALTAREWGT"));
        idNotesHistory.put("ORIGINRECEIPTKEY", shippedIdNotesHashMap.get("ORIGINRECEIPTKEY"));
        idNotesHistory.put("ORIGINRECEIPTLINENUMBER", shippedIdNotesHashMap.get("ORIGINRECEIPTLINENUMBER"));
        idNotesHistory.put("PACKKEY", shippedIdNotesHashMap.get("PACKKEY"));
        idNotesHistory.put("PROJECTCODE", shippedIdNotesHashMap.get("PROJECTCODE"));
        idNotesHistory.put("PROJECTCODEQTY", shippedIdNotesHashMap.get("PROJECTCODEQTY"));
        idNotesHistory.put("REGROSSWGT", shippedIdNotesHashMap.get("REGROSSWGT"));
        idNotesHistory.put("RETURNTIMES", shippedIdNotesHashMap.get("RETURNTIMES"));
        idNotesHistory.put("SERIALKEY", shippedIdNotesHashMap.get("SERIALKEY"));
        idNotesHistory.put("SKU", shippedIdNotesHashMap.get("SKU"));
        idNotesHistory.put("STORERKEY", shippedIdNotesHashMap.get("STORERKEY"));
        idNotesHistory.put("TAREWGT", shippedIdNotesHashMap.get("TAREWGT"));
        idNotesHistory.put("TOTALBARREL", shippedIdNotesHashMap.get("TOTALBARREL"));
        idNotesHistory.put("UOM", shippedIdNotesHashMap.get("UOM"));
        idNotesHistory.put("WHSEID", shippedIdNotesHashMap.get("WHSEID"));
        idNotesHistory.put("PRODLOTEXPECTED", shippedIdNotesHashMap.get("PRODLOTEXPECTED"));

        LegacyDBHelper.ExecInsert( "IDNOTESHISTORY", idNotesHistory);
        IDNotes.delete( shippedIdNotesHashMap.get("ID"));

        boolean isSkuSerialControl = SKU.isSerialControl(shippedIdNotesHashMap.get("SKU"));

        if(isSkuSerialControl){

            List<Map<String,String>> snList = SerialInventory.findByLpn(shippedIdNotesHashMap.get("ID"),true);

            for(Map<String,String> sn :snList){
                Map<String,String> snHistory=new HashMap<>();
                snHistory.put("IDSERIALKEY", shippedIdNotesHashMap.get("SERIALKEY"));
                snHistory.put("ID", shippedIdNotesHashMap.get("ID"));
                snHistory.put("SKU", shippedIdNotesHashMap.get("SKU"));
                snHistory.put("SERIALNUMBER", sn.get("SERIALNUMBER"));
                snHistory.put("LOT", shippedIdNotesHashMap.get("LOT"));
                snHistory.put("NETWEIGHT",sn.get("NETWEIGHT"));
                snHistory.put("SNUOM",sn.get("DATA2"));
                snHistory.put("ADDWHO", EHContextHelper.getUser().getUsername());
                snHistory.put("EDITWHO", EHContextHelper.getUser().getUsername());
                snHistory.put("ADDDATE", LocalDateTime.now().toString());
                snHistory.put("EDITDATE", LocalDateTime.now().toString());
                snHistory.put("ADDMONTH", yyyyMMStr);
                LegacyDBHelper.ExecInsert( "SNHISTORY", snHistory);
            }


        }

    }

    /**
     * 查询idnotes的所有批次及物料信息


     * @param id
     * @param checkExist
     * @return
     */
    public static Map<String,String> findByIdWithLotInfo(String id,boolean checkExist){
        String sql = "SELECT A.*,ELOT.*,s.*"+
                " FROM IDNOTES A , V_LOTATTRIBUTE ELOT, SKU S WHERE A.LOT = ELOT.LOT AND A.SKU = S.SKU AND A.ID = ? ";
        return DBHelper.getRecord(sql,new Object[]{id},"容器条码",checkExist);
    }

}
