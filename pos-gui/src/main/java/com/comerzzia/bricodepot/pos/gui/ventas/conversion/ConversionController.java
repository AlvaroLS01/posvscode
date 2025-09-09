package com.comerzzia.bricodepot.pos.gui.ventas.conversion;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.validation.ConstraintViolation;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.gui.ventas.conversion.datosadicionales.PantallaDatosAdicionalesView;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.BricodepotTicketManager;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.audit.RequestAuthorizationController;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.audit.RequestAuthorizationView;
import com.comerzzia.bricodepot.pos.services.ticket.BricodepotCabeceraTicket;
import com.comerzzia.bricodepot.pos.services.ticket.BricodepotTicketVentaAbono;
import com.comerzzia.bricodepot.pos.services.ticket.audit.TicketAuditEvent;
import com.comerzzia.bricodepot.posservices.client.ConversionApi;
import com.comerzzia.bricodepot.posservices.client.model.Conversion;
import com.comerzzia.core.servicios.api.ComerzziaApiManager;
import com.comerzzia.core.servicios.sesion.DatosSesionBean;
import com.comerzzia.core.util.base64.Base64Coder;
import com.comerzzia.pos.core.dispositivos.Dispositivos;
import com.comerzzia.pos.core.dispositivos.dispositivo.visor.IVisor;
import com.comerzzia.pos.core.gui.BackgroundTask;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.controllers.Controller;
import com.comerzzia.pos.core.gui.validation.ValidationUI;
import com.comerzzia.pos.gui.ventas.devoluciones.FormularioConsultaTicketBean;
import com.comerzzia.pos.gui.ventas.devoluciones.IntroduccionArticulosView;
import com.comerzzia.pos.gui.ventas.tickets.TicketManager;
import com.comerzzia.pos.gui.ventas.tickets.articulos.FacturacionArticulosController;
import com.comerzzia.pos.gui.ventas.tickets.factura.FacturaView;
import com.comerzzia.pos.persistence.core.documentos.tipos.TipoDocumentoBean;
import com.comerzzia.pos.persistence.fidelizacion.FidelizacionBean;
import com.comerzzia.pos.persistence.tickets.datosfactura.DatosFactura;
import com.comerzzia.pos.services.cajas.CajaEstadoException;
import com.comerzzia.pos.services.cajas.CajasServiceException;
import com.comerzzia.pos.services.core.documentos.DocumentoException;
import com.comerzzia.pos.services.core.documentos.Documentos;
import com.comerzzia.pos.services.core.menu.MenuService;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.services.devices.DeviceException;
import com.comerzzia.pos.services.fiscaldata.FiscalDataException;
import com.comerzzia.pos.services.mediospagos.MediosPagosService;
import com.comerzzia.pos.services.promociones.Promocion;
import com.comerzzia.pos.services.promociones.PromocionesServiceException;
import com.comerzzia.pos.services.ticket.ITicket;
import com.comerzzia.pos.services.ticket.Ticket;
import com.comerzzia.pos.services.ticket.TicketVenta;
import com.comerzzia.pos.services.ticket.TicketVentaAbono;
import com.comerzzia.pos.services.ticket.TicketsService;
import com.comerzzia.pos.services.ticket.TicketsServiceException;
import com.comerzzia.pos.services.ticket.cabecera.DatosDocumentoOrigenTicket;
import com.comerzzia.pos.services.ticket.cabecera.SubtotalIvaTicket;
import com.comerzzia.pos.services.ticket.cupones.CuponEmitidoTicket;
import com.comerzzia.pos.services.ticket.lineas.LineaTicket;
import com.comerzzia.pos.services.ticket.pagos.IPagoTicket;
import com.comerzzia.pos.services.ticket.pagos.PagoTicket;
import com.comerzzia.pos.services.ticket.pagos.tarjeta.DatosPeticionPagoTarjeta;
import com.comerzzia.pos.services.ticket.pagos.tarjeta.DatosRespuestaPagoTarjeta;
import com.comerzzia.pos.servicios.impresion.ServicioImpresion;
import com.comerzzia.pos.util.bigdecimal.BigDecimalUtil;
import com.comerzzia.pos.util.config.AppConfig;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.format.FormatUtil;
import com.comerzzia.pos.util.i18n.I18N;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

@SuppressWarnings({ "unchecked", "rawtypes"})
@Component
public class ConversionController extends Controller {

	private static final Logger log = Logger.getLogger(ConversionController.class);

	public static final String CONVERSION = "CONVERSION";
	private static final String COD_PAIS_PORTUGAL = "PT";
	private static final String COD_PAIS_ESPANA = "ES";
	public static final String PLANTILLA_CUPON = "cupon_promocion";
	public static final String PLANTILLA_VALE = "vale";

	final IVisor visor = Dispositivos.getInstance().getVisor();

	@Autowired
	private Sesion sesion;
	
	@Autowired
	private VariablesServices variablesServices;

	@FXML
	protected TextField tfOperacion, tfTienda, tfCodCaja, tfCodDoc, tfDesDoc;

	@FXML
	protected Label lbMensajeError;

	@FXML
	protected Button btAceptar;

	protected FormularioConsultaTicketBean frConsultaTicket;

	protected TicketManager ticketManager;

	@Autowired
	protected Documentos documentos;

	@Autowired
	protected TicketsService ticketsService;
	
	@Autowired
	private MediosPagosService mediosPagosService;
	
	@Autowired 
	protected MenuService menuService;
	
    @Autowired
    protected ComerzziaApiManager comerzziaApiManager;

	@Override
	public void initialize(URL url, ResourceBundle rb) {

		frConsultaTicket = SpringContext.getBean(FormularioConsultaTicketBean.class);

		frConsultaTicket.setFormField("codTienda", tfTienda);
		frConsultaTicket.setFormField("codOperacion", tfOperacion);
		frConsultaTicket.setFormField("codCaja", tfCodCaja);
		frConsultaTicket.setFormField("tipoDoc", tfCodDoc);
	}

	@Override
	public void initializeComponents() {

		tfCodDoc.focusedProperty().addListener(new ChangeListener<Boolean>(){

			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
				if (oldValue) {
					procesarTipoDoc();
				}
			}
		});
		addSeleccionarTodoEnFoco(tfOperacion);
		addSeleccionarTodoEnFoco(tfCodCaja);
		addSeleccionarTodoEnFoco(tfTienda);
		addSeleccionarTodoEnFoco(tfCodDoc);
	}

	@Override
	public void initializeForm() throws InitializeGuiException {
		log.debug("initializeForm()");
		ticketManager = SpringContext.getBean(TicketManager.class);

		// Realizamos las comprobaciones de apertura automática de caja y de cierre de caja obligatorio
		try {
			comprobarAperturaPantalla();
		}
		catch (CajasServiceException | CajaEstadoException e) {
			log.error("initializeForm() - Error inicializando pantalla:" + e.getMessageI18N(), e);
			throw new InitializeGuiException(e.getMessageI18N(), e);
		}

		visor.escribirLineaArriba(I18N.getTexto("--NUEVA DEVOLUCION--"));

		tfTienda.setText(sesion.getAplicacion().getTienda().getCodAlmacen());
		tfCodCaja.setText(sesion.getAplicacion().getCodCaja());
		tfOperacion.setText("");

		List<String> tiposDocumentoAbonables = documentos.getTiposDocumentoAbonables();
		if (tiposDocumentoAbonables.isEmpty()) {
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("No está configurado el tipo de documento nota de crédito en el entorno."), this.getStage());
			btAceptar.setDisable(true);
		}
		else {
			btAceptar.setDisable(false);
		}

		for (String tipoDoc : tiposDocumentoAbonables) {
			try {
				if (documentos.getDocumento(tipoDoc) != null) {
					TipoDocumentoBean docPreseleccion = documentos.getDocumento(tipoDoc);
					tfCodDoc.setText(docPreseleccion.getCodtipodocumento());
					tfDesDoc.setText(docPreseleccion.getDestipodocumento());
					break;
				}
			}
			catch (DocumentoException ex) {
				log.error("initializeForm() - No se ha encontrado el documento asociado: " + ex, ex);
			}
		}

		lbMensajeError.setText("");
	}

	/**
	 * Realiza las comprobaciones de apertura automática de caja y de cierre de caja obligatorio
	 * 
	 * @throws CajasServiceException
	 * @throws CajaEstadoException
	 * @throws InitializeGuiException
	 */
	protected void comprobarAperturaPantalla() throws CajasServiceException, CajaEstadoException, InitializeGuiException {
		if (!sesion.getSesionCaja().isCajaAbierta()) {
			Boolean aperturaAutomatica = variablesServices.getVariableAsBoolean(VariablesServices.CAJA_APERTURA_AUTOMATICA, true);
			if (aperturaAutomatica) {
				VentanaDialogoComponent.crearVentanaInfo(I18N.getTexto("No hay caja abierta. Se abrirá automáticamente."), getStage());
				sesion.getSesionCaja().abrirCajaAutomatica();
			}
			else {
				VentanaDialogoComponent.crearVentanaError(I18N.getTexto("No hay caja abierta. Deberá ir a la gestión de caja para abrirla."), getStage());
				throw new InitializeGuiException(false);
			}
		}

		if (!ticketManager.comprobarCierreCajaDiarioObligatorio()) {
			String fechaCaja = FormatUtil.getInstance().formateaFecha(sesion.getSesionCaja().getCajaAbierta().getFechaApertura());
			String fechaActual = FormatUtil.getInstance().formateaFecha(new Date());
			VentanaDialogoComponent.crearVentanaError(I18N.getTexto("No se puede realizar la venta. El día de apertura de la caja {0} no coincide con el del sistema {1}", fechaCaja, fechaActual),
			        getStage());
			throw new InitializeGuiException(false);
		}
	}

	@Override
	public void initializeFocus() {
		tfOperacion.requestFocus();
	}

	@FXML
	public void keyReleased(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER && !btAceptar.isDisable()) {
			accionAceptar();
		}
	}

	@FXML
	public void accionAceptar() {
		lbMensajeError.setText("");
		if (validarFormularioConsultaCliente()) {

			ticketManager = SpringContext.getBean(TicketManager.class);
			String codTienda = frConsultaTicket.getCodTienda();
			String codCaja = frConsultaTicket.getCodCaja();
			String codigo = frConsultaTicket.getCodOperacion();
			String codDoc = frConsultaTicket.getCodDoc();

			try {
				if (ticketManager.comprobarConfigContador(documentos.getDocumentoAbono(codDoc).getCodtipodocumento())) {
					Long idTipoDocumento = documentos.getDocumento(codDoc).getIdTipoDocumento();
					new RecuperarTicketConversion(codigo, codTienda, codCaja, idTipoDocumento).start();
				}
				else {
					ticketManager.crearVentanaErrorContador(getStage());
				}
			}
			catch (DocumentoException e) {
				VentanaDialogoComponent.crearVentanaError(getStage(), String.format(I18N.getTexto("El documento %s no se ha encontrado"), codDoc), e);
			}
		}
	}

	private void convertirFSaFT() {
		log.debug("convertirFSaFT() - convirtiendo de FS a FT");
		if(!comprobarPagosTicketParcial()) {
			abrirVentanaDatosFactura(false);
		}
	}
	
	private boolean comprobarPagosTicketParcial() {
		boolean lineasParciales = false;
		List<LineaTicket> linesOrigin = ticketManager.getTicketOrigen().getLineas();
		BigDecimal cantidadesTotalesDevueltas = BigDecimal.ZERO;
		for (LineaTicket line : linesOrigin) {
			if (BigDecimalUtil.isMayorACero(line.getCantidadDevuelta())) {
				cantidadesTotalesDevueltas = cantidadesTotalesDevueltas.add(line.getCantidadDevuelta());
			}
		}

		if (BigDecimalUtil.isMayorACero(cantidadesTotalesDevueltas) && BigDecimalUtil.isMenor(cantidadesTotalesDevueltas, ticketManager.getTicketOrigen().getCabecera().getCantidadArticulos())) {
			lineasParciales = true;
		}

		if (lineasParciales && ((BricodepotTicketManager) ticketManager).getTicketOrigen().getPagos().size() > 1) {
			String textoPopUp = I18N.getTexto("No se puede convertir la FS a FT porque se ha realizado una devolución parcial y contiene varios métodos de pago");
			VentanaDialogoComponent.crearVentanaAviso(textoPopUp, getStage());
			tfOperacion.setText("");
			return true;
		}
		else if (BigDecimalUtil.isIgual(cantidadesTotalesDevueltas, ticketManager.getTicketOrigen().getCabecera().getCantidadArticulos())) {
			String textoPopUp = I18N.getTexto("No se puede convertir la FS a FT porque se ha realizado una devolución completa");
			VentanaDialogoComponent.crearVentanaAviso(textoPopUp, getStage());
			tfOperacion.setText("");
			return true;
		}

		return false;
	}

	private void abrirVentanaDatosFactura(boolean isOperacionFlexpoint) {
		log.debug("abrirVentanaDatosFactura() - abriendo ventana de Datos Factura");
		getDatos().put(FacturacionArticulosController.TICKET_KEY, ticketManager);
		getDatos().put(CONVERSION, true);
		getApplication().getMainView().showModalCentered(FacturaView.class, getDatos(), this.getStage());

		if (getDatos().containsKey("cancela")) {
			log.debug("convertirFSaFT() - cancela convirtiendo de FS a FT");
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("Se ha cancelado la conversion a FT"), getStage());
			tfOperacion.setText("");
			return;
		}
		if(!isOperacionFlexpoint) {
			generarDevolucion();			
		}

	}

	private void generarDevolucion() {
		log.debug("generarDevolucion() - Generando documento Nota de Crédito...");
		TicketVenta ticketOrigen = ((BricodepotTicketManager) ticketManager).getTicketOrigen();
		DatosFactura datosFacturacion = new DatosFactura();
		try {
			try {
				log.debug("generarDevolucion() - seteando documento a NC...");
				ticketManager.setDocumentoActivo(sesion.getAplicacion().getDocumentos().getDocumento("NC"));
				datosFacturacion = ((TicketVenta) ticketManager.getTicket()).getDatosFacturacion();
				if (ticketOrigen.getCabecera().getDatosFidelizado() != null) {
					ticketManager.getTicket().getCabecera().setDatosFidelizado(ticketOrigen.getCabecera().getDatosFidelizado());
				}
				((TicketVenta) ticketManager.getTicket()).setDatosFacturacion(null);
				((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).setFsToFt(true);
				((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).setFechaTicketOrigen(ticketOrigen.getCabecera().getFechaAsLocale());
			}
			catch (DocumentoException e) {
				log.error("generarDevolucion() - Error inicializando ticket: " + e.getMessage(), e);
			}

			((BricodepotTicketManager) ticketManager).addLineas();
			((BricodepotTicketManager) ticketManager).addPagos();

			ticketManager.getTicket().getTotales().recalcular();
			
			if (ticketManager.getTicket().getIdTicket() == null) {
				ticketsService.setContadorIdTicket((Ticket) ticketManager.getTicket());
			}
			ticketManager.guardarCopiaSeguridadTicket();

			log.debug("generarDevolucion() - registro en bbdd");
			ticketsService.registrarTicket((Ticket) ticketManager.getTicket(), ticketManager.getDocumentoActivo(), true);

			if (ticketManager.getTicket().getCliente().getCodpais().equalsIgnoreCase("PT")) {
				if (ticketOrigen.getCabecera().getFiscalData() != null) {
					imprimirConversion(false);
				}
				else {
					throw new FiscalDataException("No se ha configurado los rangos fiscales");
				}
			}
			generarFT(ticketOrigen, datosFacturacion);

		}
		catch (Exception e) {
			log.error("generarDevolucion() - Ha habido un problema al guardar la nota de crédito: " + e.getMessage(), e);
			VentanaDialogoComponent.crearVentanaError(getStage(), I18N.getTexto("Ha habido un error al guardar el documento de devolución. Contacte con un administrador."), e);
		}
	}

	private void generarFT(TicketVenta ticketOrigen, DatosFactura datosFacturacion) {
		log.debug("generaFT() - Generando documento Factura Completa...");
		try {
			DatosDocumentoOrigenTicket datosDocOrigen = ticketManager.getTicket().getCabecera().getDatosDocOrigen();
			// Verificamos que el tipo de documento no sea FLEX
			if (!datosDocOrigen.getCodTipoDoc().equals("FLEX")) {
				// Inicializamos el ticket y configuramos el ticket origen
				TicketVentaAbono ticketDevolucion = (TicketVentaAbono) ticketManager.getTicket();
				rellenarFacturaCompleta(ticketOrigen, ticketDevolucion, datosFacturacion, datosDocOrigen);
			}
		}
		catch (Exception e) {
			log.error("generaFT() - Ha habido un problema al guardar la FT: " + e.getMessage(), e);
			VentanaDialogoComponent.crearVentanaError(getStage(), I18N.getTexto("Ha habido un error al guardar el documento de devolución. Contacte con un administrador."), e);
		}
	}

	
	private void rellenarFacturaCompleta(TicketVenta ticketOrigen, TicketVentaAbono ticketDevolucion, DatosFactura datosFacturacion, DatosDocumentoOrigenTicket datosDocOrigen) {
		try {
			// Inicializo nuevo ticket
			ticketManager.finalizarTicket();
			ticketManager.inicializarTicket();
			ticketManager.guardarCopiaSeguridadTicket();
			
			// FT
			ticketManager.setDocumentoActivo(sesion.getAplicacion().getDocumentos().getDocumento("FT"));
			TicketVentaAbono ticketPrincipal = (TicketVentaAbono) ticketManager.getTicket();
			
			setearInformacionDelTicket(ticketOrigen, ticketPrincipal, datosFacturacion, datosDocOrigen);
			
			// Compruebo si tiene devoluciones
			if (tieneDevoluciones()) {
				log.debug("rellenarFacturaCompleta() - El ticket con uid" + ticketOrigen.getUidTicket() + " tiene devoluciones, seteando cantidades, precios y recalculando");
				ticketManager.getTicket().getLineas().clear();
				ticketPrincipal.getCabecera().setCantidadArticulos(ticketDevolucion.getCabecera().getCantidadArticulos().abs());
				List<LineaTicket> lineasTicketAUsar = ticketDevolucion.getLineas();
				for (LineaTicket linea : lineasTicketAUsar) {
					// Invierto signos de las lineas para añadirlas a la ft
					invertirSignosLinea(linea);
					LineaTicket lineaActual = linea.clone();
					if (BigDecimalUtil.isMayorACero(lineaActual.getCantidadDisponibleDevolver())) {
						lineaActual.setPrecioTotalConDto(lineaActual.getPrecioTotalConDto().setScale(2));
						lineaActual.setPrecioTotalSinDto(lineaActual.getPrecioTotalSinDto().setScale(2));
						ticketPrincipal.addLinea(lineaActual);
					}
					
				}
				// Invierto signo de los pagos para recalcular
				invertirSignoPagos(ticketDevolucion);
				ticketDevolucion.recalcularSubtotalesIva();
				ticketDevolucion.getCabecera().getTotales().recalcular();
				ticketPrincipal.getCabecera().getSubtotalesIva().clear();
				ticketPrincipal.getCabecera().getSubtotalesIva().addAll(ticketDevolucion.getCabecera().getSubtotalesIva());
				ticketPrincipal.getCabecera().setTotales(ticketDevolucion.getCabecera().getTotales());
				ticketPrincipal.setPagos(ticketDevolucion.getPagos());

			}

			terminarConversionFT();
		}
		catch (DocumentoException | PromocionesServiceException | TicketsServiceException e) {
			log.error("generaFT() - Ha habido un problema al guardar la FT: " + e.getMessage(), e);
		}
	}
	
	private void invertirSignoPagos(TicketVentaAbono ticketDevolucion) {
		for (PagoTicket pago : ticketDevolucion.getPagos()) {
			pago.setImporte(pago.getImporte().abs());
		}
	}

	private void setearInformacionDelTicket(TicketVenta ticketOrigen, TicketVentaAbono ticketPrincipal, DatosFactura datosFacturacion, DatosDocumentoOrigenTicket datosDocOrigen) {
		// CABECERA
		ticketPrincipal.setCliente(ticketOrigen.getCliente());
		ticketPrincipal.getCliente().setDatosFactura(datosFacturacion);
		ticketPrincipal.setCajero(sesion.getSesionUsuario().getUsuario());
		((BricodepotCabeceraTicket) ticketPrincipal.getCabecera()).setFechaTicketOrigen(datosDocOrigen.getFecha());
		((BricodepotCabeceraTicket) ticketPrincipal.getCabecera()).setEmpresa((sesion.getAplicacion().getEmpresa()));
		((BricodepotCabeceraTicket) ticketPrincipal.getCabecera()).setTienda((sesion.getAplicacion().getTienda()));
		((BricodepotCabeceraTicket) ticketPrincipal.getCabecera()).setCodCaja((sesion.getAplicacion().getCodCaja()));
		((BricodepotCabeceraTicket) ticketPrincipal.getCabecera()).setFsToFt(true);
		
		if (ticketOrigen.getCabecera().getDatosFidelizado() != null) {
			ticketPrincipal.getCabecera().setDatosFidelizado(ticketOrigen.getCabecera().getDatosFidelizado());
		}
		
		ticketPrincipal.setPromociones(ticketOrigen.getPromociones());
		ticketPrincipal.setCuponesEmitidos(ticketOrigen.getCuponesEmitidos());
		ticketPrincipal.setCuponesAplicados(ticketOrigen.getCuponesAplicados());
		
		ticketPrincipal.getCabecera().setCantidadArticulos(ticketOrigen.getCabecera().getCantidadArticulos());
		
		for (SubtotalIvaTicket subtotalIva : (List<SubtotalIvaTicket>) ticketOrigen.getCabecera().getSubtotalesIva()) {
			ticketPrincipal.getCabecera().getSubtotalesIva().add(subtotalIva);
		}
		
		List<LineaTicket> lineaTicketOrigen = ticketOrigen.getLineas();
		for (LineaTicket linea : lineaTicketOrigen) {
			LineaTicket lineaActual = linea.clone();
			lineaActual.setPrecioTotalConDto(lineaActual.getPrecioTotalConDto().setScale(2, BigDecimal.ROUND_HALF_UP));
		    lineaActual.setPrecioTotalSinDto(lineaActual.getPrecioTotalSinDto().setScale(2, BigDecimal.ROUND_HALF_UP));
		    
			linea.getPromociones().forEach(promoLinea -> lineaActual.getPromociones().stream().filter(promoLineaActual -> promoLinea.getIdPromocion().equals(promoLineaActual.getIdPromocion()))
			        .findFirst().ifPresent(promoLineaActual -> {
				        promoLineaActual.setImporteTotalDto(promoLinea.getImporteTotalDtoMenosMargen());
				        promoLineaActual.setImporteTotalDtoMenosIngreso(promoLinea.getImporteTotalDtoMenosIngreso());
				        promoLineaActual.setImporteTotalDtoFuturo(promoLinea.getImporteTotalDtoFuturo());
			        }));

			ticketPrincipal.addLinea(lineaActual);
		}
		ticketPrincipal.setPagos(ticketOrigen.getPagos());
		IPagoTicket cambioFS = ticketOrigen.getTotales().getCambio();
		ticketPrincipal.getCabecera().getTotales().setCambio(cambioFS);
		if (cambioFS.getImporte().compareTo(BigDecimal.ZERO) > 0) {
			for (PagoTicket pago : ticketPrincipal.getPagos()) {
				if (cambioFS.getCodMedioPago().equals(pago.getCodMedioPago())) {
					BigDecimal nuevoImporte = pago.getImporte().add(cambioFS.getImporte());
					pago.setImporte(nuevoImporte);
					break;
				}
			}
		}

		ticketPrincipal.getCabecera().setDatosDocOrigen(datosDocOrigen);
		ticketPrincipal.getCabecera().getTotales().recalcular();
	}
	
	private void invertirSignosLinea(LineaTicket lineaNoDevuelta) {
		// Seteamos los precios y cantidades a positivo porque estamos cogiendo el ticket de la NC
			lineaNoDevuelta.setCantidad(lineaNoDevuelta.getCantidad().abs());
			lineaNoDevuelta.setImporteConDto(lineaNoDevuelta.getImporteConDto().abs());
			lineaNoDevuelta.setImporteTotalConDto(lineaNoDevuelta.getImporteTotalConDto().abs());
	}

	private boolean tieneDevoluciones() {
		boolean tieneDevolucionesLinea = false;
		List<LineaTicket> lineasTicketOrigen = ticketManager.getTicketOrigen().getLineas();
		for (LineaTicket linea : lineasTicketOrigen) {
			tieneDevolucionesLinea = BigDecimalUtil.isMayorACero(linea.getCantidadDevuelta());
			if (tieneDevolucionesLinea) {
				break;
			}

		}
		return tieneDevolucionesLinea;
	}

	private void terminarConversionFT() throws TicketsServiceException, PromocionesServiceException, DocumentoException {
		log.debug("terminarConversionFT() - registrando en bbdd");
		ticketsService.setContadorIdTicket((Ticket) ticketManager.getTicket());
		ticketsService.registrarTicket((Ticket) ticketManager.getTicket(), ticketManager.getDocumentoActivo(), false);
		//IMPRIMIR FACTURA
		imprimirConversion(true);
	}

	public class RecuperarTicketConversion extends BackgroundTask<Boolean> {

		private String codigo;
		private String codTienda, codCaja;
		private Long idTipoDoc;

		public RecuperarTicketConversion(String codigo, String codTienda, String codCaja, Long idTipoDoc) {
			this.codigo = codigo;
			this.codTienda = codTienda;
			this.codCaja = codCaja;
			this.idTipoDoc = idTipoDoc;
		}

		@Override
		protected Boolean call() throws Exception {
			return ((BricodepotTicketManager) ticketManager).recuperarTicketConversion(codigo, codTienda, codCaja, idTipoDoc);
		}

		@Override
		protected void failed() {
			super.failed();
			if (getException() instanceof com.comerzzia.pos.util.exception.Exception) {
				VentanaDialogoComponent.crearVentanaError(getStage(), getCMZException().getMessage(), getCMZException());
			}
			else {
				VentanaDialogoComponent.crearVentanaError(getStage(), I18N.getTexto("Lo sentimos, ha ocurrido un error."), getException());
			}
		}

		@Override
		protected void succeeded() {
			tfOperacion.setText("");
			boolean res = getValue();
			recuperarTicketConversionSucceeded(res);
			super.succeeded();
		}

	}

	protected void recuperarTicketConversionSucceeded(boolean encontrado) {
		try {
			if (encontrado) {
				boolean esMismoTratamientoFiscal = ticketManager.comprobarTratamientoFiscalDev();
				if (!esMismoTratamientoFiscal) {
					try {
						ticketManager.eliminarTicketCompleto();
					}
					catch (Exception e) {
						log.error("recuperarTicketDevolucionSucceeded() - Ha habido un error al eliminar los tickets: " + e.getMessage(), e);
					}

					lbMensajeError.setText(I18N.getTexto("El ticket fue realizando en una tienda con un tratamiento fiscal diferente al de esta tienda. No se puede realizar esta devolución."));
					return;
				}

				boolean recoveredOnline = ticketManager.getTicket().getCabecera().getDatosDocOrigen().isRecoveredOnline();
				if (!recoveredOnline) {
					VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("No se han podido recuperar las líneas devueltas desde la central. No se podrá realizar la conversión a Factura."),
					        getStage());
					return;
				}
				DatosSesionBean datosSesion = new DatosSesionBean();
				datosSesion.setUidActividad(sesion.getAplicacion().getUidActividad());
				datosSesion.setUidInstancia(sesion.getAplicacion().getUidInstancia());
				datosSesion.setLocale(new Locale(AppConfig.idioma, AppConfig.pais));
				ConversionApi api = comerzziaApiManager.getClient(datosSesion, "ConversionApi");
				TicketVenta ticketOrigen = ((BricodepotTicketManager) ticketManager).getTicketOrigen();
				Conversion conversion = api.getConversion(ticketOrigen.getUidTicket());
				if (conversion == null) {
					convertirFSaFT();
				}
				else {
					// Si tiene conversion previa y no tiene uidTicket, significa que el ticket tiene promociones y devoluciones simultaneamente
					if (StringUtils.isBlank(conversion.getUidTicket())) {
						VentanaDialogoComponent.crearVentanaError(I18N.getTexto("No se permite la conversión de una factura con devoluciones y promociones al mismo tiempo"), getStage());
						return;
					}
					VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("Ya se ha convertido este ticket a una Factura Completa."), getStage());
					return;
				}

			}
			else {
				boolean continua = VentanaDialogoComponent.crearVentanaConfirmacion(I18N.getTexto("No se ha encontrado el documento origen en central. ¿Desea continuar con la Factura?"), getStage());
				if (!continua) {
					lbMensajeError.setText(I18N.getTexto("No se ha encontrado ningún ticket con esos datos"));
				}
				else {
					abrirFlujoFlexpoint();
				}
			}
		}
		catch (Exception e) {
			log.error("recuperarTicketConversionSucceeded() - Error al recuperar el ticket:" + e.getMessage(), e);
		}
	}

	private void abrirFlujoFlexpoint() throws InitializeGuiException, DocumentoException {
		log.debug("abrirFlujoFlexpoint()- iniciando flujo de flexpoint");

		try {
			ticketManager.inicializarTicket();
		}
		catch (DocumentoException | PromocionesServiceException e) {
			log.error("abrirFlujoFlexpoint()- Error a la hora de inicializar el ticket: " + e.getMessage(), e);
		}

		abrirVentanaAutorizacion();

		if (getDatos().containsKey("cancela")) {
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("Se ha cancelado la conversion a FT"), getStage());
			tfOperacion.setText("");
			return;
		}

		abrirVentanaDatosFactura(true);

		if (getDatos().containsKey("cancela")) {
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("Se ha cancelado la conversion a FT"), getStage());
			tfOperacion.setText("");
			return;
		} ;

		abrirVentanaDatosAdicionales();

		if (getDatos().containsKey("cancela")) {
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("Se ha cancelado la conversion a FT"), getStage());
			tfOperacion.setText("");
			return;
		};
		ticketManager.getTicket().getPromociones().clear();
		

	}
	
	private void abrirVentanaDatosAdicionales() throws InitializeGuiException, DocumentoException {
		log.debug("abrirVentanaDatosAdicionales()- abriendo ventana de datos adicionales");	
		getDatos().put(FacturacionArticulosController.TICKET_KEY, ticketManager);
		getApplication().getMainView().showModalCentered(PantallaDatosAdicionalesView.class, getDatos(), this.getStage());
		((BricodepotTicketManager)ticketManager).setConversionFlexpointFT(true);
		ticketManager.setDocumentoActivo(sesion.getAplicacion().getDocumentos().getDocumento("FT"));
		getDatos().put(FacturacionArticulosController.TICKET_KEY, ticketManager);
		getView().changeSubView(IntroduccionArticulosView.class,getDatos());
	}

	protected void abrirVentanaAutorizacion() {
		log.debug("abrirVentanaAutorizacion()- abriendo ventana de autorizacion");
		List<TicketAuditEvent> events = new ArrayList<>();
		TicketAuditEvent auditEvent = TicketAuditEvent.forEvent(TicketAuditEvent.Type.CONVERSION_FACTURA, sesion);
		events.add(auditEvent);
		getDatos().put(RequestAuthorizationController.AUDIT_EVENT, events);
		getDatos().put(FacturacionArticulosController.TICKET_KEY, ticketManager);
		getApplication().getMainView().showModalCentered(RequestAuthorizationView.class, getDatos(), this.getStage());
	}

	@FXML
	public void accionCancelar() {
		getApplication().getMainView().close();
	}

	protected boolean validarFormularioConsultaCliente() {
		boolean valido;

		// Limpiamos los errores que pudiese tener el formulario
		frConsultaTicket.clearErrorStyle();
		// Limpiamos el posible error anterior
		lbMensajeError.setText("");

		frConsultaTicket.setCodCaja(tfCodCaja.getText());
		frConsultaTicket.setCodOperacion(tfOperacion.getText());
		frConsultaTicket.setCodTienda(tfTienda.getText());
		frConsultaTicket.setCodDoc(tfCodDoc.getText());

		// Validamos el formulario de login
		Set<ConstraintViolation<FormularioConsultaTicketBean>> constraintViolations = ValidationUI.getInstance().getValidator().validate(frConsultaTicket);
		if (constraintViolations.size() >= 1) {
			ConstraintViolation<FormularioConsultaTicketBean> next = constraintViolations.iterator().next();
			frConsultaTicket.setErrorStyle(next.getPropertyPath(), true);
			frConsultaTicket.setFocus(next.getPropertyPath());
			lbMensajeError.setText(next.getMessage());
			valido = false;
		}
		else {
			valido = true;
		}

		return valido;
	}

	protected void procesarTipoDoc() {
		String codDoc = tfCodDoc.getText();

		if (!codDoc.trim().isEmpty()) {
			try {
				TipoDocumentoBean documento = documentos.getDocumento(codDoc);
				if (!documentos.getTiposDocumentoAbonables().contains(documento.getCodtipodocumento())) {
					log.warn("Se seleccionó un tipo de documento no válido para la devolución.");
					VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("El documento seleccionado no es válido."), getStage());
					tfCodDoc.setText("");
					tfDesDoc.setText("");
				}
				else {
					tfCodDoc.setText(documento.getCodtipodocumento());
					tfDesDoc.setText(documento.getDestipodocumento());
				}
			}
			catch (DocumentoException ex) {
				log.error("procesarTipoDoc() - Error obteniendo el tipo de documento: " + ex.getMessage(), ex);
				VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("El tipo de documento indicado no existe en la base de datos."), getStage());
				tfCodDoc.setText("");
				tfDesDoc.setText("");
			}
			catch (NumberFormatException nfe) {
				log.error("procesarTipoDoc() - El id de documento introducido no es válido: " + nfe.getMessage(), nfe);
				VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("El id introducido no es válido."), getStage());
				tfCodDoc.setText("");
				tfDesDoc.setText("");
			}
		}
		else {
			tfDesDoc.setText("");
			tfCodDoc.setText("");
		}
	}

	@Override
	public boolean canClose() {
		visor.escribirLineaArriba(I18N.getTexto("---CAJA CERRADA---"));
		visor.modoEspera();
		return super.canClose();
	}

	protected void addSeleccionarTodoEnFoco(final TextField campo) {
		campo.focusedProperty().addListener(new ChangeListener<Boolean>(){

			@Override
			public void changed(ObservableValue ov, Boolean t, Boolean t1) {
				Platform.runLater(new Runnable(){

					@Override
					public void run() {
						if (campo.isFocused() && !campo.getText().isEmpty()) {
							campo.selectAll();
						}
					}
				});
			}
		});
	}

	private void imprimirConversion(boolean imprimeA4) {
		log.debug("imprimirConversion() - imprimiendo documento Factura Completa");
		try {
			while (true) {
				boolean hayPagosTarjeta = false;
				for (Object pago : ticketManager.getTicket().getPagos()) {
					if (pago instanceof PagoTicket && ((PagoTicket) pago).getDatosRespuestaPagoTarjeta() != null) {
						hayPagosTarjeta = true;
						break;
					}
				}

				String formatoImpresion = ticketManager.getTicket().getCabecera().getFormatoImpresion();
				if (formatoImpresion.equals(TipoDocumentoBean.PROPIEDAD_FORMATO_IMPRESION_NO_CONFIGURADO)) {
					log.info("imprimir() - Formato de impresion no configurado, no se imprimira.");
					return;
				}

				Map<String, Object> mapaParametros = new HashMap<String, Object>();
				mapaParametros.put("ticket", ticketManager.getTicket());
				mapaParametros.put("BRICO_CABECERA", (BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera());

				if (mapaParametros.get("ticket") instanceof BricodepotTicketVentaAbono) {
					BricodepotTicketVentaAbono t = (BricodepotTicketVentaAbono) ticketManager.getTicket();
					if (StringUtils.isNotBlank(t.getNumPedido())) {
						mapaParametros.put("numPedido", t.getNumPedido());
					}
					if (StringUtils.isNotBlank(t.getNumTarjetaRegalo())) {
						mapaParametros.put("numTarjetaRegalo", t.getNumTarjetaRegalo());
					}
				}

				if (hayPagosTarjeta) {
					mapaParametros.put("listaPagosTarjeta", getPagosTarjetas());
					mapaParametros.put("listaPagosTarjetaDatosPeticion", getPagosTarjetasDatosPeticion());
				}
				mapaParametros.put("urlQR", variablesServices.getVariableAsString("TPV.URL_VISOR_DOCUMENTOS"));
				FidelizacionBean datosFidelizado = ticketManager.getTicket().getCabecera().getDatosFidelizado();
				mapaParametros.put("paperLess", datosFidelizado != null && datosFidelizado.getPaperLess() != null && datosFidelizado.getPaperLess());
				if (ticketManager.getTicket().getCabecera().getCodTipoDocumento().equals(sesion.getAplicacion().getDocumentos().getDocumento(Documentos.FACTURA_COMPLETA).getCodtipodocumento())) {
					mapaParametros.put("empresa", sesion.getAplicacion().getEmpresa());
				}

				if (ticketManager.getTicket().getCabecera().getCodTipoDocumento().equals("FT") || ticketManager.getTicket().getCabecera().getCodTipoDocumento().equals("FR")
				        || (ticketManager.getTicket().getCabecera().getCodTipoDocumento().equals("NC") && !sesion.getAplicacion().getTienda().getCliente().getCodpais().equals(COD_PAIS_ESPANA))) {
					mapaParametros.put("esImpresionA4", imprimeA4);
					if (ticketManager.getTicket().getCabecera().getCodTipoDocumento().equals("FR")) {
						mapaParametros.put("DEVOLUCION", true);
					}
				}
				addQR(ticketManager.getTicket(), mapaParametros);
				aniadirLogoParametrosImprimir(mapaParametros);

				if (sesion.getAplicacion().getTienda().getCliente().getCodpais().equals(COD_PAIS_PORTUGAL)) {
					mapaParametros.put("esCopia", true);
					aniadirLogoParametrosImprimir(mapaParametros);
					addQR(ticketManager.getTicket(), mapaParametros);
					ServicioImpresion.imprimir(formatoImpresion, mapaParametros);
					mapaParametros.put("esDuplicado", true);
					mapaParametros.put("esImpresionA4", false);
				}
				ServicioImpresion.imprimir(formatoImpresion, mapaParametros);
				
				if (hayPagosTarjeta) {
					if (VentanaDialogoComponent.crearVentanaConfirmacion(I18N.getTexto("¿Es correcta la impresión del recibo del pago con tarjeta?"), getStage())) {
						break;
					}
				}
				else {
					break;
				}
			}
			// Cupones
			if (BigDecimalUtil.isMayorACero(ticketManager.getTicket().getTotales().getTotal())) {
				List<CuponEmitidoTicket> cupones = ((TicketVentaAbono) ticketManager.getTicket()).getCuponesEmitidos();
				if (cupones.size() > 0) {
					Map<String, Object> mapaParametrosCupon = new HashMap<String, Object>();
					mapaParametrosCupon.put("ticket", ticketManager.getTicket());
					FidelizacionBean datosFidelizado = ticketManager.getTicket().getCabecera().getDatosFidelizado();
					mapaParametrosCupon.put("paperLess", datosFidelizado != null && datosFidelizado.getPaperLess() != null && datosFidelizado.getPaperLess());
					for (CuponEmitidoTicket cupon : cupones) {
						mapaParametrosCupon.put("cupon", cupon);
						SimpleDateFormat df = new SimpleDateFormat();
						mapaParametrosCupon.put("fechaEmision", df.format(ticketManager.getTicket().getCabecera().getFecha()));

						Promocion promocionAplicacion = sesion.getSesionPromociones().getPromocionActiva(cupon.getIdPromocionAplicacion());
						if (promocionAplicacion != null) {
							Date fechaInicio = promocionAplicacion.getFechaInicio();
							if (fechaInicio == null || fechaInicio.before(ticketManager.getTicket().getCabecera().getFecha())) {
								mapaParametrosCupon.put("fechaInicio", FormatUtil.getInstance().formateaFecha(ticketManager.getTicket().getCabecera().getFecha()));
							}
							else {
								mapaParametrosCupon.put("fechaInicio", FormatUtil.getInstance().formateaFecha(fechaInicio));
							}
							Date fechaFin = promocionAplicacion.getFechaFin();
							mapaParametrosCupon.put("fechaFin", FormatUtil.getInstance().formateaFecha(fechaFin));

						}
						else {
							mapaParametrosCupon.put("fechaInicio", "");
							mapaParametrosCupon.put("fechaFin", "");
						}
						if (cupon.getMaximoUsos() != null) {
							mapaParametrosCupon.put("maximoUsos", cupon.getMaximoUsos().toString());
						}
						else {
							mapaParametrosCupon.put("maximoUsos", "");
						}

						ServicioImpresion.imprimir(PLANTILLA_CUPON, mapaParametrosCupon);
					}
				}
				if (!ticketManager.isEsDevolucion()) {
					
					// Imprimimos vale para cambio
					if (mediosPagosService.isCodMedioPagoVale(ticketManager.getTicket().getTotales().getCambio().getCodMedioPago(), ticketManager.getTicket().getCabecera().getTipoDocumento())
					        && !BigDecimalUtil.isIgualACero(ticketManager.getTicket().getTotales().getCambio().getImporte())) {
						printVale(ticketManager.getTicket().getTotales().getCambio());
					}
				}
				else {
					if (documentos.isDocumentoAbono(ticketManager.getTicket().getCabecera().getCodTipoDocumento())) {
						// Es una devoluciÃ³n donde el signo del tipo de documento es positivo, imprimimos vales de
						// pagos
						List<PagoTicket> pagos = ((TicketVenta) ticketManager.getTicket()).getPagos();
						for (PagoTicket pago : pagos) {
							if (mediosPagosService.isCodMedioPagoVale(pago.getCodMedioPago(), ticketManager.getTicket().getCabecera().getTipoDocumento())
							        && BigDecimalUtil.isMenorACero(pago.getImporte())) {
								printVale(pago);
							}
						}
					}
				}
			}
			else {
				// Imprimimos vales para pagos si estamos en devoluciÃ³n pero no si es de cambio (pago positivo en una
				// devolucion donde los pagos son negativos)
				List<PagoTicket> pagos = ((TicketVenta) ticketManager.getTicket()).getPagos();
				for (PagoTicket pago : pagos) {
					if (mediosPagosService.isCodMedioPagoVale(pago.getCodMedioPago(), ticketManager.getTicket().getCabecera().getTipoDocumento()) && !BigDecimalUtil.isMayorACero(pago.getImporte())) {
						printVale(pago);
					}
				}
			}
		}
		catch (Exception e) {
			log.error("imprimir() - " + e.getClass().getName() + " - " + e.getLocalizedMessage(), e);
			VentanaDialogoComponent.crearVentanaError(getStage(),
			        I18N.getTexto("Lo sentimos, ha ocurrido un error al imprimir.") + System.lineSeparator() + System.lineSeparator() + I18N.getTexto("El error es: ") + e.getMessage(), e);
		}
	}


	private void addQR(ITicket ticketOrigen, Map<String, Object> parameters) throws Exception, IOException {
		if (ticketOrigen.getCabecera() instanceof BricodepotCabeceraTicket) {
			log.debug("addQr() - La información fiscal ya viene en el ticket.");

			if (ticketOrigen.getCabecera().getFiscalData() != null) {
				if (!ticketOrigen.getCabecera().getFiscalData().getProperties().isEmpty() && ticketOrigen.getCabecera().getFiscalData().getProperty(BricodepotTicketManager.PROPERTY_QR) != null) {

					String data = ticketOrigen.getCabecera().getFiscalData().getProperty(BricodepotTicketManager.PROPERTY_QR).getValue();

					log.debug("refrescarDatosPantalla() - Generando imagen del QR de Portugal");

					Base64Coder coder = new Base64Coder(Base64Coder.UTF8);
					String qr = coder.decodeBase64(data);
					BufferedImage qrImage = generateQRCodeImage(qr);
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					ImageIO.write(qrImage, "jpeg", os);
					InputStream is = new ByteArrayInputStream(os.toByteArray());
					parameters.put("QR_PORTUGAL", is);
				}
			}
			else {
				log.debug("addQr() - La información fiscal no viene en el ticket.");
			}
		}
		else {
			log.debug("addQr() - La información fiscal no viene en el ticket.");
		}
	}

	private BufferedImage generateQRCodeImage(String barcodeText) throws Exception {
		QRCodeWriter barcodeWriter = new QRCodeWriter();
		BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 200, 200);

		return MatrixToImageWriter.toBufferedImage(bitMatrix);
	}

	protected List<DatosRespuestaPagoTarjeta> getPagosTarjetas() {
		log.debug("getPagosTarjetas");
		List<DatosRespuestaPagoTarjeta> listaPagosTarjeta = new ArrayList<DatosRespuestaPagoTarjeta>();
		List<PagoTicket> listaPagos = ticketManager.getTicket().getPagos();
		for (PagoTicket pago : listaPagos) {
			if (pago.getDatosRespuestaPagoTarjeta() != null) {
				DatosRespuestaPagoTarjeta datosRespuestaPagoTarjeta = pago.getDatosRespuestaPagoTarjeta();
				listaPagosTarjeta.add(datosRespuestaPagoTarjeta);
			}
		}
		return listaPagosTarjeta;
	}

	protected List<DatosPeticionPagoTarjeta> getPagosTarjetasDatosPeticion() {
		log.debug("getPagosTarjetasDatosPeticion()");
		List<DatosPeticionPagoTarjeta> listaPagosTarjeta = new ArrayList<DatosPeticionPagoTarjeta>();
		List<PagoTicket> listaPagos = ticketManager.getTicket().getPagos();
		for (PagoTicket pago : listaPagos) {
			if (pago.getDatosRespuestaPagoTarjeta() != null) {
				DatosRespuestaPagoTarjeta datosRespuestaPagoTarjeta = pago.getDatosRespuestaPagoTarjeta();
				DatosPeticionPagoTarjeta datosPeticion = datosRespuestaPagoTarjeta.getDatosPeticion();
				listaPagosTarjeta.add(datosPeticion);
			}
		}
		return listaPagosTarjeta;
	}

	private void aniadirLogoParametrosImprimir(Map<String, Object> mapaParametros) throws IOException {
		if(sesion.getAplicacion().getEmpresa().getLogotipo() != null) {
			InputStream is = new ByteArrayInputStream(sesion.getAplicacion().getEmpresa().getLogotipo());
			mapaParametros.put("LOGO", is);
			is.close();
		}
	}
	
	protected void printVale(IPagoTicket iPagoTicket) throws DeviceException {
		Map<String,Object> mapaParametrosTicket = new HashMap<String,Object>();
		mapaParametrosTicket.put("ticket",ticketManager.getTicket());
		mapaParametrosTicket.put("urlQR", variablesServices.getVariableAsString("TPV.URL_VISOR_DOCUMENTOS"));
		mapaParametrosTicket.put("importeVale", FormatUtil.getInstance().formateaImporte(iPagoTicket.getImporte().abs()));
		FidelizacionBean datosFidelizado = ticketManager.getTicket().getCabecera().getDatosFidelizado();
		mapaParametrosTicket.put("paperLess", datosFidelizado != null && datosFidelizado.getPaperLess() != null && datosFidelizado.getPaperLess());
		mapaParametrosTicket.put("esCopia", Boolean.FALSE);
		ServicioImpresion.imprimir(PLANTILLA_VALE, mapaParametrosTicket);
		mapaParametrosTicket.put("esCopia", Boolean.TRUE);
		ServicioImpresion.imprimir(PLANTILLA_VALE, mapaParametrosTicket);
	}

}
