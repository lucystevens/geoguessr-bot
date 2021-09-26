package uk.co.lukestevens;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestHelpers {

    static Gson gson = new Gson();

    public static String createTokenResponse(String token){
        JsonObject json = new JsonObject();
        json.addProperty("token", token);
        return gson.toJson(json);
    }

    public static String createSlackRequest(String message){
        JsonObject json = new JsonObject();
        json.addProperty("text", message);
        return gson.toJson(json);
    }

    public static String loadJson(String filename) throws IOException {
        return Files.readString(
                Paths.get("src/integrationTest/resources/json/" + filename)
        );
    }
}
