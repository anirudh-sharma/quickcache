package com.quickcache.server.protocol.processor;

import java.nio.channels.SocketChannel;
import java.util.List;

import org.springframework.stereotype.Component;

import com.quickcache.server.manager.ClusterManager;
import com.quickcache.server.protocol.ClusterResponse;

@Component
public interface RequestProcessor {

	ClusterResponse processRequest(List<String> clientRequestBuffer, ClusterManager clusterManager, SocketChannel socketChannel);

}
