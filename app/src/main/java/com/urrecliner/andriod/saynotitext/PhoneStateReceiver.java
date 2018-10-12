package com.urrecliner.andriod.saynotitext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import static com.urrecliner.andriod.saynotitext.Vars.mActivity;
import static com.urrecliner.andriod.saynotitext.Vars.text2Speech;
import static com.urrecliner.andriod.saynotitext.Vars.utils;

public class PhoneStateReceiver extends BroadcastReceiver {
    String contactName;
    String phoneState;
    TelephonyManager telephony;
    Context mContext;
    Intent mIntent;
    Boolean phoneRinging = false;
    int speakCount;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        phoneRinging = false;
        mContext = context;
        mIntent = intent;
        utils.log("action", action);
        MyPhoneStateListener phoneListener = new MyPhoneStateListener();
        telephony = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public class MyPhoneStateListener extends PhoneStateListener {

        public void onCallStateChanged(int state, String incomingNumber) {

//            utils.logE("zz call changed", "phoneState is " + phoneState);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    phoneRinging = false;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    utils.customToast("Call OFFHOOK",Toast.LENGTH_SHORT);
                    phoneRinging = false;
                    break;
                case TelephonyManager.CALL_STATE_RINGING:

                    phoneRinging = true;
//                    incomingNumber = mIntent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    contactName = getContactName(mContext, incomingNumber);
                    if (contactName != null) {
                        utils.customToast(contactName, Toast.LENGTH_LONG);
                        sayWhoIsCalling(contactName);
                    }
                    break;
                default:
                    phoneRinging = false;
                    break;
            }
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

    private void sayWhoIsCalling(String calledBy) {
        final Handler handler = new Handler();
        final String callerName = calledBy;
        handler.postDelayed(new Runnable() {
            public void run() {
                String sayText;
                if (phoneRinging && speakCount++ < 4) {
                    sayText = callerName + " 로부터 전화왔어요. ";
                    text2Speech.speak(sayText);
                    utils.log("sayCall", phoneState + ":" + sayText);
                    handler.postDelayed(this, 8000);
                }
                else {
                    phoneRinging = false;
                    handler.removeCallbacks(this);
                    text2Speech.shutdown();
                    text2Speech.initiateTTS(mActivity);
                }
            }
        }, 200);
    }
}

