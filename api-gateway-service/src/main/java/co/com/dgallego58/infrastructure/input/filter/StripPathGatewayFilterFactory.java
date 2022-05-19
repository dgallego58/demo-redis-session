package co.com.dgallego58.infrastructure.input.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class StripPathGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {
    private final String basePath;

    public StripPathGatewayFilterFactory(@Value("${spring.webflux.base-path}") String basePath) {
        this.basePath = basePath;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            var req = exchange.getRequest();
            String path = req.getURI().getRawPath();
            String newPath = path.replaceFirst(basePath, "");
            log.info("Paths old: {} new: {}", path, newPath);
            var request = req.mutate().path(newPath).contextPath(null).build();
            var mutatedRequest = exchange.mutate().request(request).build();
            return chain.filter(mutatedRequest)
                        .then(Mono.fromRunnable(() -> log.debug("Request Req:{}, Stripped:{}", path, newPath)));
        };
    }
}
