package raptor.connector.ics.timeseal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.lang.StringUtils;

import raptor.connector.ics.IcsUtils;
import raptor.util.Logger;

public class TimesealSocketMessageProducer implements MessageProducer {
	private class CryptOutputStream extends OutputStream {
		private final byte buffer[] = new byte[10000];
		private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		private final OutputStream outputStream;
		private final byte timesealKey[] = "Timestamp (FICS) v1.0 - programmed by Henrik Gram.".getBytes();
		private final long initialTime = System.currentTimeMillis();

		public CryptOutputStream(OutputStream outputStream) {
			this.outputStream = outputStream;
		}

		@Override
		public void write(int i) throws IOException {
			if (i == '\n') {
				int resultLength = -1;
				synchronized (outputStream) {
					resultLength = crypt(
						byteArrayOutputStream.toByteArray(),
						System.currentTimeMillis() - initialTime);
					outputStream.write(buffer, 0, resultLength);
					outputStream.flush();
				}
				byteArrayOutputStream.reset();
				if (LOG.isDebugEnabled())
					LOG.debug("Wrote "+ resultLength+ " bytes");
			} else {
				byteArrayOutputStream.write(i);
				//System.err.print(i);
			}
		}

		private int crypt(byte stringToWriteBytes[], long timestamp) {
			//System.err.println("Writing " + stringToWriteBytes);
			int bytesInLength = stringToWriteBytes.length;
			System.arraycopy(stringToWriteBytes, 0, buffer, 0, stringToWriteBytes.length);
			buffer[bytesInLength++] = 24;
			byte abyte1[] = Long.toString(timestamp).getBytes();
			System.arraycopy(abyte1, 0, buffer, bytesInLength, abyte1.length);
			bytesInLength += abyte1.length;
			buffer[bytesInLength++] = 25;
			int j = bytesInLength;
			for (bytesInLength += 12 - bytesInLength % 12; j < bytesInLength;) {
				buffer[j++] = 49;
			}

			for (int k = 0; k < bytesInLength; k++) {
				buffer[k] |= 0x80;
			}

			for (int i1 = 0; i1 < bytesInLength; i1 += 12) {
				byte byte0 = buffer[i1 + 11];
				buffer[i1 + 11] = buffer[i1];
				buffer[i1] = byte0;
				byte0 = buffer[i1 + 9];
				buffer[i1 + 9] = buffer[i1 + 2];
				buffer[i1 + 2] = byte0;
				byte0 = buffer[i1 + 7];
				buffer[i1 + 7] = buffer[i1 + 4];
				buffer[i1 + 4] = byte0;
			}

			int l1 = 0;
			for (int j1 = 0; j1 < bytesInLength; j1++) {
				buffer[j1] ^= timesealKey[l1];
				l1 = (l1 + 1) % timesealKey.length;
			}

			for (int k1 = 0; k1 < bytesInLength; k1++) {
				buffer[k1] -= 32;
			}

			buffer[bytesInLength++] = -128;
			buffer[bytesInLength++] = '\n';
			return bytesInLength;
		}
	}

	private static final Logger LOG = Logger.getLogger(TimesealSocketMessageProducer.class);

	private CryptOutputStream cryptedOutputStream;

	private final String initialTimesealString;

	private final Socket socket;

	private MessageListener listener;

	protected StringBuilder inboundMessageBuffer = new StringBuilder(25000);

	protected boolean isTimesealOn;

	@Override
	public void send(String message) {
		try {
			getOutputStream().write(message.getBytes());
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	@Override
	public void close() {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException ioe) {
		}

		cryptedOutputStream = null;
		if (listener != null) {
			try {
				listener.connectionClosed(inboundMessageBuffer);
			} catch (Throwable t) {
			}
			listener = null;
			inboundMessageBuffer = null;
		}
	}

	public TimesealSocketMessageProducer(String address, int port,
		String initialTimestampString, boolean isTimesealOn,
		MessageListener listener) throws IOException {
		this.isTimesealOn = isTimesealOn;
		this.listener = listener;
		this.socket = new Socket(address, port);
		this.initialTimesealString = initialTimestampString;
		init();
	}

	/**
	 * Responsible for sending timeseal acks.
	 * 
	 * @param text  ICS input for which timeseal acks may be required
	 * @return
	 * @throws IOException
	 */
	protected String handleTimeseal(String text) throws IOException {
		//text.replace("[G]\0", "") TIMESEAL 2.
		
		//Send an ack for each \n\r[G]\n\r received.
		String beforeReplace = text;
		String afterReplace  = StringUtils.replaceOnce(beforeReplace,"\n\r[G]\n\r", "\n\r");

		while (!StringUtils.equals(beforeReplace,afterReplace)) {
			sendAck();
			beforeReplace = afterReplace;
			afterReplace = StringUtils.replaceOnce(beforeReplace,"\n\r[G]\n\r", "\n\r");
		}
		return afterReplace;
	}

	/**
	 * The messageLoop. Reads the inputChannel and then invokes publishInput
	 * with the text read. Should really never be invoked.
	 */
	protected void messageLoop() {
		try {
			if (!socket.isConnected()) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Not connected disconnecting.");
                                }
				return;
			}
			byte[] buffer = new byte[40000];
			while (!socket.isClosed()) {
				//long start = System.currentTimeMillis();
				int numRead = socket.getInputStream().read(buffer);
				if (numRead > 0) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Read " + numRead + " bytes.");
					}
					//System.err.println("Raw in: " + new String(buffer, 0, numRead));

					String text = isTimesealOn ?
							handleTimeseal(new String(buffer, 0, numRead)) :
							new String(buffer, 0, numRead);

					if (StringUtils.isNotBlank(text)) {
						inboundMessageBuffer.append(IcsUtils.cleanupMessage(text));
						listener.messageArrived(inboundMessageBuffer);
					}
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Read 0 bytes disconnecting.");
					}
					close();
					break;
				}
				//System.err.println("Processed message in " + (System.currentTimeMillis() - start));
			}
		} catch (IOException e) {
			LOG.warn(
				"Connector IOException occured in messageLoop (Connection closed or lost)", e);
		} catch (Throwable t) {
			listener.onError(
				"Connector Error in DaemonRun Thowable", t);
			close();
		} finally {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Leaving readInput");
			}
		}
	}

	private OutputStream getOutputStream() throws IOException {
		return isTimesealOn ? cryptedOutputStream : socket.getOutputStream();
	}

	// OpenSeal (TimeSeal 1?) ACK sequence consisting of \002 9 \n
	private String ACK = "\0029\n";
	private void sendAck() throws IOException {
		// Not sure if this needs to be encrypted or not
		getOutputStream().write(ACK.getBytes());
	}

	private void init() throws IOException {
		if (isTimesealOn) {
			cryptedOutputStream = new CryptOutputStream(socket.getOutputStream());
		}

		// BICS can't handle speedy connections so this slows it down a bit.
		try {
			Thread.sleep(100);
		} catch (InterruptedException ie) {
		}

		if (isTimesealOn) {
			OutputStream outputStream = getOutputStream();
			synchronized (socket) {
				outputStream.write(initialTimesealString.getBytes());
				outputStream.write('\n');
			}
		}

		Thread daemonThread = new Thread(new Runnable() {
			@Override
			public void run() {
				messageLoop();
			}
		});
		daemonThread.setDaemon(true);
		daemonThread.setName("TimesealSocketMessageProducer Thread");
		daemonThread.setPriority(Thread.MAX_PRIORITY);
		daemonThread.start();
	}
}
