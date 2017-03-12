package com.example.berthold.highscore;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Button;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);


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
