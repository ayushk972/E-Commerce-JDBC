package com.ecom.exception;

public class DuplicateProductException extends RuntimeException{
	public DuplicateProductException(String msg) {
		super(msg);
	}

}
