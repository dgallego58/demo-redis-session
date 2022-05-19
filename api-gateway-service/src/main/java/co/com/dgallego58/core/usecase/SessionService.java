package co.com.dgallego58.core.usecase;

import co.com.dgallego58.core.model.Security;
import co.com.dgallego58.core.model.Store;
import co.com.dgallego58.core.model.dto.SessionCarrier;
import co.com.dgallego58.core.util.JacksonUtil;
import co.com.dgallego58.core.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public class SessionService implements SessionUseCase {

    private static final Duration TWO_MINUTES = Duration.ofMinutes(2);
    private static final Logger log = LoggerFactory.getLogger(SessionService.class);
    private final Security security;
    private final Store store;


    public SessionService(Security security, Store store) {
        this.security = security;
        this.store = store;
    }

    @Override
    public Mono<SessionCarrier> exchangeTkn(String sessionId, String tknCA) {

        return security.getForCA(tknCA)
                       .flatMap(node -> {
                           String username = node.get("username").asText();
                           String token = getTkn(sessionId, username, Instant.now());
                           var carrier = SessionCarrier.builder()
                                                       .sessionId(sessionId)
                                                       .token(token)
                                                       .lastLoggedIn(Instant.now())
                                                       .build();
                           log.warn("carrier: {}", carrier);
                           return store.save(carrier);
                       });
    }

    @Override
    public Mono<String> getCarrier(String sessionId) {
        return store.getValue(sessionId)
                    .map(SessionCarrier::getToken);
    }

    @Override
    public Mono<Boolean> checkToken(String jwt) {
        String payload = JwtUtil.verifyAndGetPayload(jwt);
        Mono<SessionCarrier> deserialize = JacksonUtil.deserialize(payload, SessionCarrier.class);
        return deserialize.map(Objects::isNull);
    }

    private String getTkn(String sessionId, String username, Instant iat) {
        var tkn = JwtUtil.create(builder -> builder.withSub(sessionId)
                                                   .withClaims(Map.of("username", username))
                                                   .withExpiration(TWO_MINUTES)
                                                   .withIssuedAt(iat))
                         .signAndGet();
        log.debug(tkn);
        return tkn;
    }
}
