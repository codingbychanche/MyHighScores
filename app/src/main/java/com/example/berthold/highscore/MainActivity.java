package com.example.berthold.highscore;

/*
 * MainActivity.java
 *
 * Created by Berthold Fritz
 *
 * This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * Last modified 1/26/18 11:27 PM
 */

/**
 * Highscore
 *
 * Save your favourite games highscore's fast, easy, permanent, offline.
 *
 */

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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

    // File system
    private static File workingDir;
    private static String appDir="MyHigscores";       // App's working dir..

    private static File workingDirPictures;            // All screenshoots go here
    private static String picDir="Screenshoots";

    // Database
    public static Connection conn;
    public static String path;

    // Sorting
    private static String sortingOrder;
    private static final String sortAscending="ASC";
    private static final String sortDescending="DESC";

    // Async task
    private GameListFillerV3 fillList;

    // ToDo Test: Used if search widget is used...
    GameListAdapter_OLD gl;
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

        // File system
        //
        // This creates a folder for this app and subFolders for asociated data
        // (Pictures, import files, export files etc.....)
        //
        // This seems to be the best practice. It creates a public folder.
        // This folder will not be deleted when the app is de- installed
        workingDir= Environment.getExternalStoragePublicDirectory(appDir);
        Log.v("---Working dir",workingDir.getAbsolutePath());
        workingDir.mkdirs(); // Create dir, if it does not already exist

        workingDirPictures=Environment.getExternalStoragePublicDirectory(appDir+"/"+picDir);
        Log.v("---Screenshoots",workingDirPictures.getAbsolutePath());
        workingDirPictures.mkdirs(); // Create dir, if it does not already exist

        // Create DB
        String dbName = "/HighScoresDB";

        //File f = getFilesDir();
        //path = (f.getAbsolutePath() + dbName);
        path=(workingDir.getAbsolutePath()+dbName);

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
        final GameListAdapter_OLD gameList = new GameListAdapter_OLD(this, gameEntry);  // Custom list adapter
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

    public void updateHighscoreList(GameListAdapter_OLD g,ProgressBar p,String sortingOrder)
    {

        //This is the old solution to fill the game- list
        //GameListFillerv2 f = new GameListFillerv2(g, getApplicationContext(), st,sortingOrder,p);
        //while (f.threadsRunning == 1);
        //filler = new Thread(f);
        //filler.start();

        // This uses an async task
        if (fillList!=null) fillList.cancel(true);
        fillList=new GameListFillerV3(g, getApplicationContext(),st,sortingOrder,p);
        fillList.execute();
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

    /**
     * Get Working dir's path
     *
     */

    public static File getWorkingDir() {return workingDir;}


    /**
     * Get Screenshoot dir's path
     *
     */

    public static File getWorkingDirPictures(){return workingDirPictures;}
}



