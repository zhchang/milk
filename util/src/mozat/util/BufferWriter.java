package mozat.util;

public class BufferWriter {

	StringBuilder builder;
	boolean finished = false;

	BufferWriter(StringBuilder builder) {
		this.builder = builder;
	}

	void write(String thing) {
		builder.append(thing);
	}

	void finish() {
		finished = true;
	}

	void waitFor() {
		if (finished) {
			return;
		} else {
			try {
				synchronized (this) {
					this.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
