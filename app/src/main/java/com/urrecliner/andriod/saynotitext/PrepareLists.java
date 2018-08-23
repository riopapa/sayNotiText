package com.urrecliner.andriod.saynotitext;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import static com.urrecliner.andriod.saynotitext.Vars.kakaoPersons;
import static com.urrecliner.andriod.saynotitext.Vars.kakaoXcludes;
import static com.urrecliner.andriod.saynotitext.Vars.packageCodes;
import static com.urrecliner.andriod.saynotitext.Vars.packageNames;
import static com.urrecliner.andriod.saynotitext.Vars.packageTypes;
import static com.urrecliner.andriod.saynotitext.Vars.packageXcludes;
import static com.urrecliner.andriod.saynotitext.Vars.smsXcludes;

public class PrepareLists {

    public void read () {

        Log.w("PREPARE", "prepare.read()");
        File storage = Environment.getExternalStorageDirectory();
        String directory = storage.toString() + "/download/sayNotiText/";
        packageXcludes =  readParameterFile(directory + "packageXcludes.txt");
        packageNames =  readParameterFile(directory + "packageNames.txt");
        kakaoXcludes =  readParameterFile(directory + "kakaoXcludes.txt");
        kakaoPersons =  readParameterFile(directory + "kakaoPersons.txt");
        smsXcludes =  readParameterFile(directory + "smsXcludes.txt");

        packageCodes = new String[packageNames.length];
        packageTypes = new String[packageNames.length];
        for (int idx = 0; idx < packageNames.length; idx++) {
            String type = packageNames[idx].split(";")[0].trim();
            String code = packageNames[idx].split(";")[1].trim();
            String name = packageNames[idx].split(";")[2].trim();
            packageTypes[idx] = type;
            packageCodes[idx] = code;
            packageNames[idx] = name;
        }
    }

    private String[] readParameterFile(String filename) {
        HandlePlainFile rf = new HandlePlainFile();
        String[] lines = {""};
        try {
            lines = rf.readLines(filename);
            return lines;
        }
        catch(IOException e)
        {
            // Print out the exception that occurred
            Log.e("PrepareLists", "Unable to create "+filename+": "+e.getMessage());
        }
        return lines;
    }

}

