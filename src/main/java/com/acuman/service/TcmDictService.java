package com.acuman.service;

import com.couchbase.client.java.document.json.JsonObject;
import org.eclipse.jetty.util.MultiMap;

import java.util.List;

/**
 * Created by hopeng on 1/05/2016.
 */
public interface TcmDictService {
    JsonObject newWord(String json);

    JsonObject newWord(JsonObject word);

    JsonObject getWord(String mid);

    boolean hasWord(String mid);

    JsonObject deleteWord(String mid);

    List<JsonObject> lookupWord(String word, int limit);

    void addTag(String id, JsonObject jsonObject, String tag);

    void removeTag(String id, JsonObject jsonObject, String tag);

    List<JsonObject> searchByTag(String tag);

    MultiMap<JsonObject> listTags();
}
