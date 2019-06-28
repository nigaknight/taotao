package com.taotao.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.taotao.common.pojo.EUDataGridResult;
import com.taotao.common.pojo.TaotaoResult;
import com.taotao.common.utils.HttpClientUtil;
import com.taotao.mapper.TbContentMapper;
import com.taotao.pojo.TbContentExample.Criteria;
import com.taotao.pojo.TbContent;
import com.taotao.pojo.TbContentExample;
import com.taotao.service.ContentService;

@Service
public class ContentServiceImpl implements ContentService {
	@Autowired
	private TbContentMapper tbContentMapper;
	@Value("${CACHE_SYNC_BASE_URL}")
	private String CACHE_SYNC_BASE_URL;
	@Value("${CONTENT_URL}")
	private String CONTENT_URL;

	@Override
	public EUDataGridResult getContentList(Long categoryId, int page, int rows) {
		// 设置查询 条件 categoryId
		TbContentExample example = new TbContentExample();
		Criteria criteria = example.createCriteria();
		criteria.andCategoryIdEqualTo(categoryId);
		// 查询前分页
		PageHelper.startPage(page, rows);
		// 按条件查询
		List<TbContent> list = tbContentMapper.selectByExample(example);
		// 将查询条件封装到EasyUI能接受的格式
		EUDataGridResult result = new EUDataGridResult();
		result.setRows(list);
		PageInfo<TbContent> info=new PageInfo<TbContent>(list);
		result.setTotal(info.getTotal());
		return result;
	}
	
	@Override
	public TaotaoResult addContent(TbContent content) {
		content.setCreated(new Date());
		content.setUpdated(new Date());
		tbContentMapper.insert(content);
		
		// 同步缓存
		try {
			HttpClientUtil.doGet(CACHE_SYNC_BASE_URL+CONTENT_URL+content.getCategoryId());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return TaotaoResult.ok();
	}
}
