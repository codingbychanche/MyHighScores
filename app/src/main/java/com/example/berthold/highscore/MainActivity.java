package com.example.berthold.highscore;

/**
 * Highscore
 *
 * Save your favourite games highscore's fast, easy, permanent, offline.
 *
 * @author  Berthold Fritz 2017
 *
 */

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

    // Logcat
    private static String tag;

    // Database
    public static Connection conn;
    public static String path;

    // Sorting
    private static String sortingOrder;
    private static final String sortAscending="ASC";
    private static final String sortDescending="DESC";

    // Threads
    static Thread filler;

    /**
     * Create activity
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout
        setContentView(R.layout.activity_main);

        // Sorting order
        sortingOrder=sortAscending;

        // Get saved instance state
        if (savedInstanceState != null) {
            final EditText searchText=(EditText)findViewById(R.id.search);
            searchText.setText(savedInstanceState.getString("searchTerm"));
            Log.d(tag,"------Instance State loaded "+savedInstanceState.getString("searchTerm"));
        }

        // Debug
        tag="Debug: Main";

        // Create DB
        String dbName = "/highscores";

        File f = getFilesDir();
        path = (f.getAbsolutePath() + dbName);

        try {
            CreateDB.make(path);
            Toast.makeText(getApplicationContext(),R.string.dbloaded, Toast.LENGTH_LONG).show();
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
            Log.d(tag, "Reading:" + DB_CONNECTION + "\n");
            conn = DB.read(DB_DRIVER, DB_CONNECTION, DB_USER, DB_PASSWORD);

        } catch (Exception e) {
            Log.d(tag, "Error opening DB\n");
            Log.d(tag, e.toString());
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
                in.putExtra("sql",getSearchTerm());
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
     * In this case 'onPause' or 'onStop' is called as well.
     *
     */

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("searchTerm",getSearchTerm());
        Log.d(tag,"Instance State saved "+getSearchTerm());
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
        Log.d (tag,"------------------Stoppppp");
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
        Log.d (tag,"------------------Pause");
    }

    /**
     * Called when activity is resumed
     * e.g. when back button of device was pressed
     */

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d (tag,"-----Main: Restart");

        // Create custom list adapter
        ArrayList<GameListEntry> gameEntry = new ArrayList<>();                 // File list
        final GameListAdapter gameList = new GameListAdapter(this, gameEntry);  // Custom list adapter
        final ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(gameList);

        // todo: Test, shows the files dir
        String [] dir=FileSystemUtils.getDir (getFilesDir());

        // Progress info
        final TextView progressInfo=(TextView)findViewById(R.id.progressInfo);

        // Show game list
        updateHighscoreList(gameList,progressInfo,sortingOrder);

        // Sort button
        final ImageButton sort=(ImageButton)findViewById(R.id.sort_button);
        sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo should also saved in instance state
                    if(sortingOrder.equals(sortAscending)) {
                        sortingOrder=sortDescending;
                        sort.setImageResource(R.drawable.sort_descending);
                    } else {
                        sortingOrder=sortAscending;
                        sort.setImageResource(R.drawable.sort_ascending);
                    }
                    updateHighscoreList(gameList,progressInfo,sortingOrder);
                    }
                });

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

            }

            @Override
            public void afterTextChanged(Editable s) {

                updateHighscoreList(gameList,progressInfo,sortingOrder);
            }
        });

        // Reset search button
        ImageButton resetSearch=(ImageButton)findViewById(R.id.reset_search_button);

        resetSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText searchText=(EditText)findViewById(R.id.search);
                searchText.getText().clear();
                updateHighscoreList(gameList,progressInfo,sortingOrder);
            }
        });
    }

    /**
     * Fill highscore list
     *
     */

    public void updateHighscoreList(GameListAdapter g,TextView p,String sortingOrder)
    {

        GameListFillerv2 f = new GameListFillerv2(g, getApplicationContext(), getSearchTerm(),sortingOrder,p);

        //todo Don't like this, but takes care that only one thread notifies the game list....
        while (f.threadsRunning == 1);

        filler = new Thread(f);
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
        Log.d (tag,"----------+++++++++++"+searchText.getText().toString());
        return searchText.getText().toString();
    }
}



