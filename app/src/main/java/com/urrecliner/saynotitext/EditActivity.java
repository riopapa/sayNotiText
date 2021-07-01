package com.urrecliner.saynotitext;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static com.urrecliner.saynotitext.Vars.nowFileName;
import static com.urrecliner.saynotitext.Vars.alertLines;
import static com.urrecliner.saynotitext.Vars.tableDirectory;
import static com.urrecliner.saynotitext.Vars.utils;

public class EditActivity extends AppCompatActivity {

    boolean isAlertFile, isPackageTable, isRotate = false;
    RecyclerView recyclerView;
    AlertAdapter alertAdapter;
    ImageView removeView, dupView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        isAlertFile = nowFileName.equals("kakaoAlerts");
//        if (isAlertFile)
//            isRotate = true;
//        if (nowFileName.equals("packageTables"))
//            isRotate = true;
//        if (isRotate)
//            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        isPackageTable = nowFileName.equals("packageTables");
        removeView = findViewById(R.id.action_remove);
        dupView = findViewById(R.id.action_dup);
        EditText tv = findViewById(R.id.text_table);
        String[] lines = utils.readLines(new File(tableDirectory, nowFileName + ".txt"));
        if (isAlertFile) {
            tv.setVisibility(View.GONE);
            buildAlertLines(lines);
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

    void buildAlertLines(String[] lines) {
        alertLines = new ArrayList<>();
        for (int idx = 0; idx < lines.length; idx++) {
            String lGroup, lWho, lKey1, lKey2, lTalk, lMemo;
            lines[idx] = lines[idx].replace("\\t", "");
            String[] strings = lines[idx].split(";");
            lMemo = (strings.length > 1) ? strings[1].trim() : "";
            strings = strings[0].split("\\^");
            lGroup = strings[0].trim();
            lWho = strings[1].trim();
            lKey1 = strings[2].trim();
            lKey2 = strings[3].trim();
            lTalk = (strings.length > 4) ? strings[4].trim() : "";
            alertLines.add(new AlertLine(lGroup, lWho, lKey1, lKey2, lTalk, lMemo));
        }
        recyclerView = findViewById(R.id.lineList);
        alertAdapter = new AlertAdapter();
        recyclerView.setAdapter(alertAdapter);
    }

    void writeTextFile(String outText) {

        try {
            File targetFile = new File(tableDirectory, nowFileName + ".txt");
            FileWriter fileWriter = new FileWriter(targetFile, false);

            // Always wrap FileWriter in BufferedWriter.
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(outText);
            bufferedWriter.close();
        } catch (IOException ex) {
            utils.logE("editor", nowFileName + "'\n" + ex.toString());
            Toast.makeText(getApplicationContext(), "Write table error " + nowFileName, Toast.LENGTH_LONG).show();
        }
    }

    String sortText(String txt) {
        String[] arrText = txt.split("\n");
        Arrays.sort(arrText);
        StringBuilder sortedText = new StringBuilder();
        for (String t : arrText)
            sortedText.append(t).append("\n");
        return sortedText.toString();
    }

    String sortPackage(String txt) {
        String[] arrText = txt.split("\n");
        for (int i = 0; i < arrText.length; i++)
            arrText[i] = arrText[i].trim();
        Arrays.sort(arrText);
        StringBuilder sortedText = new StringBuilder();
        for (String t : arrText) {
            if (isPackageTable) {
                String[] memo = t.split(";");
                String[] fields = memo[0].split("\\^");
                String oneLine = strPad(fields[0], 14) + "^" + strPad(fields[1], 10) + "^"
                        + strPad(fields[2], 40);
                if (memo.length > 1)
                    oneLine += "; " + memo[1].trim();
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
        s = s.trim();
        int chars = getByteLength(s);
        if (chars >= size)
            return s;
        int padL = (size - chars) / 2;
        int padR = size - chars - padL;
        return blank.substring(0, padL) + s + blank.substring(0, padR);
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
                // object Sort
                alertLines.sort((obj1, obj2) -> (obj1.getGroup() + obj1.getWho()).compareTo((obj2.getGroup() + obj2.getWho())));
                String sv = "sv";
                int[] padLen = getMaxLengths();
                StringBuilder s = new StringBuilder();
                for (int i = 0; i < alertLines.size(); i++) {
                    AlertLine alertLine = alertLines.get(i);
                    if (!alertLine.getGroup().equals(sv)) {
                        sv = alertLine.getGroup();
                        s.append("\n");
                    }
                    s.append(strPad(alertLine.getGroup(), padLen[0])).append("^");
                    s.append(strPad(alertLine.getWho(), padLen[1])).append("^");
                    s.append(strPad(alertLine.getKey1(), padLen[2])).append("^");
                    s.append(strPad(alertLine.getKey2(), padLen[3])).append("^");
                    s.append(strPad(alertLine.getTalk(), padLen[4])).append(";");
                    s.append(alertLine.getMemo()).append("\n");
                }
                writeTextFile(s.toString());

            } else {
                TextView tv = findViewById(R.id.text_table);
                String s = tv.getText().toString();
                writeTextFile((isPackageTable) ? sortPackage(s) : sortText(s));
            }
            Toast.makeText(getApplicationContext(), "Table Saved", Toast.LENGTH_SHORT).show();
            new ReadOptionTables().read();
            finish();
        } else if (item.getItemId() == R.id.action_rotate) {
            isRotate = !isRotate;
            this.setRequestedOrientation(
                    (isRotate) ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        return false;
    }

    int [] getMaxLengths() {
        int [] maxLen = new int[5];
        int bl;
        for (int i = 0; i < alertLines.size(); i++ ){
            AlertLine al = alertLines.get(i);
            bl = getByteLength(al.getGroup()); if (bl > maxLen[0]) maxLen[0] = bl;
            bl = getByteLength(al.getWho()); if (bl > maxLen[1]) maxLen[1] = bl;
            bl = getByteLength(al.getKey1()); if (bl > maxLen[2]) maxLen[2] = bl;
            bl = getByteLength(al.getKey2()); if (bl > maxLen[3]) maxLen[3] = bl;
            bl = getByteLength(al.getTalk()); if (bl > maxLen[4]) maxLen[4] = bl;
        }
        return maxLen;
    }

    int getByteLength(String s) {
        int chars = 0;
        for (int i = 0; i < s.length(); i++) {
            String bite = s.substring(i,i+1);
            chars += (bite.compareTo(del)>0)? 2:1;
        }
        return chars;
    }
//    private void textRemove_line() {
//        EditText tv;
//        tv = findViewById(R.id.text_table);
//        int cPos = tv.getSelectionStart();
//        String txt = tv.getText().toString();
//        int sPos = txt.lastIndexOf("\n", cPos);
//        int ePos = txt.indexOf("\n", cPos + 1);
//        txt = txt.substring(0, sPos) + txt.substring(ePos);
//        tv.setText(txt);
//        Editable et = tv.getText();
//        Selection.setSelection(et, cPos);
//    }
//
//    private void textDuplicate_line() {
//        EditText tv;
//        tv = findViewById(R.id.text_table);
//        int cPos = tv.getSelectionStart();
//        String txt = tv.getText().toString();
//        int sPos = txt.lastIndexOf("\n", cPos);
//        int ePos = txt.indexOf("\n",cPos+1);
//        String currLine = txt.substring(sPos, ePos);
//        txt = txt.substring(0,sPos)+currLine+txt.substring(sPos);
//        tv.setText(txt);
//        Editable et = tv.getText();
//        Selection.setSelection(et, cPos);
//    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAlertFile)
            alertAdapter.notifyDataSetChanged();
    }
}
