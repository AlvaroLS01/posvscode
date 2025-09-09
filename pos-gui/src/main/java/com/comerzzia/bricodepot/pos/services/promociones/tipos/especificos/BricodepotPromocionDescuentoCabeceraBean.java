package com.comerzzia.bricodepot.pos.services.promociones.tipos.especificos;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.comerzzia.bricodepot.pos.services.promociones.filtro.BricodepotFiltroLineasPromocionCabecera;
import com.comerzzia.pos.services.promociones.DocumentoPromocionable;
import com.comerzzia.pos.services.promociones.filtro.FiltroLineasPromocion;
import com.comerzzia.pos.services.promociones.tipos.especificos.PromocionDescuentoCabeceraBean;

@Component
@Primary
@Scope("prototype")
public class BricodepotPromocionDescuentoCabeceraBean extends PromocionDescuentoCabeceraBean{
	
	@Override
	protected FiltroLineasPromocion createFiltroLineasPromocion(DocumentoPromocionable documento) {
		FiltroLineasPromocion filtro = new BricodepotFiltroLineasPromocionCabecera();
		filtro.setDocumento(documento);
		filtro.setFiltrarPromoExclusivas(false);
		return filtro;
	}

}
