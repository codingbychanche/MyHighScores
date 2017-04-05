/**
 * Debug DB
 *
 * Provides an simple interface to test sql querys on the db
 *
 * Berthold Fritz 3/2017
 *
 */
package com.example.berthold.highscore;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.*;
import android.text.method.BaseMovementMethod;

public class DebugDBAdmin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_dbadmin);

        // Show titel

        getSupportActionBar().setTitle("SQL- Konsole");

        // Run SQL?

        ImageButton startConsole=(ImageButton) findViewById(R.id.runSql);
        final EditText sqlCommand=(EditText) findViewById(R.id.shellInput);
        final TextView output=(TextView) findViewById(R.id.shellOutput);
        output.setMovementMethod (new ScrollingMovementMethod());

        startConsole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuffer result=DB.sqlRequest(sqlCommand.getText().toString(),MainActivity.conn);
                String r=result.toString().replace("#","\n");
                output.append("\n"+r);

            }
        });


        // Clear command

        ImageButton clearCommand=(ImageButton) findViewById(R.id.clearCommand);

        clearCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlCommand.setText("");
            }
        });

        // Insert sql- commands
        // Select?

        Button insertSelect=(Button) findViewById(R.id.insertSelect);

        insertSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlCommand.append(" select ");
            }
        });

        // From?

        Button insertFrom=(Button) findViewById(R.id.insertFrom);

        insertFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlCommand.append(" from ");
            }
        });

        // Where?

        Button insertWhere=(Button) findViewById(R.id.insertWhere);

        insertWhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlCommand.append(" where ");
            }
        });

        // Equal?

        Button insertEqual=(Button) findViewById(R.id.insertEqual);

        insertEqual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlCommand.append("=");
            }
        });

        // Star
        Button insertStar=(Button) findViewById(R.id.insertStar);

        insertStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlCommand.append("*");
            }
        });

        // '
        Button insertHyp=(Button) findViewById(R.id.insertHyp);

        insertHyp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlCommand.append("'");
            }
        });

        // Like
        Button insertLike=(Button) findViewById(R.id.insertlike);

        insertLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlCommand.append("like");
            }
        });

        // %
        Button insertPerc=(Button) findViewById(R.id.insertPercent);

        insertPerc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqlCommand.append("%");
            }
        });
    }
}
