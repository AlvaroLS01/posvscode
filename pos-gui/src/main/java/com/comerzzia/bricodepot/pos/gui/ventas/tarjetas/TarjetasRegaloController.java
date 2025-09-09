package com.comerzzia.bricodepot.pos.gui.ventas.tarjetas;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.BricodepotPagosController;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.controllers.WindowController;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;

@Component
public class TarjetasRegaloController extends WindowController {

	public static final String TIPO_TARJETA_REGALO = "TIPO_TARJETA_REGALO";
	public static final String TIPO_TARJETA_R = "R";
	public static final String TIPO_TARJETA_GC = "GC";
	public static final String RECARGO_TARJETA = "RECARGO_TARJETA";

	protected Logger log = Logger.getLogger(TarjetasRegaloController.class);

	@FXML
	private Label lbTitulo, lbError;
	@FXML
	private RadioButton rbTarjetaRegalo, rbGestoComercial;
	ToggleGroup toggleGroup = new ToggleGroup();
	@FXML
	private TextField tfRecarga;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
	}

	@Override
	public void initializeComponents() throws InitializeGuiException {
		log.debug("initialiceComponents()");

		rbTarjetaRegalo.setToggleGroup(toggleGroup);
		rbGestoComercial.setToggleGroup(toggleGroup);
	}

	@Override
	public void initializeForm() throws InitializeGuiException {
		toggleGroup.selectToggle(null);
		tfRecarga.clear();
		lbError.setVisible(false);
	}

	@Override
	public void initializeFocus() {
	}

	@FXML
	public void accionAceptar() {
		log.debug("accionAceptar()");
		if (validarFormulario()) {
			getDatos().put(TIPO_TARJETA_REGALO, rbTarjetaRegalo.isSelected() ?TIPO_TARJETA_R :TIPO_TARJETA_GC);
			getDatos().put(RECARGO_TARJETA, tfRecarga.getText().trim().replace(',', '.'));
			getStage().close();
		}
	}

	@FXML
	public void accionCancelar() {
		log.debug("accionCancelar()");
		getDatos().put(BricodepotPagosController.ACCION_CANCELAR_TARJETA, Boolean.TRUE);
		getStage().close();
	}

	@FXML
	public void controlNumerico(KeyEvent event) {
		if (!Character.isDigit(event.getCharacter().charAt(0)) && event.getCharacter().charAt(0) != '.' && event.getCharacter().charAt(0) != ',') {
			event.consume();
		}
		else if ((event.getCharacter().charAt(0) == '.' || event.getCharacter().charAt(0) == ',') && (tfRecarga.getText().contains(".") || tfRecarga.getText().contains(","))) {
			event.consume();
		}
	}

	private boolean validarFormulario() {
		if((!rbGestoComercial.isSelected() && !rbTarjetaRegalo.isSelected()) || tfRecarga.getText().isEmpty() || Double.parseDouble(tfRecarga.getText().trim().replace(',', '.')) == 0.0) {
			lbError.setVisible(true);
			return false;
		}
		
		return true;
	}
}
