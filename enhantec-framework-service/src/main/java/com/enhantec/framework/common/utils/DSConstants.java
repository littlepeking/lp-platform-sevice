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



package com.enhantec.framework.common.utils;

public interface DSConstants {

    static String DS_ADMIN = "admin";
    //To choose a db schema for DB operation based on http header 'orgId',replaced by DS_ORG
    //static String DS_HEADER_ORG = "#header.orgId";

    static String DS_PARAM = "#dataSource";

    //To choose a db schema for DB operation based on datasource of EHRequestContext
    static String DS_DEFAULT = "#DS_DEFAULT";

}
