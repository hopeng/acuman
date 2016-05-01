package com.acuman.api;

import com.acuman.service.TcmDictService;
import com.acuman.service.couchbase.CouchbaseTcmDictService;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Spark;

import java.util.Collections;
import java.util.List;

import static com.acuman.ApiConstants.API_PATIENTS;
import static com.acuman.ApiConstants.API_TCM_WORDS;
import static spark.Spark.get;
import static spark.Spark.put;

public class TcmWordLookupApi {
    private static final Logger log = LogManager.getLogger(TcmWordLookupApi.class);

    private enum TCM_WORD_ACTION {tag, untag};

    public static void configure() {
        TcmDictService tcmDictService = new CouchbaseTcmDictService();

        get(API_TCM_WORDS, (request, response) -> {
            String word = request.queryParams("q");
            boolean noRemoteSearch = Boolean.valueOf(request.queryParams("noRemoteSearch"));
            String pageSize = request.queryParams("p");
            pageSize = StringUtils.isEmpty(pageSize) ? "10" : pageSize;


            if (word != null && word.length() < 1) {
                return Collections.EMPTY_LIST;
            }

            if (noRemoteSearch) {
                List<JsonObject> localResult = tcmDictService.lookupWord(word, 10);
                return localResult;

            } else {
                HttpResponse<String> result = Unirest.get("http://dict.paradigm-pubs.com/search3.php")
                        .queryString("q", word)
                        .queryString("p", pageSize)
                        .queryString("y", "py1")
                        .asString();

                log.info("received result: " + result.getBody());

                JsonArray array = JsonArray.fromJson(result.getBody());
                array.forEach(obj -> {
                    JsonObject matched = (JsonObject) obj;
                    if (!tcmDictService.hasWord(matched.getString("mid"))) {
                        tcmDictService.newWord(matched);
                    }
                });
                return result.getBody();
            }
        });

//        tag / untag word against a category
        put(API_TCM_WORDS + "/:id", (request, response) -> {
            String id = request.params(":id");
            String tag = request.queryParams("tag");
            TCM_WORD_ACTION action = TCM_WORD_ACTION.valueOf(request.queryParams("action"));

            JsonObject result = tcmDictService.getWord(id);

            if (result == null) {
                response.status(404);
                return "Cannot find patient by ID " + id;

            } else {
                switch (action) {
                    case tag:
                        tcmDictService.addTag(id, result, tag);
                        break;

                    case untag:
                        tcmDictService.removeTag(id, result, tag);
                        break;

                    default:
                }

                return "";
            }
        });
    }
}

