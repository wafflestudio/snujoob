package com.wafflestudio.snujoop;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	
	User user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		((Button)findViewById(R.id.registerButton)).setOnClickListener(registerButtonClickEvent);
	}
	
	Button.OnClickListener registerButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			String studentNumber = ((EditText)findViewById(R.id.studentNumber)).getText().toString();
			if (User.isStudentNumber(studentNumber) == false){
				Toast.makeText(RegisterActivity.this, "please input the student number in format (20xx-xxxx).", Toast.LENGTH_SHORT).show();
				return;
			}
			String password = ((EditText)findViewById(R.id.password)).getText().toString();
			String passwordConfirm = ((EditText)findViewById(R.id.passwordConfirm)).getText().toString();
			
			//TODO please make hashed password and send that to server.
			
			if (password.equals(passwordConfirm) == false){
				Toast.makeText(RegisterActivity.this, "please confirm your password. it's wrong.", Toast.LENGTH_SHORT).show();
				return;
			}
			
			JSONObject jsonobjectStudentNumberPassword = new JSONObject();
			try {
				jsonobjectStudentNumberPassword.put("student_number", studentNumber);
				jsonobjectStudentNumberPassword.put("password", password);
			} catch (JSONException e) {
				Toast.makeText(RegisterActivity.this, "fail making jsonobject", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
				return;
			}
			String send_msg = jsonobjectStudentNumberPassword.toString();
			
			new RequestRegister().execute("http://revreserver.me:11663/users", send_msg);
    		findViewById(R.id.linlaHeaderProgress).setVisibility(View.VISIBLE);
    		((Button)findViewById(R.id.registerButton)).setEnabled(false);
		}
	};

    private class RequestRegister extends AsyncTask<String, Boolean, String> {
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
				} catch (JSONException e) {
					Toast.makeText(RegisterActivity.this, "fail making jsonobject", Toast.LENGTH_SHORT).show();
		    		((Button)findViewById(R.id.registerButton)).setEnabled(true);
					e.printStackTrace();
					return;
				}
        		String requestRegisterResult;
				try {
					requestRegisterResult = jsonResult.getString("result");
				} catch (JSONException e) {
					Toast.makeText(RegisterActivity.this, "there are no 'result'", Toast.LENGTH_SHORT).show();
		    		((Button)findViewById(R.id.registerButton)).setEnabled(true);
					e.printStackTrace();
					return;
				}
        		switch (requestRegisterResult){ 
        		case "already":
        			Toast.makeText(RegisterActivity.this, "already exist. please contact to 'glglgozz@wafflestudio.com'", Toast.LENGTH_LONG).show();
		    		((Button)findViewById(R.id.registerButton)).setEnabled(true);
        			break;
        		case "success":
        			Toast.makeText(RegisterActivity.this, "success resgistering", Toast.LENGTH_LONG).show();
		    		((Button)findViewById(R.id.registerButton)).setEnabled(true);
        			finish();
        			break;
        		case "fail":
        			Toast.makeText(RegisterActivity.this, "fail registering\nif something wrong, please connect to 'glglgozz@wafflestudio.com'", Toast.LENGTH_LONG).show();
		    		((Button)findViewById(R.id.registerButton)).setEnabled(true);
        			break;
        		}
        	}
    		else {
				Toast.makeText(RegisterActivity.this, "please connect to Internet", Toast.LENGTH_SHORT).show();
	    		((Button)findViewById(R.id.registerButton)).setEnabled(true);
    		}
    		findViewById(R.id.linlaHeaderProgress).setVisibility(View.GONE);
        }
    }
}
