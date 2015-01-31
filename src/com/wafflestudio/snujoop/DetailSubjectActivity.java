package com.wafflestudio.snujoop;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
		Integer capacityEnrolled = intent.getIntExtra("capacityEnrolled", 0);
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
		((TextView)findViewById(R.id.capacity)).setText("정원 (재학생): " + capacity
				+ (capacityEnrolled != 0 ? " (" + capacityEnrolled + ")" : ""));
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
        	
            return MainActivity.POST(urls[0], urls[1]);
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
        			Toast.makeText(DetailSubjectActivity.this, "fail to register", Toast.LENGTH_SHORT).show();
        			return;
        		}
        		Toast.makeText(DetailSubjectActivity.this, "register", Toast.LENGTH_SHORT).show();
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
        	
            return MainActivity.POST(urls[0], urls[1]);
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
        			Toast.makeText(DetailSubjectActivity.this, "fail to unregister", Toast.LENGTH_SHORT).show();
        			return;
        		}
        		Toast.makeText(DetailSubjectActivity.this, "unregister", Toast.LENGTH_SHORT).show();
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
}
