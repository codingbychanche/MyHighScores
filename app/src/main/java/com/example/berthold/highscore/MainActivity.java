/**
 * Highscore
 *
 * Save your favourite games highscore's fast, easy, permanent, offline.
 *
 * @author  Berthold Fritz 2017
 *
 */

package com.example.berthold.highscore;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.sql.Connection;
import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Database

    public static Connection conn;
    public static String path;
    String searchTerm;          // Contents of search field, used to update list depending on it.
    static Thread filler;

    /**
     * Create activity
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get saved instance state

        if (savedInstanceState != null) {
           searchTerm=savedInstanceState.getString("searchTerm");
            System.out.println("------Instance State loaded");
        } else {
            searchTerm="";
        }

        // Layout
        setContentView(R.layout.activity_main);

        // Create DB

        String dbName = "/highscores";

        File f = getFilesDir();
        path = (f.getAbsolutePath() + dbName);

        System.out.println("+++++++++Is there" + (new File(path).exists() && new File(path).isFile()));

        try {
            CreateDB.make(path);
            Toast.makeText(getApplicationContext(), "MainActivity: Creating DB", Toast.LENGTH_LONG).show();
            Log.i("-----", "DB Created on:\n");
            Log.i("=", path);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "MainActivity: Using existing DB", Toast.LENGTH_LONG).show();
        }

        // Read DB

        String DB_DRIVER = "org.h2.Driver";
        String DB_CONNECTION = "jdbc:h2:" + path;
        String DB_USER = "";
        String DB_PASSWORD = "";

        try {
            Log.i("---", "Reading:" + DB_CONNECTION + "\n");
            conn = DB.read(DB_DRIVER, DB_CONNECTION, DB_USER, DB_PASSWORD);

        } catch (Exception e) {
            Log.i("---", "Error opening DB\n");
            Log.i("---", e.toString());
        }

        // Floating button, add a new game

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(view.getContext(), NewGame.class);
                view.getContext().startActivity(in);

            }
        });

        // Floating button, delete (game)

        FloatingActionButton delete = (FloatingActionButton) findViewById(R.id.delete_game);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(view.getContext(), GameDelete.class);
                view.getContext().startActivity(in);

            }
        });
    }

    /**
     * Saves instance state
     *
     * Is called when the user leaves this activity or the screen orientation
     * is changed.
     *
     * In this case 'onPause' or 'onStopp' is called as well.
     *
     */

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("searchTerm",getSearchTerm());
        System.out.println("-----------------Instance State saved "+getSearchTerm());
    }

    /**
     * Stop
     *
     * This method seems to be called, every time 'onSaveInstanceState()' is called.
     *
     */
    
    @Override
    protected void onStop(){
        super.onStop();
        System.out.println("------------------Stoppppp");
    }

    /**
     * Pause
     *
     * Is called when the user presses the left button (Stack) in order to change to
     * another paused App.
     */

    @Override
    protected void onPause(){
        super.onPause();
        System.out.println("------------------Pause");
    }

    /**
     * Called when activity is resumed
     * e.g. when back button of device was pressed
     *
     * toDo: Find a way to restore saved instance state from within this callback method
     */

    @Override
    public void onResume()
    {
        super.onResume();
        System.out.println("-----Main: Restart");

        // Create custom list adapter

        ArrayList<GameListEntry> gameEntry = new ArrayList<>();                 // File list
        final GameListAdapter gameList = new GameListAdapter(this, gameEntry);  // Custom list adapter
        final ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(gameList);

        // Progress info
        final TextView progressInfo=(TextView)findViewById(R.id.progressInfo);


        // Show game list
        updateHighscoreList(gameList,progressInfo);

        // Info button
        final ImageButton info=(ImageButton)findViewById(R.id.info_button);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(MainActivity.this, About.class);
                startActivity(in);
            }
        });

        // Search

        final EditText searchText=(EditText)findViewById(R.id.search);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchTerm=getSearchTerm();
                updateHighscoreList(gameList,progressInfo);

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Init search field
        searchTerm=getSearchTerm();
        updateHighscoreList(gameList,progressInfo);

        // Reset search button
        ImageButton resetSearch=(ImageButton)findViewById(R.id.reset_search_button);

        resetSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText searchText=(EditText)findViewById(R.id.search);
                searchText.setText("");
                searchTerm=getSearchTerm();
                updateHighscoreList(gameList,progressInfo);
            }
        });
    }

    /**
     * Fill highscore list
     *
     */

    public void updateHighscoreList(GameListAdapter g,TextView p)
    {
        GameListFillerv2 f = new GameListFillerv2(g, getApplicationContext(), getSearchTerm(),p);
        filler=new Thread(f);
        filler.start();
    }

    /**
     * Get's search term from search- text field
     *
     * @return
     */

    public String getSearchTerm()
    {
        final EditText searchText=(EditText)findViewById(R.id.search);
        return searchText.getText().toString();
    }

    /**
     * Set contents of search- text field
     *
     * @param search    Search term
     *
     */

    public void setSearchTerm(String search)
    {
        final EditText searchText=(EditText)findViewById(R.id.search);
        searchText.setText(search);
    }
}



