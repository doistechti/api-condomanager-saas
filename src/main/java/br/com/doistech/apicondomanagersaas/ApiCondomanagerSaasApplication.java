package br.com.doistech.apicondomanagersaas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class ApiCondomanagerSaasApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ApiCondomanagerSaasApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ApiCondomanagerSaasApplication.class);
    }
}