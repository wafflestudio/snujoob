package com.wafflestudio.snujoop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
		mainActivity.putExtra("userToken", user.getToken());
		
		((Button)findViewById(R.id.find_button)).setOnClickListener(findButtonClickEvent);
	}
	
	Button.OnClickListener findButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			String keyword = ((EditText)findViewById(R.id.keyword)).getText().toString().replaceAll("\\s+", "");
			
			new RequestFindSubject().execute(Http.HOME + "/subjects/search.json?keyword=" + keyword);
    		findViewById(R.id.linla_header_progress).setVisibility(View.VISIBLE);
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
			mainActivity.putExtra("userToken", user.getToken());
			break;
		}
	}

	private AdapterView.OnItemClickListener subjectItemClickListener = new AdapterView.OnItemClickListener() {
		@SuppressWarnings("unchecked")
		@Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long l_position) {
			HashMap<String, String> hashmap = (HashMap<String, String>) parent.getAdapter().getItem(position);
            
			Intent intent = new Intent(FindSubjectActivity.this, DetailSubjectActivity.class);
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
    
    private class RequestFindSubject extends AsyncTask<String, Boolean, String> {
        @Override
        protected String doInBackground(String... urls) {
        	
            return Http.GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        
        @Override
        protected void onPostExecute(String result) {
        	super.onPostExecute(result);
            
        	if(result != null){
        		Log.d("ASYNC", "result = " + result);
        		JSONObject jsonResult = null;
        		JSONArray jsonSubjectList = null;
        		SimpleAdapter adapter = null;
        		try {
        			List<HashMap<String, String>> subjectList = new ArrayList<HashMap<String, String>>();
					jsonResult = new JSONObject(result);
					jsonSubjectList = jsonResult.getJSONArray("result");
					
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
					}
					
					String[] from = {  "subject_name", "subject_number", "lecturer" };
					int[] to = { R.id.subject_name, R.id.subject_number, R.id.lecturer };
					adapter = new SimpleAdapter(getBaseContext(), subjectList, R.layout.subject_listview_content, from, to);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        		ListView listView = (ListView)findViewById(R.id.result_list_view);
        		listView.setAdapter(adapter);
        		listView.setOnItemClickListener(subjectItemClickListener);

        		findViewById(R.id.linla_header_progress).setVisibility(View.GONE);
        	}
    		else {
				Toast.makeText(FindSubjectActivity.this, "please connect to Internet or the server is down...", Toast.LENGTH_SHORT).show();
    		}
        }
    }
}