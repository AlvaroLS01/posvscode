package com.comerzzia.bricodepot.pos.services.promociones;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.core.servicios.ContextHolder;
import com.comerzzia.core.util.xml.XMLDocumentException;
import com.comerzzia.pos.persistence.promociones.PromocionBean;
import com.comerzzia.pos.persistence.promociones.tipos.PromocionTipoBean;
import com.comerzzia.pos.services.promociones.Promocion;
import com.comerzzia.pos.services.promociones.PromocionesBuilder;
import com.comerzzia.pos.services.promociones.PromocionesBuilderException;
import com.comerzzia.pos.util.config.SpringContext;

@Primary
@Component
public class BricodepotPromocionesBuilder extends PromocionesBuilder {

private Logger log = Logger.getLogger(BricodepotPromocionesBuilder.class);
	
	@Override
	public Promocion create(PromocionBean promocionBean) throws PromocionesBuilderException {
		log.debug("create() - Instanciando promociónn: " + promocionBean);
		try {
			PromocionTipoBean tipoPromocion = promocionBean.getTipoPromocion();
			tipoPromocion.parseConfiguracion();
			String manejador = tipoPromocion.getManejador();
			if (manejador == null) {
				throw new PromocionesBuilderException("No hay definido \"<Manejador>\" (className) dentro de la configuración del idTipoPromocion " + tipoPromocion.getIdTipoPromocion());
			}
			Promocion promocion = (Promocion) ContextHolder.getBean(manejador);
			
			promocion = SpringContext.getBean(promocion.getClass());			
			
			promocion.init(promocionBean);
			return promocion;
		} catch (XMLDocumentException | ClassNotFoundException e) {
			throw new PromocionesBuilderException(e);
		}
	}

}
