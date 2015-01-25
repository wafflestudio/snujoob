package com.wafflestudio.snujoop;

import com.wafflestudio.snujoop.R;

import java.io.BufferedInputStream;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DetailSubjectActivity extends Activity {

	Intent mainActivity = null;
	Intent findSubjectActivity = null;
	Subject subject = null;
	User user;
	
	@Override
	public void onBackPressed(){
		//Log.d("LOG", "is finish?");
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_subject);
		
		Intent intent = getIntent();
		mainActivity = new Intent(DetailSubjectActivity.this, MainActivity.class);
		findSubjectActivity = new Intent(DetailSubjectActivity.this, FindSubjectActivity.class);

		user = new User(
					intent.getIntExtra("userId", -1),
					intent.getIntegerArrayListExtra("subjectIdList"),
					intent.getStringExtra("userToken")
				);
		subject = new Subject(
					intent.getIntExtra("subjectId", -1),
					intent.getStringExtra("subjectName"),
					intent.getStringExtra("subjectNumber"),
					intent.getStringExtra("lectureNumber"),
					intent.getStringExtra("lecturer")
				);
		Integer capacity = intent.getIntExtra("capacity", 0);
		Integer enrolled = intent.getIntExtra("enrolled", 0);
		if (subject.getId() == -1 || user.getId() == -1){
			Toast.makeText(DetailSubjectActivity.this, "잘목된 접근", Toast.LENGTH_SHORT).show();
			return;
		}
		mainActivity.putExtra("userId", user.getId());
		mainActivity.putExtra("subjectIdList", user.getSubjectIdList());
		mainActivity.putExtra("userToken", user.getToken());
		setResult(RESULT_OK, mainActivity);
		findSubjectActivity.putExtra("userId", user.getId());
		findSubjectActivity.putExtra("subjectIdList", user.getSubjectIdList());
		findSubjectActivity.putExtra("userToken", user.getToken());
		setResult(RESULT_OK, findSubjectActivity);
		
		((TextView)findViewById(R.id.subjectName)).setText(subject.getSubjectName());
		((TextView)findViewById(R.id.subjectNumber)).setText("과목 번호: " 
				+ subject.getSubjectNumber() + "  " + subject.getLectureNumber());
		((TextView)findViewById(R.id.lecturer)).setText("" + subject.getLecturer());
		((TextView)findViewById(R.id.capacity)).setText("정원: " + capacity);
		((TextView)findViewById(R.id.enrolled)).setText("등록: " + enrolled);
		if (enrolled >= capacity)
			((TextView)findViewById(R.id.enrolled)).setTextColor(Color.parseColor("#FF0000"));

		if (user.isMySubjectIdList(subject.getId())){
			findViewById(R.id.registerButton).setVisibility(View.GONE);
			findViewById(R.id.unregisterButton).setVisibility(View.VISIBLE);
		}
		else{
			findViewById(R.id.unregisterButton).setVisibility(View.GONE);
			findViewById(R.id.registerButton).setVisibility(View.VISIBLE);
		}
		
		((Button)findViewById(R.id.registerButton)).setOnClickListener(registerButtonClickEvent);
		((Button)findViewById(R.id.unregisterButton)).setOnClickListener(unregisterButtonClickEvent);
	}
	
	Button.OnClickListener registerButtonClickEvent = new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			String url = "http://dev.wafflestudio.net:10101/users/" + user.getId().toString() + "/register";
			JSONObject send_msg = new JSONObject();
			try {
				send_msg.put("subject_id", subject.getId());
				send_msg.put("token", user.getToken());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			new Register().execute(url, send_msg.toString());
		}
	};
	
	Button.OnClickListener unregisterButtonClickEvent = new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			String url = "http://dev.wafflestudio.net:10101/users/" + user.getId().toString() + "/unregister";
			JSONObject send_msg = new JSONObject();
			try {
				send_msg.put("subject_id", subject.getId());
				send_msg.put("token", user.getToken());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			new Unregister().execute(url, send_msg.toString());
		}
	};
	//	"dev.wafflestudio.net:10101/users/" + user.getId() + "/register_subject"
    private class Register extends AsyncTask<String, Boolean, String> {
        @Override
        protected String doInBackground(String... urls) {
        	
            return POST(urls[0], urls[1]);
        }
        // onPostExecute displays the results of the AsyncTask.
        
        @Override
        protected void onPostExecute(String result) {
        	super.onPostExecute(result);
            
        	if(result != null){
        		Log.d("ASYNC", "result = " + result);
        		JSONObject jsonResult = null;
        		String registerResult = "";
        		try {
					jsonResult = new JSONObject(result);
					registerResult = jsonResult.getString("result");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
        		if (!registerResult.equals("success")){
        			Toast.makeText(DetailSubjectActivity.this, "fail to regitster", Toast.LENGTH_SHORT).show();
        			return;
        		}
        		Toast.makeText(DetailSubjectActivity.this, "regitster", Toast.LENGTH_SHORT).show();
        		user.appendMySubjectIdList(subject.getId());

        		mainActivity.putExtra("subjectIdList", user.getSubjectIdList());
        		findSubjectActivity.putExtra("subjectIdList", user.getSubjectIdList());
        		setResult(RESULT_OK, mainActivity);
        		setResult(RESULT_OK, findSubjectActivity);

    			findViewById(R.id.registerButton).setVisibility(View.GONE);
    			findViewById(R.id.unregisterButton).setVisibility(View.VISIBLE);
        	}
    		else {
				Toast.makeText(DetailSubjectActivity.this, "please connect to Internet or the server is down...", Toast.LENGTH_SHORT).show();
    		}
        }
    }
	
    private class Unregister extends AsyncTask<String, Boolean, String> {
        @Override
        protected String doInBackground(String... urls) {
        	
            return POST(urls[0], urls[1]);
        }
        // onPostExecute displays the results of the AsyncTask.
        
        @Override
        protected void onPostExecute(String result) {
        	super.onPostExecute(result);
            
        	if(result != null){
        		JSONObject jsonResult = null;
        		String registerResult = "";
        		try {
					jsonResult = new JSONObject(result);
					registerResult = jsonResult.getString("result");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
        		if (!registerResult.equals("success")){	
        			Toast.makeText(DetailSubjectActivity.this, "fail to unregitster", Toast.LENGTH_SHORT).show();
        			return;
        		}
        		Toast.makeText(DetailSubjectActivity.this, "unregitster", Toast.LENGTH_SHORT).show();
        		user.deleteMySubjectIdList(subject.getId());
        		
        		mainActivity.putExtra("subjectIdList", user.getSubjectIdList());
        		findSubjectActivity.putExtra("subjectIdList", user.getSubjectIdList());
        		setResult(RESULT_OK, mainActivity);
        		setResult(RESULT_OK, findSubjectActivity);

    			findViewById(R.id.unregisterButton).setVisibility(View.GONE);
    			findViewById(R.id.registerButton).setVisibility(View.VISIBLE);
        	}
    		else {
				Toast.makeText(DetailSubjectActivity.this, "please connect to Internet or the server is down...", Toast.LENGTH_SHORT).show();
    		}
        }
    }
	/*
	@Override
	public void onPause(){
		finish();
	}*/
    
    @Override
    protected void onDestroy(){
    	super.onDestroy();
    	
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
