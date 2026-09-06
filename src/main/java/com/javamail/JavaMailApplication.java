package com.javamail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class JavaMailApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(JavaMailApplication.class, args);
    }
}
