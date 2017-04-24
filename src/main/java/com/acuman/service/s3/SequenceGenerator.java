package com.acuman.service.s3;

/**
 * WARNING: not safe in distributed environment
 */
public class SequenceGenerator {
    private static String SEQ_PREFIX = "sequences/";

    public synchronized static long getNext(S3Crud bucket, String sequenceName) {
        String current = bucket.getStringNoException(SEQ_PREFIX + sequenceName);
        long seq;
        if (current == null) {
            seq = 1;
        } else {
            seq = Long.valueOf(current) + 1;
        }
        bucket.putJson(SEQ_PREFIX + sequenceName, String.valueOf(seq));

        return seq;
    }

    public synchronized static Long getCurrent(S3Crud bucket, String sequenceName) {
        String current = bucket.getStringNoException(SEQ_PREFIX + sequenceName);

        return current == null ? null : Long.valueOf(current);
    }
}
