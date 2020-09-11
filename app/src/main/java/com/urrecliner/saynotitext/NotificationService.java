package com.urrecliner.saynotitext;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.widget.RemoteViews;

import static com.urrecliner.saynotitext.Vars.readOptionTables;
import static com.urrecliner.saynotitext.Vars.text2Speech;

public class NotificationService extends Service {

    private Context mContext;
    NotificationCompat.Builder mBuilder = null;
    NotificationChannel mNotificationChannel = null;
    NotificationManager mNotificationManager;
    private RemoteViews mRemoteViews;
    private static final int STOP_SAY = 10011;
    private static final int RE_LOAD = 10022;

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
        int operation = intent.getIntExtra("operation", -1);
        boolean isUpdate = intent.getBooleanExtra("isUpdate", false);
        createNotification();
        if (isUpdate) {
            updateRemoteViews();
            startForeground(100, mBuilder.build());
            return START_STICKY;
        }

        switch (operation) {
            case STOP_SAY:
                text2Speech.ttsStop();
                break;
            case RE_LOAD:
                readOptionTables.read();
                break;
            default:
                break;
        }
        startForeground(100, mBuilder.build());
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
                    .setSmallIcon(R.mipmap.app_icon_mini)  // for notification bar
                    .setContent(mRemoteViews)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(false)
                    .setOngoing(true);
        }

        Intent mainIntent = new Intent(mContext, MainActivity.class);
        mRemoteViews.setOnClickPendingIntent(R.id.ll_customNotification, PendingIntent.getActivity(mContext, 0, mainIntent, 0));

        Intent stopSayIntent = new Intent(this, NotificationService.class);
        stopSayIntent.putExtra("operation", STOP_SAY);
        stopSayIntent.putExtra("isFromNotification", true);
        PendingIntent stopSayPi = PendingIntent.getService(mContext, 2, stopSayIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(stopSayPi);
        mRemoteViews.setOnClickPendingIntent(R.id.stopSay, stopSayPi);

    }

    private void updateRemoteViews() {
        mRemoteViews.setImageViewResource(R.id.stopSay, R.mipmap.ic_stop_say);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}