package com.urrecliner.saynotitext;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.saynotitext.Vars.mAudioManager;
import static com.urrecliner.saynotitext.Vars.mContext;
import static com.urrecliner.saynotitext.Vars.mFocusGain;
import static com.urrecliner.saynotitext.Vars.ttsPitch;
import static com.urrecliner.saynotitext.Vars.ttsSpeed;
import static com.urrecliner.saynotitext.Vars.utils;

class Text2Speech  implements TextToSpeech.OnInitListener {

    private String logID = "TTS";
    private TextToSpeech mTTS;

    void initiateTTS(Context context) {
        if (context == null) {
            utils.log(logID, "initiate TTS Context NULL ");
            utils.customToast("Context is null for initiateTTS");
        }
        mContext = context;
//        utils.append2file(notifyFile, "initiate mTTS ");
        mTTS = null;
        mTTS = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.KOREA);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        utils.logE(logID, "Language not supported");
                    }
                } else {
                    utils.logE(logID, "Initialization failed");
                }
            }
        });
        if (ttsPitch == 0f) {
            ttsPitch = 1.2f;
            ttsSpeed = 1.4f;
        }
    }

    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            mTTS.setLanguage(Locale.KOREA);
//            mTTS.setPitch(1);
        }
    }

    void setPitch(float p) {
        ttsPitch = p;
    }

    void setSpeed(float s) {
        ttsSpeed = s;
    }

    void speak(String text) {

        String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z_./,\\s]"; // 한글, 영문, 숫자 및 몇 특수문자만 OK
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
//        long delayTime = (long) ((float) (text.length() * 240) / ttsSpeed);
//        new Timer().schedule(new TimerTask() {
//            public void run() {
//                mAudioManager.abandonAudioFocusRequest(mFocusGain);
//            }
//        }, delayTime);
    }

    private void ttsSpeak(String text) {
        readyAudioTTS();
        try {
            mostRecentUtteranceID = (""+System.currentTimeMillis()).substring(5);
//            utils.log(logID,"mostRecentUtteranceID is "+mostRecentUtteranceID);
            mTTS.speak(text, TextToSpeech.QUEUE_ADD, null, mostRecentUtteranceID);
//            mTTS.speak(text, TextToSpeech.QUEUE_ADD, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
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

    String mostRecentUtteranceID;
    void readyAudioTTS() {
        utils.readyAudioManager(mContext);
        if (ttsPitch == 0f) {
            ttsPitch = 1.2f;
            ttsSpeed = 1.4f;
        }
        if (mTTS == null) {
            utils.log(logID, "$$ mTTS NULL ");
            initiateTTS(mContext);
        }
        mTTS.setPitch(ttsPitch);
        mTTS.setSpeechRate(ttsSpeed);

        mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) { }

            @Override
            // this method will always called from a background thread.
            public void onDone(String utteranceId) {
                if (utteranceId.equals(mostRecentUtteranceID)) {
//                boolean wasCalledFromBackgroundThread = (Thread.currentThread().getId() != 1);
//                utils.log("XXX", "was onDone() called on a background thread? : " + wasCalledFromBackgroundThread);
                    mAudioManager.abandonAudioFocusRequest(mFocusGain);
                }
            }

            @Override
            public void onError(String utteranceId) {
            }
        });

    }
}
