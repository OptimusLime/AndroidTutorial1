package mobilemakers.seminar.mypaintapplication;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class PaintingActivity extends Activity {
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    //we set our layout to be the file named activity_main
	    TextView tempTV = new TextView(this);
	    
	    tempTV.setText("Started empty activity");
	    tempTV.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	    
	    this.setContentView(tempTV);	    
	  }
}
