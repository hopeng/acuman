package com.acuman;

import com.acuman.api.ConsultationsApi;
import com.acuman.api.FileDownloadApi;
import com.acuman.api.Oauth2Api;
import com.acuman.api.PatientsApi;
import com.acuman.api.TcmDictLookupApi;
import com.acuman.pac4j.ExcludedPathsMatcher;
import com.acuman.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pac4j.core.authorization.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.oauth.client.FacebookClient;
import org.pac4j.oauth.client.Google2Client;
import org.pac4j.sparkjava.ApplicationLogoutRoute;
import org.pac4j.sparkjava.CallbackRoute;
import org.pac4j.sparkjava.DefaultHttpActionAdapter;
import org.pac4j.sparkjava.RequiresAuthenticationFilter;
import org.pac4j.sparkjava.SparkWebContext;
import spark.Request;
import spark.Response;
import spark.Route;

import static com.acuman.ApiConstants.API_GET_USER;
import static spark.Spark.before;
import static spark.Spark.externalStaticFileLocation;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;


public class Application {
    private static final Logger log = LogManager.getLogger(Application.class);
    private static final String LOGIN_SUCCESS_REDIRECT = "/";

    private static String[] EXCLUDED_PATHS = new String[]{
            "^/img/.*$",
            "^" + ApiConstants.API_GET_USER + "$"};

    public static void main(String[] args) {
// todo ssl  http://stackoverflow.com/a/36843005/843678
        if ("root".equals(System.getProperty("user.name"))) {
            port(80);
        } else {
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
        config.addMatcher("excludedPath", new ExcludedPathsMatcher(EXCLUDED_PATHS));
        config.setHttpActionAdapter(new DefaultHttpActionAdapter());
        final Route callback = new CallbackRoute(config);
        get("/callback", callback);
        post("/callback", callback);
        final RequiresAuthenticationFilter googleFilter = new RequiresAuthenticationFilter(config, Google2Client.class.getSimpleName(), "", "excludedPath");
        before("/doAuth", googleFilter);
        before(ApiConstants.API_VERSION + "/*", googleFilter);

        PatientsApi.configure();
        ConsultationsApi.configure();
        TcmDictLookupApi.configure();
        Oauth2Api.configure();
        FileDownloadApi.configure();

        get(API_GET_USER, (request, response) -> {
            final UserProfile profile = getUserProfile(request, response);
            return profile == null ? null : JsonUtils.toJson(profile);
        });

        get("/doAuth", (request, response) -> {
            if (getUserProfile(request, response) != null) {
                response.redirect(LOGIN_SUCCESS_REDIRECT);
            }
            return response;
        });

        get("/logout", new ApplicationLogoutRoute(config));

    }

    private static UserProfile getUserProfile(Request request, Response response) {
        final SparkWebContext context = new SparkWebContext(request, response);
        final ProfileManager manager = new ProfileManager(context);
        return manager.get(true);
    }
}
