package com.urrecliner.saynotitext;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.DecimalFormat;
import android.os.Bundle;
import android.os.Environment;

import androidx.appcompat.app.ActionBar;
import androidx.core.app.NotificationManagerCompat;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.saynotitext.Vars.Booted;
import static com.urrecliner.saynotitext.Vars.kakaoIgnores;
import static com.urrecliner.saynotitext.Vars.kakaoPersons;
import static com.urrecliner.saynotitext.Vars.mContext;
import static com.urrecliner.saynotitext.Vars.packageIgnores;
import static com.urrecliner.saynotitext.Vars.packageTables;
import static com.urrecliner.saynotitext.Vars.readOptionTables;
import static com.urrecliner.saynotitext.Vars.sharePrefer;
import static com.urrecliner.saynotitext.Vars.smsIgnores;
import static com.urrecliner.saynotitext.Vars.systemIgnores;
import static com.urrecliner.saynotitext.Vars.text2Speech;
import static com.urrecliner.saynotitext.Vars.utils;

public class MainActivity extends AppCompatActivity{

    private SeekBar mSeekBarPitch;
    private SeekBar mSeekBarSpeed;
    private TextView mPitchView;
    private TextView mSpeedView;
    private String nowFileName;
    private String logID = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askPermission();
        utils = new Utils();
        mContext = this;
        utils.log(logID,"Started");

        if (!isNotificationAllowed()) {
            utils.customToast("안드로이드 알림에서 sayNotiText 를 허가해 주세요.");
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }
        Intent intent = getIntent();
        Booted = intent.hasExtra("boot");

        sharePrefer = getApplicationContext().getSharedPreferences("sayText", MODE_PRIVATE);
        ActionBar ab = getSupportActionBar() ;

        ab.setIcon(R.mipmap.app_icon) ;
        ab.setDisplayUseLogoEnabled(true) ;
        ab.setDisplayShowHomeEnabled(true) ;

        DecimalFormat df=new DecimalFormat("0.0");
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
        prepareLists = new PrepareLists();
        prepareTable();
        set_Save_Table();
        text2Speech = new Text2Speech();
        text2Speech.initiateTTS(getApplicationContext());
        text2Speech.setPitch((float) mSeekBarPitch.getProgress() / 50);
        text2Speech.setSpeed((float) mSeekBarSpeed.getProgress() / 50);

        utils.readyAudioManager(getApplicationContext());

//        if (Booted) {
//            Booted = false;
//            Intent i = new Intent(mContext, MainActivity.class);
//            i.addCategory("android.intent.category.HOME");
//            i.setFlags(Intent.FLAG_FROM_BACKGROUND);
//            startActivity(i);
//            finish();
//        }
//        else {
            new Timer().schedule(new TimerTask() {
                public void run () {
                    Intent updateIntent = new Intent(MainActivity.this, NotificationService.class);
                    updateIntent.putExtra("isUpdate", true);
                    startService(updateIntent);
                }
            }, 100);
//        }
        clearTableColor();
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
//        utils.log(logID, "prepared");
        prepareLists.read();
        TextView tV = findViewById(R.id.text_table);
        tV.setText("");
        Toast.makeText(getApplicationContext(),"Reading param files\n" + "\npackageTables: " + packageTables.length +
                        "\npackageIgnores: " + packageIgnores.length + "\nkakaoIgnores: " + kakaoIgnores.length +
                        "\nkakaoPersons: " + kakaoPersons.length + "\nsmsIgnores: " + smsIgnores.length +
                        "\nsystemIgnores: " + systemIgnores.length
                ,Toast.LENGTH_LONG).show();
    }

    void set_Save_Table() {
        Button bt = findViewById(R.id.button_save);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                utils.log(logID,"button");
                clearTableColor();
                if(write_textFile())
                    prepareTable();
            }
        });
    }
    void clearTableColor() {
        TextView [] tvs = {findViewById(R.id.btn_kakaoIgnores), findViewById(R.id.btn_kakaoPersons),
                findViewById(R.id.btn_packageIgnores), findViewById(R.id.btn_packageTables),
                findViewById(R.id.btn_smsIgnores), findViewById(R.id.btn_systemIgnores)};
        for (int i=0; i < 6; i++) {
            tvs[i].setTextColor(Color.BLACK);
        }
    }
    public void edit_table(View v) {
        int nowTableId;
        nowTableId = v.getId();
        clearTableColor();
        TextView tv = (TextView) v;
        tv.setTextColor(Color.CYAN);
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
        for (String s : table) sb.append(s).append("\n");
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
        catch(IOException ex) {
            utils.logE("editor",nowFileName + "'\n"+ex.toString());
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
            else
                Toast.makeText(mContext, "Permissions not granted.", Toast.LENGTH_LONG).show();
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
