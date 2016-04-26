package com.acuman.service.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CouchBaseClient {
    private static final Logger log = LogManager.getLogger(CouchBaseClient.class);

    public static final String BUCKET_NAME = "acuman";
    private static CouchBaseClient INSTANCE;

    private Cluster cluster;
    private Bucket bucket;

    public synchronized static CouchBaseClient getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CouchBaseClient();
        }

        return INSTANCE;
    }

    private CouchBaseClient() {
        cluster = CouchbaseCluster.create();
        bucket = cluster.openBucket(BUCKET_NAME);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                log.info("closing couchbase client");
                cluster.disconnect();
            }
        });
    }

    public Bucket getBucket() {
        return bucket;
    }
}
