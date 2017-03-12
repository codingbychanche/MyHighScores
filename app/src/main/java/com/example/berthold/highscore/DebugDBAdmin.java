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
import android.view.View;
import android.widget.*;

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

        startConsole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuffer result=DB.sqlRequest(sqlCommand.getText().toString(),MainActivity.conn);
                output.append("\n"+result.toString());
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

    }
}
