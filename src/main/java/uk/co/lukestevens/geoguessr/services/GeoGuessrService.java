package uk.co.lukestevens.geoguessr.services;

import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import uk.co.lukestevens.config.Config;
import uk.co.lukestevens.geoguessr.models.ChallengeDefinition;
import uk.co.lukestevens.geoguessr.models.ChallengeResult;
import uk.co.lukestevens.geoguessr.models.Game;
import uk.co.lukestevens.geoguessr.models.GameOption;
import uk.co.lukestevens.geoguessr.util.CacheBuilder;
import uk.co.lukestevens.geoguessr.util.Cached;
import uk.co.lukestevens.hibernate.Dao;
import uk.co.lukestevens.hibernate.DaoProvider;
import uk.co.lukestevens.logging.Logger;
import uk.co.lukestevens.logging.LoggingProvider;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GeoGuessrService {

    private static final String DEVICETOKEN_COOKIE_KEY = "geoguessr.cookies.devicetoken";
    private static final String NCFA_COOKIE_KEY = "geoguessr.cookies.ncfa";
    private static final String COOKIE_HEADER_TEMPLATE = "devicetoken=%s; _ncfa=%s";

    private final Gson gson = new Gson();
    private final String geoguessrEndpoint;
    private final Logger logger;
    private final Config config;

    @Inject
    public GeoGuessrService(LoggingProvider loggingProvider, Config config) {
        this.logger = loggingProvider.getLogger(GeoGuessrService.class);
        this.config = config;
        this.geoguessrEndpoint = config.getAsString("geoguessr.endpoint");
    }

    public Game createChallenge(GameOption option) throws IOException {
        logger.info("Creating challenge with game option: " + option.getDescription());
        ChallengeDefinition definition = new ChallengeDefinition(option.getMap(), option.getTimeLimit());

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(gson.toJson(definition), mediaType);
        Request request = new Request.Builder()
                .url(geoguessrEndpoint +  "/api/v3/challenges")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Cookie", getCookieHeader())
                .build();

        Game game = null;

        try(Response response = client.newCall(request).execute()) {
            if(response.code() == 200){
                JsonObject responseBody = gson.fromJson(response.body().string(), JsonObject.class);
                if(responseBody.has("token")){
                    String token = responseBody.get("token").getAsString();
                    game = new Game(token, option);
                }
                else {
                    logger.error("Expected token in response but received:\n```" + gson.toJson(responseBody) + "```");
                }
            }
            else {
                logger.error("Expected 200 response but received " + response.code());
            }
        }

        if(game != null){
            return game;
        }
        else {
            throw new IOException("Error creating challenge");
        }
    }

    public List<ChallengeResult> getResults(String token) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder().get()
                .url(geoguessrEndpoint + "/api/v3/results/scores/" + token + "/0/0")
                .addHeader("Content-Type", "application/json")
                .addHeader("Cookie", getCookieHeader())
                .build();

        try(Response response = client.newCall(request).execute()) {
            if(response.code() == 200){
                Type listType = new TypeToken<ArrayList<ChallengeResult>>(){}.getType();
                return gson.fromJson(response.body().string(), listType);
            }
            else {
                logger.error("Expected 200 response but received " + response.code());
                return new ArrayList<>();
            }
        }
    }

    String getCookieHeader(){
        String deviceToken = config.getAsString(DEVICETOKEN_COOKIE_KEY);
        String ncfa = config.getAsString(NCFA_COOKIE_KEY);
        return String.format(COOKIE_HEADER_TEMPLATE, deviceToken, ncfa);
    }

}
