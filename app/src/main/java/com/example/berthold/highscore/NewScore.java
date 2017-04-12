/**
 * New score
 *
 * @author Berthold Fritz 2016
 *
 */

package com.example.berthold.highscore;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewDebug;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.net.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class NewScore extends AppCompatActivity {

    String tag;              // Debug info => for "log.."
    int IMAGE_CAPTURE=1;     // Req- code for camera usage
    int key1;                // Link to game this score belongs to
    int pictureKey;          // Link to score entry a picture belongs to
    Uri imageS;              // Uri for screenshoot in media gallery
    String name;             // Full path to which the screenshoot file will be saved

    /**
     * Add a new score
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_score);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extra=getIntent().getExtras();
        key1=extra.getInt("key1");
        name=extra.getString("name");

        tag="Debug: New Score";

        // Input fields

        final EditText scoreFieldO;
        scoreFieldO = (EditText) findViewById(R.id.score);

        final EditText gameCommentFieldO;
        gameCommentFieldO=(EditText) findViewById(R.id.comment);

        final EditText gameEvaluationO;
        gameEvaluationO=(EditText) findViewById(R.id.evaluation);

        // Fab's
        // Save score
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s=scoreFieldO.getText().toString();
                int score=getScore(s);

                String c=gameCommentFieldO.getText().toString();
                String e=gameEvaluationO.getText().toString();

                // If score is not 0, then save it and restart this activity.
                if (!s.equals("") && score !=0) {
                    saveScore(score,c,e);

                    File f = getFilesDir();
                    String path = (f.getAbsolutePath() + "/"+name);

                    DB.insert("update scores set picture='"+path+"' where key1="+pictureKey,MainActivity.conn);
                    finish();

                } else {
                    saveNegative();
                    finish();
                }
            }
        });

        // Take screenshoot ?
        ImageButton photo=(ImageButton)findViewById(R.id.kamera);
        photo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){

                // Get contents of all input fields
                String s=scoreFieldO.getText().toString();
                int score=getScore(s);
                String c=gameCommentFieldO.getText().toString();
                String e=gameEvaluationO.getText().toString();

                // If input was not empty and it's value was not 0, take picture and save pic. in DB
                if (!s.equals("") && score !=0 ){
                    startCamara();
                }
            }

        });
    }

    /**
     * Start Camera
     *
     */

    private void startCamara()
    {
        ContentValues v=new ContentValues();
        v.put(MediaStore.Images.Media.TITLE, "Highscore");
        v.put(MediaStore.Images.Media.DESCRIPTION,"Highscore Screenshoot");
        v.put(MediaStore.Images.Media.MIME_TYPE,"image/png");
        imageS=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v);

        Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT,imageS);
        startActivityForResult(i,IMAGE_CAPTURE);
    }

    /**
     * Get score
     */

    private int getScore(String s)
    {
        int score;
        try{
            score= Integer.parseInt(s);
        } catch (Exception e){
            score=0;
        }
        return score;
    }

    /**
     * Save score
     *
     * @param   score       Value of score to be saved
     * @param   comment     A comment e.g. Difficulty: hard....
     * @param   evaluation  An game speciefic evaluation,like: "Star cook first class" or "Best ever..."
     * @return  key1        If score was saved, return key1- value for the entry generated
     *
     */

    private void saveScore(int score,String comment,String evaluation)
    {
        // Get key1- value of game selected
        // Each entry in 'scores' will be: key2=key1
        DB.insert("insert into scores (key2,score,date,comment,evaluation) values " +
                "(" + key1 + "," + score + ",CURRENT_TIMESTAMP,'" + comment + "','" + evaluation + "')", MainActivity.conn);
        savePositive();
    }

    /**
     * Positive feedback
     *
     */

    private void savePositive()
    {
        Toast.makeText(getApplicationContext(), "Punkte gemerkt", Toast.LENGTH_LONG).show();
    }

    /**
     * Negative feedback
     *
     */

    private void saveNegative()
    {
        Toast.makeText(getApplicationContext(), "Nichts gemerkt", Toast.LENGTH_LONG).show();
    }

    /*
     * Restart this activity
     *
     */

    private void restart()
    {
        // Before we leave, we need to send key1 back to list per game

        Intent in=new Intent(NewScore.this,ScoreListPerGame.class);
        in.putExtra("key1",key1);
        in.putExtra("name",name);

        // This flag destroy's the activity from which this activity
        // was started from, if it is still running.

        in.addFlags(in.FLAG_ACTIVITY_CLEAR_TOP);
        savePositive();
        startActivity(in);
    }

    /**
     * This is the method called, after the picture was taken
     * and camera has finished. It gets the path of the taken
     * image from 'imageS', gets the bitmap from there, compresses
     * it and writes it to the app's home dir
     *
     * @global  pictureKey  This key is the key for the score entry matching the picture
     *                      just taken.
     *
     */

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);

        // Take picture with camera
        if(requestCode==IMAGE_CAPTURE){
            if(resultCode==RESULT_OK) {
                try
                {
                    // Get image just taken and save it compressed
                    // This only reduces the quality and the memory footprint, but not
                    // the size of the picture taken!
                    // todo: Find a way to reduce the size of the image also. Could speed up thing's a bit
                    File f = getFilesDir();

                    // Picture file will have the same name, as the game for which
                    // it was created. This file will be overwritten for each new
                    // screenshoot taken.
                    // todo: Allow to save a screenshoot for every score saved
                    String path = (f.getAbsolutePath() + "/"+name);
                    FileOutputStream fos=new FileOutputStream(path);
                    Bitmap b = MediaStore.Images.Media.getBitmap(getContentResolver(), imageS);
                    b.compress(Bitmap.CompressFormat.JPEG,15,fos);
                    fos.close();

                    // Show picture just taken
                    ImageButton photo=(ImageButton)findViewById(R.id.kamera);
                    BitmapFactory.Options metaData = new BitmapFactory.Options();
                    metaData.inJustDecodeBounds = false;
                    metaData.inSampleSize = 12;       // Scale image down in size and reduce it's memory footprint
                    b = BitmapFactory.decodeFile(path, metaData);
                    photo.setImageBitmap(b);

                } catch (IOException e) {
                    Log.d(tag,"Could not save image");
                }
            }
        }
    }
}
