package com.example.berthold.highscore;

/**
 * New score
 *
 * @author Berthold Fritz 2016
 *
 */

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class NewScoreV2 extends AppCompatActivity {

    private int IMAGE_CAPTURE=1;    // Req- code for camera usage

    private Uri imageS;             // Uri for screenshoot
    private String pic;             // Screenshoot path
    private int key1;               // Link to game this score belongs to
    private String name;            // Name of the game

    private static final int PIC_WIDTH=1000;    // This is the size at which the picture will saved
    private static final int PIC_HEIGHT=500;
    private static final int COMPRESS=100;      // Compression rate 0 means max and worst quality, 100 means best...

    // Debug info
    String tag;

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

        // Debug
        tag=NewScoreV2.class.getSimpleName();

        Bundle extra=getIntent().getExtras();
        key1=extra.getInt("key1");
        name=extra.getString("name");

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

                // If score is not 0, then save it and leave this activity.
                if (!s.equals("") && score !=0) {
                    // If score just entered already exists, don't save it!
                    if (DB.doesExist("scores","score",String.valueOf(score),MainActivity.conn)) {
                        scoreDoesAlreadyExist();
                        saveNegative();
                    } else {
                        saveScore(score, c, e);
                        finish();
                    }
                // Score is 0 or no score was entered, Inform user that this could not be saved
                } else saveNegative();
            }
        });

        // Take screenshoot ?
        ImageButton photo=(ImageButton)findViewById(R.id.kamera);
        photo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View view){
            // Get score from input

                String s=scoreFieldO.getText().toString();
                int score=getScore(s);
                String c=gameCommentFieldO.getText().toString();
                String e=gameEvaluationO.getText().toString();

                // If input was not empty and it's value was not 0, take picture
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
     */

    private void saveScore(int score,String comment,String evaluation)
    {

        DB.insert("insert into scores (key2,score,date,comment,evaluation,picture) values " +
                    "("+key1+"," + score +",CURRENT_TIMESTAMP,'"+comment+"','"+evaluation+"','"+pic+"')", MainActivity.conn);

        Log.d(tag,"Saved score. Picture Path "+pic);
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

    /**
     * Score already exists
     *
     */

    private void  scoreDoesAlreadyExist(){
        Toast.makeText(getApplicationContext(), "Dieser Punktestand wurde schon eingetragen", Toast.LENGTH_LONG).show();
    }

    /**
     * This is the method called, after the picture was taken
     * and camera has finished. It writes the uri of the taken
     * picture into the db's 'pic' field
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
                    File f = getFilesDir();
                    String path = (f.getAbsolutePath() + "/"+name);

                    // Get image just taken and save it compressed
                    // This only reduces the quality and the memory footprint, but not
                    // the size of the picture taken!
                    FileOutputStream fos=new FileOutputStream(path);
                    Bitmap b = MyBitmapTools.scaleBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), imageS),PIC_WIDTH,PIC_HEIGHT);
                    b.compress(Bitmap.CompressFormat.JPEG,COMPRESS,fos);
                    fos.close();

                    // Show picture just taken
                    ImageButton photo=(ImageButton)findViewById(R.id.kamera);
                    BitmapFactory.Options metaData = new BitmapFactory.Options();
                    metaData.inJustDecodeBounds = false;
                    metaData.inSampleSize = 1;       // Scale image down in size and reduce it's memory footprint
                    b = MyBitmapTools.scaleBitmap(BitmapFactory.decodeFile(path, metaData),800,500);
                    photo.setImageBitmap(b);

                    // Set global picture path
                    pic=path;
                    Log.d(tag,"Picture Path "+path);

                } catch (IOException e) {
                    Log.d(tag,"Could not save image");
                    Log.d(tag,e.toString());
                }
            }
        }
    }
}
