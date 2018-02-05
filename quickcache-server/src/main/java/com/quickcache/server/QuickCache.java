package com.quickcache.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.quickcache.server.manager.ClusterManager;
import com.quickcache.server.manager.StorageManager;
import com.quickcache.server.protocol.ClusterRequestResponseWrapper;

@Component
public class QuickCache {

	@Autowired
	private StorageManager storageManager;
	@Autowired
	private ClusterManager clusterManager;

	private final static AtomicInteger requestIdGenerator;

	static {
		requestIdGenerator = new AtomicInteger();
	}

	private static Map<String, Object> componentStorage = new HashMap<>();
	private static Map<Integer, ClusterRequestResponseWrapper> clusterRequestMap = new HashMap<Integer, ClusterRequestResponseWrapper>();

	public void setStorageManager(StorageManager storageManager) {
		componentStorage.put("storageManager", storageManager);
		this.storageManager = storageManager;
	}

	public void setClusterManager(ClusterManager clusterManager) {
		componentStorage.put("clusterManager", clusterManager);
		this.clusterManager = clusterManager;
	}

	public static StorageManager getStorageManager() {
		return (StorageManager) componentStorage.get("storageManager");
	}

	public static ClusterManager getClusterManager() {
		return (ClusterManager) componentStorage.get("clusterManager");
	}

	public static int generateRequestId() {
		return requestIdGenerator.incrementAndGet();
	}

	public static void putClusterRequest(ClusterRequestResponseWrapper clusterRequestResponseWrapper) {
		clusterRequestMap.put(clusterRequestResponseWrapper.getRequestId(), clusterRequestResponseWrapper);
	}
	
	public static ClusterRequestResponseWrapper getClusterRequest(int requestId) {
		return clusterRequestMap.get(requestId);
	}

	public static ClusterRequestResponseWrapper removeClusterRequest(int requestId) {
		return clusterRequestMap.remove(requestId);
	}
	
	@PostConstruct
	public void init() {
		// storageManager.setValue("foo1", "bar1");
		// storageManager.setValue("foo2", "bar2");
		// storageManager.setValue("foo3", "bar3");
		//
		// storageManager.setMapValue("map1", "mapkey1", "map1value1");
		// storageManager.setMapValue("map1", "mapkey2", "map1value2");
		// storageManager.setMapValue("map1", "mapkey3", "map1value3");
		//
		// storageManager.setMapValue("map2", "mapkey1", "map2value1");
		// storageManager.setMapValue("map2", "mapkey2", "map2value2");
		// storageManager.setMapValue("map2", "mapkey3", "map2value3");
	}

}
