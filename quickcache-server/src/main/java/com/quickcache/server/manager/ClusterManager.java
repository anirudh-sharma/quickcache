package com.quickcache.server.manager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.quickcache.server.connection.ClientCommunicationHandler;
import com.quickcache.server.connection.ClusterServer;
import com.quickcache.server.connection.NodeConnection;
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
	
	private ClusterServer clusterServer;
	private Map<String, NodeConnection> nodeMap;

	public ClusterManager() {
		nodeMap = new HashMap<String, NodeConnection>();
	}

	public void setClusterPort(int clusterPort) {
		this.clusterPort = clusterPort;
	}

	public void setNameNodeUrl(String nameNodeUrl) {
		this.nameNodeUrl = nameNodeUrl;
	}

	@PostConstruct
	public void init() {
		if (!nameNodeUrl.equals(AppConstants.DummyValue)) {
			logger.info("Running in clustered mode");
			try {
				String [] urlTokens = this.nameNodeUrl.split(":");
				Socket socket = new Socket(urlTokens[0], Integer.parseInt(urlTokens[1]));
				NodeConnection nodeConnection = new NodeConnection();
				ClusterRequest clusterRequest = new ClusterRequest(ProtocolCommand.REGISTER, null);
				nodeConnection.addRequest(clusterRequest);
				new Thread(new ClientCommunicationHandler(socket, nodeConnection)).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			ServerSocket serverSocket = null;
			try {
				serverSocket = new ServerSocket(this.clusterPort);
				this.clusterServer = new ClusterServer(serverSocket, this);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public ClusterServer getClusterServer() {
		return this.clusterServer;
	}
	
	public Map<String, NodeConnection> getNodeMap() {
		return this.nodeMap;
	}
}
