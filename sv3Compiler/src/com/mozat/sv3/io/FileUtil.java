package com.mozat.sv3.io;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class FileUtil {
	public static List<String> getLines(byte[] bytes, String encoding)
			throws IOException {
		List<String> lines = new ArrayList<String>();

		ByteArrayInputStream stream = null;
		if (bytes.length >= 3) {
			byte[] mark = { (byte) 0xef, (byte) 0xbb, (byte) 0xbf };
			if (bytes[0] != mark[0] || bytes[1] != mark[1]
					|| bytes[2] != mark[2]) {
				stream = new ByteArrayInputStream(bytes);
			} else {
				stream = new ByteArrayInputStream(bytes, 3, bytes.length - 3);
			}
		} else {
			stream = new ByteArrayInputStream(bytes);
		}

		Charset charset = Charset.forName(encoding);
		InputStreamReader reader = new InputStreamReader(stream, charset);
		LineNumberReader lreader = new LineNumberReader(reader);
		String line = lreader.readLine();
		while (line != null) {
			lines.add(line);
			line = lreader.readLine();
		}
		lreader.close();
		reader.close();
		return lines;
	}

	public abstract LinkedHashMap<String, TransUnit> getUnits(byte[] bytes)
			throws IOException;

	public abstract void writeUnits(OutputStream out,
			Map<String, TransUnit> units) throws IOException;

	public static TransUnit generateUnit(String id, String str, String comment) {
		TransUnit unit = new TransUnit();
		unit.setPkey(id);
		unit.setComment(comment);
		if (isTranslation(str)) {
			unit.setTrans(str);
		} else {
			unit.setText(str);
		}
		return unit;
	}

	private static boolean isTranslation(String str) {
		return false;
	}

	public static String escape(String input) {
		return input.replace("\"", "\\\"");
	}

	public static String unescape(String input) {
		return input.replace("\\\"", "\"");
	}

	public static byte[] readAllBytes(String infile) {
		try {
			FileInputStream fis = new FileInputStream(infile);
			return readAllBytes(fis);
		} catch (IOException e) {
			return null;
		}
	}

	public static byte[] readAllBytes(InputStream in) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int b;
		while ((b = in.read()) != -1) {
			baos.write(b);
		}
		return baos.toByteArray();
	}

	public static String readAll(String path) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(path), "UTF-8"));
		return readAll(in);
	}

	public static String readAll(BufferedReader in) throws IOException {
		StringBuffer all = new StringBuffer();
		for (String line = in.readLine(); line != null; line = in.readLine()) {
			all.append(line).append('\n');
		}
		in.close();
		return all.toString();
	}

	public static String readAll(URL url) throws IOException {
		return readAll(url.getFile());
	}

	public static void writeAll(String path, byte[] data) throws IOException {
		FileOutputStream out = new FileOutputStream(path);
		out.write(data);
		out.flush();
		out.close();
	}

}
