package com.taotao.sso.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.taotao.common.pojo.TaotaoResult;
import com.taotao.mapper.TbUserMapper;
import com.taotao.pojo.TbUser;
import com.taotao.pojo.TbUserExample;
import com.taotao.pojo.TbUserExample.Criteria;

@Service
public class UserServiceImpl implements UserService{
	
	@Autowired
	TbUserMapper userMapper;
	
	@Override
	public TaotaoResult checkData(String content, Integer type) {
		TbUserExample example=new TbUserExample();
		Criteria criteria=example.createCriteria();
		if(type==1) {
			criteria.andUsernameEqualTo(content);
		}
		else if(type==2) {
			criteria.andPhoneEqualTo(content);
		}
		else {
			criteria.andEmailEqualTo(content);
		}
		List<TbUser> list=userMapper.selectByExample(example);
		if(list==null||list.size()==0) {
			return TaotaoResult.ok(true);
		}
		return TaotaoResult.ok(false);
	}
	
	@Override
	public TaotaoResult createUser(TbUser user) {
		user.setUpdated(new Date());
		user.setCreated(new Date());
		//md5加密
		user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
		userMapper.insert(user);
		return TaotaoResult.ok();
	}

}
