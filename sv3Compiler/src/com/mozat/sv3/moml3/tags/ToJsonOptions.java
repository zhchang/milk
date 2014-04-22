package com.mozat.sv3.moml3.tags;

public class ToJsonOptions {
	public boolean includeType = true;
	public boolean includeTag = false;
	public boolean pretty = true;

	public ToJsonOptions() {

	}

	public ToJsonOptions(boolean includeType, boolean includeTag, boolean pretty) {
		super();
		this.includeType = includeType;
		this.includeTag = includeTag;
		this.pretty = pretty;
	}

}
