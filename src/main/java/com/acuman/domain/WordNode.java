package com.acuman.domain;

import com.acuman.CbDocType;
import com.acuman.service.couchbase.CouchbaseTcmDictService;

import java.util.LinkedList;
import java.util.List;

/**
 * todo extend auditable
 */
public class WordNode {
    private String type = CbDocType.WordNode;
    private String wordId;
    private ZhEnWord word;  // set to null before persisting

    private List<String> childWordId = new LinkedList<>();

    public WordNode() {
    }

    public WordNode(String wordId, List<String> childWordId) {
        this.wordId = wordId;
        this.childWordId = childWordId;
    }

    public String getWordId() {
        return wordId;
    }

    public List<String> getChildWordId() {
        return childWordId;
    }

    public String getWordNodeId() {
        return CouchbaseTcmDictService.wordNodeId(wordId);
    }

    public ZhEnWord getWord() {
        return word;
    }

    public void setWord(ZhEnWord word) {
        this.word = word;
    }

    public String getType() {
        return type;
    }
}
