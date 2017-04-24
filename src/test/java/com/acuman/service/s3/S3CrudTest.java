package com.acuman.service.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by hopeng on 22/4/17.
 */
public class S3CrudTest {
    private static S3Crud s3Crud;
    private static AmazonS3 s3 = new AmazonS3Client();
    private static String bucketName = "testbucket-acuman";

    @BeforeClass
    public static void beforeClass() {
        s3Crud = new S3Crud(bucketName);
        assertTrue(s3.doesBucketExist(bucketName));
    }


    @Test
    public void TestPutObject() throws Exception {
        String json = "{\n" +
                "  \"doctor\": \"fionafamilytcm\",\n" +
                "  \"firstName\": \"Isabel\",\n" +
                "  \"lastName\": \"Shih\",\n" +
                "  \"gender\": \"Male\",\n" +
//                "  \"dob\": \"1980-03-02\",\n" +
                "  \"healthFund\": \"HIF\"\n" +
                "}";
        PutObjectResult result = s3Crud.putObject("key1", json);
        System.out.println(result);

        String obj = s3Crud.getString("key1");
        assertNotNull(obj);
        System.out.println(result);

        List<String> list = s3Crud.listObjects("", "");
        assertEquals(1, list.size());

        s3Crud.deleteObject("key1");
        assertNull(s3Crud.getStringNoException("key1"));

        s3.deleteBucket(bucketName);
    }
}
