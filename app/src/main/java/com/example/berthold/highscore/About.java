package com.example.berthold.highscore;

/*
 * About.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 5/17/17 10:47 AM
 */

/**
 * About
 */

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.TextView;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Show database info
        // You can run all sorts of statistics here and display them

        TextView dataBaseInfoO=(TextView)findViewById(R.id.dataBaseInfo);
        dataBaseInfoO.setText("Spiele insgesammt:"+DB.getNumberOfRows("games",MainActivity.conn)+"\n");
        dataBaseInfoO.append("Erfasste Punkte:"+DB.getNumberOfRows("scores",MainActivity.conn)+"\n");

        // Start SQL- Console?

        Button startConsole=(Button) findViewById(R.id.debugConsole);

        startConsole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(About.this, DebugDBAdmin.class);
                startActivity(in);
            }
        });
    }
}
