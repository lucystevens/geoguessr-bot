package uk.co.lukestevens.geoguessr.models;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.GenericGenerator;
import uk.co.lukestevens.utils.Dates;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "games", schema = "geoguessr")
public class Game {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "game_id")
    private Long id;

    @Column(name = "challenge_token")
    private String challengeToken;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "results_posted")
    private boolean resultsPosted;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_option_id")
    private GameOption gameOption;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PlayerScore> playerScores = new ArrayList<>();

    public Game(String challengeToken, GameOption gameOption) {
        this.challengeToken = challengeToken;
        this.gameOption = gameOption;
        this.createdAt = Dates.now();
    }

    public Game() { }

    public Long getId() {
        return id;
    }

    public String getChallengeToken() {
        return challengeToken;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setResultsPosted(boolean resultsPosted) {
        this.resultsPosted = resultsPosted;
    }

    public boolean isResultsPosted() {
        return resultsPosted;
    }

    public GameOption getGameOption() {
        return gameOption;
    }

    public List<PlayerScore> getPlayerScores() {
        return playerScores;
    }
}
