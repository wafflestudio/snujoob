package me.leeingnyo.snujoob;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONObject;

import java.io.FileInputStream;

public class MyGcmListenerService extends GcmListenerService {

    public static boolean isVibrate;

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String title = data.getString("title");
        String message = data.getString("message");
        Integer lectureId = Integer.parseInt(data.getString("lecture_id"));

        sendNotification(title, message, lectureId);
    }

    private void sendNotification(String title, String message, Integer rectureId) {
        String url = "http://sugang.snu.ac.kr/sugang/co/co010.action";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        try {
            FileInputStream settingFile = openFileInput("setting");
            StringBuilder fileContent = new StringBuilder("");
            byte[] buffer = new byte[1024];
            int n;
            while ((n = settingFile.read(buffer)) != -1) {
                fileContent.append(new String(buffer, 0, n));
            }
            JSONObject setting = new JSONObject(fileContent.toString());
            isVibrate = setting.getBoolean("is_vibrate");
        } catch (Exception e){
            isVibrate = true;
        }

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent);
        if (isVibrate){
            notificationBuilder.setVibrate(new long [] {0, 100, 200, 300, 500});
        } else {
            notificationBuilder.setVibrate(new long [] {0, 0});
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(rectureId, notificationBuilder.build());
    }
}
