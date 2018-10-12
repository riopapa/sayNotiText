package com.urrecliner.andriod.saynotitext;

import android.graphics.Color;
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

import static com.urrecliner.andriod.saynotitext.Vars.mContext;

public class Utils {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss sss");

    public String[] readLines(String filename) throws IOException
    {
        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null)
        {
            lines.add(line);
        }
        bufferedReader.close();

        return lines.toArray(new String[lines.size()]);
    }

    public String getTimeStamp() { return timeFormat.format(new Date()); }

    public void append2file (String filename, String textLine) {
        File directory = new File(Environment.getExternalStorageDirectory(),"sayNotiTextLog");
        try {
            if (!directory.exists()) {
                boolean result = directory.mkdirs();
                Log.e("Directory", "Creation Error :" + result);
            }
        } catch (Exception e) {
            Log.e("creating Folder error", directory + "_" + e.toString());
        }

        File directoryDate = new File(directory, dateFormat.format(new Date()));
        try {
            if (!directoryDate.exists()) {
                boolean result = directoryDate.mkdirs();
                Log.e("Directory", "Creation Error :" + result);
            }
        } catch (Exception e) {
            Log.e("creating Folder error", directoryDate + "_" + e.toString());
        }

        BufferedWriter bw = null;
        FileWriter fw = null;
        String fullName = directoryDate.toString() + "/" + filename;
        String outText = getTimeStamp() + " " + textLine;

        try {
            File file = new File(fullName);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                if(!file.createNewFile()) {
                    Log.e("createFile"," Error");
                }
            }

            // true = append file
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write("\n" + outText + "\n");
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
        Log.e(tag , where + " " + text);
    }

    public void customToast  (String text, int length) {

        Toast toast = Toast.makeText(mContext, text, length);
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER, 0,0);
        View toastView = toast.getView(); // This'll return the default View of the Toast.

        /* And now you can get the TextView of the default View of the Toast. */
        TextView toastMessage = toastView.findViewById(android.R.id.message);
        toastMessage.setTextSize(16);
        toastMessage.setTextColor(Color.BLACK);
        toastMessage.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_launcher, 0, 0, 0);
        toastMessage.setGravity(Gravity.CENTER_VERTICAL);
        toastMessage.setCompoundDrawablePadding(8);
        toastMessage.setPadding(4,4,24,4);
        toastView.setBackgroundColor(Color.GREEN);
        toast.show();
    }
}
