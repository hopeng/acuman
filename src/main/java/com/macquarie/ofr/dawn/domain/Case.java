package com.macquarie.ofr.dawn.domain;

public class Case {

    private String caseId;
    private String description;
    private String createdUser;
    private String createdDate;
    private String lastUpdatedUser;
    private String lastUpdatedDate;

    public Case() {
    }

    public Case(String caseId, String description, String createdUser, String createdDate, String lastUpdatedUser, String lastUpdatedDate) {
        this.caseId = caseId;
        this.description = description;
        this.createdUser = createdUser;
        this.createdDate = createdDate;
        this.lastUpdatedUser = lastUpdatedUser;
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public String getLastUpdatedUser() {
        return lastUpdatedUser;
    }

    public void setLastUpdatedUser(String lastUpdatedUser) {
        this.lastUpdatedUser = lastUpdatedUser;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(String lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }
}
