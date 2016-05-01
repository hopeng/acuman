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

import static com.couchbase.client.java.query.Select.select;

public class CouchbaseTcmDictService implements TcmDictService {
    private static final Logger log = LogManager.getLogger(CouchbaseTcmDictService.class);

    public static final String DOCTOR = "HFANG";   // todo should come from session user

    private static final String PATIENT_PREFIX = "-PATIENT-";
    private static final String PATIENT_ID_SEQ = "patientIdSeq";

    private Bucket bucket = CouchBaseClient.getInstance().getTcmDictBucket();

    @Override
    public JsonObject newWord(String json) {
        return newWord(JsonObject.fromJson(json));
    }

    @Override
    public JsonObject newWord(JsonObject word) {
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
    public void addTag(String id, JsonObject jsonObject, String tag) {

    }

    @Override
    public void removeTag(String id, JsonObject jsonObject, String tag) {

    }

    @Override
    public List<JsonObject> searchByTag(String tag) {
        return null;
    }

    @Override
    public MultiMap<JsonObject> listTags() {
        return null;
    }
}
