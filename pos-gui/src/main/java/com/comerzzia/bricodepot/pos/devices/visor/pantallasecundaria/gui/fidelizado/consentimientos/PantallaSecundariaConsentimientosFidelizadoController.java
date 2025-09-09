package com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.fidelizado.consentimientos;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.BricodepotVisorPantallaSecundaria;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.controllers.Controller;
import com.comerzzia.pos.util.config.AppConfig;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;

@Component
public class PantallaSecundariaConsentimientosFidelizadoController extends Controller {

	protected static final Logger log = Logger.getLogger(PantallaSecundariaConsentimientosFidelizadoController.class.getName());

	
	@FXML
	protected WebView wvTexto;

	protected BricodepotVisorPantallaSecundaria visor;

	@Override
	public void initialize(URL url, ResourceBundle rb) {

	}

	@Override
	public void initializeComponents() throws InitializeGuiException {

	}

	@Override
	public void initializeForm() throws InitializeGuiException {

	}

	@Override
	public void initializeFocus() {

	}

	public void refrescarDatosPantalla(BricodepotVisorPantallaSecundaria visor) {
		log.debug("refrescarDatosPantalla()");
		this.visor = visor;

		try {
			cargarTexto();
		}
		catch (IOException e) {
		}
	}

	@FXML
	public void accionAceptar() {
		log.debug("accionAceptar()");
		visor.confirmacionValidacionFidelizado("SI");
	}

	@FXML
	public void accionRechazar() {
		log.debug("accionRechazar()");
		visor.confirmacionValidacionFidelizado("NO");
	}

	private void cargarTexto() throws FileNotFoundException, IOException {
		log.debug("cargarTexto()");
		URL url = Thread.currentThread().getContextClassLoader().getResource("textos_legales/Privacidad" + AppConfig.pais.toUpperCase() + ".htm");

		try (FileInputStream inputStream = new FileInputStream(url.getPath())) {
			log.debug("cargarTexto() - Url del fichero de consentimientos: " + url.getPath());
			String everything = IOUtils.toString(inputStream, "UTF-8");

			wvTexto.getEngine().loadContent(everything);
		}
	}
}
