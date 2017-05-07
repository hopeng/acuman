package com.acuman.service;

import com.acuman.domain.TagAndWords;
import com.acuman.domain.UiWordNode;
import com.acuman.domain.WordNode;
import com.acuman.domain.ZhEnWord;
import com.couchbase.client.java.document.json.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * Created by hopeng on 1/05/2016.
 */
public interface TcmDictService {
    JsonObject newWord(String json);

    JsonObject newWord(JsonObject word);

    ZhEnWord enrichAndSaveZhEnWord(String chinese, String english);

    ZhEnWord enrichAndSaveZhEnWord(ZhEnWord zhEnWord);

    WordNode newWordNode(String tagName, List<ZhEnWord> childWords);

    JsonObject getWord(String mid);

    boolean hasWord(String mid);

    ZhEnWord exactWordMatch(String csWord);

    JsonObject deleteWord(String mid);

//    List<JsonObject> lookupCustomWord(String word, int limit);

    void addWordTag(String id, String tag);

    void removeTag(String id, String tag);

    Map<String, TagAndWords> getTagsAndWords();

    UiWordNode buildWordTree();

}
