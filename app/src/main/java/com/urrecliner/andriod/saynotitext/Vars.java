package com.urrecliner.andriod.saynotitext;

import android.app.Activity;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;

public class Vars {
    static String[] packageXcludes = null;
    static String[] packageTypes = null;
    static String[] packageCodes = null;
    static String[] packageNames = null;
    static String[] kakaoXcludes = null;
    static String[] kakaoPersons = null;
    static String[] smsXcludes = null;

    static Text2Speech Tts = null;      // tts instance
    static TextToSpeech mTTS = null;
    static float pitch;
    static float speed;
    static Activity act = null;
    static AudioManager mAudioManager = null;
    static AudioFocusRequest mFocusGain = null;
    static AudioFocusRequest mFocusLoss = null;
    static PrepareLists mPrepareLists = null;

}
