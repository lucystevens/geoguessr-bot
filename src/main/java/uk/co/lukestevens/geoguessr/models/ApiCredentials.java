package uk.co.lukestevens.geoguessr.models;

import javax.persistence.*;

@Entity
@Table(name = "api_credentials", schema = "geoguessr")
public class ApiCredentials {

    private static final String COOKIE_HEADER_TEMPLATE = "devicetoken=%s; _ncfa=%s";

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "credentials_id")
    private Long id;

    @Column(name = "username")
    private String user;

    @Column(name = "device_token")
    private String deviceToken;

    @Column(name = "ncfa")
    private String ncfa;


    public String getCookieHeader(){
        return String.format(COOKIE_HEADER_TEMPLATE, deviceToken, ncfa);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public void setNcfa(String ncfa) {
        this.ncfa = ncfa;
    }
}
