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

package com.enhantec.wms.backend.common.outbound;

import com.enhantec.wms.backend.utils.common.DBHelper;

import java.util.List;
import java.util.Map;

public class AllocationStrategy {


    public static Map<String, String> findByKey(String allocationStrategyKey) {

        String SQL="SELECT * FROM NEWALLOCATIONSTRATEGY WHERE ALLOCATESTRATEGYKEY = ? ";

        Map<String,String> records= DBHelper.getRecord( SQL, new Object[]{ allocationStrategyKey},"分配策略",true);

        return records;
    }


    public static List<Map<String, String>> findAllocStrategyDetailsByKey(String allocationStrategyKey) {

        String SQL="SELECT * FROM NEWALLOCATIONSTRATEGYDETAIL WHERE NEWSTRATEGYKEY = ? ";

        List<Map<String,String>> records = DBHelper.executeQuery( SQL, new Object[]{ allocationStrategyKey});

        return records;
    }
}
