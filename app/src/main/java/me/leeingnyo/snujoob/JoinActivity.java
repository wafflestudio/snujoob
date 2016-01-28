package me.leeingnyo.snujoob;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class JoinActivity extends AppCompatActivity {

    private static final int SUCCESSFUL_JOIN = 102;
    private static final int FAIL_JOIN = 202;

    LinearLayout progressBar;
    EditText studentIdEditText;
    EditText passwordConfirmEditText;
    EditText passwordEditText;
    Button joinButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (LinearLayout)findViewById(R.id.progress_bar);
        studentIdEditText = (EditText)findViewById(R.id.student_id);
        passwordEditText = (EditText)findViewById(R.id.password);
        passwordConfirmEditText = (EditText)findViewById(R.id.password_confirm);
        joinButton = (Button)findViewById(R.id.join);

        setResult(FAIL_JOIN);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                join();
            }
        });
    }

    private void join(){
        makeControlsDisabled();
        final String studentId = studentIdEditText.getText().toString();
        final String password = passwordEditText.getText().toString();
        final String passwordConfirm = passwordConfirmEditText.getText().toString();
        if (!isValidated(studentId, password, passwordConfirm)){
            Toast.makeText(this, "입력하신 정보를 확인해주세요", Toast.LENGTH_SHORT).show();
            makeContorolsEnabled();
            return;
        }
        JSONObject params = new JSONObject();
        try {
            params.put("student_id", studentId);
            params.put("password", password);
        } catch (JSONException e) {
            makeContorolsEnabled();
            return;
        }
        JsonObjectRequest join = new JsonObjectRequest(Request.Method.POST, RequestSingleton.getJoinUrl(), params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean result = response.getBoolean("result");
                    if (result) {
                        Intent intent = new Intent();
                        intent.putExtra("student_id", studentId);
                        setResult(SUCCESSFUL_JOIN, intent);
                        Toast.makeText(getBaseContext(), "회원가입에 성공하셨습니다.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String message = "";
                        if (response.has("message")){
                            message = response.getString("message");
                        }
                        Toast.makeText(getBaseContext(), "회원가입에 실패하셨습니다. " + message, Toast.LENGTH_LONG).show();
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
        RequestSingleton.getInstance(this).addToRequestQueue(join);
    }

    private void makeControlsDisabled(){
        progressBar.setVisibility(View.VISIBLE);
        studentIdEditText.setEnabled(false);
        passwordEditText.setEnabled(false);
        passwordConfirmEditText.setEnabled(false);
        joinButton.setEnabled(false);
    }

    private boolean isValidated(String studentId, String password, String passwordConfirm){
        return !"".equals(studentId) && !"".equals(password) && password.equals(passwordConfirm) && Pattern.matches("20[0-9]{2}-[12][0-9]{4}", studentId);
    }

    private void makeContorolsEnabled(){
        progressBar.setVisibility(View.GONE);
        studentIdEditText.setEnabled(true);
        passwordEditText.setEnabled(true);
        passwordConfirmEditText.setEnabled(true);
        joinButton.setEnabled(true);
    }
}
