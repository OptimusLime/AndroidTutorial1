package mobilemakers.seminar.mypaintapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AndroidActivity extends Activity {
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //we set our layout to be the file named activity_main
    this.setContentView(R.layout.activity_main);
  }
  
  /**
   * Function to open up the paint activity. 
   * @param v
   */
  public void startPaint(View v) {
	  
	  //Create our intent to launch the paint activity
	  Intent openActivity = new Intent(this,PaintingActivity.class);
      this.startActivity(openActivity);
  }
}
