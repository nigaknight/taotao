package com.taotao.portal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexController {

	@RequestMapping("/index")
	public String showIndex() throws Exception {
		return "index";
	}
	
	@RequestMapping(value = "/httpclient/post", method =RequestMethod.POST)
	@ResponseBody
	public String testPost() {
		return "ok";
	}
	
	@RequestMapping(value = "/httpclient/post2", method =RequestMethod.POST)
	@ResponseBody
	public String testPost2(String username, String password) {
		return "username:"+username+"\t"+"password:"+password;
	}
	
}

