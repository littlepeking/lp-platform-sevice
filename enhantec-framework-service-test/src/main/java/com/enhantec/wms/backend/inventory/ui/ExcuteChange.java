
package com.enhantec.wms.backend.inventory.ui;

import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.inventory.VLotAttribute;
import com.enhantec.wms.backend.framework.LegacyBaseService;import com.enhantec.framework.common.utils.EHContextHelper;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.inventory.utils.ChangeByLotHelper;
import com.enhantec.wms.backend.inventory.utils.InventoryValidationHelper;
import com.enhantec.wms.backend.utils.common.*;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ExcuteChange extends LegacyBaseService {


    /**
     * --??
     delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHExcuteChange'
     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHExcuteChange', 'com.enhantec.sce.inventory.ui', 'enhantec', 'ExcuteChange', 'TRUE', 'JOHN', 'JOHN'
     , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,Changekey,ESIGNATUREKEY','0.10','0');
     */


    private static final long serialVersionUID = 1L;

    public ExcuteChange() {
    }
    //此类借用更改项目号功能来实现生基更改物料代码功能
    public void execute(ServiceDataHolder serviceDataHolder) {
        String userid = EHContextHelper.getUser().getUsername();


        try {



            String changekey = serviceDataHolder.getInputDataAsMap().getString("CHANGEKEY");
            String esignatureKey = serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

            String SQL = " select  e2.FROMELOT,e2.FROMSKU,e2.TOSKU,e2.PACKEY,e.STATUS,e2.changeline from ENCHGPROJECTCODE e , ENCHGPROJECTCODEDETAIL e2 " +
                    "          where e.CHANGEKEY = e2.CHANGEKEY AND e.CHANGEKEY = ?";
            List<HashMap<String,String>> list = DBHelper.executeQuery( SQL, new Object[]{
                    changekey});
            //校验后生成新批次，调用TRANSFER传入变更信息
            for(HashMap<String,String> changeRecord:list){
                if (!"3".equalsIgnoreCase(changeRecord.get("STATUS")))
                    ExceptionHelper.throwRfFulfillLogicException("变更单为"+changekey+"未复核无法操作");
                String fromSku=changeRecord.get("FROMSKU");
                String toSku=changeRecord.get("TOSKU");
                String fromLottable06=changeRecord.get("FROMELOT");
                ChangeByLotHelper.checkSkuAttributeIsMatch(fromSku,toSku);
                InventoryValidationHelper.validateLotQty(fromLottable06);
               String toLottable06= IdGenerationHelper.createReceiptLot(toSku);
               //插入新批次将旧批次批属性复制至新批次
                HashMap<String,String> elotHashMap = VLotAttribute.findElottableByLottable06(fromLottable06,true);
                HashMap<String,String> newELotHashMap = new HashMap<String,String>();
                newELotHashMap.put("STORERKEY", elotHashMap.get("STORERKEY"));
                newELotHashMap.put("SKU", toSku);
                newELotHashMap.put("ELOT", toLottable06);
                for (int i = 1; i <= 25; i++) {
                    String num = UtilHelper.addPrefixZeros4Number(i, 2);
                    newELotHashMap.put("ELOTTABLE" + num, UtilHelper.trim(elotHashMap.get("ELOTTABLE" + num)));
                }
                newELotHashMap.put("ELOTTABLE15", fromLottable06);
                LegacyDBHelper.ExecInsert( "ENTERPRISE.ELOTATTRIBUTE", newELotHashMap);
                doTransfer(fromLottable06,toSku,toLottable06);
                DBHelper.executeUpdate( "UPDATE ENCHGPROJECTCODEDETAIL SET TOELOT = ? WHERE CHANGEKEY = ? and changeline = ?", new String[]{toLottable06,changekey,changeRecord.get("CHANGELINE")});
            }




            DBHelper.executeUpdate( "UPDATE ENCHGPROJECTCODE SET STATUS = 5 WHERE CHANGEKEY = ?", new String[]{changekey});
            DBHelper.executeUpdate( "UPDATE ENCHGPROJECTCODEDETAIL SET STATUS = 5 WHERE CHANGEKEY = ?", new String[]{changekey});

          

        }catch (Exception e){
            
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());

        }finally {
            
        }
    }


    private void doTransfer( String fromLottable06,String toSku,String toLottable06) throws Exception {

        String newTransferKey = "S"+IdGenerationHelper.generateIDByKeyName( EHContextHelper.getUser().getUsername(),"EHTRANSFER",9);

        String storerKey = String.valueOf(DBHelper.getValue( "select UDF1 from Codelkup where ListName=? and Code=?",
                new Object[]{"SYSSET","STORERKEY"}, "默认货主"));

        String totalOpenQty = "0";

        int maxTransferDetailKey = 0;

        //key-transferDetailKey value-id
        HashMap<String,String> transferDetailKeyHashMap = new HashMap<>();
        //查询lottable06下所有lpn
        String SQL = " select  d.id from v_lotattribute b, IDNOTES d , lotxlocxid l " +
                "          where b.lot=d.lot AND l.id=d.id AND b.lottable06 = ? and l.QTY > 0";
        List<HashMap<String,String>> idList = DBHelper.executeQuery( SQL, new Object[]{
                fromLottable06});
        for (HashMap<String,String> id :idList) {
            HashMap<String, String> idHashMap = LotxLocxId.findFullAvailInvById( id.get("ID"), "容器" + id + "不存在或者已被分配或拣货，当前状态不允许转换");

            String newTransferDetailKey = UtilHelper.To_Char(new Integer(++maxTransferDetailKey), 5);

            DBHelper.executeUpdate(
                    "INSERT INTO TRANSFERDETAIL (" +
                            "TRANSFERKEY, TRANSFERLINENUMBER,FROMSTORERKEY,FROMSKU,FROMLOC,FROMLOT,FROMID,FROMQTY,FROMPACKKEY,FROMUOM," +
                            "LOTTABLE01,LOTTABLE02,LOTTABLE03,LOTTABLE04,LOTTABLE05,LOTTABLE06,LOTTABLE07,LOTTABLE08,LOTTABLE09,LOTTABLE10,LOTTABLE11,LOTTABLE12," +
                            "TOSTORERKEY,TOSKU,TOLOC,TOLOT,TOID,TOQTY,TOPACKKEY,TOUOM,STATUS,EFFECTIVEDATE,FORTE_FLAG) " +
                            " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    new Object[]{
                            newTransferKey, newTransferDetailKey, storerKey, idHashMap.get("SKU"), idHashMap.get("LOC"),
                            idHashMap.get("LOT"), idHashMap.get("ID"), idHashMap.get("QTY"), idHashMap.get("PACKKEY"), idHashMap.get("UOM"),
                            idHashMap.get("LOTTABLE01"), idHashMap.get("LOTTABLE02"), idHashMap.get("LOTTABLE03"),
                            UtilHelper.convertStringToSqlDate(idHashMap.get("LOTTABLE04")),
                            UtilHelper.convertStringToSqlDate(idHashMap.get("LOTTABLE05")),
                            toLottable06,
                            idHashMap.get("LOTTABLE07"), idHashMap.get("LOTTABLE08"), idHashMap.get("LOTTABLE09"),
                            idHashMap.get("LOTTABLE10"),
                            UtilHelper.convertStringToSqlDate(idHashMap.get("LOTTABLE11")),
                            UtilHelper.convertStringToSqlDate(idHashMap.get("LOTTABLE12")),
                            storerKey, toSku, idHashMap.get("LOC"),
                            "", idHashMap.get("ID"), idHashMap.get("QTY"),
                            idHashMap.get("PACKKEY"), idHashMap.get("UOM"), "0",
                            UtilHelper.getCurrentSqlDate(), "I"
                    });

            totalOpenQty = UtilHelper.decimalStrAdd(totalOpenQty, (String)idHashMap.get("QTY"));

            transferDetailKeyHashMap.put(newTransferDetailKey, id.get("ID"));
        }
            DBHelper.executeUpdate( " INSERT INTO TRANSFER (" +
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
                    "NOTES) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)", new Object[]{
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

                callTransferService( newTransferKey, tempTransferDetailKey);

                //更新IDNOTES
                String idBeUpdate = transferDetailKeyHashMap.get(tempTransferDetailKey);
                HashMap<String, String> idHashMapUpdated = LotxLocxId.findFullAvailInvById( idBeUpdate, "容器" + idBeUpdate + "不存在或者已被分配或拣货，当前状态不允许拆批次");
                DBHelper.executeUpdate( "UPDATE IDNOTES SET LOT = ?,SKU=? WHERE ID = ? ",
                        new Object[]{idHashMapUpdated.get("LOT"), toSku,idHashMapUpdated.get("ID")});
            }

            }





    private void callTransferService( String newTransferKey, String tempTransferDetailKey) {
        EXEDataObject theTriggerDO = new EXEDataObject();
//        theTriggerDO.setConstraintItem("transferkey"), newTransferKey));
//        theTriggerDO.setConstraintItem("transferdetailkey"), tempTransferDetailKey));
//        theTriggerDO.setWhereClause("WHERE TransferKey = :transferkey and TRANSFERLINENUMBER = :transferdetailkey ");
//        context.theEXEDataObjectStack.push(theTriggerDO);
//        context.theSQLMgr.searchTriggerLibrary("TRANSFERDETAIL")).preUpdateFire();
//todo
        DBHelper.executeUpdate( " UPDATE TRANSFERDETAIL SET STATUS = '9' WHERE TRANSFERKEY = ? AND TRANSFERLINENUMBER = ? ", new Object[]{
                newTransferKey,  tempTransferDetailKey
        });

//        context.theSQLMgr.searchTriggerLibrary("TRANSFERDETAIL")).postUpdateFire();
    }



}