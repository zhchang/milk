package mozat.build.info;

public class ConfigInfo {

	private String id;

	private String remark;

	private String desc;

	private int type;

	private int flag;

	private String value;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getType() {
		return type;
	}

	public String getTypeDesc() {
		switch (type) {
		case 0: {
			return "int";
		}
		case 1: {
			return "text";
		}
		case 2: {
			return "text list";
		}
		case 3: {
			return "file content";
		}
		case 4: {
			return "i18n file content";
		}
		case 5: {
			return "boolean";
		}
		default: {
			return "unknown type";
		}
		}
	}

	public ConfigInfo createDup() {
		ConfigInfo info = new ConfigInfo();
		info.setId(id);
		info.setDesc(desc);
		info.setType(type);
		info.setFlag(flag);
		info.setValue(value);
		info.setRemark(remark);
		return info;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
