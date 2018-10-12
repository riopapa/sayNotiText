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
import static com.urrecliner.andriod.saynotitext.Vars.mPrepareLists;
import static com.urrecliner.andriod.saynotitext.Vars.packageCodes;
import static com.urrecliner.andriod.saynotitext.Vars.packageNames;
import static com.urrecliner.andriod.saynotitext.Vars.packageTypes;
import static com.urrecliner.andriod.saynotitext.Vars.packageXcludes;
import static com.urrecliner.andriod.saynotitext.Vars.smsXcludes;
import static com.urrecliner.andriod.saynotitext.Vars.text2Speech;
import static com.urrecliner.andriod.saynotitext.Vars.utils;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationListener extends NotificationListenerService {

    @Override
    public void onCreate() {
        super.onCreate();
        mPrepareLists.read();
        speakANDLog("now","Listener created!");
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
        if (sbn==null) return;

        String packageName = sbn.getPackageName().toLowerCase();
        String packageCode, packageType;

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

        if(packageType.equals("kk")) {   // kakao
            if (eTitle == null || eText == null) return;
            String eSubT = extras.getString(Notification.EXTRA_SUB_TEXT);
            if (eSubT != null) {
                if (!isInKakaoXcludes(eSubT))  // eSub: 채팅방
                    speakANDLog(packageCode,  "카카오톡_" + eSubT + "_채팅방_" + eTitle + "_님으로부터_" + eText);
            }
            else {
                if(!isInKakaoPersons(eTitle))   // eTitle: 개인 이름
                    speakANDLog(packageCode,  "카카오톡_" + eTitle + "_님으로 부터._" + eText);
            }
        }
        else if(packageName.equals("android")) {
            if (eTitle != null) {
                String eSubT = extras.getString(Notification.EXTRA_SUB_TEXT);
                if (!eTitle.contains("이(가) 다른 앱 위에") && !eTitle.contains("USB로") &&
                        !eTitle.contains("Wi-Fi") && !eTitle.contains("인터넷 연결 확실치")) {
                    speakANDLog("others_" + packageName, "title_" + eTitle +
                            "_text_" + eText + "_sub_" + eSubT);
                }
            }
            else {
                speakANDLog("OthersNull_" + packageName, "_text_" + eText);
            }
        }
        else if(packageType.equals("to")) {  // eText only
            speakANDLog(packageCode,  packageCode + " (메세지입니다) " + eText);
        }
        else if(packageType.equals("sm")) {  // sms
            if(!isInSmsXcludes(eTitle)) {
                assert eText != null;
                eText = eText.replace("[Web발신]","+");
                eTitle = eTitle.replace("[Web발신]","+");
                if (!eTitle.contains("메세지") && eText != null) {
                    speakANDLog(packageCode, eTitle + " 로부터의 SMS 메세지입니다. " + eText);
                }
                else {
                    eText +=  " ; BIG[" + extras.getString(Notification.EXTRA_BIG_TEXT) + "]";
                    utils.log(packageName, eTitle + "_ 로부터의 메세지? " + eText);
                    speakANDLog(packageName, eTitle + "_ 로부터의 메세지? " + eText);
                }
            }
        }
        else if(packageType.equals("tt")) {  // eTitle + eText
            if (packageCode.equals("밴드") && eTitle.contains("아직 읽지 않은 ")) {
                // ignore
            }
            else if (eText != null) {
                speakANDLog(packageCode,packageCode + "의 메세지입니다. " + eTitle + "_로 부터_" + eText);
            }
        }
        else {
            if (eTitle != null && eText != null)
                speakANDLog("unknown " + packageName, "title:" + eTitle + "_text:" + eText);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }

    private boolean isInPackageXcludes(String packageName) {

        if (packageXcludes == null) {
            mPrepareLists.read();
            utils.logE("xtbl", "packagexclude is reloaded _x_");
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
//            utils.log("speakNlog " + tag, text);
            if (text2Speech == null) {
                utils.logE("speak 0","tts reCreated");
                text2Speech = new Text2Speech();
                text2Speech.initiateTTS(getApplicationContext());
            }
            if (!tag.equals("now"))
                text2Speech.speak("저어 알릴게 있어요!.. " + text);
        }
        text = text.replace("\n", " | ");
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
