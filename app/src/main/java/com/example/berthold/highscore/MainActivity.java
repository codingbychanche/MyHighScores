package com.example.berthold.highscore;

/**
 * Highscore
 *
 * Save your favourite games highscore's fast, easy, permanent, offline.
 *
 * @author  Berthold Fritz 2017
 *
 * @version 1.0
 *
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
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

    // ToDo Test: Used if search widget is used...
    GameListAdapter gl;
    String st="";

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

        // Inflate toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Sorting order
        sortingOrder=sortAscending;

        // Get saved instance state
        if (savedInstanceState != null) st=savedInstanceState.getString("searchTerm");

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
                in.putExtra("sql",st);
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
        outState.putString("searchTerm",st);
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setSubtitle("∑Spiele:"   +DB.getNumberOfRows("games",conn)+" ∑Punkte:"+
                +DB.getNumberOfRows("scores",conn));

        // todo: Test, shows the files dir
        String [] dir=FileSystemUtils.getDir (getFilesDir());

        // Progress info
        final ProgressBar progress=(ProgressBar)findViewById(R.id.progress);

        // Create custom list adapter
        ArrayList<GameListEntry> gameEntry = new ArrayList<>();                 // File list
        final GameListAdapter gameList = new GameListAdapter(this, gameEntry);  // Custom list adapter
        gl=gameList;
        final ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(gameList);

        // Show game list
        updateHighscoreList(gameList,progress,sortingOrder);
    }

    /**
     * Fill highscore list
     *
     */

    public void updateHighscoreList(GameListAdapter g,ProgressBar p,String sortingOrder)
    {

        //GameListFillerv2 f = new GameListFillerv2(g, getApplicationContext(), getSearchTerm(),sortingOrder,p);
        GameListFillerv2 f = new GameListFillerv2(g, getApplicationContext(), st,sortingOrder,p);
        //todo Don't like this, but takes care that only one thread notifies the game list....
        while (f.threadsRunning == 1);

        filler = new Thread(f);
        filler.start();
    }

    /**
     * Menu
     *
     * @param menu
     * @return
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Get Search field
        SearchView searchView=(SearchView) menu.findItem(R.id.action_search).getActionView();

        SearchView.OnQueryTextListener querryTextListener=new SearchView.OnQueryTextListener(){
            public boolean onQueryTextChange(String newText){
                st=newText;
                final ProgressBar progress=(ProgressBar) findViewById(R.id.progress);
                updateHighscoreList(gl,progress,sortingOrder);
                return true;
            }

            //Get search field input......
            public boolean onQueryTextSubmit (String querry){
                System.out. println("---------"+querry);
                final ProgressBar progress=(ProgressBar)findViewById(R.id.progress);
                st=querry;
                updateHighscoreList(gl,progress,sortingOrder);
                return true;
            }
        };

        searchView.setOnQueryTextListener(querryTextListener);
        // End
        return super.onCreateOptionsMenu(menu);
    }

    /*
     * Menu item clicked.
     *
     * Callback if item was clicked
     */

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {

        switch (item.getItemId()) {

            // Show info?
                case R.id.action_settings:
                    Intent in = new Intent(this, About.class);
                    this.startActivity(in);

            case R.id.action_sort:
                //todo should also saved in instance state
                if(sortingOrder.equals(sortAscending)) {
                    sortingOrder=sortDescending;
                } else {
                    sortingOrder=sortAscending;
                }
                final ProgressBar progress=(ProgressBar) findViewById(R.id.progress);
                updateHighscoreList(gl,progress,sortingOrder);
        }
        return super.onOptionsItemSelected(item);
    }

}



