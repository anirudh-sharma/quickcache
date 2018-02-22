package com.quickcache.server.protocol;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class ClusterRequestResponseWrapper {

	private long requestId;
	private ClusterRequest clusterRequest;
	private Map<String, String> responseBody;
	private CountDownLatch countDownLatch;

	public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}

	public ClusterRequest getClusterRequest() {
		return clusterRequest;
	}

	public void setClusterRequest(ClusterRequest clusterRequest) {
		this.clusterRequest = clusterRequest;
	}

	public Map<String, String> getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(Map<String, String> responseBody) {
		this.responseBody = responseBody;
	}

	public CountDownLatch getCountDownLatch() {
		return countDownLatch;
	}

	public void setCountDownLatch(CountDownLatch countDownLatch) {
		this.countDownLatch = countDownLatch;
	}

}
