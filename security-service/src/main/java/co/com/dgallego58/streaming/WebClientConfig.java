package co.com.dgallego58.streaming;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import io.netty.resolver.NoopAddressResolverGroup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.Arrays;

@Configuration
public class WebClientConfig {


    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        var httpClient = HttpClient.create()
                                   .resolver(DefaultAddressResolverGroup.INSTANCE)
                                   .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
                                   .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(60_000))
                                                                          .addHandlerLast(
                                                                                  new WriteTimeoutHandler(3_000)));
        return builder.clientConnector(new ReactorClientHttpConnector(httpClient))
                      .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                      .baseUrl("http://localhost:8080")
                      .build();
    }

}
