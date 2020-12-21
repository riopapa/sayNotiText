package com.urrecliner.saynotitext;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
        setContentView(R.layout.edit_table);

        String [] lines = utils.readLines(new File(tableDirectory, nowFileName+".txt"));
        StringBuilder sb = new StringBuilder();
        for (String s : lines) sb.append(s).append("\n");
        String text = sb.toString()+"\n";
        TextView tv = findViewById(R.id.text_table);
        tv.setText(text);
        tv.setFocusable(true);
        tv.setEnabled(true);
        tv.setClickable(true);
        tv.setFocusableInTouchMode(true);
        Button bt = findViewById(R.id.button_save);
        text = "Save ["+nowFileName+"]";
        bt.setText(text);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write_textFile();
                Toast.makeText(getApplicationContext(),"Reading param files",Toast.LENGTH_SHORT).show();
                readOptionTables.read();
                finish();
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

}
