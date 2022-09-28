/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS
 *             CONFIDENTIAL INFORMATION OF Shanghai Enhantec Information
 *             Technology Co., Ltd. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED.
 *
 *             (c) COPYRIGHT 2022 Enhantec. ALL RIGHTS RESERVED.
 *
 *******************************************************************************/

/**
 * Author: John Wang
 * john.wang_ca@hotmail.com
 */



package com.enhantec.common.utils;

import com.enhantec.common.model.EHTreeModel;

import java.util.List;

public class EHTreeHelper{

       public static Boolean evaluateOrgCheckStatus(List<EHTreeModel> organizationList) {

        boolean childrenContainsUnSelected = organizationList.stream().anyMatch(org -> compareBoolValue( org.getCheckStatus() ,false));
        boolean childrenContainsSelected = organizationList.stream().anyMatch(org -> compareBoolValue(org.getCheckStatus() , true));
        boolean childrenContainsSemiSelected = organizationList.stream().anyMatch(org -> compareBoolValue( org.getCheckStatus() , null));

        if (childrenContainsSemiSelected) {
            return null;
        } else if (childrenContainsUnSelected) {
            if (childrenContainsSelected) {
                return null;
            } else {
                return false;
            }

        } else {
            //!childrenContainsUnSelected
            return true;
        }

    }

    public static boolean compareBoolValue(Boolean b1,Boolean b2){

        if(b1==null && b2==null){
            return true;
        }else if(b1==null || b2==null){
            return false;
        }else return b1==b2;
    }


    public static void recursivelyCalculateCheckStatus(EHTreeModel treeNode) {

        if (treeNode.getChildren() == null || treeNode.getChildren().size() == 0) {
            return;//if leaf node, then value should be used directly
        } else {
            ((List<EHTreeModel>) treeNode.getChildren()).forEach(p -> recursivelyCalculateCheckStatus(p));

            treeNode.setCheckStatus(EHTreeHelper.evaluateOrgCheckStatus(treeNode.getChildren()));

        }



    }


}
