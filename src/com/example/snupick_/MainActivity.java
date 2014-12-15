package com.example.snupick_;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	final static int RESULT_LOGIN = 1;
	final static int RESULT_DETAILSUBJECT = 2;
	final static int RESULT_FINDSUBJECT = 3;

	User user = null;
	ArrayList<Subject> subjectList = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		user = new User();
		subjectList = new ArrayList<Subject>();
		
		user = new User((Integer)1, new ArrayList<Integer>());
		
		Intent intent = new Intent(MainActivity.this, FindSubjectActivity.class);
		intent.putExtra("userId", user.getId());
		intent.putExtra("subjectIdList", user.getSubjectIdList());
		
		findViewById(R.id.atferLogin).setVisibility(View.GONE);

		((Button)findViewById(R.id.loginButton)).setOnClickListener(loginButtonClickEvent);
		((Button)findViewById(R.id.registerButton)).setOnClickListener(registerButtonClickEvent);
		((Button)findViewById(R.id.findButton)).setOnClickListener(findSubjectButtonClickEvent);
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
			
			startActivityForResult(intent, RESULT_FINDSUBJECT);
		}
	};
	
	Button.OnClickListener subjectClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			for (int i = 0 ; i < subjectList.size() ; i++){
				Subject subject = subjectList.get(i);
				if (subject.getId() == v.getId()){
					Intent intent = new Intent(MainActivity.this, DetailSubjectActivity.class);
					intent.putExtra("userId", user.getId());
					intent.putExtra("subjectIdList", user.getSubjectIdList());
					intent.putExtra("subjectId", subject.getId());
					intent.putExtra("subjectName", subject.getSubjectName());
					intent.putExtra("subjectNumber", subject.getSubjectNumber());
					intent.putExtra("lectureNumber", subject.getLectureNumber());
					intent.putExtra("professorName", subject.getProfessorName());
					Log.d("LOG", "start!");
					startActivityForResult(intent, MainActivity.RESULT_DETAILSUBJECT);
					return;
				}
			}
		}
	};
	
	protected void onActivityResult(int requestCode, int resultCode, Intent Data){
		switch(requestCode){
		case RESULT_LOGIN:
			user = new User(
						Data.getIntExtra("userId", -1),
						Data.getIntegerArrayListExtra("subjectIdList")
					);
			if (user.getId() == -1){
				user = null;
				Toast.makeText(MainActivity.this, "fail to login", Toast.LENGTH_SHORT).show();
				return;
			}
			new LoadUserInformation().execute("http://dev.wafflestudio.net:10101/users/" + user.getId().toString() + ".json");
			break;
		case RESULT_DETAILSUBJECT:
			user = new User(
						Data.getIntExtra("userId", -1),
						Data.getIntegerArrayListExtra("subjectIdList")
					);
			if (user.getId() == -1){
				user = null;
				Toast.makeText(MainActivity.this, "fail to login", Toast.LENGTH_SHORT).show();
				return;
			}
			new LoadUserInformation().execute("http://dev.wafflestudio.net:10101/users/" + user.getId().toString() + ".json");
			break;
		case RESULT_FINDSUBJECT:
			user = new User(
					Data.getIntExtra("userId", -1),
					Data.getIntegerArrayListExtra("subjectIdList")
				);
			if (user.getId() == -1){
				user = null;
				Toast.makeText(MainActivity.this, "fail to login", Toast.LENGTH_SHORT).show();
				return;
			}
			new LoadUserInformation().execute("http://dev.wafflestudio.net:10101/users/" + user.getId().toString() + ".json");
			break;
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
				LinearLayout subjectListLayout = ((LinearLayout)findViewById(R.id.subjectList));
				subjectListLayout.removeAllViews();
        		JSONObject jsonResult = null;
        		JSONArray jsonSubjectList = null;
        		try {
					jsonResult = new JSONObject(result);
					jsonSubjectList = jsonResult.getJSONArray("subjects");
					for (int i = 0 ; i < jsonSubjectList.length() ; i++){
						JSONObject jsonSubject = jsonSubjectList.getJSONObject(i);
						Subject subject = new Subject(
								jsonSubject.getInt("id"),
								jsonSubject.getString("subject_name"),
								jsonSubject.getString("subject_number"),
								jsonSubject.getString("lecture_number"),
								jsonSubject.getString("professor_name")
							);
						subjectList.add(subject);
						user.appendMySubjectIdList(subject.getId());
						TextView textView = new TextView(MainActivity.this);
						textView.setId(subject.getId());
						textView.setText(subject.getSubjectName());
						subjectListLayout.addView(textView);
						textView.setOnClickListener(subjectClickEvent);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

        		findViewById(R.id.loginButton).setVisibility(View.GONE);
        		findViewById(R.id.registerButton).setVisibility(View.GONE);        		
        		findViewById(R.id.atferLogin).setVisibility(View.VISIBLE);
        	}
    		else {
				Toast.makeText(MainActivity.this, "please connect to Internet or the server is down...", Toast.LENGTH_SHORT).show();
    		}
        }
    }
	
	public String GET(String url){
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
	
	public String POST(String url, String send_msg){
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
    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected())
                return true;
            else
                return false;
    }
}

class Subject {
	private Integer id;
	private String subjectName;
	private String subjectNumber;
	private String lectureNumber;
	private String professorName;
	Subject(){
		id = 0;
		subjectName = null;
		subjectNumber = null;
		lectureNumber = null;
		professorName = null;
	}
	Subject(Integer id, String subjectName, String subjectNumber, String lectureNumber, String professorName){
		this.id = id;
		this.subjectName = subjectName;
		this.subjectNumber = subjectNumber;
		this.lectureNumber = lectureNumber;
		this.professorName = professorName;
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
	String getProfessorName(){
		return this.professorName;
	}
}

class User {
	private Integer id;
	private ArrayList<Integer> mySubjectIdList = null;
	
	User(){
		id = null;
		mySubjectIdList = new ArrayList<Integer>(); 
	}
	User(Integer id, ArrayList<Integer> subjectIdList){
		this.id = id;
		mySubjectIdList = subjectIdList;
	}
	Integer getId(){
		return this.id;
	}
	ArrayList<Integer> getSubjectIdList(){
		return this.mySubjectIdList;
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
}