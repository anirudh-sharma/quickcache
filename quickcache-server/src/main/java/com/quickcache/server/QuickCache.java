package com.quickcache.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.quickcache.server.manager.ClusterManager;
import com.quickcache.server.manager.PersistenceManager;
import com.quickcache.server.manager.StorageManager;
import com.quickcache.server.protocol.ClusterRequestResponseWrapper;

@Component
public class QuickCache {

	@Autowired
	private PersistenceManager persistenceManager;
	@Autowired
	private StorageManager storageManager;
	@Autowired
	private ClusterManager clusterManager;

	private final static AtomicLong requestIdGenerator;

	static {
		requestIdGenerator = new AtomicLong();
	}

	private static Map<String, Object> componentStorage = new HashMap<>();
	private static Map<Long, ClusterRequestResponseWrapper> clusterRequestMap = new HashMap<Long, ClusterRequestResponseWrapper>();

	public void setStorageManager(StorageManager storageManager) {
		componentStorage.put("storageManager", storageManager);
		this.storageManager = storageManager;
	}

	public void setClusterManager(ClusterManager clusterManager) {
		componentStorage.put("clusterManager", clusterManager);
		this.clusterManager = clusterManager;
	}

	public StorageManager getStorageManager() {
		return this.storageManager;
	}

	public ClusterManager getClusterManager() {
		return this.clusterManager;
	}

	public static long generateRequestId() {
		return requestIdGenerator.incrementAndGet();
	}

	public static void putClusterRequest(ClusterRequestResponseWrapper clusterRequestResponseWrapper) {
		clusterRequestMap.put(clusterRequestResponseWrapper.getRequestId(), clusterRequestResponseWrapper);
	}

	public static ClusterRequestResponseWrapper getClusterRequest(long requestId) {
		return clusterRequestMap.get(requestId);
	}

	public static ClusterRequestResponseWrapper removeClusterRequest(long requestId) {
		return clusterRequestMap.remove(requestId);
	}

	@PostConstruct
	public void init() {
		this.persistenceManager.loadData();
	}

}
