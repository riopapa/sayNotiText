package com.urrecliner.andriod.saynotitext;

import android.app.Notification;
import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import static com.urrecliner.andriod.saynotitext.Vars.kakaoIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.kakaoPersons;
import static com.urrecliner.andriod.saynotitext.Vars.packageIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.packageIncludeNames;
import static com.urrecliner.andriod.saynotitext.Vars.packageNickNames;
import static com.urrecliner.andriod.saynotitext.Vars.packageTypes;
import static com.urrecliner.andriod.saynotitext.Vars.prepareLists;
import static com.urrecliner.andriod.saynotitext.Vars.smsIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.systemIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.text2Speech;
import static com.urrecliner.andriod.saynotitext.Vars.utils;


public class NotificationListener extends NotificationListenerService {

    final String logID = "Listener";
    private int speechCount = 0;
    private int listCount  = 0;

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
            utils.log(logID, "$$ TS TEXT2SPEECH IS NULL " + ++speechCount);
            text2Speech = new Text2Speech();
            text2Speech.initiateTTS(getApplicationContext());
            text2Speech.readyAudioTTS();
        }
        if (packageIgnores == null) {
            prepareLists = new PrepareLists();
            prepareLists.read();
            utils.log(logID, "$$ PREPARE IS NULL " + ++listCount);
        }

        String packageFullName = sbn.getPackageName().toLowerCase();
        if (packageFullName.equals("")) {
            return;
        }
        if (canBeIgnored(packageFullName, packageIgnores)) {
            return;
    }
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
        if (eText != null)
            eText = eText.replaceAll("\n\n", "|").replaceAll("\n", "|");
        String eSubT = extras.getString(Notification.EXTRA_SUB_TEXT);


//        dumpExtras(eTitle, eSubT, eText, msgText);
//        utils.log(logID, "Type "+packageType+", Full "+packageFullName+", Nick "+packageNickName+", 제목 "+eTitle+", 내용 "+eText);
        switch (packageType) {
            case KK_KAKAO :
                if (eText != null)
                    sayKakao(packageNickName, eTitle, eSubT, eText);
                break;
            case TO_TEXT_ONLY :
                speakANDLog(packageNickName,  packageNickName + " (메시지입니다) " + eText);
                break;
            case SM_SMS :
//                utils.log(logID,"SMS tit: "+eTitle+", txt: "+eText);
                saySMS(packageNickName, eTitle, eText);
                break;
            case TT_TITLE_TEXT :
                sayTitleText(packageNickName, eTitle, eText);
                break;
            case AN_ANDROID :
                sayAndroid(packageFullName, eTitle, eText);
                break;
            default :
                if (canBeIgnored(eTitle, systemIgnores))
                    return;
                speakANDLog("unknown " + packageFullName+" ", "unknown title " + eTitle + "_text:" + eText);
//                else
//                    dumpExtras(eTitle, eSubT, eText, msgText);
                break;
        }
    }

    private void sayKakao (String packageShortName, String eTitle, String eSubT, String eText) {
        if (eSubT != null) {
            if (!canBeIgnored(eSubT, kakaoIgnores)) { // eSub: 채팅방
                speakANDLog(packageShortName, "카톡 " + eSubT + "_단톡방 " + eTitle + "_님으로부터." + eText);
            }
        }
        else {
            if(!canBeIgnored(eTitle, kakaoPersons)) {  // eTitle: 개인 이름
                if (!eText.contains("페이스톡 해요"))
                    speakANDLog(packageShortName, "카톡." + eTitle + "_님으로 부터._" + eText);
            }
        }
    }
    private void sayAndroid(String packageFullName, String eTitle, String eText) {

        if (eTitle == null || eText == null || eText.equals("") || canBeIgnored(eTitle, systemIgnores) || canBeIgnored(eText, systemIgnores))
            return;
        speakANDLog(packageFullName, " Android Title ~" + eTitle + " Text~" + eText);
    }

    private void sayTitleText(String packageShortName, String eTitle, String eText) {
        if (packageShortName.equals("밴드") && eTitle.contains("읽지 않은"))
            return;
        if (packageShortName.equals("씨티은행") && eTitle.contains("Vaccine"))
            return;
        if (eText != null)
            speakANDLog(packageShortName,packageShortName + " 메시지입니다. " + eTitle + "_로 부터. " + eText);
    }

    private void saySMS(String packageShortName, String eTitle, String eText) {
        if (isPhoneNumber(eTitle) || canBeIgnored(eTitle, smsIgnores))
            return;
//        if (System.currentTimeMillis() - lastTime < 500 && eTitle.contains("메세지"))
//            return;
//        if (eTitle.contains("메세지")) {
//            utils.log("msg", "/// 메세지라고 온 제목은 무시 ("+eTitle.length()+":"+eTitle+") ("+eText.length()+":"+eText+")");
//            return;
//        }

        eText = eText.replace("[Web발신]","");
        speakANDLog(packageShortName, eTitle + " 로부터 SMS 메시지가 왔어요 " + eText);
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

    private boolean canBeIgnored(String notiText, String [] Ignores) {
        for (String s : Ignores) {
            if (notiText.contains(s)) return true;
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

    private boolean isPhoneNumber(String who) {
        String temp = who.replaceAll(getString(R.string.number_only),"");
        return temp.length() <= 2;
    }
}
