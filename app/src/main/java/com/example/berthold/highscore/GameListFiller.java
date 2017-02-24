package com.example.berthold.highscore;

/**
 * Game List filler
 *
 * This fills the game list using a seperate thread
 *
 * Created by Berthold on 1/7/17.
 *
 * TIS IS THE OLD VERSION OF 'GAMELISTFILLER' IF 'V2' FAILS, RESTORE THIS FILE ;-)
 *
 * BELOW ARE THE ISSUES WITH THIS VERSION AND WHICH WERE SOLVED IN 'V2'
 * --------------------------------------------------------------------
 * todo: Take care to check if the thread is already running and prevent it from restarting if so...
 * todo: The above mentioned todo has been done! See "gameListFillerv2" THIS FILE IS NOW OBSOLETE!!!!!
 */


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.content.Context;
import android.provider.Settings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.ParseException;
import java.io.File;


public class GameListFiller {

    private static Handler h = new Handler();
    private static Thread t=null;
    static int i=0;
    static int numOfThreads;
    //private static   Bitmap bitmapOfScreenshoot;

    private static int key1;
    private static int maxScore;
    private static String comment;
    private static String evaluation;
    private static String date;
    private static StringBuffer resultFromScores;
    private static int entryType;

    public static void doUpdate(final GameListAdapter gameList, final Context c,String search) {
        System.out.println("---------------------ss--" + search);

        // Create thread
        if (t==null) {
            gameList.clear();

            if (search.equals(" ")) search = "%";
            final StringBuffer resultFromGames = DB.sqlRequest("select key1,name,picture from games where name like '%" + search + "%' order by name", MainActivity.conn);
            final String[] rsGames = resultFromGames.toString().split("#");


            if (!rsGames[0].equals("empty")) { // Search result found

                t = new Thread(new Runnable() {
                    int i=0;
                    @Override
                    public void run() {
                        for (int n = 0; n <= rsGames.length - 1; n++) {
                            
                            System.out.println("------Total:"+(rsGames.length-1)+" -----------List:"+i);
                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    String[] r = rsGames[i].split(",");
                                    i++;

                                    // r contains the row of the db- entry in the order of the sql- request
                                    // [0]key1,[1]name,[2]picture,[3]comment,[4]evaluation
                                    // The key1- value is needed when displaying all scores of one game.
                                    // The rows of the 'scores' table are linked to the key1- value of
                                    // the 'games' table.

                                    // Get primary key

                                    try {
                                        key1 = Integer.parseInt(r[0]);
                                    } catch (Exception e) {
                                        System.out.println("-------Interger Parsing went wrong");
                                        key1 = 0;
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
                                    resultFromScores = DB.sqlRequest("select picture from scores where score=(select max(score) from scores where key2=" + key1 + ")", MainActivity.conn);

                                    String path = resultFromScores.toString().replace("#", " ").trim();

                                    BitmapFactory.Options metaData = new BitmapFactory.Options();
                                    metaData.inJustDecodeBounds = false;

                                    Bitmap bitmapOfScreenshoot;

                                    //If we want to get the original image from the media gallery, use this:
                                    //Uri path = Uri.parse(resultFromScores.toString().replace("#"," ").trim()); // Make Uri from string
                                    //bitmapOfScreenshoot = MediaStore.Images.Media.getBitmap(this.getContentResolver(), path);

                                    // Check if file exists
                                    if ((new File(path)).exists()) {
                                        metaData.inSampleSize = 10;       // Scale image down in size and reduce it's memory footprint
                                        bitmapOfScreenshoot = BitmapFactory.decodeFile(path, metaData);
                                    } else {
                                        bitmapOfScreenshoot = BitmapFactory.decodeResource(c.getResources(), R.drawable.no_picture_taken_yet, metaData);
                                    }
                                    resultFromScores = DB.sqlRequest("select score from scores where key2=" + key1, MainActivity.conn);
                                    int numberOfScores = resultFromScores.toString().split("#").length;
                                    entryType = GameListEntry.IS_ENTRY_WITH_SCORE;

                                    if (resultFromScores.toString().equals("empty")) {
                                        entryType = GameListEntry.IS_ENTRY_WITHOUT_SCORE_YET;
                                        numberOfScores = 0;
                                    }

                                    // Get and format date
                                    String formatedDate = FormatTimeStamp.german(date, FormatTimeStamp.WITH_TIME);

                                    // Add all data to list view
                                    GameListEntry e = new GameListEntry(entryType, numberOfScores, 0, r[1], maxScore, key1, 0, comment, evaluation, formatedDate, bitmapOfScreenshoot);
                                    gameList.add(e);

                                } // run()
                            }); // Post/ Runable

                            // Wait a few millisec's
                            // This gives the UI- thread enough room to sort things out
                            // and to react thus preventing the main UI- thread from freezing....

                            try {
                                Thread.sleep(250);
                            }catch (InterruptedException i){}

                        } // For

                        System.out.println("----------------------------DONE");

                    } // run()

                }); // Tread

                System.out.println("----------------------###of Threads"+numOfThreads);
                if (numOfThreads==0) t.start();
                t=null;

            } else {
                GameListEntry e = new GameListEntry(GameListEntry.SEARCH_RESULT_NOT_FOUND, 0, 0, "", maxScore, key1, 0, comment, evaluation, "", null);
                gameList.add(e);
            }
        }

    }
}
