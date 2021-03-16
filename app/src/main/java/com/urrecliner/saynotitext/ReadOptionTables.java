package com.urrecliner.saynotitext;

import android.widget.Toast;

import java.io.File;

import static com.urrecliner.saynotitext.Vars.KakaoAGroupWho;
import static com.urrecliner.saynotitext.Vars.kakaoAGroup;
import static com.urrecliner.saynotitext.Vars.kakaoAKey2;
import static com.urrecliner.saynotitext.Vars.kakaoAWho;
import static com.urrecliner.saynotitext.Vars.kakaoAKey1;
import static com.urrecliner.saynotitext.Vars.kakaoAlerts;
import static com.urrecliner.saynotitext.Vars.kakaoIgnores;
import static com.urrecliner.saynotitext.Vars.kakaoPersons;
import static com.urrecliner.saynotitext.Vars.kakaoTalk;
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
        packageTables =  readOptionFile("packageTables");
        packageNickNames = new String[packageTables.length];
        packageTypes = new String[packageTables.length];
        packageIncludeNames = new String[packageTables.length];

        for (int idx = 0; idx < packageTables.length; idx++) {
            String []strings = packageTables[idx].split("\\^");
            if (strings.length >= 3) {
                packageTypes[idx] = strings[0].trim();
                packageNickNames[idx] = strings[1].trim();
                packageIncludeNames[idx] = strings[2].trim();
            }
            else {
                Toast.makeText(mContext, "PackageTable has no two semicolons(;) \n"+packageTables[idx], Toast.LENGTH_LONG).show();
            }
        }

        packageIgnores =  readOptionFile("packageIgnores");
        kakaoIgnores =  readOptionFile("kakaoIgnores");
        kakaoPersons =  readOptionFile("kakaoPersons");
        kakaoAlerts =  readOptionFile("kakaoAlerts");
        smsIgnores =  readOptionFile("smsIgnores");
        systemIgnores =  readOptionFile("systemIgnores");
        textIgnores =  readOptionFile("textIgnores");
        textSpeaks =  readOptionFile("textSpeaks");

        // 카카오 단톡방에서 특별히 얘기 되는 자만
        kakaoAGroup = new String[kakaoAlerts.length];   // 단톡방 명
        kakaoAWho = new String[kakaoAlerts.length];   // 누가
        kakaoAKey1 = new String[kakaoAlerts.length];   // 인식 문자 1
        kakaoAKey2 = new String[kakaoAlerts.length];   // 인식 문자 2
        KakaoAGroupWho = new String[kakaoAlerts.length];   // 인식 문자
        kakaoTalk = new String[kakaoAlerts.length];   // 무조건 speech
        for (int idx = 0; idx < kakaoAlerts.length; idx++) {
            String []strings = kakaoAlerts[idx].split("\\^");
            try {
                kakaoAGroup[idx] = strings[0].trim();
                kakaoAWho[idx] = strings[1].trim();
                kakaoAKey1[idx] = strings[2].trim();
                kakaoAKey2[idx] = strings[3].trim();
                kakaoTalk[idx] = (strings.length > 4)? strings[4].trim():""; // if  one more +a sign then true;
                KakaoAGroupWho[idx] = kakaoAGroup[idx] + kakaoAWho[idx];
//                utils.log("array "+idx,kakaoAGroup[idx]+" "+kakaoAWho[idx]+" - "+kakaoAKey1[idx]+" - "+kakaoAKey2[idx]+" "+KakaoAGroupWho[idx]);
            } catch (Exception e) {
                Toast.makeText(mContext, "Alert Table Error on line "+(idx+1)+" > "+kakaoAlerts[idx],Toast.LENGTH_LONG).show();
            }
        }
    }

    static String[] readOptionFile(String filename) {
        String[] lines = utils.readLines(new File(tableDirectory, filename+".txt"));
        for (int idx = 0; idx < lines.length; idx++) {      // remove to end after ; characters
            lines[idx] = lines[idx].replace("\\t","");
            String[] strings = lines[idx].split(";");
            lines[idx] = strings[0].trim();  // ignore from ;
        }
        return lines;
    }
}

