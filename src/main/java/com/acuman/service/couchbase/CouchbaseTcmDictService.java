package com.acuman.service.couchbase;

import com.acuman.CouchBaseQuery;
import com.acuman.domain.TagAndWords;
import com.acuman.service.TcmDictService;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.Statement;
import com.couchbase.client.java.query.dsl.Sort;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.utils.Assert;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.acuman.service.couchbase.CouchbasePatientService.DOCTOR;
import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.s;
import static com.couchbase.client.java.query.dsl.Expression.x;

public class CouchbaseTcmDictService implements TcmDictService {
    private static final Logger log = LogManager.getLogger(CouchbaseTcmDictService.class);

    private static final String TAG_WORD_TYPE = "TAG-WORD";
    private static final String WORD_TYPE = "WORD";

    private Bucket bucket;
    private CouchBaseQuery couchBaseQuery;

    public CouchbaseTcmDictService() {
        bucket = CouchBaseClient.getInstance().getTcmDictBucket();
        couchBaseQuery = new CouchBaseQuery(bucket);
    }

    @Override
    public JsonObject newWord(String json) {
        return newWord(JsonObject.fromJson(json));
    }

    @Override
    public synchronized JsonObject newWord(JsonObject word) {
        String mid = word.getString("mid");
        Assert.isTrue(StringUtils.isNotEmpty(mid), "no mid found for the definition " + word);
        JsonObject existing = getWord(mid);
        if (existing == null) {
            word.put("type", WORD_TYPE);
            word.put("createdDate", LocalDateTime.now().toString());
            word.put("createdBy", DOCTOR);
            JsonDocument result = bucket.insert(JsonDocument.create(mid, word));

            log.info("inserted word: " + result.content());
            return result.content();

        } else {
            log.debug("word already exist: " + existing);
            return existing;
        }
    }

    @Override
    public JsonObject getWord(String mid) {
        JsonDocument jsonDocument = bucket.get(mid, JsonDocument.class);
        return jsonDocument == null ? null : jsonDocument.content();
    }

    @Override
    public boolean hasWord(String mid) {
        return getWord(mid) != null;
    }

    @Override
    public JsonObject deleteWord(String mid) {
        JsonObject result = bucket.remove(mid).content();

        log.info("deleted word with mid " + mid);

        return result;
    }

    @Override
    public List<JsonObject> lookupCustomWord(String word, int limit) {
        List<JsonObject> result;
        String condition = "type = 'CUSTOM-WORD' and cs like '%" + word + "%' or eng1 like '%" + word + "%' or py3 like '%" + word + "%' order by py1";
        N1qlQueryResult query = bucket.query(select("*").from(bucket.name()).where(condition).limit(limit));

        result = query.allRows().stream()
                .map(row -> row.value().getObject(bucket.name()))
                .collect(Collectors.toList());

        return result;
    }

    @Override
    public void addWordTag(String wordId, String tag) {
        JsonObject wordTag = JsonObject.create();
        String wordTagId = toWordTagId(wordId, tag);
        wordTag.put("wordTagId", wordTagId);
        wordTag.put("type", TAG_WORD_TYPE);
        wordTag.put("wordId", wordId);
        wordTag.put("tagName", tag);
        wordTag.put("createdDate", LocalDateTime.now().toString());
        wordTag.put("createdBy", DOCTOR);

        bucket.insert(JsonDocument.create(wordTagId, wordTag));

        log.info("added tag {} for word {}", tag, wordId);
    }

    private String toWordTagId(String wordId, String tagName) {
        return TAG_WORD_TYPE + "-" + wordId + "-" + tagName;
    }

    @Override
    public void removeTag(String wordId, String tag) {
        String wordTagId = toWordTagId(wordId, tag);
        bucket.remove(wordTagId);

        log.info("removed word {} from tag {}", wordId, tag);
    }

    @Override
    public Map<String, TagAndWords> getTagsAndWords() {
        Map<String, TagAndWords> result = new LinkedHashMap<>();

        List<JsonObject> tagWords;
        Statement statement = select("*").from(bucket.name())
                .where(x("type").eq(s(TAG_WORD_TYPE))).orderBy(Sort.asc(x("createdDate")));
        tagWords = couchBaseQuery.query(statement);

        for (JsonObject tagWord : tagWords) {
            JsonObject word = getWord(tagWord.getString("wordId"));
            String tag = tagWord.getString("tagName");
            result.putIfAbsent(tag, new TagAndWords(tag, new LinkedHashSet<>()));
            TagAndWords tagAndWords = result.get(tag);
            tagAndWords.addWord(word);
        }

        return result;
    }
}
