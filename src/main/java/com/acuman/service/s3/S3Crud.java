package com.acuman.service.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
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

    public List<String> listObjects(String prefix, String delimiter) {
        List<String> result = new ArrayList<>();

        ObjectListing list = s3.listObjects(new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix(prefix)
                .withDelimiter(delimiter)
        );

        for (S3ObjectSummary objectSummary : list.getObjectSummaries()) {
            String json = getString(objectSummary.getKey());
            if (json != null) {
                result.add(json);
            }
        }

        return result;
    }

    public List<String> listNonFolderObjects(String prefix) {
        return listObjects(prefix, "/");
    }
}
