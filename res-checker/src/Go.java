import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mozat.util.Util;

public class Go {

	static File projectDir;
	static List<String> actions = new ArrayList<String>();
	static List<String> unReferenced = new ArrayList<String>();
	static File classDir;
	static File cocostudioDir;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		parseArgs(args);
		process(new File(projectDir, "Resources"));
		for (String path : unReferenced) {
			System.out.println(path);
		}
	}

	static void process(File dir) {
		if (dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				process(file);
			}
		} else {
			if (dir.getName().toLowerCase().endsWith(".png")) {
				if (!testReference(dir.getName(), classDir) && !testReference(dir.getName(), cocostudioDir)) {
					unReferenced.add(dir.getAbsolutePath());
				}
			}
		}
	}

	static boolean testReference(String name, File dir) {
		if (dir.isDirectory()) {
			boolean yes = false;
			for (File file : dir.listFiles()) {
				yes |= testReference(name, file);
				if (yes) {
					break;
				}
			}
			return yes;
		} else {
			boolean yes = Util.getFileContent(dir).indexOf(name) != -1;
			return yes;
		}
	}

	static void parseArgs(String[] args) {
		int size = args.length;
		for (int i = 0; i < size; i += 2) {
			String key = args[i];
			String value = args[i + 1];
			if (key.equals("-p")) {
				projectDir = new File(value);
				if (!projectDir.isAbsolute()) {
					String pwdPath = System.getProperty("user.dir");
					File pwd = new File(pwdPath);
					projectDir = new File(pwd, value);
				}
				if (!projectDir.exists() || !projectDir.isDirectory()
						|| !new File(projectDir, "Classes").exists()) {
					throw new RuntimeException("invalid project dir specified.");
				}
			} else {
				throw new RuntimeException("invalid option: " + key);
			}
		}
		if (projectDir == null) {
			throw new RuntimeException("no project dir specified.");
		}
		classDir = new File(projectDir, "Classes");
		File res = new File(projectDir, "Resources");
		cocostudioDir = new File(res,"cocostudioUI");
	}

}
