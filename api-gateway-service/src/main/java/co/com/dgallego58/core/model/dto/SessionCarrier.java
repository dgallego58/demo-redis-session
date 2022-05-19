package co.com.dgallego58.core.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Getter
@Builder(toBuilder = true)
@Jacksonized
@ToString
public class SessionCarrier {

    private final String sessionId;
    private final String token;
    private final Instant lastLoggedIn;
}
