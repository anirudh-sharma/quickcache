package com.quickcache.server.protocol;

import java.util.List;

public class ClusterRequest {

	private Integer toServerId;
	private Integer fromServerId;
	private ProtocolCommand protocolCommand;
	private String requestBody;

	public ClusterRequest(Integer fromServerId, Integer toServerId, ProtocolCommand protocolCommand, String requestBody) {
		super();
		this.fromServerId = fromServerId;
		this.toServerId = toServerId;
		this.protocolCommand = protocolCommand;
		this.requestBody = requestBody;
	}

	public Integer getFromServerId() {
		return this.fromServerId;
	}

	public Integer getToServerId() {
		return this.toServerId;
	}

	public ProtocolCommand getProtocolCommand() {
		return protocolCommand;
	}

	public String getRequestBody() {
		return requestBody;
	}

}
