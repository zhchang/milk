package milk.ui;

import java.util.TimerTask;

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import milk.implement.Adaptor;
import milk.ui2.SmsListener;

public class MilkSms implements Runnable {

	private SmsListener listener;
	private String smsAddr;
	private String smsContent;
	private TimerTask smsTimeout;
	private boolean smsSent = false;

	private boolean isSending = false;

	public MilkSms(String addr, String content, SmsListener listener) {
		smsAddr = addr;
		smsContent = content;
		this.listener = listener;
	}

	public void sendShortMessage() {
		if (!isSending) {
			new Thread(this).start();
			isSending = true;
		}
	}

	public void run() {
		if (smsTimeout != null) {
			smsTimeout.cancel();
		}
		smsTimeout = new TimerTask() {
			public void run() {
				if (!smsSent) {
					listener.sendSMSResult(true);
					MilkSms.this.smsTimeout = null;
					smsSent = true;
				}
			}
		};

		((MilkAppImpl) Adaptor.milk).timer.schedule(smsTimeout, 10000);
		boolean success = sendMessage(smsAddr, smsContent);
		if (listener != null && !smsSent) {
			listener.sendSMSResult(success);
			smsSent = true;
		}
		if (smsTimeout != null) {
			smsTimeout.cancel();
			smsTimeout = null;
		}

		// #mdebug info
		else {
			throw new NullPointerException("sms callBack=null");
		}
		// #enddebug
		isSending = false;
	}

	private boolean sendMessage(String addr, String content) {
		boolean success = false;
		MessageConnection a = null;
		try {
			String address = addr;
			if (!address.startsWith("sms://"))
				address = "sms://" + addr;
			// #mdebug info
			System.out.println("send sms **************");
			System.out.println("address:" + address);
			System.out.println("content:" + content);
			System.out.println("**********************");
			// #enddebug
			a = (MessageConnection) Connector.open(address);
			// if (a.newMessage("text") == null) {
			// sendMessage(addr, content);
			// }
			TextMessage txtmessage = (TextMessage) a
					.newMessage(MessageConnection.TEXT_MESSAGE);
			txtmessage.setAddress(address);
			txtmessage.setPayloadText(content);

			a.send(txtmessage);

			success = true;
		} catch (Exception t) {
			success = false;
			// #debug info
			t.printStackTrace();
		}
		if (a != null) {
			try {
				a.close();
			} catch (Exception ioe) {
			}
		}
		return success;
	}

}
