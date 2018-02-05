package com.quickcache.server.protocol;

public class ClusterResponse {

	private ProtocolResponseType protocolResponseType;
	private String responseBody;
	private boolean responseFromClient;

	public ClusterResponse(ProtocolResponseType protocolResponseType, String responseBody, boolean responseFromClient) {
		super();
		this.protocolResponseType = protocolResponseType;
		this.responseBody = responseBody;
		this.responseFromClient = responseFromClient;
	}

	public ProtocolResponseType getProtocolResponseType() {
		return protocolResponseType;
	}

	public String getResponseBody() {
		return responseBody;
	}
	
	public boolean isResponseFromClient() {
		return this.responseFromClient;
	}

}
