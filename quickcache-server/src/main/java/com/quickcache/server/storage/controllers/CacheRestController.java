package com.quickcache.server.storage.controllers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.quickcache.server.QuickCache;
import com.quickcache.server.manager.StorageManager;

@RestController
public class CacheRestController {

	StorageManager storageManager = QuickCache.storageManager;

	@RequestMapping("/")
	public String index() {
		return "QuickCache at your service!!";
	}

	@RequestMapping("/string/{key}")
	public String getValue(@PathVariable("key") String key) {
		return storageManager.getValue(key);
	}

	@RequestMapping(value = "/string/{key}", method = RequestMethod.POST)
	public ResponseEntity<String> setValue(@PathVariable("key") String key, @RequestBody String value) {
		storageManager.setValue(key, value);
		return new ResponseEntity<String>(value, HttpStatus.ACCEPTED);
	}

	@RequestMapping("/map/{key}/{field}")
	public String getMapValue(@PathVariable("key") String key, @PathVariable("field") String field) {
		return storageManager.getMapValue(key, field);
	}

	@RequestMapping("/map/{key}/fields")
	public Set<String> getMapFields(@PathVariable("key") String key) {
		return storageManager.getMapFields(key);
	}

	@RequestMapping("/map/{key}/all")
	public Map<String, String> getMapFieldValues(@PathVariable("key") String key) {
	//public List<String> getMapFieldValues(@PathVariable("key") String key) {
		return storageManager.getMapFieldValues(key);
	}
}
