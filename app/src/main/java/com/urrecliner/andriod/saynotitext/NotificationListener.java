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

import static com.urrecliner.andriod.saynotitext.Vars.kakaoPersons;
import static com.urrecliner.andriod.saynotitext.Vars.kakaoIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.packageCodes;
import static com.urrecliner.andriod.saynotitext.Vars.packageNames;
import static com.urrecliner.andriod.saynotitext.Vars.packageTypes;
import static com.urrecliner.andriod.saynotitext.Vars.packageIgnores;
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
    String lastTimeLog = "";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn != null) {
            addNotification(sbn);
        }
    }

    //    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void addNotification(StatusBarNotification sbn)  {

        if (utils == null) {
            utils = new Utils();
            utils.append2file(notifyFile, "$$ UTIL IS NULL AND RELOADED " + ++utilsCount);
        }
        if (text2Speech == null) {
            utils.append2file(notifyFile, "$$ TS TEXT2SPEECH IS NULL " + ++speechCount);
            text2Speech = new Text2Speech();
            text2Speech.initiateTTS(getApplicationContext());
        }
        text2Speech.readyAudioTTS();
        if (packageIgnores == null) {
            prepareLists = new PrepareLists();
            prepareLists.read();
            utils.append2file(notifyFile, "$$ PREPARE IS NULL " + ++listCount);
        }
        String packageName = sbn.getPackageName().toLowerCase();
        String packageCode, packageType;
        final String TT_TITLE_TEXT = "tt";
        final String SM_SMS = "sm";
        final String KK_KAKAO = "kk";
        final String AN_ANDROID = "an";
        final String TO_TEXT_ONLY = "to";
        if (packageName.equals("")) {
            return;
        }
        if(canBeIgnored(packageName, packageIgnores))
            return;

        packageType = getPackageType(packageName);
        packageCode = getPackageCode(packageName);

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
            eText = eText.replaceAll("\n\n","|");
            eText = eText.replaceAll("\n","|");
        }
        String nowTimeLog = utils.getTimeStamp();
        if (nowTimeLog.equals(lastTimeLog)) {   // due to "메세지","메세지 보기"
//            utils.append2file(notifyFile, "@@@ 동일시각에 두번 title~" + eTitle + ", text~" + eText );
            return;
        }
        lastTimeLog = nowTimeLog;

        utils.append2file(notifyFile, "== Start == type: " + packageType + ", code: " + packageCode);
        String eSubT = extras.getString(Notification.EXTRA_SUB_TEXT);
        String msgText = extras.getString(Notification.EXTRA_MESSAGES);
        dumpExtras(eTitle, eSubT, eText, msgText);

        switch (packageType) {
            case KK_KAKAO :
                if (eText != null) {
                    sayKakao(packageCode, eTitle, eSubT, eText);
                }
                break;
            case TO_TEXT_ONLY :
                speakANDLog(packageCode,  packageCode + " (메세지입니다) " + eText);
                break;
            case SM_SMS :
                saySMS(packageCode, eTitle, eText);
                break;
            case TT_TITLE_TEXT :
                sayTitleText(packageCode, eTitle, eText);
                break;
            case AN_ANDROID :
                sayAndroid(packageCode, eTitle, eText);
                break;
            default :
                if (eTitle != null && !eTitle.contains("Vaccine")) {
                    speakANDLog("unknown " + packageName, "title:" + eTitle + "_text:" + eText);
                }
                break;
        }
    }

    private void sayKakao (String packageCode, String eTitle, String eSubT, String eText) {
        if (eSubT != null) {
            if (!canBeIgnored(eSubT, kakaoIgnores)) { // eSub: 채팅방
                speakANDLog(packageCode, "카카오톡 " + eSubT + "_단톡방 " + eTitle + "_님으로부터." + eText);
            }
        }
        else {
            if(!canBeIgnored(eTitle, kakaoPersons)) {  // eTitle: 개인 이름
                speakANDLog(packageCode, "카카오톡." + eTitle + "_님으로 부터._" + eText);
            }
        }
    }
    private void sayAndroid(String packageCode, String eTitle, String eText) {
        if (eTitle == null || eText == null || eText.equals("") || canBeIgnored(eTitle, systemIgnores) || canBeIgnored(eText, systemIgnores))
            return;
        speakANDLog(packageCode,"eTitle ~" + eTitle + " eText~" + eText);
    }

    private void sayTitleText(String packageCode, String eTitle, String eText) {
        if (packageCode.equals("밴드")) {
            if (eTitle.contains("읽지 않은"))
                return;
        }
        else if (packageCode.equals("씨티은행")) {
            if (eTitle.contains("Vaccine"))
                return;
        }
        if (eText != null) {
            speakANDLog(packageCode,packageCode + " 메세지입니다. " + eTitle + "_로 부터 " + eText);
        }
    }

    private void saySMS(String packageCode, String eTitle, String eText) {

        smsCount++;
        if (isNumberOnly(eTitle)) {
            return;
        }
        if (canBeIgnored(eTitle, smsIgnores)) {
            return;
        }
        eText = eText.replace("[Web발신]","");
        speakANDLog(packageCode, eTitle + " 로부터 SMS 메세지가 왔어요 " + eText);
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

    private String getPackageType(String packageName){
        for (int idx = 0; idx < packageNames.length; idx++) {
            if (packageName.contains(packageNames[idx])) {
                return packageTypes[idx];
            }
        }
        return "noType";
    }

    private String getPackageCode(String packageName){
        for (int idx = 0; idx < packageNames.length; idx++) {
            if (packageName.contains(packageNames[idx])) {
                return packageCodes[idx];
            }
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

    private boolean isNumberOnly(String who) {
        String regex = "[0-9]|-()|\\s";
        String temp = who.replaceAll(regex,"");
        return temp.length() <= 2;
    }
}
