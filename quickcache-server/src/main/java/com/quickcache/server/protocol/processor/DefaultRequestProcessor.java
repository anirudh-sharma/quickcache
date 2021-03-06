package com.quickcache.server.protocol.processor;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickcache.server.connection.ServerNodeConnection;
import com.quickcache.server.manager.ClusterManager;
import com.quickcache.server.manager.StorageManager;
import com.quickcache.server.protocol.ClusterRequest;
import com.quickcache.server.protocol.ClusterResponse;
import com.quickcache.server.protocol.ProtocolCommand;
import com.quickcache.server.protocol.ProtocolResponseType;

@Component
public class DefaultRequestProcessor implements RequestProcessor {

	private static final Logger logger = LoggerFactory.getLogger(DefaultRequestProcessor.class);

	private StorageManager storageManager;
	
	public DefaultRequestProcessor(StorageManager storageManager) {
		this.storageManager = storageManager;
	}

	@Override
	public ClusterResponse processRequest(List<String> clientRequestBuffer, ClusterManager clusterManager,
			SocketChannel socketChannel) {
		int serverId = Integer.parseInt(clientRequestBuffer.get(0));
		ProtocolCommand protocolCommand = ProtocolCommand.valueOf(clientRequestBuffer.get(1));
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> requestBodyMap = null;
		if (clientRequestBuffer.size() >= 3) {
			String requestBody = clientRequestBuffer.get(2);
			if (requestBody != null && !requestBody.isEmpty()) {
				TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
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
		case UPDATE_KEYS_STORAGE:
			logger.info("Updating keys storage in name node based on data received from shard node");
			logger.info(requestBodyMap.toString());
			List<String> keyList = (ArrayList<String>)requestBodyMap.get("keys");;

			this.storageManager.updateKeysCache(keyList, serverId);
			response = new ClusterResponse(ProtocolResponseType.SUCCESS, null, true);
			break;
		case SET_STRING:
			logger.info("Set string operation from name node");
			logger.info(requestBodyMap.toString());
			this.storageManager.setValue((String)requestBodyMap.get("key"), (String)requestBodyMap.get("value"), false);
			response = new ClusterResponse(
					ProtocolResponseType.SUCCESS, "{\"requestId\":" + requestBodyMap.get("requestId") + ",\"key\":\""
							+ requestBodyMap.get("key") + "\",\"value\":\"" + requestBodyMap.get("value") + "\"}",
					false);
			break;
		case GET_STRING:
			logger.info("Get string operation from name node");
			logger.info(requestBodyMap.toString());
			value = this.storageManager.getValue((String)requestBodyMap.get("key"));
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
			value = this.storageManager.getMapValue((String)requestBodyMap.get("key"), (String)requestBodyMap.get("field"));
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
			Set<String> fields = this.storageManager.getMapFields((String)requestBodyMap.get("key"));
			try {
				Map<String, String> resMap = new HashMap<>();
				if (fields != null) {
					resMap.put("fields", mapper.writeValueAsString(fields));
				}
				if (fields.size() == 0) {
					resMap.put("fields", mapper.writeValueAsString(new HashSet<String>()));
				} else {
					resMap.put("fields", null);
				}
				resMap.put("requestId", (String)requestBodyMap.get("requestId"));
				resMap.put("key", (String)requestBodyMap.get("key"));
				response = new ClusterResponse(ProtocolResponseType.SUCCESS, mapper.writeValueAsString(resMap), false);
			} catch (JsonProcessingException exception) {
				exception.printStackTrace();
			}
			break;
		case GET_MAP_FIELD_VALUES:
			logger.info("Get map field values operation from name node");
			logger.info(requestBodyMap.toString());
			valueMap = this.storageManager.getMapFieldValues((String)requestBodyMap.get("key"));
			try {
				Map<String, String> resMap = new HashMap<>();
				if (valueMap != null) {
					resMap.put("map", mapper.writeValueAsString(valueMap));
				} else {
					resMap.put("map", null);
				}
				resMap.put("requestId", (String)requestBodyMap.get("requestId"));
				resMap.put("key", (String)requestBodyMap.get("key"));
				response = new ClusterResponse(ProtocolResponseType.SUCCESS, mapper.writeValueAsString(resMap), false);
			} catch (JsonProcessingException exception) {
				exception.printStackTrace();
			}
			break;
		case SET_MAP_VALUE:
			logger.info("Set map value operation from name node");
			logger.info(requestBodyMap.toString());
			this.storageManager.setMapValue((String)requestBodyMap.get("key"), (String)requestBodyMap.get("field"),
					(String)requestBodyMap.get("value"), false);
			response = new ClusterResponse(ProtocolResponseType.SUCCESS,
					"{\"requestId\":" + requestBodyMap.get("requestId") + ",\"key\":\"" + requestBodyMap.get("key")
							+ "\",\"field\":\"" + requestBodyMap.get("field") + "\",\"value\":\""
							+ requestBodyMap.get("value") + "\"}",
					false);
			break;

		case GET_LIST_ITEMS:
			logger.info("Get list items operation from name node");
			logger.info(requestBodyMap.toString());
			List<String> items = this.storageManager.getListItems((String)requestBodyMap.get("key"),
					Boolean.parseBoolean((String)requestBodyMap.get("allItems")),
					Integer.parseInt((String)requestBodyMap.get("offset")), Integer.parseInt((String)requestBodyMap.get("length")));
			try {
				Map<String, String> resMap = new HashMap<>();
				if (items != null) {
					resMap.put("items", mapper.writeValueAsString(items));
				} else {
					resMap.put("items", null);
				}
				resMap.put("requestId", (String)requestBodyMap.get("requestId"));
				resMap.put("key", (String)requestBodyMap.get("key"));
				logger.info("Processed: " + mapper.writeValueAsString(resMap));
				response = new ClusterResponse(ProtocolResponseType.SUCCESS, mapper.writeValueAsString(resMap), false);
			} catch (JsonProcessingException exception) {
				exception.printStackTrace();
			}
			break;
		case ADD_LIST_ITEM:
			logger.info("Add list item operation from name node");
			logger.info(requestBodyMap.toString());
			this.storageManager.addListItem((String)requestBodyMap.get("key"), (String)requestBodyMap.get("item"), false);
			try {
				Map<String, String> resMap = new HashMap<>();
				resMap.put("item", (String)requestBodyMap.get("item"));
				resMap.put("requestId", (String)requestBodyMap.get("requestId"));
				resMap.put("key", (String)requestBodyMap.get("key"));
				response = new ClusterResponse(ProtocolResponseType.SUCCESS, mapper.writeValueAsString(resMap), false);
			} catch (JsonProcessingException exception) {
				exception.printStackTrace();
			}
			break;
		case REMOVE_LIST_ITEM:
			logger.info("Remove list item operation from name node");
			logger.info(requestBodyMap.toString());
			this.storageManager.removeListItem((String)requestBodyMap.get("key"), Integer.parseInt((String)requestBodyMap.get("index")));
			try {
				Map<String, String> resMap = new HashMap<>();
				resMap.put("item", (String)requestBodyMap.get("item"));
				resMap.put("requestId", (String)requestBodyMap.get("requestId"));
				resMap.put("key", (String)requestBodyMap.get("key"));
				response = new ClusterResponse(ProtocolResponseType.SUCCESS, mapper.writeValueAsString(resMap), false);
			} catch (JsonProcessingException exception) {
				exception.printStackTrace();
			}
			break;
		default:
			response = new ClusterResponse(ProtocolResponseType.UNDEFINED, null, true);
			break;
		}
		return response;
	}

}
