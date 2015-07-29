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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class FindSubjectActivity extends Activity {

	ArrayList<Subject> subjectList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_subject);
		
		subjectList = new ArrayList<Subject>();

		((Button)findViewById(R.id.find_button)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String keyword = ((EditText)findViewById(R.id.keyword)).getText().toString().replaceAll("\\s+", "");
				if ("".equals(keyword)){
					Toast.makeText(FindSubjectActivity.this, getString(R.string.need_keyword), Toast.LENGTH_SHORT).show();
					return;
				}
				new RequestFindSubject().execute(Http.HOME + "/subjects/search.json?keyword=" + keyword);
	    		findViewById(R.id.linla_header_progress).setVisibility(View.VISIBLE);
			}
		});
	}

	private OnItemClickListener subjectItemClickListener = new OnItemClickListener() {
		@SuppressWarnings("unchecked")
		@Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long l_position) {
			HashMap<String, String> subject = (HashMap<String, String>) parent.getAdapter().getItem(position);

			Intent intent = new Intent(FindSubjectActivity.this, DetailSubjectActivity.class);
			intent.putExtra("subjectId", Integer.parseInt(subject.get("id")));
			intent.putExtra("subjectName", subject.get("subject_name"));
			intent.putExtra("subjectNumber", subject.get("subject_number").split(" ")[0]);
			intent.putExtra("lectureNumber", subject.get("subject_number").split(" ")[1]);
			intent.putExtra("lecturer", subject.get("lecturer"));
			intent.putExtra("classTime", subject.get("class_time"));
			intent.putExtra("capacity", Integer.parseInt(subject.get("capacity")));
			intent.putExtra("capacityEnrolled", Integer.parseInt(subject.get("capacity_enrolled")));
			intent.putExtra("enrolled", Integer.parseInt(subject.get("enrolled")));

			startActivity(intent);
        }
    };
    
    private class RequestFindSubject extends AsyncTask<String, Boolean, String> {
        @Override
        protected String doInBackground(String... urls) {
        	
            return Http.GET(urls[0]);
        }

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
						String classTime = jsonSubject.getString("class_time");
						Integer capacity = jsonSubject.getInt("capacity");
						Integer capacityEnrolled = jsonSubject.getInt("capacity_enrolled");
						Integer enrolled = jsonSubject.getInt("enrolled");
						hashmap.put("id", id.toString());
						hashmap.put("subject_name", subjectName);
						hashmap.put("subject_number", subjectNumber);
						hashmap.put("lecturer", lecturer);
						hashmap.put("class_time", classTime);
						hashmap.put("capacity", capacity.toString());
						hashmap.put("capacity_enrolled", capacityEnrolled.toString());
						hashmap.put("enrolled", enrolled.toString());
						subjectList.add(hashmap);
					}
					
					String[] from = {  "subject_name", "subject_number", "lecturer", "class_time" };
					int[] to = { R.id.subject_name, R.id.subject_number, R.id.lecturer, R.id.class_time };
					adapter = new SimpleAdapter(getBaseContext(), subjectList, R.layout.subject_listview_content, from, to);
				} catch (JSONException e) {
					Toast.makeText(FindSubjectActivity.this, getString(R.string.json_parsing_error_message), Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
        		
        		ListView listView = (ListView)findViewById(R.id.result_list_view);
        		listView.setAdapter(adapter);
        		listView.setOnItemClickListener(subjectItemClickListener);

        		findViewById(R.id.linla_header_progress).setVisibility(View.GONE);
        	}
    		else {
				Toast.makeText(FindSubjectActivity.this, getString(R.string.server_exception), Toast.LENGTH_SHORT).show();
    		}
        }
    }
}