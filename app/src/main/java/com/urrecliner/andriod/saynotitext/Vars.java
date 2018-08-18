package com.urrecliner.andriod.saynotitext;

import android.app.Activity;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;

public class Vars {
    static String[] packageXcludes;
    static String[] packageTypes;
    static String[] packageCodes;
    static String[] packageNames;
    static String[] kakaoXcludes;
    static String[] kakaoPersons;
    static String[] smsXcludes;

    static TextToSpeech mTTS;
    static boolean sayTTS = true;
    static float pitch;
    static float speed;
    static Activity act;

    static AudioManager mAudioManager;
    static AudioFocusRequest mFocusGain;
    static AudioFocusRequest mFocusLoss;


}
