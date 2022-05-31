package co.com.dgallego58;

import co.com.dgallego58.streaming.StreamerConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SecurityService {

    @Autowired
    StreamerConsumer streamerConsumer;

    public static void main(String[] args) {
        System.setProperty("jwt-secret", "this-is-a-demo-purpose-for-jwt-gateway");
        SpringApplication.run(SecurityService.class, args);
    }

    @GetMapping("/stream")
    public ResponseEntity<Void> trigger() {
        streamerConsumer.subscribeToData();
        return ResponseEntity.noContent().build();
    }

}
