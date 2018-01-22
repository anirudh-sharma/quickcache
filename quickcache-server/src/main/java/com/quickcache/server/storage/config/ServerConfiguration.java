package com.quickcache.server.storage.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.quickcache.server.QuickCache;
import com.quickcache.server.manager.ClientConnectionManager;
import com.quickcache.server.manager.PersistenceManager;
import com.quickcache.server.manager.StorageManager;

@Configuration
@ComponentScan(basePackages = { "com.quickcache.server.manager", "com.quickcache.server.storage.controllers" })
public class ServerConfiguration {

	@Bean
	public static PropertySourcesPlaceholderConfigurer properties() {
		PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
		Properties props = new Properties();
		try {
			props.load(ServerConfiguration.class.getClassLoader().getResourceAsStream("quickcache.config"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ppc.setProperties(props);
		ppc.setIgnoreUnresolvablePlaceholders(true);
		return ppc;
	}

	@Bean
	public QuickCache init(StorageManager storageManager, ClientConnectionManager clientConnectionManager,
			PersistenceManager persistenceManager) {
		QuickCache quickCache = new QuickCache();
		quickCache.setStorageManager(storageManager);
		quickCache.setClientConnectionManager(clientConnectionManager);
		quickCache.setPersistenceManager(persistenceManager);

		quickCache.init();

		return quickCache;
	}
}
