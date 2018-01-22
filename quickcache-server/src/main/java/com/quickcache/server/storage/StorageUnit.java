package com.quickcache.server.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import com.quickcache.server.exception.QuickCacheOperationException;
import com.quickcache.server.storage.types.ListStorage;
import com.quickcache.server.storage.types.MapStorage;
import com.quickcache.server.storage.types.StringStorage;

public class StorageUnit {

	private Map<String, StringStorage> stringStore;

	private Map<String, MapStorage> mapStore;
	
	private Map<String, ListStorage> listStore;

	private ReentrantReadWriteLock readWriteLock;
	private ReadLock readLock;
	private WriteLock writeLock;

	public StorageUnit() {
		this.stringStore = new HashMap<String, StringStorage>();
		this.mapStore = new HashMap<String, MapStorage>();
		this.listStore = new HashMap<String, ListStorage>();
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

	public Map<String, String> getMapFieldValues(String key) {
		try {
			readLock.lock();
			MapStorage mapStorage = mapStore.get(key);
			if (mapStorage != null) {
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

	public List<String> getListItems(String key, boolean allItems, int offset, int length) {
		try {
			readLock.lock();
			ListStorage listStorage = listStore.get(key);
			if (listStorage != null) {
				if(allItems) {
					return listStorage.getItems();
				} else {
					return listStorage.getItems(offset, length);
				}
			}
			return null;
		} finally {
			readLock.unlock();
		}
	}
	
	public void addListItem(String key, String item) {
		try {
			writeLock.lock();
			ListStorage listStorage = listStore.get(key);
			if (listStorage != null) {
				listStorage.add(item);
				return;
			} 
			listStorage = new ListStorage();
			listStore.put(key, listStorage);
			listStorage.add(item);
		} finally {
			writeLock.unlock();
		}
	}

	public String removeListItem(String key, boolean isItem, String item, int index) {
		try {
			writeLock.lock();
			ListStorage listStorage = listStore.get(key);
			if (listStorage != null) {
				if(isItem) {
					if(listStorage.remove(item))
						return item;
					else
						return null;
				}
				try{
					item = listStorage.remove(index);
				} catch(IndexOutOfBoundsException exception){
					throw new QuickCacheOperationException(5);
				}
				
				return item;
			}
			return null;
		} finally {
			writeLock.unlock();
		}
	}
}
