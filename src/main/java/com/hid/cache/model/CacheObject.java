package com.hid.cache.model;

public class CacheObject<V> {

	private long expirationTime; // In Milliseconds
	private V value;

	public CacheObject(V value, long expirationTime) {
		this.value = value;
		this.expirationTime = expirationTime;
	}

	public V getValue() {
		return value;
	}

	/**
	 * This method is used to check whether object in cache is expired or not.
	 * 
	 * @return boolean true or false based on whether object expired or not
	 */
	public boolean isExpired() {
		return System.currentTimeMillis() > expirationTime;
	}

}
