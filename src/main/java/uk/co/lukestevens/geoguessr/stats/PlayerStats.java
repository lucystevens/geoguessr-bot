package uk.co.lukestevens.geoguessr.stats;

import uk.co.lukestevens.geoguessr.models.Player;
import uk.co.lukestevens.geoguessr.models.PlayerScore;

import java.util.Comparator;
import java.util.IntSummaryStatistics;

public class PlayerStats {

    private final Player player;

    public PlayerStats(Player player) {
        this.player = player;
    }

    private int maxScore;
    private int minScore;
    private double averageScore;
    private String bestMap;
    private long gamesPlayed;
    private long wins;

    public void generateStats(){
        IntSummaryStatistics scoreSummary = player.getPlayerScores()
                .stream()
                .mapToInt(PlayerScore::getScore)
                .summaryStatistics();
        maxScore = scoreSummary.getMax();
        minScore = scoreSummary.getMin();
        averageScore = scoreSummary.getAverage();
        gamesPlayed = scoreSummary.getCount();
        bestMap = player.getPlayerScores().stream()
                .filter(ps -> ps.getScore() == maxScore)
                .map(ps -> ps.getGame().getGameOption().getDescription())
                .findFirst().orElse("None");
        wins = player.getPlayerScores().stream()
                .map(PlayerScore::getGame)
                .filter(game ->
                        game.getPlayerScores().stream()
                                .max(Comparator.comparingInt(PlayerScore::getScore))
                                .get().getPlayer().equals(player)

                ).count();
    }

    @Override
    public String toString() {
        return "PlayerStats{" +
                "player=" + player.getDisplayName() +
                ", maxScore=" + maxScore +
                ", minScore=" + minScore +
                ", averageScore=" + averageScore +
                ", bestMap='" + bestMap + '\'' +
                ", gamesPlayed=" + gamesPlayed +
                ", wins=" + wins +
                '}';
    }
}
