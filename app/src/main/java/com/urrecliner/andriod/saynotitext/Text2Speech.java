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
import static com.urrecliner.andriod.saynotitext.Vars.ttsPitch;
import static com.urrecliner.andriod.saynotitext.Vars.ttsSpeed;
import static com.urrecliner.andriod.saynotitext.Vars.utils;

class Text2Speech {

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
                        Log.e("TTS", "Language not supported");
                        Toast.makeText(mActivity, "Language not supported",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
        if (ttsPitch == 0f) {
            ttsPitch = 1.2f;
            ttsSpeed = 1.4f;
//            utils.append2file(logFile, "pitch, speed ZERO ");
        }
    }

    void setPitch(float p) {
        ttsPitch = p;
    }

    void setSpeed(float s) {
        ttsSpeed = s;
    }

    void speak(String text) {

        String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z./,\\s]"; // 한글, 영문, 숫자만 OK
//        String match = "[`~!@#$%^&*()'/+;<>\\_▶★]"; // 특수문자 읽기 방지
        text = text.replaceAll(match, " ");
        try {
            mAudioManager.requestAudioFocus(mFocusGain);
        } catch (Exception e) {
            utils.log(logID, "mAudioManager requestAudioFocus Error");
        }
        ttsSpeak(text);
        long delayTime = (long) ((float) (text.length() * 240) / ttsSpeed);
        new Timer().schedule(new TimerTask() {
            public void run() {
                mAudioManager.abandonAudioFocusRequest(mFocusGain);
            }
        }, delayTime);
    }

    private void ttsSpeak(String text) {
        try {
            readyAudioTTS();
            mTTS.speak(text, TextToSpeech.QUEUE_ADD, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
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
    }

}
