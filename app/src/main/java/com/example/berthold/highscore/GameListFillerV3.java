package com.example.berthold.highscore;

/*
 * GameListFillerV3.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 2/3/18 11:18 PM
 */

/**
 *
 * This fills the list using an Async Task
 *
 * This is a variation of GameListFillerv2.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.File;

public class GameListFillerV3 extends AsyncTask<String,GameListEntry,String> {

    /**
     * Constructor
     *
     * Creates a new filler object
     */

    public String search;
    public String sortingOrder;
    public Context c;
    public GameListAdapter_OLD gameList;
    public ProgressBar progressInfo;
    public int i;

    // DB
    public int key1;
    public static int maxScore;
    public static String comment;
    public static String evaluation;
    public static String date;
    public static StringBuffer resultFromScores;
    public static int entryType;
    public static SpannableString gameName;
    public StringBuffer resultFromGames;
    public String [] rsGames;

    // Debug
    String tag;

    GameListFillerV3(GameListAdapter_OLD gameList, Context c, String search, String sortingOrder, ProgressBar p) {
        this.search=search;
        this.c=c;
        this.gameList=gameList;
        this.progressInfo=p;
        this.sortingOrder=sortingOrder;
        this.gameList=gameList;
    }

    /**
     * Before execution....
     */

    @Override
    protected void onPreExecute(){

        // Get data
        resultFromGames = DB.sqlRequest("select key1,name,picture from games where name like '%" + search + "%' order by name "+sortingOrder, MainActivity.conn);
        rsGames = resultFromGames.toString().split("#");

        // Clear list and set progress info
        progressInfo.setMax(rsGames.length-1);
        progressInfo.setProgress(0);
        gameList.clear();
    }

    /**
     *  Does all the work in the background
     *  Rule! => Never change view elements of the UI- thread from here! Do it in 'onPublish'!
     */

    @Override
    protected String doInBackground(String ... params){

        // Fill highscore list
        if (!rsGames[0].equals("empty")) // Search result found?
        {
            i=0;
            for (int n = 0; n <= rsGames.length - 1; n++) {

                // This is important. If you forget it
                // the async task can nor be canceled
                if (isCancelled()) break;

                String[] r = rsGames[i].split(",");
                i++;

                // r now contains the row of the db- entry in the order of the sql- request
                // [0]key1,[1]name,[2]picture,[3]comment,[4]evaluation
                // The key1- value is needed when displaying all scores of one game.
                // The rows of the 'scores' table are linked to the key1- value of
                // the 'games' table.

                // Get primary key
                try {
                    key1 = Integer.parseInt(r[0]);
                } catch (Exception e) {
                    Log.d(tag, " Interger Parsing went wrong");
                    key1 = 0;
                }

                // Get name of game
                gameName = new SpannableString(r[1]);

                if (!search.equals("%")) {
                    int startIndex = r[1].indexOf(search);
                    int endIndex = r[1].lastIndexOf(search);
                    gameName.setSpan(new BackgroundColorSpan(Color.YELLOW), startIndex, startIndex + search.length(), 0);
                }

                // Get Highest score for this game
                resultFromScores = DB.sqlRequest("select max(score) from scores where key2=" + key1, MainActivity.conn);

                try {
                    maxScore = Integer.parseInt(resultFromScores.toString().replace("#", "").trim());
                } catch (Exception e) {
                    maxScore = 0;
                    entryType = GameListEntry.IS_ENTRY_WITHOUT_SCORE_YET;
                }

                // Get comment
                resultFromScores = DB.sqlRequest("select comment from scores where score=(select max(score) from scores where key2=" + key1 + ")", MainActivity.conn);
                comment = resultFromScores.toString().replace("#", "").trim();

                // Get evaluation
                resultFromScores = DB.sqlRequest("select evaluation from scores where score=(select max(score) from scores where key2=" + key1 + ")", MainActivity.conn);
                evaluation = resultFromScores.toString().replace("#", "").trim();

                // Get date
                resultFromScores = DB.sqlRequest("select date from scores where score=(select max(score) from scores where key2=" + key1 + ")", MainActivity.conn);
                date = resultFromScores.toString().replace("#", "").trim();

                // Get screenshoot
                //
                // This command will return the picture path for the games (key1) max score
                // 'max(score)' will get the highest score from scores....
                resultFromScores = DB.sqlRequest("select picture from scores where score=(select max(score) from scores where key2=" + key1 + ") and key2=" + key1, MainActivity.conn);
                String path = resultFromScores.toString().replace("#", " ").trim();


                // Calc sample size of image according to it's size
                // We do this in order to reduce the images memory footprint
                BitmapFactory.Options metaData = new BitmapFactory.Options();
                int sampleSize;
                metaData.inJustDecodeBounds = false;
                Bitmap bitmapOfScreenshoot=BitmapFactory.decodeFile(path,metaData);
                sampleSize=MyBitmapTools.calcSampleSize(metaData.outWidth,metaData.outHeight,200,200);
                metaData.inSampleSize = sampleSize;
                metaData.inJustDecodeBounds=false;

                    // Hint:
                    //
                    //If you want to get the original image from the media gallery, use this:
                    //Uri path = Uri.parse(resultFromScores.toString().replace("#"," ").trim()); // Make Uri from string
                    //bitmapOfScreenshoot = MediaStore.Images.Media.getBitmap(this.getContentResolver(), path);

                // Check if image file exists. If so, get Image data.
                // If not, set icon accordingly (No picutre taken yet.....)
                if ((new File(path)).exists())
                    bitmapOfScreenshoot = BitmapFactory.decodeFile(path, metaData);
                else
                    bitmapOfScreenshoot = BitmapFactory.decodeResource(c.getResources(), R.drawable.no_picture_taken_yet, metaData);

                resultFromScores = DB.sqlRequest("select score from scores where key2=" + key1, MainActivity.conn);
                int numberOfScores = resultFromScores.toString().split("#").length;
                entryType = GameListEntry.IS_ENTRY_WITH_SCORE;

                if (resultFromScores.toString().equals("empty")) {
                    entryType = GameListEntry.IS_ENTRY_WITHOUT_SCORE_YET;
                    metaData.inSampleSize = 1;
                    bitmapOfScreenshoot = BitmapFactory.decodeResource(c.getResources(), R.drawable.click_to_add, metaData);
                    numberOfScores = 0;
                }

                // Get and format date
                String formatedDate = FormatTimeStamp.german(date, FormatTimeStamp.WITH_TIME);

                // Finally! Add all data to list view
                GameListEntry e = new GameListEntry(entryType, numberOfScores, 0, gameName, maxScore, key1, 0, comment, evaluation, formatedDate, bitmapOfScreenshoot);
                publishProgress(e);
            }

            // If search term was given and no matching result......
        } else {
            GameListEntry e = new GameListEntry(GameListEntry.SEARCH_RESULT_NOT_FOUND, 0, 0, null, maxScore, key1, 0, comment, evaluation, "", null);
           publishProgress(e);
        }
        return "Done";
    }

    /**
     * Update
     *
     * This runs on the UI- Thread. No handlers and runabls needed
     * here...
     *
     * @param e     Data model for one Game list entry
     */

    @Override
    protected void onProgressUpdate (GameListEntry ... e){
        gameList.add(e[0]);
        progressInfo.setProgress(i);
    }

    /**
     * Post execcute...
     *
     * @param result
     */

    @Override
    protected void onPostExecute (String result){
        super.onPostExecute(result);
        GameListEntry e = new GameListEntry(GameListEntry.LAST_ROW, 0, 0, null, maxScore, key1, 0, comment, evaluation, "", null);
        onProgressUpdate(e);
        progressInfo.setProgress(0);
    }
}