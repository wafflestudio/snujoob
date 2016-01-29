package me.leeingnyo.snujoob;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private static final int SUCCESSFUL_LOGIN = 101;
    private static final int FAIL_LOGIN = 201;

    private static final int JOIN_ACTIVITY = 2;
    private static final int SUCCESSFUL_JOIN = 102;

    LinearLayout progressBar;
    EditText studentIdEditText;
    EditText passwordEditText;
    CheckBox autoLoginCheckBox;
    Button loginButton;
    Button joinButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (LinearLayout)findViewById(R.id.progress_bar);
        studentIdEditText = (EditText)findViewById(R.id.student_id);
        passwordEditText = (EditText)findViewById(R.id.password);
        autoLoginCheckBox = (CheckBox)findViewById(R.id.auto_login);
        loginButton = (Button)findViewById(R.id.login);
        joinButton = (Button)findViewById(R.id.join);

        setResult(FAIL_LOGIN);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToJoinActivity();
            }
        });
    }

    private void login(){
        makeControlsDisabled();
        final String studentId = studentIdEditText.getText().toString();
        final String password = passwordEditText.getText().toString();
        if (!isValidated(studentId, password)){
            Toast.makeText(this, "입력하신 정보를 확인해주세요", Toast.LENGTH_SHORT).show();
            makeContorolsEnabled();
            return;
        }
        JSONObject params = new JSONObject();
        try {
            params.put("student_id", studentId);
            params.put("password", password);
        } catch (JSONException e) {
            // 원래는 다른 걸로 고치고 던져서 밑에서 받아야겠지만 귀찮으니 생략하겠다
            makeContorolsEnabled();
            return;
        }
        JsonObjectRequest login = new JsonObjectRequest(Request.Method.POST, RequestSingleton.getLoginUrl(), params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean result = response.getBoolean("result");
                    if (result) {
                        String token = response.getString("token");
                        boolean isAutoLogin = autoLoginCheckBox.isChecked();
                        Intent intent = new Intent();
                        intent.putExtra("student_id", studentId);
                        intent.putExtra("token", token);
                        setResult(SUCCESSFUL_LOGIN, intent);
                        JSONObject information = new JSONObject();
                        information.put("student_id", studentId);
                        information.put("token", token);
                        information.put("is_auto_login", isAutoLogin);
                        FileOutputStream infoFile = openFileOutput("information", MODE_PRIVATE);
                        infoFile.write(information.toString().getBytes());
                        infoFile.close();
                        Toast.makeText(getBaseContext(), "로그인에 성공하셨습니다.", Toast.LENGTH_LONG).show();
                        if (checkPlayServices()) {
                            // Start IntentService to register this application with GCM.
                            Intent updateGCM = new Intent(LoginActivity.this, RegistrationIntentService.class);
                            startService(updateGCM);
                        }
                        finish();
                    } else {
                        String message = "";
                        if (response.has("message")){
                            message = response.getString("message");
                        }
                        Toast.makeText(getBaseContext(), "로그인에 실패하셨습니다. " + message, Toast.LENGTH_LONG).show();
                        makeContorolsEnabled();
                    }
                } catch (Exception e){
                    makeContorolsEnabled();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getBaseContext(), "현재 서비스 중이지 않거나, 기기가 인터넷에 연결되어있지 않습니다. 확인해주세요", Toast.LENGTH_LONG).show();
                makeContorolsEnabled();
            }
        });
        RequestSingleton.getInstance(this).addToRequestQueue(login);
    }

    private void makeControlsDisabled(){
        progressBar.setVisibility(View.VISIBLE);
        studentIdEditText.setEnabled(false);
        passwordEditText.setEnabled(false);
        autoLoginCheckBox.setEnabled(false);
        loginButton.setEnabled(false);
        joinButton.setEnabled(false);
    }

    private boolean isValidated(String studentId, String password){
        return !"".equals(studentId) && !"".equals(password) && Pattern.matches("20[0-9]{2}-[12][0-9]{4}", studentId);
    }

    private void makeContorolsEnabled(){
        progressBar.setVisibility(View.GONE);
        studentIdEditText.setEnabled(true);
        passwordEditText.setEnabled(true);
        autoLoginCheckBox.setEnabled(true);
        loginButton.setEnabled(true);
        joinButton.setEnabled(true);
    }

    private void goToJoinActivity(){
        // TODO go to join activity
        Intent intent = new Intent(this, JoinActivity.class);
        startActivityForResult(intent, JOIN_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
        case JOIN_ACTIVITY:
            switch (resultCode){
            case SUCCESSFUL_JOIN:
                String studentId = data.getStringExtra("student_id");
                studentIdEditText.setText(studentId);
                break;
            }
            break;
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("MainActivity", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
