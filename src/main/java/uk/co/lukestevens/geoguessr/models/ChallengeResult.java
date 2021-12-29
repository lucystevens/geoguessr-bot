package uk.co.lukestevens.geoguessr.models;

public class ChallengeResult {

    private String gameToken;
    private String playerName;
    private String userId;
    private int totalScore;

    public String getGameToken() {
        return gameToken;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getUserId() {
        return userId;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public ChallengeResult setGameToken(String gameToken) {
        this.gameToken = gameToken;
        return this;
    }

    public ChallengeResult setPlayerName(String playerName) {
        this.playerName = playerName;
        return this;
    }

    public ChallengeResult setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public ChallengeResult setTotalScore(int totalScore) {
        this.totalScore = totalScore;
        return this;
    }
}
