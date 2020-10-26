package com.urrecliner.saynotitext;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

import static com.urrecliner.saynotitext.Vars.kakaoAlert1;
import static com.urrecliner.saynotitext.Vars.kakaoAlert2;
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
import static com.urrecliner.saynotitext.Vars.utils;

class PrepareLists {
    private String logID = "prepareLists";
    private File directory = new File (Environment.getExternalStorageDirectory(), "sayNotiText/tables/");

    void read () {

        utils.log(logID, "read()");
        packageIgnores =  readParameterFile("packageIgnores.txt");
        packageTables =  readParameterFile("packageTables.txt");
        kakaoIgnores =  readParameterFile("kakaoIgnores.txt");
        kakaoPersons =  readParameterFile("kakaoPersons.txt");
        kakaoAlerts =  readParameterFile("kakaoAlerts.txt");
        smsIgnores =  readParameterFile("smsIgnores.txt");
        systemIgnores =  readParameterFile("systemIgnores.txt");

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

        kakaoAlert1 = new String[kakaoAlerts.length];
        kakaoAlert2 = new String[kakaoAlerts.length];
        for (int idx = 0; idx < kakaoAlerts.length; idx++) {
            String []strings = packageTables[idx].split("\\+");
            kakaoAlert1[idx] = strings[0].trim();
            kakaoAlert2[idx] = strings[1].trim();
        }
    }

    private String[] readParameterFile(String filename) {
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

