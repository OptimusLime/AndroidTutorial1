package mobilemakers.seminar.mypaintapplication;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import mobilemakers.seminar.mypaintapplication.SocketTask.SocketReadHandler;
import mobilemakers.seminar.mypaintapplication.SocketTask.SocketWriteHandler;

import android.util.Log;

public class PaintSocketManager {

	private SocketTask socketTask;
	private SocketWriteHandler writeHandler;
	
	public <T extends SocketReadHandler & SocketWriteHandler> PaintSocketManager(T readWriteHandler)
	{
		 //we need to create our sockettast, passing in a function that will handle reading an incoming message
	    //an displaying on the UI thread
	    
	    socketTask = new SocketTask(readWriteHandler);
	    writeHandler = readWriteHandler;
	}
	
	public void connectToServer()
	{
		 Log.d("paintSocket", "onStart");
			
		  //we need to start up our socket task, connecting to the appropriate address and port
		  try
		  {
			  int yourPortID = 10000;
			  socketTask.connect(InetAddress.getByName("192.168.1.100"), yourPortID);
			  socketTask.start();
		  }
		  catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public void doSendMessage(String message) {
			
		if (message.length() > 0) {
			// TODO: create JSON message and asynchronously send over socket
			Log.d("socketManager", "Sending message: " + message);

			try {
				//without much fanfare, we send our message to the server!
				socketTask.send(message.getBytes("UTF8"), writeHandler);
				
			}catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}
	}
}
