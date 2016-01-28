package me.leeingnyo.snujoob;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;

public class MainActivity extends AppCompatActivity {

    private static final int LOGIN_ACTIVITY = 0;
    private static final int SUCCESSFUL_LOGIN = 100;
    private static final int FAIL_LOGIN = 200;

    LinearLayout progressBar;
    String studentId;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (LinearLayout)findViewById(R.id.progress_bar);

        progressBar.setVisibility(View.VISIBLE);

        try {
            FileInputStream infoFile = openFileInput("information");
            StringBuffer fileContent = new StringBuffer("");
            byte[] buffer = new byte[1024];
            int n;
            while ((n = infoFile.read(buffer)) != -1) {
                fileContent.append(new String(buffer, 0, n));
            }
            JSONObject jsonObject = new JSONObject(fileContent.toString());
            studentId = jsonObject.getString("student_id");
            token = jsonObject.getString("token");
            boolean isAutoLogin = jsonObject.getBoolean("is_auto_login");
            if (isAutoLogin){
                autoLogin(studentId, token);
            }
            infoFile.close();
        } catch (Exception e){
            goToLoginActivity();
        }
    }

    private void autoLogin(String studentId, String token) throws JSONException {
        JSONObject params = new JSONObject();
        params.put("student_id", studentId);
        params.put("token", token);
        JsonObjectRequest autoLogin = new JsonObjectRequest(Request.Method.POST, RequestSingleton.getAutoLoginUrl(), params
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean result = response.getBoolean("result");
                    if (result){
                        progressBar.setVisibility(View.GONE);
                    } else  {
                        goToLoginActivity();
                    }
                } catch (JSONException e) {
                    goToLoginActivity();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                goToLoginActivity();
            }
        });
        RequestSingleton.getInstance(this).addToRequestQueue(autoLogin);
    }

    private void goToLoginActivity(){
        Toast.makeText(getBaseContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
        case LOGIN_ACTIVITY:
            switch (resultCode){
            case SUCCESSFUL_LOGIN:
                studentId = data.getStringExtra("student_id");
                token = data.getStringExtra("token");
                Toast.makeText(this, "환영합니다, " + studentId + " 님", Toast.LENGTH_SHORT).show();
                break;
            case FAIL_LOGIN:
                finish();
                break;
            }
            break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
