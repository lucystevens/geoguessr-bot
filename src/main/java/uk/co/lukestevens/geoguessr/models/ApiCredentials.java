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

    @Column(name = "user")
    private String user;

    @Column(name = "device_token")
    private String deviceToken;

    @Column(name = "ncfa")
    private String ncfa;


    public String getCookieHeader(){
        return String.format(COOKIE_HEADER_TEMPLATE, deviceToken, ncfa);
    }
}
