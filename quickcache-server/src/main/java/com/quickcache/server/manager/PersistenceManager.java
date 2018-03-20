package com.quickcache.server.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickcache.server.constants.AppConstants;
import com.quickcache.server.constants.DataType;
import com.quickcache.server.persistence.StorageChunk;
import com.quickcache.server.protocol.ClusterRequest;
import com.quickcache.server.protocol.ProtocolCommand;

@Component
public class PersistenceManager {

	private static final Logger logger = LoggerFactory.getLogger(PersistenceManager.class);

	private File persistenceDir;
	private boolean isShardNode;

	@Value("${" + AppConstants.ServerIdPropertyName + "}")
	private int serverId;
	@Value("${" + AppConstants.NameNodeUrlPropertyName + "}")
	private String nameNodeUrl;

	@Autowired
	private StorageManager storageManager;

	@Autowired
	private ClusterManager clusterManager;

	private final BlockingDeque<StorageChunk> storageChunkPersistenceQueue;

	public PersistenceManager() {
		this.storageChunkPersistenceQueue = new LinkedBlockingDeque<StorageChunk>();
	}

	public void setStorageManager(StorageManager storageManager) {
		this.storageManager = storageManager;
	}

	@PostConstruct
	public void init() {
		this.isShardNode = (!nameNodeUrl.equals(AppConstants.DummyValue));
		this.persistenceDir = new File("storage");
		System.out.println(this.persistenceDir.getAbsolutePath());
		if (!this.persistenceDir.exists() || !this.persistenceDir.isDirectory()) {
			this.persistenceDir.mkdir();
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				logger.info("Persistence thread started");
				while (true) {
					try {
						StorageChunk chunk = storageChunkPersistenceQueue.poll(2, TimeUnit.HOURS);
						processStorageChunk(chunk);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}, "PERSISTENCE-THREAD").start();
	}

	public void loadData() {
		File persistenceDir = new File("storage");
		if (persistenceDir.exists() && persistenceDir.isDirectory()) {
			File[] storageChunks = persistenceDir.listFiles();

			logger.info("Reading persistence data and populating cache");
			
			Set<String> toBeSentToNameNode = new HashSet<>();
			for (File storageChunk : storageChunks) {
				if (storageChunk.getName().endsWith(".stg")) {
					ObjectInputStream objectInputStream = null;
					try {
						objectInputStream = new ObjectInputStream(new FileInputStream(storageChunk));
						StorageChunk chunk = (StorageChunk) objectInputStream.readObject();
						if (chunk.getServerId() == this.storageManager.getCurrentServerId()) {
							switch (chunk.getDataType()) {
								case MAP:
									this.storageManager.setMapValue(chunk.getKey(), chunk.getField(), chunk.getValue(),
											true);
									break;
								case LIST:
									this.storageManager.addListItem(chunk.getKey(), chunk.getValue(), true);
									break;
								case STRING:
									this.storageManager.setValue(chunk.getKey(), chunk.getValue(), true);
									break;
								default:
									logger.warn("Not supported data type in chunk: " + storageChunk.getName());
									break;
							}
							toBeSentToNameNode.add(chunk.getKey());
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} finally {
						try {
							objectInputStream.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			logger.info("Loaded data from disk successfully");
			if(isShardNode) {
				if(toBeSentToNameNode.size() > 0) {
					ObjectMapper mapper = new ObjectMapper();
					String keyListStr = null;
					try {
						Map<String, Set<String>> reqMap = new HashMap<>();
						reqMap.put("keys", toBeSentToNameNode);
						keyListStr = mapper.writeValueAsString(reqMap);
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
					if(keyListStr != null) {
						ClusterRequest clusterRequest = new ClusterRequest(this.serverId, null, ProtocolCommand.UPDATE_KEYS_STORAGE, keyListStr);
						this.clusterManager.getNameNodeConnection().addRequest(clusterRequest);
					}
				}
			}
		}
	}

	public void persistData(String key, String field, String value, DataType dataType, int serverId) {
		StorageChunk chunk = new StorageChunk(key, field, value, dataType, this.serverId);
		this.storageChunkPersistenceQueue.add(chunk);
	}

	public void processStorageChunk(StorageChunk chunk) {
		File storageChunk = new File(this.persistenceDir, System.currentTimeMillis() + ".stg");
		ObjectOutputStream objectOutputStream = null;
		try {
			storageChunk.createNewFile();
			objectOutputStream = new ObjectOutputStream(new FileOutputStream(storageChunk));
			objectOutputStream.writeObject(chunk);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (objectOutputStream != null) {
				try {
					objectOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
