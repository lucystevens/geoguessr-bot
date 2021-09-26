package uk.co.lukestevens;

import uk.co.lukestevens.db.Database;
import uk.co.lukestevens.db.DatabaseResult;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseUtils {

    final Database db;


    public DatabaseUtils(Database db) {
        this.db = db;
    }

    public void insertDbConfig(String key, String value) throws SQLException {
        String insertSql = "INSERT INTO core.config " +
                "(key, value, application_name, refresh_rate) " +
                "VALUES (?,?,?,?);";

        db.update(insertSql, key, value, "geoguessr-bot", 30);
    }

    public Long insertGameOption(String description, String map, int weighting, int timeLimit) throws SQLException {
        String insertSql = "INSERT INTO geoguessr.game_options " +
                "(description, map, weighting, time_limit) " +
                "VALUES (?,?,?,?);";

        return db.update(insertSql, description, map, weighting, timeLimit).get();
    }

    public Long insertGame(String challengeToken, Long gameOption, Date createdAt) throws SQLException {
        String insertSql = "INSERT INTO geoguessr.games " +
                "(challenge_token, game_option_id, created_at) " +
                "VALUES (?,?,?);";

        return db.update(insertSql, challengeToken, gameOption, createdAt).get();
    }

    public Long insertPlayer(String geoguessrId, String name) throws SQLException {
        String insertSql = "INSERT INTO geoguessr.players " +
                "(geoguessr_id, display_name) " +
                "VALUES (?,?);";

        return db.update(insertSql, geoguessrId, name).get();
    }

    public Long assertGameCreated(String challengeToken, Long gameOption, boolean resultsPosted) throws SQLException, IOException {
        String sql = "SELECT * FROM geoguessr.games WHERE challenge_token=?;";
        try(DatabaseResult dbr = db.query(sql, challengeToken)){
            ResultSet rs = dbr.getResultSet();
            assertTrue(rs.next(), "No rows returned for challenge token " + challengeToken);
            assertEquals(gameOption, rs.getLong("game_option_id"));
            assertNotNull(rs.getDate("created_at"));
            assertEquals(resultsPosted, rs.getBoolean("results_posted"));
            return rs.getLong("game_id");
        }
    }

    public Long assertPlayerCreated(String geoguessrId, String name) throws SQLException, IOException {
        String sql = "SELECT * FROM geoguessr.players WHERE geoguessr_id=?;";
        try(DatabaseResult dbr = db.query(sql, geoguessrId)){
            ResultSet rs = dbr.getResultSet();
            assertTrue(rs.next(), "No rows returned for geoguessr_id " + geoguessrId);
            assertEquals(name, rs.getString("display_name"));
            return rs.getLong("player_id");
        }
    }

    public Long assertPlayerScoreCreated(Long playerId, Long gameId, int score) throws SQLException, IOException {
        String sql = "SELECT * FROM geoguessr.player_scores WHERE player_id=? AND game_id=?;";
        try(DatabaseResult dbr = db.query(sql, playerId, gameId)){
            ResultSet rs = dbr.getResultSet();
            assertTrue(rs.next(), "No rows returned for player_id " + playerId + " and game_id " + gameId);
            assertEquals(score, rs.getInt("score"));
            return rs.getLong("player_score_id");
        }
    }

    public void clearDatabase() throws SQLException {
        db.update("DELETE FROM geoguessr.player_scores;");
        db.update("DELETE FROM geoguessr.players;");
        db.update("DELETE FROM geoguessr.games;");
        db.update("DELETE FROM geoguessr.game_options;");
    }
}
