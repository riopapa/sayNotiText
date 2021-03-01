package com.urrecliner.saynotitext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Environment;

import java.io.File;

class Vars {
    static File packageDirectory = new File(Environment.getExternalStorageDirectory(), "sayNotiText");
    static File tableDirectory = new File(Environment.getExternalStorageDirectory(),"download/sayNoti");
    static String[] packageIgnores = null;  // ignore some package messages
    static String[] packageTypes = null;    // tt: text with title, to: text only, sm: sms ...
    static String[] packageNickNames = null;
    static String[] packageIncludeNames = null;
    static String[] packageTables = null;
    static String[] kakaoIgnores = null;
    static String[] kakaoPersons = null;
    static String[] kakaoAlerts = null;
    static String[] kakaoAGroup = null;
    static String[] kakaoAWho = null;
    static String[] KakaoAGroupWho = null;
    static String[] kakaoAKey1 = null;
    static String[] kakaoAKey2 = null;
    static boolean[] kakaoSpeech = null;
    static String[] smsIgnores = null;
    static String[] systemIgnores = null;
    static String[] textIgnores = null;
    static String[] textSpeaks = null;
    static boolean Booted = false;
    static String nowFileName;

    static SharedPreferences sharePrefer;

    @SuppressLint("StaticFieldLeak")
    static Context mContext = null;
    static AudioManager mAudioManager = null;
    static AudioFocusRequest mFocusGain = null;

    static Text2Speech text2Speech = null;
    static float ttsPitch = 1.2f;
    static float ttsSpeed = 1.4f;
    static boolean sayMessage = true; /* default is say */
    static ReadOptionTables readOptionTables = null;
    static Utils utils = null;
    static boolean isPhoneBusy = false;
}
