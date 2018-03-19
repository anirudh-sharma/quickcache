package com.quickcache.server.persistence;

import java.io.Serializable;

import com.quickcache.server.constants.DataType;

public class StorageChunk implements Serializable {

	private String key;
	private String field;
	private String value;
	private int serverId;
	private DataType dataType;

	public StorageChunk(String key, String field, String value, DataType dataType, int serverId) {
		super();
		this.key = key;
		this.field = field;
		this.value = value;
		this.dataType = dataType;
		this.serverId = serverId;
	}

	public String getKey() {
		return key;
	}

	public String getField() {
		return field;
	}

	public String getValue() {
		return value;
	}

	public DataType getDataType() {
		return dataType;
	}
	
	public int getServerId() {
		return this.serverId;
	}

}
