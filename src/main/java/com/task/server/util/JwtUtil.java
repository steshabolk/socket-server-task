package com.task.server.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.task.server.dto.ClientRequest;
import com.task.server.exception.ApiExceptionType;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;
    @Value("${app.jwt.ttl}")
    private long ttl;
    @Value("${app.jwt.header}")
    private String tokenHeader;

    public String generateToken(String login) {
        return JWT.create()
            .withSubject(login)
            .withExpiresAt(Date.from(Instant.now().plus(ttl, ChronoUnit.MINUTES)))
            .sign(Algorithm.HMAC256(secret));
    }

    public String parseLoginFromTokenHeader(ClientRequest request) {
        String token = request.headers().get(tokenHeader);
        if (token == null) {
            throw ApiExceptionType.MISSING_TOKEN.toException();
        }
        DecodedJWT decodedJWT = validateToken(token);
        return getLoginFromJWT(decodedJWT);
    }

    private DecodedJWT validateToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);
        } catch (TokenExpiredException ex) {
            throw ApiExceptionType.TOKEN_EXPIRED.toException();
        } catch (Exception ex) {
            throw ApiExceptionType.INVALID_TOKEN.toException();
        }
    }

    private String getLoginFromJWT(DecodedJWT jwt) {
        String login = jwt.getSubject();
        if (login == null) {
            throw ApiExceptionType.INVALID_TOKEN.toException();
        }
        return login;
    }
}
