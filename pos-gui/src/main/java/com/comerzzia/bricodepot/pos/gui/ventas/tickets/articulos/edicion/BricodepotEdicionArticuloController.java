package com.comerzzia.bricodepot.pos.gui.ventas.tickets.articulos.edicion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Controller;

import com.comerzzia.bricodepot.pos.gui.motivos.MotivoController;
import com.comerzzia.bricodepot.pos.gui.motivos.MotivoView;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.BricodepotTicketManager;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.audit.RequestAuthorizationController;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.audit.RequestAuthorizationView;
import com.comerzzia.bricodepot.pos.persistence.motivos.Motivo;
import com.comerzzia.bricodepot.pos.services.core.impuestos.porcentajes.BricodepotPorcentajesImpuestosService;
import com.comerzzia.bricodepot.pos.services.ticket.audit.TicketAuditEvent;
import com.comerzzia.bricodepot.pos.services.ticket.lineas.BricodepotLineaTicket;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.gui.ventas.tickets.articulos.FacturacionArticulosController;
import com.comerzzia.pos.gui.ventas.tickets.articulos.edicion.EdicionArticuloController;
import com.comerzzia.pos.persistence.core.impuestos.porcentajes.PorcentajeImpuestoBean;
import com.comerzzia.pos.persistence.core.usuarios.UsuarioBean;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.format.FormatUtil;
import com.comerzzia.pos.util.i18n.I18N;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

@Primary
@Controller
public class BricodepotEdicionArticuloController extends EdicionArticuloController {

	// log
	private static final Logger log = Logger.getLogger(EdicionArticuloController.class.getName());

	public static final String TIPO_MOTIVO_CAMBIO_PRECIO_MANUAL = "cambioPrecioManual";
	public static final String COMENTARIO_MOTIVO_CAMBIO_PRECIO_NO_COMERZIA = "PRECIO TICKET ORIGINAL";
	public static final String MOTIVO_MOTIVO_CAMBIO_PRECIO_NO_COMERZIA = "OTROS";
	@FXML
	protected TextField tfIva;
	@FXML
	protected Label lbIva;

	@Autowired
	private Sesion sesion;

	private BigDecimal porcentajeIva;

	@Override
	@FXML
	public void accionAceptar() {
		log.debug("accionAceptar()");
		BigDecimal precio;

		try {
			BigDecimal cantidad = FormatUtil.getInstance().desformateaBigDecimal(tfCantidad.getText(), 3);
			tfCantidad.setText(FormatUtil.getInstance().formateaNumero(cantidad, 3));
		}
		catch (Exception e) {
			// Manejo de excepciones si es necesario
		}

		frEdicionArticulo.setCantidad(tfCantidad.getText());

		try {
			if (!bVentaProfesional) {
				precio = FormatUtil.getInstance().desformateaBigDecimal(tfPrecio.getText(), 2);
				tfPrecio.setText(FormatUtil.getInstance().formateaImporte(precio));
				frEdicionArticulo.setPrecioFinalProfesional("0");
				frEdicionArticulo.setPrecioFinal(tfPrecio.getText());
			}
			else {
				precio = FormatUtil.getInstance().desformateaBigDecimal(tfPrecio.getText(), 4);
				tfPrecio.setText(FormatUtil.getInstance().formateaNumero(precio, 4));
				frEdicionArticulo.setPrecioFinal("0");
				frEdicionArticulo.setPrecioFinalProfesional(tfPrecio.getText());
			}
		}
		catch (Exception e) {
			// Manejo de excepciones si es necesario
		}

		frEdicionArticulo.setDescuento(tfDescuento.getText());
		frEdicionArticulo.setVendedor((UsuarioBean) cbVendedor.getSelectionModel().getSelectedItem());
		frEdicionArticulo.setDesArticulo(tfDescripcion.getText());

		if (validarFormularioEdicionArticulo() && sonNumerosSerieValidos()) {
			BigDecimal nuevaCantidad = frEdicionArticulo.getCantidadAsBD();

			BigDecimal precioLinea = FormatUtil.getInstance().desformateaBigDecimal(tfPrecio.getText()).setScale(2, RoundingMode.HALF_UP);
			BigDecimal precioOriginal = lineaOriginal.getPrecioTotalSinDto().setScale(2, RoundingMode.HALF_UP);

			Boolean isIvaCambiado = !StringUtils.isBlank(tfIva.getText()) && !tfIva.getText().equals(FormatUtil.getInstance().formateaNumero(porcentajeIva));
			Boolean isPrecioCambiado = precioLinea.compareTo(precioOriginal) != 0;

			log.debug("Precio ingresado: " + precioLinea);
			log.debug("Precio original: " + precioOriginal);
			log.debug("isPrecioCambiado: " + isPrecioCambiado);

			// Verificación directa del IVA antes de aplicar cambios
	        if (isIvaCambiado) {
	            BigDecimal ivaModificado = new BigDecimal(tfIva.getText());
	            if (!validarIvaEnBaseDeDatos(ivaModificado)) {
	                VentanaDialogoComponent.crearVentanaAviso(
	                    I18N.getTexto("El IVA introducido no es correcto."),
	                    I18N.getTexto("El IVA introducido no es correcto."),
	                    getStage()
	                );
	                super.accionCancelar(); 
	                return; 
	            }
	        }
	        
			List<TicketAuditEvent> events = new ArrayList<>();

			if (isIvaCambiado || isPrecioCambiado) {
				// Verificar si ya existe un motivo de cambio de precio
				if ((!ticketManager.isEsDevolucion() && ((BricodepotLineaTicket) lineaOriginal).getMotivos() != null && !((BricodepotLineaTicket) lineaOriginal).getMotivos().isEmpty())
				        || ((BricodepotTicketManager) ticketManager).esDevolucionFlexpoint()) {
					for (Motivo motivoAuditado : ((BricodepotLineaTicket) lineaOriginal).getMotivos()) {
						if (motivoAuditado.getCodigoTipo().equals(MotivoController.CODIGO_TIPO_CAMBIO_PRECIO.toString())) {
							VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("Cambio de precio registrado"),
							        I18N.getTexto("Se ha registrado un cambio de precio anterior, debe eliminar la línea para modificarlo."), getStage());
							super.accionCancelar();
							return;
						}
					}
				}
				// Agregar evento de auditoría
				TicketAuditEvent auditEvent = TicketAuditEvent.forEvent(TicketAuditEvent.Type.CAMBIO_PRECIO, linea, sesion);
				events.add(auditEvent);
			}

			// Manejar cambios en la cantidad
			if (nuevaCantidad.compareTo(new BigDecimal(0d)) < 0 && !nuevaCantidad.equals(lineaOriginal.getCantidad())) {
				TicketAuditEvent auditEvent = TicketAuditEvent.forEvent(TicketAuditEvent.Type.DEVOLUCION, linea, sesion);
				events.add(auditEvent);
			}
			else {
				lineaOriginal.setCantidad(ticketManager.tratarSignoCantidad(nuevaCantidad, linea.getCabecera().getCodTipoDocumento()));
			}

			if (events.size() > 0) {
				abrirVentanaAutorizacion(events, getDatos());
				if (events.stream().anyMatch(e -> e.getType() == TicketAuditEvent.Type.DEVOLUCION)) {
					if (getDatos().get(RequestAuthorizationController.PERMITIR_ACCION).equals(true)) {
						lineaOriginal.setCantidad(ticketManager.tratarSignoCantidad(nuevaCantidad, linea.getCabecera().getCodTipoDocumento()));
					}
					else {
						super.accionCancelar();
						return;
					}
				}
				if (events.stream().anyMatch(e -> e.getType() == TicketAuditEvent.Type.CAMBIO_PRECIO)) {
					if (getDatos().get(RequestAuthorizationController.PERMITIR_ACCION).equals(true)) {
						// **Aplicar cambios antes de abrir la ventana de motivo**
						if (isIvaCambiado && isPrecioCambiado) {
							lineaOriginal.setPrecioSinDto(precioLinea);
							lineaOriginal.setPrecioTotalSinDto(precioLinea);
							cambiarIva();
						}
						else if (isIvaCambiado) {
							cambiarIva();
						}
						else if (isPrecioCambiado) {
							lineaOriginal.setPrecioSinDto(precioLinea);
							lineaOriginal.setPrecioTotalSinDto(precioLinea);
							// **Aquí aseguramos que el porcentaje de IVA se almacena si no ha sido cambiado**
							if (((BricodepotLineaTicket) lineaOriginal).getPorcentajeIvaConversion() == null) {
								porcentajeIva = getPorcentajeImpuestos();
								((BricodepotLineaTicket) lineaOriginal).setPorcentajeIvaConversion(porcentajeIva);
							}
						}
						lineaOriginal.setDescuentoManual(linea.getDescuentoManual());

						// **Recalcular el importe final antes de abrir la ventana de motivo**
						lineaOriginal.recalcularImporteFinal();

						// Ahora abrimos la ventana de motivo
						if (!abrirVentanaMotivo()) {
							super.accionCancelar();
							return;
						}
					}
					else {
						super.accionCancelar();
						return;
					}
				}
			}
			else {
				// Actualizar el precio directamente si no se requiere autorización
				if (isPrecioCambiado) {
					lineaOriginal.setPrecioSinDto(precioLinea);
					lineaOriginal.setPrecioTotalSinDto(precioLinea);
					// **Aquí aseguramos que el porcentaje de IVA se almacena si no ha sido cambiado**
					if (((BricodepotLineaTicket) lineaOriginal).getPorcentajeIvaConversion() == null) {
						porcentajeIva = getPorcentajeImpuestos();
						((BricodepotLineaTicket) lineaOriginal).setPorcentajeIvaConversion(porcentajeIva);
					}
				}
				if (isIvaCambiado) {
					cambiarIva();
				}
			}

			lineaOriginal.setVendedor(frEdicionArticulo.getVendedor());
			lineaOriginal.setDesArticulo(frEdicionArticulo.getDesArticulo());
			lineaOriginal.setNumerosSerie(linea.getNumerosSerie());
			lineaOriginal.recalcularImporteFinal();

			if (aplicarPromociones) {
				ticketManager.recalcularConPromociones();
			}
			if (lineaOriginal.tieneCambioPrecioManual()) {
				cambioPrecioManual();
			}
			if (lineaOriginal.tieneDescuentoManual()) {
				cambioDescuentoManual();
			}

			ticketManager.getTicket().getTotales().recalcular();

			// Refrescar la interfaz de usuario si es necesario
			// introduccionArticulosController.refrescarDatosPantalla();

			getStage().close();
		}
	}

	@Override
	public void initializeForm() {
		super.initializeForm();
		if (((BricodepotTicketManager) ticketManager).isConversionFlexpointFT()) {
			tfDescripcion.setEditable(true);
			tfIva.setVisible(true);
			lbIva.setVisible(true);
			
			BigDecimal porcentajeImpuestos = getPorcentajeImpuestos();
			tfIva.setText(FormatUtil.getInstance().formateaNumero(porcentajeImpuestos, 0));
		}
		else {
			tfDescripcion.setEditable(false);
			tfIva.setVisible(false);
			tfIva.setText("");
			lbIva.setVisible(false);
		}
	}

	private BigDecimal getPorcentajeImpuestos() {
		PorcentajeImpuestoBean porcentaje = sesion.getImpuestos().getPorcentaje(ticketManager.getTicket().getIdTratImpuestos(), linea.getCodImpuesto());
		BigDecimal porcentajeImpuestos = porcentaje.getPorcentaje();
		
		if(((BricodepotLineaTicket) linea).isConversionAFT()) {
			porcentajeImpuestos = ((BricodepotLineaTicket) linea).getPorcentajeIvaConversion();
		}
		porcentajeIva = porcentajeImpuestos;
		
		return porcentajeImpuestos;
	}

	private void cambiarIva() {
		log.debug("cambiarIva() - Cambiando el IVA para flexpoint");
		BigDecimal porcentajeIva = FormatUtil.getInstance().desformateaImporte(tfIva.getText());
		((BricodepotLineaTicket) lineaOriginal).setConversionAFT(true);
		((BricodepotLineaTicket) lineaOriginal).setPorcentajeIvaConversion(porcentajeIva);
	}

	protected void abrirVentanaAutorizacion(List<TicketAuditEvent> auditEvent, HashMap<String, Object> datos) {
		if (!((BricodepotTicketManager) ticketManager).esDevolucionFlexpoint()) {
			datos.put(RequestAuthorizationController.AUDIT_EVENT, auditEvent);
			datos.put(FacturacionArticulosController.TICKET_KEY, ticketManager);
			getApplication().getMainView().showModalCentered(RequestAuthorizationView.class, datos, this.getStage());
		}
		else {
			getDatos().put(RequestAuthorizationController.PERMITIR_ACCION, true);
		}
	}

	private Boolean abrirVentanaMotivo() {
		Motivo motivo = null;
		HashMap<String, Object> datosVentana = new HashMap<String, Object>();
		if (!((BricodepotTicketManager) ticketManager).esDevolucionFlexpoint()) {
			datosVentana.put("linea", ((BricodepotLineaTicket) lineaOriginal)); // Cambiado 'linea' por 'lineaOriginal'
			datosVentana.put("tipoMotivo", 2);
			getApplication().getMainView().showModalCentered(MotivoView.class, datosVentana, getStage());
			motivo = (Motivo) datosVentana.get("motivo");
			if (motivo != null) {
				((BricodepotLineaTicket) lineaOriginal).getMotivos().add(motivo); // Usar 'lineaOriginal' aquí también
			}
			else {
				return false;
			}
		}
		else {
			// Ticket flexPoint y cambio precio manual
			motivo = new Motivo();
			motivo.setCodigo("0");
			motivo.setDescripcion(MOTIVO_MOTIVO_CAMBIO_PRECIO_NO_COMERZIA);
			motivo.setCodigoTipo("2");
			motivo.setComentario(COMENTARIO_MOTIVO_CAMBIO_PRECIO_NO_COMERZIA);
			motivo.setPrecioSinDtoOriginal(linea.getPrecioTotalTarifaOrigen());
			motivo.setPrecioSinDtoAplicado(linea.getPrecioTotalSinDto());
			((BricodepotLineaTicket) lineaOriginal).getMotivos().add(motivo); // Usar 'lineaOriginal' aquí también
		}

		return true;
	}
	
	private boolean validarIvaEnBaseDeDatos(BigDecimal ivaModificado) {
		BricodepotPorcentajesImpuestosService impuestosService = SpringContext.getBean(BricodepotPorcentajesImpuestosService.class);
	    
	    try {
	        long idTratamientoImpuestos = sesion.getAplicacion().getTienda().getCliente().getIdTratImpuestos();
	        PorcentajeImpuestoBean impuestosActual= impuestosService.consultarPorcentajesImpuestosActualFlex(idTratamientoImpuestos, ivaModificado);
	            if (impuestosActual.getPorcentaje().compareTo(ivaModificado) == 0) {
	                return true;
	            }
	    } catch (Exception e) {
	        log.error("Error al verificar el IVA en la base de datos: " + e.getMessage(), e);
	    }
	    return false;
	}

}
