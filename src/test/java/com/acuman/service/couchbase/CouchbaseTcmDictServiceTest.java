package com.acuman.service.couchbase;

import com.acuman.domain.ZhEnWord;
import com.acuman.service.TcmDictService;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStreamReader;

/**
 * Created by hopeng on 3/05/2016.
 */
public class CouchbaseTcmDictServiceTest {
    
    TcmDictService tcmDictService = new CouchbaseTcmDictService();

    @Test
    public void testInsertCustomWord() {
        ZhEnWord zhEnWord = new ZhEnWord();
        zhEnWord.setCc("滋陰解表");
        zhEnWord.setCs("滋阴解表");
        zhEnWord.setEng1("nourishing yin to relieve superficies syndrome");
        tcmDictService.newZhEnWord(zhEnWord);
    }

    @Test
    public void processTreeTest() throws Exception {
        String content = IOUtils.toString(new InputStreamReader(getClass().getResourceAsStream("/tree.json")));
        System.out.println(content);

//        Map<String, List<ZhEnWord>> map = JsonUtils.fromJson(content, new TypeReference<Map<String, List<ZhEnWord>>>() {});
//        map.entrySet().forEach(tcmDictService::newZhEnWords);
//        System.out.println(JsonUtils.toJson(map.entrySet()));
    }
}
