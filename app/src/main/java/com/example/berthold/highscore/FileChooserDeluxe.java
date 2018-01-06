/**
 * <b>File Picker Deluxe</b>
 *
 * Lets the user browse the devices file system and returns the selected files path.
 *
 * @author  Berthold Fritz
 * @date    9.9.2017
 */


/*
 * ToDo: App crashes when no file is selected and user leaves file chooser
 */

package com.example.berthold.highscore;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.File;
import java.util.ArrayList;

public class FileChooserDeluxe extends AppCompatActivity {

    Bundle savedState;

    // GUI
    private ListView myListView;
    private ProgressBar progress;
    private ActionBar ab;

    // Filesystem
    private File currentPath;                                       // The current path
    private File [] fileObjects;                                    // Contains the current folder's files as file- objects in the same order as 'directory'

    // List view
    private FileListAdapter myListAdapter=null;

    // Async task
    private FileListFillerV5 task;

    /**
     * Activity
     *
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_chooser);
        savedState=savedInstanceState;

        ab=getSupportActionBar();
        ab.setTitle("Dateiauswahl");

        // Create list view
        myListView=(ListView)findViewById(R.id.mylist);

        ArrayList<FileListOneEntry> dir=new ArrayList<>();       // Array containing the current path's dir
        myListAdapter=new FileListAdapter(this,dir);
        myListView.setAdapter(myListAdapter);

        // Get current path, directory as string array and as file object- list
        currentPath=new File ("/");                             // Set root as start
        refreshFiles(currentPath,myListAdapter);

        // List view
        myListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    // If a file was selected, get it's full path name and return to
                    // the calling activity
                    if (fileObjects[position].isFile() && fileObjects[position].canRead()){
                        currentPath=fileObjects[position].getAbsoluteFile();
                        finishIt();
                    }

                    // If list item is a directory, show it's files
                    if (fileObjects[position].isDirectory()) {

                        // File should be readable and it should exist (e.g. folder 'sdcard')
                        // it might be shown but it could not be mounted yet. In that case
                        // it does not exist :-)
                        if(fileObjects[position].canRead() && fileObjects[position].exists()) {

                            // Get path and display it
                            currentPath = fileObjects[position].getAbsoluteFile();
                            ActionBar ab=getSupportActionBar();
                            ab.setSubtitle(currentPath.toString());

                            // Refresh file object list and file ListArray
                            // Update listView => show changes
                            refreshFiles(currentPath,myListAdapter);
                            myListAdapter.notifyDataSetChanged();

                        }
                    }
                }
        });

        //Set list on onnScrollLstener
        //Here you can check which the first visible item is and how many
        //items are visible
        myListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            int first,last;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // ToDo: How can we update just the items, that are visible....?
                System.out.println("----updating from:"+first+"   to:"+last);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                System.out.println("OnScroll:"+firstVisibleItem+"    #ofItems"+visibleItemCount);
                first=firstVisibleItem;
                last=firstVisibleItem+visibleItemCount;
            }

        });
    }

    @Override
    protected void onSaveInstanceState(Bundle s){
        Log.v("Main","Save state-------"+currentPath+"  "+task);
        s.putString("path",currentPath.toString());
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (task!=null) task.cancel(true);
        Log.v("Main","ONPAUSEEEEEEE--------"+currentPath+"  "+task);
    }

    @Override
    protected void onResume (){
        super.onResume();
        if(savedState!=null) currentPath=new File(savedState.getString("path"));
        // Display current path
        ab.setSubtitle(currentPath.toString());
        Log.v("Main","ONRESUME----------"+currentPath+"  "+task);
        refreshFiles(currentPath,myListAdapter);
    }

    /**
     * Options menu
     *
     * @param   menu
     * @since   7/2017
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // If up button was pressed, move to parent dir or leave activity if
        // we are already at "/"
        if (id == R.id.goup) {
            if (currentPath.toString().equals("/")){
                // Cancel async task, if picture thumbnails for the
                // current dir are created....
                task.cancel(true);
                finishIt();
            } else {
                File parent = currentPath.getParentFile();
                currentPath = parent;
                ActionBar ab=getSupportActionBar();
                ab.setSubtitle(currentPath.toString());
                refreshFiles(parent,myListAdapter);
            }
        }
        return super.onOptionsItemSelected(item);
    }

        /**
         * Refresh array containing file- objects
         *
         */

        private void refreshFiles (File path,FileListAdapter a)
        {
            if (task!=null) task.cancel(true);
            // Get all file objects of the current path
            File files=new File(path.getPath());
            File []fo=files.listFiles();

            // Sort by kind, and do not show files that are not readable
            fileObjects=SortFiles.byKind(fo);

            // Clear dir array
            a.clear();
            progress=(ProgressBar)findViewById(R.id.pbar);
            task=new FileListFillerV5(a, fileObjects, 0,fileObjects.length-1,getApplicationContext(),progress);
            task.execute();

            return;
        }

        /**
         * Leave activity
         *
         */

        public void finishIt()
        {
            // Enter the caller class's name here (second parameter)
            Intent i=new Intent(FileChooserDeluxe.this,MainActivity.class);
            i.putExtra("path",currentPath.toString());
            setResult(RESULT_OK,i);
            super.finish();
        }
    }
