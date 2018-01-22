package com.quickcache.server;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.quickcache.server.manager.ClientConnectionManager;
import com.quickcache.server.manager.PersistenceManager;
import com.quickcache.server.manager.StorageManager;

@Component
public class QuickCache {

	@Autowired
	private StorageManager storageManager;
	@Autowired
	private PersistenceManager persistenceManager;
	@Autowired
	private ClientConnectionManager clientConnectionManager;

	public void setStorageManager(StorageManager storageManager) {
		this.storageManager = storageManager;
	}

	public void setPersistenceManager(PersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}

	public void setClientConnectionManager(ClientConnectionManager clientConnectionManager) {
		this.clientConnectionManager = clientConnectionManager;
	}

	@PostConstruct
	public void init() {
		storageManager.setValue("foo1", "bar1");
		storageManager.setValue("foo2", "bar2");
		storageManager.setValue("foo3", "bar3");

		storageManager.setMapValue("map1", "mapkey1", "map1value1");
		storageManager.setMapValue("map1", "mapkey2", "map1value2");
		storageManager.setMapValue("map1", "mapkey3", "map1value3");

		storageManager.setMapValue("map2", "mapkey1", "map2value1");
		storageManager.setMapValue("map2", "mapkey2", "map2value2");
		storageManager.setMapValue("map2", "mapkey3", "map2value3");
	}

}
