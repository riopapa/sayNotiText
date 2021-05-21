package com.urrecliner.saynotitext;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.urrecliner.saynotitext.Vars.speakOnOff;
import static com.urrecliner.saynotitext.Vars.text2Speech;
import static com.urrecliner.saynotitext.Vars.utils;

public class NotificationService extends Service {

    private Context mContext;
    NotificationCompat.Builder mBuilder = null;
    NotificationChannel mNotificationChannel = null;
    NotificationManager mNotificationManager;
    private RemoteViews mRemoteViews;
    private static final int STOP_SAY = 10011;
    private static final int SPEAK_ON_OFF = 1003;
    private static final int SHOW_MESSAGE = 1234;
    String msgText;

    @Override
    public void onCreate() {
//        Log.w("Noti SVC","Started");
        super.onCreate();
        mContext = this;
        if (null != mRemoteViews) {
            mRemoteViews.removeAllViews(R.layout.notification_bar);
            mRemoteViews = null;
        }
        mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification_bar);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (utils == null) utils = new Utils();
        msgText = null;
        int operation;
        try {
            operation = intent.getIntExtra("operation", -1);
        } catch (Exception e) {
            utils.logE("operation",e.toString());
            operation = -1;
        }
        createNotification();
//        boolean isUpdate = intent.getBooleanExtra("isUpdate", false);
//        if (isUpdate) {
//            updateRemoteViews();
//            startForeground(100, mBuilder.build());
//            return START_STICKY;
//        }
        switch (operation) {
            case STOP_SAY:
                text2Speech.ttsStop();
                break;
            case SPEAK_ON_OFF:
                speakOnOff = !speakOnOff;
                Log.w("sayMessage","is "+ speakOnOff);
//                mBuilder.setSmallIcon(R.mipmap.icon_launcher);
//                mRemoteViews.setImageViewResource(R.id.popCast_OnOff, (isPopCastOn)? R.mipmap.popcast_off :R.mipmap.popcast_on);
                break;
            case SHOW_MESSAGE:
                msgText = intent.getStringExtra("msg");
                break;
            default:
                break;
        }
//        startForeground(100, mBuilder.build());
        updateRemoteViews();
        return START_STICKY;
    }

    private void createNotification() {

        if (null == mNotificationChannel) {
                mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotificationChannel = new NotificationChannel("default","default", NotificationManager.IMPORTANCE_DEFAULT);
                mNotificationManager.createNotificationChannel(mNotificationChannel);
        }
        if (null == mBuilder) {
            mBuilder = new NotificationCompat.Builder(mContext,"default")
                    .setSmallIcon(R.mipmap.icon_launcher)
                    .setContent(mRemoteViews)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(false)
                    .setOngoing(true);
        }

        Intent mainIntent = new Intent(mContext, MainActivity.class);
        mRemoteViews.setOnClickPendingIntent(R.id.ll_customNotification, PendingIntent.getActivity(mContext, 0, mainIntent, 0));

        Intent stopSayIntent = new Intent(this, NotificationService.class);
        stopSayIntent.putExtra("operation", STOP_SAY);
//        stopSayIntent.putExtra("isFromNotification", true);
        PendingIntent stopSayPi = PendingIntent.getService(mContext, 2, stopSayIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(stopSayPi);
        mRemoteViews.setOnClickPendingIntent(R.id.Stop_Now, stopSayPi);

        Intent popIntent = new Intent(this, NotificationService.class);
        popIntent.putExtra("operation", SPEAK_ON_OFF);
        PendingIntent popPI = PendingIntent.getService(mContext, 3, popIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        mRemoteViews.setImageViewResource(R.id.popCast_OnOff, (isPopCastOn)? R.mipmap.popcast_off :R.mipmap.popcast_on);
        mBuilder.setContentIntent(popPI);
        mRemoteViews.setOnClickPendingIntent(R.id.Speak, popPI);
//        utils.log("speak on off",""+sayMessage);
    }

    private void updateRemoteViews() {
        mRemoteViews.setImageViewResource(R.id.Speak, (speakOnOff) ? R.mipmap.speak_on: R.mipmap.speak_off);
//        mRemoteViews.setImageViewResource(R.id.Stop_Now, R.mipmap.mute_right_now);
        if (msgText == null) {
            mRemoteViews.setViewVisibility(R.id.msgLine, View.GONE);
        } else {
            mRemoteViews.setViewVisibility(R.id.msgLine, View.VISIBLE);
            String s = new SimpleDateFormat("HH:mm", Locale.KOREA).format(new Date());
            mRemoteViews.setTextViewText(R.id.msgTime, s);
            mRemoteViews.setTextViewText(R.id.msgText, msgText);
        }
        mNotificationManager.notify(100, mBuilder.build());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
