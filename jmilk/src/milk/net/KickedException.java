package milk.net;

import java.io.IOException;

public class KickedException extends IOException {
	public int error = 0;

	public KickedException(int error) {
		this.error = error;
	}
}
