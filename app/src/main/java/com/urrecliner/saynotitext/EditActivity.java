package com.urrecliner.saynotitext;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import static com.urrecliner.saynotitext.Vars.nowFileName;
import static com.urrecliner.saynotitext.Vars.readOptionTables;
import static com.urrecliner.saynotitext.Vars.tableDirectory;
import static com.urrecliner.saynotitext.Vars.utils;

public class EditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        String [] lines = utils.readLines(new File(tableDirectory, nowFileName+".txt"));
        StringBuilder sb = new StringBuilder();
        for (String s : lines) sb.append(s).append("\n");
        String text = sb.toString()+"\n";
        EditText tv = findViewById(R.id.text_table);
        tv.setText(text);
        tv.setFocusable(true);
        tv.setEnabled(true);
        tv.setClickable(true);
        tv.setFocusableInTouchMode(true);
        tv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(EditActivity.this);
                dialog  .setTitle("Edit Table")
                        .setMessage("Select Option ")
                        .setPositiveButton("Dup Line", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                duplicate_line();
                            }
                        })
                        .setNeutralButton("Insert Tab", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                insert_tab();
                            }

                        })
                        .setNegativeButton("Del Line", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                remove_line();
                            }

                        }).create().show();
                return false;
            }
        });
    }

    void write_textFile() {
        TextView tv = findViewById(R.id.text_table);
        String outText = tv.getText().toString();
        outText = sortText(outText);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_save) {
            write_textFile();
            Toast.makeText(getApplicationContext(),"Table Saved",Toast.LENGTH_SHORT).show();
            readOptionTables.read();
            finish();
        } else if (item.getItemId() == R.id.action_dup) {
            duplicate_line();
        } else if (item.getItemId() == R.id.action_tab) {
            insert_tab();
        } else if (item.getItemId() == R.id.action_remove) {
            remove_line();
        }
        return false;
    }

    private void insert_tab() {
        EditText tv;
        tv = findViewById(R.id.text_table);
        int cPos = tv.getSelectionStart();
        String txt = tv.getText().toString();
        txt = txt.substring(0, cPos) + "    "+"\t" + txt.substring(cPos);
        tv.setText(txt);
        Editable et = tv.getText();
        Selection.setSelection(et, cPos);
    }

    private void remove_line() {
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

    private void duplicate_line() {
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
