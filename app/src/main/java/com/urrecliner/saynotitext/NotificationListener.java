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

import static com.urrecliner.saynotitext.Vars.KakaoAlertGWho;
import static com.urrecliner.saynotitext.Vars.kakaoAlertGroup;
import static com.urrecliner.saynotitext.Vars.kakaoAlertText;
import static com.urrecliner.saynotitext.Vars.kakaoIgnores;
import static com.urrecliner.saynotitext.Vars.kakaoPersons;
import static com.urrecliner.saynotitext.Vars.packageIgnores;
import static com.urrecliner.saynotitext.Vars.packageIncludeNames;
import static com.urrecliner.saynotitext.Vars.packageNickNames;
import static com.urrecliner.saynotitext.Vars.packageTypes;
import static com.urrecliner.saynotitext.Vars.readOptionTables;
import static com.urrecliner.saynotitext.Vars.smsIgnores;
import static com.urrecliner.saynotitext.Vars.sayStockOnOff;
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
        if (packageFullName.equals("") || isInTheList(packageFullName, packageIgnores))
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

        if (eText != null) {
            String txt = (eText.length() > 40) ? eText.substring(0, 39)+" ... " : eText;
            utils.append2file("log [" + eSubT + "].txt", packageFullName + ", tit:" + eTitle + ", Text:" + txt);
        }
        if (isInTheList(eText, textIgnores))
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
//                utils.append2file("kakao "+eSubT+".txt", "tit:"+eTitle+", Text:"+eText);
                if (eText != null) {
                    sayKakao();
                } else {
                    speakThenLog(packageNickName+" noText",  packageNickName + " (카카오) " + eSubT);
                }
                break;
            case SM_SMS :
                saySMS();
                break;
            case TT_TITLE_TEXT :
                sayTitleText();
                break;
            case TO_TEXT_ONLY :
                speakThenLog("to "+packageNickName,  packageNickName + " (로 부터) " + eText);
                break;
            case TT_SUBTITLE_TEXT :
                saySubTitleText();
                break;
            case AN_ANDROID :
                sayAndroid();
                break;
            default :
                if (isInTheList(eTitle, systemIgnores))
                    return;
                if (isInTheList(eText, textIgnores))
                    return;
                speakThenLog("unknown " + packageFullName, "unknown title " + eTitle + "_text:" + eText);
//                else
//                    dumpExtras(eTitle, eSubT, eText, msgText);
                break;
        }
    }

    private void sayKakao () {
        utils.log("sayKakao "+eSubT, eTitle+" , "+eText);
        if (shouldSpeak(eText, textSpeaks) || shouldSpeak(eTitle, textSpeaks) || shouldSpeak(eSubT, textSpeaks)) {
            speakThenLog(packageNickName+" "+eSubT, "주의 [" + eTitle + "] 님이. 단톡방 ["+eSubT + "]에서 " + eText);
        }
        else if (isInTheList(eTitle, kakaoIgnores) || isInTheList(eText, kakaoPersons))
                return;

        if (eSubT != null) {    // eSubT : 단톡방
            if (isInTheList(eSubT, kakaoAlertGroup)) {   // 특정 단톡방에서는
                utils.log("특정 단톡방 "+eSubT, "["+eSubT+eTitle+"]"+" with "+eText);
                if (isInTheList(eSubT + eTitle, KakaoAlertGWho) && isInTheList(eText, kakaoAlertText)) {
                    if (sayStockOnOff)
                        speakThenLog(packageNickName + " " + eSubT, "카톡 [" + eTitle + "] 님이. [" + eSubT + "] 단톡방에서 " + eText);
                    append2App("_Stock.txt", eSubT+" ; "+eTitle+" => "+((eText.length()>30) ? eText.substring(0, 30): eText));
                }
                // 아니면 해당 단톡방 무시
                // Group, 대화자, 인식문자 는 서로 연결 안 됨 ㅠ.ㅠ
                else
                    utils.log("단톡방 무시 대상", eTitle+" , "+eSubT+" , "+eText);
            }
            else if (!isInTheList(eSubT, kakaoIgnores) && !isInTheList(eTitle, kakaoPersons)) {
                speakThenLog(packageNickName+" "+eSubT, "단톡방 [" + eSubT + "] 에서 [" + eTitle + "] 님이." + eText);
            }
            else {
                utils.log("katalk ignored", eTitle+" , "+eSubT+" , "+eTitle);
            }
        }
        else
            speakThenLog(packageNickName+" "+eTitle, "카톡 [" + eTitle + "] 님이." + eText);
    }

    private void sayAndroid() {

        if (eTitle == null || eText == null || eText.equals(""))
            return;
        if (isInTheList(eTitle, systemIgnores) || isInTheList(eText, systemIgnores))
            return;
        speakThenLog(packageFullName, " Android Title [" + eTitle + "], Text =" + eText);
    }

    private void sayTitleText() {
        if (isInTheList(eTitle,systemIgnores) || isInTheList(eText, textIgnores) || isInTheList(eTitle, textIgnores))
            return;
        speakThenLog(packageNickName+" "+eTitle,packageNickName + " 에서  [" + eTitle + "]_로 부터. " + eText);
    }

    private void saySubTitleText() {
        if (isInTheList(eSubT,systemIgnores) || isInTheList(eText, textIgnores) || isInTheList(eSubT, textIgnores))
            return;
        speakThenLog(packageNickName,packageNickName + " 에서  [" + eSubT + "]내용으로 . " + eText);
    }

    private void saySMS() {
        if (isOnlyPhoneNumber(eTitle) || isInTheList(eTitle, smsIgnores) || isInTheList(eText, textIgnores))
            return;

        eText = eText.replace("[Web발신]","");
        speakThenLog(packageNickName+" "+eTitle, eTitle + " 로부터 문자메시지 왔음. " + eText);
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

    private boolean isInTheList(String text, String [] lists) {
        if (text == null)
            return false;
        for (String s : lists) {
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

    private final File packageDirectory = new File(Environment.getExternalStorageDirectory(), "sayNotiText");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("yy-MM-dd HH.mm.ss sss", Locale.KOREA);

    private void append2App(String filename, String textLine) {
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            File file = new File(packageDirectory, filename);
            if (!file.exists()) {
                if (!file.createNewFile()) {

                }
            }
            String outText = "\n" + timeFormat.format(new Date()) + " "  + textLine + "\n";
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
