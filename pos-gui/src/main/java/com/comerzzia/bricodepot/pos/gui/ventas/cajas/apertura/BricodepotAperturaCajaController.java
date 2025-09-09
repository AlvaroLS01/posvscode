package com.comerzzia.bricodepot.pos.gui.ventas.cajas.apertura;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.services.cajas.BricodepotCajasService;
import com.comerzzia.bricodepot.pos.services.core.sesion.BricodepotSesionCaja;
import com.comerzzia.bricodepot.pos.util.CajasConstants;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.gui.ventas.cajas.apertura.AperturaCajaController;
import com.comerzzia.pos.gui.ventas.cajas.apertura.AperturaCajaFormularioBean;
import com.comerzzia.pos.persistence.cajas.conceptos.CajaConceptoBean;
import com.comerzzia.pos.services.cajas.CajaEstadoException;
import com.comerzzia.pos.services.cajas.CajasServiceException;
import com.comerzzia.pos.services.cajas.conceptos.CajaConceptosServiceException;
import com.comerzzia.pos.services.cajas.conceptos.CajaConceptosServices;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.util.format.FormatUtil;
import com.comerzzia.pos.util.i18n.I18N;
@Primary
@Component
public class BricodepotAperturaCajaController extends AperturaCajaController {
	private static final Logger log = Logger.getLogger(BricodepotAperturaCajaController.class.getName());
	@Autowired
	protected Sesion sesion;
	@Autowired
	protected BricodepotCajasService cajasService;
	@Autowired
	protected CajaConceptosServices cajaConceptosServices;
	@Autowired
	protected BricodepotCajasService bricodepotCajasService;
	
	@Autowired
	private BricodepotSesionCaja cajaSesion;

	public boolean comprobarNegativo(String cantidad) {
		if (cantidad.contains(",")) {
			cantidad = cantidad.replace(",", ".");
		}
		BigDecimal cantidadDecimal = new BigDecimal(cantidad);
        return cantidadDecimal.compareTo(BigDecimal.ZERO) >= 0;
	}
	@Override
	public void accionAceptar() {
		log.debug("accionAceptar() - Inicio del proceso de apertura de caja");

		try {
			log.debug("accionAceptar()");
            //Validar formulario                  
            formularioAperturaGui = new AperturaCajaFormularioBean(tfFecha.getTexto(), tfSaldo.getText());
            if (!accionValidarForm()) {
                return;
            }
            if (!comprobarNegativo(tfSaldo.getText())) {
    			VentanaDialogoComponent.crearVentanaError(I18N.getTexto("No se pueden hacer inserciones negativas."), getStage());
    			return;
    		}
            Date fechaApertura = FormatUtil.getInstance().desformateaFechaHora(tfFecha.getTexto(), true);
            
            Calendar calendarHoy = Calendar.getInstance();
            calendarHoy.set(Calendar.HOUR_OF_DAY, 0);
            calendarHoy.set(Calendar.MINUTE, 0);
            calendarHoy.set(Calendar.SECOND, 0);
            calendarHoy.set(Calendar.MILLISECOND, 0);
            
            //Si la fecha de apertura no es hoy, la ponemos sin hora
            Calendar calendarAperturaSinHora = Calendar.getInstance();
            calendarAperturaSinHora.setTime(fechaApertura);
            calendarAperturaSinHora.set(Calendar.HOUR_OF_DAY, 0);
            calendarAperturaSinHora.set(Calendar.MINUTE, 0);
            calendarAperturaSinHora.set(Calendar.SECOND, 0);
            calendarAperturaSinHora.set(Calendar.MILLISECOND, 0);
            if(!calendarAperturaSinHora.equals(calendarHoy)){
            	fechaApertura = calendarAperturaSinHora.getTime();
            }
			cajaSesion.abrirCajaManual(fechaApertura, formularioAperturaGui.getSaldoAsBigDecimal());
			CajaConceptoBean concepto = cajaConceptosServices.getConceptoCaja(CajasConstants.PARAM_CONCEPTO_APERTURA_CAJA);
			String desConcepto = null;
			if (concepto != null) {
				desConcepto = concepto.getDesConceptoMovimiento();
			}else {
				throw new CajaConceptosServiceException("accionAceptar() - No se ha encontrado el concepto: "+ CajasConstants.PARAM_CONCEPTO_APERTURA_CAJA);
			}
			/*Se crea el movimiento 01*/
			cajaSesion.crearApunteManual(formularioAperturaGui.getSaldoAsBigDecimal(), CajasConstants.PARAM_CONCEPTO_APERTURA_CAJA, null, desConcepto);
			/*Se genera documento con concepto 81 para la caja 80*/
			cajasService.generarDocumentoParaCajaFicticia(CajasConstants.PARAM_CAJA_80,CajasConstants.PARAM_CONCEPTO_80,null,formularioAperturaGui.getSaldoAsBigDecimal(),null,null);
	
			getStage().close();
		}
		catch (Exception e) {
			log.debug("accionAceptar() - Ha ocurrido un error : " + e.getMessage());
			if (e instanceof CajasServiceException) {
				log.debug("accionAceptar() - Ha ocurrido un error consultando la caja : "+ e.getMessage());	
			}else if(e instanceof CajaEstadoException) {
				log.debug("accionAceptar() - Ha ocurrido un error con el estado de la caja : "+ e.getMessage());
			}
		}
	}
}
