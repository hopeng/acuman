package com.acuman.util;

import com.couchbase.client.deps.com.fasterxml.jackson.core.JsonProcessingException;
import com.couchbase.client.deps.com.fasterxml.jackson.core.type.TypeReference;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.ObjectMapper;
import com.couchbase.client.deps.com.fasterxml.jackson.databind.type.CollectionType;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.transcoder.JacksonTransformers;
import com.google.common.base.Ascii;
import spark.utils.Assert;

import java.io.IOException;
import java.util.List;

/**
 * todo use convert array
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = JacksonTransformers.MAPPER;

    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("error serializing object " + obj, e);
        }
    }

    public static <T> T fromJson(JsonObject jsonObject, Class<T> type) {
        return jsonObject == null ? null : fromJson(jsonObject.toString(), type);
    }

    public static <T> T fromJson(JsonObject jsonObject, TypeReference<T> typeReference) {
        return jsonObject == null ? null : fromJson(jsonObject.toString(), typeReference);

    }

    public static <T> T fromJson(String json, Class<T> type) {
        Assert.notNull(json);
        try {
            return MAPPER.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException("error deserializing json: " + Ascii.truncate(json, 100, ".."), e);
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("error deserializing json: " + Ascii.truncate(json, 100, ".."), e);
        }
    }

    public static <T> List<T> fromJsonArray(String jsonArray, Class<T> type) {
        Assert.notNull(jsonArray);
        try {
            CollectionType valueType = MAPPER.getTypeFactory().constructCollectionType(List.class, type);
            return MAPPER.readValue(jsonArray, valueType);
        } catch (IOException e) {
            throw new RuntimeException("error deserializing json array: " + Ascii.truncate(jsonArray, 100, ".."), e);
        }

    }
}
