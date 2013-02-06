package mobilemakers.seminar.mypaintapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class PaintingActivity extends Activity {
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    //we set our layout to be the file named activity_main
	    MySurfaceView tempSV = new MySurfaceView(this);
	    
	    tempSV.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	   
	    this.setContentView(tempSV);	  
	    
	  }
	  

}
