package uk.co.lukestevens.geoguessr.models

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "players", schema = "geoguessr")
class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "player_id")
    val id: Long? = null

    @Column(name = "geoguessr_id")
    var geoguessrId: String? = null

    @Column(name = "display_name")
    var displayName: String? = null

    @OneToMany(mappedBy = "player", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val playerScores: MutableList<PlayerScore> = ArrayList()

    constructor(geoguessrId: String?, displayName: String?) {
        this.geoguessrId = geoguessrId
        this.displayName = displayName
    }

    constructor()
}