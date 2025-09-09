package com.comerzzia.bricodepot.pos.gui.ventas.tickets.articulos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Controller;

import com.comerzzia.api.model.loyalty.FidelizadoBean;
import com.comerzzia.api.model.loyalty.TiposContactoFidelizadoBean;
import com.comerzzia.api.rest.client.fidelizados.FidelizadoRequestRest;
import com.comerzzia.bricodepot.pos.gui.ventas.tarjetas.TarjetasRegaloController;
import com.comerzzia.bricodepot.pos.gui.ventas.tarjetas.TarjetasRegaloView;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.BricodepotTicketManager;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.audit.RequestAuthorizationController;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.audit.RequestAuthorizationView;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.BricodepotPagosController;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.anticipo.AnticipoController;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.anticipo.AnticipoView;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.anticipo.PagoAnticipoView;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.anticipo.seleccion.SeleccionAnticipoController;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.anticipo.seleccion.SeleccionAnticipoView;
import com.comerzzia.bricodepot.pos.persistence.motivos.Motivo;
import com.comerzzia.bricodepot.pos.services.ticket.BricodepotCabeceraTicket;
import com.comerzzia.bricodepot.pos.services.ticket.audit.TicketAuditEvent;
import com.comerzzia.bricodepot.pos.services.ticket.audit.TicketAuditService;
import com.comerzzia.bricodepot.pos.services.ticket.lineas.BricodepotLineaTicket;
import com.comerzzia.bricodepot.pos.services.ventas.articulos.qr.ServicioArticulosQrPresupuesto;
import com.comerzzia.bricodepot.pos.services.ventas.articulos.qr.ValidacionRequeridaPresupuestoException;
import com.comerzzia.bricodepot.pos.util.AnticiposConstants;
import com.comerzzia.core.servicios.clases.parametros.valores.ValorParametroClaseNotFoundException;
import com.comerzzia.core.servicios.empresas.EmpresaException;
import com.comerzzia.core.servicios.sesion.DatosSesionBean;
import com.comerzzia.pos.core.dispositivos.Dispositivos;
import com.comerzzia.pos.core.dispositivos.dispositivo.DispositivoCallback;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.RestBackgroundTask;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.login.seleccionUsuarios.SeleccionUsuarioController;
import com.comerzzia.pos.core.gui.permisos.exception.SinPermisosException;
import com.comerzzia.pos.dispositivo.comun.tarjeta.CodigoTarjetaController;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.ConsultarFidelizadoPorIdTask;
import com.comerzzia.pos.gui.ventas.tickets.articulos.FacturacionArticulosController;
import com.comerzzia.pos.gui.ventas.tickets.articulos.LineaTicketGui;
import com.comerzzia.pos.gui.ventas.tickets.articulos.busquedas.BuscarArticulosController;
import com.comerzzia.pos.gui.ventas.tickets.articulos.edicion.EdicionArticuloController;
import com.comerzzia.pos.persistence.articulos.etiquetas.EtiquetaArticuloBean;
import com.comerzzia.pos.persistence.clientes.ClienteBean;
import com.comerzzia.pos.persistence.core.documentos.tipos.TipoDocumentoBean;
import com.comerzzia.pos.persistence.fidelizacion.FidelizacionBean;
import com.comerzzia.pos.persistence.tickets.aparcados.TicketAparcadoBean;
import com.comerzzia.pos.services.cajas.CajaRetiradaEfectivoException;
import com.comerzzia.pos.services.cajas.CajasServiceException;
import com.comerzzia.pos.services.core.documentos.DocumentoException;
import com.comerzzia.pos.services.core.documentos.Documentos;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.sesion.SesionImpuestos;
import com.comerzzia.pos.services.core.sesion.SesionPromociones;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.services.promociones.PromocionesServiceException;
import com.comerzzia.pos.services.ticket.TicketVentaAbono;
import com.comerzzia.pos.services.ticket.TicketsServiceException;
import com.comerzzia.pos.services.ticket.copiaSeguridad.CopiaSeguridadTicketService;
import com.comerzzia.pos.services.ticket.lineas.ILineaTicket;
import com.comerzzia.pos.services.ticket.lineas.LineaTicket;
import com.comerzzia.pos.services.ticket.lineas.LineaTicketException;
import com.comerzzia.pos.services.ticket.tarjetaRegalo.TarjetaRegaloException;
import com.comerzzia.pos.util.bigdecimal.BigDecimalUtil;
import com.comerzzia.pos.util.config.AppConfig;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.format.FormatUtil;
import com.comerzzia.pos.util.i18n.I18N;
import com.comerzzia.pos.util.xml.MarshallUtil;

import javafx.application.Platform;
import javafx.fxml.FXML;
import rest.bean.anticipo.Anticipo;
import rest.client.anticipo.RestAnticiposClient;

@Primary
@Controller
@SuppressWarnings("unchecked")
public class BricodepotFacturacionArticulosController extends FacturacionArticulosController {

	public static final String VENTA_TARJETA_REGALO_KEY = "Tarjeta Regalo";
	public static final String X_POS_TEXTO_INFORMATIVO_LEGAL = "X_POS.TEXTO_INFORMATIVO_LEGAL";
	public static final String X_POS_TEXTO_INFORMATIVO_LEGAL_2 = "X_POS.TEXTO_INFORMATIVO_LEGAL_2";
	public static final String X_POS_TEXTO_INFORMATIVO_LEGAL_3 = "X_POS.TEXTO_INFORMATIVO_LEGAL_3";
	public static final String X_POS_TEXTO_INFORMATIVO_LEGAL_4 = "X_POS.TEXTO_INFORMATIVO_LEGAL_4";
	public static final String X_POS_DIAS_LIMITE_LECTURA_QR_PRESUPUESTO = "X_POS.DIAS_LIMITE_LECTURA_QR_PRESUPUESTO";
	
	@Autowired
	private CopiaSeguridadTicketService copiaSeguridadTicketService;
	@Autowired
	private TicketAuditService ticketAuditService;
	@Autowired
	protected Sesion sesion;
	@Autowired
	protected ServicioArticulosQrPresupuesto servicioArticulosQrPresupuesto;
	@Autowired
	protected VariablesServices variablesServices;

	@Autowired
	private SesionPromociones sesionPromociones;
	
	@Override
	public void initializeForm() throws InitializeGuiException {
		super.initializeForm();
		
		// El botón de línea negativa, por defecto, no se mostrará en la pantalla de ventas
		botoneraAccionesTabla.setAccionVisible("ACCION_TABLA_NEGAR_REGISTRO", false);
	}
	
	@Override
	@FXML
	protected void accionNegarRegistroTabla() {

		try {
			log.debug("accionNegarRegistroTabla() - ");
			super.compruebaPermisos(PERMISO_DEVOLUCIONES);
			LineaTicketGui lineaSeleccionada = getLineaSeleccionada();
			if (lineaSeleccionada != null) {
				if (lineaSeleccionada.isCupon()) {
					VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("La línea seleccionada no se puede modificar."), this.getStage());
				}
				else {
					int idLinea = lineaSeleccionada.getIdLinea();
					ILineaTicket linea = ticketManager.getTicket().getLinea(idLinea);
					if (linea.isEditable()) {

						TicketAuditEvent auditEvent = TicketAuditEvent.forEvent(TicketAuditEvent.Type.DEVOLUCION, getLineaTicketFromGui(), sesion);
						abrirVentanaAutorizacion(auditEvent, getDatos());
						if (datos.get(RequestAuthorizationController.PERMITIR_ACCION).equals(true)) {
							// back to normal
							try {
								ticketManager.negarLineaArticulo(idLinea);
								escribirLineaEnVisor((LineaTicket) linea);
								guardarCopiaSeguridad();
								refrescarDatosPantalla();
								visor.modoVenta(visorConverter.convert(((TicketVentaAbono) ticketManager.getTicket())));
							}
							catch (LineaTicketException e) {
								log.error("accionNegarRegistroTabla() - Error recalculando importe de línea: " + e.getMessage(), e);
								VentanaDialogoComponent.crearVentanaError(getStage(), e.getMessageI18N(), e);
							}
						}

					}
					else {
						VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("La línea no se puede modificar."), this.getStage());
					}
				}
			}
		}
		catch (SinPermisosException ex) {
			log.debug("accionNegarRegistroTabla() - El usuario no tiene permisos para realizar devolución");
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("No tiene permisos para realizar una devolución"), getStage());
		}
	}

	@Override
	@FXML
	public void cancelarVenta() {
		log.debug("cancelarVenta()");

		boolean confirmacion = VentanaDialogoComponent.crearVentanaConfirmacion(I18N.getTexto("¿Está seguro de querer eliminar todas las líneas del ticket?"), getStage());
		if (!confirmacion) {
			return;
		}
		// si no hay lineas no hace falta autorizacion
		if (ticketManager.getTicket().getLineas().size() > 0) {
			TicketAuditEvent auditEvent = TicketAuditEvent.forEvent(TicketAuditEvent.Type.ANULACION_TICKET, sesion);
			abrirVentanaAutorizacion(auditEvent, getDatos());
			if (datos.get(RequestAuthorizationController.PERMITIR_ACCION).equals(true)) {
				eliminarTicket();
			}
		}
		else {
			eliminarTicket();
		}
	}

	private void eliminarTicket() {
		try {
			if (ticketManager.getTicket().getIdTicket() != null) {
				ticketManager.salvarTicketVacio();
			}
			ticketManager.eliminarTicketCompleto();
			// Restauramos la cantidad en la pantalla
			tfCantidadIntro.setText(FormatUtil.getInstance().formateaNumero(BigDecimal.ONE, 3));

			refrescarDatosPantalla();
			initializeFocus();
			tbLineas.getSelectionModel().clearSelection();

			visor.escribirLineaArriba(I18N.getTexto("---NUEVO CLIENTE---"));
			visor.modoEspera();
		}
		catch (TicketsServiceException | PromocionesServiceException | DocumentoException ex) {
			log.error("accionAnularTicket() - Error inicializando nuevo ticket: " + ex.getMessage(), ex);
			VentanaDialogoComponent.crearVentanaError(getStage(), ex.getMessageI18N(), ex);
		}
	}

	@Override
	@FXML
	protected void accionTablaEliminarRegistro() {
		log.debug("accionTablaEliminarRegistro() - ");
		try {
			if (!tbLineas.getItems().isEmpty() && getLineaSeleccionada() != null) {
				super.compruebaPermisos(PERMISO_BORRAR_LINEA);
				LineaTicketGui selectedItem = getLineaSeleccionada();
				if (selectedItem.isCupon()) {
					ticketManager.recalcularConPromociones();
					refrescarDatosPantalla();
				}
				else {
					int idLinea = getLineaSeleccionada().getIdLinea();
					ILineaTicket linea = ticketManager.getTicket().getLinea(idLinea);

					if (linea.isEditable()) {
						boolean confirmar = VentanaDialogoComponent.crearVentanaConfirmacion(I18N.getTexto("¿Está seguro de querer eliminar esta línea del ticket?"), getStage());
						if (!confirmar) {
							return;
						}
						TicketAuditEvent auditEvent = TicketAuditEvent.forEvent(TicketAuditEvent.Type.ANULACION_LINEA, getLineaTicketFromGui(), sesion);
						abrirVentanaAutorizacion(auditEvent, getDatos());
						if (datos.get(RequestAuthorizationController.PERMITIR_ACCION).equals(true)) {
							ILineaTicket lastLineMemory = null;
							if (ticketManager.getTicket().getLineas().size() == 1) {
								lastLineMemory = ((LineaTicket) ticketManager.getTicket().getLineas().get(0)).clone();
							}
							ticketManager.eliminarLineaArticulo(idLinea);

							int ultimArticulo = ticketManager.getTicket().getLineas().size();
							if (ultimArticulo > 0) {
								LineaTicket ultimaLinea = (LineaTicket) ticketManager.getTicket().getLineas().get(ultimArticulo - 1);
								escribirLineaEnVisor(ultimaLinea);
							}
							else {
								visor.escribirLineaArriba(I18N.getTexto("---NUEVO CLIENTE---"));
							}

							guardarCopiaSeguridad();
							seleccionarSiguienteLinea();
							refrescarDatosPantalla();

							if (ticketManager.getTicket().getLineas().size() > 0) {
								visor.modoVenta(visorConverter.convert(((TicketVentaAbono) ticketManager.getTicket())));
							}
							else {
								// guardar eventos del ticket que se ha descartado y el mismo evento de haberlo
								// descartado
								TicketAuditEvent eventoAnulado = TicketAuditEvent.forEvent(TicketAuditEvent.Type.ANULACION_TICKET, sesion);
								List<TicketAuditEvent> auditEvents = new ArrayList<>();
								if (((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).getAuditEvents() != null) {
									auditEvents = ((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).getAuditEvents();
								}
								auditEvents.add(eventoAnulado);
								// guardar eventos
								for (TicketAuditEvent e : auditEvents) {
									e.setUidTicketVenta(ticketManager.getTicket().getUidTicket());
									ticketAuditService.saveAuditEvent(e);
								}
								// borrar eventos del ticket
								((BricodepotTicketManager) ticketManager).clearAuditEvents();
								visor.modoEspera();
							}

							if (ticketManager.getTicket().getIdTicket() != null && ticketManager.getTicket().getLineas().isEmpty()) {
								if (lastLineMemory != null) {
									ticketManager.getTicket().getLineas().add(lastLineMemory);
								}
								ticketManager.salvarTicketVacio();
								try {
									ticketManager.eliminarTicketCompleto();
								}
								catch (Exception e) {
									log.error("Ha ocurrido un error al eliminar el ticket ", e);
								}
							}
						}

					}
					else {
						VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("La línea seleccionada no se puede modificar."), this.getStage());
					}

				}
			}
		}
		catch (SinPermisosException ex) {
			log.debug("accionTablaEliminarRegistro() - El usuario no tiene permisos para eliminar línea");
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("No tiene permisos para borrar una línea"), getStage());
		}
	}

	protected ILineaTicket getLineaTicketFromGui() {
		LineaTicketGui lineaSeleccionada = getLineaSeleccionada();
		int idLinea = lineaSeleccionada.getIdLinea();
		ILineaTicket linea = ticketManager.getTicket().getLinea(idLinea);
		return linea;
	}

	protected void escribirLineaEnVisor(LineaTicket linea) {
		String desc = linea.getArticulo().getDesArticulo();
		visor.escribir(desc, linea.getCantidadAsString() + " X " + FormatUtil.getInstance().formateaImporte(linea.getPrecioTotalConDto()));
	}

	protected void abrirVentanaAutorizacion(TicketAuditEvent auditEvent, HashMap<String, Object> datos) {
		List<TicketAuditEvent> events = new ArrayList<>();
		events.add(auditEvent);
		datos.put(RequestAuthorizationController.AUDIT_EVENT, events);
		datos.put(FacturacionArticulosController.TICKET_KEY, ticketManager);
		getApplication().getMainView().showModalCentered(RequestAuthorizationView.class, datos, this.getStage());
	}
	protected void abrirVentanaAutorizacion(List<TicketAuditEvent> lstauditEvent, HashMap<String, Object> datos) {
		datos.put(RequestAuthorizationController.AUDIT_EVENT, lstauditEvent);
		datos.put(FacturacionArticulosController.TICKET_KEY, ticketManager);
		getApplication().getMainView().showModalCentered(RequestAuthorizationView.class, datos, this.getStage());
	}

	// registrar eventos auditoria cuando se cancela un ticket
	@Override
	protected void consultarCopiaSeguridad() throws DocumentoException, TicketsServiceException {
		// Comprobamos si existens copias de seguridad de tickets en base de datos para
		// esta pantalla y si es así
		// ofrecemos
		// la posibilidad de recuperarlo
		TipoDocumentoBean tipoDocumentoActivo = sesion.getAplicacion().getDocumentos().getDocumento(Documentos.FACTURA_SIMPLIFICADA);
		final TicketAparcadoBean copiaSeguridad = copiaSeguridadTicketService.consultarCopiaSeguridadTicket(tipoDocumentoActivo);

		if (copiaSeguridad != null) {
			TicketVentaAbono ticketRecuperado = (TicketVentaAbono) MarshallUtil.leerXML(copiaSeguridad.getTicket(), ticketManager.getTicketClasses(tipoDocumentoActivo).toArray(new Class[] {}));

			if (ticketRecuperado != null) {
				if (ticketRecuperado.getIdTicket() != null) {
					if (ticketRecuperado.getPagos().isEmpty()) {
						VentanaDialogoComponent.crearVentanaInfo(I18N.getTexto("Existe un ticket sin finalizar. Se tiene que terminar ese ticket antes de poder vender."), getStage());
					}
					else {
						VentanaDialogoComponent.crearVentanaInfo(I18N.getTexto("Existe un ticket guardado con pagos realizados. Se tiene que terminar ese ticket antes de poder vender."), getStage());
					}

					try {
						ticketManager.recuperarCopiaSeguridadTicket(getStage(), copiaSeguridad);
						abrirPagos();
						return;
					}
					catch (Throwable e) {
						log.error("consultarCopiaSeguridad() - Ha habido un error al recuperar el ticket: " + e.getMessage(), e);
						VentanaDialogoComponent.crearVentanaError(getStage(), e.getMessage(), e);
						return;
					}
				}

				if (!tieneArticuloGiftCard(ticketRecuperado, tipoDocumentoActivo)) {
					if (VentanaDialogoComponent.crearVentanaConfirmacion(I18N.getTexto("Existe una venta sin finalizar. ¿Desea recuperarla?"), getStage())) {
						try {
							ticketManager.recuperarCopiaSeguridadTicket(getStage(), copiaSeguridad);

							Platform.runLater(new Runnable(){

								@Override
								public void run() {
									visor.modoVenta(visorConverter.convert(((TicketVentaAbono) ticketManager.getTicket())));
								}
							});
						}
						catch (Throwable e) {
							log.error("consultarCopiaSeguridad() - " + e.getMessage(), e);
							VentanaDialogoComponent.crearVentanaError(getStage(), e.getMessage(), e);
						}
					}
					else {
						// guarda los registros en de auditoria que hay en la cabecera (si es que hay)
						try {
							// crea nuevo evento auditoria ticket anulado
							TicketAuditEvent eventoAnular = TicketAuditEvent.forEvent(TicketAuditEvent.Type.ANULACION_TICKET, sesion);
							// saca todos los eventos de la cabecera del ticket
							List<TicketAuditEvent> eventos = TicketAuditService.EventsFromByteArray(copiaSeguridad.getTicket());
							eventos.add(eventoAnular);
							for (TicketAuditEvent auditEvent : eventos) {
								// este ticket sera eliminado
								auditEvent.setUidTicketVenta(copiaSeguridad.getUidTicket());
								ticketAuditService.saveAuditEvent(auditEvent);
							}

						}
						catch (Exception e) {
							log.error("consultarCopiaSeguridad() - " + e.getMessage(), e);
							VentanaDialogoComponent.crearVentanaError(getStage(), e.getMessage(), e);
						}
					}
				}
			}
		}
	}

	/**
	 * Customizacion para verificar que el articulo esta activo; en caso contrario no permitir anadir articulo y mostrar
	 * popup de alerta
	 */
	@Override
	protected boolean validarLinea(LineaTicket linea) {
		if (linea == null) {
			return true; // si codArt introducido es cupón linea es null. Evita NPE.
		}

		return linea.getArticulo().getActivo();
	}

	// forzar que insertar linea venta falle si el articulo esta inactivo
	@Override
	public synchronized LineaTicket insertarLineaVenta(String sCodart, String sDesglose1, String sDesglose2, BigDecimal nCantidad)
	        throws LineaTicketException, PromocionesServiceException, DocumentoException, CajasServiceException, CajaRetiradaEfectivoException {
		if (ticketManager.getTicket().getLineas().isEmpty()) {
			// Es la primera linea así que llamamos a nuevoTicket()
			log.debug("insertarLineaVenta() - Se inserta la primera línea al ticket por lo que inicializamos nuevo ticket");

			ClienteBean cliente = ticketManager.getTicket().getCliente();
			FidelizacionBean tarjeta = ticketManager.getTicket().getCabecera().getDatosFidelizado();
			crearNuevoTicket();
			ticketManager.getTicket().setCliente(cliente);
			ticketManager.getTicket().getCabecera().setDatosFidelizado(tarjeta);
		}

		BricodepotLineaTicket linea = (BricodepotLineaTicket) ticketManager.nuevaLineaArticulo(sCodart, sDesglose1, sDesglose2, nCantidad, getStage(), null, false);

		// Método para mostrar pop-up tipologias
		mostrarPopUpTipologias(linea);

		if (!validarLinea(linea)) {
			ticketManager.eliminarLineaArticulo(linea.getIdLinea());
		}
		guardarCopiaSeguridad();

		boolean isCoupon = sesionPromociones.isCoupon(sCodart);
		if (ticketManager.getTicket().getLineas().contains(linea) || isCoupon) {
			return linea;
		}
		else {
			throw new LineaTicketException("No se pueden añadir artículos inactivos a la venta");
		}
	}

	public void pagarAnticipo() throws TicketsServiceException, PromocionesServiceException, DocumentoException {

		getApplication().getMainView().showModalCentered(SeleccionAnticipoView.class, getDatos(), getStage());

		String cobrarPagar = (String) getDatos().get(SeleccionAnticipoController.PARAMETRO_COBRAR_PAGAR);
		if (cobrarPagar != null) {
			if (cobrarPagar.equals(AnticiposConstants.PARAMETRO_COBRAR_ANTICIPO)) {
				if (ticketManager.getTicket().getLineas().isEmpty()) {
					try {
						getApplication().getMainView().showModalCentered(AnticipoView.class, getDatos(), getStage());

						if (!getDatos().containsKey(AnticipoController.PARAMETRO_CANCELAR)) {

							String importeAnticipo = (String) getDatos().get(AnticipoController.PARAMETRO_IMPORTE);
							String numAnticipo = (String) getDatos().get(AnticipoController.PARAMETRO_NUM_ANTICIPO);
							String articulo = variablesServices.getVariableAsString("POS.ARTICULO_ANTICIPO");
							BricodepotLineaTicket lineaAnticipo = (BricodepotLineaTicket) ticketManager.nuevaLineaArticulo(articulo, null, null, BigDecimal.ONE, null);
							lineaAnticipo.setPrecioTotalSinDto((new BigDecimal(importeAnticipo)));
							lineaAnticipo.recalcularImporteFinal();
							lineaAnticipo.setIsAnticipo(true);
							ticketManager.getTicket().getTotales().recalcular();
							((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).setImporteAnticipo(importeAnticipo);
							((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).setNumAnticipo(numAnticipo);
							((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).setOperacionAnticipo(AnticiposConstants.PARAMETRO_COBRAR_ANTICIPO);
							abrirPagos();
						}
					}
					catch (Exception e) {
						log.error("Error: No se ha podido añadir el anticipo", e);
						VentanaDialogoComponent.crearVentanaError("No se ha podido añadir el anticipo", getStage());
					}
				}
				else {
					VentanaDialogoComponent.crearVentanaAviso("Para poder añadir el anticipo debe eliminar los artículos del ticket", getStage());
				}
			}
			else if (cobrarPagar.equals(AnticiposConstants.PARAMETRO_PAGAR_ANTICIPO)) {
				try {
					getApplication().getMainView().showModalCentered(PagoAnticipoView.class, getDatos(), getStage());

					if (!getDatos().containsKey(AnticipoController.PARAMETRO_CANCELAR)) {

						String apiKey = variablesServices.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
						String uidActividad = sesion.getAplicacion().getUidActividad();
						String numAnticipo = (String) getDatos().get(AnticipoController.PARAMETRO_NUM_ANTICIPO);
						Anticipo anticipo = RestAnticiposClient.getAnticipo(apiKey, uidActividad, numAnticipo);

						String operacionAnticipo = ((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).getOperacionAnticipo();

						if (anticipo != null && anticipo.getEstado().equals(AnticiposConstants.PARAMETRO_ESTADO_DISPONIBLE)) {
							if (operacionAnticipo == null || !operacionAnticipo.equals(AnticiposConstants.PARAMETRO_PAGAR_ANTICIPO)) {
								String articulo = variablesServices.getVariableAsString("POS.ARTICULO_ANTICIPO");
								LineaTicket lineaAnticipo = ticketManager.nuevaLineaArticulo(articulo, null, null, BigDecimal.ONE.negate(), null);

								lineaAnticipo.setPrecioTotalSinDto((new BigDecimal(anticipo.getImporte())));
								lineaAnticipo.recalcularImporteFinal();
								ticketManager.getTicket().getTotales().recalcular();
								((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).setImporteAnticipo(anticipo.getImporte());
								((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).setNumAnticipo(numAnticipo);
								((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).setOperacionAnticipo(AnticiposConstants.PARAMETRO_PAGAR_ANTICIPO);
								((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).setIdClieAlbaranAnticipo(anticipo.getIdClieAlbaran());

								refrescarDatosPantalla();
							}
							else {
								VentanaDialogoComponent.crearVentanaAviso("Ya hay añadido un anticipo en el ticket", getStage());
							}
						}
						else {
							VentanaDialogoComponent.crearVentanaAviso("El anticipo no es válido, ya se ha utilizado anteriormente", getStage());
						}

					}
				}
				catch (LineaTicketException e) {
					log.error("Error: No se ha podido añadir el anticipo", e);
					VentanaDialogoComponent.crearVentanaError("No se ha podido añadir el anticipo", getStage());
				}
				catch (BadRequestException e) {
					log.error("Error: No se ha podido añadir el anticipo", e);
					VentanaDialogoComponent.crearVentanaError("No se ha podido añadir el anticipo", getStage());
				}
				catch (NotFoundException e) {
					log.error("Error: No se ha podido encontrar el anticipo", e);
					VentanaDialogoComponent.crearVentanaError("No se ha podido encontrar ese anticipo", getStage());
				}
				catch (Exception e) {
					log.error("Error: No se ha podido añadir el anticipo", e);
					VentanaDialogoComponent.crearVentanaError("No se ha podido añadir el anticipo", getStage());
				}
			}
		}
	}

	@Override
	public void abrirBusquedaArticulos() {
		log.debug("abrirBusquedaArticulos()");

		/*
		 * // Validamos el campo cantidad antes de iniciar la búsqueda. Si el campo es vacío lo seteamos a 1 sin
		 * devolver // error if (tfCantidadIntro.getText().trim().equals("")) {
		 * tfCantidadIntro.setText(FormatUtil.getInstance().formateaNumero(BigDecimal.ONE, 3)); }
		 */

		// Validamos que hay introducida una cantidad válida de artículos . Nota : También valida el campo código
		// introducido. Podemos crear otro metodo de validación para que no lo haga
		frValidacionBusqueda.setCantidad(tfCantidadIntro.getText());
		if (!accionValidarFormularioBusqueda()) {
			return; // Si la validación de la cantidad no es satisfactoria, no realizamos la búsqueda
		}

		datos = new HashMap<>();
		getDatos().put(BuscarArticulosController.PARAMETRO_ENTRADA_CLIENTE, ticketManager.getTicket().getCliente());
		getDatos().put(BuscarArticulosController.PARAMETRO_ENTRADA_CODTARIFA, ticketManager.getTarifaDefault());
		getDatos().put(BuscarArticulosController.PARAM_MODAL, Boolean.TRUE);
		getDatos().put(SeleccionUsuarioController.PARAMETRO_ES_STOCK, Boolean.FALSE);
		abrirVentanaBusquedaArticulos();
		initializeFocus();

		if (datos.containsKey(BuscarArticulosController.PARAMETRO_SALIDA_CODART)) {
			String codArt = (String) getDatos().get(BuscarArticulosController.PARAMETRO_SALIDA_CODART);
			String desglose1 = (String) getDatos().get(BuscarArticulosController.PARAMETRO_SALIDA_DESGLOSE1);
			String desglose2 = (String) getDatos().get(BuscarArticulosController.PARAMETRO_SALIDA_DESGLOSE2);

			frValidacionBusqueda.setCantidad(tfCantidadIntro.getText());
			try {
				if (accionValidarFormularioBusqueda()) {
					if (ticketManager.comprobarTarjetaRegalo(codArt)) {
						abrirPantallaGiftcard();

						if (getDatos().containsKey(TarjetasRegaloController.TIPO_TARJETA_REGALO) && getDatos().containsKey(TarjetasRegaloController.RECARGO_TARJETA)) {

							BigDecimal recargoTarjeta = new BigDecimal((String) getDatos().get(TarjetasRegaloController.RECARGO_TARJETA));
							String tipoTarjeta = (String) getDatos().get(TarjetasRegaloController.TIPO_TARJETA_REGALO);
							LineaTicket linea = insertarLineaVenta(codArt, desglose1, desglose2, BigDecimal.ONE);
							if (tipoTarjeta.equals(TarjetasRegaloController.TIPO_TARJETA_R)) {
								linea.setPrecioSinDto(recargoTarjeta);
								linea.setPrecioTotalSinDto(recargoTarjeta);
								linea.recalcularImporteFinal();
								ticketManager.getTicket().getTotales().recalcular();
							}
							else if (tipoTarjeta.equals(TarjetasRegaloController.TIPO_TARJETA_GC)) {
								TicketAuditEvent auditEvent = TicketAuditEvent.forEvent(TicketAuditEvent.Type.GESTO_COMERCIAL, sesion);
								abrirVentanaAutorizacion(auditEvent, getDatos());
								if (datos.get(RequestAuthorizationController.PERMITIR_ACCION).equals(true)) {
									linea.setPrecioSinDto(BigDecimal.ZERO);
									linea.setPrecioTotalSinDto(BigDecimal.ZERO);
									linea.recalcularImporteFinal();
									ticketManager.getTicket().getTotales().recalcular();
								}
								else {
									eliminarTicket();
									return;
								}
							}
							getDatos().put(VENTA_TARJETA_REGALO_KEY, true);
							getDatos().put(TarjetasRegaloController.TIPO_TARJETA_REGALO, tipoTarjeta);
							getDatos().put(TarjetasRegaloController.RECARGO_TARJETA, recargoTarjeta.toString());

							abrirPagos();
						}
					}
					else {
						LineaTicket linea = insertarLineaVenta(codArt, desglose1, desglose2, frValidacionBusqueda.getCantidadAsBigDecimal());

						comprobarArticuloGenerico(linea);

						if (linea.getGenerico()) {
							HashMap<String, Object> parametrosEdicionArticulo = new HashMap<>();
							parametrosEdicionArticulo.put(EdicionArticuloController.CLAVE_PARAMETRO_ARTICULO, linea);
							parametrosEdicionArticulo.put(FacturacionArticulosController.TICKET_KEY, ticketManager);
							abrirVentanaEdicionArticulo(parametrosEdicionArticulo);

							if (parametrosEdicionArticulo.containsKey(EdicionArticuloController.CLAVE_CANCELADO)) {
								throw new LineaInsertadaNoPermitidaException(linea);
							}
						}

						comprobarLineaPrecioCero(linea);

						// Comprobamos si es necesario asignar números de serie
						asignarNumerosSerie(linea);

						ticketManager.recalcularConPromociones();
					}
					if (getDatos().containsKey(BricodepotPagosController.ACCION_CANCELAR_TARJETA) && getDatos().get(BricodepotPagosController.ACCION_CANCELAR_TARJETA).equals(Boolean.TRUE)) {
						eliminarTicket();
						getDatos().remove(BricodepotPagosController.ACCION_CANCELAR_TARJETA);
					}
					else {
						// Restauramos la cantidad en la pantalla
						tfCantidadIntro.setText(FormatUtil.getInstance().formateaNumero(BigDecimal.ONE, 3));
						refrescarDatosPantalla();

						if (ticketManager.getTicket().getLineas().size() > 0) {
							tbLineas.getSelectionModel().select(0);

							int ultimArticulo = ticketManager.getTicket().getLineas().size();
							LineaTicket linea = (LineaTicket) ticketManager.getTicket().getLineas().get(ultimArticulo - 1);
							escribirLineaEnVisor(linea);

							visor.modoVenta(visorConverter.convert(((TicketVentaAbono) ticketManager.getTicket())));
						}
					}
				}
			}
			catch (LineaTicketException ex) {
				log.error("abrirBusquedaArticulos() - ACCION BUSQUEDA ARTICULOS - Error registrando línea de ticket");
				VentanaDialogoComponent.crearVentanaError(ex.getLocalizedMessage(), this.getScene().getWindow());
			}
			catch (TarjetaRegaloException e) {
				log.error(e);
				VentanaDialogoComponent.crearVentanaError(e.getMessageI18N(), getStage());
			}
			catch (LineaInsertadaNoPermitidaException e) {
				ticketManager.getTicket().getLineas().remove(e.getLinea());
				guardarCopiaSeguridad();
				if (e.getMessage() != null) {
					VentanaDialogoComponent.crearVentanaError(e.getMessage(), getStage());
				}
			}
			catch (CajaRetiradaEfectivoException e) {
				VentanaDialogoComponent.crearVentanaError(e.getMessageDefault(), getStage());
			}
			catch (Exception e) {
				log.error("abrirBusquedaArticulos() - " + e.getClass().getName() + " - " + e.getLocalizedMessage(), e);
				VentanaDialogoComponent.crearVentanaError(getStage(), I18N.getTexto("No se ha podido insertar la línea"), e);
			}
		}
	}

	@Override
	protected void nuevoArticuloTaskSucceeded(LineaTicket value) {
		try {
			boolean esTarjetaRegalo = ticketManager.comprobarTarjetaRegaloLineaYaInsertada(value);

			int ultimArticulo = ticketManager.getTicket().getLineas().size();
			LineaTicket linea = (LineaTicket) ticketManager.getTicket().getLineas().get(ultimArticulo - 1);

			if (esTarjetaRegalo) {
				abrirPantallaGiftcard();

				if (getDatos().containsKey(TarjetasRegaloController.TIPO_TARJETA_REGALO) && getDatos().containsKey(TarjetasRegaloController.RECARGO_TARJETA)) {
					BigDecimal recargoTarjeta = new BigDecimal((String) getDatos().get(TarjetasRegaloController.RECARGO_TARJETA));
					String tipoTarjeta = (String) getDatos().get(TarjetasRegaloController.TIPO_TARJETA_REGALO);
					if (tipoTarjeta.equals(TarjetasRegaloController.TIPO_TARJETA_R)) {
						linea.setPrecioSinDto(recargoTarjeta);
						linea.setPrecioTotalSinDto(recargoTarjeta);
						linea.recalcularImporteFinal();
						ticketManager.getTicket().getTotales().recalcular();
					}
					else if (tipoTarjeta.equals(TarjetasRegaloController.TIPO_TARJETA_GC)) {
						TicketAuditEvent auditEvent = TicketAuditEvent.forEvent(TicketAuditEvent.Type.GESTO_COMERCIAL, sesion);
						abrirVentanaAutorizacion(auditEvent, getDatos());
						if (datos.get(RequestAuthorizationController.PERMITIR_ACCION).equals(true)) {
							linea.setPrecioSinDto(BigDecimal.ZERO);
							linea.setPrecioTotalSinDto(BigDecimal.ZERO);
							linea.recalcularImporteFinal();
							ticketManager.getTicket().getTotales().recalcular();
						}
						else {
							eliminarTicket();
							return;
						}
					}
					getDatos().put(VENTA_TARJETA_REGALO_KEY, true);
					getDatos().put(TarjetasRegaloController.TIPO_TARJETA_REGALO, tipoTarjeta);
					getDatos().put(TarjetasRegaloController.RECARGO_TARJETA, recargoTarjeta.toString());
					abrirPagos();
				}
			}
			else {
				if (changePrice != null) {
					linea.setPrecioTotalSinDto(changePrice);
					SesionImpuestos sesionImpuestos = sesion.getImpuestos();
					BigDecimal precioSinDto = sesionImpuestos.getPrecioSinImpuestos(linea.getCodImpuesto(), changePrice, linea.getCabecera().getCliente().getIdTratImpuestos());
					linea.setPrecioSinDto(precioSinDto);
					changePrice = null;
				}
			}
			linea = value;

			if (linea != null && !esTarjetaRegalo) { // No es cupón
				comprobarArticuloGenerico(value);
				if (linea.getGenerico()) {
					HashMap<String, Object> parametrosEdicionArticulo = new HashMap<>();
					parametrosEdicionArticulo.put(EdicionArticuloController.CLAVE_PARAMETRO_ARTICULO, linea);
					parametrosEdicionArticulo.put(FacturacionArticulosController.TICKET_KEY, ticketManager);
					abrirVentanaEdicionArticulo(parametrosEdicionArticulo);

					if (parametrosEdicionArticulo.containsKey(EdicionArticuloController.CLAVE_CANCELADO)) {
						throw new LineaInsertadaNoPermitidaException(linea);
					}
				}
				ticketManager.recalcularConPromociones();
				comprobarLineaPrecioCero(value);

				visor.escribir(linea.getArticulo().getDesArticulo(), linea.getCantidadAsString() + " X " + FormatUtil.getInstance().formateaImporte(linea.getPrecioTotalConDto()));
			}

			if (getDatos().containsKey(BricodepotPagosController.ACCION_CANCELAR_TARJETA) && getDatos().get(BricodepotPagosController.ACCION_CANCELAR_TARJETA).equals(Boolean.TRUE)) {
				eliminarTicket();
				getDatos().remove(BricodepotPagosController.ACCION_CANCELAR_TARJETA);
			}
			else {
				asignarNumerosSerie(linea);

				if (mostrarVentanaInfo) {
					if (linea == null) {
						VentanaDialogoComponent.crearVentanaInfo(I18N.getTexto("El cupón se ha añadido correctamente."), getStage());
					}
					else {
						VentanaDialogoComponent.crearVentanaInfo(I18N.getTexto("El artículo se ha añadido correctamente."), getStage());
					}
				}

				mostrarVentanaInfo = false;

				// Restauramos la cantidad en la pantalla
				tfCantidadIntro.setText(FormatUtil.getInstance().formateaNumero(BigDecimal.ONE, 3));
				if (!esTarjetaRegalo) {
					refrescarDatosPantalla();
				}
				visor.modoVenta(visorConverter.convert(((TicketVentaAbono) ticketManager.getTicket())));

				tbLineas.getSelectionModel().select(0);
			}
		}
		catch (LineaInsertadaNoPermitidaException e) {
			ticketManager.getTicket().getLineas().remove(value);
			guardarCopiaSeguridad();
			refrescarDatosPantalla();
			if (e.getMessage() != null) {
				VentanaDialogoComponent.crearVentanaError(e.getMessage(), getStage());
			}
		}
	}

	public void abrirPantallaGiftcard() {
		log.info("recargarGiftcard() - Abriendo pantalla de gestión de tarjetas...");
		getApplication().getMainView().showModalCentered(TarjetasRegaloView.class, getDatos(), getStage());
	}

	@Override
	public void fidelizacion() {
		log.debug("fidelizacion()");
		Dispositivos.getInstance().getFidelizacion().pedirTarjetaFidelizado(getStage(), new DispositivoCallback<FidelizacionBean>(){

			@Override
			public void onSuccess(FidelizacionBean tarjeta) {
				if (tarjeta.isBaja()) {
					VentanaDialogoComponent.crearVentanaError(I18N.getTexto("La tarjeta de fidelización {0} no está activa", tarjeta.getNumTarjetaFidelizado()), getStage());
					tarjeta = null;
					ticketManager.getTicket().getCabecera().setDatosFidelizado(tarjeta);
				}
				else {
					// Tarjeta válida - lo seteamos en el ticket
					ticketManager.getTicket().getCabecera().setDatosFidelizado(tarjeta);
				}

				ticketManager.recalcularConPromociones();
				guardarCopiaSeguridad();
				refrescarDatosPantalla();

				/* BRICO-287 Edición de fidelizados con datos incompletos */
				consultaDatosIncompletosFidelizado();
				/* fin BRICO-287 */
			}

			@Override
			public void onFailure(Throwable e) {
				// Los errores se muestran desde el código del dispositivo
				// Quitamos los datos de fidelizado
				FidelizacionBean tarjeta = null;
				ticketManager.getTicket().getCabecera().setDatosFidelizado(tarjeta);
				guardarCopiaSeguridad();
				ticketManager.recalcularConPromociones();
				refrescarDatosPantalla();
			}

		});

	}

	private boolean faltanCamposObligatoriosFidelizado(FidelizadoBean fidelizado) {
		log.debug("faltanCamposObligatoriosFidelizado()");
		if (StringUtils.isEmpty(fidelizado.getNombre())) {
			log.debug("faltanCamposObligatoriosFidelizado() - Nombre vacio");
			return true;
		}

		if (StringUtils.isEmpty(fidelizado.getApellidos())) {
			log.debug("faltanCamposObligatoriosFidelizado() - Apellidos vacio");
			return true;
		}

		if (StringUtils.isEmpty(fidelizado.getDocumento())) {
			log.debug("faltanCamposObligatoriosFidelizado() - Documento vacio");
			return true;
		}

		if (StringUtils.isEmpty(fidelizado.getCodPais())) {
			log.debug("faltanCamposObligatoriosFidelizado() - Codigo de pais vacio");
			return true;
		}

		if (StringUtils.isEmpty(fidelizado.getCp())) {
			log.debug("faltanCamposObligatoriosFidelizado() - CP vacio");
			return true;
		}

		if (StringUtils.isEmpty(fidelizado.getCodTipoIden())) {
			log.debug("faltanCamposObligatoriosFidelizado() - Codigo de tipo identificacion vacio");
			return true;
		}

		TiposContactoFidelizadoBean email = fidelizado.getTipoContacto("EMAIL");
		if (email == null || email.getValor() == null) {
			log.debug("faltanCamposObligatoriosFidelizado() - Email vacio");
			return true;
		}

		if (fidelizado.getColectivos() == null || fidelizado.getColectivos().isEmpty()) {
			log.debug("faltanCamposObligatoriosFidelizado() - Colectivo vacio");
			return true;
		}

		return false;
	}

	/**
	 * Metodo para leer ciertas propiedades y mostrar un popUp en base a estas
	 * 
	 * @throws ValorParametroClaseNotFoundException
	 * @throws EmpresaException
	 */
	public void mostrarPopUpTipologias(BricodepotLineaTicket linea) {
		log.debug("mostrarPopUpTipologias() - Iniciando PopUp tipológias");
		try {
			String texto = null;
			DatosSesionBean datosSesion = new DatosSesionBean();
			datosSesion.setUidActividad(sesion.getAplicacion().getUidActividad());
			datosSesion.setUidInstancia(sesion.getAplicacion().getUidInstancia());

			List<EtiquetaArticuloBean> etiquetas = linea.getArticulo().getEtiquetas();

			for (EtiquetaArticuloBean etiquetaArticuloBean : etiquetas) {
				switch (etiquetaArticuloBean.getEtiqueta().toLowerCase()) {
					case "aire acondicionado":
						
						//Puede que la variable sea null para asegurar usamos StringUtils.defaultString(
						texto = StringUtils.defaultString(variablesServices.getVariableAsString(X_POS_TEXTO_INFORMATIVO_LEGAL))
						        + StringUtils.defaultString(variablesServices.getVariableAsString(X_POS_TEXTO_INFORMATIVO_LEGAL_2))
						        + StringUtils.defaultString(variablesServices.getVariableAsString(X_POS_TEXTO_INFORMATIVO_LEGAL_3))
						        + StringUtils.defaultString(variablesServices.getVariableAsString(X_POS_TEXTO_INFORMATIVO_LEGAL_4));

						break;

					case "producto precursor explosivo regulado":
						texto = I18N.getTexto("Notificar las transacciones sospechosas (Compra superior a 6 unidades) en el plazo de 24 horas a Rafael Crecente, Health & Safety Manager");
						break;

					case "producto biocida uso profesional":
						texto = I18N.getTexto("Necesita acreditación de usuario profesional para la compra de este producto. Comunicación al cliente que la ficha de seguridad se encuentra en página web http://bricodepot.es");
						break;

					default:
						break;
				}
			}

			if (StringUtils.isNotBlank(texto)) {
				log.debug("mostrarPopUpTipologias() - " + texto);
				VentanaDialogoComponent.crearVentanaInfo(texto, getStage());
			}
		}
		catch (Exception e) {
			log.error("mostrarPopUpTipologias() - No se ha encontrado propiedades para el articulo: " + linea);
		}
	}
	
	private void aplicarQr(String codArticulo) throws DocumentoException, PromocionesServiceException, TicketsServiceException {
		String[] lineas = codArticulo.split("#");
		boolean presupuestoUtilizado = servicioArticulosQrPresupuesto.comprobarPresupuestoUtilizado(lineas[0]);
		if (!presupuestoUtilizado) {
			Boolean cambioPrecio = true;
			if (muestraAutorizacionQr(lineas[1])) {
				TicketAuditEvent auditEventQr = TicketAuditEvent.forEvent(TicketAuditEvent.Type.QR_CADUCADO, sesion);
				abrirVentanaAutorizacion(auditEventQr, getDatos());
				Boolean usarQrCaducado = (Boolean) getDatos().get(RequestAuthorizationController.PERMITIR_ACCION);
				if (!usarQrCaducado) {
					tfCodigoIntro.clear();
					return;
				}
			}
			
			int numLineasErroneas = 0;
			Map<Integer, BigDecimal> lineasConCambioPrecio = new HashMap<Integer, BigDecimal>();
			for (int i = 2; i < lineas.length; i++) {
				String[] partes = lineas[i].split(",");
				try {
					BricodepotLineaTicket newLinea = (BricodepotLineaTicket) super.nuevoArticuloTaskCall(partes[0], new BigDecimal(partes[1]));
					newLinea.setNumPresupuesto(lineas[0].replace("ID-", ""));
					if (BigDecimalUtil.isMenor(new BigDecimal(partes[2]), newLinea.getPrecioTotalConDto())) {
						lineasConCambioPrecio.put(newLinea.getIdLinea(), new BigDecimal(partes[2]));
					}
				}
				catch (Exception e) {
					log.error("Articulo" + partes[0] + " no encontrado, no puede ser insertado " + e.getMessage(), e);
					numLineasErroneas++;
					continue;
				}
			}
			
			ticketManager.recalcularConPromociones();
			
			Iterator<Entry<Integer, BigDecimal>> iterator = lineasConCambioPrecio.entrySet().iterator();
			while (iterator.hasNext()) {
			    Entry<Integer, BigDecimal> entry = iterator.next();
			    BricodepotLineaTicket lineaTicket = (BricodepotLineaTicket) ticketManager.getTicket().getLinea(entry.getKey());
				if(BigDecimalUtil.isMayor(entry.getValue(), lineaTicket.getPrecioTotalConDto())){
					iterator.remove(); 
				}
			}
			
			if (lineasConCambioPrecio.size() > 0) {
				List<TicketAuditEvent> lstEventosAuditoria = new ArrayList<TicketAuditEvent>();
				for (Entry<Integer, BigDecimal> entry : lineasConCambioPrecio.entrySet()) {
					BricodepotLineaTicket lineaTicket = (BricodepotLineaTicket) ticketManager.getTicket().getLinea(entry.getKey());
					aplicarQrImporteLinea(lineaTicket, entry.getValue());
					lstEventosAuditoria.add(TicketAuditEvent.forEvent(TicketAuditEvent.Type.CAMBIO_PRECIO,lineaTicket,sesion));
				}
				abrirVentanaAutorizacion(lstEventosAuditoria, getDatos());
				cambioPrecio = (Boolean) getDatos().get(RequestAuthorizationController.PERMITIR_ACCION);
				if (!cambioPrecio) {
					ticketManager.eliminarTicketCompleto();
					tfCodigoIntro.clear();
					refrescarDatosPantalla();
					return;
				}					
			}
			
			refrescarDatosPantalla();
			
			if (numLineasErroneas > 0) {
				VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("Uno o más artículos no existen, por favor, contactar con un supervisor"), getStage());
			}
			((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).setNumPresupuesto(lineas[0].replace("ID-", ""));
		}
		else {
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("El presupuesto ya ha sido pagado, por favor, revise el presupuesto"), getStage());
		}

		tfCodigoIntro.setText("");
	}

	private boolean muestraAutorizacionQr(String fechaQr) {
	    LocalDate fechaValidez;
	    try {
	        fechaValidez = LocalDate.parse(fechaQr, DateTimeFormatter.ISO_LOCAL_DATE);
	    } catch (DateTimeParseException e) {
	        mostrarAviso("aviso.presupuesto.qr.fecha.invalida");
	        return false;
	    }

	    Integer diasLimitePresupuestoQR = variablesServices.getVariableAsInteger(X_POS_DIAS_LIMITE_LECTURA_QR_PRESUPUESTO);

	    try {
	    	log.debug("muestraAutorizacionQr() - Comprobando vigencia de presupuesto para fecha: " + fechaValidez.toString());
	        servicioArticulosQrPresupuesto.validarVigenciaPresupuesto(fechaValidez, diasLimitePresupuestoQR);
	    } catch (ValidacionRequeridaPresupuestoException e) {
	        mostrarAviso("aviso.presupuesto.qr.autorizacion");
	        return true;
	    }

	    return false;
	}

		private void mostrarAviso(String mensaje) {
		    VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto(mensaje), getStage());
		}

	private void aplicarQrImporteLinea(BricodepotLineaTicket linea, BigDecimal importe) {
		log.debug("aplicarQrImporteLinea() - Aplicando lógica para el importe del QR introducido");
		aplicarPrecioQr(linea, importe);
		ticketManager.getTicket().getTotales().recalcular();
		refrescarDatosPantalla();
	}

	private void aplicarPrecioQr(BricodepotLineaTicket linea, BigDecimal importe) {
		linea.setPrecioTotalSinDto(importe);
		linea.setPrecioSinDto(importe);
		if(!linea.getPromociones().isEmpty()) {
			linea.getPromociones().clear();
		}
		Motivo motivo = new Motivo();
		motivo.setUidActividad(sesion.getAplicacion().getUidActividad());
		motivo.setCodigo("7");
		motivo.setComentario("Presupuesto");
		motivo.setDescripcion("OTROS");
		motivo.setCodigoTipo("2");
		motivo.setPrecioSinDtoAplicado(linea.getPrecioTotalSinDto());
		motivo.setPrecioSinDtoOriginal(linea.getPrecioTotalTarifaOrigen());
		linea.getMotivos().add(motivo);
	}

	@Override
	public void nuevoCodigoArticulo() {
		log.debug("nuevoCodigoArticulo()");
		String mensajeError = servicioArticulosQrPresupuesto.comprobarCodigoQrRecuperarArticulos(tfCodigoIntro.getText());

		if(mensajeError.equals("isArticulo")){
			// no dejar introducir líneas en un ticket nuevo si ha superado el importe de bloqueo de retirada
			if (tbLineas.getItems().size() == 0 && checkBloqueoRetirada()) {			
				tfCodigoIntro.clear();
				return;
			}
		
			// Validamos los datos
			if (!tfCodigoIntro.getText().trim().isEmpty()) {
				log.debug("nuevoCodigoArticulo() - Creando línea de artículo");

				frValidacion.setCantidad(tfCantidadIntro.getText().trim());
				frValidacion.setCodArticulo(tfCodigoIntro.getText().trim().toUpperCase());
				BigDecimal cantidad = frValidacion.getCantidadAsBigDecimal();
				tfCodigoIntro.clear();

				if (accionValidarFormulario() && cantidad != null && !BigDecimalUtil.isIgualACero(cantidad)) {
					log.debug("nuevoCodigoArticulo()- Formulario validado");

					// Si es prefijo de tarjeta fidelizacion, marcamos la venta como fidelizado y llamamos al REST
					if (Dispositivos.getInstance().getFidelizacion().isPrefijoTarjeta(frValidacion.getCodArticulo())) {

						ticketManager.recalcularConPromociones();
						refrescarDatosPantalla();

						Dispositivos.getInstance().getFidelizacion().cargarTarjetaFidelizado(frValidacion.getCodArticulo(), getStage(), new DispositivoCallback<FidelizacionBean>(){

							@Override
							public void onSuccess(FidelizacionBean tarjeta) {
								if (tarjeta.isBaja()) {
									VentanaDialogoComponent.crearVentanaError(I18N.getTexto("La tarjeta de fidelización {0} no está activa", tarjeta.getNumTarjetaFidelizado()), getStage());
									tarjeta = null;
									ticketManager.getTicket().getCabecera().setDatosFidelizado(tarjeta);
								}
								else {
									// Tarjeta válida - lo seteamos en el ticket
									ticketManager.getTicket().getCabecera().setDatosFidelizado(tarjeta);
								}

								ticketManager.recalcularConPromociones();
								refrescarDatosPantalla();

								/* BRICO-287 Edición de fidelizados con datos incompletos */
								consultaDatosIncompletosFidelizado();
								/* fin BRICO-287 */
							}

							@Override
							public void onFailure(Throwable e) {
								// Los errores se muestran desde el código del dispositivo
								// Quitamos los datos de fidelizado
								FidelizacionBean tarjeta = null;
								ticketManager.getTicket().getCabecera().setDatosFidelizado(tarjeta);
								ticketManager.recalcularConPromociones();
								refrescarDatosPantalla();
							}
						});
						return;
					}

					NuevoCodigoArticuloTask taskArticulo = SpringContext.getBean(NuevoCodigoArticuloTask.class, this, frValidacion.getCodArticulo(), cantidad); // anidada
					taskArticulo.start();
				}
			}
		}
		else {
			if (mensajeError.equals("qrValido")) {
				try {
					aplicarQr(tfCodigoIntro.getText());
				}
				catch (DocumentoException | PromocionesServiceException | TicketsServiceException e) {
					log.error("nuevoCodigoArticulo() - Ocurrió un error al utilizar el QR: " + e.getMessage());
				}
			}else if(mensajeError.equals("presupuestoEmpleado")) {
				VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto(("El presupuesto ya ha sido pagado, por favor, revise el presupuesto")), getStage());
				tfCodigoIntro.clear();			}
			else if(mensajeError.equals("presupuestoExpirado")){
				mostrarAviso("aviso.presupuesto.qr.fecha.expirada");
				tfCodigoIntro.clear();	
			}
			else {
			
				VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto(("QR erróneo, por favor introducir artículos manualmente")), getStage());
				tfCodigoIntro.clear();	
			}
		}
	}
	
	private void consultaDatosIncompletosFidelizado() {
		log.debug("consultaDatosIncompletosFidelizado() ");
		/* BRICO-287 Edición de fidelizados con datos incompletos */
		String apiKey = variablesServices.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
		String uidActividad = sesion.getAplicacion().getUidActividad();
		Long idFidelizado = ticketManager.getTicket().getCabecera().getDatosFidelizado().getIdFidelizado();

		FidelizadoRequestRest requestRest = new FidelizadoRequestRest(apiKey, uidActividad, idFidelizado);

		ConsultarFidelizadoPorIdTask consultarFidelizadoTask = SpringContext.getBean(ConsultarFidelizadoPorIdTask.class, requestRest, new RestBackgroundTask.FailedCallback<FidelizadoBean>(){

			@Override
			public void succeeded(FidelizadoBean result) {
				// check campos faltantes
				if (faltanCamposObligatoriosFidelizado(result)) {
					Boolean aceptar = false;
					// Si uno de los campos por completar es 'coletivo'
					if (result.getColectivos() == null || result.getColectivos().isEmpty()) {
						aceptar = VentanaDialogoComponent.crearVentanaConfirmacion(I18N.getTexto("Este fidelizado no está suscrito al Club. ¿Deseas registrarlo ahora? Hazlo añadiendo el colectivo en su ficha de fidelizado"), getStage());
					}
					else {
						aceptar = VentanaDialogoComponent.crearVentanaConfirmacion(I18N.getTexto("Campos obligatorios pendientes de cumplimentar. ¿Desea hacerlo ahora?"), getStage());
					}
					// Si el cliente ha decidido rellenar los campos ahora
					if (aceptar) {
						Map<String, Object> datos = new HashMap<String, Object>();
						datos.put(CodigoTarjetaController.PARAMETRO_ID_FIDELIZADO, result.getIdFidelizado());
						datos.put(CodigoTarjetaController.PARAMETRO_MODO, "EDICION_CAMPOS_OBLIGATORIOS_PENDIENTES");
						datos.put(TICKET_KEY, ticketManager);
						datos.put(PARAMETRO_NUMERO_TARJETA, ticketManager.getTicket().getCabecera().getDatosFidelizado().getNumTarjetaFidelizado());

						getApplication().getMainView().showActionView(AppConfig.accionFidelizado, (HashMap<String, Object>) datos);
					}
				}

				List<TiposContactoFidelizadoBean> listaContactos = result.getContactos();
				for (TiposContactoFidelizadoBean tiposContactoFidelizadoBean : listaContactos) {
					if (tiposContactoFidelizadoBean.getCodTipoCon().equalsIgnoreCase("EMAIL")) {
						ticketManager.getTicket().getCabecera().getDatosFidelizado().putAdicional("emailFidelizadoCargado", tiposContactoFidelizadoBean.getValor());
					}
				}
			}

			@Override
			public void failed(Throwable throwable) {
			}
		}, getStage());

		consultarFidelizadoTask.start();

		/* fin BRICO-287 */
	}
	
	
	/*Eliminar este metodo para activar el visor de la pantalla de cupones GAP-123 Quitar la pantalla de cupones antes de ir a pago*/
	@Override
	public void seeCustomerCoupons() {
		//super.seeCustomerCoupons();
	}
}