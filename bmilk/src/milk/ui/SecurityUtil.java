package milk.ui;

import java.io.ByteArrayOutputStream;
import java.util.Vector;

import milk.implement.Adaptor;
import milk.implement.MD5;
import net.rim.device.api.crypto.AESCBCEncryptorEngine;
import net.rim.device.api.crypto.AESEncryptorEngine;
import net.rim.device.api.crypto.AESKey;
import net.rim.device.api.crypto.BlockEncryptor;
import net.rim.device.api.crypto.InitializationVector;
import net.rim.device.api.io.Base64InputStream;
import net.rim.device.api.io.Base64OutputStream;
import net.rim.device.api.system.DeviceInfo;

public class SecurityUtil {

	static String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

	private static final String key = "K1ByTkczcWRQalFobzY2SEVOTFhCQT09LEduQTVYaEhFMG1LN25nTGRMMnhVTXUrdWhvdmN2R1QwdFQ1NE5vZDlnSzg9";

	public static String encryptAES(String content) {
		try {
			byte[] bytes = Base64InputStream.decode(key);
			byte[] clear = content.getBytes("UTF-8");
			String str = new String(bytes, "UTF-8");
			Vector strs = Adaptor.split(str, ",", 2);

			if (strs.size() == 2) {

				byte[] iv = Base64InputStream.decode(((String) strs
						.elementAt(0)));
				byte[] key = Base64InputStream.decode(((String) strs
						.elementAt(1)));

				AESKey aesKey = new AESKey(key, 0, 256);
				InitializationVector ivv = new InitializationVector(iv);
				// AESEncryptorEngine engine = new AESEncryptorEngine(aesKey);
				PKCS7FormatterEngine ciphor = new PKCS7FormatterEngine(
						new AESCBCEncryptorEngine(aesKey, 16, false, ivv));
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				BlockEncryptor encryptor = new BlockEncryptor(ciphor, output);
				encryptor.write(clear);
				encryptor.close();
				output.close();

				byte[] result = output.toByteArray();
				return Base64OutputStream.encodeAsString(result, 0,
						result.length, false, false);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// public static String encryptAES2(String content) {
	// try {
	// byte[] bytes = Base64.decode(key);
	// byte[] clear = content.getBytes("UTF-8");
	// String str = new String(bytes, "UTF-8");
	// Vector strs = Adaptor.split(str, ",", 2);
	//
	// if (strs.size() == 2) {
	//
	// byte[] iv = Base64.decode(((String) strs.elementAt(0)));
	// byte[] key = Base64.decode(((String) strs.elementAt(1)));
	//
	// RijndaelEngine aes = new RijndaelEngine(128);
	//
	// PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
	// new CBCBlockCipher(aes), new PKCS7Padding());
	// KeyParameter keyParam = new KeyParameter(key);
	// cipher.init(true, new ParametersWithIV(keyParam, iv));
	//
	// byte[] result = new byte[cipher.getOutputSize(clear.length)];
	// int len = cipher
	// .processBytes(clear, 0, clear.length, result, 0);
	// len += cipher.doFinal(result, len);
	// ByteArrayOutputStream bos = new ByteArrayOutputStream();
	// Base64.encode(result, 0, len, bos);
	// result = bos.toByteArray();
	//
	// StringBuffer sb = new StringBuffer();
	// for (int i = 0; i < result.length; i++) {
	// sb.append((char) result[i]);
	// }
	//
	// return sb.toString();
	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return null;
	// }

	public static String getUUid() {
		return new MD5()
				.getHashString(String.valueOf(DeviceInfo.getDeviceId()));
	}

}