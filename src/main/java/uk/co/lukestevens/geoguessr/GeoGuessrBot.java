package uk.co.lukestevens.geoguessr;

import uk.co.lukestevens.config.Config;
import uk.co.lukestevens.geoguessr.models.*;
import uk.co.lukestevens.geoguessr.services.GameService;
import uk.co.lukestevens.geoguessr.services.GeoGuessrService;
import uk.co.lukestevens.geoguessr.services.SlackService;
import uk.co.lukestevens.geoguessr.stats.PlayerStats;
import uk.co.lukestevens.logging.Logger;
import uk.co.lukestevens.logging.LoggingProvider;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public void createChallenge() {
        try {
            GameOption gameOption = gameService.getRandomGameOption();
            Game game = geoGuessrService.createChallenge(gameOption);
            gameService.saveGame(game);
            slackService.sendChallengeMessage(game);
        } catch (Exception e){
            logger.error("Error when creating a new challenge");
            logger.error(e);
        }
    }

    public void postChallengeResults(){
        try {
            Optional<Game> optionalGame = gameService.getMostRecentGame();
            if(optionalGame.isEmpty()) {
                logger.warning("No recent games found.");
                return;
            }

            Game game = optionalGame.get();
            if(game.isResultsPosted()) {
                logger.warning("Most recent game results have already been posted.");
                return;
            }

            List<ChallengeResult> results = geoGuessrService.getResults(game.getChallengeToken());
            if(results.isEmpty()) {
                logger.warning("Most recent game (" + game.getChallengeToken() + ") has no scores.");
                return;
            }

            gameService.updateResultsForGame(game, results);
            slackService.sendResultsMessage(game);
            game.setResultsPosted(true);
            gameService.saveGame(game);
        } catch (Exception e){
            logger.error("Error when posting results");
            logger.error(e);
        }
    }

    public void updateAllChallengeResults(){
        List<Game> games = new ArrayList<>();
        try {
            games = gameService.getAllGames();
        }  catch (Exception e){
            logger.error("Error when fetching games");
            logger.error(e);
        }

        for(Game game : games){
            try {
                List<ChallengeResult> results = geoGuessrService.getResults(game.getChallengeToken());
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

        }  catch (Exception e){
            logger.error("Error when generating stats");
            logger.error(e);
        }
    }
}
