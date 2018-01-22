package com.quickcache.server.storage.controllers;

import java.util.List;
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

import com.quickcache.server.constants.ErrorCodes;
import com.quickcache.server.exception.QuickCacheOperationException;
import com.quickcache.server.manager.StorageManager;

@RestController
@RequestMapping("/list")
public class ListRestController {

	@Autowired
	private StorageManager storageManager;

	public void setStorageManager(StorageManager storageManager) {
		this.storageManager = storageManager;
	}

	@RequestMapping("/{key}")
	public List<String> getListItems(@PathVariable("key") String key) {
		return storageManager.getListItems(key, true, 0 , 0);
	}

	@RequestMapping("/{key}/{offset}/{length}")
	public List<String> getListItems(@PathVariable("key") String key, @PathVariable("offset") int offset, @PathVariable("length") int length) {
		return storageManager.getListItems(key, false, offset , length);
	}

	@RequestMapping(value="/{key}", method=RequestMethod.POST)
	public ResponseEntity<String> addListItem(@PathVariable("key") String key, @RequestBody String item) {
		storageManager.addListItem(key, item);
		return new ResponseEntity<String>(item, HttpStatus.ACCEPTED);
	}

	@RequestMapping(value="/{key}/item/{item}", method=RequestMethod.DELETE)
	public ResponseEntity<String> removeListItem(@PathVariable("key") String key, @PathVariable("item") String item) {
		String removedItem = storageManager.removeListItem(key, true, item, 0);
		if (removedItem != null)
			return new ResponseEntity<String>(removedItem, HttpStatus.ACCEPTED);
		else
			return new ResponseEntity<String>(ErrorCodes.QC0003, HttpStatus.BAD_REQUEST);
	}
	
	@RequestMapping(value="/{key}/position/{index}", method=RequestMethod.DELETE)
	public ResponseEntity<String> removeListItem(@PathVariable("key") String key, @PathVariable("index") int index) {
		try {
			return new ResponseEntity<String>(storageManager.removeListItem(key, false, null, index), HttpStatus.ACCEPTED);
		} catch(QuickCacheOperationException exception) {
			return new ResponseEntity<String>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}
