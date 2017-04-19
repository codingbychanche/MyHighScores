/**
 * Deletes the selected game and all of it's scores
 *
 * @author Berthold Fritz 2016
 */

package com.example.berthold.highscore;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import android.util.Log;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.*;

public class GameDelete extends AppCompatActivity {

    private ListView myListView;
    private ArrayAdapter myListAdapter;
    private boolean yesNoResult;     // Yes no dialog result
    private String name;
    private String sql;              // Sql query
    private String searchTerm;       // Seach term if passed

    /**
     * On Create
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_delete);

        // Get and display search term, if given
        // Set sql query accordingly
        Bundle extra = getIntent().getExtras();
        TextView progressInfo = (TextView) findViewById(R.id.progressInfo);

        try {
            searchTerm = extra.getString("sql");

            if (searchTerm.isEmpty()) {
                sql = "select name from games order by name";
                progressInfo.setText(R.string.showingAll);
            } else {
                progressInfo.setText(R.string.showingSelection);
                progressInfo.append("'" + searchTerm + "'");
                sql = "select name from games where name like '%" + searchTerm + "%'";
            }
        } catch (Exception e) {
            sql = "select name from games order by name";
            progressInfo.setText(R.string.showingAll);
        }

        // Confirm Dialog
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) dialogReturn(true);
                if (which == DialogInterface.BUTTON_NEGATIVE) dialogReturn(false);
            }
        };

        // Build Dialog
        final AlertDialog.Builder chooseYesNo = new AlertDialog.Builder(this);
        chooseYesNo.setMessage(R.string.yesNo).setPositiveButton(R.string.yes, dialogClickListener).
                setNegativeButton(R.string.no, dialogClickListener);

        // Get data
        StringBuffer result = DB.sqlRequest(sql, MainActivity.conn);
        String[] rs = result.toString().split("#");

        // Show List, if it has entry's
        if (!rs[0].equals("empty")) {
            myListView = (ListView) findViewById(R.id.death_game_list);
            myListAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, rs);
            myListView.setAdapter(myListAdapter);

            // Query user, when list item was clicked, delete this item....
            myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    // Show dialog and proceed deleting if dialog "yes" was clicked
                    chooseYesNo.show();

                    // When clicked get field 'titel'
                    name = ((TextView) view).getText().toString();
                }
            });

            // Remove Filter
            FloatingActionButton removeFilter = (FloatingActionButton) findViewById(R.id.removeFilter);
            removeFilter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    searchTerm = "";
                    Intent i = new Intent(GameDelete.this, GameDelete.class);
                    startActivity(i);
                    finish();
                }
            });
        }  else {
            // No entrys, tell the user...
            progressInfo.setText(R.string.nothingToShow);
        }
    }

    /**
     * Callback for yesNoDialog.
     *
     * Deletes the selected DB entry if positive button of yesNoDialog was
     * clicked.
     *
     * @global  name    Game name from clicked List item
     * @param   confirmed true if positive button was clicked.
     */

    public void dialogReturn(boolean confirmed){

        yesNoResult=confirmed;

        if (confirmed) {
            int key1 = DB.getKey1("games", "name", name, MainActivity.conn);

            try {
                Statement s = MainActivity.conn.createStatement();
                // delete game

                    // 1. Delete all picture files
                    // todo: For the time being, only one screenshoot, for the highscore, is saved
                    // => This means, that only the first search result 'p[0]' contains the path
                    // to this picture. 'p[1]' and so on, contains nothing....
                    StringBuffer picturePathList=DB.sqlRequest("select picture from scores where key2="+key1,MainActivity.conn);
                    String [] pl=picturePathList.toString().split("#");
                    File f=new File(pl[0]);
                    f.delete(); // Delete file, if exists. If no such file=> no problem....

                    // Delete game and...
                    s.executeUpdate("delete from games where key1=" + key1);
                    // ...all of it's scores...
                    s.executeUpdate("delete from scores where key2=" + key1);

                // Restart activity (and show updated list of games to allow another one
                // to be deleted or to go back to main
                Intent i=new Intent(GameDelete.this,GameDelete.class);
                i.putExtra("sql",searchTerm);
                startActivity(i);
                finish();

            } catch (SQLException e) {
                // TODO: 11/8/16  Give feedback
            }
        }
    }
}
