package mobilemakers.seminar.mypaintapplication;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, OnTouchListener {

	//Our very own socket manager
	private PaintSocketManager socketManager;
	
	private SurfaceHolder sholder;
	private Bitmap bitmap;
	private Paint p; // added
	
	//a queue to hold touch events
	private Queue<PointF> pointQueue;
	private PointF lastPoint;
	
	//variables for drawing in rainbow colors!
	float[] hsv = new float[] { 0, 1, 1 };
	private boolean useRainbow = true;
	
    /**
     * Constructor for a surfaceview, pretty standard. Registers for touches, and creates our drawing thread. 
     * @param context
     */
	public MySurfaceView(Context context) {
		super(context);
		
		//SurfaceHolders are responsible for getting us our canvas to draw on!
		SurfaceHolder s = this.getHolder();
		
		//get surface event callbacks, letting us know when our surface is ready for drawing
		s.addCallback(this);
		
		//create our paint object
		p = new Paint();

		//set our color and width defaults, these can be adjusted later
		p.setColor(Color.CYAN);
		p.setStrokeWidth(5);
		
		//we initialize our queue of objects
		//this will hold all points ready to be drawn
		pointQueue = new ConcurrentLinkedQueue<PointF>();
		
		//can you focus on this view object?
		this.setFocusable(true);
		
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
				Log.d("touch", "down");	
				
				//queue up our new point
				addXYPoint(e.getX(), e.getY());

				//run our drawing code
				runDrawing();
				break;		
				
				//our user has moved their finger, but yet to withdraw it from the screen
			case MotionEvent.ACTION_MOVE:
				Log.d("touch", "move");			
				//queue up our new point
				addXYPoint(e.getX(), e.getY());
				
				//run our drawing code
				runDrawing();
				
				//this means we should draw a line from the last place we saw, to this new point!
				break;
				//the user has pulled their finger off the screen, 
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_POINTER_2_UP:
			case MotionEvent.ACTION_POINTER_3_UP:
				Log.d("touch", "up");
				
				lastPoint = null;
				
				break;
				
				//some other action has happened
			default:
				Log.d("touch", "other: " + e.getAction());
				break;
		}
					
		//we're done handling the event, let Android know
		return true;
	  }
	  
		/**
		 * Adds x,y to point queue, breaking into chunks and adding incremental points of the line
		 * @param x
		 * @param y
		 */
		public void addXYPoint(float x, float y)
		{
			//if we don't have a last point, add our first part
			if(lastPoint == null)
			{
				//we don't have a last point, simply add the x,y directly to the queue
				pointQueue.add(new PointF(x,y));
			}
			else
			{
				//how many circles per pixel distance
				float circlesPerPixelDistance = 1.1f;
				//our distance between the last point and this new point
				double pointDistance = Math.sqrt((x - lastPoint.x)*(x - lastPoint.x) 
										+  (y - lastPoint.y)* (y - lastPoint.y));
				
				//number of desired circles = pixelDistance / circlesPerPixelDistance  
				int pieces = (int)Math.ceil(pointDistance/circlesPerPixelDistance);

				
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
		
	  
		private void runDrawing()
		{
			//print our point
			updateBitmap();
			//update our screen!
			drawToScreen();	
		}
	  
	  private void drawToScreen()
	  {
		  //get the canvas from our surface holder
		  Canvas c = sholder.lockCanvas();
		  
		  //handle the drawing on our object
		  c.drawBitmap(bitmap, 0, 0, p);	
		
		  //release the canvas object for painting to the screen
		  sholder.unlockCanvasAndPost(c);
		  
	  }
	  
	  private void updateBitmap()
	  {
		  //let's paint to our bitmap using a canvas object
		  Canvas c = new Canvas(bitmap);	
		  
		  //define our color int for drawing
		  int color = Color.HSVToColor(hsv);
		  
		  while(!pointQueue.isEmpty())
		  {
			  //ask our queue the next point to draw!
			  PointF pointToDraw = pointQueue.poll();
		  
			  //do our drawing of points here!
			  c.drawCircle(pointToDraw.x, pointToDraw.y, 5, p);
			  
			  if (useRainbow && lastPoint != null) {
					p.setColor(color);
					color = Color.HSVToColor(hsv);
					hsv[0]++;
					if (hsv[0] >= 360)
						hsv[0] = 0;
				}
		  }
	  }
	  
	  private void setupBitmap()
	  {
		  Rect frame = sholder.getSurfaceFrame();
		  int width = frame.width();
		  int height = frame.height();
		  bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	  }
	  
		 /**
		  * Pass surface information to the thread for storing and updating.
		  */
		public void surfaceChanged(SurfaceHolder s, int format, int width, int height) {
			this.sholder = s;
			setupBitmap();
		}

		/**
		 * The surface has been created, we need to start our update process.
		 */
		public void surfaceCreated(SurfaceHolder s) {
			this.sholder = s;
			setupBitmap();
		}

		/**
		 * Need to make sure we stop our thread/drawing when we destroy our surface. 
		 */
		public void surfaceDestroyed(SurfaceHolder s) {
			this.sholder = null;
		}
	  
		public void clear()
		{
			//We create a canvas object for our bitmap
			Canvas c = new Canvas(bitmap);
			
			//we paint black over the entire canvas object
			c.drawColor(Color.BLACK);
			
			//we clear out our last point
			lastPoint = null;
			
			//and make sure we don't have anything in the queue for drawing
			pointQueue.clear();
			
			//then we draw the now empty screen for the user
			runDrawing();
		}
	  
		//Handling Server calls in this section!
		
		//helper function for displaying items to the user
		 private void quickToast(String msg) {
				Toast.makeText(this.getContext(), msg, Toast.LENGTH_SHORT).show();
			}		
		
		 public void setSocketManager(PaintSocketManager psm)
		 {
			 this.socketManager = psm;
		 }
		
		  //to be called on the main UI thread ONLY
		public boolean readServerMessage(byte[] rsp) {
			String msg = new String(rsp);
			Log.d("paintSocket", "Received message: " + msg);
			try {
				//create a json object from our string (parsed from the byte array)
				JSONObject json = new JSONObject(msg);
				
				//grab the object whose key is "hello" in our current json object
				String woah = json.getString("hello");
				
				//show everyone what we found!
				quickToast("hello: " + woah);				
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				quickToast(e.getMessage());
				return false;
			}
			
			return true;
			
		}
		public void sendServerMessage()
		{
			
			if(this.socketManager == null)
			{
				quickToast("Failed to send object, no socket server initialized in SurfaceView");
				return;
			}
			
			JSONObject json = new JSONObject();
			try {
				json
				.put("hello", "world"); 
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				quickToast(e.getMessage());
			}
			
			//turn our json object into a string, and away we go!
			socketManager.doSendMessage(json.toString());		
			
		}
		
}
