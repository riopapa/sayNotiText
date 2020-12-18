package com.urrecliner.saynotitext;

import android.widget.Toast;

import java.io.File;

import static com.urrecliner.saynotitext.Vars.KakaoAGroupWho;
import static com.urrecliner.saynotitext.Vars.kakaoAGroup;
import static com.urrecliner.saynotitext.Vars.kakaoAWho;
import static com.urrecliner.saynotitext.Vars.kakaoAText;
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
import static com.urrecliner.saynotitext.Vars.tableDirectory;
import static com.urrecliner.saynotitext.Vars.textIgnores;
import static com.urrecliner.saynotitext.Vars.textSpeaks;
import static com.urrecliner.saynotitext.Vars.utils;

class ReadOptionTables {
    private String logID = "prepareLists";

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
        kakaoAGroup = new String[kakaoAlerts.length];   // 단톡방 명
        kakaoAWho = new String[kakaoAlerts.length];   // 누가
        kakaoAText = new String[kakaoAlerts.length];   // 인식 문자
        KakaoAGroupWho = new String[kakaoAlerts.length];   // 인식 문자
        for (int idx = 0; idx < kakaoAlerts.length; idx++) {
            String []strings = kakaoAlerts[idx].split("\\+");
            kakaoAGroup[idx] = strings[0].trim();
            kakaoAWho[idx] = strings[1].trim();
            kakaoAText[idx] = strings[2].trim();
            KakaoAGroupWho[idx] = kakaoAGroup[idx]+ kakaoAWho[idx];
        }
    }

    static String[] readOptionFile(String filename, boolean removeComment) {
        String[] lines = {""};
        lines = utils.readLines(new File(tableDirectory, filename+".txt"));
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

