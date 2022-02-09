package uk.co.lukestevens.geoguessr.models

class ChallengeDefinition(private val map: String, private val timeLimit: Int) {
    private val forbidMoving = false
    private val forbidRotating = false
    private val forbidZooming = false
}