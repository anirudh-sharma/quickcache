package com.quickcache.server.connection;

import java.nio.channels.SocketChannel;

public class ServerNodeConnection{

	private SocketChannel channel;
	private SocketChannel serverPushChannel;

	public ServerNodeConnection() {
		super();
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public void setChannel(SocketChannel channel) {
		this.channel = channel;
	}

	public SocketChannel getServerPushChannel() {
		return serverPushChannel;
	}

	public void setServerPushChannel(SocketChannel serverPushChannel) {
		this.serverPushChannel = serverPushChannel;
	}

}
