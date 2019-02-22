package com.urrecliner.andriod.saynotitext;

import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import static com.urrecliner.andriod.saynotitext.Vars.kakaoIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.kakaoPersons;
import static com.urrecliner.andriod.saynotitext.Vars.mContext;
import static com.urrecliner.andriod.saynotitext.Vars.packageCodes;
import static com.urrecliner.andriod.saynotitext.Vars.packageIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.packageNames;
import static com.urrecliner.andriod.saynotitext.Vars.packageTables;
import static com.urrecliner.andriod.saynotitext.Vars.packageTypes;
import static com.urrecliner.andriod.saynotitext.Vars.smsIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.systemIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.utils;

class PrepareLists {

    void read () {

        utils.log("1", "read()");
        File storage = Environment.getExternalStorageDirectory();
        String directory = storage.toString() + "/sayNotiText/tables/";
        packageIgnores =  readParameterFile(directory + "packageIgnores.txt");
        packageTables =  readParameterFile(directory + "packageTables.txt");
        kakaoIgnores =  readParameterFile(directory + "kakaoIgnores.txt");
        kakaoPersons =  readParameterFile(directory + "kakaoPersons.txt");
        smsIgnores =  readParameterFile(directory + "smsIgnores.txt");
        systemIgnores =  readParameterFile(directory + "systemIgnores.txt");

        packageCodes = new String[packageTables.length];
        packageTypes = new String[packageTables.length];
        packageNames = new String[packageTables.length];
        for (int idx = 0; idx < packageTables.length; idx++) {
            if (packageTables[idx].indexOf(";")>2) {    // line should contain ";"
                String packageName[] = packageTables[idx].split(";");
                packageTypes[idx] = packageName[0].trim();
                packageCodes[idx] = packageName[1].trim();
                packageNames[idx] = packageName[2].trim();
            }
            else {
                packageTypes[idx] = "";
                packageCodes[idx] = "";
                packageNames[idx] = "";
            }
        }
        Toast.makeText(mContext, "PrepareList reloaded", Toast.LENGTH_SHORT).show();
    }

    private String[] readParameterFile(String filename) {
        Utils rf = new Utils();
        String[] lines = {""};
        try {
            lines = rf.readLines(filename);
            return lines;
        }
        catch(IOException e)
        {
            // Print out the exception that occurred
            utils.logE("PrepareLists", "Unable to create "+filename+": "+e.getMessage());
        }
        return lines;
    }
}

