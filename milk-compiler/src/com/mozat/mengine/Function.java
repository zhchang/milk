package com.mozat.mengine;

import java.util.ArrayList;
import java.util.List;

import com.mozat.mengine.MCompiler.ReturnType;

public class Function {

	public static class FunctionParam {
		ReturnType type;
		String name;
		String defType;

		public FunctionParam() {

		}

		public FunctionParam(ReturnType type) {
			this.type = type;
		}

		public FunctionParam(ReturnType type, String name, String defType) {
			this.type = type;
			this.name = name;
			this.defType = defType;
		}

		public FunctionParam(ReturnType type, String name) {
			this.type = type;
			this.name = name;
		}
	}

	String name;
	String lib;
	int isp = -1;
	List<FunctionParam> params = new ArrayList<FunctionParam>();
	int objIndex = 1;

	FunctionParam returnValue = new FunctionParam();

	Function(String name) {
		this.name = name;
	}

	Function(String name, String lib, int isp) {
		this.name = name;
		this.isp = isp;
		this.lib = lib;
	}
}
