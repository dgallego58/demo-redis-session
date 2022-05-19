package co.com.dgallego58.infrastructure.input.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SessionValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) ->
                exchange.getSession()
                        .map(session -> {
                            var xAuthTkn = session.getId();
                            log.info("x-auth-api-gw-token: {}", xAuthTkn);
                            return session;
                        })
                        .flatMap(session -> chain.filter(exchange))
                        .then(Mono.fromRunnable(() -> log.debug("after Session")));

    }
}
