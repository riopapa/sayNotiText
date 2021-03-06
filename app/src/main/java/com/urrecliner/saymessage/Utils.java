package com.urrecliner.saymessage;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.urrecliner.saymessage.Vars.mAudioManager;
import static com.urrecliner.saymessage.Vars.mContext;
import static com.urrecliner.saymessage.Vars.mFocusGain;
import static com.urrecliner.saymessage.Vars.packageDirectory;

class Utils {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd", Locale.KOREA);
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("yy-MM-dd HH.mm.ss", Locale.KOREA);
    private final String logFile = "log.txt";

    String[] readLines(File filename)  {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = new ArrayList<>();
        String line = "";
        while (true) {
            try {
                if ((line = bufferedReader.readLine()) == null) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            line = line.trim();
            if (line.length() >= 2) {        // at least 3 characters for one line
                lines.add(line);
            }
        }
        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines.toArray(new String[0]);
    }

    private String getTimeStamp() {
        return timeFormat.format(new Date());
    }

    void append2file(String filename, String textLine) {
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            File file = new File(getTodayFolder(), filename);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    logE("createFile", " Error");
                }
            }
            String outText = String.format("%s %s\n", getTimeStamp(), textLine);
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

    //    private File packageDirectory = new File(Environment.getExternalStorageDirectory(), "saymessage");
    private File getTodayFolder() {
        if (!packageDirectory.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                packageDirectory.mkdirs();
            } catch (Exception e) {
                logE("make Today", packageDirectory.toString() + "_" + e.toString());
            }
        }
        File directoryDate = new File(packageDirectory, dateFormat.format(new Date()));
        try {
            if (!directoryDate.exists()) {
                if (directoryDate.mkdirs()) {
                    deleteOldFiles();
                }
            }
        } catch (Exception e) {
            logE("creating Folder error", directoryDate + "_" + e.toString());
        }
        return directoryDate;
    }

    void readyAudioManager(Context context) {
        if(mAudioManager == null) {
//            append2file(logFile, "mAudioManager is NULL");
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
        String logText = excludeName(traces[5].getMethodName()) + excludeName(traces[4].getMethodName()) + excludeName(traceClassName(traces[3].getClassName()))+"> "+traces[3].getMethodName() + "#" + traces[3].getLineNumber() + " {"+ tag + "} " + text;
        Log.w(tag, logText);
        append2file(logFile, logText);
    }

    void logE(String tag, String text) {
        StackTraceElement[] traces;
        traces = Thread.currentThread().getStackTrace();
        String logText = excludeName(traces[5].getMethodName()) + excludeName(traces[4].getMethodName()) + traceClassName(traces[3].getClassName())+"> "+traces[3].getMethodName() + "#" + traces[3].getLineNumber() + " |err:"+ tag + "| " + text;
        Log.e("<" + tag + ">" , logText);
        append2file(logFile, logText);
    }

    private String excludeName(String s) {
        String [] omits = { "performResume", "performCreate", "callActivityOnResume", "access$",
                "onCreate", "onNotificationPosted", "NotificationListener", "performCreate", "log",
                "handleReceiver", "handleMessage", "dispatchKeyEvent", "onBindViewHolder"};
        for (String o : omits) {
            if (s.contains(o)) return ". ";
        }
        return s + "> ";
    }
    private String traceClassName(String s) {
        return s.substring(s.lastIndexOf(".")+1);
    }

    void customToast (String text) {

        Toast toast = Toast.makeText(mContext, text, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER, 0,0);
        View toastView = toast.getView();
        TextView toastMessage = toastView.findViewById(android.R.id.message);
        toastMessage.setTextSize(12);
        toastMessage.setTextColor(Color.GREEN);
        toastMessage.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.icon_launcher, 0, 0, 0);
        toastMessage.setGravity(Gravity.CENTER_VERTICAL);
        toastMessage.setCompoundDrawablePadding(8);
        toastMessage.setPadding(4,4,24,4);
        toastView.setBackgroundColor(Color.YELLOW);
        toast.show();
    }

    /* delete old packageDirectory / files if storage is less than x days */
    private void deleteOldFiles() {
        String weekAgo = dateFormat.format(System.currentTimeMillis() - 6*24*60*60*1000L);
        File[] files = packageDirectory.listFiles();
        Collator myCollator = Collator.getInstance();
        for (File file : files) {
            String shortFileName = file.getName();
            if (myCollator.compare(shortFileName, weekAgo) < 0) {
                deleteFolder(file);
//                deleteRecursive(file);
            }
        }
    }

    private void deleteFolder(File file) {
        String deleteCmd = "rm -r " + file.toString();
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(deleteCmd);
        } catch (IOException e) { }
    }

    /* delete directory and files under that directory */
    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        //noinspection ResultOfMethodCallIgnored
        fileOrDirectory.delete();
    }

    private SoundPool soundPool = null;
    private final int[] beepSound = {
            R.raw.say_notification,
            R.raw.leading_sound                   //  event button pressed
    };
    private int[] soundNbr = new int[beepSound.length];

    void beepsInitiate() {

        SoundPool.Builder builder;
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        builder = new SoundPool.Builder();
        builder.setAudioAttributes(audioAttributes).setMaxStreams(5);
        soundPool = builder.build();
        for (int i = 0; i < beepSound.length; i++) {
            soundNbr[i] = soundPool.load(mContext, beepSound[i], 1);
        }
    }

    void beepOnce(final int soundId) {

        if (soundPool == null) {
            beepsInitiate();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    beepSound(soundId);
                }
            }, 100);
        } else {
            beepSound(soundId);
        }
    }
    private void beepSound(int soundId) {
        soundPool.play(soundNbr[soundId], 1f, 1f, 1, 0, 1f);
    }

}
