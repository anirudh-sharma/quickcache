package com.quickcache.server.storage.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ListStorage implements StorageType {

	private List<String> dataList;
	private Date lastModified;
	private Date lastAccessed;

	public ListStorage() {
		dataList = new ArrayList<String>();
		this.lastModified = new Date();
		this.lastAccessed = new Date();
	}

	public boolean add(String item) {
		this.lastModified = new Date();
		return this.dataList.add(item);
	}

	public String remove(int index) {
		this.lastModified = new Date();
		return this.dataList.remove(index);
	}

	public boolean remove(String item) {
		this.lastModified = new Date();
		return this.dataList.remove(item);
	}

	public String getItemAt(int index) {
		this.lastAccessed = new Date();
		return this.dataList.get(index);
	}

	public List<String> getItems() {
		this.lastAccessed = new Date();
		return Collections.unmodifiableList(this.dataList);
	}

	public List<String> getItems(int offset, int length) {
		this.lastAccessed = new Date();
		if (length == -1) 
			length = this.dataList.size();
		return new ArrayList<>(this.dataList.subList(offset, length));
	}

	public Date getLastModified() {
		return lastModified;
	}

	public Date getLastAccessed() {
		return lastAccessed;
	}
}
