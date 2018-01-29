package com.quickcache.server.protocol;

import java.util.List;

public class ClusterRequest {

	private ProtocolCommand protocolCommand;
	private List<String> requestBody;

	public ClusterRequest(ProtocolCommand protocolCommand, List<String> requestBody) {
		super();
		this.protocolCommand = protocolCommand;
		this.requestBody = requestBody;
	}

	public ProtocolCommand getProtocolCommand() {
		return protocolCommand;
	}

	public List<String> getRequestBody() {
		return requestBody;
	}

}
