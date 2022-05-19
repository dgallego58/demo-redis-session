package co.com.dgallego58.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

public interface Security {

    Mono<JsonNode> getForCA(String tknCA);

}
