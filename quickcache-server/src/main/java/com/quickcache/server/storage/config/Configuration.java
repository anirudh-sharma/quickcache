package com.quickcache.server.storage.config;

import java.util.Properties;

import com.quickcache.server.constants.AppConstants;

public class Configuration {

	public Configuration(Properties properties) {
		try {
			this.concurrencyLevel = Integer.parseInt(properties.getProperty(AppConstants.ConcurrencyLevelPropertyName));
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}

	private int concurrencyLevel;

	public int getConcurrencyLevel() {
		return concurrencyLevel;
	}

	public void setConcurrencyLevel(int concurrencyLevel) {
		this.concurrencyLevel = concurrencyLevel;
	}
}
