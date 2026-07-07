package com.criczone.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public final class RequestMaps {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private RequestMaps() {
    }

    public static Map<String, Object> toMap(Object request) {
        if (request == null) {
            return null;
        }
        return MAPPER.convertValue(request, new TypeReference<Map<String, Object>>() {});
    }
}
