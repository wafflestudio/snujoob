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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {
	
	final static int RESULT_LOGIN = 1;
	final static int RESULT_DETAILSUBJECT = 2;
	final static int RESULT_FINDSUBJECT = 3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//TODO 유저를 유지시키는 법
		if (User.user == null && false){
			//TODO 처음 실행했을 시에 자동 로그인
		}

		if (User.user == null){
    		findViewById(R.id.before_login).setVisibility(View.VISIBLE);
			findViewById(R.id.atfer_login).setVisibility(View.GONE);
		} else {
    		findViewById(R.id.before_login).setVisibility(View.GONE);
    		findViewById(R.id.atfer_login).setVisibility(View.VISIBLE);
			new LoadUserInformation().execute(Http.HOME + "/users/"
					+ User.user.getId().toString() + ".json?token=" + User.user.getToken());
			//TODO 계속 처불러오는 것보다는 static을 통한 데이터 유지에 대해서 생각해보기 (에러가 있을 수 있으려나)			
		}

		((Button)findViewById(R.id.login_button)).setOnClickListener(loginButtonClickEvent);
		((Button)findViewById(R.id.register_button)).setOnClickListener(registerButtonClickEvent);
		((Button)findViewById(R.id.find_button)).setOnClickListener(findSubjectButtonClickEvent);
		((Button)findViewById(R.id.unregister_button)).setOnClickListener(unregisterButtonClickEvent);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();

		if (User.user == null){
    		findViewById(R.id.before_login).setVisibility(View.VISIBLE);
			findViewById(R.id.atfer_login).setVisibility(View.GONE);
		} else {
    		findViewById(R.id.before_login).setVisibility(View.GONE);
    		findViewById(R.id.atfer_login).setVisibility(View.VISIBLE);
        	findViewById(R.id.linla_header_progress).setVisibility(View.VISIBLE);
			new LoadUserInformation().execute(Http.HOME + "/users/"
					+ User.user.getId().toString() + ".json?token=" + User.user.getToken());
			//TODO 계속 처불러오는 것보다는 static을 통한 데이터 유지에 대해서 생각해보기 (에러가 있을 수 있으려나)			
		}
	}
	
	OnClickListener loginButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this, LoginActivity.class);
			startActivity(intent);
		}
	};
	
	OnClickListener registerButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
			startActivity(intent);
		}
	};
	
	OnClickListener findSubjectButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this, FindSubjectActivity.class);
			startActivity(intent);
		}
	};
	
	OnClickListener unregisterButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			Unregister();
		}
	};

	OnItemClickListener subjectItemClickListener = new OnItemClickListener() {
    	@SuppressWarnings("unchecked")
    	//TODO 이거 없애기
		@Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long l_position) {
            HashMap<String, String> hashmap = (HashMap<String, String>) parent.getAdapter().getItem(position);
            
			Intent intent = new Intent(MainActivity.this, DetailSubjectActivity.class);
			intent.putExtra("subjectId", Integer.parseInt(hashmap.get("id")));
			intent.putExtra("subjectName", hashmap.get("subject_name"));
			intent.putExtra("subjectNumber", hashmap.get("subject_number").split(" ")[0]);
			intent.putExtra("lectureNumber", hashmap.get("subject_number").split(" ")[1]);
			intent.putExtra("lecturer", hashmap.get("lecturer"));
			//TODO 시간 추가
			intent.putExtra("capacity", Integer.parseInt(hashmap.get("capacity")));
			intent.putExtra("capacityEnrolled", Integer.parseInt(hashmap.get("capacity_enrolled")));
			intent.putExtra("enrolled", Integer.parseInt(hashmap.get("enrolled")));

			startActivityForResult(intent, MainActivity.RESULT_DETAILSUBJECT);
        }
    };
	
    void Unregister() {
		if (GCMRegistrar.isRegistered(this)) {
			GCMRegistrar.unregister(this);
			Toast.makeText(this, "해제되었습니다.", Toast.LENGTH_LONG).show();
		}
	}

	private class LoadUserInformation extends AsyncTask<String, Boolean, String> {
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
        		User.user.getSubjectIdList().clear();
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
						
						User.user.appendMySubjectIdList(jsonSubject.getInt("id"));
						//TODO 지금 등록된 정보 잘 갖고 있기
					}
					
					String[] from = {  "subject_name", "subject_number", "lecturer" };
					int[] to = { R.id.subject_name, R.id.subject_number, R.id.lecturer };
					adapter = new SimpleAdapter(getBaseContext(), subjectList, R.layout.subject_listview_content, from, to);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		
        		ListView listView = (ListView)findViewById(R.id.subject_list_view);
        		listView.setAdapter(adapter);
        		listView.setOnItemClickListener(subjectItemClickListener);

            	findViewById(R.id.linla_header_progress).setVisibility(View.GONE);
        	}
    		else {
				Toast.makeText(MainActivity.this, "please connect to Internet or the server is down...", Toast.LENGTH_SHORT).show();
				//정체불명의 인터넷 코드
    		}
        }
    }
}