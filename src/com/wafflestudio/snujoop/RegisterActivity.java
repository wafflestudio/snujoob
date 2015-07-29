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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		((Button)findViewById(R.id.register_button)).setOnClickListener(registerButtonClickEvent);
	}
	
	Button.OnClickListener registerButtonClickEvent = new OnClickListener(){
		@Override
		public void onClick(View v) {
			String studentNumber = ((EditText)findViewById(R.id.student_number)).getText().toString();
			if (!User.isStudentNumber(studentNumber)){
				Toast.makeText(RegisterActivity.this, getString(R.string.student_number_format_error_message), Toast.LENGTH_SHORT).show();
				return;
			}
			String password = ((EditText)findViewById(R.id.password)).getText().toString();
			String passwordConfirm = ((EditText)findViewById(R.id.password_confirm)).getText().toString();
			if (!User.isPassword(password)){
				Toast.makeText(RegisterActivity.this, getString(R.string.password_format_error_message), Toast.LENGTH_SHORT).show();
				return;
			}
			
			//TODO please make hashed password and send that to server.
			
			if (!password.equals(passwordConfirm)){
				Toast.makeText(RegisterActivity.this, getString(R.string.password_confirm_failure_message), Toast.LENGTH_SHORT).show();
				return;
			}
			
			JSONObject registerInformation = new JSONObject();
			try {
				registerInformation.put("student_number", studentNumber);
				registerInformation.put("password", password);
			} catch (JSONException e) {
				Toast.makeText(RegisterActivity.this, getString(R.string.json_parsing_error_message), Toast.LENGTH_SHORT).show();
	    		findViewById(R.id.linla_header_progress).setVisibility(View.GONE);
				e.printStackTrace();
				return;
			}
			String send_msg = registerInformation.toString();
			
			new RequestRegister().execute(Http.HOME + "/users", send_msg);
    		findViewById(R.id.linla_header_progress).setVisibility(View.VISIBLE);
    		((Button)findViewById(R.id.register_button)).setEnabled(false);
		}
	};

    private class RequestRegister extends AsyncTask<String, Boolean, String> {
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
        		String requestRegisterResult;
        		try {
					jsonResult = new JSONObject(result);
					requestRegisterResult = jsonResult.getString("result");
				} catch (JSONException e) {
					Toast.makeText(RegisterActivity.this, getString(R.string.json_parsing_error_message), Toast.LENGTH_SHORT).show();
		    		((Button)findViewById(R.id.register_button)).setEnabled(true);
					e.printStackTrace();
					return;
				}
        		switch (requestRegisterResult){
        		case "already":
        			Toast.makeText(RegisterActivity.this, getString(R.string.sign_in_already_message), Toast.LENGTH_LONG).show();
		    		((Button)findViewById(R.id.register_button)).setEnabled(true);
        			break;
        		case "success":
        			Toast.makeText(RegisterActivity.this, getString(R.string.sign_in_success_message), Toast.LENGTH_SHORT).show();
		    		((Button)findViewById(R.id.register_button)).setEnabled(true);
        			finish();
        			break;
        		case "fail":
        			Toast.makeText(RegisterActivity.this, getString(R.string.sign_in_failure_message), Toast.LENGTH_LONG).show();
		    		((Button)findViewById(R.id.register_button)).setEnabled(true);
        			break;
        		}
        	}
    		else {
				Toast.makeText(RegisterActivity.this, getString(R.string.server_exception), Toast.LENGTH_SHORT).show();
	    		((Button)findViewById(R.id.register_button)).setEnabled(true);
    		}
    		findViewById(R.id.linla_header_progress).setVisibility(View.GONE);
        }
    }
}
