package com.quickcache.server.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.quickcache.server.storage.types.MapStorage;
import com.quickcache.server.storage.types.StringStorage;

public class StorageUnit {

	private Map<String, StringStorage> stringStore;

	private Map<String, MapStorage> mapStore;

	private ReentrantReadWriteLock readWriteLock;
	private ReadLock readLock;
	private WriteLock writeLock;

	public StorageUnit() {
		this.stringStore = new HashMap<String, StringStorage>();
		this.mapStore = new HashMap<String, MapStorage>();
		readWriteLock = new ReentrantReadWriteLock();
		readLock = readWriteLock.readLock();
		writeLock = readWriteLock.writeLock();
	}

	public String getValue(String key) {
		try {
			readLock.lock();
			StringStorage stringStorage = stringStore.get(key);
			if (stringStorage != null) {
				return stringStorage.getData();
			}
			return null;
		} finally {
			readLock.unlock();
		}
	}

	public void setValue(String key, String value) {
		try {
			writeLock.lock();
			stringStore.put(key, new StringStorage(value));
		} finally {
			writeLock.unlock();
		}
	}

	public String getMapValue(String key, String field) {
		try {
			readLock.lock();
			MapStorage mapStorage = mapStore.get(key);
			if (mapStorage != null) {
				return mapStorage.getData(field);
			}
			return null;
		} finally {
			readLock.unlock();
		}
	}
	
	public Set<String> getMapFields(String key) {
		try {
			readLock.lock();
			MapStorage mapStorage = mapStore.get(key);
			if (mapStorage != null) {
				return mapStorage.getAllFields();
			}
			return null;
		} finally {
			readLock.unlock();
		}
	}

	//public List<String> getMapFieldValues(String key) {
	public Map<String, String> getMapFieldValues(String key) {
		try {
			readLock.lock();
			MapStorage mapStorage = mapStore.get(key);
			if (mapStorage != null) {
				//return mapStorage.getAllFieldValues();
				return mapStorage.getDataMap();
			}
			return null;
		} finally {
			readLock.unlock();
		}
	}

	public void setMapValue(String key, String mapKey, String value) {
		try {
			writeLock.lock();
			MapStorage mapStorage = null;
			if (mapStore.containsKey(key)) {
				mapStorage = mapStore.get(key);
			} else {
				mapStorage = new MapStorage();
				mapStore.put(key, mapStorage);
			}
			mapStorage.putData(mapKey, value);
		} finally {
			writeLock.unlock();
		}
	}
}
