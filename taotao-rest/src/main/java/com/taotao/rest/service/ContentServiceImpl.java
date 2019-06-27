package com.taotao.rest.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import com.taotao.common.utils.JsonUtils;
import com.taotao.mapper.TbContentMapper;
import com.taotao.pojo.TbContent;
import com.taotao.pojo.TbContentExample;
import com.taotao.pojo.TbContentExample.Criteria;

@Service
public class ContentServiceImpl implements ContentService{
	@Autowired
	TbContentMapper tbContentMapper;
	@Autowired
	RedisService redisService;
	@Value(value = "${CONTENT_REDIS}")
	private String CONTENT_REDIS;
	@Override
	public List<TbContent> getContent(Long categoryId){
		// 从redis中取广告
		try {
			String result=redisService.hget(CONTENT_REDIS, categoryId+"");
			if(!StringUtils.isBlank(result)) {
				List<TbContent> list=JsonUtils.jsonToList(result, TbContent.class);
				return list;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 根据大广告分类id从mysql中取广告
		TbContentExample example=new TbContentExample();
		Criteria criteria=example.createCriteria();
		criteria.andCategoryIdEqualTo(categoryId);
		List<TbContent> list=tbContentMapper.selectByExample(example);

		
		// 向redis中添加内容
		try {
			String json = JsonUtils.objectToJson(list);
			redisService.hset(CONTENT_REDIS, categoryId+"", json);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return list;
	}
}
