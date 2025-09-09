package com.comerzzia.bricodepot.pos.gui.ventas.codigopostal;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.controllers.WindowController;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.util.i18n.I18N;

import org.springframework.beans.factory.annotation.Autowired;

@Component
public class CodigoPostalController extends WindowController {

	protected static final Logger log = Logger.getLogger(CodigoPostalController.class);

	public static final String PARAMETRO_CODIGO_POSTAL = "paramCodPostal";

	@FXML
	protected TextField tfCodPostal;
	@FXML
	protected Label lbTitulo;
	@FXML
	protected Button btnAceptar;
	@FXML
	protected Button btnCancelar;
	@Autowired
	protected Sesion sesion;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
	}

	@Override
	public void initializeComponents() {
		registrarAccionCerrarVentanaEscape();
	}

	@Override
	public void initializeForm() throws InitializeGuiException {
		tfCodPostal.clear();

		tfCodPostal.lengthProperty().addListener(new ChangeListener<Number>(){

			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number valorAnterior, Number valorActual) {
				if (valorActual.intValue() > valorAnterior.intValue()) {
					String input = tfCodPostal.getText();
					if (input.length() > 8) {
						tfCodPostal.setText(input.substring(0, 8));
						return;
					}
					Pattern permitido = null;
					Matcher mpermitido = null;

					if (input.length() == 1) {
						permitido = Pattern.compile("^[0-9A]$");
						mpermitido = permitido.matcher(tfCodPostal.getText().substring(valorAnterior.intValue()));
					}
					if (input.length() == 2) {
						permitido = Pattern.compile("^[0-9D]$");
						mpermitido = permitido.matcher(tfCodPostal.getText().substring(valorAnterior.intValue()));
					}
					if (input.length() == 3 || input.length() == 4 || input.length() == 6 || input.length() == 7 || input.length() == 8) {
						permitido = Pattern.compile("^[0-9]$");
						mpermitido = permitido.matcher(tfCodPostal.getText().substring(valorAnterior.intValue()));
					}

					if (input.length() == 5) {
						permitido = Pattern.compile("^[0-9-]$");
						mpermitido = permitido.matcher(tfCodPostal.getText().substring(valorAnterior.intValue()));
					}
					if (!mpermitido.find()) {
						// caracter no permitido, borrarlo
						tfCodPostal.setText(tfCodPostal.getText().substring(0, valorAnterior.intValue()));
						return;
					}
				}
			}
		});
	}

	@Override
	public void initializeFocus() {
		tfCodPostal.requestFocus();
	}

	@FXML
	public void accionAceptar() {
		if (validarCodigoPostal(tfCodPostal.getText())) {
			getDatos().put(PARAMETRO_CODIGO_POSTAL, tfCodPostal.getText());
			getStage().close();
		}
		else {
			VentanaDialogoComponent.crearVentanaError(I18N.getTexto("Formato incorrecto"), getStage());
			tfCodPostal.requestFocus();
		}
	}

	@FXML
	public void accionCancelar() {
		getStage().close();
	}

	public void accionTeclas(KeyEvent event) {
		if (event.getCode() == KeyCode.ESCAPE) {
			accionCancelar();
		}
		else if (event.getCode() == KeyCode.ENTER) {
			accionAceptar();
		}
	}

	public boolean validarCodigoPostal(String cp) {
		log.debug("validarCodigoPostal() - Validando codigo postal: " + cp);
		boolean validado = false;

		if (cp.length() == 5) {
			// ANDORRA
			Pattern patron = Pattern.compile("^A[D]\\d{3}$");
			Matcher matcher = patron.matcher(cp);
			if (matcher.matches()) {
				validado = true;
			}
			// ESPAÑA
			if (!validado) {
				patron = Pattern.compile("\\d{5}");
				matcher = patron.matcher(cp);
				if (matcher.matches()) {
					validado = true;
				}
			}
		}
		// PORTUGAL
		if (cp.length() == 8) {
			Pattern patron = Pattern.compile("^[0-9]{4}-[0-9]{3}$");
			Matcher matcher = patron.matcher(cp);
			if (matcher.matches()) {
				validado = true;
			}
		}

		return validado;
	}

}
