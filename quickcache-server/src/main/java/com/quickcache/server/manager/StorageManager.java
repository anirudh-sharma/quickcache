package com.quickcache.server.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickcache.server.QuickCache;
import com.quickcache.server.constants.AppConstants;
import com.quickcache.server.constants.DataType;
import com.quickcache.server.protocol.ClusterRequest;
import com.quickcache.server.protocol.ClusterRequestResponseWrapper;
import com.quickcache.server.protocol.ProtocolCommand;
import com.quickcache.server.storage.StorageUnit;

@Component
public class StorageManager {

	private static final Logger logger = LoggerFactory.getLogger(StorageManager.class);

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

	@Autowired
	private PersistenceManager persistenceManager;

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

	// Keys cache Operations
	public void updateKeysCache(List<String> keys, int serverId) {
		logger.info("Update keys cache with keys from shard node: "+serverId);
		if(keys == null || keys.size() == 0) {
			return;
		}
		for(String key: keys) {
			this.keyMap.put(key, serverId);
		}
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
				long requestId = QuickCache.generateRequestId();
				ClusterRequest clusterRequest = new ClusterRequest(this.serverId, serverId, ProtocolCommand.GET_STRING,
						"{\"requestId\":" + requestId + ",\"key\":" + "\"" + key + "\"}");

				return getResponseSynchronously(requestId, clusterRequest).get("value");
			}
		}
	}

	public void setValue(String key, String value, boolean loadPersistenceOperation) {
		if (isShardNode || loadPersistenceOperation) {
			storageUnits[getStorageUnitCount(key)].setValue(key, value);
			if (loadPersistenceOperation) {
				this.keyMap.put(key, this.serverId);
			}
		} else {
			int serverId = getServerId(key);
			if (this.serverId == serverId) {
				storageUnits[getStorageUnitCount(key)].setValue(key, value);
			} else {
				long requestId = QuickCache.generateRequestId();
				ClusterRequest clusterRequest = new ClusterRequest(this.serverId, serverId, ProtocolCommand.SET_STRING,
						"{\"requestId\":" + requestId + ",\"key\":" + "\"" + key + "\",\"value\":\"" + value + "\"}");
				getResponseSynchronously(requestId, clusterRequest).get("value");
				return;
			}
		}
		if (!loadPersistenceOperation) {
			this.persistenceManager.persistData(key, null, value, DataType.STRING, this.serverId);
		}
	}

	// Map Operations
	public String getMapValue(String key, String field) {
		if (isShardNode) {
			return storageUnits[getStorageUnitCount(key)].getMapValue(key, field);
		} else {
			int serverId = getServerId(key);
			if (this.serverId == serverId) {
				return storageUnits[getStorageUnitCount(key)].getMapValue(key, field);
			} else {
				long requestId = QuickCache.generateRequestId();
				ClusterRequest clusterRequest = new ClusterRequest(this.serverId, serverId,
						ProtocolCommand.GET_MAP_VALUE,
						"{\"requestId\":" + requestId + ",\"key\":\"" + key + "\",\"field\":\"" + field + "\"}");

				return getResponseSynchronously(requestId, clusterRequest).get("value");
			}
		}
	}

	public Set<String> getMapFields(String key) {
		if (isShardNode) {
			return storageUnits[getStorageUnitCount(key)].getMapFields(key);
		} else {
			int serverId = getServerId(key);
			if (this.serverId == serverId) {
				return storageUnits[getStorageUnitCount(key)].getMapFields(key);
			} else {
				long requestId = QuickCache.generateRequestId();
				ClusterRequest clusterRequest = new ClusterRequest(this.serverId, serverId,
						ProtocolCommand.GET_MAP_FIELDS, "{\"requestId\":" + requestId + ",\"key\":\"" + key + "\"}");

				String fields = getResponseSynchronously(requestId, clusterRequest).get("fields");
				ObjectMapper mapper = new ObjectMapper();
				TypeReference<HashSet<String>> typeRef = new TypeReference<HashSet<String>>() {
				};
				try {
					return mapper.readValue(fields, typeRef);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		}

	}

	public Map<String, String> getMapFieldValues(String key) {

		if (isShardNode) {
			return storageUnits[getStorageUnitCount(key)].getMapFieldValues(key);
		} else {
			int serverId = getServerId(key);
			if (this.serverId == serverId) {
				return storageUnits[getStorageUnitCount(key)].getMapFieldValues(key);
			} else {
				long requestId = QuickCache.generateRequestId();
				ClusterRequest clusterRequest = new ClusterRequest(this.serverId, serverId,
						ProtocolCommand.GET_MAP_FIELD_VALUES,
						"{\"requestId\":" + requestId + ",\"key\":\"" + key + "\"}");

				String map = getResponseSynchronously(requestId, clusterRequest).get("map");
				ObjectMapper mapper = new ObjectMapper();
				TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {
				};
				try {
					return mapper.readValue(map, typeRef);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		}
	}

	public void setMapValue(String key, String field, String value, boolean loadPersistenceOperation) {
		if (isShardNode || loadPersistenceOperation) {
			storageUnits[getStorageUnitCount(key)].setMapValue(key, field, value);
			if (loadPersistenceOperation) {
				this.keyMap.put(key, this.serverId);
			}
		} else {
			int serverId = getServerId(key);
			if (this.serverId == serverId) {
				storageUnits[getStorageUnitCount(key)].setMapValue(key, field, value);
			} else {
				long requestId = QuickCache.generateRequestId();
				ClusterRequest clusterRequest = new ClusterRequest(this.serverId, serverId,
						ProtocolCommand.SET_MAP_VALUE, "{\"requestId\":" + requestId + ",\"key\":\"" + key
								+ "\",\"field\":\"" + field + "\",\"value\":\"" + value + "\"}");
				getResponseSynchronously(requestId, clusterRequest).get("value");
				return;
			}
		}
		if (!loadPersistenceOperation) {
			this.persistenceManager.persistData(key, field, value, DataType.MAP, this.serverId);
		}
	}

	// List Operations
	public List<String> getListItems(String key, boolean allItems, int offset, int length) {

		if (isShardNode) {
			return storageUnits[getStorageUnitCount(key)].getListItems(key, allItems, offset, length);
		} else {
			int serverId = getServerId(key);
			if (this.serverId == serverId) {
				return storageUnits[getStorageUnitCount(key)].getListItems(key, allItems, offset, length);
			} else {
				long requestId = QuickCache.generateRequestId();
				ClusterRequest clusterRequest = new ClusterRequest(this.serverId, serverId,
						ProtocolCommand.GET_LIST_ITEMS,
						"{\"requestId\":" + requestId + ",\"key\":\"" + key + "\",\"allItems\": " + allItems
								+ ", \"offset\": " + offset + ",\"length\":" + length + "}");

				String items = getResponseSynchronously(requestId, clusterRequest).get("items");

				try {
					if (items != null) {
						ObjectMapper mapper = new ObjectMapper();
						TypeReference<ArrayList<String>> typeRef = new TypeReference<ArrayList<String>>() {
						};
						return mapper.readValue(items, typeRef);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
		}

	}

	public void addListItem(String key, String item, boolean loadPersistenceOperation) {
		if (isShardNode || loadPersistenceOperation) {
			storageUnits[getStorageUnitCount(key)].addListItem(key, item);
			if (loadPersistenceOperation) {
				this.keyMap.put(key, this.serverId);
			}
		} else {
			int serverId = getServerId(key);
			if (this.serverId == serverId) {
				storageUnits[getStorageUnitCount(key)].addListItem(key, item);
			} else {
				long requestId = QuickCache.generateRequestId();
				ClusterRequest clusterRequest = new ClusterRequest(this.serverId, serverId,
						ProtocolCommand.ADD_LIST_ITEM,
						"{\"requestId\":" + requestId + ",\"key\":\"" + key + "\",\"item\":\"" + item + "\"}");
				getResponseSynchronously(requestId, clusterRequest).get("value");
				return;
			}
		}
		if (!loadPersistenceOperation) {
			this.persistenceManager.persistData(key, null, item, DataType.LIST, this.serverId);
		}
	}

	public String removeListItem(String key, int index) {
		if (isShardNode) {
			return storageUnits[getStorageUnitCount(key)].removeListItem(key, index);
		} else {
			int serverId = getServerId(key);
			if (this.serverId == serverId) {
				storageUnits[getStorageUnitCount(key)].removeListItem(key, index);
			} else {
				long requestId = QuickCache.generateRequestId();
				ClusterRequest clusterRequest = new ClusterRequest(this.serverId, serverId,
						ProtocolCommand.REMOVE_LIST_ITEM,
						"{\"requestId\":" + requestId + ",\"key\":\"" + key + "\",\"index\":" + index + "}");
				return getResponseSynchronously(requestId, clusterRequest).get("value");
			}
		}
		return storageUnits[getStorageUnitCount(key)].removeListItem(key, index);
	}

	private int getStorageUnitCount(String key) {
		int storeCount = Math.abs(key.hashCode()) % concurrencyLevel;
		if (storeCount < concurrencyLevel) {
			return storeCount;
		} else {
			return (concurrencyLevel - 1);
		}
	}

	public int getCurrentServerId() {
		return this.serverId;
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

	private Map<String, String> getResponseSynchronously(long requestId, ClusterRequest clusterRequest) {
		CountDownLatch countDownLatch = new CountDownLatch(1);

		ClusterRequestResponseWrapper clusterRequestResponseWrapper = new ClusterRequestResponseWrapper();
		clusterRequestResponseWrapper.setRequestId(requestId);
		clusterRequestResponseWrapper.setCountDownLatch(countDownLatch);
		clusterRequestResponseWrapper.setClusterRequest(clusterRequest);
		QuickCache.putClusterRequest(clusterRequestResponseWrapper);
		this.clusterManager.addRequest(clusterRequest);
		try {
			countDownLatch.await(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		QuickCache.removeClusterRequest(requestId);
		return clusterRequestResponseWrapper.getResponseBody();
	}

}
