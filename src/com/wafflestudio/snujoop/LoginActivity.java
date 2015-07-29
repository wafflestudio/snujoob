package com.wafflestudio.snujoop;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
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
	
	String regIdToServer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

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
		} catch (IOException e) {
			studentNumber = password = "";
			e.printStackTrace();
		} catch (Exception e) {
			studentNumber = password = "";
			e.printStackTrace();
		}
		
		((EditText)findViewById(R.id.student_number)).setText(studentNumber);
		((EditText)findViewById(R.id.password)).setText(password);
		
		((Button)findViewById(R.id.login_button)).setOnClickListener(loginButtonClickEvent);
	}
	
	Button.OnClickListener loginButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			String studentNumber = ((EditText)findViewById(R.id.student_number)).getText().toString();
			String password = ((EditText)findViewById(R.id.password)).getText().toString();
			if ( User.isStudentNumber(studentNumber) == false ){
				Toast.makeText(LoginActivity.this, getString(R.string.student_number_format_error_message), Toast.LENGTH_SHORT).show();
				return;
			}
			JSONObject loginInformation = new JSONObject();
			try {
				loginInformation.put("student_number", studentNumber);
				loginInformation.put("password", password);
				loginInformation.put("reg_id", regIdToServer);
				loginInformation.put("device", "Android");
			} catch (JSONException e) {
				Toast.makeText(LoginActivity.this, getString(R.string.json_parsing_error_message), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
				return;
			}
			String send_msg = loginInformation.toString();
			
			new RequestLogin().execute(Http.HOME + "/login", send_msg);
    		findViewById(R.id.linla_header_progress).setVisibility(View.VISIBLE);
    		((Button)findViewById(R.id.login_button)).setEnabled(false);

			String filename = "idpassword";
			String string = null;
			if ( ((CheckBox)findViewById(R.id.auto_login_checkbox)).isChecked() ){
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
        	
            return Http.POST(urls[0], urls[1]);
        }
        // onPostExecute displays the results of the AsyncTask.
        
        @Override
        protected void onPostExecute(String result) {
        	super.onPostExecute(result);
            
        	if(result != null){
        		Log.d("ASYNC", "result = " + result);
        		JSONObject jsonResult;
        		try {
					jsonResult = new JSONObject(result);
					if (jsonResult.get("result").equals("fail")){
						Toast.makeText(LoginActivity.this, getString(R.string.log_in_failure_message), Toast.LENGTH_SHORT).show();
			    		((Button)findViewById(R.id.login_button)).setEnabled(true);
					} else {
						User.user = new User(jsonResult.getInt("id"), new ArrayList<Integer>(), jsonResult.getString("token"));
						Toast.makeText(LoginActivity.this, getString(R.string.log_in_success_message), Toast.LENGTH_SHORT).show();
		        		finish();
					}
				} catch (JSONException e) {
					Toast.makeText(LoginActivity.this, getString(R.string.json_parsing_error_message), Toast.LENGTH_SHORT).show();
		    		((Button)findViewById(R.id.login_button)).setEnabled(true);
		    		findViewById(R.id.linla_header_progress).setVisibility(View.GONE);
					e.printStackTrace();
					return;
				}
        	}
    		else {
				Toast.makeText(LoginActivity.this, getString(R.string.server_exception), Toast.LENGTH_SHORT).show();
				((Button)findViewById(R.id.login_button)).setEnabled(true);
    		}
        	findViewById(R.id.linla_header_progress).setVisibility(View.GONE);
        }
    }
}
