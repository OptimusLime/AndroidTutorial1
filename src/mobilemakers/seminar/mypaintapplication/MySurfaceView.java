package mobilemakers.seminar.mypaintapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Basic View object to hold the menu screen
 * 
 * @author Daniel Wasserman
 */
public class MySurfaceView extends SurfaceView implements
		SurfaceHolder.Callback, OnTouchListener {
	MySurfaceThread thread;
	
     /**
      * Constructor for a surfaceview, pretty standard. Registers for touches, and creates our drawing thread. 
      * @param context
      */
	public MySurfaceView(Context context) {
		super(context);
		
		
		SurfaceHolder s = this.getHolder();
		//get surface event callbacks, letting us know when our surface is ready for drawing
		s.addCallback(this);
		
		this.setFocusable(true);
		
		//create our drawing thread (but don't start it yet - that will happen when the surface view is ready)
		thread = new MySurfaceThread(context);
		
		//register for touch events -- onTouch will be called with touch events
		this.setOnTouchListener(this);
		
	}
	
	/**
	 * Handle touch events passed from Android directly
	 */
	public boolean onTouch(View v, MotionEvent e) {
		
		//what kind of action is happening in our touch?
		switch(e.getAction())
		{

			//the user has just started pressing the screen
			case MotionEvent.ACTION_DOWN:
				
				//then we pass our first point to the thread
				thread.onDown(e.getX(), e.getY());
				break;
	
				//our user has moved their finger, but yet to withdraw it from the screen
			case MotionEvent.ACTION_MOVE:
				
				//this means we should draw a line from the last place we saw, to this new point!
				thread.onMove(e.getX(), e.getY());
				break;

				//the user has pulled their finger off the screen, 
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_POINTER_2_UP:
			case MotionEvent.ACTION_POINTER_3_UP:
				
				//Inform the thread, we've moved on with our lives
				thread.onUp(e.getX(),e.getY());
				break;
				
				//some other action has happened, check our pointer count, if it's 0, don't ask about x,y
			default:
				
				if(e.getPointerCount() != 0)
					thread.onUp(e.getX(), e.getY());
				break;
				
		}
		
		//we're done handling the event, let Android know
		return true;
	}
	
	 /**
	  * Pass surface information to the thread for storing and updating.
	  */
	public void surfaceChanged(SurfaceHolder s, int format, int width, int height) {
		thread.setSurfaceHolder(s);
	}

	/**
	 * The surface has been created, we need to start our update process.
	 */
	public void surfaceCreated(SurfaceHolder s) {
		thread.setSurfaceHolder(s);
		thread.start();
	}

	/**
	 * Need to make sure we stop our thread/drawing when we destroy our surface. 
	 */
	public void surfaceDestroyed(SurfaceHolder s) {
		thread.setRunning(false);
	}

	/**
	 * Function to stop the drawing thread on command. 
	 */
	public void stop() {
		thread.setRunning(false);
	}

	/**
	 * Function to clear the background of the drawn bitmap. 
	 */
	public void clear() {
		thread.clear();
	}

	/**
	 * Sets the color of the drawing line. 
	 * @param color
	 */
	public void setColor(int color) {
		thread.setColor(color);
	}
	
}
