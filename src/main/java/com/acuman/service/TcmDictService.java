package com.acuman.service;

import com.acuman.domain.TagAndWords;
import com.couchbase.client.java.document.json.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * Created by hopeng on 1/05/2016.
 */
public interface TcmDictService {
    JsonObject newWord(String json);

    JsonObject newWord(JsonObject word);

    JsonObject newCustomWord(JsonObject word);

    JsonObject getWord(String mid);

    boolean hasWord(String mid);

    public JsonObject exactCustomWord(String csWord);

    JsonObject deleteWord(String mid);

    List<JsonObject> lookupCustomWord(String word, int limit);

    void addWordTag(String id, String tag);

    void removeTag(String id, String tag);

    Map<String, TagAndWords> getTagsAndWords();
}
