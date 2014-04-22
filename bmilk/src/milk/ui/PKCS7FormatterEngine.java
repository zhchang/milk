package milk.ui;

import net.rim.device.api.crypto.BlockEncryptorEngine;
import net.rim.device.api.crypto.BlockFormatterEngine;
import net.rim.device.api.crypto.CryptoTokenException;
import net.rim.device.api.crypto.MessageTooLongException;

public class PKCS7FormatterEngine implements BlockFormatterEngine {

	private BlockEncryptorEngine _encryptorEngine;

	public PKCS7FormatterEngine(BlockEncryptorEngine encryptorEngine) {
		_encryptorEngine = encryptorEngine;
	}

	public int formatAndEncrypt(byte[] input, int inputOffset, int inputLength,
			byte[] output, int outputOffset) throws MessageTooLongException,
			CryptoTokenException {
		return formatAndEncrypt(input, inputOffset, inputLength, output,
				outputOffset, false);
	}

	public int formatAndEncrypt(byte[] input, int inputOffset, int inputLength,
			byte[] output, int outputOffset, boolean lastBlock)
			throws MessageTooLongException, CryptoTokenException {
		if (lastBlock) {
			int padding = _encryptorEngine.getBlockLength()
					- (inputLength % _encryptorEngine.getBlockLength());
			int paddedInputLength = inputLength + padding;
			byte[] paddedInput = new byte[paddedInputLength];
			for (int i = 0; i < paddedInputLength; i++) {
				if (i < inputLength)
					paddedInput[i] = input[i];
				else
					paddedInput[i] = (byte) (padding);
			}
			inputLength = paddedInputLength;
			input = paddedInput;
		}
		_encryptorEngine.encrypt(input, inputOffset, output, outputOffset);
		return input.length - inputOffset;
	}

	public String getAlgorithm() {
		return _encryptorEngine.getAlgorithm() + "/PKCS7";
	}

	public int getInputBlockLength() {
		return _encryptorEngine.getBlockLength();
	}

	public int getOutputBlockLength() {
		return _encryptorEngine.getBlockLength();
	}

}