package com.quickcache.server.protocol.processor;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickcache.server.QuickCache;
import com.quickcache.server.connection.ServerNodeConnection;
import com.quickcache.server.manager.ClusterManager;
import com.quickcache.server.protocol.ClusterRequest;
import com.quickcache.server.protocol.ClusterResponse;
import com.quickcache.server.protocol.ProtocolCommand;
import com.quickcache.server.protocol.ProtocolResponseType;

@Component
public class DefaultRequestProcessor implements RequestProcessor {

	private static final Logger logger = LoggerFactory.getLogger(DefaultRequestProcessor.class);

	@Override
	public ClusterResponse processRequest(List<String> clientRequestBuffer, ClusterManager clusterManager,
			SocketChannel socketChannel) {
		int serverId = Integer.parseInt(clientRequestBuffer.get(0));
		ProtocolCommand protocolCommand = ProtocolCommand.valueOf(clientRequestBuffer.get(1));
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> requestBodyMap = null;
		if (clientRequestBuffer.size() >= 3) {
			String requestBody = clientRequestBuffer.get(2);
			if (requestBody != null && !requestBody.isEmpty()) {
				TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {
				};
				try {
					requestBodyMap = mapper.readValue(requestBody, typeRef);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		ClusterResponse response = null;
		ServerNodeConnection nodeConnection;
		String value = null;
		Set<String> fields = null;
		Map<String, String> valueMap = null;
		switch (protocolCommand) {
		case REGISTER:
			nodeConnection = clusterManager.getNodeMap().get(serverId);
			if (nodeConnection == null) {
				nodeConnection = new ServerNodeConnection();
				clusterManager.getNodeMap().put(serverId, nodeConnection);
			}
			nodeConnection.setChannel(socketChannel);
			logger.info("Shard node registered...");
			response = new ClusterResponse(ProtocolResponseType.SUCCESS, null, true);
			break;
		case REGISTER_FOR_SERVER_PUSH:
			nodeConnection = clusterManager.getNodeMap().get(serverId);
			if (nodeConnection == null) {
				nodeConnection = new ServerNodeConnection();
				clusterManager.getNodeMap().put(serverId, nodeConnection);
			}
			nodeConnection.setServerPushChannel(socketChannel);
			clusterManager.addRequest(new ClusterRequest(1, serverId, ProtocolCommand.HANDSHAKE_COMPLETE, null));
			logger.info("Shard node registered for server push...");
			response = new ClusterResponse(ProtocolResponseType.SUCCESS, null, false);
			break;
		case HANDSHAKE_COMPLETE:
			response = new ClusterResponse(ProtocolResponseType.SUCCESS, null, true);
			break;
		case RESPONSE_FROM_CLIENT:
			logger.info("Response received from client");
			response = new ClusterResponse(ProtocolResponseType.SUCCESS, null, false);
			break;
		case SET_STRING:
			logger.info("Set string operation from name node");
			logger.info(requestBodyMap.toString());
			QuickCache.getStorageManager().setValue(requestBodyMap.get("key"), requestBodyMap.get("value"));
			response = new ClusterResponse(ProtocolResponseType.SUCCESS, null, false);
			response = new ClusterResponse(
					ProtocolResponseType.SUCCESS, "{\"requestId\":" + requestBodyMap.get("requestId") + ",\"key\":\""
							+ requestBodyMap.get("key") + "\",\"value\":\"" + requestBodyMap.get("value") + "\"}",
					false);
			break;
		case GET_STRING:
			logger.info("Get string operation from name node");
			logger.info(requestBodyMap.toString());
			value = QuickCache.getStorageManager().getValue(requestBodyMap.get("key"));
			if (value != null) {
				value = "\"" + value + "\"";
			}
			response = new ClusterResponse(ProtocolResponseType.SUCCESS,
					"{\"requestId\":" + requestBodyMap.get("requestId") + ",\"key\":\"" + requestBodyMap.get("key")
							+ "\",\"value\":" + value + "}",
					false);
			break;
		case GET_MAP_VALUE:
			logger.info("Get map value operation from name node");
			logger.info(requestBodyMap.toString());
			value = QuickCache.getStorageManager().getMapValue(requestBodyMap.get("key"), requestBodyMap.get("field"));
			if (value != null) {
				value = "\"" + value + "\"";
			}
			response = new ClusterResponse(ProtocolResponseType.SUCCESS,
					"{\"requestId\":" + requestBodyMap.get("requestId") + ",\"key\":\"" + requestBodyMap.get("key")
							+ "\",\"field\":\"" + requestBodyMap.get("field") + "\",\"value\":" + value + "}",
					false);
			break;
		case GET_MAP_FIELDS:
			logger.info("Get map fields operation from name node");
			logger.info(requestBodyMap.toString());
			fields = QuickCache.getStorageManager().getMapFields(requestBodyMap.get("key"));
			try {
				Map<String, String> resMap = new HashMap<>();
				if (fields != null) {
					resMap.put("fields", mapper.writeValueAsString(fields));
				} else {
					resMap.put("fields", null);
				}
				resMap.put("requestId", requestBodyMap.get("requestId"));
				resMap.put("key", requestBodyMap.get("key"));
				response = new ClusterResponse(ProtocolResponseType.SUCCESS, mapper.writeValueAsString(resMap), false);
			} catch (JsonProcessingException exception) {
				exception.printStackTrace();
			}
			break;
		case GET_MAP_FIELD_VALUES:
			logger.info("Get map field values operation from name node");
			logger.info(requestBodyMap.toString());
			valueMap = QuickCache.getStorageManager().getMapFieldValues(requestBodyMap.get("key"));
			try {
				Map<String, String> resMap = new HashMap<>();
				if (valueMap != null) {
					resMap.put("map", mapper.writeValueAsString(valueMap));
				} else {
					resMap.put("map", null);
				}
				resMap.put("requestId", requestBodyMap.get("requestId"));
				resMap.put("key", requestBodyMap.get("key"));
				response = new ClusterResponse(ProtocolResponseType.SUCCESS, mapper.writeValueAsString(resMap), false);
			} catch (JsonProcessingException exception) {
				exception.printStackTrace();
			}
			break;
		case SET_MAP_VALUE:
			logger.info("Set map value operation from name node");
			logger.info(requestBodyMap.toString());
			QuickCache.getStorageManager().setMapValue(requestBodyMap.get("key"), requestBodyMap.get("field"),
					requestBodyMap.get("value"));
			response = new ClusterResponse(ProtocolResponseType.SUCCESS, null, false);
			response = new ClusterResponse(ProtocolResponseType.SUCCESS,
					"{\"requestId\":" + requestBodyMap.get("requestId") + ",\"key\":\"" + requestBodyMap.get("key")
							+ "\",\"field\":\"" + requestBodyMap.get("field") + "\",\"value\":\""
							+ requestBodyMap.get("value") + "\"}",
					false);
			break;
		default:
			response = new ClusterResponse(ProtocolResponseType.UNDEFINED, null, true);
			break;
		}
		return response;
	}

}
