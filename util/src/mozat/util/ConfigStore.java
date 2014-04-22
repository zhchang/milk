package mozat.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mozat.build.info.ConfigInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ConfigStore {

	private static Map<String, String> languages = new HashMap<String, String>();

	static {
		languages.put("en", "English");
		languages.put("ar", "Arabic");
		languages.put("zh-CN", "Chinese Simplified");
		languages.put("zh-TW", "Chinese Traditional");
		languages.put("en-US", "English(United States)");
		languages.put("en-UK", "English(United Kingdom");
		languages.put("fi", "Finnish");
		languages.put("fr", "French");
		languages.put("de", "German");
		languages.put("el", "Greek");
		languages.put("iw", "Hebrew");
		languages.put("id", "Indonesian");
		languages.put("in", "Indonesian(Symbian)");
		languages.put("it", "Italian");
		languages.put("ja", "Japanese");
		languages.put("ko", "Korean");
		languages.put("ru", "Russian");
		languages.put("es", "Spanish");
		languages.put("th", "Thai");
		languages.put("vi", "Vietnamese");
	}

	public static String getLangDesc(String code) {
		return languages.get(code);
	}

	private String adapterName;

	private String adapterId;

	public String getAdapterName() {
		return adapterName;
	}

	public void setAdapterName(String adapterName) {
		this.adapterName = adapterName;
	}

	public String getAdapterId() {
		return adapterId;
	}

	public void setAdapterId(String adapterId) {
		this.adapterId = adapterId;
	}

	private String baseline;

	private String sdk;

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private long timestamp;

	private List<ConfigInfo> configs = new ArrayList<ConfigInfo>();
	private File path = null;

	public String getTimeDisplay() {
		Calendar temp = Calendar.getInstance();
		temp.setTimeInMillis(timestamp);
		return temp.getTime().toString();
	}

	public List<ConfigInfo> getConfigs() {
		return configs;
	}

	public List<ConfigInfo> getNoneI18nConfigs() {
		List<ConfigInfo> list = new ArrayList<ConfigInfo>();
		for (ConfigInfo info : configs) {
			if (info.getType() != 4) {
				list.add(info);
			}
		}
		return list;
	}

	public Map<String, List<ConfigInfo>> getI18nConfigs() {
		Map<String, List<ConfigInfo>> map = new HashMap<String, List<ConfigInfo>>();
		for (ConfigInfo info : configs) {
			if (info.getType() == 4) {
				String lang = info.getRemark();
				if (lang != null && languages.containsKey(lang)) {
					List<ConfigInfo> list = map.get(lang);
					if (list == null) {
						list = new ArrayList<ConfigInfo>();
						map.put(lang, list);
					}
					list.add(info);
				}
			}
		}
		for (String key : map.keySet()) {

		}
		return map;
	}

	public String getBaseline() {
		return baseline;
	}

	public String getSdk() {
		return sdk;
	}

	public void setBaseline(String baseline) {
		this.baseline = baseline;
	}

	public void setSdk(String sdk) {
		this.sdk = sdk;
	}

	public void setConfigs(List<ConfigInfo> configs) {
		this.configs = configs;
	}

	public File getPath() {
		return path;
	}

	public void setPath(File path) {
		this.path = path;
	}

	public ConfigInfo getConfig(String id) {
		return getConfig(id, null);
	}

	public ConfigInfo getConfig(String id, String remark) {
		for (ConfigInfo info : configs) {
			if (info.getId().equals(id)) {
				if (remark == null || remark.length() == 0) {
					return info;
				} else if (remark.equals(info.getRemark())) {
					return info;
				} else {
					continue;
				}
			}
		}
		return null;
	}

	public void fromFile() throws Exception {

		if (path == null || !path.exists() || path.isDirectory()) {
			throw new Exception("file " + path + " not found.");
		} else {
			JSONObject input = new JSONObject(Util.getFileContent(path));
			adapterName = input.getString("adapterName");
			adapterId = input.getString("adapterId");
			baseline = input.getString("baseline");
			sdk = input.getString("sdk");
			timestamp = input.optLong("time");
			name = input.optString("name");
			fromConfigArray(input.getJSONArray("configs"));
		}
	}

	public void fromConfigArray(JSONArray array) {
		if (array != null) {
			getConfigs().clear();
			int count = array.length();
			for (int i = 0; i < count; i++) {
				try {
					JSONObject item = array.getJSONObject(i);
					ConfigInfo config = new ConfigInfo();
					config.setId(item.getString("id"));
					config.setRemark(item.optString("remark"));
					config.setDesc(item.optString("desc"));
					config.setFlag(item.optInt("flag"));
					config.setType(item.optInt("type"));
					config.setValue(item.optString("value"));
					if (config.getType() == 4
							&& (config.getRemark() == null || config
									.getRemark().length() == 0)) {
						config.setRemark("en");
					}
					getConfigs().add(config);
				} catch (Exception e) {
				}
			}
		}
	}

	public JSONArray toConfigArray() {
		JSONArray array = new JSONArray();
		for (ConfigInfo info : configs) {
			JSONObject temp = new JSONObject();
			try {
				temp.put("id", info.getId());
				temp.put("desc", info.getDesc());
				temp.put("remark", info.getRemark());
				temp.put("type", info.getType());
				temp.put("flag", info.getFlag());
				switch (info.getType()) {
				case 0: {
					try {
						temp.put("value", Integer.parseInt(info.getValue()));
					} catch (NumberFormatException e) {
					}

					break;
				}
				case 1:
				case 3:
				case 4: {
					temp.put("value", info.getValue());
					break;
				}
				case 2: {
					try {
						temp.put("value", new JSONArray(info.getValue()));
					} catch (JSONException e) {
					}
					break;
				}
				}
				array.put(temp);
			} catch (Exception e) {
			}
		}
		return array;
	}

	public void saveFile() {
		if (path != null) {
			try {
				timestamp = Calendar.getInstance().getTimeInMillis();
				JSONObject output = new JSONObject();
				output.put("adapterId", adapterId);
				output.put("adapterName", adapterName);
				output.put("baseline", baseline);
				output.put("sdk", sdk);
				output.put("time", timestamp);
				output.put("name", name);
				output.put("configs", toConfigArray());
				Util.writeFileContent(path, output.toString(4));
			} catch (Exception e) {

			}
		}
	}
}
