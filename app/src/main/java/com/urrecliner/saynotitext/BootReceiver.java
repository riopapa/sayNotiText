package com.urrecliner.saynotitext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Vars.Booted = "ReBooted";
        Intent i = new Intent(context, MainActivity.class);
        context.startActivity(i);
    }
}
