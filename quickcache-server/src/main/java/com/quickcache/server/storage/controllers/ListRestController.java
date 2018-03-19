package com.quickcache.server.storage.controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

	@RequestMapping("/{key}/{offset}")
	public List<String> getListItemsWithOffset(@PathVariable("key") String key, @PathVariable("offset") int offset) {
		return storageManager.getListItems(key, false, offset, -1);
	}

	@RequestMapping("/{key}/{start}/{end}")
	public List<String> getListItemsWithStartEnd(@PathVariable("key") String key, @PathVariable("start") int offset, @PathVariable("end") int length) {
		return storageManager.getListItems(key, false, offset , length);
	}

	@RequestMapping(value="/{key}", method=RequestMethod.POST)
	public ResponseEntity<String> addListItem(@PathVariable("key") String key, @RequestBody String item) {
		try {
			item = URLDecoder.decode(item, "utf-8");
			if(item.endsWith("=")){
				item = item.substring(0, (item.length() - 1));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		storageManager.addListItem(key, item, false);
		return new ResponseEntity<String>(item, HttpStatus.ACCEPTED);
	}

	@RequestMapping(value="/{key}/{index}", method=RequestMethod.DELETE)
	public ResponseEntity<String> removeListItem(@PathVariable("key") String key, @PathVariable("index") int index) {
		try {
			return new ResponseEntity<String>(storageManager.removeListItem(key, index), HttpStatus.ACCEPTED);
		} catch(QuickCacheOperationException exception) {
			return new ResponseEntity<String>(exception.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
}
