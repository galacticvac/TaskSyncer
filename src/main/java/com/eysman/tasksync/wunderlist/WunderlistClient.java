package com.eysman.tasksync.wunderlist;

import com.eysman.tasksync.wunderlist.model.WList;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WunderlistClient {
    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /** Global instance of the JSON factory. */
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    public static final String TOKEN_SERVER_URL = "https://www.wunderlist.com/oauth/access_token";
    private static final String HTTP_AUTH_URI = "https://www.wunderlist.com/oauth/authorize?client_id=49e369919ae8382b2135&redirect_uri=http://localhost:8080/wunderlistauth&state=RANDOM";
    private static final String SCOPE = "read";

    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
     * globally shared instance across your application.
     */

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/wunderlist");
    public static final String WUNDER_BASE_URL = "https://a.wunderlist.com/api/v1/";
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static HttpRequestFactory requestFactory;
    private static String accessToken;

    /** API Endpoints */
    private static String API_LISTS = "lists";

    public WunderlistClient() throws Exception {
        DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        final Credential credential = authorize();
        accessToken = credential.getAccessToken();

        requestFactory =
                HTTP_TRANSPORT.createRequestFactory(request -> {
                    credential.initialize(request);
                    request.setParser(new JsonObjectParser(JSON_FACTORY));
                });
    }

    public static List<WList> getLists() throws IOException {
        Type listsType = new TypeToken<ArrayList<WList>>(){}.getType();

        String response = call(API_LISTS);
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(response, listsType);
    }

    private static String call(String endpoint) throws IOException {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("X-Access-Token",accessToken);
        httpHeaders.set("X-Client-ID", OAuth2ClientCredentials.API_KEY);

        HttpRequest request = requestFactory.buildGetRequest(
                new GenericUrl(WUNDER_BASE_URL + endpoint))
                .setHeaders(httpHeaders);
        return request.execute().parseAsString();
    }

    /** Authorizes the installed application to access user's protected data. */
    private static Credential authorize() throws Exception {
        // set up authorization code flow
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(BearerToken
                .authorizationHeaderAccessMethod(),
                HTTP_TRANSPORT,
                JSON_FACTORY,
                new GenericUrl(TOKEN_SERVER_URL),
                new ClientParametersAuthentication(
                        OAuth2ClientCredentials.API_KEY, OAuth2ClientCredentials.API_SECRET),
                OAuth2ClientCredentials.API_KEY,
                HTTP_AUTH_URI).setScopes(Arrays.asList(SCOPE))
                .setDataStoreFactory(DATA_STORE_FACTORY).build();

        // authorize
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setHost(
                OAuth2ClientCredentials.DOMAIN).setPort(OAuth2ClientCredentials.PORT).build();
        return new WunderlistAuthorization(flow, receiver).authorize("user");
    }

    private static class OAuth2ClientCredentials {

        /** Value of the "API Key". */
        public static final String API_KEY = "49e369919ae8382b2135";

        /** Value of the "API Secret". */
        public static final String API_SECRET = "9e8f7905f7b82fb33e748d8dc069059c1f8f621560e1e4601b0c0cc1cbcf";

        /** Port in the "Callback URL". */
        public static final int PORT = 50166;

        /** Domain name in the "Callback URL". */
        public static final String DOMAIN = "127.0.0.1";
    }
}