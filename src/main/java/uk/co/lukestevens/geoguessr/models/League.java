package uk.co.lukestevens.geoguessr.models;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "leagues", schema = "geoguessr")
public class League {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "league_id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "slack_webhook")
    private String slackWebhook;

    @Column(name = "challenge_template")
    private String challengeTemplate;

    @Column(name = "challenge_day")
    @Enumerated(EnumType.STRING)
    private DayOfWeek challengeDay;

    @Column(name = "results_day")
    @Enumerated(EnumType.STRING)
    private DayOfWeek resultsDay;

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Game> games = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "league_game_options",
            joinColumns = { @JoinColumn(name = "league_id") },
            inverseJoinColumns = { @JoinColumn(name = "game_option_id") }
    )
    private List<GameOption> gameOptions = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "credentials_id")
    private ApiCredentials apiCredentials;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlackWebhook() {
        return slackWebhook;
    }

    public String getChallengeTemplate() {
        return challengeTemplate;
    }

    public DayOfWeek getChallengeDay() {
        return challengeDay;
    }

    public DayOfWeek getResultsDay() {
        return resultsDay;
    }

    public List<Game> getGames() {
        return games;
    }

    public void addGame(Game game) {
        this.games.add(game);
        game.setLeague(this);
    }

    public ApiCredentials getApiCredentials() {
        return apiCredentials;
    }

    public List<GameOption> getGameOptions() {
        return gameOptions;
    }
}
