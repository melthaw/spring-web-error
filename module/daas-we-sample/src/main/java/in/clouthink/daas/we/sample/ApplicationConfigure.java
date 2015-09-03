package in.clouthink.daas.we.sample;

import in.clouthink.daas.we.ResponseEntityExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@ComponentScan(value = "in.clouthink.daas.we.sample")
public class ApplicationConfigure {
    
    @Bean
    public ResponseEntityExceptionHandler responseEntityExceptionHandler() {
        ResponseEntityExceptionHandler result = new ResponseEntityExceptionHandler();
        result.setI18nEnabled(false);
        result.setDeveloperEnabled(true);
        return result;
    }
    
    public static void main(String[] args) throws Exception {
        SpringApplication.run(ApplicationConfigure.class, args);
    }
    
}
