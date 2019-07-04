package com.urrecliner.andriod.saynotitext;

import android.Manifest;
import android.content.Intent;
import android.icu.text.DecimalFormat;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.andriod.saynotitext.Vars.Booted;
import static com.urrecliner.andriod.saynotitext.Vars.kakaoIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.kakaoPersons;
import static com.urrecliner.andriod.saynotitext.Vars.mActivity;
import static com.urrecliner.andriod.saynotitext.Vars.mContext;
import static com.urrecliner.andriod.saynotitext.Vars.packageIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.packageTables;
import static com.urrecliner.andriod.saynotitext.Vars.prepareLists;
import static com.urrecliner.andriod.saynotitext.Vars.smsIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.systemIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.text2Speech;
import static com.urrecliner.andriod.saynotitext.Vars.utils;

public class MainActivity extends AppCompatActivity{

    private SeekBar mSeekBarPitch;
    private SeekBar mSeekBarSpeed;
    private TextView mPitchView;
    private TextView mSpeedView;
    private boolean inEditMode = false;
    private int nowTableId = 0;
    private String nowFileName;
    private View nowView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        utils = new Utils();
        mActivity = this;
        mContext = this;
        Log.w("onCreate","Started");

        if (PermissionProvider.isNotReady(getApplicationContext(), this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                PermissionProvider.isNotReady(getApplicationContext(), this,
                        Manifest.permission.READ_CONTACTS) ||
                PermissionProvider.isNotReady(getApplicationContext(), this,
                        Manifest.permission.RECEIVE_BOOT_COMPLETED) ||
                PermissionProvider.isNotReady(getApplicationContext(), this,
                        Manifest.permission.READ_PHONE_STATE)) {
            Log.e("Permission","NOT GRANTED");
            Toast.makeText(getApplicationContext(), "Check android permission",
                    Toast.LENGTH_LONG).show();
            finish();
            System.exit(0);
            android.os.Process.killProcess(android.os.Process.myPid());
        }

        if (!isNotificationAllowed()) {
            utils.customToast("안드로이드 알림에서 sayNotiText 를 허가해 주세요.", Toast.LENGTH_LONG);
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }
//
//        Button mButtonReload = findViewById(R.id.button_reload);

        mSeekBarPitch = findViewById(R.id.seek_bar_pitch);
        mSeekBarSpeed = findViewById(R.id.seek_bar_speed);

        mPitchView = findViewById(R.id.bar_pitch);
        mSpeedView = findViewById(R.id.bar_speed);

        utils.append2file("timestamp.txt", "initial load");
//        mButtonReload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                utils.log("hey","I started");
//                Intent updateIntent = new Intent(MainActivity.this, NotificationService.class);
//                updateIntent.putExtra("isUpdate", true);
//                startService(updateIntent);
//                nowView.setAlpha(1f);
//                prepareTable();
//            }
//        });
        setSeekBarPitch();
        setSeekBarSpeed();
        prepareLists = new PrepareLists();
        prepareTable();
        set_Save_Table();

        text2Speech = new Text2Speech();
        text2Speech.initiateTTS(getApplicationContext());
        text2Speech.setPitch((float) mSeekBarPitch.getProgress() / 50);
        text2Speech.setSpeed((float) mSeekBarSpeed.getProgress() / 50);

        utils.readyAudioManager(getApplicationContext());
//        utils.customToast("Initiated", Toast.LENGTH_SHORT);

        new Timer().schedule(new TimerTask() {
            public void run () {
                Intent updateIntent = new Intent(MainActivity.this, NotificationService.class);
                updateIntent.putExtra("isUpdate", true);
                startService(updateIntent);
            }
            }, 100);
        if (Booted != null) {
            Booted = null;
            Intent i = new Intent(mContext, MainActivity.class);
            i.addCategory("android.intent.category.HOME");
            i.setFlags(Intent.FLAG_FROM_BACKGROUND);
            startActivity(i);
        }
    }

    private void setSeekBarPitch() {

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

    private void setSeekBarSpeed() {

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
        utils.append2file("timestamp.txt", "prepare");
        prepareLists.read();
//        StringBuilder packageSaying = new StringBuilder("\n[ sayTable ]\n");
//        for (int i = 0; i < packageNames.length - 1; i++) {
//            if (packageTypes[i] != null) {
//                packageSaying.append(String.format("\n%s ; %s ; %s",
//                        packageTypes[i], packageShortNames[i], packageNames[i]));
//            }
//        }
        TextView tV = findViewById(R.id.text_table);
        tV.setText("");
//        text_table.setText(packageSaying.toString());

        Toast.makeText(getApplicationContext(),"Reading param files\n" +
                        "\npackageIgnores: " + packageIgnores.length + "\nkakaoIgnores: " + kakaoIgnores.length +
                        "\nkakaoPersons: " + kakaoPersons.length + "\nsmsIgnores: " + smsIgnores.length +
                        "\nsystemIgnores: " + systemIgnores.length
                ,Toast.LENGTH_SHORT).show();
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        text2Speech.shutdown();
//        utils.customToast("Now Purge the app",Toast.LENGTH_SHORT);
//        finish();
//        System.exit(0);
//        android.os.Process.killProcess(android.os.Process.myPid());
//    }

    void set_Save_Table() {
        Button bt = findViewById(R.id.button_save);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                utils.log("save","button");
                nowView.setAlpha(1f);
                if(write_textFile())
                    prepareTable();
            }
        });

    }
    public void edit_table(View v) {
        nowView = v;
        nowTableId = v.getId();
        v.setAlpha(0.5f);
        switch (nowTableId) {
            case R.id.btn_kakaoIgnores:
                show_for_edit(kakaoIgnores,"kakaoIgnores");
                break;
            case R.id.btn_kakaoPersons:
                show_for_edit(kakaoPersons,"kakaoPersons");
                break;
            case R.id.btn_packageIgnores:
                show_for_edit(packageIgnores,"packageIgnores");
                break;
            case R.id.btn_packageTables:
                show_for_edit(packageTables,"packageTables");
                break;
            case R.id.btn_smsIgnores:
                show_for_edit(smsIgnores,"smsIgnores");
                break;
            case R.id.btn_systemIgnores:
                show_for_edit(systemIgnores,"systemIgnores");
                break;
        }
    }

    void show_for_edit(String [] table, String fileName) {

        nowFileName = fileName;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < table.length; i++)
            sb.append(table[i] + "\n");
        String text = sb.toString()+"\n";
        TextView tv = findViewById(R.id.text_table);
        tv.setText(text);
        tv.setFocusable(true);
        tv.setEnabled(true);
        tv.setClickable(true);
        tv.setFocusableInTouchMode(true);
        tv = findViewById(R.id.button_save);
        tv.setVisibility(View.VISIBLE);
        text = "Save "+fileName;
        tv.setText(text);
    }

    boolean write_textFile() {
        TextView tv = findViewById(R.id.text_table);
        String outText = tv.getText().toString();
        try {
            // Assume default encoding.
            File targetFile = new File(Environment.getExternalStorageDirectory(), "sayNotiText/tables/" + nowFileName +".txt");
            FileWriter fileWriter = new FileWriter(targetFile, false);

            // Always wrap FileWriter in BufferedWriter.
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(outText);
//            bufferedWriter.newLine();
            // Always close files.
            bufferedWriter.close();
        }
        catch(IOException ex)
        {
            utils.log("editor","Error writing to file '" + nowFileName + "'\n"+ex.toString());
            return false;
        }
        tv.setClickable(false);
        tv.setFocusable(false);
        tv = findViewById(R.id.button_save);
        tv.setVisibility(View.GONE);
        return true;
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // to ignore back key
    }

}
