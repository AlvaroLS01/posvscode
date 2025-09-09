package com.comerzzia.bricodepot.pos.services.cajas.prosegur;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.comerzzia.pos.persistence.cajas.movimientos.CajaMovimientoBean;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CajaProsegurDTO {
	
	@XmlElement
	private CajaMovimientoBean movimiento;

	public void init() {
		movimiento = new CajaMovimientoBean();
	}
	
	public CajaMovimientoBean getMovimiento() {
		return movimiento;
	}

	
	public void setMovimiento(CajaMovimientoBean movimiento) {
		this.movimiento = movimiento;
	}
	
	

}
