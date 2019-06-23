package com.taotao.rest.jedis;

import java.util.HashSet;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

public class TestJedisSingle {
	public void testJedisSingle() {
		Jedis jedis=new Jedis("192.168.222.128", 6379);
		jedis.set("name","jack");
		String name=jedis.get("name");
		System.out.println(name);
		jedis.close();
	}

	public void pool() {
		JedisPool jedisPool=new JedisPool("192.168.222.128", 6379);
		Jedis jedis=jedisPool.getResource();
		jedis.set("student","mary");
		String name=jedis.get("student");
		System.out.println(name);
		jedis.close();
		jedisPool.close();
	}
	public void testJedisCluster() {
		HashSet<HostAndPort> set=new HashSet<HostAndPort>();
		set.add(new HostAndPort("192.168.222.128", 7001));
		set.add(new HostAndPort("192.168.222.128", 7002));
		set.add(new HostAndPort("192.168.222.128", 7003));
		set.add(new HostAndPort("192.168.222.128", 7004));
		set.add(new HostAndPort("192.168.222.128", 7005));
		set.add(new HostAndPort("192.168.222.128", 7006));
		JedisCluster cluster=new JedisCluster(set);
		cluster.set("key1","2");
		String key1=cluster.get("key1");
		System.out.println(key1);
		cluster.close();
	}
	@Test
	public void testSpringSingle() {
		ApplicationContext applicationContext=new ClassPathXmlApplicationContext("classpath:spring/applicationContext-jedis.xml");
		JedisPool jedisPool=(JedisPool) applicationContext.getBean("redisClient");
		Jedis jedis=jedisPool.getResource();
		jedis.set("spring","bingo");
		System.out.println(jedis.get("student"));
		jedis.close();
		jedisPool.close();
	}
}
