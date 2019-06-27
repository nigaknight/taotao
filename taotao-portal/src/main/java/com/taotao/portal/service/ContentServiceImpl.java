package com.taotao.portal.service;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.taotao.common.pojo.TaotaoResult;
import com.taotao.common.utils.HttpClientUtil;
import com.taotao.common.utils.JsonUtils;
import com.taotao.pojo.TbContent;


/*{
    "srcB": "http://image.taotao.com/images/2015/03/03/2015030304360302109345.jpg",
    "height": 240,
    "alt": "",
    "width": 670,
    "src": "http://image.taotao.com/images/2015/03/03/2015030304360302109345.jpg",
    "widthB": 550,
    "href": "http://sale.jd.com/act/e0FMkuDhJz35CNt.html?cpdad=1DLSUE",
    "heightB": 240
}*/

@Service
public class ContentServiceImpl implements ContentService{
	@Value("${REST_BASE_URL}")
	private String REST_BASE_URL;
	@Value("${REST_INDEX_AD_URL}")
	private String REST_INDEX_AD_URL;
	@Override
	public String getContentList() {
		// 调用服务层的服务
		String result=HttpClientUtil.doGet(REST_BASE_URL+REST_INDEX_AD_URL);
		// 把字符串转换为taotaoresult
		try {
			TaotaoResult taotaoResult=TaotaoResult.formatToList(result, TbContent.class);
			List<TbContent> list=(List<TbContent>) taotaoResult.getData();
			List<Map> resultList=new ArrayList<Map>();
			for(TbContent content:list) {
				Map map=new HashMap<>();
				map.put("src",content.getPic());
				map.put("height",240);
				map.put("width",670);
				map.put("heightB",240);
				map.put("widthB", 550);
				map.put("srcB", content.getPic2());
				map.put("href",content.getUrl());
				map.put("alt", content.getSubTitle());
				resultList.add(map);
			}
			return JsonUtils.objectToJson(resultList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
