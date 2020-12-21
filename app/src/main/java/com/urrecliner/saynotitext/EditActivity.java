package com.urrecliner.saynotitext;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class EditActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_table);


        searchActivity = this;
    }
}
