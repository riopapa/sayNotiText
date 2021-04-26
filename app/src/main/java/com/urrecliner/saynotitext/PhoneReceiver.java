package com.urrecliner.saynotitext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import static com.urrecliner.saynotitext.Vars.isPhoneBusy;
import static com.urrecliner.saynotitext.Vars.mContext;
import static com.urrecliner.saynotitext.Vars.utils;

public class PhoneReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String stateStr, number;
        if (utils == null)
            utils = new Utils();
        try {
            stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
        } catch (Exception e) {
            stateStr = "STATE Error";
            number = "none";
        }
        utils.log("^phone^",  " stateStr=" + stateStr + " number=" + number);
        if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            isPhoneBusy = false;
            Toast.makeText(mContext, "\nIDLE\n" + number + "\nIDLE", Toast.LENGTH_LONG).show();
        } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            isPhoneBusy = true;
            Toast.makeText(mContext, "\nOFFHOOK\n"+number+"\nOFFHOOK\n", Toast.LENGTH_LONG).show();
        } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            isPhoneBusy = true;
            Toast.makeText(mContext, "\nRING\n"+number+"\nRING\n", Toast.LENGTH_LONG).show();
        }
    }
}
