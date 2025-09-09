package com.comerzzia.bricodepot.pos.gui.ventas.cajas.cierre;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.api.rest.client.exceptions.RestException;
import com.comerzzia.api.rest.client.exceptions.RestHttpException;
import com.comerzzia.bricodepot.pos.services.cajas.BricodepotCajasService;
import com.comerzzia.bricodepot.pos.services.core.sesion.BricodepotSesionAplicacion;
import com.comerzzia.bricodepot.pos.services.core.sesion.BricodepotSesionCaja;
import com.comerzzia.bricodepot.pos.util.CajasConstants;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.exception.ValidationException;
import com.comerzzia.pos.gui.inicio.InicioView;
import com.comerzzia.pos.gui.ventas.cajas.CajasView;
import com.comerzzia.pos.gui.ventas.cajas.cierre.CierreCajaController;
import com.comerzzia.pos.gui.ventas.cajas.cierre.CierreCajaView;
import com.comerzzia.pos.persistence.cajas.acumulados.CajaLineaAcumuladoBean;
import com.comerzzia.pos.persistence.cajas.conceptos.CajaConceptoBean;
import com.comerzzia.pos.services.cajas.Caja;
import com.comerzzia.pos.services.cajas.CajasServiceException;
import com.comerzzia.pos.services.cajas.conceptos.CajaConceptosServiceException;
import com.comerzzia.pos.services.cajas.conceptos.CajaConceptosServices;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.util.i18n.I18N;

@Component
@Primary
public class BricodepotCierreCajaController extends CierreCajaController {

	private static final Logger log = Logger.getLogger(BricodepotCierreCajaController.class.getName());
	
	@Autowired
	private Sesion sesion;
	@Autowired
	protected BricodepotCajasService cajasService;
	@Autowired
	private VariablesServices variablesServices;
	@Autowired
	protected BricodepotSesionCaja cajaSesion;
	
	@Autowired
	protected CajaConceptosServices cajaConceptosServices;
	
	@Autowired
	protected BricodepotSesionAplicacion sesionAplicacion;
	@Autowired
	protected BricodepotCajasService bricodepotCajasService;
	
	private static final String CODMEDPAG_EFECTIVO = "0000";
	
	@SuppressWarnings("unchecked")
	@Override
	protected void accionCierreCaja() {
		// No necesitamos comprobar que se pulse 2 veces seguidas el botón porque se muestra una ventana de confirmación
		log.debug("accionCierreCaja() - Se procede a cerrar la caja " + sesion.getSesionCaja().getCajaAbierta().getCodCaja() + " del usuario " + sesion.getSesionCaja().getCajaAbierta().getUsuario() + " y uidDiarioCaja " + sesion.getSesionCaja().getUidDiarioCaja());
		try {
			if (getApplication().getMainView().getSubViews().size() > 2) {
				if (!VentanaDialogoComponent.crearVentanaConfirmacion(I18N.getTexto("Para cerrar la caja se deben cerrar todas las pantallas abiertas. ¿Desea continuar?"), getStage())) {
					return;
				}
				boolean couldClose = getApplication().getMainView().closeAllViewsExcept(InicioView.class, CajasView.class, CierreCajaView.class);
				if (!couldClose) {
					return;
				}
			}

			boolean tieneDescuadres = cajaSesion.tieneDescuadres();
			Integer reintentosMax = variablesServices.getVariableAsInteger(VariablesServices.CAJA_REINTENTOS_CIERRE);

			if (tieneDescuadres) {
				if (reintentosCierre == null) {
					reintentosCierre = 0;
				}
				Integer reintentosRestantes = reintentosMax - reintentosCierre;
				if (reintentosRestantes > 0) {
					VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("Caja descuadrada con un importe mayor que el permitido. Revise recuento."), getStage());
					reintentosCierre++;
					return;
				}
				else {
					if (!VentanaDialogoComponent.crearVentanaConfirmacion(I18N.getTexto("Se va a cerrar la caja con descuadres mayores al valor permitido, ¿Desea continuar?"), getStage())) {
						return;
					}
				}
			}
			if (!VentanaDialogoComponent.crearVentanaConfirmacion(I18N.getTexto("¿Seguro de realizar el Cierre?"), getStage())) {
				return;
			}

			// Procedemos al cierre de la caja
			// Actualizamos el formulario
			formularioCierreCaja.setFechaCierre(tfFechaCierre.getTexto());
			// validamos el formulario
			accionValidarForm();
			
			CajaLineaAcumuladoBean acumulado = sesion.getSesionCaja().getCajaAbierta().getAcumulados().get(CODMEDPAG_EFECTIVO); // efectivo
			BigDecimal recuentoEfectivo = acumulado == null ? BigDecimal.ZERO : acumulado.getRecuento();
			String documento = "Cierre caja" + sesion.getSesionCaja().getCajaAbierta().getCodCaja();

			CajaConceptoBean concepto = cajaConceptosServices.getConceptoCaja(CajasConstants.PARAM_CONCEPTO_CIERRE_CAJA);
			String desConcepto = null;
			if (concepto != null) {
				desConcepto = concepto.getDesConceptoMovimiento();
			}
			else {
				throw new CajaConceptosServiceException("accionCierreCaja() - No se ha encontrado el concepto: " + CajasConstants.PARAM_CONCEPTO_CIERRE_CAJA);
			}

			/* Se genera documento con concepto 81 para la caja 80 */
			cajasService.generarDocumentoParaCajaFicticia(CajasConstants.PARAM_CAJA_80, CajasConstants.PARAM_CONCEPTO_81, recuentoEfectivo, null, documento, null);
			
			log.debug("accionCierreCaja() - Realizando el cierre de caja para la caja " + cajaSesion.getUidDiarioCaja());
			cajaSesion.guardarCierreCaja(formularioCierreCaja.getDateCierre());
			log.debug("accionCierreCaja() - Cierre de caja guardado en la base de datos para uidDiarioCaja: " + cajaSesion.getUidDiarioCaja());

			try {
				imprimirCierre(cajaSesion.getCajaAbierta());
			}
			catch (CajasServiceException ex) {
				log.error("accionCierreCaja() - No se pudo realizar la impresión del cierre de caja. ");
				VentanaDialogoComponent.crearVentanaError(getStage(), ex.getMessageI18N(), ex);
			}
			
			/* Se genera el movimiento de cierre de caja */
			log.debug("accionCierreCaja() - Realizando el movimiento de cierre de caja para la caja " + cajaSesion.getUidDiarioCaja());
			cajaSesion.crearApunteManual(recuentoEfectivo, CajasConstants.PARAM_CONCEPTO_CIERRE_CAJA, documento, desConcepto);
			
			if (!sesion.getSesionCaja().getCajaAbierta().getCodCaja().equals(CajasConstants.PARAM_CAJA_MASTER)) {
				log.debug("accionCierreCaja() - No es caja master");
				sesion.getSesionCaja().actualizarDatosCaja();
				transferirCajaAMaster(formularioCierreCaja.getDateCierre());
			}
			cajaSesion.cerrarCaja();
			log.debug("accionCierreCaja() - Caja " + cajaSesion.getUidDiarioCaja() + " cerrada exitosamente.");
			
			
			
			reintentosCierre = 0;
			if (tieneDescuadres) {
				VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("Caja cerrada con descuadres"), getStage());
			}
			getStage().close();

		}
		catch (ValidationException ex) {
			log.debug("accionCierreCaja() - La validación no fué exitosa"); // La validación ya se encarga de mostrar el
			                                                                // error
		}
		catch (CajasServiceException e) {
			log.error("accionCierreCaja() - Error al tratar de realizar cierre de caja: " + e.getCause(), e);
			VentanaDialogoComponent.crearVentanaError(getStage(), e.getMessageI18N(), e);
		}
		catch (Exception e) {
			log.error("accionCierreCaja() - Error al tratar de realizar cierre de caja: " + e.getCause(), e);
			VentanaDialogoComponent.crearVentanaError(getStage(), e.getMessage(), e);
		}
	}
	
	private void transferirCajaAMaster(Date date) {
		log.debug(" transferirCajaAMaster() - Transfiriendo caja a master");
		if(sesionAplicacion.isCajaMasterActivada()) {
			try {
		        Caja cajaAbierta = sesion.getSesionCaja().getCajaAbierta();
		        cajaAbierta.setFechaCierre(new Date());
		        log.info(" transferirCajaAMaster() - Caja abierta: " + cajaAbierta.getCodCaja() + " cerrada con fecha: " + cajaAbierta.getFechaCierre());
		        cajasService.transferirCaja(cajaAbierta);
	        }
	        catch (RestException e) {
	        	log.error("transferirCajaAMaster() - Ha habido un error al transferir la caja a la caja máster: " + e.getCause().getMessage(), e);
	        	
	        	String mensaje = I18N.getTexto("No se ha podido establecer la comunicación con la caja máster. Tendrá que cerrar caja en este terminal para poder salir.");
	        	VentanaDialogoComponent.crearVentanaError(getStage(), I18N.getTexto(mensaje), e);
	        }
	        catch (RestHttpException e) {
	        	log.error("transferirCajaAMaster() - Ha habido un error al transferir la caja a la caja máster: " + e.getCause().getMessage(), e);
	        	
	        	String mensaje = I18N.getTexto("No se ha podido abrir la caja en la caja máster, tendrá que cerrar caja en este terminal para poder salir. Contacte con un administrador.");
	        	Short codigoCajaAbierta = 7001;
	        	if(codigoCajaAbierta.equals(e.getCodError())) {
	        		mensaje = I18N.getTexto("La caja ya está abierta en otro terminal. Tendrá que cerrar caja en este terminal para poder salir.");
	        		return;
	        	}
	        	
	        	VentanaDialogoComponent.crearVentanaError(getStage(), mensaje, e);
	        }
			catch (Exception e) {
	        	log.error("transferirCajaAMaster() - Ha habido un error al transferir la caja a la caja máster: " + e.getMessage(), e);
	        	
	        	String mensaje = "Ha habido un error inesperado al transferir la caja a la caja máster. Contacte con un administrador";
	        	VentanaDialogoComponent.crearVentanaError(getStage(), mensaje, e);
			}
		}
    }
	
}
