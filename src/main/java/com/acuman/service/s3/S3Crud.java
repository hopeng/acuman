package com.acuman.service.s3;

import com.acuman.util.JsonUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hopeng on 23/4/17.
 */
public class S3Crud {
    private static final String CONTENT_TYPE = "application/json";

    private String bucketName;
    AmazonS3 s3 = new AmazonS3Client();

    public S3Crud(String bucketName) {
        this.bucketName = bucketName;
        if (!s3.doesBucketExist(bucketName)) {
            System.out.println("Creating bucket " + bucketName + "\n");
            s3.createBucket(bucketName);
        }
    }

    public String getString(String key) {
        String result = null;

        try {
            S3Object obj = s3.getObject(bucketName, key);
            result = IOUtils.toString(obj.getObjectContent());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public <T> T getObject(String key, Class<T> clazz) {
        String json = getString(key);

        return JsonUtils.fromJson(json, clazz);
    }

    public <T> T getObjectNoException(String key, Class<T> clazz) {
        String json = getStringNoException(key);

        return json == null ? null : JsonUtils.fromJson(json, clazz);
    }

    public String getStringNoException(String key) {
        String result = null;

        try {
            result = getString(key);

        } catch (AmazonS3Exception e) {
            if ("NoSuchKey".equals(e.getErrorCode())) {
                // ignore
            } else {
                e.printStackTrace();
            }
        }

        return result;
    }

    // print logging if put operation updates existing record (check version)
    public PutObjectResult putJson(String key, String json) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(CONTENT_TYPE);
        metadata.setContentLength(bytes.length);
        return s3.putObject(bucketName, key, new ByteArrayInputStream(bytes), metadata);
    }

    public void deleteObject(String key) {
        s3.deleteObject(bucketName, key);
    }

    //    todo improve performance. can it list object content in one go?
    public List<String> listObjects(String prefix, String delimiter) {
        List<String> result = new ArrayList<>();
        List<String> keyList = new ArrayList<>();

        ObjectListing list = s3.listObjects(new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix(prefix)
                .withDelimiter(delimiter)
        );
        list.getObjectSummaries().forEach(s -> keyList.add(s.getKey()));

        while (list.isTruncated()) {
            list = s3.listNextBatchOfObjects(list);
            list.getObjectSummaries().forEach(s -> keyList.add(s.getKey()));
        }

        for (String key : keyList) {
            String json = getString(key);
            if (json != null) {
                result.add(json);
            }
        }

        return result;
    }

    public List<String> listNonFolderObjects(String prefix) {
        return listObjects(prefix, "/");
    }

    public <T> List<T> listNonFolderObjects(String prefix, Class<T> type) {
        List<String> jsonList = listObjects(prefix, "/");
        List<T> result = new ArrayList<>(jsonList.size());

        jsonList.forEach(e -> result.add(JsonUtils.fromJson(e, type)));

        return result;
    }
}
