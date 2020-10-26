package com.urrecliner.saynotitext;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

import static com.urrecliner.saynotitext.Vars.kakaoAlerts;
import static com.urrecliner.saynotitext.Vars.kakaoIgnores;
import static com.urrecliner.saynotitext.Vars.kakaoPersons;
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
    private File directory = new File (Environment.getExternalStorageDirectory(), "sayNotiText/tables/");

    void read () {

        utils.log(logID, "read()");
        packageIgnores =  readOptionFile("packageIgnores.txt");
        packageTables =  readOptionFile("packageTables.txt");
        kakaoIgnores =  readOptionFile("kakaoIgnores.txt");
        kakaoPersons =  readOptionFile("kakaoPersons.txt");
        kakaoAlerts =  readOptionFile("kakaoAlerts.txt");
        smsIgnores =  readOptionFile("smsIgnores.txt");
        systemIgnores =  readOptionFile("systemIgnores.txt");
        textIgnores =  readOptionFile("textIgnores.txt");
        textSpeaks =  readOptionFile("textSpeaks.txt");

        packageNickNames = new String[packageTables.length];
        packageTypes = new String[packageTables.length];
        packageIncludeNames = new String[packageTables.length];

        for (int idx = 0; idx < packageTables.length; idx++) {
            if (packageTables[idx].indexOf(";")>1) {    // line should contain ";"
                String []strings = packageTables[idx].split(";");
                packageTypes[idx] = strings[0].trim();
                packageNickNames[idx] = strings[1].trim();
                packageIncludeNames[idx] = strings[2].trim();
            }
            else {
                if (packageTables[idx].length() > 2) {
                    utils.logE(logID, "packageTable " + packageTables[idx]);
                    packageTypes[idx] = "";
                    packageNickNames[idx] = "";
                    packageIncludeNames[idx] = "";
                }
            }
        }

        for (int idx = 0; idx < textIgnores.length; idx++) {
            if (textIgnores[idx].indexOf(";")>1) {    // line should contain ";"
                String []strings = textIgnores[idx].split(";");
                textIgnores[idx] = strings[0].trim();  // ignore from ;
            }
        }

        for (int idx = 0; idx < smsIgnores.length; idx++) {
            if (smsIgnores[idx].indexOf(";")>1) {    // line should contain ";"
                String []strings = smsIgnores[idx].split(";");
                smsIgnores[idx] = strings[0].trim();  // ignore from ;
            }
        }

        for (int idx = 0; idx < packageIgnores.length; idx++) {
            if (packageIgnores[idx].indexOf(";")>1) {    // line should contain ";"
                String []strings = packageIgnores[idx].split(";");
                packageIgnores[idx] = strings[0].trim();  // ignore from ;
            }
        }

        for (int idx = 0; idx < textSpeaks.length; idx++) {
            if (textSpeaks[idx].indexOf(";")>1) {    // line should contain ";"
                String []strings = textSpeaks[idx].split(";");
                textSpeaks[idx] = strings[0].trim();  // ignore from ;
            }
        }

    }

    private String[] readOptionFile(String filename) {
        String[] lines = {""};
        try {
            lines = utils.readLines(new File(directory, filename));
            return lines;
        }
        catch(IOException e) {
            Vars.utils.logE(logID, "Unable to create "+filename+": "+e.getMessage());
        }
        return lines;
    }
}

