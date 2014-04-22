package milk.ui;

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import milk.implement.Adaptor;
import milk.ui2.SmsListener;

public class MilkSms implements Runnable {

	private SmsListener listener;
	private String smsAddr;
	private String smsContent;

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

		boolean success = sendMessage(smsAddr, smsContent);
		if (listener != null) {
			listener.sendSMSResult(success);
		} else {
			throw new NullPointerException("sms callBack=null");
		}
		isSending = false;
	}

	private boolean sendMessage(String addr, String content) {
		boolean success = false;
		MessageConnection a = null;
		try {
			String address = addr;
			if (!address.startsWith("sms://"))
				address = "sms://" + addr;
			System.out.println("send sms **************");
			System.out.println("address:" + address);
			System.out.println("content:" + content);
			System.out.println("**********************");
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
			Adaptor.exception(t);
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
