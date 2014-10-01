package com.prowritingaid.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
 
public class ProWritingAidApiClient {

	private final String USER_AGENT = "Mozilla/5.0";
	private final String _urlBase = "http://localhost:2028/";
	//private final String _urlBase = "https://prowritingaid.com/";
	private final String _licenseCode;
	
    public ProWritingAidApiClient(String licenseCode) {
    	_licenseCode = licenseCode;
	}

    public TagAnalysisResponse analyze(String text, String reports, WritingStyle writingStyle) throws ClientProtocolException, IOException{
		TagAnalysisRequest request = new TagAnalysisRequest();
		request.licenceCode = _licenseCode;
		request.reports=reports;
		request.style=writingStyle.getValue();
		request.text=text;
    	TagAnalysisResponse result = this.<TagAnalysisRequest,TagAnalysisResponse>sendJsonPost("en/apiv1/analyze", request, TagAnalysisResponse.class);
    	return result;
    }
    
	private String sendPost(String callUrl, List<NameValuePair> parameters) throws ClientProtocolException, IOException {
		String url = _urlBase + "callUrl";
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
 
		// add header
		post.setHeader("User-Agent", USER_AGENT);
 
		post.setEntity(new UrlEncodedFormEntity(parameters));
 
		HttpResponse response = client.execute(post);
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + post.getEntity());
		System.out.println("Response Code : " + 
                                    response.getStatusLine().getStatusCode());
		BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		} 
		System.out.println(result.toString());
		return result.toString();
 	}
	
	private  <TRequest, TResponse> TResponse sendJsonPost(String callUrl, TRequest request,  java.lang.reflect.Type responseType) throws ClientProtocolException, IOException {
		Gson gson = new Gson();
		String url = _urlBase + callUrl;
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
 
		// add header
		post.setHeader("User-Agent", USER_AGENT);
		post.setHeader("Content-Type", "application/json");
		String body = gson.toJson(request);
		HttpEntity entity = new ByteArrayEntity(body.getBytes("UTF-8"));
        post.setEntity(entity);
        
		HttpResponse response = client.execute(post);
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + post.getEntity());
		System.out.println("Response Code : " + 
                                    response.getStatusLine().getStatusCode());
		BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		} 
		System.out.println(result.toString());
		//new TypeToken<TResponse>(){}.getType()
		return gson.fromJson(result.toString(), responseType);
 	}
}