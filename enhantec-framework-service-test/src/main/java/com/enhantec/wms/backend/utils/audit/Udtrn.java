package com.enhantec.wms.backend.utils.audit;

import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.utils.common.LegecyUtilHelper;

import java.sql.Connection;
import java.util.ArrayList;

/**
 * Created by XVTING on 2020-03-14.
 */

public class Udtrn {
    /*
    UDTRN	用户自定义事务表
 Table表	 Field字段	TYPE	必填	Description描述
UDTRN	SERIALKEY	NUMBER	Y	序列号
UDTRN	EsignatureKey	NUMBER	N	签名KEY
UDTRN	WHSEID	NVARCHAR2 (30)	Y	仓库
UDTRN	FROMTYPE	NVARCHAR2 (125)	Y	来源类型
UDTRN	FROMTABLENAME	NVARCHAR2 (255)	Y	来源表
UDTRN	FROMKEY	NVARCHAR2 (30)	N	关键字
UDTRN	FROMKEY1	NVARCHAR2 (255)	Y	关键字1
UDTRN	FROMKEY2	NVARCHAR2 (255)	Y	关键字2
UDTRN	FROMKEY3	NVARCHAR2 (255)	Y	关键字3
UDTRN	TITLE1	NVARCHAR2 (255)	N	标题1
UDTRN	CONTENT1	NVARCHAR2 (30)	N	内容1
UDTRN	TITLE2	NVARCHAR2 (255)	N	标题2
UDTRN	CONTENT2	NVARCHAR2 (30)	N	内容2
UDTRN	TITLE3	NVARCHAR2 (255)	N	标题3
UDTRN	CONTENT3	NVARCHAR2 (30)	N	内容3
UDTRN	TITLE4	NVARCHAR2 (255)	N	标题4
UDTRN	CONTENT4	NVARCHAR2 (30)	N	内容4
UDTRN	TITLE5	NVARCHAR2 (255)	N	标题5
UDTRN	CONTENT5	NVARCHAR2 (30)	N	内容5
UDTRN	TITLE6	NVARCHAR2 (255)	N	标题6
UDTRN	CONTENT6	NVARCHAR2 (30)	N	内容6
UDTRN	TITLE7	NVARCHAR2 (255)	N	标题7
UDTRN	CONTENT7	NVARCHAR2 (30)	N	内容7
UDTRN	TITLE8	NVARCHAR2 (255)	N	标题8
UDTRN	CONTENT8	NVARCHAR2 (30)	N	内容8
UDTRN	TITLE9	NVARCHAR2 (255)	N	标题9
UDTRN	CONTENT9	NVARCHAR2 (30)	N	内容9
UDTRN	TITLE10	NVARCHAR2 (255)	N	标题10
UDTRN	CONTENT10	NVARCHAR2 (30)	N	内容10
UDTRN	TITLE11	NVARCHAR2 (255)	N	标题11
UDTRN	CONTENT11	NVARCHAR2 (30)	N	内容11
UDTRN	TITLE12	NVARCHAR2 (255)	N	标题12
UDTRN	CONTENT12	NVARCHAR2 (30)	N	内容12
UDTRN	TITLE13	NVARCHAR2 (255)	N	标题13
UDTRN	CONTENT13	NVARCHAR2 (30)	N	内容13
UDTRN	TITLE14	NVARCHAR2 (255)	N	标题14
UDTRN	CONTENT14	NVARCHAR2 (30)	N	内容14
UDTRN	TITLE15	NVARCHAR2 (255)	N	标题15
UDTRN	CONTENT15	NVARCHAR2 (30)	N	内容15
UDTRN	TITLE16	NVARCHAR2 (255)	N	标题16
UDTRN	CONTENT16	NVARCHAR2 (30)	N	内容16
UDTRN	TITLE17	NVARCHAR2 (255)	N	标题17
UDTRN	CONTENT17	NVARCHAR2 (30)	N	内容17
UDTRN	TITLE18	NVARCHAR2 (255)	N	标题18
UDTRN	CONTENT18	NVARCHAR2 (30)	N	内容18
UDTRN	TITLE19	NVARCHAR2 (255)	N	标题19
UDTRN	CONTENT19	NVARCHAR2 (30)	N	内容19
UDTRN	TITLE20	NVARCHAR2 (255)	N	标题20
UDTRN	CONTENT20	NVARCHAR2 (30)	N	内容20
UDTRN	TITLE21	NVARCHAR2 (255)	N	标题21
UDTRN	CONTENT21	NVARCHAR2 (30)	N	内容21
UDTRN	TITLE22	NVARCHAR2 (255)	N	标题22
UDTRN	CONTENT22	NVARCHAR2 (30)	N	内容22
UDTRN	TITLE23	NVARCHAR2 (255)	N	标题23
UDTRN	CONTENT23	NVARCHAR2 (30)	N	内容23
UDTRN	TITLE24	NVARCHAR2 (255)	N	标题24
UDTRN	CONTENT24	NVARCHAR2 (30)	N	内容24
UDTRN	TITLE25	NVARCHAR2 (255)	N	标题25
UDTRN	CONTENT25	NVARCHAR2 (30)	N	内容25
UDTRN	TITLE26	NVARCHAR2 (255)	N	标题26
UDTRN	CONTENT26	NVARCHAR2 (30)	N	内容26
UDTRN	TITLE27	NVARCHAR2 (255)	N	标题27
UDTRN	CONTENT27	NVARCHAR2 (30)	N	内容27
UDTRN	TITLE28	NVARCHAR2 (255)	N	标题28
UDTRN	CONTENT28	NVARCHAR2 (30)	N	内容28
UDTRN	TITLE29	NVARCHAR2 (255)	N	标题29
UDTRN	CONTENT29	NVARCHAR2 (30)	N	内容29
UDTRN	TITLE30	NVARCHAR2 (255)	N	标题30
UDTRN	CONTENT30	NVARCHAR2 (30)	N	内容30
UDTRN	ADDDATE	DATE	Y	创建时间
UDTRN	ADDWHO	NVARCHAR2 (30)	Y	创建人
UDTRN	EDITDATE	DATE	Y	编辑时间
UDTRN	EDITWHO	NVARCHAR2 (30)	Y	编辑人
     */
    public boolean isChinese = true;
    public String EsignatureKey = "";
    public String EsignatureKey1 = "";
    public String FROMTYPE = "";
    public String FROMTABLENAME = "";
    public String FROMKEY = "";
    public String FROMKEY1 = "";
    public String FROMKEY2 = "";
    public String FROMKEY3 = "";
    public String FROMKEY1LABEL = "";
    public String FROMKEY2LABEL = "";
    public String FROMKEY3LABEL = "";
    public String TITLE01 = "";
    public String CONTENT01 = "";
    public String TITLE02 = "";
    public String CONTENT02 = "";
    public String TITLE03 = "";
    public String CONTENT03 = "";
    public String TITLE04 = "";
    public String CONTENT04 = "";
    public String TITLE05 = "";
    public String CONTENT05 = "";
    public String TITLE06 = "";
    public String CONTENT06 = "";
    public String TITLE07 = "";
    public String CONTENT07 = "";
    public String TITLE08 = "";
    public String CONTENT08 = "";
    public String TITLE09 = "";
    public String CONTENT09 = "";
    public String TITLE10 = "";
    public String CONTENT10 = "";
    public String TITLE11 = "";
    public String CONTENT11 = "";
    public String TITLE12 = "";
    public String CONTENT12 = "";
    public String TITLE13 = "";
    public String CONTENT13 = "";
    public String TITLE14 = "";
    public String CONTENT14 = "";
    public String TITLE15 = "";
    public String CONTENT15 = "";
    public String TITLE16 = "";
    public String CONTENT16 = "";
    public String TITLE17 = "";
    public String CONTENT17 = "";
    public String TITLE18 = "";
    public String CONTENT18 = "";
    public String TITLE19 = "";
    public String CONTENT19 = "";
    public String TITLE20 = "";
    public String CONTENT20 = "";
    public String TITLE21 = "";
    public String CONTENT21 = "";
    public String TITLE22 = "";
    public String CONTENT22 = "";
    public String TITLE23 = "";
    public String CONTENT23 = "";
    public String TITLE24 = "";
    public String CONTENT24 = "";
    public String TITLE25 = "";
    public String CONTENT25 = "";
    public String TITLE26 = "";
    public String CONTENT26 = "";
    public String TITLE27 = "";
    public String CONTENT27 = "";
    public String TITLE28 = "";
    public String CONTENT28 = "";
    public String TITLE29 = "";
    public String CONTENT29 = "";
    public String TITLE30 = "";
    public String CONTENT30 = "";


    public String toInsertSql(String User) throws Exception {
        //String SQL="";
        //FROMTYPE,FROMTABLENAME,FROMKEY,FROMKEY1,FROMKEY2,FROMKEY3,TITLE1,CONTENT1,TITLE2,CONTENT2,TITLE3,CONTENT3,TITLE4,CONTENT4,TITLE5,CONTENT5,TITLE6,CONTENT6,TITLE7,CONTENT7,TITLE8,CONTENT8,TITLE9,CONTENT9,TITLE10,CONTENT10,TITLE11,CONTENT11,TITLE12,CONTENT12,TITLE13,CONTENT13,TITLE14,CONTENT14,TITLE15,CONTENT15,TITLE16,CONTENT16,TITLE17,CONTENT17,TITLE18,CONTENT18,TITLE19,CONTENT19,TITLE20,CONTENT20,TITLE21,CONTENT21,TITLE22,CONTENT22,TITLE23,CONTENT23,TITLE24,CONTENT24,TITLE25,CONTENT25,TITLE26,CONTENT26,TITLE27,CONTENT27,TITLE28,CONTENT28,TITLE29,CONTENT29,TITLE30,CONTENT30,ADDDATE,ADDWHO,EDITDATE,EDITWHO
        if (LegecyUtilHelper.isNull(FROMTYPE)) throw new Exception("{事务表.来源类型}不能为空");
        if (LegecyUtilHelper.isNull(FROMTABLENAME)) throw new Exception("{事务表.来源表}不能为空");
        if (LegecyUtilHelper.isNull(FROMKEY)) throw new Exception("{事务表.关键字}不能为空");
        String SQL1 = "insert into UDTRN(addwho,editwho";
        String SQL2 = "values('" + User + "','" + User + "'";
        if (!LegecyUtilHelper.isNull(EsignatureKey)) {
            SQL1 += ",EsignatureKey";
            SQL2 += "," + EsignatureKey + "";
        }
        if (!LegecyUtilHelper.isNull(EsignatureKey1)) {
            SQL1 += ",EsignatureKey1";
            SQL2 += "," + EsignatureKey1 + "";
        }
        if (!LegecyUtilHelper.isNull(FROMTYPE)) {
            SQL1 += ",FROMTYPE";
            SQL2 += ",'" + FROMTYPE + "'";
        }
        if (!LegecyUtilHelper.isNull(FROMTABLENAME)) {
            SQL1 += ",FROMTABLENAME";
            SQL2 += ",'" + FROMTABLENAME + "'";
        }
        if (!LegecyUtilHelper.isNull(FROMKEY)) {
            SQL1 += ",FROMKEY";
            SQL2 += ",'" + FROMKEY + "'";
        }
        if (!LegecyUtilHelper.isNull(FROMKEY1)) {
            SQL1 += ",FROMKEY1";
            SQL2 += ",'" + FROMKEY1 + "'";
        }
        if (!LegecyUtilHelper.isNull(FROMKEY1LABEL)) {
            SQL1 += ",FROMKEY1LABEL";
            SQL2 += ",'" + FROMKEY1LABEL + "'";
        }
        if (!LegecyUtilHelper.isNull(FROMKEY2)) {
            SQL1 += ",FROMKEY2";
            SQL2 += ",'" + FROMKEY2 + "'";
        }
        if (!LegecyUtilHelper.isNull(FROMKEY2LABEL)) {
            SQL1 += ",FROMKEY2LABEL";
            SQL2 += ",'" + FROMKEY2LABEL + "'";
        }
        if (!LegecyUtilHelper.isNull(FROMKEY3)) {
            SQL1 += ",FROMKEY3";
            SQL2 += ",'" + FROMKEY3 + "'";
        }
        if (!LegecyUtilHelper.isNull(FROMKEY3LABEL)) {
            SQL1 += ",FROMKEY3LABEL";
            SQL2 += ",'" + FROMKEY3LABEL + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE01)) {
            SQL1 += ",TITLE01";
            SQL2 += ",'" + TITLE01 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT01)) {
            SQL1 += ",CONTENT01";
            SQL2 += ",'" + CONTENT01 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE02)) {
            SQL1 += ",TITLE02";
            SQL2 += ",'" + TITLE02 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT02)) {
            SQL1 += ",CONTENT02";
            SQL2 += ",'" + CONTENT02 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE03)) {
            SQL1 += ",TITLE03";
            SQL2 += ",'" + TITLE03 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT03)) {
            SQL1 += ",CONTENT03";
            SQL2 += ",'" + CONTENT03 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE04)) {
            SQL1 += ",TITLE04";
            SQL2 += ",'" + TITLE04 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT04)) {
            SQL1 += ",CONTENT04";
            SQL2 += ",'" + CONTENT04 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE05)) {
            SQL1 += ",TITLE05";
            SQL2 += ",'" + TITLE05 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT05)) {
            SQL1 += ",CONTENT05";
            SQL2 += ",'" + CONTENT05 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE06)) {
            SQL1 += ",TITLE06";
            SQL2 += ",'" + TITLE06 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT06)) {
            SQL1 += ",CONTENT06";
            SQL2 += ",'" + CONTENT06 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE07)) {
            SQL1 += ",TITLE07";
            SQL2 += ",'" + TITLE07 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT07)) {
            SQL1 += ",CONTENT07";
            SQL2 += ",'" + CONTENT07 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE08)) {
            SQL1 += ",TITLE08";
            SQL2 += ",'" + TITLE08 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT08)) {
            SQL1 += ",CONTENT08";
            SQL2 += ",'" + CONTENT08 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE09)) {
            SQL1 += ",TITLE09";
            SQL2 += ",'" + TITLE09 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT09)) {
            SQL1 += ",CONTENT09";
            SQL2 += ",'" + CONTENT09 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE10)) {
            SQL1 += ",TITLE10";
            SQL2 += ",'" + TITLE10 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT10)) {
            SQL1 += ",CONTENT10";
            SQL2 += ",'" + CONTENT10 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE11)) {
            SQL1 += ",TITLE11";
            SQL2 += ",'" + TITLE11 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT11)) {
            SQL1 += ",CONTENT11";
            SQL2 += ",'" + CONTENT11 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE12)) {
            SQL1 += ",TITLE12";
            SQL2 += ",'" + TITLE12 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT12)) {
            SQL1 += ",CONTENT12";
            SQL2 += ",'" + CONTENT12 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE13)) {
            SQL1 += ",TITLE13";
            SQL2 += ",'" + TITLE13 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT13)) {
            SQL1 += ",CONTENT13";
            SQL2 += ",'" + CONTENT13 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE14)) {
            SQL1 += ",TITLE14";
            SQL2 += ",'" + TITLE14 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT14)) {
            SQL1 += ",CONTENT14";
            SQL2 += ",'" + CONTENT14 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE15)) {
            SQL1 += ",TITLE15";
            SQL2 += ",'" + TITLE15 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT15)) {
            SQL1 += ",CONTENT15";
            SQL2 += ",'" + CONTENT15 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE16)) {
            SQL1 += ",TITLE16";
            SQL2 += ",'" + TITLE16 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT16)) {
            SQL1 += ",CONTENT16";
            SQL2 += ",'" + CONTENT16 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE17)) {
            SQL1 += ",TITLE17";
            SQL2 += ",'" + TITLE17 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT17)) {
            SQL1 += ",CONTENT17";
            SQL2 += ",'" + CONTENT17 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE18)) {
            SQL1 += ",TITLE18";
            SQL2 += ",'" + TITLE18 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT18)) {
            SQL1 += ",CONTENT18";
            SQL2 += ",'" + CONTENT18 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE19)) {
            SQL1 += ",TITLE19";
            SQL2 += ",'" + TITLE19 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT19)) {
            SQL1 += ",CONTENT19";
            SQL2 += ",'" + CONTENT19 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE20)) {
            SQL1 += ",TITLE20";
            SQL2 += ",'" + TITLE20 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT20)) {
            SQL1 += ",CONTENT20";
            SQL2 += ",'" + CONTENT20 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE21)) {
            SQL1 += ",TITLE21";
            SQL2 += ",'" + TITLE21 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT21)) {
            SQL1 += ",CONTENT21";
            SQL2 += ",'" + CONTENT21 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE22)) {
            SQL1 += ",TITLE22";
            SQL2 += ",'" + TITLE22 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT22)) {
            SQL1 += ",CONTENT22";
            SQL2 += ",'" + CONTENT22 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE23)) {
            SQL1 += ",TITLE23";
            SQL2 += ",'" + TITLE23 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT23)) {
            SQL1 += ",CONTENT23";
            SQL2 += ",'" + CONTENT23 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE24)) {
            SQL1 += ",TITLE24";
            SQL2 += ",'" + TITLE24 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT24)) {
            SQL1 += ",CONTENT24";
            SQL2 += ",'" + CONTENT24 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE25)) {
            SQL1 += ",TITLE25";
            SQL2 += ",'" + TITLE25 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT25)) {
            SQL1 += ",CONTENT25";
            SQL2 += ",'" + CONTENT25 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE26)) {
            SQL1 += ",TITLE26";
            SQL2 += ",'" + TITLE26 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT26)) {
            SQL1 += ",CONTENT26";
            SQL2 += ",'" + CONTENT26 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE27)) {
            SQL1 += ",TITLE27";
            SQL2 += ",'" + TITLE27 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT27)) {
            SQL1 += ",CONTENT27";
            SQL2 += ",'" + CONTENT27 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE28)) {
            SQL1 += ",TITLE28";
            SQL2 += ",'" + TITLE28 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT28)) {
            SQL1 += ",CONTENT28";
            SQL2 += ",'" + CONTENT28 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE29)) {
            SQL1 += ",TITLE29";
            SQL2 += ",'" + TITLE29 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT29)) {
            SQL1 += ",CONTENT29";
            SQL2 += ",'" + CONTENT29 + "'";
        }
        if (!LegecyUtilHelper.isNull(TITLE30)) {
            SQL1 += ",TITLE30";
            SQL2 += ",'" + TITLE30 + "'";
        }
        if (!LegecyUtilHelper.isNull(CONTENT30)) {
            SQL1 += ",CONTENT30";
            SQL2 += ",'" + CONTENT30 + "'";
        }
        return SQL1 + ") " + SQL2 + ");";
    }

    public String Insert(Context context, Connection conn, String User) throws Exception {
        String SERIALKEY = Integer.toString(LegacyDBHelper.GetSeq(context, conn, "seq_UDTRN"));
        return Insert(context, conn, SERIALKEY, User);


    }


    public String Insert(Context context, Connection conn, String SERIALKEY, String User) throws Exception {


        if (LegecyUtilHelper.isNull(FROMTYPE)) throw new Exception("{事务表.来源类型}不能为空");
        if (LegecyUtilHelper.isNull(FROMTABLENAME)) throw new Exception("{事务表.来源表}不能为空");
        //if (XtUtils.isNull(FROMKEY)) throw new Exception("{事务表.关键字}不能为空");
        ArrayList<String> Params = new ArrayList<String>();
        String SQL1 = "insert into UDTRN(ADDWHO,EDITWHO";
        String SQL2 = "values( ? ,? ";
        Params.add(User);
        Params.add(User);
        if (!LegecyUtilHelper.isNull(EsignatureKey)) {
            SQL1 += ",EsignatureKey";
            SQL2 += ",? ";
            Params.add(EsignatureKey);
        }
        if (!LegecyUtilHelper.isNull(EsignatureKey1)) {
            SQL1 += ",EsignatureKey1";
            SQL2 += "," + EsignatureKey1 + "";
        }
        if (!LegecyUtilHelper.isNull(FROMTYPE)) {
            SQL1 += ",FROMTYPE";
            SQL2 += ",? ";
            Params.add(FROMTYPE);
        }
        if (!LegecyUtilHelper.isNull(FROMTABLENAME)) {
            SQL1 += ",FROMTABLENAME";
            SQL2 += ",? ";
            Params.add(FROMTABLENAME);
        }
        if (!LegecyUtilHelper.isNull(FROMKEY)) {
            SQL1 += ",FROMKEY";
            SQL2 += ",? ";
            Params.add(FROMKEY);
        }
        if (!LegecyUtilHelper.isNull(FROMKEY1)) {
            SQL1 += ",FROMKEY1";
            SQL2 += ",? ";
            Params.add(FROMKEY1);
        }
        if (!LegecyUtilHelper.isNull(FROMKEY1LABEL)) {
            SQL1 += ",FROMKEY1LABEL";
            SQL2 += ",? ";
            Params.add(FROMKEY1LABEL);
        }
        if (!LegecyUtilHelper.isNull(FROMKEY2)) {
            SQL1 += ",FROMKEY2";
            SQL2 += ",? ";
            Params.add(FROMKEY2);
        }
        if (!LegecyUtilHelper.isNull(FROMKEY2LABEL)) {
            SQL1 += ",FROMKEY2LABEL";
            SQL2 += ",? ";
            Params.add(FROMKEY2LABEL);
        }
        if (!LegecyUtilHelper.isNull(FROMKEY3)) {
            SQL1 += ",FROMKEY3";
            SQL2 += ",? ";
            Params.add(FROMKEY3);
        }
        if (!LegecyUtilHelper.isNull(FROMKEY3LABEL)) {
            SQL1 += ",FROMKEY3LABEL";
            SQL2 += ",? ";
            Params.add(FROMKEY3LABEL);
        }
        if (!LegecyUtilHelper.isNull(TITLE01)) {
            SQL1 += ",TITLE01";
            SQL2 += ",? ";
            Params.add(TITLE01);
        }
        if (!LegecyUtilHelper.isNull(CONTENT01)) {
            SQL1 += ",CONTENT01";
            SQL2 += ",? ";
            Params.add(CONTENT01);
        }
        if (!LegecyUtilHelper.isNull(TITLE02)) {
            SQL1 += ",TITLE02";
            SQL2 += ",? ";
            Params.add(TITLE02);
        }
        if (!LegecyUtilHelper.isNull(CONTENT02)) {
            SQL1 += ",CONTENT02";
            SQL2 += ",? ";
            Params.add(CONTENT02);
        }
        if (!LegecyUtilHelper.isNull(TITLE03)) {
            SQL1 += ",TITLE03";
            SQL2 += ",? ";
            Params.add(TITLE03);
        }
        if (!LegecyUtilHelper.isNull(CONTENT03)) {
            SQL1 += ",CONTENT03";
            SQL2 += ",? ";
            Params.add(CONTENT03);
        }
        if (!LegecyUtilHelper.isNull(TITLE04)) {
            SQL1 += ",TITLE04";
            SQL2 += ",? ";
            Params.add(TITLE04);
        }
        if (!LegecyUtilHelper.isNull(CONTENT04)) {
            SQL1 += ",CONTENT04";
            SQL2 += ",? ";
            Params.add(CONTENT04);
        }
        if (!LegecyUtilHelper.isNull(TITLE05)) {
            SQL1 += ",TITLE05";
            SQL2 += ",? ";
            Params.add(TITLE05);
        }
        if (!LegecyUtilHelper.isNull(CONTENT05)) {
            SQL1 += ",CONTENT05";
            SQL2 += ",? ";
            Params.add(CONTENT05);
        }
        if (!LegecyUtilHelper.isNull(TITLE06)) {
            SQL1 += ",TITLE06";
            SQL2 += ",? ";
            Params.add(TITLE06);
        }
        if (!LegecyUtilHelper.isNull(CONTENT06)) {
            SQL1 += ",CONTENT06";
            SQL2 += ",? ";
            Params.add(CONTENT06);
        }
        if (!LegecyUtilHelper.isNull(TITLE07)) {
            SQL1 += ",TITLE07";
            SQL2 += ",? ";
            Params.add(TITLE07);
        }
        if (!LegecyUtilHelper.isNull(CONTENT07)) {
            SQL1 += ",CONTENT07";
            SQL2 += ",? ";
            Params.add(CONTENT07);
        }
        if (!LegecyUtilHelper.isNull(TITLE08)) {
            SQL1 += ",TITLE08";
            SQL2 += ",? ";
            Params.add(TITLE08);
        }
        if (!LegecyUtilHelper.isNull(CONTENT08)) {
            SQL1 += ",CONTENT08";
            SQL2 += ",? ";
            Params.add(CONTENT08);
        }
        if (!LegecyUtilHelper.isNull(TITLE09)) {
            SQL1 += ",TITLE09";
            SQL2 += ",? ";
            Params.add(TITLE09);
        }
        if (!LegecyUtilHelper.isNull(CONTENT09)) {
            SQL1 += ",CONTENT09";
            SQL2 += ",? ";
            Params.add(CONTENT09);
        }
        if (!LegecyUtilHelper.isNull(TITLE10)) {
            SQL1 += ",TITLE10";
            SQL2 += ",? ";
            Params.add(TITLE10);
        }
        if (!LegecyUtilHelper.isNull(CONTENT10)) {
            SQL1 += ",CONTENT10";
            SQL2 += ",? ";
            Params.add(CONTENT10);
        }
        if (!LegecyUtilHelper.isNull(TITLE11)) {
            SQL1 += ",TITLE11";
            SQL2 += ",? ";
            Params.add(TITLE11);
        }
        if (!LegecyUtilHelper.isNull(CONTENT11)) {
            SQL1 += ",CONTENT11";
            SQL2 += ",? ";
            Params.add(CONTENT11);
        }
        if (!LegecyUtilHelper.isNull(TITLE12)) {
            SQL1 += ",TITLE12";
            SQL2 += ",? ";
            Params.add(TITLE12);
        }
        if (!LegecyUtilHelper.isNull(CONTENT12)) {
            SQL1 += ",CONTENT12";
            SQL2 += ",? ";
            Params.add(CONTENT12);
        }
        if (!LegecyUtilHelper.isNull(TITLE13)) {
            SQL1 += ",TITLE13";
            SQL2 += ",? ";
            Params.add(TITLE13);
        }
        if (!LegecyUtilHelper.isNull(CONTENT13)) {
            SQL1 += ",CONTENT13";
            SQL2 += ",? ";
            Params.add(CONTENT13);
        }
        if (!LegecyUtilHelper.isNull(TITLE14)) {
            SQL1 += ",TITLE14";
            SQL2 += ",? ";
            Params.add(TITLE14);
        }
        if (!LegecyUtilHelper.isNull(CONTENT14)) {
            SQL1 += ",CONTENT14";
            SQL2 += ",? ";
            Params.add(CONTENT14);
        }
        if (!LegecyUtilHelper.isNull(TITLE15)) {
            SQL1 += ",TITLE15";
            SQL2 += ",? ";
            Params.add(TITLE15);
        }
        if (!LegecyUtilHelper.isNull(CONTENT15)) {
            SQL1 += ",CONTENT15";
            SQL2 += ",? ";
            Params.add(CONTENT15);
        }
        if (!LegecyUtilHelper.isNull(TITLE16)) {
            SQL1 += ",TITLE16";
            SQL2 += ",? ";
            Params.add(TITLE16);
        }
        if (!LegecyUtilHelper.isNull(CONTENT16)) {
            SQL1 += ",CONTENT16";
            SQL2 += ",? ";
            Params.add(CONTENT16);
        }
        if (!LegecyUtilHelper.isNull(TITLE17)) {
            SQL1 += ",TITLE17";
            SQL2 += ",? ";
            Params.add(TITLE17);
        }
        if (!LegecyUtilHelper.isNull(CONTENT17)) {
            SQL1 += ",CONTENT17";
            SQL2 += ",? ";
            Params.add(CONTENT17);
        }
        if (!LegecyUtilHelper.isNull(TITLE18)) {
            SQL1 += ",TITLE18";
            SQL2 += ",? ";
            Params.add(TITLE18);
        }
        if (!LegecyUtilHelper.isNull(CONTENT18)) {
            SQL1 += ",CONTENT18";
            SQL2 += ",? ";
            Params.add(CONTENT18);
        }
        if (!LegecyUtilHelper.isNull(TITLE19)) {
            SQL1 += ",TITLE19";
            SQL2 += ",? ";
            Params.add(TITLE19);
        }
        if (!LegecyUtilHelper.isNull(CONTENT19)) {
            SQL1 += ",CONTENT19";
            SQL2 += ",? ";
            Params.add(CONTENT19);
        }
        if (!LegecyUtilHelper.isNull(TITLE20)) {
            SQL1 += ",TITLE20";
            SQL2 += ",? ";
            Params.add(TITLE20);
        }
        if (!LegecyUtilHelper.isNull(CONTENT20)) {
            SQL1 += ",CONTENT20";
            SQL2 += ",? ";
            Params.add(CONTENT20);
        }
        if (!LegecyUtilHelper.isNull(TITLE21)) {
            SQL1 += ",TITLE21";
            SQL2 += ",? ";
            Params.add(TITLE21);
        }
        if (!LegecyUtilHelper.isNull(CONTENT21)) {
            SQL1 += ",CONTENT21";
            SQL2 += ",? ";
            Params.add(CONTENT21);
        }
        if (!LegecyUtilHelper.isNull(TITLE22)) {
            SQL1 += ",TITLE22";
            SQL2 += ",? ";
            Params.add(TITLE22);
        }
        if (!LegecyUtilHelper.isNull(CONTENT22)) {
            SQL1 += ",CONTENT22";
            SQL2 += ",? ";
            Params.add(CONTENT22);
        }
        if (!LegecyUtilHelper.isNull(TITLE23)) {
            SQL1 += ",TITLE23";
            SQL2 += ",? ";
            Params.add(TITLE23);
        }
        if (!LegecyUtilHelper.isNull(CONTENT23)) {
            SQL1 += ",CONTENT23";
            SQL2 += ",? ";
            Params.add(CONTENT23);
        }
        if (!LegecyUtilHelper.isNull(TITLE24)) {
            SQL1 += ",TITLE24";
            SQL2 += ",? ";
            Params.add(TITLE24);
        }
        if (!LegecyUtilHelper.isNull(CONTENT24)) {
            SQL1 += ",CONTENT24";
            SQL2 += ",? ";
            Params.add(CONTENT24);
        }
        if (!LegecyUtilHelper.isNull(TITLE25)) {
            SQL1 += ",TITLE25";
            SQL2 += ",? ";
            Params.add(TITLE25);
        }
        if (!LegecyUtilHelper.isNull(CONTENT25)) {
            SQL1 += ",CONTENT25";
            SQL2 += ",? ";
            Params.add(CONTENT25);
        }
        if (!LegecyUtilHelper.isNull(TITLE26)) {
            SQL1 += ",TITLE26";
            SQL2 += ",? ";
            Params.add(TITLE26);
        }
        if (!LegecyUtilHelper.isNull(CONTENT26)) {
            SQL1 += ",CONTENT26";
            SQL2 += ",? ";
            Params.add(CONTENT26);
        }
        if (!LegecyUtilHelper.isNull(TITLE27)) {
            SQL1 += ",TITLE27";
            SQL2 += ",? ";
            Params.add(TITLE27);
        }
        if (!LegecyUtilHelper.isNull(CONTENT27)) {
            SQL1 += ",CONTENT27";
            SQL2 += ",? ";
            Params.add(CONTENT27);
        }
        if (!LegecyUtilHelper.isNull(TITLE28)) {
            SQL1 += ",TITLE28";
            SQL2 += ",? ";
            Params.add(TITLE28);
        }
        if (!LegecyUtilHelper.isNull(CONTENT28)) {
            SQL1 += ",CONTENT28";
            SQL2 += ",? ";
            Params.add(CONTENT28);
        }
        if (!LegecyUtilHelper.isNull(TITLE29)) {
            SQL1 += ",TITLE29";
            SQL2 += ",? ";
            Params.add(TITLE29);
        }
        if (!LegecyUtilHelper.isNull(CONTENT29)) {
            SQL1 += ",CONTENT29";
            SQL2 += ",? ";
            Params.add(CONTENT29);
        }
        if (!LegecyUtilHelper.isNull(TITLE30)) {
            SQL1 += ",TITLE30";
            SQL2 += ",? ";
            Params.add(TITLE30);
        }
        if (!LegecyUtilHelper.isNull(CONTENT30)) {
            SQL1 += ",CONTENT30";
            SQL2 += ",? ";
            Params.add(CONTENT30);
        }
        LegacyDBHelper.ExecSql(context, conn, SQL1 + ") " + SQL2 + "); ", Params);

        return SERIALKEY;

    }


}
