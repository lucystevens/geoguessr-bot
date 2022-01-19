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
public class CreateChallengeTest extends IntegrationTest {

    /**
     *     public void createChallenges() {
     *         try {
     *             DayOfWeek day = DayOfWeek.of(Dates.getCalendar().get(Calendar.DAY_OF_WEEK));
     *             for(League league : gameService.getLeaguesForChallengeDay(day)){
     *                 createChallenge(league);
     *             }
     *         } catch (Exception e){
     *             logger.error("Error when creating challenges");
     *             logger.error(e);
     *         }
     *     }
     *
     *         public void createChallenge(League league) {
     *         try {
     *             GameOption gameOption = gameService.getRandomGameOption(league);
     *             Game game = geoGuessrService.createChallenge(gameOption, league.getApiCredentials());
     *             // Set post results after to the next results day for the league
     *             game.setPostResultsAfter(LocalDateTime.ofInstant(
     *                     Dates.now().toInstant(), ZoneId.systemDefault())
     *                     .with(TemporalAdjusters.next(game.getLeague().getResultsDay()))
     *                     .with(ChronoField.NANO_OF_DAY, 0).toInstant(ZoneOffset.UTC));
     *             gameService.saveGame(game);
     *             slackService.sendChallengeMessage(game);
     *         } catch (Exception e){
     *             logger.error("Error when creating a new challenge for league " + league.getName());
     *             logger.error(e);
     *         }
     *     }
     */

    @Test
    public void createChallenge_singleLeague() throws SQLException, IOException, ParseException {
        // Insert players, game option, and league
        dbUtils.insertPlayer("geo-1", "Alice");
        dbUtils.insertPlayer("geo-2", "Bob");

        Long gameOption = dbUtils.insertGameOption("World", "world", 1, 300);
        Long apiCredentials = dbUtils.insertApiCredentials("Bot", "test-devicetoken","test-ncfa");
        Long league = dbUtils.insertLeague(
                "test-league",
                "http://localhost:9002" + slackPostingUrl,
                "Test: {{gameOption}} {{challengeUrl}} {{timeLimit}}",
                "MONDAY",
                "FRIDAY",
                apiCredentials);
        dbUtils.insertLeagueGameOption(league, gameOption);

        // Fix current time
        DateMocker.setCurrentDate(df.parse("2022-01-03 19:11:02"));

        // setup API responses
        stubFor(post(createChallengeUrl)
                .willReturn(okJson(TestHelpers.createTokenResponse("mock-token"))));
        stubFor(post(slackPostingUrl)
                .willReturn(ok()));
        stubFor(post(slackLoggingUrl)
                .willReturn(ok()));

        // Run script
        GeoGuessrBotMain.main(new String[]{"createChallenges"});

        // assert game created
        dbUtils.assertGameCreated("mock-token", gameOption, false, league, df.parse("2022-01-07 00:00:00"));

        // verify API requests
        verify(postRequestedFor(urlEqualTo(createChallengeUrl))
            .withCookie("devicetoken", equalTo("test-devicetoken"))
            .withCookie("_ncfa", equalTo("test-ncfa"))
            .withRequestBody(equalToJson(TestHelpers.loadJson("ChallengeDefinition.json"))));

        verify(postRequestedFor(urlEqualTo(slackPostingUrl))
            .withRequestBody(equalToJson(
                    TestHelpers.createSlackRequest("Test: World https://www.geoguessr.com/challenge/mock-token 300"))));
    }
}
