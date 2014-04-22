package com.mozat.sv3.moml3.parser;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.mozat.sv3.moml3.tags.Page;

public class ParseResult {
	public final Page page;
	// public final Moscript script;
	// public final Div root;
	public List<Moml3Exception> errors;

	public ParseResult(Page page) {
		this.page = page;
	}

	public ParseResult(Page page, Moml3Exception error) {
		this(page);
		this.errors = new ArrayList<Moml3Exception>();
		this.errors.add(error);
	}

	public ParseResult(Page page, Collection<Moml3Exception> errors) {
		this(page);
		this.errors = new ArrayList<Moml3Exception>(errors);
	}

	public void printErrors(PrintStream err) {
		for (Moml3Exception e : errors) {
			err.print(e.getPosition());
			err.print(": ");
			err.println(e.getMessage());
		}
	}

}
