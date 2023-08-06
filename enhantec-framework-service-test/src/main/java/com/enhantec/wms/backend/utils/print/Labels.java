package com.enhantec.wms.backend.utils.print;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.DBHelper;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class Labels {

    public final static String LPN_UI = "lpn_ui"; //创建ASN 单行时会打印的标签
    public final static String LPN = "LPN"; //不是整桶拣货或者lpn发生了变化时打印的标签
    public final static String LPN_REPACK = "LPN_REPACK"; //分装入库时打印的标签
    public final static String LPN_SAMPLE = "LPN_SAMPLE"; //取样剩余量标签
    public final static String SAMPLE_LY = "SAMPLE_LY"; //留样标签
    public final static String SAMPLE_YP = "SAMPLE_YP"; //样品标签
    public final static String LPN_TK = "LPN_TK";
    public final static String LPN_UI_SY = "LPN_UI_SY";//拣货剩余量标签
    public final static String SN_UI = "SN_UI";
    public final static String SN_UI_SY = "SN_UI_SY";
    public final static String SN_UI_CD = "SN_UI_CD";




    public static List<Map<String, String>> getLpnPrintDefaultData( String receiptKey, String receiptLinenumber) {
        ArrayList<Object> params = new ArrayList<>();
        String theSQLStmt= "";

        String LPN ="select SKU.SKU,"+  //物料代码
                "sku.DESCR,"+ //物料名称
                "RD.LOTTABLE06,"+ //批号
                "RD.ELOTTABLE07,"+ //规格型号
                "Convert(varchar(100),RD.LOTTABLE04,3) DATETIME,"+ //入库日期
                "STORER.DESCRIPTION,"+ //供应商
                "RD.ELOTTABLE09 AS SUPPLIERLOT,"+ //供应商批次
                "RD.TOID AS ID,"+ //容器号
                "SKU.COMMODITYCLASS AS STORAGECONDITIONS,"+ //存储条件
                "concat(CONVERT(FLOAT,RD.QTYEXPECTED),RD.UOM ) as NETWGT,"+ //数量
                "concat(CONVERT(FLOAT,RD.GROSSWGTEXPECTED),RD.UOM) as GROSSWGT,"+ //毛重
                "concat(CONVERT(FLOAT,RD.TAREWGTEXPECTED),RD.UOM) as GROSSWGT,"+ //皮重
                "(BARRELNUMBER+'/'+TOTALBARRELNUMBER) as BARREL_MSG ,"+ //桶号
                "RD.ELOTTABLE05,"+ //复验期
                "RD.ELOTTABLE11,"+ //有效期
                "RD.ELOTTABLE12,"+ //生产日期
                "RD.ADDWHO,"+ //添加人
                "Convert(varchar(100),RD.ADDDATE,3) as ADDDATE"; //添加日期

        theSQLStmt = LPN;
        //theSQLStmt.concat(" from Receipt r, storer s, receiptdetail rd where rd.storerkey = s.storerkey and r.receiptkey=rd.receiptkey ");
        theSQLStmt.concat(" from RECEIPT R "+
                "LEFT JOIN RECEIPTDETAIL RD ON R.RECEIPTKEY=RD.RECEIPTKEY "+
                "LEFT JOIN SKU ON RD.SKU=SKU.SKU AND RD.STORERKEY=SKU.STORERKEY "+
                "LEFT JOIN STORER ON RD.ELOTTABLE08=STORER.STORERKEY AND STORER.TYPE='5'");
        theSQLStmt.concat("where r.ReceiptKey = ? ");
        //theSQLStmt.concat("AND s.Type = '1' ");
        params.add(receiptKey);
        //theDO.setConstraintItem("receiptkey"), receiptkey));
        if (receiptLinenumber != null && !receiptLinenumber.equals("")) {
            theSQLStmt.concat("  AND rd.receiptlinenumber = ? ");
            params.add(receiptLinenumber);
        }
        theSQLStmt.concat(" ORDER BY rd.BARRELNUMBER ");
        List<Map<String,String>> data = DBHelper.executeQuery( theSQLStmt, params);
        return data;
    }
}
