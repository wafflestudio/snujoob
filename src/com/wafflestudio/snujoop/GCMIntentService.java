package com.wafflestudio.snujoop;

import java.util.HashMap;
import java.util.Iterator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
    private static final String tag = "GCMIntentService";
    private static final String PROJECT_ID = "801697427023";
   
    private Notification mNoti;
    private NotificationManager mNM;
    
    //public �⺻ �����ڸ� ������ ������ �Ѵ�.
    public GCMIntentService(){ this(PROJECT_ID); }
   
    public GCMIntentService(String project_id) { super(project_id); }
 
    /** Ǫ�÷� ���� �޽��� */
    @Override
    protected void onMessage(Context context, Intent intent) {
        Bundle b = intent.getExtras();

        HashMap<String, String> data = new HashMap<String, String>();
        Iterator<String> iterator = b.keySet().iterator();
        while(iterator.hasNext()) {
            String key = iterator.next();
            String value = b.get(key).toString();
            Log.d(tag, "onMessage. "+key+" : "+value);
            data.put(key, value);
        }
        
        Intent mainActivity = new Intent(this, MainActivity.class);
        mainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, mainActivity, 0);
        
        mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNoti = new NotificationCompat.Builder(getApplicationContext())
        		.setContentTitle("SNUJoop")
        		.setContentText(data.get("subject_name") + " 가 비었습니다.")
        		.setSmallIcon(R.drawable.ic_launcher)
        		.setTicker("등록한 과목에서 자리가 났습니다.")
        		.setAutoCancel(true)
        		.setOngoing(false)
        		.setVibrate(new long[] { 0, 100, 200, 300, 500 })
        		.setContentIntent(pi)
        		.build();
        mNM.notify(Integer.parseInt(data.get("id")), mNoti);
    }

    /**���� �߻���*/
    @Override
    protected void onError(Context context, String errorId) {
        Log.d(tag, "onError. errorId : "+errorId);
    }
 
    /**�ܸ����� GCM ���� ��� ���� �� ��� id�� �޴´�*/
    @Override
    protected void onRegistered(Context context, String regId) {
        Log.d(tag, "onRegistered. regId : "+regId);
    }

    /**�ܸ����� GCM ���� ��� ������ �ϸ� ������ ��� id�� �޴´�*/
    @Override
    protected void onUnregistered(Context context, String regId) {
        Log.d(tag, "onUnregistered. regId : "+regId);
    }
}