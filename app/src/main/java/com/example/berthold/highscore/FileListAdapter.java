/**
 * Adapter class for File- list
 *
 * This code creates each row of our list, each time a new entry
 * is added.
 *
 * @author  Berthold Fritz 2016
 */


package com.example.berthold.highscore;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class FileListAdapter extends ArrayAdapter <FileListOneEntry>{

    DecimalFormat df=new DecimalFormat("#,###,###");

    /**
     * Constructor
     */
    
    public FileListAdapter(Context context, ArrayList <FileListOneEntry> FileListOneEntry) {
        super (context,0, FileListOneEntry);
    }

    /*
     * Inflate layout for one row of the list
     */

    @Override
    public  View getView (int position, View convertView, ViewGroup parent) {

        final FileListOneEntry item = getItem(position);
            // Check status of entry and inflate matching layout
            // Active item. Inflate layout and fill row with data

            if (item.entryType == FileListOneEntry.IS_ACTIVE) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.file_list_active_entry, parent, false);
                final TextView tvName = (TextView) convertView.findViewById(R.id.fileNname);
                tvName.setText(item.fileName);

                final ImageView fs = (ImageView) convertView.findViewById(R.id.fileSymbol);
                fs.setImageBitmap(item.fileSymbol);

                // If folder/ file is !readable, then add lock- sym to file sym...
                if (!item.isReadable) {
                    final ImageView lock = (ImageView) convertView.findViewById(R.id.locksymbol);
                    Bitmap lockSym = BitmapFactory.decodeResource(convertView.getResources(), android.R.drawable.ic_delete);
                    lock.setImageBitmap(lockSym);
                }

                // Set last modified date
                final TextView d = (TextView) convertView.findViewById(R.id.lastmodified);
                d.setText(item.lastModified);

        }
        return convertView;
    }
}
