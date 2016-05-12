package com.acuman.domain;

import com.acuman.util.AuthUtil;
import com.acuman.util.JsonUtils;
import com.couchbase.client.java.document.json.JsonObject;

import java.time.LocalDateTime;

/**
 * Created by hopeng on 8/05/2016.
 */
public class Auditable {

    private String createdBy;
    private String createdDate; // todo use LocalDateTime
    private String lastUpdatedBy;
    private String lastUpdatedDate; // todo use LocalDateTime

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public String getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(String lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public void preInsert() {
        createdBy = AuthUtil.currentUser();
        createdDate = LocalDateTime.now().toString();
    }

    public void preUpdate() {
        lastUpdatedBy = AuthUtil.currentUser();
        lastUpdatedDate = LocalDateTime.now().toString();
    }

    public static void preInsert(JsonObject jsonObject) {
        jsonObject.put("createdBy", AuthUtil.currentUser());
        jsonObject.put("createdDate", LocalDateTime.now().toString());
    }

    public static void preUpdate(JsonObject jsonObject) {
        jsonObject.put("lastUpdatedBy", AuthUtil.currentUser());
        jsonObject.put("lastUpdatedDate", LocalDateTime.now().toString());
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }
}
