/**
 * Data model for each row in our game list
 *
 *  @author  Berthold Fritz 2016
 */

package com.example.berthold.highscore;

import android.graphics.Bitmap;

public class GameListEntry {

    // Meta Data
    // This vars contain data to specifie the nature of the row (e.g. Folder, Headline etc...)

    public static final int IS_ENTRY_WITHOUT_SCORE_YET=1;
    public static final int IS_ENTRY_WITH_SCORE=2;
    public static final int SEARCH_RESULT_NOT_FOUND=3;
    public static final int LAST_ROW=4;
    public static final int IS_ENTRY_WITHOUT_SCREENSHOOT_YET=5;
    public int entryType;

    // Data

    public int numberOfScores;      // Number of scores for this game
    public int gameType;            // Type of the game (Arcade, console,pc)
    public String gameName;         // Name of the game

    public int highScore;           // Best score ever for this game

    public int key1;                // Private key from the DB- entry
    public int key2;                // foreign key from the DB- entry

    public String comment;          // A comment
    public String evaluation;       // Evaluation (pace cook, losser etc....
    public String date;

    public Bitmap screenShoot;      // Screenshoot, if taken....

    GameListEntry(int entryType,int numberOfScores, int gameType, String gameName, int highScore,int key1,int key2,
                  String comment,String evaluation,String date,Bitmap screenShoot) {

        this.entryType=entryType;
        this.numberOfScores=numberOfScores;

        this.gameType=gameType;
        this.gameName=gameName;
        this.highScore=highScore;

        this.key1=key1;
        this.key2=key2;

        this.comment=comment;
        this.evaluation=evaluation;
        this.date=date;
        this.screenShoot=screenShoot;
    }

}
