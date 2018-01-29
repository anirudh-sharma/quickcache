package com.quickcache.server.protocol;

import java.util.List;

public class ClusterResponse {

	private ProtocolResponseType protocolResponseType;
	private List<String> responseBody;

	public ClusterResponse(ProtocolResponseType protocolResponseType, List<String> responseBody) {
		super();
		this.protocolResponseType = protocolResponseType;
		this.responseBody = responseBody;
	}

	public ProtocolResponseType getProtocolResponseType() {
		return protocolResponseType;
	}

	public List<String> getResponseBody() {
		return responseBody;
	}

}
