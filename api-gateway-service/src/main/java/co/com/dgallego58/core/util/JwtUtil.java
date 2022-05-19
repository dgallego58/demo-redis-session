package co.com.dgallego58.core.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Consumer;

public class JwtUtil implements JwtFactory {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private static final Algorithm signer;

    static {
        String secret = System.getProperty("jwt-secret");
        signer = Algorithm.HMAC256(secret);
    }

    private final Algorithm algorithm;
    private final JWTCreator.Builder creator;


    private JwtUtil(JwtUtilBuilder builder) {
        this.algorithm = builder.algorithm;
        this.creator = builder.creator;
    }

    private static JwtUtilBuilder builder() {
        return new JwtUtilBuilder();
    }

    public static JwtFactory create(Consumer<Builder> builderConsumer) {
        var builder = builder();
        builderConsumer.accept(builder);
        return builder.create();
    }

    public static String stripAuth(String authorizationHeader) {
        return authorizationHeader == null ? "" : authorizationHeader.split(" ")[1];
    }

    public static String verifyAndGetPayload(String authHeader) {
        try {
            String token = stripAuth(authHeader);

            log.info("authHeader is empty? {}", token.isEmpty());
            var decodedTkn = JWT.require(signer).build().verify(token);
            return new String(Base64.getDecoder().decode(decodedTkn.getPayload().getBytes(StandardCharsets.UTF_8)));

        } catch (JWTVerificationException e) {
            log.error("we couldn't verify the authHeader, therefore we returned an empty string", e);
            return "";
        }
    }

    public static String getPayloadWithNoVerification(String tkn) {
        log.info("tkn payload {}", tkn);
        if (tkn == null) {
            return "";
        }
        var tokenAsBytes = JWT.decode(tkn).getPayload().getBytes(StandardCharsets.UTF_8);
        var decodedTkn = Base64.getDecoder().decode(tokenAsBytes);
        return new String(decodedTkn);
    }

    public static Instant getIat(String tkn) {
        return JWT.decode(tkn).getIssuedAt().toInstant();
    }

    public static String getSub(String tkn){
        return JWT.decode(tkn).getSubject();
    }
    public static Instant getExpiration(String token) {
        var dateExpiration = JWT.decode(token).getExpiresAt();
        return dateExpiration.toInstant();
    }

    @Override
    public String signAndGet() {
        try {
            return creator.sign(algorithm);
        } catch (JWTCreationException exception) {
            log.error("Cannot be created", exception);
            return "";
        }
    }

    public enum TokenType {
        BEARER("Bearer "), BASIC("Basic "), EMPTY("");

        private final String type;

        TokenType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public static class JwtUtilBuilder implements JwtFactory.Builder {
        private final Algorithm algorithm;

        private final JWTCreator.Builder creator;

        public JwtUtilBuilder() {
            this.creator = JWT.create();
            this.algorithm = signer;
        }

        @Override
        public JwtFactory.Builder withExpiration(Duration expiration) {
            var expirateAt = Instant.now().plus(expiration);
            creator.withExpiresAt(Date.from(expirateAt));
            return this;
        }

        @Override
        public JwtFactory.Builder withIssuedAt(Instant issuedAt) {
            creator.withIssuedAt(Date.from(issuedAt));
            return this;
        }

        @Override
        public JwtFactory.Builder withClaims(Map<String, Object> claims) {
            creator.withPayload(claims);
            return this;
        }

        @Override
        public Builder withSub(String sub) {
            creator.withSubject(sub);
            return this;
        }

        @Override
        public JwtFactory create() {
            return new JwtUtil(this);
        }
    }

}
