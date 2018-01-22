package com.quickcache.server.manager;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.quickcache.server.constants.AppConstants;
import com.quickcache.server.storage.StorageUnit;

@Component
public class StorageManager {

	private StorageUnit[] storageUnits;

	@Value("${"+AppConstants.ConcurrencyLevelPropertyName+"}")
	private int concurrencyLevel;

	@PostConstruct
	public void init() {
		storageUnits = new StorageUnit[concurrencyLevel];
		for (int count = 0; count < concurrencyLevel; count++) {
			storageUnits[count] = new StorageUnit();
		}
	}

	// String Operations
	public String getValue(String key) {
		return storageUnits[getStorageUnitCount(key)].getValue(key);
	}

	public void setValue(String key, String value) {
		storageUnits[getStorageUnitCount(key)].setValue(key, value);
	}

	// Map Operations
	public String getMapValue(String key, String mapKey) {
		return storageUnits[getStorageUnitCount(key)].getMapValue(key, mapKey);
	}

	public Set<String> getMapFields(String key) {
		return storageUnits[getStorageUnitCount(key)].getMapFields(key);
	}

	public Map<String, String> getMapFieldValues(String key) {
		return storageUnits[getStorageUnitCount(key)].getMapFieldValues(key);
	}

	public void setMapValue(String key, String mapKey, String value) {
		storageUnits[getStorageUnitCount(key)].setMapValue(key, mapKey, value);
	}

	// List Operations
	public List<String> getListItems(String key, boolean allItems, int offset, int length) {
		return storageUnits[getStorageUnitCount(key)].getListItems(key, allItems, offset, length);
	}
	
	public void addListItem(String key, String item) {
		storageUnits[getStorageUnitCount(key)].addListItem(key, item);
	}

	public String removeListItem(String key, boolean isItem, String item, int index) {
		return storageUnits[getStorageUnitCount(key)].removeListItem(key, isItem, item, index);
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
