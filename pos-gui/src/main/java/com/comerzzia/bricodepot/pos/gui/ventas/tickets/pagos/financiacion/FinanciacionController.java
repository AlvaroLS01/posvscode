package com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.financiacion;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.controllers.WindowController;
import com.comerzzia.pos.persistence.mediosPagos.MedioPagoBean;
import com.comerzzia.pos.util.i18n.I18N;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

@Component
public class FinanciacionController extends WindowController {

	protected static final Logger log = Logger.getLogger(FinanciacionController.class);

	public static final String PARAMETRO_DOCUMENTO = "DOCUMENTO";
	private MedioPagoBean medioPago;
	@FXML
	private TextField tfDocumento;
	@FXML
	private Label lbTitulo, lbDocumento, lbError;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}

	@Override
	public void initializeComponents() throws InitializeGuiException {
		registrarAccionCerrarVentanaEscape();

	}

	@Override
	public void initializeFocus() {
		tfDocumento.requestFocus();

	}

	@Override
	public void initializeForm() throws InitializeGuiException {
		lbError.setText("");
		medioPago =  (MedioPagoBean) getDatos().get("medioPago");
		String documentoOrigen = (String) getDatos().get(medioPago.getCodMedioPago());
		if(StringUtils.isNotBlank(documentoOrigen)){
			tfDocumento.setText(documentoOrigen);
		}else {
			tfDocumento.setText("");
		}
	
	}

	@FXML
	public void accionAceptarIntro(KeyEvent e) {
		if (e.getCode() == KeyCode.ENTER) {
			accionAceptar();
		}
	}

	@FXML
	public void accionAceptar() {
		if (StringUtils.isNotBlank(tfDocumento.getText())) {
			if (tfDocumento.getText().length() > 40) {
				VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("El documento no puede exceder 40 caracteres."), getStage());
				lbError.setText(I18N.getTexto("El documento no puede exceder 40 caracteres."));
			}else if(!comprobarFormatoDocumentoEcommerce(tfDocumento.getText())) {
				lbError.setText(I18N.getTexto("Formato incorrecto, 10 cifras y empezar por 2 o 3, o numero de 9 cifras"));
			}
			else {
				datos.put(PARAMETRO_DOCUMENTO, tfDocumento.getText());
				getStage().close();
			}
		}
		else {
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("Introduzca un valor para poder continuar."), getStage());
		}
	}

	@FXML
	public void accionCancelar() {
		getStage().close();
	}

	private boolean comprobarFormatoDocumentoEcommerce(String codDocumento) {
		log.debug("comprobarFormatoDocumentoEcommerce() - Validando formato del documento: "+codDocumento);
		boolean esValido = false;
		if(medioPago!=null) {
			if (medioPago.getDesMedioPago().equals("ECOMMERCE")) {
				esValido = codDocumento.matches("^[23]\\d{9}$");
				if (!esValido) {
					esValido = codDocumento.matches("^\\d{9}$");
				}
				return esValido;
			}
		}
		return true;

	}

}
