package com.acuman.api;

import com.acuman.domain.UiWordNode;
import com.acuman.domain.ZhEnWord;
import com.acuman.service.TcmDictService;
import com.acuman.service.s3.S3TcmDictService;
import com.acuman.util.JsonUtils;
import com.couchbase.client.deps.com.fasterxml.jackson.core.type.TypeReference;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

import static com.acuman.ApiConstants.API_TCM_ZhEn_WORD;
import static spark.Spark.get;
import static spark.Spark.post;

public class TcmDictLookupApi {
    private static final Logger log = LogManager.getLogger(TcmDictLookupApi.class);

    public static void configure() {
        TcmDictService tcmDictService = new S3TcmDictService();

//        get(API_TCM_DICT, (request, response) -> {
//            String tags = request.queryParams("allTags");
//
//            if (Boolean.valueOf(tags)) {
//                Map<String, TagAndWords> result = tcmDictService.getTagsAndWords();
//                return JacksonTransformers.MAPPER.writeValueAsString(result);
//            }
//
//            String word = request.queryParams("q");
//            String pageSizeString = request.queryParams("p");
//            int pageSize = NumberUtils.isDigits(pageSizeString) ? Integer.valueOf(pageSizeString) : 10;
//
//            // only lookup words with at least one char
//            if (word != null && word.length() < 1) {
//                return Collections.EMPTY_LIST;
//            }
//
//            JsonArray wordsFromWeb = lookupWordOnWeb(tcmDictService, word, pageSize);
//            List<JsonObject> customWordsFromDb = tcmDictService.lookupCustomWord(word, pageSize);
//            customWordsFromDb.forEach(wordsFromWeb::add);
//
//            return wordsFromWeb;
//        });

//        add tag to a word
//        post(API_TCM_DICT + "/:id", (request, response) -> {
//            log.info("tag body: " + request.body());
//            String mid = request.params(":id");
//            String tag = request.queryParams("tagName");
//
//            if (tcmDictService.getWord(mid) == null) {
//                response.status(404);
//                return "Cannot find patient by ID " + mid;
//
//            } else {
//                tcmDictService.addWordTag(mid, tag);
//                return "";
//            }
//        });

        post(API_TCM_ZhEn_WORD, (request, response) -> {
            Map<String, List<ZhEnWord>> tagWordMap = JsonUtils.fromJson(request.body(),
                    new TypeReference<Map<String, List<ZhEnWord>>>() {});
            log.info("creating zhEnWords, size = {}", tagWordMap.size());
            log.debug("map: " + JsonUtils.toJson(tagWordMap.entrySet()));

            tagWordMap.entrySet().forEach(
                    entry -> tcmDictService.newWordNode(entry.getKey(), entry.getValue()));
            return "";
        });

        get(API_TCM_ZhEn_WORD, (request, response) -> {
            log.info("getting wordTree");
            UiWordNode uiWordNode = tcmDictService.buildWordTree();
            return uiWordNode;
        });

        // remove tag from a word
//        delete(API_TCM_DICT + "/:id", (request, response) -> {
//            String mid = request.params(":id");
//            String tag = request.queryParams("tagName");
//
//            if (tcmDictService.getWord(mid) == null) {
//                response.status(404);
//                return "Cannot find patient by ID " + mid;
//
//            } else {
//                tcmDictService.removeTag(mid, tag);
//                return "";
//            }
//        });
    }

    /**
     * @param tcmDictService
     * @param word
     * @param pageSize
     * @return
     * @throws UnirestException
     */
//    private static JsonArray lookupWordOnWeb(TcmDictService tcmDictService, String word, int pageSize)
//            throws UnirestException {
//        HttpResponse<String> result = Unirest.get("http://dict.paradigm-pubs.com/search3.php")
//                .queryString("q", word)
//                .queryString("p", pageSize)
//                .queryString("y", "py1")
//                .asString();
//
//        log.info("received result: " + result.getBody());
//
//        JsonArray wordsFromWeb = JsonArray.fromJson(result.getBody());
//        wordsFromWeb.forEach(obj -> {
//            JsonObject matched = (JsonObject) obj;
//            tcmDictService.newWord(matched);
//        });
//        return wordsFromWeb;
//    }
}

