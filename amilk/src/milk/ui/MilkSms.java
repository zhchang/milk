package milk.ui;

import milk.ui2.SmsListener;
import android.telephony.SmsManager;


public class MilkSms implements Runnable {

	private SmsListener listener;
	private String smsAddr;
	private String smsContent;
	SmsManager sms = SmsManager.getDefault();
	
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
		
		try
		{
			String address = addr;
			if (!address.startsWith("sms://"))
				address = "sms://" + addr;
			// #mdebug info
			System.out.println("send sms **************");
			System.out.println("address:" + address);
			System.out.println("content:" + content);
			System.out.println("**********************");
			sms.sendTextMessage(address, null, content, null, null);
			
			success=true;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return success;
	}


}
