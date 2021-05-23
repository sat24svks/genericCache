package com.hid.cache.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hid.cache.exception.CacheOverflowException;
import com.hid.cache.model.CacheObject;
import com.hid.cache.service.GenericCache;

/**
 * <h1>Cache Application</h1>
 * <p>
 * This is a cache application used to store objects in the memory for faster
 * retrieval. Store, Retrieve, Remove objects using key. Auto Removal of objects
 * based on Time To Live values or Global Time out settings
 * </p>
 * 
 * @author Satheesh.B
 * @version 1.0
 * @since 2021-05-23
 */
public class GenericCacheImpl<K, V> implements GenericCache<K, V> {
	private static final Logger logger = LogManager.getLogger(GenericCacheImpl.class);
	private final int sizeOfCache; // maximum size of cache
	// Global Timeout in Seconds for all entries
	private final short globalTimeOut; 
	// Clean up interval in Seconds, Run the clean up job at this interval to
	// delete the expired objects
	private final short cleanUpInterval;

	ConcurrentHashMap<K, CacheObject<V>> genericCacheMap;

	/**
	 * Constructor to create concurrent hashmap with it's configured size.
	 * Starts the cleaner thread to remove TTL and Global Timeout exceed objects
	 * for every configured interval
	 * 
	 * @param cacheBuilder
	 *            builder class object to build cache
	 */
	private GenericCacheImpl(CacheBuilder<K, V> cacheBuilder) {
		this.sizeOfCache = cacheBuilder.sizeOfCache;
		this.globalTimeOut = cacheBuilder.globalTimeOut;
		this.cleanUpInterval = cacheBuilder.cleanUpInterval;
		if (sizeOfCache <= 0) {
			logger.error("Cache Configuration - Max number of objects should be greater than 0");
			System.exit(1);
		} else {
			genericCacheMap = new ConcurrentHashMap<K, CacheObject<V>>(sizeOfCache);
			CleanerThread cleanerThread = new CleanerThread();
			cleanerThread.setDaemon(true);
			cleanerThread.start();
		}
	}

	/*
	 * This method is used to remove expired objects
	 */
	private void cleanExpiredObject() {
		Objects.requireNonNull(genericCacheMap, "Hash Map which store objects can not be null");
		genericCacheMap.entrySet()
				.removeIf(entry -> Optional.ofNullable(entry.getValue()).map(CacheObject::isExpired).orElse(false));
	}

	/**
	 * This method is used to store the object using a key in cache with
	 * expiration time. Expiration time is calculated by Global Time out
	 * settings (Current System Time in Milliseconds + Global Timeout in
	 * Milliseconds). If cache size is full, then object will not be get added
	 * and error message will be thrown. Expired objects removed first by
	 * calling cleanExpiredObject method to get some space
	 * 
	 * @param key
	 *            This is key for the stored object
	 * @param value
	 *            This is the cache object that is going to be stored in cache
	 * @return nothing.
	 */
	public void put(K key, V value) throws CacheOverflowException {
		put(key, value, getGlobalTimeOut());
	}

	public int getCacheSize() {
		return sizeOfCache;
	}

	public short getGlobalTimeOut() {
		return globalTimeOut;
	}

	public short getCleanUpInterval() {
		return cleanUpInterval;
	}

	/**
	 * This method is used to store the object using a key in cache with
	 * expiration time. Expiration time is calculated by timeToLive parameter
	 * (Current System Time in Milliseconds + Time To Live in Milliseconds). If
	 * cache size is full, then object will not be get added and error message
	 * will be thrown. Expired objects removed first by calling
	 * cleanExpiredObject to get some space.
	 * 
	 * @param key
	 *            This is key for the stored object
	 * @param value
	 *            This is the cache object that is going to be stored in cache
	 * @param timeToLiveInSeconds
	 *            This is used to set how much time the object should be
	 *            available in cache
	 * @return nothing.
	 */
	public void put(K key, V value, int timeToLiveInSeconds) throws CacheOverflowException {
		cleanExpiredObject();
		if (getCurrentCacheSize() < getCacheSize()) {
			genericCacheMap.put(key,
					new CacheObject<V>(value, System.currentTimeMillis() + (timeToLiveInSeconds * 1000)));
		} else {
			String message = "Cache is full, so not able to add the item:" + key;
			logger.error(message);
			throw new CacheOverflowException(message);
		}
	}

	/**
	 * This method is used to retrieve the stored object from cache using key,
	 * if not found returns null. Expired Objects removed first and then object
	 * will be retrieved.
	 * 
	 * @param key
	 *            this key is used to retrieve the object
	 */
	public V get(K key) {
		cleanExpiredObject();
		CacheObject<V> cacheObject = genericCacheMap.get(key);
		if (cacheObject == null)
			return null;
		else
			return cacheObject.getValue();
	}

	/**
	 * This method is used to delete the stored object in cache using key
	 * 
	 * @param key
	 *            this key is used to delete the object in cache
	 */
	public void remove(K key) {
		genericCacheMap.remove(key);
	}

	/**
	 * This method is used to get the current cache size
	 * 
	 * @return int returns the current cache size
	 */
	public int getCurrentCacheSize() {
		return genericCacheMap.size();
	}

	/*
	 * This thread class is used to call the clean method at configured
	 * CLEAN_UP_INTERVAL
	 */
	class CleanerThread extends Thread {
		boolean alwaysRun = true;

		@Override
		public void run() {
			while (alwaysRun) {
				try {
					Thread.sleep(getCleanUpInterval() * 1000);
					cleanExpiredObject();
					logger.info("Cache Clean Up Completed");
				} catch (InterruptedException e) {
					alwaysRun = false;
					logger.error("Interrupted Exception occurred in cleaner thread:", e);
				}
			}
		}
	}

	/**
	 * 
	 * Builder class to build cache object
	 *
	 */
	public static class CacheBuilder<K, V> {
		private final int sizeOfCache;
		private final short globalTimeOut;
		// Default clean up interval is 3 seconds
		private short cleanUpInterval = 3;

		public CacheBuilder(int sizeOfCache, short globalTimeOut) {
			this.sizeOfCache = sizeOfCache;
			this.globalTimeOut = globalTimeOut;
		}

		public CacheBuilder<K, V> cleanUpInterval(short cleanUpInterval) {
			this.cleanUpInterval = cleanUpInterval;
			return this;
		}

		public GenericCache<K, V> build() {
			GenericCache<K, V> genericCache = new GenericCacheImpl<K, V>(this);
			return genericCache;
		}
	}

}
