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

	Subject subject = null;
	
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
		subject = new Subject(
					intent.getIntExtra("subjectId", -1),
					intent.getStringExtra("subjectName"),
					intent.getStringExtra("subjectNumber"),
					intent.getStringExtra("lectureNumber"),
					intent.getStringExtra("lecturer"),
					intent.getStringExtra("classTime")
				);
		Integer capacity = intent.getIntExtra("capacity", 0);
		Integer capacityEnrolled = intent.getIntExtra("capacityEnrolled", 0);
		Integer enrolled = intent.getIntExtra("enrolled", 0);
		if (subject.getId() == -1 || User.user == null){
			Toast.makeText(DetailSubjectActivity.this, getString(R.string.error_message), Toast.LENGTH_SHORT).show();
			return;
		}
		
		((TextView)findViewById(R.id.subject_name)).setText(subject.getSubjectName());
		((TextView)findViewById(R.id.subject_number)).setText("과목 번호: " 
				+ subject.getSubjectNumber() + "  " + subject.getLectureNumber());
		((TextView)findViewById(R.id.lecturer)).setText("" + subject.getLecturer());
		((TextView)findViewById(R.id.class_time)).setText("" + subject.getClassTime());
		((TextView)findViewById(R.id.capacity)).setText("정원 (재학생): " + capacity
				+ (capacityEnrolled != 0 ? " (" + capacityEnrolled + ")" : ""));
		((TextView)findViewById(R.id.enrolled)).setText("등록한사람: " + enrolled);
		if (enrolled >= capacity)
			((TextView)findViewById(R.id.enrolled)).setTextColor(Color.parseColor("#FF0000"));

		if (User.user.isMySubjectIdList(subject.getId())){
			findViewById(R.id.register_button).setVisibility(View.GONE);
			findViewById(R.id.unregister_button).setVisibility(View.VISIBLE);
		}
		else{
			findViewById(R.id.unregister_button).setVisibility(View.GONE);
			findViewById(R.id.register_button).setVisibility(View.VISIBLE);
		}
		
		((Button)findViewById(R.id.register_button)).setOnClickListener(registerButtonClickEvent);
		((Button)findViewById(R.id.unregister_button)).setOnClickListener(unregisterButtonClickEvent);
	}
	
	Button.OnClickListener registerButtonClickEvent = new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			String url = Http.HOME + "/users/" + User.user.getId().toString() + "/register";
			JSONObject send_msg = new JSONObject();
			try {
				send_msg.put("subject_id", subject.getId());
				send_msg.put("token", User.user.getToken());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			new Register().execute(url, send_msg.toString());
			findViewById(R.id.register_button).setClickable(false);
		}
	};
	
	Button.OnClickListener unregisterButtonClickEvent = new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			String url = Http.HOME + "/users/" + User.user.getId().toString() + "/unregister";
			JSONObject send_msg = new JSONObject();
			try {
				send_msg.put("subject_id", subject.getId());
				send_msg.put("token", User.user.getToken());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			new Unregister().execute(url, send_msg.toString());
			findViewById(R.id.unregister_button).setClickable(false);
		}
	};
	//	Http.HOME + "users/" + user.getId() + "/register_subject"
    private class Register extends AsyncTask<String, Boolean, String> {
        @Override
        protected String doInBackground(String... urls) {
        	
            return Http.POST(urls[0], urls[1]);
        }
        // onPostExecute displays the results of the AsyncTask.
        
        @Override
        protected void onPostExecute(String result) {
        	super.onPostExecute(result);
            
        	if(result != null){
        		Log.d("ASYNC", "result = " + result);
        		JSONObject jsonResult;
        		String registerResult;
        		try {
					jsonResult = new JSONObject(result);
					registerResult = jsonResult.getString("result");
				} catch (JSONException e) {
					Toast.makeText(DetailSubjectActivity.this, getString(R.string.json_parsing_error_message), Toast.LENGTH_SHORT).show();
					e.printStackTrace();
					return;
				}
        		if (registerResult.equals("success")){
	        		Toast.makeText(DetailSubjectActivity.this, getString(R.string.register_suceess_message), Toast.LENGTH_SHORT).show();
	        		User.user.appendMySubjectIdList(subject.getId());
	    			findViewById(R.id.register_button).setClickable(true);
	    			findViewById(R.id.register_button).setVisibility(View.GONE);
	    			findViewById(R.id.unregister_button).setVisibility(View.VISIBLE);
        		} else {
        			Toast.makeText(DetailSubjectActivity.this, getString(R.string.register_failure_message), Toast.LENGTH_SHORT).show();
        		}
        	}
    		else {
				Toast.makeText(DetailSubjectActivity.this, getString(R.string.server_exception), Toast.LENGTH_SHORT).show();
    		}
        }
    }
	
    private class Unregister extends AsyncTask<String, Boolean, String> {
        @Override
        protected String doInBackground(String... urls) {
        	
            return Http.POST(urls[0], urls[1]);
        }
        // onPostExecute displays the results of the AsyncTask.
        
        @Override
        protected void onPostExecute(String result) {
        	super.onPostExecute(result);
            
        	if(result != null){
        		JSONObject jsonResult;
        		String registerResult;
        		try {
					jsonResult = new JSONObject(result);
					registerResult = jsonResult.getString("result");
				} catch (JSONException e) {
					Toast.makeText(DetailSubjectActivity.this, getString(R.string.json_parsing_error_message), Toast.LENGTH_SHORT).show();
					e.printStackTrace();
					return;
				}
        		if (registerResult.equals("success")){
            		Toast.makeText(DetailSubjectActivity.this, getString(R.string.unregister_success_message), Toast.LENGTH_SHORT).show();
            		User.user.deleteMySubjectIdList(subject.getId());
        			findViewById(R.id.unregister_button).setClickable(true);
        			findViewById(R.id.unregister_button).setVisibility(View.GONE);
        			findViewById(R.id.register_button).setVisibility(View.VISIBLE);
        		} else {
        			Toast.makeText(DetailSubjectActivity.this, R.string.unregister__failure_message, Toast.LENGTH_SHORT).show();
        		}
        	}
    		else {
				Toast.makeText(DetailSubjectActivity.this, getString(R.string.server_exception), Toast.LENGTH_SHORT).show();
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
