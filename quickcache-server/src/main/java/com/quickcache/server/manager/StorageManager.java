package com.quickcache.server.manager;

import java.util.Map;
import java.util.Set;

import com.quickcache.server.storage.StorageUnit;
import com.quickcache.server.storage.config.Configuration;

public class StorageManager {

	private StorageUnit[] storageUnits;
	private int concurrencyLevel;

	public StorageManager(Configuration cacheConfiguration) {
		this.concurrencyLevel = cacheConfiguration.getConcurrencyLevel();
		storageUnits = new StorageUnit[concurrencyLevel];
		for (int count = 0; count < concurrencyLevel; count++) {
			storageUnits[count] = new StorageUnit();
		}
	}

	public String getValue(String key) {
		return storageUnits[getStorageUnitCount(key)].getValue(key);
	}

	public void setValue(String key, String value) {
		storageUnits[getStorageUnitCount(key)].setValue(key, value);
	}

	public String getMapValue(String key, String mapKey) {
		return storageUnits[getStorageUnitCount(key)].getMapValue(key, mapKey);
	}

	public Set<String> getMapFields(String key) {
		return storageUnits[getStorageUnitCount(key)].getMapFields(key);
	}

	//public List<String> getMapFieldValues(String key) {
	public Map<String, String> getMapFieldValues(String key) {
		return storageUnits[getStorageUnitCount(key)].getMapFieldValues(key);
	}

	public void setMapValue(String key, String mapKey, String value) {
		storageUnits[getStorageUnitCount(key)].setMapValue(key, mapKey, value);
	}

	private int getStorageUnitCount(String key) {
		int storeCount = Math.abs(key.hashCode()) % concurrencyLevel;
		if (storeCount < concurrencyLevel) {
			return storeCount;
		} else {
			return (concurrencyLevel - 1);
		}
	}
}
