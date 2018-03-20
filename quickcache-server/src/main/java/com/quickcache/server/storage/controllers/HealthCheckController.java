package com.quickcache.server.storage.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthCheckController {

	@RequestMapping
	public ResponseEntity<String> isAlive() {
		return new ResponseEntity<String>("OK", HttpStatus.OK);
	}
}
