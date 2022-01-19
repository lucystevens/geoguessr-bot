package uk.co.lukestevens;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import uk.co.lukestevens.geoguessr.GeoGuessrBotMain;
import uk.co.lukestevens.testing.mocks.DateMocker;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WireMockTest(httpPort  = 9002)
public class UpdateResultsTest extends IntegrationTest {

    // Posting results is now done via a separate cron to updating results
    @Test
    public void updateChallengeResults_singleLeague() throws SQLException, ParseException, IOException {
        // Fix current time
        DateMocker.setCurrentDate(df.parse("2021-09-29 19:11:02"));

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

        Long gameToUpdate = dbUtils.insertGame("mock-token1", gameOption, df.parse("2021-09-24 09:00:00"), league, df.parse("2021-09-30 00:00:00"));
        dbUtils.insertPlayerScore(gameToUpdate, alice, 1000);
        dbUtils.insertPlayerScore(gameToUpdate, bob, 1000);

        Long oldGame = dbUtils.insertGame("mock-token2", gameOption, df.parse("2021-09-16 09:00:00"), league, df.parse("2021-09-23 00:00:00"));
        dbUtils.insertPlayerScore(oldGame, alice, 2000);




        // setup API responses
        stubFor(get(getChallengeResultsUrl + "/mock-token1/0/0")
                .willReturn(okJson(TestHelpers.loadJson("ChallengeResults.json"))));
        stubFor(post(slackPostingUrl)
                .willReturn(ok()));
        stubFor(post(slackLoggingUrl)
                .willReturn(ok()));

        // Run script
        GeoGuessrBotMain.main(new String[]{"updateChallengeResults"});

        // assert new player created
        Long carl = dbUtils.assertPlayerCreated("geo-3", "Carl");

        // assert scores saved
        dbUtils.assertPlayerScoreCreated(alice, gameToUpdate, 1000);
        dbUtils.assertPlayerScoreCreated(bob, gameToUpdate, 500);
        dbUtils.assertPlayerScoreCreated(carl, gameToUpdate, 2000);

        // verify API requests
        verify(getRequestedFor(urlEqualTo(getChallengeResultsUrl + "/mock-token1/0/0"))
                .withCookie("devicetoken", equalTo("test-devicetoken"))
                .withCookie("_ncfa", equalTo("test-ncfa")));
    }
}
