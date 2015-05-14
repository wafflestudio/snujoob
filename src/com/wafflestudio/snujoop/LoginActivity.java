package com.wafflestudio.snujoop;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class LoginActivity extends Activity {
	
	Intent mainActivity = null;
	User user = null;
	String regIdToServer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mainActivity = new Intent(LoginActivity.this, MainActivity.class);
		setResult(RESULT_CANCELED, mainActivity);

		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if("".equals(regId))
			GCMRegistrar.register(this, "801697427023");
		if (GCMRegistrar.isRegistered(this))
			regIdToServer = GCMRegistrar.getRegistrationId(this);

		FileInputStream fis;
		String studentNumber = "";
		String password = "";
		
		try {
			fis = openFileInput("idpassword");
			StringBuffer fileContent = new StringBuffer("");
			byte[] buffer = new byte[1024];
			int n;
			while ((n = fis.read(buffer)) != -1) {
				fileContent.append(new String(buffer, 0, n)); 
			}
			studentNumber = fileContent.toString().split("\n")[0];
			password = fileContent.toString().split("\n")[1];
			
		} catch (FileNotFoundException e) {
			studentNumber = password = "";
			e.printStackTrace();
		} catch (Exception e) {
			studentNumber = password = "";
			e.printStackTrace();
		}
		
		((EditText)findViewById(R.id.studentNumber)).setText(studentNumber);
		((EditText)findViewById(R.id.password)).setText(password);
		
		((Button)findViewById(R.id.loginButton)).setOnClickListener(loginButtonClickEvent);
	}
	
	Button.OnClickListener loginButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			String studentNumber = ((EditText)findViewById(R.id.studentNumber)).getText().toString();
			String password = ((EditText)findViewById(R.id.password)).getText().toString();
			if ( User.isStudentNumber(studentNumber) == false ){
				Toast.makeText(LoginActivity.this, "please input the student number in format (20xx-xxxx).", Toast.LENGTH_SHORT).show();
				return;
			}
			JSONObject jsonobjectStudentNumberPassword = new JSONObject();
			try {
				jsonobjectStudentNumberPassword.put("student_number", studentNumber);
				jsonobjectStudentNumberPassword.put("password", password);
				jsonobjectStudentNumberPassword.put("reg_id", regIdToServer);
				jsonobjectStudentNumberPassword.put("device", "Android");
			} catch (JSONException e) {
				Toast.makeText(LoginActivity.this, "fail making jsonobject", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
				return;
			}
			String send_msg = jsonobjectStudentNumberPassword.toString();
			
			new RequestLogin().execute("http://revreserver.me:11663/login", send_msg);
    		findViewById(R.id.linlaHeaderProgress).setVisibility(View.VISIBLE);
    		((Button)findViewById(R.id.loginButton)).setEnabled(false);

			String filename = "idpassword";
			String string = null;
			if ( ((CheckBox)findViewById(R.id.autoLoginCheckBox)).isChecked() ){
				string = studentNumber + "\n" + password;
			} else {
				string = "\n";
			}
			FileOutputStream outputStream;
			try {
				outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
				outputStream.write(string.getBytes());
				outputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

    private class RequestLogin extends AsyncTask<String, Boolean, String> {
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
        		try {
					jsonResult = new JSONObject(result);
					if (jsonResult.get("result") == "fail"){
						Toast.makeText(LoginActivity.this, "fail to login", Toast.LENGTH_SHORT).show();
			    		((Button)findViewById(R.id.loginButton)).setEnabled(true);
						return;
					}
					user = new User(jsonResult.getInt("id"), new ArrayList<Integer>(), jsonResult.getString("token"));
				} catch (JSONException e) {
					Toast.makeText(LoginActivity.this, "fail making jsonobject", Toast.LENGTH_SHORT).show();
		    		((Button)findViewById(R.id.loginButton)).setEnabled(true);
					e.printStackTrace();
					return;
				}
        		mainActivity.putExtra("userId", user.getId());
        		mainActivity.putExtra("subjectIdList", user.getSubjectIdList());
        		mainActivity.putExtra("userToken", user.getToken());
        		setResult(RESULT_OK, mainActivity);
        		finish();
        	}
    		else {
				Toast.makeText(LoginActivity.this, "please connect to Internet or the server is down...", Toast.LENGTH_SHORT).show();
	    		((Button)findViewById(R.id.loginButton)).setEnabled(true);
    		}
    		findViewById(R.id.linlaHeaderProgress).setVisibility(View.GONE);
        }
    }
}
