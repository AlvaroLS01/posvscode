package com.comerzzia.bricodepot.pos.services.core.impuestos.porcentajes;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.comerzzia.core.util.mybatis.session.SqlSession;
import com.comerzzia.pos.persistence.core.impuestos.porcentajes.PorcentajeImpuestoBean;
import com.comerzzia.pos.persistence.core.impuestos.porcentajes.PorcentajeImpuestoExample;
import com.comerzzia.pos.persistence.mybatis.SessionFactory;
import com.comerzzia.pos.services.core.impuestos.porcentajes.PorcentajesImpuestosService;

@SuppressWarnings("deprecation")
@Service
@Primary
public class BricodepotPorcentajesImpuestosService extends PorcentajesImpuestosService {
	
	
	
	public PorcentajeImpuestoBean consultarPorcentajesImpuestosActualFlex(long idTratamientoImpuestos, BigDecimal porcentajeIvaConversion) {
		SqlSession sqlSession = new SqlSession();
		try {
			sqlSession.openSession(SessionFactory.openSession());
			String uidActividad = sesion.getAplicacion().getUidActividad();

			PorcentajeImpuestoExample filtro = new PorcentajeImpuestoExample();
			filtro.or().andUidActividadEqualTo(uidActividad).andIdTratamientoImpuestosEqualTo(idTratamientoImpuestos);

			List<PorcentajeImpuestoBean> porcentajesImp = porcentajeImpuestoMapper.selectByExample(filtro);

			for (PorcentajeImpuestoBean impuesto : porcentajesImp) {
				if (impuesto.getPorcentaje().compareTo(porcentajeIvaConversion) == 0) {
					return impuesto;
				}
			}

		}
		catch (Exception e) {
			String msg = "Se ha producido un error consultando porcentaje de impuestos con vigencia actual. " + e.getMessage();
			log.error("consultarPorcentajesImpuestosActual() - " + msg, e);
		}
		finally {
			sqlSession.close();
		}
		return null;
	}
}
