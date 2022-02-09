package uk.co.lukestevens.geoguessr

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import uk.co.lukestevens.config.Config
import uk.co.lukestevens.geoguessr.models.ApiCredentials
import uk.co.lukestevens.geoguessr.models.ChallengeResult
import uk.co.lukestevens.geoguessr.models.Game
import uk.co.lukestevens.geoguessr.models.GameOption
import uk.co.lukestevens.geoguessr.models.League
import uk.co.lukestevens.geoguessr.services.GameService
import uk.co.lukestevens.geoguessr.services.GeoGuessrService
import uk.co.lukestevens.geoguessr.services.SlackService
import uk.co.lukestevens.logging.LoggerLevel
import uk.co.lukestevens.logging.provider.ConsoleLoggingProvider
import uk.co.lukestevens.testing.mocks.DateMocker
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant

class GeoGuesserBotTest {

    private val df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    private val config = mockk<Config>()
    private val gameService = mockk<GameService>(relaxed = true)
    private val geoGuessrService = mockk<GeoGuessrService>()
    private val slackService = mockk<SlackService>(relaxed = true)
    private val geoGuessrBot = GeoGuessrBot(
        ConsoleLoggingProvider(LoggerLevel.DEBUG),
        config,
        gameService,
        geoGuessrService,
        slackService
    )

    @Test
    fun createChallenges_noChallenges_doesNothing() {
        // Set date to Friday
        DateMocker.setCurrentDate(df.parse("2022-01-07 07:00:00"))
        every { gameService.getLeaguesForChallengeDay(DayOfWeek.FRIDAY) } returns listOf()

        geoGuessrBot.createChallenges()
        verify(exactly = 0) { geoGuessrService.createChallenge(any(), any()) }
    }

    @Test
    fun createChallenges_singleChallenge_createsChallenge() {
        // Set date to Friday
        DateMocker.setCurrentDate(df.parse("2022-01-07 07:00:00"))
        val credentials = ApiCredentials().apply { id = 2L }
        val league = League().apply {
            id = 1
            resultsDay = DayOfWeek.MONDAY
            apiCredentials = credentials
        }
        val gameOption = GameOption().apply { id = 3 }
        val game = Game().apply { id = 4 }

        every { gameService.getLeaguesForChallengeDay(DayOfWeek.FRIDAY) } returns listOf(league)
        every { gameService.getRandomGameOption(league) } returns gameOption
        every { geoGuessrService.createChallenge(gameOption, credentials) } returns game

        geoGuessrBot.createChallenges()

        verify(exactly = 1) { gameService.saveGame(game) }
        verify(exactly = 1) { slackService.sendChallengeMessage(game) }

        assertEquals(Instant.parse("2022-01-10T00:00:00Z"), game.postResultsAfter)
    }

    @Test
    fun postChallengeResults_noGames_doesNothing(){
        every { gameService.gamesToPostResultsFor } returns listOf()

        geoGuessrBot.postChallengeResults()

        verify(exactly = 0) { slackService.sendResultsMessage(any()) }
        verify(exactly = 0) { gameService.saveGame(any()) }
    }

    @Test
    fun postChallengeResults_withGames_postsResults(){
        val game1 = Game().apply{ id = 1}
        val game2 = Game().apply{ id = 2}
        every { gameService.gamesToPostResultsFor } returns listOf(game1, game2)

        geoGuessrBot.postChallengeResults()

        verify(exactly = 1) { slackService.sendResultsMessage(game1) }
        verify(exactly = 1) { gameService.saveGame(game1) }
        assertTrue(game1.isResultsPosted)

        verify(exactly = 1) { slackService.sendResultsMessage(game2) }
        verify(exactly = 1) { gameService.saveGame(game2) }
        assertTrue(game2.isResultsPosted)
    }

    @Test
    fun updateChallengeResults_lastDayOfMonth_updatesFromWholeMonth(){
        DateMocker.setCurrentDate(df.parse("2022-01-31 08:00:00"))
        val expectedDate = Instant.parse("2022-01-01T00:00:00Z")
        every { gameService.getAllGamesCreatedSince(expectedDate) } returns listOf()

        geoGuessrBot.updateChallengeResults()
        verify { gameService.getAllGamesCreatedSince(expectedDate) }
    }

    @Test
    fun updateChallengeResults_firstDayOfMonth_updatesLastWeek(){
        DateMocker.setCurrentDate(df.parse("2022-02-01 09:00:00"))
        val expectedDate = Instant.parse("2022-01-25T00:00:00Z")
        every { gameService.getAllGamesCreatedSince(expectedDate) } returns listOf()

        geoGuessrBot.updateChallengeResults()
        verify { gameService.getAllGamesCreatedSince(expectedDate) }
    }

    @Test
    fun updateChallengeResults_firstDayOfYear_updatesLastWeek(){
        DateMocker.setCurrentDate(df.parse("2022-01-01 15:00:00"))
        val expectedDate = Instant.parse("2021-12-25T00:00:00Z")
        every { gameService.getAllGamesCreatedSince(expectedDate) } returns listOf()

        geoGuessrBot.updateChallengeResults()
        verify { gameService.getAllGamesCreatedSince(expectedDate) }
    }

    /*
    updateChallengeResults_noResults
    updateChallengeResults_multipleGames
     */

    @Test
    fun updateChallengeResults_noResults_doesNothing(){
        val sinceWhen = Instant.parse("2021-01-07T00:00:00Z")
        val credentials = ApiCredentials().apply { id = 1L }
        val league = League().apply {
            id = 2
            apiCredentials = credentials
        }
        val game = Game().apply {
            id = 3
            challengeToken = "test-token"
            this.league = league
        }

        every { gameService.getAllGamesCreatedSince(sinceWhen) } returns listOf(game)
        every { geoGuessrService.getResults("test-token", credentials) } returns listOf()

        verify(exactly = 0) { gameService.updateResultsForGame(any(), any()) }
        assertFalse(game.isResultsPosted)
    }

    //@Test
    // TODO updateChallengeResults does not post results
    fun updateChallengeResults_multipleGames_postsMultipleResults(){
        val sinceWhen = Instant.parse("2021-01-07T00:00:00Z")
        val credentials1 = ApiCredentials().apply { id = 1L }
        val league1 = League().apply {
            id = 2
            apiCredentials = credentials1
        }
        val game1 = Game().apply {
            id = 3
            challengeToken = "test-token1"
            this.league = league1
        }
        val result1 = ChallengeResult().apply { userId = "user1" }
        val result2 = ChallengeResult().apply { userId = "user2" }

        val credentials2 = ApiCredentials().apply { id = 4L }
        val league2 = League().apply {
            id = 5
            apiCredentials = credentials2
        }
        val game2 = Game().apply {
            id = 6
            challengeToken = "test-token2"
            this.league = league2
        }
        val result3 = ChallengeResult().apply { userId = "user3" }
        val result4 = ChallengeResult().apply { userId = "user4" }

        every { gameService.getAllGamesCreatedSince(sinceWhen) } returns listOf(game1, game2)
        every { geoGuessrService.getResults("test-token1", credentials1) } returns listOf(result1, result2)
        every { geoGuessrService.getResults("test-token2", credentials2) } returns listOf(result3, result4)

        verify(exactly = 1) { gameService.updateResultsForGame(game1, listOf(result1, result2)) }
        assertTrue(game1.isResultsPosted)

        verify(exactly = 1) { gameService.updateResultsForGame(game2, listOf(result3, result4)) }
        assertTrue(game2.isResultsPosted)
    }

}