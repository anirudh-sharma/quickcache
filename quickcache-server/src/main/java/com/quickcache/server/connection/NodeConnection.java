package com.quickcache.server.connection;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.quickcache.server.protocol.ClusterRequest;

public class NodeConnection {

	private BlockingQueue<ClusterRequest> requests;

	public NodeConnection() {
		this.requests = new LinkedBlockingQueue<>();
	}

	public void addRequest(ClusterRequest clusterRequest) {
		this.requests.add(clusterRequest);
	}

	public ClusterRequest getPendingRequest() {
		try {
			return this.requests.poll(2, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

}
