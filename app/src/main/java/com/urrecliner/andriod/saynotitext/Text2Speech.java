package com.urrecliner.andriod.saynotitext;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

import static com.urrecliner.andriod.saynotitext.Vars.mActivity;
import static com.urrecliner.andriod.saynotitext.Vars.mAudioManager;
import static com.urrecliner.andriod.saynotitext.Vars.mContext;
import static com.urrecliner.andriod.saynotitext.Vars.mFocusGain;
import static com.urrecliner.andriod.saynotitext.Vars.pitch;
import static com.urrecliner.andriod.saynotitext.Vars.speed;
import static com.urrecliner.andriod.saynotitext.Vars.utils;

public class Text2Speech {

    private TextToSpeech mTTS;
    public void initiateTTS(Context context) {
        mContext = context;
        utils.log("mTTS", "initiating...");
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

    public void setPitch (float mPitch) {
        pitch = mPitch;
    }
    public void setSpeed (float mSpeed) {
        speed = mSpeed;
    }

    public void speak(String text) {

        final int STRING_MAX = 150;

        if (text.length() > STRING_MAX) {
            text = text.substring(0, STRING_MAX) + " ! 등등등";
        }
        text = text.replace("_", " ");
        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
        getAudioFocus();
        mTTS.speak(text, TextToSpeech.QUEUE_ADD, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
//            isTTSSpeaking();
//            releaseAudioFocus();
    }
    public void shutdown() {
        mTTS.stop();
        mTTS.shutdown();
    }

    private void getAudioFocus() {
//        AudioManager mAudioManager = (AudioManager) act.getSystemService(Context.AUDIO_SERVICE);
//        AudioFocusRequest mFocusRequest
//                = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
//                .build();
        mAudioManager.requestAudioFocus(mFocusGain);
    }
//    private void releaseAudioFocus() {
//        AudioManager mAudioManager = (AudioManager) act.getSystemService(Context.AUDIO_SERVICE);
//        AudioFocusRequest mFocusRequest
//                = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
//                .build();
//        mAudioManager.requestAudioFocus(mFocusLoss);
//        Toast.makeText(act, "release AudioFocus " , Toast.LENGTH_SHORT).show();
//    }

//    private boolean isAudioActive(){
//        AudioManager am = (AudioManager) act.getSystemService(Context.AUDIO_SERVICE);
//        boolean audioActive = am.isMusicActive();
//        return audioActive;
//    }
}
