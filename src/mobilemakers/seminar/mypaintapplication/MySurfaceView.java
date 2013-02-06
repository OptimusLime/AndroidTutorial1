package mobilemakers.seminar.mypaintapplication;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

public class MySurfaceView extends SurfaceView implements OnTouchListener {

    /**
     * Constructor for a surfaceview, pretty standard. Registers for touches, and creates our drawing thread. 
     * @param context
     */
	public MySurfaceView(Context context) {
		super(context);
		
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
				break;		
				
				//our user has moved their finger, but yet to withdraw it from the screen
			case MotionEvent.ACTION_MOVE:
				Log.d("touch", "move");
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
	
}
