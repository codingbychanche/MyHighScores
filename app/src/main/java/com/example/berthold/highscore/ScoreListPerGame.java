package com.example.berthold.highscore;

/*
 * ScoreListPerGame.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 1/18/18 10:20 PM
 */

/**
 * Lists the scores of the selected game
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Process;
import android.os.StrictMode;
import android.renderscript.Sampler;
import android.support.annotation.IntegerRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;

public class ScoreListPerGame extends AppCompatActivity {

    private ListView        myListView;
    private ArrayAdapter    myListAdapter;
    private int             key1;

    StringBuffer    result;
    String []       rs;
    String []       r;

    String date;

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

        // Show game title in action bar
        getSupportActionBar().setTitle(name);

        // Add new score?
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

        // Delete score?
        FloatingActionButton deleteScore=(FloatingActionButton) findViewById(R.id.delete_score);

        deleteScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(view.getContext(),ScoreDelete.class);
                i.putExtra("key1",key1);
                i.putExtra("name",name);
                view.getContext().startActivity(i);
            }
        });

        // Get data
        result = DB.sqlRequest("select key1,score,date,picture,comment,evaluation from scores where key2=" + key1 + " order by score DESC", MainActivity.conn);
        rs = result.toString().split("#");              // Get rows
        StringBuffer niceResult=new StringBuffer();     // Will contain date and score in a user- readable format

        if (!rs[0].equals("empty")) {
            fab.clearAnimation();
            deleteScore.show();

            for (int i = 0; i < rs.length; i++) {
                r = rs[i].toString().split(",");       // Separate fields

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

            // Item clicked? If yes, open detail view.....
            // We need the key1 value of the selected score row which
            // we pass to to detail view activity in order to open the
            // data base entry....
            myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    r = rs[position].toString().split(",");       // Separate fields
                    int k1= Integer.valueOf(r[0]);
                    System.out.println("--------------Pos:"+position+"     Key1"+k1);

                    Intent in = new Intent(view.getContext(), ScoreDetailView.class);
                    in.putExtra("key1",k1);
                    in.putExtra("game",name);
                    in.putExtra("date",FormatTimeStamp.german(r[2], FormatTimeStamp.WITH_TIME));
                    in.putExtra("score",df.format(Integer.decode(r[1])));
                    in.putExtra("picpath",r[3]);

                    if (r.length>4) in.putExtra("comment",r[4]);
                    if (r.length>5) in.putExtra("evaluation",r[5]);

                    view.getContext().startActivity(in);
                }
            });
        } else {
            niceResult.append("Keine Einträge...");
            fab.startAnimation(animation);
            deleteScore.hide();
            showList(niceResult);
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
