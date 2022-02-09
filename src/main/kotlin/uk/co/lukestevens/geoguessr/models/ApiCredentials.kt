package uk.co.lukestevens.geoguessr.models

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "api_credentials", schema = "geoguessr")
class ApiCredentials {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credentials_id")
    var id: Long? = null

    @Column(name = "username")
    var user: String? = null

    @Column(name = "device_token")
    var deviceToken: String? = null

    @Column(name = "ncfa")
    var ncfa: String? = null

    val cookieHeader: String
        get() = String.format(COOKIE_HEADER_TEMPLATE, deviceToken, ncfa)

    companion object {
        private const val COOKIE_HEADER_TEMPLATE = "devicetoken=%s; _ncfa=%s"
    }
}