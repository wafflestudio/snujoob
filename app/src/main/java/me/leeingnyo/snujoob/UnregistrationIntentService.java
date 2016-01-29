package me.leeingnyo.snujoob;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

public class UnregistrationIntentService extends IntentService {

    public UnregistrationIntentService() {
        super("UnregistrationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        InstanceID instanceID = InstanceID.getInstance(this);
        try {
            String default_senderId = getString(R.string.gcm_defaultSenderId);
            String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
            instanceID.deleteToken(default_senderId, scope);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
