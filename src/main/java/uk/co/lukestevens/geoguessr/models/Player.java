package uk.co.lukestevens.geoguessr.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "players", schema = "geoguessr")
public class Player {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "player_id")
    private Long id;

    @Column(name = "geoguessr_id")
    private String geoguessrId;

    @Column(name = "display_name")
    private String displayName;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PlayerScore> playerScores = new ArrayList<>();

    public Player(String geoguessrId, String displayName) {
        this.geoguessrId = geoguessrId;
        this.displayName = displayName;
    }

    public Player() { }

    public Long getId() {
        return id;
    }

    public String getGeoguessrId() {
        return geoguessrId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<PlayerScore> getPlayerScores() {
        return playerScores;
    }
}
