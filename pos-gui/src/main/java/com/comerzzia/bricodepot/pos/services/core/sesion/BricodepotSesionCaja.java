package com.comerzzia.bricodepot.pos.services.core.sesion;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.services.cajas.BricodepotCajasService;
import com.comerzzia.bricodepot.pos.util.CajasConstants;
import com.comerzzia.pos.services.cajas.Caja;
import com.comerzzia.pos.services.cajas.CajaEstadoException;
import com.comerzzia.pos.persistence.cajas.acumulados.CajaLineaAcumuladoBean;
import com.comerzzia.pos.persistence.cajas.conceptos.CajaConceptoBean;
import com.comerzzia.pos.services.cajas.CajasServiceException;
import com.comerzzia.pos.services.cajas.conceptos.CajaConceptosServices;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.sesion.SesionCaja;
import com.comerzzia.pos.util.bigdecimal.BigDecimalUtil;

@Primary
@Component
public class BricodepotSesionCaja extends SesionCaja {
	
	@Autowired
	protected BricodepotCajasService bricodepotCajasService;
	
	@Autowired
	protected CajaConceptosServices cajaConceptosServices;
	
	@Autowired
	protected Sesion sesion;
	

	
	public void setCajaAbierta(Caja cajaAbierta) {
		this.cajaAbierta = cajaAbierta;
	}
	
	public void abrirCajaManualAportacion(Date fecha, BigDecimal importe) throws CajasServiceException, CajaEstadoException {
        try {
            // Antes comprobamos que no hay ya caja abierta
            cajaAbierta = cajasService.consultarCajaAbierta();
            log.debug("abrirCajaManualAportacion() - La caja ya se encontraba abierta en BBDD. ");
        }
        catch (CajaEstadoException e) {
            log.debug("abrirCajaManualAportacion() - Abriendo nueva caja con parámetros indicados.. ");
            cajaAbierta = cajasService.crearCaja(fecha);
            if (BigDecimalUtil.isMayorACero(importe)) {
                cajasService.crearMovimientoApertura(importe, fecha);
                actualizarDatosCaja();
            }
        }
	}
	@Override
	public void abrirCajaManual(Date fecha, BigDecimal importe) throws CajasServiceException, CajaEstadoException {
	log.debug("abrirCajaManual() - abriendo caja manual");
	  try {
          // Antes comprobamos que no hay ya caja abierta
          cajaAbierta = cajasService.consultarCajaAbierta();
          log.debug("abrirCajaManual() - La caja ya se encontraba abierta en BBDD. ");
      }
      catch (CajaEstadoException e) {
          log.debug("abrirCajaManual() - Abriendo nueva caja con parámetros indicados.. ");
          cajaAbierta = cajasService.crearCaja(fecha);
      }
	
	}
	

}
