package com.example.snupick_;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FindSubjectActivity extends Activity {

	Intent mainActivity = null;
	User user = null;
	ArrayList<Subject> subjectList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_subject);
		
		Intent intent = getIntent();
		subjectList = new ArrayList<Subject>();
		mainActivity = new Intent(FindSubjectActivity.this, MainActivity.class);
		setResult(RESULT_OK, mainActivity);

		user = new User(
					intent.getIntExtra("userId", -1),
					intent.getIntegerArrayListExtra("subjectIdList"),
					intent.getStringExtra("userToken")
				);
		mainActivity.putExtra("userId", user.getId());
		mainActivity.putExtra("subjectIdList", user.getSubjectIdList());
		
		((Button)findViewById(R.id.findButton)).setOnClickListener(findButtonClickEvent);
	}
	
	Button.OnClickListener findButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			String keyword = ((EditText)findViewById(R.id.keyword)).getText().toString();
			
			new RequestFindSubject().execute("http://dev.wafflestudio.net:10101/subjects/search.json?keyword=" + keyword);
		}
	};
	
	protected void onActivityResult(int requestCode, int resultCode, Intent Data){
		switch(requestCode){
		case MainActivity.RESULT_DETAILSUBJECT:
			user = new User(
					Data.getIntExtra("userId", -1),
					Data.getIntegerArrayListExtra("subjectIdList"),
					Data.getStringExtra("userToken")
				);
			mainActivity.putExtra("userId", user.getId());
			mainActivity.putExtra("subjectIdList", user.getSubjectIdList());
			break;
		}
	}
	
	Button.OnClickListener subjectClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			for (int i = 0 ; i < subjectList.size() ; i++){
				Subject subject = subjectList.get(i);
				if (subject.getId() == v.getId()){
					Intent intent = new Intent(FindSubjectActivity.this, DetailSubjectActivity.class);
					intent.putExtra("userId", user.getId());
					intent.putExtra("subjectIdList", user.getSubjectIdList());
					intent.putExtra("subjectId", subject.getId());
					intent.putExtra("subjectName", subject.getSubjectName());
					intent.putExtra("subjectNumber", subject.getSubjectNumber());
					intent.putExtra("lectureNumber", subject.getLectureNumber());
					intent.putExtra("lecturer", subject.getLecturer());
					startActivityForResult(intent, MainActivity.RESULT_DETAILSUBJECT);
				}
			}
		}
	};

    private class RequestFindSubject extends AsyncTask<String, Boolean, String> {
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
								jsonSubject.getString("lecturer")
							);
						subjectList.add(subject);
						TextView textView = new TextView(FindSubjectActivity.this);
						textView.setId(subject.getId());
						textView.setText(subject.getSubjectName());
						subjectListLayout.addView(textView);
						textView.setOnClickListener(subjectClickEvent);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
    		else {
				Toast.makeText(FindSubjectActivity.this, "please connect to Internet or the server is down...", Toast.LENGTH_SHORT).show();
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
}