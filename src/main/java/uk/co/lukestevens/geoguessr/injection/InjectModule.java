package uk.co.lukestevens.geoguessr.injection;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import uk.co.lukestevens.config.ApplicationProperties;
import uk.co.lukestevens.config.Config;
import uk.co.lukestevens.config.application.ConfiguredApplicationProperties;
import uk.co.lukestevens.config.application.SimpleApplicationProperties;
import uk.co.lukestevens.config.models.DatabaseConfig;
import uk.co.lukestevens.config.models.EnvironmentConfig;
import uk.co.lukestevens.config.models.PropertiesConfig;
import uk.co.lukestevens.config.services.DatabasePropertyService;
import uk.co.lukestevens.config.services.PropertyService;
import uk.co.lukestevens.db.Database;
import uk.co.lukestevens.geoguessr.logging.SlackLoggingProvider;
import uk.co.lukestevens.hibernate.DaoProvider;
import uk.co.lukestevens.hibernate.HibernateController;
import uk.co.lukestevens.jdbc.ConfiguredDatabase;
import uk.co.lukestevens.logging.LoggerLevel;
import uk.co.lukestevens.logging.LoggingProvider;
import uk.co.lukestevens.logging.provider.ConsoleLoggingProvider;
import uk.co.lukestevens.logging.provider.DatabaseLoggingProvider;

import java.io.IOException;
import java.util.Properties;

public class InjectModule extends AbstractModule {

    private static final String LOGGING_LEVEL_KEY = "logging.level";

    private final String slackLogUrl;

    private final Config databaseConfig;
    private final ApplicationProperties appProperties = new SimpleApplicationProperties(
            "1.0.0-SNAPSHOT",
            "geoguessr-bot",
            "uk.co.lukestevens");

    public InjectModule(String slackLogUrl, String databaseUrl, String databaseUser, String databasePassword) {
        this.slackLogUrl = slackLogUrl;

        Properties dbProps = new Properties();
        dbProps.put("database.url", databaseUrl);
        dbProps.put("database.username", databaseUser);
        dbProps.put("database.password", databasePassword);
        dbProps.put("hibernate.show_sql", false);
        this.databaseConfig = new PropertiesConfig(dbProps);
    }

    @Override
    protected void configure() {
        bind(ApplicationProperties.class).toInstance(appProperties);
        bind(PropertyService.class).to(DatabasePropertyService.class);
    }

    @Provides
    @Singleton
    protected DaoProvider providesDaoProvider() {
        return new HibernateController(databaseConfig, appProperties);
    }

    @Provides
    @Singleton
    protected Database providesDatabase() {
        return new ConfiguredDatabase(databaseConfig);
    }

    @Provides
    @Singleton
    protected LoggingProvider providesLogging(Config config) {
        return new SlackLoggingProvider(slackLogUrl, () -> config.getParsedValue(LOGGING_LEVEL_KEY, LoggerLevel::valueOf));
    }

    @Provides
    @Singleton
    protected Config providesConfig(PropertyService propertyService) throws IOException{
        Config config = new DatabaseConfig(propertyService);
        config.load();
        return config;
    }
}
