package uk.co.lukestevens.geoguessr;

import com.google.inject.Guice;
import com.google.inject.Injector;
import uk.co.lukestevens.geoguessr.injection.InjectModule;
import uk.co.lukestevens.geoguessr.util.SystemExit;
import uk.co.lukestevens.utils.EnvironmentVariables;

public class GeoGuessrBotMain {

    static final String errorMessage = "Expected either 'createChallenge' or 'postChallengeResults' as args!";

    public static void main(String[] args){
        Injector injector = Guice.createInjector(new InjectModule(
                EnvironmentVariables.get("SLACK_LOGGING_WEBHOOK"),
                EnvironmentVariables.get("DATABASE_URL"),
                EnvironmentVariables.get("DATABASE_USER"),
                EnvironmentVariables.get("DATABASE_PASSWORD")
        ));
        GeoGuessrBot bot = injector.getInstance(GeoGuessrBot.class);

        if(args.length == 0){
            System.err.println(errorMessage);
        }
        else if(args[0].equals("createChallenge")) {
            bot.createChallenge();
        }
        else if(args[0].equals("postChallengeResults")) {
            bot.postChallengeResults();
        }
        else {
            System.err.println(errorMessage);
        }
        SystemExit.forceExit(0);
    }

}
