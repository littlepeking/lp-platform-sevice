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

public class JsonDataSerializer<T> implements IDataSerializer<T> {

    final static private ObjectMapper om = new ObjectMapper().registerModule(new JavaTimeModule());

    public String serializeData(T data) throws JsonProcessingException {

        ObjectWriter ow = om.writer().withDefaultPrettyPrinter();
        String jsonString = ow.writeValueAsString(data);

        return jsonString;
    }

    public T deserializeData(String data) throws JsonProcessingException {
        Class<T> type = (Class<T>) GenericTypeResolver.resolveTypeArgument(getClass(), JsonDataSerializer.class);

        T readDto = om.readValue(data, type);

        return  readDto;
    }

}
