package com.acuman.service.couchbase;

import com.acuman.service.TcmDictService;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQueryResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.util.MultiMap;
import spark.utils.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.acuman.service.couchbase.CouchbasePatientService.DOCTOR;
import static com.couchbase.client.java.query.Select.select;

public class CouchbaseTcmDictService implements TcmDictService {
    private static final Logger log = LogManager.getLogger(CouchbaseTcmDictService.class);

    private static final String WORD_TAG_TYPE = "WORD-TAG";
    private static final String WORD_TYPE = "WORD";

    private Bucket bucket = CouchBaseClient.getInstance().getTcmDictBucket();

    @Override
    public JsonObject newWord(String json) {
        return newWord(JsonObject.fromJson(json));
    }

    @Override
    public JsonObject newWord(JsonObject word) {
        word.put("type", WORD_TYPE);
        word.put("createdDate", LocalDateTime.now().toString());
        word.put("createdBy", DOCTOR);
        String mid = word.getString("mid");
        Assert.isTrue(StringUtils.isNotEmpty(mid), "no mid found for the definition " + word);
        JsonDocument result = bucket.insert(JsonDocument.create(mid, word));

        log.info("inserted word: " + result.content());

        return result.content();
    }


    @Override
    public JsonObject getWord(String mid) {
        JsonDocument jsonDocument = bucket.get(mid, JsonDocument.class);
        return jsonDocument == null ? null : jsonDocument.content();
    }

    @Override
    public boolean hasWord(String mid) {
        return getWord(mid) != null;
    }

    @Override
    public JsonObject deleteWord(String mid) {
        JsonObject result = bucket.remove(mid).content();

        log.info("deleted word with mid " + mid);

        return result;
    }

    @Override
    public List<JsonObject> lookupWord(String word, int limit) {
        List<JsonObject> result;
        String condition = "cs like '%" + word + "%' order by py1";
        N1qlQueryResult query = bucket.query(select("*").from(bucket.name()).where(condition).limit(limit));

        result = query.allRows().stream()
                .map(row -> row.value().getObject(bucket.name()))
                .collect(Collectors.toList());

        return result;
    }

    @Override
    public void addTag(String wordId, String tag) {
        JsonObject wordTag = JsonObject.create();
        String wordTagId = toWordTagId(wordId, tag);
        wordTag.put("wordTagId", wordTagId);
        wordTag.put("type", WORD_TAG_TYPE);
        wordTag.put("wordId", wordId);
        wordTag.put("tagName", tag);
        wordTag.put("createdDate", LocalDateTime.now().toString());
        wordTag.put("createdBy", DOCTOR);

        bucket.insert(JsonDocument.create(wordTagId, wordTag));

        log.info("added tag {} for word {}", tag, wordId);
    }

    private String toWordTagId(String wordId, String tagName) {
        return WORD_TAG_TYPE + "-" + wordId + "-" + tagName;
    }

    @Override
    public void removeTag(String wordId, String tag) {
        String wordTagId = toWordTagId(wordId, tag);
        bucket.remove(wordTagId);

        log.info("removed word {} from tag {}", wordId, tag);
    }

    @Override
    public List<JsonObject> searchByTag(String tag) {
        return null;
    }

    @Override
    public MultiMap<JsonObject> listTags() {
        MultiMap<JsonObject> result = new MultiMap<>();
        List<JsonObject> wordTags;
        String condition = String.format("type='%s' order by createdDate", WORD_TAG_TYPE);
        N1qlQueryResult query = bucket.query(select("*").from(bucket.name()).where(condition));

        wordTags = query.allRows().stream()
                .map(row -> row.value().getObject(bucket.name()))
                .collect(Collectors.toList());


        for (JsonObject wordTag : wordTags) {
            JsonObject word = getWord(wordTag.getString("wordId"));
            result.put(wordTag.getString("tagName"), word);
        }

        return result;
    }
}
