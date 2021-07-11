package com.urrecliner.saymessage;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;

import static com.urrecliner.saymessage.Vars.mAudioManager;
import static com.urrecliner.saymessage.Vars.mContext;
import static com.urrecliner.saymessage.Vars.mFocusGain;
import static com.urrecliner.saymessage.Vars.ttsPitch;
import static com.urrecliner.saymessage.Vars.ttsSpeed;
import static com.urrecliner.saymessage.Vars.utils;

class Text2Speech {

    private final String logID = "TTS";
    private TextToSpeech mTTS;

    void initiateTTS(Context context) {
        if (context == null) {
            utils.log(logID, "initiate TTS Context NULL ");
            utils.customToast("Context is null for initiateTTS");
        }
        mContext = context;
//        utils.append2file(notifyFile, "initiate mTTS ");
        mTTS = null;
        mTTS = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = mTTS.setLanguage(Locale.KOREA);
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    utils.logE(logID, "Language not supported");
                }
            } else {
                utils.logE(logID, "Initialization failed");
            }
        });
        readyAudioTTS();
    }

    void setPitch(float p) {
        ttsPitch = p;
    }

    void setSpeed(float s) {
        ttsSpeed = s;
    }

    void speak(String text) {

        String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z.,\\s]"; // 한글, 영문, 숫자만 OK
//        String match = "[`~!@#$%^&*()'/+;<>\\_▶★]"; // 특수문자 읽기 방지
        text = text
                .replace("ㅇㅋ","오케이")
                .replace("ㅎ","흐")
                .replace("ㅋ","크")
                .replace("ㅠ","흑")
                .replaceAll(match, " ");
        try {
            mAudioManager.requestAudioFocus(mFocusGain);
        } catch (Exception e) {
            utils.logE(logID, "requestAudioFocus");
        }
        int idx = text.indexOf("http");
        if (idx > 0)
            text = text.substring(0,idx)+" url 있음";
        ttsSpeak(text);
//        long delayTime = (long) ((float) (text.length() * 230) / ttsSpeed);
//        new Timer().schedule(new TimerTask() {
//            public void run() {
//                mAudioManager.abandonAudioFocusRequest(mFocusGain);
//            }
//        }, delayTime);

        mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) { }

            @Override
            // this method will always called from a background thread.
            public void onDone(String utteranceId) {
                mAudioManager.abandonAudioFocusRequest(mFocusGain);
                utils.beepOnce(0);
            }

            @Override
            public void onError(String utteranceId) { }
        });
    }

    private void ttsSpeak(String text) {
        readyAudioTTS();
        Bundle params = new Bundle();
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f); // change the 0.5f to any value from 0f-1f (1f is default)
        try {
            mTTS.speak(text, TextToSpeech.QUEUE_ADD, params, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
        } catch (Exception e) {
            utils.logE(logID, "justSpeak:" + e.toString());
        }
    }

    void ttsStop() {
        mTTS.stop();
        mAudioManager.abandonAudioFocusRequest(mFocusGain);
        try {
            readyAudioTTS();
        } catch (Exception e) {
            utils.logE(logID, "ttsStop:" + e.toString());
        }
    }

    void readyAudioTTS() {
//        utils.readyAudioManager(mContext);
//        if (mTTS == null) {
//            utils.log(logID, "$$ mTTS NULL ");
//            initiateTTS(mContext);
//        }
        mTTS.setPitch(ttsPitch);
        mTTS.setSpeechRate(ttsSpeed);
    }

}
