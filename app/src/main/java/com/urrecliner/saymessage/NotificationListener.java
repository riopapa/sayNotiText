package com.urrecliner.saymessage;

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

import static com.urrecliner.saymessage.Vars.kTalkAGroupWho;
import static com.urrecliner.saymessage.Vars.isPhoneBusy;
import static com.urrecliner.saymessage.Vars.kTalkAGroup;
import static com.urrecliner.saymessage.Vars.kTalkAKey1;
import static com.urrecliner.saymessage.Vars.kTalkAKey2;
import static com.urrecliner.saymessage.Vars.kTalkIgnores;
import static com.urrecliner.saymessage.Vars.kTalkSaved;
import static com.urrecliner.saymessage.Vars.kTalkSay;
import static com.urrecliner.saymessage.Vars.mActivity;
import static com.urrecliner.saymessage.Vars.mContext;
import static com.urrecliner.saymessage.Vars.oldMessage;
import static com.urrecliner.saymessage.Vars.packageIgnores;
import static com.urrecliner.saymessage.Vars.packageIncludeNames;
import static com.urrecliner.saymessage.Vars.packageNickNames;
import static com.urrecliner.saymessage.Vars.packageTypes;
import static com.urrecliner.saymessage.Vars.smsIgnores;
import static com.urrecliner.saymessage.Vars.speakOnOff;
import static com.urrecliner.saymessage.Vars.systemIgnores;
import static com.urrecliner.saymessage.Vars.text2Speech;
import static com.urrecliner.saymessage.Vars.textIgnores;
import static com.urrecliner.saymessage.Vars.tvOldMessage;
import static com.urrecliner.saymessage.Vars.utils;

public class NotificationListener extends NotificationListenerService {

    final String TT_TITLE_TEXT = "tt";
    final String SM_SMS = "sms";
    final String KK_TALK = "kk";
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
        if (packageIgnores == null)
            new ReadOptionTables().read();

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
            case KK_TALK:
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

    private void sayKaTalk() {
        if (eText == null)
            return;
        if (isInTable(eWho, kTalkIgnores))
            return;
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
        if (isInTable(eGroup, kTalkIgnores))
            return;

        if (isInTable(eGroup, kTalkAGroup)) {   // 특정 단톡방
                int aIdx = getAlertIndex(eGroup + eWho, 0);
                if (aIdx != -1) { // stock open chat
                    if (eText.equals(kTalkSaved[aIdx])) // 같은 소리 계속 하는 건 빼자
                        return;
                    if (eText.contains(kTalkAKey1[aIdx]) && eText.contains(kTalkAKey2[aIdx])) {
                        sayStockAlert(aIdx);
                    } else {
                       while (++aIdx < kTalkAGroupWho.length) { // 한사람이 여러 키워드를 말할 수도 있어서 로직 추가
                           aIdx = getAlertIndex(eGroup + eWho, aIdx);
                           if (aIdx >= 0 && eText.contains(kTalkAKey1[aIdx]) && eText.contains(kTalkAKey2[aIdx]))
                               sayStockAlert(aIdx);
                           else
                               break;
                       }
                    }
                }
        } else
             logBeepThenSpeak(eGroup +"_단톡", "단톡방 [" + eGroup + "] 에서 [" + eWho + "] 님이; " + eText);
        savedText = eText;
    }

    private int getAlertIndex(String text, int sIdx) {  // sIdx is used for same person with diff keys
        for (int idx = sIdx; idx < kTalkAGroupWho.length; idx++) {
            int compared= text.compareTo(kTalkAGroupWho[idx]);
            if (compared == 0)
                return idx;
            if (compared < 0)
                return -1;
        }
        return -1;
    }

    private void sayStockAlert(int aIdx) {
        s = (eText.length()>110) ? eText.substring(0, 109): eText;
        append2App("/_stocks/"+ eGroup + ".txt",eGroup, eWho, s);
        kTalkSaved[aIdx] = eText;
        if (speakOnOff || kTalkSay[aIdx].length() > 1) {
            s  = kTalkSay[aIdx]+ "[" + eGroup + " " + kTalkSay[aIdx]+ " " +
                    eWho + " 님이. " + kTalkSay[aIdx]+ " "+eText;
            beepNSpeak(s, 45,"");
            updateNotification("["+eGroup+":"+eWho+"]"+eText);
        }
    }

    private void sayAndroid() {
        if (eWho == null || eText == null || eText.equals("")
                || (isInTable(eWho, systemIgnores) || isInTable(eText, systemIgnores)))
            return;
        logBeepThenSpeak(packageFullName, " Android Title [" + eWho + "], Text =" + eText);
    }

    private void sayNHStock() {
        String s = eText.contains("매수") ? " 시세포착 ": "";
        logBeepThenSpeak(packageNickName, eWho + "_로 연락옴. " + s+ eText);
        append2App("/_"+ packageNickName + ".txt", packageNickName, eWho, eText);
        updateNotification("[NH:"+eWho+"]"+eText);
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
