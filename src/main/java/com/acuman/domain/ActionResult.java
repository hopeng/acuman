package com.acuman.domain;

/**
 * Created by hopeng on 2/5/17.
 */
public class ActionResult {

    private int httpCode;
    private String message;

    public ActionResult(int httpCode, String message) {
        this.httpCode = httpCode;
        this.message = message;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public String getMessage() {
        return message;
    }
}
