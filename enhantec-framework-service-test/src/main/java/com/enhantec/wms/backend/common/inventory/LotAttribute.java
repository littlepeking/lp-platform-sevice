package com.enhantec.wms.backend.common.inventory;


import com.enhantec.wms.backend.utils.common.*;
import com.enhantec.framework.common.utils.EHContextHelper;

import com.enhantec.framework.common.utils.EHContextHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class LotAttribute {


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

        if(UtilHelper.isEmpty(lot)) ExceptionHelper.throwRfFulfillLogicException("未找到查询的批次"+lot);


        // LOTTABLE01, LOTTABLE09, LOTTABLE10 未使用
        //LOTTABLE04 收货日期, LOTTABLE12 成品生产日期 在相同收货批次下存在不同的值，忽略

        String SQL="SELECT " +
                "STORERKEY,"+
                "SKU,"+
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

    public static String findMatchedLot(String lot,
                                        String StorerKey, String Sku
            , String LOTTABLE01, String LOTTABLE02, String LOTTABLE03, String LOTTABLE04, String LOTTABLE05, String LOTTABLE06, String LOTTABLE07, String LOTTABLE08, String LOTTABLE09, String LOTTABLE10, String LOTTABLE11, String LOTTABLE12
    )
    {
        Map<String,String> lotAttribute = null;
        if (StringUtils.isEmpty(lot))
        {
            lotAttribute = DBHelper.getRecord("SELECT * FROM LOTATTRIBUTE WHERE STORERKEY=? AND SKU=?"
                            +" AND LOTTABLE01=? AND LOTTABLE02=? AND LOTTABLE03=?"
                            +" AND "+ (LOTTABLE04 == null ? " LOTTABLE04 IS NULL" : "LOTTABLE04 = '"+LOTTABLE04+"'")
                            +" AND "+ (LOTTABLE05 == null ? " LOTTABLE05 IS NULL" : "LOTTABLE05 = '"+LOTTABLE05+"'")
                            +" AND LOTTABLE06=? AND LOTTABLE07=? AND LOTTABLE08=? AND LOTTABLE09=? AND LOTTABLE10=?"
                            +" AND "+ (LOTTABLE11 == null ? " LOTTABLE11 IS NULL" : "LOTTABLE11 = '"+LOTTABLE11+"'")
                            +" AND "+ (LOTTABLE12 == null ? " LOTTABLE12 IS NULL" : "LOTTABLE12 = '"+LOTTABLE12+"'")
                    , new Object[] {StorerKey,Sku
                            ,UtilHelper.getString(LOTTABLE01," "),
                            UtilHelper.getString(LOTTABLE02," "),
                            UtilHelper.getString(LOTTABLE03," "),
                            UtilHelper.getString(LOTTABLE06," "),
                            UtilHelper.getString(LOTTABLE07," "),
                            UtilHelper.getString(LOTTABLE08," "),
                            UtilHelper.getString(LOTTABLE09," "),
                            UtilHelper.getString(LOTTABLE10," "),
                    });
            if (lotAttribute==null) return null;
        }
        else
        {
            lotAttribute=DBHelper.getRecord( "SELECT * FROM LOTATTRIBUTE WHERE LOT=?", new Object[] {lot});
            if (lotAttribute==null) throw new FulfillLogicException("系统中无此LOT("+lot+")");
        }
        if (!StorerKey.equals(UtilHelper.getString(lotAttribute.get("STORERKEY"), ""))) throw new FulfillLogicException("LOT("+lot+")货主匹配错误");
        if (!Sku.equals(UtilHelper.getString(lotAttribute.get("SKU"), ""))) throw new FulfillLogicException("LOT("+lot+")物料代码匹配错误");
        if (!UtilHelper.getString(LOTTABLE01," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE01"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE01匹配错误");
        if (!UtilHelper.getString(LOTTABLE02," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE02"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE02匹配错误");
        if (!UtilHelper.getString(LOTTABLE03," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE03"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE03匹配错误");
        if (!UtilHelper.getString(LOTTABLE04," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE04"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE04匹配错误");
        if (!UtilHelper.getString(LOTTABLE05," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE05"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE05匹配错误");
        if (!UtilHelper.getString(LOTTABLE06," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE06"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE06匹配错误");
        if (!UtilHelper.getString(LOTTABLE07," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE07"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE07匹配错误");
        if (!UtilHelper.getString(LOTTABLE08," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE08"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE08匹配错误");
        if (!UtilHelper.getString(LOTTABLE09," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE09"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE09匹配错误");
        if (!UtilHelper.getString(LOTTABLE10," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE10"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE10匹配错误");
        if (!UtilHelper.getString(LOTTABLE11," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE11"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE11匹配错误");
        if (!UtilHelper.getString(LOTTABLE12," ").equals(UtilHelper.getString(lotAttribute.get("LOTTABLE12"), " "))) throw new FulfillLogicException("LOT("+lot+")LOTTABLE12匹配错误");

        return lotAttribute.get("LOT");
    }

    public static String createLot( String StorerKey,String Sku
            ,String LOTTABLE01,String LOTTABLE02,String LOTTABLE03,String LOTTABLE04,String LOTTABLE05,String LOTTABLE06,String LOTTABLE07,String LOTTABLE08,String LOTTABLE09,String LOTTABLE10,String LOTTABLE11,String LOTTABLE12
    )
    {
        String LOT=IdGenerationHelper.getNCounterStrWithLength("LOT",10);
        LinkedHashMap<String,String> mLOT=new LinkedHashMap<>();
        mLOT.put("ADDWHO",EHContextHelper.getUser().getUsername());
        mLOT.put("EDITWHO",EHContextHelper.getUser().getUsername());
        mLOT.put("LOT",LOT);
        mLOT.put("STORERKEY",StorerKey);
        mLOT.put("SKU",Sku);
        mLOT.put("LOTTABLE01",UtilHelper.getString(LOTTABLE01, " "));
        mLOT.put("LOTTABLE02",UtilHelper.getString(LOTTABLE02, " "));
        mLOT.put("LOTTABLE03",UtilHelper.getString(LOTTABLE03, " "));
        mLOT.put("LOTTABLE04",UtilHelper.getString(LOTTABLE04, null));
        mLOT.put("LOTTABLE05",UtilHelper.getString(LOTTABLE05, null));
        mLOT.put("LOTTABLE06",UtilHelper.getString(LOTTABLE06, " "));
        mLOT.put("LOTTABLE07",UtilHelper.getString(LOTTABLE07, " "));
        mLOT.put("LOTTABLE08",UtilHelper.getString(LOTTABLE08, " "));
        mLOT.put("LOTTABLE09",UtilHelper.getString(LOTTABLE09, " "));
        mLOT.put("LOTTABLE10",UtilHelper.getString(LOTTABLE10, " "));
        mLOT.put("LOTTABLE11",UtilHelper.getString(LOTTABLE11, null));
        mLOT.put("LOTTABLE12",UtilHelper.getString(LOTTABLE12, null));
        LegacyDBHelper.ExecInsert( "LOTATTRIBUTE", mLOT);
        return LOT;
    }


}
