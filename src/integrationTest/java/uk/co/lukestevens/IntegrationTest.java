package uk.co.lukestevens;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.co.lukestevens.db.Database;
import uk.co.lukestevens.geoguessr.GeoGuessrBotMain;
import uk.co.lukestevens.geoguessr.util.SystemExit;
import uk.co.lukestevens.jdbc.SimpleDatabase;
import uk.co.lukestevens.testing.mocks.EnvironmentVariableMocker;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WireMockTest(httpPort  = 9002)
public class IntegrationTest {

    static String dbUrl = "jdbc:postgresql://localhost:9001/postgres";
    static String dbUser = "postgres";
    static String dbPassword = "password";

    static String createChallengeUrl = "/geoguessr/api/v3/challenges";
    static String getChallengeResultsUrl = "/geoguessr/api/v3/results/scores";
    static String slackPostingUrl = "/slack-posting";
    static String slackLoggingUrl = "/slack-logging";

    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    static Database db = new SimpleDatabase(dbUrl, dbUser, dbPassword);
    static DatabaseUtils dbUtils = new DatabaseUtils(db);

    public static void setupDatabaseConfig() throws SQLException {
        dbUtils.insertDbConfig("geoguessr.cookies.devicetoken", "test-devicetoken");
        dbUtils.insertDbConfig("geoguessr.cookies.ncfa", "test-ncfa");
        dbUtils.insertDbConfig("geoguessr.endpoint", "http://localhost:9002/geoguessr");
        dbUtils.insertDbConfig("logging.level", "INFO");
        dbUtils.insertDbConfig("slack.challenge.template", "Test: {{gameOption}} {{challengeUrl}} {{timeLimit}}");
        dbUtils.insertDbConfig("slack.webhook", "http://localhost:9002" + slackPostingUrl);
    }

    public static void setupEnvironmentConfig() {
        EnvironmentVariableMocker.build()
                .with("SLACK_LOGGING_WEBHOOK", "http://localhost:9002" + slackLoggingUrl)
                .with("DATABASE_URL", dbUrl)
                .with("DATABASE_USER", dbUser)
                .with("DATABASE_PASSWORD", dbPassword)
                .mock();
    }

    @BeforeAll
    public static void setup() throws SQLException {
        // setup config
        setupEnvironmentConfig();
        setupDatabaseConfig();
        SystemExit.disableExit();
    }

    @AfterEach
    public void teardown() throws SQLException {
        dbUtils.clearDatabase();
    }

    @Test
    public void integrationTest_createChallenge() throws SQLException, IOException {
        // Insert players and game option
        Long gameOption = dbUtils.insertGameOption("World", "world", 1, 300);
        dbUtils.insertPlayer("geo-1", "Alice");
        dbUtils.insertPlayer("geo-2", "Bob");


        // setup API responses
        stubFor(post(createChallengeUrl)
                .willReturn(okJson(TestHelpers.createTokenResponse("mock-token"))));
        stubFor(post(slackPostingUrl)
                .willReturn(ok()));
        stubFor(post(slackLoggingUrl)
                .willReturn(ok()));

        // Run script
        GeoGuessrBotMain.main(new String[]{"createChallenge"});

        // assert game created
        dbUtils.assertGameCreated("mock-token", gameOption, false);

        // verify API requests
        verify(postRequestedFor(urlEqualTo(createChallengeUrl))
            .withCookie("devicetoken", equalTo("test-devicetoken"))
            .withCookie("_ncfa", equalTo("test-ncfa"))
            .withRequestBody(equalToJson(TestHelpers.loadJson("ChallengeDefinition.json"))));

        verify(postRequestedFor(urlEqualTo(slackPostingUrl))
            .withRequestBody(equalToJson(
                    TestHelpers.createSlackRequest("Test: World https://www.geoguessr.com/challenge/mock-token 300"))));
    }

    @Test
    public void integrationTest_postChallengeResults() throws SQLException, ParseException, IOException {
        // Insert players and game option
        Long gameOption = dbUtils.insertGameOption("world", "world", 1, 300);
        Long alice = dbUtils.insertPlayer("geo-1", "Alice");
        Long bob = dbUtils.insertPlayer("geo-2", "Bob");
        Long game = dbUtils.insertGame("mock-token", gameOption, df.parse("2021-09-23"));

        // setup API responses
        stubFor(get(getChallengeResultsUrl + "/mock-token/0/0")
                .willReturn(okJson(TestHelpers.loadJson("ChallengeResults.json"))));
        stubFor(post(slackPostingUrl)
                .willReturn(ok()));
        stubFor(post(slackLoggingUrl)
                .willReturn(ok()));

        // Run script
        GeoGuessrBotMain.main(new String[]{"postChallengeResults"});

        // assert new player created
        Long carl = dbUtils.assertPlayerCreated("geo-3", "Carl");

        // assert scores saved
        dbUtils.assertPlayerScoreCreated(alice, game, 1000);
        dbUtils.assertPlayerScoreCreated(bob, game, 500);
        dbUtils.assertPlayerScoreCreated(carl, game, 2000);

        // verify API requests
        verify(getRequestedFor(urlEqualTo(getChallengeResultsUrl + "/mock-token/0/0"))
                .withCookie("devicetoken", equalTo("test-devicetoken"))
                .withCookie("_ncfa", equalTo("test-ncfa")));

        verify(postRequestedFor(urlEqualTo(slackPostingUrl))
                .withRequestBody(equalToJson(
                        TestHelpers.createSlackRequest(
                                "*The results are in!*\n"+
                                ":first_place_medal: Carl - 2000\n" +
                                ":second_place_medal: Alice - 1000\n" +
                                ":third_place_medal: Bob - 500\n"))));
    }
}
