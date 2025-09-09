package com.comerzzia.bricodepot.pos.services.core.sesion.exception;

public class MinHorasException extends RuntimeException {

	private static final long serialVersionUID = -3913811220016455974L;

	public MinHorasException() {
		super();
	}

	public MinHorasException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MinHorasException(String message, Throwable cause) {
		super(message, cause);

	}

	public MinHorasException(String message) {
		super(message);
	}

	public MinHorasException(Throwable cause) {
		super(cause);
	}

}
