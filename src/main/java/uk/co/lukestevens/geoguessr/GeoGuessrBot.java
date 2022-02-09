package uk.co.lukestevens.geoguessr;

import uk.co.lukestevens.config.Config;
import uk.co.lukestevens.geoguessr.models.*;
import uk.co.lukestevens.geoguessr.services.GameService;
import uk.co.lukestevens.geoguessr.services.GeoGuessrService;
import uk.co.lukestevens.geoguessr.services.SlackService;
import uk.co.lukestevens.geoguessr.stats.GameStats;
import uk.co.lukestevens.geoguessr.stats.PlayerStats;
import uk.co.lukestevens.logging.Logger;
import uk.co.lukestevens.logging.LoggingProvider;
import uk.co.lukestevens.utils.Dates;

import javax.inject.Inject;
import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class GeoGuessrBot {

    private final Logger logger;

    private final Config config;
    private final GameService gameService;
    private final GeoGuessrService geoGuessrService;
    private final SlackService slackService;

    @Inject
    public GeoGuessrBot(LoggingProvider loggingProvider, Config config, GameService gameService, GeoGuessrService geoGuessrService, SlackService slackService) {
        this.logger = loggingProvider.getLogger(GeoGuessrBot.class);
        this.config = config;
        this.gameService = gameService;
        this.geoGuessrService = geoGuessrService;
        this.slackService = slackService;
    }

    // TODO replace once base-lib is updated to support new DateTime
    LocalDateTime now(){
        return LocalDateTime.ofInstant(
                Dates.now().toInstant(), ZoneId.systemDefault());
    }

    public void createChallenges() {
        try {
            DayOfWeek day = DayOfWeek.of(now().get(ChronoField.DAY_OF_WEEK));
            for(League league : gameService.getLeaguesForChallengeDay(day)){
                createChallenge(league);
            }
        } catch (Exception e){
            logger.error("Error when creating challenges");
            e.printStackTrace();
        }
    }

    void createChallenge(League league) {
        try {
            GameOption gameOption = gameService.getRandomGameOption(league);
            Game game = geoGuessrService.createChallenge(gameOption, league.getApiCredentials());
            // Set post results after to the next results day for the league
            game.setPostResultsAfter(now()
                    .with(TemporalAdjusters.next(league.getResultsDay()))
                    .with(ChronoField.NANO_OF_DAY, 0).toInstant(ZoneOffset.UTC));
            league.addGame(game);

            gameService.saveGame(game);
            slackService.sendChallengeMessage(game);
        } catch (Exception e){
            logger.error("Error when creating a new challenge for league " + league.getName());
            e.printStackTrace();
        }
    }

    public void postChallengeResults(){
        try {
            List<Game> games = gameService.getGamesToPostResultsFor();
            if(games.isEmpty()) {
                logger.info("No games found to post results for");
                return;
            }

            for(Game game : games) {
                slackService.sendResultsMessage(game);
                game.setResultsPosted(true);
                gameService.saveGame(game);
            }
        } catch (Exception e){
            logger.error("Error when posting results");
            logger.error(e);
        }
    }

    public void updateChallengeResults(){
        // If last day of month then since first day
        // else since one week ago
        LocalDateTime sinceWhen = LocalDateTime.ofInstant(
                Dates.now().toInstant(), ZoneId.systemDefault());
        if(sinceWhen.getDayOfMonth() == sinceWhen.getMonth().length(false)){
            sinceWhen = sinceWhen.with(ChronoField.DAY_OF_MONTH, 1);
        } else {
            sinceWhen = sinceWhen.minusDays(7);
        }
        sinceWhen = sinceWhen.with(ChronoField.NANO_OF_DAY, 0);
        updateChallengeResults(sinceWhen.toInstant(ZoneOffset.UTC));
    }

    public void updateChallengeResults(Instant sinceWhen){
        List<Game> games = new ArrayList<>();
        try {
            games = gameService.getAllGamesCreatedSince(sinceWhen);
        }  catch (Exception e){
            logger.error("Error when fetching games");
            logger.error(e);
        }

        for(Game game : games){
            try {
                ApiCredentials credentials = game.getLeague().getApiCredentials();
                List<ChallengeResult> results = geoGuessrService.getResults(game.getChallengeToken(), credentials);
                if(!results.isEmpty()){
                    game.setResultsPosted(true);
                    gameService.updateResultsForGame(game, results);
                }
            }  catch (Exception e){
                logger.error("Error when updating results for game " + game.getChallengeToken());
                logger.error(e);
            }
        }
    }

    public void generateStats(){
        try {
            gameService.getPlayers().values().stream()
                    .map(PlayerStats::new)
                    .forEach(playerStats -> {
                        playerStats.generateStats();
                        System.out.println(playerStats);
                    });
            System.out.println();

            gameService.getAllGames().stream()
                    .map(GameStats::new)
                    .forEach(gameStats -> {
                        gameStats.generateStats();
                        System.out.println(gameStats);
                    });

        }  catch (Exception e){
            logger.error("Error when generating stats");
            logger.error(e);
        }
    }
}
