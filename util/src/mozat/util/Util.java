package mozat.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tmatesoft.hg.core.HgRepoFacade;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.ISVNInfoHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class Util {

	private static List<File> rollbacks = new ArrayList<File>();

	private static final String DATE_FORMAT_NOW = "yyyyMMdd";

	public static String getFormattedDataString() {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(Calendar.getInstance().getTime());
	}

	public static String getFormattedDateTimeString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-hhmmss");
		return sdf.format(Calendar.getInstance().getTime());
	}

	private static void writeFile(File oldPath, File newPath) throws Exception {
		newPath.getParentFile().mkdirs();
		newPath.createNewFile();
		FileChannel inChannel = new FileInputStream(oldPath).getChannel();
		FileChannel outChannel = new FileOutputStream(newPath).getChannel();
		try {
			// magic number for Windows, 64Mb - 32Kb)
			int maxCount = (64 * 1024 * 1024) - (32 * 1024);
			long size = inChannel.size();
			long position = 0;
			while (position < size) {
				position += inChannel
						.transferTo(position, maxCount, outChannel);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}

	}

	public static void deleteDir(File dir) throws Exception {
		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			for (File child : children) {
				deleteDir(child);
			}
		}
		if (!dir.delete()) {
			throw new Exception("cannot remove file: " + dir.getAbsolutePath());
		} else {
			int count = 0;
			while (dir.exists() && count < 10) {
				Thread.sleep(300);
				count++;
			}
			if (dir.exists()) {
				throw new Exception("cannot remove file: "
						+ dir.getAbsolutePath());
			}
		}
		dir = null;
	}

	public static List<String> getJsonKeys(JSONObject input) throws Exception {
		List<String> list = new ArrayList<String>();
		@SuppressWarnings("rawtypes")
		Iterator it = input.keys();
		while (it.hasNext()) {
			list.add((String) it.next());
		}
		return list;
	}

	public static List<JSONObject> getJsonObjects(JSONArray array)
			throws Exception {
		List<JSONObject> list = new ArrayList<JSONObject>();
		int count = array.length();
		for (int i = 0; i < count; i++) {
			JSONObject item = array.getJSONObject(i);
			list.add(item);
		}
		return list;
	}

	public static void copyFile(File oldPath, File newPath) throws Exception {
		if (oldPath != null && newPath != null) {
			if (oldPath.isDirectory()) {
				File[] children = oldPath.listFiles();
				for (File child : children) {
					if (child.getName().equals(".svn")) {
						continue;
					}
					String toPath = newPath.getAbsolutePath();
					if (!toPath.endsWith("\\")) {
						toPath += "\\";
					}
					toPath += child.getName();
					File childNew = new File(toPath);
					copyFile(child, childNew);
				}
			} else {
				writeFile(oldPath, newPath);
			}
		}
	}

	public static SVNClientManager getSVNClientManager() {
		DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
		options.setAuthStorageEnabled(false);
		SVNClientManager manager = SVNClientManager.newInstance(options,
				"build-adapter", "svn");
		return manager;
	}

	public static SVNClientManager getSVNClientManager(String username,
			String password) {
		DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
		options.setAuthStorageEnabled(false);
		SVNClientManager manager = SVNClientManager.newInstance(options,
				username, password);
		return manager;
	}

	private static class TempLong {
		long value = 0;
	}

	private static class ChangeLog {

		private static final String addTag = "-a:";
		private static final String removeTag = "-r:";
		private static final String fixTag = "-f:";

		ChangeLog(Date date, long revision) {
			this.date = date;
			this.revision = revision;
		}

		Date date;
		long revision;
		List<String> adds = new ArrayList<String>();
		List<String> removes = new ArrayList<String>();
		List<String> fixs = new ArrayList<String>();
		List<String> others = new ArrayList<String>();

		boolean isValid() {
			return adds.size() > 0 || fixs.size() > 0 || removes.size() > 0
					|| others.size() > 0;
		}
	}

	public static String doChangeLogHg(File base) throws Exception {
		StringBuffer buffer = new StringBuffer();
		HgRepoFacade hgRepo = new HgRepoFacade();
		if (!hgRepo.initFrom(base)) {
			throw new Exception("cannot init hg repo: " + base.toString());
		}
		String[] cmds = { "hg", "id", "-n", "-b" };
		StringBuilder output = new StringBuilder();
		StringBuilder error = new StringBuilder();
		if (Util.exec(cmds, base, output, error) == 0) {
			String[] items = output.toString().replaceAll("\r", "")
					.replaceAll("\n", "").split(" ");
			if (items != null && items.length == 2
					&& items[1].startsWith("master")) {

				final TreeMap<Date, TreeMap<Long, ChangeLog>> changeLogs = new TreeMap<Date, TreeMap<Long, ChangeLog>>();
				final DateFormat format = new SimpleDateFormat(
						"EEE MMM dd HH:mm:ss yyyy Z");

				String[] logCmds = { "hg", "-v", "log", "-b", items[1] };
				StringBuilder logOutput = new StringBuilder();
				StringBuilder logError = new StringBuilder();
				int ok = Util.exec(logCmds, base, logOutput, logError);
				if (ok == 0) {
					String logAll = logOutput.toString();
					logAll = logAll.replaceAll("\\\r", "");
					String[] things = logAll.split("\n\n");

					for (String thing : things) {
						if (thing.length() == 1) {
							continue;
						}
						thing = thing.replaceAll("cl\\s*\\{", "cl{");
						thing = thing.replaceAll("cL\\s*\\{", "cl{");
						thing = thing.replaceAll("CL\\s*\\{", "cl{");
						thing = thing.replaceAll("Cl\\s*\\{", "cl{");

						int index1 = thing.indexOf("date:");
						int index2 = index1 != -1 ? thing.indexOf(
								"description:", index1) : -1;

						String dateStr = "";
						Date date = new Date();
						if (index2 != -1) {
							dateStr = thing.substring(
									index1 + "date:".length(), index2);
							int index3 = dateStr.indexOf("files:");
							if (index3 != -1) {
								dateStr = dateStr.substring(0, index3);
							}
							dateStr = dateStr.replaceAll("\n", "");
							dateStr = dateStr.trim();
							try {
								date = format.parse(dateStr);
							} catch (Exception e) {
							}
						} else {
							throw new Exception("bad log string");
						}

						int rev = 0;

						index1 = thing.indexOf("changeset:");
						index2 = index1 != -1 ? thing.indexOf("branch:") : -1;
						if (index1 != -1 && index2 != -1) {
							String temp = thing.substring(
									index1 + "changeset:".length(), index2)
									.trim();
							String[] temps = temp.split(":");
							if (temps.length == 2) {
								try {
									rev = Integer.parseInt(temps[0]);
								} catch (Throwable t) {
								}
							}
						}

						index1 = thing.indexOf("cl{");
						index2 = index1 != -1 ? thing.indexOf("}", index1) : -1;
						if (index1 == -1 || index2 == -1) {
							continue;
						}
						String comment = thing.substring(index1, index2 + 1);

						{
							int pos1 = comment.indexOf("cl{");

							if (pos1 != -1) {
								int pos2 = comment.indexOf("}", pos1);
								if (pos2 != -1) {
									String input = comment.substring(pos1 + 3,
											pos2).replaceAll("\r", "");
									String[] logs = input.split("\n");

									ChangeLog changeLog = new ChangeLog(date,
											rev);
									for (String log : logs) {
										String singleLine = log.trim();
										if (singleLine.length() > ChangeLog.addTag
												.length()
												&& singleLine
														.substring(
																0,
																ChangeLog.addTag
																		.length())
														.equalsIgnoreCase(
																ChangeLog.addTag)) {
											changeLog.adds.add(singleLine
													.substring(
															ChangeLog.addTag
																	.length())
													.trim());
										} else if (singleLine.length() > ChangeLog.fixTag
												.length()
												&& singleLine
														.substring(
																0,
																ChangeLog.fixTag
																		.length())
														.equalsIgnoreCase(
																ChangeLog.fixTag)) {
											changeLog.fixs.add(singleLine
													.substring(
															ChangeLog.fixTag
																	.length())
													.trim());
										} else if (singleLine.length() > ChangeLog.removeTag
												.length()
												&& singleLine
														.substring(
																0,
																ChangeLog.removeTag
																		.length())
														.equalsIgnoreCase(
																ChangeLog.removeTag)) {
											changeLog.removes.add(singleLine
													.substring(
															ChangeLog.removeTag
																	.length())
													.trim());
										} else if (singleLine.length() > 0) {
											changeLog.others.add(singleLine);
										}
									}
									if (changeLog.isValid()) {
										Calendar calendar = Calendar
												.getInstance();
										calendar.setTime(changeLog.date);
										calendar.set(Calendar.HOUR, 0);
										calendar.set(Calendar.MINUTE, 0);
										calendar.set(Calendar.SECOND, 0);
										calendar.set(Calendar.MILLISECOND, 0);
										TreeMap<Long, ChangeLog> perday = changeLogs
												.get(calendar.getTime());
										if (perday == null) {
											perday = new TreeMap<Long, ChangeLog>();
											changeLogs.put(calendar.getTime(),
													perday);
										}
										perday.put(changeLog.revision,
												changeLog);
									}

								}
							}
						}

					}

					if (changeLogs.size() > 0) {
						buffer.append("CHANGE LOG AUTO GEN RESULT:\r\n\r\n");
						for (Date date : changeLogs.descendingKeySet()) {
							SimpleDateFormat sdf = new SimpleDateFormat(
									"yyyy-MM-dd");
							buffer.append("\tDate: " + sdf.format(date)
									+ "\r\n");
							for (Long revision : changeLogs.get(date)
									.descendingKeySet()) {
								ChangeLog changeLog = changeLogs.get(date).get(
										revision);
								buffer.append("\t\tRevision: "
										+ changeLog.revision + "\r\n");
								if (changeLog.adds.size() > 0) {
									buffer.append("\t\t\t[Adds]:\r\n");
									int i = 0;
									for (String add : changeLog.adds) {
										buffer.append("\t\t\t\t" + (++i)
												+ ":\t" + add + "\r\n");
									}
								}
								if (changeLog.removes.size() > 0) {
									buffer.append("\t\t\t[Removes]:\r\n");
									int i = 0;
									for (String remove : changeLog.removes) {
										buffer.append("\t\t\t\t" + (++i)
												+ ":\t" + remove + "\r\n");
									}
								}
								if (changeLog.fixs.size() > 0) {
									buffer.append("\t\t\t[Fixes]:\r\n");
									int i = 0;
									for (String fix : changeLog.fixs) {
										buffer.append("\t\t\t\t" + (++i)
												+ ":\t" + fix + "\r\n");
									}
								}
								if (changeLog.others.size() > 0) {
									buffer.append("\t\t\t[Others]:\r\n");
									int i = 0;
									for (String other : changeLog.others) {
										buffer.append("\t\t\t\t" + (++i)
												+ ":\t" + other + "\r\n");
									}
								}
								buffer.append("\r\n");
							}
							buffer.append("\r\n");
						}

					}
				}

			}

		} else {
		}
		return buffer.toString();

	}

	public static long getRevisionForPath(File path, String username,
			String password) throws Exception {
		if (path.isDirectory()) {
			if (new File(path, ".svn").exists()) {
				DAVRepositoryFactory.setup();
				SVNRepositoryFactoryImpl.setup();
				long revision = 0;
				final TempLong thing = new TempLong();
				try {
					getSVNClientManager(username, password).getWCClient()
							.doInfo(path, SVNRevision.WORKING,
									SVNRevision.WORKING, SVNDepth.INFINITY,
									null, new ISVNInfoHandler() {

										@Override
										public void handleInfo(SVNInfo arg0)
												throws SVNException {
											thing.value = Math.max(thing.value,
													arg0.getRevision()
															.getNumber());
										}
									});
					revision = thing.value;
					// SVNWCClient client = getSVNClientManager(username,
					// password)
					// .getWCClient();
					// revision = getMaxRevisionFromPath(path, client,
					// revision);
				} catch (Exception e) {
				}
				return revision;
			} else if (new File(path, ".hg").exists()) {
				String[] cmds = { "hg", "id", "-n", "-b" };
				StringBuilder output = new StringBuilder();
				StringBuilder error = new StringBuilder();
				if (Util.exec(cmds, path, output, error) == 0) {
					String[] items = output.toString().replaceAll("\r", "")
							.replaceAll("\n", "").split(" ");
					if (items != null && items.length == 2) {
						int result = 0;

						if (items[1].toLowerCase().startsWith("master")) {
							String temp = items[0].replaceAll("\\+", "");
							result = Integer.parseInt(temp);
						}

						return result;
					}

				} else {
					throw new Exception("hg log failed");
				}

			}
		}
		return -1;
	}

	// private static long getMaxRevisionFromPath(File path, SVNWCClient client,
	// long revision) {
	// if (path.isDirectory()) {
	// for (File file : path.listFiles()) {
	// revision = Math.max(revision,
	// getMaxRevisionFromPath(file, client, revision));
	// }
	// return revision;
	// } else {
	// try {
	// return Math.max(revision,
	// client.doInfo(path, SVNRevision.WORKING).getRevision()
	// .getNumber());
	// } catch (SVNException e) {
	// return revision;
	// }
	// }
	// }

	public static void checkoutUrlToPath(String url, File path)
			throws Exception {
		DAVRepositoryFactory.setup();
		SVNClientManager manager = getSVNClientManager();
		manager.getUpdateClient().doCheckout(SVNURL.parseURIDecoded(url), path,
				SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);
	}

	public static JSONObject parseJson(File file) {
		JSONObject json = null;
		if (file != null && file.exists()) {
			try {
				String value = Util.getFileContent(file);
				if (value != null && value.length() > 0) {
					json = new JSONObject(value);
				}
			} catch (JSONException e) {
				System.err.println("json parser: " + e.toString());
				e.printStackTrace();
			}
		}
		return json;
	}

	public static boolean exec(String cmd, File dir) {
		String[] cmds = { cmd };
		return exec(cmds, dir);
	}

	public static String execGetStdout(String[] cmds, File dir)
			throws Exception {
		if (dir != null && !dir.exists()) {
			throw new Exception(dir.getAbsolutePath() + " not found.");
		}

		Process p = Runtime.getRuntime().exec(cmds, null, dir);

		final StringBuilder output = new StringBuilder();
		final StringBuilder error = new StringBuilder();

		BufferWriter out = new BufferWriter(output);
		BufferWriter err = new BufferWriter(error);

		new StdioReader(p.getInputStream(), out);
		new StdioReader(p.getErrorStream(), err);

		int rc = p.waitFor();
		if (rc != 0) {
			throw new Exception(cmds[0] + " : " + rc);
		}

		out.waitFor();
		err.waitFor();

		String thing = output.toString();

		if (thing == null || thing.length() == 0) {
			int b = output.length();
			int haha = 0;
			haha = b;
			b = haha;
		} else {
			int b = output.length();
			int haha = 0;
			haha = b;
			b = haha;
		}
		return thing;

	}

	public static int exec(String[] cmds, File dir, final StringBuilder output,
			final StringBuilder error) throws Exception {
		return exec(cmds, dir, output, error, null);
	}

	public static int exec(String[] cmds, File dir, final StringBuilder output,
			final StringBuilder error, Map<String, String> evns)
			throws Exception {
		if (dir != null && !dir.exists()) {
			throw new Exception(dir.getAbsolutePath() + " not found.");
		}
		String[] params = null;
		if (evns != null) {
			List<String> things = new ArrayList<String>();
			for (String key : evns.keySet()) {
				things.add(key + "=" + evns.get(key));
			}
			params = things.toArray(new String[] {});
		}
		Process p = Runtime.getRuntime().exec(cmds, params, dir);
		BufferWriter out = new BufferWriter(output);
		BufferWriter err = new BufferWriter(error);
		new StdioReader(p.getInputStream(), out);
		new StdioReader(p.getErrorStream(), err);

		int res = p.waitFor();
		out.waitFor();
		err.waitFor();
		return res;
	}

	public static boolean exec(String[] cmds, File dir) {
		return exec(cmds, dir, null);

	}

	public static boolean exec(String[] cmds, File dir, Map<String, String> evns) {
		if (dir != null && !dir.exists()) {
			Util.die(dir.getAbsolutePath() + " not found.");
		}
		try {
			String[] params = null;
			if (evns != null) {
				List<String> things = new ArrayList<String>();
				for (String key : evns.keySet()) {
					things.add(key + "=" + evns.get(key));
				}
				params = things.toArray(new String[] {});
			}
			Process p = Runtime.getRuntime().exec(cmds, params, dir);
			final StringBuilder output = new StringBuilder();
			final StringBuilder error = new StringBuilder();
			BufferWriter out = new BufferWriter(output);
			BufferWriter err = new BufferWriter(error);

			new StdioReader(p.getInputStream(), out);
			new StdioReader(p.getErrorStream(), err);

			int rc = p.waitFor();
			if (rc != 0) {
				System.err.println(cmds[0] + " : " + rc);
			}
			out.waitFor();
			err.waitFor();
			return rc == 0;
		} catch (IOException e) {
			System.err.println(cmds[0] + " : " + e);
			return false;
		} catch (InterruptedException e) {
			System.err.println(cmds[0] + " : " + e);
			return false;
		}
	}

	public static void rollBack() {
		List<File> list = new ArrayList<File>();
		list.addAll(rollbacks);
		for (File file : list) {
			Util.performRollback(file);
		}
	}

	public static void deleteRollBack() {
		for (File file : rollbacks) {
			Util.deleteRollback(file);
		}
	}

	public static void die(String error) {
		System.err.println("Error Occured: [ " + error + " ]");
		rollBack();
		System.exit(1);
	}

	public static byte[] getFileBytes(File file) {
		try {
			FileInputStream is = new FileInputStream(file);
			byte[] bytes = new byte[(int) file.length()];
			is.read(bytes);
			is.close();
			return bytes;
		} catch (Throwable t) {
			System.err.println("error in reading file: "
					+ file.getAbsolutePath());
			return null;
		}
	}

	public static String getMd5(String input) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(input.getBytes(), 0, input.length());
			return new BigInteger(1, m.digest()).toString(16);
		} catch (Exception e) {
			return input;
		}

	}

	public static String getFileContent(File file) {
		byte[] bytes = getFileBytes(file);
		return new String(bytes, Charset.forName("UTF8"));
	}

	public static List<String[]> getKeyValuePairs(File file) {
		List<String[]> result = new ArrayList<String[]>();
		try {
			InputStreamReader reader = new InputStreamReader(
					new FileInputStream(file), Charset.forName("UTF8"));
			LineNumberReader lreader = new LineNumberReader(reader);
			String line = lreader.readLine();
			while (line != null) {
				if (!line.startsWith("#")) {
					String[] items = line.split("=", 2);
					if (items != null && items.length == 2) {
						result.add(items);
					}
				} else {
					String[] item = { line };
					result.add(item);
				}
				line = lreader.readLine();
			}
			lreader.close();
			reader.close();
		} catch (Throwable t) {
			System.err.println("error in merging file: "
					+ file.getAbsolutePath());
		}
		return result;
	}

	public static List<String> getLines(File file) {
		List<String> lines = new ArrayList<String>();
		try {
			InputStreamReader reader = new InputStreamReader(
					new FileInputStream(file), Charset.forName("UTF8"));
			LineNumberReader lreader = new LineNumberReader(reader);
			String line = lreader.readLine();
			while (line != null) {
				lines.add(line);
				line = lreader.readLine();
			}
			lreader.close();
			reader.close();
		} catch (Throwable t) {
			System.err.println("error in merging file: "
					+ file.getAbsolutePath());
		}
		return lines;
	}

	public static String getLastLine(File file) {
		String result = null;
		if (file.exists()) {
			try {
				InputStreamReader reader = new InputStreamReader(
						new FileInputStream(file), Charset.forName("UTF8"));
				LineNumberReader lreader = new LineNumberReader(reader);
				String line = lreader.readLine();
				while (line != null) {
					result = line;
					line = lreader.readLine();
				}
				lreader.close();
				reader.close();
			} catch (Throwable t) {
				System.err.println("error in Reading file: "
						+ file.getAbsolutePath());
			}
		}
		return result;
	}

	public static boolean createRollback(File file) {
		if (file != null && file.exists()) {
			File rollback = new File(file.getAbsolutePath() + ".rollback");
			if (rollback.exists()) {
				rollback.delete();
			}
			if (file.renameTo(rollback)) {
				System.out.println("rollback file created: "
						+ rollback.getAbsolutePath());
				if (!rollbacks.contains(file)) {
					rollbacks.add(file);
				}
				return true;
			} else {
				System.err.println("failed to create rollback file: "
						+ rollback.getAbsolutePath());
				return false;
			}
		}
		return false;
	}

	public static void performRollback(File file) {
		rollbacks.remove(file);
		if (file != null && file.exists()) {
			File rollback = new File(file.getAbsolutePath() + ".rollback");
			if (file.delete()) {
				if (rollback.renameTo(file)) {
					System.out.println("rollbacked file: " + file);
				} else {
					System.err.println("failed to rollback file: " + file);
				}
			} else {
				System.err.println("failed to rollback file: " + file);
			}
		}
	}

	public static File getSubFile(File parent, String fileName) {
		if (parent.exists()) {
			String path = parent.getAbsolutePath();
			if (!path.endsWith("//")) {
				path += "//";
			}
			path += fileName;
			File subFile = new File(path);
			if (subFile.exists()) {
				return subFile;
			}
		}
		return null;
	}

	public static void ReplaceFile(File file, String pattern, String replace) {
		try {
			FileInputStream fis = new FileInputStream(file);
			byte[] content = new byte[(int) file.length()];
			fis.read(content);
			fis.close();
			String str = new String(content);
			str = str.replaceAll(pattern, replace);
			Util.createRollback(file);
			Util.writeFile(str, file);
		} catch (Throwable t) {
			System.err.println("error in replacing file: "
					+ file.getAbsolutePath());
		}

	}

	public static void deleteRollback(File file) {
		if (file != null && file.exists()) {
			File rollback = new File(file.getAbsolutePath() + ".rollback");
			if (rollback.delete()) {
				System.out.println("deleted rollbacked file: " + file);

			} else {
				System.err.println("failed to delete rollback file: " + file);
			}
		}
	}

	public static boolean writeFile(List<String[]> lines, File output) {
		StringBuffer buffer = new StringBuffer();
		for (String[] line : lines) {
			if (line.length == 1) {
				buffer.append(line[0]);
				buffer.append("\r\n");
			} else if (line.length == 2) {
				buffer.append(line[0]);
				buffer.append("=");
				buffer.append(line[1]);
				buffer.append("\r\n");
			}
		}
		return writeFile(buffer.toString(), output);
	}

	public static boolean writeFile(String content, File output) {
		try {
			OutputStreamWriter writer = new OutputStreamWriter(
					new FileOutputStream(output), Charset.forName("UTF8"));
			writer.write(content);
			writer.flush();
			writer.close();
			return true;
		} catch (Throwable t) {
			System.err.println("error [" + t.toString() + "] writing output : "
					+ output.getAbsolutePath());
			return false;
		}
	}

	public static String appendSlash(String path) {
		if (!path.endsWith("//")) {
			path += "//";
		}
		return path;
	}

	public static void writeFileContent(File file, String content) {
		FileOutputStream fos = null;
		if (file != null && content != null) {
			try {
				fos = new FileOutputStream(file);
				fos.write(content.getBytes(Charset.forName("UTF8")));
				fos.close();
				fos = null;
			} catch (IOException e) {
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {

					}
				}
			}
		}
	}

	public static void writeFileBytes(File file, byte[] bytes) {
		FileOutputStream fos = null;
		if (file != null && bytes != null) {
			try {
				fos = new FileOutputStream(file);
				fos.write(bytes);
				fos.close();
				fos = null;
			} catch (IOException e) {
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {

					}
				}
			}
		}
	}

}
