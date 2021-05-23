package com.hid.cache.service;

import com.hid.cache.exception.CacheOverflowException;

public interface GenericCache<K, V> {
	public void put(K key, V value) throws CacheOverflowException;

	public void put(K key, V value, int timeToLiveInSeconds) throws CacheOverflowException;

	public V get(K key);

	public void remove(K key);

}
