package co.com.dgallego58.core.util;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

public interface JwtFactory {

    String signAndGet();

    interface Builder {
        Builder withExpiration(Duration expiration);

        Builder withIssuedAt(Instant issuedAt);

        Builder withClaims(Map<String, Object> claims);

        Builder withSub(String sub);

        JwtFactory create();

    }
}
