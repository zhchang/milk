package com.mozat.mengine;

import com.mozat.mengine.MCompiler.CodeError;

public class StatementException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public CodeError codeError;

	StatementException(CodeError error) {
		super(error.toString());
		codeError = error;
	}
}
