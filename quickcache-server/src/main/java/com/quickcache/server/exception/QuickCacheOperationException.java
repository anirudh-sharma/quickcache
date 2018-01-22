package com.quickcache.server.exception;

import com.quickcache.server.constants.ErrorCodes;

public class QuickCacheOperationException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;
	
	int code;
	
	public int getCode(){
		return this.code;
	}
	
	public QuickCacheOperationException() {
	}
	
	public QuickCacheOperationException(String message) {
		super(message);
	}
	
	public QuickCacheOperationException(Integer code) {
		super(ErrorCodes.getMessage(code));
	}
}
