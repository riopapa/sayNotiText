package com.urrecliner.andriod.saynotitext;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.icu.text.DecimalFormat;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import static com.urrecliner.andriod.saynotitext.Vars.Tts;
import static com.urrecliner.andriod.saynotitext.Vars.act;
import static com.urrecliner.andriod.saynotitext.Vars.mPrepareLists;
import static com.urrecliner.andriod.saynotitext.Vars.kakaoXcludes;
import static com.urrecliner.andriod.saynotitext.Vars.kakaoPersons;
import static com.urrecliner.andriod.saynotitext.Vars.mAudioManager;
import static com.urrecliner.andriod.saynotitext.Vars.mFocusGain;
import static com.urrecliner.andriod.saynotitext.Vars.packageCodes;
import static com.urrecliner.andriod.saynotitext.Vars.packageNames;
import static com.urrecliner.andriod.saynotitext.Vars.packageTypes;
import static com.urrecliner.andriod.saynotitext.Vars.packageXcludes;
import static com.urrecliner.andriod.saynotitext.Vars.smsXcludes;

public class MainActivity extends AppCompatActivity{

    private SeekBar mSeekBarPitch;
    private SeekBar mSeekBarSpeed;
    private TextView mPitchView;
    private TextView mSpeedView;

    private final static int MY_PERMISSIONS_WRITE_FILE = 101;
    private final static int MY_PERMISSIONS_CONTACTS = 103;
    private final static int MY_PERMISSIONS_PHONE = 104;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (act == null) act = this;
        if (Tts == null) {
            Tts = new Text2Speech();
            Tts.initiateTTS();
        }

        if (PermissionProvider.isPermitted(getApplicationContext(), this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, MY_PERMISSIONS_WRITE_FILE) == 0 ||
           PermissionProvider.isPermitted(getApplicationContext(), this,
                Manifest.permission.READ_CONTACTS, MY_PERMISSIONS_CONTACTS) == 0 ||
           PermissionProvider.isPermitted(getApplicationContext(), this,
                        Manifest.permission.READ_PHONE_STATE, MY_PERMISSIONS_PHONE) == 0 ) {
            Toast.makeText(getApplicationContext(),"Check android permission",
                    Toast.LENGTH_LONG).show();
            finish();
            System.exit(0);
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        boolean isPermissionAllowed = isNotificationAllowed();

        if(!isPermissionAllowed) {
            Toast.makeText(getApplicationContext(),"안드로이드 알림에서 sayNotiText 를 허가해 주세요.",
                    Toast.LENGTH_LONG).show();
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }

        Button mButtonReload = findViewById(R.id.button_reload);

        mSeekBarPitch = findViewById(R.id.seek_bar_pitch);
        mSeekBarSpeed = findViewById(R.id.seek_bar_speed);

        mPitchView = findViewById(R.id.bar_pitch);
        mSpeedView = findViewById(R.id.bar_speed);

        Tts.setPitch((float) mSeekBarPitch.getProgress() / 50);
        Tts.setSpeed((float) mSeekBarSpeed.getProgress() / 50);

        mPrepareLists = new PrepareLists();

        mButtonReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareTable();
            }
        });

        mSeekBarPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float mPitch = (float) mSeekBarPitch.getProgress() / 50;
                if (mPitch > 1.0f) mPitch *= 1.2f;
                if (mPitch < 1.0f) mPitch /= 1.2f;
                if (mPitch < 0.1f) mPitch = 0.1f;
                DecimalFormat df=new DecimalFormat("0.0");
                mPitchView.setText(df.format(mPitch));
                Tts.setPitch(mPitch);
            }
        });

        Toast.makeText(getApplicationContext(),"sayNotiText Initiated", Toast.LENGTH_SHORT).show();

                mSeekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float mSpeed = (float) mSeekBarSpeed.getProgress() / 50;
                if (mSpeed > 1.0f) mSpeed *= 1.2f;
                if (mSpeed < 1.0f) mSpeed /= 1.2f;
                if (mSpeed < 0.1f) mSpeed = 0.1f;
                DecimalFormat df=new DecimalFormat("0.0");
                mSpeedView.setText(df.format(mSpeed));
                Tts.setSpeed(mSpeed);
            }
        });
        prepareTable();
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mFocusGain = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                    .build();
        }
//        long usedSize = -1L;
        try {
            long freeSize, totalSize;
            Runtime info = Runtime.getRuntime();
            freeSize = info.freeMemory();
            totalSize = info.totalMemory();
//            usedSize = totalSize - freeSize;
            Log.w("_main_",  "totalSZ = " + totalSize + ", freeSZ = " + freeSize +"  _x_");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void prepareTable() {
        mPrepareLists.read();
        StringBuilder packageSaying = new StringBuilder("\n[ sayTable ]\n");
        for (int i = 0; i < packageNames.length - 1; i++) {
            packageSaying.append(String.format("\n%s,%s : %s",
                    packageTypes[i], packageCodes[i], packageNames[i]));
        }
        TextView text_table = findViewById(R.id.text_table);
        text_table.setText(packageSaying.toString());

        Toast.makeText(getApplicationContext(),"Reading param files\n" +
                        "\npackageXcludes: " + packageXcludes.length + "\nkakaoXcludes: " + kakaoXcludes.length +
                        "\nkakaoPersons: " + kakaoPersons.length + "\nsmsXcludes: " + smsXcludes.length
                ,Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(),"Now Purge the app",Toast.LENGTH_LONG).show();
        finish();
//        System.exit(0);
//        android.os.Process.killProcess(android.os.Process.myPid());
    }
    private boolean isNotificationAllowed() {
        Set<String> notiListenerSet = NotificationManagerCompat.getEnabledListenerPackages(this);
        String myPackageName = getPackageName();

        for(String packageName : notiListenerSet) {
            if(packageName == null) {
                continue;
            }
            if(packageName.equals(myPackageName)) {
                return true;
            }
        }
        return false;
    }
}
