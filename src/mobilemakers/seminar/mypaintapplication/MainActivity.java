package mobilemakers.seminar.mypaintapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_main);
  }
  
  public void startPaint(View v) {
	  Intent openActivity = new Intent(this,PaintingActivity.class);
      this.startActivity(openActivity);
  }
}
