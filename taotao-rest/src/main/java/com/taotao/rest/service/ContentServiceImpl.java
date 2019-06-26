package com.taotao.rest.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taotao.mapper.TbContentMapper;
import com.taotao.pojo.TbContent;
import com.taotao.pojo.TbContentExample.Criteria;
import com.taotao.pojo.TbContentExample;

@Service
public class ContentServiceImpl implements ContentService{
	@Autowired
	TbContentMapper tbContentMapper;
	
	@Override
	public List<TbContent> getContent(Long categoryId){
		TbContentExample example=new TbContentExample();
		Criteria criteria=example.createCriteria();
		criteria.andCategoryIdEqualTo(categoryId);
		List<TbContent> list=tbContentMapper.selectByExample(example);
		return list;
	}
}
