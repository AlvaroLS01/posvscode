package com.comerzzia.bricodepot.pos.services.anticipo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AnticipoDTO {

	private String uidActividad;
	
	private String uidAnticipo;

	private String numAnticipo;

	private String importeAnticipo;

	private Long idClieAlbaran;

	private String operacionAnticipo;

	
	public String getUidAnticipo() {
		return uidAnticipo;
	}

	public void setUidAnticipo(String uidAnticipo) {
		this.uidAnticipo = uidAnticipo;
	}

	public String getUidActividad() {
		return uidActividad;
	}

	public void setUidActividad(String uidActividad) {
		this.uidActividad = uidActividad;
	}

	public String getNumAnticipo() {
		return numAnticipo;
	}

	public void setNumAnticipo(String numAnticipo) {
		this.numAnticipo = numAnticipo;
	}

	public String getImporteAnticipo() {
		return importeAnticipo;
	}

	public void setImporteAnticipo(String importeAnticipo) {
		this.importeAnticipo = importeAnticipo;
	}

	public Long getIdClieAlbaran() {
		return idClieAlbaran;
	}

	public void setIdClieAlbaran(Long idClieAlbaran) {
		this.idClieAlbaran = idClieAlbaran;
	}

	public String getOperacionAnticipo() {
		return operacionAnticipo;
	}

	public void setOperacionAnticipo(String operacionAnticipo) {
		this.operacionAnticipo = operacionAnticipo;
	}
	
	

}
