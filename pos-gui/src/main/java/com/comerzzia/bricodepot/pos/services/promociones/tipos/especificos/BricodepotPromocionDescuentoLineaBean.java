package com.comerzzia.bricodepot.pos.services.promociones.tipos.especificos;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.services.promociones.filtro.BricodepotFiltroLineasPromocionCabecera;
import com.comerzzia.pos.services.promociones.DocumentoPromocionable;
import com.comerzzia.pos.services.promociones.filtro.FiltroLineasPromocion;
import com.comerzzia.pos.services.promociones.tipos.especificos.PromocionDescuentoLineaBean;

@Component
@Scope("prototype")
@Primary
public class BricodepotPromocionDescuentoLineaBean extends PromocionDescuentoLineaBean {
	@Override
	protected FiltroLineasPromocion createFiltroLineasPromocion(DocumentoPromocionable documento) {
		FiltroLineasPromocion filtro = new BricodepotFiltroLineasPromocionCabecera();
		filtro.setDocumento(documento);
		filtro.setFiltrarPromoExclusivas(false);
		return filtro;
	}

}
