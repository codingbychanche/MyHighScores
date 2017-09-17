package com.example.berthold.highscore;

/**
 * Lists the scores of the selected game
 *
 * Allows the user to enter a new score for the game
 *
 * @author Berthold Fritz 2016
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Process;
import android.os.StrictMode;
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

    private ListView myListView;
    private ArrayAdapter myListAdapter;
    private int key1;
    DecimalFormat df=new DecimalFormat("#,###,###");

    ImageView screenshoot;

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
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.save_score);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.fab_bounce);
        screenshoot=(ImageView)findViewById(R.id.screenshoot);

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

            // Get Highest score for this game and load the screenshoot
            StringBuffer highscore = DB.sqlRequest("select max(score),picture from scores where key2=" + key1, MainActivity.conn);
            String[] h=highscore.toString().split(",");
            //todo bug when no picture taken
            final String path=h[1].replace("#","");

            // Check if image file exists
            if ((new File(path)).exists()) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmapOfScreenshoot = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.no_picture_taken_yet);

                        BitmapFactory.Options metaData = new BitmapFactory.Options();
                        metaData.inJustDecodeBounds = false;

                        //Get screen size = target size of pic
                        Display display = getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        float displayWidth = size.x;
                        float displayHeight = size.y;

                        metaData.inSampleSize = 1;       // Scale image down in size and reduce it's memory footprint
                        bitmapOfScreenshoot = MyBitmapTools.scaleBitmap(BitmapFactory.decodeFile(path, metaData), displayWidth, displayHeight);
                        screenshoot.setImageBitmap(bitmapOfScreenshoot);

                    }
                }).run();
            }

            // Show List
            myListView = (ListView) findViewById(R.id.scoreList);
            String[] rr = niceResult.toString().split(",");
            myListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, rr);
            myListView.setAdapter(myListAdapter);

        } else fab.startAnimation(animation);

    }
}
