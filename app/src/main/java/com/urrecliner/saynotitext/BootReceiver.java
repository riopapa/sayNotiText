package com.urrecliner.saynotitext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import static com.urrecliner.saynotitext.Vars.Booted;
import static com.urrecliner.saynotitext.Vars.utils;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        String stateStr = null, number;
        int state = -1;
        if (utils == null)
            utils = new Utils();
        utils.log("boot receiver action", action);
        if (action.equals("android.intent.action.NEW_OUTGOING_CALL")) {
            number = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        } else {
            stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            state = 0;
            if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                state = TelephonyManager.CALL_STATE_IDLE;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                state = TelephonyManager.CALL_STATE_RINGING;
            }
        }
        utils.log("outgoing action="+action,  " statestr=" + stateStr + " state=" + state + " number=" + number);
        Booted = true;

        Intent i = new Intent(context, MainActivity.class);
        context.startActivity(i);
    }
}
