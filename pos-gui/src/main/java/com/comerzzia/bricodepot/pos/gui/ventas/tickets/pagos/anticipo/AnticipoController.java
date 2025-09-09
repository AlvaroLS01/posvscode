package com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.anticipo;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

import javax.ws.rs.NotFoundException;

import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.controllers.WindowController;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.variables.VariablesServices;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import rest.bean.anticipo.Anticipo;
import rest.client.anticipo.RestAnticiposClient;

@Component
public class AnticipoController extends WindowController {

	protected static final Logger log = Logger.getLogger(AnticipoController.class);

	public static final String PARAMETRO_NUM_ANTICIPO = "NUM_ANTICIPO";
	public static final String PARAMETRO_IMPORTE = "PARAMETRO_IMPORTE";
	public static final String PARAMETRO_CANCELAR = "PARAMETRO_CANCELAR";

	@FXML
	protected TextField tfAnticipo, tfImporte;

	protected Boolean anticipoCorrecto;

	@Autowired
	private VariablesServices variablesServices;

	@Autowired
	private Sesion sesion;

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
		tfImporte.setText("");

	}

	@FXML
	public void accionAceptar() {
		try {
			if (StringUtils.isNotBlank(tfAnticipo.getText()) && StringUtils.isNotBlank(tfImporte.getText())) {
				String numAnticipo = tfAnticipo.getText();
				String importe = tfImporte.getText().replace(",", ".");
				if (validador(numAnticipo, importe)) {
					String apiKey = variablesServices.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
					String uidActividad = sesion.getAplicacion().getUidActividad();
					Anticipo anticipo = null;
					try {
						anticipo = RestAnticiposClient.getAnticipo(apiKey, uidActividad, numAnticipo);
					}
					catch (NotFoundException e) {
					}
					if (anticipo == null) {
						getDatos().put(PARAMETRO_NUM_ANTICIPO, numAnticipo);
						getDatos().put(PARAMETRO_IMPORTE, importe);
						getStage().close();
					}
					else {
						VentanaDialogoComponent.crearVentanaAviso("Ya existe un anticipo con ese código, no se puede volver a registrar.", getStage());
					}
				}
				else {
					VentanaDialogoComponent.crearVentanaAviso("No se ha podido registrar el anticipo, rellene bien los campos.", getStage());
				}
			}
			else {
				VentanaDialogoComponent.crearVentanaAviso("Rellene ambos campos para poder continuar.", getStage());
			}
		}
		catch (Exception e) {
			VentanaDialogoComponent.crearVentanaError("Se ha producido un error registrando el anticipo.", getStage());
			getDatos().put(PARAMETRO_CANCELAR, true);
			getStage().close();
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

	public boolean validador(String numAnticipo, String importeAnticipo) {
		boolean res = false;

		try {
			new BigDecimal(importeAnticipo);
			res = StringUtils.isAlphanumeric(numAnticipo);
		}
		catch (Exception e) {
			res = false;
		}
		return res;

	}

}
