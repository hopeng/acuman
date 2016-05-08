package com.acuman.domain;

import com.acuman.CbDocType;

import java.util.List;

/**
 * todo extend auditable
 */
public class WordNode {
    private String wordNodeId;
    private String type = CbDocType.WordNode;
    private String wordId;

    private List<String> childWordId;

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
        return wordNodeId;
    }

    public void setWordNodeId(String wordNodeId) {
        this.wordNodeId = wordNodeId;
    }

    public String getType() {
        return type;
    }
}
