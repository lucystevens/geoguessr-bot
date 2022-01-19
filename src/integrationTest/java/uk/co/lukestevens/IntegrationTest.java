package uk.co.lukestevens;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import uk.co.lukestevens.db.Database;
import uk.co.lukestevens.geoguessr.util.SystemExit;
import uk.co.lukestevens.jdbc.SimpleDatabase;
import uk.co.lukestevens.testing.mocks.EnvironmentVariableMocker;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

@WireMockTest(httpPort  = 9002)
public class IntegrationTest {

    static String dbUrl = "jdbc:postgresql://localhost:9001/postgres";
    static String dbUser = "postgres";
    static String dbPassword = "password";

    static String createChallengeUrl = "/geoguessr/api/v3/challenges";
    static String getChallengeResultsUrl = "/geoguessr/api/v3/results/scores";
    static String slackPostingUrl = "/slack-posting";
    static String slackLoggingUrl = "/slack-logging";

    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static Database db = new SimpleDatabase(dbUrl, dbUser, dbPassword);
    static DatabaseUtils dbUtils = new DatabaseUtils(db);

    public static void setupDatabaseConfig() throws SQLException {
        dbUtils.insertDbConfig("geoguessr.endpoint", "http://localhost:9002/geoguessr");
        dbUtils.insertDbConfig("logging.level", "INFO");
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

}
