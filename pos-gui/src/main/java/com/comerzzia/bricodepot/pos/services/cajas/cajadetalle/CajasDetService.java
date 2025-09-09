package com.comerzzia.bricodepot.pos.services.cajas.cajadetalle;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.persistence.cajas.cajadetalle.CajaDet;
import com.comerzzia.bricodepot.pos.persistence.cajas.cajadetalle.CajaDetExample;
import com.comerzzia.bricodepot.pos.persistence.cajas.cajadetalle.CajaDetExample.Criteria;
import com.comerzzia.bricodepot.pos.persistence.cajas.cajadetalle.CajaDetMapper;
import com.comerzzia.pos.persistence.cajas.movimientos.CajaMovimientoBean;

@Component
public class CajasDetService {

	protected static Logger log = Logger.getLogger(CajasDetService.class);
	
	@Autowired
	protected CajaDetMapper cajaMapper;

	public Double consultarTotalVendido(Date fecha) {
		Double totalVendido = 0.;

		try {
			CajaDetExample cajaExample = formatKeyQuery(fecha);
			totalVendido = cajaMapper.sumByExample(cajaExample);
		}
		catch (Exception e) {
			log.error("consultarTotalVendido() - Error: No se ha podido consultar el total vendido", e);
			throw e;
		}
		return totalVendido;
	}

	public static CajaDetExample formatKeyQuery(Date fechaDesde) {

		CajaDetExample cajaExample = new CajaDetExample();
		Date fechaHasta = sumarRestarDiasFecha(fechaDesde, (1));
		
		Criteria crit = cajaExample.createCriteria();
		crit.andFechaGreaterThan(fechaDesde);
		crit.andFechaLessThan(fechaHasta);
		crit.andIdDocumentoIsNotNull();
		
		return cajaExample;
	}
	
	public static Date sumarRestarDiasFecha(Date fecha, int dias) {
		Calendar calendar = Calendar.getInstance();

		calendar.setTime(fecha); // Configuramos la fecha que se recibe

		calendar.add(Calendar.DAY_OF_YEAR, dias); // numero de días a añadir, o restar en caso de días<0

		return calendar.getTime(); // Devuelve el objeto Date con los nuevos días añadidos
	}
	
	public List<CajaMovimientoBean> consultarMovimientos90(String uidActividad, String uidCajaDiario){
			
		List<CajaMovimientoBean> movimientos = new ArrayList<CajaMovimientoBean>();
		CajaDetExample cajaExample = new CajaDetExample();
		Criteria crit  = cajaExample.createCriteria();
		crit.andUidActividadEqualTo(uidActividad).andUidDiarioCajaEqualTo(uidCajaDiario).andCodconceptoMovEqualTo("90");
		
		try {
			movimientos = cajaMapper.selectByExample(cajaExample);
		} catch (Exception e) {
			log.error("consultarMovimientos() - Error: No se ha podido consultar los movimientos para la caja "+ uidCajaDiario, e);
		}
		return movimientos;
	}
	
}
