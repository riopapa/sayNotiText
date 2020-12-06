package com.urrecliner.saynotitext;

import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import static com.urrecliner.saynotitext.Vars.KakaoAlertGWho;
import static com.urrecliner.saynotitext.Vars.kakaoAlertGroup;
import static com.urrecliner.saynotitext.Vars.kakaoAlertWho;
import static com.urrecliner.saynotitext.Vars.kakaoAlertText;
import static com.urrecliner.saynotitext.Vars.kakaoAlerts;
import static com.urrecliner.saynotitext.Vars.kakaoIgnores;
import static com.urrecliner.saynotitext.Vars.kakaoPersons;
import static com.urrecliner.saynotitext.Vars.mContext;
import static com.urrecliner.saynotitext.Vars.packageIgnores;
import static com.urrecliner.saynotitext.Vars.packageIncludeNames;
import static com.urrecliner.saynotitext.Vars.packageNickNames;
import static com.urrecliner.saynotitext.Vars.packageTables;
import static com.urrecliner.saynotitext.Vars.packageTypes;
import static com.urrecliner.saynotitext.Vars.smsIgnores;
import static com.urrecliner.saynotitext.Vars.systemIgnores;
import static com.urrecliner.saynotitext.Vars.textIgnores;
import static com.urrecliner.saynotitext.Vars.textSpeaks;
import static com.urrecliner.saynotitext.Vars.utils;

class ReadOptionTables {
    private String logID = "prepareLists";
    static File directory = new File (Environment.getExternalStorageDirectory(), "sayNotiText/tables/");

    void read () {

        utils.log(logID, "read()");
        packageTables =  readOptionFile("packageTables", false);
        packageNickNames = new String[packageTables.length];
        packageTypes = new String[packageTables.length];
        packageIncludeNames = new String[packageTables.length];

        for (int idx = 0; idx < packageTables.length; idx++) {
            String []strings = packageTables[idx].split(";");
            if (strings.length >= 3) {
                packageTypes[idx] = strings[0].trim();
                packageNickNames[idx] = strings[1].trim();
                packageIncludeNames[idx] = strings[2].trim();
            }
            else {
                Toast.makeText(mContext, "PackageTable has no two semicolons(;) \n"+packageTables[idx], Toast.LENGTH_LONG).show();
//                if (packageTables[idx].length() > 2) {
//                    utils.logE(logID, "packageTable " + packageTables[idx]);
//                    packageTypes[idx] = "";
//                    packageNickNames[idx] = "";
//                    packageIncludeNames[idx] = "";
//                }
            }
        }

        packageIgnores =  readOptionFile("packageIgnores", true);
        kakaoIgnores =  readOptionFile("kakaoIgnores", true);
        kakaoPersons =  readOptionFile("kakaoPersons", true);
        kakaoAlerts =  readOptionFile("kakaoAlerts", true);
        smsIgnores =  readOptionFile("smsIgnores", true);
        systemIgnores =  readOptionFile("systemIgnores", true);
        textIgnores =  readOptionFile("textIgnores", true);
        textSpeaks =  readOptionFile("textSpeaks", true);


        // 카카오 단톡방에서 특별히 얘기 되는 자만
        kakaoAlertGroup = new String[kakaoAlerts.length];   // 단톡방 명
        kakaoAlertWho = new String[kakaoAlerts.length];   // 누가
        kakaoAlertText = new String[kakaoAlerts.length];   // 인식 문자
        KakaoAlertGWho = new String[kakaoAlerts.length];   // 인식 문자
        for (int idx = 0; idx < kakaoAlerts.length; idx++) {
            String []strings = kakaoAlerts[idx].split("\\+");
            kakaoAlertGroup[idx] = strings[0].trim();
            kakaoAlertWho[idx] = strings[1].trim();
            kakaoAlertText[idx] = strings[2].trim();
            KakaoAlertGWho[idx] = kakaoAlertGroup[idx]+kakaoAlertWho[idx];
        }
    }

    static String[] readOptionFile(String filename, boolean removeComment) {
        String[] lines = {""};
        lines = utils.readLines(new File(directory, filename+".txt"));
        if (removeComment) {
            for (int idx = 0; idx < lines.length; idx++) {
                if (lines[idx].indexOf(";") > 1) {    // line should contain ";"
                    String[] strings = lines[idx].split(";");
                    lines[idx] = strings[0].trim();  // ignore from ;
                }
            }
        }
        return lines;
    }
}

