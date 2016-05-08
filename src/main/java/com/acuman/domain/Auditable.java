package com.acuman.domain;

import com.acuman.util.JsonUtils;

/**
 * Created by hopeng on 8/05/2016.
 */
public class Auditable {

    private String createdBy;
    private String createdDate;

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

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }
}
