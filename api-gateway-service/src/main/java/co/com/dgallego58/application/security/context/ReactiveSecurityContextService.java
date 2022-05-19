package co.com.dgallego58.application.security.context;

import co.com.dgallego58.core.util.JwtUtil;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

//@Component
public class ReactiveSecurityContextService implements ServerSecurityContextRepository {

    private final ReactiveAuthenticationManager authenticationManager;
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public ReactiveSecurityContextService(ReactiveAuthenticationManager authenticationManager,
                                          ReactiveRedisTemplate<String, String> redisTemplate) {
        this.authenticationManager = authenticationManager;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.error(new UnsupportedOperationException("Not implemented"));
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {

        ServerHttpRequest request = exchange.getRequest();
        String bearerAuth = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerAuth == null) {
            return Mono.error(() -> new IllegalStateException("No token requested"));
        }
        AuthImpl auth = AuthImpl.builder().principal(JwtUtil.stripAuth(bearerAuth)).build();
        return authenticationManager.authenticate(auth)
                                    .map(SecurityContextImpl::new);
    }

}
