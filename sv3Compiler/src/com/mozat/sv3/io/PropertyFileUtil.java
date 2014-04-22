package com.mozat.sv3.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PropertyFileUtil extends FileUtil {

	private static final String COMMENT_START = "#";

	public static void processLine(LinkedHashMap<String, TransUnit> result,
			String line) {
		if (!line.startsWith(COMMENT_START)) {
			int index = line.indexOf('=');
			if (index >= 0) {
				String id = line.substring(0, index);
				String str = line.substring(index + 1);
				TransUnit unit = FileUtil.generateUnit(id, str, null);
				result.put(id, unit);
			}
		}
	}

	@Override
	public LinkedHashMap<String, TransUnit> getUnits(byte[] bytes)
			throws IOException {
		LinkedHashMap<String, TransUnit> result = new LinkedHashMap<String, TransUnit>();

		List<String> lines = FileUtil.getLines(bytes, "UTF-8");
		for (String line : lines) {
			PropertyFileUtil.processLine(result, line);
		}
		return result;
	}

	@Override
	public void writeUnits(OutputStream out, Map<String, TransUnit> units)
			throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");

		for (TransUnit unit : units.values()) {
			writer.write(unit.getPkey());
			writer.write("=");
			writer.write(unit.getTransSafely());
			writer.write("\r\n");
		}
		writer.close();
	}
}
