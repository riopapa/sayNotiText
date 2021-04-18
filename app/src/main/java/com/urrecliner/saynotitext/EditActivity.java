package com.urrecliner.saynotitext;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Selection;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import static com.urrecliner.saynotitext.Vars.nowFileName;
import static com.urrecliner.saynotitext.Vars.alertOneLines;
import static com.urrecliner.saynotitext.Vars.linePos;
import static com.urrecliner.saynotitext.Vars.readOptionTables;
import static com.urrecliner.saynotitext.Vars.tableDirectory;
import static com.urrecliner.saynotitext.Vars.utils;

public class EditActivity extends AppCompatActivity {

    boolean isAlertFile, isPackageTable;
    RecyclerView recyclerView;
    AlertAdapter alertAdapter;
    ImageView removeView, dupView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        isAlertFile = nowFileName.equals("kakaoAlerts");
        isPackageTable = nowFileName.equals("packageTables");
        removeView = findViewById(R.id.action_remove);
        dupView = findViewById(R.id.action_dup);
        EditText tv = findViewById(R.id.text_table);
        String [] lines = utils.readLines(new File(tableDirectory, nowFileName+".txt"));
        if (isAlertFile) {
            tv.setVisibility(View.GONE);
            build_OneLine(lines);
        } else {
            tv.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            for (String s : lines) sb.append(s).append("\n");
            String text = sb.toString() + "\n";
            tv.setText(text);
            tv.setFocusable(true);
            tv.setEnabled(true);
            tv.setClickable(true);
            tv.setFocusableInTouchMode(true);
        }
    }

    void build_OneLine(String [] lines) {
        alertOneLines = new ArrayList<>();
        for (int idx = 0; idx < lines.length; idx++) {
            String lGroup, lWho, lKey1, lKey2, lTalk, lComment;
            lines[idx] = lines[idx].replace("\\t","");
            String[] strings = lines[idx].split(";");
            lComment = (strings.length > 1) ? strings[1].trim() : "";
            strings = strings[0].split("\\^");
            lGroup = strings[0].trim();
            lWho = strings[1].trim();
            lKey1 = strings[2].trim();
            lKey2 = strings[3].trim();
            lTalk = (strings.length > 4) ? strings[4].trim(): "";
            alertOneLines.add(new AlertOneLine(false, lGroup, lWho, lKey1, lKey2, lTalk, lComment));
        }
        recyclerView = findViewById(R.id.lineList);
        alertAdapter = new AlertAdapter();
        recyclerView.setAdapter(alertAdapter);
    }

    void write_textFile(String outText) {

        try {
            File targetFile = new File(tableDirectory,  nowFileName +".txt");
            FileWriter fileWriter = new FileWriter(targetFile, false);

            // Always wrap FileWriter in BufferedWriter.
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(outText);
            bufferedWriter.close();
        }
        catch(IOException ex) {
            utils.logE("editor",nowFileName + "'\n"+ex.toString());
            Toast.makeText(getApplicationContext(), "Write table error "+nowFileName,Toast.LENGTH_LONG).show();
        }
    }

    String sortText(String txt) {
        String [] arrText = txt.split("\n");
        Arrays.sort(arrText);
        StringBuilder sortedText = new StringBuilder();
        for (String t: arrText)
            sortedText.append(t).append("\n");
        return sortedText.toString();
    }

    String sortPackage(String txt) {
        String [] arrText = txt.split("\n");
        for (int i = 0; i < arrText.length; i++)
            arrText[i] = arrText[i].trim();
        Arrays.sort(arrText);
        StringBuilder sortedText = new StringBuilder();
        for (String t: arrText) {
            if (isPackageTable) {
                String [] comment = t.split(";");
                String [] fields = comment[0].split("\\^");
                String oneLine = strPad(fields[0],14) + "^" + strPad(fields[1], 10) + "^"
                        + strPad(fields[2], 40);
                if (comment.length > 1)
                    oneLine += "; " + comment[1].trim();
                sortedText.append(oneLine).append("\n");
            } else
                sortedText.append(t).append("\n");
        }
        return sortedText.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    final String blank = "                         ";
    final String del = String.copyValueOf(new char[]{(char) Byte.parseByte("7F", 16)});
    private String strPad(String s, int size) {
        int chars = 0;
        s = s.trim();
        for (int i = 0; i < s.length(); i++) {
            String bite = s.substring(i,i+1);
            chars += (bite.compareTo(del)>0)? 2:1;
        }
        if (chars >= size)
            return s;
        int padL = (size - chars) / 2;
        int padR = size - chars - padL;
        return blank.substring(0, padL)+ s + blank.substring(0, padR);
    }
//
//    private String blankPad(String s, int size) {
//        int chars = 0;
//        s = s.trim();
//        for (int i = 0; i < s.length(); i++) {
//            String bite = s.substring(i,i+1);
//            chars += (bite.compareTo(del)>0)? 2:1;
//        }
//        if (chars >= size)
//            return s;
//        return s+blank.substring(0, size-chars);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_save) {
            if (isAlertFile) {
                Collections.sort(alertOneLines, new Comparator<AlertOneLine>(){       // object Sort
                    public int compare(AlertOneLine obj1, AlertOneLine obj2) {
                        return (obj1.getGroup()+obj1.getWho()).compareTo((obj2.getGroup()+obj2.getWho()));
                    }
                });
                StringBuilder s = new StringBuilder();
                for (int i = 0; i < alertOneLines.size(); i++) {
                    AlertOneLine alertOneLine = alertOneLines.get(i);
                    s.append(strPad(alertOneLine.getGroup(), 18)).append("^");
                    s.append(strPad(alertOneLine.getWho(), 32)).append("^");
                    s.append(strPad(alertOneLine.getKey1(), 12)).append("^");
                    s.append(strPad(alertOneLine.getKey2(), 12)).append("^");
                    s.append(strPad(alertOneLine.getTalk(), 12)).append(";");
                    s.append(alertOneLine.getComment()).append("\n");
                }
                write_textFile(s.toString());
            } else {
                TextView tv = findViewById(R.id.text_table);
                String s = tv.getText().toString();
                write_textFile((isPackageTable) ? sortPackage(s) : sortText(s));
            }
            Toast.makeText(getApplicationContext(),"Table Saved",Toast.LENGTH_SHORT).show();
            readOptionTables.read();
            finish();
        } else if (item.getItemId() == R.id.action_dup) {
            if (isAlertFile) {
                AlertOneLine alertOneLine = alertOneLines.get(linePos);
                alertOneLine.setSelect(false);
//                alertOneLines.set(linePos, alertOneLine);
//                alertOneLines.add(linePos, alertOneLine);
                alertOneLines.add(alertOneLine);
                linePos++;
                alertAdapter.notifyDataSetChanged();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.scrollToPosition(alertOneLines.size()-1);
                    }
                }, 100);
            } else
                textDuplicate_line();
        } else if (item.getItemId() == R.id.action_tab) {
            if (!isAlertFile)
                textInsert_tab();
        } else if (item.getItemId() == R.id.action_remove) {
            if (isAlertFile) {
                AlertOneLine alertOneLine = alertOneLines.get(linePos-1);
                if (alertOneLine.isSelect())
                    alertOneLines.remove(linePos-1);
                else
                    alertOneLines.remove(linePos);
                alertAdapter.notifyDataSetChanged();
            } else
                textRemove_line();
        }
        return false;
    }

    private void textInsert_tab() {
        EditText tv;
        tv = findViewById(R.id.text_table);
        int cPos = tv.getSelectionStart();
        String txt = tv.getText().toString();
        txt = txt.substring(0, cPos) + "    "+"\t" + txt.substring(cPos);
        tv.setText(txt);
        Editable et = tv.getText();
        Selection.setSelection(et, cPos);
    }

    private void textRemove_line() {
        EditText tv;
        tv = findViewById(R.id.text_table);
        int cPos = tv.getSelectionStart();
        String txt = tv.getText().toString();
        int sPos = txt.lastIndexOf("\n", cPos);
        int ePos = txt.indexOf("\n", cPos + 1);
        txt = txt.substring(0, sPos) + txt.substring(ePos);
        tv.setText(txt);
        Editable et = tv.getText();
        Selection.setSelection(et, cPos);
    }

    private void textDuplicate_line() {
        EditText tv;
        tv = findViewById(R.id.text_table);
        int cPos = tv.getSelectionStart();
        String txt = tv.getText().toString();
        int sPos = txt.lastIndexOf("\n", cPos);
        int ePos = txt.indexOf("\n",cPos+1);
        String currLine = txt.substring(sPos, ePos);
        txt = txt.substring(0,sPos)+currLine+txt.substring(sPos);
        tv.setText(txt);
        Editable et = tv.getText();
        Selection.setSelection(et, cPos);
    }
}
