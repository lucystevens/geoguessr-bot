package uk.co.lukestevens.geoguessr.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import uk.co.lukestevens.config.Config;
import uk.co.lukestevens.geoguessr.models.Game;
import uk.co.lukestevens.geoguessr.models.PlayerScore;
import uk.co.lukestevens.geoguessr.util.StringTemplate;
import uk.co.lukestevens.logging.Logger;
import uk.co.lukestevens.logging.LoggingProvider;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SlackService {

    private static final String SLACK_WEBHOOK_KEY = "slack.webhook";

    private static final String CHALLENGE_TEMPLATE_KEY = "slack.challenge.template";
    private static final String CHALLENGE_TEMPLATE_DEFAULT = "New Friday challenge! %s";

    private final Logger logger;
    private final Config config;
    private final Gson gson = new Gson();

    @Inject
    public SlackService(LoggingProvider loggingProvider, Config config) {
        this.logger = loggingProvider.getLogger(SlackService.class);
        this.config = config;
    }

    public void sendChallengeMessage(Game game){
        String messageTemplate = config.getAsStringOrDefault(CHALLENGE_TEMPLATE_KEY, CHALLENGE_TEMPLATE_DEFAULT);
        String challengeUrl = "https://www.geoguessr.com/challenge/" + game.getChallengeToken();
        logger.info("Sending challenge " + challengeUrl);
        String message = StringTemplate.fromTemplate(messageTemplate)
                .withVariable("challengeUrl", challengeUrl)
                .withVariable("gameOption", game.getGameOption().getDescription())
                .withVariable("timeLimit", game.getGameOption().getTimeLimit())
                .build();
        sendSlackMessage(message);
    }

    public void sendResultsMessage(Game game){
        List<PlayerScore> scores = game.getPlayerScores()
                .stream()
                .sorted(Comparator.comparing(PlayerScore::getScore).reversed())
                .collect(Collectors.toList());

        StringBuilder payload = new StringBuilder("*The results are in!*\n");
        String[] icons = {":first_place_medal:", ":second_place_medal:", ":third_place_medal:"};
        for(int i = 0; i<scores.size(); i++){
            PlayerScore score = scores.get(i);
            if(i < 3){
                payload.append(icons[i]).append(" ");
            }
            payload.append(score.getPlayer().getDisplayName())
                    .append(" - ")
                    .append(score.getScore())
                    .append("\n");
        }
        sendSlackMessage(payload.toString());
    }

    void sendSlackMessage(String message){
        JsonObject payload = new JsonObject();
        payload.addProperty("text", message);
        String json = gson.toJson(payload);

        String slackWebhook = config.getAsString(SLACK_WEBHOOK_KEY);
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
                logger.error("Error when logging to slack:\n```" + json + "```\nResponse:```" + response.body().string() + "```");
            }
        } catch (IOException e) {
            logger.error(e);
        }
    }

}
