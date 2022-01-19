package uk.co.lukestevens.geoguessr.services;

import uk.co.lukestevens.geoguessr.models.*;
import uk.co.lukestevens.geoguessr.util.CacheBuilder;
import uk.co.lukestevens.geoguessr.util.Cached;
import uk.co.lukestevens.hibernate.Dao;
import uk.co.lukestevens.hibernate.DaoProvider;
import uk.co.lukestevens.jdbc.filter.QueryFilters;
import uk.co.lukestevens.logging.Logger;
import uk.co.lukestevens.logging.LoggingProvider;
import uk.co.lukestevens.utils.Dates;

import javax.inject.Inject;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GameService {

    private final Logger logger;

    private final Dao<Game> gameDao;
    private final Dao<League> leagueDao;
    private final Dao<Player> playerDao;

    private final Cached<List<GameOption>> gameOptions;
    private final Cached<Map<String, Player>> players;

    @Inject
    public GameService(LoggingProvider loggingProvider, DaoProvider daoProvider) {
        this.logger = loggingProvider.getLogger(GameService.class);

        this.gameDao = daoProvider.getDao(Game.class);
        this.playerDao = daoProvider.getDao(Player.class);
        this.leagueDao = daoProvider.getDao(League.class);

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

    public League getLeague(String leagueName) throws IOException {
        return this.leagueDao.get(
                QueryFilters.column("name").isEqualTo(leagueName)
        );
    }

    public List<League> getLeaguesForChallengeDay(DayOfWeek day) throws IOException {
        return this.leagueDao.list(
                QueryFilters.column("challenge_day").isEqualTo(day.toString())
        );
    }

    public void saveGame(Game game) throws IOException {
        this.gameDao.save(game);
    }

    public List<Game> getAllGames() throws IOException {
        return gameDao.list();
    }

    public List<Game> getAllGamesCreatedSince(Instant sinceWhen) throws IOException {
        return gameDao.list(
                QueryFilters.column("created_at").isGreaterThan(sinceWhen)
        );
    }

    public Optional<Game> getMostRecentGame() throws IOException {
        return gameDao.list()
                .stream()
                .max(Comparator.comparing(Game::getCreatedAt));
    }

    public List<Game> getGamesToPostResultsFor() throws IOException {
        return gameDao.list(
                QueryFilters.and(
                    QueryFilters.column("post_results_after").isLessThan(Dates.now()),
                    QueryFilters.column("results_posted").isEqualTo(false)
                )
        ).stream()
                .filter(game -> !game.getPlayerScores().isEmpty())
                .collect(Collectors.toList());
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

    public GameOption getRandomGameOption(League league){
        List<GameOption> gameOptions = league.getGameOptions();
        int totalWeight = gameOptions.stream()
                .mapToInt(GameOption::getWeighting)
                .sum();
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

}
