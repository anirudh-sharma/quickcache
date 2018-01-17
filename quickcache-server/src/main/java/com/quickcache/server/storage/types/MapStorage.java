package com.quickcache.server.storage.types;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

	public List<String> getAllFieldValues() {
		List<String> fieldValues = new ArrayList<String>();
		for(Entry<String, String> entry: dataMap.entrySet()){
			fieldValues.add(entry.getKey());
			fieldValues.add(entry.getValue());
		}
		return fieldValues;
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
