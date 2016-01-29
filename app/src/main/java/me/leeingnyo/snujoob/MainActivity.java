package me.leeingnyo.snujoob;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int LOGIN_ACTIVITY = 1;
    private static final int SUCCESSFUL_LOGIN = 101;
    private static final int FAIL_LOGIN = 201;
    private static final int SEARCH_ACTIVITY = 3;

    LinearLayout progressBar;
    EditText queryEditText;
    Button searchButton;
    ImageView adImageView;
    RecyclerView lectureListView;
    View emptyView;
    Button retryButton;

    String studentId;
    String token;
    List<Integer> watchingList;
    List<Integer> registeredList;
    List<Lecture> lecturesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (LinearLayout)findViewById(R.id.progress_bar);
        queryEditText = (EditText)findViewById(R.id.query);
        searchButton = (Button)findViewById(R.id.search_button);
        adImageView = (ImageView)findViewById(R.id.image_ad);
        lectureListView = (RecyclerView)findViewById(R.id.lecture_list);
        // emptyView = findViewById(R.id.empty);
        // retryButton = (Button)findViewById(R.id.retry_button);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        lectureListView.setHasFixedSize(true);
        lectureListView.setLayoutManager(layoutManager);
        watchingList = new ArrayList<>();
        registeredList = new ArrayList<>();
        lecturesList = new ArrayList<>();
        queryEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                goToSearchActivity();
                return true;
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSearchActivity();
            }
        });

        makeControlsDisabled();
        try {
            FileInputStream infoFile = openFileInput("information");
            StringBuilder fileContent = new StringBuilder("");
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

    private void goToSearchActivity(){
        String query = queryEditText.getText().toString();
        if (query.length() < 2){
            Toast.makeText(MainActivity.this, "2글자 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("query", query);
        intent.putExtra("student_id", studentId);
        intent.putExtra("token", token);
        intent.putIntegerArrayListExtra("registered_list", new ArrayList<>(registeredList));
        startActivityForResult(intent, SEARCH_ACTIVITY);
    }

    private void makeControlsDisabled(){
        progressBar.setVisibility(View.VISIBLE);
        queryEditText.setEnabled(false);
        searchButton.setEnabled(false);
    }

    private void makeControlsEnabled(){
        progressBar.setVisibility(View.GONE);
        queryEditText.setEnabled(true);
        searchButton.setEnabled(true);
    }

    private void autoLogin(String studentId, final String token) throws JSONException {
        JSONObject params = new JSONObject();
        params.put("student_id", studentId);
        JsonObjectRequest autoLogin = new JsonObjectRequest(Request.Method.POST, RequestSingleton.getAutoLoginUrl(), params
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean result = response.getBoolean("result");
                    if (result){
                        makeControlsEnabled();
                        getUserInformation();
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
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-user-token", token);
                return headers;
            }
        };
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
                makeControlsEnabled();
                getUserInformation();
                break;
            case FAIL_LOGIN:
                finish();
                break;
            }
            break;
        case SEARCH_ACTIVITY:
            String query = data.getStringExtra("query");
            queryEditText.setText(query);
            getUserInformation();
            break;
        }
    }

    private void getUserInformation(){
        makeControlsDisabled();
        JsonObjectRequest getUserInformation = new JsonObjectRequest(Request.Method.GET, RequestSingleton.getUserUrl(studentId), null
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray lectures = response.getJSONArray("lectures");
                    JSONArray watchings = response.getJSONArray("watching_list");
                    showRegisteredLectures(lectures, watchings);
                } catch (JSONException e) {
                    Toast.makeText(getBaseContext(), "유저 정보를 가져오는데 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    makeControlsEnabled();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getBaseContext(), "유저 정보를 가져오는데 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                makeControlsEnabled();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-user-token", token);
                return headers;
            }
        };
        RequestSingleton.getInstance(this).addToRequestQueue(getUserInformation);
    }

    private void showRegisteredLectures(JSONArray lectures, JSONArray watchings){
        watchingList = new ArrayList<>();
        lecturesList = new ArrayList<>();
        registeredList = new ArrayList<>();
        try {
            for (int index = 0; index < watchings.length(); index++) {
                JSONObject watching = watchings.getJSONObject(index);
                watchingList.add(watching.getInt("lecture_id"));
            }
            for (int index = 0; index < lectures.length(); index++) {
                JSONObject lectureJson = lectures.getJSONObject(index);
                Lecture lecture = new Lecture();
                lecture.id = lectureJson.getInt("id");
                lecture.name = lectureJson.getString("name");
                lecture.subjectNumber = lectureJson.getString("subject_number");
                lecture.lectureNumber = lectureJson.getString("lecture_number");
                lecture.lecturer = lectureJson.getString("lecturer");
                lecture.time = lectureJson.getString("time");
                lecture.wholeCapacity = lectureJson.getInt("whole_capacity");
                lecture.enrolledCapacity = lectureJson.getInt("enrolled_capacity");

                lecture.enrolled = lectureJson.getInt("enrolled");
                lecture.competitor = lectureJson.getInt("competitors_number");
                lecturesList.add(lecture);
                registeredList.add(lecture.id);
            }
        } catch (JSONException e){
            Toast.makeText(this, "강의 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
        lectureListView.setAdapter(new RegisteredLecture(getApplicationContext(), lecturesList, R.layout.item_registered_lecture));
        makeControlsEnabled();
    }

    public class RegisteredLecture extends RecyclerView.Adapter<RegisteredLecture.ViewHolder> {

        Context context;
        List<Lecture> lectures;
        int item_registered_lecture;

        public RegisteredLecture(Context context, List<Lecture> lectures, int item_registered_lecture) {
            this.context = context;
            this.lectures = lectures;
            this.item_registered_lecture = item_registered_lecture;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(item_registered_lecture, null);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Lecture lecture = lectures.get(position);
            holder.name.setText(lecture.name);
            holder.name.setSelected(true);
            holder.name.setMarqueeRepeatLimit(-1);
            // FIXME marquee가 잘 안 되는 것 같음
            holder.number.setText(String.format("%s %s", lecture.subjectNumber, lecture.lectureNumber));
            holder.lecturer.setText(lecture.lecturer);
            holder.time.setText(lecture.time);
            holder.enrolled.setText(String.format("%d / %d", lecture.enrolled, lecture.wholeCapacity));
            if (lecture.enrolled == lecture.wholeCapacity){
                holder.enrolled.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            }
            holder.competitor.setText(lecture.competitor.toString());

            if (watchingList.contains(lecture.id)){
                holder.watching.setChecked(true);
            } else {
                holder.watching.setChecked(false);
            }
            holder.watching.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        watching(buttonView, lecture);
                    } else {
                        unwatching(buttonView, lecture);
                    }
                }
            });
            holder.register.setVisibility(View.GONE);
            holder.unregister.setVisibility(View.VISIBLE);
            holder.unregister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unregister(v, lecture);
                }
            });
        }

        @Override
        public int getItemCount() {
            return this.lectures.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            TextView number;
            TextView lecturer;
            TextView time;
            TextView enrolled;
            TextView competitor;
            Button register;
            Button unregister;
            CheckBox watching;

            public ViewHolder(View itemView) {
                super(itemView);
                name = (TextView)itemView.findViewById(R.id.name);
                number = (TextView)itemView.findViewById(R.id.number);
                lecturer = (TextView)itemView.findViewById(R.id.lecturer);
                time = (TextView)itemView.findViewById(R.id.time);
                enrolled = (TextView)itemView.findViewById(R.id.enrolled);
                competitor = (TextView)itemView.findViewById(R.id.competitor);
                register = (Button)itemView.findViewById(R.id.register_button);
                unregister = (Button)itemView.findViewById(R.id.unregister_button);
                watching = (CheckBox)itemView.findViewById(R.id.watching);
            }
        }

        public void removeItem(Lecture lecture){
            int position = lectures.indexOf(lecture);
            if (position != -1){
                lectures.remove(lecture);
                notifyItemRemoved(position);
            }
        }
    }

    private void unregister(final View view, final Lecture lecture){
        class Function {
            public void disable(){
                progressBar.setVisibility(View.VISIBLE);
                view.setEnabled(false);
            }
            public void successEnable(){
                progressBar.setVisibility(View.GONE);
                view.setEnabled(true);
            }
            public void failureEnable(){
                progressBar.setVisibility(View.GONE);
                view.setEnabled(true);
            }
        }
        final Function f = new Function();
        f.disable();
        JSONObject params = new JSONObject();
        try {
            params.put("lecture_id", lecture.id);
        } catch (JSONException e){
            f.failureEnable();
        }
        JsonObjectRequest watch = new JsonObjectRequest(Request.Method.POST, RequestSingleton.getUnregisterUrl(studentId), params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean result = response.getBoolean("result");
                    if (result){
                        Toast.makeText(getBaseContext(), "과목을 해제하셨습니다", Toast.LENGTH_SHORT).show();
                        int pos = lecturesList.indexOf(lecture);
                        registeredList.remove(lecture.id);
                        watchingList.remove(lecture.id);
                        ((RegisteredLecture)lectureListView.getAdapter()).removeItem(lecture);
                        f.successEnable();
                    } else {
                        f.failureEnable();
                    }
                } catch (JSONException e){
                    f.failureEnable();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-user-token", token);
                return headers;
            }
        };
        RequestSingleton.getInstance(this).addToRequestQueue(watch);
    }

    private void watching(final CompoundButton buttonView, final Lecture lecture){
        class Function {
            public void disable(){
                progressBar.setVisibility(View.VISIBLE);
                buttonView.setEnabled(false);
            }
            public void successEnable(){
                progressBar.setVisibility(View.GONE);
                buttonView.setEnabled(true);
            }
            public void failureEnable(){
                progressBar.setVisibility(View.GONE);
                buttonView.setEnabled(true);
                buttonView.setChecked(false);
            }
        }
        final Function f = new Function();
        f.disable();
        JSONObject params = new JSONObject();
        try {
            params.put("lecture_id", lecture.id);
        } catch (JSONException e){
            f.failureEnable();
        }
        JsonObjectRequest watch = new JsonObjectRequest(Request.Method.POST, RequestSingleton.getWatchUrl(studentId), params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean result = response.getBoolean("result");
                    if (result){
                        Toast.makeText(getBaseContext(), "감시에 등록하셨습니다", Toast.LENGTH_SHORT).show();
                        watchingList.add(lecture.id);
                        f.successEnable();
                    } else {
                        f.failureEnable();
                    }
                } catch (JSONException e){
                    f.failureEnable();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-user-token", token);
                return headers;
            }
        };
        RequestSingleton.getInstance(this).addToRequestQueue(watch);
    }

    private void unwatching(final CompoundButton buttonView, final Lecture lecture){
        class Function {
            public void disable(){
                progressBar.setVisibility(View.VISIBLE);
                buttonView.setEnabled(false);
            }
            public void successEnable(){
                progressBar.setVisibility(View.GONE);
                buttonView.setEnabled(true);
            }
            public void failureEnable(){
                progressBar.setVisibility(View.GONE);
                buttonView.setEnabled(true);
                buttonView.setChecked(true);
            }
        }
        final Function f = new Function();
        f.disable();
        JSONObject params = new JSONObject();
        try {
            params.put("lecture_id", lecture.id);
        } catch (JSONException e){
            f.failureEnable();
        }
        JsonObjectRequest unwatch = new JsonObjectRequest(Request.Method.POST, RequestSingleton.getUnwatchUrl(studentId), params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean result = response.getBoolean("result");
                    if (result){
                        Toast.makeText(getBaseContext(), "감시를 해제하셨습니다", Toast.LENGTH_SHORT).show();
                        watchingList.remove(lecture.id);
                        f.successEnable();
                    } else {
                        f.failureEnable();
                    }
                } catch (JSONException e){
                    f.failureEnable();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-user-token", token);
                return headers;
            }
        };
        RequestSingleton.getInstance(this).addToRequestQueue(unwatch);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
