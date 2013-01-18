package mobilemakers.seminar.mypaintapplication;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;

/**
 * The thread that runs the menu screen.
 * 
 * @author Daniel Wasserman
 * 
 */
public class MySurfaceThread extends Thread {
	private SurfaceHolder sholder;
	private int backGroundColor = Color.BLACK;
	private boolean running, paused, started;
	private Context context;
	private Paint p; // added
	private Queue<Float> xQueue; // added
	private Queue<Float> yQueue; // added
	private Bitmap bitmap;
	float lastX, lastY;
	float[] hsv = new float[] { 0, 1, 1 };
	private boolean useRainbow;

	/**
	 * @param c
	 *          The context this thread is in.
	 * 
	 *          The main thread to run the Menu Screen.
	 */
	public MySurfaceThread(Context c) {
		this.context = c;
		started = false;
		paused = true;
		running = false;
		p = new Paint();
		xQueue = new ConcurrentLinkedQueue();
		yQueue = new ConcurrentLinkedQueue();
		p.setColor(Color.CYAN);
		lastX = -1;
		lastY = -1;
		useRainbow = true;
	}

	/**
	 * @param e
	 *          MotionEvent for the "down" event.
	 */
	public void onDown(float x, float y) {
		if (lastX != -1) {
			float dy = (y - lastY) / 15;
			float dx = (x - lastX) / 15;
			for (int i = 0; i < 15; i++) {
				this.xQueue.add(lastX + dx * i);
				this.yQueue.add(lastY + dy * i);
			}
		} else {
			this.xQueue.add(x);
			this.yQueue.add(y);
		}

		lastX = x;
		lastY = y;
	}

	/**
	 * 
	 * @param h
	 *          The surface holder for the thread
	 * 
	 *          The surface holder allows the program to get the canvas to draw
	 *          things.
	 */
	public void setSurfaceHolder(SurfaceHolder h) {
		this.sholder = h;
		Rect frame = sholder.getSurfaceFrame();
		int width = frame.width();
		int height = frame.height();
		bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	}

	/**
	 * @param r
	 *          Set whether the program should start running or stop.
	 */
	public void setRunning(boolean r) {
		running = r;
	}

	/**
	 * @param p
	 *          Whether or not to pause the thread
	 */
	public void setPaused(boolean p) {
		paused = p;
	}

	/**
	 * Function to start the thread running.
	 */
	@Override
	public void start() {
		if (!started) {
			started = true;
			setPaused(false);
			setRunning(true);
			super.start();
		}
	}

	/**
	 * Pretty self explanatory.
	 */
	@Override
	public void run() {
		while (running) {
			if (!paused) {
				Canvas c = null;
				try {
					c = sholder.lockCanvas();
					if (c != null) {
						synchronized (sholder) {
							doDraw(c);
						}
					}
				} finally {
					if (c != null)
						sholder.unlockCanvasAndPost(c);
				}
				update();
			}
		}
	}

	public void update() {
		Canvas c = new Canvas(bitmap);
		int color = -1;
		if (useRainbow) {
			color = Color.HSVToColor(hsv);
			p.setColor(color);
		}
		while (!xQueue.isEmpty() && !yQueue.isEmpty()) {
			float x = xQueue.poll();
			float y = yQueue.poll();
			c.drawCircle(x, y, 5, p);
			if (useRainbow) {
				p.setColor(color);
				color = Color.HSVToColor(hsv);
				hsv[0]++;
				if (hsv[0] >= 360)
					hsv[0] = 0;
			}
		}
	}

	public void doDraw(Canvas c) {
		c.drawBitmap(bitmap, 0, 0, p);
	}

	public void clear() {
		Canvas c = new Canvas(bitmap);
		c.drawColor(Color.BLACK);
	}

	public void setColor(int color) {
		if (color == -1) {
			useRainbow = true;
		} else {
			useRainbow = false;
			p.setColor(color);
		}
	}
}
