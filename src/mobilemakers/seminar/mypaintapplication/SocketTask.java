package mobilemakers.seminar.mypaintapplication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;

import android.os.Build;
import android.util.Log;

public class SocketTask implements Runnable {

	static final String TAG = "SocketTask";

	public class ChangeRequest {
		public static final int REGISTER = 1;
		public static final int CHANGEOPS = 2;
		public static final int DISCONNECT = 3;

		public int type;
		public int ops;

		public ChangeRequest(int type, int ops) {
			this.type = type;
			this.ops = ops;
		}
	}

	public interface SocketReadHandler {
		public boolean handleSocketRead(byte[] rsp);
	}

	public interface SocketWriteHandler {
		public boolean handleSocketWrite();
	}

	public class PendingWrite {
		public ByteBuffer data;
		public SocketWriteHandler handler;

		public PendingWrite(ByteBuffer data, SocketWriteHandler handler) {
			this.data = data;
			this.handler = handler;
		}
	}

	// The host:port combination to connect to
	private InetAddress hostAddress;
	private int port;

	// The selector we'll be monitoring
	private Selector selector;

	// The buffer into which we'll read data when it's available
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

	// Our socket
	private SocketChannel socket;

	// A list of PendingChange instances
	private List<ChangeRequest> pendingChanges = new LinkedList<ChangeRequest>();

	// A list of ByteBuffer instances waiting to be written
	private List<PendingWrite> pendingData = new LinkedList<PendingWrite>();

	// Callback to invoke when data is read
	private SocketReadHandler readHandler;

	// The background thread which reads and writes to the socket
	private Thread thread;

	// Flag indicating if thread is running or not
	private boolean running = false;

	// Flag indicating that we started to connect
	private boolean connecting = false;

	// Flag indicating when connected or not
	private boolean connected = false;

	// Flag indicating that we started to disconnect
	private boolean disconnecting = false;

	public SocketTask(SocketReadHandler handler) {
		// Create a new selector
		try {
			
			//Paul- the emulator doesn't support IPv6, so you have to disable it, or 
			//the socket open request will fail, causing a null pointer exception later.
			//This is checking if you're using the emulator, then disabling IPv6
			//from: http://stackoverflow.com/questions/2879455/android-2-2-and-bad-address-family-on-socket-connect
			  if("google_sdk".equals( Build.PRODUCT ) || "sdk".equals( Build.PRODUCT )){
				  System.setProperty("java.net.preferIPv6Addresses", "false");
			  }
			
			this.selector = SelectorProvider.provider().openSelector();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Register the response handler
		this.readHandler = handler;
	}

	public void start() {
		// Start background thread
		if (!running) {
			thread = new Thread(this);
			running = true;
			thread.start();
		}
	}

	public void stop() {
		// Stop background thread
		if (running) {
			running = false;

			// Wakeup selector so loop can disconnect and terminate
			this.selector.wakeup();
		}
	}

	public void connect(InetAddress hostAddress, int port) {
		Log.i(TAG, "connect");

		// Ignore if already connected
		if (connecting || connected)
			return;

		this.hostAddress = hostAddress;
		this.port = port;

		try {
			// Start a new connection
			// Create a non-blocking socket channel
			socket = SocketChannel.open();
			socket.configureBlocking(false);

			// Kick off connection establishment
			socket.connect(new InetSocketAddress(this.hostAddress, this.port));
			connecting = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (connecting) {
			// Queue a channel registration since the caller is not the
			// selecting thread. As part of the registration we'll register
			// an interest in connection events. These are raised when a channel
			// is ready to complete connection establishment.
			synchronized (this.pendingChanges) {
				this.pendingChanges.add(new ChangeRequest(
						ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
			}

			// Wakeup the selector if we're already running
			this.selector.wakeup();
		}
	}

	public void disconnect() {
		if (disconnecting || !connected)
			return;
		disconnecting = true;
		// Add pending change to disconnect
		synchronized (this.pendingChanges) {
			this.pendingChanges.add(new ChangeRequest(ChangeRequest.DISCONNECT,
					0));
		}
		// And wakeup selector
		this.selector.wakeup();
	}

	public void send(byte[] data, SocketWriteHandler handler) {
		// Queue the data we want written, even if we're not yet connected
		synchronized (this.pendingData) {
			if (this.pendingData == null) {
				this.pendingData = new ArrayList<PendingWrite>();
			}
			this.pendingData.add(new PendingWrite(ByteBuffer.wrap(data),
					handler));
		}

		if (connected) {
			// Register interest in writing data
			synchronized (this.pendingChanges) {
				this.pendingChanges.add(new ChangeRequest(
						ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
			}

			// Finally, wake up our selecting thread so it can make the required
			// changes
			this.selector.wakeup();
		} // Otherwise shouldn't do a change request since still waiting for
			// connect request to complete
	}

	public void run() {
		while (running) {
			Log.i(TAG, "looping");
			try {
				// Process any pending changes
				synchronized (this.pendingChanges) {
					Iterator<ChangeRequest> changes = this.pendingChanges
							.iterator();
					while (changes.hasNext()) {
						Log.i(TAG, "pending changes");
						ChangeRequest change = changes.next();
						switch (change.type) {
						case ChangeRequest.CHANGEOPS:
							Log.i(TAG, "change ops");
							SelectionKey key = socket.keyFor(this.selector);
							if (key != null) {
								key.interestOps(change.ops);
							}
							break;
						case ChangeRequest.REGISTER:
							Log.i(TAG, "change register");
							socket.register(this.selector, change.ops);
							break;
						case ChangeRequest.DISCONNECT:
							Log.i(TAG, "change disconnect");
							close();
							disconnecting = false;
							break;
						}
					}
					this.pendingChanges.clear();
				}

				Log.i(TAG, "pre select");
				// Wait for an event on one of the registered channels
				this.selector.select();

				// If thread stopped then disconnect before terminating
				if (!running) {
					close();
				}

				Log.i(TAG, "post select");

				// Iterate over the set of keys for which events are available
				Iterator<SelectionKey> selectedKeys = this.selector
						.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					// Should only ever be one socket channel being selected
					assert this.socket == (SocketChannel) key.channel();

					if (!key.isValid()) {
						continue;
					}

					// Check what event is available and deal with it
					if (key.isConnectable()) {
						this.finishConnection(key);
					} else if (connected) {
						if (key.isReadable()) {
							this.read(key);
						} else if (key.isWritable()) {
							this.write(key);
						}
					} else {
						Log.i(TAG,
								"socket not yet connected but trying to read/write");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Log.i(TAG, "stopped looping");
	}

	private void read(SelectionKey key) throws IOException {
		Log.i(TAG, "reading");
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Clear out our read buffer so it's ready for new data
		this.readBuffer.clear();

		// Attempt to read off the channel
		int numRead;
		try {
			numRead = socketChannel.read(this.readBuffer);
		} catch (IOException e) {
			Log.i(TAG, "remote force closed");
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			close();
			return;
		}

		if (numRead == -1) {
			Log.i(TAG, "remote clean close");
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			close();
			return;
		}

		// Handle the response
		this.handleResponse(socketChannel, this.readBuffer.array(), numRead);
	}

	private void close() {
		// Ignore if not connected
		if (!connected)
			return;
		SelectionKey key = this.socket.keyFor(this.selector);
		if (key != null) {
			key.cancel();
		}
		try {
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		connected = false;
	}

	private void handleResponse(SocketChannel socketChannel, byte[] data,
			int numRead) throws IOException {
		Log.i(TAG, "handle response");
		// Make a correctly sized copy of the data before handing it
		// to the client
		byte[] rspData = new byte[numRead];
		System.arraycopy(data, 0, rspData, 0, numRead);

		// Pass the response to the handler
		if (this.readHandler.handleSocketRead(rspData)) {
			Log.i(TAG, "handler closed");
			// The handler has seen enough, close the connection
			close();
		}
	}

	private void write(SelectionKey key) throws IOException {
		Log.i(TAG, "writing");
		SocketChannel socketChannel = (SocketChannel) key.channel();

		synchronized (this.pendingData) {
			// Write until there's not more data ...
			while (!this.pendingData.isEmpty()) {
				PendingWrite pending = this.pendingData.get(0);
				socketChannel.write(pending.data);
				if (pending.data.remaining() > 0) {
					// ... or the socket's buffer fills up
					break;
				}
				this.pendingData.remove(0);

				// invoke write completion handler
				pending.handler.handleSocketWrite();
			}

			if (this.pendingData.isEmpty()) {
				// We wrote away all data, so we're no longer interested
				// in writing on this socket. Switch back to waiting for
				// data.
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}

	private void finishConnection(SelectionKey key) throws IOException {
		Log.i(TAG, "finishConnection");
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Finish the connection. If the connection operation failed
		// this will raise an IOException.
		try {
			socketChannel.finishConnect();
		} catch (IOException e) {
			// Cancel the channel's registration with our selector
			System.out.println(e);
			key.cancel();
			return;
		}

		// We're now connected
		connecting = false;
		connected = true;

		if (this.pendingData.isEmpty()) {
			// And no pending writes, so register an interest in reading on this
			// channel
			key.interestOps(SelectionKey.OP_READ);
		} else {
			// Otherwise start writing
			key.interestOps(SelectionKey.OP_WRITE);
		}
	}
}
