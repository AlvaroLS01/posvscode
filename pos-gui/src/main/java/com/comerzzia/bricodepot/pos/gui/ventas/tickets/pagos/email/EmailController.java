package com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.email;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.BricodepotVisorPantallaSecundaria;
import com.comerzzia.bricodepot.pos.gui.componentes.VentanaEspera;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.pagos.BricodepotPagosController;
import com.comerzzia.bricodepot.pos.services.cliente.ProcesoValidacionClienteListener;
import com.comerzzia.bricodepot.pos.util.format.BricoEmailValidator;
import com.comerzzia.pos.core.dispositivos.Dispositivos;
import com.comerzzia.pos.core.dispositivos.dispositivo.visor.IVisor;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.controllers.WindowController;
import com.comerzzia.pos.dispositivo.visor.VisorPantallaSecundaria;
import com.comerzzia.pos.util.i18n.I18N;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

@Component
public class EmailController extends WindowController {

	private static Logger log = Logger.getLogger(EmailController.class);

	public static final Pattern REGEX_MAGENTO_EMAIL = Pattern.compile("^((([a-z]|\\d|[!#\\$%&\\u0027\\*\\+\\-\\/=\\?\\^_`{\\|}~]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])+(\\.([a-z]|\\d|[!#\\$%&\\u0027\\*\\+\\-\\/=\\?\\^_`{\\|}~]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])+)*)|((\\u0022)((((\\x20|\\x09)*(\\x0d\\x0a))?(\\x20|\\x09)+)?(([\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x7f]|\\x21|[\\x23-\\x5b]|[\\x5d-\\x7e]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(\\\\([\\x01-\\x09\\x0b\\x0c\\x0d-\\x7f]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF]))))*(((\\x20|\\x09)*(\\x0d\\x0a))?(\\x20|\\x09)+)?(\\u0022)))@((([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])([a-z]|\\d|-|\\.|_|~|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])*([a-z]|\\d|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])))\\.)*(([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])|(([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])([a-z]|\\d|-|\\.|_|~|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])*([a-z]|[\\u00A0-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFEF])))\\.?$");
	public static final String CANCELAR = "Cancelar";

	public static final String EMAIL = "EMAIL";
	public static final String CORREO = "CORREO";
	public static final String AMBOS = "AMBOS";
	public static final String TIPO_ENVIO = "TIPO_ENVIO";
	public static final String PAPEL = "PAPEL";
	@FXML
	private TextField tfEmail;
	@FXML
	protected Label lbTextError;

	public TextField getTfEmail() {
		return tfEmail;
	}

	protected IVisor visor;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}

	@Override
	public void initializeComponents() throws InitializeGuiException {
		visor = Dispositivos.getInstance().getVisor();
	}

	@Override
	public void initializeFocus() {
		tfEmail.requestFocus();
	}

	@Override
	public void initializeForm() throws InitializeGuiException {
		lbTextError.setVisible(false);
		tfEmail.clear();
		
		if(getDatos().get(BricodepotPagosController.EMAIL_FIDELIZADO_CARGADO) != null) {
			String emailFidelizadoCargado = (String) getDatos().get(BricodepotPagosController.EMAIL_FIDELIZADO_CARGADO);
			
			tfEmail.setText(emailFidelizadoCargado.toLowerCase());
		}
	}

	@FXML
	private void accionCancelar(ActionEvent event) {
		getDatos().put(CANCELAR, true);
		super.accionCancelar();
	}

	@FXML
	private void accionVisor() {
		String email = tfEmail.getText().trim().toLowerCase();
		lbTextError.setVisible(false);

		if (StringUtils.isBlank(email)) {
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("Debe rellenar el campo del email."), getStage());
			return;
		}

		String errorKey = BricoEmailValidator.getValidationErrorKey(email);
		if (errorKey != null) {
			VentanaDialogoComponent.crearVentanaError(I18N.getTexto(errorKey), getStage());
			tfEmail.requestFocus();
			return;
		}

		if (!REGEX_MAGENTO_EMAIL.matcher(email).matches()) {
			lbTextError.setText(I18N.getTexto("El email no tiene un formato correcto"));
			lbTextError.setVisible(true);
			return;
		}

		if (visor.getEstado() == VisorPantallaSecundaria.APAGADO) {
			log.error("No se puede completar proceso, visor apagado.");
			VentanaDialogoComponent.crearVentanaError(this.getStage(), I18N.getTexto("Lo sentimos, el visor no se encuentra disponible"), new RuntimeException());
			return;
		}

		VentanaEspera.crearVentanaCargando(getStage());
		VentanaEspera.setMensaje(I18N.getTexto("Pendiente Validación del Cliente"));
		VentanaEspera.mostrar();

		visor.reset();
		((BricodepotVisorPantallaSecundaria) visor).modoEmailCliente(this);
		((BricodepotVisorPantallaSecundaria) visor).setListenerClienteOK(new ProcesoValidacionClienteListener(){

			@Override
			public void procesoValidacionOK() {
				log.debug("procesoValidacionOK() - Se ha terminado el proceso de confirmación del correo del cliente. Se procede a realizar la venta.");
				VentanaEspera.cerrar();
				accionAceptar();
			}
		});
	}
	
	public void accionAceptar() {
		getDatos().put(EMAIL, tfEmail.getText());
		getStage().close();
	}

	@FXML
	public void accionAceptarIntro(KeyEvent e) {
		if (e.getCode() == KeyCode.ENTER) {
			accionVisor();
		}
	}
	
}
