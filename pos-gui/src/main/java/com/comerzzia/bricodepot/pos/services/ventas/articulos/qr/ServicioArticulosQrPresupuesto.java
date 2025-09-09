package com.comerzzia.bricodepot.pos.services.ventas.articulos.qr;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.gui.ventas.tickets.audit.RequestAuthorizationController;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.audit.RequestAuthorizationView;
import com.comerzzia.bricodepot.pos.services.ticket.audit.TicketAuditEvent;
import com.comerzzia.bricodepot.posservices.client.PresupuestosApi;
import com.comerzzia.bricodepot.posservices.client.model.PresupuestoVenta;
import com.comerzzia.core.servicios.api.ComerzziaApiManager;
import com.comerzzia.core.servicios.sesion.DatosSesionBean;
import com.comerzzia.pos.core.gui.POSApplication;
import com.comerzzia.pos.gui.ventas.tickets.TicketManager;
import com.comerzzia.pos.gui.ventas.tickets.articulos.FacturacionArticulosController;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.util.config.AppConfig;
import javafx.stage.Stage;

@Component

@Service
public class ServicioArticulosQrPresupuesto {

	protected Logger log = Logger.getLogger(ServicioArticulosQrPresupuesto.class);

	@Autowired
	protected Sesion sesion;
	@Autowired
    protected ComerzziaApiManager comerzziaApiManager;
	@Autowired
	protected VariablesServices variablesServices;

	@Autowired
	protected TicketManager ticketManager;
	
	public static final String X_POS_DIAS_LIMITE_LECTURA_QR_PRESUPUESTO = "X_POS.DIAS_LIMITE_LECTURA_QR_PRESUPUESTO";

	/**
	 * Se comprueba si el codigo qr es valido y se pueden recuperar articulos
	 * 
	 * @param codArticulo
	 * @param datos
	 * @param stage
	 * @return true or false
	 */
	public String comprobarCodigoQrRecuperarArticulos(String codArticulo) {
		boolean validacionCabecera = false;
		boolean validacionLineas = false;
		log.debug("comprobarCodigoQrRecuperarArticulos() - Comprobando si el codigo :" + codArticulo + " es un QR que permite recuperar articulos");
		String[] lineas = codArticulo.split("#");

		boolean presupuestoUtilizado = comprobarPresupuestoUtilizado(lineas[0].replace("ID-", ""));
		if(presupuestoUtilizado) {
			return "presupuestoEmpleado";
		}
		
		// Validar cabecera
		try {
			if (lineas.length >= 2 && validarCabecera(lineas[0], lineas[1])) {
				validacionCabecera = true;
			}
		} catch (PresupuestoExpiradoException e) {
			return "presupuestoExpirado";
		}

		// Validar detalle de líneas
		for (int i = 2; i < lineas.length; i++) {
			if (validarDetalleLinea(lineas[i])) {
				validacionLineas = true;
			}
		}
		
		

		if (validacionCabecera && validacionLineas) {
			return "qrValido";
		}
		else if (!validacionCabecera && !validacionLineas) {
			return "isArticulo";
		}
		else if (validacionCabecera && !validacionLineas) {
			return "lineasQrNoValida";
		}
		return "qrNoValido";
	}

	/**
	 * Abrimos ventana autorizacion en la stage donde se llama al metodo el servicio
	 * 
	 * @param auditEvent
	 * @param datos
	 * @param stage
	 */
	protected void abrirVentanaAutorizacion(TicketAuditEvent auditEvent, HashMap<String, Object> datos, Stage stage) {
		List<TicketAuditEvent> events = new ArrayList<>();
		events.add(auditEvent);
		datos.put(RequestAuthorizationController.AUDIT_EVENT, events);
		datos.put(FacturacionArticulosController.TICKET_KEY, ticketManager);
		POSApplication.getInstance().getMainView().showModalCentered(RequestAuthorizationView.class, datos, stage);
	}

	/**
	 * Validamos la cabecera y en caso de que sea valida comprobamos la fecha para los articulos
	 * 
	 * @param identificador
	 * @param fechaValidacion
	 * @param datos
	 * @param stage
	 * @return true or false
	 */
	private boolean validarCabecera(String identificador, String fechaValidacion) {
		boolean cabeceraValida = false;
		try {
			LocalDate fecha = LocalDate.parse(fechaValidacion, DateTimeFormatter.ISO_LOCAL_DATE);
			LocalDate fechaActual = LocalDate.now();
			Integer diasLimite = variablesServices.getVariableAsInteger(X_POS_DIAS_LIMITE_LECTURA_QR_PRESUPUESTO);
			// VALIDACION 1 MES FECHAS
			cabeceraValida = (fecha.isAfter(fechaActual.minusMonths(1).minusDays(1)) && fecha.isBefore(fechaActual.plusMonths(1).plusDays((long)diasLimite + 1L))) || fecha.equals(fechaActual);
			if (!cabeceraValida) {
				log.error("validarCabecera() - El presupuesto " + identificador + " ha expirado.");
				throw new PresupuestoExpiradoException("El presupuesto ha expirado, por favor revise el presupuesto");			
			}
		}
		catch (DateTimeParseException e) {
			return false;
		}
		
		return cabeceraValida;
	}

	/**
	 * Validamos los detalles de linea relacionados con el articulo a insertar
	 * 
	 * @param linea
	 * @return true or false
	 */
	private static boolean validarDetalleLinea(String linea) {
		String[] partes = linea.split(",");
		if (partes.length != 3) {
			return false;
		}

		try {
			Long.parseLong(partes[0]);
			Integer.parseInt(partes[1]);
			Double.parseDouble(partes[2]);
		}
		catch (NumberFormatException e) {
			return false;
		}

		return true;
	}

	/**
	 * Comprobamos si el presupuesto ha sido utilizado en otro ticket.
	 * 
	 * @param presupuesto
	 * @return true or false
	 */
	@SuppressWarnings("unused")
	public boolean comprobarPresupuestoUtilizado(String presupuesto) {
		log.debug("comprobarPresupuestoUtilizado() Comprobando si el presupuesto " + presupuesto + " ha sido utilizado");
		try {
			// Llamada servicio REST para consultar tabla en base de datos central
			DatosSesionBean datosSesion = new DatosSesionBean();
			datosSesion.setUidActividad(sesion.getAplicacion().getUidActividad());
			datosSesion.setUidInstancia(sesion.getAplicacion().getUidInstancia());
			datosSesion.setLocale(new Locale(AppConfig.idioma, AppConfig.pais));
			PresupuestosApi api = comerzziaApiManager.getClient(datosSesion, "PresupuestoApi");
			PresupuestoVenta presupuestoConsultado = api.getPresupuestoUtilizado(Long.parseLong(presupuesto.replace("ID-", "")), sesion.getAplicacion().getUidActividad());
			
			if(presupuestoConsultado==null) {
				return false;
			}else {
				return true;
			}
		}
		catch (Exception e) {
			log.error("comprobarPresupuestoUtilizado() Ha ocurrido un error al comprobar si el presupuesto ha sido utilizado " + e.getMessage(), e);
		}
		return false;
	}
	
	public void validarVigenciaPresupuesto(LocalDate fechaValidez, Integer diasLimite) {
	    LocalDate hoy = LocalDate.now();

	    long diasDiferencia = ChronoUnit.DAYS.between(fechaValidez, hoy);
	    if(Math.abs(diasDiferencia) > diasLimite && fechaValidez.isAfter(hoy)) {
	    	// Si ha expirado, pero está dentro del límite, requiere validación.
		    log.error("validarVigenciaPresupuesto() - El presupuesto requiere validación del supervisor.");
		    throw new ValidacionRequeridaPresupuestoException("El presupuesto requiere validación del supervisor");
		    
    	} 
	}
}
