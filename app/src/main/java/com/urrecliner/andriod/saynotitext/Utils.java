package com.urrecliner.andriod.saynotitext;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.urrecliner.andriod.saynotitext.Vars.mAudioManager;
import static com.urrecliner.andriod.saynotitext.Vars.mContext;
import static com.urrecliner.andriod.saynotitext.Vars.mFocusGain;

class Utils {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss sss", Locale.KOREA);
    private static final String notifyFile = "notification.txt";

    String[] readLines(String filename) throws IOException {
        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.length()>0)
                lines.add(line);
        }
        bufferedReader.close();

        return lines.toArray(new String[0]);
    }

    String getTimeStamp() {
        return timeFormat.format(new Date());
    }

    void append2file(String filename, String textLine) {

        File directoryDate = getTodayFolder();

        BufferedWriter bw = null;
        FileWriter fw = null;
        String fullName = directoryDate.toString() + "/" + filename;

        try {
            File file = new File(fullName);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    logE("createFile", " Error");
                }
            }
            StackTraceElement[] traces;
            traces = Thread.currentThread().getStackTrace();
            String outText = "\n" + getTimeStamp() + " " + traces[5].getMethodName() + " > " + traces[4].getMethodName() + " > " + traces[3].getMethodName() + " #" + traces[3].getLineNumber() + " [[" + textLine + "]]\n";
            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(outText);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private File packageDirectory = new File(Environment.getExternalStorageDirectory(), "sayNotiText");
    private File getTodayFolder() {
        try {
            if (!packageDirectory.exists()) {
                packageDirectory.mkdirs();
            }
        } catch (Exception e) {
            Log.e("creating Directory error", packageDirectory.toString() + "_" + e.toString());
        }

        File directoryDate = new File(packageDirectory, dateFormat.format(new Date()));
        try {
            if (!directoryDate.exists()) {
                if (directoryDate.mkdirs()) {
                    deleteOldFiles();
                    log("Directory", directoryDate.toString() + " created ");
                }
            }
        } catch (Exception e) {
            logE("creating Folder error", directoryDate + "_" + e.toString());
        }
        return directoryDate;
    }

    void readyAudioManager(Context context) {
        if(mAudioManager == null) {
            append2file(notifyFile, "mAudioManager is NULL");
            mContext = context;
            try {
                mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
                mFocusGain = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                        .build();
            } catch (Exception e) {
                append2file("timestamp.txt", "mAudioManager Error " + e.toString());
            }
        }
    }

    void log(String tag, String text) {
        StackTraceElement[] traces;
        traces = Thread.currentThread().getStackTrace();
        String log = traceName(traces[5].getMethodName()) + traceName(traces[4].getMethodName()) + traceShortName(traces[3].getClassName())+"> "+traces[3].getMethodName() + "#" + traces[3].getLineNumber() + " {"+ tag + "} " + text;
        Log.w(tag, log);
    }

    void logE(String tag, String text) {
        StackTraceElement[] traces;
        traces = Thread.currentThread().getStackTrace();
        String log = traceName(traces[5].getMethodName()) + traceName(traces[4].getMethodName()) + traceShortName(traces[3].getClassName())+"> "+traces[3].getMethodName() + "#" + traces[3].getLineNumber() + " {"+ tag + "} " + text;
        Log.e("<" + tag + ">" , log);
    }

    private String traceName (String s) {
        if (s.equals("performResume") || s.equals("performCreate") || s.equals("callActivityOnResume") || s.equals("access$1200")
                || s.equals("handleReceiver"))
            return "";
        else
            return s + "> ";
    }
    private String traceShortName (String s) {
        return s.substring(s.lastIndexOf(".")+1);
    }


    void customToast  (String text, int length) {

        Toast toast = Toast.makeText(mContext, text, length);
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER, 0,0);
        View toastView = toast.getView(); // This'll return the default View of the Toast.

        /* And now you can get the TextView of the default View of the Toast. */
        TextView toastMessage = toastView.findViewById(android.R.id.message);
        toastMessage.setTextSize(12);
        toastMessage.setTextColor(Color.BLACK);
        toastMessage.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_launcher, 0, 0, 0);
        toastMessage.setGravity(Gravity.CENTER_VERTICAL);
        toastMessage.setCompoundDrawablePadding(8);
        toastMessage.setPadding(4,4,24,4);
        toastView.setBackgroundColor(Color.YELLOW);
        toast.show();
    }

    /* delete old packageDirectory / files if storage is less than xx */
    private void deleteOldFiles() {

        String weekAgo = dateFormat.format(System.currentTimeMillis() - 3*24*60*60*1000L);
        File[] files = getDirectoryList(packageDirectory);
        Collator myCollator = Collator.getInstance();
        for (File file : files) {
            String shortFileName = file.getName();
            if (myCollator.compare(shortFileName, weekAgo) < 0) {
                deleteRecursive(file);
            }
        }
    }

    private File[] getDirectoryList(File fullPath) {
        File[] files = fullPath.listFiles();
//        log("# of files", "in dir : " + files.length);
        return files;
    }

    /* delete directory and files under that directory */
    private void deleteRecursive(File fileOrDirectory) {
//        Log.w("deleteRecursive",fileOrDirectory.toString());
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        fileOrDirectory.delete();
    }
}
