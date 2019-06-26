package com.taotao.httpClient;


import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
	
	@Test
	public void doGetWithParam() throws ClientProtocolException, IOException, URISyntaxException {
		// 创建一个httpClient对象
		CloseableHttpClient client=HttpClients.createDefault();
		// 创建一个uri对象
		URIBuilder builder=new URIBuilder("http://www.sogou.com/web");
		builder.addParameter("query", "许嵩");
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
}
