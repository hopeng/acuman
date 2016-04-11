package com.macquarie.ofr.dawn.domain;

import java.util.HashMap;
import java.util.Map;

public class Document {

    private String uri;
    private String domain;
    private String source;
    private String idType;
    private String id;
    private Map<String, String> dynamicFields = new HashMap<>();

    public Document() {
    }

    public Document(String uri, String domain, String source, String idType, String id, Map<String, String> dynamicFields) {
        this.uri = uri;
        this.domain = domain;
        this.source = source;
        this.idType = idType;
        this.id = id;
        this.dynamicFields = dynamicFields;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getDynamicFields() {
        return dynamicFields;
    }

    public void setDynamicFields(Map<String, String> dynamicFields) {
        this.dynamicFields = dynamicFields;
    }
}
