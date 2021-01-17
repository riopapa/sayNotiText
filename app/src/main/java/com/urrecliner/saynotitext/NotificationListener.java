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
import static com.urrecliner.saynotitext.Vars.kakaoAKey1;
import static com.urrecliner.saynotitext.Vars.kakaoAKey2;
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

    final String logID = "Listener";
    private long lastTime = 0;
    private String lastAppName = "last";
    String eWho, eText, eGroup, lastWho;
    String packageFullName, packageNickName, packageType;
    final String TT_TITLE_TEXT = "tt";
    final String TT_SUBTITLE_TEXT = "ts";
    final String SM_SMS = "sms";
    final String KK_KAKAO = "kk";
    final String AN_ANDROID = "an";
    final String TO_TEXT_ONLY = "to";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null)
            return;
        if (utils == null) {
            utils = new Utils();
            text2Speech = new Text2Speech();
            text2Speech.initiateTTS(getApplicationContext());
        }
        checkTables();

        packageFullName = sbn.getPackageName().toLowerCase();
        if (packageFullName.equals("") || isInTable(packageFullName, packageIgnores)) {
//            utils.log(logID,"pkg =" + packageFullName+" IGNORED .");
            return;
        }
        Notification mNotification=sbn.getNotification();
        Bundle extras = mNotification.extras;
        eWho = extras.getString(Notification.EXTRA_TITLE);
        if (eWho == null)
            return;
        try {
            eText = extras.getString(Notification.EXTRA_TEXT);
            eText = eText.replace("\n", "|");
        } catch (Exception e) {
            return;
        }

        try {
            eGroup = extras.getString(Notification.EXTRA_SUB_TEXT);
        } catch (Exception e) {
            utils.log("Grp","is SpannableString "+eText+" with "+eText);
            eGroup = null;
        }
        if (isInTable(eText, textIgnores) || (isInTable(eWho, textIgnores)) ||
                (eGroup != null && (isInTable(eGroup, textIgnores))))
            return;

        long nowTime = System.currentTimeMillis();
        if (lastWho != null && lastWho.equals(eWho) && lastAppName.equals(packageFullName)) {
            if ((lastTime + 1000) > nowTime ) {
                lastTime = nowTime;
                return;
            }
        }
        lastTime = nowTime;
        lastWho = eWho;
        lastAppName = packageFullName;
        packageType = getPackageType(packageFullName);
        packageNickName = getPackageNickName(packageFullName);

//        String msgText;
//        try {
//            msgText = extras.getString(Notification.EXTRA_MESSAGES);
//        } catch (Exception e) {
//            msgText = null;
//        }

//        dumpExtras(eTitle, Grp, eText, msgText);
//        utils.log(logID, "Type "+packageType+", Full "+packageFullName+", Nick "+packageNickName+
//                ", who "+eWho+", text "+((eText.length()> 50)? eText.substring(0,49):eText));
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
                speakThenLog(packageNickName,  packageNickName + " 로 부터 " + eText);
                break;
            case TT_SUBTITLE_TEXT :
                saySubTitleText();
                break;
            case AN_ANDROID :
                sayAndroid();
                break;
            default :
                if (isInTable(eWho, systemIgnores))
                    return;
                if (isInTable(eText, textIgnores))
                    return;
                speakThenLog("unknown " + packageFullName, "unknown title " + eWho + "_text:" + eText);
//                else
//                    dumpExtras(eTitle, Grp, eText, msgText);
                break;
        }
    }

    private void checkTables() {
        if (packageIgnores == null) {
            readOptionTables = new ReadOptionTables();
            readOptionTables.read();
            utils.log(logID, "$$ Table REloaded ");
        }
    }

    private void sayKakao () {
        if (eText == null)
            return;
        if (shouldSpeak(eText, textSpeaks) || shouldSpeak(eWho, textSpeaks) || shouldSpeak(eGroup, textSpeaks)) {
            speakThenLog(eGroup + "_" +packageNickName , "주의 주의 [" + eWho + "] 님이. 단톡방 ["+ eGroup + "]에서 " + eText);
        }
        else if (isInTable(eWho, kakaoIgnores) || isInTable(eText, kakaoPersons)) {
            return;
        }

        if (eGroup != null) {    // Grp : 단톡방
            if (isInTable(eGroup, kakaoAGroup)) {   // 특정 단톡방
                int alertIdx = getAlertIndex(eGroup + eWho);
//                utils.log("Alert Grp "+ eGroup, "["+ eWho +"]"+" idx="+alertIdx);
                if (alertIdx != -1) { // stock open chat
                    if (eText.contains(kakaoAKey1[alertIdx]) && eText.contains(kakaoAKey2[alertIdx])) {
                        append2App("_stockOpen "+dateFormat.format(new Date()) + ".txt", eGroup +" ; "+ eWho,
                                ((eText.length()>60) ? eText.substring(0, 59): eText));
                        if (sayMessage) {
                            speakThenLog(eGroup + "_오챗" , "카톡 [" + eWho + "] 님이. [" + eGroup + "] 단톡방에서 "
                                    + ((eText.length()>50) ? eText.substring(0, 49): eText));
                        }
                    }
                }
            }
            else {
                if (isInTable(eGroup, kakaoIgnores))
                        return;
                if (isInTable(eWho, kakaoPersons))
                    return;
                speakThenLog(eGroup +"_단톡", "단톡방 [" + eGroup + "] 에서 [" + eWho + "] 님이." + eText);
            }
        }
        else {
            speakThenLog( eWho + "_카톡", "카톡 [" + eWho + "] 님이." + eText);
        }
    }

    private void sayAndroid() {
        if (eWho == null || eText == null || eText.equals(""))
            return;
        if (isInTable(eWho, systemIgnores) || isInTable(eText, systemIgnores))
            return;
        speakThenLog(packageFullName, " Android Title [" + eWho + "], Text =" + eText);
    }

    private void sayTitleText() {
        if (isInTable(eWho,systemIgnores) || isInTable(eText, textIgnores) || isInTable(eWho, textIgnores))
            return;
        speakThenLog(packageNickName+"_"+ eWho,"["+packageNickName + "] 에서  [" + eWho + "]_로 부터. " + eText);
    }

    private void saySubTitleText() {
        if (isInTable(eGroup,systemIgnores) || isInTable(eText, textIgnores) || isInTable(eGroup, textIgnores))
            return;
        speakThenLog(packageNickName,packageNickName + " 에서  [" + eGroup + "]내용으로 . " + eText);
    }

    private void saySMS() {
        if (isOnlyPhoneNumber(eWho) || isInTable(eWho, smsIgnores) || isInTable(eText, textIgnores))
            return;

        eText = eText.replace("[Web발신]","");
        speakThenLog(eWho + "_" + packageNickName , eWho + " 로부터 문자메시지 왔음. " + eText);
    }
    
//    private void dumpExtras(String eTitle, String Grp, String eText, String msgText){
//        if (eText != null) {
//            if (eText.length() > 100)
//                eText = eText.substring(0,100);
//            eText = eText.replaceAll("\n", "|");
//        }
//        String dumpText = "TIT:" + eTitle + ", SUBT:" + Grp + ", TEXT:" + eText + ", MESSAGE:" + msgText;
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

    private int getAlertIndex(String text) {
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

    private void append2App(String filename, String groupWho, String textLine) {
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory(),"download/"+filename);
//            File file = new File (getAbsoluteFile("download",getApplicationContext()), filename);
            if (!file.exists())
                file.createNewFile();
            String outText = "\n" + groupWho+" " + hourMinFormat.format(new Date()) + " "  + textLine;
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

    private File getAbsoluteFile(String relativePath, Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return new File(context.getExternalFilesDir(null), relativePath);
        } else {
            return new File(context.getFilesDir(), relativePath);
        }
    }
}
