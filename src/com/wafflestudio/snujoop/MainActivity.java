package com.wafflestudio.snujoop;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {
	
	final static int RESULT_LOGIN = 1;
	final static int RESULT_DETAILSUBJECT = 2;
	final static int RESULT_FINDSUBJECT = 3;

	User user = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		user = new User();
		
		user = new User((Integer)1, new ArrayList<Integer>(), null);
		
		findViewById(R.id.atferLogin).setVisibility(View.GONE);

		((Button)findViewById(R.id.loginButton)).setOnClickListener(loginButtonClickEvent);
		((Button)findViewById(R.id.registerButton)).setOnClickListener(registerButtonClickEvent);
		((Button)findViewById(R.id.findButton)).setOnClickListener(findSubjectButtonClickEvent);
		((Button)findViewById(R.id.unregisterButton)).setOnClickListener(unregisterButtonClickEvent);
	}
	
	Button.OnClickListener loginButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this, LoginActivity.class);
			startActivityForResult(intent, RESULT_LOGIN);
		}
	};
	
	Button.OnClickListener registerButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
			startActivity(intent);
		}
	};
	
	Button.OnClickListener findSubjectButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this, FindSubjectActivity.class);
			intent.putExtra("userId", user.getId());
			intent.putExtra("subjectIdList", user.getSubjectIdList());
			intent.putExtra("userToken", user.getToken());
			
			startActivityForResult(intent, RESULT_FINDSUBJECT);
		}
	};
	
	Button.OnClickListener unregisterButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			Unregister();
		}
	};

	private AdapterView.OnItemClickListener subjectItemClickListener = new AdapterView.OnItemClickListener() {
        @SuppressWarnings("unchecked")
		@Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long l_position) {
            HashMap<String, String> hashmap = (HashMap<String, String>) parent.getAdapter().getItem(position);
            
			Intent intent = new Intent(MainActivity.this, DetailSubjectActivity.class);
			intent.putExtra("userId", user.getId());
			intent.putExtra("subjectIdList", user.getSubjectIdList());
			intent.putExtra("userToken", user.getToken());
			
			intent.putExtra("subjectId", Integer.parseInt(hashmap.get("id")));
			intent.putExtra("subjectName", hashmap.get("subject_name"));
			intent.putExtra("subjectNumber", hashmap.get("subject_number").split(" ")[0]);
			intent.putExtra("lectureNumber", hashmap.get("subject_number").split(" ")[1]);
			intent.putExtra("lecturer", hashmap.get("lecturer"));
			intent.putExtra("capacity", Integer.parseInt(hashmap.get("capacity")));
			intent.putExtra("capacityEnrolled", Integer.parseInt(hashmap.get("capacity_enrolled")));
			intent.putExtra("enrolled", Integer.parseInt(hashmap.get("enrolled")));

			startActivityForResult(intent, MainActivity.RESULT_DETAILSUBJECT);
        }
    };
	
	protected void onActivityResult(int requestCode, int resultCode, Intent Data){
		switch(requestCode){
		case RESULT_LOGIN:
			user = new User(
						Data.getIntExtra("userId", -1),
						Data.getIntegerArrayListExtra("subjectIdList"),
						Data.getStringExtra("userToken")
					);
			if (user.getId() == -1){
				user = null;
				Toast.makeText(MainActivity.this, "fail to login or cancel", Toast.LENGTH_SHORT).show();
				return;
			}
			new LoadUserInformation().execute("http://dev.wafflestudio.net:10101/users/"
						+ user.getId().toString() + ".json?token=" + user.getToken());
			break;
		case RESULT_DETAILSUBJECT:
			user = new User(
						Data.getIntExtra("userId", -1),
						Data.getIntegerArrayListExtra("subjectIdList"),
						Data.getStringExtra("userToken")
					);
			if (user.getId() == -1){
				user = null;
				Toast.makeText(MainActivity.this, "fail to login", Toast.LENGTH_SHORT).show();
				return;
			}
			new LoadUserInformation().execute("http://dev.wafflestudio.net:10101/users/"
					+ user.getId().toString() + ".json?token=" + user.getToken());
			break;
		case RESULT_FINDSUBJECT:
			user = new User(
					Data.getIntExtra("userId", -1),
					Data.getIntegerArrayListExtra("subjectIdList"),
					Data.getStringExtra("userToken")
				);
			if (user.getId() == -1){
				user = null;
				Toast.makeText(MainActivity.this, "fail to login", Toast.LENGTH_SHORT).show();
				return;
			}
			new LoadUserInformation().execute("http://dev.wafflestudio.net:10101/users/"
					+ user.getId().toString() + ".json?token=" + user.getToken());
			break;
		}
	}
	
    protected void Unregister() {
		if (GCMRegistrar.isRegistered(this)) {
			GCMRegistrar.unregister(this);
			Toast.makeText(this, "기기가 해지되었습니다.\n로그인 시 다시 등록됩니다.", Toast.LENGTH_LONG).show();
		}
	}

	private class LoadUserInformation extends AsyncTask<String, Boolean, String> {
        @Override
        protected String doInBackground(String... urls) {
        		
            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        
        @Override
        protected void onPostExecute(String result) {
        	super.onPostExecute(result);
            
        	if(result != null){
        		Log.d("ASYNC", "result = " + result);
        		user.getSubjectIdList().clear();
        		JSONObject jsonResult = null;
        		JSONArray jsonSubjectList = null;
        		SimpleAdapter adapter = null;
        		try {
        			List<HashMap<String, String>> subjectList = new ArrayList<HashMap<String, String>>();
					jsonResult = new JSONObject(result);
					jsonSubjectList = jsonResult.getJSONArray("subjects");
					for (int i = 0 ; i < jsonSubjectList.length() ; i++){
						JSONObject jsonSubject = jsonSubjectList.getJSONObject(i);

	        			HashMap<String, String> hashmap = new HashMap<String, String>();
	        			
						Integer id = jsonSubject.getInt("id");
						String subjectName = jsonSubject.getString("subject_name");
						String subjectNumber = jsonSubject.getString("subject_number") + " " + jsonSubject.getString("lecture_number");
						String lecturer = jsonSubject.getString("lecturer");
						Integer capacity = jsonSubject.getInt("capacity");
						Integer capacityEnrolled = jsonSubject.getInt("capacity_enrolled");
						Integer enrolled = jsonSubject.getInt("enrolled");
						hashmap.put("id", id.toString());
						hashmap.put("subject_name", subjectName);
						hashmap.put("subject_number", subjectNumber);
						hashmap.put("lecturer", lecturer);
						hashmap.put("capacity", capacity.toString());
						hashmap.put("capacity_enrolled", capacityEnrolled.toString());
						hashmap.put("enrolled", enrolled.toString());
						subjectList.add(hashmap);
						
						user.appendMySubjectIdList(jsonSubject.getInt("id"));
					}
					
					String[] from = {  "subject_name", "subject_number", "lecturer" };
					int[] to = { R.id.subjectName, R.id.subjectNumber, R.id.lecturer };
					adapter = new SimpleAdapter(getBaseContext(), subjectList, R.layout.subject_listview_content, from, to);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        		ListView listView = (ListView)findViewById(R.id.subjectListView);
        		listView.setAdapter(adapter);
        		listView.setOnItemClickListener(subjectItemClickListener);

        		findViewById(R.id.loginButton).setVisibility(View.GONE);
        		findViewById(R.id.registerButton).setVisibility(View.GONE);
        		findViewById(R.id.unregisterButton).setVisibility(View.GONE);
        		findViewById(R.id.linlaHeaderProgress).setVisibility(View.GONE);
        		findViewById(R.id.developer).setVisibility(View.GONE);
        		findViewById(R.id.atferLogin).setVisibility(View.VISIBLE);
        	}
    		else {
				Toast.makeText(MainActivity.this, "please connect to Internet or the server is down...", Toast.LENGTH_SHORT).show();
    		}
        }
    }
	
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

class Subject {
	private Integer id;
	private String subjectName;
	private String subjectNumber;
	private String lectureNumber;
	private String lecturer;
	
	Subject(){
		id = 0;
		subjectName = null;
		subjectNumber = null;
		lectureNumber = null;
		lecturer = null;
	}
	Subject(Integer id, String subjectName, String subjectNumber, String lectureNumber, String lecturer){
		this.id = id;
		this.subjectName = subjectName;
		this.subjectNumber = subjectNumber;
		this.lectureNumber = lectureNumber;
		this.lecturer = lecturer;
	}
	void setId(int id){
		this.id = id;
	}
	void setName(String name){
		this.subjectName = name;
	}
	Integer getId(){
		return this.id;
	}
	String getSubjectName(){
		return this.subjectName;
	}
	String getSubjectNumber(){
		return this.subjectNumber;
	}
	String getLectureNumber(){
		return this.lectureNumber;
	}
	String getLecturer(){
		return this.lecturer;
	}
}

class User {
	private Integer id;
	private ArrayList<Integer> mySubjectIdList = null;
	private String token;
	
	User(){
		id = null;
		mySubjectIdList = new ArrayList<Integer>();
		token = null;
	}
	User(Integer id, ArrayList<Integer> subjectIdList, String token){
		this.id = id;
		mySubjectIdList = subjectIdList;
		this.token = token;
	}
	Integer getId(){
		return this.id;
	}
	ArrayList<Integer> getSubjectIdList(){
		return this.mySubjectIdList;
	}
	String getToken(){
		return this.token;
	}
	void setId(Integer id){
		this.id = id;
	}
	void appendMySubjectIdList(Integer id){
		mySubjectIdList.add(id);
	}
	void deleteMySubjectIdList(Integer id){
		mySubjectIdList.remove(id);
	}
	Boolean isMySubjectIdList(Integer id){
		return mySubjectIdList.contains(id);
	}
	static Boolean isStudentNumber(String studentNumber){
		return Pattern.matches("20[0-9]{2}-[0-9]{5}", studentNumber);
	}
	void setToken(String token){
		this.token = token;
	}
}