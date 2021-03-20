package com.urrecliner.saynotitext;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import static com.urrecliner.saynotitext.Vars.nowFileName;
import static com.urrecliner.saynotitext.Vars.oneLines;
import static com.urrecliner.saynotitext.Vars.linePos;
import static com.urrecliner.saynotitext.Vars.readOptionTables;
import static com.urrecliner.saynotitext.Vars.tableDirectory;
import static com.urrecliner.saynotitext.Vars.utils;
import static java.lang.Byte.compare;

public class EditActivity extends AppCompatActivity {

    boolean isAlertFile;
    RecyclerView recyclerView;
    AlertAdapter alertAdapter;
    static ImageView removeView, dupView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        isAlertFile = nowFileName.equals("kakaoAlerts");
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
        oneLines = new ArrayList<>();

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
            oneLines.add(new OneLine(false, lGroup, lWho, lKey1, lKey2, lTalk, lComment));
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
        String sortedText = "";
        for (String t: arrText) {
            sortedText += t + "\n";
        }
        return sortedText;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_save) {
            if (isAlertFile) {
                Collections.sort(oneLines, new Comparator<OneLine>(){       // object Sort
                    public int compare(OneLine obj1, OneLine obj2) {
                        return (obj1.getGroup()+obj1.getWho()).compareTo((obj2.getGroup()+obj2.getWho()));
                    }
                });
                String s = "";
                for (int i = 0; i < oneLines.size(); i++) {
                    OneLine oneLine = oneLines.get(i);
                    s += strPad(oneLine.getGroup(), 18)+"^";
                    s += strPad(oneLine.getWho(), 32)+"^";
                    s += strPad(oneLine.getKey1(), 12)+"^";
                    s += strPad(oneLine.getKey2(), 12)+"^";
                    s += strPad(oneLine.getTalk(), 12)+";";
                    s += oneLine.getComment()+"\n";
                }
                write_textFile(s);
            } else {
                TextView tv = findViewById(R.id.text_table);
                String s = tv.getText().toString();
                write_textFile(sortText(s));
            }
            Toast.makeText(getApplicationContext(),"Table Saved",Toast.LENGTH_SHORT).show();
            readOptionTables.read();
            finish();
        } else if (item.getItemId() == R.id.action_dup) {
            if (isAlertFile) {
                OneLine oneLine = oneLines.get(linePos);
                oneLine.setSelect(false);
                oneLines.set(linePos, oneLine);
                oneLines.add(linePos, oneLine);
                alertAdapter.notifyDataSetChanged();
            } else
                textDuplicate_line();
        } else if (item.getItemId() == R.id.action_tab) {
            if (isAlertFile) {

            } else
                textInsert_tab();
        } else if (item.getItemId() == R.id.action_remove) {
            if (isAlertFile) {
                OneLine oneLine = oneLines.get(linePos-1);
                if (oneLine.isSelect())
                    oneLines.remove(linePos-1);
                else
                    oneLines.remove(linePos);
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
