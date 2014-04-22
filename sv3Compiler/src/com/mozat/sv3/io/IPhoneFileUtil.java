package com.mozat.sv3.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPhoneFileUtil extends FileUtil {

	@Override
	public LinkedHashMap<String, TransUnit> getUnits(byte[] bytes)
			throws IOException {
		LinkedHashMap<String, TransUnit> result = new LinkedHashMap<String, TransUnit>();

		List<String> lines = FileUtil.getLines(bytes, "UTF-16");
		String lastComment = null;
		Pattern p = Pattern.compile("\\s*\"(.*)\"\\s*=\\s*\"(.*)\";\\s*");
		for (String line : lines) {
			line = line.trim();
			if (line.startsWith("/*")) {
				lastComment = line;
			} else {
				Matcher m = p.matcher(line);
				if (m.matches()) {
					String id = FileUtil.unescape(m.group(1));
					String str = FileUtil.unescape(m.group(2));

					TransUnit unit = FileUtil
							.generateUnit(id, str, lastComment);
					lastComment = null;
					result.put(id, unit);
				}
			}
		}
		return result;
	}

	@Override
	public void writeUnits(OutputStream out, Map<String, TransUnit> units)
			throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-16");
		for (TransUnit unit : units.values()) {
			if (unit.getComment() != null) {
				writer.write(unit.getComment());
				writer.write("\n");
			}
			writer.write("\"");
			writer.write(FileUtil.escape(unit.getPkey()));
			writer.write("\" = \"");
			String trans = unit.getTransSafely();
			writer.write(FileUtil.escape(trans));
			writer.write("\";");
			writer.write("\n\n");
		}
		writer.close();
	}

}
