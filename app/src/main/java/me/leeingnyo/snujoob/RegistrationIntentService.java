package me.leeingnyo.snujoob;

import android.app.IntentService;
import android.content.Intent;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegistrationIntentService extends IntentService {

    public RegistrationIntentService() {
        super("RegistrationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        InstanceID instanceID = InstanceID.getInstance(this);
        String token = null;
        try {
            String default_senderId = getString(R.string.gcm_defaultSenderId);
            String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
            token = instanceID.getToken(default_senderId, scope, null);

            updateGcm(token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateGcm(String gcmToken){
        try {
            FileInputStream infoFile = openFileInput("information");
            StringBuilder fileContent = new StringBuilder("");
            byte[] buffer = new byte[1024];
            int n;
            while ((n = infoFile.read(buffer)) != -1) {
                fileContent.append(new String(buffer, 0, n));
            }
            JSONObject information = new JSONObject(fileContent.toString());
            final String studentId = information.getString("student_id");
            final String token = information.getString("token");
            JSONObject params = new JSONObject();
            params.put("student_id", studentId);
            params.put("gcm_token", gcmToken);
            JsonObjectRequest updateGcm = new JsonObjectRequest(Request.Method.POST, RequestSingleton.getUpdateGcmUrl(studentId), params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

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
            RequestSingleton.getInstance(this).addToRequestQueue(updateGcm);
        } catch (Exception e){

        }
    }
}