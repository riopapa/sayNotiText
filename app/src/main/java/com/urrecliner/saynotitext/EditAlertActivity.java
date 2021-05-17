package com.urrecliner.saynotitext;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.urrecliner.saynotitext.Vars.alertLines;
import static com.urrecliner.saynotitext.Vars.linePos;

public class EditAlertActivity extends AppCompatActivity {

    AlertLine alertLine;
    EditText eTGroup, eTWho, eTKey1, eTKey2, eTTalk, eTMemo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_edit);
//        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        alertLine = alertLines.get(linePos);
        eTGroup = findViewById(R.id.eGroup); eTGroup.setText(alertLine.getGroup());
        eTWho = findViewById(R.id.eWho); eTWho.setText(alertLine.getWho());
        eTKey1 = findViewById(R.id.eKey1); eTKey1.setText(alertLine.getKey1());
        eTKey2 = findViewById(R.id.eKey2); eTKey2.setText(alertLine.getKey2());
        eTTalk = findViewById(R.id.eTalk);  eTTalk.setText(alertLine.getTalk());
        eTMemo = findViewById(R.id.eMemo); eTMemo.setText(alertLine.getMemo());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_one, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_save) {
            alertLine = new AlertLine(eTGroup.getText().toString(), eTWho.getText().toString(),
                    eTKey1.getText().toString(), eTKey2.getText().toString(),
                    eTTalk.getText().toString(), eTMemo.getText().toString());
            alertLines.set(linePos, alertLine);
            finish();
        } else if (item.getItemId() == R.id.action_dup) {
            alertLines.add(linePos, alertLine);
            eTMemo.setText(new SimpleDateFormat("MM-dd", Locale.KOREA).format(new Date()));
        } else if (item.getItemId() == R.id.action_remove) {
            alertLines.remove(linePos);
            finish();
        }
        return false;
    }
}
