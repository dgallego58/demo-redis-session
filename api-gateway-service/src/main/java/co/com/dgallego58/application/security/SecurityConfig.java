package co.com.dgallego58.application.security;


import co.com.dgallego58.core.usecase.SessionUseCase;
import co.com.dgallego58.infrastructure.input.filter.JwtWebFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.data.redis.ReactiveRedisSessionRepository;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;
import reactor.core.publisher.Mono;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private static final String[] IGNORED_PATHS = {"/access/sign-in",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/webjars/swagger-ui/**"
    };


    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http,
                                                      SessionUseCase sessionUseCase) {

        return http.securityMatcher(new NegatedServerWebExchangeMatcher(pathMatchers(IGNORED_PATHS)))
                   .addFilterAt(new JwtWebFilter(sessionUseCase), SecurityWebFiltersOrder.AUTHENTICATION)
                   .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                   .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                   .exceptionHandling(handler -> {
                       handler.accessDeniedHandler((exchange, denied) -> forbid(exchange));
                       handler.authenticationEntryPoint((exchange, ex) -> deny(exchange));
                   })
                   .cors()
                   .and()
                   .csrf(ServerHttpSecurity.CsrfSpec::disable)
                   .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                   .requestCache(cache -> cache.requestCache(NoOpServerRequestCache.getInstance()))
                   //.requestCache(cache -> cache.requestCache(new CookieServerRequestCache()))
                   .build();
    }

    @Bean
    public CorsConfigurationSource corsWebFilter(GlobalCorsProperties globalCorsProperties) {
        var config = new UrlBasedCorsConfigurationSource();
        globalCorsProperties.getCorsConfigurations().forEach(config::registerCorsConfiguration);
        return config;
    }

    public Mono<Void> deny(ServerWebExchange swe) {
        return Mono.fromRunnable(() -> {
            log.warn("Unauthorized access [401] {}", swe.getRequest().getPath());
            swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        });
    }

    public Mono<Void> forbid(ServerWebExchange swe) {
        return Mono.fromRunnable(() -> {
            log.warn("Forbidden access [403] {}", swe.getRequest().getPath());
            swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        });
    }


    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        var sessionIdResolver = new CookieWebSessionIdResolver();
        sessionIdResolver.addCookieInitializer(rcb -> rcb.secure(false).httpOnly(true).sameSite("Lax"));
        sessionIdResolver.setCookieName("X-Auth-Token");
        return sessionIdResolver;
    }



}
