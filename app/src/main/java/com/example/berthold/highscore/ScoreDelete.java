package com.example.berthold.highscore;

/**
 * Delete score and, if taken, it's screenshot
 *
 * @author Berthold Fritz 2016
 */

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

public class ScoreDelete extends AppCompatActivity {

    private ListView        myListView;
    private ArrayAdapter    myListAdapter;
    private int             key1;               // Primary key of game the scores belong to
    private int             key1OfScoreEntry;   // Primary key of the score entry to be deleted
    private String          name;

    StringBuffer result;
    String[] rs;
    String[] r;

    String date;

    DecimalFormat df = new DecimalFormat("#,###,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_delete);
    }

    /**
     * Called when activity is resumed
     * e.g. when back button of device was pressed
     */

    @Override
    public void onResume() {

        super.onResume();

        // Get name of game
        Bundle extra = getIntent().getExtras();
        name = extra.getString("name");
        key1 = extra.getInt("key1");

        // Show game title in action bar
        getSupportActionBar().setTitle("Punkte von \"" + name + "\" löschen.....");

        // Confirm Dialog
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) dialogReturn(true);
                if (which == DialogInterface.BUTTON_NEGATIVE) dialogReturn(false);
            }
        };

        // Build Dialog
        final AlertDialog.Builder chooseYesNo = new AlertDialog.Builder(this);
        chooseYesNo.setMessage(R.string.reallyDeleteScore).setPositiveButton(R.string.yes, dialogClickListener).
                setNegativeButton(R.string.no, dialogClickListener);

        // Get data
        result = DB.sqlRequest("select key1,score,date,picture,comment,evaluation from scores where key2=" + key1 + " order by score DESC", MainActivity.conn);
        rs = result.toString().split("#");                  // Get rows
        StringBuffer niceResult = new StringBuffer();       // Will contain date and score in a user- readable format

        if (!rs[0].equals("empty")) {
            for (int i = 0; i < rs.length; i++) {
                r = rs[i].toString().split(",");                // Separate fields

                // r[0]=Key1
                // r[1]=Score
                // r[2]=date
                // r[3]=picture path
                // r[4]=comment
                // r[5]=evaluation
                date = FormatTimeStamp.german(r[2], FormatTimeStamp.WITH_TIME);
                niceResult.append(df.format(Integer.decode(r[1])) + "\n" + date + ",");
            }

            showList(niceResult);

            // Item was clicked...
            // Delete?

            myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    r = rs[position].toString().split(",");       // Separate fields
                    key1OfScoreEntry = Integer.valueOf(r[0]);
                    System.out.println("--------------Pos:" + position + "     Key1" + key1OfScoreEntry);
                    chooseYesNo.show();

                }
            });
        } else {
            niceResult.append("Keine Einträge...");
            showList(niceResult);
        }
    }

    /**
     * Callback for yesNoDialog.
     *
     * Deletes the selected DB entry if positive button of yesNoDialog was
     * clicked.
     *
     * @param confirmed true if positive button was clicked.
     */

    public void dialogReturn(boolean confirmed) {

        if (confirmed) {

            try {
                Statement s = MainActivity.conn.createStatement();
                // delete score

                s.executeUpdate("delete from scores where key1=" + key1OfScoreEntry);

                // Restart activity (and show updated list of scores to allow another one
                // to be deleted or to go back to main
                Intent i=new Intent(ScoreDelete.this,ScoreDelete.class);
                i.putExtra("key1",key1);
                i.putExtra("name",name);
                startActivity(i);
                finish();


            } catch (SQLException e) {}
        }
    }

    /**
     * Show list
     */

    private void showList(StringBuffer niceResult){

        myListView = (ListView) findViewById(R.id.scoreList);
        String[] rr = niceResult.toString().split(",");
        myListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, rr);
        myListView.setAdapter(myListAdapter);
    }
}

