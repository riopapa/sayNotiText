package com.urrecliner.andriod.saynotitext;

import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import static com.urrecliner.andriod.saynotitext.Vars.kakaoPersons;
import static com.urrecliner.andriod.saynotitext.Vars.kakaoIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.mContext;
import static com.urrecliner.andriod.saynotitext.Vars.packageCodes;
import static com.urrecliner.andriod.saynotitext.Vars.packageNames;
import static com.urrecliner.andriod.saynotitext.Vars.packageTypes;
import static com.urrecliner.andriod.saynotitext.Vars.packageIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.smsIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.systemIgnores;
import static com.urrecliner.andriod.saynotitext.Vars.utils;

class PrepareLists {

    public void read () {

        utils.log("1", "read()");
        File storage = Environment.getExternalStorageDirectory();
        String directory = storage.toString() + "/download/sayNotiText/";
        packageIgnores =  readParameterFile(directory + "packageIgnores.txt");
        packageNames =  readParameterFile(directory + "packageNames.txt");
        kakaoIgnores =  readParameterFile(directory + "kakaoIgnores.txt");
        kakaoPersons =  readParameterFile(directory + "kakaoPersons.txt");
        smsIgnores =  readParameterFile(directory + "smsIgnores.txt");
        systemIgnores =  readParameterFile(directory + "systemIgnores.txt");

        packageCodes = new String[packageNames.length];
        packageTypes = new String[packageNames.length];
        for (int idx = 0; idx < packageNames.length; idx++) {
            String packageName[] = packageNames[idx].split(";");
            packageTypes[idx] = packageName[0].trim();
            packageCodes[idx] = packageName[1].trim();
            packageNames[idx] = packageName[2].trim();
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

