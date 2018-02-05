package com.quickcache.server.manager;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.quickcache.server.connection.ClientCommunicationHandler;
import com.quickcache.server.connection.ClusterServer;
import com.quickcache.server.connection.CommunicationHandler;
import com.quickcache.server.connection.NodeConnection;
import com.quickcache.server.connection.ServerNodeConnection;
import com.quickcache.server.constants.AppConstants;
import com.quickcache.server.protocol.ClusterRequest;
import com.quickcache.server.protocol.ProtocolCommand;

@Component
public class ClusterManager {

	private static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);

	@Value("${" + AppConstants.ClusterConnectionPropertyName + "}")
	private int clusterPort;
	@Value("${" + AppConstants.NameNodeUrlPropertyName + "}")
	private String nameNodeUrl;
	@Value("${" + AppConstants.ServerIdPropertyName + "}")
	private int serverId;

	private ClusterServer clusterServer;
	private Map<Integer, ServerNodeConnection> nodeMap;
	private BlockingQueue<ClusterRequest> pushRequests;

	public ClusterManager() {
		nodeMap = new HashMap<Integer, ServerNodeConnection>();
	}

	public void setClusterPort(int clusterPort) {
		this.clusterPort = clusterPort;
	}

	public void setNameNodeUrl(String nameNodeUrl) {
		this.nameNodeUrl = nameNodeUrl;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	@PostConstruct
	public void init() {
		if (!nameNodeUrl.equals(AppConstants.DummyValue)) {
			logger.info("Running in clustered mode");
			try {
				String[] urlTokens = this.nameNodeUrl.split(":");
				Socket socket = new Socket(urlTokens[0], Integer.parseInt(urlTokens[1]));
				Socket pushSocket = new Socket(urlTokens[0], Integer.parseInt(urlTokens[1]));
				NodeConnection nodeConnection = new NodeConnection();
				ClusterRequest clusterRequest1 = new ClusterRequest(this.serverId, null, ProtocolCommand.REGISTER, null);
				nodeConnection.addRequest(clusterRequest1);
				new Thread(new ClientCommunicationHandler(socket, nodeConnection)).start();
				new Thread(new CommunicationHandler(this.serverId, pushSocket)).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			this.nodeMap.put(this.serverId, new ServerNodeConnection());
			this.pushRequests = new LinkedBlockingQueue<>();
			this.clusterServer = new ClusterServer(this, this.clusterPort);
			this.clusterServer.initialize();
		}

	}

	public ClusterServer getClusterServer() {
		return this.clusterServer;
	}

	public Map<Integer, ServerNodeConnection> getNodeMap() {
		return this.nodeMap;
	}

	public void addRequest(ClusterRequest clusterRequest) {
		this.pushRequests.add(clusterRequest);
		return;
	}

	public ClusterRequest getPendingRequest() {
		try {
			return this.pushRequests.poll(2, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
