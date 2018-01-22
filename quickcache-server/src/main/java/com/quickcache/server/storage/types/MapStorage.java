package com.quickcache.server.storage.types;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MapStorage implements StorageValue {

	private ConcurrentMap<String, String> dataMap;
	private Date lastModified;
	private Date lastAccessed;

	public MapStorage() {
		dataMap = new ConcurrentHashMap<String, String>();
		this.lastModified = new Date();
		this.lastAccessed = new Date();
	}

	public String getData(String field) {
		this.lastAccessed = new Date();
		return dataMap.get(field);
	}

	public Set<String> getAllFields() {
		return dataMap.keySet();
	}

	public Map<String, String> getDataMap() {
		return dataMap;
	}

	public void putData(String field, String value) {
		dataMap.put(field, value);
		this.lastModified = new Date();
	}

	public Date getLastModified() {
		return lastModified;
	}

	public Date getLastAccessed() {
		return lastAccessed;
	}
}
