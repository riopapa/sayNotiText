package com.urrecliner.saymessage;

import android.widget.Toast;

import java.io.File;

import static com.urrecliner.saymessage.Vars.kTalkAGroupWho;
import static com.urrecliner.saymessage.Vars.kTalkAGroup;
import static com.urrecliner.saymessage.Vars.kTalkAKey2;
import static com.urrecliner.saymessage.Vars.kTalkAWho;
import static com.urrecliner.saymessage.Vars.kTalkAKey1;
import static com.urrecliner.saymessage.Vars.kTalkAlerts;
import static com.urrecliner.saymessage.Vars.kTalkIgnores;
import static com.urrecliner.saymessage.Vars.kTalkSaved;
import static com.urrecliner.saymessage.Vars.kTalkSay;
import static com.urrecliner.saymessage.Vars.mContext;
import static com.urrecliner.saymessage.Vars.packageIgnores;
import static com.urrecliner.saymessage.Vars.packageIncludeNames;
import static com.urrecliner.saymessage.Vars.packageNickNames;
import static com.urrecliner.saymessage.Vars.packageTables;
import static com.urrecliner.saymessage.Vars.packageTypes;
import static com.urrecliner.saymessage.Vars.smsIgnores;
import static com.urrecliner.saymessage.Vars.systemIgnores;
import static com.urrecliner.saymessage.Vars.tableDirectory;
import static com.urrecliner.saymessage.Vars.textIgnores;
import static com.urrecliner.saymessage.Vars.utils;

class ReadOptionTables {

    void read () {

        packageTables =  readOptionFile("packageTables");
        packageNickNames = new String[packageTables.length];
        packageTypes = new String[packageTables.length];
        packageIncludeNames = new String[packageTables.length];

        for (int idx = 0; idx < packageTables.length; idx++) {
            String []strings = packageTables[idx].split("\\^");
            if (strings.length >= 3) {
                packageNickNames[idx] = strings[0].trim();      // 카톡
                packageTypes[idx] = strings[1].trim();          // kk
                packageIncludeNames[idx] = strings[2].trim();   // com.kakako.talk
            }
            else {
                Toast.makeText(mContext, "PackageTable has no two semicolons(;) \n"+packageTables[idx], Toast.LENGTH_LONG).show();
            }
        }

        packageIgnores =  readOptionFile("packageIgnores");
        kTalkIgnores =  readOptionFile("kTalkIgnores");
        kTalkAlerts =  readOptionFile("kTalkAlerts");
        smsIgnores =  readOptionFile("smsIgnores");
        systemIgnores =  readOptionFile("systemIgnores");
        textIgnores =  readOptionFile("textIgnores");

        // 카카오 단톡방에서 특별히 얘기 되는 자만

        kTalkAGroup = new String[kTalkAlerts.length];   // 단톡방 명
        kTalkAWho = new String[kTalkAlerts.length];   // 누가
        kTalkAKey1 = new String[kTalkAlerts.length];   // 인식 문자 1
        kTalkAKey2 = new String[kTalkAlerts.length];   // 인식 문자 2
        kTalkAGroupWho = new String[kTalkAlerts.length];   // 인식 문자
        kTalkSay = new String[kTalkAlerts.length];   // 무조건 speech
        kTalkSaved = new String[kTalkAlerts.length];   // 무조건 speech
        for (int idx = 0; idx < kTalkAlerts.length; idx++) {
            String []strings = kTalkAlerts[idx].split("\\^");
            try {
                kTalkAGroup[idx] = strings[0].trim();
                kTalkAWho[idx] = strings[1].trim();
                kTalkAKey1[idx] = strings[2].trim();
                kTalkAKey2[idx] = strings[3].trim();
                kTalkSay[idx] = (strings.length > 4)? strings[4].trim():""; // if  one more +a sign then true;
                kTalkAGroupWho[idx] = kTalkAGroup[idx] + kTalkAWho[idx];
                kTalkSaved[idx] = "";
            } catch (Exception e) {
                Toast.makeText(mContext, "Alert Table Error on line "+(idx+1)+" > "+ kTalkAlerts[idx],Toast.LENGTH_LONG).show();
            }
        }
    }

    static String[] readOptionFile(String filename) {
        String[] lines = utils.readLines(new File(tableDirectory, filename+".txt"));
        for (int idx = 0; idx < lines.length; idx++) {      // remove to end after ; characters
            String[] strings = lines[idx].split(";");
            lines[idx] = strings[0].trim();  // ignore from ;
        }
        return lines;
    }
}

