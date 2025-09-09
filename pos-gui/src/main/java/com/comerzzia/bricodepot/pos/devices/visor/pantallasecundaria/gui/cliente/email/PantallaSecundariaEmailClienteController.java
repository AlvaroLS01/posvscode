package com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.cliente.email;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.BricodepotVisorPantallaSecundaria;
import com.comerzzia.bricodepot.pos.gui.componentes.VentanaEspera;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.email.EmailController;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.controllers.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;

@Component
public class PantallaSecundariaEmailClienteController extends Controller {

	protected static final Logger log = Logger.getLogger(PantallaSecundariaEmailClienteController.class);

	protected BricodepotVisorPantallaSecundaria visor;
	
	@FXML
	protected Button btValidar, btRechazar;

	@FXML
	protected TextField tfEmail;

	@FXML
	protected ScrollPane scrollPane;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}

	@Override
	public void initializeComponents() throws InitializeGuiException {
		setShowKeyboard(false);
		scrollPane.setStyle("-fx-font-size: 40px");
	}

	@Override
	public void initializeFocus() {
	}

	@Override
	public void initializeForm() throws InitializeGuiException {
	}

	@FXML
	private void accionValidar(ActionEvent event) {
		log.debug("accionValidar()");
		visor.confirmacionValidacionCliente();
	}

	@FXML
	private void accionRechazar(ActionEvent event) {
		log.debug("accionRechazar()");
		visor.modoEspera();
		VentanaEspera.cerrar();
	}

	public void refrescarDatosPantalla(BricodepotVisorPantallaSecundaria visor, EmailController correoController) {
		log.debug("refrescarDatosPantalla()");
		this.visor = visor;
		String email = correoController.getTfEmail().getText();
		tfEmail.setText(email);
	}
}
