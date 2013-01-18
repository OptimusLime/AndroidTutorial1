package mobilemakers.seminar.mypaintapplication;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class PaintingActivity extends Activity {

	private MySurfaceView msv;
	 private SensorManager mSensorManager;
     private Sensor mAccelerometer;
     
   
     private void initializeAccelerometer()
     {
    		//Deal with accelerometer initialization!
		 mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
	     mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	     
	     msv.setMaxSensorRange(mAccelerometer.getMaximumRange());
		
     }
    		 
     //Handle Accelerometer registration
     protected void onResume() {
         super.onResume();
         mSensorManager.registerListener(msv, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
     }

     protected void onPause() {
         super.onPause();
         mSensorManager.unregisterListener(msv);
     }

     
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		msv = new MySurfaceView(this);
		this.setContentView(msv);
		
		//init accelerometer after surface view is created, passing maximum sensorrange
		initializeAccelerometer();
		
	}
	

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_options, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear:
			msv.clear();
			break;
		case R.id.red:
			msv.setColor(Color.RED);
			break;
		case R.id.blue:
			msv.setColor(Color.BLUE);
			break;
		case R.id.green:
			msv.setColor(Color.GREEN);
			break;
		case R.id.yellow:
			msv.setColor(Color.YELLOW);
			break;
		case R.id.cyan:
			msv.setColor(Color.CYAN);
			break;
		case R.id.rainbow:
			msv.setColor(-1);
			break;
		}
		return true;
	}
}
