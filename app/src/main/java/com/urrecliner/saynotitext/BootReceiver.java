package com.urrecliner.saynotitext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import static com.urrecliner.saynotitext.Vars.Booted;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Booted = true;
        Intent i = new Intent(context, MainActivity.class);
        context.startActivity(i);
    }
}
