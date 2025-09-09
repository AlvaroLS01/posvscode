package com.comerzzia.bricodepot.pos.services.core.sesion.exception;

import com.comerzzia.pos.services.core.usuarios.UsuarioInvalidLoginException;
import com.comerzzia.pos.util.i18n.I18N;

public class VariableNoConfiguradaException extends UsuarioInvalidLoginException {

	private static final long serialVersionUID = -4445383132168405397L;

	public VariableNoConfiguradaException() {
	}

	public VariableNoConfiguradaException(String message) {
		super(message);
	}

	public VariableNoConfiguradaException(String message, Throwable cause) {
		super(message, cause);
	}

	public VariableNoConfiguradaException(Throwable cause) {
		super(cause);
	}

	public VariableNoConfiguradaException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	@Override
	public String getMessageDefault() {
		return I18N.getTexto("Variable no configurada consulte administrador");
	}

}
