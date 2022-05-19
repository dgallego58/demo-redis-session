package co.com.dgallego58.application.security.context;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Builder
@Getter
public class AuthImpl implements Authentication{
    private final String name;
    private final String credentials;
    private final String details;
    private final String principal;
    private boolean authenticated;
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }
}
