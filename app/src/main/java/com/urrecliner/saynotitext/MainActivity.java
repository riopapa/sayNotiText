package com.urrecliner.saynotitext;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.icu.text.DecimalFormat;
import android.os.Bundle;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static android.telephony.TelephonyManager.CALL_STATE_RINGING;
import static com.urrecliner.saynotitext.Vars.mContext;
import static com.urrecliner.saynotitext.Vars.nowFileName;
import static com.urrecliner.saynotitext.Vars.isPhoneBusy;
import static com.urrecliner.saynotitext.Vars.readOptionTables;
import static com.urrecliner.saynotitext.Vars.sharePrefer;
import static com.urrecliner.saynotitext.Vars.text2Speech;
import static com.urrecliner.saynotitext.Vars.utils;

public class MainActivity extends AppCompatActivity {

    private SeekBar mSeekBarPitch;
    private SeekBar mSeekBarSpeed;
    private TextView mPitchView;
    private TextView mSpeedView;
    TelephonyManager phoneManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askPermission();
        utils = new Utils();
        mContext = this;
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

        DecimalFormat df = new DecimalFormat("0.0");
        int pitch = sharePrefer.getInt("pitch", 70);
        mSeekBarPitch = findViewById(R.id.seek_bar_pitch);
        mSeekBarPitch.setProgress(pitch);
        mPitchView = findViewById(R.id.bar_pitch);
        mPitchView.setText(df.format((float) pitch / 50));
        setSeekBarPitch();

        mSpeedView = findViewById(R.id.bar_speed);
        int speed = sharePrefer.getInt("speed", 70);
        mSeekBarSpeed = findViewById(R.id.seek_bar_speed);
        mSeekBarSpeed.setProgress(speed);
        mSpeedView = findViewById(R.id.bar_speed);
        mSpeedView.setText(df.format((float) speed / 50));
        setSeekBarSpeed();
        readOptionTables = new ReadOptionTables();
        prepareTable();
        utils.readyAudioManager(getApplicationContext());
        text2Speech = new Text2Speech();
        text2Speech.initiateTTS(getApplicationContext());
        text2Speech.setPitch((float) mSeekBarPitch.getProgress() / 50);
        text2Speech.setSpeed((float) mSeekBarSpeed.getProgress() / 50);

        new Timer().schedule(new TimerTask() {
            public void run () {
                Intent updateIntent = new Intent(MainActivity.this, NotificationService.class);
                updateIntent.putExtra("isUpdate", true);
                startService(updateIntent);
            }
        }, 100);
        ready_Telephony();
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
                int pitch = mSeekBarPitch.getProgress();
                float mPitch = (float) pitch / 50;
                DecimalFormat df=new DecimalFormat("0.0");
                mPitchView.setText(df.format(mPitch));
                text2Speech.setPitch(mPitch);
                SharedPreferences.Editor editor = sharePrefer.edit();
                editor.putInt("pitch", pitch);
                editor.apply();
                editor.commit();
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
                int speed = mSeekBarSpeed.getProgress();
                float mSpeed = (float) speed / 50;
                DecimalFormat df=new DecimalFormat("0.0");
                mSpeedView.setText(df.format(mSpeed));
                text2Speech.setSpeed(mSpeed);
                SharedPreferences.Editor editor = sharePrefer.edit();
                editor.putInt("speed", speed);
                editor.apply();
                editor.commit();
            }
        });
    }

    private void prepareTable() {
        Toast.makeText(getApplicationContext(),"loading tables",Toast.LENGTH_SHORT).show();
        readOptionTables.read();
    }

    void ready_Telephony() {
        phoneManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    String savedNumber = "x";
    public PhoneStateListener phoneStateListener = new PhoneStateListener()
    {
        public void onCallStateChanged(int state, String callNumber)
        {

            switch (state) {
                case CALL_STATE_RINGING:
                    isPhoneBusy = true;
                    Toast.makeText(mContext, "\nRINGING\n\nRINGING\n", Toast.LENGTH_LONG).show();
                    utils.log("^phone^ "+callNumber,"RINGING RINGING ["+callNumber+"]");
                    if (callNumber.length()> 2)
                        savedNumber = callNumber;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    isPhoneBusy = true;
                    Toast.makeText(mContext, "\nOFFHOOK\n\nOFFHOOK\n", Toast.LENGTH_LONG).show();
                    utils.log("^phone^ ["+callNumber+"]"," OFFHOOK OFFHOOK ["+callNumber+"]");
                    if (callNumber.length()> 2)
                        savedNumber = callNumber;
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    Toast.makeText(mContext, "CALL IDLE", Toast.LENGTH_LONG).show();
                    if (callNumber.equals(savedNumber)) {
                        savedNumber = "x";
                        isPhoneBusy = false;
                    }
                    utils.log("^^phone^ "+isPhoneBusy,isPhoneBusy+" Phone IDLE ["+callNumber+"]");
                    break;
                default:
                    utils.log("^^phone^ ["+callNumber+"]","--------///----- Phone STATE UNKNOWN ------------"+state);
                    text2Speech.speak("폰 상태 변화 : 상태 모름 모름 "+state);
                    isPhoneBusy = false;
            }

            super.onCallStateChanged(state, callNumber);
        }
    };

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
    public void onBackPressed() {
        super.onBackPressed();
        // to ignore back key
    }

    // ↓ ↓ ↓ P E R M I S S I O N   RELATED /////// ↓ ↓ ↓ ↓  with no lambda
    private final static int ALL_PERMISSIONS_RESULT = 101;
    ArrayList permissionsToRequest;
    ArrayList<String> permissionsRejected = new ArrayList<>();
    String [] permissions;

    private void askPermission() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_PERMISSIONS);
            permissions = info.requestedPermissions;//This array contain
        } catch (Exception e) {
            Log.e("Permission", "No Permission "+e.toString());
        }

        permissionsToRequest = findUnAskedPermissions();
        if (permissionsToRequest.size() != 0) {
            requestPermissions((String[]) permissionsToRequest.toArray(new String[0]),
//            requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                    ALL_PERMISSIONS_RESULT);
        }
    }

    private ArrayList findUnAskedPermissions() {
        ArrayList <String> result = new ArrayList<String>();
        for (String perm : permissions) if (hasPermission(perm)) result.add(perm);
        return result;
    }
    private boolean hasPermission(String permission) {
        return (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == ALL_PERMISSIONS_RESULT) {
            for (Object perms : permissionsToRequest) {
                if (hasPermission((String) perms)) {
                    permissionsRejected.add((String) perms);
                }
            }
            if (permissionsRejected.size() > 0) {
                if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                    String msg = "These permissions are mandatory for the application. Please allow access.";
                    showDialog(msg);
                }
            }
        }
    }
    private void showDialog(String msg) {
        showMessageOKCancel(msg,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.requestPermissions(permissionsRejected.toArray(
                                new String[0]), ALL_PERMISSIONS_RESULT);
                    }
                });
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

// ↑ ↑ ↑ ↑ P E R M I S S I O N    RELATED /////// ↑ ↑ ↑

}
