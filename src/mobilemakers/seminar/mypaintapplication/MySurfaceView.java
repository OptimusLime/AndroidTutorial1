package mobilemakers.seminar.mypaintapplication;

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

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, OnTouchListener {


	private SurfaceHolder sholder;
	private Bitmap bitmap;
	private Paint p; // added
	
	//a queue to hold touch events
	private Queue<PointF> pointQueue;
	
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
				pointQueue.add(new PointF(e.getX(), e.getY()));
				//print our point
				updateBitmap();
				//update our screen!
				drawToScreen();				
				break;		
				
				//our user has moved their finger, but yet to withdraw it from the screen
			case MotionEvent.ACTION_MOVE:
				Log.d("touch", "move");			
				//queue up our new point
				pointQueue.add(new PointF(e.getX(), e.getY()));
				//print our point
				updateBitmap();
				//update our screen!
				drawToScreen();				
				//this means we should draw a line from the last place we saw, to this new point!
				break;
				//the user has pulled their finger off the screen, 
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_POINTER_2_UP:
			case MotionEvent.ACTION_POINTER_3_UP:
				Log.d("touch", "up");
				break;
				
				//some other action has happened
			default:
				Log.d("touch", "other: " + e.getAction());
				break;
		}
					
		//we're done handling the event, let Android know
		return true;
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
		  
		  while(!pointQueue.isEmpty())
		  {
			  //ask our queue the next point to draw!
			  PointF pointToDraw = pointQueue.poll();
		  
			  //do our drawing of points here!
			  c.drawCircle(pointToDraw.x, pointToDraw.y, 5, p);
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
	  
	  
	
}
