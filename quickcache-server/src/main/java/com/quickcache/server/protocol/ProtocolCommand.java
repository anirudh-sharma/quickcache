package com.quickcache.server.protocol;

public enum ProtocolCommand {
	REGISTER,
	REGISTER_FOR_SERVER_PUSH,
	HANDSHAKE_COMPLETE,
	RESPONSE_FROM_CLIENT,
	GET_STRING,
	SET_STRING,
	GET_MAP_VALUE,
	SET_MAP_VALUE;
}
