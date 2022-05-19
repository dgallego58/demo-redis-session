package co.com.dgallego58.infrastructure.input.filter;

import co.com.dgallego58.core.usecase.SessionUseCase;
import co.com.dgallego58.core.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Objects;

public class JwtWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtWebFilter.class);
    private final SessionUseCase sessionUseCase;

    public JwtWebFilter(SessionUseCase sessionUseCase) {
        this.sessionUseCase = sessionUseCase;
    }

    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        var request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String checkedAuthHeader = JwtUtil.stripAuth(authHeader);
        ServerHttpResponse response = exchange.getResponse();
        String sub = JwtUtil.getSub(checkedAuthHeader);
        log.warn("Sub - SessionID {}", sub);
        HttpCookie xAuthCookie = exchange.getRequest().getCookies()
                                         .getFirst("X-Auth-Token");
        if (Objects.isNull(xAuthCookie)) {
            log.warn("No Cookie for Authentication was found in the request, returning [401]");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        log.warn("cookie {}", xAuthCookie);

        return sessionUseCase.getCarrier(xAuthCookie.getValue())
                             .flatMap(jwt -> {
                                 String payload = JwtUtil.verifyAndGetPayload(jwt);
                                 if (payload.isEmpty()) {
                                     log.warn("Token was invalid, returning [403]");
                                     response.setStatusCode(HttpStatus.FORBIDDEN);
                                     return response.setComplete();
                                 }
                                 return chain.filter(exchange);
                             });
    }


}
