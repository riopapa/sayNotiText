package com.urrecliner.andriod.saynotitext;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.urrecliner.andriod.saynotitext.Vars.kakaoPersons;
import static com.urrecliner.andriod.saynotitext.Vars.kakaoXcludes;
import static com.urrecliner.andriod.saynotitext.Vars.mPrepareLists;
import static com.urrecliner.andriod.saynotitext.Vars.packageCodes;
import static com.urrecliner.andriod.saynotitext.Vars.packageNames;
import static com.urrecliner.andriod.saynotitext.Vars.packageTypes;
import static com.urrecliner.andriod.saynotitext.Vars.packageXcludes;
import static com.urrecliner.andriod.saynotitext.Vars.smsXcludes;
import static com.urrecliner.andriod.saynotitext.Vars.text2Speech;
import static java.util.Locale.getDefault;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationListener extends NotificationListenerService {

    static final String MY_LOGFOLDER = "/sayNotiTextLog";
    int Counter = 0;
    File directory = new File(Environment.getExternalStorageDirectory(), MY_LOGFOLDER);

    @Override
    public void onCreate() {
        super.onCreate();
        speakANDLog("now","Listener created! " + ++Counter);
        if (mPrepareLists == null) {
            Log.w("_x_ onCreate", "mPrepareList is null");
            mPrepareLists = new PrepareLists();
            mPrepareLists.read();
        }
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
//        speakANDLog("now",packageType + "_" + packageCode+"_" + packageName + " title:"+ eTitle + " text:" + eText);

//        speakANDLog("now",packageName +  "_title " + eTitle + "_text : " + eText);


        if(packageType.equals("kk")) {   // kakao
//            speakANDLog("now",packageName +  "_카카오일 것 " + eTitle + "_text : " + eText);
            if (eTitle == null || eText == null) return;
            String eSubT = extras.getString(Notification.EXTRA_SUB_TEXT);
            if (eSubT != null) {
                if (!isInKakaoXcludes(eSubT))  // eSub: 채팅방
                    speakANDLog(packageCode,  "카카오톡 옴_" + eSubT + "_채팅방 에서_" + eTitle + "_님._" + eText);
            }
            else {
                if(!isInKakaoPersons(eTitle))   // eTitle: 개인 이름
                    speakANDLog(packageCode,  "카카오톡 옴_" + eTitle + "_님으로 부터._" + eText);
            }
        }
        else if(packageName.equals("android")) {
            if (eTitle != null) {
                String eSubT = extras.getString(Notification.EXTRA_SUB_TEXT);
                if (!eTitle.contains("이(가) 다른 앱 위에") && !eTitle.contains("USB로") &&
                        !eTitle.contains("뭔가 다른 거") && !eTitle.contains("Wi-Fi")) {
                    speakANDLog("xtras_" + packageName, "title_" + eTitle +
                            "_text_" + eText + "_sub_" + eSubT);
                }
            }
            else {
                speakANDLog("xtrasnull_" + packageName, "_text_" + eText);
            }
        }
        else if(packageType.equals("to")) {  // eText only
            speakANDLog(packageCode,  packageCode + " (메세지) " + eText);
        }
        else if(packageType.equals("sm")) {  // sms
            if(!isInSmsXcludes(eTitle)) {
                assert eText != null;
                eText = eText.replace("[Web발신]","");
                if (!eTitle.equals("메세지")) {
                    speakANDLog(packageCode, eTitle + " 로부터의 SMS 메세지입니다 " + eText);
                }
            }
        }
        else if(packageType.equals("tt")) {  // eTitle + eText
            if (packageCode.equals("밴드") && eTitle.contains("아직 읽지 않은 ")) {
                return;
            }
            else {
                speakANDLog(packageCode,packageCode + ", 메세지입니다. " + eTitle + "_로 부터_" + eText);
            }
        }
        else {
            if(eTitle != null && eText != null)
                speakANDLog("unknown " + packageName, "title_" + eTitle + "_text_" + eText);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }

    private boolean isInPackageXcludes(String packageName) {

        if (packageXcludes == null) {
            mPrepareLists.read();
            Log.w("xtbl", "packagexclude is reloaded _x_");
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
//            Log.w("speakNlog " + tag, text);
            if (text2Speech == null) {
                Log.e("speakcall0","text2speech created");
                text2Speech = new Text2Speech();
                Log.w("speakcall2","Text2Speech speakandlog");
                text2Speech.initiateTTS(getApplicationContext());
            }
            if (!tag.equals("now"))
                text2Speech.speak("저어 알릴게 있어요!.. " + text);
        }

        try {
            if (!directory.exists()) {
                boolean result = directory.mkdirs();
                Log.e("Directory", "Creation Error :" + result);
            }
        } catch (Exception e) {
            Log.e("creating Folder error", directory + tag + "_" + e.toString());
        }
        text = text.replace("\n", " | ");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        tag = dateFormat.format(new Date()) + "_" + tag;
        append2file(directory, tag  + ".txt", getIMGTimeText() + text);
        Log.w(tag, text);
//        logFreeMemory();
//        File file = new File(directory, filename);
//        FileOutputStream os;
//        try {
//            os = new FileOutputStream(file);
//            os.write((filename + "\n" + tag + "\n" + text).getBytes());
//            os.close();
//        } catch (IOException e) {
//            Toast.makeText(getApplicationContext(), "File write error\n" + filename, Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//            Toast.makeText(getApplicationContext(), "ERROR to print " + e, Toast.LENGTH_LONG).show();
//        }
    }

    private String getIMGTimeText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss_", getDefault());
        return dateFormat.format(new Date());
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
    private void append2file (File directory, String filename, String textline) {
        BufferedWriter bw = null;
        FileWriter fw = null;
        String fullName = directory + "/" + filename;

        try {
            File file = new File(fullName);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                if(!file.createNewFile()) {
                    Log.e("createFile"," Error");
                }
            }

            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write("\n" + textline + "\n");

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "ERROR File write\n" + filename + e.toString(),
                    Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException ex) {
                Toast.makeText(getApplicationContext(), "FileWrite Err \n" + filename + ex.toString(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    private void logFreeMemory() {
        try {
            Runtime info = Runtime.getRuntime();
            Log.w("_x_" + Counter++,  "totalSZ = " + info.totalMemory() + ", freeSZ = " + info.freeMemory());
            append2file(directory, "freesize"  + ".txt", getIMGTimeText() + info.freeMemory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
