package com.comerzzia.bricodepot.pos.persistence.cajas.central;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "TyllSyncManager")
@XmlAccessorType(XmlAccessType.FIELD)
public class TillSyncManager {
	
	@XmlElement(name = "uid_actividad")
	protected String uidActividad;
	@XmlElement(name = "cod_alm")
	protected String codAlm;
	@XmlElement(name = "cod_caja")
	protected String codCaja;
	@XmlElement(name = "importe")
	protected BigDecimal importe;
	
	public String getUidActividad() {
		return uidActividad;
	}
	
	public void setUidActividad(String uidActividad) {
		this.uidActividad = uidActividad;
	}
	
	public String getCodAlm() {
		return codAlm;
	}
	
	public void setCodAlm(String codAlm) {
		this.codAlm = codAlm;
	}
	
	public String getCodCaja() {
		return codCaja;
	}
	
	public void setCodCaja(String codCaja) {
		this.codCaja = codCaja;
	}

	
	public BigDecimal getImporte() {
		return importe;
	}

	
	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}
	

	
}
