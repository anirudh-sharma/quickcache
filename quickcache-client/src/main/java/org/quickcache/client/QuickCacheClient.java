package org.quickcache.client;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.quickcache.client.config.ClientConfig;
import org.quickcache.client.config.operations.ListOperation;
import org.quickcache.client.config.operations.MapOperation;
import org.quickcache.client.config.operations.StringOperation;

public class QuickCacheClient {

	private ClientConfig clientConfig;

	private String baseUrl;

	private StringOperation stringOperation;

	private MapOperation mapOperation;

	private ListOperation listOperation;

	public ClientConfig getClientConfig() {
		return clientConfig;
	}

	public void setClientConfig(ClientConfig clientConfig) {
		this.clientConfig = clientConfig;
		if (this.clientConfig.getHostName() == null || this.clientConfig.getPort() == null) {
			throw new IllegalArgumentException("Hostname or port is not set in client config");
		}
		this.baseUrl = new StringBuilder().append("http://").append(this.clientConfig.getHostName()).append(":")
				.append(this.clientConfig.getPort()).toString();
		this.stringOperation = new StringOperation(baseUrl);
		this.mapOperation = new MapOperation(baseUrl);
		this.listOperation = new ListOperation(baseUrl);
	}

	// String Operations
	public <T extends Serializable> T getObject(String key) {
		return this.stringOperation.getObject(key);
	}

	public <T extends Serializable> void setObject(String key, T object) {
		this.stringOperation.setObject(key, object);
	}

	public String getString(String key) {
		return this.stringOperation.getString(key);
	}

	public String setString(String key, String value) {
		return this.stringOperation.setString(key, value);
	}

	// Map Operations
	public String getMapValue(String key, String field) {
		return this.mapOperation.getMapValue(key, field);
	}

	public Set<String> getMapFields(String key) {
		return this.mapOperation.getMapFields(key);
	}

	public Map<String, String> getMapFieldValues(String key) {
		return this.mapOperation.getMapFieldValues(key);
	}

	public String setMapValue(String key, String field, String value) {
		return this.mapOperation.setMapValue(key, field, value);
	}

	// List Operations
	public List<String> getListItems(String key) {
		return this.listOperation.getListItems(key);
	}

	public List<String> getListItems(String key, int offset) {
		return this.listOperation.getListItems(key, offset);
	}

	public List<String> getListItems(String key, int start, int length) {
		return this.listOperation.getListItems(key, start, length);
	}

	public String addListItem(String key, String item) {
		return this.listOperation.addListItem(key, item);
	}

	public String removeListItem(String key, int position) {
		return this.listOperation.removeListItem(key, position);
	}

}
