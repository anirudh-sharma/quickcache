package com.quickcache.server.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.quickcache.server.QuickCache;
import com.quickcache.server.constants.AppConstants;
import com.quickcache.server.protocol.ClusterRequest;
import com.quickcache.server.protocol.ClusterRequestResponseWrapper;
import com.quickcache.server.protocol.ProtocolCommand;
import com.quickcache.server.storage.StorageUnit;

@Component
public class StorageManager {

	private StorageUnit[] storageUnits;

	private Map<String, Integer> keyMap;
	private boolean isShardNode;

	@Value("${" + AppConstants.ConcurrencyLevelPropertyName + "}")
	private int concurrencyLevel;

	@Value("${" + AppConstants.ServerIdPropertyName + "}")
	private int serverId;

	@Value("${" + AppConstants.NameNodeUrlPropertyName + "}")
	private String nameNodeUrl;

	@Autowired
	private ClusterManager clusterManager;

	private Random randomServerIdGenerator;

	public void setClusterManager(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	@PostConstruct
	public void init() {
		this.isShardNode = (!nameNodeUrl.equals(AppConstants.DummyValue));
		this.storageUnits = new StorageUnit[concurrencyLevel];
		for (int count = 0; count < concurrencyLevel; count++) {
			storageUnits[count] = new StorageUnit();
		}
		this.keyMap = new ConcurrentHashMap<String, Integer>();
		this.randomServerIdGenerator = new Random();
	}

	// String Operations
	public String getValue(String key) {
		if (isShardNode) {
			return storageUnits[getStorageUnitCount(key)].getValue(key);
		} else {
			int serverId = getServerId(key);
			if (this.serverId == serverId) {
				return storageUnits[getStorageUnitCount(key)].getValue(key);
			} else {
				int requestId = QuickCache.generateRequestId();
				ClusterRequest clusterRequest = new ClusterRequest(this.serverId, serverId, ProtocolCommand.GET_STRING,
						"{\"requestId\":"+requestId+",\"key\":" + "\"" + key + "\"}");
				
				return getResponseSynchronously(requestId, clusterRequest).get("value");
			}
		}
	}

	public void setValue(String key, String value) {
		if (isShardNode) {
			storageUnits[getStorageUnitCount(key)].setValue(key, value);
		} else {
			int serverId = getServerId(key);
			if (this.serverId == serverId) {
				storageUnits[getStorageUnitCount(key)].setValue(key, value);
			} else {
				int requestId = QuickCache.generateRequestId();
				ClusterRequest clusterRequest = new ClusterRequest(this.serverId, serverId, ProtocolCommand.SET_STRING,
						"{\"requestId\":"+requestId+",\"key\":" + "\"" + key + "\",\"value\":\"" + value + "\"}");
				getResponseSynchronously(requestId, clusterRequest).get("value");
				return;
			}
		}
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

	private Integer getServerId(String key) {
		Integer serverId = this.keyMap.get(key);
		if (serverId == null) {
			serverId = assignServerId();
			keyMap.put(key, serverId);
		}
		return serverId;
	}

	private int assignServerId() {
		int num = Math.abs(this.randomServerIdGenerator.nextInt((this.clusterManager.getNodeMap().keySet().size())));
		return new ArrayList<Integer>(this.clusterManager.getNodeMap().keySet()).get(num);
	}
	
	private Map<String, String> getResponseSynchronously(int requestId, ClusterRequest clusterRequest) {
		CountDownLatch countDownLatch = new CountDownLatch(1);
		
		ClusterRequestResponseWrapper clusterRequestResponseWrapper = new ClusterRequestResponseWrapper();
		clusterRequestResponseWrapper.setRequestId(requestId);
		clusterRequestResponseWrapper.setCountDownLatch(countDownLatch);
		clusterRequestResponseWrapper.setClusterRequest(clusterRequest);
		QuickCache.putClusterRequest(clusterRequestResponseWrapper);
		this.clusterManager.addRequest(clusterRequest);
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		QuickCache.removeClusterRequest(requestId);
		return clusterRequestResponseWrapper.getResponseBody();
	}
}
