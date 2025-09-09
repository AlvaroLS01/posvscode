package com.comerzzia.bricodepot.pos.services.cliente;

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
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.comerzzia.api.rest.client.clientes.ResponseGetClienteRest;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.BricodepotVisorPantallaSecundaria;
import com.comerzzia.core.util.base.Estado;
import com.comerzzia.core.util.fechas.Fecha;
import com.comerzzia.pos.persistence.clientes.ClienteBean;
import com.comerzzia.pos.persistence.tickets.TicketBean;
import com.comerzzia.pos.services.core.contadores.ServicioContadores;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.ticket.TicketsService;
import com.comerzzia.pos.servicios.impresion.ImpresionJasper;
import com.comerzzia.pos.util.config.AppConfig;
import com.comerzzia.pos.util.format.FormatUtil;
import com.comerzzia.pos.util.xml.MarshallUtil;

import org.apache.commons.lang3.StringUtils;
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
import rest.bean.cliente.BricodepotClienteBean;

@Service
public class BricodepotClientesService {

	protected static final Logger log = Logger.getLogger(BricodepotClientesService.class.getName());

	public static final String ID_CLIENTE_CAP = "ID_CLIENTE_CAP";
	public static final long ID_TIPO_DOC_CAPTACION_FIDELIZADO = 100001L;

	@Autowired
	private Sesion sesion;
	@Autowired
	private ServicioContadores contadoresService;
	@Autowired
	private TicketsService ticketsService;

	public void registrarTicketCliente(TicketClienteCaptacion ticketCliente, ClienteBean cliente) {
		TicketBean ticket;
		byte[] xmlTicketCliente = null;
		log.debug("registrarTicketCliente() - Construyendo objeto persistente");
		try {
			// Construimos objeto persistente
			ticket = new TicketBean();

			// uid documento
			String uidTicket = UUID.randomUUID().toString();
			ticket.setUidTicket(uidTicket);
			// id documento
			Long idTicket = contadoresService.obtenerValorContador(ID_CLIENTE_CAP, sesion.getAplicacion().getUidActividad());
			ticket.setIdTicket(idTicket);
			// serie documento
			String serieTicket = sesion.getAplicacion().getCodAlmacen() + "/" + sesion.getAplicacion().getCodCaja();
			ticket.setSerieTicket(serieTicket);
			// cod documento
			String codigoTicket = sesion.getAplicacion().getCodAlmacen() + "/" + sesion.getAplicacion().getCodCaja() + "/" + String.format("%08d", idTicket);
			ticket.setCodTicket(codigoTicket);
			// firma documento (no
			ticket.setFirma("*");
			// tipo documento
			ticket.setIdTipoDocumento(ID_TIPO_DOC_CAPTACION_FIDELIZADO);

			ticket.setCodAlmacen(sesion.getAplicacion().getCodAlmacen());
			ticket.setCodcaja(sesion.getAplicacion().getCodCaja());
			ticket.setFecha(new Date());

			// localizador
			// formato: yyMMdd[codalmacen][idticket con padding][3 ultimos caracteres del
			// uid ticket]
			SimpleDateFormat format = new SimpleDateFormat("yyMMdd");
			String locator = format.format(new Date()) + sesion.getAplicacion().getCodAlmacen() + String.format("%06d", idTicket) + StringUtils.right(ticket.getUidTicket(), 3);
			ticket.setLocatorId(locator);

			/* Se codifica el pdf en base64 */
			byte[] pdfEncoded = Base64.getEncoder().encode(ticketCliente.getPdfCliente());
			ticketCliente.setPdfCliente(pdfEncoded);

			String fechaAlta = Fecha.getFecha(new Date()).getString("YYYMMdd");
			ticketCliente.setFechaAlta(fechaAlta);

			generaPDF(ticketCliente, cliente);

			xmlTicketCliente = MarshallUtil.crearXML(ticketCliente);
			ticket.setTicket(xmlTicketCliente);

			log.debug("registrarTicketCliente() - Guardando ticket de cliente");
			ticketsService.insertarTicket(null, ticket, false);
		}
		catch (Exception e) {
			log.error("registrarTicketCliente() - Error saving document: " + e.getMessage(), e);
		}
		finally {

		}

	}

	public void generaPDF(TicketClienteCaptacion ticketCliente, ClienteBean cliente) throws JRException, IOException {
		log.debug("generaPDF() - Inicio del proceso de generacion de pdf");
		Locale locale = new Locale(AppConfig.idioma, AppConfig.pais);
		FormatUtil.getInstance().init(locale);

		URL url = Thread.currentThread().getContextClassLoader().getResource("plantillas/jasper/clientes/formulariocliente.jasper");
		log.debug("generaPDF() - Ruta inicial de comerzzia - " + url.getPath());

		File reportFile = new File(url.getPath());
		JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportFile);
		jasperReport.setWhenNoDataType(WhenNoDataTypeEnum.ALL_SECTIONS_NO_DETAIL);

		List<ClienteBean> clientes = new ArrayList<ClienteBean>();
		HashMap<String, Object> parametros = new HashMap<String, Object>();

		// Muestra check de alta o modificación dependiendo de lo que es
		parametros.put("ES_ALTA", Boolean.valueOf(cliente.getEstadoBean() == Estado.NUEVO));

		clientes.add(cliente);
		parametros.put(ImpresionJasper.LISTA, clientes);

		if (sesion.getAplicacion().getEmpresa().getLogotipo() != null) {
			ByteArrayInputStream bis = new ByteArrayInputStream(sesion.getAplicacion().getEmpresa().getLogotipo());
			parametros.put("LOGO", bis);
		}

		/* Se pasa por parametro la imagen de la firma */
		if (BricodepotVisorPantallaSecundaria.getFirma() != null) {
			ByteArrayInputStream bis = new ByteArrayInputStream(BricodepotVisorPantallaSecundaria.getFirma());
			parametros.put("FIRMA", bis);
		}

		JRDataSource dataSource = new JRBeanCollectionDataSource(clientes);

		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

		ticketCliente.setPdfCliente(outputStream.toByteArray());
		log.debug("generaPDF() - Generacion correcta del PDF");
	}

	/* BRICO-253 por incompatibilidad de nombres de campos de bean de Cliente en api-pos-bo se crea este casteo */
	public static ClienteBean castClienteApiAPos(ResponseGetClienteRest clienteAPI) {
		ClienteBean cliente = new ClienteBean();
		cliente.setCodCliente(clienteAPI.getCodCliente());
		cliente.setDesCliente(clienteAPI.getDesCliente());
		cliente.setNombreComercial(clienteAPI.getNombreComercial());
		cliente.setDomicilio(clienteAPI.getDomicilio());
		cliente.setPoblacion(clienteAPI.getPoblacion());
		cliente.setProvincia(clienteAPI.getProvincia());
		cliente.setCp(clienteAPI.getCp());
		cliente.setTelefono1(clienteAPI.getTelefono1());
		cliente.setTelefono2(clienteAPI.getTelefono2());
		cliente.setFax(clienteAPI.getFax());
		cliente.setCodpais(clienteAPI.getCodPais());
		cliente.setPersonaContacto(clienteAPI.getPersonaContacto());
		cliente.setEmail(clienteAPI.getEmail());
		cliente.setCif(clienteAPI.getCif());
		cliente.setIdTratImpuestos(clienteAPI.getIdTratImp());
		cliente.setIdMedioPagoVencimiento(clienteAPI.getIdMedioPagoVencimiento());

		cliente.setCodtar(clienteAPI.getCodTar());
		cliente.setCodsec(clienteAPI.getCodSec());
		cliente.setBanco(clienteAPI.getBanco());
		cliente.setBancoDomicilio(clienteAPI.getDomicilioBanco());
		cliente.setBancoPoblacion(clienteAPI.getPoblacionBanco());
		cliente.setCcc(clienteAPI.getCcc());
		cliente.setObservaciones(clienteAPI.getObservaciones());
		cliente.setActivo(clienteAPI.isActivo());
		cliente.setFechaAlta(clienteAPI.getFechaAlta());
		cliente.setFechaBaja(clienteAPI.getFechaBaja());
		cliente.setRiesgoConcedido(clienteAPI.getRiesgoConcedido());
		cliente.setLocalidad(clienteAPI.getLocalidad());
		cliente.setPais(clienteAPI.getDesPais());
		cliente.setTipoIdentificacion(clienteAPI.getCodTipoIden());
//		cliente.setDeposito(clienteAPI.getDeposito());
//		cliente.setCodlengua(clienteAPI.getCodLengua());

//		cliente.setEstadoBean(clienteAPI.getEstadoBean());

		return cliente;
	}

	/* BRICO-253 por incompatibilidad de nombres de campos de bean de Cliente en api-pos-bo se crea este casteo */
	public static BricodepotClienteBean castClientePosAApi(ClienteBean cliente) {
		if (cliente == null)
			return null;
		BricodepotClienteBean clienteAPI = new BricodepotClienteBean();
		clienteAPI.setCodCliente(cliente.getCodCliente());
		clienteAPI.setDesCliente(cliente.getDesCliente());
		clienteAPI.setNombreComercial(cliente.getNombreComercial());
		clienteAPI.setDomicilio(cliente.getDomicilio());
		clienteAPI.setPoblacion(cliente.getPoblacion());
		clienteAPI.setProvincia(cliente.getProvincia());
		clienteAPI.setCp(cliente.getCp());
		clienteAPI.setTelefono1(cliente.getTelefono1());
		clienteAPI.setTelefono2(cliente.getTelefono2());
		clienteAPI.setFax(cliente.getFax());
		clienteAPI.setCodpais(cliente.getCodpais());
		clienteAPI.setPersonaContacto(cliente.getPersonaContacto());
		clienteAPI.setEmail(cliente.getEmail());
		clienteAPI.setCif(cliente.getCif());
		clienteAPI.setIdTratImpuestos(cliente.getIdTratImpuestos());
		clienteAPI.setIdMedioPagoVencimiento(cliente.getIdMedioPagoVencimiento());

		clienteAPI.setCodtar(cliente.getCodtar());
		clienteAPI.setCodsec(cliente.getCodsec());
		clienteAPI.setBanco(cliente.getBanco());
		clienteAPI.setBancoDomicilio(cliente.getBancoDomicilio());
		clienteAPI.setBancoPoblacion(cliente.getBancoPoblacion());
		clienteAPI.setCcc(cliente.getCcc());
		clienteAPI.setObservaciones(cliente.getObservaciones());
		clienteAPI.setActivo(cliente.getActivo());
		clienteAPI.setFechaAlta(cliente.getFechaAlta());
		clienteAPI.setFechaBaja(cliente.getFechaBaja());
		clienteAPI.setRiesgoConcedido(cliente.getRiesgoConcedido());
		clienteAPI.setLocalidad(cliente.getLocalidad());
		clienteAPI.setPais(cliente.getPais());
		clienteAPI.setTipoIdentificacion(cliente.getTipoIdentificacion());
		clienteAPI.setDeposito(cliente.getDeposito());
		clienteAPI.setCodlengua(cliente.getCodlengua());

		clienteAPI.setEstadoBean(cliente.getEstadoBean());

		return clienteAPI;
	}

	
	/* Por problemas de compatibilidad entre POS/SCO en la recuperacion de clientes se crea otro metodo de casteo */
	public static ClienteBean castClienteApiAPos(BricodepotClienteBean clienteAPI) {
		ClienteBean cliente = new ClienteBean();
		cliente.setCodCliente(clienteAPI.getCodCliente());
		cliente.setDesCliente(clienteAPI.getDesCliente());
		cliente.setNombreComercial(clienteAPI.getNombreComercial());
		cliente.setDomicilio(clienteAPI.getDomicilio());
		cliente.setPoblacion(clienteAPI.getPoblacion());
		cliente.setProvincia(clienteAPI.getProvincia());
		cliente.setCp(clienteAPI.getCp());
		cliente.setTelefono1(clienteAPI.getTelefono1());
		cliente.setTelefono2(clienteAPI.getTelefono2());
		cliente.setFax(clienteAPI.getFax());
		cliente.setCodpais(clienteAPI.getCodpais());
		cliente.setPersonaContacto(clienteAPI.getPersonaContacto());
		cliente.setEmail(clienteAPI.getEmail());
		cliente.setCif(clienteAPI.getCif());
		cliente.setIdTratImpuestos(clienteAPI.getIdTratImpuestos());
		cliente.setIdMedioPagoVencimiento(clienteAPI.getIdMedioPagoVencimiento());

		cliente.setCodtar(clienteAPI.getCodtar());
		cliente.setCodsec(clienteAPI.getCodsec());
		cliente.setBanco(clienteAPI.getBanco());
		cliente.setBancoDomicilio(clienteAPI.getBancoDomicilio());
		cliente.setBancoPoblacion(clienteAPI.getBancoPoblacion());
		cliente.setCcc(clienteAPI.getCcc());
		cliente.setObservaciones(clienteAPI.getObservaciones());
		cliente.setActivo(clienteAPI.getActivo());
		cliente.setFechaAlta(clienteAPI.getFechaAlta());
		cliente.setFechaBaja(clienteAPI.getFechaBaja());
		cliente.setRiesgoConcedido(clienteAPI.getRiesgoConcedido());
		cliente.setLocalidad(clienteAPI.getLocalidad());
		cliente.setPais(clienteAPI.getPais());
		cliente.setTipoIdentificacion(clienteAPI.getTipoIdentificacion());
//		cliente.setDeposito(clienteAPI.getDeposito());
//		cliente.setCodlengua(clienteAPI.getCodLengua());

//		cliente.setEstadoBean(clienteAPI.getEstadoBean());

		return cliente;
	}
}
