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
import com.quickcache.server.protocol.processor.DefaultRequestProcessor;
import com.quickcache.server.protocol.processor.RequestProcessor;

public class CommunicationHandler implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(ClusterManager.class);

	private final int serverId;
	private Socket socket;
	private RequestProcessor requestProcessor;

	public CommunicationHandler(int serverId, Socket socket) {
		this.serverId = serverId;
		this.socket = socket;
		this.requestProcessor = new DefaultRequestProcessor();
	}

	@Override
	public void run() {
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
			if (connectToNameNode(bufferedReader, printWriter))
				logger.info("Connected to name node for server push");
			while (true) {
				List<String> clientRequestBuffer = new ArrayList<>();
				String clientRequestPart = bufferedReader.readLine();
				while (!(clientRequestPart).equals(ProtocolDefinition.SERVER_PUSH_END)) {
					if (!clientRequestPart.equals(ProtocolDefinition.EMPTY_STRING))
						clientRequestBuffer.add(clientRequestPart);
					clientRequestPart = bufferedReader.readLine();
				}
				logger.info("Server Push Recieved: "+clientRequestBuffer);
				ClusterResponse response = processRequest(clientRequestBuffer);

				printWriter.println(this.serverId);
				printWriter.println(ProtocolCommand.RESPONSE_FROM_CLIENT);
				if (response.getResponseBody() != null) {
					printWriter.println(response.getResponseBody());
				}
				printWriter.println(ProtocolDefinition.CLIENT_ACK_END.toString());
				printWriter.println("\n");
				printWriter.flush();

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ClusterResponse processRequest(List<String> clientRequestBuffer) {
		return this.requestProcessor.processRequest(clientRequestBuffer, null, null);
	}

	private boolean connectToNameNode(BufferedReader bufferedReader, PrintWriter printWriter) throws IOException {
		printWriter.println(this.serverId);
		printWriter.println(ProtocolCommand.REGISTER_FOR_SERVER_PUSH);
		printWriter.println(ProtocolDefinition.REQUEST_END);
		printWriter.flush();
		return true;
	}

}
