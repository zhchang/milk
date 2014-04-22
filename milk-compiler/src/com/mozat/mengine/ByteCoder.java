package com.mozat.mengine;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mozat.util.Util;

import org.json.JSONObject;

enum ByteCode {
	END, ADD, SUB, MUL, DIV, MOD, LSH, RSH, AND, OR, XOR, CMP, NOT, MOVINT, MOVSTR, CREATEVAR, DELETEVAR, COPYVAR, JE, JNE, JL, JLE, JG, JGE, JMP, CALL, RET, TOSTR, TOINT, STRLEN, SUBSTR, STRCAT, STRREPLACE, STRMATCH, STRPOS, STRCASE, LBLTOINT, URLENCODE, MSIZE, MHAS, MGET, MSET, MDEL, MGETTYPE, MFROMSTR, ASIZE, AGET, ASET, ADEL, AAPPEND, AINSERT, AGETTYPE, AFROMSTR, REGCALLBACK, EMPTY, RAND, SETTIMEOUT, CANCELTIMEOUT, PLAYSOUND, STOPSOUND, LOADSOUND, UNLOADSOUND, GETENV, PLAYSCENE, GETTIMESTAMP, DEBUG, GETRECTDATA, SETRECTDATA, GETPLAYERDATA, SETPLAYERDATA, ATTACHPLAYER, DETACHPLAYER, DEFINESTATE, SENDDATA, GETTEXTDATA, SETTEXTDATA, ATTACHTEXT, DETACHTEXT, LAYOUTTEXT, SAVEDB, LOADDB, MOVE, STOP, REGPLAYERCALLBACK, NAVIGATE, ISNULL, INITOBJ, LOADRESOURCE, GETIMAGESIZE, STACKSIZE, SETENV, GETGROUPDATA, SETGROUPDATA, ADDCHILD, REMOVECHILD, RELATIVEPOS, GETSCREEN, GETPARENT, GETCHILDREN, DRAWSHAPE, TRANSFORM, ARGB, MASK, GETTILESDATA, SETTILESDATA, TILESOP, TRYSTART, TRYFINISH, THROW, GETELEMENT, MOMLSET, MOMLGET, MOMLDO, WINDOWDO, CLEAN, DODEBUG, QUICKINPUT, STRSPLIT, DOINPUT, IMPORT, SENDHTTPDATA, DOCHAT, RESOLVE, AHAS,QUERYENV
}

public class ByteCoder {

	private static final String funcEntry = ":FUNC_ENTRY_";

	public static String genInstruction(ByteCode byteCode, int[] opcodes) {
		switch (byteCode) {
		case END:
		case RET:
		case EMPTY: {
			assert (opcodes.length == 0);
			break;
		}
		case CREATEVAR:
		case DELETEVAR:
		case JMP:
		case CANCELTIMEOUT:
		case STOPSOUND:
		case LOADSOUND:
		case UNLOADSOUND:
		case PLAYSCENE:
		case GETTIMESTAMP:
		case DEBUG:
		case ATTACHPLAYER:
		case DETACHPLAYER:
		case ATTACHTEXT:
		case DETACHTEXT:
		case LAYOUTTEXT:
		case GETENV:
		case STOP: {
			assert (opcodes.length == 1);
			break;
		}
		case MOVINT:
		case MOVSTR:
		case TOINT:
		case STRLEN:
		case LBLTOINT:
		case URLENCODE:
		case MSIZE:
		case MDEL:
		case MGETTYPE:
		case ADEL:
		case AGETTYPE:
		case REGCALLBACK:
		case SENDDATA:
		case NAVIGATE:
		case ISNULL: {
			assert (opcodes.length == 2);
			break;
		}
		case ADD:
		case SUB:
		case MUL:
		case DIV:
		case MOD:
		case LSH:
		case RSH:
		case AND:
		case OR:
		case XOR:
		case NOT:
		case JE:
		case JNE:
		case JL:
		case JLE:
		case JG:
		case JGE:
		case STRCAT:
		case STRCASE:
		case MHAS:
		case AAPPEND:
		case RAND:
		case SETTIMEOUT:
		case PLAYSOUND:
		case GETRECTDATA:
		case SETRECTDATA:
		case GETPLAYERDATA:
		case SETPLAYERDATA:
		case GETTEXTDATA:
		case SETTEXTDATA:
		case SAVEDB:
		case LOADDB:
		case REGPLAYERCALLBACK:
		case GETIMAGESIZE:
		case TOSTR: {
			assert (opcodes.length == 3);
			break;
		}
		case COPYVAR:
		case SUBSTR:
		case STRREPLACE:
		case MGET:
		case MSET:
		case AGET:
		case ASET:
		case AINSERT:
		case DEFINESTATE:
		case LOADRESOURCE: {
			assert (opcodes.length == 4);
			break;
		}
		case CMP:
		case STRPOS:
		case MOVE: {
			assert (opcodes.length == 5);
			break;
		}
		case STRMATCH: {
			assert (opcodes.length == 7);
			break;
		}
		}
		StringBuilder builder = new StringBuilder();
		builder.append(byteCode.toString());
		int count = opcodes.length;
		for (int i = 0; i < count; i++) {
			builder.append(" ").append(opcodes[i]);
		}
		return builder.toString();

	}

	private static int max = -1;

	static Map<String, String> resources = new HashMap<String, String>();

	static int index = 0;

	public static byte[] genByteCode(File asm) {
		return genByteCode(Util.getFileContent(asm));
	}

	public static Map<String, Integer> genInterface(File asm) throws Exception {
		return genInterface(Util.getFileContent(asm));
	}

	public static Map<String, Integer> genInterface(String asm)
			throws Exception {
		Map<String, Integer> interfaces = new HashMap<String, Integer>();
		int curCode = 0;
		String[] lines = asm.replaceAll("\\\r", "").split("\n");
		int count = lines.length;
		for (int i = 0; i < count; i++) {
			String line = lines[i];
			if (line.length() == 0 || line.startsWith(";")
					|| line.trim().length() == 0) {
				continue;
			} else {

				if (line.startsWith("@")) {
				} else if (line.startsWith("%")) {
					throw new Exception("no int vars allowed in lib");
				} else if (line.startsWith("$")) {
					throw new Exception("no string vars allowed in lib");
				} else if (line.startsWith("&")) {
					throw new Exception("no vars allowed in lib");
				} else if (line.startsWith("*")) {
					throw new Exception("no vars allowed in lib");

				} else if (line.startsWith(":")) {
					if (line.startsWith(funcEntry)) {
						interfaces.put(line.substring(funcEntry.length()),
								curCode);
					}
				}
				curCode++;
			}
		}
		return interfaces;
	}

	public static byte[] genByteCode(String asm) {

		byte[] resultBytes = null;

		List<Map<String, String>> strConsts = new ArrayList<Map<String, String>>();
		Map<String, Integer> strVars = new HashMap<String, Integer>();
		Map<String, Integer> objVars = new HashMap<String, Integer>();
		List<String> objLists = new ArrayList<String>();
		int strVarsCount = 0;

		Map<String, Integer> intVars = new HashMap<String, Integer>();
		int intVarsCount = 0;

		int objVarsCount = 0;

		List<Map<String, String>> arrayVars = new ArrayList<Map<String, String>>();

		Map<String, Integer> labels = new HashMap<String, Integer>();
		Map<String, Integer> functions = new HashMap<String, Integer>();

		List<List<Integer>> codes = new ArrayList<List<Integer>>();

		String[] lines = asm.replaceAll("\\\r", "").split("\n");
		int count = lines.length;
		for (int i = 0; i < count; i++) {
			String line = lines[i];
			if (line.length() == 0 || line.startsWith(";")
					|| line.trim().length() == 0) {
				continue;
			} else {

				if (line.startsWith("@")) {
					String[] things = line.split("=", 2);
					if (things.length == 2) {
						Map<String, String> item = new HashMap<String, String>();
						item.put("id", things[0]);
						String value = things[1].replaceAll("\\\\r", "");
						value = value.replaceAll("\\\\n", "\n");
						value = value.replaceAll("\\\\\"", "\"");
						item.put("value", value);
						strConsts.add(item);
					} else if (things.length == 1) {
						Map<String, String> item = new HashMap<String, String>();
						item.put("id", things[0]);
						item.put("value", "");
						strConsts.add(item);
					}
				} else if (line.startsWith("%")) {
					intVars.put(line, intVarsCount++);
				} else if (line.startsWith("$")) {
					strVars.put(line, strVarsCount++);
				} else if (line.startsWith("&")) {
					objVars.put(line, objVarsCount++);
					objLists.add(line);
				} else if (line.startsWith("*")) {
					String[] things = line.split(" ", 2);
					if (things.length == 2) {
						Map<String, String> item = new HashMap<String, String>();
						item.put("id", things[0]);
						item.put("value", things[1]);
						arrayVars.add(item);
					}

				} else if (line.startsWith(":")) {
					int curCode = codes.size();
					labels.put(line.substring(1), curCode);
					if (line.startsWith(funcEntry)) {
						functions.put(line.substring(funcEntry.length()),
								curCode);
					}
					List<Integer> code = new ArrayList<Integer>();
					code.add(ByteCode.valueOf("EMPTY").ordinal());
					codes.add(code);
				} else {
					codes.add(new ArrayList<Integer>());
				}
			}
		}
		codes.clear();
		for (int i = 0; i < count; i++) {
			String line = lines[i].trim();
			if (line.length() == 0 || line.startsWith(";")) {
				continue;
			} else {
				if (line.startsWith("@")) {

				} else if (line.startsWith("%")) {

				} else if (line.startsWith("$")) {

				} else if (line.startsWith("*")) {

				} else if (line.startsWith("&")) {

				} else if (line.startsWith(":")) {
					List<Integer> code = new ArrayList<Integer>();
					code.add(ByteCode.valueOf("EMPTY").ordinal());
					codes.add(code);
				} else {
					boolean invalid = true;
					String[] parts = line.split(" ");
					if (parts != null && parts.length > 0) {
						String inst = parts[0];

						List<Integer> code = new ArrayList<Integer>();
						code.add(ByteCode.valueOf(inst).ordinal());
						if (parts.length == 1) {
							invalid = false;
						}
						for (int k = 1; k < parts.length; k++) {
							String thing = parts[k];
							if (thing.startsWith("@")) {
								int length = strConsts.size();
								for (int j = 0; j < length; j++) {
									Map<String, String> strConst = strConsts
											.get(j);
									String id = strConst.get("id");
									if (id.equals(thing)) {
										code.add(j);
										invalid = false;
										break;
									}
								}

							} else if (thing.startsWith("%")) {
								if (intVars.containsKey(thing)) {
									code.add(intVars.get(thing));
									invalid = false;
								}
							} else if (thing.startsWith("$")) {
								if (strVars.containsKey(thing)) {
									code.add(strVars.get(thing));
									invalid = false;
								}
							} else if (thing.startsWith("&")) {
								if (objVars.containsKey(thing)) {
									code.add(objVars.get(thing));
									invalid = false;
								}
							} else if (thing.startsWith("*")) {
								int length = arrayVars.size();
								for (int j = 0; j < length; j++) {
									Map<String, String> array = arrayVars
											.get(j);
									String id = array.get("id");
									if (id.equals(thing)) {
										code.add(j);
										invalid = false;
										break;
									}
								}
							} else {
								try {
									code.add(Integer.parseInt(thing));
									invalid = false;
								} catch (Exception e) {
									if (labels.containsKey(thing)) {
										code.add(labels.get(thing));
										invalid = false;
									}
								}
							}
						}
						if (!invalid) {
							codes.add(code);
						}

					}
					if (invalid) {
						throw new RuntimeException("Error at line: " + (i + 1)
								+ " " + line);
					}

				}
			}
		}

		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeShort(strConsts.size());
			for (Map<String, String> strConst : strConsts) {
				String id = strConst.get("id");
				String value = strConst.get("value");
				byte[] bytes = value.getBytes(Charset.forName("UTF-8"));
				writeVarInt(bytes.length, dos);
				dos.write(bytes);
			}
			dos.writeInt(codes.size());
			for (List<Integer> code : codes) {
				dos.writeByte(code.get(0));
				ByteArrayOutputStream tempBos = new ByteArrayOutputStream();
				DataOutputStream tempDos = new DataOutputStream(tempBos);
				for (int i = 1; i < code.size(); i++) {
					long var = code.get(i);
					writeVarInt(var, tempDos);
				}
				byte[] temp = tempBos.toByteArray();
				dos.writeByte(temp.length);
				dos.write(temp);
			}
			dos.writeInt(functions.size());
			for (String key : functions.keySet()) {
				writeVarChar(key, dos);
				dos.writeShort(functions.get(key));
			}
			resultBytes = bos.toByteArray();
			dos.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultBytes;

	}

	public static void writeVarChar(String thing, DataOutputStream dos)
			throws IOException {
		byte[] bytes = thing.getBytes(Charset.forName("utf-8"));
		dos.writeByte(bytes.length);
		dos.write(bytes);
	}

	public static void writeVarInt(long var, DataOutputStream dos)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if (var < 0)
			var += 0xFFFFFFFFL;
		while (var > 0) {
			int b = (int) (0x7F & var);
			var >>= 7;
			baos.write(b);
		}
		byte[] bytes = baos.toByteArray();
		for (int k = bytes.length - 1; k > 0; k--)
			dos.write(bytes[k] | 0x80);
		if (bytes.length > 0)
			dos.write(bytes[0]);
		else
			dos.write(0);
	}
}