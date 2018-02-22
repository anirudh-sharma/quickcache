package com.quickcache.server.storage.types;

import java.util.Date;

public class StringStorage implements StorageType {

	private String data;
	private Date lastModified;
	private Date lastAccessed;

	public StringStorage(String data) {
		this.data = data;
		this.lastModified = new Date();
		this.lastAccessed = new Date();
	}

	public String getData() {
		this.lastAccessed = new Date();
		return data;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public Date getLastAccessed() {
		return lastAccessed;
	}

}
