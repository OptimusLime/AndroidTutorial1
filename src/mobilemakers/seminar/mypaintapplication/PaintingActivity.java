package mobilemakers.seminar.mypaintapplication;

import mobilemakers.seminar.mypaintapplication.SocketTask.SocketReadHandler;
import mobilemakers.seminar.mypaintapplication.SocketTask.SocketWriteHandler;
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
import android.widget.Toast;

public class PaintingActivity extends Activity implements SocketReadHandler, SocketWriteHandler{
	
	private MySurfaceView msv;
	private PaintSocketManager socketManager;
	
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    //we set our layout to be the file named activity_main
	    msv = new MySurfaceView(this);	    
	    msv.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	   
	    //set our view as being our custom surface view object
	    this.setContentView(msv);
	    
	    //initialize our socket manager, informing it that we'll be the read callback object
	    socketManager = new PaintSocketManager(this);	  
	    
	    //we make sure to let our surfaceview know about the socket manager for sending messages
	    msv.setSocketManager(socketManager);
	    
	  }
	  
	  public boolean handleSocketRead(final byte[] msg)
	  {		  
		  //we want to make sure we modify our UI objects on the UI thread, which means running the read server message
		  //on the write thread
		  runOnUiThread(new Runnable() {
				@Override
				public void run() {
					msv.readServerMessage(msg);
				}
			});
			return false;
	  }
	  
	  //just lets us known when we've successfully written our info to the server
	  public boolean handleSocketWrite() {
		  //we should toast on the correct UI thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getApplicationContext(), "Finished socket write", Toast.LENGTH_SHORT).show();
				}
			});
			return false;
		}
	  
	  @Override 
	  public void onStart(){
		  //call the super function to do normal android OS stuff
		  super.onStart();
		  
		  //we need to initialize our socketManager, and start connecting to the server
		  socketManager.connectToServer();
	    	
	  }
	  
	  
	  //Menu Creation
	  
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
