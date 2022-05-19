package co.com.dgallego58.infrastructure.output;

import co.com.dgallego58.core.model.Store;
import co.com.dgallego58.core.model.dto.SessionCarrier;
import co.com.dgallego58.core.util.JacksonUtil;
import co.com.dgallego58.core.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.UnaryOperator;

@Service
public class RedisAdapter implements Store {
    public static final UnaryOperator<String> LOGIN_KEY = key -> StringUtils.getIfEmpty(
            String.format("netw:user:%s", key), () -> "rejected");
    private static final Logger log = LoggerFactory.getLogger(RedisAdapter.class);
    private static final Duration FIVE_MINUTES = Duration.ofMinutes(5);
    private final ReactiveRedisOperations<String, String> redisTemplate;

    public RedisAdapter(ReactiveRedisOperations<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<SessionCarrier> save(final SessionCarrier sessionCarrier) {
        String jwt = sessionCarrier.getToken();
        var jwtExpiration = JwtUtil.getExpiration(jwt);
        //adds five minutes of duration to redis session
        var redisExpiration = jwtExpiration.plus(FIVE_MINUTES);
        var expiration = Duration.between(jwtExpiration, redisExpiration);

        String sessionCarrierAsJson = JacksonUtil.SERIALIZE.apply(sessionCarrier);
        log.warn("JSON Carrier: {}", sessionCarrierAsJson);
        return redisTemplate.opsForValue()
                            .set(LOGIN_KEY.apply(sessionCarrier.getSessionId()), sessionCarrierAsJson, expiration)
                            .flatMap(val -> getValue(sessionCarrier.getSessionId()));
    }

    @Override
    public Mono<SessionCarrier> getValue(String uuidTkn) {
        log.info("Called with {}", uuidTkn);
        return redisTemplate.opsForValue()
                            .get(LOGIN_KEY.apply(uuidTkn))
                            .flatMap(value -> JacksonUtil.deserialize(value, SessionCarrier.class));
    }

}
