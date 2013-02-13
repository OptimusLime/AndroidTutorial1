package mobilemakers.seminar.mypaintapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class PaintingActivity extends Activity {
	
	private MySurfaceView msv;
	
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    //we set our layout to be the file named activity_main
	    msv = new MySurfaceView(this);
	    
	    msv.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	   
	    this.setContentView(msv);	  
	    
	  }
	  
	  @Override
	  public boolean onCreateOptionsMenu(Menu menu) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.menu_options, menu);
			return true;
		}
	  
	  @Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			case R.id.Clear:
					msv.clear();
				break;
			
			}
			return true;
		}
	  
	  
}
