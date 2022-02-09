package uk.co.lukestevens.geoguessr.models

import org.hibernate.annotations.LazyCollection
import org.hibernate.annotations.LazyCollectionOption
import java.time.DayOfWeek
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "leagues", schema = "geoguessr")
class League {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "league_id")
    var id: Long? = null

    @Column(name = "name")
    var name: String? = null

    @Column(name = "slack_webhook")
    var slackWebhook: String? = null

    @Column(name = "challenge_template")
    var challengeTemplate: String? = null

    @Column(name = "challenge_day")
    @Enumerated(EnumType.STRING)
    var challengeDay: DayOfWeek? = null

    @Column(name = "results_day")
    @Enumerated(EnumType.STRING)
    var resultsDay: DayOfWeek? = null

    @OneToMany(mappedBy = "league", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var games: MutableList<Game> = ArrayList()

    @ManyToMany
    @JoinTable(
        name = "geoguessr.league_game_options",
        joinColumns = [JoinColumn(name = "league_id")],
        inverseJoinColumns = [JoinColumn(name = "game_option_id")]
    )
    @LazyCollection(LazyCollectionOption.FALSE) // TODO remove with migration to v3 libs
    var gameOptions: List<GameOption> = ArrayList()

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "credentials_id")
    var apiCredentials: ApiCredentials? = null

    fun addGame(game: Game) {
        games.add(game)
        game.league = this
    }

}