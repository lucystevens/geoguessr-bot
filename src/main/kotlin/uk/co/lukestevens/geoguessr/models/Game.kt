package uk.co.lukestevens.geoguessr.models

import uk.co.lukestevens.utils.Dates
import java.time.Instant
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "games", schema = "geoguessr")
class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    var id: Long? = null

    @Column(name = "challenge_token")
    var challengeToken: String? = null

    @Column(name = "created_at")
    var createdAt: Instant = Dates.now().toInstant()

    @Column(name = "results_posted")
    var isResultsPosted = false

    @Column(name = "post_results_after")
    var postResultsAfter: Instant? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_option_id")
    var gameOption: GameOption? = null

    @OneToMany(mappedBy = "game", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var playerScores: MutableList<PlayerScore> = ArrayList()

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "league_id")
    var league: League? = null

    constructor(challengeToken: String, gameOption: GameOption) {
        this.challengeToken = challengeToken
        this.gameOption = gameOption
        createdAt = Dates.now().toInstant()
    }

    constructor()
}