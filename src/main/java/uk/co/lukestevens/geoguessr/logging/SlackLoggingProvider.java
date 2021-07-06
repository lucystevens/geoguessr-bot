package uk.co.lukestevens.geoguessr.logging;

import uk.co.lukestevens.logging.Logger;
import uk.co.lukestevens.logging.LoggerLevel;
import uk.co.lukestevens.logging.LoggingProvider;

import java.util.function.Supplier;

public class SlackLoggingProvider implements LoggingProvider {

    private final String slackWebhookUrl;
    private final Supplier<LoggerLevel> loggerLevelSupplier;

    public SlackLoggingProvider(String slackWebhookUrl, Supplier<LoggerLevel> loggerLevelSupplier) {
        this.slackWebhookUrl = slackWebhookUrl;
        this.loggerLevelSupplier = loggerLevelSupplier;
    }

    @Override
    public Logger getLogger(String s) {
        return new SlackLogger(s, loggerLevelSupplier, slackWebhookUrl);
    }

}
