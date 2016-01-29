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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    LinearLayout progressBar;
    EditText queryEditText;
    Button searchButton;
    ImageView adImageView;
    RecyclerView lectureListView;

    String studentId;
    String token;
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

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        lectureListView.setHasFixedSize(true);
        lectureListView.setLayoutManager(layoutManager);
        registeredList = new ArrayList<>();
        lecturesList = new ArrayList<>();
        queryEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                search();
                return true;
            }
        });
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });

        Intent intent = getIntent();
        studentId = intent.getStringExtra("student_id");
        token = intent.getStringExtra("token");
        queryEditText.setText(intent.getStringExtra("query"));
        registeredList = new ArrayList<>(intent.getIntegerArrayListExtra("registered_list"));
        search();
    }

    public void search(){
        String query = queryEditText.getText().toString();
        String queryEncoded;
        if (query.length() < 2){
            Toast.makeText(getBaseContext(), "2글자 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            queryEncoded = URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(getBaseContext(), "이상이 생겼습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        makeControlsDisabled();
        JsonObjectRequest search = new JsonObjectRequest(Request.Method.GET, RequestSingleton.getSearchUrl() + "?query=" + queryEncoded, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray lectures = response.getJSONArray("lectures");
                    showSearchedLectures(lectures);
                } catch (JSONException e) {
                    Toast.makeText(getBaseContext(), "검색에 실패했습니다. 인터넷을 확인해주세요.", Toast.LENGTH_SHORT).show();
                    makeControlsEnabled();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getBaseContext(), "검색에 실패했습니다. 인터넷을 확인해주세요.", Toast.LENGTH_SHORT).show();
                makeControlsEnabled();
            }
        });
        RequestSingleton.getInstance(this).addToRequestQueue(search);
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

    private void showSearchedLectures(JSONArray lectures){
        lecturesList = new ArrayList<>();
        try {
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
            }
            if (lecturesList.size() == 0){
                Toast.makeText(getBaseContext(), "검색결과가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e){
            Toast.makeText(getBaseContext(), "검색에 실패했습니다. 인터넷을 확인해주세요.", Toast.LENGTH_SHORT).show();
        }
        lectureListView.setAdapter(new SearchLecture(getApplicationContext(), lecturesList, R.layout.item_search_lecture));
        makeControlsEnabled();
    }

    public class SearchLecture extends RecyclerView.Adapter<SearchLecture.ViewHolder> {

        Context context;
        List<Lecture> lectures;
        int item_registered_lecture;

        public SearchLecture(Context context, List<Lecture> lectures, int item_registered_lecture) {
            this.context = context;
            this.lectures = lectures;
            this.item_registered_lecture = item_registered_lecture;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v= LayoutInflater.from(parent.getContext()).inflate(item_registered_lecture, null);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
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

            if (registeredList.contains(lecture.id)){
                holder.register.setVisibility(View.GONE);
                holder.unregister.setVisibility(View.VISIBLE);
            } else {
                holder.register.setVisibility(View.VISIBLE);
                holder.unregister.setVisibility(View.GONE);
            }
            holder.register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    register(holder.register, holder.unregister, lecture);
                }
            });
            holder.unregister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    unregister(holder.register, holder.unregister, lecture);
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
            }
        }

    }

    private void register(final Button registerButton, final Button unregisterButton, final Lecture lecture){
        class Function {
            public void disable(){
                progressBar.setVisibility(View.VISIBLE);
                registerButton.setEnabled(false);
            }
            public void successEnable(){
                progressBar.setVisibility(View.GONE);
                registerButton.setEnabled(true);
                registerButton.setVisibility(View.GONE);
                unregisterButton.setVisibility(View.VISIBLE);
            }
            public void failureEnable(){
                progressBar.setVisibility(View.GONE);
                registerButton.setEnabled(true);
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
        JsonObjectRequest register = new JsonObjectRequest(Request.Method.POST, RequestSingleton.getRegisterUrl(studentId), params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean result = response.getBoolean("result");
                    if (result){
                        Toast.makeText(getBaseContext(), lecture + " 을(를) 등록하셨습니다", Toast.LENGTH_SHORT).show();
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
                f.failureEnable();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-user-token", token);
                return headers;
            }
        };
        RequestSingleton.getInstance(this).addToRequestQueue(register);
    }

    private void unregister(final Button registerButton, final Button unregisterButton, final Lecture lecture){
        class Function {
            public void disable(){
                progressBar.setVisibility(View.VISIBLE);
                unregisterButton.setEnabled(false);
            }
            public void successEnable(){
                progressBar.setVisibility(View.GONE);
                unregisterButton.setEnabled(true);
                unregisterButton.setVisibility(View.GONE);
                registerButton.setVisibility(View.VISIBLE);
            }
            public void failureEnable(){
                progressBar.setVisibility(View.GONE);
                unregisterButton.setEnabled(true);
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
        JsonObjectRequest unregister = new JsonObjectRequest(Request.Method.POST, RequestSingleton.getUnregisterUrl(studentId), params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean result = response.getBoolean("result");
                    if (result){
                        Toast.makeText(getBaseContext(), lecture + " 을(를) 해제하셨습니다", Toast.LENGTH_SHORT).show();
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
                f.failureEnable();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-user-token", token);
                return headers;
            }
        };
        RequestSingleton.getInstance(this).addToRequestQueue(unregister);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("query", queryEditText.getText().toString());
        setResult(0, intent);
        super.onBackPressed();
    }
}
