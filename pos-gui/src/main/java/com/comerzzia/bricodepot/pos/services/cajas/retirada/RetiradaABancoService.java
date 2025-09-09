package com.comerzzia.bricodepot.pos.services.cajas.retirada;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.persistence.cajas.cajadetalle.retirada.DetMovRetiradaExample;
import com.comerzzia.bricodepot.pos.persistence.cajas.cajadetalle.retirada.DetMovRetiradaExample.Criteria;
import com.comerzzia.bricodepot.pos.persistence.cajas.cajadetalle.retirada.DetMovRetiradaKey;
import com.comerzzia.bricodepot.pos.persistence.cajas.cajadetalle.retirada.DetMovRetiradaMapper;
import com.comerzzia.bricodepot.pos.services.cajas.BricodepotCajasService;
import com.comerzzia.bricodepot.pos.util.CajasConstants;
import com.comerzzia.core.servicios.ventas.tickets.TicketException;
import com.comerzzia.pos.persistence.core.documentos.tipos.TipoDocumentoBean;
import com.comerzzia.pos.persistence.tickets.TicketBean;
import com.comerzzia.pos.services.core.contadores.ServicioContadores;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.ticket.TicketsService;
import com.comerzzia.pos.util.xml.MarshallUtil;

@Component
public class RetiradaABancoService {

	private Logger log = Logger.getLogger(RetiradaABancoService.class);

	public static final String ID_MOV_RETIRADA = "ID_MOV_RETIRADA";
	public static final String TIPO_DOC_MOVIMIENTO_RETIRADA = "MOV_RET";

	@Autowired
	protected Sesion sesion;
	@Autowired
	protected TicketsService ticketsService;
	@Autowired
	protected ServicioContadores servicioContadores;
	@Autowired
	protected DetMovRetiradaMapper detMovRetiradaMapper;
	@Autowired
	protected BricodepotCajasService cajasService;
	
	public void generarDocumentoRetirada(RetiradaABancoDTO retiradaABancoDTO) {
		log.debug("generarDocumento() - Se procede a guardar los movimientos de retiradas a banco");
		
		TicketBean ticket = new TicketBean();
		try {
			ticket.setUidActividad(sesion.getAplicacion().getUidActividad());
			String uid = UUID.randomUUID().toString();
			ticket.setUidTicket(uid);
			ticket.setLocatorId(uid);

			String codalm = sesion.getAplicacion().getCodAlmacen();
			ticket.setCodAlmacen(codalm);

			String codcaja = sesion.getAplicacion().getCodCaja();
			ticket.setCodcaja(codcaja);

			TipoDocumentoBean tipoDocumentoMovRet = sesion.getAplicacion().getDocumentos().getDocumento(TIPO_DOC_MOVIMIENTO_RETIRADA);
			ticket.setIdTipoDocumento(tipoDocumentoMovRet.getIdTipoDocumento());

			ticket.setCodTicket("*");
			ticket.setFirma("*");
			ticket.setSerieTicket("*");

			ticket.setCodAlmacen(sesion.getAplicacion().getCodAlmacen());
			ticket.setCodcaja(CajasConstants.PARAM_CAJA_80);
			ticket.setFecha(new Date());

			ticket.setIdTicket(servicioContadores.obtenerValorContador(ID_MOV_RETIRADA, sesion.getAplicacion().getUidActividad()));
			
			byte[] xml = MarshallUtil.crearXML(retiradaABancoDTO);
			log.debug("guardarMovimientos() - XML de retirada: " + new String(xml));
			ticket.setTicket(xml);

			ticketsService.insertarTicket(null, ticket, false);
		}
		catch (Exception e) {
			log.error("guardarMovimientos() - Ha habido un error al guardar los movimientos de retirada a banco: " + e.getMessage(), e);
			throw new TicketException("guardarMovimientos() - Ha habido un error al guardar los movimientos de retirada a banco: ", e);
		}
		
		
	}
	
	public void guardarMovimientos(RetiradaABancoDTO retiradaABancoDTO, List<DetMovRetiradaKey> movimientos) {
		log.debug("guardarMovimientos() - Se procede a guardar los movimientos de retiradas a banco");

		TicketBean ticket = new TicketBean();
		try {
			String uidActividad = sesion.getAplicacion().getUidActividad();
			retiradaABancoDTO.setUidActividad(uidActividad);
			ticket.setUidActividad(uidActividad);

			String uid = UUID.randomUUID().toString();
			ticket.setUidTicket(uid);
			ticket.setLocatorId(uid);

			String codalm = sesion.getAplicacion().getCodAlmacen();
			ticket.setCodAlmacen(codalm);

			String codcaja = sesion.getAplicacion().getCodCaja();
			ticket.setCodcaja(codcaja);

			TipoDocumentoBean tipoDocumentoMovRet = sesion.getAplicacion().getDocumentos().getDocumento(TIPO_DOC_MOVIMIENTO_RETIRADA);
			ticket.setIdTipoDocumento(tipoDocumentoMovRet.getIdTipoDocumento());

			ticket.setCodTicket("*");
			ticket.setFirma("*");
			ticket.setSerieTicket("*");

			ticket.setCodAlmacen(sesion.getAplicacion().getCodAlmacen());
			ticket.setCodcaja(sesion.getAplicacion().getCodCaja());
			ticket.setFecha(new Date());

			ticket.setIdTicket(servicioContadores.obtenerValorContador(ID_MOV_RETIRADA, uidActividad));

			byte[] xml = MarshallUtil.crearXML(retiradaABancoDTO);
			log.debug("guardarMovimientos() - XML de retirada: " + new String(xml));
			ticket.setTicket(xml);

			ticketsService.insertarTicket(null, ticket, false);

			insertaMovimientos(movimientos);

		}
		catch (Exception e) {
			log.error("guardarMovimientos() - Ha habido un error al guardar los movimientos de retirada a banco: " + e.getMessage(), e);
		}
		finally {
		}
	}

	public List<DetMovRetiradaKey> consultarMovimientos(String uidActividad, String uidDiarioCaja) {

		DetMovRetiradaExample cajaExample = new DetMovRetiradaExample();
		Criteria crit = cajaExample.createCriteria();
		crit.andUidActividadEqualTo(uidActividad).andUidDiarioCajaEqualTo(uidDiarioCaja);

		List<DetMovRetiradaKey> movimientos = new ArrayList<DetMovRetiradaKey>();
		try {
			movimientos = detMovRetiradaMapper.selectByExample(cajaExample);
		}
		catch (Exception e) {
			log.error("consultarMovimientos() - Error: No se ha podido consultar los movimientos para la caja " + uidDiarioCaja, e);
		}
		return movimientos;

	}

	public void insertaMovimientos(List<DetMovRetiradaKey> movimientos) {

		for (DetMovRetiradaKey mov : movimientos) {
			detMovRetiradaMapper.insert(mov);
		}
	}

}
