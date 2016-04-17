package com.acuman;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.JsonStringDocument;
import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;

import static com.couchbase.client.java.query.Select.select;

public class AcuService {

    public void connect() {
        // Connect to localhost
        Cluster cluster = CouchbaseCluster.create();

        // Open the default bucket and the "beer-sample" one
        Bucket bucket = cluster.openBucket();

//        JsonObject user = JsonObject.empty()
//                .put("firstname", "Walter")
//                .put("lastname", "White bul")
//                .put("job", "chemistry teacher")
//                .put("age", 50);
//        JsonDocument stored = bucket.upsert(JsonDocument.create("walter", user));
//
//        JsonDocument walter = bucket.get("walter");
//        System.out.println("Found: " + walter.content().getString("firstname"));
//        System.out.println("Json: " + walter);

        bucket.upsert(RawJsonDocument.create("james", "{\n" +
                "  \"firstname\": \"Walter\",\n" +
                "  \"job\": \"FX Trader 交易员 交易員\",\n" +
                "  \"age\": 50,\n" +
                "  \"lastname\": \"White bul\"\n" +
                "}"));

        System.out.println(bucket.get("james", RawJsonDocument.class).content());
        System.out.println(bucket.get("james").content().getString("job"));

        N1qlQueryResult query = bucket.query(select("*").from("default").limit(10));
        for (N1qlQueryRow row : query.allRows()) {
            System.out.println("query result: " + row.value().toString());
        }


        // Disconnect and clear all allocated resources
        cluster.disconnect();
    }

    public static void main(String args[]) {
        new AcuService().connect();
    }
}
