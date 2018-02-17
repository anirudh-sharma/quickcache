package com.quickcache.server.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickcache.server.QuickCache;
import com.quickcache.server.manager.ClusterManager;
import com.quickcache.server.protocol.ClusterRequest;
import com.quickcache.server.protocol.ClusterRequestResponseWrapper;
import com.quickcache.server.protocol.ClusterResponse;
import com.quickcache.server.protocol.ProtocolDefinition;
import com.quickcache.server.protocol.processor.DefaultRequestProcessor;
import com.quickcache.server.protocol.processor.RequestProcessor;

public class ClusterServer {

	private static final Logger logger = LoggerFactory.getLogger(ClusterServer.class);

	private final ClusterManager clusterManager;
	private final RequestProcessor requestProcessor;
	private final int clusterPort;

	public ClusterServer(ClusterManager clusterManager, int clusterPort) {
		this.clusterManager = clusterManager;
		this.clusterPort = clusterPort;
		this.requestProcessor = new DefaultRequestProcessor();
	}

	public void initialize() {
		ServerSocketChannel serverSocketChannel = null;
		Selector selector = null;
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();

			serverSocketChannel.configureBlocking(false);

			serverSocketChannel.socket().bind(new InetSocketAddress(clusterPort));

			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException exception) {
			exception.printStackTrace();
		}

		logger.info("Name node started...");

		final Selector clientConnectionSelector = selector;

		new Thread(new Runnable() {
			@Override
			public void run() {
				logger.info("Started connection accepting thread...");
				while (true) {
					try {
						clientConnectionSelector.select();
						Iterator<SelectionKey> iterator = clientConnectionSelector.selectedKeys().iterator();

						while (iterator.hasNext()) {
							SelectionKey selectionKey = iterator.next();
							iterator.remove();

							if (!selectionKey.isValid()) {
								continue;
							}
							if (selectionKey.isAcceptable()) {
								connectionAcceptHandler(selectionKey, clientConnectionSelector);
							}
							if (selectionKey.isReadable()) {
								connectionReadHandler(selectionKey);
							}
						}
					} catch (IOException e) {
						logger.error("Error accepting connection. Retry..");
					}
				}
			}
		}, "CONNECTION-ACCEPT-THREAD").start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				logger.info("Started server push thread...");
				ClusterRequest clusterRequest = null;
				while ((clusterRequest = clusterManager.getPendingRequest()) != null) {
					SocketChannel socketChannel = clusterManager.getNodeMap().get(clusterRequest.getToServerId())
							.getServerPushChannel();
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append(clusterRequest.getFromServerId() + "\n");
					stringBuilder.append(clusterRequest.getProtocolCommand().toString() + "\n");
					if (clusterRequest.getRequestBody() != null)
						stringBuilder.append(clusterRequest.getRequestBody() + "\n");
					stringBuilder.append(ProtocolDefinition.SERVER_PUSH_END + "\n");
					ByteBuffer buffer = ByteBuffer.wrap(stringBuilder.toString().getBytes());
					try {
						logger.info("Sending " + clusterRequest.getProtocolCommand() + " to shard node");
						while (buffer.hasRemaining())
							socketChannel.write(buffer);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}, "PUSH-PROCESS-THREAD").start();

	}

	public void connectionAcceptHandler(SelectionKey selectionKey, Selector selector) throws IOException {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);
		logger.info("Shard node connected at: " + socketChannel.socket().getRemoteSocketAddress().toString());

		socketChannel.register(selector, SelectionKey.OP_READ);
	}

	public void connectionReadHandler(SelectionKey selectionKey) throws IOException {
		SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
		try {
			socketChannel.configureBlocking(false);
			logger.info("Connection Handler Started...");

			String requestStrData = new String();

			ByteBuffer buffer = ByteBuffer.allocate(2048);

			int numRead = -1;
			numRead = socketChannel.read(buffer);
			if (numRead == -1) {
				Socket socket = socketChannel.socket();
				SocketAddress remoteAddr = socket.getRemoteSocketAddress();
				logger.info("Connection closed by client: " + remoteAddr);
				socketChannel.close();
				selectionKey.cancel();
				return;
			}

			byte[] data = new byte[numRead];
			System.arraycopy(buffer.array(), 0, data, 0, numRead);
			requestStrData = new String(data);
			data = null;
			logger.info("Request Data: " + requestStrData);

			//Chip off protocol request end/client acknowledgement string from request
			List<String> clientRequestBuffer = new ArrayList<>(3);
			for (String requestPart : requestStrData.split("\\n")) {
				clientRequestBuffer.add(requestPart);
			}
			int requestBuffSize = clientRequestBuffer.size();
			String responseBody = null;
			if (requestBuffSize >= 3 && (clientRequestBuffer.get((requestBuffSize - 1))
					.equals(ProtocolDefinition.REQUEST_END)
					|| clientRequestBuffer.get((requestBuffSize - 1)).equals(ProtocolDefinition.CLIENT_ACK_END))) {
				clientRequestBuffer.remove((requestBuffSize - 1));
				if(clientRequestBuffer.size() >=3 ){
					responseBody = clientRequestBuffer.get(2);
				}
			}

			ClusterResponse response = requestProcessor.processRequest(clientRequestBuffer, clusterManager,
					socketChannel);

			if (!response.isResponseRequired()) {
				logger.info("Response from client......" + clientRequestBuffer);
				Map<String, String> responseBodyMap = null;
				if(responseBody != null) {
					ObjectMapper mapper = new ObjectMapper();
					TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {
					};
					try {
						responseBodyMap = mapper.readValue(responseBody, typeRef);
						if(responseBodyMap != null) {
							int requestId = Integer.parseInt(responseBodyMap.get("requestId"));
							ClusterRequestResponseWrapper clusterRequestResponseWrapper = QuickCache.getClusterRequest(requestId);
							clusterRequestResponseWrapper.setResponseBody(responseBodyMap);
							clusterRequestResponseWrapper.getCountDownLatch().countDown();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				StringBuilder responseBuilder = new StringBuilder();
				responseBuilder.append(response.getProtocolResponseType().toString() + "\n");
				if (response.getResponseBody() != null) {
					responseBuilder.append(response.getResponseBody() + "\n");
				}
				responseBuilder.append(ProtocolDefinition.RESPONSE_END.toString());

				ByteBuffer byteBuffer = ByteBuffer.wrap(responseBuilder.toString().getBytes());
				socketChannel.write(byteBuffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public ClusterManager getClusterManager() {
		return this.clusterManager;
	}
}
