package uk.co.lukestevens.geoguessr.stats;

import uk.co.lukestevens.geoguessr.models.Game;
import uk.co.lukestevens.geoguessr.models.PlayerScore;

import java.util.*;
import java.util.stream.Collectors;

public class GameStats {

    private final Game game;

    public GameStats(Game game) {
        this.game = game;
    }

    private int maxScore;
    private int minScore;
    private double averageScore;
    private List<String> places;

    public void generateStats(){
        IntSummaryStatistics scoreSummary = game.getPlayerScores()
                .stream()
                .mapToInt(PlayerScore::getScore)
                .summaryStatistics();
        maxScore = scoreSummary.getMax();
        minScore = scoreSummary.getMin();
        averageScore = scoreSummary.getAverage();

        places = game.getPlayerScores().stream()
                .sorted(Comparator.comparing(PlayerScore::getScore))
                .map(ps -> ps.getPlayer().getDisplayName())
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "GameStats{" +
                "game=" + game.getCreatedAt() +
                ", maxScore=" + maxScore +
                ", minScore=" + minScore +
                ", averageScore=" + averageScore +
                ", places=" + places +
                '}';
    }
}
