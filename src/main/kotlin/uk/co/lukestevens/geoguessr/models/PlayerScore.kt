package uk.co.lukestevens.geoguessr.models

import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import uk.co.lukestevens.geoguessr.models.ApiCredentials
import uk.co.lukestevens.geoguessr.models.ChallengeResult
import java.time.Instant
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import uk.co.lukestevens.geoguessr.models.GameOption
import uk.co.lukestevens.geoguessr.models.PlayerScore
import uk.co.lukestevens.geoguessr.models.League
import javax.persistence.Enumerated
import java.time.DayOfWeek
import uk.co.lukestevens.geoguessr.models.Game
import javax.persistence.ManyToMany
import javax.persistence.JoinTable
import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import uk.co.lukestevens.geoguessr.models.Player
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "player_scores", schema = "geoguessr")
class PlayerScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "player_score_id")
    val id: Long? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    var game: Game? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    var player: Player? = null

    @Column(name = "score")
    var score = 0

    constructor(game: Game, player: Player) {
        this.game = game.apply { playerScores.add(this@PlayerScore) }
        this.player = player.apply { playerScores.add(this@PlayerScore) }
    }

    constructor()
}