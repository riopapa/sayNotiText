package com.urrecliner.andriod.saynotitext;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;

public class Vars {
    static String[] packageXcludes = null;
    static String[] packageTypes = null;
    static String[] packageCodes = null;
    static String[] packageNames = null;
    static String[] kakaoXcludes = null;
    static String[] kakaoPersons = null;
    static String[] smsXcludes = null;

    static Activity mActivity = null;
    static Context mContext = null;
    static AudioManager mAudioManager = null;
    static AudioFocusRequest mFocusGain = null;
//    static AudioFocusRequest mFocusLoss = null;

    static Text2Speech text2Speech = new Text2Speech();
    static PrepareLists prepareLists = new PrepareLists();
    static Utils utils = new Utils();

}
