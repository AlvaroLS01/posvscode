package com.comerzzia.bricodepot.pos.services.ticket;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.jfree.util.Log;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.services.ticket.audit.TicketAuditEvent;
import com.comerzzia.pos.services.ticket.cabecera.CabeceraTicket;

@XmlAccessorType(XmlAccessType.FIELD)
@Component
@Primary
@Scope("prototype")
public class BricodepotCabeceraTicket extends CabeceraTicket {

	protected String numAnticipo;

	@XmlElement(name = "numPresupuesto")
	protected String numPresupuesto;

	protected String importeAnticipo;

	protected String operacionAnticipo;

	protected Long idClieAlbaranAnticipo;

	@XmlElementWrapper(name = "eventos_auditoria")
	@XmlElement(name = "evento")
	protected List<TicketAuditEvent> auditEvents;

	@XmlElement(name = "codigo_postal_venta")
	protected String ventaCodigoPostal;

	@XmlElement(name = "email_envio_ticket")
	private String emailEnvioTicket;

	@XmlElement(name = "fechaTicketOrigen")
	protected String fechaTicketOrigen;

	@XmlElement(name = "tipo_impresion")
	protected String tipoImpresion;

	@XmlElement(name = "conversion_fs_ft")
	private Boolean fsToFt;


	public String getVentaCodigoPostal() {
		return ventaCodigoPostal;
	}

	public void setVentaCodigoPostal(String ventaCodigoPostal) {
		this.ventaCodigoPostal = ventaCodigoPostal;
	}

	public List<TicketAuditEvent> getAuditEvents() {
		return auditEvents;
	}

	public void setAuditEvents(List<TicketAuditEvent> auditEvents) {
		this.auditEvents = auditEvents;
	}

	public void addAuditEvent(TicketAuditEvent auditEvent) {
		if (this.auditEvents == null) {
			this.auditEvents = new ArrayList<>();
		}
		this.auditEvents.add(auditEvent);
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

	public String getOperacionAnticipo() {
		return operacionAnticipo;
	}

	public void setOperacionAnticipo(String operacionAnticipo) {
		this.operacionAnticipo = operacionAnticipo;
	}

	public Long getIdClieAlbaranAnticipo() {
		return idClieAlbaranAnticipo;
	}

	public void setIdClieAlbaranAnticipo(Long idClieAlbaranAnticipo) {
		this.idClieAlbaranAnticipo = idClieAlbaranAnticipo;
	}

	@Override
	public void setCodTipoDocumento(String codTipoDocumento) {
		Log.debug("setCodTipoDocumento() - codTipoDocumento " + codTipoDocumento);
		super.setCodTipoDocumento(codTipoDocumento);
	}

	public String getEmailEnvioTicket() {
		return emailEnvioTicket;
	}

	public void setEmailEnvioTicket(String emailEnvioTicket) {
		this.emailEnvioTicket = emailEnvioTicket;
	}

	public String getFechaTicketOrigen() {
		return fechaTicketOrigen;
	}

	public void setFechaTicketOrigen(String fechaTicketOrigen) {
		this.fechaTicketOrigen = fechaTicketOrigen;
	}

	public String getTipoImpresion() {
		return tipoImpresion;
	}

	public void setTipoImpresion(String tipoImpresion) {
		this.tipoImpresion = tipoImpresion;
	}

	public String getNumPresupuesto() {
		return numPresupuesto;
	}

	public void setNumPresupuesto(String numPresupuesto) {
		this.numPresupuesto = numPresupuesto;
	}

	public Boolean getFsToFt() {
		return fsToFt;
	}

	public void setFsToFt(Boolean fsToFt) {
		this.fsToFt = fsToFt;
	}
	
	
	public void setCodCaja(String codCaja) {
		this.codCaja = codCaja;
	}

}
