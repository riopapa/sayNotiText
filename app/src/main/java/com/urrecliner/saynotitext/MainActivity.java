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
import static com.urrecliner.saynotitext.Vars.sharePrefer;
import static com.urrecliner.saynotitext.Vars.text2Speech;
import static com.urrecliner.saynotitext.Vars.tvOldMessage;
import static com.urrecliner.saynotitext.Vars.tvOldScroll;
import static com.urrecliner.saynotitext.Vars.utils;

public class MainActivity extends AppCompatActivity {

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
        tvOldScroll = findViewById(R.id.oldScroll);

        new ReadOptionTables().read();
        prepare_Speech();

        new Timer().schedule(new TimerTask() {
            public void run () {
                Intent updateIntent = new Intent(MainActivity.this, NotificationService.class);
                updateIntent.putExtra("isUpdate", true);
                startService(updateIntent);
            }
        }, 100);

        tvOldMessage.post(() -> {
            int vTop = tvOldMessage.getTop();
            int vBottom = tvOldMessage.getBottom();
            int sHeight = tvOldScroll.getBottom();
            tvOldScroll.smoothScrollTo(((vTop + vBottom - sHeight) * 3 / 4), 0);
//            tvOldMessage.scrollTo(0, tvOldMessage.getBottom());
//            tvOldMessage.scrollTo(0, tvOldMessage.getBottom()/2);
        });
    }

    private void prepare_Speech() {
        int pitch = sharePrefer.getInt("pitch", 70);    // 70/50 = 1.4
        int speed = sharePrefer.getInt("speed", 70);
        utils.readyAudioManager(getApplicationContext());
        text2Speech = new Text2Speech();
        text2Speech.initiateTTS(getApplicationContext());
        text2Speech.setPitch((float) pitch / 50);
        text2Speech.setSpeed((float) speed / 50);
        utils.beepsInitiate();
        utils.beepOnce(0); utils.beepOnce(1);

    }

    String[] editTables = { "textIgnores", "kakaoIgnores",
                            "systemIgnores","packageIgnores",
                            "smsIgnores","packageTables" ,
                            "kakaoAlerts",};
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
