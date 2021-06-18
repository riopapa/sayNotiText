package com.urrecliner.saynotitext;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.saynotitext.Vars.KakaoAGroupWho;
import static com.urrecliner.saynotitext.Vars.isPhoneBusy;
import static com.urrecliner.saynotitext.Vars.kakaoAGroup;
import static com.urrecliner.saynotitext.Vars.kakaoAKey1;
import static com.urrecliner.saynotitext.Vars.kakaoAKey2;
import static com.urrecliner.saynotitext.Vars.kakaoIgnores;
import static com.urrecliner.saynotitext.Vars.kakaoPersons;
import static com.urrecliner.saynotitext.Vars.kakaoSaved;
import static com.urrecliner.saynotitext.Vars.kakaoTalk;
import static com.urrecliner.saynotitext.Vars.mActivity;
import static com.urrecliner.saynotitext.Vars.mContext;
import static com.urrecliner.saynotitext.Vars.oldMessage;
import static com.urrecliner.saynotitext.Vars.packageIgnores;
import static com.urrecliner.saynotitext.Vars.packageIncludeNames;
import static com.urrecliner.saynotitext.Vars.packageNickNames;
import static com.urrecliner.saynotitext.Vars.packageTypes;
import static com.urrecliner.saynotitext.Vars.readOptionTables;
import static com.urrecliner.saynotitext.Vars.smsIgnores;
import static com.urrecliner.saynotitext.Vars.speakOnOff;
import static com.urrecliner.saynotitext.Vars.systemIgnores;
import static com.urrecliner.saynotitext.Vars.text2Speech;
import static com.urrecliner.saynotitext.Vars.textIgnores;
import static com.urrecliner.saynotitext.Vars.tvOldMessage;
import static com.urrecliner.saynotitext.Vars.utils;

public class NotificationListener extends NotificationListenerService {

    final String TT_TITLE_TEXT = "tt";
    final String SM_SMS = "sms";
    final String KK_KAKAO = "kk";
    final String AN_ANDROID = "an";
    final String NAH_MOO = "nh";
    final String TO_TEXT_ONLY = "to";
    final String DELIMITER = " ~ ";
    private long lastTime = 0;
    private String lastAppName = "없음";
    String eWho, eText, lastWho;
    String eGroup = null;
    String packageFullName, packageNickName, packageType;
    String savedText = "", s;
    AudioManager audioManager;

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
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
        readyTables();

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
            eGroup = ""+extras.getString(Notification.EXTRA_SUB_TEXT);  // if no Group then return String "null"
            if (eGroup.equals("null"))
                eGroup = null;
        } catch (Exception e) {
            utils.logE("Grp","is SpannableString "+eText+" with "+eText);
            eGroup = null;
        }

        try {
            eText = ""+extras.get(Notification.EXTRA_TEXT);
            if (eText.equals("null"))
                return;
            eText = eText.replaceAll("[\\n]", "|");
        } catch (Exception e) {
            return;
        }
        if (isInTable(eText, textIgnores) || isInTable(eWho, textIgnores) || isInTable(eGroup, textIgnores))
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
        switch (packageType) {
            case KK_KAKAO :
                sayKaTalk();
                break;
            case SM_SMS :
                saySMS();
                break;
            case TT_TITLE_TEXT :
                sayTitleText();
                break;
            case TO_TEXT_ONLY :
                logBeepThenSpeak(packageNickName,  packageNickName + " 로 부터 " + eText);
                updateNotification("["+packageNickName+"]"+eText);
                break;
            case AN_ANDROID :
                sayAndroid();
                break;
            case NAH_MOO :
                sayNHStock();
                break;
            default :
                if (!isInTable(eWho, systemIgnores)) {
                    logBeepThenSpeak("unknown " + packageFullName, "unknown " + eWho + "_text:" + eText);
                    updateNotification("["+packageNickName+"]"+eText);
                }
                break;
        }
    }

    private void readyTables() {
        if (packageIgnores == null) {
            utils.logE("table","** not READY **");
            readOptionTables = new ReadOptionTables();
            readOptionTables.read();
        }
    }

    private void sayKaTalk() {
        if (eText == null)
            return;
        if (isInTable(eWho, kakaoIgnores) || isInTable(eText, kakaoPersons)) {
            return;
        }
        if (eGroup == null) {
            logBeepThenSpeak(eWho + "_카톡", "카톡 [" + eWho + "] 님이." + eText);
            updateNotification("["+eWho+"]"+eText);
        }
        else
            groupTalk();
    }

    private void groupTalk() {
        if (eText.equals(savedText))
            return;
        utils.log(eGroup+";"+eWho, eText);
        if (isInTable(eGroup, kakaoIgnores) || isInTable(eWho, kakaoPersons))
            return;

        if (isInTable(eGroup, kakaoAGroup)) {   // 특정 단톡방
                int aIdx = getAlertIndex(eGroup + eWho);
                if (aIdx != -1) { // stock open chat
                    if (eText.equals(kakaoSaved[aIdx])) // 같은 소리 계속 하는 건 빼자
                        return;
                    if (eText.contains(kakaoAKey1[aIdx]) && eText.contains(kakaoAKey2[aIdx])) {
                        s = (eText.length()>110) ? eText.substring(0, 109): eText;
//                        append2App("_stock "+dateFormat.format(new Date()) + ".txt",eGroup, eWho, s);
                        append2App("/_stocks/"+ eGroup + ".txt",eGroup, eWho, s);
//                        append2App("/stocks/merged.txt",eGroup, eWho, s);
                        if (speakOnOff || kakaoTalk[aIdx].length() > 1) {
                            s  = kakaoTalk[aIdx]+ "[" + eGroup + " " + kakaoTalk[aIdx]+ " " +
                                    eWho + " 님이. " + kakaoTalk[aIdx]+ " "+eText;
                            beepNSpeak(s, 45,"");
                            updateNotification("["+eGroup+"]"+eText);
                        }
                    }
                    kakaoSaved[aIdx] = eText;
                }
        } else
             logBeepThenSpeak(eGroup +"_단톡", "단톡방 [" + eGroup + "] 에서 [" + eWho + "] 님이; " + eText);
        savedText = eText;
    }

    private void sayAndroid() {
        if (eWho == null || eText == null || eText.equals("")
                || (isInTable(eWho, systemIgnores) || isInTable(eText, systemIgnores)))
            return;
        logBeepThenSpeak(packageFullName, " Android Title [" + eWho + "], Text =" + eText);
    }

    private void sayNHStock() {
        String s = eText.contains("매수") ? " 주식 삼, 시세포착 ": "";
        logBeepThenSpeak(packageNickName, eWho + "_로 연락옴. " + s+ eText);
        append2App("/_"+ packageNickName + ".txt", packageNickName, eWho, eText);
        updateNotification("[NH]"+eText);
    }

    private void sayTitleText() {
        if (eText == null)
            return;
        if (isInTable(eWho,systemIgnores) || isInTable(eText, textIgnores) || isInTable(eWho, textIgnores))
            return;
        String groupName = (eGroup == null) ? " " : "[" + eGroup+" 팀]의 ";
        logBeepThenSpeak(packageNickName,"["+packageNickName + "] 에서 " + groupName + eWho + "_로 부터. " + eText);
    }

    private void saySMS() {
        if (isOnlyPhoneNumber(eWho) || isInTable(eWho, smsIgnores) || isInTable(eText, textIgnores))
            return;
        eText = eText.replace("[Web발신]","");
        logBeepThenSpeak(eWho + "_" + packageNickName , eWho + " 로부터 문자메시지 왔음. " + eText);
        updateNotification("[SMS]"+eText);
    }

    private void updateNotification(String msg) {
        Intent updateIntent = new Intent(mContext, NotificationService.class);
        s = (msg.length() > 81) ? msg.substring(0,80) : msg;
        updateIntent.putExtra("operation", 1234);
        updateIntent.putExtra("msg", s);
        startService(updateIntent);
        squeezeOldMessage(new SimpleDateFormat("HH:mm ", Locale.KOREA).format(new Date()) + s);
        mActivity.runOnUiThread(() -> tvOldMessage.setText(oldMessage));
    }

//    private void dumpExtras(String eTitle, String Grp, String eText, String msgText){
//        if (eText != null) {
//            if (eText.length() > 100)
//                eText = eText.substring(0,100);
//            eText = eText.replaceAll("\n", "|");
//        }
//        String dumpText = "TIT:" + eTitle + ", Group:" + Grp + ", TEXT:" + eText + ", MESSAGE:" + msgText;
//        utils.log(logID, dumpText);
//    }

    void squeezeOldMessage(String sIn) {
        oldMessage += sIn + "\n";
        if (StringUtils.countMatches(oldMessage, "\n") > 7) {
            String[] s = oldMessage.split("\n");
            oldMessage = "";
            for (int i = s.length - 7; i < s.length; i++) {
                oldMessage = oldMessage + s[i] + "\n";
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) { }

    private String getPackageType(String packageFullName){
        for (int idx = 0; idx < packageIncludeNames.length; idx++) {
            if (packageFullName.contains(packageIncludeNames[idx]))
                return packageTypes[idx];
        }
        return "notFnd";
    }

    private String getPackageNickName(String packageFullName){
        for (int idx = 0; idx < packageIncludeNames.length; idx++) {
            if (packageFullName.contains(packageIncludeNames[idx]))
                return packageNickNames[idx];
        }
        return "noNick";
    }

    private boolean isInTable(String text, String [] lists) {
        if (text != null) {
            for (String s : lists) {
                if (text.contains(s)) return true;
            }
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

//    private void logThenSpeech(String logFile, String text, Integer... txtLen) {
    private void logBeepThenSpeak(String logFile, String text) {
        utils.append2file(logFile + ".txt", ((isPhoneBusy)? "폰 비지 busy ":" ")+ text);
        if (!isPhoneBusy) {
            if ((isHeadphonesPlugged() || isRingerON()))
                beepNSpeak(text, 200, " 등등");
        }
    }

    private void beepNSpeak(String text, int i, String added) {
        if (text2Speech == null) {
            text2Speech = new Text2Speech();
            text2Speech.initiateTTS(getApplicationContext());
        }
        utils.beepOnce(1);
        if ((isHeadphonesPlugged() || isRingerON())) {
            final String fText = (text.length() > i)? text.substring(0, i) + added : text;
            new Timer().schedule(new TimerTask() {
                public void run () {
                    text2Speech.speak(fText);
                }
            }, 1000);
        }
    }

    private boolean isRingerON() {
        return audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
    }

    private boolean isHeadphonesPlugged(){
        AudioDeviceInfo[] audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);
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
        return who.replaceAll(getString(R.string.number_only),"").length() <= 2;
    }

    private final SimpleDateFormat hourMinFormat = new SimpleDateFormat("yy-MM-dd HH.mm", Locale.KOREA);

    private void append2App(String filename, String group, String who, String textLine) {
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory(),"download/"+filename);
            if (!file.exists() && !file.createNewFile())
                return;
            String outText = group + DELIMITER + hourMinFormat.format(new Date()) + DELIMITER
                    + who + DELIMITER  + DELIMITER + textLine + "\n";
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

//    private File getAbsoluteFile(String relativePath, Context context) {
//        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
//            return new File(context.getExternalFilesDir(null), relativePath);
//        } else {
//            return new File(context.getFilesDir(), relativePath);
//        }
//    }
}
