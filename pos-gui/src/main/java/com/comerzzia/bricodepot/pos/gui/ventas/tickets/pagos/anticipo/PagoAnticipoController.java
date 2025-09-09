package com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.anticipo;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.controllers.WindowController;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

@Component
public class PagoAnticipoController extends WindowController {
	
	protected static final Logger log = Logger.getLogger(PagoAnticipoController.class);

	public static final String PARAMETRO_NUM_ANTICIPO = "NUM_ANTICIPO";
	public static final String PARAMETRO_CANCELAR = "PARAMETRO_CANCELAR";
	
	@FXML
	protected TextField tfAnticipo;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}

	@Override
	public void initializeComponents() throws InitializeGuiException {
		registrarAccionCerrarVentanaEscape();
	}

	@Override
	public void initializeFocus() {
		tfAnticipo.requestFocus();
	}

	@Override
	public void initializeForm() throws InitializeGuiException {
		tfAnticipo.setText("");
	}
	
	@FXML
	public void accionAceptar() {
		
		if (StringUtils.isNotBlank(tfAnticipo.getText())) {
			getDatos().put(PARAMETRO_NUM_ANTICIPO, tfAnticipo.getText());		
			getStage().close();
		}else {
			VentanaDialogoComponent.crearVentanaAviso("Rellene el número de anticipo para poder continuar.", getStage());
		}
	}

	@FXML
	public void accionCancelar() {
		getDatos().put(PARAMETRO_CANCELAR, true);
		getStage().close();

	}
	
	@FXML
	public void accionAceptarIntro(KeyEvent e) {
		if (e.getCode() == KeyCode.ENTER) {
			accionAceptar();
		}
	}

}
