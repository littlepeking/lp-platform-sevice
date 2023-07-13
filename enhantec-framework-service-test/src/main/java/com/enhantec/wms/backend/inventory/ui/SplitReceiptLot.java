package com.enhantec.wms.backend.inventory.ui;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.inventory.VLotAttribute;
import com.enhantec.wms.backend.framework.LegacyBaseService;import com.enhantec.wms.backend.framework.Context;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * --注册方法
 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHSplitReceiptLot'
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHSplitReceiptLot', 'com.enhantec.sce.inventory.ui', 'enhantec', 'SplitReceiptLot', 'TRUE', 'JOHN', 'JOHN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,TOLOT,IDS,ESIGNATUREKEY','0.10','0');
 */


public class SplitReceiptLot extends com.enhantec.wms.backend.framework.LegacyBaseService {


    private static final long serialVersionUID = 1L;

    public SplitReceiptLot() {
    }

    public void execute(com.enhantec.wms.backend.framework.ServiceDataHolder processData) {
        
        String userid = context.getUserID();

        Connection conn = null;
        try
        {
            conn = context.getConnection();
            String ids = processData.getInputDataAsMap().getString("IDS");
            String eSignatureKey = processData.getInputDataAsMap().getString("ESIGNATUREKEY");
            String toLottablemerge=processData.getInputDataAsMap().getString("TOLOT");
            if(ids==null || ids.trim().length()==0) ExceptionHelper.throwRfFulfillLogicException("传入的容器条码列表不能为空");

            String[] idArray = ids.split(",");

            String idStr = "'" + String.join("','",idArray)+ "'" ;

            List<HashMap<String,String>> lottabl06List = DBHelper.executeQuery(context, conn, "SELECT DISTINCT la.LOTTABLE06 FROM IDNOTES  id, LOTATTRIBUTE la " +
                    " WHERE id.LOT = la.LOT AND ID IN ("+idStr+")", new Object[]{});

            if(lottabl06List.size()>1) ExceptionHelper.throwRfFulfillLogicException("请选择相同收货批次的容器进行批次拆分合并");
            if(lottabl06List.size()==0) ExceptionHelper.throwRfFulfillLogicException("未找到待拆分合并批次的容器条码");


            String lottabl06ToBeSplit = lottabl06List.get(0).get("LOTTABLE06");

            //String lottable06Splitted = IdGenerationHelper.generateID(context, conn, userid,lottabl06ToBeSplit + "S",2);
            String lottable06Splitted = " ";//默认
            if (UtilHelper.isEmpty(toLottablemerge)) {
                 lottable06Splitted = IdGenerationHelper.createSubReceiptLot(context, conn, lottabl06ToBeSplit, "S");

                //新增ELOTATTIBUTE批次记录
                HashMap<String, String> elotHashMap = VLotAttribute.findElottableByLottable06(context, conn, lottabl06ToBeSplit, true);

                LinkedHashMap<String, String> newELotHashMap = new LinkedHashMap<String, String>();
                newELotHashMap.put("STORERKEY", elotHashMap.get("STORERKEY"));
                newELotHashMap.put("SKU", elotHashMap.get("SKU"));
                newELotHashMap.put("ELOT", lottable06Splitted);
                for (int i = 1; i <= 25; i++) {
                    String num = UtilHelper.addPrefixZeros4Number(i, 2);
                    newELotHashMap.put("ELOTTABLE" + num, UtilHelper.trim(elotHashMap.get("ELOTTABLE" + num)));
                }
                newELotHashMap.put("ELOTTABLE15", lottabl06ToBeSplit);

                LegacyDBHelper.ExecInsert(context, conn, "ENTERPRISE.ELOTATTRIBUTE", newELotHashMap);
                doReceiptLotTransfer(context,conn, lottable06Splitted, idArray);

            }else {
                //合并
                HashMap<String, String> elotHashMap = VLotAttribute.findElottableByLottable06(context, conn, lottabl06ToBeSplit, true);
                HashMap<String, String> mergeelotHashMap = VLotAttribute.findElottableByLottable06(context, conn, toLottablemerge, true);
                for (int i = 1; i <= 25; i++) {
                    String num = UtilHelper.addPrefixZeros4Number(i, 2);
                    String toElotpro=UtilHelper.isEmpty(mergeelotHashMap.get("ELOTTABLE" +num))?" ":mergeelotHashMap.get("ELOTTABLE" +num);
                    String fromElotpro=UtilHelper.isEmpty(elotHashMap.get("ELOTTABLE" +num))?" ":elotHashMap.get("ELOTTABLE" +num);
                    if (!toElotpro.equalsIgnoreCase(fromElotpro)&&!num.equalsIgnoreCase("15"))
                    throw new Exception("所选托盘批次内容与“至批号”属性不一致请选择批次一致的托盘");
                }
                if (!elotHashMap.get("SKU").equals(mergeelotHashMap.get("SKU" )))
                throw new Exception("所选托盘批次内容与“至批号”物料不一致请选择物料一致的托盘");

                doReceiptLotTransfer(context,conn, toLottablemerge, idArray);


            }


            Udtrn UDTRN=new Udtrn();
            UDTRN.EsignatureKey=eSignatureKey;
            UDTRN.FROMTYPE="拆分合并收货批次";
            UDTRN.FROMTABLENAME="ELOTTATTRIBUTE";
            UDTRN.FROMKEY=lottabl06ToBeSplit;
            UDTRN.FROMKEY1="";
            UDTRN.FROMKEY2="";
            UDTRN.TITLE01="原批次"; UDTRN.CONTENT01=lottabl06ToBeSplit;
            UDTRN.TITLE02="拆分批次"; UDTRN.CONTENT02=lottable06Splitted;
            UDTRN.TITLE03="合并批次"; UDTRN.CONTENT03=toLottablemerge;
            UDTRN.Insert(context, conn, userid);
            //--------------------------------------------------------------
            try	{	context.releaseConnection(conn); 	}	catch (Exception e1) {		}

            ServiceDataMap theOutDO = new ServiceDataMap();

            theOutDO.setAttribValue("SplittedLot",lottable06Splitted);
            processData.setReturnCode(1);
            processData.setOutputData(theOutDO);
          
        }
        catch (Exception e)
        {
            try
            {
                context.releaseConnection(conn);
            }	catch (Exception e1) {		}
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());
        }finally {
            try	{	context.releaseConnection(conn); }	catch (Exception e1) {		}
        }
    }

    private void doReceiptLotTransfer(Context context, Connection conn, String  lottable06Splitted, String[] idArray) throws Exception {

        String newTransferKey = "S"+IdGenerationHelper.generateIDByKeyName(context, conn, context.getUserID(),"EHTRANSFER",9);

        String storerKey = String.valueOf(DBHelper.getValue(context, conn, "select UDF1 from Codelkup where ListName=? and Code=?",
                            new Object[]{"SYSSET","STORERKEY"}, "默认货主"));

        String totalOpenQty = "0";

        int maxTransferDetailKey = 0;

        //key-transferDetailKey value-id
        HashMap<String,String> transferDetailKeyHashMap = new HashMap<>();

        for(String id : idArray) {

            HashMap<String, String> idHashMap = LotxLocxId.findFullAvailInvById(context, conn, id, "容器" + id + "不存在或者已被分配或拣货，当前状态不允许拆批次");

            String newTransferDetailKey = UtilHelper.To_Char(new Integer(++maxTransferDetailKey), 5);

            DBHelper.executeUpdate(context, conn,
                    "INSERT INTO TRANSFERDETAIL (" +
                    "TRANSFERKEY, TRANSFERLINENUMBER,FROMSTORERKEY,FROMSKU,FROMLOC,FROMLOT,FROMID,FROMQTY,FROMPACKKEY,FROMUOM," +
                    "LOTTABLE01,LOTTABLE02,LOTTABLE03,LOTTABLE04,LOTTABLE05,LOTTABLE06,LOTTABLE07,LOTTABLE08,LOTTABLE09,LOTTABLE10,LOTTABLE11,LOTTABLE12," +
                    "TOSTORERKEY,TOSKU,TOLOC,TOLOT,TOID,TOQTY,TOPACKKEY,TOUOM,STATUS,EFFECTIVEDATE,FORTE_FLAG) " +
                    " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    new Object[]{
                            newTransferKey,newTransferDetailKey, storerKey, idHashMap.get("SKU"), idHashMap.get("LOC"),
                            idHashMap.get("LOT"), idHashMap.get("ID"), idHashMap.get("QTY"), idHashMap.get("PACKKEY"), idHashMap.get("UOM"),
                            idHashMap.get("LOTTABLE01"), idHashMap.get("LOTTABLE02"), idHashMap.get("LOTTABLE03"),
                            UtilHelper.convertStringToSqlDate(idHashMap.get("LOTTABLE04")),
                            UtilHelper.convertStringToSqlDate(idHashMap.get("LOTTABLE05")),
                            lottable06Splitted,
                            idHashMap.get("LOTTABLE07"), idHashMap.get("LOTTABLE08"), idHashMap.get("LOTTABLE09"),
                             idHashMap.get("LOTTABLE10"),
                            UtilHelper.convertStringToSqlDate(idHashMap.get("LOTTABLE11")),
                            UtilHelper.convertStringToSqlDate(idHashMap.get("LOTTABLE12")),
                            storerKey, idHashMap.get("SKU"), idHashMap.get("LOC"),
                            "", idHashMap.get("ID"), idHashMap.get("QTY"),
                            idHashMap.get("PACKKEY"), idHashMap.get("UOM"), "0",
                            UtilHelper.getCurrentSqlDate(), "I"
            });

            totalOpenQty = UtilHelper.decimalStrAdd(totalOpenQty,idHashMap.get("QTY"));

            transferDetailKeyHashMap.put(newTransferDetailKey,id);
        }

            DBHelper.executeUpdate(context,conn," INSERT INTO TRANSFER (" +
                    "TRANSFERKEY," +
                    "FROMSTORERKEY," +
                    "TOSTORERKEY," +
                    "[TYPE]," +
                    "OPENQTY," +
                    "STATUS," +
                    "GENERATEHOCHARGES," +
                    "GENERATEIS_HICHARGES," +
                    "EFFECTIVEDATE," +
                    "FORTE_FLAG," +
                    "RELOT," +
                    "REFERENCENUMBER," +
                    "NOTES) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",new Object[]{
                    newTransferKey,
                    storerKey,
                    storerKey,
                    "STORER",
                    totalOpenQty,
                    "0",
                    "0",
                    "0",
                    UtilHelper.getCurrentSqlDate(),
                    "I",
                    "0",
                    null,
                    null

            });

        //call native internal transfer service
        for(String tempTransferDetailKey: transferDetailKeyHashMap.keySet()) {

            callTransferService(context, conn, newTransferKey, tempTransferDetailKey);

            //更新IDNOTES的LOT
            String id =transferDetailKeyHashMap.get(tempTransferDetailKey);
            HashMap<String, String> idHashMapUpdated = LotxLocxId.findFullAvailInvById(context, conn, id, "容器" + id + "不存在或者已被分配或拣货，当前状态不允许拆批次");
            DBHelper.executeUpdate(context,conn,"UPDATE IDNOTES SET LOT = ? WHERE ID = ? ",
                    new Object[]{idHashMapUpdated.get("LOT"),idHashMapUpdated.get("ID")});


        }



    }

    private void callTransferService(Context context, Connection conn, String newTransferKey, String tempTransferDetailKey) {
//        EXEDataObject theTriggerDO = new EXEDataObject();
//        theTriggerDO.setConstraintItem("transferkey"), newTransferKey));
//        theTriggerDO.setConstraintItem("transferdetailkey"), tempTransferDetailKey));
//        theTriggerDO.setWhereClause("WHERE TransferKey = :transferkey and TRANSFERLINENUMBER = :transferdetailkey ");
//        context.theEXEDataObjectStack.push(theTriggerDO);
//        context.theSQLMgr.searchTriggerLibrary("TRANSFERDETAIL")).preUpdateFire(context);

        //todo
        DBHelper.executeUpdate(context, conn, " UPDATE TRANSFERDETAIL SET STATUS = '9' WHERE TRANSFERKEY = ? AND TRANSFERLINENUMBER = ? ", new Object[]{
                newTransferKey,  tempTransferDetailKey
        });

//        context.theSQLMgr.searchTriggerLibrary("TRANSFERDETAIL")).postUpdateFire(context);
    }


}
