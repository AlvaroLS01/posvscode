package com.comerzzia.bricodepot.pos.services.ventas.articulos.qr;


public class PresupuestoExpiradoException extends RuntimeException {
	private static final long serialVersionUID = -3251098630744497585L;

	public PresupuestoExpiradoException(String message) {
        super(message);
    }
}
