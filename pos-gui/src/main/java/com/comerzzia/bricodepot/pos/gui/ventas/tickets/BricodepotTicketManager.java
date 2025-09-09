package com.comerzzia.bricodepot.pos.gui.ventas.tickets;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.comerzzia.api.model.sales.ArticulosDevueltosBean;
import com.comerzzia.api.rest.client.tickets.ResponseGetTicketDev;
import com.comerzzia.bricodepot.pos.persistence.motivos.Motivo;
import com.comerzzia.bricodepot.pos.services.ticket.BricodepotCabeceraTicket;
import com.comerzzia.bricodepot.pos.services.ticket.BricodepotTicketVentaAbono;
import com.comerzzia.bricodepot.pos.services.ticket.audit.TicketAuditEvent;
import com.comerzzia.bricodepot.pos.services.ticket.audit.TicketAuditService;
import com.comerzzia.bricodepot.pos.services.ticket.lineas.BricodepotLineaTicket;
import com.comerzzia.pos.core.dispositivos.Dispositivos;
import com.comerzzia.pos.core.dispositivos.dispositivo.balanza.BalanzaNoConfig;
import com.comerzzia.pos.core.dispositivos.dispositivo.balanza.IBalanza;
import com.comerzzia.pos.core.dispositivos.dispositivo.fidelizacion.ConsultaTarjetaFidelizadoException;
import com.comerzzia.pos.core.gui.POSApplication;
import com.comerzzia.pos.gui.ventas.tickets.TicketManager;
import com.comerzzia.pos.gui.ventas.tickets.articulos.balanza.SolicitarPesoArticuloController;
import com.comerzzia.pos.gui.ventas.tickets.articulos.balanza.SolicitarPesoArticuloView;
import com.comerzzia.pos.persistence.codBarras.CodigoBarrasBean;
import com.comerzzia.pos.persistence.core.conceptosalmacen.ConceptoAlmacenBean;
import com.comerzzia.pos.persistence.core.documentos.tipos.TipoDocumentoBean;
import com.comerzzia.pos.persistence.core.usuarios.UsuarioBean;
import com.comerzzia.pos.persistence.fidelizacion.CustomerCouponDTO;
import com.comerzzia.pos.persistence.fidelizacion.FidelizacionBean;
import com.comerzzia.pos.persistence.tickets.aparcados.TicketAparcadoBean;
import com.comerzzia.pos.services.articulos.ArticuloNotFoundException;
import com.comerzzia.pos.services.codBarrasEsp.CodBarrasEspecialesServices;
import com.comerzzia.pos.services.core.conceptosalmacen.ConceptoNotFoundException;
import com.comerzzia.pos.services.core.conceptosalmacen.ConceptoService;
import com.comerzzia.pos.services.core.conceptosalmacen.ConceptoServiceException;
import com.comerzzia.pos.services.core.documentos.DocumentoException;
import com.comerzzia.pos.services.core.documentos.Documentos;
import com.comerzzia.pos.services.core.sesion.SesionImpuestos;
import com.comerzzia.pos.services.core.sesion.SesionPromociones;
import com.comerzzia.pos.services.core.usuarios.UsuarioNotFoundException;
import com.comerzzia.pos.services.core.usuarios.UsuariosService;
import com.comerzzia.pos.services.core.usuarios.UsuariosServiceException;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.services.cupones.CuponAplicationException;
import com.comerzzia.pos.services.cupones.CuponUseException;
import com.comerzzia.pos.services.cupones.CuponesServiceException;
import com.comerzzia.pos.services.mediospagos.MediosPagosService;
import com.comerzzia.pos.services.promociones.PromocionesServiceException;
import com.comerzzia.pos.services.ticket.TicketVenta;
import com.comerzzia.pos.services.ticket.TicketVentaAbono;
import com.comerzzia.pos.services.ticket.TicketsServiceException;
import com.comerzzia.pos.services.ticket.aparcados.TicketsAparcadosService;
import com.comerzzia.pos.services.ticket.cabecera.CabeceraTicket;
import com.comerzzia.pos.services.ticket.cabecera.DatosDocumentoOrigenTicket;
import com.comerzzia.pos.services.ticket.cabecera.TotalesTicket;
import com.comerzzia.pos.services.ticket.lineas.LineaTicket;
import com.comerzzia.pos.services.ticket.lineas.LineaTicketAbstract;
import com.comerzzia.pos.services.ticket.lineas.LineaTicketException;
import com.comerzzia.pos.services.ticket.lineas.LineasTicketServices;
import com.comerzzia.pos.services.ticket.pagos.PagoTicket;
import com.comerzzia.pos.util.bigdecimal.BigDecimalUtil;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.format.FormatUtil;
import com.comerzzia.pos.util.i18n.I18N;
import com.comerzzia.pos.util.xml.MarshallUtil;

import javafx.stage.Stage;

@Primary
@Component
@Scope("prototype")
public class BricodepotTicketManager extends TicketManager{

	/* Constantes para las propiedades fiscales */
	public static final String PROPERTY_QR = "QR";
	public static final String PROPERTY_ATCUD = "ATCUD";
	public static final String EXTENSION_RANGE_ID = "RANGE_ID";
	
	public static final String PROPIEDAD_CODIGO_ANTICIPO = "POS.ARTICULO_ANTICIPO";

	@Autowired
	protected TicketAuditService ticketAuditService;
	
	@Autowired
    private SesionPromociones sesionPromociones;
	
	@Autowired
    private CodBarrasEspecialesServices codBarrasEspecialesServices;
	
	@Autowired
	private LineasTicketServices lineasTicketServices;
	
	@Autowired
	private UsuariosService usuariosService;
	
	@Autowired
	private MediosPagosService mediosPagosService;
	
	@Autowired
	private TicketsAparcadosService ticketsAparcadosService;
	
	@Autowired
	private ConceptoService conceptoService;
	
	@Autowired
	private VariablesServices variablesServices;

	public static final String UID_DEVOLUCION_FLEXPOINT = "UID_DEVOLUCION_FLEXPOINT";
	private boolean esDevolucionFlexpoint; 
	private boolean esConversionFlexpointFT;
	
	@Override
	public void inicializarTicket() throws DocumentoException, PromocionesServiceException {
		super.inicializarTicket();
		esDevolucionFlexpoint = false;
		esConversionFlexpointFT = false;
	}
	
	@Override
	public void eliminarTicketCompleto()
			throws TicketsServiceException, PromocionesServiceException, DocumentoException {
		List<TicketAuditEvent> auditEvents = new ArrayList<>();
		//TicketAuditEvent eventoAnular = TicketAuditEvent.forEvent(TicketAuditEvent.Type.ANULACION_TICKET, sesion);
		if(((BricodepotCabeceraTicket)this.ticketPrincipal.getCabecera()).getAuditEvents()!=null) {
			auditEvents = ((BricodepotCabeceraTicket)this.ticketPrincipal.getCabecera()).getAuditEvents();
		}
		//auditEvents.add(eventoAnular);
		
		for(TicketAuditEvent auditEvent : auditEvents) {
			auditEvent.setUidTicketVenta(this.ticketPrincipal.getUidTicket());
			ticketAuditService.saveAuditEvent(auditEvent);
		}
		
		super.eliminarTicketCompleto();
	}
	
	public void clearAuditEvents() {
		((BricodepotCabeceraTicket)this.ticketPrincipal.getCabecera()).setAuditEvents(new ArrayList<>());
	}

	
	@SuppressWarnings("unchecked")
	protected boolean tratarTicketRecuperado(byte[] ticketRecuperado) throws TicketsServiceException {
    	try{
	        log.debug(new String(ticketRecuperado, "UTF-8"));
	        
	        contadorLinea = 1;
	        boolean resultado = false;

			nuevoTicket();

			// Realizamos el unmarshall
			ticketOrigen = (TicketVentaAbono) MarshallUtil.leerXML(ticketRecuperado, getTicketClasses(documentoActivo).toArray(new Class[]{}));

			TipoDocumentoBean docAbono = documentos.getDocumentoAbono(ticketOrigen.getCabecera().getCodTipoDocumento());

			setEsDevolucion(true);
			
			log.debug("tratarTicketRecuperado() - Se va a cambiar el documento activo. Documento activo actual " + getDocumentoActivo());
			setDocumentoActivo(docAbono);

			if (!documentos.isDocumentosAbonoConfigurados()) {
				List<String> tiposDoc = docAbono.getTiposDocumentosOrigen();
				boolean codTipoDocumentoValido = false;
				for (String codTipoDocumento : tiposDoc) {
					if (codTipoDocumento.equals(ticketOrigen.getCabecera().getCodTipoDocumento())) {
						codTipoDocumentoValido = true;
						break;
					}
				}
				if (!codTipoDocumentoValido) {
					throw new TicketsServiceException(String.format("El documento obtenido '%s' no se encuentra entre los documentos de origen del tipo '%s'",
							ticketOrigen.getCabecera().getCodTipoDocumento(), docAbono.getCodtipodocumento()));
				}
			}
	        
	        if(ticketOrigen!=null ){
                //Se tiene que crear este DatosDocumentoOrigen antes de inicializar los datos de la cabecera con los datos de la devoluciÃ³n
                DatosDocumentoOrigenTicket datosOrigen = new DatosDocumentoOrigenTicket();
                datosOrigen.setCaja(ticketOrigen.getCabecera().getCodCaja());
                datosOrigen.setCodTipoDoc(ticketOrigen.getCabecera().getCodTipoDocumento());
                datosOrigen.setIdTipoDoc(ticketOrigen.getCabecera().getTipoDocumento());
                datosOrigen.setNumFactura(ticketOrigen.getIdTicket());
                datosOrigen.setSerie(ticketOrigen.getCabecera().getTienda().getCodAlmacen());
                
                datosOrigen.setFecha(ticketOrigen.getCabecera().getFechaAsLocale());
				((BricodepotCabeceraTicket) ticketPrincipal.getCabecera()).setFechaTicketOrigen(ticketOrigen.getCabecera().getFechaAsLocale());
				
                datosOrigen.setDesTipoDoc(ticketOrigen.getCabecera().getDesTipoDocumento());
                datosOrigen.setUidTicket(ticketOrigen.getUidTicket());
                datosOrigen.setCodTicket(ticketOrigen.getCabecera().getCodTicket());
                datosOrigen.setTienda(ticketOrigen.getTienda().getCodAlmacen());
                
                ticketPrincipal.getCabecera().setUidTicketEnlace(ticketOrigen.getUidTicket());
                // Seteamos las referencias internas de Cabecera y totales hacia el ticket
                ticketPrincipal.getCabecera().inicializarCabecera(ticketPrincipal);
                ticketPrincipal.setCajero(sesion.getSesionUsuario().getUsuario());
                ticketPrincipal.getCabecera().getTotales().setCambio(SpringContext.getBean(PagoTicket.class, MediosPagosService.medioPagoDefecto));
                ticketPrincipal.getCabecera().setDatosDocOrigen(datosOrigen);
                ticketPrincipal.getCabecera().setDatosEnvio(ticketOrigen.getCabecera().getDatosEnvio());
                
                ticketPrincipal.setCliente(ticketOrigen.getCliente());
                
                documentoActivo = docAbono;
                
                // Establecemos los parÃ¡metros de tipo de documento del ticket
                ticketPrincipal.getCabecera().setTipoDocumento(documentoActivo.getIdTipoDocumento());
                ticketPrincipal.getCabecera().setCodTipoDocumento(documentoActivo.getCodtipodocumento());
                ticketPrincipal.getCabecera().setFormatoImpresion(documentoActivo.getFormatoImpresion());
                
                //Establecemos los datos de la cabecera personalizados
                
                String importeAnticipo = ((BricodepotCabeceraTicket) ticketOrigen.getCabecera()).getImporteAnticipo();
                String numAnticipo = ((BricodepotCabeceraTicket) ticketOrigen.getCabecera()).getNumAnticipo();
                String operacionAnticipo = ((BricodepotCabeceraTicket) ticketOrigen.getCabecera()).getOperacionAnticipo();
                Long idClieAlbaranAnticipo = ((BricodepotCabeceraTicket) ticketOrigen.getCabecera()).getIdClieAlbaranAnticipo();
                
                ((BricodepotCabeceraTicket) ticketPrincipal.getCabecera()).setNumAnticipo(numAnticipo);
                ((BricodepotCabeceraTicket) ticketPrincipal.getCabecera()).setOperacionAnticipo(operacionAnticipo);
                ((BricodepotCabeceraTicket) ticketPrincipal.getCabecera()).setIdClieAlbaranAnticipo(idClieAlbaranAnticipo);
                ((BricodepotCabeceraTicket) ticketPrincipal.getCabecera()).setImporteAnticipo(importeAnticipo);
                
                
                resultado = true;
                
	            ticketPrincipal.getCabecera().setDatosFidelizado(ticketOrigen.getCabecera().getDatosFidelizado());
	        }
	        
	        return resultado;
    	}catch(Exception e){
    		throw new TicketsServiceException(new com.comerzzia.core.util.base.Exception(I18N.getTexto("Lo sentimos, ha ocurrido un error al recuperar el ticket"), e));
    	}
    }
	
	/* [BRICO-78] - Devoluciones de tickets de Flexpoint */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void crearTicketOrigenFlexpoint(String idDocumento, String codDoc) throws DocumentoException {
		log.debug("crearTicketOrigenVacio() - Creando nuevo ticket con valores iniciales...");
		documentoActivo = getNuevoDocumentoActivo();
		if(StringUtils.equals(Documentos.FACTURA_COMPLETA, codDoc)) {
			documentoActivo = sesion.getAplicacion().getDocumentos().getDocumento(Documentos.FACTURA_COMPLETA);
		}
        ticketOrigen = (TicketVenta) SpringContext.getBean(getTicketClass(documentoActivo));
        ticketOrigen.getCabecera().inicializarCabecera(ticketPrincipal);
        ((TicketVentaAbono)ticketOrigen).inicializarTotales();
        ticketOrigen.setCliente(sesion.getAplicacion().getTienda().getCliente().clone());
        ticketOrigen.setCajero(sesion.getSesionUsuario().getUsuario());
        ticketOrigen.getCabecera().getTotales().setCambio(SpringContext.getBean(PagoTicket.class , MediosPagosService.medioPagoDefecto));
        ticketOrigen.getTotales().recalcular();
        ticketOrigen.getCabecera().setCodTipoDocumento(codDoc);
        ticketOrigen.getCabecera().setDocumento(documentoActivo);
        ticketOrigen.getCabecera().setCodTicket(idDocumento);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void crearTicketOrigenFlexPointParaConversion(String idDocumento,String codTicket, Date fechaIndicada) throws DocumentoException {

		documentoActivo = getNuevoDocumentoActivo();
        ticketOrigen = (TicketVenta) SpringContext.getBean(getTicketClass(documentoActivo));
        ticketOrigen.getCabecera().inicializarCabecera(ticketPrincipal);
        ((TicketVentaAbono)ticketOrigen).inicializarTotales();
        ticketOrigen.setCliente(sesion.getAplicacion().getTienda().getCliente().clone());
        ticketOrigen.setCajero(sesion.getSesionUsuario().getUsuario());
        ticketOrigen.getCabecera().getTotales().setCambio(SpringContext.getBean(PagoTicket.class , MediosPagosService.medioPagoDefecto));
        ticketOrigen.getTotales().recalcular();
        ticketOrigen.getCabecera().setCodTipoDocumento("FS");
        ticketOrigen.getCabecera().setDocumento(documentoActivo);
        ticketOrigen.getCabecera().setCodTicket(idDocumento);
        DatosDocumentoOrigenTicket dot = new DatosDocumentoOrigenTicket();
		dot.setSerie(null);
		dot.setCaja(null);
		dot.setNumFactura(null);
		dot.setIdTipoDoc(null);
		dot.setCodTipoDoc("FLEX");
		dot.setDesTipoDoc("FLEXPOINT");
		dot.setUidTicket(UUID.randomUUID().toString());			
		dot.setCodTicket(codTicket);
		String fechaTicket = FormatUtil.getInstance().formateaFechaCorta(fechaIndicada);
		String horaTicket = FormatUtil.getInstance().formateaHora(fechaIndicada);
		String fechaFormateada = fechaTicket + " " + horaTicket;
		dot.setFecha(fechaFormateada);
		dot.setTienda(null);
		dot.setRecoveredOnline(false);
		ticketOrigen.getCabecera().setDatosDocOrigen(dot);
		ticketPrincipal.getCabecera().setDatosDocOrigen(dot);
	}
	public void rellenarDatosDocOrigenFlexpoint() throws DocumentoException {
		DatosDocumentoOrigenTicket datos = new DatosDocumentoOrigenTicket();
		datos.setCodTicket(ticketOrigen.getCabecera().getCodTicket());
		datos.setUidTicket(UID_DEVOLUCION_FLEXPOINT);
		datos.setNumFactura(0L);
		datos.setCaja(sesion.getAplicacion().getCodCaja());

		TipoDocumentoBean documentoActivoTicketFalso = sesion.getAplicacion().getDocumentos().getDocumento(ticketOrigen.getCabecera().getCodTipoDocumento());
		datos.setIdTipoDoc(documentoActivoTicketFalso.getIdTipoDocumento());
		datos.setCodTipoDoc(documentoActivoTicketFalso.getCodtipodocumento());
		datos.setDesTipoDoc(documentoActivoTicketFalso.getDestipodocumento());

		SimpleDateFormat format = new SimpleDateFormat("yyyy");
		String serie = documentoActivoTicketFalso.getCodtipodocumento() + " " + format.format(new Date()) + sesion.getAplicacion().getTienda().getCodAlmacen() + sesion.getAplicacion().getCodCaja();
		datos.setSerie(serie);
		
		ticketPrincipal.getCabecera().setDatosDocOrigen(datos);
		ticketPrincipal.setEsDevolucion(true);
	}
	
	@SuppressWarnings("rawtypes")
	public synchronized LineaTicket nuevaLineaArticuloFlexpoint(String codArticulo, String desglose1, String desglose2, BigDecimal cantidad, Stage stage, boolean esLineaDevolucionPositiva,
	        boolean applyDUN14Factor) throws LineaTicketException {
		log.debug("nuevaLineaArticulo() - Creando nueva línea de artículo...");
		LineaTicketAbstract linea = null;

		boolean isCupon = sesionPromociones.isCouponWithPrefix(codArticulo);
		if (isCupon) {
			try {
				CustomerCouponDTO customerCouponDTO = new CustomerCouponDTO(codArticulo, true);
				if (!sesionPromociones.aplicarCupon(customerCouponDTO, (TicketVentaAbono) ticketPrincipal)) {
					throw new LineaTicketException(I18N.getTexto("No se ha podido aplicar el cupón."));
				}
				ticketPrincipal.getTotales().recalcular();
			}
			catch (CuponAplicationException | CuponUseException | CuponesServiceException ex) {
				log.warn("nuevaLineaArticulo() - Error en la aplicación del cupón -" + ex.getMessageI18N());
				throw new LineaTicketException(ex.getMessageI18N(), ex);
			}
		}
		else {
			BigDecimal precio = null;

			boolean pesarArticulo = stage != null;

			String codBarras = null;

			if (!esDevolucion) {
				// Comprobamos si es codigo de barras especial o normal y actualizamos codigoArticulo y otras variables
				try {
					CodigoBarrasBean codBarrasEspecial = codBarrasEspecialesServices.esCodigoBarrasEspecial(codArticulo);

					if (codBarrasEspecial != null) {

						codBarras = codArticulo;

						// Ponemos la variable a falsa ya que se cogerá el peso del código de barras
						pesarArticulo = false;

						if (codBarrasEspecial.getCodticket() != null) {
							return tratarCodigoBarraEspecialTicket(codBarrasEspecial);
						}

						codArticulo = codBarrasEspecial.getCodart();
						String cantCodBar = codBarrasEspecial.getCantidad();
						if (cantCodBar != null) {
							cantidad = FormatUtil.getInstance().desformateaBigDecimal(cantCodBar, 3);
						}
						else {
							cantidad = BigDecimal.ONE;
						}
						String precioCodBar = codBarrasEspecial.getPrecio();
						if (precioCodBar != null) {
							precio = FormatUtil.getInstance().desformateaBigDecimal(codBarrasEspecial.getPrecio(), 2);
						}
						else {
							precio = null;
						}

						if (codArticulo == null) {
							log.error(String.format("nuevaLineaArticulo() - El código de barra especial obtenido no es válido. CodArticulo: %s, cantidad: %s, precio: %s", codArticulo, cantidad,
							        precio));
							throw new LineaTicketException(I18N.getTexto("Error procesando el código de barras. Revise configuración."));
						}
					}
				}
				catch (LineaTicketException e) {
					throw e;
				}
				catch (Exception e) {
					log.error("Error procesando el código de barras especial : " + codArticulo, e);
					throw new LineaTicketException(I18N.getTexto("Error procesando el código de barras. Revise configuración."));
				}
			}

			try {
				linea = lineasTicketServices.createLineaArticulo((TicketVenta) ticketPrincipal, codArticulo, desglose1, desglose2, cantidad, precio, createLinea(), applyDUN14Factor);
				linea.setCantidad(tratarSignoCantidad(linea.getCantidad(), linea.getCabecera().getCodTipoDocumento()));
				if (esLineaDevolucionPositiva) {
					linea.setCantidad(linea.getCantidad().abs());
				}

				if (codBarras != null) {
					linea.setCodigoBarras(codBarras);
				}

				// Si el artículo tiene en su campo FORMATO en BBDD...
				if (pesarArticulo && StringUtils.isNotBlank(linea.getArticulo().getBalanzaTipoArticulo()) && linea.getArticulo().getBalanzaTipoArticulo().trim().toUpperCase().equals(PESAR_ARTICULO)) {
					IBalanza balanza = Dispositivos.getInstance().getBalanza();
					if (!(balanza instanceof BalanzaNoConfig)) {
						HashMap<String, Object> params = new HashMap<String, Object>();
						POSApplication.getInstance().getMainView().showModalCentered(SolicitarPesoArticuloView.class, params, stage);
						if (params.containsKey(SolicitarPesoArticuloController.PARAM_PESO)) {
							BigDecimal peso = (BigDecimal) params.get(SolicitarPesoArticuloController.PARAM_PESO);

							if (peso == null || BigDecimalUtil.isMenorOrIgualACero(peso)) {
								throw new LineaTicketException(I18N.getTexto("No se ha podido pesar el artículo, compruebe la configuración de la balanza."));
							}

							linea.setCantidad(peso);
						}
						else {
							throw new LineaTicketException(I18N.getTexto("Este artículo no puede ser introducido sin ser pesado previamente."));
						}
					}
				}
				linea.resetPromociones();
				linea.recalcularImporteFinal();

				addLinea(linea);
				ticketPrincipal.getTotales().recalcular();
			}
			catch (ArticuloNotFoundException e) {
				linea = null;

				try { // Si no se ha encontrado artículo, intentamos aplicar cupón
					CustomerCouponDTO coupon = new CustomerCouponDTO(codArticulo, false);

					isCupon = sesionPromociones.aplicarCupon(coupon, (TicketVentaAbono) ticketPrincipal);
					if (!isCupon) { // Si el código no es de un cupón válido,
					                // lanzamos excepción de artículo no encontrado
						log.warn("nuevaLineaArticulo() - Artículo no encontrado " + codArticulo);
						throw new LineaTicketException(e.getMessageI18N());
					}
					ticketPrincipal.getTotales().recalcular();

				} // Si tenemos excepción durante la aplicación del cupón, lanzamos
				  // excepción indicativa
				catch (CuponAplicationException | CuponUseException | CuponesServiceException ex) {
					log.warn("nuevaLineaArticulo() - Error en la aplicación del cupón -" + ex.getMessageI18N());
					throw new LineaTicketException(ex.getMessageI18N(), e);
				}
			}
		}
		return (LineaTicket) linea;
	}

	@Override
	protected void recuperarDatosPersonalizadosLinea(LineaTicket lineaRecuperada, LineaTicket nuevaLineaArticulo) {
		log.debug("recuperarDatosPersonalizadosLinea() - Añadimos datos personalizados a la lineaTicket");
		
		if (lineaRecuperada instanceof BricodepotLineaTicket) {
			
			Boolean isAnticipo = ((BricodepotLineaTicket) lineaRecuperada).getIsAnticipo();
			((BricodepotLineaTicket) nuevaLineaArticulo).setIsAnticipo(isAnticipo);
			
			List<Motivo> motivo = ((BricodepotLineaTicket) lineaRecuperada).getMotivos();
			((BricodepotLineaTicket) nuevaLineaArticulo).setMotivos(motivo);

		} else {
			log.error("recuperarDatosPersonalizadosLinea() - Error al convertir la línea recuperada al tipo BricodepotLineaTicket.");
		}
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected void recuperarDatosPersonalizados(TicketVenta ticketRecuperado) {
		log.debug("recuperarDatosPersonalizados() - Añadimos datos personalizados a la cabecera");
		String importeAnticipo = ((BricodepotCabeceraTicket) ticketRecuperado.getCabecera()).getImporteAnticipo();
		String numAnticipo = ((BricodepotCabeceraTicket) ticketRecuperado.getCabecera()).getNumAnticipo();
		String operacionAnticipo = ((BricodepotCabeceraTicket) ticketRecuperado.getCabecera()).getOperacionAnticipo();
		Long idClieAlbaranAnticipo = ((BricodepotCabeceraTicket) ticketRecuperado.getCabecera()).getIdClieAlbaranAnticipo();
		List<TicketAuditEvent> auditEvents = ((BricodepotCabeceraTicket) ticketRecuperado.getCabecera()).getAuditEvents();
		
		((BricodepotCabeceraTicket) ticketPrincipal.getCabecera()).setNumAnticipo(numAnticipo);
		((BricodepotCabeceraTicket) ticketPrincipal.getCabecera()).setOperacionAnticipo(operacionAnticipo);
		((BricodepotCabeceraTicket) ticketPrincipal.getCabecera()).setIdClieAlbaranAnticipo(idClieAlbaranAnticipo);
		((BricodepotCabeceraTicket) ticketPrincipal.getCabecera()).setImporteAnticipo(importeAnticipo);
		((BricodepotCabeceraTicket) ticketPrincipal.getCabecera()).setAuditEvents(auditEvents);
		

	}

	@Override
	public synchronized LineaTicket nuevaLineaArticulo(String codArticulo, String desglose1, String desglose2, BigDecimal cantidad, Integer idLineaDocOrigen) throws LineaTicketException {
		if (comprobarCodigoQrRecuperarArticulos(codArticulo)) {
			desglosarQrRecuperacionArticulos(codArticulo);
//			int contadorLinea = 0;
//			if (!listaArticulosRecuperados.isEmpty()) {
////				for (LineaTicket articulo : listaArticulosRecuperados) {
////
////					LineaTicket newArticulo = super.nuevaLineaArticulo(articulo.getCodArticulo(), articulo.getDesglose1(), articulo.getDesglose2(), articulo.getCantidad(), null);
////					if (contadorLinea == listaArticulosRecuperados.size()) {
////						return newArticulo;
////					}
////					contadorLinea++;
////				}
//			}
		}
		else {
			return super.nuevaLineaArticulo(codArticulo, desglose1, desglose2, cantidad, idLineaDocOrigen);

		}
		return null;
	}

	private List<LineaTicket> desglosarQrRecuperacionArticulos(String codArticulo) {
		log.debug("desglosarQrRecuperacionArticulos() - Desglosando codigo QR :" + codArticulo + "para recuperar articulos");
		List<LineaTicket> listaArticulosRecuperados = new ArrayList<>();
		try {
	        String[] lineas = codArticulo.split("\n");

	        for (int i = 2; i < lineas.length; i++) {
	            String[] partes = lineas[i].split(",");
	            if (partes.length == 3) {
	                try {
	                    String codArt = partes[0];
	                    BigDecimal cantidad = new BigDecimal(partes[1]);
	                    // Aquí asumimos que desglose1 es cantidad y desglose2 es precio, ajusta según sea necesario
	                    LineaTicket articulo = super.nuevaLineaArticulo(codArt, null, null, cantidad, null);
	                    listaArticulosRecuperados.add(articulo);
	                } catch (NumberFormatException e) {
	                    log.error("desglosarQrRecuperacionArticulos() - Error recuperando los articulos del QR "+e.getMessage(),e);
	                }
	            }
	        }

	        return listaArticulosRecuperados;

		}
		catch (Exception e) {
			log.error("desglosarQrRecuperacionArticulos() - Error desglosando codigo QR :" + codArticulo + "para recuperar articulos " + e.getMessage(), e);
		}

		return listaArticulosRecuperados;
	}

	private boolean comprobarCodigoQrRecuperarArticulos(String codArticulo) {
		log.debug("comprobarCodigoQrRecuperarArticulos() - Comprobando si el codigo :" + codArticulo + " es un QR que permite recuperar articulos");
		String[] lineas = codArticulo.split("\n");

		// Validar cabecera
		if (lineas.length < 2 || !validarCabecera(lineas[0], lineas[1])) {
			return false;
		}

		// Validar detalle de líneas
		for (int i = 2; i < lineas.length; i++) {
			if (!validarDetalleLinea(lineas[i])) {
				return false;
			}
		}

		return true;
	}

	private boolean validarCabecera(String identificador, String fechaValidacion) {
		try {
			LocalDate fecha = LocalDate.parse(fechaValidacion, DateTimeFormatter.ISO_LOCAL_DATE);
			LocalDate fechaActual = LocalDate.now();
			return !fecha.isBefore(fechaActual.minusMonths(1)) && !fecha.isAfter(fechaActual.plusMonths(1));
		}
		catch (DateTimeParseException e) {
			return false;
		}
	}

	private boolean validarDetalleLinea(String linea) {
		String[] partes = linea.split(",");
		if (partes.length != 3) {
			return false;
		}

		try {
			Long.parseLong(partes[0]); // EAN como número largo
			Integer.parseInt(partes[1]); // CANTIDAD como entero
			Double.parseDouble(partes[2]); // PRECIO como doble
		}
		catch (NumberFormatException e) {
			return false;
		}

		return true;
	}

	// TODO
	/* Se personaliza este metodo para poder meter logs */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void recuperarTicket(Stage stage, TicketAparcadoBean ticketAparcado) throws TicketsServiceException, PromocionesServiceException, DocumentoException, LineaTicketException {
        log.debug("recuperarTicket() - Recuperando ticket...");
        
        nuevoTicket();
        // Realizamos el unmarshall
        log.debug("Ticket recuperado:\n"+new String(ticketAparcado.getTicket()));
        TicketVenta ticketRecuperado = (TicketVentaAbono) MarshallUtil.leerXML(ticketAparcado.getTicket(), getTicketClasses(documentoActivo).toArray(new Class[]{}));

        ticketPrincipal.getCabecera().setIdTicket(ticketRecuperado.getIdTicket());
        ticketPrincipal.getCabecera().setUidTicket(ticketRecuperado.getUidTicket());
        ticketPrincipal.getCabecera().setCodTicket(ticketRecuperado.getCabecera().getCodTicket());
        ticketPrincipal.getCabecera().setSerieTicket(ticketRecuperado.getCabecera().getSerieTicket());
        
        String tipoDocumentoFacturaDirecta = getDocumentoActivo().getTipoDocumentoFacturaDirecta();
		if (ticketRecuperado.getCabecera().getCodTipoDocumento().equals(tipoDocumentoFacturaDirecta)) {
			log.debug("recuperarTicket() - Se va a cambiar el documento activo. Documento activo actual " + getDocumentoActivo());
			ticketPrincipal.getCabecera().setDocumento(documentos.getDocumento(tipoDocumentoFacturaDirecta));
		}
		ticketPrincipal.getCabecera().setCantidadArticulos(ticketRecuperado.getCabecera().getCantidadArticulos());
        
        if(ticketAparcado.getUsuario() == null || !ticketAparcado.getUsuario().equals("FASTPOS")){
        	// Recuperamos el cliente del ticket aparcado
        	ticketPrincipal.getCabecera().setCliente(ticketRecuperado.getCabecera().getCliente());
        }
	    String uidDiarioCaja = sesion.getSesionCaja().getUidDiarioCaja();
        ticketPrincipal.getCabecera().setUidDiarioCaja(uidDiarioCaja);
        
        recuperarDatosPersonalizados(ticketRecuperado);

        List<LineaTicket> lineas = ticketRecuperado.getLineas();
        for (LineaTicket lineaRecuperada : lineas) {
			String codigo = lineaRecuperada.getCodigoBarras();
			String desglose1 = lineaRecuperada.getDesglose1();
			String desglose2 = lineaRecuperada.getDesglose2();
			if(StringUtils.isBlank(codigo)) {
				codigo = lineaRecuperada.getCodArticulo();
			}
			else {
				desglose1 = null;
				desglose2 = null;
			}
			LineaTicket nuevaLineaArticulo = nuevaLineaArticulo(codigo, desglose1, desglose2, lineaRecuperada.getCantidad(), null, null, false, false);
			
			nuevaLineaArticulo.setDocumentoOrigen(lineaRecuperada.getDocumentoOrigen());
			
			nuevaLineaArticulo.setDesArticulo(lineaRecuperada.getDesArticulo());
			nuevaLineaArticulo.setDescuentoManual(lineaRecuperada.getDescuentoManual());
			BigDecimal nuevoPrecio = lineaRecuperada.getPrecioTotalSinDto();
			nuevaLineaArticulo.setPrecioTotalSinDto(nuevoPrecio);
			BigDecimal precioSinDto = lineaRecuperada.getPrecioSinDto();
			nuevaLineaArticulo.setPrecioSinDto(precioSinDto);
			nuevaLineaArticulo.setCodigoBarras(lineaRecuperada.getCodigoBarras());
			nuevaLineaArticulo.setNumerosSerie(lineaRecuperada.getNumerosSerie());
			nuevaLineaArticulo.setEditable(lineaRecuperada.isEditable());
			
			String sellerName = lineaRecuperada.getVendedor().getUsuario();
			try {
				UsuarioBean seller = usuariosService.consultarUsuario(sellerName);
				nuevaLineaArticulo.setVendedor(seller);
			} catch (UsuarioNotFoundException e) {
				// active user
				log.warn("recuperarTicket() - No se ha encontrado el usuario: " + sellerName);
			} catch (UsuariosServiceException e) {
				// active user
				log.warn("recuperarTicket() - Se ha producido un error al consultar el: " + sellerName);
			}

			recuperarDatosPersonalizadosLinea(lineaRecuperada, nuevaLineaArticulo);
			
		}
        
        FidelizacionBean datosFidelizado = ticketRecuperado.getCabecera().getDatosFidelizado();
		if(datosFidelizado!=null){
        	try {
				FidelizacionBean tarjetaFidelizado = Dispositivos.getInstance().getFidelizacion().consultarTarjetaFidelizado(stage, datosFidelizado.getNumTarjetaFidelizado(), ticketPrincipal.getCabecera().getUidActividad());
				ticketPrincipal.getCabecera().setDatosFidelizado(tarjetaFidelizado);
			} catch (ConsultaTarjetaFidelizadoException e) {
				log.debug("recuperarTicket() - Error al consultar fidelizado", e);
				FidelizacionBean fidelizacionBean = new FidelizacionBean();
				fidelizacionBean.setNumTarjetaFidelizado(datosFidelizado.getNumTarjetaFidelizado());
				ticketPrincipal.getCabecera().setDatosFidelizado(fidelizacionBean);
			}
        }
		
		for(PagoTicket pago : (List<PagoTicket>) ticketRecuperado.getPagos()) {
			pago.setMedioPago(mediosPagosService.getMedioPago(pago.getCodMedioPago()));
			ticketPrincipal.getPagos().add(pago);
		}
        
        recalcularConPromociones();
        
        // Establecemos el contador
        contadorLinea = ticketPrincipal.getLineas().size()+1;
        //Eliminamos el ticket recuperado de la lista de tickets aparcados.
        ticketsAparcadosService.eliminarTicket(ticketAparcado.getUidTicket());
        
        // TODO JGG
		mostrarLogXML();
	}
	
	public void guardarCopiaSeguridadTicket() {
		super.guardarCopiaSeguridadTicket();
		// TODO JGG
		mostrarLogXML();
	}

	public void recuperarCopiaSeguridadTicket(Stage stage, TicketAparcadoBean ticketAparcado) throws LineaTicketException, TicketsServiceException, PromocionesServiceException, DocumentoException {
		super.recuperarCopiaSeguridadTicket(stage, ticketAparcado);

		//TODO JGG
		mostrarLogXML();
	}
	
	private void mostrarLogXML() {
		try {
			if (ticketPrincipal.getLineas() != null && !ticketPrincipal.getLineas().isEmpty() && !(ticketPrincipal.getLineas().get(0) instanceof BricodepotLineaTicket)) {
			byte[] xmlTicket = MarshallUtil.crearXML(ticketPrincipal, getTicketClasses(documentoActivo));
//			byte[] xmlTicket = MarshallUtil.crearXML(ticketPrincipal);
				log.debug("recuperarCopiaSeguridadTicket() - XML TICKET:");
				log.debug(new String(xmlTicket, "UTF-8") + "\n");
			}
		}
		catch (Exception ignore) {
		}
	}
	
	@Override
	public List<Class<?>> getTicketClasses(TipoDocumentoBean tipoDocumento) {
		List<Class<?>> classes = super.getTicketClasses(tipoDocumento);

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
	
    public boolean recuperarTicketConversion(String codigo, String codAlmacen, String codCaja, Long idTipoDoc) throws TicketsServiceException {
    	return recuperarTicketConversion(codigo, codAlmacen, codCaja, idTipoDoc, true);
    }
	
	  public boolean recuperarTicketConversion(String codigo, String codAlmacen, String codCaja, Long idTipoDoc, boolean controlarPlazoMaximoDevolucion) throws TicketsServiceException {
	    	try{
		    	log.debug("recuperarTicketDevolucion() - Recuperando ticket...");
		    	byte[] xmlTicketOrigen = null;
		    	ResponseGetTicketDev datosDevolucion = null;
				
		    	//Si es localizador
				//Obtenemos por localizador desde central
				try {
					xmlTicketOrigen = obtenerTicketDevolucionCentralLocalizador(codigo, false, idTipoDoc);
				} catch (LineaTicketException e) {
					log.warn("recuperarTicketDevolucion() - Error al obtener ticket devolución desde central - " + e.getClass().getName() + " - " + e.getLocalizedMessage(), e);
				}
				
				if(xmlTicketOrigen != null){
					//Si no null, buscamos datos devolucion
					tratarTicketRecuperado(xmlTicketOrigen);

					datosDevolucion = obtenerDatosDevolucion(ticketOrigen.getUidTicket());
				}
		    	
				//Si no tenemos ticket, consultamos como id de documento en lugar de como localizador 
				if(xmlTicketOrigen == null){
					//por codigo desde central
					try{
						xmlTicketOrigen = obtenerTicketDevolucionCentral(codCaja, codAlmacen, codigo, idTipoDoc);
					}catch(Exception e){
						log.warn("recuperarTicketDevolucion() - Error al obtener ticket devolución desde central - " + e.getClass().getName() + " - " + e.getLocalizedMessage(), e);
					}
					
					if(xmlTicketOrigen != null){
						//Si no null, buscamos datos devolucion
						tratarTicketRecuperado(xmlTicketOrigen);
						datosDevolucion = obtenerDatosDevolucion(ticketOrigen.getUidTicket());
					}
					else{
						throw new TicketsServiceException("No se ha encontrado ticket con codigo: " + codigo);						
					}
				}
				
				asignarLineasDevueltasConversion(datosDevolucion);
				
		        descontarLineasNegativasTicketOrigen();
		        
	    	} catch (TicketsServiceException e) {
	    		log.error("recuperarTicketDevolucion() - " + e.getClass().getName() + " - " + e.getLocalizedMessage(), e);
				return false;
			}
	    	
	    	if(controlarPlazoMaximoDevolucion) {
	    		controlarPlazoMaximoDevolucion();
	    	}
	    	
	    	bloquearConversionArticulosAnticipo();
	    	
	    	return true;
	    }

		@SuppressWarnings("unchecked")
		private void bloquearConversionArticulosAnticipo() throws TicketsServiceException {
			String codAnticipo = variablesServices.getVariableAsString(PROPIEDAD_CODIGO_ANTICIPO);
			for (LineaTicket linea : (List<LineaTicket>) ticketOrigen.getLineas()) {
				if (linea.getCodArticulo().equals(codAnticipo)) {
					throw new TicketsServiceException(I18N.getTexto("El ticket contiene un ANTICIPO y no se puede transformar. Pase por caja central para su transformación manual."));
				}
			}
		}

		@SuppressWarnings({ "unchecked" })
		public void addLineas() throws DocumentoException, LineaTicketException {
		    log.debug("addLineas() - Añadiendo líneas al ticket de devolución");

		    if (ticketOrigen == null || ticketOrigen.getLineas() == null) {
		        log.debug("addLineas() - El documento recuperado no tiene líneas");
		        return;
		    }

		    inicializarDocumentoDevolucion();

		    List<LineaTicket> lineasClonadas = clonarLineas(ticketOrigen.getLineas());
		    tratarLineasParaDevolucion(lineasClonadas);

		    ((TicketVentaAbono) getTicket()).getCantidadTotal();
		    completaLineaDevolucionPunto();
		}

		private void inicializarDocumentoDevolucion() throws DocumentoException {
		    setDocumentoActivo(sesion.getAplicacion().getDocumentos().getDocumento(Documentos.NOTA_CREDITO));
		    setEsDevolucion(true);
		    getTicket().getLineas().clear();
		}

		private List<LineaTicket> clonarLineas(List<LineaTicket> lineasOriginales) {
		    return lineasOriginales.stream()
		            .map(LineaTicket::clone)
		            .collect(Collectors.toList());
		}

		private void tratarLineasParaDevolucion(List<LineaTicket> lineasDevolucion) throws LineaTicketException {
		    for (LineaTicket original : lineasDevolucion) {
		        if (esAnticipo(original) && original.getImporteTotalConDto().compareTo(BigDecimal.ZERO) < 0) {
		            original.setCantidad(original.getCantidad().abs());
		            LineaTicket nuevaLinea = clonarLineaParaDevolucion(original);
		            nuevaLinea.setCantidad(original.getCantidad());
		        } else if (BigDecimalUtil.isMayorACero(original.getCantidadDisponibleDevolver())) {
		            clonarLineaParaDevolucion(original);
		        }
		    }
		}

		private boolean esAnticipo(LineaTicketAbstract linea) {
		    String codigoAnticipo = variablesServices.getVariableAsString(PROPIEDAD_CODIGO_ANTICIPO);
		    return linea.getCodArticulo().equals(codigoAnticipo);
		}
		

		private LineaTicket clonarLineaParaDevolucion(LineaTicket devolucion) throws LineaTicketException {
		    LineaTicket nuevaLinea = nuevaLineaArticulo(
		        devolucion.getCodArticulo(),
		        devolucion.getDesglose1(),
		        devolucion.getDesglose2(),
		        devolucion.getCantidadDisponibleDevolver(),
		        devolucion.getIdLinea()
		    );

		    devolucion.setCantidadADevolver(BigDecimal.ZERO);
		    nuevaLinea.setLineaDocumentoOrigen(devolucion.getIdLinea());

		    if (nuevaLinea instanceof BricodepotLineaTicket && devolucion instanceof BricodepotLineaTicket) {
		        BricodepotLineaTicket nuevaLineaBrico = (BricodepotLineaTicket) nuevaLinea;
		        BricodepotLineaTicket lineaOrigenBrico = (BricodepotLineaTicket) devolucion;

		        nuevaLineaBrico.setConversionAFT(lineaOrigenBrico.isConversionAFT());
		        nuevaLineaBrico.setPorcentajeIvaConversion(lineaOrigenBrico.getPorcentajeIvaConversion());
		        nuevaLineaBrico.setIsAnticipo(true);
		    }

		    nuevaLinea.setPrecioTarifaOrigen(devolucion.getPrecioTarifaOrigen());
		    nuevaLinea.setPrecioTotalTarifaOrigen(devolucion.getPrecioTotalTarifaOrigen());

		    return nuevaLinea;
		}
		
		@SuppressWarnings({ "unchecked" })
		public void addPagos() throws Exception { 
			log.debug("addPagos() - Añadiendo pagos al ticket de devolución");
			
			if(getTicketOrigen() == null || getTicketOrigen().getPagos() == null) {
				log.debug("addPagos() - El documento recuperado no tiene pagos");
			}
			
			//Copiamos los pagos para que no alteren los datos originales para la FT
			List<PagoTicket> pagosDev = new ArrayList<>();
			BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
			try {
				for (PagoTicket pagoTicket : (List<PagoTicket>)getTicketOrigen().getPagos()) {
					PagoTicket pagoDev = new PagoTicket();
					BeanUtils.copyProperties(pagoDev, pagoTicket);
					pagosDev.add(pagoDev);
				}
			}
			catch (Exception e) {
				log.error("addPagos() - Error al tratar los medios de pago");
				throw new Exception(e.getMessage(),e);
			}
			//Los setamos al ticket principal
			getTicket().setPagos(pagosDev);
			
			BigDecimal factorSigno = obtenFactorSigno();
			if (!getTicket().getPagos().isEmpty()) {
				for (PagoTicket pago : (List<PagoTicket>) getTicket().getPagos()) {
					pago.setImporte(pago.getImporte().multiply(factorSigno));
				}
			}
			
			// BRICOD-718 - Ajustar importe pagos para devoluciones parciales
			BigDecimal importeLineasTicket = BigDecimal.ZERO;
			BigDecimal importePagosTicketOrigen = BigDecimal.ZERO;
			for (LineaTicket linea : (List<LineaTicket>) getTicket().getLineas()) {
				importeLineasTicket = importeLineasTicket.add(linea.getImporteTotalConDto());
			}
			for (PagoTicket pago : (List<PagoTicket>) getTicket().getPagos()) {
				importePagosTicketOrigen = importePagosTicketOrigen.add(pago.getImporte());
			}
			
			if(importeLineasTicket.compareTo(importePagosTicketOrigen) != 0) {
				PagoTicket pagoAjustado = (PagoTicket) getTicket().getPagos().get(0);
				pagoAjustado.setImporte(importeLineasTicket.abs().multiply(factorSigno));
				getTicket().getPagos().clear();
				getTicket().getPagos().add(pagoAjustado);
			}
		}

		private BigDecimal obtenFactorSigno() throws ConceptoServiceException, ConceptoNotFoundException { // SOS-155
			log.debug("obtenFactorSigno() - Obteniendo signo para documento de devolución");

			BigDecimal factorSigno = BigDecimal.ONE.negate();
			ConceptoAlmacenBean cocAlm = conceptoService.consultarConcepto(sesion.getAplicacion().getUidActividad(), getDocumentoActivo().getCodaplicacion(), getDocumentoActivo().getCodconalm());
			if (StringUtils.isNotBlank(cocAlm.getSigno()) && cocAlm.getSigno().equals("+")) {
				factorSigno = factorSigno.negate();
			}

			log.debug("obtenFactorSigno() Signo: " + (BigDecimalUtil.isMenorACero(factorSigno) ? " - " : " + "));
			
			return factorSigno;
		}

		
		public void recalcularConPromociones() {
			if (!isConversionFlexpointFT()) {
				super.recalcularConPromociones();
				return;
			}    
			
			ticketPrincipal.getTotales().recalcular();
		}

		public boolean esDevolucionFlexpoint() {
			return esDevolucionFlexpoint;
		}
		
		public void setEsDevolucionFlexpoint(boolean esDevolucionFlexpoint) {
			this.esDevolucionFlexpoint = esDevolucionFlexpoint;
		}
		
		public boolean isConversionFlexpointFT() {
			return esConversionFlexpointFT;
		}
		
		public void setConversionFlexpointFT(boolean esConversionFlexpointFT) {
			this.esConversionFlexpointFT = esConversionFlexpointFT;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected void descontarLineasNegativasTicketOrigen() {
			List<LineaTicket> lineasNegativas = new ArrayList<LineaTicket>();
	    	 
	    	Iterator<LineaTicket> it = ticketOrigen.getLineas().iterator();
	    	while(it.hasNext()) {
	    		LineaTicket linea = it.next();
	    		if(BigDecimalUtil.isMenorACero(linea.getImporteTotalConDto()) || linea.getLineaDocumentoOrigen() != null)  {
	    			if (!Boolean.TRUE.equals(((BricodepotCabeceraTicket) ticketOrigen.getCabecera()).getFsToFt())
	    				    && !variablesServices.getVariableAsString(PROPIEDAD_CODIGO_ANTICIPO).equals(linea.getCodArticulo())) {
	    				    lineasNegativas.add(linea);
	    				    it.remove();
	    				}
	    		}
	    	}
		}
		
		@SuppressWarnings("unchecked")
		protected void asignarLineasDevueltasConversion(ResponseGetTicketDev res) {
			log.debug("asignarLineasDevueltas() - Actualizamos precio de lineas origen restando importe de promociones de tipo menos ingreso");
			List<LineaTicketAbstract> lineas = ticketOrigen.getLineas();
			SesionImpuestos sesionImpuestos = sesion.getImpuestos();
			BigDecimal importeLineas = BigDecimal.ZERO; 
			BigDecimal importeLineasOriginal = BigDecimal.ZERO; 
			BigDecimal lessIncomeTotal = BigDecimal.ZERO;
			Boolean promocionMenosIngreso = variablesServices.getVariableAsBoolean(VariablesServices.TPV_TRATAR_PROMOCIONES_MENOS_INGRESO);
			
			//calculamos el precio origen restando el precio de las promociones de menos ingreso
			for (LineaTicketAbstract lineaOrigen : lineas) {
				
				if(promocionMenosIngreso && !BigDecimalUtil.isIgualACero(lineaOrigen.getImporteTotalPromocionesMenosIngreso())){
	                //Calculamos el precioSinDto 
	                BigDecimal importeSinPromocionesMenosIngreso = lineaOrigen.getImporteTotalConDto().subtract(lineaOrigen.getImporteTotalPromocionesMenosIngreso()); 
	                BigDecimal precioSinPromocionesMenosIngreso =  BigDecimalUtil.isIgualACero(lineaOrigen.getCantidad()) ? BigDecimal.ZERO : importeSinPromocionesMenosIngreso.divide(lineaOrigen.getCantidad(), 6, RoundingMode.HALF_UP);
	                lessIncomeTotal = lessIncomeTotal.add(lineaOrigen.getImporteTotalPromocionesMenosIngreso());
	                
	                //Actualizamos todas los demÃ¡s precios e importes a partir del precioSinDto 
	                lineaOrigen.setPrecioTotalSinDto(precioSinPromocionesMenosIngreso); 
	                BigDecimal precioSinImpuestos = sesionImpuestos.getPrecioSinImpuestos(lineaOrigen.getCodImpuesto(), precioSinPromocionesMenosIngreso, lineaOrigen.getCabecera().getCliente().getIdTratImpuestos());
	                
	              //Actualizamos todas los demás precios e importes a partir del precioSinDto
	    			lineaOrigen.setPrecioSinDto(precioSinImpuestos);
	    			lineaOrigen.setPrecioConDto(lineaOrigen.getPrecioSinDto());
	    			lineaOrigen.setPrecioTotalConDto(lineaOrigen.getPrecioTotalSinDto());
	    			lineaOrigen.setImporteConDto(BigDecimalUtil.redondear(lineaOrigen.getPrecioConDto().multiply(lineaOrigen.getCantidad()))); 
	                lineaOrigen.setImporteTotalConDto(BigDecimalUtil.redondear(lineaOrigen.getPrecioTotalConDto().multiply(lineaOrigen.getCantidad())));
				}
				
				/*
				 * PERSONALIZACIÓN BRICO DE MÉTODO ORIGINAL SOLO PARA CONVERSIONES
				 * Lo que se hace es dejar el precio total sin DTO como estaba ya que en este caso (conversión FS-FT) se tiene en cuenta las promociones
				 */
				
//				else{
//					//igualmos los precios sin DTO a los precios con DTO, en devoluciones no hay descuentos 
//					lineaOrigen.setPrecioTotalConDto(BigDecimalUtil.isIgualACero(lineaOrigen.getCantidad())?BigDecimal.ZERO:lineaOrigen.getImporteTotalConDto().setScale(6, BigDecimal.ROUND_HALF_UP).divide(lineaOrigen.getCantidad().setScale(6, BigDecimal.ROUND_HALF_UP),BigDecimal.ROUND_HALF_UP)); 
//					lineaOrigen.setPrecioTotalSinDto(lineaOrigen.getPrecioTotalConDto()); 
//					lineaOrigen.setPrecioSinDto(lineaOrigen.getPrecioConDto()); 
//					
//				}
				
				importeLineasOriginal = importeLineasOriginal.add(lineaOrigen.getImporteTotalConDto());
				importeLineas = importeLineas.add(lineaOrigen.getImporteTotalConDto());
			}
			
			if(promocionMenosIngreso){
	            //Añadimos a la ultima linea la diferencia entre el total de la cabecera del ticket y el total de precio de las lineas 
				if(!lineas.isEmpty()){
					BigDecimal totalTicket = ticketOrigen.getCabecera().getTotales().getTotalAPagar().subtract(lessIncomeTotal);
					
					BigDecimal diferenciaImportes = totalTicket.subtract(importeLineas);
					
					int i = 1;
					LineaTicketAbstract ultimaLinea = lineas.get(lineas.size()-i);
					BigDecimal precioTotalSinDtoUltimaLinea = BigDecimalUtil.isIgualACero(ultimaLinea.getCantidad()) ? BigDecimal.ZERO : ultimaLinea.getPrecioTotalSinDto().add(diferenciaImportes.divide(ultimaLinea.getCantidad(), 2, RoundingMode.HALF_UP));
					BigDecimal precioTotalSinDto = precioTotalSinDtoUltimaLinea;
					while(BigDecimalUtil.isMenorACero(precioTotalSinDto)) {
						i++;
						ultimaLinea = lineas.get(lineas.size()-i);
						precioTotalSinDtoUltimaLinea = BigDecimalUtil.isIgualACero(ultimaLinea.getCantidad()) ? BigDecimal.ZERO : ultimaLinea.getPrecioTotalSinDto().add(diferenciaImportes.divide(ultimaLinea.getCantidad(), 2, RoundingMode.HALF_UP));
						precioTotalSinDto = precioTotalSinDtoUltimaLinea;
					}
					
					BigDecimal precioSinImpuestos = sesionImpuestos.getPrecioSinImpuestos(ultimaLinea.getCodImpuesto(), ultimaLinea.getPrecioTotalSinDto(), ultimaLinea.getCabecera().getCliente().getIdTratImpuestos());
					ultimaLinea.setPrecioTotalSinDto(precioTotalSinDtoUltimaLinea);
					ultimaLinea.setPrecioSinDto(precioSinImpuestos);
					ultimaLinea.setPrecioConDto(ultimaLinea.getPrecioSinDto());
					ultimaLinea.setPrecioTotalConDto(ultimaLinea.getPrecioTotalSinDto());
	                ultimaLinea.setImporteConDto(ultimaLinea.getPrecioConDto().multiply(ultimaLinea.getCantidad())); 
	                ultimaLinea.setImporteTotalConDto(ultimaLinea.getPrecioTotalConDto().multiply(ultimaLinea.getCantidad()));
					
				}
			}
			else{
				//Añadimos a la ultima linea la diferencia entre la suma de importes de las lineas del ticket original y la suma de importes de las lineas del ticket de devolución
				BigDecimal diferenciaImportes = importeLineasOriginal.subtract(importeLineas);
				if(!lineas.isEmpty() && !BigDecimalUtil.isIgualACero(diferenciaImportes)){
					LineaTicketAbstract ultimaLinea = lineas.get(lineas.size()-1);
					ultimaLinea.setPrecioTotalSinDto(ultimaLinea.getPrecioTotalSinDto().add(diferenciaImportes));
					
					BigDecimal precioSinImpuestos = sesionImpuestos.getPrecioSinImpuestos(ultimaLinea.getCodImpuesto(), ultimaLinea.getPrecioTotalSinDto(), ultimaLinea.getCabecera().getCliente().getIdTratImpuestos());
					ultimaLinea.setPrecioSinDto(precioSinImpuestos);
					ultimaLinea.setPrecioConDto(ultimaLinea.getPrecioSinDto());
					ultimaLinea.setPrecioTotalConDto(ultimaLinea.getPrecioTotalSinDto());
					ultimaLinea.setImporteConDto(BigDecimalUtil.redondear(ultimaLinea.getPrecioConDto().multiply(ultimaLinea.getCantidad())));
					ultimaLinea.setImporteTotalConDto(BigDecimalUtil.redondear(ultimaLinea.getPrecioTotalConDto().multiply(ultimaLinea.getCantidad())));
				}
			}

			
			//Si tenemos datos devoluciÃ³n los usamos
	    	if(res != null){
	    		log.debug("asignarLineasDevueltas() - SÃí hemos recibido datos de devolución, actualizamos precios de lí­neas origen");
		        List<ArticulosDevueltosBean> lineasDevolucion = res.getLineas();
		        for(ArticulosDevueltosBean articulo: lineasDevolucion){
		            
		            LineaTicketAbstract linea = (LineaTicketAbstract) ticketOrigen.getLinea(articulo.getLinea());
		            if(linea!=null){
		            	linea.setCantidadDevuelta(new BigDecimal(articulo.getCantidadDevuelta()).setScale(3, RoundingMode.HALF_UP));
		                linea.setPrecioTotalConDto(new BigDecimal(articulo.getPrecioTotal()).setScale(4, RoundingMode.HALF_UP));
		            }
		        }
	    		ticketPrincipal.getCabecera().getDatosDocOrigen().setRecoveredOnline(true);
	        }
	    	else {
	    		ticketPrincipal.getCabecera().getDatosDocOrigen().setRecoveredOnline(false);
	    	}
	    }
		
		@Override
		public void actualizarCantidadesOrigenADevolver(LineaTicketAbstract linea, BigDecimal cantidad) {
	        BigDecimal oldCantidadADevolver = linea.getCantidadADevolver();
	        linea.setCantidadADevolver(cantidad.abs());
	        BigDecimal cantidadADevolver = linea.getCantidadADevolver();
	        if(BigDecimalUtil.isMenorACero(cantidadADevolver) && !linea.getCodArticulo().equals(variablesServices.getVariableAsString(PROPIEDAD_CODIGO_ANTICIPO))){
	        	log.error("actualizarCantidadesOrigenADevolver() - Ha habido un error al ser la cantidad a devolver de la linea " + linea.toString() + " inferior a 0.");
	        	linea.setCantidadADevolver(oldCantidadADevolver);
	        	//Error en la programación, nunca debería ser menor a 0, hay que validar antes
	        	throw new RuntimeException();
	        }
	    }
}
