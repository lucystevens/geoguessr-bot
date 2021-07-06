package uk.co.lukestevens.geoguessr.models;

public class ChallengeDefinition {

    private final String map;
    private final boolean forbidMoving = false;
    private final boolean forbidRotating = false;
    private final boolean forbidZooming = false;
    private final int timeLimit;

    public ChallengeDefinition(String map, int timeLimit) {
        this.map = map;
        this.timeLimit = timeLimit;
    }
}
