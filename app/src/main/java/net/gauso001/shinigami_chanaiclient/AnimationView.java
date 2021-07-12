package net.gauso001.shinigami_chanaiclient;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;


public class AnimationView extends android.support.v7.widget.AppCompatImageView
{
    private static final String TAG = "AnimationView";
    private Context mContext = null;

    private static final int DELAY = 32; //delay between frames in milliseconds
    private  int drawX = 0;
    private  int drawY = 0;

    private boolean mIsPlaying = false;
    private boolean mStartPlaying = false;


    private ArrayList<Bitmap> mBitmapList = new ArrayList<Bitmap>();

    ArrayList<Bitmap> idle;
    ArrayList<Bitmap> blink = new ArrayList<Bitmap>();
    ArrayList<Bitmap> head_turn_left = new ArrayList<Bitmap>();
    ArrayList<Bitmap> head_turn_right = new ArrayList<Bitmap>();
    ArrayList<Bitmap> bend_left = new ArrayList<Bitmap>();
    ArrayList<Bitmap> bend_right = new ArrayList<Bitmap>();
    ArrayList<Bitmap> blush = new ArrayList<Bitmap>();

    private int play_frame = 0;
    private long last_tick = 0;

    public boolean blinkNext = false;
    public boolean idleNext = false;
    public boolean head_turn_left_next = false;
    public boolean head_turn_right_next = false;
    public boolean bend_left_next = false;
    public boolean bend_right_next = false;
    public boolean blushNext = false;

    public AnimationView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onDraw(Canvas c)
    {
        /******* onDraw method called first time and when postInvalidate() called *****/

        //Log.d(TAG, "onDraw called");
        if (mStartPlaying)
        {
            Log.d(TAG, "starting animation...");
            play_frame = 0;
            mStartPlaying = false;
            mIsPlaying = true;

            // Again call onDraw method
            postInvalidate();
        }
        else if (mIsPlaying)
        {

            if (play_frame >= mBitmapList.size()) {
                //mIsPlaying = false;
                if (blushNext) { mBitmapList = blush; idleNext = true; blushNext = false;}
                else if (blinkNext) { mBitmapList = blink; idleNext = true; blinkNext = false;}
                else if (head_turn_left_next) { mBitmapList = head_turn_left; idleNext = true; head_turn_left_next = false;}
                else if (head_turn_right_next) { mBitmapList = head_turn_right; idleNext = true; head_turn_right_next = false;}
                else if (bend_left_next) { mBitmapList = bend_left; idleNext = true; bend_left_next = false;}
                else if (bend_right_next) { mBitmapList = bend_right; idleNext = true; bend_right_next = false;}
                else if (idleNext) { mBitmapList = idle; }


                play_frame = 0;
            }

                long time = (System.currentTimeMillis() - last_tick);

                int draw_x = drawX;
                int draw_y = drawY;
                if (time >= DELAY) //the delay time has passed. set next frame
                {
                    last_tick = System.currentTimeMillis();
                    c.drawBitmap(mBitmapList.get(play_frame), draw_x, draw_y, null);
                    play_frame++;

                    // Again call onDraw method
                    postInvalidate();
                }
                else //still within delay.  redraw current frame
                {
                    c.drawBitmap(mBitmapList.get(play_frame), draw_x, draw_y, null);

                    // Again call onDraw method
                    postInvalidate();
                }
        }
    }

    /*ideally this should be in a background thread*/
    public void loadAnimation(String prefix, int beginFrame, int nframes)
    {
        drawX = 0;
        drawY = 0;
        mBitmapList.clear();

        for (int x = beginFrame; x < nframes; x++)
        {
            String name = prefix + "" + x;  // prefix = "spark" see loadAnimation call

            //Log.d(TAG, "loading animation frame: " + name);

            // Set Bitmap image
            int res_id = mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
            BitmapDrawable d = (BitmapDrawable) mContext.getResources().getDrawable(res_id);
            mBitmapList.add(Bitmap.createScaledBitmap(d.getBitmap(), 1080, 1920, false));
        }

        for (int x = 0; x < 26; x++)
        {
            String name = "blush" + "" + x;  // prefix = "spark" see loadAnimation call

            //Log.d(TAG, "loading animation frame: " + name);

            // Set Bitmap image
            int res_id = mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
            BitmapDrawable d = (BitmapDrawable) mContext.getResources().getDrawable(res_id);
            blush.add(Bitmap.createScaledBitmap(d.getBitmap(), 1080, 1920, false));
        }

        idle = mBitmapList;
    }

    public void loadAdditional()
    {
        for (int x = 0; x < 11; x++)
        {
            String name = "blink" + "" + x;  // prefix = "spark" see loadAnimation call

            //Log.d(TAG, "loading animation frame: " + name);

            // Set Bitmap image
            int res_id = mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
            BitmapDrawable d = (BitmapDrawable) mContext.getResources().getDrawable(res_id);
            blink.add(Bitmap.createScaledBitmap(d.getBitmap(), 1080, 1920, false));
        }

        for (int x = 0; x < 21; x++)
        {
            String name = "head_turn_left" + "" + x;  // prefix = "spark" see loadAnimation call

            //Log.d(TAG, "loading animation frame: " + name);

            // Set Bitmap image
            int res_id = mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
            BitmapDrawable d = (BitmapDrawable) mContext.getResources().getDrawable(res_id);
            head_turn_left.add(Bitmap.createScaledBitmap(d.getBitmap(), 1080, 1920, false));
        }

        for (int x = 0; x < 21; x++)
        {
            String name = "head_turn_right" + "" + x;  // prefix = "spark" see loadAnimation call

            //Log.d(TAG, "loading animation frame: " + name);

            // Set Bitmap image
            int res_id = mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
            BitmapDrawable d = (BitmapDrawable) mContext.getResources().getDrawable(res_id);
            head_turn_right.add(Bitmap.createScaledBitmap(d.getBitmap(), 1080, 1920, false));
        }

        for (int x = 0; x < 41; x++)
        {
            String name = "bend_right" + "" + x;  // prefix = "spark" see loadAnimation call

            //Log.d(TAG, "loading animation frame: " + name);

            // Set Bitmap image
            int res_id = mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
            BitmapDrawable d = (BitmapDrawable) mContext.getResources().getDrawable(res_id);
            bend_right.add(Bitmap.createScaledBitmap(d.getBitmap(), 1080, 1920, false));
        }

        for (int x = 0; x < 41; x++)
        {
            String name = "bend_left" + "" + x;  // prefix = "spark" see loadAnimation call

            //Log.d(TAG, "loading animation frame: " + name);

            // Set Bitmap image
            int res_id = mContext.getResources().getIdentifier(name, "drawable", mContext.getPackageName());
            BitmapDrawable d = (BitmapDrawable) mContext.getResources().getDrawable(res_id);
            bend_left.add(Bitmap.createScaledBitmap(d.getBitmap(), 1080, 1920, false));
        }

    }


    public void playAnimation()
    {
        mStartPlaying = true;

        // Again call onDraw method
        postInvalidate();
    }
}
