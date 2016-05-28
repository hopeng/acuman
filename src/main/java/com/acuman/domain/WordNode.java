package com.acuman.domain;

import com.acuman.CbDocType;
import com.acuman.service.couchbase.CouchbaseTcmDictService;

import java.util.LinkedList;
import java.util.List;

/**
 * WordNode is persisted to DB, while UiWordNode is used for UI display
 */
public class WordNode extends Auditable {
    private String type = CbDocType.WordNode;
    private String wordId;  // mid of ZhEnWord

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

    public void setWordNodeId(String ignored) {
    }

    public String getType() {
        return type;
    }
}
