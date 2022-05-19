package co.com.dgallego58.application.client;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    private static final int UNTIL_20MB = 20 * 1_024 * 1_024;
    private static final int CONNECT_TIMEOUT = 2_000;
    private static final int READ_TIMEOUT = 3;
    private static final int WRITE_TIMEOUT = 4;
    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);

    @Bean
    public WebClient configuredWebClient(WebClient.Builder builder) {
        var httpClient = HttpClient.create()
                                   .resolver(DefaultAddressResolverGroup.INSTANCE)
                                   .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT)
                                   .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT))
                                                              .addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT)));
        return builder.clientConnector(new ReactorClientHttpConnector(httpClient))
                      .baseUrl("http://localhost:8081")
                      .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(UNTIL_20MB))
                      .filter(logRequestHeader())
                      .filter(logResponseHeader())
                      .build();
    }

    public ExchangeFilterFunction logRequestHeader() {
        return (request, next) -> {
            log.info("--- Request");
            log.info("Method {}", request.method());
            log.info("URL {}", request.url());
            log.info("Headers {}", request.headers().toSingleValueMap());

            return next.exchange(request);
        };
    }

    public ExchangeFilterFunction logResponseHeader() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.info("--- Response");
            log.info("Status {}", clientResponse.statusCode());
            log.info("Headers {}", clientResponse.headers().asHttpHeaders());
            return Mono.just(clientResponse);
        });
    }

}
