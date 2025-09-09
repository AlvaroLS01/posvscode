package com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.email.seleccionenvio;

import java.net.URL;
import java.util.ResourceBundle;

import com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.BricodepotPagosController;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.email.EmailController;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.email.EmailView;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.controllers.WindowController;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

@Component
public class SeleccionEnvioTicketController extends WindowController {

	protected Logger log = Logger.getLogger(getClass());

	protected String emailFidelizadoCargado;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}

	@Override
	public void initializeComponents() throws InitializeGuiException {
	}

	@Override
	public void initializeFocus() {
	}

	@Override
	public void initializeForm() throws InitializeGuiException {
		emailFidelizadoCargado = null;
		
		if (getDatos().get(BricodepotPagosController.EMAIL_FIDELIZADO_CARGADO) != null) {
			emailFidelizadoCargado = (String) getDatos().get(BricodepotPagosController.EMAIL_FIDELIZADO_CARGADO);
		}
	}

	@FXML
	private void accionPapel(ActionEvent event) {
		log.debug("accionPapel()");
		getDatos().put(EmailController.TIPO_ENVIO, EmailController.PAPEL);
		getStage().close();
	}

	@FXML
	private void accionCorreo(ActionEvent event) {
		log.debug("accionCorreo()");
		enviarEmailFidelizado();

		getApplication().getMainView().showModalCentered(EmailView.class, getDatos(), getStage());

		if (getDatos().get(EmailController.EMAIL) != null) {
			getDatos().put(EmailController.TIPO_ENVIO, EmailController.CORREO);
		}

		getStage().close();
	}

	@FXML
	private void accionAmbos(ActionEvent event) {
		log.debug("accionAmbos()");
		enviarEmailFidelizado();

		getApplication().getMainView().showModalCentered(EmailView.class, getDatos(), getStage());

		if (getDatos().get(EmailController.EMAIL) != null) {
			getDatos().put(EmailController.TIPO_ENVIO, EmailController.AMBOS);
		}

		getStage().close();
	}

	private void enviarEmailFidelizado() {
		log.debug("enviarEmailFidelizado()");
		if (StringUtils.isNotBlank(emailFidelizadoCargado)) {
			getDatos().put(BricodepotPagosController.EMAIL_FIDELIZADO_CARGADO, emailFidelizadoCargado);
		}
	}

}
