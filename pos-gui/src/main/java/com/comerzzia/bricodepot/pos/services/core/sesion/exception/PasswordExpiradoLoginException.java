package com.comerzzia.bricodepot.pos.services.core.sesion.exception;

import com.comerzzia.pos.services.core.usuarios.UsuarioInvalidLoginException;
import com.comerzzia.pos.util.i18n.I18N;

public class PasswordExpiradoLoginException extends UsuarioInvalidLoginException {

	/**
	* 
	*/
	private static final long serialVersionUID = 6484380833087288688L;

	public PasswordExpiradoLoginException() {
	}

	public PasswordExpiradoLoginException(String message) {
		super(message);
	}

	public PasswordExpiradoLoginException(String message, Throwable cause) {
		super(message, cause);
	}

	public PasswordExpiradoLoginException(Throwable cause) {
		super(cause);
	}

	public PasswordExpiradoLoginException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	@Override
	public String getMessageDefault() {
		return I18N.getTexto("La contraseña del usuario indicado ha expirado");
	}

}
