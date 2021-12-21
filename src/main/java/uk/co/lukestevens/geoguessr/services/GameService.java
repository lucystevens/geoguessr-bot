package uk.co.lukestevens.geoguessr.services;

import com.google.common.base.Functions;
import uk.co.lukestevens.config.Config;
import uk.co.lukestevens.geoguessr.models.*;
import uk.co.lukestevens.geoguessr.util.CacheBuilder;
import uk.co.lukestevens.geoguessr.util.Cached;
import uk.co.lukestevens.hibernate.Dao;
import uk.co.lukestevens.hibernate.DaoProvider;
import uk.co.lukestevens.logging.Logger;
import uk.co.lukestevens.logging.LoggingProvider;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GameService {

    private final Logger logger;

    private final Dao<Game> gameDao;
    private final Dao<Player> playerDao;

    private final Cached<List<GameOption>> gameOptions;
    private final Cached<Map<String, Player>> players;

    @Inject
    public GameService(LoggingProvider loggingProvider, DaoProvider daoProvider) {
        this.logger = loggingProvider.getLogger(GameService.class);

        this.gameDao = daoProvider.getDao(Game.class);
        this.playerDao = daoProvider.getDao(Player.class);

        Dao<GameOption> gameOptionsDao = daoProvider.getDao(GameOption.class);
        this.gameOptions = CacheBuilder
                .withSource(gameOptionsDao::list)
                .withCacheLength(1, TimeUnit.HOURS)
                .build();

        this.players = CacheBuilder
                .withSource(this::getPlayers)
                .withCacheLength(1, TimeUnit.HOURS)
                .build();
    }

    public void saveGame(Game game) throws IOException {
        this.gameDao.save(game);
    }

    public Optional<Game> getMostRecentGame() throws IOException {
        return gameDao.list()
                .stream()
                .max(Comparator.comparing(Game::getCreatedAt));
    }

    public List<Game> getAllGames() throws IOException {
        return gameDao.list();
    }

    public GameOption getRandomGameOption(){
        List<GameOption> gameOptions = this.gameOptions.get();
        int totalWeight = gameOptions.stream().mapToInt(GameOption::getWeighting).sum();
        double random = Math.random() * ((double) totalWeight);
        for(GameOption option :  gameOptions) {
            if(random < option.getWeighting()){
                return option;
            }
            else {
                random -= option.getWeighting();
            }
        }

        logger.warning("No game options matched weighting, returning first.");
        return gameOptions.get(0);
    }

    public void updateResultsForGame(Game game, List<ChallengeResult> results) throws IOException {
        for(ChallengeResult result : results){
            updateResultForGame(game, result);
        }
        gameDao.save(game);
    }

    public Map<String, Player> getPlayers() throws IOException {
        return playerDao.list()
                .stream()
                .collect(Collectors.toMap(Player::getGeoguessrId, p -> p));
    }

    public void updateResultForGame(Game game, ChallengeResult result) throws IOException {
        PlayerScore playerScore = game.getPlayerScores()
                .stream()
                .filter(ps -> ps.getPlayer().getGeoguessrId().equals(result.getUserId()))
                .findFirst()
                .orElseGet(() -> new PlayerScore(game,
                        players.get().computeIfAbsent(result.getUserId(), id -> new Player(id, result.getPlayerName()))));

        playerScore.setScore(result.getTotalScore());
        playerDao.save(playerScore.getPlayer());
    }

}
