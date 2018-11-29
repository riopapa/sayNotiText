package com.urrecliner.andriod.saynotitext;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.andriod.saynotitext.Vars.text2Speech;
import static com.urrecliner.andriod.saynotitext.Vars.utils;

public class PhoneStateReceiver extends BroadcastReceiver {
    String contactName;
    TelephonyManager telephony;
    Context mContext;
    Intent mIntent;
    Boolean phoneRinging = false;
    int speakCount = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
//        String action = intent.getAction();
        phoneRinging = false;
        mContext = context;
        mIntent = intent;
//        utils.log("action", action);
        MyPhoneStateListener phoneListener = new MyPhoneStateListener();
        telephony = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
//        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE); // 벨소리가 안나서 일단 죽임
    }

    public class MyPhoneStateListener extends PhoneStateListener {

        public void onCallStateChanged(int state, String incomingNumber) {

            utils.logE("call changed", "phoneState is " + phoneStateString(state));
            utils.append2file(("call.txt"),"phone state=" + phoneStateString(state));
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                phoneRinging = true;
                contactName = getContactName(mContext, incomingNumber);
                if (contactName != null) {
                    speakCount = 0;
                    utils.customToast(contactName, Toast.LENGTH_LONG);
//                    startCalling(contactName);
                }
            }
            else
                phoneRinging = false;
        }
    }

    private String phoneStateString(int state) {
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                return "STATE_IDLE";
            case TelephonyManager.CALL_STATE_OFFHOOK:
                return "STATE_OFFHOOK";
            case TelephonyManager.CALL_STATE_RINGING:
                return "STATE_RINGING";
            default:
                return "STATE_DEFAULT=" + state;
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
        else {
            name = null;
        }
        return name;
    }

    private void startCalling(String calledBy) {
        utils.log("sayCall", calledBy + " count " + speakCount);
        utils.append2file("call.txt", calledBy + " count " + speakCount);
        utils.append2file("call.txt", "phone ring status "  + phoneRinging);
        saySomebodyCalling();
//        handler.postDelayed(new Runnable() {
//            public void run() {
//                String sayText;
//                if (phoneRinging && speakCount++ < 4) {
//                    sayText = callerName + " 로부터 전화왔어요. ";
//                    text2Speech.speak(sayText);
//                    utils.log("sayCall", sayText);
//                    utils.append2file("call.txt",sayText);
//                    handler.postDelayed(this, 8000);
//                }
//                else {
//                    phoneRinging = false;
//                    handler.removeCallbacks(this);
////                    text2Speech.shutdown();
//                    text2Speech.initiateTTS(mContext);
//                }
//            }
//        }, 200);
    }

    final Handler repeatSayWho = new Handler() { public void handleMessage(Message msg) { if(msg.what < 5) saySomebodyCalling(); }};

    private void saySomebodyCalling() {
        String sayText = contactName + " 로부터 전화왔어요. ";
        text2Speech.ttsSpeak(sayText, TextToSpeech.QUEUE_FLUSH);
        utils.append2file("call.txt", sayText);
        speakCount++;
        if (phoneRinging) {
            new Timer().schedule(new TimerTask() {
                public void run() {
                    repeatSayWho.sendEmptyMessage(speakCount);
                }
            }, 5000);
        }
    }
}

