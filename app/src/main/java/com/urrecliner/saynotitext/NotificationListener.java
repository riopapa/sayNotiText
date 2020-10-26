package com.urrecliner.saynotitext;

import android.app.Notification;
import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import static com.urrecliner.saynotitext.Vars.kakaoAlertGroup;
import static com.urrecliner.saynotitext.Vars.kakaoAlertWho;
import static com.urrecliner.saynotitext.Vars.kakaoAlertText;
import static com.urrecliner.saynotitext.Vars.kakaoIgnores;
import static com.urrecliner.saynotitext.Vars.kakaoPersons;
import static com.urrecliner.saynotitext.Vars.packageIgnores;
import static com.urrecliner.saynotitext.Vars.packageIncludeNames;
import static com.urrecliner.saynotitext.Vars.packageNickNames;
import static com.urrecliner.saynotitext.Vars.packageTypes;
import static com.urrecliner.saynotitext.Vars.readOptionTables;
import static com.urrecliner.saynotitext.Vars.smsIgnores;
import static com.urrecliner.saynotitext.Vars.systemIgnores;
import static com.urrecliner.saynotitext.Vars.text2Speech;
import static com.urrecliner.saynotitext.Vars.textIgnores;
import static com.urrecliner.saynotitext.Vars.textSpeaks;
import static com.urrecliner.saynotitext.Vars.utils;


public class NotificationListener extends NotificationListenerService {

    private int speechCount = 0;
    private int listCount  = 0;
    final String logID = "Listener";
    private long lastTime = 0;
    private String lastAppName = "last";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn != null)
            addNotification(sbn);
    }

    public void addNotification(StatusBarNotification sbn) {

        final String TT_TITLE_TEXT = "tt";
        final String SM_SMS = "sm";
        final String KK_KAKAO = "kk";
        final String AN_ANDROID = "an";
        final String TO_TEXT_ONLY = "to";

        if (utils == null) utils = new Utils();

        if (text2Speech == null) {
            utils.log(logID, "$$ TTS is NULL " + ++speechCount);
            text2Speech = new Text2Speech();
            text2Speech.initiateTTS(getApplicationContext());
            text2Speech.readyAudioTTS();
        }
        if (packageIgnores == null) {
            readOptionTables = new ReadOptionTables();
            readOptionTables.read();
            utils.log(logID, "$$ Table reloaded " + ++listCount);
        }

        String packageFullName = sbn.getPackageName().toLowerCase();
        if (packageFullName.equals("") || isInTheList(packageFullName, packageIgnores))
            return;
        String packageNickName, packageType;
        packageType = getPackageType(packageFullName);
        packageNickName = getPackageNickName(packageFullName);

        Notification mNotification=sbn.getNotification();
        Bundle extras = mNotification.extras;
        String eTitle = extras.getString(Notification.EXTRA_TITLE);
        String eText = extras.getString(Notification.EXTRA_TEXT);
        if (eTitle == null && eText == null) {
            return;
        }
        if (eTitle == null) {
            utils.log(logID, packageFullName + " Title ```null text``` :" + eText);
            return;
        }

        if (eText != null && isInTheList(eText, textIgnores))
            return;
        if (eText != null)
            eText = eText.replaceAll("\n", "|");

        String eSubT = extras.getString(Notification.EXTRA_SUB_TEXT);
        long nowTime = System.currentTimeMillis();
        if (lastAppName.equals(packageFullName)) {
            if ((lastTime + 2000) > nowTime) {
                lastTime = nowTime;
                return;
            }
        }
        lastTime = nowTime;
        lastAppName = packageFullName;

//        String msgText;
//        try {
//            msgText = extras.getString(Notification.EXTRA_MESSAGES);
//        } catch (Exception e) {
//            msgText = null;
//        }

//        if ((thisTime-lastTime) <500 && (packageType.equals(AN_ANDROID) || packageType.equals(KK_KAKAO))) {
//            utils.log(logID, packageType+" Too SHORT TIME "+packageFullName+" with "+eTitle+" -_- "+eText);
//            return;
//        }
//        else
//            lastTime = thisTime;

//        dumpExtras(eTitle, eSubT, eText, msgText);
//        utils.log(logID, "Type "+packageType+", Full "+packageFullName+", Nick "+packageNickName+", 제목 "+eTitle+", 내용 "+eText);
        switch (packageType) {
            case KK_KAKAO :
                if (eText != null) {
                    sayKakao(packageNickName, eTitle, eSubT, eText);
                } else {
                    speakANDLog(packageNickName+" noText",  packageNickName + " (카카오) " + eSubT);
                }
                break;
            case TO_TEXT_ONLY :
                speakANDLog(packageNickName,  packageNickName + " (메시지입니다) " + eText);
                break;
            case SM_SMS :
                saySMS(packageNickName, eTitle, eText);
                break;
            case TT_TITLE_TEXT :
                sayTitleText(packageNickName, eTitle, eText);
                break;
            case AN_ANDROID :
                sayAndroid(packageFullName, eTitle, eText);
                break;
            default :
                if (isInTheList(eTitle, systemIgnores))
                    return;
                if (isInTheList(eText, textIgnores))
                    return;
                speakANDLog("unknown " + packageFullName, "unknown title " + eTitle + "_text:" + eText);
//                else
//                    dumpExtras(eTitle, eSubT, eText, msgText);
                break;
        }
    }

    private void sayKakao (String packageShortName, String eTitle, String eSubT, String eText) {
        if (shouldSpeak(eText, textSpeaks) || shouldSpeak(eTitle, textSpeaks) || shouldSpeak(eSubT, textSpeaks)) {
        }
        else if (isInTheList(eTitle, kakaoIgnores) || isInTheList(eText, kakaoPersons))
                return;
        if (eSubT != null) {    // eSubT : 단톡방
            if (isInTheList(eSubT, kakaoAlertGroup)) {   // 특정 단톡방에서는
                if (isInTheList(eTitle, kakaoAlertWho) && isInTheList(eText, kakaoAlertText))
                    speakANDLog(packageShortName+" "+eSubT, "[" + eTitle + "] 님이. 단톡방 ["+eSubT + "]에서 " + eText);
                // 아니면 해당 단톡방 무시
                // Group, 대화자, 인식문자 는 서로 연결 안 됨 ㅠ.ㅠ
            }
            else if (!isInTheList(eSubT, kakaoIgnores) && !isInTheList(eTitle, kakaoPersons)) {
                speakANDLog(packageShortName+" "+eSubT, "단톡방 [" + eSubT + "] 에서 [" + eTitle + "] 님이." + eText);
            }
        }
        else
            speakANDLog(packageShortName+" "+eTitle, "카톡[" + eTitle + "] 님이." + eText);
    }

    private void sayAndroid(String packageFullName, String eTitle, String eText) {

        if (eTitle == null || eText == null || eText.equals(""))
            return;
        if (isInTheList(eTitle, systemIgnores) || isInTheList(eText, systemIgnores))
            return;
        speakANDLog(packageFullName, " Android Title [" + eTitle + "], Text =" + eText);
    }

    private void sayTitleText(String packageShortName, String eTitle, String eText) {
        if (isInTheList(eTitle,systemIgnores) || isInTheList(eText, textIgnores) || isInTheList(eTitle, textIgnores))
            return;
        speakANDLog(packageShortName+" "+eTitle,packageShortName + " 에서  [" + eTitle + "]_로 부터. " + eText);
    }

    private void saySMS(String packageShortName, String eTitle, String eText) {
        if (isOnlyPhoneNumber(eTitle) || isInTheList(eTitle, smsIgnores) || isInTheList(eText, textIgnores))
            return;

        eText = eText.replace("[Web발신]","");
        speakANDLog(packageShortName+" "+eTitle, eTitle + " 로부터 SMS 왔음. " + eText);
    }
    
//    private void dumpExtras(String eTitle, String eSubT, String eText, String msgText){
//        if (eText != null) {
//            if (eText.length() > 100)
//                eText = eText.substring(0,100);
//            eText = eText.replaceAll("\n", "|");
//        }
//        String dumpText = "TIT:" + eTitle + ", SUBT:" + eSubT + ", TEXT:" + eText + ", MESSAGE:" + msgText;
//        utils.log(logID, dumpText);
//    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) { }

    private String getPackageType(String packageFullName){
        for (int idx = 0; idx < packageIncludeNames.length; idx++) {
            if (packageFullName.contains(packageIncludeNames[idx]))
                return packageTypes[idx];
        }
        return "noType";
    }

    private String getPackageNickName(String packageFullName){
        for (int idx = 0; idx < packageIncludeNames.length; idx++) {
            if (packageFullName.contains(packageIncludeNames[idx]))
                return packageNickNames[idx];
        }
        return "noNickName";
    }

    private boolean isInTheList(String text, String [] Ignores) {
        if (text == null)
            return false;
        for (String s : Ignores) {
            if (text.contains(s)) return true;
        }
        return false;
    }

    private boolean shouldSpeak(String text, String [] speaks) {
        if (text == null)
            return false;
        for (String s : speaks) {
            if (text.contains(s)) return true;
        }
        return false;
    }

    private void speakANDLog(String tag, String text) {
        String filename = tag + ".txt";
        utils.append2file(filename, text);
        if (isHeadphonesPlugged() || isRingerON()) {
            if (text2Speech == null) {
                utils.log(logID, "tts is null, reCreated");
                text2Speech = new Text2Speech();
                text2Speech.initiateTTS(getApplicationContext());
            }
            if (text.length() > 200)
                text = text.substring(0, 200) + ". 등등등";
            text2Speech.speak("잠시만요. " + text);
        }
    }

    private boolean isRingerON() {
        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        assert am != null;
        return am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
    }

    private boolean isHeadphonesPlugged(){
        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        assert am != null;
        AudioDeviceInfo[] audioDevices = am.getDevices(AudioManager.GET_DEVICES_ALL);
        for(AudioDeviceInfo deviceInfo : audioDevices){
            if(deviceInfo.getType()==AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                    || deviceInfo.getType()==AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                    || deviceInfo.getType()==AudioDeviceInfo.TYPE_BLUETOOTH_SCO
                    || deviceInfo.getType()==AudioDeviceInfo.TYPE_WIRED_HEADSET){
                return true;
            }
        }
        return false;
    }

    private boolean isOnlyPhoneNumber(String who) {
        String temp = who.replaceAll(getString(R.string.number_only),"");
        return temp.length() <= 2;
    }
}
