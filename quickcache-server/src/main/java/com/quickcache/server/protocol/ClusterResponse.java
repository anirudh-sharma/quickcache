package com.quickcache.server.protocol;

public class ClusterResponse {

	private ProtocolResponseType protocolResponseType;
	private String responseBody;
	private boolean responseRequired;

	public ClusterResponse(ProtocolResponseType protocolResponseType, String responseBody, boolean responseRequired) {
		super();
		this.protocolResponseType = protocolResponseType;
		this.responseBody = responseBody;
		this.responseRequired = responseRequired;
	}

	public ProtocolResponseType getProtocolResponseType() {
		return protocolResponseType;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public boolean isResponseRequired() {
		return this.responseRequired;
	}

}
