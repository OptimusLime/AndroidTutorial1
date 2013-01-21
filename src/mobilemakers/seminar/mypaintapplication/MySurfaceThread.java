package mobilemakers.seminar.mypaintapplication;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.SensorEvent;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * The thread that runs the draw screen.
 * 
 * @author Daniel Wasserman
 * 
 */
public class MySurfaceThread extends Thread {
	
	private SurfaceHolder sholder;
	private int backGroundColor = Color.BLACK;
	
	private boolean running, paused, started;
	
	private Context context;
	private Paint p; // added
	
	private Queue<PointF> pointQueue;
	
	private Bitmap bitmap;
	
	//variables for drawing in rainbow colors!
	float[] hsv = new float[] { 0, 1, 1 };
	private boolean useRainbow;
	
	//Point to track the last known location of a screen touch
	PointF lastPoint = null;
	
	//acceleration variables to track drawing with the accelerometer
	private boolean useAccelerometerToDraw = false;
	PointF initialAcceleration = null;
	//defines what the maximum expected range of the accel sensor is
	//used to normalize the change in accel values
	public float maxmiumAccelerometerRange;
	
	/**
	 * @param c
	 *          The context this thread is in.
	 * 
	 *          The main thread to run the Menu Screen.
	 */
	public MySurfaceThread(Context c) {
		this.context = c;
		
		//default, we haven't started our thread, we're paused and not running
		started = false;
		paused = true;
		running = false;

		//we default to using a rainbow line paint!
		useRainbow = true;
		
		//create our paint object
		p = new Paint();

		//set our color and width defaults, these can be adjusted later
		p.setColor(Color.CYAN);
		p.setStrokeWidth(5);
		

		//point to keep track of the last touch we saw
		lastPoint = null;
		
		//we initialize our queue of objects
		//this will hold all points ready to be drawn
		pointQueue = new ConcurrentLinkedQueue<PointF>();
		
		//by default, we use the accelerometer to draw now!
		useAccelerometerForDrawing();
		
	}


	//We group touch event functions together, these functions decide how we interpret touch events
	
	/**
	 * Are we listening to the touch events being passed in? Depends on accelerometer drawing. 
	 * @return
	 */
	private boolean allowTouchEvents()
	{
		return !useAccelerometerToDraw;
	}
	 
	/**
	 * @param e
	 *          MotionEvent for the "down" event.
	 */
	public void onDown(float x, float y) {
		
		if(!allowTouchEvents())
			return;
		
		//stomp out our last point, we're starting a new line!
		lastPoint = null;
		
		//now add this point first
		addXYPoint(x,y);
	}
	public void onMove(float x, float y)
	{
		if(!allowTouchEvents())
			return;
		
		//add our point on move
		addXYPoint(x, y);		
		
	}
	public void onUp(float x, float y) {
		
		//ignore requests, if we're already 
		if(!allowTouchEvents())
			return;
		
		//we've ended a series of points, make sure we don't store the last point
				//this will prevent the line from continuing from the last point when we clikc on a new area
		clearVariables();
		
	}
	
	
	/**
	 * Adds x,y to point queue, breaking into chunks and adding incremental points of the line
	 * @param x
	 * @param y
	 */
	public void addXYPoint(float x, float y)
	{
		//keep in mind, we could be adding points WHILE we're printing points. 
		//To prevent this, we must synchronize our point queue, and wait until the other points
		//have been ejected for drawing
		synchronized(pointQueue)
		{
			//if we don't have a last point, add our first part
			if(lastPoint == null)
			{
				//we don't have a last point, simply add the x,y directly to the queue
				pointQueue.add(new PointF(x,y));
			}
			else
			{
				int pieces = 15;
				
				//we have a lastpoint, break down the difference into chunks, and draw all those chunks
				float dx = (x - lastPoint.x)/pieces;
				float dy = (y - lastPoint.y)/pieces;
				
					
				//loop through, adding the incremental points for our draw line
				for (int i = 0; i < pieces; i++) {
					pointQueue.add(new PointF(lastPoint.x + dx * i, lastPoint.y + dy*i));
				}
				pointQueue.add(new PointF(x,y));
			}

			lastPoint = new PointF(x,y);
		}
		
	}

	/** 
	 * Simple clearing of our important draw variables. Blows out the point queue. 
	 * Thread safe. 
	 */
	public void clearVariables()
	{
		synchronized(pointQueue)
		{
			lastPoint = null;
			pointQueue.clear();
		}
	}


	/**
	 * 
	 * @param h
	 *          The surface holder for the thread
	 * 
	 *          The surface holder allows the program to get the canvas to draw
	 *          things.
	 */
	public void setSurfaceHolder(SurfaceHolder h) {
		this.sholder = h;
		Rect frame = sholder.getSurfaceFrame();
		int width = frame.width();
		int height = frame.height();
		bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	}

	/**
	 * @param r
	 *          Set whether the program should start running or stop.
	 */
	public void setRunning(boolean r) {
		running = r;
	}

	/**
	 * @param p
	 *          Whether or not to pause the thread
	 */
	public void setPaused(boolean p) {
		paused = p;
	}

	/**
	 * Function to start the thread running.
	 */
	@Override
	public void start() {
		if (!started) {
			started = true;
			setPaused(false);
			setRunning(true);
			super.start();
		}
	}


	
	/**
	 * Pretty self explanatory, this code runs the thread. In this case, 
	 * we lock our canvas object, draw to it, then update. 
	 */
	@Override
	public void run() {
		while (running) {
			if (!paused) {
				
				Canvas c = null;
				try {
					c = sholder.lockCanvas();
					if (c != null) {
						synchronized (sholder) {
							doDraw(c);
						}
					}
				} finally {
					if (c != null)
						sholder.unlockCanvasAndPost(c);
				}
				
				update();
			}
		}
	}
	
	public void update() {
		Canvas c = new Canvas(bitmap);
		int color = -1;
		if (useRainbow) {
			color = Color.HSVToColor(hsv);
			p.setColor(color);
		}

		//we make sure that nobody is accessing pointqueue, while we're draining it
		//and drawing our lines
		synchronized(pointQueue)
		{
			
			PointF lp = null;
			
			//as long as we have points to draw, keep looping
			while (!pointQueue.isEmpty()) {
				
				//grab our point from the line
				PointF drawPoint = pointQueue.poll();
				
				//make sure we have the previous point
				if(lp != null)
					//if we have a previous point, draw a line between the two
					c.drawLine(lp.x, lp.y, drawPoint.x, drawPoint.y, p);
				
				//set our previous point as the point we just looked at
				lp = new PointF(drawPoint.x, drawPoint.y);
				
				//if we're using a random style of drawing, we need to update our color 
				//after each line drawn!
				if (useRainbow && lastPoint != null) {
					p.setColor(color);
					color = Color.HSVToColor(hsv);
					hsv[0]++;
					if (hsv[0] >= 360)
						hsv[0] = 0;
				}
			}
		}
	}

	/**
	 * Draws our bitmap to the canvas. Generic. 
	 * @param c
	 */
	public void doDraw(Canvas c) {
		c.drawBitmap(bitmap, 0, 0, p);
	}

	/**
	 * Clears the canvas to a specific color. Creates completely new canvas. 
	 */
	public void clear() {
		//do we need to create an entirely new canvas? Possibly not. 
		Canvas c = new Canvas(bitmap);
		c.drawColor(Color.BLACK);
	}

	/**
	 * Sets our painting color for the brush. 
	 * @param color
	 */
	public void setColor(int color) {
		if (color == -1) {
			useRainbow = true;
		} else {
			useRainbow = false;
			p.setColor(color);
		}
	}
	
	//Our helper functions for some math operations. Best practice is to put these in their own class
	//but for our purposes, we'll leave them with our drawing code
	 /**
	  * Here we take in the x and y value, along with the min and max of x/y coordinates, and return the
	  * clamped value. It's a very simple function. 
	  * @param xValue
	  * @param yValue
	  * @param minWidth
	  * @param maxWidth
	  * @param minHeight
	  * @param maxHeigth
	  * @return
	  */
	 private PointF clampAccelXY(float xValue, float yValue, 
			 float minWidth, float maxWidth, 
			 float minHeight, float maxHeigth)
	 {
		 float finX = clampValue(xValue, minWidth, maxWidth);
		 float finY = clampValue(yValue, minHeight, maxHeigth);
		 
		 return new PointF(finX, finY);
	 }
	 
	 /**
	  * Clamps any value between min and max specified floats
	  * @param val
	  * @param min
	  * @param max
	  * @return
	  */
	 private float clampValue(float val, float min, float max)
	 {
		 return Math.max(min,  Math.min(val, max));
	 }
	 
	
	//Group our accelerometer functions together
	//these functions handling turning on/off accelerometer drawing
	
	/**
	 * Override touch drawing with accelerometer 
	 */
	public void useAccelerometerForDrawing()
	{
		useAccelerometerToDraw = true;
		clearVariables();
		resetAccelerometerVariables();
	}
	public void stopUsingAccelerometer()
	{
		useAccelerometerToDraw = false;
		clearVariables();
	}
	
	private void resetAccelerometerVariables()
	{
		initialAcceleration = null;	
	}
	private void setInitialAccelerationData(float x, float y, float z)
	{
		//if we don't have initial acceleration, create new point object
		if(initialAcceleration == null)
			initialAcceleration = new PointF(x, y);
		//otherwise, just update our variable with the new information
		else {
			initialAcceleration.x = x;
			initialAcceleration.y = y;
		}
	}
	/**
	 * Get the difference in accelerations, and return a scaled delta movement based on draw speed. 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private PointF getAccelerationDelta(float x, float y, float z)
	{
		float drawSpeed = 10f;
		float dx = drawSpeed*(initialAcceleration.x - x)/maxmiumAccelerometerRange;
		float dy =	drawSpeed*(y - initialAcceleration.y)/maxmiumAccelerometerRange;
		return new PointF(dx,dy);
	}
	
	

	
	/**
	 * Receive and process accelerometer data in order to draw using accelerometer. 
	 * This is where most of the logic for drawing with accelerometer happens. 
	 * @param x
	 * @param y
	 * @param z
	 * @param accuracy
	 */
	 public void receiveSensorData(float x, float y, float z, float accuracy) {

		 //are we using the accelerometer? No, then ignore sensor data
		 //ideally, we would deregister from receiving these sensor events, 
		 //but this will do fine for now. 
		 if(!useAccelerometerToDraw)
			 return;
		 
    	//Need to decide what to do with sensor data, when do you move, how do you move?
		 if(initialAcceleration == null)
		 {
			 //here we set our initialAcceleration data
			 setInitialAccelerationData(x, y, z);
			 
			 //now we have say what our "lastPoint" is.
			 //we default to the center of the screen
			 
			 lastPoint = centerOfScreen();
			 
		 }
		 else
		 {
			 //if our last point gets reset, start from the center again
			 if(lastPoint == null)
				 lastPoint = centerOfScreen();
			 
			 //we get the magnitude of change, 
			 //based on the difference between current accel, and initial accel vector
			 PointF direction = getAccelerationDelta(x, y, z);
			 
			 //we need to make sure we don't draw outside of our bounds
			 Rect frame = sholder.getSurfaceFrame();
			 
			 //now we clamp our position, based on the width and height
			 PointF clampedXY = clampAccelXY(lastPoint.x + direction.x, lastPoint.y + direction.y, 
					 0, frame.width(),
					 0, frame.height());
			 
			 //then we add our modified point to be drawn
			 addXYPoint(clampedXY.x, clampedXY.y);
			 
			 //we modify our lastPoint to be the clamped point we just calculated
			 lastPoint = new PointF(clampedXY.x, clampedXY.y);
		 }
	 }

	 /**
	  * Returns half width, half height
	  * @return
	  */
	 private PointF centerOfScreen()
	 {
		 //now we have say what our "lastPoint" is.
		 //We get the size of the screen
		 Rect frame = sholder.getSurfaceFrame();
		 
		 //then we set our initial draw point to be the center of the screen
		 return new PointF(frame.width()/2, frame.height()/2);
	 }
	
}
