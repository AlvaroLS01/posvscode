package com.comerzzia.bricodepot.pos.gui.ventas.devoluciones;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.api.rest.client.exceptions.RestException;
import com.comerzzia.api.rest.client.exceptions.RestHttpException;
import com.comerzzia.bricodepot.pos.gui.motivos.MotivoController;
import com.comerzzia.bricodepot.pos.gui.motivos.MotivoView;
import com.comerzzia.bricodepot.pos.gui.ventas.devoluciones.mediosPago.BricoDepotVerMediosPagoView;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.BricodepotTicketManager;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.audit.RequestAuthorizationController;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.audit.RequestAuthorizationView;
import com.comerzzia.bricodepot.pos.persistence.motivos.Motivo;
import com.comerzzia.bricodepot.pos.services.ticket.BricodepotCabeceraTicket;
import com.comerzzia.bricodepot.pos.services.ticket.audit.TicketAuditEvent;
import com.comerzzia.bricodepot.pos.services.ticket.lineas.BricodepotLineaTicket;
import com.comerzzia.bricodepot.pos.util.AnticiposConstants;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.botonaccion.normal.BotonBotoneraNormalComponent;
import com.comerzzia.pos.core.gui.componentes.botonaccion.simple.BotonBotoneraSimpleComponent;
import com.comerzzia.pos.core.gui.componentes.botonera.BotoneraComponent;
import com.comerzzia.pos.core.gui.componentes.botonera.ConfiguracionBotonBean;
import com.comerzzia.pos.core.gui.componentes.botonera.PanelBotoneraBean;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.exception.CargarPantallaException;
import com.comerzzia.pos.core.gui.permisos.exception.SinPermisosException;
import com.comerzzia.pos.gui.ventas.devoluciones.IntroduccionArticulosController;
import com.comerzzia.pos.gui.ventas.tickets.TicketManager;
import com.comerzzia.pos.gui.ventas.tickets.articulos.FacturacionArticulosController;
import com.comerzzia.pos.gui.ventas.tickets.articulos.FormularioLineaArticuloBean;
import com.comerzzia.pos.gui.ventas.tickets.articulos.edicion.EdicionArticuloController;
import com.comerzzia.pos.gui.ventas.tickets.articulos.edicion.EdicionArticuloView;
import com.comerzzia.pos.gui.ventas.tickets.pagos.PagosController;
import com.comerzzia.pos.gui.ventas.tickets.pagos.PagosView;
import com.comerzzia.pos.persistence.giftcard.GiftCardBean;
import com.comerzzia.pos.services.cajas.CajaEstadoException;
import com.comerzzia.pos.services.cajas.CajasServiceException;
import com.comerzzia.pos.services.core.documentos.DocumentoException;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.sesion.SesionInitException;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.services.giftcard.GiftCardService;
import com.comerzzia.pos.services.promociones.PromocionesServiceException;
import com.comerzzia.pos.services.ticket.TicketVentaAbono;
import com.comerzzia.pos.services.ticket.lineas.ILineaTicket;
import com.comerzzia.pos.services.ticket.lineas.LineaTicket;
import com.comerzzia.pos.services.ticket.lineas.LineaTicketException;
import com.comerzzia.pos.util.bigdecimal.BigDecimalUtil;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.i18n.I18N;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import rest.bean.anticipo.Anticipo;
import rest.client.anticipo.RestAnticiposClient;

@Component
@Primary
@SuppressWarnings("unchecked")
public class BricodepotIntroduccionArticulosController extends IntroduccionArticulosController {

	private static final Logger log = Logger.getLogger(BricodepotIntroduccionArticulosController.class.getName());


	public static final String POS_ARTICULO_ANTICIPO = "POS.ARTICULO_ANTICIPO";


	private static final String DOC_FLEX = "FLEX";

	@Autowired
	private VariablesServices variablesServices;
	@Autowired
	private Sesion sesion;
	@Autowired
	private GiftCardService giftCardService;

	@Override
	public void initializeForm() throws InitializeGuiException {

		log.debug("initializeForm() - Inicializando formulario...");
		try {
			tfCodigoIntro.setText("");
			tbLineas.getSelectionModel().clearSelection();

			boolean cajaAbierta = sesion.getSesionCaja().isCajaAbierta();
			comprobarAperturaPantalla();

			if (cajaAbierta) {
				HashMap<String, Object> datos = getView().getParentView().getController().getDatos();
				if (datos.containsKey(TICKET_KEY)) {
					ticketManager = (TicketManager) datos.get(TICKET_KEY);
					if (!((BricodepotTicketManager) ticketManager).isConversionFlexpointFT()) {
						ocultarBotonesParaConversionFlexPoint();
						boolean esFlexpoint = ((BricodepotTicketManager) ticketManager).esDevolucionFlexpoint();
						if (!ticketManager.isTicketAbierto()) {
							try {
								ticketManager.nuevoTicket();
								((BricodepotTicketManager) ticketManager).setEsDevolucionFlexpoint(esFlexpoint);
								ticketManager.setEsDevolucion(true);

								log.debug("initializeForm() - Se va a cambiar el documento activo. Documento activo actual " + ticketManager.getDocumentoActivo());
								ticketManager.setDocumentoActivo(documentos.getDocumentoAbono(ticketManager.getTicketOrigen().getCabecera().getCodTipoDocumento()));
							}
							catch (PromocionesServiceException | DocumentoException ex) {
								log.error("initializeForm() - Error inicializando ticket", ex);
								VentanaDialogoComponent.crearVentanaError(getStage(), ex.getMessageI18N(), ex);
							}
						}
						else {
							if (!sesion.getSesionCaja().getCajaAbierta().getUidDiarioCaja().equals(ticketManager.getTicket().getCabecera().getUidDiarioCaja())) {
								try {
									ticketManager.nuevoTicket();
									((BricodepotTicketManager) ticketManager).setEsDevolucionFlexpoint(esFlexpoint);
									ticketManager.setEsDevolucion(true);
									ticketManager.setDocumentoActivo(documentos.getDocumentoAbono(ticketManager.getTicketOrigen().getCabecera().getCodTipoDocumento()));
								}
								catch (PromocionesServiceException | DocumentoException ex) {
									log.error("initializeForm() - Error inicializando ticket", ex);
									VentanaDialogoComponent.crearVentanaError(getStage(), ex.getMessageI18N(), ex);
								}
							}
							ticketManager.setEsDevolucion(true);
						}
					}else {
						ocultarBotonesParaConversionFlexPoint();
					}
				}
						if (tipoDocumentoInicial == null) {
							tipoDocumentoInicial = documentos.getDocumento(((TicketVentaAbono) ticketManager.getTicket()).getCabecera().getTipoDocumento());

						}
					
				if (ticketManager == null) {
					log.error("initializeForm() -----No se ha inicializado el ticket manager-----");
					throw new InitializeGuiException();
				}

				// Comprobamos si la operación es una devolución de tarjeta regalo
				if (ticketManager.esDevolucionRecargaTarjetaRegalo()) {
					GiftCardBean tarjeta = giftCardService.getGiftCard(ticketManager.getTicketOrigen().getCabecera().getTarjetaRegalo().getNumTarjetaRegalo());
					// Comprobamos si la tarjeta en cuestión está dada de baja
					if (tarjeta != null) {
						if (tarjeta.isBaja()) {
							VentanaDialogoComponent.crearVentanaConfirmacion(I18N.getTexto("La tarjeta introducida está dada de baja."), getStage());
						}
					}
					else {
						VentanaDialogoComponent.crearVentanaConfirmacion(I18N.getTexto("El número de tarjeta no es válido."), getStage());
					}
				}

				if (ticketManager.getTicket() != null && ticketManager.getTicket().getLineas() != null && !ticketManager.getTicket().getLineas().isEmpty()) {
					escribirUltimaLineaEnVisor();
				}
				refrescarDatosPantalla();
			}
		}
		catch (CajaEstadoException | CajasServiceException e) {
			log.error("initializeForm() - Error de caja : " + e.getMessageI18N());
			throw new InitializeGuiException(e.getMessageI18N(), e);
		}
		catch (RestException | RestHttpException e) {
			log.error("initializeForm() - Error inesperado inicializando formulario. " + e.getMessage(), e);
			throw new InitializeGuiException(I18N.getTexto("Ha ocurrido un error al conectar con el servidor para consultar la tarjeta regalo"), e);
		}
		catch (InitializeGuiException e) {
			throw e;
		}
		catch (Exception e) {
			log.error("initializeForm() - Error inesperado inicializando formulario. " + e.getMessage(), e);
			throw new InitializeGuiException(e);
		}

		/* [BRICO-78] - Devoluciones de tickets de Flexpoint */
		if (!((BricodepotTicketManager) ticketManager).esDevolucionFlexpoint()) {
			if (getView().getParentView().getController().getDatos().get("diasSuperados") != null) {
				if ((boolean) getView().getParentView().getController().getDatos().get("diasSuperados")) {
					VentanaDialogoComponent.crearVentanaAviso("El periodo de devolución se ha excedido", getStage());
				}
			}
		}
	}
	
	public void abrirMediosPago() {
		TicketVentaAbono ticketVenta = (TicketVentaAbono) ticketManager.getTicketOrigen();
		getDatos().put(TICKET_KEY, ticketVenta);
		getApplication().getMainView().showModalCentered(BricoDepotVerMediosPagoView.class, getDatos(), getStage());
	}

	@Override
	protected BricodepotLineaTicket nuevaLineaArticulo(String codart, String desglose1, String desglose2, BigDecimal cantidad) throws LineaTicketException {
		BricodepotLineaTicket linea = null;
		if (!((BricodepotTicketManager) ticketManager).esDevolucionFlexpoint() && !((BricodepotTicketManager) ticketManager).isConversionFlexpointFT()) {
			linea = (BricodepotLineaTicket) super.nuevaLineaArticulo(codart, desglose1, desglose2, cantidad);
			tratarMotivos(linea);
		}else if (((BricodepotTicketManager) ticketManager).isConversionFlexpointFT()){
			linea = (BricodepotLineaTicket) ticketManager.nuevaLineaArticulo(codart, desglose1, desglose2, cantidad, null);
		}
		else {
			linea = (BricodepotLineaTicket) ((BricodepotTicketManager) ticketManager).nuevaLineaArticuloFlexpoint(codart, desglose1, desglose2, cantidad, getStage(), false, true);
			tratarMotivos(linea);
		}
	
		return linea;
	}
	
	public void tratarMotivos(BricodepotLineaTicket linea) {
		log.debug("nuevaLineaArticulo() - Mostrando ventana modal de motivos");
		HashMap<String, Object> datosVentana = new HashMap<String, Object>();
		datosVentana.put(MotivoController.PARAMETRO_LINEA, linea);
		// Motivo devolucion
		datosVentana.put(MotivoController.PARAMETRO_TIPO_MOTIVO, MotivoController.CODIGO_TIPO_DEVOLUCION);
		getApplication().getMainView().showModalCentered(MotivoView.class, datosVentana, getStage());
		
		Motivo mot = (Motivo) datosVentana.get(MotivoController.PARAMETRO_MOTIVO);
		if (linea.getMotivos() == null) {
			linea.setMotivos(new ArrayList<Motivo>());
		}
		if(mot != null) {
			linea.getMotivos().add(mot);
		}

		log.debug("nuevaLineaArticulo() - Volviendo de la pantalla modal");
	}

	@Override
	public void abrirPagos() {
		log.trace("abrirPagos()");
		if (!ticketManager.isTicketVacio()) { // Si el ticket no es vacío se puede aparcar
			if (validarTicket()) {
				log.debug("abrirPagos() - El ticket tiene líneas");
				if (((BricodepotTicketManager) ticketManager).isConversionFlexpointFT()) {
					super.abrirPagos();
				}
				else {
					if (compruebaExisteMotivo()) {

						TicketAuditEvent auditEvent = TicketAuditEvent.forEvent(TicketAuditEvent.Type.DEVOLUCION, sesion);
						abrirVentanaAutorizacion(auditEvent, datos);

						if (datos.get(RequestAuthorizationController.PERMITIR_ACCION).equals(true)) {
							log.debug("abrirPagos() - Se permite accion");
							String codAnticipo = variablesServices.getVariableAsString(POS_ARTICULO_ANTICIPO);
							List<LineaTicket> listaLineas = ticketManager.getTicket().getLineas();
							Boolean existeAnticipo = Boolean.FALSE;
							for (LineaTicket lineaTicket : listaLineas) {
								if (lineaTicket.getCodArticulo().equalsIgnoreCase(codAnticipo)) {
									existeAnticipo = Boolean.TRUE;
								}
							}

							if (listaLineas.size() > 1 && existeAnticipo) {
								log.debug("abrirPagos() - No se pueden devolver anticipos junto con las demás líneas");
								VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("No se pueden devolver anticipos junto con las demás líneas"), getStage());
								return;
							}
							else {
								if (!existeAnticipo || anticipoAptoDevolucion(((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).getNumAnticipo())
								        || ((BricodepotTicketManager) ticketManager).esDevolucionFlexpoint()) {
									getDatos().put(TICKET_KEY, ticketManager);
									getDatos().put(PagosController.TIPO_DOC_INICIAL, tipoDocumentoInicial);

									/*
									 * En este punto, si existeAnticipo significa que la única línea que hay es la del
									 * anticipo, por lo que estamos en el caso de querer devolver un anticipo que
									 * todavía no se ha usado
									 */
									if (existeAnticipo) {
										((BricodepotLineaTicket) ticketManager.getTicket().getLineas().get(0)).setIsAnticipo(true);
									}
									getApplication().getMainView().showModal(PagosView.class, getDatos());
									try {
										getView().resetSubViews();
										if (getDatos().containsKey(PagosController.ACCION_CANCELAR)) {
											initializeForm();
										}
										else {
											try {
												ticketManager.nuevoTicket();
												ticketManager.setEsDevolucion(true);

												log.debug("abrirPagos() - Se va a cambiar el documento activo. Documento activo actual " + ticketManager.getDocumentoActivo());
												ticketManager.setDocumentoActivo(documentos.getDocumentoAbono(ticketManager.getTicketOrigen().getCabecera().getCodTipoDocumento()));
											}
											catch (Exception e) {
												log.error("abrirPagos() - Ha habido un error al inicializar un nuevo ticket para borrar la copia de seguridad: " + e.getMessage(), e);
											}
											getView().getParentView().loadAndInitialize();
										}
									}
									catch (InitializeGuiException e) {
										VentanaDialogoComponent.crearVentanaError(getStage(), e);
									}
								}
								else {
									log.debug("abrirPagos() - El anticipo no se puede devolver porque ya ha sido liquidado/devuelto");
									VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("El anticipo no se puede devolver porque ya ha sido liquidado/devuelto"), getStage());
									return;
								}
							}
						}
						else {
							log.debug("abrirPagos() - No se permite accion");
						}
					}
					else {
						log.warn("abrirPagos() - Faltan motivos de devolución");
						VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("Es necesario indicar los motivos de devolución de una o varias líneas"), getStage());
						List<BricodepotLineaTicket> lineasDevolucion = ticketManager.getTicket().getLineas();
						for (BricodepotLineaTicket lineaDev : lineasDevolucion) {
							if(BigDecimalUtil.isMenorACero(lineaDev.getCantidad()) && (lineaDev.getMotivos() == null || lineaDev.getMotivos().isEmpty())){
								tratarMotivos(lineaDev);
							}
						}
					}
				}
			}
		}
		else {
			log.warn("abrirPagos() - Ticket vacío");
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("El ticket no contiene líneas de artículo."), this.getStage());
		}
	}

	private boolean compruebaExisteMotivo() {
		List<BricodepotLineaTicket> lineasDevolucion = ticketManager.getTicket().getLineas();
		for (BricodepotLineaTicket lineaDev : lineasDevolucion) {
			if(lineaDev.getMotivos() == null || lineaDev.getMotivos().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private boolean anticipoAptoDevolucion(String numAnticipo) {
		Boolean resultado = Boolean.FALSE;
		String apiKey = variablesServices.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
		String uidActividad = sesion.getAplicacion().getUidActividad();
		try {
			Anticipo anticipo = RestAnticiposClient.getAnticipo(apiKey, uidActividad, numAnticipo);
			if (AnticiposConstants.PARAMETRO_ESTADO_DISPONIBLE.equals(anticipo.getEstado())) {
				resultado = true;
			}
		}
		catch (Exception e) {
			log.error("anticipoAptoDevolucion() - El anticipo no se puede devolver porque ya ha sido liquidado/devuelto", e);
		}

		return resultado;
	}

	/* [BRICO-78] - Devoluciones de tickets de Flexpoint */
	@Override
	protected void introducirNuevoArticulo(String codArticulo, String desglose1, String desglose2, BigDecimal cantidad) {
		log.debug("introducirNuevoArticulo() - Añadiendo artículo con código: " + codArticulo);
		if (!((BricodepotTicketManager) ticketManager).isConversionFlexpointFT()) {
			if (((BricodepotTicketManager) ticketManager).esDevolucionFlexpoint()) {
				mostrarMotivosNuevoArticulo(codArticulo, desglose1, desglose2, cantidad);
			}
			else {
				if (VentanaDialogoComponent.crearVentanaConfirmacion(I18N.getTexto("Se va a introducir un artículo que no estaba en la venta original. ¿Está seguro?"), getStage())) {
					mostrarMotivosNuevoArticulo(codArticulo, desglose1, desglose2, cantidad);
				}
			}
		}
	}

	private void mostrarMotivosNuevoArticulo(String codArticulo, String desglose1, String desglose2, BigDecimal cantidad) {
		log.debug("mostrarMotivosNuevoArticulo() - Mostrando pantalla para añadir los motivos del artículo a añadir.");
		try {
			BricodepotLineaTicket linea = (BricodepotLineaTicket) ((BricodepotTicketManager) ticketManager).nuevaLineaArticuloFlexpoint(codArticulo, desglose1, desglose2, cantidad, getStage(), false,
			        true);

			HashMap<String, Object> datosVentana = new HashMap<String, Object>();
			datosVentana.put(MotivoController.PARAMETRO_LINEA, (BricodepotLineaTicket) ticketManager.getTicket().getLineas().get((ticketManager.getTicket().getLineas().size()) - 1));
			// Motivo devolucion
			datosVentana.put(MotivoController.PARAMETRO_TIPO_MOTIVO, MotivoController.CODIGO_TIPO_DEVOLUCION);
			getApplication().getMainView().showModalCentered(MotivoView.class, datosVentana, getStage());
			refrescarDatosPantalla();
		}
		catch (LineaTicketException e) {
			log.error("mostrarMotivosNuevoArticulo() - Error mostrando pantalla para añadir los motivos del artículo a añadir.", e);
		}
	}

	@Override
	protected void accionTablaEditarRegistro() {
		try {
			log.debug("accionTablaEditarRegistro() - Acción ejecutada");
			super.compruebaPermisos(PERMISO_MODIFICAR_LINEA);
			if (tbLineas.getItems() != null && getLineaSeleccionada() != null) {
				int linea = getLineaSeleccionada().getIdLinea();
				if (linea > 0) {
					ILineaTicket lineaTicket = ticketManager.getTicket().getLinea(linea);
					if (BigDecimalUtil.isMayorACero(lineaTicket.getCantidad()) || ((BricodepotTicketManager) ticketManager).esDevolucionFlexpoint()) {
						// Creamos la ventana de edición de artículos
						HashMap<String, Object> parametrosEdicionArticulo = new HashMap<>();
						parametrosEdicionArticulo.put(EdicionArticuloController.CLAVE_PARAMETRO_ARTICULO, lineaTicket);
						parametrosEdicionArticulo.put(EdicionArticuloController.CLAVE_APLICAR_PROMOCIONES, false);
						parametrosEdicionArticulo.put(FacturacionArticulosController.TICKET_KEY, ticketManager);
						getApplication().getMainView().showModalCentered(EdicionArticuloView.class, parametrosEdicionArticulo, this.getStage());

						((LineaTicket) lineaTicket).setCantidad(ticketManager.tratarSignoCantidad(lineaTicket.getCantidad(), ticketManager.getTicket().getCabecera().getCodTipoDocumento()));
						((LineaTicket) lineaTicket).recalcularImporteFinal();
						ticketManager.recalcularConPromociones();

						refrescarDatosPantalla();
					}
					else {
						VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("No se puede editar una línea con cantidad negativa."), getStage());
					}
				}
			}
		}
		catch (SinPermisosException ex) {
			log.debug("accionTablaEditarRegistro() - El usuario no tiene permisos para modificar línea");
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("No tiene permisos para modificar una línea."), getStage());
		}
	}

	@Override
	protected boolean tienePermisosCambioArticulo() {
		boolean tienePermisosCambio = false;
		try {
			if (!tipoDocumentoInicial.isSignoPositivo() || ((BricodepotTicketManager) ticketManager).esDevolucionFlexpoint() || 
					((BricodepotTicketManager) ticketManager).getTicket().getCabecera().getDatosDocOrigen().getCodTipoDoc().equals(DOC_FLEX)) {
				compruebaPermisos(PERMISO_CAMBIAR_ARTICULOS);
				tienePermisosCambio = true;
			}
			else {
				log.debug("No es posible añadir una linea de venta a una devolución con el signo forzado a positivo.");
			}
		}
		catch (SinPermisosException e) {
		}
		return tienePermisosCambio;
	}

	@Override
	protected boolean tienePermisosIntroducirNuevoArticulo() {
		boolean tienePermisosCambio = false;
		try {
			if (!tipoDocumentoInicial.isSignoPositivo() || ((BricodepotTicketManager) ticketManager).esDevolucionFlexpoint()) {
				compruebaPermisos(PERMISO_INTRODUCIR_ARTICULOS_NUEVOS);
				tienePermisosCambio = true;
			}
			else {
				log.debug("No es posible añadir una linea de venta a una devolución con el signo forzado a positivo.");
			}
		}
		catch (SinPermisosException e) {
		}
		return tienePermisosCambio;
	}

	protected void abrirVentanaAutorizacion(TicketAuditEvent auditEvent, HashMap<String, Object> datos) {
		log.debug("abrirVentanaAutorizacion() - Inicio del proceso de auditoria");
		List<TicketAuditEvent> events = new ArrayList<>();
		events.add(auditEvent);
		datos.put(RequestAuthorizationController.AUDIT_EVENT, events);
		datos.put(FacturacionArticulosController.TICKET_KEY, ticketManager);
		getApplication().getMainView().showModalCentered(RequestAuthorizationView.class, datos, this.getStage());
	}
	
	
	public void ocultarBotonesParaConversionFlexPoint() {
		if(((BricodepotTicketManager)ticketManager).isConversionFlexpointFT()) {
			botonera.setAccionVisible("verDocumentoOrigen",false);
			botonera.setAccionVisible("abrirMediosPago",false);
			botonera.setAccionVisible("abrirGestionTickets",false);
			botonera.setAccionVisible("abrirBusquedaArticulos",false);
			botonera.setAccionVisible("abrirGestionTickets",false);
			botonera.setAccionVisible("cambiarCajero",false);
		}else {
			botonera.setAccionVisible("verDocumentoOrigen",true);
			botonera.setAccionVisible("abrirMediosPago",true);
			botonera.setAccionVisible("abrirGestionTickets",true);
			botonera.setAccionVisible("abrirBusquedaArticulos",true);
			botonera.setAccionVisible("abrirGestionTickets",true);
			botonera.setAccionVisible("cambiarCajero",true);
		}
	}
	
	protected void abrirVentanaPagos() {
		getApplication().getMainView().showModal(PagosView.class, getDatos());
	}
	
	@Override
	public void initializeComponents() throws InitializeGuiException {

		try {
			if (getDatos().get(FacturacionArticulosController.TICKET_KEY) != null) {
				ticketManager = (TicketManager) getDatos().get(FacturacionArticulosController.TICKET_KEY);
			}else {
				ticketManager = SpringContext.getBean(TicketManager.class);
				ticketManager.init();
			}
			initTecladoNumerico(tecladoNumerico);

			log.debug("inicializarComponentes() - Inicialización de componentes...");

			log.debug("inicializarComponentes() - Carga de acciones de botonera inferior");

			PanelBotoneraBean panelBotoneraBean = getView().loadBotonera();
			botonera = new BotoneraComponent(panelBotoneraBean, panelBotonera.getPrefWidth(), panelBotonera.getPrefHeight(), this, BotonBotoneraNormalComponent.class);
			panelBotonera.getChildren().add(botonera);

			// Botonera de Tabla
			log.debug("inicializarComponentes() - Carga de acciones de botonera de tabla de ventas");
			List<ConfiguracionBotonBean> listaAccionesAccionesTabla = cargarAccionesTabla();
			botoneraAccionesTabla = new BotoneraComponent(1, listaAccionesAccionesTabla.size(), this, listaAccionesAccionesTabla, panelMenuTabla.getPrefWidth(), panelMenuTabla.getPrefHeight(),
			        BotonBotoneraSimpleComponent.class.getName());
			panelMenuTabla.getChildren().add(botoneraAccionesTabla);

			log.debug("inicializarComponentes() - registrando acciones de la tabla principal");
			crearEventoEnterTabla(tbLineas);
			crearEventoNegarTabla(tbLineas);
			crearEventoEliminarTabla(tbLineas);
			crearEventoNavegacionTabla(tbLineas);

			log.debug("inicializarComponentes() - Configuración de la tabla");
			if (sesion.getAplicacion().isDesglose1Activo()) { // Si hay desglose 1, establecemos el texto
				tcLineasDesglose1.setText(I18N.getTexto(variablesServices.getVariableAsString(VariablesServices.ARTICULO_DESGLOSE1_TITULO)));
			}
			else { // si no hay desgloses, compactamos la línea
				tcLineasDesglose1.setVisible(false);
			}
			if (sesion.getAplicacion().isDesglose2Activo()) { // Si hay desglose 1, establecemos el texto
				tcLineasDesglose2.setText(I18N.getTexto(variablesServices.getVariableAsString(VariablesServices.ARTICULO_DESGLOSE2_TITULO)));
			}
			else { // si no hay desgloses, compactamos la línea
				tcLineasDesglose2.setVisible(false);
			}

			frValidacionBusqueda = SpringContext.getBean(FormularioLineaArticuloBean.class);
			frValidacionBusqueda.setFormField("cantidad", tfCantidadIntro);
			// Inicializamos los formularios

			frValidacion = new FormularioLineaArticuloBean();
			frValidacion.setFormField("codArticulo", tfCodigoIntro);
			frValidacion.setFormField("cantidad", tfCantidadIntro);

			registraEventoTeclado(new EventHandler<KeyEvent>(){

				@Override
				public void handle(KeyEvent event) {
					if (event.getCode() == KeyCode.MULTIPLY) {
						cambiarCantidad();
					}
				}
			}, KeyEvent.KEY_RELEASED);

			addSeleccionarTodoCampos();

		}
		catch (CargarPantallaException | SesionInitException ex) {
			log.error("inicializarComponentes() - Error inicializando pantalla de venta de artículos");
			VentanaDialogoComponent.crearVentanaError("Error cargando pantalla. Para mas información consulte el log.", getStage());
		}
	}
	
	@Override
	protected void checkDocumentType() {
	
		super.checkDocumentType();
		
		if (((BricodepotTicketManager)ticketManager).isConversionFlexpointFT()) {
			try {
				ticketManager.setDocumentoActivo(sesion.getAplicacion().getDocumentos().getDocumento("FT"));
			}
			catch (DocumentoException e) {
				log.error("checkDocumentType() - Ha ocurrido un error en la conversion a ft estableciendo el tipo de documento : "+e.getMessage());
			}
		}
	}
}
