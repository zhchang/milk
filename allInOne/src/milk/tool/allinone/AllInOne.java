package milk.tool.allinone;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import milk.tool.i18n;
import mozat.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mozat.mengine.MCompiler;
import com.mozat.milk.ResourceLoader;

public class AllInOne {

	private static JSONObject input;

	private static File parent;

	static boolean compiled = false;

	public static void main(String[] args) {
		if (args.length > 0) {
			File file = new File(args[0]);
			if (!file.isAbsolute()) {
				String pwdPath = System.getProperty("user.dir");
				File pwd = new File(pwdPath);
				file = new File(pwd, args[0]);
			}
			System.out.println("using config: " + file.getAbsolutePath());
			parent = file.getParentFile();
			input = Util.parseJson(file);

			System.out.println(input.toString());

			try {
				process();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.exit(0);
		} else {
			System.err.println("cmd path-to-json-file");
			System.exit(1);
		}
	}

	private static void process() throws Exception {
		JSONArray actions = input.getJSONArray("actions");
		int count = actions.length();
		for (int i = 0; i < count; i++) {
			String action = actions.getString(i);
			processAction(action);
		}
	}

	private static void processAction(String action) throws Exception {
		String projectPath = input.getString("project-path");
		File projectFile = new File(projectPath);
		File projectDir = new File(projectPath).getParentFile();
		if (!projectPath.endsWith("test.proj")) {
			File custFolder = new File(projectDir, "customizations");
			if (custFolder.exists() && custFolder.isDirectory()) {
				String patchName = projectFile.getName().substring(0,
						projectFile.getName().length() - 5)
						+ ".patch";
				File patch = new File(custFolder, patchName);
				if (patch.exists() && patch.isFile()) {
					System.out.println("Appling project patch : "
							+ patch.getAbsolutePath());
					JSONObject orgJson = Util.parseJson(new File(projectDir,
							"stc_test.proj"));
					JSONObject patchJson = Util.parseJson(patch);
					JSONObject resources = orgJson.getJSONObject("resources");
					if (patchJson.has("resources")) {
						Iterator it = patchJson.keys();
						while (it.hasNext()) {
							String key = (String) it.next();
							if (key.equals("resources")) {
								JSONObject value = patchJson.getJSONObject(key);

								Iterator rit = value.keys();
								while (rit.hasNext()) {
									String rkey = (String) rit.next();
									JSONObject rvalue = value
											.getJSONObject(rkey);
									resources.put(rkey, rvalue);
								}
								orgJson.put("resources", resources);
							} else {
								orgJson.put(key, patchJson.get(key));
							}
						}
					} else {
						Iterator it = patchJson.keys();
						while (it.hasNext()) {
							String key = (String) it.next();
							JSONObject value = patchJson.getJSONObject(key);
							resources.put(key, value);
						}
						orgJson.put("resources", resources);
					}

					Util.writeFileContent(projectFile, orgJson.toString(4));
					System.out.println("patch applied");
				}
			}
		}
		if (action.equalsIgnoreCase("deploy")) {
			deploy();
		} else if (action.equalsIgnoreCase("prepack")) {
			prepack();
		} else if (action.equalsIgnoreCase("translate")) {
			translate(true);
		}
	}

	private static void translate(boolean force) throws Exception {
		String projectPath = input.getString("project-path");
		File projectDir = new File(projectPath).getParentFile();

		String poPath = input.optString("po-path");

		File poDir = null;
		if (poPath != null) {
			poDir = new File(poPath);
			if (!poDir.exists() || !poDir.isDirectory()) {
				System.err.println("invalid po path");
				poDir = null;
			}
		}

		String[] args = {
				"-a",
				"merge",
				"-p",
				projectDir.getAbsolutePath(),
				"-t",
				poDir == null ? projectDir.getAbsolutePath() : poDir
						.getAbsolutePath() };
		i18n.main(args);

	}

	private static void deploy() throws Exception {
		boolean valid = true;
		String projectPath = input.getString("project-path");
		File projectFile = new File(projectPath);
		JSONObject projectJson = Util.parseJson(projectFile);
		if (input.has("app-server-url")) {
			String appServerUrl = input.getString("app-server-url");
			projectJson.put("app-server-url", appServerUrl);
		}
		String[] args = { "-o", "bin", "-r", projectPath, "-ds",
				input.getString("deploy-url"), "-mu",
				input.getString("monet-user"), "-mp",
				input.getString("monet-password"), "-murl",
				input.getString("monet-url"), "-mport",
				input.getString("monet-port"), "-ms",
				input.getString("mgserver-id"), "-json", projectJson.toString() };
		try {
			new MCompiler().doMain(args, true, null, null);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			valid = false;
		}
		if (!valid) {
			System.exit(1);
		}
	}

	private static File getCorrectFile(String filePath) {
		File file = new File(filePath);
		if (!file.isAbsolute()) {
			file = new File(parent, filePath);
		}
		return file;
	}

	private static void prepack() throws Exception {
		JSONObject project = Util.parseJson(new File(input
				.getString("project-path")));
		JSONObject config = new JSONObject();
		config.put("domain", project.getString("domain"));
		config.put("game", project.getString("game"));
		config.put("monetUrl", input.getString("monet-url"));
		config.put("monetPort", input.getInt("monet-port"));
		config.put("monetUser", input.getString("monet-user"));
		config.put("monetPassword", input.getString("monet-password"));
		config.put("serviceType", input.getInt("mgserver-id"));
		config.put("browserServiceType", input.getInt("browser-service-id"));
		config.put("chatServiceType", input.getInt("chat-service-id"));
		config.put("gameServiceType", input.optInt("game-service-id", -1));
		config.put("feedbackServiceType",
				input.optInt("feedback-service-id", -1));
		config.put("newGameServiceType",
				input.optInt("new-game-service-id", -1));
		config.put("moAgentWapUrl", input.optString("moagent-wap-url", ""));
		config.put("channelId", input.optInt("channel-id", 0));
		config.put("billing", input.optString("billing"));

		JSONObject configs = new JSONObject();
		JSONObject packConfig = new JSONObject();
		packConfig.put("output", input.getString("prepack-output"));
		packConfig.put("width", input.getInt("width"));
		packConfig.put("height", input.getInt("height"));
		packConfig.put("platform", input.getString("platform"));
		packConfig.put("density", input.getInt("density"));
		packConfig.put("pack-level", input.getInt("pack-level"));
		configs.put("running", packConfig);
		config.put("configs", configs);

		ResourceLoader.perform(config, parent);

		try {
			String jarPath = input.getString("jar-path");

			File jarFile = getCorrectFile(jarPath);
			if (jarFile.exists()) {

				System.out.println("using jar file: "
						+ jarFile.getAbsolutePath());
				File packFolder = getCorrectFile(input
						.getString("prepack-output"));
				System.out.println("pack folder : "
						+ packFolder.getAbsolutePath());
				// File toPack = new File(packFolder, jarFile.getName());
				// Util.copyFile(jarFile, toPack);
				String[] cmds = { "jar", "-fu", jarFile.getAbsolutePath(),
						"-C", packFolder.getAbsolutePath(), "." };

				ProcessBuilder pb = new ProcessBuilder(cmds);

				Process p = pb.start();

				if (p.waitFor() != 0) {
					System.err.println("error in packing config into jar file");
				}

				if (input.has("game-name")) {
					String gameName = input.getString("game-name");
					StringBuffer manifest = new StringBuffer();
					manifest.append("MIDlet-Name: ").append(gameName)
							.append("\r\n");
					manifest.append("MIDlet-1: ").append(gameName)
							.append(",/icon.png,milk.ui.MilkAppImpl\r\n");
					File mfile = new File(jarFile.getParentFile(), "update.mf");
					Util.writeFileContent(mfile, manifest.toString());
					String[] cmds2 = { "jar", "umf", mfile.getAbsolutePath(),
							jarFile.getAbsolutePath() };

					ProcessBuilder pb2 = new ProcessBuilder(cmds2);

					Process p2 = pb2.start();

					if (p2.waitFor() != 0) {
						System.err.println("error in updating manifest");
					}
				}

				// if (!(Util.exec(cmds, packFolder, stdout, stderr) == 0)) {
				// System.err.println("error in packing config into jar file");
				// System.err.println(stderr.toString());
				// }
				// Util.copyFile(toPack, jarFile);
				// Util.deleteDir(toPack);

				String jadPath = jarPath.substring(0, jarPath.length() - 4)
						+ ".jad";
				File jadFile = new File(jadPath);
				List<String> lines = Util.getLines(jadFile);
				StringBuffer buffer = new StringBuffer();
				for (String line : lines) {
					if (line.startsWith("MIDlet-Jar-Size: ")) {
						buffer.append("MIDlet-Jar-Size: " + jarFile.length())
								.append("\r\n");
					} else {
						buffer.append(line).append("\r\n");
					}
				}
				String jadContent = buffer.toString();
				if (input.has("game-name")) {
					String gameName = input.getString("game-name");
					jadContent = jadContent
							.replaceAll("OceanAge Pro", gameName);
				}
				Util.writeFile(jadContent, jadFile);
			} else {
				System.out
						.println("skipping config jarfile. (null or non-exists)");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
