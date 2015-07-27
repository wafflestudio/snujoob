package com.wafflestudio.snujoop;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class Http {
	final static String HOME = "http://snujoob.wafflestudio.com:11663";
	
	public static String GET(String url){
		InputStream inputStream = null;
		String result = "";
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
			inputStream = httpResponse.getEntity().getContent();	
			if(inputStream != null){
		        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
		        String line = "";
		        String temp = "";
		        while((line = bufferedReader.readLine()) != null)
		            temp += line;
		        inputStream.close();
		        result = new String(temp);
			}
			else
				result = "Did not work!";

		} catch (Exception e) {
			Log.d("InputStream", e.getLocalizedMessage());
		}
		return result;
	}
	
	public static String POST(String url, String send_msg){
		StringBuilder JSONdata = new StringBuilder();
		InputStream inputStream = null;
		byte[] buffer = new byte[1024];
		String result = null;
		
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			StringEntity se = new StringEntity(send_msg);
			httpPost.setEntity(se);
			httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            HttpResponse httpResponse = httpClient.execute(httpPost);
            inputStream = httpResponse.getEntity().getContent();
            if(inputStream != null){
                try {
                    int bytesRead = 0;
                    BufferedInputStream bis = new BufferedInputStream(inputStream);
                    while ((bytesRead = bis.read(buffer) ) != -1) {
                        String line = new String(buffer, 0, bytesRead);
                        JSONdata.append(line);
                    }
                    result = JSONdata.toString();
                } catch (Exception e) {
                    Log.e("logcat", Log.getStackTraceString(e));
                } finally {
                    try {
                        inputStream.close();
                    } catch (Exception ignore) {
                    }
                }
            }
            else{
                result = "{'result' : 'fail'}";
            }
        } catch (Exception e) {
        	Log.d("InputStream", e.getLocalizedMessage());
        }
		return result;
	}
}
