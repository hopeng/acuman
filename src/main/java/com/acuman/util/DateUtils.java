package com.acuman.util;

import com.couchbase.client.java.document.json.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class DateUtils {

    public static void convertISODateToLocalDateString(JsonObject obj, String fieldName) {
        String dateString = obj.getString(fieldName);
        if (StringUtils.isNotEmpty(dateString)) {
            ZonedDateTime dateTime = ZonedDateTime.parse(dateString).withZoneSameInstant(ZoneId.systemDefault());
            obj.put(fieldName, dateTime.toLocalDate().toString());
        }
    }
}
