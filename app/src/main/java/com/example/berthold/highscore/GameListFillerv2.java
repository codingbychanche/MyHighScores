package com.example.berthold.highscore;

/**
 * Game List filler V2
 *
 * This fills the game list running on it's own thread.
 *
 * This file replaces "gameListFiller" which was hard to control in means of starting threat's.
 * (Check the To Do's in the file mentioned)
 *
 * Created by Berthold on 2/9/17.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.TextView;

import java.io.File;


public class GameListFillerv2 implements Runnable {

    //public static   Bitmap bitmapOfScreenshoot;

    public static int key1;
    public static int maxScore;
    public static String comment;
    public static String evaluation;
    public static String date;
    public static StringBuffer resultFromScores;
    public static int entryType;
    public static Handler h=new Handler();
    public static int threadsRunning;
    public int i;

    /**
     * Constructor
     */

    public String search;
    public Context c;
    public GameListAdapter gameList;
    public TextView progressInfo;

    GameListFillerv2(GameListAdapter gameList, Context c,String search,TextView p) {

        this.search=search;
        this.c=c;
        this.gameList=gameList;
        this.progressInfo=p;

    }

    /**
     * Runable
     *
     * This fills the high score list. Only one thread allowed!
     *
     */

    @Override
    public void run() {

        // Thread control
        // Check if ths thread is aready running. If so, don't do it again!

        if (threadsRunning ==0) {   // Check if already running

            threadsRunning++;
            System.out.println("--------------------------- länge"+gameList.getCount());

            // Check if a search item was passed:
            // If so, clear the list and fill it with the search result or,
            // if no matching entries where found, add a row saying so...
            //
            // If no search item was passed, do not clear the list and add only
            // new items. This should speed things up, but! For the time being,
            // 'gameList' is empty if a new activity was started thus this makes no
            // difference for the time being:
            // todo: save/ load 'gameList' in instance state

            if (search.equals(" ")){
                search = "%";
            } else{

                // Post "clear the list" to UI- Thread
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        gameList.clear();

                    }
                });
            }

            final StringBuffer resultFromGames = DB.sqlRequest("select key1,name,picture from games where name like '%" + search + "%' order by name", MainActivity.conn);
            final String[] rsGames = resultFromGames.toString().split("#");

            if (!rsGames[0].equals("empty")) // Search result found?
            {

                // Fill highscore list

                for (int n = 0; n <= rsGames.length - 1; n++) {
                    h.post(new Runnable() {

                        @Override
                        public void run() {
                            String[] r = rsGames[i].split(",");
                            i++;
                            progressInfo.setText("Lade:"+i+" von:"+rsGames.length);

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

                            // Check if this entry already exists in 'gameList'
                            // if so, don't add it again....

                            if (!isEntry(gameList,key1)) { // Entry exists?

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

                                // This is the time consuming part!
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
                            } // Entry exists?
                        }
                    });// Post

                    // Wait a few millisec's
                    // This gives the UI- thread enough room to sort things out
                    // and to react thus preventing the main UI- thread from freezing....

                    try {
                        Thread.sleep(55);
                    } catch (InterruptedException in) {}

                } // for

                // If search was in vain, tell the user so....

            } else {
                h.post(new Runnable (){
                    @Override
                    public void run() {
                        gameList.clear();
                        GameListEntry e = new GameListEntry(GameListEntry.SEARCH_RESULT_NOT_FOUND, 0, 0, "", maxScore, key1, 0, comment, evaluation, "", null);
                        gameList.add(e);
                    }
                });
            }

            // List filled, Thread done!
            // Add last row to list to give some room at it's end in order to improve
            // visibility of it's rows

            threadsRunning--;   // One thread less

            h.post(new Runnable (){
                @Override
                public void run() {
                    GameListEntry e = new GameListEntry(GameListEntry.LAST_ROW, 0, 0, "", maxScore, key1, 0, comment, evaluation, "", null);
                    gameList.add(e);
                    progressInfo.setText("");

                    // todo: Cool, this code changes the items screenshoot and updates the list...
                    //GameListEntry ee=gameList.getItem(1);
                    //ee.screenShoot=null;

                }
            });

        }
    }

    /**
     * Check if an item in 'gameList' already exists
     *
     * @param   gameList    The list of items to check
     * @param   key1        Primary key of DB- Entry which acts as the unique identifier
     * @return              'true' if the entry exists
     *
     * todo: not working yet
     */

    private boolean isEntry(GameListAdapter gameList,int key1)
    {
        for (int i=0;i<=gameList.getCount()-1;i++){
            GameListEntry k=gameList.getItem(i);
            if (k.key1==key1) {
                return true;
            }
        }
        return false;
    }
}
