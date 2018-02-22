package org.quickcache.client.config;

import org.quickcache.client.config.credentials.UserCredentials;

public class ClientConfig {

	private String hostName;
	private Integer port;

	private UserCredentials userCredentials;

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = Integer.valueOf(port);
	}

	public UserCredentials getUserCredentials() {
		return userCredentials;
	}

	public void setUserCredentials(UserCredentials userCredentials) {
		this.userCredentials = userCredentials;
	}

}
