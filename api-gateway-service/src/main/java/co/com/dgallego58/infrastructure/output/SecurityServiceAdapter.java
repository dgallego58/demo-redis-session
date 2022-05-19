package co.com.dgallego58.infrastructure.output;

import co.com.dgallego58.core.model.Security;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class SecurityServiceAdapter implements Security {

    private final WebClient webClient;

    public SecurityServiceAdapter(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<JsonNode> getForCA(String tknCA) {
        return webClient.get()
                        .uri("/security/session/{id}", tknCA)
                        .retrieve()
                        .bodyToMono(JsonNode.class);
    }
}
