package com.quickcache.server.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.quickcache.server.constants.AppConstants;

@Configuration
@ComponentScan(basePackages = { "com.quickcache.server", "com.quickcache.server.manager", "com.quickcache.server.storage.controllers", "com.quickcache.server.protocol.processor"})
public class ServerConfiguration {

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties() {
		PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
		Properties props = new Properties();
		try {
			props.load(ServerConfiguration.class.getClassLoader().getResourceAsStream("quickcache.manifest"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ppc.setProperties(props);
		ppc.setIgnoreUnresolvablePlaceholders(true);
		return ppc;
	}

    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer(@Value(value="${"+AppConstants.HttpConnectionPropertyName+"}") int serverPort) {
    	return (container -> {
            container.setPort(serverPort);
        });
    }

}
