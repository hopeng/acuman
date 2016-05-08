package com.acuman.service.couchbase;

import com.acuman.CbDocType;
import com.acuman.CouchBaseQuery;
import com.acuman.domain.TagAndWords;
import com.acuman.domain.WordNode;
import com.acuman.domain.ZhEnWord;
import com.acuman.service.TcmDictService;
import com.acuman.util.JsonUtils;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.Document;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.RawJsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.Statement;
import com.couchbase.client.java.query.dsl.Sort;
import com.hankcs.hanlp.HanLP;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.utils.Assert;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.acuman.CbDocType.ZhEnWord;
import static com.acuman.service.couchbase.CouchbasePatientService.DOCTOR;
import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.s;
import static com.couchbase.client.java.query.dsl.Expression.x;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class CouchbaseTcmDictService implements TcmDictService {
    private static final Logger log = LogManager.getLogger(CouchbaseTcmDictService.class);

    public static final String TAG_WORD_TYPE = "TAG-WORD";
    public static final String WORD_TYPE = "WORD";
//    public static final String CUSTOM_WORD_TYPE = "CUSTOM-WORD";
    public static final String CUSTOM_WORD_ID_SEQ = "customWordIdSeq";


    private Bucket bucket;
    private CouchBaseQuery couchBaseQuery;

    public CouchbaseTcmDictService() {
        bucket = CouchBaseClient.getInstance().getTcmDictBucket();
        couchBaseQuery = new CouchBaseQuery(bucket);

        // initData
        ZhEnWord root = new ZhEnWord();
        root.setCs("中医");
        root.setEng1("Traditional Chinese Medicine");
        newZhEnWord(root);
    }

    @Override
    public JsonObject newWord(String json) {
        return newWord(JsonObject.fromJson(json));
    }

    @Override
    public synchronized JsonObject newWord(JsonObject word) {
        String mid = word.getString("mid");
        Assert.isTrue(isNotEmpty(mid), "no mid found for the definition " + word);
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
    public ZhEnWord newZhEnWord(ZhEnWord zhEnWord) {
        Assert.isTrue(isNotEmpty(zhEnWord.getCs()) || isNotEmpty(zhEnWord.getCc()), "chinese must be provided");
        Assert.isTrue(isNotEmpty(zhEnWord.getEng1()), "english must be provided");

        if (isEmpty(zhEnWord.getCc())) {
            zhEnWord.setCc(HanLP.convertToTraditionalChinese(zhEnWord.getCs()));
        }
        if (isEmpty(zhEnWord.getCs())) {
            zhEnWord.setCs(HanLP.convertToTraditionalChinese(zhEnWord.getCc()));
        }
        if (isEmpty(zhEnWord.getPy3())) {
            zhEnWord.setPy3(HanLP.convertToPinyinString(zhEnWord.getCs(), " ", false));
        }

        ZhEnWord existing = exactWordMatch(zhEnWord.getCs());
        if (existing != null) {
            log.warn("word {} already exist", zhEnWord.getCs());
            return existing;
        }

        String customeWordId = generateId();
        zhEnWord.setMid(customeWordId);
        zhEnWord.setCreatedBy(DOCTOR);
        zhEnWord.setCreatedDate(LocalDateTime.now().toString());

        Document rawJsonDocument = RawJsonDocument.create(customeWordId, JsonUtils.toJson(zhEnWord));
        Document result = bucket.insert(rawJsonDocument);

        log.info("inserted custom word: " + result.content());
        return JsonUtils.fromJson((String) result.content(), ZhEnWord.class);
    }

    @Override
    public WordNode newZhEnWords(Map.Entry<String, List<ZhEnWord>> zhEnWordsEntry) {
        List<ZhEnWord> zhEnWords = zhEnWordsEntry.getValue();

        List<String> children = new LinkedList<>();
        zhEnWords.forEach(word -> {
            ZhEnWord newWord = this.newZhEnWord(word);
            children.add(newWord.getMid());
        });

        String tag = zhEnWordsEntry.getKey();
        ZhEnWord tagWord = exactWordMatch(tag);

        WordNode wordNode = new WordNode(tagWord.getMid(), children);
        String wordNodeId = CbDocType.WordNode + "-" + wordNode.getWordId();
        wordNode.setWordNodeId(wordNodeId);

        WordNode result = couchBaseQuery.upsert(CbDocType.WordNode + "-" + wordNode.getWordId(), wordNode);
//        String wordNodeId = CbDocType.WordNode + "-" + wordNode.getWordId();
//        Document rawJsonDocument = RawJsonDocument.create(wordNodeId, JsonUtils.toJson(wordNode));
//        Document result = bucket.upsert(rawJsonDocument);
//        log.info("upserted wordNode: " + result.content());
//        return JsonUtils.fromJson((String) result.content(), WordNode.class);

        return result;
    }


    @Override
    public ZhEnWord newZhEnWord(String chineseSimplified, String english) {
        ZhEnWord zhEnWord = new ZhEnWord();
        zhEnWord.setCs(chineseSimplified);
        zhEnWord.setEng1(english);

        return newZhEnWord(zhEnWord);
    }

    private String generateId() {
        long nextSquence = bucket.counter(CUSTOM_WORD_ID_SEQ, 1, 1).content();
        String id = ZhEnWord.class.getSimpleName() + "-" + String.format("%06d", nextSquence);

        return id;
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
    public ZhEnWord exactWordMatch(String csOrCcWord) {
        Statement statement = select("*").from(bucket.name()).where(
                x("type").eq(s(ZhEnWord))
                        .and((x("cs").eq(s(csOrCcWord)).or(x("cc").eq(s(csOrCcWord))))));
        List<JsonObject> result = couchBaseQuery.query(statement);

        Assert.isTrue(result.size() <= 1, "more than one ZhEnWord " + csOrCcWord + " found");

        return result.isEmpty() ? null : JsonUtils.fromJson(result.get(0), ZhEnWord.class);
    }

    @Override
    public List<JsonObject> lookupCustomWord(String word, int limit) {
//        String condition = "type = 'CUSTOM-WORD' and cs like '%" + word + "%' or eng1 like '%" + word + "%' or py3 like '%" + word + "%' order by py3";
//        N1qlQueryResult query = bucket.query(select("*").from(bucket.name()).where(condition).limit(limit));

        String wordLike = "%" + word + "%";
        Statement statement = select("*").from(bucket.name()).where(x("type").eq(s(ZhEnWord))
                .and((x("cs").like(s(wordLike)
                        .or(x("cc").like(s(wordLike)))
                        .or(x("py3").like(s(wordLike)))
                ))))
                .limit(limit);
        List<JsonObject> result = couchBaseQuery.query(statement);

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
            String wordId = tagWord.getString("wordId");
            JsonObject word = getWord(wordId);
            if (word == null) {
                log.error("no word found for Id {}", wordId);
                continue;
            }
            String tag = tagWord.getString("tagName");
            result.putIfAbsent(tag, new TagAndWords(tag, new LinkedHashSet<>()));
            TagAndWords tagAndWords = result.get(tag);
            tagAndWords.addWord(word);
        }

        return result;
    }
}
