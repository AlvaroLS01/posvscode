package com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.cliente.info;

import java.net.URL;
import java.util.ResourceBundle;

import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.BricodepotVisorPantallaSecundaria;
import com.comerzzia.bricodepot.pos.gui.componentes.VentanaEspera;
import com.comerzzia.bricodepot.pos.gui.mantenimientos.clientes.BricodepotMantenimientoClienteController;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.controllers.Controller;
import com.comerzzia.pos.services.core.sesion.Sesion;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

@Component
public class PantallaSecundariaInfoClienteController extends Controller {

	protected static final Logger log = Logger.getLogger(PantallaSecundariaInfoClienteController.class.getName());

	public static final String ESTADO = "ESTADO";
	public static final String VALIDADO = "VALIDADO";
	public static final String RECHAZADO = "RECHAZADO";

	protected BricodepotVisorPantallaSecundaria visor;

	@FXML
	protected ScrollPane scrollPane;

	@FXML
	protected HBox hbDocumento, hbDescripcion, hbNombre, hbDomicilio, hbCP, hbPoblacion, hbProvincia, hbLocalidad, hbTelefono, hbTelefono2, hbFax, hbEmail, hbPais, hbTratamientoImpuestos,
	        hbObservaciones;

	@FXML
	protected TextField tfDocumento, tfDescripcion, tfNombre, tfDomicilio, tfCP, tfPoblacion, tfProvincia, tfLocalidad, tfTelefono, tfTelefono2, tfFax, tfEmail, tfPais, tfTratamientoImpuestos;

	@FXML
	protected TextArea taObservaciones;

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

	public void refrescarDatosPantalla(BricodepotVisorPantallaSecundaria visor, BricodepotMantenimientoClienteController mantenimientoCliente) {
		log.debug("refrescarDatosPantalla()");
		this.visor = visor;

		ocultarCampos(false);

		if (StringUtils.isNotBlank(mantenimientoCliente.getTfNumDocIdent().getText())) {
			hbDocumento.setVisible(true);
			tfDocumento.setText(mantenimientoCliente.getTfNumDocIdent().getText());
		}

		if (StringUtils.isNotBlank(mantenimientoCliente.getTfDescripcion().getText())) {
			hbDescripcion.setVisible(true);
			tfDescripcion.setText(mantenimientoCliente.getTfDescripcion().getText());
		}

		if (StringUtils.isNotBlank(mantenimientoCliente.getTfRazonSocial().getText())) {
			hbNombre.setVisible(true);
			tfNombre.setText(mantenimientoCliente.getTfRazonSocial().getText());
		}

		if (StringUtils.isNotBlank(mantenimientoCliente.getTfDomicilio().getText())) {
			hbDomicilio.setVisible(true);
			tfDomicilio.setText(mantenimientoCliente.getTfDomicilio().getText());
		}

		if (StringUtils.isNotBlank(mantenimientoCliente.getTfCP().getText())) {
			hbCP.setVisible(true);
			tfCP.setText(mantenimientoCliente.getTfCP().getText());
		}

		if (StringUtils.isNotBlank(mantenimientoCliente.getTfPoblacion().getText())) {
			hbPoblacion.setVisible(true);
			tfPoblacion.setText(mantenimientoCliente.getTfPoblacion().getText());
		}

		if (StringUtils.isNotBlank(mantenimientoCliente.getTfProvincia().getText())) {
			hbProvincia.setVisible(true);
			tfProvincia.setText(mantenimientoCliente.getTfProvincia().getText());
		}

		if (StringUtils.isNotBlank(mantenimientoCliente.getTfLocalidad().getText())) {
			hbLocalidad.setVisible(true);
			tfLocalidad.setText(mantenimientoCliente.getTfLocalidad().getText());
		}

		if (StringUtils.isNotBlank(mantenimientoCliente.getTfTelefono().getText())) {
			hbTelefono.setVisible(true);
			tfTelefono.setText(mantenimientoCliente.getTfTelefono().getText());
		}

		if (StringUtils.isNotBlank(mantenimientoCliente.getTfTelefono2().getText())) {
			hbTelefono2.setVisible(true);
			tfTelefono2.setText(mantenimientoCliente.getTfTelefono2().getText());
		}

		if (StringUtils.isNotBlank(mantenimientoCliente.getTfFax().getText())) {
			hbFax.setVisible(true);
			tfFax.setText(mantenimientoCliente.getTfFax().getText());
		}

		if (StringUtils.isNotBlank(mantenimientoCliente.getTfEmail().getText())) {
			hbEmail.setVisible(true);
			tfEmail.setText(mantenimientoCliente.getTfEmail().getText());
		}

		if (StringUtils.isNotBlank(mantenimientoCliente.getTfDesPais().getText())) {
			hbPais.setVisible(true);
			tfPais.setText(mantenimientoCliente.getTfDesPais().getText());
		}

		if (mantenimientoCliente.getCbTratamientoImpuestos().getValue() != null && StringUtils.isNotBlank(mantenimientoCliente.getCbTratamientoImpuestos().getValue().getDestratimp())) {
			hbTratamientoImpuestos.setVisible(true);
			tfTratamientoImpuestos.setText(mantenimientoCliente.getCbTratamientoImpuestos().getValue().getDestratimp());
		}

		if (StringUtils.isNotBlank(mantenimientoCliente.getTaObservaciones().getText())) {
			hbObservaciones.setVisible(true);
			taObservaciones.setText(mantenimientoCliente.getTaObservaciones().getText());
		}
	}

	private void ocultarCampos(Boolean visible) {
		hbDocumento.setVisible(visible);
		hbDocumento.managedProperty().bind(hbDocumento.visibleProperty());
		hbDescripcion.setVisible(visible);
		hbDescripcion.managedProperty().bind(hbDescripcion.visibleProperty());
		hbDomicilio.setVisible(visible);
		hbDomicilio.managedProperty().bind(hbDomicilio.visibleProperty());
		hbCP.setVisible(visible);
		hbCP.managedProperty().bind(hbCP.visibleProperty());
		hbPoblacion.setVisible(visible);
		hbPoblacion.managedProperty().bind(hbPoblacion.visibleProperty());
		hbProvincia.setVisible(visible);
		hbProvincia.managedProperty().bind(hbProvincia.visibleProperty());
		hbLocalidad.setVisible(visible);
		hbLocalidad.managedProperty().bind(hbLocalidad.visibleProperty());
		hbTelefono.setVisible(visible);
		hbTelefono.managedProperty().bind(hbTelefono.visibleProperty());
		hbTelefono2.setVisible(visible);
		hbTelefono2.managedProperty().bind(hbTelefono2.visibleProperty());
		hbFax.setVisible(visible);
		hbFax.managedProperty().bind(hbFax.visibleProperty());
		hbEmail.setVisible(visible);
		hbEmail.managedProperty().bind(hbEmail.visibleProperty());
		hbPais.setVisible(visible);
		hbPais.managedProperty().bind(hbPais.visibleProperty());
		hbTratamientoImpuestos.setVisible(visible);
		hbTratamientoImpuestos.managedProperty().bind(hbTratamientoImpuestos.visibleProperty());
		hbObservaciones.setVisible(visible);
		hbObservaciones.managedProperty().bind(hbObservaciones.visibleProperty());
	}

	@FXML
	public void accionValidar() {
		log.debug("accionValidar()");
		
		// BRICO-337 Si es tienda portuguesa no pide firma ni muestra documento
		if (isTiendaPortugal()) {
			visor.confirmacionValidacionCliente();
			return;
		}
		// fin BRICO-337

		visor.modoLeyProteccionDatosCliente();
	}

	@FXML
	public void accionRechazar() {
		log.debug("accionRechazar()");
		visor.modoEspera();
		VentanaEspera.cerrar();
	}
	
	private boolean isTiendaPortugal() { // BRICO-325
		log.debug("isTiendaPortugal() - Comprobando el idioma del cliente de la tienda");

		String codpais = sesion.getAplicacion().getTienda().getCliente().getCodpais();
		if (StringUtils.isNotBlank(codpais) && codpais.equals("PT")) {
			log.debug("isClientePortugal() - Se trata de una tienda de PORTUGAL");
			return true;
		}

		log.debug("isTiendaPortugal() - No es tienda de PORTUGAL");
		return false;
	}
}
