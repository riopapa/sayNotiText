package com.urrecliner.saynotitext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

class Vars {
    static File packageDirectory = new File(Environment.getExternalStorageDirectory(), "_SayMessage");
    static File tableDirectory = new File(Environment.getExternalStorageDirectory(),"download/_sayMsgTable");
    static String[] packageIgnores = null;  // ignore some package messages
    static String[] packageTypes = null;    // tt: text with title, to: text only, sm: sms ...
    static String[] packageNickNames = null;
    static String[] packageIncludeNames = null;
    static String[] packageTables = null;
    static String[] kTalkIgnores = null;
    static String[] kTalkAlerts = null;
    static String[] kTalkAGroup = null;
    static String[] kTalkAWho = null;
    static String[] kTalkAGroupWho = null;
    static String[] kTalkAKey1 = null;
    static String[] kTalkAKey2 = null;
    static String[] kTalkSay = null;
    static String[] kTalkSaved = null;
    static String[] smsIgnores = null;
    static String[] systemIgnores = null;
    static String[] textIgnores = null;
    static String nowFileName;

    static SharedPreferences sharePrefer;

    @SuppressLint("StaticFieldLeak")
    static Context mContext = null;
    static Activity mActivity = null;
    static TextView tvOldMessage;
    static ScrollView tvOldScroll;
    static String oldMessage = "";

    static AudioManager mAudioManager = null;
    static AudioFocusRequest mFocusGain = null;

    static Text2Speech text2Speech = null;
    static float ttsPitch = 1.2f;
    static float ttsSpeed = 1.4f;
    static boolean speakOnOff = true; /* default is say */
    static Utils utils = null;
    static boolean isPhoneBusy = false;

    static ArrayList<AlertLine> alertLines;
    static int linePos = 999;
}
