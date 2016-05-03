package com.acuman.domain;

import com.couchbase.client.java.document.json.JsonObject;

import java.util.Collections;
import java.util.Set;

public class TagAndWords {
    private String tagName;
    private Set<JsonObject> words;

    public TagAndWords() {
    }

    public TagAndWords(String tagName, Set<JsonObject> words) {
        this.tagName = tagName;
        this.words = words;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Set<JsonObject> getWords() {
        return Collections.unmodifiableSet(words);
    }

    public void setWords(Set<JsonObject> words) {
        this.words = words;
    }

    public void addWord(JsonObject word) {
        words.add(word);
    }
}
