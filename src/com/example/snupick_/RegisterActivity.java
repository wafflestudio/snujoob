package com.example.snupick_;

import java.io.BufferedInputStream;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

		((Button)findViewById(R.id.register)).setOnClickListener(registerButtonClickEvent);
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
			
			new RequestRegister().execute("http://dev.wafflestudio.net:10101/users", send_msg);
		}
	};

    private class RequestRegister extends AsyncTask<String, Boolean, String> {
        @Override
        protected String doInBackground(String... urls) {
 
            return POST(urls[0], urls[1]);
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
					e.printStackTrace();
					return;
				}
        		String requestRegisterResult;
				try {
					requestRegisterResult = jsonResult.getString("result");
				} catch (JSONException e) {
					Toast.makeText(RegisterActivity.this, "there are no 'result'", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
					return;
				}
        		switch (requestRegisterResult){
        		case "already":
        			Toast.makeText(RegisterActivity.this, "already exist. please contact to glglgozz@wafflestudio.com", Toast.LENGTH_LONG).show();
        			break;
        		case "success":
        			Toast.makeText(RegisterActivity.this, "success resgistering", Toast.LENGTH_LONG).show();
        			finish();
        			break;
        		case "fail":
        			Toast.makeText(RegisterActivity.this, "fail registering", Toast.LENGTH_LONG).show();
        			break;
        		}
        	}
    		else {
				Toast.makeText(RegisterActivity.this, "please connect to Internet", Toast.LENGTH_SHORT).show();
    		}
        }
    }
	
	public String POST(String url, String send_msg){
		StringBuilder JSONdata = new StringBuilder();
		InputStream inputStream = null;
		byte[] buffer = new byte[1024];
		String result = null;
		
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			StringEntity se = new StringEntity(send_msg);
			httpPost.setEntity(se);
			httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            HttpResponse httpResponse = httpClient.execute(httpPost);
            inputStream = httpResponse.getEntity().getContent();
            if(inputStream != null){
                try {
                    int bytesRead = 0;
                    BufferedInputStream bis = new BufferedInputStream(inputStream);
                    while ((bytesRead = bis.read(buffer) ) != -1) {
                        String line = new String(buffer, 0, bytesRead);
                        JSONdata.append(line);
                    }
                    result = JSONdata.toString();
                } catch (Exception e) {
                    Log.e("logcat", Log.getStackTraceString(e));
                } finally {
                    try {
                        inputStream.close();
                    } catch (Exception ignore) {
                    }
                }
            }
            else{
                result = "{'result' : 'fail'}";
            }
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
		return result;
	}
    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected())
                return true;
            else
                return false;
    }
}
