package com.enhantec.wms.backend.common;

public class Const {
    public static String DateFormat="MM/dd/yyyy";
    public static String DateTimeFormat="MM/dd/yyyy HH:mm:ss";//MM/dd/yyyy hh24:mi:ss
    //质量状态
    public static final String QUALITYSTATUS_RELEASE = "RELEASE";
    public static final String QUALITYSTATUS_REJECT = "REJECT";
    public static final String QUALITYSTATUS_QUARANTINE = "QUARANTINE";
    public static final String QUALITYSTATUS_CONDIREL = "CONDIREL";


    //收货类型
    public static final String RECEIPT_RF_TYPE_WITH_ASN = "1";
    public static final String RECEIPT_RF_TYPE_RETURN_WITH_ASN = "R1";
    public static final String RECEIPT_RF_TYPE_RETURN_NO_ASN = "R2";
    public static final String RECEIPT_RF_TYPE_RETURN_NO_ASN_BIND_PID = "R3";

    public static final String commonSkuFieldsWithAlias = "s.STORERKEY, s.SKU, s.DESCR SKUDESCR, s.COMMODITYCLASS STORAGECONDITIONS, s.BUSR8 ";
    public static final String commonSkuFields = "s.STORERKEY, s.SKU, s.DESCR , s.COMMODITYCLASS , s.BUSR8 ";

//    public static final String CommonLottableFields = "elot.LOTTABLE01,"  +   "elot.ELOTTABLE02," +
//            "elot.ELOTTABLE03," +   "elot.LOTTABLE04," +
//            "elot.ELOTTABLE05," +   "elot.LOTTABLE06," +
//            "elot.ELOTTABLE07," +   "elot.ELOTTABLE08," +
//            "elot.ELOTTABLE09," +   "elot.LOTTABLE10," +
//            "FORMAT(elot.ELOTTABLE11, 'yyyyMMdd') ELOTTABLE11," +
//            "FORMAT(elot.ELOTTABLE12, 'yyyyMMdd') ELOTTABLE12" ;


    public static final String CommonLottableFields =
            "elot.ELOTTABLE01," +   "elot.ELOTTABLE02," +
            "elot.ELOTTABLE03," +
            "FORMAT(elot.ELOTTABLE04, '"+DateTimeFormat+"') ELOTTABLE04," +
            "FORMAT(elot.ELOTTABLE05, '"+DateTimeFormat+"') ELOTTABLE05," +
            "elot.ELOTTABLE06," +
            "elot.ELOTTABLE07," +   "elot.ELOTTABLE08," +
            "elot.ELOTTABLE09," +   "elot.ELOTTABLE10," +
            "FORMAT(elot.ELOTTABLE11, '"+DateTimeFormat+"') ELOTTABLE11," +
            "FORMAT(elot.ELOTTABLE12, '"+DateTimeFormat+"') ELOTTABLE12," +
            "elot.LOTTABLE01," +   "elot.LOTTABLE02," +
            "elot.LOTTABLE03," +
            "FORMAT(elot.LOTTABLE04, '"+DateTimeFormat+"') LOTTABLE04," +
            "FORMAT(elot.LOTTABLE05, '"+DateTimeFormat+"') LOTTABLE05," +
            "elot.LOTTABLE06," +
            "elot.LOTTABLE07," +   "elot.LOTTABLE08," +
            "elot.LOTTABLE09," +   "elot.LOTTABLE10," +
            "FORMAT(elot.LOTTABLE11, '"+DateTimeFormat+"') LOTTABLE11," +
            "FORMAT(elot.LOTTABLE12, '"+DateTimeFormat+"') LOTTABLE12" ;

    public static final String receiptDetailLottableFields = "A.LOTTABLE01,"  +   "A.ELOTTABLE02," +
            "A.ELOTTABLE03," +   "A.LOTTABLE04," +
            "A.ELOTTABLE05," +   "A.LOTTABLE06," +
            "A.ELOTTABLE07," +   "A.ELOTTABLE08," +
            "A.ELOTTABLE09," +   "A.LOTTABLE10," +
            "FORMAT(A.ELOTTABLE11, 'yyyyMMdd') ELOTTABLE11," +
            "FORMAT(A.ELOTTABLE12, 'yyyyMMdd') ELOTTABLE12" ;


}
