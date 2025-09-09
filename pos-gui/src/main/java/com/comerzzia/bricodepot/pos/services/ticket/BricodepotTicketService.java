package com.comerzzia.bricodepot.pos.services.ticket;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.comerzzia.bricodepot.pos.services.anticipo.AnticipoDTO;
import com.comerzzia.bricodepot.pos.services.anticipo.AnticiposService;
import com.comerzzia.bricodepot.pos.services.ticket.audit.TicketAuditEvent;
import com.comerzzia.bricodepot.pos.services.ticket.audit.TicketAuditService;
import com.comerzzia.bricodepot.pos.services.ticket.lineas.BricodepotLineaTicket;
import com.comerzzia.bricodepot.pos.util.AnticiposConstants;
import com.comerzzia.core.util.config.ComerzziaApp;
import com.comerzzia.core.util.mybatis.session.SqlSession;
import com.comerzzia.pos.persistence.cajas.movimientos.CajaMovimientoBean;
import com.comerzzia.pos.persistence.core.documentos.tipos.TipoDocumentoBean;
import com.comerzzia.pos.persistence.mediosPagos.MedioPagoBean;
import com.comerzzia.pos.persistence.mybatis.SessionFactory;
import com.comerzzia.pos.persistence.mybatis.SpringTransactionSqlSession;
import com.comerzzia.pos.persistence.tickets.TicketBean;
import com.comerzzia.pos.services.cajas.CajasServiceException;
import com.comerzzia.pos.services.core.listeners.ListenersExecutor;
import com.comerzzia.pos.services.core.listeners.tipos.ticket.SalvadoTicketListener;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.services.mediospagos.MediosPagosService;
import com.comerzzia.pos.services.ticket.Ticket;
import com.comerzzia.pos.services.ticket.TicketVenta;
import com.comerzzia.pos.services.ticket.TicketVentaAbono;
import com.comerzzia.pos.services.ticket.TicketsService;
import com.comerzzia.pos.services.ticket.TicketsServiceException;
import com.comerzzia.pos.services.ticket.cabecera.CabeceraTicket;
import com.comerzzia.pos.services.ticket.cabecera.FirmaTicket;
import com.comerzzia.pos.services.ticket.cabecera.TotalesTicket;
import com.comerzzia.pos.services.ticket.lineas.LineaTicket;
import com.comerzzia.pos.services.ticket.pagos.IPagoTicket;
import com.comerzzia.pos.services.ticket.pagos.PagoTicket;
import com.comerzzia.pos.util.bigdecimal.BigDecimalUtil;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.xml.MarshallUtil;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import rest.client.anticipo.RestAnticiposClient;

@Primary
@Component
@SuppressWarnings({ "rawtypes", "deprecation" })
public class BricodepotTicketService extends TicketsService {

	@Autowired
	private TicketAuditService ticketAuditService;
	@Autowired
	private AnticiposService anticiposService;
	@Autowired
    private ListenersExecutor listenersExecutor;
    
	@Override
	public synchronized void insertarTicket(SqlSession sqlSession, TicketBean ticket, boolean ticketProcesado)
			throws TicketsServiceException {
		super.insertarTicket(sqlSession, ticket, ticketProcesado);
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public synchronized void registrarTicket(Ticket ticket, TipoDocumentoBean tipoDocumento, boolean procesarTicket)
	        throws TicketsServiceException {
		List<TicketAuditEvent> auditEvents = new ArrayList<>();
		if (((BricodepotCabeceraTicket) ticket.getCabecera()).getAuditEvents() != null) {
			auditEvents = ((BricodepotCabeceraTicket) ticket.getCabecera()).getAuditEvents();
		}
		cambiarEstadoAnticipo(ticket);

		// TODO JGG
		/* Estandar */
//		super.registrarTicket(ticket, tipoDocumento, procesarTicket);
		log.debug("registrarTicket() - Procesando ticket ... ");
		SqlSession sqlSession = SpringContext.getBean(SpringTransactionSqlSession.class);
		byte[] xmlTicket = null;
		TicketBean ticketBean;
		try {
			// Establecemos fecha del ticket
			ticket.setFecha(new Date());
			ticket.setFechaContable(sesion.getSesionCaja().getCajaAbierta().getFechaContable());

			ComerzziaApp comerzziaApp = ComerzziaApp.get();
			ticket.setSoftwareVersion(comerzziaApp.getVersionRevision());

			if (comerzziaApp.getLocalRepositoryVersion().equals(comerzziaApp.getRemoteRepositoryVersion())) {
				ticket.setLocalCopyVersion(comerzziaApp.getLocalRepositoryVersion());
			}
			else {
				ticket.setLocalCopyVersion(comerzziaApp.getLocalRepositoryVersion() + "|" + comerzziaApp.getRemoteRepositoryVersion());
			}

			sqlSession.openSession(SessionFactory.openSession());

			reiniciarContadoresLineas(ticket);

			IPagoTicket cambio = ticket.getTotales().getCambio();
			List<PagoTicket> pagos = ((TicketVenta) ticket).getPagos();

			// Borramos pagos a cero
			ListIterator<PagoTicket> listIterator = pagos.listIterator();
			while (listIterator.hasNext()) {
				PagoTicket pago = listIterator.next();
				if (BigDecimalUtil.isIgualACero(pago.getImporte())) {
					listIterator.remove();
				}
			}

			// Añadimos un pago a cero si el importe total es cero y no hay pagos
			if (BigDecimalUtil.isIgualACero(ticket.getCabecera().getTotales().getTotal()) && pagos.size() == 0) {
				PagoTicket pagoVacio = createPago();
				pagoVacio.setMedioPago(MediosPagosService.medioPagoDefecto);
				pagoVacio.setImporte(BigDecimal.ZERO);

				Integer paymentId = generateNewPaymentId(ticket);
				if (paymentId != null && paymentId > 0) {
					pagoVacio.setPaymentId(paymentId);
				}

				((TicketVenta) ticket).addPago(pagoVacio);
			}

			// Generamos movimientos de caja
			registrarMovimientosCaja((TicketVentaAbono) ticket, cambio, pagos, sqlSession);

			// Añadimos el cambio como un pago
			if (!BigDecimalUtil.isIgualACero(ticket.getCabecera().getTotales().getCambio().getImporte())) {
				IPagoTicket pagoCodMedPagoCambio = ((TicketVenta) ticket).getPago(cambio.getMedioPago().getCodMedioPago());
				MedioPagoBean medioPagoCambio = ticket.getCabecera().getTotales().getCambio().getMedioPago();

				if (pagoCodMedPagoCambio == null) {
					pagoCodMedPagoCambio = createPago();
					pagoCodMedPagoCambio.setEliminable(false);
					pagoCodMedPagoCambio.setMedioPago(medioPagoCambio);

					Integer paymentId = generateNewPaymentId(ticket);
					if (paymentId != null && paymentId > 0) {
						pagoCodMedPagoCambio.setPaymentId(paymentId);
					}

					((TicketVenta) ticket).addPago(pagoCodMedPagoCambio);
				}

				pagoCodMedPagoCambio.setImporte(pagoCodMedPagoCambio.getImporte().subtract(((TicketVenta) ticket).getTotales().getCambio().getImporte()));
			}

			String firma = generarFirma(sqlSession, ticket);

			log.debug("registrarTicket() - Ejecutando listeners posteriores al guardado del ticket");
			listenersExecutor.executeListeners(SalvadoTicketListener.class, "executeBeforeSave", sqlSession, ticket, tipoDocumento);

			// Construimos objeto persistente
			log.debug("registrarTicket() - Construyendo objeto persistente...");
			ticketBean = new TicketBean();
			ticketBean.setCodAlmacen(ticket.getCabecera().getTienda().getAlmacenBean().getCodAlmacen());
			ticketBean.setCodcaja(ticket.getCodCaja());
			ticketBean.setFecha(ticket.getFecha());
			ticketBean.setIdTicket(ticket.getIdTicket());
			ticketBean.setUidTicket(ticket.getUidTicket());
			ticketBean.setIdTipoDocumento(ticket.getCabecera().getTipoDocumento());
			ticketBean.setCodTicket(ticket.getCabecera().getCodTicket());
			ticketBean.setSerieTicket(ticket.getCabecera().getSerieTicket());
			ticketBean.setFirma(firma);
			ticketBean.setLocatorId(ticket.getCabecera().getLocalizador());

			String hashControl = ticket.getCabecera().getFirma().getHashControl();
			FirmaTicket firmaTicket = new FirmaTicket();
			firmaTicket.setHashControl(hashControl);
			firmaTicket.setFirma(ticketBean.getFirma());
			ticket.getCabecera().setFirma(firmaTicket);

			setFiscalData(ticket);

			log.debug("registrarTicket() - Generando XML del ticket...");
			xmlTicket = MarshallUtil.crearXML(ticket, getTicketClasses());
			ticketBean.setTicket(xmlTicket);

			log.debug("registrarTicket() - TICKET INSERT: " + ticket.getUidTicket());
			log.trace(new String(xmlTicket, "UTF-8") + "\n");

			insertarTicket(sqlSession, ticketBean, false);

			log.debug("registrarTicket() - Ejecutando listeners posteriores al guardado del ticket");
			listenersExecutor.executeListeners(SalvadoTicketListener.class, "executeAfterSave", sqlSession, ticket, tipoDocumento, ticketBean);

			log.debug("registrarTicket() - Eliminando copia de seguridad...");
			copiaSeguridadTicketService.eliminarBackup(sqlSession, ticketBean.getUidTicket());
			log.debug("registrarTicket() - Confirmando transacción...");
			sqlSession.commit();
			log.debug("registrarTicket() - Ticket salvado correctamente.");
		}
		catch (Throwable e) {
			try {
				sqlSession.rollback();
			}
			catch (Exception e2) {
				log.error("registrarTicket() - " + e2.getClass().getName() + " - " + e2.getLocalizedMessage(), e2);
			}
			String msg = "Se ha producido un error procesando ticket con uid " + ticket.getUidTicket() + " : " + e.getMessage();
			log.error("registrarTicket() - " + msg, e);
			throw new TicketsServiceException(e);
		}
		finally {
			sqlSession.close();
		}

		if (ticketBean != null && xmlTicket != null && procesarTicket) {
			try {
				log.debug("registrarTicket() - Procesando ticket...");
				procesarTicket(ticketBean, xmlTicket);
			}
			catch (Exception e) {
				log.warn("registrarTicket() - Ha ocurrido un error procesando ticket: " + e.getMessage(), e);
			}
		}

		try {
			log.debug("registrarTicket() - Ejecutando listeners posteriores al commit del ticket");
			listenersExecutor.executeListeners(SalvadoTicketListener.class, "executeAfterCommit", sqlSession, ticket, tipoDocumento);
		}
		catch (Exception e) {
			throw new TicketsServiceException(e);
		}
		/* Estandar */

		for (TicketAuditEvent auditEvent : auditEvents) {
			auditEvent.setUidTicketVenta(ticket.getUidTicket());
			ticketAuditService.saveAuditEvent(auditEvent);
		}
	}

	public void cambiarEstadoAnticipo(Ticket ticket) {
		String numAnticipo = ((BricodepotCabeceraTicket) ticket.getCabecera()).getNumAnticipo();

		if (StringUtils.isBlank(numAnticipo)) {
			log.warn("cambiarEstadoAnticipo() - El numero de anticipo viene vacío, no se cambiará el estado");
			return;
		}
		String importeAnticipo = ((BricodepotCabeceraTicket) ticket.getCabecera()).getImporteAnticipo();
		if (StringUtils.isBlank(importeAnticipo)) {
			log.warn("cambiarEstadoAnticipo() - El importe de anticipo viene vacío, no se cambiará el estado");
			return;
		}
		String operacionAnticipo = ((BricodepotCabeceraTicket) ticket.getCabecera()).getOperacionAnticipo();
		if (StringUtils.isBlank(operacionAnticipo)) {
			log.warn("cambiarEstadoAnticipo() - La operación de anticipo viene vacío, no se cambiará el estado");
			return;
		}
		Long idClieAlbaranAnticipo = ((BricodepotCabeceraTicket) ticket.getCabecera()).getIdClieAlbaranAnticipo();
		String apiKey = variablesServices.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
		String uidActividad = sesion.getAplicacion().getUidActividad();
		try {
			if (operacionAnticipo.equals(AnticiposConstants.PARAMETRO_PAGAR_ANTICIPO)) {
				log.debug("cambiarEstadoAnticipo() - Actualizamos el estado del anticipo");
				RestAnticiposClient.setEstado(apiKey, uidActividad, numAnticipo,
						AnticiposConstants.PARAMETRO_ESTADO_LIQUIDADO, ticket.getUidTicket());
			} else if (operacionAnticipo.equals(AnticiposConstants.PARAMETRO_ESTADO_DEVUELTO)) {
				log.debug("cambiarEstadoAnticipo() - Actualizamos el estado del anticipo");
				RestAnticiposClient.setEstado(apiKey, uidActividad, numAnticipo,
						AnticiposConstants.PARAMETRO_ESTADO_DEVUELTO, ticket.getUidTicket());
			}
		} catch (Exception e) {
			log.error("cambiarEstadoAnticipo() - No se ha podido actualizar el estado del anticipo: " + e.getMessage());
			log.debug("cambiarEstadoAnticipo() - Procedemos a guardar un documento de anticipo para que se procese.");
			AnticipoDTO anticipo = new AnticipoDTO();
			anticipo.setIdClieAlbaran(idClieAlbaranAnticipo);
			anticipo.setImporteAnticipo(importeAnticipo);
			anticipo.setNumAnticipo(numAnticipo);
			anticipo.setOperacionAnticipo(operacionAnticipo);
			anticipo.setUidActividad(uidActividad);
			anticipo.setUidAnticipo(ticket.getIdTicket().toString());

			anticiposService.guardarAnticipo(anticipo);

		}

	}

	@SuppressWarnings("unchecked")
	@Override
	protected void reiniciarContadoresLineas(Ticket ticket) {
		int contador = 1;
		List<TicketAuditEvent> auditEvents = ((BricodepotCabeceraTicket) ticket.getCabecera()).getAuditEvents();
		List<LineaTicket> lineasTicket = ticket.getLineas();

		if (auditEvents != null) {
			log.debug("reiniciarContadoresLineas - Procedemos a reiniciar y asignar los eventos auditorias a las lineas.");
			for (LineaTicket lineaTicket : lineasTicket) {
				for (TicketAuditEvent audit : auditEvents) {
					if (audit.getLineaReferencia() != null && lineaTicket.getIdLinea().equals(audit.getLineaReferencia())) {
						audit.setLineaReferencia(contador);
						break;
					}
				}
				lineaTicket.setIdLinea(contador);
				contador++;
			}
		}
		else {
			super.reiniciarContadoresLineas(ticket);
		}
	}

	public List<Class<?>> getTicketClasses() {
		List<Class<?>> classes = new LinkedList<>();

		// Obtenemos la clase root
		Class<?> clazz = SpringContext.getBean(BricodepotTicketVentaAbono.class).getClass();

		// Generamos lista de clases "ancestras" de la principal
		Class<?> superClass = clazz.getSuperclass();
		while (!superClass.equals(Object.class)) {
			classes.add(superClass);
			superClass = superClass.getSuperclass();
		}
		// Las ordenamos descendentemente
		Collections.reverse(classes);

		// Añadimos la clase principal y otras necesarias
		classes.add(clazz);
		classes.add(SpringContext.getBean(BricodepotLineaTicket.class).getClass());
		classes.add(SpringContext.getBean(CabeceraTicket.class).getClass());
		classes.add(SpringContext.getBean(TotalesTicket.class).getClass());
		classes.add(SpringContext.getBean(PagoTicket.class).getClass());

		return classes;
	}
	
	@Override
	protected void registrarMovimientosCaja(TicketVentaAbono ticket, IPagoTicket cambio, List<PagoTicket> pagos, SqlSession sqlSession) throws CajasServiceException {
		log.debug("registrarMovimientosCaja() - Registrando movimientos de caja");

		Integer idLineaCaja = cajasService.consultarProximaLineaDetalleCaja(sqlSession);

		boolean esVenta = ticket.getCabecera().esVenta();
		
		// Para comprobar que el ticket es de conversión o no y poder admitir en el if los pagos con tarjeta en estos casos.
		BricodepotCabeceraTicket cabecera = (BricodepotCabeceraTicket) ticket.getCabecera();
		if (cabecera.getFsToFt() == null) {
			cabecera.setFsToFt(false);
		}
		
		for (IPagoTicket pago : pagos) {
			if(!pago.isIntroducidoPorCajero() || !pago.isMovimientoCajaInsertado() || cabecera.getFsToFt()) {
				CajaMovimientoBean detalleCaja = new CajaMovimientoBean();
				detalleCaja.setLinea(idLineaCaja);
				detalleCaja.setFecha(ticket.getFecha());
	
				if (!esVenta) {
					if (BigDecimalUtil.isMayorOrIgualACero(ticket.getCabecera().getTotales().getTotal())) {
						if (pago.getImporte().compareTo(BigDecimal.ZERO) >= 0) {
							detalleCaja.setCargo(BigDecimal.ZERO);
							detalleCaja.setAbono(pago.getImporte().abs());
						}
						else {
							detalleCaja.setCargo(pago.getImporte().abs());
							detalleCaja.setAbono(BigDecimal.ZERO);
						}
					}
					else {
						if (pago.getImporte().compareTo(BigDecimal.ZERO) < 0) {
							detalleCaja.setCargo(BigDecimal.ZERO);
							detalleCaja.setAbono(pago.getImporte().abs());
						}
						else {
							detalleCaja.setCargo(pago.getImporte().abs());
							detalleCaja.setAbono(BigDecimal.ZERO);
						}
					}
				}
				else {
					if (pago.getImporte().compareTo(BigDecimal.ZERO) < 0) {
						detalleCaja.setCargo(BigDecimal.ZERO);
						detalleCaja.setAbono(pago.getImporte().abs());
					}
					else {
						detalleCaja.setCargo(pago.getImporte().abs());
						detalleCaja.setAbono(BigDecimal.ZERO);
					}
				}
	
				detalleCaja.setConcepto(ticket.getCabecera().getDesTipoDocumento() + ": " + ticket.getCabecera().getCodTicket());
				detalleCaja.setDocumento(ticket.getCabecera().getCodTicket());
				detalleCaja.setCodMedioPago(pago.getCodMedioPago());
				detalleCaja.setIdDocumento(ticket.getUidTicket());
				detalleCaja.setIdTipoDocumento(ticket.getCabecera().getTipoDocumento());
				cajasService.crearMovimiento(sqlSession, detalleCaja);
				idLineaCaja++;
			}
		}

		if (!BigDecimalUtil.isIgualACero(ticket.getCabecera().getTotales().getCambio().getImporte())) {
			log.debug("registrarMovimientosCaja() - Registrando movimiento de cambio");
			CajaMovimientoBean detalleCaja = new CajaMovimientoBean();

			detalleCaja.setLinea(idLineaCaja);

			detalleCaja.setFecha(ticket.getFecha());
			// Cargo o abono al revés de lo normal
			if (ticket.getCabecera().esVenta()) {
				detalleCaja.setCargo(cambio.getImporte().abs().negate());
				detalleCaja.setAbono(BigDecimal.ZERO);
			}
			else {
				detalleCaja.setCargo(BigDecimal.ZERO);
				detalleCaja.setAbono(cambio.getImporte().abs().negate());
			}

			detalleCaja.setConcepto(ticket.getCabecera().getDesTipoDocumento() + ": " + ticket.getCabecera().getCodTicket() + " (cambio)");
			detalleCaja.setDocumento(ticket.getCabecera().getCodTicket());
			detalleCaja.setCodMedioPago(cambio.getCodMedioPago());
			detalleCaja.setIdDocumento(ticket.getUidTicket());
			detalleCaja.setIdTipoDocumento(ticket.getCabecera().getTipoDocumento());
			cajasService.crearMovimiento(sqlSession, detalleCaja);
		}
	}
}
