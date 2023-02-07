package com.enhantec.framework.common.utils.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.core.GenericTypeResolver;

/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */


public class JsonDataSerializer {
    private static final ObjectMapper om = (new ObjectMapper()).registerModule(new JavaTimeModule());

    public static String serializeData(Object data) throws JsonProcessingException {
        ObjectWriter ow = om.writer().withDefaultPrettyPrinter();
        String jsonString = ow.writeValueAsString(data);
        return jsonString;
    }

    public static  <T> T deserializeData(String data, Class<T> clazz) throws JsonProcessingException {
        return om.readValue(data, clazz);
    }
}
