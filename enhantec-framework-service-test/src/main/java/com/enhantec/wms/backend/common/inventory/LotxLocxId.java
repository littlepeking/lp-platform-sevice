package com.enhantec.wms.backend.common.inventory;

import com.enhantec.wms.backend.common.Const;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import static com.enhantec.wms.backend.common.Const.CommonLottableFields;

public class LotxLocxId {

    private static  String idDetailSelectClause =
            "select a.ID,a.LOT,a.LOC,a.ID,a.STORERKEY,a.SKU,a.QTY,a.QTYALLOCATED,a.QTYPICKED,a.QTY-a.QTYPICKED-a.QTYALLOCATED AVAILABLEQTY, a.STATUS "
                    + ",b.LOTTABLE01,b.LOTTABLE02,b.ELOTTABLE02,b.LOTTABLE03,b.ELOTTABLE03"
                    + ",FORMAT(b.LOTTABLE04,'"+ Const.DateTimeFormat+"') AS LOTTABLE04"
                    + ",b.LOTTABLE05,FORMAT(b.ELOTTABLE05,'"+Const.DateTimeFormat+"') as ELOTTABLE05"
                    + ",b.ELOTTABLE06,b.LOTTABLE06,b.LOTTABLE07,b.ELOTTABLE07,b.LOTTABLE08,b.ELOTTABLE08,b.LOTTABLE09,b.ELOTTABLE09,b.LOTTABLE10"
                    + ",b.LOTTABLE11,FORMAT(b.ELOTTABLE11,'"+Const.DateTimeFormat+"') as ELOTTABLE11"
                    + ",b.LOTTABLE12,FORMAT(b.ELOTTABLE12,'"+Const.DateTimeFormat+"') as ELOTTABLE12"
                    + ",b.ELOTTABLE13,b.ELOTTABLE14,b.ELOTTABLE15,b.ELOTTABLE16,b.ELOTTABLE17,b.ELOTTABLE18,b.ELOTTABLE19,b.ELOTTABLE20,b.ELOTTABLE21"
                    + ",D.NETWGT, D.GROSSWGT,D.TAREWGT,D.ORIGINALNETWGT,D.ORIGINALGROSSWGT, D.ORIGINALTAREWGT, D.PACKKEY,D.UOM, D.BARRELDESCR ,D.TOTALBARREL, D.PROJECTCODE , D.LASTSHIPPEDLOC,D.ISOPENED,D.RETURNTIMES,D.BARRELNUMBER,D.TOTALBARREL, " +Const.commonSkuFieldsWithAlias;

    public static Map<String, String> findAvailInvByLocId( String FROMLOC, String FROMID, boolean useHold, boolean checkExist) {

        String SQL = idDetailSelectClause
                + " from  lotxlocxid a,v_lotattribute b, IDNOTES d, SKU s "
                + " where a.lot=b.lot and a.SKU = s.SKU and a.qty>0 and d.netwgt>0 and A.QTY-A.QTYPICKED-A.QTYALLOCATED > 0 AND A.LOC= ? and a.id = d.id and a.id = ? ";
        if(!useHold) SQL += " AND A.STATUS <> 'HOLD'";
        Map<String,String> record= DBHelper.getRecord( SQL, new Object[]{ FROMLOC, FROMID},"库存明细");
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("未找到可用库存");

        int holdRecords = Integer.parseInt(DBHelper.getStringValue("SELECT count(1) from inventoryhold where hold = '1' and (lot = ? or loc = ? or id = ?) ",
                new Object[]{record.get("LOT"), FROMLOC, FROMID},
                "").toString());

        if(!useHold && holdRecords>0) ExceptionHelper.throwRfFulfillLogicException("库存已被冻结，不允许使用");

        return record;
    }

    public static Map<String, String> findFullAvailInvById( String FROMLOC, String FROMID, String errMsg) {

        String SQL = idDetailSelectClause
                + " from  lotxlocxid a,v_lotattribute b, IDNOTES d, SKU s "
                + " where a.lot=b.lot and a.SKU = s.SKU and a.qty>0 and d.netwgt>0 and A.QTY-A.QTYPICKED-A.QTYALLOCATED = A.QTY AND A.LOC= ? and a.id = d.id and a.id = ? ";
        Map<String,String> record= DBHelper.getRecord( SQL, new Object[]{ FROMLOC, FROMID},"库存明细");
        if(record == null) ExceptionHelper.throwRfFulfillLogicException(errMsg);

        return record;
    }

    public static Map<String, String> findFullAvailInvById( String FROMID, String errMsg) {

        String SQL = idDetailSelectClause
                + " from  lotxlocxid a,v_lotattribute b, IDNOTES d, SKU s "
                + " where a.lot=b.lot and a.SKU = s.SKU and a.qty>0 and d.netwgt>0 and A.QTY-A.QTYPICKED-A.QTYALLOCATED = A.QTY and a.id = d.id and a.id = ? ";
        Map<String,String> record= DBHelper.getRecord( SQL, new Object[]{  FROMID},"库存明细");
        if(record == null) ExceptionHelper.throwRfFulfillLogicException(errMsg);

        return record;
    }



    public static Map<String, String> findAvailInvById( String FROMID, boolean useHold, boolean checkExist) {

        String SQL = idDetailSelectClause
                + " from  lotxlocxid a,v_lotattribute b, IDNOTES d , SKU s "
                + " where a.lot=b.lot and a.SKU = s.SKU and a.qty>0 and d.netwgt>0 and A.QTY-A.QTYPICKED-A.QTYALLOCATED > 0 AND a.id = d.id and a.id = ? ";
        if(!useHold) SQL += " AND A.STATUS <> 'HOLD'";
        Map<String,String> record= DBHelper.getRecord( SQL, new Object[]{ FROMID},"库存明细");
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("未找到ID为"+FROMID+"的可用库存");

        int holdRecords = Integer.parseInt(DBHelper.getStringValue("SELECT count(1) from inventoryhold where hold = '1' and (lot = ? or loc = ? or id = ?) ",
                new Object[]{record.get("LOT"),record.get("LOC"), FROMID},
                "").toString());

        if(!useHold && holdRecords>0) ExceptionHelper.throwRfFulfillLogicException("库存已被冻结，不允许使用");

        return record;
    }

    public static Map<String, String> findById( String FROMID, boolean checkExist) {

        String SQL = idDetailSelectClause
                + " from  lotxlocxid a,v_lotattribute b, IDNOTES d , SKU s "
                + " where a.lot=b.lot and a.SKU = s.SKU and a.qty>0 and d.netwgt>0 and a.id = d.id and a.id = ? ";
        Map<String,String> record= DBHelper.getRecord( SQL, new Object[]{ FROMID},"库存明细");
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("未找到容器条码"+FROMID);
        return record;

    }


    public static Map<String,String> findBySkuAndSerialNum( String sku, String serialNum) {


        Map<String,String> record = DBHelper.getRecord( "SELECT C.STORERKEY,C.LOT,C.LOC,C.QTY, C.QTYALLOCATED,C.QTYPICKED, C.ID," + Const.commonSkuFieldsWithAlias +
                        ",A.GROSSWGT,A.TAREWGT,A.NETWGT,ELOT.ELOTTABLE03 QUALITYSTATUS, " +
                        "  C.QTY-QTYALLOCATED-QTYPICKED AVAILABLEQTY, "+ CommonLottableFields +", A.BARRELDESCR,A.PACKKEY,A.UOM " +
                        " FROM SERIALINVENTORY SI , IDNOTES A , LOTXLOCXID C,V_LOTATTRIBUTE ELOT ,SKU S " +
                        " WHERE SI.ID =A.ID AND A.ID = C.ID AND ELOT.LOT = C.LOT " +
                        "  AND SI.SKU = S.SKU AND A.NETWGT >0 AND C.QTY - C.QTYALLOCATED - C.QTYPICKED >0 AND SI.SKU = ? AND SI.SERIALNUMBERLONG = ? "
                , new Object[]{sku, serialNum},"箱号",true);

        return record;

    }



    /**
     * 目前仅用于收货后回填IDNOTES.LOT


     * @param FROMID
     * @param checkExist
     * @return
     */
    public static Map<String, String> findWithoutCheckIDNotes( String FROMID, boolean checkExist) {

        String SQL="select a.ID,a.LOT,a.LOC,a.ID,a.STORERKEY,a.SKU,a.QTY,a.QTYALLOCATED,a.QTYPICKED,a.QTY-a.QTYPICKED-a.QTYALLOCATED AVAILABLEQTY, a.STATUS "
                + ",b.LOTTABLE01,b.LOTTABLE02,b.ELOTTABLE02,b.ELOTTABLE03"
                + ",FORMAT(b.LOTTABLE04,'"+ Const.DateTimeFormat+"') AS LOTTABLE04"
                + ",FORMAT(b.ELOTTABLE05,'"+Const.DateTimeFormat+"') as ELOTTABLE05"
                + ",b.ELOTTABLE06,b.LOTTABLE06,b.ELOTTABLE07,b.ELOTTABLE08,b.ELOTTABLE09,b.LOTTABLE10"
                + ",FORMAT(b.ELOTTABLE11,'"+Const.DateTimeFormat+"') as ELOTTABLE11"
                + ",FORMAT(b.ELOTTABLE12,'"+Const.DateTimeFormat+"') as ELOTTABLE12"
                + ",b.LOTTABLE01 PACKKEY"
                + " from  lotxlocxid a,v_lotattribute b "
                + " where a.lot=b.lot and a.qty>0 and a.id = ? ";
        Map<String,String> record= DBHelper.getRecord( SQL, new Object[]{ FROMID},"库存明细");
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("未找到容器条码"+FROMID);
        return record;
    }


    /**
     * 目前仅用于收货后回填IDNOTES.LOT
     * @param storerKey
     * @param fromId
     * @param checkExist
     * @return
     */
    public static Map<String, String> findWithoutCheckIDNotes(String storerKey, String fromId, boolean checkExist) {

        String SQL="select a.ID,a.LOT,a.LOC,a.ID,a.STORERKEY,a.SKU,a.QTY,a.QTYALLOCATED,a.QTYPICKED,a.QTY-a.QTYPICKED-a.QTYALLOCATED AVAILABLEQTY, a.STATUS "
                + ",b.LOTTABLE01,b.LOTTABLE02,b.ELOTTABLE02,b.ELOTTABLE03"
                + ",FORMAT(b.LOTTABLE04,'"+ Const.DateTimeFormat+"') AS LOTTABLE04"
                + ",FORMAT(b.ELOTTABLE05,'"+Const.DateTimeFormat+"') as ELOTTABLE05"
                + ",b.ELOTTABLE06,b.LOTTABLE06,b.ELOTTABLE07,b.ELOTTABLE08,b.ELOTTABLE09,b.LOTTABLE10"
                + ",FORMAT(b.ELOTTABLE11,'"+Const.DateTimeFormat+"') as ELOTTABLE11"
                + ",FORMAT(b.ELOTTABLE12,'"+Const.DateTimeFormat+"') as ELOTTABLE12"
                + ",b.LOTTABLE01 PACKKEY"
                + " from  lotxlocxid a,v_lotattribute b "
                + " where a.lot=b.lot and a.qty>0 and a.STORERKEY = ? and a.ID = ? ";
        Map<String,String> record= DBHelper.getRecord( SQL, new Object[]{storerKey, fromId},"库存明细",checkExist);
//        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("在货主"+storerKey+"下未找到容器条码"+fromId);
        return record;
    }

    /**
     * 目前仅用于收货后回填IDNOTES.LOT
     * @param storerKey
     * @param fromId
     * @param checkExist
     * @return
     */
    public static Map<String, Object> findRawRecordWithoutCheckIDNotes(String storerKey, String fromId, boolean checkExist) {

        String SQL="select a.ID,a.LOT,a.LOC,a.ID,a.STORERKEY,a.SKU,a.QTY,a.QTYALLOCATED,a.QTYPICKED,a.QTY-a.QTYPICKED-a.QTYALLOCATED AVAILABLEQTY, a.STATUS "
                + ",b.LOTTABLE01,b.LOTTABLE02,b.ELOTTABLE02,b.ELOTTABLE03"
                + ",FORMAT(b.LOTTABLE04,'"+ Const.DateTimeFormat+"') AS LOTTABLE04"
                + ",FORMAT(b.ELOTTABLE05,'"+Const.DateTimeFormat+"') as ELOTTABLE05"
                + ",b.ELOTTABLE06,b.LOTTABLE06,b.ELOTTABLE07,b.ELOTTABLE08,b.ELOTTABLE09,b.LOTTABLE10"
                + ",FORMAT(b.ELOTTABLE11,'"+Const.DateTimeFormat+"') as ELOTTABLE11"
                + ",FORMAT(b.ELOTTABLE12,'"+Const.DateTimeFormat+"') as ELOTTABLE12"
                + ",b.LOTTABLE01 PACKKEY"
                + " from  lotxlocxid a,v_lotattribute b "
                + " where a.lot=b.lot and a.qty>0 and a.STORERKEY = ? and a.ID = ? ";
        Map<String,Object> record= DBHelper.getRawRecord( SQL, new Object[]{storerKey, fromId},"库存明细",checkExist);
//        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("在货主"+storerKey+"下未找到容器条码"+fromId);
        return record;
    }

    public static Map<String, Object> findRawRecordWithoutCheckIDNotes(String storerKey, String loc, String id, boolean checkExist) {

        String SQL="select a.ID,a.LOT,a.LOC,a.ID,a.STORERKEY,a.SKU,a.QTY,a.QTYALLOCATED,a.QTYPICKED,a.QTY-a.QTYPICKED-a.QTYALLOCATED AVAILABLEQTY, a.STATUS "
                + ",b.LOTTABLE01,b.LOTTABLE02,b.ELOTTABLE02,b.ELOTTABLE03"
                + ",FORMAT(b.LOTTABLE04,'"+ Const.DateTimeFormat+"') AS LOTTABLE04"
                + ",FORMAT(b.ELOTTABLE05,'"+Const.DateTimeFormat+"') as ELOTTABLE05"
                + ",b.ELOTTABLE06,b.LOTTABLE06,b.ELOTTABLE07,b.ELOTTABLE08,b.ELOTTABLE09,b.LOTTABLE10"
                + ",FORMAT(b.ELOTTABLE11,'"+Const.DateTimeFormat+"') as ELOTTABLE11"
                + ",FORMAT(b.ELOTTABLE12,'"+Const.DateTimeFormat+"') as ELOTTABLE12"
                + ",b.LOTTABLE01 PACKKEY"
                + " FROM  LOTXLOCXID a,V_LOTATTRIBUTE b "
                + " WHERE a.LOT=b.LOT AND a.QTY>0 AND a.STORERKEY = ? AND a.LOC = ? AND a.ID = ? ";
        Map<String,Object> record= DBHelper.getRawRecord( SQL, new Object[]{storerKey, loc, id},"库存明细",checkExist);
        return record;
    }

    public static Map<String, Object> findRawRecordWithoutCheckIDNotesAngQty(String storerKey, String loc, String id, boolean checkExist) {

        String SQL="select a.ID,a.LOT,a.LOC,a.ID,a.STORERKEY,a.SKU,a.QTY,a.QTYALLOCATED,a.QTYPICKED,a.QTY-a.QTYPICKED-a.QTYALLOCATED AVAILABLEQTY, a.STATUS "
                + ",b.LOTTABLE01,b.LOTTABLE02,b.ELOTTABLE02,b.ELOTTABLE03"
                + ",FORMAT(b.LOTTABLE04,'"+ Const.DateTimeFormat+"') AS LOTTABLE04"
                + ",FORMAT(b.ELOTTABLE05,'"+Const.DateTimeFormat+"') as ELOTTABLE05"
                + ",b.ELOTTABLE06,b.LOTTABLE06,b.ELOTTABLE07,b.ELOTTABLE08,b.ELOTTABLE09,b.LOTTABLE10"
                + ",FORMAT(b.ELOTTABLE11,'"+Const.DateTimeFormat+"') as ELOTTABLE11"
                + ",FORMAT(b.ELOTTABLE12,'"+Const.DateTimeFormat+"') as ELOTTABLE12"
                + ",b.LOTTABLE01 PACKKEY"
                + " FROM  LOTXLOCXID a,V_LOTATTRIBUTE b "
                + " WHERE a.LOT=b.LOT AND a.STORERKEY = ? AND a.LOC = ? AND a.ID = ? ";
        Map<String,Object> record= DBHelper.getRawRecord( SQL, new Object[]{storerKey, loc, id},"库存明细",checkExist);
        return record;
    }


    /**
     * 查找待发运明细


     * @param parentId
     * @return
     * @throws Exception
     */
    public static List<Map<String,String>> findPickedIdsByParentId( String parentId) throws Exception {


        List<Map<String,String>> orderHashMap = DBHelper.executeQuery(
                "select DISTINCT O.ORDERKEY, O.TYPE, O.STATUS from IDNOTES A,PICKDETAIL P, ORDERS O " +
                        "WHERE A.ID = P.ID and P.ORDERKEY = O.ORDERKEY and P.STATUS in ('0','5') and A.PARENTID = ?  "
                , new Object[]{parentId});
        if(orderHashMap.size()>1) throw new Exception("不允许扫描箱号"+parentId+"，因为该箱号下库存已分配给了多个订单，请扫描唯一码出库");

        List<Map<String,String>> availInvHashMaps = DBHelper.executeQuery(
                "select 1 from IDNOTES A, LOTXLOCXID L " +
                        "WHERE A.ID = L.ID and L.QTYPICKED <>L.QTY and A.PARENTID = ? "
                , new Object[]{parentId});
        if(availInvHashMaps.size()>0) throw new Exception("不允许扫描箱号"+parentId+"，因为该箱号下仍有未拣货的库存，请扫描唯一码");

        List<Map<String,String>> boxPickDetailHashMaps =  DBHelper.executeQuery(
                "select P.ID, P.ORDERKEY, P.PICKDETAILKEY, P.QTY, P.STATUS from IDNOTES A,PICKDETAIL P " +
                        "WHERE A.ID = P.ID and P.STATUS <>'9' and A.PARENTID = ? "
                , new Object[]{parentId});

        List<Map<String,String>> boxPickedDetailHashMaps = new ArrayList<>();
        if(boxPickDetailHashMaps.size()>0) {
            for (Map<String, String> pickDetailHashMap : boxPickDetailHashMaps) {

                if (pickDetailHashMap.get("STATUS").equals("0")) {
                    throw new Exception("不允许发运箱号" + parentId + "，因为该箱号仍存在待拣货项");
                }else {
                    boxPickedDetailHashMaps.add(pickDetailHashMap);
                }

            }
        }

        if(boxPickedDetailHashMaps.size()==0) throw new Exception("未找到箱号"+parentId+"下的待发运拣货明细");

        return boxPickedDetailHashMaps;
    }

    /**
     * TODO:合并至FIND BY ID
     */
    public static Map<String,String> getAvailInvById( String id) throws Exception {

        Map<String,String> idHashMap = DBHelper.getRecord(
                "select C.STORERKEY,C.LOT,C.LOC,C.QTY, C.QTYALLOCATED,C.QTYPICKED, C.ID," + Const.commonSkuFieldsWithAlias +
                        ",A.GROSSWGT,A.TAREWGT,A.NETWGT,ELOT.ELOTTABLE03 QUALITYSTATUS, " +
                        " C.QTY-QTYALLOCATED-QTYPICKED AVAILABLEQTY, "+ CommonLottableFields+", A.BARRELDESCR,A.PACKKEY, A.UOM " +
                        " from IDNOTES A,LOTXLOCXID C,SKU S,V_LOTATTRIBUTE ELOT " +
                        "WHERE A.ID = C.ID and C.SKU = S.SKU and C.QTY>0 and C.LOT = ELOT.LOT and A.ID=?"
                , new Object[]{id},"容器条码");
        if(idHashMap==null) throw new Exception("未找容器条码"+id+"的可用库存");

        return idHashMap;
    }

}
