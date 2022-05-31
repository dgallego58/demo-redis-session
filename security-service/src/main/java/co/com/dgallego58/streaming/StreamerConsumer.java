package co.com.dgallego58.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StreamerConsumer {

    private static final Logger log = LoggerFactory.getLogger(StreamerConsumer.class);
    private final WebClient webClient;

    public StreamerConsumer(WebClient webClient) {
        this.webClient = webClient;
    }

    public void subscribeToData() {
        final AtomicInteger i = new AtomicInteger();
        webClient.get()
                 .uri("/effective-jpa/editorial/authors-json-stream")
                 .accept(MediaType.APPLICATION_NDJSON)
                 .retrieve()
                 .bodyToFlux(AuthorDTO.class)
                .doOnNext(data -> {
                    log.info("Client Subscription: {}", data);
                    i.incrementAndGet();
                })
                .blockLast();
                 /*.subscribe(data -> {
                     log.info("Client Subscription: {}", data);
                 });*/
        log.info("Records Received: {}", i.get());
    }
}
