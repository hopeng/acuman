package com.acuman;

import com.couchbase.client.java.Bucket;
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
}
