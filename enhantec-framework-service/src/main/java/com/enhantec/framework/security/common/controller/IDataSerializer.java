package com.enhantec.framework.security.common.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.core.GenericTypeResolver;

import java.io.IOException;

/**
 * Copyright (C) 2022, Enhantec
 * All rights reserved.
 * <p>
 * Author: John Wang
 * Email: john.wang_ca@hotmail.com
 */

public interface IDataSerializer<T> {

    public String serializeData(T data) throws IOException;

    public T deserializeData(String data) throws  IOException;

}
