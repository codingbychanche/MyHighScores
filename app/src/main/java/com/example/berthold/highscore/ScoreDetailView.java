package com.example.berthold.highscore;

/*
 * ScoreDetailView.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 1/2/18 9:49 PM
 */

/**
 * Detail view of one score entry
 *
 * Shows Screenshoot and other info...
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.FileNotFoundException;

public class ScoreDetailView extends AppCompatActivity {

    Bitmap pic;
    Handler h=new Handler();
    ImageView canvas;
    ProgressBar picProgress;
    TextView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_detail_view);

        // Get data
        Bundle extra = getIntent().getExtras();
        String name = extra.getString("game");
        int key1 = extra.getInt("key1");
        String score=extra.getString("score");
        String date=extra.getString("date");

        String comment=extra.getString("comment");
        if (comment==null) comment="-";

        String evaluation=extra.getString("evaluation");
        if(evaluation==null) evaluation="-";

        final String picture = extra.getString("picpath");

        // Show game title in action bar
        getSupportActionBar().setTitle(name+" : "+score+" Punkte");

        // UI
        canvas = (ImageView) findViewById(R.id.screenshoot);
        picProgress = (ProgressBar) findViewById(R.id.pic_progress);
        info=(TextView) findViewById(R.id.info);
        info.setText(date+"\n\n"+comment+"\n"+evaluation);

        //Get screen size = target size of pic
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final float displayWidth = size.x;
        final float displayHeight = size.y;

            // Load and scale picture
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    pic = null;
                    do {

                        // If this score has a screenshoot, show it. if not, tell the user so.....
                        try {
                            pic = MyBitmapTools.scaleBitmap(BitmapFactory.decodeFile(picture), displayWidth, displayHeight, "-");
                        } catch (Exception f){
                            pic= MyBitmapTools.scaleBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.no_picture_taken_yet),displayWidth, displayHeight, "-");
                        }

                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                picProgress.setVisibility(View.VISIBLE);
                            }
                        });

                        // Wait a vew millisec's to enable the main UI thread
                        // to react.
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }

                    } while (pic == null);

                    // All done... Set picture....
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            canvas.setImageBitmap(pic);
                            picProgress.setVisibility(View.GONE);
                        }
                    });
                }
            });
            // Load picture....
            t.start();
    }
}
