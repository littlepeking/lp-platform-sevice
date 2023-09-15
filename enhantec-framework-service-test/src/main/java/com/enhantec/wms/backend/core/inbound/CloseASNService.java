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

package com.enhantec.wms.backend.core.inbound;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.framework.common.utils.EHDateTimeHelper;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.utils.common.DBHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service("core.inbound.closeASN")
@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
public class CloseASNService extends WMSBaseService {

    public void execute(ServiceDataHolder serviceDataHolder){

        String receiptKey = serviceDataHolder.getInputDataAsMap().getString("RECEIPTKEY");

        //todo: crossdock
        //   if (this.isCrossdockAllocationOnASNClose(receiptKey)) {
        //         this.validateOpenQty(receiptKey);
        //         this.runCrossDockProcess(receiptKey);
        //   }

        updateASNHeaderStatus(receiptKey, "11");
        updateASNDetailStatus(receiptKey, "*", "11");

    }

    public void updateASNHeaderStatus(String receiptKey, String pStatus) {
            if (pStatus.equalsIgnoreCase("11")) {

                    DBHelper.executeUpdate("UPDATE RECEIPT SET Status = ?, ClosedDate = ?, EditDate = ?, EditWho = ? where Receiptkey = ?",
                            new Object[]{
                                    pStatus,
                                    EHDateTimeHelper.getCurrentDate(),
                                    EHDateTimeHelper.getCurrentDate(),
                                    EHContextHelper.getUser().getUsername(),
                                    receiptKey
                            });

            } else if (pStatus.equalsIgnoreCase("15")) {
                DBHelper.executeUpdate("UPDATE RECEIPT SET Status = ?, EditDate = ?, EditWho = ?, VERIFIEDCLOSEDDATE = ? where Receiptkey = ?",
                        new Object[]{
                                pStatus,
                                EHDateTimeHelper.getCurrentDate(),
                                EHContextHelper.getUser().getUsername(),
                                EHDateTimeHelper.getCurrentDate(),
                                receiptKey
                        });
            } else {

                DBHelper.executeUpdate("UPDATE RECEIPT SET Status = ?, EditDate = ?, EditWho = ? where Receiptkey = ?" ,
                        new Object[]{
                                pStatus,
                                EHDateTimeHelper.getCurrentDate(),
                                EHContextHelper.getUser().getUsername(),
                                receiptKey
                });

            }
    }


    public void updateASNDetailStatus(String receiptKey, String receiptLinenumber, String pStatus) {

        if (receiptLinenumber != null && receiptLinenumber.equals("*")) {
            DBHelper.executeUpdate("UPDATE RECEIPTDETAIL SET Status = ?, EditDate = ?, EditWho = ? where Receiptkey = ?", new Object[]{
                    pStatus,
                    EHDateTimeHelper.getCurrentDate(),
                    EHContextHelper.getUser().getUsername(),
                    receiptKey
            });

        } else {
            DBHelper.executeUpdate("UPDATE RECEIPTDETAIL SET Status = ?, EditDate = ?, EditWho = ? where Receiptkey = ? AND Receiptlinenumber = ?",
                    new Object[]{
                            pStatus,
                            EHDateTimeHelper.getCurrentDate(),
                            EHContextHelper.getUser().getUsername(),
                            receiptKey,
                            receiptLinenumber
                    });
        }
    }
}
