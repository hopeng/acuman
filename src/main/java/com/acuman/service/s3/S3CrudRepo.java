package com.acuman.service.s3;

import com.acuman.util.AuthUtil;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by hopeng on 25/4/17.
 */
public class S3CrudRepo {

    private static Map<String, S3Crud> s3CrudRefs = new WeakHashMap<>();

    public synchronized static S3Crud currentUserS3Crud() {
        String bucketName = "acuman-" + AuthUtil.currentUser();
        s3CrudRefs.computeIfAbsent(bucketName, S3Crud::new);

        return s3CrudRefs.get(bucketName);
    }
}
