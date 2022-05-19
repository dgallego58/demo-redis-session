package co.com.dgallego58.application.serializer;

import co.com.dgallego58.core.util.JacksonUtil;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer builderCustomizer() {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder.configure(JacksonUtil.jsonMapper);
    }
}
