package com.acuman.service.s3;

import com.acuman.CbDocType;
import com.acuman.domain.Auditable;
import com.acuman.domain.TagAndWords;
import com.acuman.domain.UiWordNode;
import com.acuman.domain.WordNode;
import com.acuman.domain.ZhEnWord;
import com.acuman.service.TcmDictService;
import com.acuman.util.JsonUtils;
import com.acuman.util.StringUtils;
import com.couchbase.client.java.document.json.JsonObject;
import com.luhuiguo.chinese.ChineseUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.utils.Assert;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.luhuiguo.chinese.pinyin.PinyinFormat.TONELESS_PINYIN_FORMAT;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class S3TcmDictService implements TcmDictService {
    private static final Logger log = LogManager.getLogger(S3TcmDictService.class);

    public static final String TAG_WORD_TYPE = "TAG-WORD";
    public static final String WORD_TYPE = "WORD";
    public static final String ZH_EN_WORD_ID_SEQ = "zhEnWordIdSeq";

    private static final String WORD_NODES_PREFIX = "WordNodes/";
    private static final String ZH_EN_WORDS_PREFIX = "ZhEnWords/";

// todo not safe for distributed system
    private static Map<String, ZhEnWord> zhEnWordMap = new ConcurrentHashMap<>();

    private S3Crud tcmDict = new S3Crud("tcmdict");

    private ZhEnWord rootWord;


    public static String wordNodeId(String wordId) {
        return CbDocType.WordNode + "-" + wordId;
    }

    private String generateWordNodeId(ZhEnWord word) {
        return CbDocType.WordNode + "-" + word.getMid();
    }

    public S3TcmDictService() {
        buildZhEnWordIndex();

        // initData
        rootWord = new ZhEnWord();
        rootWord.setCs("中医");
        rootWord.setEng1("Traditional Chinese Medicine");
        rootWord = enrichAndSaveZhEnWord(rootWord);

    }

//    todo map IllegalArgumentException to HTTP 400
    @Override
    public ZhEnWord enrichAndSaveZhEnWord(ZhEnWord zhEnWord) {
        zhEnWord.trimFields();
        Assert.isTrue(isNotEmpty(zhEnWord.getCs()) || isNotEmpty(zhEnWord.getCc()),
                "chinese must be provided for word: " + zhEnWord);
        Assert.isTrue(isNotEmpty(zhEnWord.getEng1()), "english must be provided for word " + zhEnWord);

        if (isEmpty(zhEnWord.getCc())) {
            zhEnWord.setCc(ChineseUtils.toTraditional(zhEnWord.getCs()));
        }
        if (isEmpty(zhEnWord.getCs())) {
            zhEnWord.setCs(ChineseUtils.toSimplified(zhEnWord.getCc()));
        }
        if (isEmpty(zhEnWord.getPy3())) {
            zhEnWord.setPy3(ChineseUtils.toPinyin(zhEnWord.getCs(), TONELESS_PINYIN_FORMAT));
        }

        ZhEnWord existing = exactWordMatch(zhEnWord.getCs());
        if (existing != null) {
            log.warn("word {} {} already exist", existing.getCs(), existing.getMid());
            return existing;
        }

        String wordId = generateZhEnWordId();
        zhEnWord.setMid(wordId);
        zhEnWord.preInsert();

        String json = JsonUtils.toJson(zhEnWord);
        tcmDict.putJson(ZH_EN_WORDS_PREFIX + wordId, json);

        // todo build the index while adding new words into dict. Any better idea?
        zhEnWordMap.put(zhEnWord.getCs(), zhEnWord);
//        Document rawJsonDocument = RawJsonDocument.create(wordId, JsonUtils.toJson(zhEnWord));
//        Document result = bucket.insert(rawJsonDocument);

        log.info("inserted ZhEnWord word: " + json);
        return JsonUtils.fromJson(json, ZhEnWord.class);
    }

    @Override
    public WordNode newWordNode(String tagName, List<ZhEnWord> childWords) {
        List<String> children = new LinkedList<>();
        childWords.forEach(word -> {
            ZhEnWord newWord = this.enrichAndSaveZhEnWord(word);
            children.add(newWord.getMid());
        });

        ZhEnWord tag = exactWordMatch(StringUtils.trimNonBreaking(tagName)); // assume the parent tag word already inserted before children
        Assert.notNull(tag, "word doesn't exist for tagName " + tagName);

        WordNode wordNode = new WordNode(tag.getMid(), children);
        tcmDict.putJson(WORD_NODES_PREFIX + wordNode.getWordNodeId(), wordNode.toString());
//        WordNode result = couchBaseQuery.upsert(wordNode.getWordNodeId(), wordNode);
        return wordNode;
    }


    @Override
    public ZhEnWord enrichAndSaveZhEnWord(String chineseSimplified, String english) {
        ZhEnWord zhEnWord = new ZhEnWord();
        zhEnWord.setCs(chineseSimplified);
        zhEnWord.setEng1(english);

        return enrichAndSaveZhEnWord(zhEnWord);
    }

    private String generateZhEnWordId() {
        long nextSquence = SequenceGenerator.getNext(tcmDict, ZH_EN_WORD_ID_SEQ);
        String id = ZhEnWord.class.getSimpleName() + "-" + String.format("%06d", nextSquence);

        return id;
    }


    public void buildZhEnWordIndex() {
        log.info("retrieving list of ZhEnWords from s3");
        List<ZhEnWord> list = tcmDict.listNonFolderObjects(ZH_EN_WORDS_PREFIX, ZhEnWord.class);
        list.forEach(w -> zhEnWordMap.putIfAbsent(w.getCs(), w));
        log.info("finished building word index");
    }

    @Override
    public ZhEnWord exactWordMatch(String csOrCcWord) {
        return zhEnWordMap.get(csOrCcWord);
//        String whereClause = String.format("type='%s' and (cs='%s' or cc='%s')", ZhEnWord, csOrCcWord, csOrCcWord);
//        Statement statement = select("*").from(bucket.name()).where(whereClause);
//        List<JsonObject> result = couchBaseQuery.query(statement);
//
//        Assert.isTrue(result.size() <= 1, "more than one ZhEnWord " + csOrCcWord + " found");
//
//        return result.isEmpty() ? null : JsonUtils.fromJson(result.get(0), ZhEnWord.class);
    }

    @Override
    public UiWordNode buildWordTree() {
        WordNode rootNode=null;
        rootNode = tcmDict.getObjectNoException(WORD_NODES_PREFIX + generateWordNodeId(rootWord), WordNode.class);
//        rootNode = couchBaseQuery.get(generateWordNodeId(rootWord), WordNode.class);
        UiWordNode rootUiWordNode = UiWordNode.fromWord(rootWord);
        if (rootNode == null) {
            log.warn("root WordNode doesn't exist for rootWord: " + rootWord.getCc());
            return rootUiWordNode;
        }

        populateUiWordNodeChildren(rootNode, rootUiWordNode);

        return rootUiWordNode;
    }

    private void populateUiWordNodeChildren(WordNode parent, UiWordNode uiParent) {
        for (String childWordId : parent.getChildWordId()) {
            ZhEnWord childWord = tcmDict.getObjectNoException(ZH_EN_WORDS_PREFIX + childWordId, ZhEnWord.class);
//            ZhEnWord childWord = couchBaseQuery.getZhEnWord(childWordId);
            if (childWord == null) {
                log.fatal("childWordId {} doesn't exist for parent {}", childWordId, parent.getWordNodeId());
                continue;
            }
            UiWordNode uiChildNode = UiWordNode.fromWord(childWord);
            uiParent.addChild(uiChildNode);

            // recurse
            WordNode childNode = tcmDict.getObjectNoException(WORD_NODES_PREFIX + wordNodeId(childWordId), WordNode.class);
//            WordNode childNode = couchBaseQuery.getWordNode(wordNodeId(childWordId));
            if (childNode != null) {
                if (parent.getWordId().equals(childWordId)) {
                    log.debug("word mid {} is a child of itself. Stop recursing", parent.getWordId());
                } else {
                    populateUiWordNodeChildren(childNode, uiChildNode);
                }
            }
        }
    }

    //-----------------------
    // deprecated code
    //-----------------------
    @Override
    public JsonObject getWord(String mid) {
        String json = tcmDict.getString(mid);
        return JsonObject.fromJson(json);
    }

    @Override
    public boolean hasWord(String mid) {
        return getWord(mid) != null;
    }

    @Override
    public JsonObject deleteWord(String mid) {
        tcmDict.deleteObject(mid);
        log.info("deleted word with mid " + mid);

        return null;
    }

    @Override
    public JsonObject newWord(String json) {
        return newWord(JsonObject.fromJson(json));
    }

    /**
     * @deprecated
     * @param word
     * @return
     */
    @Override
    public synchronized JsonObject newWord(JsonObject word) {
        String mid = word.getString("mid");
        Assert.isTrue(isNotEmpty(mid), "no mid found for the definition " + word);
        JsonObject existing = getWord(mid);
        if (existing == null) {
            word.put("type", WORD_TYPE);
            Auditable.preInsert(word);
            tcmDict.putJson(mid, word.toString());
//            JsonDocument result = bucket.insert(JsonDocument.create(mid, word));

            log.info("inserted word: " + word);
            return word;

        } else {
            log.debug("word already exist: " + existing);
            return existing;
        }
    }
//    @Override
//    public List<JsonObject> lookupCustomWord(String word, int limit) {
////        String condition = "type = 'CUSTOM-WORD' and cs like '%" + word + "%' or eng1 like '%" + word + "%' or py3 like '%" + word + "%' order by py3";
////        N1qlQueryResult query = bucket.query(select("*").from(bucket.name()).where(condition).limit(limit));
//        // todo use raw string condition, or is not wrapped
//        String wordLike = "%" + word + "%";
//        Statement statement = select("*").from(bucket.name()).where(x("type").eq(s(ZhEnWord))
//                .and((x("cs").like(s(wordLike)
//                        .or(x("cc").like(s(wordLike)))
//                        .or(x("py3").like(s(wordLike)))
//                ))))
//                .limit(limit);
//        List<JsonObject> result = couchBaseQuery.query(statement);
//
//        return result;
//        return null;
//    }

    /**
     * @deprecated todo do it properly with ZhEnWord
     * @return
     */
    @Override
    public void addWordTag(String wordId, String tag) {
//        JsonObject wordTag = JsonObject.create();
//        String wordTagId = toWordTagId(wordId, tag);
//        wordTag.put("wordTagId", wordTagId);
//        wordTag.put("type", TAG_WORD_TYPE);
//        wordTag.put("wordId", wordId);
//        wordTag.put("tagName", tag);
//        Auditable.preInsert(wordTag);
//
//        bucket.insert(JsonDocument.create(wordTagId, wordTag));
//
//        log.info("added tag {} for word {}", tag, wordId);
    }

    private String toWordTagId(String wordId, String tagName) {
        return TAG_WORD_TYPE + "-" + wordId + "-" + tagName;
    }

    /**
     * @deprecated todo do it properly with ZhEnWord
     * @return
     */
    @Override
    public void removeTag(String wordId, String tag) {
//        String wordTagId = toWordTagId(wordId, tag);
//        bucket.remove(wordTagId);
//
//        log.info("removed word {} from tag {}", wordId, tag);
    }

    /**
     * @deprecated
     * @return
     */
    @Override
    public Map<String, TagAndWords> getTagsAndWords() {
        Map<String, TagAndWords> result = new LinkedHashMap<>();

//        List<JsonObject> tagWords;
//        Statement statement = select("*").from(bucket.name())
//                .where(x("type").eq(s(TAG_WORD_TYPE))).orderBy(Sort.asc(x("createdDate")));
//        tagWords = couchBaseQuery.query(statement);
//
//        for (JsonObject tagWord : tagWords) {
//            String wordId = tagWord.getString("wordId");
//            JsonObject word = getWord(wordId);
//            if (word == null) {
//                log.error("no word found for Id {}", wordId);
//                continue;
//            }
//            String tag = tagWord.getString("tagName");
//            result.putIfAbsent(tag, new TagAndWords(tag, new LinkedHashSet<>()));
//            TagAndWords tagAndWords = result.get(tag);
//            tagAndWords.addWord(word);
//        }

        return result;
    }
}
