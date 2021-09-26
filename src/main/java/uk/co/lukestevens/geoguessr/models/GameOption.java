package uk.co.lukestevens.geoguessr.models;

import javax.persistence.*;

@Entity
@Table(name = "game_options", schema = "geoguessr")
public class GameOption {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "game_option_id")
    private Long id;

    @Column(name = "description")
    private String description;

    @Column(name = "map")
    private String map;

    @Column(name = "time_limit")
    private int timeLimit;

    @Column(name = "weighting")
    private int weighting;

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getMap() {
        return map;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public int getWeighting() {
        return weighting;
    }
}
