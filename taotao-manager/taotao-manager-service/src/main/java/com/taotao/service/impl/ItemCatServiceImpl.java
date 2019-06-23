package com.taotao.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taotao.common.pojo.EUTreeNode;
import com.taotao.mapper.TbItemCatMapper;
import com.taotao.pojo.TbItemCat;
import com.taotao.pojo.TbItemCatExample;
import com.taotao.pojo.TbItemCatExample.Criteria;
import com.taotao.service.ItemCatService;

@Service
public class ItemCatServiceImpl implements ItemCatService {
	
	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Override
	public List<EUTreeNode> getItemCatList(long parentId) {
		// 创建查询条件
		TbItemCatExample example=new TbItemCatExample();
		Criteria criteria=example.createCriteria();
		criteria.andParentIdEqualTo(parentId);
		// 根据条件查询
		List<TbItemCat> list=itemCatMapper.selectByExample(example);
		List<EUTreeNode> resultList=new ArrayList<>();
		// 把列表转换为treeNodeList
		for(TbItemCat tbItemCat:list) {
			EUTreeNode node=new EUTreeNode();
			node.setId(tbItemCat.getId());
			node.setText(tbItemCat.getName());
			node.setState(tbItemCat.getIsParent()?"closed":"open");
			resultList.add(node);
		}
		return resultList;
	}

}
