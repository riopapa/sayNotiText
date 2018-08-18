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

import static com.urrecliner.andriod.saynotitext.Vars.act;
import static com.urrecliner.andriod.saynotitext.Vars.speed;

public class PhoneStateReceiver extends BroadcastReceiver {
    String incomingNumber;
    String callerName;
    String state;
    int Count = 0;
    tts tts;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action;
        tts = new tts();
        try {
//            System.out.println("Receiver start");
            action = intent.getAction();
                    Toast.makeText(context, "call action [" + action + "]", Toast.LENGTH_SHORT);
            state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
//                Toast.makeText(context," Number is -"+incomingNumber,Toast.LENGTH_SHORT).show();

                callerName = getContactName(context, incomingNumber);
                if (callerName != null) {
                    Count = 0;
                    sayCallerName(callerName, context);
                }
            }
            if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))){
                Toast.makeText(context,"Call Received State",Toast.LENGTH_SHORT).show();
            }
            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                Toast.makeText(context,"Call Idle State",Toast.LENGTH_SHORT).show();
            }
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
    private void sayCallerName (String calledBy, Context context) {
        final Handler handler = new Handler();
        final String callerName = calledBy;
        final float mSpeed = speed;
        final Context con = context;
        speed = 0.9f;

        handler.postDelayed(new Runnable() {
            public void run() {
                String sayText;
                if (Count++ < 7 && state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    sayText = callerName + " 로부터 전화왔어요. " + callerName + " 로부터 전화왔어요. " + "," + Count + " 번,";
                    tts.speak(sayText);
                    Toast.makeText(con, sayText,Toast.LENGTH_SHORT).show();
                    handler.postDelayed(this, 3000);
                }
                else {
                    handler.removeCallbacks(this);
                    speed = mSpeed;
                    tts.stop();
                    tts.shutdown();
                    tts.initiateTTS(act);
                }
            }
        }, 100);
    }

}

