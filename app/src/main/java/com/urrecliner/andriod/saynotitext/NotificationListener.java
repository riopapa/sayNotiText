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
import static com.urrecliner.andriod.saynotitext.Vars.kakaoXcludes;
import static com.urrecliner.andriod.saynotitext.Vars.packageCodes;
import static com.urrecliner.andriod.saynotitext.Vars.packageNames;
import static com.urrecliner.andriod.saynotitext.Vars.packageTypes;
import static com.urrecliner.andriod.saynotitext.Vars.packageXcludes;
import static com.urrecliner.andriod.saynotitext.Vars.prepareLists;
import static com.urrecliner.andriod.saynotitext.Vars.smsXcludes;
import static com.urrecliner.andriod.saynotitext.Vars.text2Speech;
import static com.urrecliner.andriod.saynotitext.Vars.utils;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NotificationListener extends NotificationListenerService {

    final String notifyFile = "notification.txt";
    int smsCount = 0;
    String lastTimeLog = "";

    @Override
    public void onCreate() {
        super.onCreate();
//        prepareLists.read();
//        speakANDLog("now","Listener created!");
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
            utils.append2file(notifyFile, "$util is null and reloaded");
        }
        if (text2Speech == null) {
            utils.append2file(notifyFile, "$ts text2Speech is null");
            text2Speech = new Text2Speech();
            text2Speech.initiateTTS(getApplicationContext());
        }
        text2Speech.readyAudioTTS();
        if (packageXcludes == null) {
            prepareLists = new PrepareLists();
            prepareLists.read();
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
        if(isInPackageXcludes(packageName)) {
            return;
        }
        packageType = getPackageType(packageName);
        packageCode = getPackageCode(packageName);

        Notification mNotification=sbn.getNotification();
        Bundle extras = mNotification.extras;
        String eTitle = extras.getString(Notification.EXTRA_TITLE);
        String eText = extras.getString(Notification.EXTRA_TEXT);
        if (eTitle == null && eText == null)
            return;
        if (eText != null) {
            if (eText.length() > 150) {
                eText = eText.substring(0, 150);
                eText = eText.replaceAll("\n\n","|");
                eText = eText.replaceAll("\n","|");
            }
        }
        String nowTimeLog = utils.getTimeStamp();
        if (nowTimeLog.equals(lastTimeLog)) {   // due to "메세지","메세지 보기"
            utils.append2file(notifyFile, "@@@ 동일시각에 두번 title~" + eTitle + ", text~" + eText );
            return;
        }
        lastTimeLog = nowTimeLog;

        utils.append2file(notifyFile, "== addNotification Started ==\npackage type:" + packageType);
        dumpAll(packageCode, notifyFile, eTitle, eText, extras);

        switch (packageType) {
            case KK_KAKAO :
                if (eText != null) {
                    String eSubT = extras.getString(Notification.EXTRA_SUB_TEXT);
                    sayKakao(packageCode, eTitle, eText, eSubT);
                }
                break;
            case TO_TEXT_ONLY :
                speakANDLog(packageCode,  packageCode + ". (메세지입니다.) " + eText);
                break;
            case SM_SMS :
                saySMS(packageCode, eTitle, eText, sbn);
                break;
            case TT_TITLE_TEXT :
                sayTitleText(packageCode, eTitle, eText);
                break;
            case AN_ANDROID :
                sayAndroid(packageCode, eTitle, eText);
                break;
            default :
                if (eTitle.contains("Vaccine")) {
                // ignore
                }
                else {
                    speakANDLog("unknown " + packageName, "title:" + eTitle + "_text:" + eText);
                }
                break;
        }
    }

    private void sayKakao (String packageCode, String eTitle, String eText, String eSubT) {
        if (eSubT != null) {
            if (!isInKakaoXcludes(eSubT)) { // eSub: 채팅방
                speakANDLog(packageCode, "카카오톡." + eSubT + "_단톡방." + eTitle + "_님으로부터." + eText);
            }
        }
        else {
            if(!isInKakaoPersons(eTitle)) {  // eTitle: 개인 이름
                speakANDLog(packageCode, "카카오톡." + eTitle + "_님으로 부터._" + eText);
            }
        }
    }
    private void sayAndroid(String packageCode, String eTitle, String eText) {
        if (eTitle == null || eText == null)
            return;
        final String [] ignoreTables  = {"이(가) 다른 앱 위에", "USB로", "도시락톡", "Wi-Fi", "인터넷 연결 확실치"};
        for (String ignoreTable : ignoreTables) {
            if (eTitle.contains(ignoreTable)) return;
        }
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
            speakANDLog(packageCode,packageCode + ". 메세지입니다. " + eTitle + "_로 부터." + eText);
        }
    }

    private void saySMS(String packageCode, String eTitle, String eText, StatusBarNotification sbn) {

        smsCount++;
        final String filename = "SMS_log.txt";
        if(isInSmsXcludes(eTitle)) {
            utils.append2file(filename, "sms excluded title~" + smsCount + eTitle );
            return;
        }
        if (isNumberOnly(eTitle)) {
            utils.append2file(filename, "sms numbers only title~" + smsCount + eTitle );
            return;
        }
        utils.append2file(filename, "-- sms --- " + smsCount);
        utils.append2file(filename, "sms org title~" + eTitle + ";sms text~" + eText);
        Bundle extras = sbn.getNotification().extras;
//        dumpAll(packageCode,notifyFile, eTitle, text, extras);

        final String msg = "메세지";
    //                assert eText != null;
        if (eTitle.contains(msg)) {
            dumpAll(packageCode,filename, "@@@ title 에 메세지 가 있음 "+ smsCount +  eTitle, eText, extras);
            return;
        }
        if (eText.contains("메세지 보기")) {
            dumpAll(packageCode,filename, "@@@ text에 메세지 보기 " + eTitle, eText, extras);
            return;
        }
        eText = eText.replace("[Web발신]","");

        speakANDLog(packageCode, eTitle + " 로부터 SMS 메세지가 왔어요. ~" + eText + "~");
    }
    
    private void dumpAll (String packageCode, String filename, String eTitle, String eText, Bundle extras){
        String eSubT = extras.getString(Notification.EXTRA_SUB_TEXT);
        if (eText != null)
            eText = eText.replaceAll("\n","|");
        String dumpText = "msg dump ----" + "\nPKGCODE:" + packageCode + ", TITLE:" + eTitle + ", SUBT:" + eSubT + ", TEXT:" + eText;
        String msgText = extras.getString(Notification.EXTRA_MESSAGES);
        if (msgText != null)
            dumpText += ", MESSAGE:" + msgText.replaceAll("\n", "|");
        utils.append2file(filename, dumpText);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) { }

    private boolean isInPackageXcludes(String packageName) {

        if (packageXcludes == null) {
            utils.append2file(notifyFile, "@@ xclude null, so reload prepareLists");
            prepareLists.read();
        }
        for (String packageXclude : packageXcludes) {
            if (packageName.contains(packageXclude)) return true;
        }
        return false;
    }

    private String getPackageType(String packageName){
        for (int idx = 0; idx < packageNames.length; idx++) {
            if (packageName.contains(packageNames[idx])) {
                return packageTypes[idx];
            }
        }
        return "unKnownT";
    }

    private String getPackageCode(String packageName){
        for (int idx = 0; idx < packageNames.length; idx++) {
            if (packageName.contains(packageNames[idx])) {
                return packageCodes[idx];
            }
        }
        return "unKnownC";
    }

    private boolean isInKakaoXcludes(String chatbang) {
        for (String kakaoXclude : kakaoXcludes) {
            if (chatbang.contains(kakaoXclude)) return true;
        }
        return false;
    }

    private boolean isInKakaoPersons(String person){
        for (String kakaoPerson : kakaoPersons) {
            if (person.contains(kakaoPerson)) return true;
        }
        return false;
    }

    private boolean isInSmsXcludes(String smsFrom) {
        for (String smsXclude : smsXcludes) {
            if (smsFrom.contains(smsXclude)) return true;
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
            if (!tag.equals("now"))
                text2Speech.speak("잠시만요 " + text);
        }
        text = text.replaceAll("\n", "|");
        utils.log("speakNlog " + tag, text);
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
        String temp = who.replaceAll("[0-9]|-","");
        if (temp.length() <= 2)
            return true;
        utils.append2file(notifyFile,"Title filtered [" + temp + "]");
        return false;
    }
//    private void logFreeMemory() {
//        try {
//            Runtime info = Runtime.getRuntime();
//            utils.log("_x_",  "totalSZ = " + info.totalMemory() + ", freeSZ = " + info.freeMemory());
//            utils.append2file(directory, "freesize"  + ".txt", "" + info.freeMemory());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
//        else if (packageCode.equals("whatsapp")){
//            eTitle=sbn.getNotification().tickerText.toString();
//            speakANDLog(packageCode,"whatsapp>> from: "+eTitle);
//        }
//        else if(packageCode.equals("facebook")) {
//            StringTokenizer stringTokenizer = new StringTokenizer(sbn.getNotification().tickerText.toString(),":");
////            speakANDLog(packageCode,"Ticker: "+sbn.getNotification().tickerText.toString());
//            eTitle=stringTokenizer.nextToken();
//            eText=stringTokenizer.nextToken();
//            speakANDLog(packageCode,"facebook>>"+"from: "+eTitle+", Text: "+eText);
//        }
