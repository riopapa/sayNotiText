package com.urrecliner.saynotitext;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.saynotitext.Vars.mActivity;
import static com.urrecliner.saynotitext.Vars.mContext;
import static com.urrecliner.saynotitext.Vars.nowFileName;
import static com.urrecliner.saynotitext.Vars.oldMessage;
import static com.urrecliner.saynotitext.Vars.readOptionTables;
import static com.urrecliner.saynotitext.Vars.sharePrefer;
import static com.urrecliner.saynotitext.Vars.text2Speech;
import static com.urrecliner.saynotitext.Vars.tvOldMessage;
import static com.urrecliner.saynotitext.Vars.utils;

public class MainActivity extends AppCompatActivity {

//    private SeekBar mSeekBarPitch;
//    private SeekBar mSeekBarSpeed;
//    private TextView mPitchView;
//    private TextView mSpeedView;
    private ImageView ivErase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_PERMISSIONS);
            Permission.ask(this, this, info);
        } catch (Exception e) {
            Log.e("Permission", "No Permission "+e.toString());
        }

        utils = new Utils();
        mContext = this;
        mActivity = this;
        utils.log("Main","Started");

        if (!isNotificationAllowed()) {
            utils.customToast("Allow permission on Android notification");
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }
        text2Speech = new Text2Speech();
        text2Speech.initiateTTS(getApplicationContext());

        sharePrefer = getApplicationContext().getSharedPreferences("sayText", MODE_PRIVATE);

        ActionBar ab = getSupportActionBar() ;
        assert ab != null;
        ab.setIcon(R.mipmap.icon_launcher);
        ab.setDisplayUseLogoEnabled(true);
        ab.setDisplayShowHomeEnabled(true);

        tvOldMessage = findViewById(R.id.oldMessage);
        tvOldMessage.setText(oldMessage);
        tvOldMessage.setMovementMethod(new ScrollingMovementMethod());
//        DecimalFormat df = new DecimalFormat("0.0");
//        int pitch = sharePrefer.getInt("pitch", 70);
//        mSeekBarPitch = findViewById(R.id.seek_bar_pitch);
//        mSeekBarPitch.setProgress(pitch);
//        mPitchView = findViewById(R.id.bar_pitch);
//        mPitchView.setText(df.format((float) pitch / 50));
//        setSeekBarPitch();
//
//        mSpeedView = findViewById(R.id.bar_speed);
//        int speed = sharePrefer.getInt("speed", 70);
//        mSeekBarSpeed = findViewById(R.id.seek_bar_speed);
//        mSeekBarSpeed.setProgress(speed);
//        mSpeedView = findViewById(R.id.bar_speed);
//        mSpeedView.setText(df.format((float) speed / 50));
//        setSeekBarSpeed();

        prepareTable();
        prepare_Speech();

        new Timer().schedule(new TimerTask() {
            public void run () {
                Intent updateIntent = new Intent(MainActivity.this, NotificationService.class);
                updateIntent.putExtra("isUpdate", true);
                startService(updateIntent);
            }
        }, 100);
    }

    private void prepare_Speech() {
        int pitch = sharePrefer.getInt("pitch", 70);
        int speed = sharePrefer.getInt("speed", 70);
        utils.readyAudioManager(getApplicationContext());
        text2Speech = new Text2Speech();
        text2Speech.initiateTTS(getApplicationContext());
        text2Speech.setPitch((float) pitch / 50);
        text2Speech.setSpeed((float) speed / 50);
        utils.beepsInitiate();
        utils.beepOnce(0); utils.beepOnce(1);

    }

//    private void setSeekBarPitch() {
//        mSeekBarPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                float mPitch = (float) progress / 50;
//                DecimalFormat df=new DecimalFormat("0.0");
//                mPitchView.setText(df.format(mPitch));
//                text2Speech.setPitch(mPitch);
//            }
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                int pitch = mSeekBarPitch.getProgress();
//                float mPitch = (float) pitch / 50;
//                DecimalFormat df=new DecimalFormat("0.0");
//                mPitchView.setText(df.format(mPitch));
//                text2Speech.setPitch(mPitch);
//                SharedPreferences.Editor editor = sharePrefer.edit();
//                editor.putInt("pitch", pitch);
//                editor.apply();
//                editor.commit();
//            }
//        });
//    }
//
//    private void setSeekBarSpeed() {
//        mSeekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                float mSpeed = (float) progress / 50;
//                DecimalFormat df=new DecimalFormat("0.0");
//                mSpeedView.setText(df.format(mSpeed));
//                text2Speech.setSpeed(mSpeed);
//            }
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//            }
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                int speed = mSeekBarSpeed.getProgress();
//                float mSpeed = (float) speed / 50;
//                DecimalFormat df=new DecimalFormat("0.0");
//                mSpeedView.setText(df.format(mSpeed));
//                text2Speech.setSpeed(mSpeed);
//                SharedPreferences.Editor editor = sharePrefer.edit();
//                editor.putInt("speed", speed);
//                editor.apply();
//                editor.commit();
//            }
//        });
//    }

    private void prepareTable() {
        Toast.makeText(getApplicationContext(),"loading tables",Toast.LENGTH_SHORT).show();
        readOptionTables = new ReadOptionTables();
        readOptionTables.read();
    }

    String[] editTables = { "textIgnores", "kakaoIgnores",
                            "kakaoPersons", "kakaoAlerts",
                            "packageIgnores","packageTables",
                            "smsIgnores", "systemIgnores"};
    public void edit_table(View v) {
        int tag = Integer.parseInt(v.getTag().toString());
        nowFileName = editTables[tag];
        Intent intent = new Intent(MainActivity.this, EditActivity.class);
        startActivity(intent);
    }

    private boolean isNotificationAllowed() {
        Set<String> listenerSet = NotificationManagerCompat.getEnabledListenerPackages(this);
        String myPackageName = getPackageName();

        for(String packageName : listenerSet) {
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
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_clear) {
            oldMessage = "";
            tvOldMessage.setText(oldMessage);
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // to ignore back key
    }

}
