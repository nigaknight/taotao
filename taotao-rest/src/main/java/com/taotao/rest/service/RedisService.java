package com.taotao.rest.service;

public interface RedisService {
	public String get(String key);
	public String set(String key1, String value);
	public long hset(String hkey, String key, String value);
	public String hget(String hkey, String key);
	public long incr(String key);
	public long expire(String key, int seconds);
	public long ttl(String key);
	long hdel(String hkey, String key);

}
