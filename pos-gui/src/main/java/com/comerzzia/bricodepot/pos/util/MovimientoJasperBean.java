package com.comerzzia.bricodepot.pos.util;

import java.math.BigDecimal;
import java.util.Date;

public class MovimientoJasperBean {

	private String fecha;

	private String cargo;

	private String documento;
	
	private String usuario;

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public String getCargo() {
		return cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	
}
