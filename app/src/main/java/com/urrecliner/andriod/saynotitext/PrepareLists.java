package com.urrecliner.andriod.saynotitext;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

import static com.urrecliner.andriod.saynotitext.Vars.kakaoIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.kakaoPersons;
import static com.urrecliner.andriod.saynotitext.Vars.packageIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.packageIncludeNames;
import static com.urrecliner.andriod.saynotitext.Vars.packageNickNames;
import static com.urrecliner.andriod.saynotitext.Vars.packageTables;
import static com.urrecliner.andriod.saynotitext.Vars.packageTypes;
import static com.urrecliner.andriod.saynotitext.Vars.smsIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.systemIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.utils;

class PrepareLists {
    String logID = "prepareLists";

    void read () {

        utils.log(logID, "read()");
        File storage = Environment.getExternalStorageDirectory();
        String directory = storage.toString() + "/sayNotiText/tables/";
        packageIgnores =  readParameterFile(directory + "packageIgnores.txt");
        packageTables =  readParameterFile(directory + "packageTables.txt");
        kakaoIgnores =  readParameterFile(directory + "kakaoIgnores.txt");
        kakaoPersons =  readParameterFile(directory + "kakaoPersons.txt");
        smsIgnores =  readParameterFile(directory + "smsIgnores.txt");
        systemIgnores =  readParameterFile(directory + "systemIgnores.txt");

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
    }

    private String[] readParameterFile(String filename) {
        Utils utils = new Utils();
        String[] lines = {""};
        try {
            lines = utils.readLines(filename);
            return lines;
        }
        catch(IOException e)
        {
            Vars.utils.logE(logID, "Unable to create "+filename+": "+e.getMessage());
        }
        return lines;
    }
}

