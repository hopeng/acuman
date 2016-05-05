package com.acuman.service.couchbase;

import com.acuman.service.TcmDictService;
import com.couchbase.client.java.document.json.JsonObject;
import org.junit.Test;

/**
 * Created by hopeng on 3/05/2016.
 */
public class CouchbaseTcmDictServiceTest {
    
    TcmDictService tcmDictService = new CouchbaseTcmDictService();

    @Test
    public void testInsertCustomWord() {
        String cc = "頸椎病";
        String cs = "颈椎病";
        String py3 = "jing zhui bing";
        String eng1 = "cervical spondylosis";

        JsonObject customWord = JsonObject.create()
                .put("cc", cc)
                .put("cs", cs)
                .put("py3", py3)
                .put("eng1", eng1);

        tcmDictService.newCustomWord(customWord);
    }
}