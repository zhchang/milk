package com.mozat.mengine;

import java.util.ArrayList;
import java.util.List;

import com.mozat.mengine.Function.FunctionParam;
import com.mozat.mengine.MCompiler.ReturnType;
import com.mozat.mengine.MCompiler.ReturnValue;

public class Context {

	Function function = null;
	boolean functionReturned = false;

	private List<VarContext> varContexts = new ArrayList<VarContext>();
	private VarContext curVarContext = new VarContext();

	List<Integer> tempPool = new ArrayList<Integer>();
	List<Integer> usingTempPool = new ArrayList<Integer>();

	int varIndex = 1;

	void pushVarContext(boolean isUnderFor) {
		varContexts.add(curVarContext);
		curVarContext = new VarContext();
		curVarContext.isUnderFor = isUnderFor;
	}

	void popVarContext(boolean really) {
		for (VarDef def : curVarContext.varDefs) {
			// addAsmLine(ByteCode.DELETEVAR, new int[] { def.index });
		}
		if (really) {
			curVarContext = varContexts.remove(varContexts.size() - 1);
		}

	}

	void popVarContextUntilFor() {
		for (VarDef def : curVarContext.varDefs) {
			// addAsmLine(ByteCode.DELETEVAR, new int[] { def.index });
		}
		if (curVarContext.isUnderFor) {
			return;
		}
		int count = varContexts.size();
		for (int i = count - 1; i >= 0; i--) {
			for (VarDef def : varContexts.get(i).varDefs) {
				// addAsmLine(ByteCode.DELETEVAR, new int[] { def.index });
			}
			if (varContexts.get(i).isUnderFor) {
				break;
			}
		}
	}

	void popAllVarContext(boolean really) {
		for (VarDef def : curVarContext.varDefs) {
			// addAsmLine(ByteCode.DELETEVAR, new int[] { def.index });
		}
		for (VarContext context : varContexts) {
			for (VarDef def : context.varDefs) {
				// addAsmLine(ByteCode.DELETEVAR, new int[] { def.index });
			}
		}
		if (really) {
			varContexts.clear();
		}
	}

	void addAsmLine(ByteCode bc, int[] opCode) {
		asmCodes.add(ByteCoder.genInstruction(bc, opCode));
	}

	List<String> asmCodes = null;
	private Context parent = null;

	Context(List<String> asmCodes, Context parent) {
		this.asmCodes = asmCodes;
		if (parent == null) {
			this.parent = this;
		} else {
			this.parent = parent;
		}
	}

	Context(List<String> asmCodes, Context parent, Function function) {
		this.function = function;
		this.asmCodes = asmCodes;
		this.parent = parent;
	}

	void addFuncParam(String id, ReturnType type, String defType) {
		if (type != ReturnType.Void) {
			curVarContext.addVarDef(id, type, varIndex++, parent == this,
					defType);
		} else {
			throw new RuntimeException("invalid function parameter : " + id);
		}
	}

	void addVar(String id, ReturnType type, String defType) {
		if (type != ReturnType.Void) {
			curVarContext
					.addVarDef(id, type, varIndex, parent == this, defType);
			// addAsmLine(ByteCode.CREATEVAR, new int[] { varIndex++ });
			varIndex++;
		} else {
			throw new RuntimeException("invalid variable type: " + id);
		}
	}

	ReturnType getIdType(String id) {
		ReturnType type = ReturnType.Void;
		VarDef def = getVarDef(id);
		if (def != null) {
			type = def.type;
		}
		return type;
	}

	void assignVar(String id, ReturnValue value) {
		VarDef thing = getVarDef(id);
		if (thing == null) {
			throw new RuntimeException("no variable def found for id: " + id);
		}
		boolean valid = thing.type == value.type;

		if (valid) {
			addAsmLine(ByteCode.COPYVAR, new int[] { thing.index,
					(thing.global ? 0 : 1), value.index, 1 });
		} else {
			if ((thing.type == ReturnType.Map || thing.type == ReturnType.Array)
					&& value.type == ReturnType.Str) {
				ByteCode op = (thing.type == ReturnType.Map) ? ByteCode.MFROMSTR
						: ByteCode.AFROMSTR;
				if (thing.global) {
					int temp = getTempVar();
					addAsmLine(op, new int[] { temp, value.index });
					addAsmLine(ByteCode.COPYVAR, new int[] { thing.index, 0,
							temp, 1 });
				} else {
					addAsmLine(op, new int[] { thing.index, value.index });
				}
			} else {
				throw new RuntimeException(
						"invalid variable assignment:(type mismatch) " + id);
			}
		}
	}

	VarDef getVarDef(String id) {
		VarDef thing = curVarContext.getVar(id);
		if (thing == null) {
			int count = varContexts.size();
			for (int i = count - 1; i >= 0; i--) {
				thing = varContexts.get(i).getVar(id);
				if (thing != null) {
					break;
				}
			}
		}
		if (thing == null && parent != this) {
			thing = parent.getVarDef(id);
		}
		return thing;
	}

	ReturnValue loadVar(String id) {
		ReturnValue load = new ReturnValue(ReturnType.Void);
		VarDef thing = getVarDef(id);
		if (thing != null) {
			if (thing.global) {
				int temp = getTempVar();
				addAsmLine(ByteCode.COPYVAR, new int[] { temp, 1, thing.index,
						0 });
				load.type = thing.type;
				load.globalIndex = thing.index;
				load.index = temp;
				load.global = true;
			} else {
				load.type = thing.type;
				load.index = thing.index;
			}
			load.defType = thing.defType;
		}
		return load;
	}

	int getTempVar() {
		if (tempPool.size() > 0) {
			int temp = tempPool.remove(0);
			if (!usingTempPool.contains(temp)) {
				usingTempPool.add(temp);
			}
			// addAsmLine(ByteCode.CREATEVAR, new int[] { temp });
			return temp;
		} else {
			int temp = varIndex++;
			usingTempPool.add(temp);
			// addAsmLine(ByteCode.CREATEVAR, new int[] { temp });
			return temp;
		}
	}

	void releaseTempVar() {
		for (int index : usingTempPool) {
			if (!tempPool.contains(index)) {
				tempPool.add(index);
			}
		}
		usingTempPool.clear();
	}

}