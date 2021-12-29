package uk.co.lukestevens.geoguessr.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import uk.co.lukestevens.config.Config;
import uk.co.lukestevens.geoguessr.models.*;
import uk.co.lukestevens.logging.Logger;
import uk.co.lukestevens.logging.LoggingProvider;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GeoGuessrService {

    private final Gson gson = new Gson();
    private final String geoguessrEndpoint;
    private final Logger logger;

    @Inject
    public GeoGuessrService(LoggingProvider loggingProvider, Config config) {
        this.logger = loggingProvider.getLogger(GeoGuessrService.class);
        this.geoguessrEndpoint = config.getAsString("geoguessr.endpoint");
    }

    public Game createChallenge(GameOption option, ApiCredentials credentials) throws IOException {
        logger.info("Creating challenge with game option: " + option.getDescription());
        ChallengeDefinition definition = new ChallengeDefinition(option.getMap(), option.getTimeLimit());

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(gson.toJson(definition), mediaType);
        Request request = new Request.Builder()
                .url(geoguessrEndpoint +  "/api/v3/challenges")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Cookie", credentials.getCookieHeader())
                .build();

        Game game = null;

        try(Response response = client.newCall(request).execute()) {
            String responseBodyString = getBody(response);
            if(response.code() == 200){
                JsonObject responseBody = gson.fromJson(responseBodyString, JsonObject.class);
                if(responseBody.has("token")){
                    String token = responseBody.get("token").getAsString();
                    game = new Game(token, option);
                }
                else {
                    logger.error("Expected token in response but received:\n```" + gson.toJson(responseBody) + "```");
                }
            }
            else {
                logger.error("Expected 200 response but received " + response.code() + ". Content: " + responseBodyString);
            }
        }

        if(game != null){
            return game;
        }
        else {
            throw new IOException("Error creating challenge");
        }
    }

    public List<ChallengeResult> getResults(String token, ApiCredentials credentials) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request request = new Request.Builder().get()
                .url(geoguessrEndpoint + "/api/v3/results/scores/" + token + "/0/0")
                .addHeader("Content-Type", "application/json")
                .addHeader("Cookie", credentials.getCookieHeader())
                .build();

        try(Response response = client.newCall(request).execute()) {
            String body = getBody(response);
            if(response.code() == 200){
                Type listType = new TypeToken<ArrayList<ChallengeResult>>(){}.getType();
                return gson.fromJson(body, listType);
            }
            else {
                logger.error("Expected 200 response but received " + response.code() + ". Content: " + body);
                return new ArrayList<>();
            }
        }
    }

    String getBody(Response response) throws IOException {
        ResponseBody body = response.body();
        return body != null? body.string() : null;
    }



}
