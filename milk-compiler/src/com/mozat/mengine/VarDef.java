package com.mozat.mengine;

import com.mozat.mengine.MCompiler.ReturnType;

public class VarDef {

	int index;
	ReturnType type;
	String defType = null;
	String id;
	boolean global;

	public VarDef(String id, ReturnType type, int index, boolean global,
			String defType) {
		this.index = index;
		this.type = type;
		this.id = id;
		this.global = global;
		this.defType = defType;
	}
}
