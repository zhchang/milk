package com.mozat.mengine;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.zip.GZIPOutputStream;

import mozat.util.Base64;
import mozat.util.Util;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mozat.mengine.Function.FunctionParam;
import com.mozat.sv3.moml3.parser.ErrorLevel;
import com.mozat.sv3.moml3.parser.Moml3Parser;
import com.mozat.sv3.moml3.parser.ParseResult;
import com.mozat.sv3.moml3.tags.Moscript;
import com.mozat.sv3.moml3.tags.TagUtil;

public class MCompiler {

	public enum ReturnType {
		Int, Str, Array, Map, Rect, Player, Text, Group, Tiles, Page, Element, Void
	}

	private static class BuildCacheResult {
		boolean cached = false;
	}

	private static List<String> validTypes = new ArrayList<String>();
	static {
		validTypes.add("source");
		validTypes.add("image");
		validTypes.add("l10n");
		validTypes.add("l10n2");
		validTypes.add("page");
		validTypes.add("sprite");
		validTypes.add("9patch");
		validTypes.add("audio");
		validTypes.add("text");
		validTypes.add("lib");
	}

	public static class CodePortion {
		public int orgline = -1;
		public int start = -1;
		public int end = -1;
		public String name = "";

		public CodePortion(int a1, int a2, int a3, String name) {
			orgline = a1;
			start = a2;
			end = a3;
			this.name = name;
		}
	}

	public static class CodeError {
		public int includeLine = 0;
		public int realLine = 0;
		public String include = "";
		public boolean isInclude = false;
		public String source = "";
		public String error;

		public String toString() {

			String result = null;
			if (isInclude) {
				result = "<" + error + "> at line[" + realLine + "] @ file["
						+ include + "]";
				result += " included from line[" + includeLine + "] @ file["
						+ source + "]";
			} else {
				result = "<" + error + "> at line[" + realLine + "] @ file["
						+ source + "]";
			}
			return result;
		}
	}

	public static class ReturnValue {
		ReturnType type = ReturnType.Void;
		String defType = null;
		int index = 0;
		int globalIndex = 0;
		boolean global = false;

		ReturnValue(ReturnType type) {
			this.type = type;
		}

	}

	private Stack<Context> contexts = new Stack<Context>();

	private List<String> strConsts = new ArrayList<String>();
	private List<String> mainAsmCodes = new ArrayList<String>();
	private List<String> funcAsmCodes = new ArrayList<String>();
	private Map<String, Integer> intConsts = new HashMap<String, Integer>();

	private Context global = new Context(mainAsmCodes, null);
	private Context curContext = global;

	private List<String> functionCalls = new ArrayList<String>();

	private int curIfEndIndex = 0;
	private int curForIndex = 0;
	private int curSwitchIndex = 0;
	private int curAndIndex = 0;
	private int curOrIndex = 0;
	private int curTryIndex = 0;

	private Stack<String> breaks = new Stack<String>();
	private Stack<String> continues = new Stack<String>();

	private List<Function> functions = new ArrayList<Function>();

	private List<String> milkFilePaths = new ArrayList<String>();
	private List<String> moml3FilePaths = new ArrayList<String>();
	private List<String> libPaths = new ArrayList<String>();
	private Map<String, JSONObject> libs = new HashMap<String, JSONObject>();
	private Map<String, String> libMap = new HashMap<String, String>();
	public String currentScript = null;
	public boolean compilingLib = false;
	public TypeDef curDef = null;
	private String outputPath = null;
	private File resourceFile = null;

	private JSONObject resourceJson = null;
	private JSONArray libDefs = null;
	private String apkPath = "mge.apk";

	private boolean startActivity = true;
	private String androidHome = null;

	private boolean reInstall = false;
	private File jadFile = null;
	private String serverUrl = null;
	private String monetServiceId = null;
	private String monetUser = null;
	private String monetPassword = null;
	private String userDomain = null;

	private String monetUrl = null;
	private String monetPort = null;
	private Properties buildCache = new Properties();
	private File buildCacheFile;

	public List<CodePortion> codePortions = new ArrayList<CodePortion>();

	private void parseArgs(String[] args) throws Throwable {
		int size = args.length;
		for (int i = 0; i < size; i += 2) {
			String key = args[i];
			String value = args[i + 1];
			if (key.equals("-o")) {
				outputPath = value;
			} else if (key.equals("-r")) {
				resourceFile = new File(value);
				if (!resourceFile.isAbsolute()) {
					String pwdPath = System.getProperty("user.dir");
					File pwd = new File(pwdPath);
					resourceFile = new File(pwd, value);
				}
			} else if (key.equals("-s")) {
				if (value.equalsIgnoreCase("y")) {
					startActivity = true;
				} else {
					startActivity = false;
				}
			} else if (key.equals("-a")) {
				apkPath = value;
			} else if (key.equals("-ah")) {
				androidHome = value;
			} else if (key.equals("-ri")) {
				if (value.equalsIgnoreCase("y")) {
					reInstall = true;
				} else {
					reInstall = false;
				}
			} else if (key.equals("-jad")) {
				jadFile = new File(value);
			} else if (key.equals("-ds")) {
				serverUrl = value;
			} else if (key.equals("-ms")) {
				monetServiceId = value;
			} else if (key.equals("-mu")) {
				monetUser = value;
				userDomain = monetUser.substring(monetUser.indexOf('@'));
				System.out.println("user-domain:" + userDomain);
			} else if (key.equals("-mp")) {
				monetPassword = value;
			} else if (key.equals("-murl")) {
				monetUrl = value;
			} else if (key.equals("-mport")) {
				monetPort = value;
			} else if (key.equals("-json")) {
				resourceJson = new JSONObject(value);
			} else {
				throw new RuntimeException("invalid option: " + key);
			}
		}
		parseResourceFile();
		if (milkFilePaths.size() == 0
				|| (resourceFile == null || !resourceFile.exists())) {
			throw new RuntimeException("invalid input");
		}
	}

	private List<String> getPaths(JSONObject resource) throws Throwable {
		List<String> list = new ArrayList<String>();
		@SuppressWarnings("rawtypes")
		Iterator it = resource.keys();
		while (it.hasNext()) {
			String key = (String) it.next();
			if (key.startsWith("path")) {
				list.add(key);
			}
		}

		return list;
	}

	private File getResourceFile(String path, File parent) {
		File test = new File(path);
		if (!test.isAbsolute()) {
			test = new File(parent, path);
		}
		if (test.exists() && test.isFile()) {
			return test;
		} else {
			System.err.println("path [" + path + "] not found");
			return null;
		}
	}

	private void parseResourceFile() throws Throwable {
		if (resourceJson == null) {
			resourceJson = Util.parseJson(resourceFile);
		}
		String game = resourceJson.getString("game");
		libDefs = resourceJson.optJSONArray("libs");

		if (game.length() > 255) {
			throw new RuntimeException("game name too long: [" + game + "] "
					+ game.length());
		}
		String domain = resourceJson.getString("domain");
		if (domain.length() > 255) {
			throw new RuntimeException("domain name too long: [" + domain
					+ "] " + domain.length());
		}
		JSONObject resources = resourceJson.getJSONObject("resources");

		@SuppressWarnings("rawtypes")
		Iterator keys = resources.keys();
		while (keys.hasNext()) {
			String id = (String) keys.next();
			JSONObject resource = resources.getJSONObject(id);
			String type = resource.getString("type");
			if (type == null || !validTypes.contains(type)) {
				throw new RuntimeException("invalid type for resource [" + id
						+ "]");
			}
			if (id.length() > 255) {
				throw new RuntimeException("resource id too long: [" + id
						+ "] " + id.length());
			}
			JSONArray paths = resource.getJSONArray("paths");
			int len = paths.length();

			for (int k = 0; k < len; k++) {
				JSONObject pathItem = paths.getJSONObject(k);
				String path = pathItem.getString("path");
				File test = getResourceFile(path, resourceFile.getParentFile());
				if (test == null) {
					throw new RuntimeException("File " + path
							+ " cannot be found");
				} else {
					if (type.equalsIgnoreCase("source")) {
						if (path.endsWith(".mk")) {
							if (!milkFilePaths.contains(test.getAbsolutePath())) {
								milkFilePaths.add(test.getAbsolutePath());
							}
						} else {
							throw new RuntimeException("Source File " + path
									+ " has a invalid extension.(mk expected)");
						}
					} else if (type.equalsIgnoreCase("page")) {
						if (path.endsWith(".moml")) {
							if (!moml3FilePaths
									.contains(test.getAbsolutePath())) {
								moml3FilePaths.add(test.getAbsolutePath());
							}
						} else {
							throw new RuntimeException(
									"moml3 File "
											+ path
											+ " has a invalid extension.(moml expected)");
						}
					} else if (type.equalsIgnoreCase("lib")) {
						if (path.endsWith(".mk")) {
							libMap.put(id, test.getAbsolutePath());
						} else {
							throw new RuntimeException("Lib File " + path
									+ " has an invalid extension(.mk expected)");
						}
					}

				}
			}
		}
		if (libDefs != null) {
			int count = libDefs.length();
			for (int i = 0; i < count; i++) {
				String lib = libDefs.getString(i);
				if (libMap.containsKey(lib)) {
					String libPath = libMap.get(lib);
					if (!libPaths.contains(libPath)) {
						libPaths.add(libPath);
					}
				}
			}
		}
	}

	private static void addCodePortion(int line, int length,
			List<CodePortion> list, String name) {
		int offset = 0;
		for (CodePortion portion : list) {
			offset += portion.end - portion.start - 1;
		}
		list.add(new CodePortion(line, line + offset, line + offset + length,
				name));
	}

	public String preprocessMoml(String momlContent, File parent)
			throws Exception {
		if (momlContent != null) {
			String[] lines = momlContent.replaceAll("\\\r", "").split("\n");
			if (lines != null) {
				for (int i = 0; i < lines.length; i++) {
					String line = lines[i];
					if (line.trim().startsWith("#include")) {
						int index1 = line.indexOf("<");
						int index2 = -1;
						if (index1 != -1) {
							index2 = line.indexOf(">", index1);
							if (index2 != -1) {
								String filePath = line.substring(index1 + 1,
										index2);
								File includeFile = new File(filePath);
								if (includeFile.exists()
										&& includeFile.isFile()) {
									String includeContent = Util
											.getFileContent(includeFile);
									LineNumberReader lnr = new LineNumberReader(
											new FileReader(includeFile));
									lnr.skip(Long.MAX_VALUE);
									momlContent = momlContent.replaceAll(line,
											includeContent);
								} else {
									throw new StatementException(getError(
											i + 1, "cannot include file: "
													+ filePath));
								}
							}
						} else {
							index1 = line.indexOf("\"");
							if (index1 != -1) {
								index2 = line.indexOf("\"", index1 + 1);
								if (index2 != -1) {
									String filePath = line.substring(
											index1 + 1, index2);
									File includeFile = new File(filePath);
									if (!includeFile.isAbsolute()) {
										includeFile = new File(parent, filePath);
									}
									if (includeFile.exists()
											&& includeFile.isFile()) {
										String includeContent = Util
												.getFileContent(includeFile);
										LineNumberReader lnr = new LineNumberReader(
												new FileReader(includeFile));
										lnr.skip(Long.MAX_VALUE);
										momlContent = momlContent.replaceAll(
												line, includeContent);
									} else {
										throw new StatementException(getError(
												i + 1, "cannot include file: "
														+ filePath));
									}
								}
							}
						}
					}
				}
			}

		}
		return momlContent;
	}

	public String preprocess(String scriptContent, File parent,
			List<CodePortion> portions) throws Exception {
		portions.clear();
		if (scriptContent != null) {
			String[] lines = scriptContent.replaceAll("\\\r", "").split("\n");
			if (lines != null) {
				for (int i = 0; i < lines.length; i++) {
					String line = lines[i];
					if (line.trim().startsWith("#include")) {
						int index1 = line.indexOf("<");
						int index2 = -1;
						if (index1 != -1) {
							index2 = line.indexOf(">", index1);
							if (index2 != -1) {
								String filePath = line.substring(index1 + 1,
										index2);
								File includeFile = new File(filePath);
								if (includeFile.exists()
										&& includeFile.isFile()) {
									String includeContent = Util
											.getFileContent(includeFile);
									LineNumberReader lnr = new LineNumberReader(
											new FileReader(includeFile));
									lnr.skip(Long.MAX_VALUE);
									int lineCount = lnr.getLineNumber() + 1;
									addCodePortion(i + 1, lineCount, portions,
											filePath);
									scriptContent = scriptContent.replaceAll(
											line, includeContent);
								} else {
									throw new StatementException(getError(
											i + 1, "cannot include file: "
													+ filePath));
								}
							}
						} else {
							index1 = line.indexOf("\"");
							if (index1 != -1) {
								index2 = line.indexOf("\"", index1 + 1);
								if (index2 != -1) {
									String filePath = line.substring(
											index1 + 1, index2);
									File includeFile = new File(filePath);
									if (!includeFile.isAbsolute()) {
										includeFile = new File(parent, filePath);
									}
									if (includeFile.exists()
											&& includeFile.isFile()) {
										String includeContent = Util
												.getFileContent(includeFile);
										LineNumberReader lnr = new LineNumberReader(
												new FileReader(includeFile));
										lnr.skip(Long.MAX_VALUE);
										int lineCount = lnr.getLineNumber() + 1;
										addCodePortion(i + 1, lineCount,
												portions, filePath);
										scriptContent = scriptContent
												.replaceAll(line,
														includeContent);
									} else {
										throw new StatementException(getError(
												i + 1, "cannot include file: "
														+ filePath));
									}
								}
							}
						}
					}
				}
			}

		}
		return scriptContent;
	}

	private void fillDefaultIntConsts() {
		intConsts.put("true", 1);
		intConsts.put("false", 0);

		intConsts.put("PLATFORM_ANDROID", 0);
		intConsts.put("PLATFORM_IOS", 1);
		intConsts.put("PLATFORM_S60", 2);
		intConsts.put("PLATFORM_JME", 3);
		intConsts.put("PLATFORM_BB", 4);

		intConsts.put("KEY_0", 48);
		intConsts.put("KEY_1", 49);
		intConsts.put("KEY_2", 50);
		intConsts.put("KEY_3", 51);
		intConsts.put("KEY_4", 52);
		intConsts.put("KEY_5", 53);
		intConsts.put("KEY_6", 54);
		intConsts.put("KEY_7", 55);
		intConsts.put("KEY_8", 56);
		intConsts.put("KEY_9", 57);

		intConsts.put("KEY_UP", -1);
		intConsts.put("KEY_DOWN", -2);
		intConsts.put("KEY_LEFT", -3);
		intConsts.put("KEY_RIGHT", -4);
		intConsts.put("KEY_SELECT", -5);

		intConsts.put("INPUT_URL", 0x4);
		intConsts.put("INPUT_EMAIL", 0x1);
		intConsts.put("INPUT_PHONE", 0x3);
		intConsts.put("INPUT_NUMERIC", 0x2);
		intConsts.put("INPUT_ANY", 0x0);
		intConsts.put("INPUT_PASSWORD", 0x10000);

		intConsts.put("FLAG_AUTOCENTER", 0x01);
		intConsts.put("FLAG_SHADING", 0x02);
		intConsts.put("FLAG_FOCUSFIRST", 0x04);
		intConsts.put("FLAG_NOBG", 0x08);
		intConsts.put("FLAG_NOCLOSE", 0x10);
		intConsts.put("FLAG_FITHEIGHT", 0x20);
		intConsts.put("FLAG_HASMENU", 0x40);
		intConsts.put("FLAG_EMBEDDED", 0x80);

		intConsts.put("TINT", 0);
		intConsts.put("TSTRING", 1);
		intConsts.put("TARRAY", 2);
		intConsts.put("TMAP", 3);
		intConsts.put("TRECT", 4);
		intConsts.put("TPLAYER", 5);
		intConsts.put("TTEXT", 6);
		intConsts.put("TGROUP", 7);
		intConsts.put("TTILES", 8);
		intConsts.put("TELEMENT", 9);

		intConsts.put("LARGE_FONT", 1);
		intConsts.put("NORMAL_FONT", 0);
		intConsts.put("SMALL_FONT", -1);

	}

	public void clearParseResults() {
		contexts.clear();
		strConsts.clear();
		mainAsmCodes.clear();
		funcAsmCodes.clear();
		intConsts.clear();
		fillDefaultIntConsts();

		global = new Context(mainAsmCodes, null);
		curContext = global;

		curIfEndIndex = 0;
		curForIndex = 0;
		curSwitchIndex = 0;
		curTryIndex = 0;

		breaks.clear();
		continues.clear();

		functions.clear();
		codePortions.clear();
		libs.clear();
		curDef = null;
	}

	public CodeError getError(int line, String error) {
		CodeError codeError = new CodeError();
		codeError.source = currentScript;
		codeError.error = error;

		int temp = 0;
		if (codePortions.size() > 0) {
			temp = codePortions.get(codePortions.size() - 1).end;
		}

		for (CodePortion codePortion : codePortions) {
			if (codePortion.start <= line && line <= codePortion.end) {
				codeError.includeLine = codePortion.orgline;
				codeError.realLine = line - codePortion.start + 1;
				codeError.include = codePortion.name;
				codeError.source = currentScript;
				codeError.isInclude = true;
				break;
			}
		}
		if (!codeError.isInclude) {
			codeError.realLine = line - temp + codePortions.size();
		}

		return codeError;
	}

	public void compileMoml(File momlFile, File sv3File) throws Throwable {

		String momlSource = Util.getFileContent(momlFile);
		momlSource = preprocessMoml(momlSource, momlFile.getParentFile());

		Moml3Parser parser = new Moml3Parser(momlSource);
		ParseResult result = parser.parse(ErrorLevel.Warning);
		List<Moscript> scripts = result.page.getScripts();
		StringBuilder sb = new StringBuilder();
		for (Moscript script : scripts) {
			sb.append(script.getText()).append("\n");
		}
		String source = sb.toString();
		if (source != null && source.length() > 0) {
			// long start = System.currentTimeMillis();
			currentScript = momlFile.getAbsolutePath();
			File asmFile = new File(sv3File.getParentFile(), sv3File.getName()
					.replaceAll("sv3", "asm"));
			BuildCacheResult bcs = new BuildCacheResult();
			String asm = assembleMilk(source, momlFile, asmFile, bcs, false);
			result.page.setByteCode(asmToByteCode(asm));
			currentScript = null;
		}
		// long dur = System.currentTimeMillis() - start;
		// System.out.println("complie embedded milk code consumed [" + dur
		// + "]ms");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		TagUtil.toMobon(result.page, bos);
		byte[] thing = gzip(bos.toByteArray());
		Util.writeFileBytes(sv3File, thing);

	}

	public String assembleMilk(String source, File input, File asmFile,
			BuildCacheResult bcs, boolean isLib) {
		functionCalls.clear();
		String asm = null;
		String lastAsm = null;
		try {
			File parent = input.getParentFile();
			source = preprocess(source, parent, codePortions);
			String sourceMd5 = getMD5(source);
			String binMd5 = null;
			if (asmFile.exists()) {
				lastAsm = Util.getFileContent(asmFile);
				binMd5 = getMD5(lastAsm);
				String lastBuildResult = buildCache.getProperty(input
						.getAbsolutePath());
				if ((sourceMd5 + "-" + binMd5).equals(lastBuildResult)) {
					bcs.cached = true;
					return lastAsm;
				} else {
					System.out.println("ASM " + input.getAbsolutePath());
				}
			}

			CharStream stream = new ANTLRStringStream(source);

			MEngineLexer lexer = new MEngineLexer(stream);

			MEngineParser parser = new MEngineParser(new CommonTokenStream(
					lexer));
			MEngineParser.program_return result = parser.program();
			int errorCount = parser.getNumberOfSyntaxErrors();
			if (errorCount > 0) {
				System.out
						.println("Error in <" + input.getAbsolutePath() + ">");

				throw new Throwable(errorCount + " parser errors");
			}
			CommonTree ast = (CommonTree) result.getTree();
			parse(ast, isLib);

			asm = outputAsm(isLib);
			binMd5 = getMD5(asm);
			buildCache.setProperty(input.getAbsolutePath(), sourceMd5 + "-"
					+ binMd5);
		} catch (RecognitionException re) {
			// System.err.println(getError(re.line, re.getMessage()));
			String pattern = "";
			try {
				String[] lines = source.replaceAll("\\\r", "").split("\n");
				for (int i = re.line - 5; i < re.line + 5; i++) {
					if (i >= 0 && i < lines.length - 1) {
						pattern += lines[i];
						pattern += "\r\n";
					}
				}
				pattern += "<<<" + lines[re.line] + ">>>";
			} catch (Throwable t) {
				pattern = "unknown";
			}
			System.out.println("Error in <" + input.getAbsolutePath() + ">");
			throw new RuntimeException("recognition error arround pattern: ["
					+ pattern + "]");

		} catch (Throwable t) {
			throw new RuntimeException(t);

		}
		Util.writeFileContent(asmFile, asm);
		return asm;
	}

	public byte[] asmToByteCode(String asm) {
		return ByteCoder.genByteCode(asm);
	}

	private byte[] gzip(byte[] input) {
		try {
			ByteArrayOutputStream tempBos = new ByteArrayOutputStream();
			GZIPOutputStream gos = new GZIPOutputStream(tempBos);
			gos.write(input);
			gos.flush();
			gos.close();
			return tempBos.toByteArray();
		} catch (Throwable t) {
			return null;
		}
	}

	public static String typeToString(FunctionParam p) {
		String typeStr = null;
		switch (p.type) {
		case Int: {
			typeStr = "int";
			break;
		}
		case Str: {
			typeStr = "String";
			break;
		}
		case Array: {
			if (p.defType != null) {
				typeStr = p.defType;
			} else {
				typeStr = "Array";
			}
			break;
		}
		case Map: {
			typeStr = "Map";
			break;
		}
		case Player: {
			typeStr = "MPlayer";
			break;
		}
		case Text: {
			typeStr = "MText";
			break;
		}
		case Tiles: {
			typeStr = "MTiles";
			break;
		}
		case Rect: {
			typeStr = "MRect";
			break;
		}
		case Group: {
			typeStr = "MGroup";
			break;
		}
		case Element: {
			typeStr = "Element";
			break;
		}
		case Void: {
			typeStr = "void";
			break;
		}

		}
		return typeStr;
	}

	public void compileMilk(File sourceFile, File asmFile, File bcFile,
			File ifFile, boolean isLib) {

		try {
			String source = Util.getFileContent(sourceFile);

			BuildCacheResult bcs = new BuildCacheResult();
			String asm = assembleMilk(source, sourceFile, asmFile, bcs, isLib);

			if (bcs.cached == false) {
				byte[] bcBytes = asmToByteCode(asm);
				if (isLib) {

					Map<String, Integer> interfaces = ByteCoder
							.genInterface(asm);

					if (interfaces != null) {
						JSONObject thing = new JSONObject();
						if (curDef != null) {
							JSONObject cdef = new JSONObject();
							cdef.put("name", curDef.name);
							JSONArray pArr = new JSONArray();
							for (int i = 0; i < curDef.params.size(); i++) {
								FunctionParam p = curDef.params.get(i);
								JSONObject pj = new JSONObject();
								pj.put("name", p.name);
								pj.put("type", typeToString(p));
								pArr.put(pj);
							}
							cdef.put("params", pArr);
							thing.put("class-def", cdef);
						}
						JSONObject functions = new JSONObject();
						for (String key : interfaces.keySet()) {
							JSONObject item = new JSONObject();
							item.put("isp", interfaces.get(key));
							Function function = this.getFunctionById(key);
							JSONArray params = new JSONArray();
							for (FunctionParam param : function.params) {
								params.put(typeToString(param));
							}
							item.put("params", params);
							item.put("return-type",
									typeToString(function.returnValue));
							functions.put(key, item);
						}
						thing.put("functions", functions);
						thing.put("lib", getLibName());
						Util.writeFileContent(ifFile, thing.toString(4));
					}
				}
				if (bcBytes != null && bcBytes.length > 0) {
					Util.writeFileBytes(bcFile, gzip(bcBytes));
				} else {
					throw new RuntimeException("bytecoder error.");
				}
			}
		} catch (Throwable t) {

			throw new RuntimeException(t);

		}
	}

	private String getPathRelativeToOutput(File output, File thing) {
		String result = null;
		String absPath = thing.getAbsolutePath();
		String outputPath = output.getParentFile().getAbsolutePath();
		if (absPath.startsWith(outputPath)) {
			result = absPath.substring(outputPath.length());
		} else {
			result = absPath;
		}
		if (result.startsWith("/")) {
			result = result.substring(1);
		} else if (result.startsWith("\\")) {
			result = result.substring(1);
		}
		return result;
	}

	private File getOutputPath(File output, File source) {
		File file = null;
		String absPath = source.getAbsolutePath();
		String absParent = resourceFile.getParentFile().getAbsolutePath();
		if (absPath.startsWith(absParent)) {
			file = new File(output, absPath.substring(absParent.length()));
			file.getParentFile().mkdirs();
		} else {
			String result = source.getParentFile().getAbsolutePath();
			if (result.charAt(1) == ':') {
				result = result.substring(0, 1) + result.substring(2);
			}
			file = new File(output, result);
			file.getParentFile().mkdirs();
		}
		return file.getParentFile();
	}

	private boolean isModified(File asmFile, File input) {
		File parent = input.getParentFile();
		String source = Util.getFileContent(input);
		try {
			source = preprocess(source, parent, codePortions);
			String sourceMd5 = getMD5(source);
			if (asmFile.exists()) {
				String lastAsm = Util.getFileContent(asmFile);
				String binMd5 = getMD5(lastAsm);
				String lastBuildResult = buildCache.getProperty(input
						.getAbsolutePath());
				if ((sourceMd5 + "-" + binMd5).equals(lastBuildResult)) {
					return false;
				} else {
					return true;
				}
			}
		} catch (Exception e) {
			return true;
		}
		return true;
	}

	private boolean testRebuildAll() {
		boolean result = false;
		File outputFolder = new File(outputPath);
		if (!outputFolder.isAbsolute()) {
			File resourceParent = resourceFile.getParentFile();
			outputFolder = new File(resourceParent, outputFolder.getName());
		}
		for (String lib : libPaths) {
			clearParseResults();
			try {
				File sourceFile = new File(lib);
				String name = sourceFile.getName();
				String withoutExt = name.substring(0, name.lastIndexOf("."));
				File asmFile = new File(
						getOutputPath(outputFolder, sourceFile), withoutExt
								+ ".asm");
				result = isModified(asmFile, sourceFile);
				if (result == true) {
					break;
				}

			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}
		return result;
	}

	public void doMain(String[] args, boolean standalone, PrintStream out,
			PrintStream err) {

		System.out.println("\n\nInvoking Milk Compiler");

		try {
			parseArgs(args);
		} catch (Throwable t) {
			if (standalone) {
				System.err.println(t);
				System.err
						.println("Usage:\n cmd -r <resource-file> -o <output-dir> -d <dimension:x,y> -p <platform:S60,J2ME,BB,Android,iOs> -s <y/n> -ah <path-to-android-home> -a <path-to-apk-file>");
				System.exit(1);
			} else {
				System.err.println("parseArgs : " + t);
				throw new RuntimeException(t.getMessage());
			}
		}

		try {
			if (outputPath != null) {

				File outputFolder = new File(outputPath);
				if (!outputFolder.isAbsolute()) {
					File resourceParent = resourceFile.getParentFile();
					outputFolder = new File(resourceParent,
							outputFolder.getName());
				}
				if (!outputFolder.exists()) {
					if (!outputFolder.mkdirs()) {
						throw new RuntimeException(
								outputFolder.getAbsolutePath()
										+ " is not a valid output path");
					}
				}
				buildCacheFile = new File(outputFolder, "build.properties");
				if (buildCacheFile.exists()) {
					try {
						buildCache.load(new FileInputStream(buildCacheFile));
					} catch (Exception e) {
						System.err.println("failed to load build cache");
					}
				}

				long start = System.currentTimeMillis();

				boolean rebuildAll = testRebuildAll();
				if (rebuildAll) {
					buildCache.clear();
				}

				for (String lib : libPaths) {
					currentScript = lib;
					compilingLib = true;
					clearParseResults();
					try {
						File sourceFile = new File(lib);
						String name = sourceFile.getName();
						String withoutExt = name.substring(0,
								name.lastIndexOf("."));
						File asmFile = new File(getOutputPath(outputFolder,
								sourceFile), withoutExt + ".asm");
						File libFile = new File(getOutputPath(outputFolder,
								sourceFile), withoutExt + ".lib");
						File ifFile = new File(getOutputPath(outputFolder,
								sourceFile), withoutExt + ".if");
						asmFile.getParentFile().mkdirs();
						compileMilk(sourceFile, asmFile, libFile, ifFile, true);

					} catch (Throwable t) {
						throw new RuntimeException(t);
					}
					compilingLib = false;
				}

				start = System.currentTimeMillis();

				for (String script : milkFilePaths) {
					currentScript = script;
					if (currentScript.contains("-android")) {
						int brk = 1;
						int a = brk;
					}
					compilingLib = false;
					clearParseResults();
					try {
						File sourceFile = new File(script);
						String name = sourceFile.getName();
						String withoutExt = name.substring(0,
								name.lastIndexOf("."));
						File asmFile = new File(getOutputPath(outputFolder,
								sourceFile), withoutExt + ".asm");
						File bcFile = new File(getOutputPath(outputFolder,
								sourceFile), withoutExt + ".bc");
						asmFile.getParentFile().mkdirs();
						compileMilk(sourceFile, asmFile, bcFile, null, false);

					} catch (Throwable t) {
						throw new RuntimeException(t);
					}
				}

				System.out.println("milk files compiled in ["
						+ (System.currentTimeMillis() - start) + "] ms ");
				start = System.currentTimeMillis();

				for (String moml3 : moml3FilePaths) {
					compilingLib = false;
					clearParseResults();
					try {
						File moml3File = new File(moml3);
						String name = moml3File.getName();
						String withoutExt = name.substring(0,
								name.lastIndexOf("."));
						File sv3File = new File(getOutputPath(outputFolder,
								moml3File), withoutExt + ".sv3");

						compileMoml(moml3File, sv3File);
					} catch (Throwable t) {
						System.err.println("error in " + moml3);
						throw new RuntimeException(t);

					}
				}
				System.out.println("moml3 files compiled in ["
						+ (System.currentTimeMillis() - start) + "] ms ");

				System.out.println("ByteCode compiled");
				try {
					if (!buildCacheFile.exists()) {
						buildCacheFile.createNewFile();
					}
					FileOutputStream fos = new FileOutputStream(buildCacheFile);
					buildCache.store(fos, "");
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("failed to otuput build-cache");
				}

				if (serverUrl != null) {
					// deploy to server

					try {
						System.out.println("Deploying to server...");

						deployToServer(outputFolder, standalone);

						System.out.println("Deployment finished.");
					} catch (Throwable t) {
						t.printStackTrace();
						throw new RuntimeException(t);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {

		}

		if (jadFile != null && jadFile.exists() && jadFile.isFile()) {
			File jarFile = new File(jadFile.getParentFile(), jadFile.getName()
					.substring(0, jadFile.getName().length() - 1) + "r");

			System.out.println("configuring jad file.");

			try {

				String contents = Util.getFileContent(jadFile);
				String[] lines = contents.replaceAll("\\\r", "").split("\n");
				List<String> output = new ArrayList<String>();
				boolean domainOk = false;
				boolean gameOk = false;
				boolean userOk = false;
				boolean userDomainOk = false;
				boolean passOk = false;
				boolean serviceOk = false;

				boolean monetUrlOk = false;
				boolean monetPortOk = false;
				for (String line : lines) {
					String[] parts = line.split(":", 2);
					if (parts.length == 2) {
						String key = parts[0];
						String value = parts[1].trim();
						if (key.equals("domain")) {
							value = getDomain();
							domainOk = true;
						} else if (key.equals("game")) {
							value = getGameId();
							gameOk = true;
						} else if (key.equals("username")) {
							value = monetUser;
							userOk = true;
						} else if (key.equals("password")) {
							value = getMD5(monetPassword.getBytes());
							passOk = true;
						} else if (key.equals("userdomain")) {
							value = userDomain;
							userDomainOk = true;
						} else if (key.equals("serviceid")
								&& monetServiceId != null
								&& monetServiceId.length() > 0) {
							value = monetServiceId;
							serviceOk = true;
						} else if (key.equals("moneturl") && monetUrl != null
								&& monetUrl.length() > 0) {
							value = monetUrl;
							monetUrlOk = true;
						} else if (key.equals("monetport") && monetPort != null
								&& monetPort.length() > 0) {
							value = monetPort;
							monetPortOk = true;
						} else if (key.startsWith("MIDlet-Jar-Size")) {
							value = "" + jarFile.length();
						} else if (key.startsWith("MIDlet-Jar-URL")) {
							value = jarFile.getName();
						}
						output.add(key + ": " + value);
					}
				}
				if (!domainOk) {
					output.add("domain: " + getDomain());
				}
				if (!gameOk) {
					output.add("game: " + getGameId());
				}
				if (!userOk) {
					output.add("username: " + monetUser);
				}
				if (!passOk) {
					output.add("password: " + getMD5(monetPassword.getBytes()));
				}
				if (!userDomainOk) {
					output.add("userdomain: " + userDomain);
				}
				if (!serviceOk && monetServiceId != null
						&& monetServiceId.length() > 0) {
					output.add("serviceid: " + monetServiceId);
				}
				if (!monetUrlOk && monetUrl != null && monetUrl.length() > 0) {
					output.add("moneturl: " + monetUrl);
				}
				if (!monetPortOk && monetPort != null && monetPort.length() > 0) {
					output.add("monetport: " + monetPort);
				}
				StringBuilder builder = new StringBuilder();
				for (String line : output) {
					builder.append(line).append("\r\n");
				}
				Util.writeFileContent(jadFile, builder.toString());
			} catch (Throwable t) {
				t.printStackTrace();
			}

			System.out.println("Finish configuring jad file.");
		}

	}

	private JSONObject genResourceJson(File outputFolder, boolean standalone) {
		JSONObject json = null;
		try {
			File resourceParent = resourceFile.getParentFile();

			json = new JSONObject(resourceJson.toString());
			JSONObject resources = json.getJSONObject("resources");
			Iterator keys = resources.keys();
			while (keys.hasNext()) {
				String id = (String) keys.next();
				JSONObject resource = resources.getJSONObject(id);
				String type = resource.getString("type");
				if (type.equalsIgnoreCase("source")) {
					JSONArray paths = resource.getJSONArray("paths");
					int size = paths.length();
					for (int k = 0; k < size; k++) {
						JSONObject pathItem = paths.getJSONObject(k);
						String path = pathItem.getString("path");
						File file = getResourceFile(path, resourceParent);

						String name = file.getName();
						String withoutExt = name.substring(0,
								name.lastIndexOf("."));
						name = withoutExt + ".bc";
						file = new File(getOutputPath(outputFolder, file), name);
						pathItem.put("path",
								getPathRelativeToOutput(outputFolder, file));
						paths.put(k, pathItem);
					}
					resource.put("paths", paths);
					resources.put(id, resource);
				} else if (type.equalsIgnoreCase("page")) {
					JSONArray paths = resource.getJSONArray("paths");
					int size = paths.length();
					for (int k = 0; k < size; k++) {
						JSONObject pathItem = paths.getJSONObject(k);
						String path = pathItem.getString("path");
						File file = getResourceFile(path, resourceParent);
						String name = file.getName();
						String withoutExt = name.substring(0,
								name.lastIndexOf("."));
						name = withoutExt + ".sv3";
						file = new File(getOutputPath(outputFolder, file), name);
						pathItem.put("path",
								getPathRelativeToOutput(outputFolder, file));
						paths.put(k, pathItem);
					}
					resource.put("paths", paths);
					resources.put(id, resource);
				} else if (type.equalsIgnoreCase("lib")) {
					JSONArray paths = resource.getJSONArray("paths");
					int size = paths.length();
					for (int k = 0; k < size; k++) {
						JSONObject pathItem = paths.getJSONObject(k);
						String path = pathItem.getString("path");
						File file = getResourceFile(path, resourceParent);
						String name = file.getName();
						String withoutExt = name.substring(0,
								name.lastIndexOf("."));
						name = withoutExt + ".lib";
						file = new File(getOutputPath(outputFolder, file), name);
						pathItem.put("path",
								getPathRelativeToOutput(outputFolder, file));
						paths.put(k, pathItem);
					}
					resource.put("paths", paths);
					resources.put(id, resource);
				}
			}
			json.put("resources", resources);
		} catch (Throwable t) {
			if (standalone) {
				System.err.println(t);
				System.exit(1);
			} else {
				throw new RuntimeException(t.getMessage());
			}
		}
		return json;
	}

	private String getGameId() throws JSONException {
		return resourceJson.getString("game");
	}

	private String getDomain() throws JSONException {
		return resourceJson.getString("domain");
	}

	private boolean processServerResponse(JSONObject response) throws Exception {
		System.out.println(response.toString());
		if (!response.getBoolean("result")) {

			throw new RuntimeException("server returns error :"
					+ response.optString("error"));
		}
		return true;
	}

	private void deployToServer(File outputFolder, boolean standalone)
			throws Throwable {

		List<String> queryCache = new ArrayList<String>();

		JSONObject deployableResource = genResourceJson(outputFolder,
				standalone);

		JSONObject resources = deployableResource.getJSONObject("resources");

		JSONObject config = new JSONObject();
		config.put("action", "config");
		config.put("domain", getDomain());
		config.put("game", getGameId());
		config.put("content", deployableResource);
		JSONObject queryResponse = new JSONObject(postData(serverUrl,
				config.toString()));

		System.out.println("querying server for updates");
		processServerResponse(queryResponse);

		File resourceParent = resourceFile.getParentFile();

		JSONObject query = new JSONObject();
		query.put("action", "query");
		query.put("domain", getDomain());
		query.put("game", getGameId());
		JSONArray items = new JSONArray();

		Iterator keys = resources.keys();

		while (keys.hasNext()) {
			String id = (String) keys.next();
			JSONObject resource = resources.getJSONObject(id);
			JSONArray paths = resource.getJSONArray("paths");
			int len = paths.length();
			for (int k = 0; k < len; k++) {
				JSONObject pathItem = paths.getJSONObject(k);
				String path = pathItem.getString("path");
				if (queryCache.contains(path)) {
					continue;
				} else {
					queryCache.add(path);
				}
				File test = getResourceFile(path, resourceParent);
				JSONObject item = new JSONObject();
				item.put("file", path);
				String md5 = null;

				md5 = getMD5(Util.getFileBytes(test));

				item.put("md5", md5);
				items.put(item);

			}
		}

		query.put("files", items);
		String queryData = query.toString();
		queryResponse = new JSONObject(postData(serverUrl, queryData));
		processServerResponse(queryResponse);

		JSONArray updates = queryResponse.optJSONArray("updates");
		boolean skipall = updates == null || updates.length() == 0;
		if (skipall) {
			System.out.println("all resources are up2date");
		} else {
			int len = updates.length();

			for (int i = 0; i < len; i++) {
				String path = updates.getString(i);
				File thing = getResourceFile(path, resourceParent);

				JSONObject deploy = new JSONObject();
				deploy.put("action", "deploy");
				deploy.put("game", getGameId());
				deploy.put("domain", getDomain());
				deploy.put("file", path);
				deploy.put("bytes",
						Base64.encodeBytes(Util.getFileBytes(thing)));
				System.out.println("deploying [" + thing.getAbsolutePath()
						+ "]");
				queryResponse = new JSONObject(postData(serverUrl,
						deploy.toString()));
				processServerResponse(queryResponse);

			}
		}

	}

	private static String getMD5(String input) {
		if (input != null) {
			byte[] thing = input.getBytes(Charset.forName("UTF-8"));
			return getMD5(thing);
		} else {
			return "";
		}
	}

	private static String getMD5(byte[] bytes) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(bytes);
			BigInteger number = new BigInteger(1, messageDigest);
			String hashtext = number.toString(16);
			// Now we need to zero pad it if you actually want the full 32
			// chars.
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static String postData(String server, String data) throws Throwable {
		URL url = new URL(server);
		URLConnection conn = url.openConnection();
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(data);
		wr.flush();
		wr.close();
		// System.out.println(data);
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		StringBuilder response = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			response.append(line);
		}
		rd.close();
		String value = response.toString();
		// System.out.println(value);
		return value;
	}

	private static String getMacAddress() {
		InetAddress ip;
		try {

			ip = InetAddress.getLocalHost();

			NetworkInterface network = NetworkInterface.getByInetAddress(ip);

			byte[] mac = network.getHardwareAddress();

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X", mac[i]));
			}
			return sb.toString();

		} catch (UnknownHostException e) {
		} catch (SocketException e) {
		}
		return "unknown";
	}

	private void regUser() {
		if (monetUser != null && monetUser.length() > 0
				&& monetPassword != null && monetPassword.length() > 0) {
			return;
		}
		String mac = getMacAddress();
		boolean userOk = false;
		try {
			JSONObject json = new JSONObject();

			json.put("username", mac);
			json.put("password", mac);
			json.put("domain", "@shabik.com");
			String replyStr = postData("http://momo.mozat.com/?r=register",
					json.toString());
			JSONObject reply = new JSONObject(replyStr);
			int result = reply.getInt("ret");
			System.out.println("User registration result: " + result);
			userOk = result == 0;
		} catch (Throwable t) {
		}

		if (!userOk) {
			throw new RuntimeException("failed to use user: " + mac);
		}
		monetUser = mac + "@shabik.com";
		monetPassword = mac;
	}

	private void runInAndroid(File outputFolder, boolean standalone) {

		regUser();

		String fileSep = System.getProperty("file.separator");

		try {
			Map<String, String> envs = System.getenv();
			if (androidHome == null) {
				androidHome = envs.get("ANDROID_HOME");
			}
			if (androidHome != null) {
				String adb = androidHome + fileSep + "platform-tools" + fileSep
						+ "adb";

				if (startActivity) {
					String[] startCmd = { adb, "shell", "am", "start", "-a",
							"mozat.engine.StartGame", "-n",
							"mozat.engine/mozat.shell.MGameActivity", "--es",
							"GAME_DOMAIN", getDomain(), "--es", "GAME_ID",
							getGameId(), "--ei", "FORCE_EXIT", "1", "--es",
							"USER_NAME", monetUser, "--es", "PASSWORD",
							monetPassword };
					try {
						if (reInstall) {
							throw new Exception();
						}
						StringBuilder stdout = new StringBuilder();
						StringBuilder stderr = new StringBuilder();
						Util.exec(startCmd, null, stdout, stderr);
						String out = stdout.toString();
						System.out.println(out);
						System.err.println(stderr.toString());
						if (out.contains("Error")) {
							throw new Exception();
						} else if (out.contains("Starting: Intent")) {
							System.out.println("staring app ok.");
						}
					} catch (Exception e) {
						{
							System.out.println("try to install application.");
							String[] installCmd = { adb, "install", "-r",
									apkPath };
							{
								StringBuilder stdout = new StringBuilder();
								StringBuilder stderr = new StringBuilder();
								Util.exec(installCmd, null, stdout, stderr);
								System.out.println(stdout.toString());
								System.err.println(stderr.toString());
							}
							{
								StringBuilder stdout = new StringBuilder();
								StringBuilder stderr = new StringBuilder();
								Util.exec(startCmd, null, stdout, stderr);
								String out = stdout.toString();
								System.out.println(out);
								System.err.println(stderr.toString());
								if (out.contains("Bad component name")) {
									throw new Exception();
								} else if (out.contains("Starting: Intent")) {
									System.out.println("staring app ok.");
								}
							}
						}
					}
				}
			} else {
				if (standalone) {
					throw new Exception("%ANDROID_HOME% not found.");
				} else {
					throw new Exception(
							"%ANDROID_HOME% not found. Try set it in [Preference=>Mscript Editor]");
				}
			}

		} catch (Throwable t) {
			if (standalone) {
				System.err.println(t);
			} else {
				throw new RuntimeException(t.getMessage());
			}
		}

	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) {
		try {
			new MCompiler().doMain(args, true, null, null);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	public void parse(CommonTree tree, boolean isLib) {
		if (tree == null) {
			return;
		}
		if (tree.getParent() == null) {
			CommonTree temp = new CommonTree();
			temp.addChild(tree);
			tree = temp;
		}
		if (isLib) {
			strConsts.add(getLibName());
		}
		parseConstants(tree);
		List<Integer> funcs = new ArrayList<Integer>();
		List<Integer> gVars = new ArrayList<Integer>();
		int classDef = -1;
		{// get definitions
			int count = tree.getChildCount();
			for (int i = 0; i < count; i++) {
				CommonTree statement = (CommonTree) tree.getChild(i);
				try {
					int defType = getDefs(statement, isLib);
					switch (defType) {
					case 1: {
						funcs.add(i);
						break;
					}
					case 2: {
						gVars.add(i);
						break;
					}
					case 3: {
						classDef = i;
						break;
					}
					}
				} catch (Throwable t) {
					throw new StatementException(getError(statement.getLine(),
							t.getMessage()));
				}

			}
		}

		{// parse statements
			// curContext.addAsmLine(ByteCode.CREATEVAR, new int[] { 0 });
			int index = curContext.asmCodes.size();
			int count = tree.getChildCount();
			for (int i = 0; i < count; i++) {
				if (funcs.contains(i) || classDef == i) {
					continue;
				}

				CommonTree statement = (CommonTree) tree.getChild(i);
				parseStatement(statement);
				curContext.releaseTempVar();
			}
			curContext.asmCodes.add(index, "STACKSIZE " + curContext.varIndex);
		}

		{// parse functions
			int count = tree.getChildCount();
			for (int i = 0; i < count; i++) {
				if (!funcs.contains(i)) {
					continue;
				}
				CommonTree statement = (CommonTree) tree.getChild(i);
				parseFunc(statement);
			}
		}
	}

	private void parseConstants(CommonTree tree) {
		if (tree.getChildCount() > 0) {
			for (Object thing : tree.getChildren()) {
				parseConstants((CommonTree) thing);
			}
		} else if (tree.getType() == MEngineParser.StringLiteral) {
			String thing = getStringFromLiteral(tree.getText());

			if (!strConsts.contains(thing)) {
				strConsts.add(thing);
			}
		}
	}

	private int getStringConstIndex(String value) {
		if (value != null) {
			int count = strConsts.size();
			for (int i = 0; i < count; i++) {
				if (strConsts.get(i).equals(value)) {
					return i;
				}
			}
		}
		return -1;
	}

	private boolean isFunction(String id) {
		for (Function function : functions) {
			if (function.name.equals(id)) {
				return true;
			}
		}
		for (String libName : libs.keySet()) {
			JSONObject lib = libs.get(libName);
			if (!lib.has("class-def")) {
				try {
					JSONObject functions = lib.getJSONObject("functions");
					if (functions.has(id)) {
						return true;
					}
				} catch (Exception e) {

				}
			}
		}
		return false;
	}

	private Function getFunctionById(String id) throws RuntimeException {
		for (Function function : functions) {
			if (function.name.equals(id)) {
				return function;
			}
		}
		for (String libName : libs.keySet()) {
			JSONObject lib = libs.get(libName);
			try {
				if (!lib.has("class-def")) {
					JSONObject functions = lib.getJSONObject("functions");
					if (functions.has(id)) {

						JSONObject func = functions.getJSONObject(id);
						Function function = new Function(id, libName,
								func.getInt("isp"));
						JSONArray params = func.getJSONArray("params");
						int count = params.length();
						for (int i = 0; i < count; i++) {
							function.params.add(this.getParamFromTokens(
									params.getString(i), null));
						}
						function.returnValue = this.getParamFromTokens(
								func.getString("return-type"), null);
						return function;

					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	private ReturnValue parseStatement(CommonTree tree) {
		try {
			return parseStatement2(tree);
		} catch (StatementException e) {
			throw e;
		} catch (RuntimeException e) {
			throw new StatementException(getError(tree.getLine(),
					e.getMessage()));
		}
	}

	private ReturnValue parseStatement2(CommonTree tree) {

		ReturnValue returnValue = new ReturnValue(ReturnType.Void);
		int type = tree.getType();
		int count = tree.getChildCount();
		boolean valid = false;
		switch (type) {
		case MEngineParser.TypeDef: {
			if (!this.compilingLib) {
				throw new RuntimeException("TYPEDEF can only occur in lib");
			}
			if (curDef == null) {
				String classId = tree.getChild(0).getText();
				curDef = new TypeDef();
				curDef.name = classId;
				for (int i = 1; i < count; i += 2) {
					FunctionParam p = getParamFromTokens(tree.getChild(i)
							.getText(), tree.getChild(i + 1).getText());
					curDef.params.add(p);
				}
				try {
					JSONObject thing = new JSONObject();
					JSONObject cdef = new JSONObject();
					cdef.put("name", curDef.name);
					JSONArray pArr = new JSONArray();
					for (int i = 0; i < curDef.params.size(); i++) {
						FunctionParam p = curDef.params.get(i);
						JSONObject pj = new JSONObject();
						pj.put("name", p.name);
						pj.put("type", typeToString(p));
						pArr.put(pj);
					}
					cdef.put("params", pArr);
					thing.put("class-def", cdef);
					libs.put(getLibName(), thing);
					valid = true;
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				throw new RuntimeException(
						"only one typedef allowed in each lib file");
			}
			break;
		}
		case MEngineParser.Define: {
			String ttype = tree.getChild(0).getText();
			String id = tree.getChild(1).getText();
			JSONObject tObj = getDefObj(ttype);
			if (tObj != null) {
				try {
					JSONObject cObj = tObj.getJSONObject("class-def");
					JSONArray pArr = cObj.getJSONArray("params");
					ReturnValue array = new ReturnValue(ReturnType.Array);
					array.defType = ttype;
					array.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.INITOBJ, new int[] { 12,
							array.index, pArr.length() });
					valid = true;
					curContext.addVar(id, ReturnType.Array, ttype);
					curContext.assignVar(id, array);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				throw new RuntimeException("invalid define statement");
			}
			break;
		}
		case MEngineParser.Const: {
			if (count == 2) {
				if (tree.getChild(0).getType() == MEngineParser.Identifier
						&& tree.getChild(1).getType() == MEngineParser.NumericLiteral) {
					valid = true;
					String id = tree.getChild(0).getText();
					checkDupName(id);
					intConsts.put(id,
							Integer.parseInt(tree.getChild(1).getText()));
				}
			}
			if (!valid) {
				throw new RuntimeException("invalid const definition");
			}
			break;
		}
		case MEngineParser.PTINT: {
			String id = tree.getChild(0).getText();
			curContext.addVar(id, ReturnType.Int, null);
			if (count == 2) {
				if (tree.getChild(1).getType() == MEngineParser.EQ) {
					ReturnValue value = parseStatement((CommonTree) tree
							.getChild(1));
					valid = true;
					curContext.assignVar(id, value);
				}
			} else if (count == 1) {
				valid = true;
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid int vairable initialize for : " + id);
			}
			break;
		}
		case MEngineParser.PTSTRING: {
			String id = tree.getChild(0).getText();
			curContext.addVar(id, ReturnType.Str, null);
			if (count == 2) {
				if (tree.getChild(1).getType() == MEngineParser.EQ) {
					ReturnValue value = parseStatement((CommonTree) tree
							.getChild(1));

					valid = true;
					curContext.assignVar(id, value);

				}
			} else if (count == 1) {
				valid = true;
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid String vairable initialize for : " + id);
			}
			break;
		}
		case MEngineParser.PTARRAY: {
			String id = tree.getChild(0).getText();
			curContext.addVar(id, ReturnType.Array, null);
			if (count == 2) {
				if (tree.getChild(1).getType() == MEngineParser.EQ) {
					ReturnValue value = parseStatement((CommonTree) tree
							.getChild(1));
					if (value.type == ReturnType.Str) {

						curContext.assignVar(id, value);
						valid = true;
					} else if (value.type == ReturnType.Array) {
						valid = true;
						curContext.assignVar(id, value);
					}

				}
			} else if (count == 1) {
				valid = true;
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid Array vairable initialize for : " + id);
			}

			break;
		}
		case MEngineParser.PTMAP: {
			String id = tree.getChild(0).getText();
			curContext.addVar(id, ReturnType.Map, null);
			if (count == 2) {
				if (tree.getChild(1).getType() == MEngineParser.EQ) {
					ReturnValue value = parseStatement((CommonTree) tree
							.getChild(1));
					if (value.type == ReturnType.Str) {

						curContext.assignVar(id, value);
						valid = true;
					} else if (value.type == ReturnType.Map) {
						valid = true;
						curContext.assignVar(id, value);
					}
				}
			} else if (count == 1) {
				valid = true;
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid Map vairable initialize for : " + id);
			}
			break;
		}
		case MEngineParser.PTRECT: {
			String id = tree.getChild(0).getText();
			curContext.addVar(id, ReturnType.Rect, null);
			if (count == 2) {
				if (tree.getChild(1).getType() == MEngineParser.EQ) {
					ReturnValue value = parseStatement((CommonTree) tree
							.getChild(1));
					valid = true;
					curContext.assignVar(id, value);
				}
			} else if (count == 1) {
				valid = true;
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid MRect vairable initialize for : " + id);
			}
			break;
		}
		case MEngineParser.PTPLAYER: {
			String id = tree.getChild(0).getText();
			curContext.addVar(id, ReturnType.Player, null);
			if (count == 2) {
				if (tree.getChild(1).getType() == MEngineParser.EQ) {
					ReturnValue value = parseStatement((CommonTree) tree
							.getChild(1));
					valid = true;
					curContext.assignVar(id, value);
				}
			} else if (count == 1) {
				valid = true;
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid MPlayer vairable initialize for : " + id);
			}
			break;
		}
		case MEngineParser.PTTEXT: {
			String id = tree.getChild(0).getText();
			curContext.addVar(id, ReturnType.Text, null);
			if (count == 2) {
				if (tree.getChild(1).getType() == MEngineParser.EQ) {
					ReturnValue value = parseStatement((CommonTree) tree
							.getChild(1));
					valid = true;
					curContext.assignVar(id, value);
				}
			} else if (count == 1) {
				valid = true;
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid MText vairable initialize for : " + id);
			}
			break;
		}
		case MEngineParser.PTGROUP: {
			String id = tree.getChild(0).getText();
			curContext.addVar(id, ReturnType.Group, null);
			if (count == 2) {
				if (tree.getChild(1).getType() == MEngineParser.EQ) {
					ReturnValue value = parseStatement((CommonTree) tree
							.getChild(1));
					valid = true;
					curContext.assignVar(id, value);
				}
			} else if (count == 1) {
				valid = true;
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid MGroup vairable initialize for : " + id);
			}
			break;
		}
		case MEngineParser.PTTILES: {
			String id = tree.getChild(0).getText();
			curContext.addVar(id, ReturnType.Tiles, null);
			if (count == 2) {
				if (tree.getChild(1).getType() == MEngineParser.EQ) {
					ReturnValue value = parseStatement((CommonTree) tree
							.getChild(1));
					valid = true;
					curContext.assignVar(id, value);
				}
			} else if (count == 1) {
				valid = true;
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid MTiles vairable initialize for : " + id);
			}
			break;
		}
		case MEngineParser.PTELEMENT: {
			String id = tree.getChild(0).getText();
			curContext.addVar(id, ReturnType.Element, null);
			if (count == 2) {
				if (tree.getChild(1).getType() == MEngineParser.EQ) {
					ReturnValue value = parseStatement((CommonTree) tree
							.getChild(1));
					valid = true;
					curContext.assignVar(id, value);
				}
			} else if (count == 1) {
				valid = true;
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid Element vairable initialize for : " + id);
			}
			break;
		}
		case MEngineParser.SetBackgroundColor: {
			if (count == 1) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				if (value0.type == ReturnType.Int) {
					valid = true;
					curContext.addAsmLine(ByteCode.SETENV, new int[] {
							value0.index, 0 });
				}
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid void setBackgroundColor(int)");
			}
			break;
		}
		case MEngineParser.PlaySound: {
			if (count == 2) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue value1 = parseStatement((CommonTree) tree
						.getChild(1));
				if (value0.type == ReturnType.Str
						&& value1.type == ReturnType.Int) {
					valid = true;
					returnValue.type = ReturnType.Int;
					returnValue.index = curContext.getTempVar();

					curContext.addAsmLine(ByteCode.PLAYSOUND, new int[] {
							returnValue.index, value0.index, value1.index });
				}
			}
			if (!valid) {
				throw new RuntimeException("invalid int playSound(Str,int)");
			}
			break;
		}
		case MEngineParser.StopSound: {
			if (count == 1) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				if (value0.type == ReturnType.Int) {
					valid = true;

					curContext.addAsmLine(ByteCode.STOPSOUND,
							new int[] { value0.index });
				}
			}
			if (!valid) {
				throw new RuntimeException("invalid void stopSound(int)");
			}
			break;
		}
		case MEngineParser.PlayScene: {
			if (count == 3) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue value1 = parseStatement((CommonTree) tree
						.getChild(1));
				ReturnValue value2 = parseStatement((CommonTree) tree
						.getChild(2));
				if (value0.type == ReturnType.Str
						&& value1.type == ReturnType.Int
						&& value2.type == ReturnType.Map) {
					valid = true;
					curContext.addAsmLine(ByteCode.PLAYSCENE, new int[] { 0,
							value0.index, value1.index, value2.index });
				}
			}
			if (!valid) {
				throw new RuntimeException("invalid void playScene(String)");
			}
			break;
		}
		case MEngineParser.LoadSound: {
			if (count == 1) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				if (value0.type == ReturnType.Str) {
					valid = true;
					curContext.addAsmLine(ByteCode.LOADSOUND,
							new int[] { value0.index });
				}
			}
			if (!valid) {
				throw new RuntimeException("invalid void loadSound(Str)");
			}
			break;
		}
		case MEngineParser.UnloadSound: {
			if (count == 1) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				if (value0.type == ReturnType.Str) {
					valid = true;
					curContext.addAsmLine(ByteCode.UNLOADSOUND,
							new int[] { value0.index });
				}
			}
			if (!valid) {
				throw new RuntimeException("invalid void unloadSound(Str)");
			}
			break;
		}
		case MEngineParser.SendHttpData: {
			ReturnValue value0 = parseStatement((CommonTree) tree.getChild(0));
			ReturnValue value1 = parseStatement((CommonTree) tree.getChild(1));
			ReturnValue value2 = parseStatement((CommonTree) tree.getChild(2));
			ReturnValue value3 = null;
			if (count == 4) {
				value3 = parseStatement((CommonTree) tree.getChild(3));
				if (value0.type == ReturnType.Str
						&& value1.type == ReturnType.Str
						&& value2.type == ReturnType.Str
						&& value3.type == ReturnType.Int) {
					valid = true;
					// sendHttpData(url, key ,data);
					curContext.addAsmLine(ByteCode.SENDHTTPDATA, new int[] {
							value0.index, value1.index, value2.index,
							value3.index });
				}
			} else {
				if (value0.type == ReturnType.Str
						&& value1.type == ReturnType.Str
						&& value2.type == ReturnType.Str) {
					valid = true;
					// sendHttpData(url, key ,data);
					curContext.addAsmLine(ByteCode.SENDHTTPDATA, new int[] {
							value0.index, value1.index, value2.index });
				}
			}

			break;
		}
		case MEngineParser.SendData: {
			if (count == 2) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue value1 = parseStatement((CommonTree) tree
						.getChild(1));
				if (value0.type == ReturnType.Str
						&& value1.type == ReturnType.Str) {
					valid = true;
					curContext.addAsmLine(ByteCode.SENDDATA, new int[] {
							value0.index, value1.index });
				}

			} else if (count == 3) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue value1 = parseStatement((CommonTree) tree
						.getChild(1));
				ReturnValue value2 = parseStatement((CommonTree) tree
						.getChild(2));
				if (value0.type == ReturnType.Str
						&& value1.type == ReturnType.Str
						&& value2.type == ReturnType.Str) {
					valid = true;
					curContext.addAsmLine(ByteCode.SENDDATA, new int[] {
							value0.index, value1.index, value2.index });
				}
			}
			if (!valid) {
				throw new RuntimeException(
						"sendData(String key , String value)");
			}
			break;
		}
		case MEngineParser.SendCommand: {
			ReturnValue value0 = parseStatement((CommonTree) tree.getChild(0));
			ReturnValue value1 = parseStatement((CommonTree) tree.getChild(1));
			if (value0.type == ReturnType.Str && value1.type == ReturnType.Map) {
				valid = true;
				curContext.addAsmLine(ByteCode.SENDDATA, new int[] {
						value0.index, value1.index });
			}
			if (!valid) {
				throw new RuntimeException(
						"sendCommand(String target, Map data)");
			}
			break;
		}
		case MEngineParser.LoadResource: {
			ReturnValue value0 = parseStatement((CommonTree) tree.getChild(0));
			ReturnValue value1 = parseStatement((CommonTree) tree.getChild(1));
			ReturnValue value2 = parseStatement((CommonTree) tree.getChild(2));
			if (value0.type == ReturnType.Str && value1.type == ReturnType.Int
					&& value2.type == ReturnType.Int) {
				valid = true;
				curContext.addAsmLine(ByteCode.LOADRESOURCE, new int[] {
						value0.index, value1.index, value2.index, });
			}
			if (!valid) {
				throw new RuntimeException(
						"invlaid loadResource input parameters");
			}
			break;
		}
		case MEngineParser.GetLoadingProgress: {
			returnValue.type = ReturnType.Int;
			returnValue.index = curContext.getTempVar();
			valid = true;
			curContext.addAsmLine(ByteCode.GETENV, new int[] {
					returnValue.index, 7 });

			break;
		}
		case MEngineParser.GetQuickInput: {
			if (count == 4) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue value1 = parseStatement((CommonTree) tree
						.getChild(1));
				ReturnValue value2 = parseStatement((CommonTree) tree
						.getChild(2));
				ReturnValue value3 = parseStatement((CommonTree) tree
						.getChild(3));
				if (value0.type == ReturnType.Str
						&& value1.type == ReturnType.Str
						&& value2.type == ReturnType.Int
						&& value3.type == ReturnType.Int) {
					valid = true;
					returnValue.type = ReturnType.Str;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.QUICKINPUT, new int[] {
							returnValue.index, value0.index, value1.index,
							value2.index, value3.index });

				}
			}
			break;
		}
		case MEngineParser.GetTimeStamp: {
			returnValue.type = ReturnType.Int;
			returnValue.index = curContext.getTempVar();
			curContext.addAsmLine(ByteCode.GETTIMESTAMP, new int[] {
					returnValue.index, 0 });
			valid = true;
			break;
		}
		case MEngineParser.GetTimeElapsedInMilliseconds: {
			returnValue.type = ReturnType.Int;
			returnValue.index = curContext.getTempVar();
			curContext.addAsmLine(ByteCode.GETTIMESTAMP, new int[] {
					returnValue.index, 1 });
			valid = true;
			break;
		}
		case MEngineParser.InitArray: {
			returnValue.type = ReturnType.Array;
			returnValue.index = curContext.getTempVar();
			curContext.addAsmLine(ByteCode.INITOBJ, new int[] { 10,
					returnValue.index });
			valid = true;
			break;
		}
		case MEngineParser.InitMap: {
			returnValue.type = ReturnType.Map;
			returnValue.index = curContext.getTempVar();
			curContext.addAsmLine(ByteCode.INITOBJ, new int[] { 11,
					returnValue.index });
			valid = true;
			break;
		}
		case MEngineParser.InitPlayer: {
			if (tree.getChildCount() == 1) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				if (value0.type == ReturnType.Rect) {
					valid = true;
					returnValue.type = ReturnType.Player;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.INITOBJ, new int[] { 4,
							returnValue.index, value0.index });

				} else if (value0.type == ReturnType.Player) {
					valid = true;
					returnValue.type = ReturnType.Player;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.INITOBJ, new int[] { 3,
							returnValue.index, value0.index });
				}
				if (!valid) {
					throw new RuntimeException("initPlayer(MRect|MPlayer)");
				}
			} else if (tree.getChildCount() == 4) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue value1 = parseStatement((CommonTree) tree
						.getChild(1));
				ReturnValue value2 = parseStatement((CommonTree) tree
						.getChild(2));
				ReturnValue value3 = parseStatement((CommonTree) tree
						.getChild(3));
				if (value0.type == ReturnType.Int
						&& value1.type == ReturnType.Int
						&& value2.type == ReturnType.Int
						&& value3.type == ReturnType.Int) {

					valid = true;
					returnValue.type = ReturnType.Player;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.INITOBJ, new int[] { 2,
							returnValue.index, value0.index, value1.index,
							value2.index, value3.index });
				}
				if (!valid) {
					throw new RuntimeException("initPlayer(int,int,int,int)");
				}
			}
			if (!valid) {
				throw new RuntimeException("invalid initPlayer parameters");
			}
			break;
		}
		case MEngineParser.InitRect: {
			if (tree.getChildCount() == 4) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue value1 = parseStatement((CommonTree) tree
						.getChild(1));
				ReturnValue value2 = parseStatement((CommonTree) tree
						.getChild(2));
				ReturnValue value3 = parseStatement((CommonTree) tree
						.getChild(3));
				if (value0.type == ReturnType.Int
						&& value1.type == ReturnType.Int
						&& value2.type == ReturnType.Int
						&& value3.type == ReturnType.Int) {
					valid = true;
					returnValue.type = ReturnType.Rect;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.INITOBJ, new int[] { 0,
							returnValue.index, value0.index, value1.index,
							value2.index, value3.index });
				}
				if (!valid) {
					throw new RuntimeException("initRect(int,int,int,int)");
				}
			} else if (tree.getChildCount() == 1) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				if (value0.type == ReturnType.Rect) {
					valid = true;
					returnValue.type = ReturnType.Rect;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.INITOBJ, new int[] { 1,
							returnValue.index, value0.index });
				}
				if (!valid) {
					throw new RuntimeException("initRect(MRect)");
				}
			}
			if (!valid) {
				throw new RuntimeException("invalid initRect parameters");
			}
			break;
		}
		case MEngineParser.InitText: {
			if (tree.getChildCount() == 3) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue value1 = parseStatement((CommonTree) tree
						.getChild(1));
				ReturnValue value2 = parseStatement((CommonTree) tree
						.getChild(2));
				if (value0.type == ReturnType.Str
						&& value1.type == ReturnType.Int
						&& value2.type == ReturnType.Int) {

					valid = true;
					returnValue.type = ReturnType.Text;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.INITOBJ, new int[] { 5,
							returnValue.index, value0.index, value1.index,
							value2.index });
				}
			} else if (tree.getChildCount() == 1) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				if (value0.type == ReturnType.Text) {
					valid = true;
					returnValue.type = ReturnType.Text;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.INITOBJ, new int[] { 6,
							returnValue.index, value0.index });
				}
			}
			if (!valid) {
				throw new RuntimeException("initText(String,int,int)");
			}
			break;
		}
		case MEngineParser.InitGroup: {
			if (tree.getChildCount() == 2) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue value1 = parseStatement((CommonTree) tree
						.getChild(1));
				if (value0.type == ReturnType.Int
						&& value1.type == ReturnType.Int) {

					valid = true;
					returnValue.type = ReturnType.Group;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.INITOBJ, new int[] { 8,
							returnValue.index, value0.index, value1.index });
				}
				if (!valid) {
					throw new RuntimeException("initGroup(int,int)");
				}
			} else if (tree.getChildCount() == 1) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				if (value0.type == ReturnType.Group) {
					valid = true;
					returnValue.type = ReturnType.Group;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.INITOBJ, new int[] { 7,
							returnValue.index, value0.index });
				}
				if (!valid) {
					throw new RuntimeException("initGroup(MGroup)");
				}
			}
			break;
		}
		case MEngineParser.InitTiles: {
			if (tree.getChildCount() == 5) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue value1 = parseStatement((CommonTree) tree
						.getChild(1));
				ReturnValue value2 = parseStatement((CommonTree) tree
						.getChild(2));
				ReturnValue value3 = parseStatement((CommonTree) tree
						.getChild(3));
				ReturnValue value4 = parseStatement((CommonTree) tree
						.getChild(4));
				if (value0.type == ReturnType.Str
						&& value1.type == ReturnType.Int
						&& value2.type == ReturnType.Int
						&& value3.type == ReturnType.Int
						&& value4.type == ReturnType.Int) {

					valid = true;
					returnValue.type = ReturnType.Tiles;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.INITOBJ, new int[] { 9,
							returnValue.index, value0.index, value1.index,
							value2.index, value3.index, value4.index });
				}
				if (!valid) {
					throw new RuntimeException(
							"initTiles(String,int,int,int,int)");
				}
			} else if (tree.getChildCount() == 1) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				if (value0.type == ReturnType.Str) {

					valid = true;
					returnValue.type = ReturnType.Tiles;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.INITOBJ, new int[] { 9,
							returnValue.index, value0.index, -1, -1, -1, -1 });
				}
				if (!valid) {
					throw new RuntimeException("initTiles(String)");
				}
			}
			break;
		}
		case MEngineParser.RandExp: {
			ReturnValue value0 = parseStatement((CommonTree) tree.getChild(0));
			ReturnValue value1 = parseStatement((CommonTree) tree.getChild(1));
			if (value0.type == ReturnType.Int && value1.type == ReturnType.Int) {
				valid = true;
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.RAND, new int[] {
						returnValue.index, value0.index, value1.index });
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid parameters for random(int,int)");
			}
			break;
		}
		case MEngineParser.Global: {
			returnValue.type = ReturnType.Player;
			returnValue.index = 0;
			valid = true;
			break;
		}
		case MEngineParser.GetCurrentFocus: {
			valid = true;
			returnValue.type = ReturnType.Player;
			returnValue.index = curContext.getTempVar();
			curContext.addAsmLine(ByteCode.NAVIGATE, new int[] { 2,
					returnValue.index });
			break;
		}
		case MEngineParser.GetScreenExp: {
			returnValue.type = ReturnType.Group;
			returnValue.index = curContext.getTempVar();
			curContext.addAsmLine(ByteCode.GETSCREEN,
					new int[] { returnValue.index });
			valid = true;
			break;
		}
		case MEngineParser.GetEnvVar: {
			returnValue.type = ReturnType.Map;
			returnValue.index = curContext.getTempVar();
			ReturnValue value0 = parseStatement((CommonTree) tree.getChild(0));
			if (value0.type == ReturnType.Map) {
				curContext.addAsmLine(ByteCode.QUERYENV, new int[] {
						returnValue.index, value0.index });
				valid = true;
			}

			break;
		}
		case MEngineParser.GetPage: {
			returnValue.type = ReturnType.Element;
			returnValue.index = curContext.getTempVar();
			curContext.addAsmLine(ByteCode.GETELEMENT, new int[] {
					returnValue.index, -2 });
			valid = true;
			break;
		}

		case MEngineParser.GetRoot: {
			returnValue.type = ReturnType.Element;
			returnValue.index = curContext.getTempVar();
			curContext.addAsmLine(ByteCode.GETELEMENT, new int[] {
					returnValue.index, -1 });
			valid = true;
			break;
		}
		case MEngineParser.RGB: {
			if (tree.getChildCount() == 3) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue value1 = parseStatement((CommonTree) tree
						.getChild(1));
				ReturnValue value2 = parseStatement((CommonTree) tree
						.getChild(2));
				if (value0.type == ReturnType.Int
						&& value1.type == ReturnType.Int
						&& value2.type == ReturnType.Int) {
					valid = true;
					returnValue.index = curContext.getTempVar();
					returnValue.type = ReturnType.Int;
					curContext.addAsmLine(ByteCode.MOVINT, new int[] {
							returnValue.index, 255 });
					curContext.addAsmLine(ByteCode.ARGB, new int[] {
							returnValue.index, returnValue.index, value0.index,
							value1.index, value2.index, });
				}
			}
			if (!valid) {
				throw new RuntimeException("invalid RGB calling");
			}
			break;
		}
		case MEngineParser.ARGB: {
			if (tree.getChildCount() == 4) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue value1 = parseStatement((CommonTree) tree
						.getChild(1));
				ReturnValue value2 = parseStatement((CommonTree) tree
						.getChild(2));
				ReturnValue value3 = parseStatement((CommonTree) tree
						.getChild(3));
				if (value0.type == ReturnType.Int
						&& value1.type == ReturnType.Int
						&& value2.type == ReturnType.Int
						& value3.type == ReturnType.Int) {
					valid = true;
					returnValue.index = curContext.getTempVar();
					returnValue.type = ReturnType.Int;
					curContext.addAsmLine(ByteCode.ARGB, new int[] {
							returnValue.index, value0.index, value1.index,
							value2.index, value3.index });
				}
			}
			if (!valid) {
				throw new RuntimeException("invalid ARGB calling");
			}
			break;
		}
		case MEngineParser.Mask: {
			if (count == 3) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue value1 = parseStatement((CommonTree) tree
						.getChild(1));
				ReturnValue value2 = parseStatement((CommonTree) tree
						.getChild(2));
				if (value0.type == ReturnType.Str
						&& value1.type == ReturnType.Str
						&& value2.type == ReturnType.Int) {
					valid = true;
					curContext.addAsmLine(ByteCode.MASK, new int[] {
							value0.index, value1.index, value2.index });
				}
				if (!valid) {
					throw new RuntimeException(
							"invalid parameters for void Mask(String,String,int)");
				}
			}
			break;
		}
		case MEngineParser.Point: {
			ReturnValue value0 = parseStatement((CommonTree) tree.getChild(0));
			CommonTree middle = (CommonTree) tree.getChild(1);
			if (value0.type == ReturnType.Array && value0.defType != null) {
				String methodName = middle.getText();
				JSONObject tObj = getDefObj(value0.defType);
				try {
					JSONObject cObj = tObj.getJSONObject("class-def");
					JSONArray pArr = cObj.getJSONArray("params");
					if (tObj != null) {
						if (tObj.has("class-def")) {

							if (tree.getChildCount() > 2) {
								boolean found = false;

								CommonTree args = (CommonTree) tree.getChild(2);

								int pc = pArr.length();

								for (int i = 0; i < pc; i++) {

									JSONObject item = pArr.getJSONObject(i);
									FunctionParam thing = this
											.getParamFromTokens(
													item.getString("type"),
													item.getString("name"));

									if (thing.name.equals(methodName)) {
										ReturnValue value1 = parseStatement((CommonTree) args
												.getChild(0));
										if (thing.type == value1.type) {
											valid = true;
											int index = curContext.getTempVar();
											curContext.addAsmLine(
													ByteCode.MOVINT, new int[] {
															index, i });
											curContext
													.addAsmLine(
															ByteCode.ASET,
															new int[] {
																	value0.index,
																	index,
																	value1.index,
																	thing.type
																			.ordinal() });
										}
										found = true;
										break;
									}
								}

								if (!found) {
									JSONObject fObj = tObj
											.getJSONObject("functions");
									JSONObject function = fObj
											.getJSONObject(methodName);
									JSONArray params = function
											.getJSONArray("params");
									int paramSize = params.length();
									int[] callInputs = new int[2 + paramSize];
									int temp = curContext.getTempVar();
									curContext.addAsmLine(
											ByteCode.MOVINT,
											new int[] { temp,
													function.getInt("isp") });
									callInputs[0] = temp;
									callInputs[1] = this
											.getStringConstIndex(getLibName(value0.defType));

									for (int i = 0, j = 0; i < paramSize; i++, j++) {
										ReturnValue thing = parseStatement((CommonTree) args
												.getChild(j));
										FunctionParam param = this
												.getParamFromTokens(
														params.getString(i),
														null);
										if (i == 0
												&& (thing.defType == null || !thing.defType
														.equals(value0.defType))
												&& param.type == ReturnType.Array
												&& param.defType != null
												&& param.defType
														.equals(value0.defType)) {
											callInputs[i + 2] = value0.index;
											j--;
											continue;
										}
										if (param.type != thing.type) {

											throw new RuntimeException(
													"parameter unmatching for "
															+ value0.defType
															+ "->" + methodName);
										}
										callInputs[i + 2] = thing.index;

									}
									curContext.addAsmLine(ByteCode.CALL,
											callInputs);
									int ret = curContext.getTempVar();
									curContext.addAsmLine(ByteCode.COPYVAR,
											new int[] { ret, 1, 0, 1 });

									returnValue.index = ret;
									returnValue.type = this.getParamFromTokens(
											function.getString("return-type"),
											"").type;

									valid = true;

								}
							} else {
								int pc = pArr.length();
								boolean found = false;
								for (int i = 0; i < pc; i++) {
									JSONObject item = pArr.getJSONObject(i);
									String iname = item.getString("name");

									if (iname.equals(methodName)) {
										String itype = item.getString("type");
										FunctionParam thing = this
												.getParamFromTokens(itype,
														iname);
										valid = true;
										returnValue.type = thing.type;
										returnValue.index = curContext
												.getTempVar();
										int index = curContext.getTempVar();
										curContext.addAsmLine(ByteCode.MOVINT,
												new int[] { index, i });
										curContext.addAsmLine(
												ByteCode.AGET,
												new int[] {
														returnValue.index,
														value0.index,
														index,
														returnValue.type
																.ordinal() });
										found = true;
										break;
									}

								}
								if (!found) {
									JSONObject fObj = tObj
											.getJSONObject("functions");
									if (fObj.has(methodName)) {
										JSONObject function = fObj
												.getJSONObject(methodName);
										JSONArray params = function
												.getJSONArray("params");
										int paramSize = params.length();
										if (paramSize == 1) {
											FunctionParam param = this
													.getParamFromTokens(
															params.getString(0),
															null);
											if (param.type == ReturnType.Array
													&& param.defType != null
													&& param.defType
															.equals(value0.defType)) {
												int[] callInputs = new int[2 + paramSize];
												int temp = curContext
														.getTempVar();
												curContext
														.addAsmLine(
																ByteCode.MOVINT,
																new int[] {
																		temp,
																		function.getInt("isp") });
												callInputs[0] = temp;
												callInputs[1] = this
														.getStringConstIndex(getLibName(value0.defType));
												callInputs[2] = value0.index;

												curContext.addAsmLine(
														ByteCode.CALL,
														callInputs);
												int ret = curContext
														.getTempVar();
												curContext.addAsmLine(
														ByteCode.COPYVAR,
														new int[] { ret, 1, 0,
																1 });

												returnValue.index = ret;
												returnValue.type = this
														.getParamFromTokens(
																function.getString("return-type"),
																"").type;

												valid = true;
											}
										}
									} else {
										throw new Exception("unknown method :"
												+ methodName);
									}

								}
							}

						}

					}

				} catch (Exception e) {
				}

			}
			break;

		}
		case MEngineParser.Dot: {
			CommonTree left = (CommonTree) tree.getChild(0);
			CommonTree right = (CommonTree) tree.getChild(1);
			valid = doDot(left, right, returnValue, type);
			break;

		}
		case MEngineParser.GetUsername: {
			returnValue.type = ReturnType.Str;
			returnValue.index = curContext.getTempVar();
			valid = true;
			curContext.addAsmLine(ByteCode.GETENV, new int[] {
					returnValue.index, 8 });
			break;
		}
		case MEngineParser.GetPassword: {
			returnValue.type = ReturnType.Str;
			returnValue.index = curContext.getTempVar();
			valid = true;
			curContext.addAsmLine(ByteCode.GETENV, new int[] {
					returnValue.index, 9 });
			break;
		}
		case MEngineParser.GetVersion: {
			returnValue.type = ReturnType.Array;
			returnValue.index = curContext.getTempVar();
			valid = true;
			curContext.addAsmLine(ByteCode.GETENV, new int[] {
					returnValue.index, 11 });
			break;
		}
		case MEngineParser.GetRequiredVersion: {
			returnValue.type = ReturnType.Array;
			returnValue.index = curContext.getTempVar();
			valid = true;
			curContext.addAsmLine(ByteCode.GETENV, new int[] {
					returnValue.index, 12 });
			break;
		}
		case MEngineParser.GetAutoRegParams: {
			returnValue.type = ReturnType.Map;
			returnValue.index = curContext.getTempVar();
			if (count == 0) {
				valid = true;
				curContext.addAsmLine(ByteCode.GETENV, new int[] {
						returnValue.index, 13 });
			} else if (count == 2) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue value1 = parseStatement((CommonTree) tree
						.getChild(1));
				if (value0.type == ReturnType.Str
						&& value1.type == ReturnType.Str) {
					valid = true;
					curContext
							.addAsmLine(ByteCode.GETENV, new int[] {
									returnValue.index, 14, value0.index,
									value1.index });
				}
			}
			break;
		}
		case MEngineParser.GetScreenWidth: {
			returnValue.type = ReturnType.Int;
			returnValue.index = curContext.getTempVar();
			valid = true;
			curContext.addAsmLine(ByteCode.GETENV, new int[] {
					returnValue.index, 0 });
			break;
		}
		case MEngineParser.GetScreenHeight: {
			returnValue.type = ReturnType.Int;
			returnValue.index = curContext.getTempVar();
			valid = true;
			curContext.addAsmLine(ByteCode.GETENV, new int[] {
					returnValue.index, 1 });
			break;
		}
		case MEngineParser.GetPlatform: {
			returnValue.type = ReturnType.Int;
			returnValue.index = curContext.getTempVar();
			valid = true;
			curContext.addAsmLine(ByteCode.GETENV, new int[] {
					returnValue.index, 2 });
			break;
		}
		case MEngineParser.IsTouchSupported: {
			returnValue.type = ReturnType.Int;
			returnValue.index = curContext.getTempVar();
			valid = true;
			curContext.addAsmLine(ByteCode.GETENV, new int[] {
					returnValue.index, 3 });
			break;
		}
		case MEngineParser.GetMyUserId: {
			returnValue.type = ReturnType.Int;
			returnValue.index = curContext.getTempVar();
			valid = true;
			curContext.addAsmLine(ByteCode.GETENV, new int[] {
					returnValue.index, 4 });
			break;
		}
		case MEngineParser.GetStartParams: {
			returnValue.type = ReturnType.Map;
			returnValue.index = curContext.getTempVar();
			valid = true;
			curContext.addAsmLine(ByteCode.GETENV, new int[] {
					returnValue.index, 5 });
			break;
		}
		case MEngineParser.GetLanguage: {
			returnValue.type = ReturnType.Str;
			returnValue.index = curContext.getTempVar();
			valid = true;
			curContext.addAsmLine(ByteCode.GETENV, new int[] {
					returnValue.index, 10 });
			break;
		}
		case MEngineParser.Translate: {
			returnValue.type = ReturnType.Str;
			returnValue.index = curContext.getTempVar();
			if (count == 2) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue value1 = parseStatement((CommonTree) tree
						.getChild(1));
				if (value0.type == ReturnType.Str
						&& value1.type == ReturnType.Array) {
					valid = true;
					curContext.addAsmLine(ByteCode.GETENV, new int[] {
							returnValue.index, 6, value0.index, value1.index });
				}
			} else if (count == 1) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				if (value0.type == ReturnType.Str) {
					valid = true;
					curContext.addAsmLine(ByteCode.GETENV, new int[] {
							returnValue.index, 6, value0.index });
				}
			}

			if (!valid) {
				throw new RuntimeException("invalid translate calling");
			}
			break;
		}

		case MEngineParser.GetImageWidth: {
			CommonTree arg = (CommonTree) tree.getChild(0);
			ReturnValue thing = parseStatement(arg);
			if (thing.type == ReturnType.Str) {
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETIMAGESIZE, new int[] {
						returnValue.index, 0, thing.index });
				valid = true;
			}

			break;
		}
		case MEngineParser.GetImageHeight: {
			CommonTree arg = (CommonTree) tree.getChild(0);
			ReturnValue thing = parseStatement(arg);
			if (thing.type == ReturnType.Str) {
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETIMAGESIZE, new int[] {
						returnValue.index, 1, thing.index });
				valid = true;
			}
			break;
		}
		case MEngineParser.EQ: {
			if (count == 2) {
				// assignment
				CommonTree left = (CommonTree) tree.getChild(0);
				if (left.getType() != MEngineParser.Identifier) {
					throw new RuntimeException("invalid lvalue : "
							+ left.getText() + " at line : " + left.getLine());
				}

				ReturnType leftType = curContext.getIdType(left.toString());
				if (leftType == ReturnType.Void) {
					throw new RuntimeException("invalid lvalue : "
							+ left.getText() + " at line : " + left.getLine());
				}

				CommonTree right = (CommonTree) tree.getChild(1);
				ReturnValue value = parseStatement(right);
				String id = left.getText();
				curContext.assignVar(id, value);
				valid = true;
			} else if (count == 1) {
				CommonTree right = (CommonTree) tree.getChild(0);
				returnValue = parseStatement(right);
				valid = true;
			}
			break;
		}

		case MEngineParser.SetTimeOut: {
			CommonTree event = (CommonTree) tree.getChild(0);
			CommonTree delay = (CommonTree) tree.getChild(1);
			ReturnValue delayReturn = parseStatement(delay);
			Function function = getFunctionById(event.getText());
			if (event.getType() == MEngineParser.Identifier && function != null
					&& delayReturn.type == ReturnType.Int) {
				int temp = curContext.getTempVar();
				if (function.isp == -1) {

					String func = event.getText();
					curContext.asmCodes.add("LBLTOINT " + temp + " FUNC_ENTRY_"
							+ func);
					functionCalls.add(func);
					returnValue.type = ReturnType.Int;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(
							ByteCode.SETTIMEOUT,
							new int[] { returnValue.index, temp,
									this.getStringConstIndex(function.lib),
									delayReturn.index });
				} else {
					returnValue.type = ReturnType.Int;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.MOVINT, new int[] { temp,
							function.isp });
					curContext.addAsmLine(
							ByteCode.SETTIMEOUT,
							new int[] { returnValue.index, temp,
									this.getStringConstIndex(function.lib),
									delayReturn.index });
				}
				valid = true;
			}
			break;
		}
		case MEngineParser.CancelTimeOut: {
			CommonTree param = (CommonTree) tree.getChild(0);
			ReturnValue temp = parseStatement(param);
			if (temp.type == ReturnType.Int) {
				returnValue.type = ReturnType.Void;
				curContext.addAsmLine(ByteCode.CANCELTIMEOUT,
						new int[] { temp.index });
				valid = true;
			}
			break;
		}
		case MEngineParser.Identifier: {
			String id = tree.getText();
			if (intConsts.containsKey(id)) {
				valid = true;
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.MOVINT, new int[] {
						returnValue.index, intConsts.get(id) });
				break;
			}

			returnValue = curContext.loadVar(id);

			switch (returnValue.type) {
			case Int:
			case Str:
			case Array:
			case Map:
			case Rect:
			case Player:
			case Text:
			case Group:
			case Tiles:
			case Page:
			case Element: {
				valid = true;
				break;
			}
			default: {
				if (id.equals("_t")) {
					int brk = 1;
					int a = brk;
				}
				if (isFunction(id)) {
					valid = true;
					Function function = getFunctionById(id);
					if (tree.getChildCount() == 0) {
						returnValue.type = ReturnType.Void;
					} else {
						CommonTree params = (CommonTree) tree.getChild(0);
						int size = params.getChildCount();
						if (size != function.params.size()) {
							valid = false;
							throw new RuntimeException(
									"[Function parameter mismatch] : " + id
											+ " " + size
											+ " inputs instead of "
											+ function.params.size());
						}
						List<ReturnValue> inputs = new ArrayList<ReturnValue>();
						for (int i = 0; i < size; i++) {
							CommonTree child = (CommonTree) params.getChild(i);

							ReturnValue temp = parseStatement(child);
							inputs.add(temp);
							FunctionParam param = function.params.get(i);
							if (param.type != temp.type) {
								valid = false;
								throw new RuntimeException(
										"[Function parameter not matched] : "
												+ id + " on param:"
												+ child.getText());
							}
						}
						int size2 = inputs.size();
						int[] inputIndexs = new int[size2 + 2];

						for (int i = 0; i < size2; i++) {
							inputIndexs[i + 2] = inputs.get(i).index;
						}
						int temp = curContext.getTempVar();
						if (function.isp == -1) {
							curContext.asmCodes.add("LBLTOINT " + temp
									+ " FUNC_ENTRY_" + id);
							functionCalls.add(id);
							inputIndexs[0] = temp;
							inputIndexs[1] = this
									.getStringConstIndex(function.lib);

						} else {
							curContext.addAsmLine(ByteCode.MOVINT, new int[] {
									temp, function.isp });
							inputIndexs[0] = temp;
							inputIndexs[1] = this
									.getStringConstIndex(function.lib);
						}
						curContext.addAsmLine(ByteCode.CALL, inputIndexs);

						int ret = curContext.getTempVar();
						curContext.addAsmLine(ByteCode.COPYVAR, new int[] {
								ret, 1, 0, 1 });

						returnValue.index = ret;
						returnValue.type = function.returnValue.type;
					}
				}
				break;
			}
			}
			if (!valid) {
				throw new RuntimeException("unknown identifier: " + id);
			}
			break;
		}
		case MEngineParser.Navigate: {
			ReturnValue value = parseStatement((CommonTree) tree.getChild(0));
			if (value.type == ReturnType.Int) {
				valid = true;
				curContext.addAsmLine(ByteCode.NAVIGATE, new int[] { 1,
						value.index });
			}
			break;
		}
		case MEngineParser.Focus: {
			ReturnValue value = parseStatement((CommonTree) tree.getChild(0));
			if (value.type == ReturnType.Player) {
				valid = true;
				curContext.addAsmLine(ByteCode.NAVIGATE, new int[] { 0,
						value.index });
			}
			break;
		}
		case MEngineParser.Return: {
			if (curContext.function == null) {
				throw new RuntimeException(
						" Return statement can only occur in function body.");
			}
			if (count == 1) {
				ReturnValue temp = parseStatement((CommonTree) tree.getChild(0));
				if (temp.type != ReturnType.Void
						&& temp.type == curContext.function.returnValue.type) {
					curContext.addAsmLine(ByteCode.COPYVAR, new int[] { 0, 1,
							temp.index, 1 });
					valid = true;
				}
			} else if (count == 0
					&& curContext.function.returnValue.type == ReturnType.Void) {
				valid = true;
			}
			if (!valid) {
				throw new RuntimeException("invalid return values");
			} else {
				curContext.functionReturned = true;
			}
			curContext.popAllVarContext(false);
			curContext.asmCodes.add("RET");
			curContext.asmCodes.add("END");
			break;
		}
		case MEngineParser.NumericLiteral: {
			returnValue.type = ReturnType.Int;
			returnValue.index = curContext.getTempVar();
			curContext.addAsmLine(ByteCode.MOVINT, new int[] {
					returnValue.index, Integer.parseInt(tree.getText()) });
			valid = true;
			break;
		}
		case MEngineParser.StringLiteral: {
			returnValue.type = ReturnType.Str;
			returnValue.index = curContext.getTempVar();
			int index = getStringConstIndex(getStringFromLiteral(tree.getText()));
			if (index == -1) {
				throw new RuntimeException("unknown error happened at line :"
						+ tree.getLine());
			} else {
				curContext.addAsmLine(ByteCode.MOVSTR, new int[] {
						returnValue.index, index });
				valid = true;
			}
			break;
		}
		case MEngineParser.Add:
		case MEngineParser.Minus:
		case MEngineParser.Multiply:
		case MEngineParser.Divide:
		case MEngineParser.Mod:
		case MEngineParser.LeftShift:
		case MEngineParser.RightShift:
		case MEngineParser.BitAnd:
		case MEngineParser.BitOr:
		case MEngineParser.BitXor: {
			if (count == 2) {
				CommonTree left = (CommonTree) tree.getChild(0);
				CommonTree right = (CommonTree) tree.getChild(1);
				ReturnValue leftReturn = parseStatement(left);
				ReturnValue rightReturn = parseStatement(right);
				if (leftReturn.type == ReturnType.Int
						&& rightReturn.type == ReturnType.Int) {
					valid = true;
					returnValue.type = ReturnType.Int;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(
							ByteCode.valueOf(getArithOpcode(type)), new int[] {
									returnValue.index, leftReturn.index,
									rightReturn.index });
					valid = true;
				} else if (leftReturn.type == ReturnType.Str) {
					returnValue.type = ReturnType.Str;
					returnValue.index = curContext.getTempVar();
					if (rightReturn.type == ReturnType.Str) {
						curContext.addAsmLine(ByteCode.STRCAT, new int[] {
								returnValue.index, leftReturn.index,
								rightReturn.index });
						valid = true;
					} else if (rightReturn.type == ReturnType.Array
							|| rightReturn.type == ReturnType.Map
							|| rightReturn.type == ReturnType.Int) {
						int temp = curContext.getTempVar();
						curContext
								.addAsmLine(ByteCode.TOSTR,
										new int[] { temp, rightReturn.index,
												rightReturn.type.ordinal() });
						curContext.addAsmLine(ByteCode.STRCAT, new int[] {
								returnValue.index, leftReturn.index, temp });
						valid = true;
					}
				}
			} else if (count == 1 && type == MEngineParser.Minus) {
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext
						.addAsmLine(
								ByteCode.MOVINT,
								new int[] {
										returnValue.index,
										0 - Integer.parseInt(tree.getChild(0)
												.getText()) });
				valid = true;
			}
			break;
		}
		case MEngineParser.AddEQ:
		case MEngineParser.MinusEQ:
		case MEngineParser.MultiplyEQ:
		case MEngineParser.DivideEQ:
		case MEngineParser.ModEQ:
		case MEngineParser.LeftShiftEQ:
		case MEngineParser.RightShiftEQ:
		case MEngineParser.AndEQ:
		case MEngineParser.OrEQ: {
			if (count == 2) {
				CommonTree left = (CommonTree) tree.getChild(0);
				ReturnValue value0 = parseStatement(left);
				ReturnValue value1 = parseStatement((CommonTree) tree
						.getChild(1));
				if (left.getType() == MEngineParser.Identifier) {
					if (value0.type == ReturnType.Int
							&& value1.type == ReturnType.Int) {
						curContext.addAsmLine(
								ByteCode.valueOf(getArithAssignOpCode(type)),
								new int[] { value0.index, value0.index,
										value1.index });
						if (value0.global) {
							curContext.addAsmLine(ByteCode.COPYVAR, new int[] {
									value0.globalIndex, 0, value0.index, 1 });
						}
						valid = true;
					} else if (value0.type == ReturnType.Str
							&& value1.type == ReturnType.Str
							&& type == MEngineParser.AddEQ) {
						curContext.addAsmLine(ByteCode.STRCAT, new int[] {
								value0.index, value0.index, value1.index });
						if (value0.global) {
							curContext.addAsmLine(ByteCode.COPYVAR, new int[] {
									value0.globalIndex, 0, value0.index, 1 });
						}
						valid = true;
					}
				}
			}
			break;
		}

		case MEngineParser.GreatThan:
		case MEngineParser.GreatOrEq:
		case MEngineParser.EqEq:
		case MEngineParser.NotEq:
		case MEngineParser.LessThan:
		case MEngineParser.LessOrEq: {
			if (count == 2) {
				CommonTree left = (CommonTree) tree.getChild(0);
				CommonTree right = (CommonTree) tree.getChild(1);
				ReturnValue leftReturn = parseStatement(left);
				ReturnValue rightReturn = parseStatement(right);
				if (leftReturn.type == ReturnType.Int
						&& rightReturn.type == ReturnType.Int) {
					valid = true;
					returnValue.type = ReturnType.Int;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.CMP, new int[] {
							returnValue.index, leftReturn.index,
							rightReturn.index, getCompareType(type),
							leftReturn.type.ordinal() });
					valid = true;
				} else if (type == MEngineParser.EqEq
						|| type == MEngineParser.NotEq) {
					if (rightReturn.type == leftReturn.type) {
						valid = true;
						returnValue.index = curContext.getTempVar();
						returnValue.type = ReturnType.Int;
						curContext.addAsmLine(ByteCode.CMP, new int[] {
								returnValue.index, leftReturn.index,
								rightReturn.index, getCompareType(type),
								leftReturn.type.ordinal() });
					}
				}
			}

			break;
		}
		case MEngineParser.If: {
			if (count == 2 || count == 3) {
				CommonTree test = (CommonTree) tree.getChild(0);
				CommonTree ifyes = (CommonTree) tree.getChild(1);
				CommonTree ifno = count == 3 ? ((CommonTree) tree.getChild(2))
						: null;
				valid = true;
				ReturnValue testReturn = parseStatement(test);
				int temp = curIfEndIndex++;
				int temp2 = curContext.getTempVar();
				curContext.asmCodes.add("MOVINT " + temp2 + " 0");
				curContext.asmCodes.add("JE ifelse" + temp + " " + temp2 + " "
						+ testReturn.index);
				parseStatement(ifyes);
				curContext.asmCodes.add("JMP ifend" + temp);
				curContext.asmCodes.add(":ifelse" + temp);
				if (ifno != null) {
					parseStatement(ifno);
				}
				curContext.asmCodes.add(":ifend" + temp);
				returnValue.type = ReturnType.Void;
			}

			break;
		}
		case MEngineParser.Empty: {
			valid = true;
			break;
		}
		case MEngineParser.Throw: {
			if (count == 1) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				if (value0.type == ReturnType.Int) {
					curContext.addAsmLine(ByteCode.THROW,
							new int[] { value0.index });
					valid = true;
				}
			}
			break;
		}
		case MEngineParser.Try: {
			ReturnValue code = parseStatement((CommonTree) tree.getChild(1));
			if (code.type == ReturnType.Int) {
				valid = true;
				int tryIndex = curTryIndex++;
				curContext.pushVarContext(true);
				curContext.asmCodes.add("TRYSTART catchStart" + tryIndex + " "
						+ code.index);
				parseStatement((CommonTree) tree.getChild(0));
				curContext.asmCodes.add("TRYFINISH catchEnd" + tryIndex);
				curContext.asmCodes.add(":catchStart" + tryIndex);
				parseStatement((CommonTree) tree.getChild(2));
				curContext.asmCodes.add(":catchEnd" + tryIndex);
				curContext.popVarContext(true);
			}
			break;
		}
		case MEngineParser.For: {
			int forIndex = curForIndex++;
			breaks.push("forEnd" + forIndex);
			continues.push("forStart" + forIndex);
			List<CommonTree> statements = getForStatements(tree);
			CommonTree init = statements.get(0);
			CommonTree exp1 = statements.get(1);
			CommonTree exp2 = statements.get(2);
			CommonTree exp3 = statements.get(3);

			curContext.pushVarContext(true);
			if (init != null) {
				parseStatement(init);
			}

			curContext.asmCodes.add(":forStart" + forIndex);
			if (exp1 != null) {
				ReturnValue testReturn = parseStatement(exp1);
				int temp = curContext.getTempVar();
				curContext.asmCodes.add("MOVINT " + temp + " 0");
				curContext.asmCodes.add("JE forEnd" + forIndex + " " + temp
						+ " " + testReturn.index);
			}

			curContext.asmCodes.add("JMP forStatement" + forIndex);
			curContext.asmCodes.add(":forFinal" + forIndex);
			if (exp2 != null) {
				parseStatement(exp2);
			}
			curContext.asmCodes.add("JMP forStart" + forIndex);

			curContext.asmCodes.add(":forStatement" + forIndex);
			if (exp3 != null) {
				parseStatement(exp3);
			}
			curContext.asmCodes.add("JMP forFinal" + forIndex);
			curContext.asmCodes.add(":forEnd" + forIndex);
			breaks.pop();
			continues.pop();
			curContext.popVarContext(true);
			valid = true;
			returnValue.type = ReturnType.Void;
			break;
		}
		case MEngineParser.Switch: {
			CommonTree expression = (CommonTree) tree.getChild(0);
			CommonTree cases = (CommonTree) tree.getChild(1);
			int switchIndex = curSwitchIndex++;
			breaks.push("switchEnd" + switchIndex);
			ReturnValue temp = parseStatement(expression);
			if (temp.type == ReturnType.Int) {
				int expIndex = temp.index;
				List<Integer> list = getCaseValues(cases);
				for (int value : list) {
					if (value != Integer.MAX_VALUE) {
						int tempThing = curContext.getTempVar();
						curContext.asmCodes.add("MOVINT " + tempThing + " "
								+ value);
						curContext.asmCodes.add("JE switch" + switchIndex
								+ "_case" + value + " " + expIndex + " "
								+ tempThing);
					}
				}
				if (list.contains(Integer.MAX_VALUE)) {
					curContext.asmCodes.add("JMP switch" + switchIndex
							+ "default");
				}
				curContext.asmCodes.add("JMP switchEnd" + switchIndex);

				for (Object thing : cases.getChildren()) {
					CommonTree child = (CommonTree) thing;
					genCaseBlock(child, switchIndex);
				}

				curContext.asmCodes.add(":switchEnd" + switchIndex);

				breaks.pop();
				valid = true;
				returnValue.type = ReturnType.Void;
			}
			break;
		}
		case MEngineParser.Break: {
			if (breaks.size() > 0) {
				if (continues.size() > 0) {
					curContext.popVarContextUntilFor();
				} else {
					curContext.popVarContext(false);
				}
				curContext.asmCodes.add("JMP " + breaks.peek());
				returnValue.type = ReturnType.Void;
				valid = true;
			}
			break;
		}
		case MEngineParser.Continue: {
			if (continues.size() > 0) {
				curContext.asmCodes.add("JMP " + continues.peek());
				returnValue.type = ReturnType.Void;
				valid = true;
			}
			break;
		}
		case MEngineParser.LeftCurley: {

			// contexts.push(curContext);
			// Function function = new Function("bracket");
			// curContext = new Context(mainAsmCodes, global, new Function(
			// "bracket"));
			if (tree.getChildren() != null) {
				curContext.pushVarContext(false);
				for (Object thing : tree.getChildren()) {
					CommonTree child = (CommonTree) thing;
					parseStatement(child);
				}
				curContext.popVarContext(true);
			}
			// curContext = contexts.pop();
			returnValue.type = ReturnType.Void;
			valid = true;
			break;
		}
		case MEngineParser.LogicalOr: {
			int orIndex = curOrIndex++;
			returnValue.index = curContext.getTempVar();
			returnValue.type = ReturnType.Int;
			ReturnValue value0 = parseStatement((CommonTree) tree.getChild(0));
			if (value0.type == ReturnType.Int) {
				curContext.addAsmLine(ByteCode.MOVINT, new int[] {
						returnValue.index, 0 });
				curContext.asmCodes.add("JNE logicalOrSkip" + orIndex + " "
						+ value0.index + " " + returnValue.index);
			}
			ReturnValue value1 = parseStatement((CommonTree) tree.getChild(1));
			if (value0.type == ReturnType.Int && value1.type == ReturnType.Int) {
				valid = true;
				curContext.addAsmLine(ByteCode.OR, new int[] {
						returnValue.index, value0.index, value1.index });
				curContext.asmCodes.add("JMP logicalOrEnd" + orIndex);
			}
			curContext.asmCodes.add(":logicalOrSkip" + orIndex);
			curContext.addAsmLine(ByteCode.MOVINT, new int[] {
					returnValue.index, 1 });
			curContext.asmCodes.add(":logicalOrEnd" + orIndex);
			break;
		}
		case MEngineParser.LogicalAnd: {
			int andIndex = curAndIndex++;
			returnValue.index = curContext.getTempVar();
			returnValue.type = ReturnType.Int;
			ReturnValue value0 = parseStatement((CommonTree) tree.getChild(0));
			if (value0.type == ReturnType.Int) {
				curContext.addAsmLine(ByteCode.MOVINT, new int[] {
						returnValue.index, 0 });
				curContext.asmCodes.add("JE logicalAndSkip" + andIndex + " "
						+ value0.index + " " + returnValue.index);
			}
			ReturnValue value1 = parseStatement((CommonTree) tree.getChild(1));
			if (value0.type == ReturnType.Int && value1.type == ReturnType.Int) {
				valid = true;
				curContext.addAsmLine(ByteCode.AND, new int[] {
						returnValue.index, value0.index, value1.index });
				curContext.asmCodes.add("JMP logicalAndEnd" + andIndex);
			}
			curContext.asmCodes.add(":logicalAndSkip" + andIndex);
			curContext.addAsmLine(ByteCode.MOVINT, new int[] {
					returnValue.index, 0 });
			curContext.asmCodes.add(":logicalAndEnd" + andIndex);
			break;
		}
		case MEngineParser.LeftBracket: {
			valid = true;

			returnValue = parseStatement((CommonTree) tree.getChild(0));

			break;
		}
		case MEngineParser.DoDebug: {
			valid = true;
			curContext.addAsmLine(ByteCode.DODEBUG, new int[] {});
			break;
		}
		case MEngineParser.Debug: {
			if (count == 1) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				if (value0.type == ReturnType.Str) {
					valid = true;
					curContext.addAsmLine(ByteCode.DEBUG,
							new int[] { value0.index });
				}
			}
		}

		case MEngineParser.DbSave:
		case MEngineParser.MemSave: {
			if (count == 2) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue value1 = parseStatement((CommonTree) tree
						.getChild(1));
				if (value0.type == ReturnType.Str
						&& value1.type == ReturnType.Str) {
					valid = true;
					int opType = type == MEngineParser.DbSave ? 0 : 1;
					curContext.addAsmLine(ByteCode.SAVEDB, new int[] {
							value0.index, value1.index, opType });
				}
			}
			break;
		}
		case MEngineParser.DbLoad:
		case MEngineParser.MemLoad: {
			if (count == 1) {
				ReturnValue value0 = parseStatement((CommonTree) tree
						.getChild(0));
				if (value0.type == ReturnType.Str) {
					valid = true;
					int opType = type == MEngineParser.DbLoad ? 0 : 1;
					returnValue.type = ReturnType.Str;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.LOADDB, new int[] {
							returnValue.index, value0.index, opType });
				}
			}
			break;
		}
		case MEngineParser.PlusPlus: {
			if (count == 1) {
				CommonTree child = (CommonTree) tree.getChild(0);
				if (child.getType() == MEngineParser.Identifier) {
					ReturnType temp = curContext.getIdType(child.getText());
					if (temp == ReturnType.Int) {
						returnValue.type = ReturnType.Int;
						ReturnValue value0 = parseStatement(child);
						returnValue.index = value0.index;
						int reg1 = curContext.getTempVar();
						curContext.addAsmLine(ByteCode.MOVINT, new int[] {
								reg1, 1 });
						curContext.addAsmLine(ByteCode.ADD, new int[] {
								value0.index, reg1, value0.index });
						curContext.assignVar(child.getText(), value0);
						valid = true;
					}
				}

			}
			break;
		}
		case MEngineParser.MinusMinus: {
			if (count == 1) {
				CommonTree child = (CommonTree) tree.getChild(0);
				if (child.getType() == MEngineParser.Identifier) {
					ReturnType temp = curContext.getIdType(child.getText());
					if (temp == ReturnType.Int) {
						returnValue.type = ReturnType.Int;
						ReturnValue value0 = parseStatement(child);
						returnValue.index = value0.index;
						int reg1 = curContext.getTempVar();
						curContext.addAsmLine(ByteCode.MOVINT, new int[] {
								reg1, 1 });
						curContext.addAsmLine(ByteCode.SUB, new int[] {
								value0.index, value0.index, reg1 });
						curContext.assignVar(child.getText(), value0);
						valid = true;
					}
				}

			}
			break;
		}
		case MEngineParser.OpenUrl: {
			if (count == 1) {
				ReturnValue value0 = parseStatement((CommonTree) (tree
						.getChild(0)));
				if (value0.type == ReturnType.Str) {
					valid = true;
					curContext.addAsmLine(ByteCode.WINDOWDO, new int[] { 3,
							value0.index });
				}
			}
			break;
		}
		case MEngineParser.OpenWorldChat: {
			valid = true;
			curContext.addAsmLine(ByteCode.WINDOWDO, new int[] { 4 });
			break;
		}
		case MEngineParser.OpenPrivateChat: {
			ReturnValue value0 = parseStatement((CommonTree) (tree.getChild(0)));
			ReturnValue value1 = parseStatement((CommonTree) (tree.getChild(1)));
			if (value0.type == ReturnType.Int && value1.type == ReturnType.Str) {
				valid = true;
				curContext.addAsmLine(ByteCode.WINDOWDO, new int[] { 5,
						value0.index, value1.index });
			}
			break;
		}
		case MEngineParser.SetChatParams: {
			valid = true;
			ReturnValue value0 = parseStatement((CommonTree) (tree.getChild(0)));
			ReturnValue value1 = parseStatement((CommonTree) (tree.getChild(1)));
			ReturnValue value2 = parseStatement((CommonTree) (tree.getChild(2)));
			if (value0.type == ReturnType.Str && value1.type == ReturnType.Int
					&& value2.type == ReturnType.Str) {
				valid = true;
				curContext.addAsmLine(ByteCode.WINDOWDO, new int[] { 7,
						value0.index, value1.index, value2.index });
			}
			break;
		}
		case MEngineParser.SendSms: {
			ReturnValue value0 = parseStatement((CommonTree) (tree.getChild(0)));
			ReturnValue value1 = parseStatement((CommonTree) (tree.getChild(1)));
			if (value0.type == ReturnType.Str && value1.type == ReturnType.Str) {
				valid = true;
				curContext.addAsmLine(ByteCode.WINDOWDO, new int[] { 6,
						value0.index, value1.index });
			}
			break;
		}
		case MEngineParser.PrepareAssets: {
			ReturnValue value0 = parseStatement((CommonTree) (tree.getChild(0)));
			if (value0.type == ReturnType.Array) {
				valid = true;
				curContext.addAsmLine(ByteCode.WINDOWDO, new int[] { 8,
						value0.index });
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid prepareAssets(Array) calling");
			}
			break;
		}
		case MEngineParser.StartInput: {
			ReturnValue value0 = parseStatement((CommonTree) (tree.getChild(0)));
			ReturnValue value1 = parseStatement((CommonTree) (tree.getChild(1)));
			ReturnValue value2 = parseStatement((CommonTree) (tree.getChild(2)));
			ReturnValue value3 = parseStatement((CommonTree) (tree.getChild(3)));
			ReturnValue value4 = parseStatement((CommonTree) (tree.getChild(4)));
			if (value0.type == ReturnType.Element
					&& value1.type == ReturnType.Element
					&& value2.type == ReturnType.Int
					&& value3.type == ReturnType.Int
					&& value4.type == ReturnType.Int) {
				valid = true;
				curContext.addAsmLine(ByteCode.DOINPUT, new int[] { 0, 0,
						value0.index, value1.index, value2.index, value3.index,
						value4.index });
			}
			break;
		}
		case MEngineParser.StopInput: {
			valid = true;
			curContext.addAsmLine(ByteCode.DOINPUT, new int[] { 1 });
			break;
		}
		case MEngineParser.InitChatTabRect: {
			ReturnValue value0 = parseStatement((CommonTree) (tree.getChild(0)));
			ReturnValue value1 = parseStatement((CommonTree) (tree.getChild(1)));
			ReturnValue value2 = parseStatement((CommonTree) (tree.getChild(2)));
			ReturnValue value3 = parseStatement((CommonTree) (tree.getChild(3)));
			if (value0.type == ReturnType.Int && value1.type == ReturnType.Int
					&& value2.type == ReturnType.Int
					&& value3.type == ReturnType.Int) {
				valid = true;
				curContext.addAsmLine(ByteCode.DOCHAT,
						new int[] { 0, value0.index, value1.index,
								value2.index, value3.index });
			}
			break;
		}
		case MEngineParser.EnableShowChatTab: {
			ReturnValue value0 = parseStatement((CommonTree) (tree.getChild(0)));
			if (value0.type == ReturnType.Int) {
				valid = true;
				curContext.addAsmLine(ByteCode.DOCHAT, new int[] { 1,
						value0.index });
			}
			break;
		}
		case MEngineParser.Import: {
			String libName = null;
			if (count == 1) {
				CommonTree child0 = (CommonTree) (tree.getChild(0));
				libName = getStringFromLiteral(child0.getText());
				importLib(libName);
				int index = getStringConstIndex(libName);
				if (index != -1 && libs.containsKey(libName)) {
					curContext.addAsmLine(ByteCode.IMPORT, new int[] { index });
					valid = true;
				}
			}
			if (!valid) {
				throw new RuntimeException("lib " + libName + " not found");
			}
			break;
		}
		case MEngineParser.SetMenus: {
			if (count == 1) {
				ReturnValue value0 = parseStatement((CommonTree) (tree
						.getChild(0)));
				if (value0.type == ReturnType.Array) {
					valid = true;
					curContext.addAsmLine(ByteCode.MOMLDO, new int[] { 5,
							value0.index });
				}
			}
			break;
		}
		case MEngineParser.SetLoadingScreen: {
			if (count == 1) {
				ReturnValue value0 = parseStatement((CommonTree) (tree
						.getChild(0)));
				if (value0.type == ReturnType.Int) {
					valid = true;
					curContext.addAsmLine(ByteCode.SETENV, new int[] {
							value0.index, 1 });
				}
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid parameter for setLoadingScreen(int)");
			}
			break;
		}
		case MEngineParser.OpenWindow: {
			if (count == 5) {
				ReturnValue value0 = parseStatement((CommonTree) (tree
						.getChild(0)));
				ReturnValue value1 = parseStatement((CommonTree) (tree
						.getChild(1)));
				ReturnValue value2 = parseStatement((CommonTree) (tree
						.getChild(2)));
				ReturnValue value3 = parseStatement((CommonTree) (tree
						.getChild(3)));
				ReturnValue value4 = parseStatement((CommonTree) (tree
						.getChild(4)));
				if (value0.type == ReturnType.Str
						&& value1.type == ReturnType.Str
						&& value2.type == ReturnType.Rect
						&& value3.type == ReturnType.Map
						&& value4.type == ReturnType.Int) {
					valid = true;
					curContext.addAsmLine(ByteCode.WINDOWDO, new int[] { 0,
							value0.index, value1.index, value2.index,
							value3.index, value4.index });
				}
			}
			if (!valid) {
				throw new RuntimeException("invalid parameter for openWindow()");
			}
			break;
		}
		case MEngineParser.CloseWindow: {
			if (count == 1) {
				ReturnValue value0 = parseStatement((CommonTree) (tree
						.getChild(0)));
				if (value0.type == ReturnType.Str) {
					valid = true;
					curContext.addAsmLine(ByteCode.WINDOWDO, new int[] { 1,
							value0.index });
				}
			}
			break;
		}
		case MEngineParser.CloseAllWindows: {
			if (count == 0) {
				valid = true;
				curContext.addAsmLine(ByteCode.WINDOWDO, new int[] { 2 });
			}
			break;
		}

		case MEngineParser.GetElementById: {
			if (count == 1) {
				ReturnValue value0 = parseStatement((CommonTree) (tree
						.getChild(0)));
				if (value0.type == ReturnType.Str) {
					valid = true;
					returnValue.type = ReturnType.Element;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.GETELEMENT, new int[] {
							returnValue.index, value0.index });
				}
			}
			break;
		}

		case MEngineParser.CreateElement: {
			if (count == 2) {
				ReturnValue value0 = parseStatement((CommonTree) (tree
						.getChild(0)));
				ReturnValue value1 = parseStatement((CommonTree) (tree
						.getChild(1)));
				if (value0.type == ReturnType.Str
						&& value1.type == ReturnType.Str) {
					valid = true;
					returnValue.type = ReturnType.Element;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.MOMLDO, new int[] { 0,
							returnValue.index, value0.index, value1.index });
				}
			}
			break;
		}

		case MEngineParser.SetFillColor:
		case MEngineParser.SetStrokeColor: {
			int actionType = 0;
			switch (type) {
			case MEngineParser.SetStrokeColor: {
				actionType = 0;
				break;
			}
			case MEngineParser.SetFillColor: {
				actionType = 1;
				break;
			}
			}
			ReturnValue param0 = parseStatement((CommonTree) tree.getChild(0));
			if (param0.type == ReturnType.Int) {
				valid = true;
				curContext.addAsmLine(ByteCode.DRAWSHAPE, new int[] {
						actionType, param0.index });
				break;
			}
			break;
		}

		case MEngineParser.DrawRect:
		case MEngineParser.FillRect:
		case MEngineParser.DrawEclipse:
		case MEngineParser.FillEclipse: {

			if (count == 1) {
				int actionType = 0;
				switch (type) {
				case MEngineParser.DrawRect: {
					actionType = 3;
					break;
				}
				case MEngineParser.FillRect: {
					actionType = 5;
					break;
				}
				case MEngineParser.DrawEclipse: {
					actionType = 10;
					break;
				}
				case MEngineParser.FillEclipse: {
					actionType = 12;
					break;
				}
				}
				ReturnValue param0 = parseStatement((CommonTree) tree
						.getChild(0));
				if (param0.type == ReturnType.Rect) {
					valid = true;
					curContext.addAsmLine(ByteCode.DRAWSHAPE, new int[] {
							actionType, param0.index });
				}
			} else if (count == 4) {
				int actionType = 0;
				switch (type) {
				case MEngineParser.DrawRect: {
					actionType = 4;
					break;
				}
				case MEngineParser.FillRect: {
					actionType = 6;
					break;
				}
				case MEngineParser.DrawEclipse: {
					actionType = 11;
					break;
				}
				case MEngineParser.FillEclipse: {
					actionType = 13;
					break;
				}
				}
				ReturnValue param0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue param1 = parseStatement((CommonTree) tree
						.getChild(1));
				ReturnValue param2 = parseStatement((CommonTree) tree
						.getChild(2));
				ReturnValue param3 = parseStatement((CommonTree) tree
						.getChild(3));
				if (param0.type == ReturnType.Int
						&& param1.type == ReturnType.Int
						&& param2.type == ReturnType.Int
						&& param3.type == ReturnType.Int) {
					valid = true;
					curContext.addAsmLine(ByteCode.DRAWSHAPE, new int[] {
							actionType, param0.index, param1.index,
							param2.index, param3.index });
				}
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid drawRect() / fillRect() calling");
			}
			break;
		}
		case MEngineParser.DrawLine: {
			if (count == 4) {
				ReturnValue param0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue param1 = parseStatement((CommonTree) tree
						.getChild(1));
				ReturnValue param2 = parseStatement((CommonTree) tree
						.getChild(2));
				ReturnValue param3 = parseStatement((CommonTree) tree
						.getChild(3));
				if (param0.type == ReturnType.Int
						&& param1.type == ReturnType.Int
						&& param2.type == ReturnType.Int
						&& param3.type == ReturnType.Int) {
					valid = true;
					curContext.addAsmLine(ByteCode.DRAWSHAPE, new int[] { 7,
							param0.index, param1.index, param2.index,
							param3.index });
				}
			}
			if (!valid) {
				throw new RuntimeException("invalid drawLine() calling");
			}
			break;
		}
		case MEngineParser.DrawArc:
		case MEngineParser.FillArc: {
			if (count == 5) {
				ReturnValue param0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue param1 = parseStatement((CommonTree) tree
						.getChild(1));
				ReturnValue param2 = parseStatement((CommonTree) tree
						.getChild(2));
				ReturnValue param3 = parseStatement((CommonTree) tree
						.getChild(3));
				ReturnValue param4 = parseStatement((CommonTree) tree
						.getChild(4));
				if (param0.type == ReturnType.Int
						&& param1.type == ReturnType.Int
						&& param2.type == ReturnType.Int
						&& param3.type == ReturnType.Int
						&& param4.type == ReturnType.Int) {
					valid = true;
					curContext.addAsmLine(ByteCode.DRAWSHAPE, new int[] {
							type == MEngineParser.DrawArc ? 8 : 9,
							param0.index, param1.index, param2.index,
							param3.index, param4.index });
				}
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid drawArc() / fillArc() calling");
			}
			break;
		}
		case MEngineParser.DrawRoundRect:
		case MEngineParser.FillRoundRect: {
			if (count == 5) {
				ReturnValue param0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue param1 = parseStatement((CommonTree) tree
						.getChild(1));
				ReturnValue param2 = parseStatement((CommonTree) tree
						.getChild(2));
				ReturnValue param3 = parseStatement((CommonTree) tree
						.getChild(3));
				ReturnValue param4 = parseStatement((CommonTree) tree
						.getChild(4));
				if (param0.type == ReturnType.Int
						&& param1.type == ReturnType.Int
						&& param2.type == ReturnType.Int
						&& param3.type == ReturnType.Int
						&& param4.type == ReturnType.Int) {
					valid = true;
					curContext.addAsmLine(ByteCode.DRAWSHAPE, new int[] {
							type == MEngineParser.DrawRoundRect ? 17 : 18,
							param0.index, param1.index, param2.index,
							param3.index, param4.index });
				}
			}
			break;
		}
		case MEngineParser.DrawImage: {
			if (count == 2) {
				ReturnValue param0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue param1 = parseStatement((CommonTree) tree
						.getChild(1));
				if (param0.type == ReturnType.Str
						&& param1.type == ReturnType.Rect) {
					valid = true;
					curContext.addAsmLine(ByteCode.DRAWSHAPE, new int[] { 14,
							param0.index, param1.index });
				}
			} else if (count == 3) {
				ReturnValue param0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue param1 = parseStatement((CommonTree) tree
						.getChild(1));
				ReturnValue param2 = parseStatement((CommonTree) tree
						.getChild(2));
				if (param0.type == ReturnType.Str
						&& param1.type == ReturnType.Int
						&& param2.type == ReturnType.Int) {
					valid = true;
					curContext.addAsmLine(ByteCode.DRAWSHAPE, new int[] { 15,
							param0.index, param1.index, param2.index });
				}
			} else if (count == 5) {
				ReturnValue param0 = parseStatement((CommonTree) tree
						.getChild(0));
				ReturnValue param1 = parseStatement((CommonTree) tree
						.getChild(1));
				ReturnValue param2 = parseStatement((CommonTree) tree
						.getChild(2));
				ReturnValue param3 = parseStatement((CommonTree) tree
						.getChild(3));
				ReturnValue param4 = parseStatement((CommonTree) tree
						.getChild(4));
				if (param0.type == ReturnType.Str
						&& param1.type == ReturnType.Int
						&& param2.type == ReturnType.Int
						&& param3.type == ReturnType.Int
						&& param4.type == ReturnType.Int) {
					valid = true;
					curContext.addAsmLine(ByteCode.DRAWSHAPE, new int[] { 16,
							param0.index, param1.index, param2.index,
							param3.index, param4.index });
				}
			}
			break;
		}
		}
		if (!valid) {
			throw new RuntimeException("invalid statement arround pattern: ["
					+ tree.getText() + "]");
		}
		return returnValue;
	}

	private boolean doDot(CommonTree left, CommonTree right,
			ReturnValue returnValue, int type) {
		boolean valid = false;
		ReturnValue value0 = parseStatement(left);
		switch (right.getType()) {
		case MEngineParser.MakeCopy: {
			if (value0.type == ReturnType.Element) {
				if (right.getChildCount() == 1) {
					ReturnValue right0 = parseStatement((CommonTree) right
							.getChild(0));
					if (right0.type == ReturnType.Str) {
						valid = true;
						returnValue.type = ReturnType.Element;
						returnValue.index = curContext.getTempVar();
						curContext
								.addAsmLine(ByteCode.MOMLDO, new int[] { 2,
										returnValue.index, value0.index,
										right0.index });
					}
				}
			}
			break;
		}
		case MEngineParser.Rotate: {
			if (value0.type == ReturnType.Player
					|| value0.type == ReturnType.Text
					|| value0.type == ReturnType.Group) {
				if (right.getChildCount() == 1) {
					ReturnValue right0 = parseStatement((CommonTree) right
							.getChild(0));

					if (right0.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.TRANSFORM, new int[] {
								value0.index, 1, right0.index });
					}
				}
			}
			break;
		}
		case MEngineParser.Scale: {
			if (value0.type == ReturnType.Player
					|| value0.type == ReturnType.Text
					|| value0.type == ReturnType.Group) {
				if (right.getChildCount() == 2) {
					ReturnValue right0 = parseStatement((CommonTree) right
							.getChild(0));
					ReturnValue right1 = parseStatement((CommonTree) right
							.getChild(1));

					if (right0.type == ReturnType.Int
							&& right1.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.TRANSFORM, new int[] {
								value0.index, 2, right0.index, right1.index });
					}
				}
			}
			break;
		}
		case MEngineParser.Transform: {
			if (value0.type == ReturnType.Player) {
				if (right.getChildCount() == 5) {
					ReturnValue right0 = parseStatement((CommonTree) right
							.getChild(0));
					ReturnValue right1 = parseStatement((CommonTree) right
							.getChild(1));
					ReturnValue right2 = parseStatement((CommonTree) right
							.getChild(2));
					ReturnValue right3 = parseStatement((CommonTree) right
							.getChild(3));
					ReturnValue right4 = parseStatement((CommonTree) right
							.getChild(4));
					if (right0.type == ReturnType.Int
							&& right1.type == ReturnType.Int
							&& right2.type == ReturnType.Int
							&& right3.type == ReturnType.Int
							&& right4.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.TRANSFORM, new int[] {
								value0.index, right0.index, right1.index,
								right2.index, right3.index, right4.index });
					}
				}
			}
			break;
		}
		case MEngineParser.SetPivot: {
			if (value0.type == ReturnType.Player
					|| value0.type == ReturnType.Text
					|| value0.type == ReturnType.Group) {
				if (right.getChildCount() == 2) {
					ReturnValue right0 = parseStatement((CommonTree) right
							.getChild(0));
					ReturnValue right1 = parseStatement((CommonTree) right
							.getChild(1));
					if (right0.type == ReturnType.Int
							&& right1.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.TRANSFORM, new int[] {
								value0.index, 3, right0.index, right1.index });
					}
				}
			}
			break;
		}
		case MEngineParser.GetState: {
			if (value0.type == ReturnType.Player) {
				valid = true;
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETPLAYERDATA,
						new int[] { returnValue.index, value0.index,
								getPlayerOpType(right.getType()) });
			}
			break;
		}

		case MEngineParser.CreateAnimatedTile: {
			if (value0.type == ReturnType.Tiles) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Int) {
						valid = true;
						returnValue.index = curContext.getTempVar();
						returnValue.type = ReturnType.Int;
						curContext
								.addAsmLine(ByteCode.TILESOP, new int[] { 0,
										value0.index, returnValue.index,
										value1.index });
					}
				}

			}
			break;
		}
		case MEngineParser.GetAnimatedTile: {
			if (value0.type == ReturnType.Tiles) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Int) {
						valid = true;
						returnValue.index = curContext.getTempVar();
						returnValue.type = ReturnType.Int;
						curContext
								.addAsmLine(ByteCode.TILESOP, new int[] { 1,
										value0.index, returnValue.index,
										value1.index });
					}
				}

			}
			break;
		}
		case MEngineParser.SetAnimatedTile: {
			if (value0.type == ReturnType.Tiles) {
				if (right.getChildCount() == 2) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					ReturnValue value2 = parseStatement((CommonTree) right
							.getChild(1));
					if (value1.type == ReturnType.Int
							&& value2.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.TILESOP, new int[] { 2,
								value0.index, value1.index, value2.index });
					}
				}

			}
			break;
		}
		case MEngineParser.SetCell: {
			if (value0.type == ReturnType.Tiles) {
				if (right.getChildCount() == 3) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					ReturnValue value2 = parseStatement((CommonTree) right
							.getChild(1));
					ReturnValue value3 = parseStatement((CommonTree) right
							.getChild(2));
					if (value1.type == ReturnType.Int
							&& value2.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.TILESOP, new int[] { 3,
								value0.index, value1.index, value2.index,
								value3.index });
					}
				}

			}
			break;
		}
		case MEngineParser.GetCell: {
			if (value0.type == ReturnType.Tiles) {
				if (right.getChildCount() == 2) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					ReturnValue value2 = parseStatement((CommonTree) right
							.getChild(1));
					if (value1.type == ReturnType.Int
							&& value2.type == ReturnType.Int) {
						valid = true;
						returnValue.type = ReturnType.Int;
						returnValue.index = curContext.getTempVar();
						curContext.addAsmLine(ByteCode.TILESOP, new int[] { 4,
								value0.index, returnValue.index, value1.index,
								value2.index });
					}
				}

			}
			break;
		}
		case MEngineParser.StartAnimation: {
			if (value0.type == ReturnType.Tiles) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Array) {
						curContext.addAsmLine(ByteCode.TILESOP, new int[] { 5,
								value0.index, value1.index });
						valid = true;
					}
				}
			}
			break;
		}
		case MEngineParser.Clean: {
			if (value0.type == ReturnType.Array
					|| value0.type == ReturnType.Map) {
				curContext.addAsmLine(ByteCode.CLEAN,
						new int[] { value0.index });
				valid = true;
			}
			break;
		}
		case MEngineParser.StopAnimation: {
			if (value0.type == ReturnType.Tiles) {
				if (right.getChildCount() == 0) {

					curContext.addAsmLine(ByteCode.TILESOP, new int[] { 6,
							value0.index });
					valid = true;

				}
			}
			break;
		}
		case MEngineParser.GetClip: {
			if (value0.type == ReturnType.Player) {

				valid = true;
				returnValue.type = ReturnType.Rect;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETPLAYERDATA,
						new int[] { returnValue.index, value0.index,
								getPlayerOpType(right.getType()) });
			}
			break;
		}

		case MEngineParser.CancelClip: {
			if (value0.type == ReturnType.Player) {
				valid = true;
				curContext.addAsmLine(ByteCode.SETPLAYERDATA, new int[] { -1,
						value0.index, getPlayerOpType(right.getType()) });
			}
			break;
		}

		case MEngineParser.SetClip: {
			if (value0.type == ReturnType.Player) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Rect) {
						valid = true;
						curContext
								.addAsmLine(
										ByteCode.SETPLAYERDATA,
										new int[] {
												value1.index,
												value0.index,
												this.getPlayerOpType(right
														.getType()) });
					}
				}
			}
			break;
		}
		case MEngineParser.GetViewPort: {
			if (value0.type == ReturnType.Group) {
				valid = true;
				returnValue.type = ReturnType.Rect;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETGROUPDATA,
						new int[] { returnValue.index, value0.index,
								getGroupOpType(right.getType()) });
			}
			break;
		}
		case MEngineParser.ContainsPoint: {
			if (value0.type == ReturnType.Rect) {
				valid = true;
				ReturnValue value1 = parseStatement((CommonTree) right
						.getChild(0));
				ReturnValue value2 = parseStatement((CommonTree) right
						.getChild(1));
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETRECTDATA,
						new int[] { returnValue.index, value0.index,
								getRectOpType(right.getType()), value1.index,
								value2.index });
			}
			break;
		}
		case MEngineParser.Intersacts:
		case MEngineParser.ContainsRect: {
			if (value0.type == ReturnType.Rect) {
				if (right.getChildCount() == 4) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					ReturnValue value2 = parseStatement((CommonTree) right
							.getChild(1));
					ReturnValue value3 = parseStatement((CommonTree) right
							.getChild(2));
					ReturnValue value4 = parseStatement((CommonTree) right
							.getChild(3));
					if (value1.type == ReturnType.Int
							&& value2.type == ReturnType.Int
							&& value3.type == ReturnType.Int
							&& value4.type == ReturnType.Int) {
						valid = true;
						returnValue.type = ReturnType.Int;
						returnValue.index = curContext.getTempVar();
						curContext.addAsmLine(ByteCode.GETRECTDATA, new int[] {
								returnValue.index, value0.index,
								getRectOpType(right.getType()), value1.index,
								value2.index, value3.index, value4.index });
					}
				} else if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Rect) {
						valid = true;
						returnValue.type = ReturnType.Int;
						returnValue.index = curContext.getTempVar();
						curContext.addAsmLine(ByteCode.GETRECTDATA, new int[] {
								returnValue.index, value0.index,
								getRectOpType(right.getType()), value1.index });
					}
				}
			}
			break;
		}

		case MEngineParser.GetX:
		case MEngineParser.GetY: {
			if (value0.type == ReturnType.Group) {
				valid = true;
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETGROUPDATA,
						new int[] { returnValue.index, value0.index,
								getGroupOpType(right.getType()) });
			}
		}
		case MEngineParser.GetWidth:
		case MEngineParser.GetHeight: {
			if (value0.type == ReturnType.Rect) {
				valid = true;
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETRECTDATA,
						new int[] { returnValue.index, value0.index,
								getRectOpType(right.getType()) });
			} else if (value0.type == ReturnType.Text
					&& (right.getType() == MEngineParser.GetX || right
							.getType() == MEngineParser.GetY)) {
				valid = true;
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETTEXTDATA,
						new int[] { returnValue.index, value0.index,
								getTextOpType(right.getType()) });
			} else if (value0.type == ReturnType.Player) {
				valid = true;
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETPLAYERDATA,
						new int[] { returnValue.index, value0.index,
								getPlayerOpType(right.getType()) });
			} else if (value0.type == ReturnType.Tiles) {
				valid = true;
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETTILESDATA,
						new int[] { returnValue.index, value0.index,
								getTilesOpType(right.getType()) });
			}

			break;
		}
		case MEngineParser.GetLayoutHeight:
		case MEngineParser.GetLayoutWidth:
		case MEngineParser.GetBgTransparent: {
			if (value0.type == ReturnType.Text) {
				valid = true;
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETTEXTDATA,
						new int[] { returnValue.index, value0.index,
								getTextOpType(right.getType()) });
			}

			break;
		}
		case MEngineParser.SetStates: {
			if (value0.type == ReturnType.Player) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Array) {
						valid = true;
						curContext.addAsmLine(ByteCode.SETPLAYERDATA,
								new int[] { value1.index, value0.index, 9 });
					}
				}
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid Player.setStates(Array) calling");
			}
			break;
		}
		case MEngineParser.SetCells: {
			if (value0.type == ReturnType.Tiles) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Array) {
						curContext.addAsmLine(ByteCode.SETTILESDATA, new int[] {
								value1.index, value0.index,
								getTilesOpType(right.getType()) });
						valid = true;
					}
				}
			}
			break;
		}

		case MEngineParser.SetMaxWidth:
		case MEngineParser.SetMaxHeight: {
			if (value0.type == ReturnType.Text) {
				if (right.getChildCount() == 1) {

					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.SETTEXTDATA,
								new int[] { value1.index, value0.index,
										getTextOpType(right.getType()) });
					}
				}
			}
			break;
		}
		case MEngineParser.SetViewPort: {
			if (value0.type == ReturnType.Group) {
				ReturnValue value1 = parseStatement((CommonTree) right
						.getChild(0));
				if (value1.type == ReturnType.Rect) {
					curContext.addAsmLine(ByteCode.SETGROUPDATA,
							new int[] { value1.index, value0.index,
									getGroupOpType(right.getType()) });
					valid = true;
				}
			}
			break;
		}
		case MEngineParser.MovePos:
		case MEngineParser.ResizeBounds: {
			if (value0.type == ReturnType.Rect) {
				ReturnValue value1 = parseStatement((CommonTree) right
						.getChild(0));
				ReturnValue value2 = parseStatement((CommonTree) right
						.getChild(1));
				if (value1.type == ReturnType.Int
						&& value2.type == ReturnType.Int) {
					curContext.addAsmLine(ByteCode.SETRECTDATA,
							new int[] { value1.index, value0.index,
									getRectOpType(right.getType()),
									value2.index });
					valid = true;
				}
			}
			break;
		}
		case MEngineParser.SetX:
		case MEngineParser.SetY: {
			if (value0.type == ReturnType.Group) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.SETGROUPDATA, new int[] {
								value1.index, value0.index,
								getGroupOpType(right.getType()) });
					}
				}
				break;
			} else if (value0.type == ReturnType.Text) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.SETTEXTDATA,
								new int[] { value1.index, value0.index,
										getTextOpType(right.getType()) });
					}
				}
				break;
			} else if (value0.type == ReturnType.Tiles) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.SETTILESDATA, new int[] {
								value1.index, value0.index,
								getTilesOpType(right.getType()) });
					}
				}
				break;
			}
		}
		case MEngineParser.SetWidth:
		case MEngineParser.SetHeight: {
			if (value0.type == ReturnType.Rect) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.SETRECTDATA,
								new int[] { value1.index, value0.index,
										getRectOpType(right.getType()) });

					}
				}
			} else if (value0.type == ReturnType.Player) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.SETPLAYERDATA,
								new int[] { value1.index, value0.index,
										getPlayerOpType(right.getType()) });
					}
				}
			}

			break;
		}
		case MEngineParser.GetPivotX:
		case MEngineParser.GetPivotY:
		case MEngineParser.GetRotateDegree:
		case MEngineParser.GetScaleX:
		case MEngineParser.GetScaleY:
			if (value0.type == ReturnType.Player) {
				if (right.getChildCount() == 0) {
					returnValue.type = ReturnType.Int;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.GETPLAYERDATA, new int[] {
							returnValue.index, value0.index,
							getPlayerOpType(right.getType()) });
					valid = true;
				}
				if (!valid) {
					throw new RuntimeException("invalid statement: " + type);
				}
			} else if (value0.type == ReturnType.Text) {
				if (right.getChildCount() == 0) {
					returnValue.type = ReturnType.Int;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.GETTEXTDATA, new int[] {
							returnValue.index, value0.index,
							getTextOpType(right.getType()) });
					valid = true;
				}
				if (!valid) {
					throw new RuntimeException("invalid statement: " + type);
				}
			} else if (value0.type == ReturnType.Group) {
				if (right.getChildCount() == 0) {
					returnValue.type = ReturnType.Int;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.GETGROUPDATA, new int[] {
							returnValue.index, value0.index,
							getGroupOpType(right.getType()) });
					valid = true;
				}
				if (!valid) {
					throw new RuntimeException("invalid statement: " + type);
				}
			}
			break;

		case MEngineParser.SetData: {
			if (value0.type == ReturnType.Player) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Str) {
						valid = true;
						curContext.addAsmLine(ByteCode.SETPLAYERDATA,
								new int[] { value1.index, value0.index,
										getPlayerOpType(right.getType()) });
					}
				}
				if (!valid) {
					throw new RuntimeException(
							"invalid params for MPlayer.setData(String)");
				}
			} else if (value0.type == ReturnType.Group) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Str) {
						valid = true;
						curContext.addAsmLine(ByteCode.SETGROUPDATA, new int[] {
								value1.index, value0.index,
								getGroupOpType(right.getType()) });
					}
				}
				if (!valid) {
					throw new RuntimeException(
							"invalid params for MGroup.setData(String)");
				}
			}

			break;
		}
		case MEngineParser.GetData: {
			if (value0.type == ReturnType.Player) {
				if (right.getChildCount() == 0) {
					returnValue.type = ReturnType.Str;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.GETPLAYERDATA, new int[] {
							returnValue.index, value0.index,
							getPlayerOpType(right.getType()) });
					valid = true;
				}
				if (!valid) {
					throw new RuntimeException(
							"invalid params for String MPlayer.getData()");
				}
			} else if (value0.type == ReturnType.Group) {
				if (right.getChildCount() == 0) {
					returnValue.type = ReturnType.Str;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.GETGROUPDATA, new int[] {
							returnValue.index, value0.index,
							getGroupOpType(right.getType()) });
					valid = true;
				}
				if (!valid) {
					throw new RuntimeException(
							"invalid params for String MGroup.getData()");
				}
			}

			break;
		}
		case MEngineParser.SetText: {
			if (value0.type == ReturnType.Text) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Str) {
						valid = true;
						curContext.addAsmLine(ByteCode.SETTEXTDATA,
								new int[] { value1.index, value0.index,
										getTextOpType(right.getType()) });
					}
				}
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid params for MText.setText(string)");
			}
			break;
		}
		case MEngineParser.GetText: {
			if (value0.type == ReturnType.Text) {
				if (right.getChildCount() == 0) {
					valid = true;
					returnValue.type = ReturnType.Str;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.GETTEXTDATA, new int[] {
							returnValue.index, value0.index,
							getTextOpType(right.getType()) });
				}
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid params for String MText.getText()");
			}
			break;
		}
		case MEngineParser.Stop: {
			if (value0.type == ReturnType.Player
					|| value0.type == ReturnType.Text
					|| value0.type == ReturnType.Group
					|| value0.type == ReturnType.Tiles) {
				if (right.getChildCount() == 0) {
					valid = true;
					curContext.addAsmLine(ByteCode.STOP,
							new int[] { value0.index });
				}
			}
			if (!valid) {
				throw new RuntimeException("invalid params for MPlayer.stop()");
			}
			break;
		}
		case MEngineParser.HopTo:
		case MEngineParser.MoveTo: {
			if (value0.type == ReturnType.Player
					|| value0.type == ReturnType.Text
					|| value0.type == ReturnType.Group
					|| value0.type == ReturnType.Tiles) {
				if (right.getChildCount() == 4) {
					ReturnValue param0 = parseStatement((CommonTree) right
							.getChild(0));
					ReturnValue param1 = parseStatement((CommonTree) right
							.getChild(1));
					ReturnValue param2 = parseStatement((CommonTree) right
							.getChild(2));
					CommonTree tree3 = (CommonTree) right.getChild(3);
					ReturnValue param3 = parseStatement(tree3);

					if (param0.type == ReturnType.Int
							&& param1.type == ReturnType.Int
							&& param2.type == ReturnType.Int) {
						String funcName = tree3.getText();
						if (isFunction(funcName)) {
							Function function = getFunctionById(funcName);
							valid = true;
							int isp = -1;
							int funcIndex = -1;
							int temp = curContext.getTempVar();
							if (function.isp == -1) {
								String func = tree3.getText();
								curContext.asmCodes.add("LBLTOINT " + temp
										+ " FUNC_ENTRY_" + func);
								functionCalls.add(func);
								isp = temp;
							} else {
								curContext.addAsmLine(ByteCode.MOVINT,
										new int[] { temp, function.isp });
								isp = temp;
								funcIndex = this.getStringConstIndex(funcName);
							}
							curContext.addAsmLine(ByteCode.MOVE, new int[] {
									value0.index,
									right.getType() == MEngineParser.MoveTo ? 0
											: 4, param0.index, param1.index,
									param2.index, isp, funcIndex });
						} else if (param3.type == ReturnType.Int) {
							valid = true;
							curContext.addAsmLine(ByteCode.MOVE, new int[] {
									value0.index,
									right.getType() == MEngineParser.MoveTo ? 0
											: 4, param0.index, param1.index,
									param2.index, param3.index });
						}
					}
				}
			}
			break;
		}
		case MEngineParser.MoveBy: {
			if (value0.type == ReturnType.Player
					|| value0.type == ReturnType.Text
					|| value0.type == ReturnType.Group
					|| value0.type == ReturnType.Tiles) {
				if (right.getChildCount() == 3) {
					ReturnValue param0 = parseStatement((CommonTree) right
							.getChild(0));
					ReturnValue param1 = parseStatement((CommonTree) right
							.getChild(1));
					ReturnValue param2 = parseStatement((CommonTree) right
							.getChild(2));

					if (param0.type == ReturnType.Int
							&& param1.type == ReturnType.Int
							&& param2.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.MOVE, new int[] {
								value0.index, 1, param0.index, param1.index,
								param2.index });
					}
				}
			}
			break;
		}
		case MEngineParser.RotateTo: {
			if (value0.type == ReturnType.Player
					|| value0.type == ReturnType.Text
					|| value0.type == ReturnType.Group) {
				if (right.getChildCount() == 3) {
					ReturnValue param0 = parseStatement((CommonTree) right
							.getChild(0));
					ReturnValue param1 = parseStatement((CommonTree) right
							.getChild(1));
					ReturnValue param2 = parseStatement((CommonTree) right
							.getChild(2));

					if (param0.type == ReturnType.Int
							&& param1.type == ReturnType.Int
							&& param2.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.MOVE, new int[] {
								value0.index, 2, param0.index, param1.index,
								param2.index });
					}
				}
			}
			break;
		}
		case MEngineParser.RotateBy: {
			if (value0.type == ReturnType.Player
					|| value0.type == ReturnType.Text
					|| value0.type == ReturnType.Group) {
				if (right.getChildCount() == 2) {
					ReturnValue param0 = parseStatement((CommonTree) right
							.getChild(0));
					ReturnValue param1 = parseStatement((CommonTree) right
							.getChild(1));

					if (param0.type == ReturnType.Int
							&& param1.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.MOVE, new int[] {
								value0.index, 3, param0.index, param1.index });
					}
				}
			}
			break;
		}
		case MEngineParser.GetLocalPoint: {
			if (value0.type.ordinal() > ReturnType.Rect.ordinal()
					&& value0.type != ReturnType.Void) {
				if (right.getChildCount() == 2) {
					ReturnValue param0 = parseStatement((CommonTree) right
							.getChild(0));
					ReturnValue param1 = parseStatement((CommonTree) right
							.getChild(1));
					if (param0.type == ReturnType.Int
							&& param1.type == ReturnType.Int) {
						returnValue.type = ReturnType.Array;
						returnValue.index = curContext.getTempVar();
						curContext.addAsmLine(ByteCode.RELATIVEPOS, new int[] {
								returnValue.index, value0.index, param0.index,
								param1.index, 0 });
						valid = true;
					}
				}
			}
			break;
		}
		case MEngineParser.GetGlobalX: {
			if (value0.type.ordinal() > ReturnType.Rect.ordinal()
					&& value0.type != ReturnType.Void) {
				if (right.getChildCount() == 1) {
					ReturnValue param0 = parseStatement((CommonTree) right
							.getChild(0));
					if (param0.type == ReturnType.Int) {
						returnValue.type = ReturnType.Int;
						returnValue.index = curContext.getTempVar();
						curContext.addAsmLine(ByteCode.RESOLVE, new int[] {
								returnValue.index, value0.index, param0.index,
								1 });
						valid = true;
					}
				}
			}
			break;
		}
		case MEngineParser.GetGlobalY: {
			if (value0.type.ordinal() > ReturnType.Rect.ordinal()
					&& value0.type != ReturnType.Void) {
				if (right.getChildCount() == 1) {
					ReturnValue param0 = parseStatement((CommonTree) right
							.getChild(0));
					if (param0.type == ReturnType.Int) {
						returnValue.type = ReturnType.Int;
						returnValue.index = curContext.getTempVar();
						curContext.addAsmLine(ByteCode.RESOLVE, new int[] {
								returnValue.index, value0.index, param0.index,
								2 });
						valid = true;
					}
				}
			}
			break;
		}
		case MEngineParser.GetLocalX: {
			if (value0.type.ordinal() > ReturnType.Rect.ordinal()
					&& value0.type != ReturnType.Void) {
				if (right.getChildCount() == 1) {
					ReturnValue param0 = parseStatement((CommonTree) right
							.getChild(0));
					if (param0.type == ReturnType.Int) {
						returnValue.type = ReturnType.Int;
						returnValue.index = curContext.getTempVar();
						curContext.addAsmLine(ByteCode.RESOLVE, new int[] {
								returnValue.index, value0.index, param0.index,
								3 });
						valid = true;
					}
				}
			}
			break;
		}
		case MEngineParser.GetLocalY: {
			if (value0.type.ordinal() > ReturnType.Rect.ordinal()
					&& value0.type != ReturnType.Void) {
				if (right.getChildCount() == 1) {
					ReturnValue param0 = parseStatement((CommonTree) right
							.getChild(0));
					if (param0.type == ReturnType.Int) {
						returnValue.type = ReturnType.Int;
						returnValue.index = curContext.getTempVar();
						curContext.addAsmLine(ByteCode.RESOLVE, new int[] {
								returnValue.index, value0.index, param0.index,
								4 });
						valid = true;
					}
				}
			}
			break;
		}
		case MEngineParser.GetGlobalPoint: {
			if (value0.type.ordinal() > ReturnType.Rect.ordinal()
					&& value0.type != ReturnType.Void) {
				if (right.getChildCount() == 2) {
					ReturnValue param0 = parseStatement((CommonTree) right
							.getChild(0));
					ReturnValue param1 = parseStatement((CommonTree) right
							.getChild(1));
					if (param0.type == ReturnType.Int
							&& param1.type == ReturnType.Int) {
						returnValue.type = ReturnType.Array;
						returnValue.index = curContext.getTempVar();
						curContext.addAsmLine(ByteCode.RELATIVEPOS, new int[] {
								returnValue.index, value0.index, param0.index,
								param1.index, 1 });
						valid = true;
					}
				}
			}
			break;
		}

		case MEngineParser.GetParent: {
			if (value0.type.ordinal() > ReturnType.Rect.ordinal()
					&& value0.type != ReturnType.Void) {
				valid = true;
				returnValue.type = ReturnType.Group;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETPARENT, new int[] {
						returnValue.index, value0.index });
			}
			break;
		}
		case MEngineParser.GetChildren: {
			if (value0.type == ReturnType.Group) {
				valid = true;
				returnValue.type = ReturnType.Array;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETCHILDREN, new int[] {
						returnValue.index, value0.index });
			}
			break;
		}
		case MEngineParser.NotNull: {
			if (right.getChildCount() == 0) {
				valid = true;
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.ISNULL, new int[] {
						returnValue.index, value0.index, 1 });
			}

			if (!valid) {
				throw new RuntimeException(
						"invalid params for Object.notNull()");
			}
			break;
		}
		case MEngineParser.IsNull: {

			if (right.getChildCount() == 0) {
				valid = true;
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.ISNULL, new int[] {
						returnValue.index, value0.index, 0 });
			}

			if (!valid) {
				throw new RuntimeException("invalid params for Object.isNull()");
			}
			break;
		}
		case MEngineParser.SetTileMode: {
			if (value0.type == ReturnType.Tiles) {
				if (right.getChildCount() == 1) {
					ReturnValue param0 = parseStatement((CommonTree) right
							.getChild(0));
					if (param0.type == ReturnType.Int) {
						returnValue.type = ReturnType.Void;
						curContext.addAsmLine(
								ByteCode.SETTILESDATA,
								new int[] { param0.index, value0.index,
										this.getTilesOpType(right.getType()) });
						valid = true;
					}

				}
			}
			break;
		}
		case MEngineParser.OnClick: {
			if (value0.type == ReturnType.Element) {
				if (right.getChildCount() == 1) {
					CommonTree rightThing = (CommonTree) right.getChild(0);
					if (rightThing.getType() == MEngineParser.NumericLiteral) {
						if (rightThing.getText().equals("0")
								|| rightThing.getText().equals("-1")) {
							valid = true;
							int temp = curContext.getTempVar();
							curContext.addAsmLine(ByteCode.MOVINT, new int[] {
									temp, -1 });
							curContext.addAsmLine(ByteCode.MOMLDO, new int[] {
									3, value0.index, 1, temp });
						}
					} else if (rightThing.getType() == MEngineParser.Identifier) {
						String id = rightThing.getText();
						if (isFunction(id)) {
							Function func = this.getFunctionById(id);
							if (func.params.size() == 1
									&& func.params.get(0).type == ReturnType.Element) {
								valid = true;
								int temp = curContext.getTempVar();
								if (func.isp != -1) {
									curContext.addAsmLine(ByteCode.MOVINT,
											new int[] { temp, func.isp });
									curContext
											.addAsmLine(
													ByteCode.MOMLDO,
													new int[] {
															3,
															value0.index,
															1,
															temp,
															this.getStringConstIndex(func.lib) });
								} else {

									curContext.asmCodes.add("LBLTOINT " + temp
											+ " FUNC_ENTRY_" + id);
									functionCalls.add(id);
									curContext
											.addAsmLine(
													ByteCode.MOMLDO,
													new int[] {
															3,
															value0.index,
															1,
															temp,
															this.getStringConstIndex(func.lib) });
								}
							} else {
								throw new RuntimeException(
										"invalid onClick callback<" + id
												+ ">. (parameter mismatch)");
							}
						}
					}
				}
			}
			break;
		}

		case MEngineParser.OnFocus:
		case MEngineParser.OnLostFocus: {
			if (value0.type == ReturnType.Element) {
				if (right.getChildCount() == 1) {
					int eventType = (right.getType() == MEngineParser.OnFocus) ? 2
							: 3;
					CommonTree rightThing = (CommonTree) right.getChild(0);
					if (rightThing.getType() == MEngineParser.NumericLiteral) {
						if (rightThing.getText().equals("0")
								|| rightThing.getText().equals("-1")) {
							valid = true;
							int temp = curContext.getTempVar();
							curContext.addAsmLine(ByteCode.MOVINT, new int[] {
									temp, -1 });
							curContext.addAsmLine(ByteCode.MOMLDO, new int[] {
									3, value0.index, eventType, temp });
						}
					} else if (rightThing.getType() == MEngineParser.Identifier) {
						String id = rightThing.getText();
						if (isFunction(id)) {

							Function func = this.getFunctionById(id);
							if (func.params.size() == 1
									&& func.params.get(0).type == ReturnType.Element) {
								valid = true;
								int temp = curContext.getTempVar();
								if (func.isp != -1) {
									curContext.addAsmLine(ByteCode.MOVINT,
											new int[] { temp, func.isp });
									curContext
											.addAsmLine(
													ByteCode.MOMLDO,
													new int[] {
															3,
															value0.index,
															eventType,
															temp,
															this.getStringConstIndex(func.lib) });
								} else {

									curContext.asmCodes.add("LBLTOINT " + temp
											+ " FUNC_ENTRY_" + id);
									functionCalls.add(id);
									curContext
											.addAsmLine(
													ByteCode.MOMLDO,
													new int[] {
															3,
															value0.index,
															eventType,
															temp,
															this.getStringConstIndex(func.lib) });
								}
							} else {
								throw new RuntimeException(
										"invalid onFocus/LostFocus callback<"
												+ id
												+ ">. (parameter mismatch)");
							}
						}
					}
				}
				break;
			}
		}

		case MEngineParser.OnKeyDown:
		case MEngineParser.OnKeyUp:
		case MEngineParser.OnKeyPress:
		case MEngineParser.OnLeftSoftKey:
		case MEngineParser.OnRightSoftKey:
		case MEngineParser.OnFingerDown:
		case MEngineParser.OnFingerUp:
		case MEngineParser.OnFingerMove:
		case MEngineParser.OnFingerZoomOut:
		case MEngineParser.OnFingerZoomIn:
		case MEngineParser.OnResourceLoaded:
		case MEngineParser.OnData:
		case MEngineParser.OnFrameUpdate:
		case MEngineParser.OnCommand:
		case MEngineParser.OnSms: {
			if (value0.type == ReturnType.Player
					|| value0.type == ReturnType.Text) {
				if (right.getChildCount() == 1) {
					CommonTree rightThing = (CommonTree) right.getChild(0);
					if (rightThing.getType() == MEngineParser.NumericLiteral) {
						if (rightThing.getText().equals("0")
								|| rightThing.getText().equals("-1")) {
							valid = true;

							int temp = curContext.getTempVar();
							curContext.addAsmLine(ByteCode.MOVINT, new int[] {
									temp, -1 });
							if (left.getType() == MEngineParser.Global) {
								curContext
										.addAsmLine(
												ByteCode.REGCALLBACK,
												new int[] {
														getGlobalRegOp(right
																.getType()),
														temp, -1 });
							} else {
								curContext
										.addAsmLine(
												ByteCode.REGPLAYERCALLBACK,
												new int[] {
														value0.index,
														getPlayerRegOp(right
																.getType()),
														temp, -1 });
							}

						}
					} else if (rightThing.getType() == MEngineParser.Identifier) {
						valid = true;
						String id = rightThing.getText();
						if (!isFunction(id)) {
							throw new RuntimeException(id
									+ " is not a valid function.");
						}
						Function function = getFunctionById(id);
						int isp = -1;
						int libIndex = -1;
						int temp = curContext.getTempVar();
						if (function.isp == -1) {

							curContext.asmCodes.add("LBLTOINT " + temp
									+ " FUNC_ENTRY_" + id);
							functionCalls.add(id);
							isp = temp;
							libIndex = this.getStringConstIndex(function.lib);
						} else {
							curContext.addAsmLine(ByteCode.MOVINT, new int[] {
									temp, function.isp });
							isp = temp;
							libIndex = this.getStringConstIndex(function.lib);
						}
						if (left.getType() == MEngineParser.Global) {
							curContext.addAsmLine(ByteCode.REGCALLBACK,
									new int[] {
											getGlobalRegOp(right.getType()),
											isp, libIndex });
						} else {
							curContext.addAsmLine(ByteCode.REGPLAYERCALLBACK,
									new int[] { value0.index,
											getPlayerRegOp(right.getType()),
											isp, libIndex });
						}

					}
				}
				if (!valid) {
					throw new RuntimeException(
							"invalid params for MPlayer callbacks");
				}
			}
			break;
		}

		case MEngineParser.DefineState: {
			if (value0.type == ReturnType.Player) {
				if (right.getChildCount() == 2) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					ReturnValue value2 = parseStatement((CommonTree) right
							.getChild(1));
					if (value1.type == ReturnType.Int) {
						if (value2.type == ReturnType.Array) {
							valid = true;
							curContext.addAsmLine(ByteCode.DEFINESTATE,
									new int[] { value0.index, value1.index,
											value2.index, 1 });
						} else if (value2.type == ReturnType.Str) {
							valid = true;
							curContext.addAsmLine(ByteCode.DEFINESTATE,
									new int[] { value0.index, value1.index,
											value2.index, 0 });
						}
					}
				}
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid params for MPlayer.defineState(int,String) or MPlayer.defineState(int,Array)");
			}
			break;
		}

		case MEngineParser.SetState: {
			if (value0.type == ReturnType.Player) {
				ReturnValue value1 = parseStatement((CommonTree) right
						.getChild(0));
				if (value1.type == ReturnType.Int) {
					valid = true;
					curContext.addAsmLine(ByteCode.SETPLAYERDATA,
							new int[] { value1.index, value0.index,
									getPlayerOpType(right.getType()) });
				}
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid params for MPlayer.setState(int)");
			}
			break;
		}
		case MEngineParser.GetZIndex:
		case MEngineParser.GetVisible: {
			if (value0.type == ReturnType.Group) {
				valid = true;
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETGROUPDATA,
						new int[] { returnValue.index, value0.index,
								getGroupOpType(right.getType()) });
			} else if (value0.type == ReturnType.Tiles) {
				valid = true;
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETTILESDATA,
						new int[] { returnValue.index, value0.index,
								getTilesOpType(right.getType()) });
			}
		}
		case MEngineParser.GetFocusable: {
			if (value0.type == ReturnType.Player) {
				valid = true;
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETPLAYERDATA,
						new int[] { returnValue.index, value0.index,
								getPlayerOpType(right.getType()) });
			} else if (value0.type == ReturnType.Text) {
				valid = true;
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETTEXTDATA,
						new int[] { returnValue.index, value0.index,
								getTextOpType(right.getType()) });
			}
			break;
		}
		case MEngineParser.SetRect: {
			if (value0.type == ReturnType.Player) {
				if (right.getChildCount() == 1) {

					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Rect) {
						valid = true;
						curContext.addAsmLine(ByteCode.SETPLAYERDATA,
								new int[] { value1.index, value0.index,
										getPlayerOpType(right.getType()) });
					}
				}
			}
			break;
		}
		case MEngineParser.SetZIndex:
		case MEngineParser.SetVisible: {
			if (value0.type == ReturnType.Group) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.SETGROUPDATA, new int[] {
								value1.index, value0.index,
								getGroupOpType(right.getType()) });
					}
				}
				break;
			} else if (value0.type == ReturnType.Tiles) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.SETTILESDATA, new int[] {
								value1.index, value0.index,
								getTilesOpType(right.getType()) });
					}
				}
				break;
			}
		}
		case MEngineParser.SetFocusable: {
			if (value0.type == ReturnType.Player) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.SETPLAYERDATA,
								new int[] { value1.index, value0.index,
										getPlayerOpType(right.getType()) });
					}
				}
			} else if (value0.type == ReturnType.Text) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.SETTEXTDATA,
								new int[] { value1.index, value0.index,
										getTextOpType(right.getType()) });
					}
				}
			}
			break;
		}
		case MEngineParser.GetBgColor:
			if (value0.type == ReturnType.Player) {
				valid = true;
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETPLAYERDATA,
						new int[] { returnValue.index, value0.index,
								getPlayerOpType(right.getType()) });
				break;
			}
		case MEngineParser.GetFontSize:
		case MEngineParser.GetFontModifier:
		case MEngineParser.GetBorderColor:
		case MEngineParser.GetTextColor:
		case MEngineParser.GetMaxLines:
		case MEngineParser.GetAlign:
		case MEngineParser.GetVerticalAlign: {
			if (value0.type == ReturnType.Text) {
				valid = true;
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETTEXTDATA,
						new int[] { returnValue.index, value0.index,
								getTextOpType(right.getType()) });
			}
			break;
		}
		case MEngineParser.SetBorderColor:
		case MEngineParser.SetBgColor:
			if (value0.type == ReturnType.Player) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.SETPLAYERDATA,
								new int[] { value1.index, value0.index,
										getPlayerOpType(right.getType()) });
					}
				}
				break;
			}
		case MEngineParser.SetFontSize:
		case MEngineParser.SetFontModifier:
		case MEngineParser.SetTextColor:
		case MEngineParser.SetMaxLines:
		case MEngineParser.SetAlign:
		case MEngineParser.SetVerticalAlign: {
			if (value0.type == ReturnType.Text) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type == ReturnType.Int) {
						valid = true;
						curContext.addAsmLine(ByteCode.SETTEXTDATA,
								new int[] { value1.index, value0.index,
										getTextOpType(right.getType()) });
					}
				}
			}
			break;
		}
		case MEngineParser.Layout: {
			if (value0.type == ReturnType.Text) {
				valid = true;
				curContext.addAsmLine(ByteCode.LAYOUTTEXT,
						new int[] { value0.index });
			}
			if (!valid) {
				throw new RuntimeException("invalid params for MText.layout()");
			}
			break;
		}

		case MEngineParser.AddChild: {
			if (value0.type == ReturnType.Group
					|| value0.type == ReturnType.Element) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type.ordinal() > ReturnType.Rect.ordinal()
							&& value1.type != ReturnType.Void)
						valid = true;

					curContext.addAsmLine(ByteCode.ADDCHILD, new int[] {
							value0.index, value1.index });
				}
			}
			if (!valid) {
				throw new RuntimeException("invalid params for addChild()");
			}
			break;
		}
		case MEngineParser.RemoveChild: {
			if (value0.type == ReturnType.Group
					|| value0.type == ReturnType.Element) {
				if (right.getChildCount() == 1) {
					ReturnValue value1 = parseStatement((CommonTree) right
							.getChild(0));
					if (value1.type.ordinal() > ReturnType.Rect.ordinal()
							&& value1.type != ReturnType.Void)
						valid = true;

					curContext.addAsmLine(ByteCode.REMOVECHILD, new int[] {
							value0.index, value1.index });
				}
			}
			if (!valid) {
				throw new RuntimeException("invalid params for removeChild()");
			}
			break;
		}
		case MEngineParser.InsertAfter:
		case MEngineParser.InsertBefore: {
			if (value0.type == ReturnType.Element) {
				if (right.getChildCount() == 2) {
					ReturnValue right0 = parseStatement((CommonTree) right
							.getChild(0));
					ReturnValue right1 = parseStatement((CommonTree) right
							.getChild(1));
					if (right0.type == ReturnType.Element
							&& right1.type == ReturnType.Str) {
						valid = true;
						int mode = 0;
						if (right.getType() == MEngineParser.InsertAfter) {
							mode = 1;
						}
						curContext.addAsmLine(ByteCode.MOMLDO,
								new int[] { 4, mode, value0.index,
										right0.index, right1.index });
					}
				}
			}
			break;
		}

		case MEngineParser.Append: {
			if (value0.type == ReturnType.Array) {
				ReturnValue value1 = parseStatement((CommonTree) right
						.getChild(0));

				valid = true;
				curContext.addAsmLine(ByteCode.AAPPEND, new int[] {
						value0.index, value1.index, value1.type.ordinal() });
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid params for MArray.append(...)");
			}

			break;
		}
		case MEngineParser.Delete: {
			if (value0.type == ReturnType.Array) {
				ReturnValue value1 = parseStatement((CommonTree) right
						.getChild(0));
				if (value1.type == ReturnType.Int) {
					valid = true;
					curContext.addAsmLine(ByteCode.ADEL, new int[] {
							value0.index, value1.index });
				}

			} else if (value0.type == ReturnType.Map) {
				ReturnValue value1 = parseStatement((CommonTree) right
						.getChild(0));
				if (value1.type == ReturnType.Str) {
					valid = true;
					curContext.addAsmLine(ByteCode.MDEL, new int[] {
							value0.index, value1.index });
				}
			}
			if (!valid) {
				throw new RuntimeException(
						"invalid params for Array.delete(int/String)");
			}
			break;
		}
		case MEngineParser.HasValue: {
			if (value0.type == ReturnType.Array) {
				ReturnValue value1 = parseStatement((CommonTree) right
						.getChild(0));
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.AHAS, new int[] {
						returnValue.index, value0.index, value1.index });
				valid = true;
			}

			break;
		}
		case MEngineParser.HasKey: {
			if (value0.type == ReturnType.Map) {
				ReturnValue value1 = parseStatement((CommonTree) right
						.getChild(0));
				returnValue.type = ReturnType.Int;
				returnValue.index = curContext.getTempVar();
				if (value1.type == ReturnType.Str) {
					curContext.addAsmLine(ByteCode.MHAS, new int[] {
							returnValue.index, value0.index, value1.index });
					valid = true;
				}
			}

			break;
		}
		case MEngineParser.GetType: {
			returnValue.type = ReturnType.Int;
			returnValue.index = curContext.getTempVar();
			if (value0.type == ReturnType.Array) {
				ReturnValue value1 = parseStatement((CommonTree) right
						.getChild(0));
				if (value1.type == ReturnType.Int) {
					curContext.addAsmLine(ByteCode.AGETTYPE, new int[] {
							returnValue.index, value0.index, value1.index });
					valid = true;
				}
			} else if (value0.type == ReturnType.Map) {
				ReturnValue value1 = parseStatement((CommonTree) right
						.getChild(0));
				if (value1.type == ReturnType.Str) {
					curContext.addAsmLine(ByteCode.MGETTYPE, new int[] {
							returnValue.index, value0.index, value1.index });
					valid = true;
				}
			}
			break;
		}
		case MEngineParser.Insert: {
			if (value0.type == ReturnType.Array) {
				ReturnValue value1 = parseStatement((CommonTree) right
						.getChild(0));
				ReturnValue value2 = parseStatement((CommonTree) right
						.getChild(1));

				if (value1.type == ReturnType.Int) {
					valid = true;
					curContext.addAsmLine(ByteCode.AINSERT, new int[] {
							value0.index, value1.index, value2.index,
							value2.type.ordinal() });

				}

			}
			if (!valid) {
				throw new RuntimeException(
						"invalid params for Array.insert(int,...)");
			}
			break;
		}

		case MEngineParser.GetSize: {
			returnValue.type = ReturnType.Int;
			returnValue.index = curContext.getTempVar();
			if (value0.type == ReturnType.Array) {
				valid = true;
				curContext.addAsmLine(ByteCode.ASIZE, new int[] {
						returnValue.index, value0.index });
			} else if (value0.type == ReturnType.Map) {
				valid = true;
				curContext.addAsmLine(ByteCode.MSIZE, new int[] {
						returnValue.index, value0.index });
			}
			if (!valid) {
				throw new RuntimeException("getSize() must op on Array object");
			}
			break;
		}
		case MEngineParser.ToString: {
			returnValue.type = ReturnType.Str;
			returnValue.index = curContext.getTempVar();
			if (value0.type == ReturnType.Array) {
				valid = true;

			} else if (value0.type == ReturnType.Map) {
				valid = true;
			} else if (value0.type == ReturnType.Int) {
				valid = true;

			}
			curContext.addAsmLine(ByteCode.TOSTR, new int[] {
					returnValue.index, value0.index, value0.type.ordinal() });
			if (!valid) {
				throw new RuntimeException(
						"["
								+ left.getText()
								+ "].toStr() ERROR only int and Array type can have toString() method");
			}
			break;
		}

		case MEngineParser.GetRect: {
			if (value0.type == ReturnType.Player) {
				valid = true;
				returnValue.type = ReturnType.Rect;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.GETPLAYERDATA,
						new int[] { returnValue.index, value0.index,
								getPlayerOpType(right.getType()) });
				break;
			}
		}
		case MEngineParser.GetInt:
		case MEngineParser.GetString:
			if (value0.type == ReturnType.Element
					&& (right.getType() == MEngineParser.GetInt || right
							.getType() == MEngineParser.GetString)) {
				if (right.getChildCount() == 1) {
					ReturnValue right0 = parseStatement((CommonTree) right
							.getChild(0));
					if (right0.type == ReturnType.Str) {
						valid = true;
						if (right.getType() == MEngineParser.GetInt) {
							returnValue.type = ReturnType.Int;
							returnValue.index = curContext.getTempVar();
							curContext.addAsmLine(ByteCode.MOMLGET, new int[] {
									returnValue.index, 0, value0.index,
									right0.index });
						} else if (right.getType() == MEngineParser.GetString) {
							returnValue.type = ReturnType.Str;
							returnValue.index = curContext.getTempVar();
							curContext.addAsmLine(ByteCode.MOMLGET, new int[] {
									returnValue.index, 1, value0.index,
									right0.index });
						}
					}
				}

				break;
			}
		case MEngineParser.GetArray:
		case MEngineParser.GetMap:
		case MEngineParser.GetPlayer:
		case MEngineParser.GetTextObj:
		case MEngineParser.GetGroup:
		case MEngineParser.GetElement:
		case MEngineParser.GetTiles: {
			ReturnValue value1 = parseStatement((CommonTree) right.getChild(0));
			returnValue.index = curContext.getTempVar();
			returnValue.type = getArrayOpReturnType(right.getType());
			if (value0.type == ReturnType.Array
					&& value1.type == ReturnType.Int) {
				valid = true;
				curContext.addAsmLine(ByteCode.AGET, new int[] {
						returnValue.index, value0.index, value1.index,
						returnValue.type.ordinal() });

			} else if (value0.type == ReturnType.Map
					&& value1.type == ReturnType.Str) {
				valid = true;
				curContext.addAsmLine(ByteCode.MGET, new int[] {
						returnValue.index, value0.index, value1.index,
						returnValue.type.ordinal() });
			}
			break;
		}
		case MEngineParser.GetKeys: {
			if (value0.type == ReturnType.Map) {
				valid = true;
				returnValue.type = ReturnType.Array;
				returnValue.index = curContext.getTempVar();
				curContext.addAsmLine(ByteCode.MGET, new int[] {
						returnValue.index, value0.index, 0, -1 });
			}
			if (!valid) {
				throw new RuntimeException("invalid getKeys expression");
			}
			break;
		}
		case MEngineParser.Set: {
			ReturnValue value1 = parseStatement((CommonTree) right.getChild(0));
			ReturnValue value2 = parseStatement((CommonTree) right.getChild(1));
			if (value0.type == ReturnType.Array
					&& value1.type == ReturnType.Int) {
				valid = true;
				curContext.addAsmLine(ByteCode.ASET, new int[] { value0.index,
						value1.index, value2.index, value2.type.ordinal() });
			} else if (value0.type == ReturnType.Map
					&& value1.type == ReturnType.Str) {
				valid = true;
				curContext.addAsmLine(ByteCode.MSET, new int[] { value0.index,
						value1.index, value2.index, value2.type.ordinal() });
			} else if (value0.type == ReturnType.Element
					&& value1.type == ReturnType.Str
					&& (value2.type == ReturnType.Int || value2.type == ReturnType.Str)) {

				valid = true;
				int mode = 0;
				if (value2.type == ReturnType.Str) {
					mode = 1;
				}
				curContext.addAsmLine(ByteCode.MOMLSET, new int[] { mode,
						value0.index, value1.index, value2.index });
			}

			break;
		}
		case MEngineParser.ToInt: {
			if (value0.type == ReturnType.Str) {
				returnValue.index = curContext.getTempVar();
				returnValue.type = ReturnType.Int;
				curContext.addAsmLine(ByteCode.TOINT, new int[] {
						returnValue.index, value0.index });
				valid = true;
			} else if (value0.type == ReturnType.Void
					&& isFunction(left.getText())) {

				valid = true;
				returnValue.index = curContext.getTempVar();
				returnValue.type = ReturnType.Int;
				String func = left.getText();
				curContext.asmCodes.add("LBLTOINT " + returnValue.index
						+ " FUNC_ENTRY_" + func);
				functionCalls.add(func);
			}
			break;
		}
		case MEngineParser.Length: {
			if (value0.type == ReturnType.Str) {
				returnValue.index = curContext.getTempVar();
				returnValue.type = ReturnType.Int;
				curContext.addAsmLine(ByteCode.STRLEN, new int[] {
						returnValue.index, value0.index });
				valid = true;
			}
			break;
		}
		case MEngineParser.SubString: {
			if (value0.type == ReturnType.Str) {
				ReturnValue value1 = parseStatement((CommonTree) right
						.getChild(0));
				ReturnValue value2 = parseStatement((CommonTree) right
						.getChild(1));
				if (value1.type == ReturnType.Int
						&& value2.type == ReturnType.Int) {
					returnValue.type = ReturnType.Str;
					returnValue.index = curContext.getTempVar();

					curContext.addAsmLine(ByteCode.SUBSTR, new int[] {
							returnValue.index, value0.index, value1.index,
							value2.index });
					valid = true;
				}
			}
			break;
		}
		case MEngineParser.Replace: {
			if (value0.type == ReturnType.Str) {
				ReturnValue value1 = parseStatement((CommonTree) right
						.getChild(0));
				ReturnValue value2 = parseStatement((CommonTree) right
						.getChild(1));
				if (value1.type == ReturnType.Str
						&& value2.type == ReturnType.Str) {
					returnValue.type = ReturnType.Str;
					returnValue.index = curContext.getTempVar();
					curContext.addAsmLine(ByteCode.STRREPLACE, new int[] {
							returnValue.index, value0.index, value1.index,
							value2.index });
					valid = true;
				}
			}
			break;
		}
		case MEngineParser.MatchFingerToCell: {
			if (value0.type == ReturnType.Tiles) {
				ReturnValue value1 = parseStatement((CommonTree) right
						.getChild(0));
				ReturnValue value2 = parseStatement((CommonTree) right
						.getChild(1));
				if (value1.type == ReturnType.Int
						&& value2.type == ReturnType.Int) {
					returnValue.index = curContext.getTempVar();
					returnValue.type = ReturnType.Array;
					valid = true;
					curContext.addAsmLine(ByteCode.GETTILESDATA, new int[] {
							returnValue.index, value0.index,
							getTilesOpType(right.getType()), value1.index,
							value2.index });

				}
			}
			break;
		}
		case MEngineParser.MatchCellToCoord: {
			if (value0.type == ReturnType.Tiles) {
				ReturnValue value1 = parseStatement((CommonTree) right
						.getChild(0));
				ReturnValue value2 = parseStatement((CommonTree) right
						.getChild(1));
				if (value1.type == ReturnType.Int
						&& value2.type == ReturnType.Int) {
					returnValue.index = curContext.getTempVar();
					returnValue.type = ReturnType.Array;
					valid = true;
					curContext.addAsmLine(ByteCode.GETTILESDATA, new int[] {
							returnValue.index, value0.index,
							getTilesOpType(right.getType()), value1.index,
							value2.index });

				}
			}
			break;
		}
		case MEngineParser.Split: {
			if (value0.type == ReturnType.Str) {
				ReturnValue value1 = parseStatement((CommonTree) right
						.getChild(0));
				ReturnValue value2 = parseStatement((CommonTree) right
						.getChild(1));
				if (value1.type == ReturnType.Str
						&& value2.type == ReturnType.Int) {
					returnValue.index = curContext.getTempVar();
					returnValue.type = ReturnType.Array;
					valid = true;
					curContext.addAsmLine(ByteCode.STRSPLIT, new int[] {
							returnValue.index, value0.index, value1.index,
							value2.index });
				}
			}
			break;
		}
		case MEngineParser.IndexOf: {
			if (value0.type == ReturnType.Str) {
				ReturnValue value1 = parseStatement((CommonTree) right
						.getChild(0));
				ReturnValue value2 = parseStatement((CommonTree) right
						.getChild(1));
				ReturnValue value3 = parseStatement((CommonTree) right
						.getChild(2));
				if (value1.type == ReturnType.Str
						&& value2.type == ReturnType.Int
						&& value3.type == ReturnType.Int) {
					returnValue.index = curContext.getTempVar();
					returnValue.type = ReturnType.Int;
					valid = true;
					curContext.addAsmLine(ByteCode.STRPOS, new int[] {
							returnValue.index, value0.index, value1.index,
							value2.index, 0 });

				}
			}
			break;
		}
		case MEngineParser.ToUpper:
		case MEngineParser.ToLower: {
			if (value0.type == ReturnType.Str) {
				returnValue.index = curContext.getTempVar();
				returnValue.type = ReturnType.Str;
				valid = true;
				curContext.addAsmLine(ByteCode.STRCASE, new int[] {
						returnValue.index, value0.index,
						(type == MEngineParser.ToUpper ? 1 : 2) });
			}
			break;
		}

		}
		if (!valid) {
			throw new RuntimeException(
					"invalid dotted statement arround pattern: ["
							+ left.getText() + "." + right.getText() + "]");
		}
		return valid;
	}

	private void genCaseBlock(CommonTree tree, int switchIndex) {

		int start = 1;
		if (tree.getType() == MEngineParser.Default) {
			curContext.asmCodes.add(":switch" + switchIndex + "default");
			start = 0;
		} else {
			int value = 0;
			String text = tree.getChild(0).getText();
			if (tree.getChild(0).getType() == MEngineParser.NumericLiteral) {
				value = Integer.parseInt(tree.getChild(0).getText());
			} else if (tree.getChild(0).getType() == MEngineParser.Identifier) {
				if (intConsts.containsKey(text)) {
					value = intConsts.get(text);
				}
			} else {
				throw new RuntimeException("invalid case : " + text);
			}
			curContext.asmCodes.add(":switch" + switchIndex + "_case" + value);
		}
		int count = tree.getChildCount();
		for (int i = start; i < count; i++) {
			parseStatement((CommonTree) tree.getChild(i));
		}
		curContext.asmCodes.add("JMP switchEnd" + switchIndex);
	}

	private ReturnType getArrayOpReturnType(int type) {
		switch (type) {
		case MEngineParser.GetInt: {
			return ReturnType.Int;
		}
		case MEngineParser.GetString: {
			return ReturnType.Str;
		}
		case MEngineParser.GetArray: {
			return ReturnType.Array;
		}
		case MEngineParser.GetMap: {
			return ReturnType.Map;
		}
		case MEngineParser.GetRect: {
			return ReturnType.Rect;
		}
		case MEngineParser.GetPlayer: {
			return ReturnType.Player;
		}
		case MEngineParser.GetTextObj: {
			return ReturnType.Text;
		}
		case MEngineParser.GetGroup: {
			return ReturnType.Group;
		}
		case MEngineParser.GetTiles: {
			return ReturnType.Tiles;
		}
		case MEngineParser.GetElement: {
			return ReturnType.Element;
		}
		default: {
			return ReturnType.Void;
		}
		}
	}

	private List<Integer> getCaseValues(CommonTree tree) {
		List<Integer> list = new ArrayList<Integer>();
		for (Object thing : tree.getChildren()) {
			CommonTree child = (CommonTree) thing;
			if (child.getType() == MEngineParser.Case) {
				try {
					if (child.getChild(0).getType() == MEngineParser.NumericLiteral) {
						list.add(Integer.parseInt(child.getChild(0).getText()));
					} else if (child.getChild(0).getType() == MEngineParser.Identifier) {
						String id = child.getChild(0).getText();
						if (intConsts.containsKey(id)) {
							list.add(intConsts.get(id));
						} else {
							throw new Exception(id
									+ " is not a numeric contant");
						}
					} else {
						throw new Exception(child.getChild(0).getText()
								+ " invalid case block");
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else if (child.getType() == MEngineParser.Default) {
				list.add(Integer.MAX_VALUE);
			}
		}
		return list;
	}

	private List<CommonTree> getForStatements(CommonTree tree) {
		List<CommonTree> list = new ArrayList<CommonTree>();
		CommonTree init = null;
		CommonTree exp1 = null;
		CommonTree exp2 = null;
		CommonTree exp3 = null;
		int state = 0;
		for (Object thing : tree.getChildren()) {
			CommonTree child = (CommonTree) thing;
			if (child.getText().equals("(")) {
				continue;
			}
			switch (state) {
			case 0: {
				if (child.getText().equals(";")) {
					state = 1;
				} else {
					init = child;
				}
				break;
			}
			case 1: {
				if (child.getText().equals(";")) {
					state = 2;
				} else {
					exp1 = child;
				}
				break;
			}
			case 2: {
				if (child.getText().equals(")")) {
					state = 3;
				} else {
					exp2 = child;
				}
				break;
			}
			case 3: {
				exp3 = child;
				break;
			}
			}
		}
		list.add(init);
		list.add(exp1);
		list.add(exp2);
		list.add(exp3);
		return list;
	}

	private String getArithAssignOpCode(int nodeType) {
		switch (nodeType) {
		case MEngineParser.AddEQ: {
			return "ADD";
		}
		case MEngineParser.MinusEQ: {
			return "SUB";
		}
		case MEngineParser.MultiplyEQ: {
			return "MUL";
		}
		case MEngineParser.DivideEQ: {
			return "DIV";
		}
		case MEngineParser.ModEQ: {
			return "MOD";
		}
		case MEngineParser.LeftShiftEQ: {
			return "LSH";
		}
		case MEngineParser.RightShiftEQ: {
			return "RSH";
		}
		case MEngineParser.AndEQ: {
			return "AND";
		}
		case MEngineParser.OrEQ: {
			return "OR";
		}
		}
		return null;
	}

	private String getArithOpcode(int nodeType) {
		switch (nodeType) {
		case MEngineParser.Add: {
			return "ADD";
		}
		case MEngineParser.Minus: {
			return "SUB";
		}
		case MEngineParser.Multiply: {
			return "MUL";
		}
		case MEngineParser.Divide: {
			return "DIV";
		}
		case MEngineParser.Mod: {
			return "MOD";
		}
		case MEngineParser.LeftShift: {
			return "LSH";
		}
		case MEngineParser.RightShift: {
			return "RSH";
		}
		case MEngineParser.BitAnd: {
			return "AND";
		}
		case MEngineParser.BitOr: {
			return "OR";
		}
		case MEngineParser.BitXor: {
			return "XOR";
		}
		}
		return null;
	}

	private int getCompareType(int nodeType) {
		switch (nodeType) {
		case MEngineParser.EqEq: {
			return 0;
		}
		case MEngineParser.NotEq: {
			return 1;
		}
		case MEngineParser.GreatThan: {
			return 2;
		}
		case MEngineParser.GreatOrEq: {
			return 3;
		}
		case MEngineParser.LessThan: {
			return 4;
		}
		case MEngineParser.LessOrEq: {
			return 5;
		}
		}
		return -1;
	}

	private int getTextOpType(int type) {
		switch (type) {
		case MEngineParser.GetX: {
			return 0;
		}
		case MEngineParser.GetY: {
			return 1;
		}
		case MEngineParser.GetZIndex: {
			return 4;
		}
		case MEngineParser.GetVisible: {
			return 5;
		}
		case MEngineParser.GetMaxWidth: {
			return 2;
		}
		case MEngineParser.GetMaxHeight: {
			return 3;
		}
		case MEngineParser.GetFontSize: {
			return 6;
		}
		case MEngineParser.GetFontModifier: {
			return 7;
		}
		case MEngineParser.GetBgColor: {
			return 8;
		}
		case MEngineParser.GetBorderColor: {
			return 9;
		}
		case MEngineParser.GetTextColor: {
			return 10;
		}
		case MEngineParser.GetMaxLines: {
			return 11;
		}
		case MEngineParser.GetAlign: {
			return 12;
		}
		case MEngineParser.GetVerticalAlign: {
			return 13;
		}
		case MEngineParser.GetBgTransparent: {
			return 14;
		}
		case MEngineParser.GetText: {
			return 15;
		}

		case MEngineParser.SetX: {
			return 0;
		}
		case MEngineParser.SetY: {
			return 1;
		}
		case MEngineParser.SetZIndex: {
			return 4;
		}
		case MEngineParser.SetVisible: {
			return 5;
		}
		case MEngineParser.SetMaxWidth: {
			return 2;
		}
		case MEngineParser.SetMaxHeight: {
			return 3;
		}
		case MEngineParser.SetFontSize: {
			return 6;
		}
		case MEngineParser.SetFontModifier: {
			return 7;
		}
		case MEngineParser.SetBgColor: {
			return 8;
		}
		case MEngineParser.SetBorderColor: {
			return 9;
		}
		case MEngineParser.SetTextColor: {
			return 10;
		}
		case MEngineParser.SetMaxLines: {
			return 11;
		}
		case MEngineParser.SetAlign: {
			return 12;
		}
		case MEngineParser.SetVerticalAlign: {
			return 13;
		}

		case MEngineParser.SetText: {
			return 15;
		}
		case MEngineParser.GetPivotX: {
			return 16;
		}
		case MEngineParser.GetPivotY: {
			return 17;
		}
		case MEngineParser.GetRotateDegree: {
			return 18;
		}
		case MEngineParser.GetScaleX: {
			return 19;
		}
		case MEngineParser.GetScaleY: {
			return 20;
		}
		case MEngineParser.GetLayoutWidth: {
			return 21;
		}
		case MEngineParser.GetLayoutHeight: {
			return 22;
		}

		}
		throw new RuntimeException("invalid text optype: " + type);
	}

	private int getGroupOpType(int type) {
		switch (type) {
		case MEngineParser.GetZIndex: {
			return 0;
		}
		case MEngineParser.GetVisible: {
			return 1;
		}

		case MEngineParser.GetX: {
			return 2;
		}
		case MEngineParser.GetY: {
			return 3;
		}
		case MEngineParser.GetData: {
			return 4;
		}
		case MEngineParser.GetViewPort: {
			return 5;
		}
		case MEngineParser.SetZIndex: {
			return 0;
		}
		case MEngineParser.SetVisible: {
			return 1;
		}

		case MEngineParser.SetX: {
			return 2;
		}
		case MEngineParser.SetY: {
			return 3;
		}
		case MEngineParser.SetData: {
			return 4;
		}
		case MEngineParser.SetViewPort: {
			return 5;
		}
		case MEngineParser.GetPivotX: {
			return 6;
		}
		case MEngineParser.GetPivotY: {
			return 7;
		}
		case MEngineParser.GetRotateDegree: {
			return 8;
		}
		case MEngineParser.GetScaleX: {
			return 9;
		}
		case MEngineParser.GetScaleY: {
			return 10;
		}
		}
		throw new RuntimeException("invalid group optype: " + type);
	}

	private int getTilesOpType(int type) {
		switch (type) {
		case MEngineParser.GetZIndex: {
			return 0;
		}
		case MEngineParser.GetVisible: {
			return 1;
		}
		case MEngineParser.GetX: {
			return 2;
		}
		case MEngineParser.GetY: {
			return 3;
		}
		case MEngineParser.GetWidth: {
			return 4;
		}
		case MEngineParser.GetHeight: {
			return 5;
		}
		case MEngineParser.MatchFingerToCell: {
			return 7;
		}
		case MEngineParser.MatchCellToCoord: {
			return 8;
		}
		case MEngineParser.SetZIndex: {
			return 0;
		}
		case MEngineParser.SetVisible: {
			return 1;
		}
		case MEngineParser.SetX: {
			return 2;
		}
		case MEngineParser.SetY: {
			return 3;
		}
		case MEngineParser.SetCells: {
			return 6;
		}
		case MEngineParser.SetTileMode: {
			return 7;
		}

		}
		return -1;
	}

	private int getPlayerOpType(int type) {
		switch (type) {
		case MEngineParser.GetZIndex: {
			return 0;
		}
		case MEngineParser.GetVisible: {
			return 1;
		}
		case MEngineParser.GetState: {
			return 2;
		}
		case MEngineParser.GetFocusable: {
			return 3;
		}
		case MEngineParser.GetRect: {
			return 6;
		}

		case MEngineParser.GetData: {
			return 8;
		}
		case MEngineParser.GetBgColor: {
			return 10;
		}
		case MEngineParser.GetPivotX: {
			return 11;
		}
		case MEngineParser.GetPivotY: {
			return 12;
		}
		case MEngineParser.GetRotateDegree: {
			return 13;
		}
		case MEngineParser.GetScaleX: {
			return 14;
		}
		case MEngineParser.GetScaleY: {
			return 15;
		}
		case MEngineParser.GetX: {
			return 16;
		}
		case MEngineParser.GetY: {
			return 17;
		}
		case MEngineParser.GetWidth: {
			return 18;
		}
		case MEngineParser.GetHeight: {
			return 19;
		}
		case MEngineParser.SetZIndex: {
			return 0;
		}
		case MEngineParser.SetVisible: {
			return 1;
		}
		case MEngineParser.SetState: {
			return 2;
		}
		case MEngineParser.SetFocusable: {
			return 3;
		}
		case MEngineParser.SetRect: {
			return 6;
		}
		case MEngineParser.SetData: {
			return 8;
		}
		case MEngineParser.SetBgColor: {
			return 10;
		}
		case MEngineParser.SetX: {
			return 16;
		}
		case MEngineParser.SetY: {
			return 17;
		}
		case MEngineParser.SetWidth: {
			return 18;
		}
		case MEngineParser.SetHeight: {
			return 19;
		}
		case MEngineParser.GetClip: {
			return 20;
		}
		case MEngineParser.SetClip: {
			return 20;
		}
		case MEngineParser.CancelClip: {
			return 20;
		}
		case MEngineParser.GetBorderColor: {
			return 21;
		}
		case MEngineParser.SetBorderColor: {
			return 21;
		}

		}
		throw new RuntimeException("invalid player optype: " + type);
	}

	private int getGlobalRegOp(int type) {
		switch (type) {
		case MEngineParser.OnKeyDown: {
			return 1;
		}
		case MEngineParser.OnKeyPress: {
			return 2;
		}
		case MEngineParser.OnKeyUp: {
			return 3;
		}
		case MEngineParser.OnFingerDown: {
			return 4;
		}
		case MEngineParser.OnFingerMove: {
			return 5;
		}
		case MEngineParser.OnFingerUp: {
			return 6;
		}
		case MEngineParser.OnResourceLoaded: {
			return 7;
		}
		case MEngineParser.OnData: {
			return 8;
		}
		case MEngineParser.OnFocus: {
			return 9;
		}
		case MEngineParser.OnLostFocus: {
			return 10;
		}
		case MEngineParser.OnCommand: {
			return 11;
		}
		case MEngineParser.OnLeftSoftKey: {
			return 12;
		}
		case MEngineParser.OnRightSoftKey: {
			return 13;
		}
		case MEngineParser.OnSms: {
			return 14;
		}
		case MEngineParser.OnFingerZoomIn: {
			return 15;
		}
		case MEngineParser.OnFingerZoomOut: {
			return 16;
		}
		case MEngineParser.OnFrameUpdate: {
			return 17;
		}
		}

		throw new RuntimeException("invalid global callback: " + type);
	}

	private int getPlayerRegOp(int type) {
		switch (type) {
		case MEngineParser.OnFingerDown: {
			return 3;
		}
		case MEngineParser.OnFingerMove: {
			return 4;
		}
		case MEngineParser.OnFingerUp: {
			return 5;
		}
		case MEngineParser.OnResourceLoaded: {
			throw new RuntimeException(
					"onResourceLoaded must be registered on global!");
		}
		case MEngineParser.OnData: {
			throw new RuntimeException("onData must be registered on global!");
		}
		case MEngineParser.OnCommand: {
			throw new RuntimeException(
					"onCommand must be registered on global!");
		}
		// case MEngineParser.OnWillDraw: {
		// return 6;
		// }
		// case MEngineParser.OnDidDraw: {
		// return 7;
		// }
		}

		throw new RuntimeException("invalid player callback: " + type);
	}

	private int getRectOpType(int type) {
		switch (type) {
		case MEngineParser.GetX: {
			return 0;
		}
		case MEngineParser.GetY: {
			return 1;
		}
		case MEngineParser.GetWidth: {
			return 2;
		}
		case MEngineParser.GetHeight: {
			return 3;
		}
		case MEngineParser.ContainsPoint: {
			return 6;
		}
		case MEngineParser.ContainsRect: {
			return 7;
		}
		case MEngineParser.Intersacts: {
			return 8;
		}
		case MEngineParser.SetX: {
			return 0;
		}
		case MEngineParser.SetY: {
			return 1;
		}
		case MEngineParser.SetWidth: {
			return 2;
		}
		case MEngineParser.SetHeight: {
			return 3;
		}
		case MEngineParser.MovePos: {
			return 4;
		}
		case MEngineParser.ResizeBounds: {
			return 5;
		}
		}
		throw new RuntimeException("invalid rect optype: " + type);
	}

	private FunctionParam getParamFromTokens(String token, String name) {
		if (token.equals("int")) {
			return new FunctionParam(ReturnType.Int, name, null);
		} else if (token.equals("String")) {
			return new FunctionParam(ReturnType.Str, name, null);
		} else if (token.equals("Array")) {
			return new FunctionParam(ReturnType.Array, name, null);
		} else if (token.equals("Map")) {
			return new FunctionParam(ReturnType.Map, name, null);
		} else if (token.equals("void")) {
			return new FunctionParam(ReturnType.Void, name, null);
		} else if (token.equals("MRect")) {
			return new FunctionParam(ReturnType.Rect, name, null);
		} else if (token.equals("MPlayer")) {
			return new FunctionParam(ReturnType.Player, name, null);
		} else if (token.equals("MText")) {
			return new FunctionParam(ReturnType.Text, name, null);
		} else if (token.equals("MGroup")) {
			return new FunctionParam(ReturnType.Group, name, null);
		} else if (token.equals("MTiles")) {
			return new FunctionParam(ReturnType.Tiles, name, null);
		} else if (token.equals("Element")) {
			return new FunctionParam(ReturnType.Element, name, null);
		} else {
			for (String key : libs.keySet()) {
				JSONObject lib = libs.get(key);
				if (lib.has("class-def")) {
					try {
						JSONObject cdef = lib.getJSONObject("class-def");
						if (cdef.getString("name").equals(token)) {
							return new FunctionParam(ReturnType.Array, name,
									token);
						}
					} catch (Exception e) {
					}
				}
			}
		}
		return null;
	}

	private ReturnType getReturnTypeFromToken(String token) {
		if (token.equals("int")) {
			return ReturnType.Int;
		} else if (token.equals("String")) {
			return ReturnType.Str;
		} else if (token.equals("Array")) {
			return ReturnType.Array;
		} else if (token.equals("Map")) {
			return ReturnType.Map;
		} else if (token.equals("void")) {
			return ReturnType.Void;
		} else if (token.equals("MRect")) {
			return ReturnType.Rect;
		} else if (token.equals("MPlayer")) {
			return ReturnType.Player;
		} else if (token.equals("MText")) {
			return ReturnType.Text;
		} else if (token.equals("MGroup")) {
			return ReturnType.Group;
		} else if (token.equals("MTiles")) {
			return ReturnType.Tiles;
		} else if (token.equals("Element")) {
			return ReturnType.Element;
		}
		return ReturnType.Void;
	}

	private String getStringFromLiteral(String literal) {
		String result = null;
		if (literal.startsWith("\"") && literal.endsWith("\"")) {
			result = literal.substring(1, literal.length() - 1);
		} else if (literal.startsWith("\'") && literal.endsWith("\'")) {
			result = literal.substring(1, literal.length() - 1);
			result = result.replaceAll("\\\"", "\\\\\"");
		} else {
			result = literal;
		}
		return result;
	}

	private String getLibName() {
		for (String key : libMap.keySet()) {
			String value = libMap.get(key);
			if (value.equals(currentScript)) {
				return key;

			}
		}
		return null;
	}

	private int getDefs(CommonTree tree, boolean isLib) {

		int type = tree.getType();
		if (type == MEngineParser.TypeDef) {
			parseStatement(tree);
			return 3;
		}
		int count = tree.getChildCount();
		boolean isDef = (type == MEngineParser.PTINT)
				|| (type == MEngineParser.PTSTRING)
				|| (type == MEngineParser.PTMAP)
				|| (type == MEngineParser.PTARRAY)
				|| (type == MEngineParser.PTRECT)
				|| (type == MEngineParser.PTPLAYER)
				|| (type == MEngineParser.PTTEXT)
				|| (type == MEngineParser.PTVOID)
				|| (type == MEngineParser.PTGROUP)
				|| (type == MEngineParser.PTTILES)
				|| (type == MEngineParser.PTPAGE)
				|| (type == MEngineParser.PTELEMENT)
				|| (type == MEngineParser.Define);
		isDef &= (count > 0)
				&& (tree.getChild(0).getType() == MEngineParser.Identifier);
		if (isDef) {
			boolean isFunc = (count == 3)
					&& (tree.getChild(0).getType() == MEngineParser.Identifier
							&& tree.getChild(1).getType() == MEngineParser.LeftBracket && tree
							.getChild(2).getType() == MEngineParser.LeftCurley);
			if (isFunc) {
				CommonTree funcName = (CommonTree) tree.getChild(0);
				CommonTree parameterList = (CommonTree) tree.getChild(1);
				Function function = new Function(funcName.getText());
				function.returnValue = getParamFromTokens(tree.getText(),
						funcName.getText());
				int paramSize = parameterList.getChildCount();
				for (int i = 0; i < paramSize; i += 2) {
					String ptype = parameterList.getChild(i).getText();
					String pname = parameterList.getChild(i + 1).getText();

					FunctionParam param = getParamFromTokens(ptype, pname);
					function.params.add(param);
				}
				checkDupName(function.name);
				if (isLib) {
					function.lib = getLibName();
				}
				functions.add(function);

				return 1;
			} else {
				return 2;
			}
		}
		return 0;

	}

	private void checkDupName(String id) {
		if (intConsts.containsKey(id)) {
			throw new RuntimeException(id
					+ " is already declared as integer constants");
		} else {
			for (Function function : functions) {
				if (function.name.equals(id)) {
					throw new RuntimeException(id
							+ " is already declared as function.");
				}
			}
			for (String libName : libs.keySet()) {
				JSONObject lib = libs.get(libName);
				if (lib.has(id)) {
					throw new RuntimeException(id
							+ " is already declared as function in lib: "
							+ libName);
				}
			}

		}
	}

	private void parseFunc(CommonTree tree) {
		CommonTree funcName = (CommonTree) tree.getChild(0);
		CommonTree body = (CommonTree) tree.getChild(2);
		String name = funcName.getText();
		Function function = null;
		for (Function thing : functions) {
			if (thing.name.equals(name)) {
				function = thing;
				break;
			}
		}
		contexts.push(curContext);
		curContext = new Context(funcAsmCodes, global, function);
		curContext.asmCodes.add(":FUNC_ENTRY_" + function.name);
		int index = curContext.asmCodes.size();
		// curContext.addAsmLine(ByteCode.CREATEVAR, new int[] { 0 });
		curContext.pushVarContext(false);
		{
			int paramSize = function.params.size();

			for (int i = 0; i < paramSize; i++) {
				FunctionParam param = function.params.get(i);
				curContext.addFuncParam(param.name, param.type, param.defType);
			}
		}
		if (body.getChildren() != null) {
			for (Object child : body.getChildren()) {
				parseStatement((CommonTree) child);
				curContext.releaseTempVar();
			}
		}
		if (function.returnValue.type != ReturnType.Void
				&& !curContext.functionReturned) {
			throw new RuntimeException("Function " + function.name
					+ " expects return value.");
		}
		curContext.asmCodes.add(":FUNC_EXIT_" + function.name);
		curContext.popAllVarContext(true);
		curContext.asmCodes.add("RET");
		curContext.asmCodes.add("END");
		curContext.asmCodes.add(index, "STACKSIZE " + curContext.varIndex);
		curContext = contexts.pop();

	}

	private String outputAsm(boolean isLib) {
		StringBuffer buffer = new StringBuffer();
		if (!isLib) {
			buffer.append("JMP main_start\r\n");
		}
		boolean skip = false;
		for (String asm : funcAsmCodes) {
			if (asm.startsWith(":FUNC_ENTRY_")) {
				String func = asm.substring(":FUNC_ENTRY_".length());
				if (functionCalls.contains(func)) {
					skip = false;
				} else {
					skip = true;
				}
			}
			if (!skip || isLib) {
				buffer.append(asm).append("\r\n");
			}

		}

		if (!isLib) {
			buffer.append(":main_start\r\n");
			for (String asm : mainAsmCodes) {
				buffer.append(asm).append("\r\n");
			}
			buffer.append("END\r\n");
		}

		int size = strConsts.size();
		for (int i = 0; i < size; i++) {
			String value = strConsts.get(i);
			buffer.append("@" + i + "=" + value + "\r\n");
		}

		buffer.append(";EndVariable definitions\r\n");
		return buffer.toString();
	}

	private void importLib(String libName) throws RuntimeException {
		boolean valid = false;
		if (libMap.containsKey(libName)) {
			if (!libs.containsKey(libName)) {
				String libPath = libMap.get(libName);
				File libSourceFile = new File(libPath);
				String name = libSourceFile.getName();
				String withoutExt = name.substring(0, name.lastIndexOf("."));
				File outputFolder = new File(outputPath);
				if (!outputFolder.isAbsolute()) {
					File resourceParent = resourceFile.getParentFile();
					outputFolder = new File(resourceParent,
							outputFolder.getName());
				}
				File parent = getOutputPath(outputFolder, libSourceFile);
				File ifFile = new File(parent, withoutExt + ".if");
				if (ifFile.exists()) {
					try {
						libs.put(libName, Util.parseJson(ifFile));
					} catch (Exception e) {
						throw new RuntimeException(
								"failed to load lib def for " + libName);
					}
					valid = true;
				}
			} else {
				valid = true;
			}
		}
		if (!valid) {
			throw new RuntimeException("failed to load lib : " + libName);
		}
	}

	String getLibName(String cname) {
		try {
			for (String key : libs.keySet()) {
				JSONObject lib = libs.get(key);
				JSONObject cdef = lib.optJSONObject("class-def");
				if (cdef != null && cdef.getString("name").equals(cname)) {
					return lib.getString("lib");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	JSONObject getDefObj(String name) {
		try {
			for (String key : libs.keySet()) {
				JSONObject lib = libs.get(key);
				JSONObject cdef = lib.optJSONObject("class-def");
				if (cdef != null && cdef.getString("name").equals(name)) {
					return lib;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
