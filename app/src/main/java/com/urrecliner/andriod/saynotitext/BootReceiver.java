package com.urrecliner.andriod.saynotitext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Vars.Booted = "ReBooted";
        Log.w("SayNotiText",Vars.Booted);
        Intent i = new Intent(context, MainActivity.class);
        context.startActivity(i);
    }
}
