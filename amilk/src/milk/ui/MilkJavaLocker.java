package milk.ui;

import milk.ui2.MilkLocker;

public class MilkJavaLocker implements MilkLocker {

	public synchronized void lock() {
		try {
			wait();
		} catch (Exception e) {
		}

	}

	public synchronized void unlock() {
		this.notifyAll();

	}

}
