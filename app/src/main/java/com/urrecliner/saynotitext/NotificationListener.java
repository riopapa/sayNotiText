package com.urrecliner.saynotitext;

import android.app.Notification;
import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.urrecliner.saynotitext.Vars.KakaoAGroupWho;
import static com.urrecliner.saynotitext.Vars.kakaoAGroup;
import static com.urrecliner.saynotitext.Vars.kakaoAText;
import static com.urrecliner.saynotitext.Vars.kakaoIgnores;
import static com.urrecliner.saynotitext.Vars.kakaoPersons;
import static com.urrecliner.saynotitext.Vars.packageIgnores;
import static com.urrecliner.saynotitext.Vars.packageIncludeNames;
import static com.urrecliner.saynotitext.Vars.packageNickNames;
import static com.urrecliner.saynotitext.Vars.packageTypes;
import static com.urrecliner.saynotitext.Vars.readOptionTables;
import static com.urrecliner.saynotitext.Vars.smsIgnores;
import static com.urrecliner.saynotitext.Vars.sayMessage;
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
    String eTitle, eText, eSubT;
    String packageFullName, packageNickName, packageType;

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
        final String TT_SUBTITLE_TEXT = "ts";
        final String SM_SMS = "sms";
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

        packageFullName = sbn.getPackageName().toLowerCase();
        if (packageFullName.equals("") || isInTable(packageFullName, packageIgnores))
            return;
        packageType = getPackageType(packageFullName);
        packageNickName = getPackageNickName(packageFullName);

        Notification mNotification=sbn.getNotification();
        Bundle extras = mNotification.extras;
        eTitle = extras.getString(Notification.EXTRA_TITLE);
        eText = extras.getString(Notification.EXTRA_TEXT);
        if (eTitle == null && eText == null) {
            return;
        }
        if (eText != null)
            eText = eText.replaceAll("\n", "|");

        if (eTitle == null) {
            utils.log(logID+" Title null", packageFullName + " Title IS NULL $$$ `null text``` :" + eText);
            return;
        }
        try {
            eSubT = extras.getString(Notification.EXTRA_SUB_TEXT);
        } catch (Exception e) {
            utils.log("eSubt","is SpannableString "+eText+" with "+eText);
            eSubT = null;
        }

//        if (eText != null) {
//            String txt = (eText.length() > 40) ? eText.substring(0, 39)+" ... " : eText;
//            utils.append2file("log [" + eSubT + "].txt", packageFullName + ", tit:" + eTitle + ", Text:" + txt);
//        }
        if (isInTable(eText, textIgnores))
            return;

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
                sayKakao();
                break;
            case SM_SMS :
                saySMS();
                break;
            case TT_TITLE_TEXT :
                sayTitleText();
                break;
            case TO_TEXT_ONLY :
                speakThenLog("to_"+packageNickName,  packageNickName + " (로 부터) " + eText);
                break;
            case TT_SUBTITLE_TEXT :
                saySubTitleText();
                break;
            case AN_ANDROID :
                sayAndroid();
                break;
            default :
                if (isInTable(eTitle, systemIgnores))
                    return;
                if (isInTable(eText, textIgnores))
                    return;
                speakThenLog("unknown " + packageFullName, "unknown title " + eTitle + "_text:" + eText);
//                else
//                    dumpExtras(eTitle, eSubT, eText, msgText);
                break;
        }
    }

    private void sayKakao () {
        if (eText == null)
            return;
//        utils.log("sayKakao "+eSubT, eTitle+" , "+eText);
        if (shouldSpeak(eText, textSpeaks) || shouldSpeak(eTitle, textSpeaks) || shouldSpeak(eSubT, textSpeaks)) {
            speakThenLog(packageNickName+"_"+eSubT, "주의 [" + eTitle + "] 님이. 단톡방 ["+eSubT + "]에서 " + eText);
        }
        else if (isInTable(eTitle, kakaoIgnores) || isInTable(eText, kakaoPersons))
                return;

        if (eSubT != null) {    // eSubT : 단톡방
            if (isInTable(eSubT, kakaoAGroup)) {   // 특정 단톡방에서는
                utils.log("특정 단톡방 "+eSubT, "["+eTitle+"]"+" with "+eText);
                int stockIndex = getStockIndex(eSubT + eTitle);
                if (stockIndex != -1) { // stock open chat
//                    append2App("_stockOpen "+dateFormat.format(new Date()) + ".txt", "stock "+stockIndex+" <"+eSubT+eTitle+">  "+eText);
                    if (eText.contains(kakaoAText[stockIndex])) {
                        append2App("_stockOpen "+dateFormat.format(new Date()) + ".txt", eSubT+" ; "+eTitle+" => "+((eText.length()>80) ? eText.substring(0, 79): eText));
                        if (sayMessage)
                            speakThenLog(packageNickName + "_" + eSubT, "카톡 [" + eTitle + "] 님이. [" + eSubT + "] 단톡방에서 " + eText);
                    }
                }
                else
                    utils.log("단톡방 무시 대상", eTitle+" , "+eSubT+" , "+eText);
            }
            else {
                if (isInTable(eSubT, kakaoIgnores))
                        return;
                if (isInTable(eTitle, kakaoPersons))
                    return;
                speakThenLog(packageNickName+"_"+eSubT, "단체카톡방 [" + eSubT + "] 에서 [" + eTitle + "] 님이." + eText);
            }
        }
        else {
            speakThenLog(packageNickName + "_" + eTitle, "카톡 [" + eTitle + "] 님이." + eText);
        }
    }

    private void sayAndroid() {
        if (eTitle == null || eText == null || eText.equals(""))
            return;
        if (isInTable(eTitle, systemIgnores) || isInTable(eText, systemIgnores))
            return;
        speakThenLog(packageFullName, " Android Title [" + eTitle + "], Text =" + eText);
    }

    private void sayTitleText() {
        if (isInTable(eTitle,systemIgnores) || isInTable(eText, textIgnores) || isInTable(eTitle, textIgnores))
            return;
        speakThenLog(packageNickName+"_"+eTitle,packageNickName + " 에서  [" + eTitle + "]_로 부터. " + eText);
    }

    private void saySubTitleText() {
        if (isInTable(eSubT,systemIgnores) || isInTable(eText, textIgnores) || isInTable(eSubT, textIgnores))
            return;
        speakThenLog(packageNickName,packageNickName + " 에서  [" + eSubT + "]내용으로 . " + eText);
    }

    private void saySMS() {
        if (isOnlyPhoneNumber(eTitle) || isInTable(eTitle, smsIgnores) || isInTable(eText, textIgnores))
            return;

        eText = eText.replace("[Web발신]","");
        speakThenLog(packageNickName+"_"+eTitle, eTitle + " 로부터 문자메시지 왔음. " + eText);
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

    private boolean isInTable(String text, String [] lists) {
        if (text == null)
            return false;
        for (String s : lists) {
            if (text.contains(s)) return true;
        }
        return false;
    }

    private int getStockIndex(String text) {
        for (int idx = 0; idx < KakaoAGroupWho.length; idx++) {
            if (text.contains(KakaoAGroupWho[idx]))
                return idx;
        }
        return -1;
    }
    private boolean shouldSpeak(String text, String [] speaks) {
        if (text == null)
            return false;
        for (String s : speaks) {
            if (text.contains(s)) return true;
        }
        return false;
    }

    private void speakThenLog(String tag, String text) {
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

    private final SimpleDateFormat hourMinFormat = new SimpleDateFormat("yy-MM-dd HH.mm", Locale.KOREA);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd", Locale.KOREA);

    private void append2App(String filename, String textLine) {
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory(),"download/"+filename);
            if (!file.exists())
                file.createNewFile();
            String outText = "\n" + hourMinFormat.format(new Date()) + " "  + textLine + "\n";
            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(outText);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
