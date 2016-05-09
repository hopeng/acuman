package com.acuman;

import com.acuman.domain.WordNode;
import com.acuman.domain.ZhEnWord;
import com.acuman.util.JsonUtils;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.Document;
import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.Statement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public final class CouchBaseQuery {
    private static final Logger log = LogManager.getLogger(CouchBaseQuery.class);

    private Bucket bucket;

    public CouchBaseQuery(Bucket bucket) {
        this.bucket = bucket;
    }

    public List<JsonObject> query(Statement statement) {
        log.info("running: " + statement);

        N1qlQueryResult queryResult = bucket.query(statement);

        return queryResult.allRows().stream()
                .map(row -> row.value().getObject(bucket.name()))
                .collect(Collectors.toList());
    }

    public List<JsonObject> query(String statement) {
        N1qlQueryResult queryResult = bucket.query(N1qlQuery.simple(statement));

        return queryResult.allRows().stream()
                .map(row -> row.value().getObject(bucket.name()))
                .collect(Collectors.toList());
    }

    public <T> T insert(String docId, T object) {
        Document rawJsonDocument = RawJsonDocument.create(docId, JsonUtils.toJson(object));
        Document result = bucket.insert(rawJsonDocument);

        log.info("inserted object: " + result.content());
        return JsonUtils.fromJson((String) result.content(), (Class<T>) object.getClass());
    }

    public <T> T upsert(String docId, T object) {
        Document rawJsonDocument = RawJsonDocument.create(docId, JsonUtils.toJson(object));
        Document result = bucket.upsert(rawJsonDocument);

        log.info("upsert object: " + result.content());
        return JsonUtils.fromJson((String) result.content(), (Class<T>) object.getClass());
    }

    public <T> T get(String docId, Class<T> clazz) {
        Document result = bucket.get(docId);

        log.info("getting object by id: " + docId);
        return JsonUtils.fromJson((String) result.content(), clazz);
    }

    public ZhEnWord getZhEnWord(String docId) {
        return get(docId, ZhEnWord.class);
    }

    public WordNode getWordNode(String docId) {
        return get(docId, WordNode.class);
    }
}
