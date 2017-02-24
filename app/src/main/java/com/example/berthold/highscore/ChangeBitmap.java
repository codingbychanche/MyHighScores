package com.example.berthold.highscore;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.DisplayMetrics;

/**
 * Change Bitmap
 *
 * A collection of methods to process bitmap gfx
 *
 * Created by Berthold on 12/21/16.
 */

public class ChangeBitmap {

    /**
     * Returns a circular picture from the given bitmap
     *
     * @param   b               Bitmap to process
     * @return  changedBitmap   Processed bitmap
     */
    public static Bitmap toRoundedImage(Bitmap b,DisplayMetrics m)
    {
        // Create a mutable bitmap

        Bitmap changedBitmap=Bitmap.createBitmap(m,b.getWidth(),b.getHeight(),Bitmap.Config.ARGB_8888);

        // Create a round Image

        // Calc max rad for pic

        int radius;

        int width=b.getWidth();         // Size
        int height=b.getHeight();

        int cx=width/2;                  // Center of pic
        int cy=b.getHeight()/2;

        if(width>b.getHeight()) radius=height/2;
        else radius=width/2;

        for (int y=0;y<height;y++){
            for (int x=0;x<width;x++){

                if (((x-cx)*(x-cx)+(y-cy)*(y-cy))<radius*radius ) {

                    int color = b.getPixel(x, y);        // Get pixel from source
                    changedBitmap.setPixel(x, y, color);
                } else {
                    changedBitmap.setPixel(x,y, Color.TRANSPARENT);
                }
            }
        }

        return changedBitmap;
    }
}
