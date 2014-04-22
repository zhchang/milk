package mozat.build.info;

import java.util.ArrayList;
import java.util.List;

public class BaselineInfo {

	private String id = null;

	private String desc = null;

	private String url = null;

	private long revision = 0;

	private String version = null;

	private List<SdkInfo> sdks = new ArrayList<SdkInfo>();

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public long getRevision() {
		return revision;
	}

	public void setRevision(long revision) {
		this.revision = revision;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<SdkInfo> getSdks() {
		return sdks;
	}

	public void setSdks(List<SdkInfo> sdks) {
		this.sdks = sdks;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public String toString() {
		return id + " | " + desc + " | " + url + " | " + revision;
	}

}
