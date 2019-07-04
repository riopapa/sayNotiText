package com.urrecliner.andriod.saynotitext;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import static com.urrecliner.andriod.saynotitext.Vars.kakaoIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.kakaoPersons;
import static com.urrecliner.andriod.saynotitext.Vars.packageIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.packageNames;
import static com.urrecliner.andriod.saynotitext.Vars.packageShortNames;
import static com.urrecliner.andriod.saynotitext.Vars.packageTables;
import static com.urrecliner.andriod.saynotitext.Vars.packageTypes;
import static com.urrecliner.andriod.saynotitext.Vars.prepareLists;
import static com.urrecliner.andriod.saynotitext.Vars.smsIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.systemIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.text2Speech;
import static com.urrecliner.andriod.saynotitext.Vars.utils;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NotificationListener extends NotificationListenerService {

    final String notifyFile = "notification.txt";
    int smsCount = 0;
    int utilsCount = 0;
    int speechCount = 0;
    int listCount  = 0;
    long lastTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn != null)
            addNotification(sbn);
    }

    public void addNotification(StatusBarNotification sbn)  {

        final String TT_TITLE_TEXT = "tt";
        final String SM_SMS = "sm";
        final String KK_KAKAO = "kk";
        final String AN_ANDROID = "an";
        final String TO_TEXT_ONLY = "to";

        if (utils == null) {
            utils = new Utils();
            utils.append2file(notifyFile, "$$ UTIL IS NULL AND RELOADED " + ++utilsCount);
        }
        long nowTime = System.currentTimeMillis();
        if (lastTime == nowTime)
            return;
        lastTime = nowTime;

        if (text2Speech == null) {
            utils.append2file(notifyFile, "$$ TS TEXT2SPEECH IS NULL " + ++speechCount);
            text2Speech = new Text2Speech();
            text2Speech.initiateTTS(getApplicationContext());
        }
        text2Speech.readyAudioTTS();
        if (packageIgnores == null) {
            prepareLists = new PrepareLists();
            prepareLists.read();
            utils.log("readqq", "len="+packageTables.length);
            utils.append2file(notifyFile, "$$ PREPARE IS NULL " + ++listCount);
        }
//        Log.w("then","last time 2 "+lastTime);

        String packageFullName = sbn.getPackageName().toLowerCase();
        if (packageFullName.contains("adguard"))
            return;
        String packageNickName, packageType;
        if (packageFullName.equals("")) {
            return;
        }
        if(canBeIgnored(packageFullName, packageIgnores))
            return;
        Log.w("packageFullName",packageFullName);

        packageType = getPackageType(packageFullName);
        packageNickName = getPackageNickName(packageFullName);

        Notification mNotification=sbn.getNotification();
        Bundle extras = mNotification.extras;
        String eTitle = extras.getString(Notification.EXTRA_TITLE);
        String eText = extras.getString(Notification.EXTRA_TEXT);
        if (eTitle == null && eText == null)
            return;
        if (eText != null) {
            if (eText.length() > 250) {
                eText = eText.substring(0, 250) + ". 등등등";
            }
            eText = eText.replaceAll("\n\n","|").replaceAll("\n","|");
        }

        String eSubT = extras.getString(Notification.EXTRA_SUB_TEXT);
        String msgText = extras.getString(Notification.EXTRA_MESSAGES);

//        dumpExtras(eTitle, eSubT, eText, msgText);

        switch (packageType) {
            case KK_KAKAO :
                if (eText != null) {
                    sayKakao(packageNickName, eTitle, eSubT, eText);
                }
                break;
            case TO_TEXT_ONLY :
                speakANDLog(packageNickName,  packageNickName + " (메세지입니다) " + eText);
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
                if (eTitle != null && !eTitle.contains("Vaccine")) {
                    speakANDLog("unknown " + packageFullName, "title:" + eTitle + "_text:" + eText);
                }
                else
                    dumpExtras(eTitle, eSubT, eText, msgText);

                break;
        }
    }

    private void sayKakao (String packageShortName, String eTitle, String eSubT, String eText) {
        if (eSubT != null) {
            if (!canBeIgnored(eSubT, kakaoIgnores)) { // eSub: 채팅방
                speakANDLog(packageShortName, "카카오톡 " + eSubT + "_단톡방 " + eTitle + "_님으로부터." + eText);
            }
        }
        else {
            if(!canBeIgnored(eTitle, kakaoPersons)) {  // eTitle: 개인 이름
                speakANDLog(packageShortName, "카카오톡." + eTitle + "_님으로 부터._" + eText);
            }
        }
    }
    private void sayAndroid(String packageFullName, String eTitle, String eText) {

        if (eTitle == null || eText == null || eText.equals("") || canBeIgnored(eTitle, systemIgnores) || canBeIgnored(eText, systemIgnores))
            return;
        speakANDLog(packageFullName, " Title ~" + eTitle + " Text~" + eText);
    }

    private void sayTitleText(String packageShortName, String eTitle, String eText) {
        if (packageShortName.equals("밴드") && eTitle.contains("읽지 않은"))
                return;
        if (packageShortName.equals("씨티은행") && eTitle.contains("Vaccine"))
                return;
        if (eText != null)
            speakANDLog(packageShortName,packageShortName + " 메세지입니다. " + eTitle + "_로 부터 " + eText);
    }

    private void saySMS(String packageShortName, String eTitle, String eText) {

        smsCount++;
        if (isPhoneNumber(eTitle))
            return;
        if (canBeIgnored(eTitle, smsIgnores))
            return;
        eText = eText.replace("[Web발신]","");
        speakANDLog(packageShortName, eTitle + " 로부터 SMS 메세지가 왔어요 " + eText);
    }
    
    private void dumpExtras(String eTitle, String eSubT, String eText, String msgText){
        if (eText != null) {
            if (eText.length() > 100)
                eText = eText.substring(0,100);
            eText = eText.replaceAll("\n", "|");
        }
        String dumpText = "TIT:" + eTitle + ", SUBT:" + eSubT + ", TEXT:" + eText + ", MESSAGE:" + msgText;
        utils.append2file(notifyFile, dumpText);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) { }

    private String getPackageType(String packageFullName){
        for (int idx = 0; idx < packageNames.length; idx++) {
            if (packageFullName.contains(packageNames[idx]))
                return packageTypes[idx];
        }
        return "noType";
    }

    private String getPackageNickName(String packageFullName){
//        utils.log("readGet", "lenxx="+packageTables.length);
        for (int idx = 0; idx < packageNames.length; idx++) {
//            utils.log("idx"+idx,packageNames[idx]);
            if (packageFullName.contains(packageNames[idx]))
                return packageShortNames[idx];
        }
        return "noCode";
    }

    private boolean canBeIgnored(String notiText, String [] Ignores) {
        for (String s : Ignores) {
            if (notiText.contains(s)) return true;
        }
        return false;
    }

    private void speakANDLog(String tag, String text) {
        if (isHeadphonesPlugged() || isRingerON()) {
            if (text2Speech == null) {
                utils.append2file(notifyFile, "tts is null, reCreated");
                text2Speech = new Text2Speech();
                text2Speech.initiateTTS(getApplicationContext());
            }
            text2Speech.speak("잠시만요 " + text);
        }
        String filename = tag + ".txt";
        utils.append2file(filename, text);
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
//        String regex = "[0-9]|-()|\\s";
        String temp = who.replaceAll(getString(R.string.number_only),"");
        return temp.length() <= 2;
    }
}
