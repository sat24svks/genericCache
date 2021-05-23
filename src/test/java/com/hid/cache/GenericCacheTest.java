package com.hid.cache;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

import com.hid.cache.exception.CacheOverflowException;
import com.hid.cache.impl.GenericCacheImpl;
import com.hid.cache.service.GenericCache;

public class GenericCacheTest {

	private GenericCache<String, String> genericCache;

	@Before
	public void setup() {
		// Generic Cache created with size 5, Global Timeout is 2 seconds, Cache
		// Cleanup interval is 1 second
		genericCache = new GenericCacheImpl.CacheBuilder<String, String>(5, (short) 2).cleanUpInterval((short) 1).build();
	}

	@Test
	public void testPutWithTTL() throws CacheOverflowException, InterruptedException {
		// Time to live is 1 second
		genericCache.put("key1", "val1", 1);
		assertEquals("val1", (genericCache.get("key1")));
		Thread.sleep(1050);
		assertEquals(null, (genericCache.get("key1")));
	}

	@Test
	public void testPutWithoutTTL() throws CacheOverflowException, InterruptedException {
		// Global Timeout is 2 seconds
		genericCache.put("k1", "v1");
		assertEquals("v1", (genericCache.get("k1")));
		Thread.sleep(1000);
		assertEquals("v1", (genericCache.get("k1")));
		Thread.sleep(1000);
		assertEquals(null, genericCache.get("k1"));
	}

	@Test
	public void testGetObjectNotInCache() throws CacheOverflowException {
		// K2 is not in cache
		genericCache.put("k1", "v1");
		assertEquals(null, (genericCache.get("k2")));
	}

	@Test
	public void testCacheOverflow() {
		// Cache size is 5, trying to add 6th item
		IntStream.range(1, 7).forEach(i -> {
			try {
				genericCache.put("key" + i, "value" + i);
			} catch (CacheOverflowException e) {
				assertThat(e.getMessage(), is("Cache is full, so not able to add the item:key6"));
			}
		});
	}

	@Test
	public void testByteArrayInCache() throws CacheOverflowException {
		//Generic Cache to add byte[]
		GenericCache<String, byte[]> byteCache = new GenericCacheImpl.CacheBuilder<String, byte[]>(3, (short) 2)
				.cleanUpInterval((short) 1).build();
		byteCache.put("b1", "bus".getBytes());
		String retrievedObject = new String(byteCache.get("b1"));
		assertEquals("bus", retrievedObject);
	}

	@Test
	public void testCacheRemove() throws CacheOverflowException {
		genericCache.put("k1", "v1");
		assertEquals("v1", (genericCache.get("k1")));
		genericCache.remove("k1");
		assertEquals(null, (genericCache.get("k1")));
	}
}
