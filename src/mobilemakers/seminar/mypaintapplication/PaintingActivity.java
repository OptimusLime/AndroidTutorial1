package mobilemakers.seminar.mypaintapplication;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class PaintingActivity extends Activity {

	private MySurfaceView msv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		msv = new MySurfaceView(this);
		this.setContentView(msv);
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
