package com.acuman.api;

import com.acuman.domain.TagAndWords;
import com.acuman.service.TcmDictService;
import com.acuman.service.couchbase.CouchbaseTcmDictService;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.transcoder.JacksonTransformers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.acuman.ApiConstants.API_TCM_CUSTOM_WORD;
import static com.acuman.ApiConstants.API_TCM_DICT;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;

public class TcmDictLookupApi {
    private static final Logger log = LogManager.getLogger(TcmDictLookupApi.class);

    public static void configure() {
        TcmDictService tcmDictService = new CouchbaseTcmDictService();

        get(API_TCM_DICT, (request, response) -> {
            String tags = request.queryParams("allTags");

            if (Boolean.valueOf(tags)) {
                Map<String, TagAndWords> result = tcmDictService.getTagsAndWords();
                return JacksonTransformers.MAPPER.writeValueAsString(result);
            }

            String word = request.queryParams("q");
            String pageSizeString = request.queryParams("p");
            int pageSize = NumberUtils.isDigits(pageSizeString) ? Integer.valueOf(pageSizeString) : 10;

            // only lookup words with at least one char
            if (word != null && word.length() < 1) {
                return Collections.EMPTY_LIST;
            }

            JsonArray wordsFromWeb = lookupWordOnWeb(tcmDictService, word, pageSize);
            List<JsonObject> customWordsFromDb = tcmDictService.lookupCustomWord(word, pageSize);
            customWordsFromDb.forEach(wordsFromWeb::add);

            return wordsFromWeb;
        });

//        add tag to a word
        post(API_TCM_DICT + "/:id", (request, response) -> {
            String mid = request.params(":id");
            String tag = request.queryParams("tagName");

            if (tcmDictService.getWord(mid) == null) {
                response.status(404);
                return "Cannot find patient by ID " + mid;

            } else {
                tcmDictService.addWordTag(mid, tag);
                return "";
            }
        });

//        todo create UI for this, use CouchbaseTcmDictServiceTest for now
        post(API_TCM_CUSTOM_WORD, (request, response) -> {
            String customWord = request.body();
            log.info("creating custom word {}", customWord);

            JsonObject result = tcmDictService.newCustomWord(JsonObject.fromJson(customWord));
            return result;
        });

        // remove tag from a word
        delete(API_TCM_DICT + "/:id", (request, response) -> {
            String mid = request.params(":id");
            String tag = request.queryParams("tagName");

            if (tcmDictService.getWord(mid) == null) {
                response.status(404);
                return "Cannot find patient by ID " + mid;

            } else {
                tcmDictService.removeTag(mid, tag);
                return "";
            }
        });
    }

    private static JsonArray lookupWordOnWeb(TcmDictService tcmDictService, String word, int pageSize)
            throws UnirestException {
        HttpResponse<String> result = Unirest.get("http://dict.paradigm-pubs.com/search3.php")
                .queryString("q", word)
                .queryString("p", pageSize)
                .queryString("y", "py1")
                .asString();

        log.info("received result: " + result.getBody());

        JsonArray wordsFromWeb = JsonArray.fromJson(result.getBody());
        wordsFromWeb.forEach(obj -> {
            JsonObject matched = (JsonObject) obj;
            tcmDictService.newWord(matched);
        });
        return wordsFromWeb;
    }
}

