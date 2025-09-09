package com.comerzzia.bricodepot.pos.services.ticket.lineas;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class BricodepotDatosDevolucionBean {

	@XmlElement(name = "ticket_devolucion")
	private String ticketDevolucion;

	@XmlElement(name = "tienda_devolucion")
	private String tiendaDevolucion;

	public String getTicketDevolucion() {
		return ticketDevolucion;
	}

	public void setTicketDevolucion(String ticketDevolucion) {
		this.ticketDevolucion = ticketDevolucion;
	}

	public String getTiendaDevolucion() {
		return tiendaDevolucion;
	}

	public void setTiendaDevolucion(String tiendaDevolucion) {
		this.tiendaDevolucion = tiendaDevolucion;
	}
}
