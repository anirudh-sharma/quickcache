package com.quickcache.server.storage.types;

public class StorageKey {

	private String key;
	
	/** Cache the hash code for the key */
    private long hash; // Default to 0

    public long keyHash() {
    	long h = hash;
    	char [] value = key.toCharArray();
    	
        if (h == 0 && value.length > 0) {
            char val[] = key.toCharArray();;

            for (int i = 0; i < value.length; i++) {
                h = 31 * h + val[i];
            }
            hash = h;
        }
        return h;
    }
}
