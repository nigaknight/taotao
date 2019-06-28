package com.taotao.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.taotao.common.pojo.TaotaoResult;
import com.taotao.common.utils.ExceptionUtil;

@Service
public class CacheSyncServiceImpl implements CacheSyncService{
	@Autowired
	private RedisService redisService;
	@Value(value = "${CONTENT_REDIS}")
	private String CONTENT_REDIS;
	@Override
	public TaotaoResult syncContent(long categoryId) {
		try {
			redisService.hdel(CONTENT_REDIS, categoryId+"");
		} catch (Exception e) {
			e.printStackTrace();
			return TaotaoResult.build(500, ExceptionUtil.getStackTrace(e));
		}
		return TaotaoResult.ok();
	}
}
