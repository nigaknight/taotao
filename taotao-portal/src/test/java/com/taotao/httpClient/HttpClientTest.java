package com.taotao.httpClient;


import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class HttpClientTest {

	public void doGet() throws ClientProtocolException, IOException {
		// 创建一个httpClient对象
		CloseableHttpClient client=HttpClients.createDefault();
		// 创建一个Get对象
		HttpGet get=new HttpGet("http://www.baidu.com");
		// 执行请求
		CloseableHttpResponse response= client.execute(get);
		// 取响应的结果
		int statusCode=response.getStatusLine().getStatusCode();
		System.out.println(statusCode);
		HttpEntity httpEntity=response.getEntity();
		String s=EntityUtils.toString(httpEntity, "utf-8");
		System.out.println(s);
		response.close();
		client.close();
		
	}
	

	public void doGetWithParam() throws ClientProtocolException, IOException, URISyntaxException {
		// 创建一个httpClient对象
		CloseableHttpClient client=HttpClients.createDefault();
		// 创建一个uri对象
		URIBuilder builder=new URIBuilder("http://www.baidu.com/baidu");
		builder.addParameter("wd", "许嵩");
		// 创建一个Get对象
		HttpGet get=new HttpGet(builder.build());
		// 执行请求
		CloseableHttpResponse response= client.execute(get);
		// 取响应的结果
		int statusCode=response.getStatusLine().getStatusCode();
		System.out.println(statusCode);
		HttpEntity httpEntity=response.getEntity();
		String s=EntityUtils.toString(httpEntity, "utf-8");
		System.out.println(s);
		response.close();
		client.close();
	}
	

	public void doPost() throws Exception {
		CloseableHttpClient httpClient = HttpClients.createDefault();
	
		//创建一个post对象
		HttpPost post = new HttpPost("http://localhost:8082/httpclient/post.html");
		//执行post请求
		CloseableHttpResponse response = httpClient.execute(post);
		String string = EntityUtils.toString(response.getEntity());
		System.out.println(string);
		response.close();
		httpClient.close();
		
	}
	

	@Test
	public void doPostWithParam() throws Exception{
		CloseableHttpClient httpClient = HttpClients.createDefault();
		
		//创建一个post对象
		HttpPost post = new HttpPost("http://localhost:8082/httpclient/post2.html");
		//创建一个Entity。模拟一个表单
		List<NameValuePair> kvList = new ArrayList<>();
		kvList.add(new BasicNameValuePair("username", "zhangsan"));
		kvList.add(new BasicNameValuePair("password", "123"));
		
		//包装成一个Entity对象
		StringEntity entity = new UrlEncodedFormEntity(kvList, "utf-8");
		//设置请求的内容
		post.setEntity(entity);
		
		//执行post请求
		CloseableHttpResponse response = httpClient.execute(post);
		String string = EntityUtils.toString(response.getEntity());
		System.out.println(string);
		response.close();
		httpClient.close();
	}


}
