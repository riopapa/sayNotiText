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


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationListener extends NotificationListenerService {

    int smsCount = 0;
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

        utils.readyAudioManager(getApplicationContext());

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
        if (eText != null && eText.length() > 50) {
            eText = eText.substring(0,50);
        }
        String filename = "notification.txt";
        utils.append2file(filename, "\n-----notification--- type:" + packageType);
        dumpAll(packageCode,filename, eTitle, eText, extras);

        switch (packageType) {
            case KK_KAKAO :
//                if (eTitle != null && eText != null) {
                    String eSubT = extras.getString(Notification.EXTRA_SUB_TEXT);
                    sayKakao(packageCode, eTitle, eText, eSubT);
//                }
                break;
            case TO_TEXT_ONLY :
                speakANDLog(packageCode,  packageCode + ". (메세지입니다.) " + eText);
                break;
            case SM_SMS :
                saySMS(packageCode, eTitle, eText, sbn);
                break;
            case TT_TITLE_TEXT :
                sayTitleText(packageCode, packageName, eTitle, eText, sbn);
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
        if (eTitle.contains("이(가) 다른 앱 위에") || eTitle.contains("USB로") ||
                eTitle.contains("Wi-Fi") || eTitle.contains("인터넷 연결 확실치"))
            return;
        speakANDLog("안드로이드","eTitle ~" + eTitle + " eText~" + eText);
    }

    private void sayTitleText(String packageCode, String packageName, String eTitle, String eText, StatusBarNotification sbn) {
        if (packageCode.equals("밴드") && eTitle.contains("읽지 않은")) {
            // ignore
        }
        else if (packageCode.equals("씨티은행") && eTitle.contains("Vaccine")) {
            // ignore
        }
        else if (eText != null) {
            speakANDLog(packageCode,packageCode + ". 메세지입니다. " + eTitle + "_로 부터." + eText);
        }
        else {
            eText = sbn.getNotification().tickerText.toString();
            if (eText != null ) {
                speakANDLog(packageCode,packageCode + "의 티켓 메세지입니다. " + eTitle + "_로 부터." + eText);
            }
            else {
                utils.log(packageCode + packageName, "not matched case");
                utils.log(packageName, "eTitle:" + eTitle);
                utils.append2file("xtraLog.txt", packageCode + ":" + packageName + " not matched ");
                utils.append2file("xtraLog.txt", "eTitle=" + eTitle);
                utils.append2file("xtraLog.txt", "eText=" + eText);
            }
        }
    }

    String lastTimeLog = "";
    private void saySMS(String packageCode, String eTitle, String eText, StatusBarNotification sbn) {
        String filename = "SMS_log.txt";
        String nowTimeLog = utils.getTimeStamp();
        if (nowTimeLog.equals(lastTimeLog)) {   // due to "메세지","메세지 보기"
            utils.append2file(filename, "sms 같은 시각 무시 title~" + smsCount + eTitle + ", text~" + eText );
            return;
        }
        lastTimeLog = nowTimeLog;
        utils.append2file(filename, "-- sms --- " + ++smsCount);
        String text = eText.replace("[Web발신]","");
        text = text.replaceAll("\n","|");
        utils.append2file(filename, "sms org title~" + eTitle + ";sms text~" + text);
        Bundle extras = sbn.getNotification().extras;
//        dumpAll(packageCode,filename, eTitle, text, extras);

        if(isInSmsXcludes(eTitle)) {
            utils.append2file(filename, "sms excluded title~" + smsCount + eTitle );
            return;
        }
        final String msg = "메세지";
    //                assert eText != null;
        if (eTitle.contains(msg)) {
            dumpAll(packageCode,filename, "@@@ title 에 메세지 가 있음 "+ smsCount +  eTitle, text, extras);
            return;
        }
        if (eText.contains("메세지 보기")) {
            dumpAll(packageCode,filename, "@@@ text에 메세지 보기 " + eTitle, text, extras);
            return;
        }
        if (eText != null) { // normal SMS
            speakANDLog(packageCode, eTitle + " 로부터 SMS 메세지가 왔어요. ~" + text + "~");
        }
    }
    
    private void dumpAll (String packageCode, String filename, String eTitle, String eText, Bundle extras){
        String eSubT = extras.getString(Notification.EXTRA_SUB_TEXT);
        String text = eText.replaceAll("\n","|");
        String dumpText = "msg dump ----" + "\nPKGCODE:" + packageCode + "\nTITLE:" + eTitle + "\nSUBT:" + eSubT + "\nTEXT:" + text;
        String msgText = extras.getString(Notification.EXTRA_MESSAGES);
        if (msgText != null)
            dumpText += "\nMESSAGE:" + msgText.replaceAll("\n", "|");
        utils.append2file(filename, dumpText);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) { }

    private boolean isInPackageXcludes(String packageName) {

        if (packageXcludes == null) {
            utils.append2file("notification.txt", "xclude null, so reload prepareLists");
            prepareLists.read();
//            utils.logE("xtbl", "packagexclude is reloaded _x_");
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
                utils.append2file("speakANdlog.txt", "tts is null, reCreated");
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
