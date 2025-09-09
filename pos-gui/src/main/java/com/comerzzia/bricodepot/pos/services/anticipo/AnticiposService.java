package com.comerzzia.bricodepot.pos.services.anticipo;

import java.util.Date;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.comerzzia.pos.persistence.tickets.TicketBean;
import com.comerzzia.pos.services.core.contadores.ServicioContadores;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.ticket.TicketsService;
import com.comerzzia.pos.util.xml.MarshallUtil;

@Component
public class AnticiposService {

	private Logger log = Logger.getLogger(AnticiposService.class);

	public static Long ID_TIPO_DOC_EANTICIPO = 601L;

	public static String ID_CONTADOR_EANTICIPO = "EANTICIPO";

	@Autowired
	private Sesion sesion;

	@Autowired
	private TicketsService ticketsService;

	@Autowired
	private ServicioContadores servicioContadores;

	public void guardarAnticipo(AnticipoDTO anticipo) {
		TicketBean ticket = new TicketBean();

		String uidActividad = sesion.getAplicacion().getUidActividad();
		anticipo.setUidActividad(uidActividad);
		ticket.setUidActividad(uidActividad);

		String uid = UUID.randomUUID().toString();
		ticket.setUidTicket(uid);
		ticket.setLocatorId(uid);

		String codalm = sesion.getAplicacion().getCodAlmacen();
		ticket.setCodAlmacen(codalm);

		String codcaja = sesion.getAplicacion().getCodCaja();
		ticket.setCodcaja(codcaja);

		ticket.setIdTipoDocumento(ID_TIPO_DOC_EANTICIPO);

		ticket.setCodTicket("*");
		ticket.setFirma("*");
		ticket.setSerieTicket("*");
		
		ticket.setCodAlmacen(sesion.getSesionCaja().getCajaAbierta().getCodAlm());
		ticket.setCodcaja(sesion.getSesionCaja().getCajaAbierta().getCodCaja());
		ticket.setFecha(new Date());

		try {
			ticket.setIdTicket(servicioContadores.obtenerValorContador(ID_CONTADOR_EANTICIPO, uidActividad));

			byte[] xml = MarshallUtil.crearXML(anticipo);
			log.debug("guardarAnticipo() - XML de Anticipo: " + new String(xml));
			ticket.setTicket(xml);

			ticketsService.insertarTicket(null, ticket, false);

		}
		catch (Exception e) {
			log.error("guardarAnticipo() - Ha habido un error al guardar la Anticipo: " + e.getMessage(), e);
		}
		finally {
		}
	}

}
