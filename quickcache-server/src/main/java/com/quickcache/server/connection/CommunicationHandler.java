package com.quickcache.server.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quickcache.server.manager.ClusterManager;
import com.quickcache.server.protocol.ClusterResponse;
import com.quickcache.server.protocol.ProtocolCommand;
import com.quickcache.server.protocol.ProtocolDefinition;
import com.quickcache.server.protocol.ProtocolResponseType;

public class CommunicationHandler implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);

	private Socket socket;

	public CommunicationHandler(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
			while (true) {
				List<String> clientRequestBuffer = new ArrayList<>();
				String clientRequestPart = null;
				while (!(clientRequestPart = bufferedReader.readLine()).equals(ProtocolDefinition.REQUEST_END)) {
					clientRequestBuffer.add(clientRequestPart);
				}
				ClusterResponse response = handleRequest(clientRequestBuffer);
				printWriter.println(response.getProtocolResponseType().toString());
				if (response.getResponseBody() != null) {
					for (String responsePart : response.getResponseBody()) {
						printWriter.println(responsePart);
					}
				}
				printWriter.println(ProtocolDefinition.RESPONSE_END);
				printWriter.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ClusterResponse handleRequest(List<String> clientRequestBuffer) {
		ProtocolCommand protocolCommand = ProtocolCommand.valueOf(clientRequestBuffer.get(0));
		ClusterResponse response = null;
		switch (protocolCommand) {
		case REGISTER:
			response = new ClusterResponse(ProtocolResponseType.SUCCESS, null);
			logger.info("Successfully registered a shard node at: "+this.socket.getRemoteSocketAddress());
			break;
		default:
			response = new ClusterResponse(ProtocolResponseType.UNDEFINED, null);
			break;
		}
		return response;
	}

}
