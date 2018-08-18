package com.urrecliner.andriod.saynotitext;

import android.app.Activity;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

import static com.urrecliner.andriod.saynotitext.Vars.act;
import static com.urrecliner.andriod.saynotitext.Vars.mAudioManager;
import static com.urrecliner.andriod.saynotitext.Vars.mFocusGain;
import static com.urrecliner.andriod.saynotitext.Vars.mFocusLoss;
import static com.urrecliner.andriod.saynotitext.Vars.mTTS;
import static com.urrecliner.andriod.saynotitext.Vars.pitch;
import static com.urrecliner.andriod.saynotitext.Vars.sayTTS;
import static com.urrecliner.andriod.saynotitext.Vars.speed;

public class tts {

    public void initiateTTS(Activity activity) {
        act = activity;

        mTTS = new TextToSpeech(activity, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.KOREA);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                        Toast.makeText(act,"Language not supported",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
    }

    public void setPitch (float mpitch) {
        pitch = mpitch;
    }
    public void setSpeed (float mspeed) {
        speed = mspeed;
    }

    public void setSayit(boolean msayTTS) {
        sayTTS = msayTTS;
    }
    public void speak(String text) {

        final int STRING_MAX = 150;

        if (sayTTS) {
            if (text.length() > STRING_MAX) {
                text = text.substring(0, STRING_MAX) + " 등등등";
            }
            text = text.replace("_", " ");
            mTTS.setPitch(pitch);
            mTTS.setSpeechRate(speed);
            getAudioFocus();
            mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
//            isTTSSpeaking();
//            releaseAudioFocus();
        }
    }
    public void stop() {
        sayTTS = false;
        mTTS.stop();
    }

    public void shutdown() {
        sayTTS = false;
        mTTS.shutdown();
    }

    public void checkRelease() {
        long l = 0;
        Toast.makeText(act,"Speaking check " + l, Toast.LENGTH_SHORT).show();
        while (mTTS.isSpeaking()) {
            l += 1;
        }
        Toast.makeText(act,"Speaking done " + l, Toast.LENGTH_SHORT).show();
        releaseAudioFocus();
    }
//                if (!mTTS.isSpeaking()) {

    private void getAudioFocus() {
//        AudioManager mAudioManager = (AudioManager) act.getSystemService(Context.AUDIO_SERVICE);
//        AudioFocusRequest mFocusRequest
//                = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
//                .build();
        mAudioManager.requestAudioFocus(mFocusGain);
    }
    private void releaseAudioFocus() {
//        AudioManager mAudioManager = (AudioManager) act.getSystemService(Context.AUDIO_SERVICE);
//        AudioFocusRequest mFocusRequest
//                = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
//                .build();
        mAudioManager.requestAudioFocus(mFocusLoss);
        Toast.makeText(act, "release AudioFocus " , Toast.LENGTH_SHORT).show();
    }

//    private boolean isAudioActive(){
//        AudioManager am = (AudioManager) act.getSystemService(Context.AUDIO_SERVICE);
//        boolean audioActive = am.isMusicActive();
//        return audioActive;
//    }

}
