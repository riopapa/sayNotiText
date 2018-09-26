package com.urrecliner.andriod.saynotitext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import static com.urrecliner.andriod.saynotitext.Vars.mActivity;
import static com.urrecliner.andriod.saynotitext.Vars.speed;
import static com.urrecliner.andriod.saynotitext.Vars.text2Speech;
import static com.urrecliner.andriod.saynotitext.Vars.utils;

public class PhoneStateReceiver extends BroadcastReceiver {
    String incomingNumber;
    String callerName;
    String state;
    Context mContext;
    int repeatCount;
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        String action;
        try {
            action = intent.getAction();
            utils.customToast("call action [" + action + "]", Toast.LENGTH_SHORT);
//            utils.logE("onReceive", "call action is " + action);
            state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                callerName = getContactName(context, incomingNumber);
                if (callerName != null) {
                    sayCallerName(callerName);
                }
            }
//            if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))){
//                text2Speech.customToast("Call Received State",Toast.LENGTH_SHORT);
//            }
//            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
//                text2Speech.customToast("Call Idle State", Toast.LENGTH_SHORT);
//            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getContactName(Context context, String incomingNumber) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(incomingNumber));
        String[] projection = new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME};
        String name = null;
        Cursor cursor = context.getContentResolver().query(
                uri, projection, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()){
                name = cursor.getString(0);
            }
            cursor.close();
        }
        return name;
    }
    private void sayCallerName (String calledBy) {
        final Handler handler = new Handler();
        final String callerName = calledBy;
        final float mSpeed = speed;
        speed = 0.9f;
        handler.postDelayed(new Runnable() {
            public void run() {
                String sayText;
                if (repeatCount++ < 5 && state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    utils.customToast("[" + state + "]", Toast.LENGTH_SHORT);
                    sayText = callerName + " 로부터 전화왔어요. "  + "," + repeatCount + " 번,";
                    text2Speech.speak(sayText);
                    utils.log("sayCall", sayText);
                    handler.postDelayed(this, 5000);
                }
                else {
                    handler.removeCallbacks(this);
                    speed = mSpeed;
                    text2Speech.shutdown();
                    text2Speech.initiateTTS(mActivity);
                }
            }
        }, 2000);
    }
}
