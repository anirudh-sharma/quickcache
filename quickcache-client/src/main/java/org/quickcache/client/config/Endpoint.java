package org.quickcache.client.config;

public enum Endpoint {
	
	STRING("string"),
	MAP("map"),
	LIST("list");
	
	private Endpoint(String value) {
		this.value = value;
	}
	
	private String value;
	
	public String getValue() {
		return this.value;
	}
}
