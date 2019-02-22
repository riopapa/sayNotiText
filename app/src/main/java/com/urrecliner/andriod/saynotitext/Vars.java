package com.urrecliner.andriod.saynotitext;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;

public class Vars {
    static String[] packageIgnores = null;
    static String[] packageTypes = null;
    static String[] packageCodes = null;
    static String[] packageNames = null;
    static String[] packageTables = null;
    static String[] kakaoIgnores = null;
    static String[] kakaoPersons = null;
    static String[] smsIgnores = null;
    static String[] systemIgnores = null;
    static String Booted = null;

    static Activity mActivity = null;
    static Context mContext = null;
    static AudioManager mAudioManager = null;
    static AudioFocusRequest mFocusGain = null;
//    static AudioFocusRequest mFocusLoss = null;

    static Text2Speech text2Speech = null;
    static float ttsPitch = 1.2f;
    static float ttsSpeed = 1.4f;

    static PrepareLists prepareLists = null;
    static Utils utils = null;

}
