package uk.co.lukestevens;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.lukestevens.db.Database;
import uk.co.lukestevens.geoguessr.GeoGuessrBotMain;
import uk.co.lukestevens.geoguessr.util.SystemExit;
import uk.co.lukestevens.jdbc.SimpleDatabase;
import uk.co.lukestevens.testing.mocks.DateMocker;
import uk.co.lukestevens.testing.mocks.EnvironmentVariableMocker;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WireMockTest(httpPort  = 9002)
public class PostResultsTest extends IntegrationTest {

    // Posting results is now done via a separate cron to updating results
    @Test
    public void postChallengeResults_singleLeague() throws SQLException, ParseException, IOException {
        // Fix current time
        DateMocker.setCurrentDate(df.parse("2021-09-30 19:11:02"));

        // Insert players, game option, game, and league
        Long gameOption = dbUtils.insertGameOption("world", "world", 1, 300);
        Long alice = dbUtils.insertPlayer("geo-1", "Alice");
        Long bob = dbUtils.insertPlayer("geo-2", "Bob");

        Long apiCredentials = dbUtils.insertApiCredentials("Bot", "test-devicetoken","test-ncfa");
        Long league = dbUtils.insertLeague(
                "test-league",
                "http://localhost:9002" + slackPostingUrl,
                "Test: {{gameOption}} {{challengeUrl}} {{timeLimit}}",
                "MONDAY",
                "THURSDAY",
                apiCredentials);
        dbUtils.insertLeagueGameOption(league, gameOption);

        // Insert games and scores
        Long gameToPost = dbUtils.insertGame("mock-token1", gameOption, df.parse("2021-09-23 09:00:00"), league, df.parse("2021-09-30 00:00:00"));
        dbUtils.insertPlayerScore(gameToPost, alice, 1000);
        dbUtils.insertPlayerScore(gameToPost, bob, 500);

        Long postedGame = dbUtils.insertGame("mock-token2", gameOption, df.parse("2021-09-16 09:00:00"), league, df.parse("2021-09-23 00:00:00"), true);
        dbUtils.insertPlayerScore(postedGame, alice, 2000);

        Long newGame = dbUtils.insertGame("mock-token3", gameOption, df.parse("2021-09-26 09:00:00"), league, df.parse("2021-10-03 00:00:00"));
        dbUtils.insertPlayerScore(newGame, alice, 3000);

        Long noScoreGame = dbUtils.insertGame("mock-token4", gameOption, df.parse("2021-09-23 09:00:00"), league, df.parse("2021-09-30 00:00:00"));

        // setup API responses
        stubFor(post(slackPostingUrl)
                .willReturn(ok()));
        stubFor(post(slackLoggingUrl)
                .willReturn(ok()));

        // Run script
        GeoGuessrBotMain.main(new String[]{"postChallengeResults"});

        // Assert game marked as results posted
        dbUtils.assertGameResultsPosted(gameToPost, true);
        dbUtils.assertGameResultsPosted(postedGame, true);
        dbUtils.assertGameResultsPosted(newGame, false);
        dbUtils.assertGameResultsPosted(noScoreGame, false);

        // Assert message posted to slack
        verify(postRequestedFor(urlEqualTo(slackPostingUrl))
                .withRequestBody(equalToJson(
                        TestHelpers.createSlackRequest(
                                "*The results are in!*\n"+
                                ":first_place_medal: Alice - 1000\n" +
                                ":second_place_medal: Bob - 500\n"))));

        getAllServeEvents().forEach(se -> System.out.println(se.getRequest().getUrl()));
    }
}
