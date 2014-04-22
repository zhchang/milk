package smartview3.utils;

public class StringUtil {
	public static String[] split(String str, char separator) {
		int len = str.length();
		int sepCount = 0;
		for (int i = 0; i < len; ++i) {
			if (str.charAt(i) == separator) {
				++sepCount;
			}
		}
		String[] values = new String[sepCount + 1];
		int last = 0;
		int index = str.indexOf(separator);
		int i = 0;
		while (index >= 0) {
			String sub = str.substring(last, index);
			values[i] = sub;
			++i;
			last = index + 1;
			index = str.indexOf(separator, last);
		}
		String sub = str.substring(last);
		values[i] = sub;

		return values;
	}

	public static String[] splitWithEscaping(String str, char separator) {
		int len = str.length();
		int sepCount = 0;
		char lastC = 0;
		for (int i = 0; i < len; ++i) {
			char c = str.charAt(i);
			if (c == separator && lastC != '\\') {
				++sepCount;
			}
			lastC = c;
		}
		String[] values = new String[sepCount + 1];
		int last = 0;
		int index = str.indexOf(separator);
		int i = 0;
		String part = null;
		while (index >= 0) {
			if (index > 0 && str.charAt(index - 1) == '\\') {
				if (part == null) {
					part = str.substring(last, index - 1) + separator;
				} else {
					part += str.substring(last, index - 1) + separator;
				}
			} else {
				String sub = str.substring(last, index);
				sub = part == null ? sub : part + sub;
				part = null;
				values[i] = sub;
				++i;
			}
			last = index + 1;
			index = str.indexOf(separator, last);
		}
		String sub = str.substring(last);
		sub = part == null ? sub : part + sub;
		part = null;
		values[i] = sub;

		return values;
	}

	public static String joinWithEscaping(String[] strArray, char separator) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < strArray.length; ++i) {
			String s = strArray[i];
			if (s.indexOf(separator) >= 0) {
				s = join(split(s, separator), "\\" + separator);
			}
			if (i > 0) {
				sb.append(separator);
			}
			sb.append(s);
		}
		return sb.toString();
	}

	public static String join(String[] strArray, String separator) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < strArray.length; ++i) {
			if (i > 0) {
				sb.append(separator);
			}
			sb.append(strArray[i]);
		}
		return sb.toString();
	}

	public static String join(short[] shortArray, char separator) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < shortArray.length; ++i) {
			if (i > 0) {
				sb.append(separator);
			}
			sb.append(shortArray[i]);
		}
		return sb.toString();
	}

}
