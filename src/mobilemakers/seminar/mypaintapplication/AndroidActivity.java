package mobilemakers.seminar.mypaintapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AndroidActivity extends Activity {
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //we set our layout to be the file named activity_main
    this.setContentView(R.layout.activity_main);
  }
  
  /**
   * Function to change our text!. 
   * @param v
   */
  public void pingButton(View v) {
	  
	  TextView tvHello = (TextView) findViewById(R.id.tvHello);
	  tvHello.setText("Pong");
  
  }
}
