package com.comerzzia.bricodepot.pos.gui.ventas.tickets.articulos.busquedas;


import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Controller;

import com.comerzzia.pos.gui.ventas.tickets.articulos.busquedas.BuscarArticulosController;
import com.comerzzia.pos.persistence.articulos.buscar.ArticulosParamBuscar;

import javafx.fxml.FXML;

@Primary
@Controller
public class BricodepotBuscarArticulosController extends BuscarArticulosController{

	/**
	 * Solo esta aqui para debugear 
	 */
	@Override
	@FXML
	public void accionBuscar() {
		//log.trace("accionBuscar()");

        //Vaciamos el resultado de la busqueda anterior
        lineas.clear();
        //Limpiamos los detalles del artículo de la posible selección anterior
        tfDetalleCodArticulo.setText("");
        tfDetalleDescripcion.setText("");
        tfDetallePrecio.setText("");
        tfDetalleDesglose1.setText("");
        tfDetalleDesglose2.setText("");
        
        frBusquedaArt.setCodArticulo(tfCodigoArt.getText());
        frBusquedaArt.setDescripcion(tfDescripcion.getText());
        
        if (validarFormularioBusqueda()) {
            ArticulosParamBuscar parametrosArticulo = new ArticulosParamBuscar();
            parametrosArticulo.setCodigo(tfCodigoArt.getText().toUpperCase());
            parametrosArticulo.setDescripcion(tfDescripcion.getText());
            parametrosArticulo.setCliente(clienteBusqueda);
            parametrosArticulo.setCodTarifa(codTarifaBusqueda);
            //DEBUG: buscar todo articulos esten activos o no
            parametrosArticulo.setActivo(true);
            
            BuscarArticulosTask buscarArticulosTask = new BuscarArticulosTask(parametrosArticulo);
            buscarArticulosTask.start();
        }
	}
	
	

}
