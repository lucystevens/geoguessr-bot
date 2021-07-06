package uk.co.lukestevens.geoguessr.models;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "player_scores", schema = "geoguessr")
public class PlayerScore {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "player_score_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    private Game game;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id")
    private Player player;

    @Column(name = "score")
    private int score;

    public PlayerScore(Game game, Player player) {
        this.game = game;
        this.game.getPlayerScores().add(this);

        this.player = player;
        this.player.getPlayerScores().add(this);
    }

    public PlayerScore() { }

    public Long getId() {
        return id;
    }

    public Game getGame() {
        return game;
    }

    public Player getPlayer() {
        return player;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}
