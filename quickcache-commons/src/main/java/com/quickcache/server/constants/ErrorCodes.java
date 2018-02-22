package com.quickcache.server.constants;

import java.util.HashMap;
import java.util.Map;

public class ErrorCodes {

	public static final String QC0001 = "QC0001: key is unassigned";
	public static final String QC0002 = "QC0002: Key is used with different datatype";
	public static final String QC0003 = "QC0003: Item Does Not Exist in List";
	public static final String QC0004 = "QC0004: Field Does Not Exist in Map";
	public static final String QC0005 = "QC0005: Index out of bound";
	public static final String QC0006 = "QC0006: key cannot be null";
	public static final String QC0007 = "QC0007: value cannot be null";
	
	public static final Map<Integer, String> errorCodeMap;
	
	static {
		errorCodeMap = new HashMap<Integer, String>();
		errorCodeMap.put(1, QC0001);
		errorCodeMap.put(2, QC0002);
		errorCodeMap.put(3, QC0003);
		errorCodeMap.put(4, QC0004);
		errorCodeMap.put(5, QC0005);
		errorCodeMap.put(6, QC0006);
		errorCodeMap.put(7, QC0007);
	}
	
	public static String getMessage(Integer code) {
		return errorCodeMap.get(code);
	}
}
