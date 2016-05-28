package com.acuman;

import com.acuman.api.ConsultationsApi;
import com.acuman.api.FileDownloadApi;
import com.acuman.api.Oauth2Api;
import com.acuman.api.PatientsApi;
import com.acuman.api.TcmDictLookupApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static spark.Spark.before;
import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.halt;
import static spark.Spark.port;


public class Application {
    private static final Logger log = LogManager.getLogger(Application.class);


    public static void main(String[] args) {
// todo ssl  http://stackoverflow.com/a/36843005/843678
        if (Boolean.valueOf(System.getProperty("dev"))) {
            port(4568);
        }
        
        externalStaticFileLocation("static/");

//        final FacebookClient facebookClient = new FacebookClient("145278422258960", "be21409ba8f39b5dae2a7de525484da8");
//        final Google2Client google2Client = new Google2Client(
//                "600496270016-1d1vj6ouotucs3i6a95k84dingkkbiqb.apps.googleusercontent.com",
//                "qfGHbd7dg5rvX5b8vZGJbdbz");
//        final Clients clients = new Clients("http://localhost:4568/callback", facebookClient, google2Client);
//        final Config config = new Config(clients);
//        config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"));
//        config.addMatcher("excludedPath", new ExcludedPathMatcher("^/facebook/notprotected$"));
//        config.setHttpActionAdapter(new DefaultHttpActionAdapter());
//        final Route callback = new CallbackRoute(config);
//        get("/callback", callback);
//        post("/callback", callback);
//        final RequiresAuthenticationFilter googleFilter = new RequiresAuthenticationFilter(config, Google2Client.class.getSimpleName(), "", "excludedPath");
//        final RequiresAuthenticationFilter facebookFilter = new RequiresAuthenticationFilter(config, FacebookClient.class.getSimpleName(), "", "excludedPath");
//        before("/facebook", facebookFilter);
//        before("/facebook/*", facebookFilter);
//        before("/google", googleFilter);

        PatientsApi.configure();
        ConsultationsApi.configure();
        TcmDictLookupApi.configure();
        Oauth2Api.configure();
        FileDownloadApi.configure();

        before( (request, response) -> {
            String uri = request.uri();
            if (uri.startsWith("/img/") || uri.startsWith("/oauth2callback")) {
                return;
            }

            boolean authorized = true; // todo
            if (!authorized) {
                halt(401, "You are not authorized!");
            }
        });
    }
}
