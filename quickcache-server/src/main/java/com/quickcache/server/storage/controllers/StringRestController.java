package com.quickcache.server.storage.controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.quickcache.server.manager.StorageManager;

@RestController
@RequestMapping("/string")
public class StringRestController {

	@Autowired
	private StorageManager storageManager;

	public void setStorageManager(StorageManager storageManager) {
		this.storageManager = storageManager;
	}

	@RequestMapping("/{key}")
	public String getValue(@PathVariable("key") String key) {
		return storageManager.getValue(key);
	}

	@RequestMapping(value = "/{key}", method = RequestMethod.POST)
	public ResponseEntity<String> setValue(@PathVariable("key") String key, @RequestBody String value) {
		try {
			value = URLDecoder.decode(value, "utf-8");
			if(value.endsWith("=")){
				value = value.substring(0, (value.length() - 1));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		storageManager.setValue(key, value, false);
		return new ResponseEntity<String>(value, HttpStatus.ACCEPTED);
	}
}
