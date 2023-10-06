package com.enhantec.wms.backend.common.inventory;


import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;

import java.util.Map;

public class VLotAttribute {


    public static Map<String, String> getEnterpriseReceiptLotInfo( String elot) {
        // LOTTABLE01, LOTTABLE10 未使用
        //LOTTABLE04 收货日期, LOTTABLE12 成品生产日期 在相同收货批次下存在不同的值，忽略
        //ELOTTABLE07规格、ELOTTABLE08供应商代码、ELOTTABLE09供应商批次 允许有多条，因为可能出现入库后录入错误导致调整出库又入库的情况，
        // 但是LOTATTRIBUTE表以前的0库存记录不会被删除造成多条记录的情况，因此这里不做比较。
        //但对于收货批次质量状态、复验期、有效期的记录必须唯一，即使存在0库存记录，应以0库存记录的信息为准进行入库，入库后可通过批次放行功能进行相关信息的更新。
        String SQL="select STORERKEY, SKU, " +
                "ELOT," +
                "ELOTTABLE02," + //质量等级
                "ELOTTABLE03," + //质量状态
                "FORMAT(ELOTTABLE04,'"+ LegacyDBHelper.DateTimeFormat+"') AS ELOTTABLE04,"+
                "FORMAT(ELOTTABLE05,'"+ LegacyDBHelper.DateTimeFormat+"') AS ELOTTABLE05,"+//复验日期
                "ELOTTABLE07," +
                "ELOTTABLE08," +
                "ELOTTABLE09," +
                "ELOTTABLE12," +
                "FORMAT(ELOTTABLE11,'"+ LegacyDBHelper.DateTimeFormat+"') AS ELOTTABLE11,"+//有效期
                "FORMAT(ELOTTABLE12,'"+ LegacyDBHelper.DateTimeFormat+"') AS ELOTTABLE12 "+
                " from enterprise.elotattribute " +
                " where elot = ?";

        return DBHelper.getRecord( SQL, new Object[]{ elot},"收货批次",false);

    }

    public static Map<String,Object> findByLot( String lot, boolean checkExist) {

        if(UtilHelper.isEmpty(lot)) ExceptionHelper.throwRfFulfillLogicException("查询的批次不允许为空");


        // LOTTABLE01, LOTTABLE09, LOTTABLE10 未使用
        //LOTTABLE04 收货日期, LOTTABLE12 成品生产日期 在相同收货批次下存在不同的值，忽略

        String SQL="SELECT " +
                "STORERKEY"+
                "SKU"+
                "LOTTABLE01," + //项目号
                "LOTTABLE02," + //存货类型
                "LOTTABLE03," +
                "LOTTABLE04," + //入库日期
                "LOTTABLE05," +
                "LOTTABLE06," +//批号
                "LOTTABLE07," +
                "LOTTABLE08," +
                "LOTTABLE09," +
                "LOTTABLE10," +//采购
                "LOTTABLE11," +
                "LOTTABLE12," +
                "ELOTTABLE01," +
                "ELOTTABLE02," + //质量等级
                "ELOTTABLE03," + //质量状态
                "ELOTTABLE04," +
                "ELOTTABLE05," + //复验日期
                "ELOTTABLE06," +
                "ELOTTABLE07," +//规格
                "ELOTTABLE08," +//供应商
                "ELOTTABLE09," +//供应商批次
                "ELOTTABLE10," +
                "ELOTTABLE11," +//有效期
                "ELOTTABLE12 " +//成品生产日期
                " FROM V_LOTATTRIBUTE " +
                " WHERE LOT = ?";

        Map<String,Object> record= DBHelper.getRawRecord( SQL, new Object[]{ lot},"批属性");
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("未找到WMS批次"+lot);
        return record;
    }

    public static Map<String, String> findElottableByLottable06( String lottable06, boolean checkExist) {
        String SQL="select " +
                "SKU," +
                "STORERKEY," +
                "ELOTTABLE01," +
                "ELOTTABLE02," +
                "ELOTTABLE03," +
                "ELOTTABLE04," +
                "ELOTTABLE05," +
                "ELOTTABLE06," +
                "ELOTTABLE07," +
                "ELOTTABLE08," +
                "ELOTTABLE09," +
                "ELOTTABLE10," +
                "ELOTTABLE11," +
                "ELOTTABLE12," +
                "ELOTTABLE13," +
                "ELOTTABLE14," +
                "ELOTTABLE15," +
                "ELOTTABLE16," +
                "ELOTTABLE17," +
                "ELOTTABLE18," +
                "ELOTTABLE19," +
                "ELOTTABLE20," +
                "ELOTTABLE21," +
                "ELOTTABLE22," +
                "ELOTTABLE23," +
                "ELOTTABLE24," +
                "ELOTTABLE25 "
                + " from v_elotattribute "
                + " where ELOT = ?";

        Map<String,String> record= DBHelper.getRecord( SQL, new Object[]{ lottable06},"Elottable批属性");
        if(checkExist && record == null) ExceptionHelper.throwRfFulfillLogicException("未找到收货批次"+lottable06);
        return record;
    }


}
