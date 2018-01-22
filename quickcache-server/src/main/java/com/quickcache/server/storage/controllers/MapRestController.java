package com.quickcache.server.storage.controllers;

import java.util.Map;
import java.util.Set;

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
@RequestMapping("/map")
public class MapRestController {

	@Autowired
	private StorageManager storageManager;

	public void setStorageManager(StorageManager storageManager) {
		this.storageManager = storageManager;
	}

	@RequestMapping("/{key}/{field}")
	public String getMapValue(@PathVariable("key") String key, @PathVariable("field") String field) {
		return storageManager.getMapValue(key, field);
	}

	@RequestMapping("/{key}/fields")
	public Set<String> getMapFields(@PathVariable("key") String key) {
		return storageManager.getMapFields(key);
	}

	@RequestMapping("/{key}/all")
	public Map<String, String> getMapFieldValues(@PathVariable("key") String key) {
		return storageManager.getMapFieldValues(key);
	}
	
	@RequestMapping(value = "/{key}/{field}", method = RequestMethod.POST)
	public ResponseEntity<String> setValue(@PathVariable("key") String key, @PathVariable("field") String field, @RequestBody String value) {
		storageManager.setMapValue(key, field, value);
		return new ResponseEntity<String>(value, HttpStatus.ACCEPTED);
	}
}
