package com.comerzzia.bricodepot.pos.gui.ventas.gestiontickets.detalle;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Controller;

import com.comerzzia.bricodepot.pos.gui.ventas.tickets.BricodepotTicketManager;
import com.comerzzia.bricodepot.pos.services.ticket.BricodepotCabeceraTicket;
import com.comerzzia.bricodepot.pos.services.ticket.BricodepotTicketVentaAbono;
import com.comerzzia.core.util.base64.Base64Coder;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.gui.ventas.gestiontickets.detalle.DetalleGestionticketsController;
import com.comerzzia.pos.persistence.core.documentos.tipos.TipoDocumentoBean;
import com.comerzzia.pos.persistence.tickets.TicketBean;
import com.comerzzia.pos.services.core.documentos.DocumentoException;
import com.comerzzia.pos.services.devices.DeviceException;
import com.comerzzia.pos.services.ticket.ITicket;
import com.comerzzia.pos.services.ticket.TicketsServiceException;
import com.comerzzia.pos.services.ticket.cabecera.CabeceraTicket;
import com.comerzzia.pos.services.ticket.cabecera.TarjetaRegaloTicket;
import com.comerzzia.pos.services.ticket.cabecera.TotalesTicket;
import com.comerzzia.pos.services.ticket.lineas.LineaTicket;
import com.comerzzia.pos.services.ticket.pagos.PagoTicket;
import com.comerzzia.pos.services.ticket.pagos.tarjeta.DatosPeticionPagoTarjeta;
import com.comerzzia.pos.services.ticket.pagos.tarjeta.DatosRespuestaPagoTarjeta;
import com.comerzzia.pos.servicios.impresion.ServicioImpresion;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.i18n.I18N;
import com.comerzzia.pos.util.xml.MarshallUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javafx.application.Platform;
import javafx.event.ActionEvent;

@SuppressWarnings("unchecked")
@Controller
@Primary
public class BricodepotDetalleGestionticketsController extends DetalleGestionticketsController {
	
	private static final Logger log = Logger.getLogger(DetalleGestionticketsController.class.getName());
	
	@Override
	public void refrescarDatosPantalla() throws InitializeGuiException {
		try {
            log.debug("refrescarDatosPantalla()");

            log.debug("Obtenemos el XML del ticket que queremos visualizar");
            
            this.ticket = tickets.get(posicionActual);

            TicketBean ticketConsultado = null;
            byte[] ticketXML = null;
            
        	ticketConsultado = ticketsService.consultarTicket(ticket.getUidTicket(), sesion.getAplicacion().getUidActividad());
        	ticketXML = ticketConsultado.getTicket();
            	
            TipoDocumentoBean documento = sesion.getAplicacion().getDocumentos().getDocumento(ticketConsultado.getIdTipoDocumento());
            if(documento.getFormatoImpresion().equals(TipoDocumentoBean.PROPIEDAD_FORMATO_IMPRESION_NO_CONFIGURADO)){
            	if(getStage() != null && getStage().isShowing()){
            		VentanaDialogoComponent.crearVentanaInfo(I18N.getTexto("No es posible visualizar este tipo de documento"), getStage());
            	}else{
            		Platform.runLater(new Runnable() {
						@Override
						public void run() {
							VentanaDialogoComponent.crearVentanaInfo(I18N.getTexto("No es posible visualizar este tipo de documento"), getStage());
						}
					});
            	}
            	setTicketText("<html><body></body></html>");
            	return;
            }
            
            ticketOperacion = (BricodepotTicketVentaAbono) MarshallUtil.leerXML(ticketXML, getTicketClasses(documento).toArray(new Class[]{})); 
            
            if (ticketOperacion != null) {
            	ticketOperacion.getCabecera().setDocumento(sesion.getAplicacion().getDocumentos().getDocumento(ticketOperacion.getCabecera().getTipoDocumento()));
            	if(sesion.getAplicacion().getDocumentos().getDocumento(ticketOperacion.getCabecera().getTipoDocumento()).getPermiteTicketRegalo()){
            		btnTicketRegalo.setDisable(false);
            	}
            	else{
            		btnTicketRegalo.setDisable(true);
            	}  
            	try{
					Map<String, Object> mapaParametros = new HashMap<String, Object>();
					mapaParametros.put("ticket", ticketOperacion);
					mapaParametros.put("BRICO_CABECERA", (BricodepotCabeceraTicket) ticketOperacion.getCabecera());
	                mapaParametros.put("urlQR", variablesService.getVariableAsString("TPV.URL_VISOR_DOCUMENTOS"));
	                mapaParametros.put("esGestion", true);
	                if(ticketOperacion.getCabecera().getCodTipoDocumento().equals("NC")||ticketOperacion.getCabecera().getCodTipoDocumento().equals("FRECTIFIC") 
	                		||ticketOperacion.getCabecera().getCodTipoDocumento().equals("FS")||ticketOperacion.getCabecera().getCodTipoDocumento().equals("FT")
	                		||ticketOperacion.getCabecera().getCodTipoDocumento().equals("FRS")) {
	                	mapaParametros.put("esDuplicado", true);
	                }
	                if (ticketOperacion.getCabecera().getCodTipoDocumento().equals("NC")||ticketOperacion.getCabecera().getCodTipoDocumento().equals("FRECTIFIC")) {
						mapaParametros.put("esDobleImpresion",true);

					}

	                boolean hayPagosTarjeta = false;
					for (Object pago : ticketOperacion.getPagos()) {
						if (pago instanceof PagoTicket && ((PagoTicket) pago).getDatosRespuestaPagoTarjeta() != null) {
							hayPagosTarjeta = true;
							break;
						}
					}
					
		            addPagoGiftcard(mapaParametros);
		            
					if (hayPagosTarjeta) {
						mapaParametros.put("listaPagosTarjeta", getPagosTarjetas());
						mapaParametros.put("listaPagosTarjetaDatosPeticion", getPagosTarjetasDatosPeticion());
					}
	                
					if (ticketOperacion.getCabecera().getCodTipoDocumento().equals("FT") || ticketOperacion.getCabecera().getCodTipoDocumento().equals("FR")) {
						mapaParametros.put("esImpresionA4", true);
						
						if (ticketOperacion.getCabecera().getCodTipoDocumento().equals("FR")) {
							mapaParametros.put("DEVOLUCION", true);
						}
					}
					else {
						mapaParametros.put("esImpresionA4", false);
					}
					
					if (sesion.getAplicacion().getEmpresa().getLogotipo() != null) {
						InputStream is = new ByteArrayInputStream(sesion.getAplicacion().getEmpresa().getLogotipo());
						mapaParametros.put("LOGO", is);
						is.close();
					}
					
		            mapaParametros.put("esCopia", true);
					
		            if(mapaParametros.get("ticket") instanceof BricodepotTicketVentaAbono) {
						BricodepotTicketVentaAbono ticketVentaAbono = (BricodepotTicketVentaAbono) ticketOperacion;
						if(StringUtils.isNotBlank(ticketVentaAbono.getNumPedido())){
							mapaParametros.put("numPedido", ticketVentaAbono.getNumPedido());
						}
						if(StringUtils.isNotBlank(ticketVentaAbono.getNumTarjetaRegalo())) {
							mapaParametros.put("numTarjetaRegalo", ticketVentaAbono.getNumTarjetaRegalo());	
						}
					}
					
					addQR(ticketOperacion, mapaParametros);
					// Hay que obtener el resultado de mostrar en pantalla el ticket y mostrarlo en taTicket
	            
	            	String previsualizacion = ServicioImpresion.imprimirPantalla(ticketOperacion.getCabecera().getFormatoImpresion(),mapaParametros);
	            	setTicketText(previsualizacion);	                
            	}catch (Exception e) {
        			VentanaDialogoComponent.crearVentanaError(getStage(), I18N.getTexto("Lo sentimos, ha ocurrido un error al imprimir."), e);
        			throw new InitializeGuiException(false);
        		}
            }
            else {
                log.error("refrescarDatosPantalla()- Error leyendo ticket otriginal");
                VentanaDialogoComponent.crearVentanaError(I18N.getTexto("Error leyendo información de ticket."), getStage());
                throw new InitializeGuiException(false);
            }

        }
        catch (TicketsServiceException ex) {
            log.error("refrescarDatosPantalla() - " + ex.getMessage());
            VentanaDialogoComponent.crearVentanaError(getStage(), I18N.getTexto("Error leyendo información de ticket"), ex);
        } catch (DocumentoException e) {
			log.error("Error recuperando el tipo de documento del ticket.",e);
		}
	}
	@SuppressWarnings("rawtypes")
	private void addQR(ITicket ticketOrigen, Map<String, Object> parameters) throws Exception, IOException {
		if (ticketOrigen.getCabecera() instanceof BricodepotCabeceraTicket) {
			log.debug("addQr() - La información fiscal ya viene en el ticket.");

			if (ticketOrigen.getCabecera().getFiscalData() != null) {
				if (ticketOrigen.getCabecera().getFiscalData().getProperties() != null && !ticketOrigen.getCabecera().getFiscalData().getProperties().isEmpty() && ticketOrigen.getCabecera().getFiscalData().getProperty(BricodepotTicketManager.PROPERTY_QR) != null) {

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

	public List<Class<?>> getTicketClasses(TipoDocumentoBean tipoDocumento) {
		List<Class<?>> classes = new LinkedList<>();

		// Obtenemos la clase root
		Class<?> clazz = SpringContext.getBean(getTicketClass(tipoDocumento)).getClass();

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
		classes.add(SpringContext.getBean(LineaTicket.class).getClass());
		classes.add(SpringContext.getBean(CabeceraTicket.class).getClass());
		classes.add(SpringContext.getBean(TotalesTicket.class).getClass());
		classes.add(SpringContext.getBean(PagoTicket.class).getClass());

		return classes;
	}

	protected List<DatosRespuestaPagoTarjeta> getPagosTarjetas() {
		log.debug("getPagosTarjetas()");
		List<DatosRespuestaPagoTarjeta> listaPagosTarjeta = new ArrayList<DatosRespuestaPagoTarjeta>();
		List<PagoTicket> listaPagos = ticketOperacion.getPagos();
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

		List<PagoTicket> listaPagos = ticketOperacion.getPagos();
		for (PagoTicket pago : listaPagos) {
			if (pago.getDatosRespuestaPagoTarjeta() != null) {
				DatosRespuestaPagoTarjeta datosRespuestaPagoTarjeta = pago.getDatosRespuestaPagoTarjeta();
				DatosPeticionPagoTarjeta datosPeticion = datosRespuestaPagoTarjeta.getDatosPeticion();
				listaPagosTarjeta.add(datosPeticion);
			}
		}
		return listaPagosTarjeta;
	}
	
	@Override
	protected void accionImprimirCopia(ActionEvent event) {
		log.debug("accionImprimirCopia()");
		try {
			Map<String, Object> mapaParametros = new HashMap<String, Object>();
			mapaParametros.put("ticket", ticketOperacion);
			mapaParametros.put("urlQR", variablesService.getVariableAsString("TPV.URL_VISOR_DOCUMENTOS"));
			mapaParametros.put("esCopia", true);
			mapaParametros.put("esDuplicado", true);

			ServicioImpresion.imprimir(ticketOperacion.getCabecera().getFormatoImpresion(), mapaParametros);
		}
		catch (DeviceException ex) {
			VentanaDialogoComponent.crearVentanaInfo(I18N.getTexto("Fallo al imprimir ticket."), getStage());
		}
	}
	
	private void addPagoGiftcard(Map<String, Object> mapaParametros) {
		List<PagoTicket> listaPagos = ticketOperacion.getPagos();
		BigDecimal pagoGiftcard = new BigDecimal(0);
		String uidTransaccion = "";
		BigDecimal saldo = new BigDecimal(0);
		BigDecimal importePago = new BigDecimal(0);
		BigDecimal importeRecarga = new BigDecimal(0);
		TarjetaRegaloTicket tarjetaRegalo = new TarjetaRegaloTicket();
		for (PagoTicket pago : listaPagos) {
			if(pago.getGiftcards()!=null && !pago.getGiftcards().isEmpty()) {
				uidTransaccion = pago.getGiftcards().get(0).getUidTransaccion();
				saldo = pago.getGiftcards().get(0).getSaldo();
				importePago = pago.getGiftcards().get(0).getImportePago();
				importeRecarga = pago.getGiftcards().get(0).getImporteRecarga();
				pagoGiftcard = pago.getGiftcards().get(0).getImportePago();
							
			}
		}
		tarjetaRegalo.setUidTransaccion(uidTransaccion);
		tarjetaRegalo.setSaldo(saldo);
		tarjetaRegalo.setImporteRecarga(importeRecarga);
		ticketOperacion.getCabecera().setTarjetaRegalo(tarjetaRegalo);
		mapaParametros.put("pagoGiftcard", pagoGiftcard);
	}
}
