package com.mozat.sv3.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PoFileUtil extends FileUtil {

	// private static final String fileStart = "#: FILE:";
	// private static final String fileEnd = "#: ENDFILE";
	//
	// private static final String localizable = "localizable.strings";
	//
	// private static final String keyKey = "key";
	// private static final String valueKey = "value";
	// private static final String commentKey = "comment";
	// private static final String commentStart = "#.";

	@Override
	public void writeUnits(OutputStream out, Map<String, TransUnit> units)
			throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
		writer.write("msgid \"\"\n");
		writer.write("msgstr \"\"\n");
		writer.write("\"Content-Type: text/plain; charset=UTF-8\\n\"\n");
		writer.write("\"Content-Transfer-Encoding: 8bit\\n\"\n");
		writer.write("\n");
		// buffer.write(fileStart + fc.fileName + "\n");
		for (TransUnit unit : units.values()) {
			String key = unit.getPkey();
			String value = unit.getTransSafely();
			String comment = unit.getComment();
			if (comment != null && comment.length() > 0) {
				writer.write(comment);
				writer.write("\n");
			}
			writer.write("msgid \"" + FileUtil.escape(key) + "\"\n");
			writer.write("msgstr \"" + FileUtil.escape(value) + "\"\n");
			writer.write("\n");
		}
		writer.close();
	}

	@Override
	public LinkedHashMap<String, TransUnit> getUnits(byte[] bytes)
			throws IOException {
		List<String> lines = FileUtil.getLines(bytes, "UTF8");
		LinkedHashMap<String, TransUnit> units = new LinkedHashMap<String, TransUnit>();
		String msgId = null;
		String msgStr = null;
		String comment = null;
		boolean gotId = false;
		boolean gotStr = false;
		for (String line : lines) {
			if (line.startsWith("#")) {
				comment = line;
			} else if (line.startsWith("msgid")) {
				gotId = true;
				String rawMsgId = line.substring(5).trim();
				msgId = FileUtil.unescape(parseRawValue(rawMsgId));
			} else if (line.startsWith("msgstr")) {
				gotStr = true;
				String rawMsgStr = line.substring(6).trim();
				msgStr = FileUtil.unescape(parseRawValue(rawMsgStr));

			} else {
				// String temp = line.trim();
				// if (temp.length() > 2) {
				// temp = temp.substring(1, temp.length() - 1);
				// if (msgStr != null) {
				// msgStr += temp;
				// } else if (msgId != null) {
				// msgId += temp;
				// }
				// }
			}
			if (gotId && gotStr) {
				if (msgId != null && msgId.length() > 0 && msgStr != null) {
					TransUnit unit = FileUtil.generateUnit(msgId, msgStr,
							comment);
					units.put(msgId, unit);
				}
				gotId = false;
				gotStr = false;
				msgId = null;
				msgStr = null;
				comment = null;
			}
		}
		if (msgId != null && msgStr != null) {
			TransUnit unit = FileUtil.generateUnit(msgId, msgStr, comment);
			units.put(msgId, unit);
		}
		return units;
	}

	private String parseRawValue(String rawValue) {
		int firstQuote = rawValue.indexOf('"');
		int lastQuote = rawValue.lastIndexOf('"');
		if (firstQuote >= 0 && lastQuote > firstQuote) {
			return rawValue.substring(firstQuote + 1, lastQuote);
		} else {
			return null;
		}
	}

	// public static void main(String[] args) throws IOException {
	// File file = new File("/Users/luyx/Downloads/hb_resource_ar.po");
	// FileInputStream fis = new FileInputStream(file);
	// byte[] buffer = new byte[1000000];
	// int read = 0;
	// while (read < buffer.length) {
	// int r = fis.read(buffer, read, buffer.length - read);
	// if (r > 0) {
	// read += r;
	// } else {
	// break;
	// }
	// }
	// System.out.println(read);
	//
	// byte[] bytes = Arrays.copyOfRange(buffer, 0, read);
	//
	// PoFileUtil util = new PoFileUtil();
	// Map<String, TransUnit> result = util.getUnits(bytes);
	// FileOutputStream fos = new
	// FileOutputStream("/Users/luyx/Downloads/hb_resource_ar.out.po");
	// util.writeUnits(fos, result);
	// fos.close();
	//
	// }
}
