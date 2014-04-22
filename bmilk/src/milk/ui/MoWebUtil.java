package milk.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.HttpConnection;

import milk.implement.Adaptor;
import milk.implement.MilkOutputStream;
import milk.implement.TaskRunner;
import milk.net.OutMoWebMessage;
import milk.ui2.RawRequest;

public class MoWebUtil {

	public static class MoWebTaskRunner implements TaskRunner {

		RawRequest request;

		public MoWebTaskRunner(RawRequest request) {
			this.request = request;
		}

		public void doTask() {
			if (request != null && request.listener != null) {
				request.listener.onComplete(null, request);
			}
		}

	}

	public static void sendMessage(byte[] bytes, String url, RawRequest request) {
		new Thread(new SendTask(url, bytes, request)).start();
	}

	public static void sendRawRequest(RawRequest request) {
		if (request.moagentWap) {
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				MilkOutputStream dos = new MilkOutputStream(bos);
				dos.writeByte(11);
				Adaptor.writeShortStr(dos, request.url);
				Adaptor.writeIntStr(dos, request.value);
				OutMoWebMessage msg = new OutMoWebMessage(
						Adaptor.getInstance().browserServiceId, "test",
						bos.toByteArray());
				sendMessage(msg.toBytes(), Adaptor.getInstance().moagentWap,
						request);

			} catch (Exception t) {
				Adaptor.exception(t);
			}
		} else {
			try {
				if (request.value != null && request.value.length() > 0) {
					sendMessage(request.value.getBytes("UTF-8"), request.url,
							request);
				} else {
					sendMessage(null, request.url, request);
				}
			} catch (Exception e) {
				MilkTaskImpl task = new MilkTaskImpl(new MoWebTaskRunner(
						request));
				// MilkApp.milk.timer.schedule(new TimerTask() {
				//
				// public void run() {
				//
				// }
				// }, 100);
				Adaptor.milk.scheduleTask(task, 100);
			}
		}
	}

	private static class SendTask implements Runnable {

		private String url;
		private byte[] bytes;
		private RawRequest request;

		SendTask(String url, byte[] bytes, RawRequest request) {
			this.url = url;
			this.bytes = bytes;
			this.request = request;
		}

		public void run() {
			HttpConnection c = null;
			InputStream is = null;
			int rc;
			byte[] result = null;
			try {
				c = ((MilkAppImpl) Adaptor.milk).getHttpConnection(url);
				c.setRequestMethod(HttpConnection.POST);
				c.setRequestProperty("Accept", "*/*");
				if (bytes != null) {
					c.setRequestProperty("Content-Length",
							String.valueOf(bytes.length));
				} else {
					c.setRequestProperty("Content-Length", "0");
				}
				c.setRequestProperty("User-Agent", "OA2/"
						+ Adaptor.getInstance().getVersion().toString());
				c.setRequestProperty("Connection", "Close");
				if (bytes != null) {
					OutputStream os = c.openOutputStream();
					os.write(bytes);
					os.flush();
				}
				rc = c.getResponseCode();
				Adaptor.infor("http response: " + rc);
				if (rc != HttpConnection.HTTP_OK) {
					throw new IOException("HTTP response code: " + rc);
				}

				is = c.openInputStream();

				int actual = 0;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] temp = new byte[1024];
				while (actual != -1) {
					actual = is.read(temp, 0, 1024);
					if (actual != -1) {
						bos.write(temp, 0, actual);
					}
				}
				result = bos.toByteArray();

			} catch (Exception e) {
				Adaptor.exception(e);
			} finally {
				try {
					if (is != null)
						is.close();
				} catch (Exception e) {
				}
				try {
					if (c != null)
						c.close();
				} catch (Exception e) {
				}
			}
			if (request != null && request.listener != null) {
				if (result == null) {
					request.listener.onComplete(null, request);
				} else {
					try {
						request.listener.onComplete(result, request);
					} catch (Exception e) {
						request.listener.onComplete(null, request);
					}
				}
			}
		}
	}

}
