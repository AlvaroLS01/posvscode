package com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.anticipo.seleccion;

import java.net.URL;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.util.AnticiposConstants;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.controllers.WindowController;

import javafx.fxml.FXML;

@Component
public class SeleccionAnticipoController extends WindowController {

	public static final String PARAMETRO_COBRAR_PAGAR = "PARAMETRO_COBRAR_PAGAR";

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}

	@Override
	public void initializeComponents() throws InitializeGuiException {
		registrarAccionCerrarVentanaEscape();
	}

	@Override
	public void initializeFocus() {
	}

	@Override
	public void initializeForm() throws InitializeGuiException {
	}

	@FXML
	public void accionCobrar() {
		getDatos().put(PARAMETRO_COBRAR_PAGAR, AnticiposConstants.PARAMETRO_COBRAR_ANTICIPO);
		getStage().close();
	}

	@FXML
	public void accionPagar() {
		getDatos().put(PARAMETRO_COBRAR_PAGAR, AnticiposConstants.PARAMETRO_PAGAR_ANTICIPO);
		getStage().close();
	}

}
