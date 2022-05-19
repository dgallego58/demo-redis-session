package co.com.dgallego58;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        System.setProperty("jwt-secret", "this-is-a-demo-purpose-for-jwt-gateway");
        SpringApplication.run(GatewayApplication.class, args);
    }
}
