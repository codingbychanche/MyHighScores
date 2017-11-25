package com.example.berthold.highscore;

/**
 * Bitmap Tools
 *
 * A collection of methods to process bitmap gfx
 *
 * @author  Berthold Fritz 2017
 *
 * 16.6.2017
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;

public class MyBitmapTools {

    // Debug
    static String tag=MyBitmapTools.class.getSimpleName();

    /**
     * Scale bitmap
     *
     * Scales a bitmap and keeps it's aspect ratio
     *
     * @param b         Bitmap to be scaled
     * @param destW     New size
     * @param destH
     * @return          Scaled bitmap
     */

    public static Bitmap scaleBitmap(Bitmap b,float destW,float destH){

        // Calculate new width and height, keep aspect ratio
        float sourceRatio;
        float sourceW=b.getWidth();
        float sourceH=b.getHeight();

        // Calculate...
        if (sourceW >= sourceH){            // Source picture is in landscape mode?
            sourceRatio=sourceH/sourceW;
            destH=destW*sourceRatio;
        } else {                            // Source picture is in portrait mode!
            sourceRatio=sourceW/sourceH;
            destW=destH*sourceRatio;
            destH=destH/2;                  // That is just an educated guess, remove if you don't like it....
        }
        Log.v(tag,"Ratio:"+sourceRatio+"  destW "+destW+"   destH"+destH);

        // Scale picture to screen size
        Bitmap changedBitmap=Bitmap.createScaledBitmap(b,(int)destW,(int)destH,false);
        Log.v(tag,"Destination picture => Ratio:"+sourceRatio+"  destW "+(int)destW+"   destH "+(int)destH);

        return changedBitmap;
    }

    /**
     * Get dominant color at top of a Bitmap
     *
     * @param b     Bitmap to get the color from
     * @return      Dominant color at top of image
     *
     */

    public static int getDominatColorAtTop(Bitmap b) {

        // Get dominant color of picture and set background color of toolbar and layout accordingly
        // This is a neat little trick I got from 'stackOverflow'.
        Bitmap c = Bitmap.createScaledBitmap(b, 1, b.getHeight(), true);
        int colorTop = c.getPixel(0, 0);
        c.recycle();                        // Remove image from memory, don't wait for GC

        return colorTop;
    }

    /**
     * Get dominant color at bottom of a Bitmap
     *
     */

    public static int getDominantColorAtBottom(Bitmap b){

        // Get dominant color of picture and set background color of toolbar and layout accordingly
        // This is a neat little trick I got from 'stackOverflow'.
        Bitmap c = Bitmap.createScaledBitmap(b, 1, b.getHeight(), true);
        int colorBottom = c.getPixel(0, b.getHeight()-1);
        c.recycle();                        // Remove image from memory, don't wait for GC

        return colorBottom;
    }

    /**
     * Returns a circular picture from the given bitmap
     *
     * @param   b               Bitmap to process
     * @return  changedBitmap   Processed bitmap
     *
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

    /**
     * Returns a monocrome image of the image given.
     * Uses Floyd- Steinberg single line dithering algorythmen
     *
     * @param   b               Bitmap to process
     * @param   thr             Threshold
     * @return  changedBitmap   Changed bitmap
     *
     */

    public static Bitmap toMonocrome (Bitmap b,float thr,DisplayMetrics m)
    {
        // Create a mutable bitmap

        Bitmap changedBitmap=Bitmap.createBitmap(m,b.getWidth(),b.getHeight(),Bitmap.Config.ARGB_8888);


        int width=b.getWidth();         // Size
        int height=b.getHeight();

        // even lines

        for (int y=0;y<height;y=y+2) {
            float error = 0;
            for (int x = 0; x < width; x++) {
                int color = b.getPixel(x, y)*-1;

                int r=(color>>>16) & 0xFF;
                int g=(color>>>8) & 0xFF;
                int bl=(color>>>0) & 0xFF;

                float lum=(r*0.2126f+g*0.7125f+bl*0.0722f)/255;

                lum=lum+ error;

                if (lum <= thr) {
                    error = lum;
                    changedBitmap.setPixel(x, y, 0);
                }
                if (lum > thr) {
                    //error = lum ;
                    changedBitmap.setPixel(x, y, Color.BLACK);
                    error = 0;
                }
            }
        }

        // Odd lines

        for (int y = 1; y <= height-1; y = y + 2) {
            float error = 0;
            for (int x = width-1; x!=0; x--) {
                int color = b.getPixel(x, y)*-1;

                int r=(color>>>16) & 0xFF;
                int g=(color>>>8) & 0xFF;
                int bl=(color>>>0) & 0xFF;

                float lum=(r*0.2126f+g*0.7125f+bl*0.0722f)/255;

               lum=lum+ error;

                if (lum <= thr) {
                    error = lum;
                    changedBitmap.setPixel(x, y, 0);
                }
                if (color > thr) {
                    //error = lum ;
                    changedBitmap.setPixel(x, y, Color.BLACK);
                    error = 0;

                }
            }
        }
        return changedBitmap;
    }

    /**
     * Returns a pixelated picture from the given bitmap
     *
     * @param   b               Bitmap to process
     * @return  changedBitmap   Processed bitmap
     *
     * todo: Display metrics, why?
     *
     */

    public static Bitmap toPixelatedImage(Bitmap b,DisplayMetrics m,int pixelSize)
    {
        // Create a mutable bitmap
        Bitmap changedBitmap=Bitmap.createBitmap(m,b.getWidth(),b.getHeight(),Bitmap.Config.ARGB_8888);

        int width=b.getWidth();         // Size
        int height=b.getHeight();

        Canvas c=new Canvas(changedBitmap);

        Paint p=new Paint();
        p.setAntiAlias(false);

        for (int y=0;y<height;y=y+pixelSize){
            for (int x=0;x<width;x=x+pixelSize){

                int color = b.getPixel(x, y);        // Get pixel from source
                p.setColor(color);
                c.drawRect(x,y,x+pixelSize,y+pixelSize,p);
            }
        }
        return changedBitmap;
    }
}
