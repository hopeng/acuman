package com.acuman.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Serialize Object into JSON string and deserialize JSON string to Java object
 */
public final class JSONConverter {
    private static final ObjectMapper mapper = new ObjectMapper();

    private JSONConverter() {
    }

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("error serialising object to json string", e);
        }
    }

    public static <T> T fromJson(String jsonContent, Class<T> valueType) {
        if (jsonContent == null) {
            return null;
        }

        try {
            return mapper.readValue(jsonContent, valueType);

        } catch (IOException e) {
            throw new RuntimeException("error deserialising json string to object " + valueType, e);
        }
    }

    public static <T> List<T> fromJsonArray(String jsonContent, Class<T> valueType) {
        if (StringUtils.isEmpty(jsonContent)) {
            return Collections.emptyList();
        }

        try {
            return mapper.readValue(jsonContent,
                    mapper.getTypeFactory().constructCollectionType(List.class, valueType));

        } catch (IOException e) {
            throw new RuntimeException("error deserialising json string to list of " +  valueType, e);
        }
    }
}
