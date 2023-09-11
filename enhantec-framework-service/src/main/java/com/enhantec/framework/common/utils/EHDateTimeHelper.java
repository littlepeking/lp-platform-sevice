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

import java.time.*;

public class EHDateTimeHelper {

   public static LocalDateTime timeStamp2LocalDateTime(Object val){
        long timestamp = (long) val;
        if (timestamp > 0) {
            //return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);
        } else {
            return null;
        }
    }

    public static String getCurrentDateStr(){
        return LocalDateTime.now().toString();
    }

    public static LocalDateTime getCurrentDate(){
        return LocalDateTime.now();
    }

    public static LocalDateTime getCurrentDate11AM(){
        return LocalDateTime.of(LocalDate.now(), LocalTime.of(11, 0));
    }
}
