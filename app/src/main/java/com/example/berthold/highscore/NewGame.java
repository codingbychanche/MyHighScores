/**
 * Add a new game
 *
 * @author  Berthold Fritz 2016
 *
 */
package com.example.berthold.highscore;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NewGame extends AppCompatActivity {

    /**
     * Add a new game
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Input fields

        final EditText gameNameFieldO;
        gameNameFieldO = (EditText) findViewById(R.id.nameOfGame);


        // Button
        final Button enterScoreO;
        enterScoreO=(Button) findViewById(R.id.add_score);

        // Show titel

        getSupportActionBar().setTitle("Neues Spiel");

        // Floating action button, Save and back

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.save_and_exit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get name of game
                String name=gameNameFieldO.getText().toString();


                // If name of game was entered, save entry. Else, leave without saving

                if (!name.equals("")) {

                    // Check if this game already exists

                    if (!DB.doesExist("games","name",name,MainActivity.conn)){
                        DB.insert("insert into games (name) values ('" + name + "')", MainActivity.conn);
                        Toast.makeText(getApplicationContext(), "Neues Spiel hinzugefügt", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Das Spiel gibt es schon, Bitte einen anderen Namen wählen", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Kein Spielname eingegeben. Nichts gespeichert", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

        // Button 'save Game and add score'

        enterScoreO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get name of game
                String name=gameNameFieldO.getText().toString();

                // Save game, if it does not already exists
                if (!DB.doesExist("games","name",name,MainActivity.conn)) {
                    DB.insert("insert into games (name) values ('" + name + "')", MainActivity.conn);
                    Toast.makeText(getApplicationContext(), "Neues Spiel hinzugefügt", Toast.LENGTH_LONG).show();

                    // Get primary key
                    int key1=DB.getKey1("games","name",name,MainActivity.conn);

                    // Now enter score for this game....
                    Intent i = new Intent(NewGame.this, NewScoreV2.class);

                    // This flag destroy's the activity from which this activity
                    // was started from, if it is still running.

                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra("key1", key1);
                    i.putExtra("name", name);
                    startActivity(i);
                } else {
                    Toast.makeText(getApplicationContext(), "Das Spiel gibt es schon, Bitte einen anderen Namen wählen", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
