package com.comerzzia.bricodepot.pos.services.core.sesion.exception;

import com.comerzzia.pos.services.core.usuarios.UsuarioInvalidLoginException;
import com.comerzzia.pos.util.i18n.I18N;

public class UsuarioNoEncontradoException extends UsuarioInvalidLoginException {

	/**
	* 
	*/
	private static final long serialVersionUID = 6484380833087288688L;

	public UsuarioNoEncontradoException() {
	}

	public UsuarioNoEncontradoException(String message) {
		super(message);
	}

	public UsuarioNoEncontradoException(String message, Throwable cause) {
		super(message, cause);
	}

	public UsuarioNoEncontradoException(Throwable cause) {
		super(cause);
	}

	public UsuarioNoEncontradoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	@Override
	public String getMessageDefault() {
		return I18N.getTexto("El usuario no existe en BD central");
	}

}
