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