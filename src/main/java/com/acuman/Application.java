package com.acuman;

import static spark.Spark.*;


public class Application {

    public static void main(String[] args) {
        get("/hello", (request, response) -> "Hello World!");
    }
}
