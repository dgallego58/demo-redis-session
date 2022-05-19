package co.com.dgallego58.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.stream.Stream;

@Configuration
@EnableRedisHttpSession(redisNamespace = "netw:security")
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private static final String[] excludedPaths = {"/session/{id}", "/session/authenticate"};


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.formLogin().disable();
        http.httpBasic().disable();
        http.addFilterAt(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
        http.authorizeHttpRequests(auth -> auth.mvcMatchers("/session/{id}", "/session/authenticate").permitAll()
                                               .anyRequest().authenticated());
        http.csrf().disable();
        //    http.userDetailsService(userDetailsRepository());
        //    http.authenticationManager(authenticationManager());
    /*    http.securityContext()
            .securityContextRepository(securityContextRepository());*/

        http.sessionManagement(mng -> mng.sessionConcurrency(concurrencyConf -> concurrencyConf.maximumSessions(1)));
        return http.build();
    }

    public OncePerRequestFilter jwtFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (authHeader == null) {
                    log.warn("Unauthorized... no token has found in request");
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    return;
                }
                String token = authHeader.split(" ")[1];
                try {
                    Algorithm secret = Algorithm.HMAC512(System.getProperty("jwt-secret"));
                    var decodedToken = JWT.require(secret)
                                          .build()
                                          .verify(token);
                    String jsonPayload = new String(
                            Base64.getDecoder().decode(decodedToken.getPayload().getBytes(StandardCharsets.UTF_8)));
                    log.info("payload: {}", jsonPayload);
                    var tree = JsonMapper.builder().build().readTree(jsonPayload);

                    var auth = AuthenticationImpl.builder()
                                                 .authenticated(true)
                                                 .principal(tree.get("username"))
                                                 .build();

                    var authenticated = authenticationManager().authenticate(auth);
                    var ctx = SecurityContextHolder.createEmptyContext();
                    ctx.setAuthentication(authenticated);
                    SecurityContextHolder.setContext(ctx);
                } catch (JWTVerificationException e) {
                    log.warn("Invalid Token", e);
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    return;
                }

                filterChain.doFilter(request, response);
            }

            @Override
            protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
                var ppp = new PathPatternRouteMatcher();
                String requestPath = request.getRequestURI().substring(request.getContextPath().length());
                log.info("RequestPath {}", requestPath);
                return Stream.of(excludedPaths)
                             .anyMatch(s -> ppp.match(s, ppp.parseRoute(requestPath)));
            }
        };
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return authentication -> authentication;
    }

    public SecurityContextRepository securityContextRepository() {
        return new SecurityContextRepository() {
            @Override
            public SecurityContext loadContext(HttpRequestResponseHolder requestResponseHolder) {
                return SecurityContextHolder.getContext();
            }

            @Override
            public void saveContext(SecurityContext context, HttpServletRequest request, HttpServletResponse response) {
                authenticationManager();

            }

            @Override
            public boolean containsContext(HttpServletRequest request) {
                return true;
            }
        };
    }

    @Bean
    public UserDetailsService userDetailsRepository() {
        UserDetails user = User.withUsername("user").password("{noop}password").roles("USER").build();
        UserDetails admin = User.withUsername("admin").password("{noop}password").roles("USER", "ADMIN").build();
        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return new HeaderHttpSessionIdResolver("X-Auth-Security-Service-Token");
    }

    @Builder
    @Getter
    public static class AuthenticationImpl implements Authentication {
        private final String name;
        private final String password;
        private final Object credentials;
        private final Object principal;
        private final Collection<? extends GrantedAuthority> authorities;
        private final Object details;
        private boolean authenticated;

        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            this.authenticated = isAuthenticated;
        }
    }

}
