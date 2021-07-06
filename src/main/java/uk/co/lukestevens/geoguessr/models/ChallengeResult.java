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
}
