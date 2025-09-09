package com.comerzzia.bricodepot.pos.services.promociones.filtro;
import java.util.List;

import com.comerzzia.pos.services.promociones.LineaDocumentoPromocionable;
import com.comerzzia.pos.services.promociones.filtro.LineasAplicablesPromoBean;

public class BricodepotLineasAplicablesEditadasManualmentePromoBean extends LineasAplicablesPromoBean {
	
	@Override
	public void setLineasAplicables(List<LineaDocumentoPromocionable> lineasAplicables) {
        reset();
        this.lineasAplicables.clear();
        for (LineaDocumentoPromocionable linea : lineasAplicables) {
            if (filtroPromoExclusiva && linea.tienePromocionLineaExclusiva()){
                continue;
            }
            if (filtroLineasCantidadDecimales && tieneCantidadDecimales(linea)) {
            	continue;
            }
            this.lineasAplicables.add(linea);
            cantidadArticulos = cantidadArticulos.add(linea.getCantidad());
            importeLineasConDto = importeLineasConDto.add(linea.getImporteAplicacionPromocionConDto());
        }
        
        
	}
}
