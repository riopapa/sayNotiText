package com.urrecliner.andriod.saynotitext;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.andriod.saynotitext.Vars.mActivity;
import static com.urrecliner.andriod.saynotitext.Vars.mAudioManager;
import static com.urrecliner.andriod.saynotitext.Vars.mContext;
import static com.urrecliner.andriod.saynotitext.Vars.mFocusGain;
import static com.urrecliner.andriod.saynotitext.Vars.utils;

public class Text2Speech {

    float pitch;
    float speed;

    private TextToSpeech mTTS;

    public void initiateTTS(Context context) {
        mContext = context;
        utils.log("mTTS", "initiating...");
        mTTS = null;
        mTTS = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.KOREA);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                        Toast.makeText(mActivity, "Language not supported",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
    }

    public void setPitch (float p) {
        pitch = p;
    }
    public void setSpeed (float s) {
        speed = s;
    }

    public void speak(String text) {

        final int STRING_MAX = 150;
        if (text.length() > STRING_MAX) {
            text = text.substring(0, STRING_MAX) + " ! 등등등";
        }
//        String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]"; 한글, 영문, 숫자만 OK
        String match = "[`~!@#$%^&*()'/+;<>\\_▶★]";
        text =text.replaceAll(match, " ");
        justSpeak(text);
        long delayTime = (long) ((float) (text.length() * 240) / speed);
        new Timer().schedule(new TimerTask() {
            public void run () {
            mAudioManager.abandonAudioFocusRequest(mFocusGain);
            }
        }, delayTime);
    }

    public void justSpeak(String text) {
        utils.readyAudioManager(mContext);
        mAudioManager.requestAudioFocus(mFocusGain);
        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
        mTTS.speak(text, TextToSpeech.QUEUE_ADD, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
    }

    public void flushSpeak(String text) {
        utils.readyAudioManager(mContext);
        mAudioManager.requestAudioFocus(mFocusGain);
        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
        mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
    }

    public void shutdown() {
        mTTS.speak("_", TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
        mTTS.stop();
        mTTS.shutdown();
    }

}
