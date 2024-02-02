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

package com.enhantec.wms.backend.common.inventory;

import com.enhantec.wms.backend.utils.common.DBHelper;

import java.util.Map;

public class Lot {

    public static Map<String,Object> findById(String lot){

        return DBHelper.getRawRecord("SELECT *, QTY-QTYPICKED-QTYALLOCATED-QTYPREALLOCATED-QTYONHOLD QTYAVAIL,QTY-QTYPICKED-QTYALLOCATED-QTYPREALLOCATED QTYAVAILWITHHOLD FROM LOT WHERE LOT = ? ", new Object[]{lot},"批次",false);

    }


}
