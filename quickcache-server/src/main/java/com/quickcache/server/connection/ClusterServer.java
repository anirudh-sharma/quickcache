package com.quickcache.server.connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quickcache.server.manager.ClusterManager;

public class ClusterServer {

	private static final Logger logger = LoggerFactory.getLogger(ClusterServer.class);

	private final ServerSocket serverSocket;
	private final ClusterManager clusterManager;

	public ClusterServer(ServerSocket serverSocket, ClusterManager clusterManager) {
		this.serverSocket = serverSocket;
		this.clusterManager = clusterManager;

		logger.info("Name node started...");
		logger.info("Cluster listening at: " + this.serverSocket.getLocalPort());
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					Socket socket;
					try {
						socket = serverSocket.accept();
						logger.info("Node added to cluster..");
						new Thread(new CommunicationHandler(socket)).start();
					} catch (IOException e) {
						logger.error("Error accepting connection. Retry..");
					}
				}
			}
		}).start();
	}

}
