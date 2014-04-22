package mozat.build.info;

public class SdkInfo {

	private String id = null;
	private String path = null;
	private String desc = null;

	public String getEdition() {
		if (id != null && id.contains("S60_5th")) {
			return "5";
		}
		return "3";
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return id + " | " + path;
	}

}
