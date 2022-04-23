package com.enhantec.common.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
}
