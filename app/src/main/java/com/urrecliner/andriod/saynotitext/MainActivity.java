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
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import static com.urrecliner.andriod.saynotitext.Vars.kakaoPersons;
import static com.urrecliner.andriod.saynotitext.Vars.kakaoXcludes;
import static com.urrecliner.andriod.saynotitext.Vars.mActivity;
import static com.urrecliner.andriod.saynotitext.Vars.mAudioManager;
import static com.urrecliner.andriod.saynotitext.Vars.mContext;
import static com.urrecliner.andriod.saynotitext.Vars.mFocusGain;
import static com.urrecliner.andriod.saynotitext.Vars.mPrepareLists;
import static com.urrecliner.andriod.saynotitext.Vars.packageCodes;
import static com.urrecliner.andriod.saynotitext.Vars.packageNames;
import static com.urrecliner.andriod.saynotitext.Vars.packageTypes;
import static com.urrecliner.andriod.saynotitext.Vars.packageXcludes;
import static com.urrecliner.andriod.saynotitext.Vars.smsXcludes;
import static com.urrecliner.andriod.saynotitext.Vars.text2Speech;
import static com.urrecliner.andriod.saynotitext.Vars.utils;

public class MainActivity extends AppCompatActivity{

    private SeekBar mSeekBarPitch;
    private SeekBar mSeekBarSpeed;
    private TextView mPitchView;
    private TextView mSpeedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mActivity = this;
        mContext = this; // getApplicationContext();
//        if (text2Speech == null) {
//            utils.logE("text2Speech", "IS NULL");
//            text2Speech = new Text2Speech();
//        }

        if (PermissionProvider.isNotReady(getApplicationContext(), this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
           PermissionProvider.isNotReady(getApplicationContext(), this,
                Manifest.permission.READ_CONTACTS) ||
           PermissionProvider.isNotReady(getApplicationContext(), this,
                        Manifest.permission.READ_PHONE_STATE) ) {
            Toast.makeText(getApplicationContext(),"Check android permission",
                    Toast.LENGTH_LONG).show();
            finish();
            System.exit(0);
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        boolean isPermissionAllowed = isNotificationAllowed();

        if(!isPermissionAllowed) {
            utils.customToast("안드로이드 알림에서 sayNotiText 를 허가해 주세요.", Toast.LENGTH_LONG);
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }

        Button mButtonReload = findViewById(R.id.button_reload);

        text2Speech.initiateTTS(getApplicationContext());

        mSeekBarPitch = findViewById(R.id.seek_bar_pitch);
        mSeekBarSpeed = findViewById(R.id.seek_bar_speed);

        mPitchView = findViewById(R.id.bar_pitch);
        mSpeedView = findViewById(R.id.bar_speed);

        text2Speech.setPitch((float) mSeekBarPitch.getProgress() / 50);
        text2Speech.setSpeed((float) mSeekBarSpeed.getProgress() / 50);

//        mPrepareLists = new PrepareLists();

        mButtonReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareTable();
            }
        });
        setmSeekBarPitch();
        setmSeekBarSpeed();
        prepareTable();

        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mFocusGain = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .build();
        }
        utils.customToast("sayNotiText Initiated", Toast.LENGTH_SHORT);
    }

    private void setmSeekBarPitch() {

        mSeekBarPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float mPitch = (float) progress / 50;
                DecimalFormat df=new DecimalFormat("0.0");
                mPitchView.setText(df.format(mPitch));
                text2Speech.setPitch(mPitch);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float mPitch = (float) mSeekBarPitch.getProgress() / 50;
                DecimalFormat df=new DecimalFormat("0.0");
                mPitchView.setText(df.format(mPitch));
                text2Speech.setPitch(mPitch);
            }
        });

    }
    private void setmSeekBarSpeed() {

        mSeekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float mSpeed = (float) progress / 50;
                DecimalFormat df=new DecimalFormat("0.0");
                mSpeedView.setText(df.format(mSpeed));
                text2Speech.setSpeed(mSpeed);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float mSpeed = (float) mSeekBarSpeed.getProgress() / 50;
                DecimalFormat df=new DecimalFormat("0.0");
                mSpeedView.setText(df.format(mSpeed));
                text2Speech.setSpeed(mSpeed);
            }
        });
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
        text2Speech.shutdown();
        utils.customToast("Now Purge the app",Toast.LENGTH_SHORT);
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
