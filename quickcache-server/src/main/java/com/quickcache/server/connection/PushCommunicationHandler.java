package com.quickcache.server.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.quickcache.server.protocol.ClusterRequest;
import com.quickcache.server.protocol.ProtocolDefinition;
import com.quickcache.server.protocol.ProtocolResponseType;

public class PushCommunicationHandler implements Runnable{

	private Socket socket;
	private NodeConnection nodeConnection;

	public PushCommunicationHandler(Socket socket, NodeConnection nodeConnection) {
		this.socket = socket;
		this.nodeConnection = nodeConnection;
	}

	@Override
	public void run() {
		try {
			BufferedReader bufferedReader;
				bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
			ClusterRequest clusterRequest = null;
			while((clusterRequest = this.nodeConnection.getPendingRequest()) != null) {
				sendToNameNode(clusterRequest, printWriter);
				String serverResponse = bufferedReader.readLine();
				if(!serverResponse.equals(ProtocolResponseType.SUCCESS.toString())) {
					sendToNameNode(clusterRequest, printWriter);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendToNameNode(ClusterRequest clusterRequest, PrintWriter printWriter) {
		printWriter.println(clusterRequest.getProtocolCommand().toString());
		if(clusterRequest.getRequestBody() != null) {
			printWriter.println(clusterRequest.getRequestBody());
		}
		printWriter.println(ProtocolDefinition.REQUEST_END);
		printWriter.flush();
	}
	
}
