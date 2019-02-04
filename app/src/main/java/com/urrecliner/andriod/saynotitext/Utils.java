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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.urrecliner.andriod.saynotitext.Vars.mAudioManager;
import static com.urrecliner.andriod.saynotitext.Vars.mContext;
import static com.urrecliner.andriod.saynotitext.Vars.mFocusGain;

public class Utils {

    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
    final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss sss", Locale.KOREA);
    final static String notifyFile = "notification.txt";

    public String[] readLines(String filename) throws IOException {
        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }
        bufferedReader.close();

        return lines.toArray(new String[lines.size()]);
    }

    public String getTimeStamp() {
        return timeFormat.format(new Date());
    }

    public void append2file(String filename, String textLine) {

        File directoryDate = getTodayFolder();

        BufferedWriter bw = null;
        FileWriter fw = null;
        String fullName = directoryDate.toString() + "/" + filename;
        if (textLine.length() > 100) {
            textLine = textLine.substring(0,100);
        }

        try {
            File file = new File(fullName);
            // if file doesnt exists, then create it
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

//            for (int i = 3; i < 8; i++) {
//                bw.write("                ".substring(0, i ) + "+- " + i + traces[i].getMethodName() + "#" +traces[i].getLineNumber() + "\n");
//            }
            //            Toast.makeText(getApplicationContext(), "File wrote to " + fullName,
//                    Toast.LENGTH_SHORT).show();

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
    private File getTodayFolder() {
        File directory = new File(Environment.getExternalStorageDirectory(), "sayNotiTextLog");
        try {
            if (!directory.exists()) {
                directory.mkdirs();
            }
        } catch (Exception e) {
            Log.e("creating Directory error", directory.toString() + "_" + e.toString());
        }

        File directoryDate = new File(directory, dateFormat.format(new Date()));
        try {
            if (!directoryDate.exists()) {
                if (directoryDate.mkdirs())
                    log("Directory", directoryDate.toString() + " created ");
            }
        } catch (Exception e) {
            logE("creating Folder error", directoryDate + "_" + e.toString());
        }
        return directoryDate;
    }

    public void readyAudioManager(Context context) {
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

    public void log(String tag, String text) {
        StackTraceElement[] traces;
        traces = Thread.currentThread().getStackTrace();
        String where = " " + traces[5].getMethodName() + " > " + traces[4].getMethodName() + " > " + traces[3].getMethodName() + " #" + traces[3].getLineNumber();
        Log.w(tag , where + " " + text);
    }

    public void logE(String tag, String text) {
        StackTraceElement[] traces;
        traces = Thread.currentThread().getStackTrace();
        String where = " " + traces[5].getMethodName() + " > " + traces[4].getMethodName() + " > " + traces[3].getMethodName() + " #" + traces[3].getLineNumber();
        Log.e("<" + tag + ">" , where + " " + text);
    }

    public void customToast  (String text, int length) {

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
}
