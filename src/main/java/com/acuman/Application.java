package com.acuman;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static spark.Spark.*;


public class Application {

    private static Map<String, Book> books = new HashMap<String, Book>();
    private static final Logger log = LogManager.getLogger(Application.class);

    private static final String API_VERSION = "/v1";
    private static final String API_PATIENTS = API_VERSION + "/patients";

    public static void main(String[] args) {
        final Random random = new Random();

        externalStaticFileLocation("static/");

       post(API_PATIENTS, (request, response) -> {
            String patientJson = request.body();
            log.info("received patient {}", patientJson);

            String author = request.queryParams("author");
            String title = request.queryParams("title");
            Book book = new Book(author, title);

            int id = random.nextInt(Integer.MAX_VALUE);
            books.put(String.valueOf(id), book);

            response.status(200);
            return id;
        });

        // Gets the book resource for the provided id
        get("/books/:id", (request, response) -> {
            Book book = books.get(request.params(":id"));
            if (book != null) {
                return "Title: " + book.getTitle() + ", Author: " + book.getAuthor();
            } else {
                response.status(404); // 404 Not found
                return "Book not found";
            }
        });

        // Updates the book resource for the provided id with new information
        // author and title are sent in the request body as x-www-urlencoded values e.g. author=Foo&title=Bar
        // you get them by using request.queryParams("valuename")
        put("/books/:id", (request, response) -> {
            String id = request.params(":id");
            Book book = books.get(id);
            if (book != null) {
                String newAuthor = request.queryParams("author");
                String newTitle = request.queryParams("title");
                if (newAuthor != null) {
                    book.setAuthor(newAuthor);
                }
                if (newTitle != null) {
                    book.setTitle(newTitle);
                }
                return "Book with id '" + id + "' updated";
            } else {
                response.status(404); // 404 Not found
                return "Book not found";
            }
        });

        // Deletes the book resource for the provided id
        delete("/books/:id", (request, response) -> {
            String id = request.params(":id");
            Book book = books.remove(id);
            if (book != null) {
                return "Book with id '" + id + "' deleted";
            } else {
                response.status(404); // 404 Not found
                return "Book not found";
            }
        });

        // Gets all available book resources (ids)
        get("/books", (request, response) -> {
            String ids = "";
            for (String id : books.keySet()) {
                ids += id + " ";
            }
            return ids;
        });
    }

    public static class Book {

        public String author, title;

        public Book(String author, String title) {
            this.author = author;
            this.title = title;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
