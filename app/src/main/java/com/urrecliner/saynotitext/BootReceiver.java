package com.urrecliner.saynotitext;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = context.getPackageName();
        for (int i=0; i<10; i++)
            Log.w("pkg "+i,"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + packageName);
        Intent sendIntent = context.getPackageManager().getLaunchIntentForPackage("com.urrecliner.saynotitext");
        if (sendIntent != null)
            for (int i=0; i<10; i++)
                Log.w("intent assigned "+i," iiiiiiiiii"+sendIntent.toString());
        assert sendIntent != null;
        sendIntent.putExtra("boot", true);
        context.startActivity(sendIntent);
        for (int i=0; i<10; i++)
            Log.w("nothing "+i,sendIntent.toString());
    }
}
