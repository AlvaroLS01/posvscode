package com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.fidelizado.info;

import java.net.URL;
import java.util.ResourceBundle;

import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.BricodepotVisorPantallaSecundaria;
import com.comerzzia.bricodepot.pos.gui.componentes.VentanaEspera;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.controllers.Controller;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.datosgenerales.PaneDatosGeneralesController;
import com.comerzzia.pos.services.core.sesion.Sesion;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

@Component
public class PantallaSecundariaInfoFidelizadoController extends Controller {

	protected static final Logger log = Logger.getLogger(PantallaSecundariaInfoFidelizadoController.class.getName());
	
	public static final String ESTADO = "ESTADO";
	public static final String VALIDADO = "VALIDADO";
	public static final String RECHAZADO = "RECHAZADO";

	protected BricodepotVisorPantallaSecundaria visor;

	@FXML
	protected ScrollPane scrollPane;

	@FXML
	protected HBox hbNombre, hbApellidos, hbDocumento, hbPais, hbEmail, hbMovil, hbColectivo, hbCP, hbProvincia, hbLocalidad, hbPoblacion, hbDomicilio, hbEstadoCivil, hbSexo, hbTiendaFavorita,
	        hbFechaNacimiento;

	@FXML
	protected TextField tfNombre, tfApellidos, tfDocumento, tfPais, tfEmail, tfMovil, tfColectivo, tfCP, tfProvincia, tfLocalidad, tfPoblacion, tfDomicilio, tfEstadoCivil, tfSexo, tfTiendaFavorita,
	        tfFechaNacimiento;

	@FXML
	protected Label lbNombre, lbDocumento;

	@Autowired
	protected Sesion sesion;
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
	}

	@Override
	public void initializeComponents() throws InitializeGuiException {
		setShowKeyboard(false);

		scrollPane.setStyle("-fx-font-size: 40px");

	}

	@Override
	public void initializeForm() throws InitializeGuiException {
	}

	@Override
	public void initializeFocus() {
	}

	public void refrescarDatosPantalla(BricodepotVisorPantallaSecundaria visor, PaneDatosGeneralesController paneDatosGenerales) {
		log.debug("refrescarDatosPantalla()");
		this.visor = visor;
		
		ocultarCampos(true);

		if (StringUtils.isNotBlank(paneDatosGenerales.getTfNombre().getText())) {
			hbNombre.setVisible(true);
			tfNombre.setText(paneDatosGenerales.getTfNombre().getText());
		}
		else {
			ocultarHboxAltura0(hbNombre);
		}

		if (StringUtils.isNotBlank(paneDatosGenerales.getTfApellidos().getText())) {
			tfApellidos.setText(paneDatosGenerales.getTfApellidos().getText());
			hbApellidos.setVisible(true);
		}
		else {
			ocultarHboxAltura0(hbApellidos);
		}

		if (StringUtils.isNotBlank(paneDatosGenerales.getTfDocumento().getText())) {
			hbDocumento.setVisible(true);
			tfDocumento.setText(paneDatosGenerales.getTfDocumento().getText());
		}
		else {
			ocultarHboxAltura0(hbDocumento);
		}

		if (StringUtils.isNotBlank(paneDatosGenerales.getTfDesPais().getText())) {
			hbPais.setVisible(true);
			tfPais.setText(paneDatosGenerales.getTfDesPais().getText());
		}
		else {
			ocultarHboxAltura0(hbPais);
		}

		if (StringUtils.isNotBlank(paneDatosGenerales.getTfEmail().getText())) {
			hbEmail.setVisible(true);
			tfEmail.setText(paneDatosGenerales.getTfEmail().getText());
		}
		else {
			ocultarHboxAltura0(hbEmail);
		}

		if (StringUtils.isNotBlank(paneDatosGenerales.getTfMovil().getText())) {
			hbMovil.setVisible(true);
			tfMovil.setText(paneDatosGenerales.getTfMovil().getText());
		}
		else {
			ocultarHboxAltura0(hbMovil);
		}

		if (StringUtils.isNotBlank(paneDatosGenerales.getTfDesColectivo().getText())) {
			hbColectivo.setVisible(true);
			tfColectivo.setText(paneDatosGenerales.getTfDesColectivo().getText());
		}
		else {
			ocultarHboxAltura0(hbColectivo);
		}

		if (StringUtils.isNotBlank(paneDatosGenerales.getTfCodPostal().getText())) {
			hbCP.setVisible(true);
			tfCP.setText(paneDatosGenerales.getTfCodPostal().getText());
		}
		else {
			ocultarHboxAltura0(hbCP);
		}

		if (StringUtils.isNotBlank(paneDatosGenerales.getTfProvincia().getText())) {
			hbProvincia.setVisible(true);
			tfProvincia.setText(paneDatosGenerales.getTfProvincia().getText());
		}
		else {
			ocultarHboxAltura0(hbProvincia);
		}

		if (StringUtils.isNotBlank(paneDatosGenerales.getTfLocalidad().getText())) {
			hbLocalidad.setVisible(true);
			tfLocalidad.setText(paneDatosGenerales.getTfLocalidad().getText());
		}
		else {
			ocultarHboxAltura0(hbLocalidad);
		}

		if (StringUtils.isNotBlank(paneDatosGenerales.getTfPoblacion().getText())) {
			hbPoblacion.setVisible(true);
			tfPoblacion.setText(paneDatosGenerales.getTfPoblacion().getText());
		}
		else {
			ocultarHboxAltura0(hbPoblacion);
		}

		if (StringUtils.isNotBlank(paneDatosGenerales.getTfDomicilio().getText())) {
			hbDomicilio.setVisible(true);
			tfDomicilio.setText(paneDatosGenerales.getTfDomicilio().getText());
		}
		else {
			ocultarHboxAltura0(hbDomicilio);
		}

		if (paneDatosGenerales.getCbEstadoCivil().getValue() != null && StringUtils.isNotBlank(paneDatosGenerales.getCbEstadoCivil().getValue().getDesEstadoCivil())) {
			hbEstadoCivil.setVisible(true);
			tfEstadoCivil.setText(paneDatosGenerales.getCbEstadoCivil().getValue().getDesEstadoCivil());
		}
		else {
			ocultarHboxAltura0(hbEstadoCivil);
		}

		if (paneDatosGenerales.getCbSexo().getValue() != null && StringUtils.isNotBlank(paneDatosGenerales.getCbSexo().getValue().getValor())) {
			hbSexo.setVisible(true);
			tfSexo.setText(paneDatosGenerales.getCbSexo().getValue().getValor());
		}
		else {
			ocultarHboxAltura0(hbSexo);
		}

		if (StringUtils.isNotBlank(paneDatosGenerales.getDpFechaNacimiento().getValue())) {
			hbFechaNacimiento.setVisible(true);
			tfFechaNacimiento.setText(paneDatosGenerales.getDpFechaNacimiento().getValue());
		}
		else {
			ocultarHboxAltura0(hbFechaNacimiento);
		}

		if (StringUtils.isNotBlank(paneDatosGenerales.getTfDesTienda().getText())) {
			hbTiendaFavorita.setVisible(true);
			tfTiendaFavorita.setText(paneDatosGenerales.getTfDesTienda().getText());
		}
		else {
			ocultarHboxAltura0(hbTiendaFavorita);
		}
	}

	private void ocultarCampos(Boolean visible) {
		hbNombre.setVisible(visible);
		hbApellidos.setVisible(visible);
		hbDocumento.setVisible(visible);
		hbPais.setVisible(visible);
		hbEmail.setVisible(visible);
		hbMovil.setVisible(visible);
		hbColectivo.setVisible(visible);
		hbCP.setVisible(visible);
		hbProvincia.setVisible(visible);
		hbLocalidad.setVisible(visible);
		hbPoblacion.setVisible(visible);
		hbDomicilio.setVisible(visible);
		hbEstadoCivil.setVisible(visible);
		hbSexo.setVisible(visible);
		hbFechaNacimiento.setVisible(visible);
		hbTiendaFavorita.setVisible(visible);
	}

	private void ocultarHboxAltura0(HBox hb) {
		hb.setVisible(false);
		hb.managedProperty().bind(hb.visibleProperty());
	}

	@FXML
	public void accionValidar() {
		log.debug("accionValidar()");
		visor.modoLeyProteccionDatosFidelizado();
	}

	@FXML
	public void accionRechazar() {
		log.debug("accionRechazar()");
		visor.modoEspera();
		VentanaEspera.cerrar();
	}
	
}
