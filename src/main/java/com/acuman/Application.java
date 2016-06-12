package com.acuman;

import com.acuman.api.ConsultationsApi;
import com.acuman.api.FileDownloadApi;
import com.acuman.api.Oauth2Api;
import com.acuman.api.PatientsApi;
import com.acuman.api.TcmDictLookupApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pac4j.core.authorization.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.matching.ExcludedPathMatcher;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.Google2Client;
import org.pac4j.sparkjava.CallbackRoute;
import org.pac4j.sparkjava.DefaultHttpActionAdapter;
import org.pac4j.sparkjava.RequiresAuthenticationFilter;
import spark.Route;

import static spark.Spark.before;
import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.port;
import static spark.Spark.post;


public class Application {
    private static final Logger log = LogManager.getLogger(Application.class);


    public static void main(String[] args) {
// todo ssl  http://stackoverflow.com/a/36843005/843678
        if (Boolean.valueOf(System.getProperty("dev"))) {
            port(4568);
        }
        
        externalStaticFileLocation("static/");

        final FacebookClient facebookClient = new FacebookClient(
                "1749785868632021",
                "0306c77fe48ca0eab1893b438aa126b7");
        final Google2Client google2Client = new Google2Client(
                "600496270016-metkjbkp5d6d63gqbmfe9b11i1pcfoph.apps.googleusercontent.com",
                "Zq1zVON3KFrmHJwPH9usEjr1");
        final Clients clients = new Clients("http://localhost:4568/callback", facebookClient, google2Client);
        final Config config = new Config(clients);
        config.addAuthorizer("admin", new RequireAnyRoleAuthorizer("ROLE_ADMIN"));
        config.addMatcher("excludedPath", new ExcludedPathMatcher("^/facebook/notprotected$"));
        config.setHttpActionAdapter(new DefaultHttpActionAdapter());
        final Route callback = new CallbackRoute(config);
        get("/callback", callback);
        post("/callback", callback);
        final RequiresAuthenticationFilter googleFilter = new RequiresAuthenticationFilter(config, Google2Client.class.getSimpleName(), "", "excludedPath");
        final RequiresAuthenticationFilter facebookFilter = new RequiresAuthenticationFilter(config, FacebookClient.class.getSimpleName(), "", "excludedPath");
        before("/facebook", facebookFilter);
        before("/facebook/*", facebookFilter);
        before("/google", googleFilter);

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
