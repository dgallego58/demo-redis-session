package co.com.dgallego58.application.security.context;

import co.com.dgallego58.core.util.JwtUtil;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

//@Component
public class ReactiveAuthenticationManagerService implements ReactiveAuthenticationManager {

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String tknVerified = JwtUtil.verifyAndGetPayload((String) authentication.getPrincipal());
        authentication.setAuthenticated(!tknVerified.isEmpty());
        return Mono.just(authentication);
    }
}
