/**
 *  Data model for each row in our file list
 *
 *  @author  Berthold Fritz 2017
 */

package com.example.berthold.highscore;

import android.graphics.Bitmap;

public class FileListOneEntry {

    // Meta Data
    // This vars contain data to determine the nature of the row (e.g. Folder, Headline etc...)

    public int entryType;
    public static final int IS_ACTIVE=1;


    // Data

    public String fileName;
    public Bitmap fileSymbol;
    public boolean isReadable;
    public String lastModified;

    /**
     * Constructor, assign properties
     */

    FileListOneEntry(int entryType, Bitmap fileSymbol,String fileName,boolean isReadable,String lastModified) {

        this.entryType=entryType;
        this.fileSymbol=fileSymbol;
        this.fileName=fileName;
        this.isReadable=isReadable;
        this.lastModified=lastModified;
    }

}
