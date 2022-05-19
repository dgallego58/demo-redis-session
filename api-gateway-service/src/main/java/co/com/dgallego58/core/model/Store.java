package co.com.dgallego58.core.model;

import co.com.dgallego58.core.model.dto.SessionCarrier;
import reactor.core.publisher.Mono;

public interface Store {
    Mono<SessionCarrier> save(SessionCarrier sessionCarrier);

    Mono<SessionCarrier> getValue(String uuidTkn);
}
