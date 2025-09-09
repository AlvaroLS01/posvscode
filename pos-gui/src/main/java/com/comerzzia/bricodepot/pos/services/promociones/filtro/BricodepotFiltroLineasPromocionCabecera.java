package com.comerzzia.bricodepot.pos.services.promociones.filtro;
import com.comerzzia.pos.services.promociones.filtro.FiltroLineasPromocion;
import com.comerzzia.pos.services.promociones.filtro.LineasAplicablesPromoBean;


public class BricodepotFiltroLineasPromocionCabecera extends FiltroLineasPromocion {
	
	@Override
	protected LineasAplicablesPromoBean createLineasAplicables() {
		LineasAplicablesPromoBean aplicables = new BricodepotLineasAplicablesEditadasManualmentePromoBean();
		aplicables.setFiltroPromoExclusiva(filtrarPromoExclusivas);
		return aplicables;
	}

	
}
