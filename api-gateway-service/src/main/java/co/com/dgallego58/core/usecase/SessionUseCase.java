package co.com.dgallego58.core.usecase;

import co.com.dgallego58.core.model.dto.SessionCarrier;
import reactor.core.publisher.Mono;

public interface SessionUseCase {

    Mono<SessionCarrier> exchangeTkn(String sessionId, String tknCA);

    Mono<String> getCarrier(String sessionId);

    Mono<Boolean> checkToken(String jwt);

}
