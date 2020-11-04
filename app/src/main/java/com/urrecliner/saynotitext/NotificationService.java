package com.urrecliner.saynotitext;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.widget.RemoteViews;

import static com.urrecliner.saynotitext.Vars.sayStockOnOff;
import static com.urrecliner.saynotitext.Vars.text2Speech;

public class NotificationService extends Service {

    private Context mContext;
    NotificationCompat.Builder mBuilder = null;
    NotificationChannel mNotificationChannel = null;
    NotificationManager mNotificationManager;
    private RemoteViews mRemoteViews;
    private static final int STOP_SAY = 10011;
    private static final int STOCK_ON_OFF = 10022;

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
            case STOCK_ON_OFF:
                sayStockOnOff = !sayStockOnOff;
                mBuilder.setSmallIcon(R.mipmap.icon_launcher);
                mRemoteViews.setImageViewResource(R.id.stock_OnOff, (sayStockOnOff)? R.mipmap.say_stock_off :R.mipmap.say_stock_on);
                break;
            default:
                break;
        }
        startForeground(100, mBuilder.build());
        return START_STICKY;
    }

    private void createNotification() {

        if (null == mNotificationChannel) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotificationChannel = new NotificationChannel("default","default", NotificationManager.IMPORTANCE_DEFAULT);
                mNotificationManager.createNotificationChannel(mNotificationChannel);
            }
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

        Intent onOffIntent = new Intent(this, NotificationService.class);
        onOffIntent.putExtra("operation", STOCK_ON_OFF);
//        onOffIntent.putExtra("isFromNotification", true);
        PendingIntent onOffPi = PendingIntent.getService(mContext, 1, onOffIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setImageViewResource(R.id.stock_OnOff, (sayStockOnOff)? R.mipmap.say_stock_off :R.mipmap.say_stock_on);
        mBuilder.setContentIntent(onOffPi);
        mRemoteViews.setOnClickPendingIntent(R.id.stock_OnOff, onOffPi);

    }

    private void updateRemoteViews() {
//        mRemoteViews.setImageViewResource(R.id.reLoad, R.mipmap.ic_reloading);
        mRemoteViews.setImageViewResource(R.id.stock_OnOff, (sayStockOnOff)? R.mipmap.say_stock_off :R.mipmap.say_stock_on);
        mRemoteViews.setImageViewResource(R.id.Stop_Now, R.mipmap.mute_right_now);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
