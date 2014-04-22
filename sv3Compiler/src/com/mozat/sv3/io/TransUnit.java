package com.mozat.sv3.io;

public class TransUnit {
	String pkey;
	String text;
	String trans;
	String comment;

	public String getPkey() {
		return pkey;
	}

	public void setPkey(String pkey) {
		this.pkey = pkey;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getText() {
		return text;
	}

	public boolean hasText() {
		return text != null && text.length() > 0;
	}

	public boolean hasTrans() {
		return trans != null && trans.length() > 0;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTrans() {
		return trans;
	}

	public void setTrans(String trans) {
		this.trans = trans;
	}

	@Override
	public String toString() {
		return "TransUnit [pkey=" + pkey + ", text=" + text + ", trans=" + trans + ", comment=" + comment + "]";
	}

	public String getTransSafely() {
		if (trans == null || trans.length() == 0) {
			if (text == null || text.length() == 0) {
				return pkey;
			} else {
				return text;
			}
		} else {
			return trans;
		}
	}

}
