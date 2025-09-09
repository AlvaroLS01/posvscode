package com.comerzzia.bricodepot.pos.gui.mantenimientos.fidelizados.colectivos;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.colectivos.ColectivoAyudaGui;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.colectivos.ColectivoController;
import com.comerzzia.pos.util.i18n.I18N;

@Primary
@Component
public class BricoColectivoController extends ColectivoController {

    @Override
    @SuppressWarnings("unchecked")
    public void initializeForm() throws InitializeGuiException {
        List<ColectivoAyudaGui> lista = (List<ColectivoAyudaGui>) getDatos().get("colectivos");

        if (lista != null) {
            for (ColectivoAyudaGui colectivo : lista) {
                String textoOriginal = colectivo.getDesColectivo();
                String traducido = I18N.getTexto(textoOriginal);
                colectivo.desColectivoProperty().set(traducido);
            }
        }
        
        super.initializeForm();
    }
}
