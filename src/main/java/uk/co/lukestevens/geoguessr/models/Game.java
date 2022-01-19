package uk.co.lukestevens.geoguessr.models;

import uk.co.lukestevens.utils.Dates;

import javax.persistence.*;
import java.time.Instant;
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
    private Instant createdAt;

    @Column(name = "results_posted")
    private boolean resultsPosted;

    @Column(name = "post_results_after")
    private Instant postResultsAfter;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_option_id")
    private GameOption gameOption;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<PlayerScore> playerScores = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "league_id")
    private League league;

    public Game(String challengeToken, GameOption gameOption) {
        this.challengeToken = challengeToken;
        this.gameOption = gameOption;
        this.createdAt = Dates.now().toInstant();
    }

    public Game() { }

    public Long getId() {
        return id;
    }

    public String getChallengeToken() {
        return challengeToken;
    }

    public Instant getCreatedAt() {
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

    public League getLeague() {
        return league;
    }

    public void setLeague(League league) {
        this.league = league;
    }

    public void setPostResultsAfter(Instant postResultsAfter) {
        this.postResultsAfter = postResultsAfter;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setChallengeToken(String challengeToken) {
        this.challengeToken = challengeToken;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getPostResultsAfter() {
        return postResultsAfter;
    }

    public void setGameOption(GameOption gameOption) {
        this.gameOption = gameOption;
    }

    public void setPlayerScores(List<PlayerScore> playerScores) {
        this.playerScores = playerScores;
    }
}
