/**
 * Lists the scores of the selected game
 *
 * Allows the user to enter a new score for the game
 *
 * @author Berthold Fritz 2016
 */

package com.example.berthold.highscore;

import android.app.Activity;
import android.content.Intent;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class ScoreListPerGame extends AppCompatActivity {

    private ListView myListView;
    private ArrayAdapter myListAdapter;
    private int key1;
    DecimalFormat df=new DecimalFormat("#,###,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_list_per_game);
    }

    /**
     * Called when activity is resumed
     * e.g. when back button of device was pressed
     *
     */

    @Override
    public void onResume() {

        super.onResume();

        // get key 1 and name of game
        Bundle extra = getIntent().getExtras();
        key1 = extra.getInt("key1");
        final String name = extra.getString("name");
        System.out.println("----- recieved key 1:" + name);

        // Show game title in action bar

        getSupportActionBar().setTitle(name);

        // Gui
        // Floating button, add a new score
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.save_score);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fab_bounce);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), NewScoreV2.class);
                i.putExtra("key1", key1);
                i.putExtra("name", name);
                view.getContext().startActivity(i);
            }
        });

        // Get data
        StringBuffer result = DB.sqlRequest("select score,date from scores where key2=" + key1 + " order by date DESC", MainActivity.conn);
        String[] rs = result.toString().split("#");     // Get rows
        StringBuffer niceResult=new StringBuffer();     // Will contain date and score in a user- readable format

        if (!rs[0].equals("empty")) {
            fab.clearAnimation();
            for (int i = 0; i < rs.length; i++) {
                String[] r = rs[i].toString().split(",");       // Separate fields r[0]=score// r[1]= date

                // r[0]=Score
                // r[1]=date
                // r[2]=comment
                // r[3]=evaluation
                String date = FormatTimeStamp.german(r[1], FormatTimeStamp.WITH_TIME);
                niceResult.append(df.format(Integer.decode(r[0])) + "\n" + date + ",");

            }

            // Show List
            myListView = (ListView) findViewById(R.id.scoreList);
            String[] rr = niceResult.toString().split(",");
            myListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, rr);
            myListView.setAdapter(myListAdapter);

        } else fab.startAnimation(animation);

    }
}
