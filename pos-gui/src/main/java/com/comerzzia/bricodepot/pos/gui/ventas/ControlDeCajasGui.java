package com.comerzzia.bricodepot.pos.gui.ventas;

import com.comerzzia.bricodepot.pos.util.CajasConstants;
import com.comerzzia.pos.services.cajas.Caja;
import com.comerzzia.pos.util.format.FormatUtil;
import com.ibm.icu.text.SimpleDateFormat;

public class ControlDeCajasGui {

	protected String codCaja;
	
	protected String fecha;

	protected String usuario;

	protected String totalCaja;

	public ControlDeCajasGui(Caja caja) {
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		fecha = format.format(caja.getFechaApertura());
		
		usuario = caja.getUsuario();
		
		codCaja = caja.getCodCaja();
		
		totalCaja = codCaja.equals(CajasConstants.PARAM_CAJA_MASTER) ? FormatUtil.getInstance().formateaNumero(caja.getTotal(),2) : "";
		
		
	}

	
	public String getCodCaja() {
		return codCaja;
	}


	public void setCodCaja(String codCaja) {
		this.codCaja = codCaja;
	}


	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fechaApertura) {
		this.fecha = fechaApertura;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getTotalCaja() {
		return totalCaja;
	}

	public void setTotalCaja(String totalCaja) {
		this.totalCaja = totalCaja;
	}
	
	

}
