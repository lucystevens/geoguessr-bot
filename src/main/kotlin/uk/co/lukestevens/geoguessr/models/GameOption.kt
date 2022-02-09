package uk.co.lukestevens.geoguessr.models

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "game_options", schema = "geoguessr")
class GameOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_option_id")
    var id: Long? = null

    @Column(name = "description")
    var description: String? = null

    @Column(name = "map")
    var map: String? = null

    @Column(name = "time_limit")
    var timeLimit = 0

    @Column(name = "weighting")
    var weighting = 0
}