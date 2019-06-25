package com.taotao.service;

import com.taotao.common.pojo.EUDataGridResult;

public interface ContentService {

	EUDataGridResult getContentList(Long categoryId, int page, int rows);

}
