package com.acuman;

import com.acuman.domain.TagAndWords;
import com.couchbase.client.deps.com.fasterxml.jackson.core.JsonProcessingException;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.transcoder.JacksonTransformers;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;

public class DateUtilsTest {

    @Test
    public void test() throws JsonProcessingException {
        LinkedHashSet<JsonObject> words = new LinkedHashSet<>();
        words.add(JsonObject.create().put("abc", "deb"));
        TagAndWords tagAndWords = new TagAndWords("symp", words);

        System.out.println(JacksonTransformers.MAPPER.writeValueAsString(Arrays.asList(tagAndWords, tagAndWords)));
    }
}