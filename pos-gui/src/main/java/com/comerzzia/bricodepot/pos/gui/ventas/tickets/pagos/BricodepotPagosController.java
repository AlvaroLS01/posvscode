package com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.ws.rs.BadRequestException;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Controller;

import com.comerzzia.api.model.loyalty.FidelizadoBean;
import com.comerzzia.api.model.loyalty.TarjetaBean;
import com.comerzzia.api.model.loyalty.TiposContactoFidelizadoBean;
import com.comerzzia.api.rest.client.exceptions.RestConnectException;
import com.comerzzia.api.rest.client.exceptions.RestException;
import com.comerzzia.api.rest.client.exceptions.RestHttpException;
import com.comerzzia.api.rest.client.exceptions.RestTimeoutException;
import com.comerzzia.api.rest.client.fidelizados.ConsultarFidelizadoRequestRest;
import com.comerzzia.api.rest.client.fidelizados.FidelizadoRequestRest;
import com.comerzzia.api.rest.client.fidelizados.FidelizadosRest;
import com.comerzzia.api.rest.client.fidelizados.ResponseGetFidelizadoRest;
import com.comerzzia.api.rest.client.movimientos.ListaMovimientoRequestRest;
import com.comerzzia.api.rest.client.movimientos.MovimientoRequestRest;
import com.comerzzia.api.rest.client.movimientos.MovimientosRest;
import com.comerzzia.bricodepot.pos.devices.visor.comun.tarjeta.BricodepotCodigoTarjetaController;
import com.comerzzia.bricodepot.pos.gui.ventas.codigopostal.CodigoPostalController;
import com.comerzzia.bricodepot.pos.gui.ventas.codigopostal.CodigoPostalView;
import com.comerzzia.bricodepot.pos.gui.ventas.devoluciones.BricodepotIntroduccionArticulosController;
import com.comerzzia.bricodepot.pos.gui.ventas.tarjetas.TarjetasRegaloController;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.BricodepotTicketManager;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.articulos.BricodepotFacturacionArticulosController;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.email.EmailController;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.email.seleccionenvio.SeleccionEnvioTicketView;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.financiacion.FinanciacionController;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.financiacion.FinanciacionView;
import com.comerzzia.bricodepot.pos.services.payments.methods.types.FinanciacionManager;
import com.comerzzia.bricodepot.pos.services.ticket.BricodepotCabeceraTicket;
import com.comerzzia.bricodepot.pos.services.ticket.BricodepotTicketVentaAbono;
import com.comerzzia.bricodepot.pos.services.ticket.lineas.BricodepotLineaTicket;
import com.comerzzia.bricodepot.pos.util.AnticiposConstants;
import com.comerzzia.bricodepot.posservices.client.TarjetasApi;
import com.comerzzia.bricodepot.posservices.client.model.ResponseGetTarjetaregaloRest;
import com.comerzzia.core.servicios.api.ComerzziaApiManager;
import com.comerzzia.core.servicios.contadores.ServicioContadoresImpl;
import com.comerzzia.core.servicios.sesion.DatosSesionBean;
import com.comerzzia.core.servicios.variables.Variables;
import com.comerzzia.core.util.base64.Base64Coder;
import com.comerzzia.pos.core.dispositivos.Dispositivos;
import com.comerzzia.pos.core.dispositivos.dispositivo.visor.IVisor;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.POSApplication;
import com.comerzzia.pos.core.gui.RestBackgroundTask;
import com.comerzzia.pos.core.gui.componentes.botonaccion.BotonBotoneraComponent;
import com.comerzzia.pos.core.gui.componentes.botonaccion.normal.BotonBotoneraNormalComponent;
import com.comerzzia.pos.core.gui.componentes.botonera.BotoneraComponent;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.dispositivo.comun.tarjeta.CodigoTarjetaController;
import com.comerzzia.pos.dispositivo.comun.tarjeta.CodigoTarjetaView;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.ConsultarFidelizadoPorIdTask;
import com.comerzzia.pos.gui.ventas.tickets.TicketManager.SalvarTicketCallback;
import com.comerzzia.pos.gui.ventas.tickets.articulos.FacturacionArticulosController;
import com.comerzzia.pos.gui.ventas.tickets.factura.FacturaView;
import com.comerzzia.pos.gui.ventas.tickets.pagos.PagoTicketGui;
import com.comerzzia.pos.gui.ventas.tickets.pagos.PagosController;
import com.comerzzia.pos.persistence.core.documentos.tipos.TipoDocumentoBean;
import com.comerzzia.pos.persistence.fidelizacion.FidelizacionBean;
import com.comerzzia.pos.persistence.giftcard.GiftCardBean;
import com.comerzzia.pos.persistence.mediosPagos.MedioPagoBean;
import com.comerzzia.pos.persistence.tickets.datosfactura.DatosFactura;
import com.comerzzia.pos.services.core.documentos.DocumentoException;
import com.comerzzia.pos.services.core.documentos.Documentos;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.services.fiscaldata.FiscalDataException;
import com.comerzzia.pos.services.mediospagos.MediosPagosService;
import com.comerzzia.pos.services.payments.events.PaymentOkEvent;
import com.comerzzia.pos.services.payments.events.PaymentSelectEvent;
import com.comerzzia.pos.services.payments.methods.PaymentMethodManager;
import com.comerzzia.pos.services.payments.methods.types.GiftCardManager;
import com.comerzzia.pos.services.promociones.Promocion;
import com.comerzzia.pos.services.ticket.ITicket;
import com.comerzzia.pos.services.ticket.Ticket;
import com.comerzzia.pos.services.ticket.TicketVenta;
import com.comerzzia.pos.services.ticket.TicketVentaAbono;
import com.comerzzia.pos.services.ticket.TicketsService;
import com.comerzzia.pos.services.ticket.cabecera.CabeceraTicket;
import com.comerzzia.pos.services.ticket.cabecera.DatosDocumentoOrigenTicket;
import com.comerzzia.pos.services.ticket.cabecera.TarjetaRegaloTicket;
import com.comerzzia.pos.services.ticket.cabecera.TotalesTicket;
import com.comerzzia.pos.services.ticket.cupones.CuponEmitidoTicket;
import com.comerzzia.pos.services.ticket.pagos.PagoTicket;
import com.comerzzia.pos.services.ticket.pagos.tarjeta.DatosPeticionPagoTarjeta;
import com.comerzzia.pos.services.ticket.pagos.tarjeta.DatosRespuestaPagoTarjeta;
import com.comerzzia.pos.servicios.impresion.ServicioImpresion;
import com.comerzzia.pos.util.bigdecimal.BigDecimalUtil;
import com.comerzzia.pos.util.config.AppConfig;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.format.FormatUtil;
import com.comerzzia.pos.util.i18n.I18N;
import com.comerzzia.pos.util.xml.MarshallUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import rest.client.tarjeta.RestTarjetaClient;

@SuppressWarnings("unchecked")
@Controller
@Primary
public class BricodepotPagosController extends PagosController {

	public static final String EMAIL_FIDELIZADO_CARGADO = "emailFidelizadoCargado";

	public static final String ACCION_CANCELAR_TARJETA = "CANCELAR_TARJETA";
	@Autowired
	protected TicketsService ticketsService;
	private static final String COD_PAIS_PORTUGAL = "PT";
	private static final String COD_PAIS_ESPANA = "ES";

	public static final String COD_MP_EFECTIVO = "0000";

	public static final String COD_MP_GIFTCARD = "0020";

	public static final String COD_MP_VALE = "1000";

	private static final String COD_MP_GIFTCARD_FLEX = "0060";

	@Autowired
	private MediosPagosService mediosPagosService;

	@Autowired
	private VariablesServices variablesServices;

	@Autowired
	protected ComerzziaApiManager comerzziaApiManager;

	private String tipoTarjeta, recargoTarjeta;

	private boolean esVentaTarjeta = false;

	private static String accionEmail;

	final IVisor visor = Dispositivos.getInstance().getVisor();

	protected int paymentId;

	@Override
	public void initializeForm() throws InitializeGuiException {
		/* Activamos todos los medios de pago [BRICO-198] */
		desactivarTodosLosbotones(false);

		if (getDatos().containsKey(BricodepotFacturacionArticulosController.VENTA_TARJETA_REGALO_KEY)) {
			esVentaTarjeta = (boolean) getDatos().get(BricodepotFacturacionArticulosController.VENTA_TARJETA_REGALO_KEY);
		}
		else {
			esVentaTarjeta = false;
		}

		super.initializeForm();

		if (ticketManager.isEsDevolucion()) {
			if (!((BricodepotTicketManager) ticketManager).esDevolucionFlexpoint()) {
				/*
				 * Solo en los casos de devoluciones normales/estandar. En las devoluciones sólo aparecerán activos los
				 * medios de pago que se hayan usado en el ticket origen
				 */
				panelBotoneraDatosAdicionales.setVisible(Boolean.FALSE);
			}
			else {
				/* En el caso de devoluciones flexpoint debe de aparecer todas los medios de pago disponibles */
				desactivarTodosLosbotones(false);
				panelBotoneraDatosAdicionales.setVisible(Boolean.TRUE);
			}
		}
		/* Venimos de una venta */
		else {
			panelBotoneraDatosAdicionales.setVisible(Boolean.TRUE);
			tipoTarjeta = null;
			recargoTarjeta = null;
			if (getDatos().containsKey(TarjetasRegaloController.TIPO_TARJETA_REGALO) && getDatos().containsKey(TarjetasRegaloController.RECARGO_TARJETA)) {
				tipoTarjeta = (String) getDatos().get(TarjetasRegaloController.TIPO_TARJETA_REGALO);
				recargoTarjeta = (String) getDatos().get(TarjetasRegaloController.RECARGO_TARJETA);
			}
		}
		controlBotones();
		this.paymentId = 0;

		// TODO JGG
		mostrarLogXML();
	}
	
	/**
	 * Cambia el tipo del ticket a NC y registra una devolución FLEX del ticket actual.
	 */
	@SuppressWarnings("rawtypes")
	private void generarDevolucion() {
		log.debug("generarDevolucion() - Generando documento Nota de Crédito...");
		DatosFactura datosFacturacion = new DatosFactura();
		try {

			try {
				log.debug("generarDevolucion() - seteando documento a NC...");
				ticketManager.setDocumentoActivo(sesion.getAplicacion().getDocumentos().getDocumento("NC"));
				datosFacturacion = ((TicketVenta)ticketManager.getTicket()).getDatosFacturacion();
				 ((TicketVenta)ticketManager.getTicket()).setDatosFacturacion(null);
				 guardaFechaOrigen();
			}
			catch (DocumentoException e) {
				log.error("Error inicializando ticket");
			}

			ticketManager.getTicketOrigen().setPagos(ticketManager.getTicket().getPagos());
			ticketManager.getTicketOrigen().getLineas().addAll(ticketManager.getTicket().getLineas());
			((BricodepotTicketManager) ticketManager).addLineas();
			((BricodepotTicketManager) ticketManager).addPagos();
			ticketManager.getTicket().getTotales().recalcular();
			
			if (ticketManager.getTicket().getIdTicket() == null) { 
				ticketsService.setContadorIdTicket((Ticket) ticketManager.getTicket());
			}
			ticketManager.guardarCopiaSeguridadTicket();
			log.debug("generarDevolucion() - registro en bbdd");
			ticketsService.registrarTicket((Ticket) ticketManager.getTicket(), ticketManager.getDocumentoActivo(), true);
			if(ticketManager.getTicket().getCliente().getCodpais() .equalsIgnoreCase("PT")) {
				if(ticketManager.getTicket().getCabecera().getFiscalData()!=null) {
					imprimir();
				}else {
					throw new FiscalDataException("No se ha configurado los rangos fiscales");
				}
			}
			((TicketVenta)ticketManager.getTicket()).setDatosFacturacion(datosFacturacion);
			generaFlexConversion();

		}
		catch (Exception e) {
			log.error("generarDevolucion() - Ha habido un problema al guardar la nota de crédito: " + e.getMessage(), e);
			VentanaDialogoComponent.crearVentanaError(getStage(), I18N.getTexto("Ha habido un error al guardar el documento de devolución. Contacte con un administrador."), e);
		}

	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	/**
	 * Cambia el tipo del ticket y genera una FT del ticket actual
	 */
	private void generaFlexConversion() {
		try {
			ITicket ticketOrigen = ticketManager.getTicket();
			// Finalizamos el tcket anterior y creamos uno nuevo y guardamos la fecha indicada
			String fechaIndicada = null;
			if(((BricodepotCabeceraTicket)ticketManager.getTicket().getCabecera()).getFechaTicketOrigen() != null) {
				fechaIndicada = ((BricodepotCabeceraTicket)ticketManager.getTicket().getCabecera()).getFechaTicketOrigen();
			}
			ticketManager.finalizarTicket();
			ticketManager.inicializarTicket();
			((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).setFechaTicketOrigen(fechaIndicada);
			ticketManager.setDocumentoActivo(sesion.getAplicacion().getDocumentos().getDocumento("FT"));

			DatosSesionBean datosSesion = new DatosSesionBean();
			datosSesion.setUidActividad(sesion.getAplicacion().getUidActividad());
			datosSesion.setUidInstancia(sesion.getAplicacion().getUidInstancia());

			// Obtenemos los datosFactura y los datos documento origen.
			DatosFactura datosFacturacion = ((TicketVenta) ticketOrigen).getDatosFacturacion();
			DatosDocumentoOrigenTicket datosDocOrigen = ticketOrigen.getCabecera().getDatosDocOrigen();
			
			BricodepotCabeceraTicket cab = (BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera();
			if(datosDocOrigen != null) {
				cab.setFechaTicketOrigen(datosDocOrigen.getFecha());
			}
			cab.setIdTicket(ServicioContadoresImpl.get().obtenerValorContador(datosSesion, ticketManager.getDocumentoActivo().getIdContador()));
			cab.setCodTicket(cab.getCodTipoDocumento() + " " + cab.getFechaAsLocale().substring(6, 10) + cab.getCliente().getCodCliente() + cab.getCodCaja() + "/"
			        + StringUtils.leftPad(cab.getIdTicket().toString(), 8, "0"));
			// Añadimos las lineas del ticket origen al nuevo ticket FT
			ticketManager.getTicket().getLineas().clear();
			for (BricodepotLineaTicket lineaOrigen : (List<BricodepotLineaTicket>) ticketOrigen.getLineas()) {
				BricodepotLineaTicket newLine =  (BricodepotLineaTicket) ticketManager.nuevaLineaArticulo(lineaOrigen.getCodArticulo(), lineaOrigen.getDesglose1(), lineaOrigen.getDesglose2(), BigDecimal.ONE, getStage(), null, false);
				newLine.setCantidad(lineaOrigen.getCantidad().abs());
				newLine.setPrecioTarifaOrigen(lineaOrigen.getPrecioTarifaOrigen());
				newLine.setPrecioConDto(lineaOrigen.getPrecioConDto().abs());
				newLine.setPrecioTotalConDto(lineaOrigen.getPrecioTotalConDto().abs());
				newLine.setPrecioTotalSinDto(lineaOrigen.getPrecioTotalSinDto().abs());
				newLine.setImporteTotalConDto(lineaOrigen.getImporteTotalConDto().abs());
				newLine.setImporteConDto(lineaOrigen.getImporteConDto().abs());
				newLine.setPorcentajeIvaConversion(lineaOrigen.getPorcentajeIvaConversion());
				newLine.setConversionAFT(lineaOrigen.isConversionAFT());
			}
			ticketManager.getTicket().getPagos().clear();
			List<PagoTicket> pagos = ticketOrigen.getPagos();
			for (PagoTicket pago : pagos) {
				pago.setImporte(pago.getImporte().abs());
			}
			ticketManager.getTicket().getCabecera().setCantidadArticulos(ticketOrigen.getCabecera().getCantidadArticulos());
			ticketManager.getTicket().setPagos(pagos);
			ticketManager.getTicket().getTotales().recalcular();
			((TicketVenta) ticketManager.getTicket()).setDatosFacturacion(datosFacturacion);
			ticketManager.getTicket().getCabecera().setDatosDocOrigen(datosDocOrigen);
			ticketsService.setContadorIdTicket((Ticket) ticketManager.getTicket());
			ticketManager.guardarCopiaSeguridadTicket();
		}
		catch (Exception e) {
			log.error("Ha ocurrido un error al convertir a FT: " + e.getMessage(), e);
		}
	}
	
	
	@Override
	public void aceptar() throws DocumentoException {
		log.debug(String.format(
				"aceptar() - Antes de comenzar guardado de ticket - UID: %s | Total: %.2f | Entregado: %.2f | Promociones: %.2f",
				ticketManager.getTicket().getUidTicket(), ticketManager.getTicket().getTotales().getTotal(), ticketManager.getTicket().getTotales().getEntregado(),
				ticketManager.getTicket().getTotales().getTotalPromociones()));

		if (ticketManager.getTicket().getCabecera().getDatosDocOrigen() != null && ticketManager.getTicket().getCabecera().getDatosDocOrigen().getCodTipoDoc().equals("FLEX")) {
			generarDevolucion();
		}
		if (ticketManager.isEsDevolucion()) {
			guardaFechaOrigen();
			if (compruebaMediosPago()) {
				String codAnticipo = variablesServices.getVariableAsString(BricodepotIntroduccionArticulosController.POS_ARTICULO_ANTICIPO);
				if (ticketManager.getTicket().getLineas().size() == 1) {
					if (((BricodepotLineaTicket) ticketManager.getTicket().getLineas().get(0)).getCodArticulo().equals(codAnticipo)) {
						/* Estamos en una devolución de un anticipo que no se ha usado todavía */
						((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).setOperacionAnticipo(AnticiposConstants.PARAMETRO_ESTADO_DEVUELTO);
					}
				}
				if (((BricodepotTicketManager) ticketManager).esDevolucionFlexpoint()) {
					((BricodepotTicketManager) ticketManager).rellenarDatosDocOrigenFlexpoint();
				}
				super.aceptar();
			}
			else {
				return;
			}
		}
		else {
			super.aceptar();
		}
	}

	private void guardaFechaOrigen() {
		log.debug("guardarFechaOrigen() - guardando fecha de origen en la devolucion");
		BricodepotCabeceraTicket cabecera = (BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera();
		if (ticketManager.getTicketOrigen() != null && ticketManager.getTicketOrigen().getFecha() != null) {
			Date fechaOrigen = ticketManager.getTicketOrigen().getFecha();
			String fechaTicket = FormatUtil.getInstance().formateaFechaCorta(fechaOrigen);
			String horaTicket = FormatUtil.getInstance().formateaHora(fechaOrigen);
			cabecera.setFechaTicketOrigen(fechaTicket + " " + horaTicket);
		}else if(ticketManager.getTicketOrigen() != null && ticketManager.getTicketOrigen().getCabecera().getDatosDocOrigen()!= null) {
			cabecera.setFechaTicketOrigen(ticketManager.getTicketOrigen().getCabecera().getDatosDocOrigen().getFecha());
		}
	}

	private boolean compruebaMediosPago() {
		boolean result = true;
		if (!((BricodepotTicketManager) ticketManager).esDevolucionFlexpoint()) {
			TicketVentaAbono ticketVenta = (TicketVentaAbono) ticketManager.getTicketOrigen();
			List<String> pagosVenta = ticketVenta.getPagos().stream().map(x -> x.getCodMedioPago()).collect(Collectors.toList());
			log.info("compruebaMediosPago() - Medios de pago usados en la venta: " + pagosVenta.toString());
			TicketVentaAbono ticketDevolucion = (TicketVentaAbono) ticketManager.getTicket();
			List<String> pagosDevolucion = ticketDevolucion.getPagos().stream().map(x -> x.getCodMedioPago()).collect(Collectors.toList());
			log.info("compruebaMediosPago() - Medios de pago usados en la devolución " + pagosDevolucion.toString());

			if (!pagosVenta.containsAll(pagosDevolucion)) { // Si la lista de devolucion no esta completamente contenida
			                                                // en
				// la lista de ventas,
				// significa que en la devolucion hay medios de pagos que no
				// estan en la venta.
				if (!VentanaDialogoComponent.crearVentanaConfirmacion("La forma de pago es diferente a la forma de pago original", getStage())) {
					result = false;
				}
			}

		}

		return result;
	}

	@Override
	protected void selectCustomPaymentMethod(PaymentSelectEvent paymentSelectEvent) {
		Object source = paymentSelectEvent.getSource();
		if (source instanceof FinanciacionManager) {
			MedioPagoBean medioPago = mediosPagosService.getMedioPago(((FinanciacionManager) source).getPaymentCode());

			askDocumento((FinanciacionManager) source, medioPago);
		}
		else {
			actionBtAnotarPago();
		}
	}

	private void askDocumento(PaymentMethodManager source, MedioPagoBean medioPago) {
		try {
			HashMap<String, Object> parametros = new HashMap<>();
			parametros.put("medioPago", medioPago);
			obtenerDocumentoMedioPagoOrigen(parametros, medioPago.getCodMedioPago());
			getApplication().getMainView().showModalCentered(FinanciacionView.class, parametros, getStage());
			if (parametros.containsKey(FinanciacionController.PARAMETRO_DOCUMENTO)) {
				String documento = (String) parametros.get(FinanciacionController.PARAMETRO_DOCUMENTO);
				if (StringUtils.isNotBlank(documento)) {
					source.addParameter(FinanciacionController.PARAMETRO_DOCUMENTO, documento);
				}
				actionBtAnotarPago();
			}
			else {
				selectDefaultPaymentMethod();
			}
		}
		catch (Exception e) {
			VentanaDialogoComponent.crearVentanaError(getStage(), "Lo sentimos, se ha producido un error guardando el número de financiación", e);
			selectDefaultPaymentMethod();
		}
	}

	@SuppressWarnings("rawtypes")
	private void obtenerDocumentoMedioPagoOrigen(HashMap<String, Object> parametros, String codMedioPago) {
		log.debug("obtenerDocumentoMedioPagoOrigen() - Obteniendo documento asociado al medio pago origen: " + codMedioPago);
		TicketVenta ticketOrigen = ticketManager.getTicketOrigen();
		if (ticketOrigen != null) {
			List<PagoTicket> pagosOrigen = ticketOrigen.getPagos();
			for (PagoTicket pago : pagosOrigen) {
				if (pago.getExtendedData().containsKey("documento") && pago.getCodMedioPago().equals(codMedioPago)) {
					parametros.put(pago.getCodMedioPago(), pago.getExtendedData().get("documento"));
				}
			}
		}
	}

	protected void addCustomPaymentData(PaymentOkEvent eventOk, PagoTicket payment) {

		if (eventOk.getSource() instanceof FinanciacionManager) {
			payment.setExtendedData(eventOk.getExtendedData());
		}
	}

	@SuppressWarnings({ "rawtypes" })
	protected void imprimir() {
		log.debug("imprimir()");
		try {
			if (accionEmail != null && accionEmail.equalsIgnoreCase(EmailController.CORREO)) {
				log.debug("imprimir() - Se ha seleccionado la opcion de envio solo por correo, por lo que se realizara la impresion por plantilla");

				/* Comprobamos si en los pagos hay algún pago con efectivo para proceder a abrir el cajón */
				Boolean abreCajon = Boolean.FALSE;
				for (Object pago : ticketManager.getTicket().getPagos()) {
					if (pago instanceof PagoTicket && ((PagoTicket) pago).getCodMedioPago().equals(COD_MP_EFECTIVO)) {
						abreCajon = Boolean.TRUE;
					}
				}

				if (abreCajon) {
					Dispositivos.abrirCajon();
				}
			}
			else {
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
					addPagoGiftcard(mapaParametros);
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
						mapaParametros.put("esImpresionA4", true);
						if (ticketManager.getTicket().getCabecera().getCodTipoDocumento().equals("FR")) {
							mapaParametros.put("DEVOLUCION", true);
						}
					}
					
					if (ticketManager.getTicket().getCabecera().getCodTipoDocumento().equals("NC") || ticketManager.getTicket().getCabecera().getCodTipoDocumento().equals("FR")) {
						mapaParametros.put("DEVOLUCION", true);
					}
					addQR(ticketManager.getTicket(), mapaParametros);
					aniadirLogoParametrosImprimir(mapaParametros);

					if (sesion.getAplicacion().getTienda().getCliente().getCodpais().equals(COD_PAIS_PORTUGAL)) {
						mapaParametros.put("esCopia", true);
					}
					
					ServicioImpresion.imprimir(formatoImpresion, mapaParametros);
					
					if (sesion.getAplicacion().getTienda().getCliente().getCodpais().equals(COD_PAIS_PORTUGAL) && ticketManager.getTicket().getCabecera().getFiscalData() != null) {
						mapaParametros.put("esDuplicado", true);
						mapaParametros.put("esImpresionA4", false);
						aniadirLogoParametrosImprimir(mapaParametros);
						addQR(ticketManager.getTicket(), mapaParametros);
						ServicioImpresion.imprimir(formatoImpresion, mapaParametros);
					}
					

					if (hayPagosTarjeta) {
						if (VentanaDialogoComponent.crearVentanaConfirmacion(I18N.getTexto("¿Es correcta la impresión del recibo del pago con tarjeta?"), getStage())) {
							break;
						}
					}
					else {
						break;
					}
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

	private void aniadirLogoParametrosImprimir(Map<String, Object> mapaParametros) throws IOException {
		if (sesion.getAplicacion().getEmpresa().getLogotipo() != null) {
			InputStream is = new ByteArrayInputStream(sesion.getAplicacion().getEmpresa().getLogotipo());
			mapaParametros.put("LOGO", is);
			is.close();
		}
	}

	private void controlBotones() {
		if (((BricodepotTicketManager) ticketManager).esDevolucionFlexpoint()) {
			desactivarTodosLosbotones(false);
			panelBotoneraDatosAdicionales.setVisible(Boolean.TRUE);
		}
		else if (ticketManager.isEsDevolucion() && ticketManager.getTicketOrigen() != null) {
			desactivarTodosLosbotones(true);
			activarBotonComodin();
			List<PagoTicket> pagosOrigen = ticketManager.getTicketOrigen().getPagos();

			for (PagoTicket pagoTicket : pagosOrigen) {
				if (pagoTicket.getMedioPago().getCodMedioPago().equals("1000") || pagoTicket.getMedioPago().getCodMedioPago().equals("0020")) {
					activarBoton("0060");
				}
				else {
					activarBoton(pagoTicket.getCodMedioPago());
				}
			}
		}
		else {
			desactivarTodosLosbotones(false);
		}

	}

	private void activarBotonComodin() {
		// Se activan siempre los medios de pago efecrivo y vale //BRICO-264
		activarBoton("0000");
		activarBoton("0060");
	}

	private void desactivarTodosLosbotones(boolean desactivar) {
		BotoneraComponent botonera = (BotoneraComponent) panelMediosPago.getChildren().get(0);
		for (BotonBotoneraComponent boton : botonera.getMapConfiguracionesBotones().values()) {
			boton.setDisable(desactivar);
		}
	}

	private void activarBoton(String codMedioPago) {
		BotoneraComponent botonera = (BotoneraComponent) panelMediosPago.getChildren().get(0);
		for (BotonBotoneraComponent boton : botonera.getMapConfiguracionesBotones().values()) {

			if (((BotonBotoneraNormalComponent) boton).getConfiguracionBoton() != null && ((BotonBotoneraNormalComponent) boton).getConfiguracionBoton().getParametros() != null
			        && !((BotonBotoneraNormalComponent) boton).getConfiguracionBoton().getParametros().isEmpty()) {
				boton.setDisable(false);
			}
		}
	}

	@SuppressWarnings("rawtypes")
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

	@Override
	public void accionFactura() {
		log.debug("accionFactura()");
		// Comprobamos que se hayan cubierto los pagos
		log.debug("accionFactura() - Pagos cubiertos");
		try {
			if (ticketManager.comprobarConfigContador(Documentos.FACTURA_COMPLETA)) {
				getDatos().put(FacturacionArticulosController.TICKET_KEY, ticketManager);
				getApplication().getMainView().showModalCentered(FacturaView.class, getDatos(), this.getStage());
				lbDocActivo.setText(ticketManager.getDocumentoActivo().getDestipodocumento());
				cargarBotoneraDatosAdicionales();
			}
			else {
				ticketManager.crearVentanaErrorContador(getStage());
			}
		}
		catch (Exception e) {
			log.error("accionFactura() - Excepción no controlada : " + e.getCause(), e);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void aceptarPagos(boolean repiteOperacion) {
		if (!enProceso) {
			enProceso = true;
			log.debug("aceptar()");

			if ((((TicketVenta) ticketManager.getTicket()).isPagosCubiertos() && ticketManager.getDocumentoActivo().getRequiereCompletarPagos())
			        || !ticketManager.getDocumentoActivo().getRequiereCompletarPagos()) {
				log.debug("aceptar() - Pagos cubiertos");

				if (superaImporteMaximoEfectivo()) {
					VentanaDialogoComponent.crearVentanaError(
					        I18N.getTexto("No se puede realizar la venta. La cantidad que se quiere pagar en efectivo supera el máximo permitido ({0})", importeMaxEfectivo), getStage());
					enProceso = false;
					recuperarDatosIniciales(repiteOperacion);
					return;
				}

				if (!ticketManager.comprobarImporteMaximoOperacion(getStage())) {
					enProceso = false;
					recuperarDatosIniciales(repiteOperacion);
					return;
				}

				if (!ticketManager.comprobarCierreCajaDiarioObligatorio()) {
					String fechaCaja = FormatUtil.getInstance().formateaFecha(sesion.getSesionCaja().getCajaAbierta().getFechaApertura());
					String fechaActual = FormatUtil.getInstance().formateaFecha(new Date());
					VentanaDialogoComponent.crearVentanaError(
					        I18N.getTexto("No se puede realizar la venta. El día de apertura de la caja {0} no coincide con el del sistema {1}", fechaCaja, fechaActual), getStage());
					enProceso = false;
					recuperarDatosIniciales(repiteOperacion);
					return;
				}

				ticketManager.completaLineaDevolucionPunto();

				if (!ticketManager.isEsDevolucion()) {
					abrirVentanaCodigoPostal();
				}

				abrirVentanaCorreo(repiteOperacion);
				// ticketManager.salvarTicketSeguridad(getStage(), new SalvarTicketCallback(){
				//
				// @Override
				// public void onSucceeded() {
				// accionSalvarTicketSucceeded(repiteOperacion);
				// enProceso = false;
				// }
				//
				// @Override
				// public void onFailure(Exception e) {
				// accionSalvarTicketOnFailure(e);
				// recuperarDatosIniciales(repiteOperacion);
				// enProceso = false;
				// }
				// });

			}
			else {
				log.debug("aceptar() - Pagos no cubiertos");
				VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("Los pagos han de cubrir el importe a pagar."), this.getStage());
				recuperarDatosIniciales(repiteOperacion);
				enProceso = false;
			}
			if (esVentaTarjeta) {
				try {
					ticketManager.getTicket().getCabecera().setTarjetaRegalo(crearTarjetaRegalo());
				}
				catch (Exception e1) {
					log.error("aceptar() Error creando tarjeta regalo: " + e1.getMessage());
					VentanaDialogoComponent.crearVentanaError("Error creando la tarjeta regalo: " + e1.getMessage(), getStage());
					return;
				}
			}
		}
		else {
			log.warn("aceptar() - Pago en proceso");
		}
	}

	private TarjetaRegaloTicket crearTarjetaRegalo() throws Exception {
		log.debug("crearTarjetaRegalo() - Petición al servidor para crear la tarjeta regalo...");
		TarjetaRegaloTicket tarjetaTicket = new TarjetaRegaloTicket();

		try {
			String apiKey = variablesServices.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
			String uidActividad = sesion.getAplicacion().getUidActividad();
			TarjetaBean tarjeta = RestTarjetaClient.salvarTarjeta(apiKey, uidActividad, "0.0", tipoTarjeta);
			String uidTransaccion = UUID.randomUUID().toString();

			ListaMovimientoRequestRest request;
			request = createProvisionalMovements(new BigDecimal(recargoTarjeta), uidTransaccion, tarjeta);
			MovimientosRest.crearMovimientosTarjetaRegaloProvisionales(request);

			tarjetaTicket.setUidTransaccion(uidTransaccion);
			tarjetaTicket.setNumTarjetaRegalo(tarjeta.getNumeroTarjeta());
			tarjetaTicket.setImporteRecarga(new BigDecimal(recargoTarjeta));
			tarjetaTicket.setSaldo(new BigDecimal(recargoTarjeta));
			tarjetaTicket.setSaldoProvisional(BigDecimal.ZERO);

		}
		catch (RestException | RestHttpException e) {
			log.error("crearTarjetaRegalo() - Error creando movimiento: " + e.getMessage());
			throw new RestException(e.getMessage(), e);

		}
		catch (BadRequestException e) {
			log.error("crearTarjetaRegalo() - Error salvando tarjeta: " + e.getMessage());
			throw new BadRequestException(e.getMessage(), e);
		}
		catch (Exception e) {
			log.error("crearTarjetaRegalo() - Error salvando tarjeta: " + e.getMessage());
			throw new Exception(e.getMessage(), e);
		}

		return tarjetaTicket;
	}

	@Override
	public void accionCancelar() {
		super.accionCancelar();
		if (tipoTarjeta != null || recargoTarjeta != null) {
			getDatos().put(ACCION_CANCELAR_TARJETA, Boolean.TRUE);
		}
	}

	// TODO
	/* Se personaliza este metodo para poder meter logs */
	protected void recuperarDatosIniciales(boolean repiteOperacion) {
		if (repiteOperacion) {
			log.debug("recuperarDatosIniciales() - Se va a cambiar el documento activo. Documento activo actual " + ticketManager.getDocumentoActivo());
			ticketManager.setDocumentoActivo(tipoDocumentoInicial);
			ticketManager.getTicket().setPagos(new ArrayList<>(copiaPagos));
			ticketManager.getTicket().getLineas().clear();
			ticketManager.getTicket().getLineas().addAll(new ArrayList<>(lineasVentaAbono));
			((TicketVentaAbono) ticketManager.getTicket()).getTotales().recalcular();
		}
	}

	@Override
	protected void askGiftCardNumber(PaymentMethodManager source) {
		try {
			HashMap<String, Object> parametros = new HashMap<>();
			// Se sobreescribe el método para poder pasar el siguiente parámetro
			parametros.put(BricodepotCodigoTarjetaController.PARAMETRO_ES_DEVOLUCION, ticketManager.isEsDevolucion());
			parametros.put(BricodepotCodigoTarjetaController.PARAMETRO_ES_DEVOLUCION_FLEXPOINT, ((BricodepotTicketManager) ticketManager).esDevolucionFlexpoint());
			parametros.put(CodigoTarjetaController.PARAMETRO_IN_TEXTOCABECERA, I18N.getTexto("Lea o escriba el código de barras de la tarjeta de regalo"));
			String parametroTipoTarjeta = "GIFTCARD";
			if (source.getPaymentCode().equals("1000")) {
				parametroTipoTarjeta = "VALE";
			}

			parametros.put(CodigoTarjetaController.PARAMETRO_TIPO_TARJETA, parametroTipoTarjeta);
			String apiKey = variablesServices.getVariableAsString(Variables.WEBSERVICES_APIKEY);

			POSApplication posApplication = POSApplication.getInstance();
			posApplication.getMainView().showModalCentered(CodigoTarjetaView.class, parametros, getStage());

			String numTarjeta = (String) parametros.get(CodigoTarjetaController.PARAMETRO_NUM_TARJETA);
			ResponseGetTarjetaregaloRest tarjetaRest = new ResponseGetTarjetaregaloRest();

			if (StringUtils.isNotBlank(numTarjeta)) {
				String uidActividad = sesion.getAplicacion().getUidActividad();
				ConsultarFidelizadoRequestRest paramConsulta = new ConsultarFidelizadoRequestRest(apiKey, uidActividad);
				paramConsulta.setNumeroTarjeta(numTarjeta);

				ResponseGetFidelizadoRest result = FidelizadosRest.getTarjetaRegalo(paramConsulta);

				DatosSesionBean datosSesion = new DatosSesionBean();
				datosSesion.setUidActividad(sesion.getAplicacion().getUidActividad());
				datosSesion.setUidInstancia(sesion.getAplicacion().getUidInstancia());
				datosSesion.setLocale(new Locale(AppConfig.idioma, AppConfig.pais));
				TarjetasApi api = comerzziaApiManager.getClient(datosSesion, "TarjetasApi");
				tarjetaRest = api.validarTarjeta(numTarjeta);

				if (tarjetaRest.getTarjetaValida()) {
					GiftCardBean tarjetaRegalo = SpringContext.getBean(GiftCardBean.class);
					tarjetaRegalo.setNumTarjetaRegalo(result.getNumeroTarjeta());
					tarjetaRegalo.setBaja(result.getBaja().equals("S"));
					tarjetaRegalo.setActiva(result.getActiva().equals("S"));
					tarjetaRegalo.setSaldoProvisional(BigDecimal.ZERO);
					tarjetaRegalo.setSaldo(BigDecimal.valueOf(result.getSaldo()));
					tarjetaRegalo.setSaldoProvisional(BigDecimal.valueOf(result.getSaldoProvisional()));
					tarjetaRegalo.setCodTipoTarjeta(result.getTipoTarjeta() != null ? result.getTipoTarjeta().getCodtipotarj() : null);

					if (tarjetaRegalo != null) {
						if (tarjetaRegalo.isBaja()) {
							VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("La tarjeta introducida está dada de baja."), getStage());
						}
						else {
							MedioPagoBean medioPago = mediosPagosService.getMedioPago(source.getPaymentCode());

							lbSaldo.setText(I18N.getTexto("Saldo") + ": (" + FormatUtil.getInstance().formateaImporte(tarjetaRegalo.getSaldoTotal()) + ")");
							lbMedioPago.setText(medioPago.getDesMedioPago());
							String tipoTarjeta = result.getTipoTarjeta().getCodtipotarj();

							if (tipoTarjeta.equals("R") || tipoTarjeta.equals("P") || tipoTarjeta.equals("GC")) {
								MedioPagoBean medioPagoTarjetaRegalo = mediosPagosService.getMedioPago(COD_MP_GIFTCARD);
								if (medioPagoTarjetaRegalo == null) {
									throw new Exception("Esta forma de pago “Giftcard“ no está activa.");
								}
								if (ticketManager.getTicket().getCabecera().esVenta() && !medioPagoTarjetaRegalo.getVisibleVenta()) {
									throw new Exception("Esta forma de pago “Giftcard“ no está activa para la venta.");
								}
								else if (ticketManager.isEsDevolucion() && !medioPagoTarjetaRegalo.getVisibleDevolucion()) {
									throw new Exception("Esta forma de pago “Giftcard“ no está activa para la devolución.");
								}
								medioPagoSeleccionado = medioPagoTarjetaRegalo;
								PaymentMethodManager managerGiftcard = paymentsManager.getPaymentsMehtodManagerAvailables().get(COD_MP_GIFTCARD);
								managerGiftcard.addParameter(GiftCardManager.PARAM_TARJETA, tarjetaRegalo);
							}
							else if (tipoTarjeta.equals("AB") || tipoTarjeta.equals("ABC")) {
								MedioPagoBean medioPagoVale = mediosPagosService.getMedioPago(COD_MP_VALE);
								if (medioPagoVale == null) {
									throw new Exception("Esta forma de pago “Vale“ no está activa.");
								}
								if (ticketManager.getTicket().getCabecera().esVenta() && !medioPagoVale.getVisibleVenta()) {
									throw new Exception("Esta forma de pago “Vale“ no está activa para la venta.");
								}
								else if (ticketManager.isEsDevolucion() && !medioPagoVale.getVisibleDevolucion()) {
									throw new Exception("Esta forma de pago “Vale“ no está activa para la devolución.");
								}
								medioPagoSeleccionado = medioPagoVale;
								PaymentMethodManager managerVale = paymentsManager.getPaymentsMehtodManagerAvailables().get(COD_MP_VALE);
								managerVale.addParameter(GiftCardManager.PARAM_TARJETA, tarjetaRegalo);
							}

							GiftCardBean tarjetaRegaloPago = obtenerPagoTarjetaRegalo(tarjetaRegalo);

							if (tarjetaRegaloPago != null) {
								asociarPagoTarjetaRegalo(ticketManager.getTicket().getCabecera().esVenta(), tarjetaRegaloPago);
							}
							else {
								lbSaldo.setText(I18N.getTexto("Saldo") + ": (" + FormatUtil.getInstance().formateaImporte(tarjetaRegalo.getSaldoTotal()) + ")");
								if (ticketManager.getTicket().getCabecera().esVenta()) {
									if (BigDecimalUtil.isMayor(ticketManager.getTicket().getTotales().getPendiente(), tarjetaRegalo.getSaldoTotal())) {
										tfImporte.setText(FormatUtil.getInstance().formateaImporte(tarjetaRegalo.getSaldoTotal()));
									}
								}
							}
						}
					}
					source.addParameter(GiftCardManager.PARAM_TARJETA, tarjetaRegalo);
				}
				else {
					if (tarjetaRest.getErrorValidacion().equals("PREFIJO")) {
						VentanaDialogoComponent.crearVentanaError(I18N.getTexto("Esta tarjeta no es válida, prefijo incorrecto."), getStage());
						selectDefaultPaymentMethod();
					}
					else if (tarjetaRest.getErrorValidacion().equals("LONGITUD")) {
						VentanaDialogoComponent.crearVentanaError(I18N.getTexto("Esta tarjeta no es válida, longitud incorrecta."), getStage());
						selectDefaultPaymentMethod();
					}
					else if (tarjetaRest.getErrorValidacion().equals("FORMATO")) {
						VentanaDialogoComponent.crearVentanaError(I18N.getTexto("Esta tarjeta no es válida."), getStage());
						selectDefaultPaymentMethod();
					}
				}
			}
			else {
				selectDefaultPaymentMethod();
			}
		}
		catch (Exception e) {
			log.error("askGiftCardNumber() - Ha habido un error al pedir el número de tarjeta: " + e.getMessage(), e);

			if (e instanceof RestHttpException) {
				String message = I18N.getTexto("Lo sentimos, ha ocurrido un error en la petición.") + System.lineSeparator() + System.lineSeparator() + e.getMessage();
				VentanaDialogoComponent.crearVentanaError(getStage(), message, e);
			}
			else if (e instanceof RestConnectException) {
				VentanaDialogoComponent.crearVentanaError(getStage(), I18N.getTexto("No se ha podido conectar con el servidor"), e);
			}
			else if (e instanceof RestTimeoutException) {
				VentanaDialogoComponent.crearVentanaError(getStage(), I18N.getTexto("El servidor ha tardado demasiado tiempo en responder"), e);
			}
			else if (e instanceof RestException) {
				VentanaDialogoComponent.crearVentanaError(getStage(), I18N.getTexto("Lo sentimos, ha ocurrido un error en la petición"), e);
			}
			else {
				VentanaDialogoComponent.crearVentanaError(getStage(), I18N.getTexto(e.getMessage()), e);
			}

			selectDefaultPaymentMethod();
		}
	}

	private ListaMovimientoRequestRest createProvisionalMovements(BigDecimal amount, String uidTransaccion, TarjetaBean tarjeta) {
		List<MovimientoRequestRest> movimientos = new ArrayList<>();

		MovimientoRequestRest mov = new MovimientoRequestRest();
		mov.setUidActividad(sesion.getAplicacion().getUidActividad());
		mov.setNumeroTarjeta(tarjeta.getNumeroTarjeta());
		mov.setConcepto(I18N.getTexto("Saldo inicial"));
		mov.setUidTransaccion(uidTransaccion);
		mov.setFecha(new Date());
		mov.setDocumento(String.valueOf(ticketManager.getTicket().getIdTicket()));
		mov.setApiKey(variablesServices.getVariableAsString(Variables.WEBSERVICES_APIKEY));
		mov.setSaldo(tarjeta.getSaldo());
		mov.setSaldoProvisional(tarjeta.getSaldoProvisional());
		mov.setSalida(0.0);
		mov.setEntrada(amount.doubleValue());
		movimientos.add(mov);

		ListaMovimientoRequestRest request = new ListaMovimientoRequestRest();
		request.setMovimientos(movimientos);

		return request;
	}

	private void abrirVentanaCodigoPostal() {
		log.debug("abrirVentanaCodigoPostal() - Abriendo pantalla para introducir codigo postal");
		HashMap<String, Object> parametros = new HashMap<>();
		getApplication().getMainView().showModalCentered(CodigoPostalView.class, parametros, this.getStage());

		if (parametros.get(CodigoPostalController.PARAMETRO_CODIGO_POSTAL) != null) {
			String codigoPostalInsertado = (String) parametros.get(CodigoPostalController.PARAMETRO_CODIGO_POSTAL);
			((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).setVentaCodigoPostal(codigoPostalInsertado);

			parametros.remove(CodigoPostalController.PARAMETRO_CODIGO_POSTAL);
		}
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

	private void abrirVentanaCorreo(boolean repiteOperacion) {
		log.debug("abrirVentanaCorreo()");
		boolean tienePaperLess = false;
		String emailFidelizadoCargado = null;

		if (ticketManager.getTicket().getCabecera().getDatosFidelizado() != null) {
			tienePaperLess = Boolean.TRUE.equals(ticketManager.getTicket().getCabecera().getDatosFidelizado().getPaperLess());
			log.debug("abrirVentanaCorreo() - Fidelizado identificado. ¿Tiene PaperLess? " + tienePaperLess);

		}

		if (!tienePaperLess) {

			/*
			 * En el caso de que se trate de una venta fidelizada, mostraremos en pantalla el correo del fidelizado
			 * cargado
			 */
			if (ticketManager.getTicket().getCabecera().getDatosFidelizado() != null && ticketManager.getTicket().getCabecera().getDatosFidelizado().getAdicionales() != null
			        && !ticketManager.getTicket().getCabecera().getDatosFidelizado().getAdicionales().isEmpty()) {

				if (ticketManager.getTicket().getCabecera().getDatosFidelizado().getAdicionales().get(EMAIL_FIDELIZADO_CARGADO) != null) {
					emailFidelizadoCargado = (String) ticketManager.getTicket().getCabecera().getDatosFidelizado().getAdicionales().get(EMAIL_FIDELIZADO_CARGADO);

					getDatos().put(EMAIL_FIDELIZADO_CARGADO, emailFidelizadoCargado);
				}

			}
			getApplication().getMainView().showModalCentered(SeleccionEnvioTicketView.class, getDatos(), getStage());

			accionEmail = (String) getDatos().get(EmailController.TIPO_ENVIO);
			if (StringUtils.isNotBlank(accionEmail)) {
				((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).setTipoImpresion(accionEmail);
			}
			String email = (String) getDatos().get(EmailController.EMAIL);
			if (StringUtils.isNotBlank(email)) {
				log.debug("abrirVentanaCorreo() - Email para realizar el envio del ticket: " + email);

				((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).setEmailEnvioTicket(email);
			}

			salvarTicket(repiteOperacion);
		}
		else {
			buscarFidelizadoCentral(repiteOperacion);
		}

	}

	private void buscarFidelizadoCentral(boolean repiteOperacion) {
		log.debug("buscarFidelizadoCentral() - Se realiza busqueda en central del fidelizado para obtener su email");
		String apiKey = variablesServices.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
		String uidActividad = sesion.getAplicacion().getUidActividad();
		Long idFidelizado = ticketManager.getTicket().getCabecera().getDatosFidelizado().getIdFidelizado();

		FidelizadoRequestRest requestRest = new FidelizadoRequestRest(apiKey, uidActividad, idFidelizado);

		ConsultarFidelizadoPorIdTask consultarFidelizadoTask = SpringContext.getBean(ConsultarFidelizadoPorIdTask.class, requestRest, new RestBackgroundTask.FailedCallback<FidelizadoBean>(){

			@Override
			public void succeeded(FidelizadoBean result) {
				// check campos faltantes
				TiposContactoFidelizadoBean email = result.getTipoContacto("EMAIL");
				if (email != null) {
					log.debug("buscarFidelizadoCentral() - Email obtenido: " + email.getValor());
					((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).setEmailEnvioTicket(email.getValor());

					VentanaDialogoComponent.crearVentanaInfo(I18N.getTexto("Su ticket se enviará al correo:") + " " + email.getValor(), getStage());
				}

				salvarTicket(repiteOperacion);
			}

			@Override
			public void failed(Throwable throwable) {
			}
		}, getStage());

		consultarFidelizadoTask.start();
	}

	private void salvarTicket(boolean repiteOperacion) {
		log.debug("salvarTicket()");
		ticketManager.salvarTicketSeguridad(getStage(), new SalvarTicketCallback(){

			@Override
			public void onSucceeded() {
				accionSalvarTicketSucceeded(repiteOperacion);
				enProceso = false;
			}

			@Override
			public void onFailure(Exception e) {
				accionSalvarTicketOnFailure(e);
				recuperarDatosIniciales(repiteOperacion);
				enProceso = false;
			}
		});

	}

	@Override
	protected void cargarBotoneraDatosAdicionales() {
		if (!((BricodepotTicketManager) ticketManager).isConversionFlexpointFT()) {
			super.cargarBotoneraDatosAdicionales();
		}
	}

	@Override
	public void seleccionarMedioPago(HashMap<String, String> parametros) {
		if (parametros.containsKey("codMedioPago")) {
			String codMedioPago = parametros.get("codMedioPago");
			MedioPagoBean medioPago = mediosPagosService.getMedioPago(codMedioPago);

			if (medioPago != null) {
				medioPagoSeleccionado = medioPago;
				if (!((BricodepotTicketManager) ticketManager).isConversionFlexpointFT()) {
					paymentsManager.select(medioPago.getCodMedioPago());
				}
				lbMedioPago.setText(medioPago.getDesMedioPago());

				if (parametros.containsKey("valor")) {
					String valor = parametros.get("valor");
					try {
						BigDecimal importe = new BigDecimal(valor);
						anotarPago(importe);
					}
					catch (Exception e) {
						log.error("El valor configurado no se puede formatear: " + valor);
					}
				}
			}
			else {
				VentanaDialogoComponent.crearVentanaError(I18N.getTexto("Ha habido un error al recuperar el medio de pago"), getStage());
				log.error("No se ha encontrado el medio de pago con código: " + codMedioPago);
			}

		}
		else {
			VentanaDialogoComponent.crearVentanaError(I18N.getTexto("No se ha especificado una acción correcta para este botón"), getStage());
			log.error("No existe el código del medio de pago para este botón.");
		}
	}

	@Override
	protected void incluirPagoTicket(BigDecimal importe) {
		if (!((BricodepotTicketManager) ticketManager).isConversionFlexpointFT()) {
			super.incluirPagoTicket(importe);
		}
		else {
			completarPagoParaFlexpointFT(importe);
		}
	}

	@SuppressWarnings("rawtypes")
	private void completarPagoParaFlexpointFT(BigDecimal importe) {
		log.debug("completarPagoParaFlexpointFT() - Completando pago para flexpoint con importe: " + importe);
		MedioPagoBean medioPago = new MedioPagoBean();
		BeanUtils.copyProperties(medioPagoSeleccionado, medioPago);
		String codMedioPago = medioPago.getCodMedioPago();
		PagoTicket pago = new PagoTicket();
		pago.setPaymentId(paymentId);
		pago.setCodMedioPago(codMedioPago);
		pago.setDesMedioPago(medioPago.getDesMedioPago());
		pago.setImporte(importe);
		pago.setMedioPago(medioPago);
		pago.setEliminable(true);
		
		if (pago.getCodMedioPago().equals(COD_MP_GIFTCARD) || pago.getCodMedioPago().equals(COD_MP_VALE)) {
			getApplication().getMainView().showModalCentered(CodigoTarjetaView.class, getDatos(), this.getStage());
			if (getDatos().get(CodigoTarjetaController.PARAMETRO_NUM_TARJETA) != null) {
				String codigo = (String) getDatos().get(CodigoTarjetaController.PARAMETRO_NUM_TARJETA);
				Map<String, Object> extendedData = new HashMap<String, Object>();
				extendedData.put(CodigoTarjetaController.PARAMETRO_NUM_TARJETA, codigo);
				pago.setExtendedData(extendedData);
			}
			else {
				return;
			}
		}else if(pago.getCodMedioPago().equals(COD_MP_GIFTCARD_FLEX)){
			// BRICOD-853 Pagos giftcard en conversiones flexpoint con codigo 1000
			pago.setCodMedioPago(COD_MP_VALE);
			pago.getMedioPago().setCodMedioPago(COD_MP_VALE);
			
			
		}
		ticketManager.getTicket().getPagos().add(pago);
		ticketManager.getTicket().getTotales().recalcular();

		tfImporte.setText(ticketManager.getTicket().getTotales().getPendienteAsString());
		visor.modoPago(visorConverter.convert((TicketVenta) ticketManager.getTicket()));
		escribirVisor();
		tfImporte.requestFocus();
		paymentId++;
		ticketManager.guardarCopiaSeguridadTicket();
		refrescarDatosPantalla();
	}

	@Override
	protected void accionBorrarRegistroTabla() {
		if (!((BricodepotTicketManager) ticketManager).isConversionFlexpointFT()) {
			super.accionBorrarRegistroTabla();
		}
		else {
			borrarRegistroTablaParaFlexpointFT();
		}
	}

	private void borrarRegistroTablaParaFlexpointFT() {

		PagoTicketGui gui = tbPagos.getSelectionModel().getSelectedItem();
		if (gui == null) {
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("No hay ningún pago seleccionado."), getStage());
			return;
		}
		List<PagoTicket> listaPagos = ticketManager.getTicket().getPagos();
		PagoTicket pago = null;
		for (PagoTicket pagoTicket : listaPagos) {
			if (pagoTicket.getPaymentId() == gui.getPaymentId()) {
				pago = pagoTicket;
				break;
			}
		}
		listaPagos.remove(pago);
		ticketManager.getTicket().getTotales().recalcular();
		ticketManager.guardarCopiaSeguridadTicket();
		refrescarDatosPantalla();
	}

	private void addPagoGiftcard(Map<String, Object> mapaParametros) {
		List<PagoTicket> listaPagos = ticketManager.getTicket().getPagos();
		BigDecimal pagoGiftcard = new BigDecimal(0);
		String uidTransaccion = "";
		BigDecimal saldo = new BigDecimal(0);
//		BigDecimal importePago = new BigDecimal(0);
		BigDecimal importeRecarga = new BigDecimal(0);
		TarjetaRegaloTicket tarjetaRegalo = new TarjetaRegaloTicket();
		for (PagoTicket pago : listaPagos) {
			if (pago.getGiftcards() != null && !pago.getGiftcards().isEmpty()) {
				uidTransaccion = pago.getGiftcards().get(0).getUidTransaccion();
				saldo = pago.getGiftcards().get(0).getSaldo();
//				importePago = pago.getGiftcards().get(0).getImportePago();
				importeRecarga = pago.getGiftcards().get(0).getImporteRecarga();
				pagoGiftcard = pago.getGiftcards().get(0).getImportePago();

			}
		}
		tarjetaRegalo.setUidTransaccion(uidTransaccion);
		tarjetaRegalo.setSaldo(saldo);
		tarjetaRegalo.setImporteRecarga(importeRecarga);
		ticketManager.getTicket().getCabecera().setTarjetaRegalo(tarjetaRegalo);
		mapaParametros.put("pagoGiftcard", pagoGiftcard);
	}

	private void mostrarLogXML() {
		try {
			if (ticketManager.getTicket().getLineas() != null && !ticketManager.getTicket().getLineas().isEmpty() && !(ticketManager.getTicket().getLineas().get(0) instanceof BricodepotLineaTicket)) {
				byte[] xmlTicket = MarshallUtil.crearXML(ticketManager.getTicket(), getTicketClasses());
				log.debug("recuperarCopiaSeguridadTicket() - XML TICKET:");
				log.debug(new String(xmlTicket, "UTF-8") + "\n");
			}
		}
		catch (Exception ignore) {
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

}
