package com.comerzzia.bricodepot.pos.devices.fidelizacion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.comerzzia.api.rest.client.fidelizados.ConsultarFidelizadoRequestRest;
import com.comerzzia.api.rest.client.fidelizados.FidelizadosRest;
import com.comerzzia.api.rest.client.fidelizados.ResponseGetFidelizadoColectivoRest;
import com.comerzzia.api.rest.client.fidelizados.ResponseGetFidelizadoEtiquetasRest;
import com.comerzzia.api.rest.client.fidelizados.ResponseGetFidelizadoRest;
import com.comerzzia.pos.core.dispositivos.dispositivo.fidelizacion.ConsultaTarjetaFidelizadoException;
import com.comerzzia.pos.core.dispositivos.dispositivo.fidelizacion.IFidelizacion;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.dispositivo.fidelizacion.FidelizacionCodigoBarras;
import com.comerzzia.pos.persistence.fidelizacion.FidelizacionBean;
import com.comerzzia.pos.services.core.sesion.SesionPromociones;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.i18n.I18N;

import javafx.stage.Stage;

public class BricodepotFidelizacionCodigoBarras extends FidelizacionCodigoBarras implements IFidelizacion {
	
	public static final Logger log = Logger.getLogger(BricodepotFidelizacionCodigoBarras.class.getName());
	
	@Override
	public void ignorarTarjetaFidelizado() {
		if (currentTask != null) {
			currentTask.cancel();
		}
	}
	
	@Override
	public FidelizacionBean consultarTarjetaFidelizado(Stage stage, String numTarjRegalo, String uidActividad) throws ConsultaTarjetaFidelizadoException {
    	try {
	    	FidelizacionBean tarjetaFidelizacion = null;
	        ConsultarFidelizadoRequestRest paramConsulta = new ConsultarFidelizadoRequestRest(wsApiKey, uidActividad);
	        
	        paramConsulta.setNumeroTarjeta(numTarjRegalo);
	        
	        ResponseGetFidelizadoRest result = FidelizadosRest.getFidelizado(paramConsulta);
	        tarjetaFidelizacion = new FidelizacionBean();
	        tarjetaFidelizacion.setIdFidelizado(result.getIdFidelizado());
	        tarjetaFidelizacion.setNumTarjetaFidelizado(result.getNumeroTarjeta());
	        tarjetaFidelizacion.setBaja(result.getBaja().equals("S"));
	        tarjetaFidelizacion.setActiva(result.getActiva().equals("S"));
	        tarjetaFidelizacion.setSaldo(BigDecimal.valueOf(result.getSaldo()));
	        tarjetaFidelizacion.setSaldoProvisional(BigDecimal.valueOf(result.getSaldoProvisional()));
	        List<String> codColectivos = new ArrayList<>();
	        if(result.getColectivos() != null && !result.getColectivos().isEmpty()) {
		        for (ResponseGetFidelizadoColectivoRest responseGetFidelizadoColectivoRest : result.getColectivos()) {
		        	codColectivos.add(responseGetFidelizadoColectivoRest.getCodigo());
				}
	        }
	        tarjetaFidelizacion.setCodColectivos(codColectivos);
	        List<String> uidEtiquetas = new ArrayList<String>();
	        if(result.getEtiquetas() != null && !result.getEtiquetas().isEmpty()){
	        	for(ResponseGetFidelizadoEtiquetasRest responseGetFidelizadoEtiquetaRest : result.getEtiquetas()){
	        		uidEtiquetas.add(responseGetFidelizadoEtiquetaRest.getUidEtiqueta());
	        	}
	        }
	        tarjetaFidelizacion.setUidEtiquetas(uidEtiquetas);
	        tarjetaFidelizacion.setNombre(result.getNombre());
	        tarjetaFidelizacion.setApellido(result.getApellidos());
	        tarjetaFidelizacion.setCodTipoIden(result.getCodTipoIden());
	        tarjetaFidelizacion.setCp(result.getCp());
	        tarjetaFidelizacion.setDocumento(result.getDocumento());
	        tarjetaFidelizacion.setDomicilio(result.getDomicilio());
	        tarjetaFidelizacion.setLocalidad(result.getLocalidad());
	        tarjetaFidelizacion.setPoblacion(result.getPoblacion());
	        tarjetaFidelizacion.setProvincia(result.getProvincia());
	        tarjetaFidelizacion.setCodPais(result.getCodPais());
	        tarjetaFidelizacion.setDesPais(result.getDesPais());
	        tarjetaFidelizacion.setPaperLess(result.getPaperLess());
	        
	        SesionPromociones promotionSession = SpringContext.getBean(SesionPromociones.class);
	        if(promotionSession.isLoadedLoyaltyModule()) {
		        try {
		        	getCustomerCoupons(tarjetaFidelizacion);
		        }catch(Exception e) {
		        	String msg = "El fidelizado se ha consultado correctamente, pero ha ocurrido un error cargando sus cupones disponibles. ";
		        	log.error(msg, e);
		        	if(stage != null) {
						if (currentTask != null && !currentTask.isCancelled()) {
							VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto(msg + "Consulte con el administrador."), stage);
						}
		        	}
		        	
		        }
	        }
			
	
	        return tarjetaFidelizacion;
		} catch (Exception e) {
			throw new ConsultaTarjetaFidelizadoException(e);
		}
    }

}
