/**
 * Adapter class for game- list
 *
 * This code creates each row of our game list, each time a new entry
 * is made.
 *
 *  @author  Berthold Fritz 2016
 */


package com.example.berthold.highscore;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import java.io.IOException;
import java.util.ArrayList;

public class GameListAdapter extends ArrayAdapter <GameListEntry>{

    StringBuffer result; // Result of Db request

    // Constructor

    public GameListAdapter(Context context, ArrayList <GameListEntry> GameListEntry) {
        super (context,0,GameListEntry);
        System.out.println("-------Adapter: Item build");
    }

    // Custom view

    @Override
    public View getView (int position, View convertView, ViewGroup parent)  {

        final GameListEntry item=getItem(position);

            // Check if the view already exists, if not, inflate it
            // Game has scores? Yes, inflate the complete view, if not, just the title

            if (item.entryType == GameListEntry.SEARCH_RESULT_NOT_FOUND)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.game_list_entry_not_found, parent, false);

            if (item.entryType == GameListEntry.IS_ENTRY_WITH_SCORE)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.game_list_entry, parent, false);

            if (item.entryType==GameListEntry.IS_ENTRY_WITHOUT_SCORE_YET)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.game_list_entry_no_score, parent, false);

            if (item.entryType== GameListEntry.LAST_ROW)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.game_list_entry_last_row, parent, false);

            // If entry has scores:
            if (item.entryType == GameListEntry.IS_ENTRY_WITH_SCORE) {
                // Put data into view
                final TextView tvName = (TextView) convertView.findViewById(R.id.tvGameName);
                final TextView tvHighScore = (TextView) convertView.findViewById(R.id.tvHighScore);

                final TextView tvNumberOfScores = (TextView) convertView.findViewById(R.id.scoreCount);

                final TextView tvComment = (TextView) convertView.findViewById(R.id.tvComment);
                final TextView tvEvaluation = (TextView) convertView.findViewById(R.id.tvEvaluation);

                final TextView tvDate = (TextView) convertView.findViewById(R.id.tvDate);

                ImageView ivScreenShoot = (ImageView) convertView.findViewById(R.id.gameScreenShoot);

                tvName.setText(item.gameName);                              // Put name of the game into listView

                tvHighScore.setText(String.valueOf(item.highScore));        // Put Highest score for this game into list view

                tvComment.setText(item.comment);
                tvEvaluation.setText(item.evaluation);
                tvDate.setText(item.date);
                tvNumberOfScores.setText(String.valueOf(item.numberOfScores));
                ivScreenShoot.setImageBitmap(item.screenShoot);

                // Set Screenshoot, if taken for this game and for highscore reached
                result = DB.sqlRequest("select picture,max(score) from scores where key2=" + item.key1, MainActivity.conn);
                String picturePath = result.toString().replace("#", "").trim();
            }

            if (item.entryType == GameListEntry.IS_ENTRY_WITHOUT_SCORE_YET) {

                // Entry has no scores:
                final TextView tvName = (TextView) convertView.findViewById(R.id.tvNoScoreGameName);
                tvName.setText(item.gameName);

            }

        // Event handler
        // This method is called, when the list item was clicked

        if (item.entryType!=item.LAST_ROW) { // Info items may not be set on the click listener!

            convertView.setTag(position);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Get item pos
                    int pos = (Integer) v.getTag();
                    String nameOfSelectedGame = item.gameName;
                    int key1 = item.key1;

                    // Get object at pos i from GameListEntry
                    GameListEntry i = getItem(pos);

                    // Show all scores for the selected game

                    Intent in = new Intent(v.getContext(), ScoreListPerGame.class);
                    in.putExtra("key1", key1);
                    in.putExtra("name", nameOfSelectedGame);
                    System.out.println("------------key1 passed" + key1);
                    v.getContext().startActivity(in);
                }
            });
        }
        return convertView;
    }
}
