/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 *
 *******************************************************************************/

package com.enhantec.wms.core.inventory;


import com.enhantec.framework.common.exception.EHApplicationException;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.framework.common.utils.EHDateTimeHelper;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.outbound.PickDetail;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.utils.common.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
public class MoveService extends WMSBaseService {

    /**
     * 支持三种移动方式：
     * 1.标准移动：只允许移动托盘可用量至目标库位容器。
     * 2.拣货：移动的数量为拣货量，移动的同时更新分配和拣货量。
     * 3.移动已被分配或部分分配的整托盘：LPN会被移动，同时移动的分配量会被同时更新对应的拣货明细记录。（今的设计中LPN为最小颗粒度，不允许混批次，拣货明细中只记录LPN，不记录批次和库位，因此移动LPN后不影响后续的拣货操作）
     * @param serviceDataHolder
     */
    public void execute(ServiceDataHolder serviceDataHolder){

        String storerKey = serviceDataHolder.getInputDataAsMap().getString("storerKey");
        String fromId = serviceDataHolder.getInputDataAsMap().getString("fromId");
        //toId为空：当为整容器移动时代表目标容器仍为原容器，当对容器中的部分数量做移动时提示错误，子容器直接传入toId，这里为核心逻辑，不做LPN生成。
        String toId = serviceDataHolder.getInputDataAsMap().getString("toId");
        String toLoc = serviceDataHolder.getInputDataAsMap().getString("toLoc");
        BigDecimal qtyToBeMoved = serviceDataHolder.getInputDataAsMap().getDecimalValue("qty");

        String pickDetailKey = serviceDataHolder.getInputDataAsMap().getString("pickDetailKey");//是否为拣货操作
        boolean allowOverPick = serviceDataHolder.getInputDataAsMap().getBoolean("allowOverPick");//是否允许超拣

        // 默认允许移动带分配量或拣货量的整容器
        Boolean allowMoveAllocOrPickedId = serviceDataHolder.getInputDataAsMap().getBoolean("allowMoveAllocOrPickedId");
        allowMoveAllocOrPickedId =  allowMoveAllocOrPickedId == null ? true : allowMoveAllocOrPickedId ;

        if(UtilHelper.isEmpty(fromId)) throw new FulfillLogicException("移动的源容器号不能为空");

        Map<String,String> fromIdHashMap = LotxLocxId.findWithoutCheckIDNotes(storerKey, fromId,true);

        String sku = fromIdHashMap.get("SKU");
        BigDecimal qtyInFromId = new BigDecimal(fromIdHashMap.get("QTY"));
        BigDecimal qtyAvailableInFromId = new BigDecimal(fromIdHashMap.get("AVAILABLEQTY"));
        BigDecimal qtyAllocatedInFromId = new BigDecimal(fromIdHashMap.get("QTYALLOCATED"));
        BigDecimal qtyPickedInFromId = new BigDecimal(fromIdHashMap.get("QTYPICKED"));
        String fromLoc = fromIdHashMap.get("LOC");
        boolean fromIdStatus ="1".equals(fromIdHashMap.get("STATUS"));
        String lot = fromIdHashMap.get("LOT");

        boolean isFullLpnMove = qtyToBeMoved.compareTo(qtyInFromId) == 0;

        if(UtilHelper.isEmpty(toId)){
            if(isFullLpnMove){
                toId = fromId;
            }else {
                toId = IdGenerationHelper.generateIDByKeyName("CASEID",10);
            }
        }

        if (qtyToBeMoved.compareTo(qtyInFromId)>0) {
            throw new EHApplicationException("容器" + fromId + "的数量小于请求的移动数量");
        }

        if(fromIdHashMap.get("LOC").equals(toLoc) && fromId == toId)
            throw new EHApplicationException("容器已在目标库位，不需要移动");


        Map<String,String> toIdHashMap = toId == null ? null : LotxLocxId.findWithoutCheckIDNotes(storerKey, toId,false);

        if(toIdHashMap != null && toIdHashMap.get("LOT").equals(fromIdHashMap.get("LOT"))){
            throw new EHApplicationException("目标容器的批和待移动的容器批次不同，不允许移动");

        }

        boolean isFromIdHold = 0 < DBHelper.getCount("SELECT COUNT(1) FROM INVENTORYHOLD WHERE HOLD = 1 AND ID = ? ", new String[]{fromId});

        boolean isToIdHold = 0 < DBHelper.getCount("SELECT COUNT(1) FROM INVENTORYHOLD WHERE HOLD = 1 AND ID = ? ", new String[]{toId});

        boolean isFromLocHold = 0 < DBHelper.getCount("SELECT COUNT(1) FROM INVENTORYHOLD WHERE HOLD = 1 AND LOC = ? ", new String[]{fromLoc});

        boolean isToLocHold = 0 < DBHelper.getCount("SELECT COUNT(1) FROM INVENTORYHOLD WHERE HOLD = 1 AND LOC = ? ", new String[]{toIdHashMap.get("LOC")});

        boolean isLotHold = 0 < DBHelper.getCount("SELECT COUNT(1) FROM INVENTORYHOLD WHERE HOLD = 1 AND LOT = ? ", new String[]{fromIdHashMap.get("LOT")});

        BigDecimal qtyAllocChangeFromId;
        BigDecimal qtyPickChangeFromId;
        BigDecimal qtyAllocChangeToId;
        BigDecimal qtyPickChangeToId;

        if(pickDetailKey!=null){
            //拣货操作
            Map<String, String> pickDetailHashMap = PickDetail.findByPickDetailKey(pickDetailKey,true);
            Map<String, Object> pdIdHashMap = LotxLocxId.findRawRecordWithoutCheckIDNotes(storerKey,pickDetailHashMap.get("ID"),true);
            if(!pickDetailHashMap.get("ID").equals(fromId))
            {
                if(!pdIdHashMap.get("LOT").equals(fromIdHashMap.get("LOT"))) {
                    throw new EHApplicationException("拣货明细中的容器号" + pickDetailHashMap.get("ID") + "和当前拣货的容器" + fromId + "的批次不相同，不允许替换");
                }
            }

            BigDecimal pdQty = (BigDecimal) pdIdHashMap.get("QTY");
            if(pdQty.compareTo(qtyAllocatedInFromId)>0){
                throw new EHApplicationException("数量异常:拣货明细待拣货数量" + pdQty + "大于和当前拣货容器" + fromId + "的分配量"+ qtyAllocatedInFromId);
            }

            qtyAllocChangeFromId = pdQty;
            qtyPickChangeFromId = BigDecimal.ZERO;
            qtyAllocChangeToId = BigDecimal.ZERO;


            BigDecimal maxPickQty = pdQty.add(qtyAvailableInFromId);
            if(qtyToBeMoved.compareTo(maxPickQty)>0){
                throw new EHApplicationException("拣货量" + qtyToBeMoved + "大于当前拣货容器" + fromId + "的最大可拣货量(分配量+可用量)"+ maxPickQty);
            }else{
                //判断是否能超拣，不能报错
                if(!allowOverPick && qtyToBeMoved.compareTo(pdQty)>0){
                    throw new EHApplicationException("拣货量" + qtyToBeMoved + "大于拣货明细的分配量"+pdQty+",系统设置不允许超额拣货");
                }
            }

            qtyPickChangeToId = qtyToBeMoved;

            //更新拣货明细和任务明细为已拣货
            DBHelper.executeUpdate("UPDATE PICKDETAIL SET QTY = ?, DROPID = ?, TOLOC = ?, STATUS = ? , EDITWHO = ?,EDITDATE = ? WHERE STORERKEY = ? and PICKDETAILKEY = ? ",
                    new Object[]{qtyToBeMoved, toId, toLoc, "5", EHContextHelper.getUser().getUsername(), EHDateTimeHelper.getCurrentDate(), storerKey, pickDetailKey});
            DBHelper.executeUpdate(
                    "UPDATE TASKDETAIL SET  QTY = ?, TOID = ?, TOLOC = ?, STATUS = ?, USERKEY = ? , EDITWHO = ?,EDITDATE = ? WHERE STORERKEY = ? and PICKDETAILKEY = ?",
                    new Object[]{qtyToBeMoved, toId, toLoc,  "9", EHContextHelper.getUser().getUsername(), EHContextHelper.getUser().getUsername(), EHDateTimeHelper.getCurrentDate(), storerKey, pickDetailKey });

        }else {
            //标准移动
            if (isFullLpnMove) {
                //整容器移动支持两种场景：
                //1.在目标容器号等于原容器号的情况下：表示沿用原容器号，无需合并到其他容器，这种情况下，允许任意移动。
                //2.在目标容器号不等于原容器号的情况下：则判断原容器是否存在分配量和拣货量，如不存在则可以移动，否则会导致ID和PICKDETAIL不一致，不允许移动。
                if(qtyAllocatedInFromId.compareTo(BigDecimal.ZERO) > 0 || qtyPickedInFromId.compareTo(BigDecimal.ZERO) > 0){

                    if(!allowMoveAllocOrPickedId)
                        throw new EHApplicationException("容器" + fromId + "存在分配量或拣货量，不允许移动");
                    if (fromId != toId)
                        throw new EHApplicationException("容器" + fromId + "存在分配量或拣货量，不允许更换容器号");

                }


                qtyAllocChangeFromId = qtyAllocatedInFromId;
                qtyPickChangeFromId = qtyPickedInFromId;
                qtyAllocChangeToId = qtyAllocatedInFromId;
                qtyPickChangeToId = qtyPickedInFromId;

            }else
            {
                //部分容器数量移动，只允许移动可用库存
                if (qtyToBeMoved.compareTo(qtyAvailableInFromId) > 0)
                    throw new EHApplicationException("容器" + fromId + "的可用量数量小于请求的移动数量");

                qtyAllocChangeFromId = BigDecimal.ZERO;
                qtyPickChangeFromId = BigDecimal.ZERO;
                qtyAllocChangeToId = BigDecimal.ZERO;
                qtyPickChangeToId = BigDecimal.ZERO;
            }
        }

        //更新LOTCLOCXID表中的库存数据
        DBHelper.executeUpdate("UPDATE LOTXLOCXID SET QTY = QTY - ?, QTYALLOCATED = QTYALLOCATED - ?,QTYPICKED = QTYPICKED - ?,EDITWHO = ?,EDITDATE = ? WHERE STORERKEY = ？AND LOC =? AND ID = ?"
                , new Object[]{qtyToBeMoved, qtyAllocChangeFromId, qtyPickChangeFromId, EHContextHelper.getUser().getUsername(), EHDateTimeHelper.getCurrentDate(), storerKey, fromIdHashMap.get("LOC"), fromId});

        //更新后检查ID的数量，如果为0直接删除（不直接删除的原因是避免并发的问题）
        DBHelper.executeUpdate("DELETE FROM LOTXLOCXID WHERE QTY = 0, STORERKEY = ？and ID = ? ", new Object[]{storerKey, fromId});

        boolean toIdStatus = isFromIdHold || isToLocHold || isLotHold;

        if (toIdHashMap == null) {
            Map<String,Object> lotxlocxid = new HashMap<>();
            lotxlocxid.put("STORERKEY", storerKey);
            lotxlocxid.put("LOT", fromIdHashMap.get("LOT"));
            lotxlocxid.put("LOC", toLoc);
            lotxlocxid.put("ID", toId);
            lotxlocxid.put("SKU", sku);
            lotxlocxid.put("QTY", BigDecimal.ZERO);
            lotxlocxid.put("QTYALLOCATED", BigDecimal.ZERO);
            lotxlocxid.put("QTYPICKED", BigDecimal.ZERO);
            lotxlocxid.put("STATUS", toIdStatus);
            lotxlocxid.put("ADDWHO", EHContextHelper.getUser().getUsername());
            lotxlocxid.put("EDITWHO", EHContextHelper.getUser().getUsername());
            DBHelper.insert( "LOTXLOCXID", lotxlocxid);
        }

        //TODO: 暂不对原LPN和新LPN冻结状态的一致性做校验，冻结状态暂以目标容器为准。相关逻辑可现在项目代码中实现，后续有必要再考虑抽离到核心逻辑。
        DBHelper.executeUpdate("UPDATE LOTXLOCXID SET QTY = QTY + ?, QTYALLOCATED = QTYALLOCATED + ?,QTYPICKED = QTYPICKED + ?, EDITWHO = ?,EDITDATE = ? WHERE STORERKEY = ? AND LOC = ? AND ID = ?"
                    , new Object[]{qtyToBeMoved,qtyAllocChangeToId, qtyPickChangeToId, EHContextHelper.getUser().getUsername(),EHDateTimeHelper.getCurrentDate(),storerKey, toId});

        //TODO: 仅保留用于和INFORWMS兼用性，后续会删除ID表
        //更新ID表中的库存数据
        if(DBHelper.getCount("SELECT COUNT(*) FROM ID WHERE ID=?", new String[]{fromId})==0)
            throw new EHApplicationException("ID表未找到容器"+fromId);

        DBHelper.executeUpdate("UPDATE ID SET QTY=QTY-?,EDITWHO=?,EDITDATE=? WHERE ID=?", new Object[]{qtyToBeMoved, EHContextHelper.getUser().getUsername(), EHDateTimeHelper.getCurrentDate(), fromId});

        if (DBHelper.getCount("SELECT COUNT(1) FROM ID WHERE ID = ?", new String[]{toId})==0)
        {
            Map<String,Object> id = new HashMap<>();
            id.put("addwho", EHContextHelper.getUser().getUsername());
            id.put("editwho", EHContextHelper.getUser().getUsername());
            id.put("ID", toId);
            id.put("QTY", qtyToBeMoved);
            id.put("STATUS", toIdStatus);
            id.put("PACKKEY", fromIdHashMap.get("PACKKEY"));
            DBHelper.insert("ID", id);
        }
        else{
            DBHelper.executeUpdate( "UPDATE ID SET QTY=QTY+?,EDITWHO=?,EDITDATE=? WHERE ID=?",
                    new Object[]{qtyToBeMoved,EHContextHelper.getUser().getUsername(),EHDateTimeHelper.getCurrentDate(),toId});
        }

        //TODO: 仅保留用于和INFORWMS兼用性，后续会删除SKUXLOC表
        //更新SKUXLOC表中的库存数据
        if (DBHelper.getCount("SELECT COUNT(1) FROM SKUXLOC WHERE STORERKEY = ? AND LOC= ? AND SKU=?", new String[]{storerKey, fromLoc,sku})==0)
            throw new EHApplicationException("SKUXLOC表未找到数据("+fromLoc+","+storerKey+","+sku+")");

        DBHelper.executeUpdate("UPDATE SKUXLOC SET QTY=QTY-?,QTYALLOCATED = QTYALLOCATED-?,QTYPICKED=QTYPICKED-?,EDITWHO=?,EDITDATE=? WHERE STORERKEY=? AND LOC=? AND SKU=?"
                , new Object[]{qtyToBeMoved,qtyAllocChangeFromId,qtyPickChangeFromId,EHContextHelper.getUser().getUsername(),EHDateTimeHelper.getCurrentDate(),storerKey,fromLoc,sku});

        if (DBHelper.getCount("SELECT COUNT(1) FROM SKUXLOC WHERE STORERKEY=? AND LOC=? AND SKU=?", new String[]{storerKey, toLoc,sku})==0)
        {
            Map<String,Object> skuxLocRec = new HashMap<>();
            skuxLocRec.put("STORERKEY", storerKey);
            skuxLocRec.put("SKU", sku);
            skuxLocRec.put("LOC", toLoc);
            skuxLocRec.put("QTY", qtyToBeMoved);
            skuxLocRec.put("QTYALLOCATED", qtyAllocChangeToId);
            skuxLocRec.put("QTYPICKED", qtyPickChangeToId);
            //SKUXLOC.put("LOCATIONTYPE", LOCATIONTYPE);
            skuxLocRec.put("LOCATIONTYPE", "OTHER");//给默认值，仅用于满足INFOR WMS兼容性。
            skuxLocRec.put("ADDWHO", EHContextHelper.getUser().getUsername());
            skuxLocRec.put("EDITWHO", EHContextHelper.getUser().getUsername());
            DBHelper.insert("SKUXLOC", skuxLocRec);
        }
        else{
            DBHelper.executeUpdate("UPDATE SKUXLOC SET QTY=QTY+?,QTYALLOCATED=QTYALLOCATED+?,QTYPICKED=QTYPICKED+?,EDITWHO=?,EDITDATE=? WHERE STORERKEY=? AND LOC=? AND SKU=?"
                    , new Object[]{qtyToBeMoved,qtyAllocChangeToId,qtyPickChangeToId,EHContextHelper.getUser().getUsername(),EHDateTimeHelper.getCurrentDate(),storerKey,toLoc,sku});
        }

        //更新LOT表中的库存数据
        if (DBHelper.getCount("SELECT COUNT(1) FROM LOT WHERE LOT = ?", new String[]{lot})==0)
            throw new EHApplicationException("未找到批次"+lot);

        //LOT总量不会变化，分配量和拣货量需要根据实际情况调整
        DBHelper.executeUpdate("UPDATE LOT SET QTYALLOCATED = QTYALLOCATED-?+?,QTYPICKED=QTYPICKED-?+?,EDITWHO=?,EDITDATE=? WHERE LOT=?"
                , new Object[]{qtyToBeMoved,qtyAllocChangeFromId,qtyAllocChangeToId,qtyPickChangeFromId,qtyPickChangeToId,EHContextHelper.getUser().getUsername(),EHDateTimeHelper.getCurrentDate(),lot});

        if (fromIdStatus != toIdStatus)
        {
            //TODO: 当前实现性能上不是最优，后续应改为直接根据fromid-toid的数量并考虑fromIdStatus、toIdStatus来更新冻结数量
            BigDecimal QtyOnHold = DBHelper.getDecimalValue("SELECT SUM(QTY) FROM LOTXLOCXID WHERE STATUS='HOLD' AND LOT=?", new Object[]{lot});
            DBHelper.executeUpdate("UPDATE LOT SET QTYONHOLD=?,EDITWHO=?,EDITDATE=? WHERE LOT=?", new Object[]{QtyOnHold,EHContextHelper.getUser().getUsername(),EHDateTimeHelper.getCurrentDate(),lot});
        }
    }
}
