package com.enhantec.wms.backend.inventory.repackaging;

import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.util.HashMap;


/**
 --注册方法

 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME = 'EHPreRepackaging';
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHPreRepackaging', 'com.enhantec.sce.inventory.repackaging', 'enhantec', 'PreRepackaging','TRUE','JOHN','JOHN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,TYPE,ORDERKEY,ORDERLINENUMBER,FROMID,PACKLOC','0.10','0');


 **/

public class PreRepackaging extends LegacyBaseService {


    private static final long serialVersionUID = 1L;
    public static final String ADD_LPN = "ADD";
    public static final String REMOVE_LPN = "REMOVE";

    public PreRepackaging()
    {
    }

    public void execute(ServiceDataHolder serviceDataHolder)
    {
        String userid = context.getUserID();

        Connection conn = null;


        try
        {
            conn = context.getConnection();
            final String type = serviceDataHolder.getInputDataAsMap().getString("TYPE");
            final String orderKey = serviceDataHolder.getInputDataAsMap().getString("ORDERKEY");
            final String orderLineNumber = serviceDataHolder.getInputDataAsMap().getString("ORDERLINENUMBER");
            final String fromId= serviceDataHolder.getInputDataAsMap().getString("FROMID");
            final String packLoc= serviceDataHolder.getInputDataAsMap().getString("PACKLOC");

            boolean isInRepackProcess = RepackgingUtils.isInRepackProcess(context,conn,orderKey,orderLineNumber);
            if(isInRepackProcess) ExceptionHelper.throwRfFulfillLogicException("分装进行中，不允许备货");

            String opName = null;

            if(!UtilHelper.isEmpty(type)){
                if(type.equalsIgnoreCase(ADD_LPN)){
                    opName = "分装备货";
                }else if(type.equalsIgnoreCase(REMOVE_LPN)){
                    opName = "取消分装备货";
                }else{
                    throw new Exception("未找到备货备货操作类型"+opName);
                }
            }else{
                throw new Exception("备货操作类型不能为空");
            }


            if (UtilHelper.isEmpty(orderKey)) throw new Exception("订单号不能为空");
            if (UtilHelper.isEmpty(orderLineNumber)) throw new Exception("订单行号不能为空");
            if (UtilHelper.isEmpty(fromId)) throw new Exception("备货容器条码不能为空");
            if (UtilHelper.isEmpty(packLoc)) throw new Exception("分装间不能为空");

            HashMap<String,String> lotxlocxidHashMap = LotxLocxId.findAvailInvById(context,conn,fromId,true,true);

            HashMap<String,String> orderLineHashMap = Orders.findOrderDetailByKey(context,conn,orderKey,orderLineNumber,true);

            String currentPackLoc = orderLineHashMap.get("SUSR3");

            if(type.equals(ADD_LPN)){

                if(!orderLineHashMap.get("SKU").equals(lotxlocxidHashMap.get("SKU")))
                    throw new Exception("备货物料"+lotxlocxidHashMap.get("SKU")+"和订单行物料"+orderLineHashMap.get("SKU")+"不符");

                HashMap<String,String> skuHashMap = SKU.findById(context,conn,orderLineHashMap.get("SKU"),true);

                if(!skuHashMap.get("PACKKEY").equals(lotxlocxidHashMap.get("LOTTABLE01")))
                    throw new Exception("当前备货物料的包装为"+lotxlocxidHashMap.get("LOTTABLE01")+",而非正常包装"+skuHashMap.get("PACKKEY")+"，请选择其他容器备货");

                if (UtilHelper.isEmpty(currentPackLoc)) {

                    //检查分装间是否已被占用
                    String SQL="SELECT * FROM ORDERDETAIL OD WHERE OD.STATUS <> '95' AND OD.SUSR3 = ? ";
                    HashMap<String,String> rec= DBHelper.getRecord(context, conn, SQL, new Object[]{ packLoc},"",false);

                    if(rec!=null) throw new Exception("分装间"+packLoc+"已被订单号:"+rec.get("ORDERKEY")+" 行号:"+rec.get("ORDERLINENUMBER")+" 使用");


                    //在备货开始前不允许有剩余库存遗留在分装库位。必须将分装剩余库存移出后才允许开始为下一订单行进行备货。
                    String locSkuIDCount = DBHelper.getValue(context,conn,
                            "SELECT COUNT(1) FROM LOTXLOCXID WHERE QTY>0 AND LOC = ? ",
                            new Object[]{   packLoc},    String.class,null);

                    if(!locSkuIDCount.equals("0")) throw new Exception("分装间"+packLoc+"存在非当前订单行的剩余物料遗留在分装间，请将其移出后再进行分装");


                    DBHelper.executeUpdate(context, conn,
                            "UPDATE ORDERDETAIL SET SUSR3 = ? WHERE ORDERKEY = ? AND ORDERLINENUMBER = ? ", new Object[]{
                                    packLoc, orderKey, orderLineNumber
                            });

                } else {

                    if (!currentPackLoc.equals(packLoc))
                        throw new Exception("当前订单行已开始备货，分装间为" + currentPackLoc + ",分装完成前不允许修改");

                }



                DBHelper.executeUpdate(context, conn,
                        "UPDATE IDNOTES SET LOCBEFOREPACK = ? WHERE ID = ? ", new Object[]{
                                lotxlocxidHashMap.get("LOC"), fromId
                        });

                HashMap<String,Object> paramHashMap = new HashMap<String,Object>();
                paramHashMap.put("OPNAME",opName);
                paramHashMap.put("FROMID",fromId);
                paramHashMap.put("FROMLOC",lotxlocxidHashMap.get("LOC"));
                paramHashMap.put("TOLOC",packLoc);
                paramHashMap.put("SKU",lotxlocxidHashMap.get("SKU"));

                ServiceHelper.executeService(context,"EHMove", new ServiceDataHolder(new ServiceDataMap(paramHashMap)));

            }else if(type.equals(REMOVE_LPN)){
                //ORDERDETAIL.SUSR1 当前进行中的分装入库单号
                if(!UtilHelper.isEmpty(orderLineHashMap.get("SUSR1")))
                    ExceptionHelper.throwRfFulfillLogicException("分装间已经开始分装，不允许取消备货");

                if(lotxlocxidHashMap == null || !lotxlocxidHashMap.get("LOC").equals(currentPackLoc))
                    throw new Exception("分装间"+currentPackLoc+"未找到备货容器"+fromId);

                HashMap<String,String> idnotesHashMap = IDNotes.findById(context,conn,fromId,true);
                if(UtilHelper.isEmpty(idnotesHashMap.get("LOCBEFOREPACK"))) throw new Exception("未找到容器备货前的存储库位，请使用移动功能将该容器移出分装间");



                HashMap<String,Object> moveParamsHashMap = new HashMap<String,Object>();
                moveParamsHashMap.put("OPNAME",opName);
                moveParamsHashMap.put("FROMID",fromId);
                moveParamsHashMap.put("FROMLOC",currentPackLoc);
                moveParamsHashMap.put("TOLOC",idnotesHashMap.get("LOCBEFOREPACK"));
                moveParamsHashMap.put("SKU",idnotesHashMap.get("SKU"));

                ServiceHelper.executeService(context,"EHMove",
                        new ServiceDataHolder(new ServiceDataMap(moveParamsHashMap)));

                //检查是否分装间已没有备货容器，如果没有自动释放占用的分装间
                String locSkuIDCount = DBHelper.getValue(context,conn,
                        "SELECT COUNT(1) FROM LOTXLOCXID WHERE LOC = ? AND SKU = ? AND QTY>0",
                        new Object[]{
                                currentPackLoc,
                                orderLineHashMap.get("SKU")},
                        String.class,null);

                if(locSkuIDCount.equals("0")){

                    DBHelper.executeUpdate(context, conn,
                            "UPDATE ORDERDETAIL SET SUSR3 = null WHERE ORDERKEY = ? AND ORDERLINENUMBER = ? ", new Object[]{
                                   orderKey, orderLineNumber
                            });

                }

            }

          
        }
        catch (Exception e)
        {
            try	{	context.releaseConnection(conn); }	catch (Exception e1) {		}
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());
        }finally {
            try	{	context.releaseConnection(conn); }	catch (Exception e1) {		}
        }

    }

}