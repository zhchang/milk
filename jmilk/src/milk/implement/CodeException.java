package milk.implement;

public class CodeException extends Exception {

	int error;

	CodeException(int error) {
		this.error = error;
	}
}
