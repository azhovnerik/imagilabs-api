package com.anahoret.imagilabsapi.auth.web.jwt

import com.anahoret.imagilabsapi.users.UserType
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.ZonedDateTime
import java.util.*

@Component
class JwtTokenUtil(
    private val jwtProperties: JwtProperties
) {

    private val algorithm = Algorithm.HMAC256(jwtProperties.secret)
    private val jwtVerifier = JWT.require(algorithm).build()

    companion object {

        const val USER_TYPE_CLAIM = "userType"
    }

    fun createToken(
        userId: UUID,
        userType: UserType,
        tokenTTL: Duration = jwtProperties.ttlWeb
    ): JwtTokenData {
        val expiresAt = (ZonedDateTime.now().plusSeconds(20)) //ZonedDateTime.now().plus(tokenTTL) //changing this to test unauthorized state on the app side
        val token = JWT.create()
            .withSubject(userId.toString())
            .withExpiresAt(expiresAt)
            .withClaim(USER_TYPE_CLAIM, userType.name)
            .sign(algorithm)
        val expiresAtMillis = expiresAt.toInstant().toEpochMilli()
        return JwtTokenData(token, expiresAtMillis)
    }

    fun verifyAndDecode(token: String): DecodedJWT? {
        return try {
            jwtVerifier.verify(token)
        } catch (t: Throwable) {
            null
        }
    }

    fun JWTCreator.Builder.withExpiresAt(dt: ZonedDateTime): JWTCreator.Builder {
        withExpiresAt(Date.from(dt.toInstant()))
        return this
    }

}

fun DecodedJWT.getUserType(): UserType? {
    return getClaim(JwtTokenUtil.USER_TYPE_CLAIM).asString()?.let { UserType.valueOf(it) }
}
