package com.acuman.service.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by hopeng on 22/4/17.
 */
public class S3CrudTest {
    private static S3Crud s3Crud;
    private static AmazonS3 s3 = new AmazonS3Client();
    private static String bucketName = "acuman-testbucket";

    @BeforeClass
    public static void beforeClass() {
        // clean up
        if (s3.doesBucketExist(bucketName)) {
            s3.listObjects(bucketName).getObjectSummaries().forEach(
                    obj -> s3.deleteObject(bucketName, obj.getKey())
            );
        }

        // create bucket
        s3Crud = new S3Crud(bucketName);
        assertTrue(s3.doesBucketExist(bucketName));
    }

    /**
     * default listObjects() method only get 1000 record at a time. make sure this gets t
     */
    @Test
    public void testLongList() {
        S3Crud s3Crud = new S3Crud("tcmdict");
        List<String> list = s3Crud.listNonFolderObjects("ZhEnWords/");
        assertTrue(list.size() > 1000);
    }

    @Test
    public void TestPutObject() throws Exception {
//        WordNode-ZhEnWord-000002

        String json = "{\n" +
                "  \"doctor\": \"fionafamilytcm\",\n" +
                "  \"firstName\": \"Isabel\",\n" +
                "  \"lastName\": \"Shih\",\n" +
                "  \"gender\": \"Male\",\n" +
//                "  \"dob\": \"1980-03-02\",\n" +
                "  \"healthFund\": \"HIF\"\n" +
                "}";
        PutObjectResult result = s3Crud.putJson("key1", json);
        System.out.println(result);

        String obj = s3Crud.getString("key1");
        assertNotNull(obj);
        System.out.println(result);

        List<String> list = s3Crud.listObjects("", "");
        assertTrue(list.size() > 0);

        s3Crud.deleteObject("key1");
        assertNull(s3Crud.getStringNoException("key1"));
    }

    @Test
    public void testSeq() {
        SequenceGenerator.reset(s3Crud, "testSeq");
        SequenceGenerator.reset(s3Crud, "anotherNewSeq");

        long seq = SequenceGenerator.getNext(s3Crud, "testSeq");
        assertEquals(1, seq);

        seq = SequenceGenerator.getNext(s3Crud, "testSeq");
        assertEquals(2, seq);

        seq = SequenceGenerator.getNext(s3Crud, "testSeq");
        assertEquals(3, seq);

        seq = SequenceGenerator.getCurrent(s3Crud, "testSeq");
        assertEquals(3, seq);


        seq = SequenceGenerator.getNext(s3Crud, "anotherNewSeq");
        assertEquals(1, seq);

        assertNull(SequenceGenerator.getCurrent(s3Crud, "nonExistingSeq"));
    }
}
