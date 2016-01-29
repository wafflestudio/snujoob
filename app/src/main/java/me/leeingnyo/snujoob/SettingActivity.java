package me.leeingnyo.snujoob;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class SettingActivity extends AppCompatActivity {

    CheckBox vibrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        vibrate = (CheckBox)findViewById(R.id.vibrate);

        vibrate.setChecked(MyGcmListenerService.isVibrate);
        vibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyGcmListenerService.isVibrate = isChecked;
                try {
                    FileOutputStream settingFile = openFileOutput("setting", MODE_PRIVATE);
                    JSONObject setting = new JSONObject();
                    setting.put("is_vibrate", MyGcmListenerService.isVibrate);
                    settingFile.write(setting.toString().getBytes());
                } catch (Exception e){

                }
            }
        });
    }
}
