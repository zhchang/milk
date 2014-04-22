package com.mozat.sv3.moml3.tokens;

import java.util.Map.Entry;
import java.util.TreeMap;

public class CharPosition implements Cloneable {
	int index;
	int row = -1;
	int col = -1;

	public CharPosition(int index) {
		this.index = index;
	}

	private CharPosition(int row, int col) {
		super();
		this.row = row;
		this.col = col;
	}

	public void resolvePosition(TreeMap<Integer, Integer> lineMap) {
		if (row < 0) {
			Entry<Integer, Integer> entry = lineMap.floorEntry(index);
			row = entry.getValue();
			// col = index - entry.getKey() + 1;
			col = index - entry.getKey();
		}
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	@Override
	public CharPosition clone() {
		return new CharPosition(row, col);
	}

	@Override
	public String toString() {
		return "[" + row + "," + col + "]";
	}

}
