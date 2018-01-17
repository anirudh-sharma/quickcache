package com.quickcache.server;

import java.util.Properties;

import com.quickcache.server.manager.ClientConnectionManager;
import com.quickcache.server.manager.StorageManager;
import com.quickcache.server.storage.config.Configuration;

public class QuickCache {

	private static Configuration configuration;

	public static StorageManager storageManager;

	private static ClientConnectionManager clientConnectionManager;

	public static void init() {
		Properties properties = new Properties();
		try {
			properties.load(QuickCache.class.getClassLoader().getResourceAsStream("quickcache.config"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		configuration = new Configuration(properties);
		storageManager = new StorageManager(configuration);
		clientConnectionManager = new ClientConnectionManager(configuration);
	}

	public static void loadDummyData(){
		storageManager.setValue("foo1", "bar1");
		storageManager.setValue("foo2", "bar2");
		storageManager.setValue("foo3", "bar3");

		storageManager.setMapValue("map1", "mapkey1", "map1value1");
		storageManager.setMapValue("map1", "mapkey2", "map1value2");
		storageManager.setMapValue("map1", "mapkey3", "map1value3");

		storageManager.setMapValue("map2", "mapkey1", "map2value1");
		storageManager.setMapValue("map2", "mapkey2", "map2value2");
		storageManager.setMapValue("map2", "mapkey3", "map2value3");

//		System.out.println(storageManager.getValue("foo1"));
//		System.out.println(storageManager.getValue("foo2"));
//		System.out.println(storageManager.getValue("foo3"));
//
//		System.out.println(storageManager.getMapValue("map1", "mapkey1"));
//		System.out.println(storageManager.getMapValue("map1", "mapkey2"));
//		System.out.println(storageManager.getMapValue("map1", "mapkey3"));
//
//		System.out.println(storageManager.getMapValue("map2", "mapkey1"));
//		System.out.println(storageManager.getMapValue("map2", "mapkey2"));
//		System.out.println(storageManager.getMapValue("map2", "mapkey3"));

	}
}
