package com.mozat.mengine;

import java.util.ArrayList;
import java.util.List;

import com.mozat.mengine.MCompiler.ReturnType;

public class VarContext {
	static int defIndex = 0;

	List<VarDef> varDefs = new ArrayList<VarDef>();

	boolean isUnderFor = false;

	VarDef getVar(String id) {
		for (VarDef thing : varDefs) {
			if (thing.id.equals(id)) {
				return thing;
			}
		}
		return null;
	}

	void addVarDef(String id, ReturnType type, int index, boolean global,
			String defType) {
		if (getVar(id) != null) {
			throw new RuntimeException("duplicated variable name: " + id);
		}

		varDefs.add(new VarDef(id, type, index, global, defType));
	}

}
