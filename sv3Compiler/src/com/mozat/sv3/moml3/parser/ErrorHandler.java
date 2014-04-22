package com.mozat.sv3.moml3.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import com.mozat.sv3.moml3.tokens.CharPosition;

public class ErrorHandler {
	private List<Moml3Exception> errors = new LinkedList<Moml3Exception>();

	private final ErrorLevel minimumThrowLevel;

	private final String src;

	public ErrorHandler(String src) {
		this(src, ErrorLevel.IgnorableError);
	}

	TreeMap<Integer, Integer> map;

	private synchronized TreeMap<Integer, Integer> getLineMap() {
		if (map == null) {
			TreeMap<Integer, Integer> m = createLineMap(src);
			map = m;
		}
		return map;
	}

	public static TreeMap<Integer, Integer> createLineMap(String src) {
		TreeMap<Integer, Integer> m = new TreeMap<Integer, Integer>();
		boolean lineStarted = false;
		int line = 0;
		int lastC = 0;
		for (int i = 0; i < src.length(); ++i) {
			if (!lineStarted) {
				lineStarted = true;
				m.put(i, ++line);
			}
			int c = src.charAt(i);
			if (c == '\r') {
				lineStarted = false;
			} else if (c == '\n') {
				if (lastC != '\r') {
					lineStarted = false;
				}
			}
			lastC = c;
		}
		return m;
	}

	public ErrorHandler(String src, ErrorLevel level) {
		this.src = src;
		this.minimumThrowLevel = level;
	}

	public List<Moml3Exception> getErrors() {
		return errors;
	}

	public List<Moml3Exception> copyAndClearErrors() {
		List<Moml3Exception> result = errors;
		errors = new LinkedList<Moml3Exception>();
		return result;
	}

	public void raise(ErrorLevel level, String msg, CharPosition pos)
			throws Moml3Exception {
		if (level.ordinal() >= minimumThrowLevel.ordinal()) {
			Moml3Exception e = new Moml3Exception(level, msg, pos);
			e.getPosition().resolvePosition(getLineMap());
			throw e;
		} else {
			Moml3Exception e = new Moml3Exception(level, msg, pos);
			e.getPosition().resolvePosition(getLineMap());
			errors.add(e);
		}
	}

	public void raise(ErrorLevel level, String msg, int index)
			throws Moml3Exception {
		raise(level, msg, new CharPosition(index));
	}
}
