package com.mozat.sv3.io;

import java.io.UnsupportedEncodingException;

public enum FileFormat {
	PlainTextFormat, PoeditFormat;

	public static FileFormat detectFormat(byte[] bytes) {
		if (bytes != null && bytes.length > 0) {
			String content;
			try {
				if (bytes.length > 10000) {
					content = new String(bytes, 0, 10000, "UTF-8"); // use only
																	// 10000
																	// characters
																	// for
																	// efficiency
				} else {
					content = new String(bytes, "UTF-8"); // use only 10000
															// characters for
															// efficiency
				}
				if (content.contains("msgid") && content.contains("msgstr")) {
					return PoeditFormat;
				} else {
					return PlainTextFormat;
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return PlainTextFormat;
	}

	public FileUtil getFileUtil() {
		if (this == PoeditFormat) {
			return new PoFileUtil();
		} else {
			return new PropertyFileUtil();
		}
	}
}
