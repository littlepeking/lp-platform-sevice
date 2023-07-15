package com.enhantec.wms.backend.outbound.ship;

import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.outbound.PickDetail;
import com.enhantec.wms.backend.framework.LegacyBaseService;import com.enhantec.wms.backend.framework.Context;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.outbound.utils.OrderValidationHelper;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;


/**
 --注册方法

 DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHShipById'
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHShipById', 'com.enhantec.sce.outbound.order.ship', 'enhantec', 'ShipById', 'FALSE', 'JOHN',  'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,fromid,esignaturekey','0.10','0');


 */

public class ShipById extends LegacyBaseService {

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {

        Context context = null;


        try {

            String userid = context.getUserID();



            String fromId = serviceDataHolder.getInputDataAsMap().getString("fromid");

            boolean isBoxId = IDNotes.isBoxId(context, fromId);

            String orderKey = "";


        /*
        0	正常        1	已发放        3	处理中        4	已结        5	拣货量
        6	已包装        7	已分类        8	装载量        9	发货量        */
        //
        //            if (pickDetailInfo.get("STATUS").equals("9")) {
        //                ExceptionHelper.throwRfFulfillLogicException("容器条码不能重复发货");
        //            }
        //            if (!(pickDetailInfo.get("STATUS").equals("5")
        //                    || pickDetailInfo.get("STATUS").equals("6")
        //                    || pickDetailInfo.get("STATUS").equals("7")
        //                    || pickDetailInfo.get("STATUS").equals("8"))) {
        //                ExceptionHelper.throwRfFulfillLogicException("容器条码未拣货不能发运");
        //            }

            if(isBoxId) {

                List<HashMap<String, String>> pickDetailInfos = LotxLocxId.findPickedIdsByParentId(context,fromId);

                OrderValidationHelper.checkOrderTypeAndQualityStatusByPickDetailKey(context,pickDetailInfos.get(0).get("PICKDETAILKEY"));

                orderKey = pickDetailInfos.get(0).get("ORDERKEY");

                for( HashMap<String, String> pickDetailInfo: pickDetailInfos){
                    shipSingleLpn(context,pickDetailInfo);
                }


            }else {

                HashMap<String, String> pickDetailInfo = PickDetail.findPickedLpn(context, fromId, true);

                OrderValidationHelper.checkOrderTypeAndQualityStatusByPickDetailKey(context,pickDetailInfo.get("PICKDETAILKEY"));

                orderKey = pickDetailInfo.get("ORDERKEY");

                shipSingleLpn(context,pickDetailInfo);

            }




            String esignaturekey = serviceDataHolder.getInputDataAsMap().getString("esignaturekey");
            Udtrn UDTRN = new Udtrn();
            UDTRN.EsignatureKey = esignaturekey;
            UDTRN.FROMTYPE = "按容器条码发运";
            UDTRN.FROMTABLENAME = "PICKDETAIL";
            UDTRN.FROMKEY = fromId;
            UDTRN.FROMKEY1 = "";
            UDTRN.FROMKEY2 = "";
            UDTRN.FROMKEY3 = "";
            UDTRN.TITLE01 = "订单号";
            UDTRN.CONTENT01 = orderKey;
            UDTRN.TITLE02 = "容器条码/箱号";
            UDTRN.CONTENT02 = fromId;

            UDTRN.Insert(context, context.getUserID());


        }catch (Exception e) {
            if (e instanceof FulfillLogicException)
                throw (FulfillLogicException) e;
            else
                throw new FulfillLogicException(e.getMessage());
        }
        finally {
//            if (context.theSQLMgr.isActive()) {
//                context.theSQLMgr.transactionCommit();
//            }
            
        }
    }

    private void shipSingleLpn(Context context, HashMap<String, String> pickDetailInfo ) throws Exception {

        
        DBHelper.executeUpdate(context,"UPDATE DROPID SET STATUS = '0' WHERE DROPID = ? ", new Object[]{pickDetailInfo.get("ID")});

        //
        HashMap<String,String> shippedIdNotesHashMap = IDNotes.decreaseWgtById(context,new BigDecimal(pickDetailInfo.get("QTY")), pickDetailInfo.get("ID"));
        if(UtilHelper.decimalStrCompare(shippedIdNotesHashMap.get("NETWGT"), "0")>0)
            ExceptionHelper.throwRfFulfillLogicException("待发运的容器条码/箱号"+ pickDetailInfo.get("ID")+"发运时扣库存异常(不为零)，发运失败");
        //校验发运的IDNOTES库存余额应为0并移至历史表，否则报错
        IDNotes.archiveIDNotes(context, shippedIdNotesHashMap);

        //TODO:通过修改拣货明细状态的方式发运，看是否还有事务问题
        ServiceDataHolder inboundDo = new ServiceDataHolder();

        inboundDo.getInputDataAsMap().setAttribValue("ttm", "N");
        inboundDo.getInputDataAsMap().setAttribValue("fromid", pickDetailInfo.get("ID"));
        inboundDo.getInputDataAsMap().setAttribValue("fromloc", "");
        inboundDo.getInputDataAsMap().setAttribValue("toid", "");
        inboundDo.getInputDataAsMap().setAttribValue("toloc", "");
        inboundDo.getInputDataAsMap().setAttribValue("action", "SHIP");
        inboundDo.getInputDataAsMap().setAttribValue("confirm", "");
        inboundDo.getInputDataAsMap().setAttribValue("carrierid", "");
        inboundDo.getInputDataAsMap().setAttribValue("cartongroup", "");
        inboundDo.getInputDataAsMap().setAttribValue("cartontype", "");
        inboundDo.getInputDataAsMap().setAttribValue("islabelsshipped", "N");
//
//        context.theEXEDataObjectStack.stackList.clear();
//        context.theEXEDataObjectStack.stackList.add(inboundDo);

       // todo ship: super.execute(inboundDo);
    }


}
