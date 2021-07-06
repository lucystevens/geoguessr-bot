package uk.co.lukestevens.geoguessr.logging;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import uk.co.lukestevens.logging.LoggerLevel;
import uk.co.lukestevens.logging.loggers.AbstractLogger;
import uk.co.lukestevens.logging.models.Log;
import uk.co.lukestevens.utils.StringUtils;

import java.io.IOException;
import java.util.function.Supplier;

public class SlackLogger extends AbstractLogger {

    private static final String MESSAGE_TEMPLATE = ":%s: _[%s]:_ %s";
    private static final String EXCEPTION_MESSAGE_TEMPLATE = "%s\n```%s```";

    private final String slackWebhook;
    private final Gson gson = new Gson();


    /**
     * Create a new SlackLogger
     * @param name The name of the logger
     * @param minLevel The minimum level this logger should log for
     */
    public SlackLogger(String name, Supplier<LoggerLevel> minLevel, String slackWebhook) {
        super(name, minLevel);
        this.slackWebhook = slackWebhook;
    }

    @Override
    public void log(Exception e, LoggerLevel level) {
        log(String.format(EXCEPTION_MESSAGE_TEMPLATE, e.getMessage(), StringUtils.parseStackTrace(e)), level);
    }

    @Override
    protected void log(Log log) {
        JsonObject payload = new JsonObject();
        payload.addProperty("text", String.format(MESSAGE_TEMPLATE,
                log.getSeverity().toString().toLowerCase(),
                log.getName(),
                log.getMessage()));
        String json = gson.toJson(payload);

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(slackWebhook)
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();

        try(Response response = client.newCall(request).execute()) {
            if(response.code() != 200) {
                System.err.println("Error when logging to slack: " + json);
                System.err.println(response.body().string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
