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
		SurfaceHolder.Callback, OnTouchListener, SensorEventListener {
	MySurfaceThread thread;
	
	//Accelerometer Variables
	//sensor manager has access to all hardware sensors
	 private SensorManager mSensorManager;
	 
	 //accelerometer is a sensor object we store
     private Sensor mAccelerometer;
	
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
	 * Handle activity resume, register listening for accelerometer data.
	 */
	 public void onResume() {

		 Log.d("registering accel listener", "registered");
		 //we need to register to receive sensor events (and how often we want to receive them)
		  mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
     }
	 
	 /**
	  * Handling activity pausing, stop listening to accelerometer data.
	  */
	 public void onPause() {
		 Log.d("releasing accel listener", "released");
		 //we've paused, stop asking for sensor information
         mSensorManager.unregisterListener(this);
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
	
	/**
	 * Function to initialize sensors for this view. Used to access the accelerometer for drawing. 
	 * Disables accelerometer drawing for the simulator. 
	 * @param sensorManager
	 */
	public void setSensorManagement(SensorManager sensorManager)
	{
		//set our sensor manager. This object has access to all the different hardware sensors
		mSensorManager = sensorManager;
		
		//get our accelerometer
		mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		//here we check if we're in the simulator or not, disabling the accelerometer if required   
	     if("google_sdk".equals( Build.PRODUCT ) || "sdk".equals( Build.PRODUCT )){
	    	 Log.d("accelerometerdisable", "Disabled accelerometer");
	    	 disableAccelerometerDrawing();
	     }
		
		//set our max accelerometer range
		setMaxSensorRange(mAccelerometer.getMaximumRange());
		
	}
	
	/**
	 * We pass max sensor range to our thread, so that it can calculate the change in accel data.
	 * @param sensorRange
	 */
	public void setMaxSensorRange(float sensorRange)
	{
		//let the thread know what the maximum acceleration range for calculating brush 
		//movement
		thread.maxmiumAccelerometerRange = sensorRange;
	}
	
	/**
	 * Pretty straightforward, stop using accelerometer data for drawing. 
	 */
	public void disableAccelerometerDrawing()
	{
		//cut off the accelerometer
		thread.stopUsingAccelerometer();
	}
	/**
	 * When accuracy changes for the sensor, we are updated. We aren't too concerned since drawing doesn't have
	 * to be that precise. 
	 */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    	//nothing to do in our case
    }

    
    /**
     * Receive sensor events from Android. We're only listening for accelerometer information.
     */
    public void onSensorChanged(SensorEvent event) {

    	//we only acknowledge accelerometer data here, but in the future, maybe we want other data
    	//make sure we future proof a little, and don't accidentally read other sensor data
    	if(event.sensor != mAccelerometer)
    		return;
    	
    	//We know that we only register for accelerometer info, 
    	//and we know there is x, y, z information passed from this sensor
    	
    	//pass x,y,z reading, and the accuracy to the thread.
    	thread.receiveSensorData(event.values[0], event.values[1], event.values[2], event.accuracy);
		
    }
	
}
