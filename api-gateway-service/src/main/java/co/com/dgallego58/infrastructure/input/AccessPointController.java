package co.com.dgallego58.infrastructure.input;


import co.com.dgallego58.core.model.dto.SessionCarrier;
import co.com.dgallego58.core.usecase.SessionUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping(path = "/access")
public class AccessPointController {

    private final SessionUseCase sessionUseCase;

    public AccessPointController(SessionUseCase sessionUseCase) {
        this.sessionUseCase = sessionUseCase;
    }

    @PostMapping(path = "/sign-in")

    public Mono<ResponseEntity<SessionCarrier>> login(@Schema(hidden = true) WebSession webSession, @Schema(example =
            " {\"tokenCA\":\"1\"}") @RequestBody JsonNode jsonNode) {

        webSession.getAttributes().putIfAbsent("access", "control-" + UUID.randomUUID());
        return sessionUseCase.exchangeTkn(webSession.getId(), jsonNode.get("tokenCA").asText())
                             .map(ResponseEntity::ok);
    }
}
