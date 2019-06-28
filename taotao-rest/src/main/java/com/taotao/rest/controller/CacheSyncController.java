package com.taotao.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.taotao.common.pojo.TaotaoResult;
import com.taotao.rest.service.CacheSyncService;

@Controller
@RequestMapping("/cache/sync")
public class CacheSyncController {
	@Autowired
	CacheSyncService cacheSyncService;
	@RequestMapping("/content/{categoryId}")
	public TaotaoResult syncContent(@PathVariable long categoryId) {
		TaotaoResult result=cacheSyncService.syncContent(categoryId);
		return result;
	}
}
