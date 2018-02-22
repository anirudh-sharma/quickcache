package com.quickcache.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.quickcache.server.config.ServerConfiguration;

@SpringBootApplication
@Import(value=ServerConfiguration.class)
public class HttpApplication {
    public static void main(String[] args) {
        SpringApplication.run(HttpApplication.class, args);
    }
}
