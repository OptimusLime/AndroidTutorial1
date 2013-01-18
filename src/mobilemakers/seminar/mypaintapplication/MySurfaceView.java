package mobilemakers.seminar.mypaintapplication;

import android.content.Context;
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

	public MySurfaceView(Context context) {
		super(context);
		SurfaceHolder s = this.getHolder();
		s.addCallback(this);
		this.setFocusable(true);
		thread = new MySurfaceThread(context);
		this.setOnTouchListener(this);
	}

	public boolean onTouch(View v, MotionEvent e) {
		
		switch(e.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			thread.lastX = -1;
			thread.lastY = -1;
			thread.onDown(e.getX(), e.getY());
			Log.d("actiondown", "made it down");
			break;
		case MotionEvent.ACTION_MOVE:
			Log.d("actionMove", "made it to move");
			thread.onMove(e.getX(), e.getY());
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_POINTER_2_UP:
		case MotionEvent.ACTION_POINTER_3_UP:
			
			Log.d("actionup", "made it up");
			thread.onUp(0,0);
			
			break;
			default:
				if(e == null)
					break;
				Log.d("actionEvent", "" + e.getAction());
				break;
		}
		
	

		return true;
	}
	
	
	public void surfaceChanged(SurfaceHolder s, int format, int width, int height) {
		thread.setSurfaceHolder(s);
	}

	public void surfaceCreated(SurfaceHolder s) {
		thread.setSurfaceHolder(s);
		thread.start();
	}

	public void surfaceDestroyed(SurfaceHolder s) {
		thread.setRunning(false);
	}

	public void stop() {
		thread.setRunning(false);
	}

	public void clear() {
		thread.clear();
	}

	public void setColor(int color) {
		thread.setColor(color);
	}
}
