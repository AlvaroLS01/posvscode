package com.comerzzia.bricodepot.pos.services.fidelizado;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.comerzzia.api.model.loyalty.FidelizadoBean;
import com.comerzzia.api.rest.client.exceptions.RestException;
import com.comerzzia.api.rest.client.exceptions.RestHttpException;
import com.comerzzia.api.rest.client.fidelizados.ConsultarFidelizadoRequestRest;
import com.comerzzia.api.rest.client.fidelizados.FidelizadosRest;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.BricodepotVisorPantallaSecundaria;
import com.comerzzia.bricodepot.pos.gui.mantenimientos.BricodepotFidelizadoController;
import com.comerzzia.bricodepot.pos.util.format.BricoEmailValidator;
import com.comerzzia.core.util.fechas.Fecha;
import com.comerzzia.pos.persistence.tickets.TicketBean;
import com.comerzzia.pos.services.core.contadores.ServicioContadores;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.services.ticket.TicketsService;
import com.comerzzia.pos.util.config.AppConfig;
import com.comerzzia.pos.util.format.FormatUtil;
import com.comerzzia.pos.util.i18n.I18N;
import com.comerzzia.pos.util.xml.MarshallUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.type.WhenNoDataTypeEnum;
import net.sf.jasperreports.engine.util.JRLoader;
import rest.bean.enlace.Enlace;
import rest.client.fidelizados.enlaces.BricodepotEnlacesFidelizadosRest;

@Service
public class BricodepotFidelizadosService {

	protected static final Logger log = Logger.getLogger(BricodepotFidelizadosService.class.getName());

	public static final String ID_FIDELIZADO_CAP = "ID_FIDELIZADO_CAP";
	public static final String INFORME_FIDELIZADO_ES = "plantillas/jasper/fidelizados/formulariofidelizado.jasper";
	public static final String INFORME_FIDELIZADO_PT = "plantillas/jasper/fidelizados/formulariofidelizado_PT.jasper";
	public static final String COD_PAIS_PT = "PT";

	@Autowired
	private Sesion sesion;
	@Autowired
	private ServicioContadores contadoresService;
	@Autowired
	private TicketsService ticketsService;
	
	@Autowired
	private VariablesServices variablesServices;

	public void registrarTicketFidelizado(TicketFidelizadoCaptacion ticketFidelizado, FidelizadoBean fidelizado) {
		TicketBean ticket;
		byte[] xmlTicketFidelizado = null;
		log.debug("registrarTicketFidelizado() - Construyendo objeto persistente");
		try {
			// Construimos objeto persistente
			ticket = new TicketBean();

			// uid documento
			String uidTicket = UUID.randomUUID().toString();
			ticket.setUidTicket(uidTicket);
			// id documento
			Long idTicket = contadoresService.obtenerValorContador(ID_FIDELIZADO_CAP, sesion.getAplicacion().getUidActividad());
			ticket.setIdTicket(idTicket);
			// serie documento
			String serieTicket = sesion.getAplicacion().getCodAlmacen() + "/" + sesion.getSesionCaja().getCajaAbierta().getCodCaja();
			ticket.setSerieTicket(serieTicket);
			// cod documento
			String codigoTicket = sesion.getAplicacion().getCodAlmacen() + "/" + sesion.getSesionCaja().getCajaAbierta().getCodCaja() + "/" + String.format("%08d", idTicket);
			ticket.setCodTicket(codigoTicket);
			// firma documento (no
			ticket.setFirma("*");
			// tipo documento
			ticket.setIdTipoDocumento(100000L);

			ticket.setCodAlmacen(sesion.getAplicacion().getCodAlmacen());
			ticket.setCodcaja(sesion.getSesionCaja().getCajaAbierta().getCodCaja());
			ticket.setFecha(new Date());

			// localizador
			// formato: yyMMdd[codalmacen][idticket con padding][3 ultimos caracteres del
			// uid ticket]
			SimpleDateFormat format = new SimpleDateFormat("yyMMdd");
			String locator = format.format(new Date()) + sesion.getAplicacion().getCodAlmacen() + String.format("%06d", idTicket) + StringUtils.right(ticket.getUidTicket(), 3);
			ticket.setLocatorId(locator);

			/* Se codifica el pdf en base64 */
			byte[] pdfEncoded = Base64.getEncoder().encode(ticketFidelizado.getPdfFidelizado());
			ticketFidelizado.setPdfFidelizado(pdfEncoded);

			String fechaAlta = Fecha.getFecha(new Date()).getString("YYYMMdd");
			ticketFidelizado.setFechaAlta(fechaAlta);

			generaPDF(ticketFidelizado, fidelizado);

			xmlTicketFidelizado = MarshallUtil.crearXML(ticketFidelizado);
			ticket.setTicket(xmlTicketFidelizado);

			log.debug("registrarTicketFidelizado() - Guardando ticket de fidelizado");
			ticketsService.insertarTicket(null, ticket, false);
		}
		catch (Exception e) {
			log.error("registrarTicketFidelizado() - Error saving document: " + e.getMessage());
		}
		finally {

		}

	}

	public void generaPDF(TicketFidelizadoCaptacion ticketFidelizado, FidelizadoBean fidelizado) throws JRException, IOException {
		log.debug("generaPDF() - Inicio del proceso de generacion de pdf");
		Locale locale = new Locale(AppConfig.idioma, AppConfig.pais);
		FormatUtil.getInstance().init(locale);
		
		URL url = null;
		if(StringUtils.isNotBlank(sesion.getAplicacion().getTienda().getCliente().getCodpais()) && StringUtils.equals(sesion.getAplicacion().getTienda().getCliente().getCodpais(), COD_PAIS_PT)) {
			url = Thread.currentThread().getContextClassLoader().getResource(INFORME_FIDELIZADO_PT);
		}else {
			url = Thread.currentThread().getContextClassLoader().getResource(INFORME_FIDELIZADO_ES);
		}
		
		log.debug("generaPDF() - Ruta inicial de comerzzia - " + url.getPath());

		File reportFile = new File(url.getPath());
		JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportFile);
		jasperReport.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);

		Map<String, Object> parametros = new HashMap<String, Object>();
		if (fidelizado != null) {
			if (fidelizado.getTipoContacto("MOVIL") != null) {
				parametros.put("MOVIL", fidelizado.getTipoContacto("MOVIL").getValor());
				parametros.put("MOVIL_NOTIF", fidelizado.getTipoContacto("MOVIL").getRecibeNotificaciones() ? I18N.getTexto("Si") : I18N.getTexto("No"));
			}
			else {
				parametros.put("MOVIL", "");
				parametros.put("MOVIL_NOTIF", "-");
			}
			if (fidelizado.getTipoContacto("EMAIL") != null) {
				parametros.put("EMAIL", fidelizado.getTipoContacto("EMAIL").getValor());
				parametros.put("EMAIL_NOTIF", fidelizado.getTipoContacto("EMAIL").getRecibeNotificaciones() ? I18N.getTexto("Si") : I18N.getTexto("No"));
			}
			else {
				parametros.put("EMAIL", "");
				parametros.put("EMAIL_NOTIF", "-");
			}

		}
		else { // Para que no aparezca 'null' en 'Permite notificaciones'
			parametros.put("MOVIL_NOTIF", "");
			parametros.put("EMAIL_NOTIF", "");
		}
		parametros.put("DESEMP", sesion.getAplicacion().getEmpresa().getDesEmpresa());
		parametros.put("DOMICILIO", sesion.getAplicacion().getEmpresa().getDomicilio());
		parametros.put("CP", sesion.getAplicacion().getEmpresa().getCp());
		parametros.put("PROVINCIA", sesion.getAplicacion().getEmpresa().getProvincia());

		if (sesion.getAplicacion().getEmpresa().getLogotipo() != null) {
			ByteArrayInputStream bis = new ByteArrayInputStream(sesion.getAplicacion().getEmpresa().getLogotipo());
			parametros.put("LOGO", bis);
		}
		/* Se pasa por parametro la imagen de la firma */
		if (BricodepotVisorPantallaSecundaria.getFirma() != null) {
			ByteArrayInputStream bis = new ByteArrayInputStream(BricodepotVisorPantallaSecundaria.getFirma());
			parametros.put("FIRMA", bis);
		}
		List<FidelizadoBean> fidelizados = new ArrayList<FidelizadoBean>();
		fidelizados.add(fidelizado);
		JRDataSource dataSource = new JRBeanCollectionDataSource(fidelizados);

		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

		ticketFidelizado.setPdfFidelizado(outputStream.toByteArray());
		log.debug("generaPDF() - Generacion correcta del PDF");
	}

	public List<String> getNumTarjetasFidelizados(List<FidelizadoBean> fidelizados) { // BRICO-281
		log.debug("getNumTarjetasFidelizados() - Obteniendo los números de tarjetas de los fidelizados");

		Set<String> numTarjetas = new HashSet<>();
		for (FidelizadoBean fidelizado : fidelizados) {
			if (fidelizado.getNumeroTarjeta() != null && !fidelizado.getNumeroTarjeta().isEmpty()) {
				numTarjetas.add(fidelizado.getNumeroTarjeta());
			}
		}
		
		log.debug("getNumTarjetasFidelizados() - Tarjetas obtenidas: " + numTarjetas.toString());

		return new ArrayList<String>(numTarjetas);
	}
	
	public String compruebaEmailNoRepetido(String email, String documentoCabecera) { // BRICO-281
		log.debug("compruebaEmailNoRepetido() - Comprobando formato y existencia de email: " + email);

		String errorKey = BricoEmailValidator.getValidationErrorKey(email);
		if (errorKey != null) {
			return I18N.getTexto(errorKey);
		}

		String apiKey = variablesServices.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
		String uidActividad = sesion.getAplicacion().getUidActividad();
		ConsultarFidelizadoRequestRest req = new ConsultarFidelizadoRequestRest(apiKey, uidActividad);
		req.setEmail(email);

		try {
			List<FidelizadoBean> fidelizadosPorEmail = FidelizadosRest.getFidelizadosDatos(req);

			if (fidelizadosPorEmail == null || fidelizadosPorEmail.isEmpty()) {
				return BricodepotFidelizadoController.EMAIL_NO_REPETIDO;
			}
			else {
				List<String> numTarjetas = getNumTarjetasFidelizados(fidelizadosPorEmail);
				if (StringUtils.isNotBlank(documentoCabecera) && numTarjetas.contains(documentoCabecera)) {
					return BricodepotFidelizadoController.EMAIL_NO_REPETIDO;
				}
				return I18N.getTexto("Este Email ya está asignado al fidelizado con Número de Tarjeta: " + numTarjetas);
			}
		}
		catch (RestException | RestHttpException e) {
			log.error("compruebaEmailNoRepetido() - Error buscando fidelizados por email: " + email, e);
			return null;
		}
	}
    
	public String compruebaDocumentoNoRepetido(String documento, String documentoCabecera) { // BRICO-281
		log.debug("compruebaEmailNoRepetido() - Comprobando que no existe otro fidelizado con documento: " + documento);
	
		String apiKey = variablesServices.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
		String uidActividad = sesion.getAplicacion().getUidActividad();
		ConsultarFidelizadoRequestRest req = new ConsultarFidelizadoRequestRest(apiKey, uidActividad);
		req.setDocumento(documento);
	
		try {
			List<FidelizadoBean> fidelizadosPorEmail = FidelizadosRest.getFidelizadosDatos(req);
	
			if (fidelizadosPorEmail == null || fidelizadosPorEmail.isEmpty()) {
				return BricodepotFidelizadoController.DOCUMENTO_NO_REPETIDO;
			}
			else {
				List<String> numTarjetas = getNumTarjetasFidelizados(fidelizadosPorEmail);
	
				if (StringUtils.isNotBlank(documentoCabecera)) {
					if (numTarjetas.contains(documentoCabecera)) {
						return BricodepotFidelizadoController.DOCUMENTO_NO_REPETIDO;
					}
				}
	
				String msgError = I18N.getTexto("Este Documento ya está asignado al fidelizado con Número de Tarjeta: " + numTarjetas);
				return msgError;
			}
	
		}
		catch (RestException | RestHttpException e) {
			String errorMsg = "Error buscando fidelizados por documento: " + documento;
			log.error("compruebaDocumentoNoRepetido() - " + errorMsg + " : " + e.getMessage(), e);
			return null;
		}
	}	
	
	public void registrarEnlacesColectivoFidelizado(FidelizadoBean fidelizado) {
		log.debug("registrarEnlacesColectivoFidelizado() - Registrando enlace de colectivo para el fidelizado: " + fidelizado.getIdFidelizado());
		String apiKey = variablesServices.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
		String uidActividad = sesion.getAplicacion().getUidActividad();

		 try {
			 Enlace enlace = new Enlace();
			 enlace.setCodAlmacen(sesion.getAplicacion().getCodAlmacen());
			 enlace.setFechaAlta(new Date());
			 enlace.setIdFidelizado(fidelizado.getIdFidelizado());
			 enlace.setIdUsuario(sesion.getSesionUsuario().getUsuario().getIdUsuario());
			 enlace.setUidActividad(uidActividad);
			 enlace.setApiKey(apiKey);
			 BricodepotEnlacesFidelizadosRest.insertEnlacesColectivoDeFidelizado(enlace);
		}
		catch (RestHttpException | RestException e) {
			//NO es determinante para el alta, no debemos dar error en el proceso.
			log.error("No se ha podido registrar los enlaces de los colectivos para el id fidelizado "+ fidelizado.getIdFidelizado());
		}

		
	}

}
