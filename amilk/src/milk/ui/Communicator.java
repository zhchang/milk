package milk.ui;

import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

import milk.implement.Adaptor;
import milk.implement.MilkInputStream;
import milk.implement.MilkOutputStream;
import milk.net.CommListener;
import milk.net.InDataEventMessage;
import milk.net.InLoginMessage;
import milk.net.InMessage;
import milk.net.MoPacket;
import milk.net.OutGameMessage;
import milk.net.OutHeartMessage;
import milk.net.OutLoginMessage;
import milk.net.OutMessage;
import milk.ui.model.MessagePool;

public class Communicator {

	public static final int STATE_IDLE = 0;
	public static final int STATE_CONNECTING = 1;
	public static final int STATE_ERROR = 2;
	public static final int STATE_CONNECTED = 3;

	private final Vector outQueue = new Vector();
	private final Vector inQueue = new Vector();

	ReceiveMessageTask reader = null;
	Thread readerThread = null;
	Thread senderThread = null;

	public int state = STATE_IDLE;

	private static Communicator instance;
	private CommListener listener;

	public String serverUrl;

	private final static int HEART_INTERVAL = 60 * 1000;

	private long lastReceiveMsgTime;
	private boolean netException;

	private boolean processInMessageInBackgroundThread = true;

	private Communicator() {
	}

	public static synchronized Communicator getInstance() {
		if (instance == null) {
			instance = new Communicator();
		}
		return instance;
	}

	public void setProcessInMessageInBackgroundThread(
			boolean processInBackgroundThread) {
		// this.processInMessageInBackgroundThread = processInBackgroundThread;
	}

	public void setCommListener(CommListener listener) {
		if (listener != null) {
			this.listener = listener;

		}
	}

	public void send(MoPacket packet) {
		synchronized (outQueue) {
			outQueue.addElement(packet);
			outQueue.notifyAll();
		}
	}

	public void handShake() {
		// setProcessInMessageInBackgroundThread(false);
		if (reader != null) {
			reader.kill();
		}
		reader = new ReceiveMessageTask();
		if (readerThread != null) {
			readerThread.interrupt();
		}

		readerThread = new Thread(reader);
		readerThread.start();

	}

	private class SendMessageTask implements Runnable {
		Socket conn;
		MilkOutputStream dos;
		private boolean killed = false;

		SendMessageTask(Socket conn, MilkOutputStream dos) {
			this.conn = conn;
			this.dos = dos;
		}

		public void run() {

			try {
				while (!killed) {
					synchronized (outQueue) {
						if (outQueue.size() == 0) {
							outQueue.addElement(MessagePool
									.getOutHeartMessage());
						}
					}

					while (outQueue.size() > 0) {
						OutMessage msg = null;
						synchronized (outQueue) {
							msg = (OutMessage) outQueue.elementAt(0);
							outQueue.removeElementAt(0);
						}
						dos.write(msg.toBytes());
						dos.flush();
						if (msg instanceof OutHeartMessage) {
							MessagePool.recovery((OutHeartMessage) msg);
						} else if (msg instanceof OutGameMessage) {
							MessagePool.recovery((OutGameMessage) msg);
						}
					}
					synchronized (outQueue) {
						try {
							outQueue.wait(HEART_INTERVAL);
						} catch (Exception e) {
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				cleanAll(conn, null, dos);
			}

		}

		public void kill() {
			killed = true;
			synchronized (outQueue) {
				outQueue.notifyAll();
			}

		}

	}

	private class ReceiveMessageTask implements Runnable {

		ReceiveMessageTask() {
		}

		Socket conn = null;
		MilkOutputStream dos = null;
		MilkInputStream dis = null;
		SendMessageTask sender = null;

		private boolean killed = false;
		int reconnectCount = 0;
		final int retryInterval = 500;

		public void run() {

			while (!killed) {

				try {

					int sleepTime = reconnectCount * retryInterval;
					if (sleepTime > 30000) {
						sleepTime = 30000;
					}
					try {
						Thread.sleep(sleepTime);
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						conn = ((MilkAppImpl) Adaptor.milk).getConnection();
						dos = new MilkOutputStream(conn.getOutputStream());
						dis = new MilkInputStream(conn.getInputStream());
						String passWord = Adaptor.getInstance().password;
						String userName = Adaptor.getInstance().username;
						if (!userName
								.endsWith(Adaptor.getInstance().userDomain)) {
							userName += Adaptor.getInstance().userDomain;
						}
						OutLoginMessage login = new OutLoginMessage(userName,
								passWord);
						dos.write(login.toBytes());
						dos.flush();
						long last = System.currentTimeMillis();

						InMessage packet = InMessage.parse(dis);
						lastReceiveMsgTime = System.currentTimeMillis();
						Adaptor.getInstance().loginDuration = lastReceiveMsgTime
								- last;

						if (packet instanceof InLoginMessage) {
							InLoginMessage loginMessage = (InLoginMessage) packet;
							if (loginMessage.result == 0) {
								reconnectCount = 0;
								// setProcessInMessageInBackgroundThread(false);
								sender = new SendMessageTask(conn, dos);
								senderThread = new Thread(sender);
								senderThread.start();
								Adaptor.getInstance().loginSuccess();

							} else {// notify and login again.
								Adaptor.getInstance().loginFailure(true);
								break;

							}
						} else {
							throw new IOException("Invalid login response");
						}
						lastReceiveMsgTime = System.currentTimeMillis();

					} catch (Exception t) {
						throw t;
					}

					while (true) {
						InMessage packet = InMessage.parse(dis);
						if (packet != null) {
							if (processInMessageInBackgroundThread) {
								if (listener != null) {
									listener.process(packet);
								}
							} else {
								synchronized (inQueue) {
									inQueue.addElement(packet);
								}
							}
							if (packet instanceof InDataEventMessage) {
								MessagePool
										.recovery((InDataEventMessage) packet);
							}

						}
						lastReceiveMsgTime = System.currentTimeMillis();
					}

				} catch (Exception e) {
					e.printStackTrace();
					reconnectCount++;

				} finally {
					cleanAll(conn, dis, dos);
					if (sender != null) {
						sender.kill();
						sender = null;
						if (senderThread != null) {
							senderThread.interrupt();
							senderThread = null;
						}
					}
				}
			}
		}

		public void kill() {
			killed = true;
			cleanAll(conn, dis, dos);
			if (sender != null) {
				sender.kill();
				sender = null;
				if (senderThread != null) {
					senderThread.interrupt();
					senderThread = null;
				}
			}

		}
	}

	private void cleanAll(Socket conn, MilkInputStream dis, MilkOutputStream dos) {
		if (dos != null) {
			try {
				dos.close();
				dos = null;
			} catch (Exception e) {
				// ignore
			}
		}
		if (dis != null) {
			try {
				dis.close();
				dis = null;
			} catch (Exception e) {
				// ignore
			}
		}
		if (conn != null) {
			try {
				conn.close();
				conn = null;
			} catch (Exception e) {
				// ignore
			}
		}
	}

	public void processInMessage() {
		synchronized (inQueue) {
			int maxNum = 3;
			while (inQueue.size() > 0 && maxNum-- > 0) {
				InMessage msg = (InMessage) inQueue.elementAt(0);
				inQueue.removeElementAt(0);
				if (listener != null) {
					long start = System.currentTimeMillis();
					listener.process(msg);
					Adaptor.debug("msg [" + msg.getClass() + "] process in ["
							+ (System.currentTimeMillis() - start) + "]ms");
				}
			}
		}
	}

	public void processDisconnection() {

	}

	public void stop() {
		if (reader != null) {
			reader.kill();
			reader = null;
		}
	}

	// public void doTask() {
	// try {
	//
	// state = STATE_CONNECTING;
	// conn = ((MilkAppImpl) Adaptor.milk).getConnection();
	//
	// dos = new MilkOutputStream(conn.openDataOutputStream());
	// dis = new MilkInputStream(conn.openDataInputStream());
	// String passWord = Adaptor.getInstance().password;
	// String userName = Adaptor.getInstance().username;
	// if (!userName.endsWith(Adaptor.getInstance().userDomain)) {
	// userName += Adaptor.getInstance().userDomain;
	// }
	// OutLoginMessage login = new OutLoginMessage(userName, passWord);
	// dos.write(login.toBytes());
	// dos.flush();
	// long last = System.currentTimeMillis();
	//
	// InMessage packet = InMessage.parse(dis);
	// lastReceiveMsgTime = System.currentTimeMillis();
	// Adaptor.getInstance().loginDuration = lastReceiveMsgTime - last;
	//
	// if (packet instanceof InLoginMessage) {
	// InLoginMessage loginMessage = (InLoginMessage) packet;
	// if (loginMessage.result == 0) {
	// Communicator.getInstance().loginSuccess();
	// Adaptor.getInstance().queryIsSendReport();
	// Adaptor.getInstance().loginSuccess();
	// } else {// notify and login again.
	// Communicator.getInstance().loginFailure();
	// Adaptor.getInstance().loginFailure(true);
	// }
	// } else {
	// throw new IOException("Invalid login response");
	// }
	// lastReceiveMsgTime = System.currentTimeMillis();
	// } catch (Exception t) {
	// Adaptor.exception(t);
	// Communicator.getInstance().loginFailure();
	// Adaptor.getInstance().loginFailure(false);
	// this.reconnectImmediately(t.getMessage());
	// }
	//
	// }

}
