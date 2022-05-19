package co.com.dgallego58.application.usecaseconfig;

import co.com.dgallego58.core.model.Security;
import co.com.dgallego58.core.model.Store;
import co.com.dgallego58.core.usecase.SessionService;
import co.com.dgallego58.core.usecase.SessionUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public SessionUseCase sessionUseCase(Security security, Store store) {
        return new SessionService(security, store);
    }

}
