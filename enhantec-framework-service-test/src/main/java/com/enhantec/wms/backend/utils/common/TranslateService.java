//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.enhantec.wms.backend.utils.common;

import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;

/**
 --注册方法
 DELETE FROM wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHTranslateService';
 insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHTranslateService', 'com.enhantec.sce.utils.common', 'enhantec', 'TranslateService', 'TRUE',  'JOHN', 'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,Locale, FieldName,Description,TableName,Filter1,Filter2,Filter3,Filter4,Filter5,Filter6,Filter7,Filter8,Filter9,Filter10,Filter11,Filter12,Filter13,Filter14,Filter15,Value1,Value2,Value3,Value4,Value5,Value6,Value7,Value8,Value9,Value10,Value11,Value12,Value13,Value14,Value15','0.10','0');

 */


public class TranslateService extends WMSBaseService {
//    private static final ILogger SCE_LOGGER = SCELoggerFactory.getInstance("RFLookUpP1S1.class");
    private static final int VALUESPERPAGE = 7;

    public TranslateService() {
       super();
    }

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {
//        Context var20;
//        try {
//
//            String locale = "";
//            GetStringOutputParam qqGetStringOutputParamLocale =processData.getInputDataMap().getString("LOCALE"), fieldName);
//            locale = qqGetStringOutputParamLocale.pResult;
//            if(UtilHelper.isEmpty(locale)){
//                locale = Task.getLocale();
//            }
//            Locale sLocale = DBLanguageUtil.getSanitizedLocale( locale);
//            GetStringOutputParam qqGetStringOutputParam =processData.getInputDataMap().getString("FieldName"), fieldName);
//            fieldName = qqGetStringOutputParam.pResult;
//            GetStringOutputParam qqGetStringOutputParam1 =processData.getInputDataMap().getString("Description"), description);
//            description = qqGetStringOutputParam1.pResult;
//            GetStringOutputParam qqGetStringOutputParam2 =processData.getInputDataMap().getString("TableName"), tableName);
//            tableName = qqGetStringOutputParam2.pResult;
//            DateTimeNullable dateValue = new DateTimeNullable();
//            TextData theSQLStmt = );
//            TextData tranSQLStmt = );
//            this.theDO.clearDO();
//            tranSQLStmt.setValue("SELECT ");
//            tranSQLStmt.concat(tableName).concat(".").concat(fieldName).concat(" VALUE, ");
//            tranSQLStmt.concat("TRANSLATIONLIST.DESCRIPTION DESCR FROM ").concat(tableName);
//            tranSQLStmt.concat(", TRANSLATIONLIST ");
//            tranSQLStmt = this.buildWhereClause(tranSQLStmt,processData.getInputDataMap(), tableName, fieldName, sLocale);
//            this.theDO.setSQL(tranSQLStmt, true);
//            context.theSQLMgr.executeSQLStatement(this.theDO);
//            if (this.theDO.getRowCount() == 0) {
//                this.theDO.clearDO();
//                theSQLStmt.setValue("SELECT ");
//                theSQLStmt.concat(fieldName).concat(" VALUE, ").concat(description).concat(" DESCR FROM ").concat(tableName);
//                boolean useAND = false;
//                RFLookUpP1S1.IsDateOutputParam qqIsDateOutputParam;
//                if (!processData.getInputDataMap().isEmptyOrBlank("Filter1")) && !processData.getInputDataMap().isEmptyOrBlank("Value1"))) {
//                    theSQLStmt.concat(" WHERE ");
//                    qqIsDateOutputParam = this.isDate(processData.getInputDataMap().getAttribValue("Filter1")).getTextValue(),processData.getInputDataMap().getAttribValue("Value1")).getTextValue(), dateValue);
//                   processData.getInputDataMap().getAttribValue("Filter1")).setValue(qqIsDateOutputParam.fieldName);
//                    dateValue = qqIsDateOutputParam.dateValue;
//                    if (qqIsDateOutputParam.returnValue) {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter1"))).concat(" = :Filter1 ");
//                        this.theDO.setConstraintItem("Filter1"), dateValue);
//                    } else if (tableName.equalsIgnoreCase("STORER") &&processData.getInputDataMap().getAttribValue("Filter1")).getTextValue().getValue().equalsIgnoreCase("TYPE")) {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter1"))).concat(" = '").concat(processData.getInputDataMap().getAttribValue("Value1"))).concat("'");
//                    } else {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter1"))).concat(" LIKE '%").concat(processData.getInputDataMap().getAttribValue("Value1"))).concat("%'");
//                    }
//
//                    useAND = true;
//                }
//
//                if (!processData.getInputDataMap().isEmptyOrBlank("Filter2")) && !processData.getInputDataMap().isEmptyOrBlank("Value2"))) {
//                    if (useAND) {
//                        theSQLStmt.concat(" AND ");
//                    } else {
//                        theSQLStmt.concat(" WHERE ");
//                    }
//
//                    qqIsDateOutputParam = this.isDate(processData.getInputDataMap().getAttribValue("Filter2")).getTextValue(),processData.getInputDataMap().getAttribValue("Value2")).getTextValue(), dateValue);
//                   processData.getInputDataMap().getAttribValue("Filter2")).setValue(qqIsDateOutputParam.fieldName);
//                    dateValue = qqIsDateOutputParam.dateValue;
//                    if (qqIsDateOutputParam.returnValue) {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter2"))).concat(" = :Filter2 ");
//                        this.theDO.setConstraintItem("Filter2"), dateValue);
//                    } else {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter2"))).concat(" LIKE '%").concat(processData.getInputDataMap().getAttribValue("Value2"))).concat("%'");
//                    }
//
//                    useAND = true;
//                }
//
//                if (!processData.getInputDataMap().isEmptyOrBlank("Filter3")) && !processData.getInputDataMap().isEmptyOrBlank("Value3"))) {
//                    if (useAND) {
//                        theSQLStmt.concat(" AND ");
//                    } else {
//                        theSQLStmt.concat(" WHERE ");
//                    }
//
//                    qqIsDateOutputParam = this.isDate(processData.getInputDataMap().getAttribValue("Filter3")).getTextValue(),processData.getInputDataMap().getAttribValue("Value3")).getTextValue(), dateValue);
//                   processData.getInputDataMap().getAttribValue("Filter3")).setValue(qqIsDateOutputParam.fieldName);
//                    dateValue = qqIsDateOutputParam.dateValue;
//                    if (qqIsDateOutputParam.returnValue) {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter3"))).concat(" = :Filter3 ");
//                        this.theDO.setConstraintItem("Filter3"), dateValue);
//                    } else {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter3"))).concat(" LIKE '%").concat(processData.getInputDataMap().getAttribValue("Value3"))).concat("%'");
//                    }
//
//                    useAND = true;
//                }
//
//                if (!processData.getInputDataMap().isEmptyOrBlank("Filter4")) && !processData.getInputDataMap().isEmptyOrBlank("Value4"))) {
//                    if (useAND) {
//                        theSQLStmt.concat(" AND ");
//                    } else {
//                        theSQLStmt.concat(" WHERE ");
//                    }
//
//                    qqIsDateOutputParam = this.isDate(processData.getInputDataMap().getAttribValue("Filter4")).getTextValue(),processData.getInputDataMap().getAttribValue("Value4")).getTextValue(), dateValue);
//                   processData.getInputDataMap().getAttribValue("Filter4")).setValue(qqIsDateOutputParam.fieldName);
//                    dateValue = qqIsDateOutputParam.dateValue;
//                    if (qqIsDateOutputParam.returnValue) {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter4"))).concat(" = :Filter4 ");
//                        this.theDO.setConstraintItem("Filter4"), dateValue);
//                    } else {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter4"))).concat(" LIKE '%").concat(processData.getInputDataMap().getAttribValue("Value4"))).concat("%'");
//                    }
//
//                    useAND = true;
//                }
//
//                if (!processData.getInputDataMap().isEmptyOrBlank("Filter5")) && !processData.getInputDataMap().isEmptyOrBlank("Value5"))) {
//                    if (useAND) {
//                        theSQLStmt.concat(" AND ");
//                    } else {
//                        theSQLStmt.concat(" WHERE ");
//                    }
//
//                    qqIsDateOutputParam = this.isDate(processData.getInputDataMap().getAttribValue("Filter5")).getTextValue(),processData.getInputDataMap().getAttribValue("Value5")).getTextValue(), dateValue);
//                   processData.getInputDataMap().getAttribValue("Filter5")).setValue(qqIsDateOutputParam.fieldName);
//                    dateValue = qqIsDateOutputParam.dateValue;
//                    if (qqIsDateOutputParam.returnValue) {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter5"))).concat(" = :Filter5 ");
//                        this.theDO.setConstraintItem("Filter5"), dateValue);
//                    } else {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter5"))).concat(" LIKE '%").concat(processData.getInputDataMap().getAttribValue("Value5"))).concat("%'");
//                    }
//
//                    useAND = true;
//                }
//
//                if (!processData.getInputDataMap().isEmptyOrBlank("Filter6")) && !processData.getInputDataMap().isEmptyOrBlank("Value6"))) {
//                    if (useAND) {
//                        theSQLStmt.concat(" AND ");
//                    } else {
//                        theSQLStmt.concat(" WHERE ");
//                    }
//
//                    qqIsDateOutputParam = this.isDate(processData.getInputDataMap().getAttribValue("Filter6")).getTextValue(),processData.getInputDataMap().getAttribValue("Value6")).getTextValue(), dateValue);
//                   processData.getInputDataMap().getAttribValue("Filter6")).setValue(qqIsDateOutputParam.fieldName);
//                    dateValue = qqIsDateOutputParam.dateValue;
//                    if (qqIsDateOutputParam.returnValue) {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter6"))).concat(" = :Filter6 ");
//                        this.theDO.setConstraintItem("Filter6"), dateValue);
//                    } else {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter6"))).concat(" LIKE '%").concat(processData.getInputDataMap().getAttribValue("Value6"))).concat("%'");
//                    }
//
//                    useAND = true;
//                }
//
//                if (!processData.getInputDataMap().isEmptyOrBlank("Filter7")) && !processData.getInputDataMap().isEmptyOrBlank("Value7"))) {
//                    if (useAND) {
//                        theSQLStmt.concat(" AND ");
//                    } else {
//                        theSQLStmt.concat(" WHERE ");
//                    }
//
//                    qqIsDateOutputParam = this.isDate(processData.getInputDataMap().getAttribValue("Filter7")).getTextValue(),processData.getInputDataMap().getAttribValue("Value7")).getTextValue(), dateValue);
//                   processData.getInputDataMap().getAttribValue("Filter7")).setValue(qqIsDateOutputParam.fieldName);
//                    dateValue = qqIsDateOutputParam.dateValue;
//                    if (qqIsDateOutputParam.returnValue) {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter7"))).concat(" = :Filter7 ");
//                        this.theDO.setConstraintItem("Filter7"), dateValue);
//                    } else {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter7"))).concat(" LIKE '%").concat(processData.getInputDataMap().getAttribValue("Value7"))).concat("%'");
//                    }
//
//                    useAND = true;
//                }
//
//                if (!processData.getInputDataMap().isEmptyOrBlank("Filter8")) && !processData.getInputDataMap().isEmptyOrBlank("Value8"))) {
//                    if (useAND) {
//                        theSQLStmt.concat(" AND ");
//                    } else {
//                        theSQLStmt.concat(" WHERE ");
//                    }
//
//                    qqIsDateOutputParam = this.isDate(processData.getInputDataMap().getAttribValue("Filter8")).getTextValue(),processData.getInputDataMap().getAttribValue("Value8")).getTextValue(), dateValue);
//                   processData.getInputDataMap().getAttribValue("Filter8")).setValue(qqIsDateOutputParam.fieldName);
//                    dateValue = qqIsDateOutputParam.dateValue;
//                    if (qqIsDateOutputParam.returnValue) {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter8"))).concat(" = :Filter8 ");
//                        this.theDO.setConstraintItem("Filter8"), dateValue);
//                    } else {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter8"))).concat(" LIKE '%").concat(processData.getInputDataMap().getAttribValue("Value8"))).concat("%'");
//                    }
//
//                    useAND = true;
//                }
//
//                if (!processData.getInputDataMap().isEmptyOrBlank("Filter9")) && !processData.getInputDataMap().isEmptyOrBlank("Value9"))) {
//                    if (useAND) {
//                        theSQLStmt.concat(" AND ");
//                    } else {
//                        theSQLStmt.concat(" WHERE ");
//                    }
//
//                    qqIsDateOutputParam = this.isDate(processData.getInputDataMap().getAttribValue("Filter9")).getTextValue(),processData.getInputDataMap().getAttribValue("Value9")).getTextValue(), dateValue);
//                   processData.getInputDataMap().getAttribValue("Filter9")).setValue(qqIsDateOutputParam.fieldName);
//                    dateValue = qqIsDateOutputParam.dateValue;
//                    if (qqIsDateOutputParam.returnValue) {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter9"))).concat(" = :Filter9 ");
//                        this.theDO.setConstraintItem("Filter9"), dateValue);
//                    } else {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter9"))).concat(" LIKE '%").concat(processData.getInputDataMap().getAttribValue("Value9"))).concat("%'");
//                    }
//
//                    useAND = true;
//                }
//
//                if (!processData.getInputDataMap().isEmptyOrBlank("Filter10")) && !processData.getInputDataMap().isEmptyOrBlank("Value10"))) {
//                    if (useAND) {
//                        theSQLStmt.concat(" AND ");
//                    } else {
//                        theSQLStmt.concat(" WHERE ");
//                    }
//
//                    qqIsDateOutputParam = this.isDate(processData.getInputDataMap().getAttribValue("Filter10")).getTextValue(),processData.getInputDataMap().getAttribValue("Value10")).getTextValue(), dateValue);
//                   processData.getInputDataMap().getAttribValue("Filter10")).setValue(qqIsDateOutputParam.fieldName);
//                    dateValue = qqIsDateOutputParam.dateValue;
//                    if (qqIsDateOutputParam.returnValue) {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter10"))).concat(" = :Filter10 ");
//                        this.theDO.setConstraintItem("Filter10"), dateValue);
//                    } else {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter10"))).concat(" LIKE '%").concat(processData.getInputDataMap().getAttribValue("Value10"))).concat("%'");
//                    }
//
//                    useAND = true;
//                }
//
//                if (!processData.getInputDataMap().isEmptyOrBlank("Filter11")) && !processData.getInputDataMap().isEmptyOrBlank("Value11"))) {
//                    if (useAND) {
//                        theSQLStmt.concat(" AND ");
//                    } else {
//                        theSQLStmt.concat(" WHERE ");
//                    }
//
//                    qqIsDateOutputParam = this.isDate(processData.getInputDataMap().getAttribValue("Filter11")).getTextValue(),processData.getInputDataMap().getAttribValue("Value11")).getTextValue(), dateValue);
//                   processData.getInputDataMap().getAttribValue("Filter11")).setValue(qqIsDateOutputParam.fieldName);
//                    dateValue = qqIsDateOutputParam.dateValue;
//                    if (qqIsDateOutputParam.returnValue) {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter11"))).concat(" = :Filter11 ");
//                        this.theDO.setConstraintItem("Filter11"), dateValue);
//                    } else {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter11"))).concat(" LIKE '%").concat(processData.getInputDataMap().getAttribValue("Value11"))).concat("%'");
//                    }
//
//                    useAND = true;
//                }
//
//                if (!processData.getInputDataMap().isEmptyOrBlank("Filter12")) && !processData.getInputDataMap().isEmptyOrBlank("Value12"))) {
//                    if (useAND) {
//                        theSQLStmt.concat(" AND ");
//                    } else {
//                        theSQLStmt.concat(" WHERE ");
//                    }
//
//                    qqIsDateOutputParam = this.isDate(processData.getInputDataMap().getAttribValue("Filter12")).getTextValue(),processData.getInputDataMap().getAttribValue("Value12")).getTextValue(), dateValue);
//                   processData.getInputDataMap().getAttribValue("Filter12")).setValue(qqIsDateOutputParam.fieldName);
//                    dateValue = qqIsDateOutputParam.dateValue;
//                    if (qqIsDateOutputParam.returnValue) {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter12"))).concat(" = :Filter12 ");
//                        this.theDO.setConstraintItem("Filter12"), dateValue);
//                    } else {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter12"))).concat(" LIKE '%").concat(processData.getInputDataMap().getAttribValue("Value12"))).concat("%'");
//                    }
//
//                    useAND = true;
//                }
//
//                if (!processData.getInputDataMap().isEmptyOrBlank("Filter13")) && !processData.getInputDataMap().isEmptyOrBlank("Value13"))) {
//                    if (useAND) {
//                        theSQLStmt.concat(" AND ");
//                    } else {
//                        theSQLStmt.concat(" WHERE ");
//                    }
//
//                    qqIsDateOutputParam = this.isDate(processData.getInputDataMap().getAttribValue("Filter13")).getTextValue(),processData.getInputDataMap().getAttribValue("Value13")).getTextValue(), dateValue);
//                   processData.getInputDataMap().getAttribValue("Filter13")).setValue(qqIsDateOutputParam.fieldName);
//                    dateValue = qqIsDateOutputParam.dateValue;
//                    if (qqIsDateOutputParam.returnValue) {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter13"))).concat(" = :Filter13 ");
//                        this.theDO.setConstraintItem("Filter13"), dateValue);
//                    } else {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter13"))).concat(" LIKE '%").concat(processData.getInputDataMap().getAttribValue("Value13"))).concat("%'");
//                    }
//
//                    useAND = true;
//                }
//
//                if (!processData.getInputDataMap().isEmptyOrBlank("Filter14")) && !processData.getInputDataMap().isEmptyOrBlank("Value14"))) {
//                    if (useAND) {
//                        theSQLStmt.concat(" AND ");
//                    } else {
//                        theSQLStmt.concat(" WHERE ");
//                    }
//
//                    qqIsDateOutputParam = this.isDate(processData.getInputDataMap().getAttribValue("Filter14")).getTextValue(),processData.getInputDataMap().getAttribValue("Value14")).getTextValue(), dateValue);
//                   processData.getInputDataMap().getAttribValue("Filter14")).setValue(qqIsDateOutputParam.fieldName);
//                    dateValue = qqIsDateOutputParam.dateValue;
//                    if (qqIsDateOutputParam.returnValue) {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter14"))).concat(" = :Filter14 ");
//                        this.theDO.setConstraintItem("Filter14"), dateValue);
//                    } else {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter14"))).concat(" LIKE '%").concat(processData.getInputDataMap().getAttribValue("Value14"))).concat("%'");
//                    }
//
//                    useAND = true;
//                }
//
//                if (!processData.getInputDataMap().isEmptyOrBlank("Filter15")) && !processData.getInputDataMap().isEmptyOrBlank("Value15"))) {
//                    if (useAND) {
//                        theSQLStmt.concat(" AND ");
//                    } else {
//                        theSQLStmt.concat(" WHERE ");
//                    }
//
//                    qqIsDateOutputParam = this.isDate(processData.getInputDataMap().getAttribValue("Filter15")).getTextValue(),processData.getInputDataMap().getAttribValue("Value15")).getTextValue(), dateValue);
//                   processData.getInputDataMap().getAttribValue("Filter15")).setValue(qqIsDateOutputParam.fieldName);
//                    dateValue = qqIsDateOutputParam.dateValue;
//                    if (qqIsDateOutputParam.returnValue) {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter15"))).concat(" = :Filter15 ");
//                        this.theDO.setConstraintItem("Filter15"), dateValue);
//                    } else {
//                        theSQLStmt.concat(processData.getInputDataMap().getAttribValue("Filter15"))).concat(" LIKE '%").concat(processData.getInputDataMap().getAttribValue("Value15"))).concat("%'");
//                    }
//
//                    useAND = true;
//                }
//
//                if (tableName.equalsIgnoreCase("CODELKUP")) {
//                    theSQLStmt.concat(" AND ACTIVE = '1' ");
//                }
//
//                this.theDO.setSQL(theSQLStmt, true);
//                context.theSQLMgr.executeSQLStatement(this.theDO);
//                if (this.theDO.getRowCount() == 0) {
//                    throw (new FulfillLogicException()).setup(2, 2, 8, 99, FObject.NX(this, "No Rows Returned..."), FulfillException.D_PARAM_SETUP_PPARAM1, FulfillException.D_PARAM_SETUP_PPARAM2, FulfillException.D_PARAM_SETUP_PPARAM3, FulfillException.D_PARAM_SETUP_PPARAM4, FulfillException.D_PARAM_SETUP_PPARAM5, FulfillException.D_PARAM_SETUP_PPARAM6, FulfillException.D_PARAM_SETUP_PPARAM7, FulfillException.D_PARAM_SETUP_PPARAM8, FulfillException.D_PARAM_SETUP_PPARAM9);
//                }
//            }
//
//            if (!processData.getInputDataMap().isEmptyOrBlank("Filter1")) && !processData.getInputDataMap().isEmptyOrBlank("Value1")) &&processData.getInputDataMap().getAttribValue("Value1")).toString().equals("TRLSTATUS")) {
//                context.theEXEDataObjectStack.push(this.getReturnDOforTrailerStatus());
//            } else {
//                context.theEXEDataObjectStack.push(this.getReturnDO());
//            }
//
//            var20 = context;
//        } finally {
//            SCE_LOGGER.debug("leaving execute(ObjectprocessData.getInputDataMap())");
//        }
//
//        return var20;
    }

}
