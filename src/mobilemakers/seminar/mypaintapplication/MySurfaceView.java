package mobilemakers.seminar.mypaintapplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
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
	
	
	//Server related variables
	
	//a map of queues to hold touch events for each socket
	private Map<String, Queue<PointF>> sidToQueue= new HashMap<String, Queue<PointF>>();	
	//a map to hold last points
	private Map<String, PointF> sidLastPoints = new HashMap<String, PointF>();
	//a map of paint objects for each sid
	private Map<String, Paint> sidPaintObjects = new HashMap<String, Paint>();
	
	private Queue<PointF> pointQueue;
	private PointF lastPoint;	
	
	//variables for drawing in rainbow colors!
	float[] hsv = new float[] { 0, 1, 1 };
	private boolean useRainbow = false;
	
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
				addXYPointUser(e.getX(), e.getY());

				//run our drawing code
				runDrawing();
				
				//let's take this oportunity to send a message to the server!
				this.sendServerMessage(new PointF(e.getX(), e.getY()), MotionEvent.ACTION_DOWN);
				
				
				break;		
				
				//our user has moved their finger, but yet to withdraw it from the screen
			case MotionEvent.ACTION_MOVE:
				Log.d("touch", "move");			
				//queue up our new point
				addXYPointUser(e.getX(), e.getY());
				
				//run our drawing code
				runDrawing();
				
				//let's take this oportunity to send a message to the server!
				this.sendServerMessage(new PointF(e.getX(), e.getY()),MotionEvent.ACTION_MOVE);
				
				//this means we should draw a line from the last place we saw, to this new point!
				break;
				//the user has pulled their finger off the screen, 
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_POINTER_2_UP:
			case MotionEvent.ACTION_POINTER_3_UP:
				Log.d("touch", "up");
								
				//let's take this oportunity to send a message to the server!
				this.sendServerMessage(new PointF(e.getX(), e.getY()),MotionEvent.ACTION_UP);
				
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
	  
	  public void addXYPointUser(float x, float y)
		{
		  lastPoint = addXYPoint(x, y, pointQueue, lastPoint);
		}
	  public void addXYPointServer(float x, float y, String socketID)
		{
		  sidLastPoints.put(socketID, addXYPoint(x, y, sidToQueue.get(socketID), sidLastPoints.get(socketID)));
		}
	  
		/**
		 * Adds x,y to point queue, breaking into chunks and adding incremental points of the line
		 * @param x
		 * @param y
		 */
		public PointF addXYPoint(float x, float y, Queue<PointF> pq, PointF lp)
		{
			//if we don't have a last point, add our first part
			if(lp == null)
			{
				//we don't have a last point, simply add the x,y directly to the queue
				pq.add(new PointF(x,y));
			}
			else
			{
				//how many circles per pixel distance
				float circlesPerPixelDistance = 1.1f;
				//our distance between the last point and this new point
				double pointDistance = Math.sqrt((x - lp.x)*(x - lp.x) 
										+  (y - lp.y)* (y - lp.y));
				
				//number of desired circles = pixelDistance / circlesPerPixelDistance  
				int pieces = (int)Math.ceil(pointDistance/circlesPerPixelDistance);

				
				//we have a lastpoint, break down the difference into chunks, and draw all those chunks
				float dx = (x - lp.x)/pieces;
				float dy = (y - lp.y)/pieces;
				
					
				//loop through, adding the incremental points for our draw line
				for (int i = 0; i < pieces; i++) {
					pq.add(new PointF(lp.x + dx * i, lp.y + dy*i));
				}
				pq.add(new PointF(x,y));
			}

			return new PointF(x,y);			
			
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
		  
		  
		  for(String key : sidToQueue.keySet())
		  {
			  Queue<PointF> pointQueue = sidToQueue.get(key);
			  while(!pointQueue.isEmpty())
			  {
				  //ask our queue the next point to draw!
				  PointF pointToDraw = pointQueue.poll();
			  
				  //do our drawing of points here!
				  c.drawCircle(pointToDraw.x, pointToDraw.y, 5, sidPaintObjects.get(key));
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
			

			//we clear out our last server points
			sidLastPoints.clear();
			for(String key: sidToQueue.keySet())
			{
				//and make sure we don't have anything in the queue for drawing server related variables
				sidToQueue.get(key).clear();
			}			
			
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
		 
		 void checkSocketExists(String sid)
		{
			if(!sidToQueue.containsKey(sid))
			{
				sidToQueue.put(sid, new ConcurrentLinkedQueue<PointF>());
			}
			if(!sidPaintObjects.containsKey(sid))
			{
				sidPaintObjects.put(sid, createRandomPaintObject());
			}
		}
		 Paint createRandomPaintObject()
		 {
			//initialize our server object too
			Paint rPaint = new Paint();

			//set our color and width defaults, these can be adjusted later
			int rColor = Color.rgb((int)Math.floor(Math.random()*255), 
					(int)Math.floor(Math.random()*255), 
					(int)Math.floor(Math.random()*255));
			
			rPaint.setColor(rColor);
			rPaint.setStrokeWidth(5);
			
			return rPaint;
		 }
		
		  //to be called on the main UI thread ONLY
		public boolean readServerMessage(byte[] rsp) {
			String msg = new String(rsp);
			
			Log.d("paintSocket", "Received message: " + msg);
			
			
			try {
				JSONObject json = new JSONObject(msg);
				
				float x = Float.parseFloat(json.getString("x"));
				float y = Float.parseFloat(json.getString("y"));
				
				String socketID = json.getString("sid");
				
				//nothing to do without the socketid
				if(socketID == null)
					return true;
				
				checkSocketExists(socketID);
				
				int mouseMessage = Integer.parseInt(json.getString("mouse"));
				
				switch(mouseMessage)
				{
				//we do the same things whether it's first touch or last -- 
					case MotionEvent.ACTION_DOWN:
						
						sidLastPoints.remove(socketID);
						//queue up our new point
						addXYPointServer(x,y, socketID);
						
						break;
					case MotionEvent.ACTION_MOVE:
						
						//queue up our new point
						addXYPointServer(x,y, socketID);
						
						break;
						
					case MotionEvent.ACTION_UP:
					//clear out the server variables
						//we clear out our last server point
						sidLastPoints.remove(socketID);	
						//and make sure we don't have anything in the queue for drawing server related variables
//						serverPointQueue.clear();
						
						break;
				}
				
				//run our drawing code
				runDrawing();
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				quickToast(e.getMessage());
			}
			
			
			
			return true;
			
		}
		public void sendServerMessage(PointF point, int mouseMessage)
		{
			
			if(this.socketManager == null)
			{
				quickToast("Failed to send object, no socket server initialized in SurfaceView");
				return;
			}
			
			JSONObject json = new JSONObject();
			try {
				
				//grab the size of our surface view
			  Rect frame = sholder.getSurfaceFrame();
				
			  //set our json object, but invert the y point, so that it's mirrored across the x axis!
				json
				.put("x", point.x)
				.put("y", point.y)
				.put("mouse", mouseMessage);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				quickToast(e.getMessage());
			}
			
			//turn our json object into a string, and away we go!
			socketManager.doSendMessage(json.toString());		
			
		}
		
}
