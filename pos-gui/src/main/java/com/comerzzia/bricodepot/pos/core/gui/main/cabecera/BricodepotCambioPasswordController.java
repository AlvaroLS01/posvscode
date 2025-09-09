package com.comerzzia.bricodepot.pos.core.gui.main.cabecera;

import java.awt.Toolkit;

import com.comerzzia.api.rest.client.exceptions.RestException;
import com.comerzzia.api.rest.client.exceptions.RestHttpException;
import com.comerzzia.api.rest.client.exceptions.ValidationDataRestException;
import com.comerzzia.bricodepot.pos.services.core.sesion.exception.MinHorasException;
import com.comerzzia.bricodepot.pos.services.core.sesion.exception.VarHorasMinException;
import com.comerzzia.bricodepot.pos.services.passwords.validacion.ValidacionPasswordService;
import com.comerzzia.core.util.criptografia.CriptoUtil;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.main.cabecera.cambioPassword.CambioPasswordController;
import com.comerzzia.pos.persistence.core.usuarios.UsuarioBean;
import com.comerzzia.pos.util.i18n.I18N;
import com.comerzzia.validacion.passwords.constants.ErroresValidacionPassword;
import com.comerzzia.validacion.passwords.exceptions.LowerCaseException;
import com.comerzzia.validacion.passwords.exceptions.MinNumCharException;
import com.comerzzia.validacion.passwords.exceptions.NewPasswordConfirmEmptyException;
import com.comerzzia.validacion.passwords.exceptions.NewPasswordEmptyException;
import com.comerzzia.validacion.passwords.exceptions.NotMatchingException;
import com.comerzzia.validacion.passwords.exceptions.OldPasswordEmptyException;
import com.comerzzia.validacion.passwords.exceptions.SamePasswordException;
import com.comerzzia.validacion.passwords.exceptions.SpecialCharException;
import com.comerzzia.validacion.passwords.exceptions.UpperCaseException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;

@Component
@Primary
public class BricodepotCambioPasswordController extends CambioPasswordController {

	public static final String PARAM_PANTALLA_ORIGEN = "pantallaOrigen"; // Pantalla origen, será null en estándar
	public static final String PANTALLA_ORIGEN_LOGIN = "LOGIN";

	public static final String USUARIO_CAMBIO_PASSWORD = "usuarioCambioPassword";
	public static final String HA_CAMBIADO_PASSWORD = "haCambiadoPassword";

	private static Logger log = Logger.getLogger(BricodepotCambioPasswordController.class);

	private String usuario, textoLbUsuario;

	@Autowired
	protected ValidacionPasswordService validacionPasswordService;

	@Override
	public void initializeForm() throws InitializeGuiException {

		if (datos == null || datos.get(PARAM_PANTALLA_ORIGEN) == null || !(datos.get(PARAM_PANTALLA_ORIGEN) instanceof String) || StringUtils.isEmpty((String) datos.get(PARAM_PANTALLA_ORIGEN))) {
			// Si no hay param, viene de estándar, por lo que se conserva comportamiento anterior
			usuario = sesion.getSesionUsuario().getUsuario().getUsuario();
			textoLbUsuario = sesion.getSesionUsuario().getUsuario().getDesusuario();
		}
		else if (PANTALLA_ORIGEN_LOGIN.equals(datos.get(PARAM_PANTALLA_ORIGEN))) {
			textoLbUsuario = I18N.getTexto("Su contraseña ha expirado, restaurar contraseña");
			usuario = (String) datos.get(USUARIO_CAMBIO_PASSWORD);
		}

		lbUsuario.setText(textoLbUsuario);
		pfAntiguaPw.setText("");
		pfNuevaPw.setText("");
		pfConfirmPw.setText("");

	}

	@FXML
	@Override
	public void accionAceptar() {
		String passAntigua = pfAntiguaPw.getText();
		String passNueva = pfNuevaPw.getText();
		String passConfirmar = pfConfirmPw.getText();
		
//		if(!passNueva.equals(passConfirmar)) {
//			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("Las contraseña nueva y la contraseña confirmación deben coincidir"), getStage());
//			return;
//		}

		boolean pwCambiada;
		try {

			boolean passwordValido = validaCambioPassword(passAntigua, passNueva, passConfirmar);
			if (!passwordValido) {
				return;
			}

			pwCambiada = sesion.getSesionUsuario().cambiarPassword(CriptoUtil.cifrar(CriptoUtil.ALGORITMO_MD5, passNueva.getBytes("UTF-8")),
			        CriptoUtil.cifrar(CriptoUtil.ALGORITMO_MD5, passAntigua.getBytes("UTF-8")),
			        /* BRICO-128 */
			        usuario);
			/* fin BRICO-128 */

			if (pwCambiada) {
				/* BRICO-128 */
				UsuarioBean usuarioBean = usuariosService.consultarUsuario(usuario);
				usuariosService.cambiarPassword(usuarioBean.getIdUsuario(), passAntigua, passNueva);

				getDatos().put(HA_CAMBIADO_PASSWORD, "S");
				/* fin BRICO-128 */

				VentanaDialogoComponent.crearVentanaInfo(I18N.getTexto("Clave cambiada correctamente"), getStage());
				getStage().close();
			}
			else {
				VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("No se pudo cambiar el password"), getStage());
			}

		}
		// BRICO-326
		catch (MinHorasException | VarHorasMinException e) {
			VentanaDialogoComponent.crearVentanaError(getStage(), e.getMessage(), e);
		}
		// BRICO-326
		catch (ValidationDataRestException ex) {
			if (ex.getCodError() == 5) {
				VentanaDialogoComponent.crearVentanaError(I18N.getTexto("La clave actual no es correcta"), getStage());
			}
		}
		catch (RestException | RestHttpException e) {
			log.error("accionAceptar() - " + e.getClass().getName() + " - " + e.getLocalizedMessage(), e);
			VentanaDialogoComponent.crearVentanaError(getStage(), I18N.getTexto("No se pudo cambiar el password: No hay conexión con central."), e);
		}
		catch (Exception e) {
			log.error("accionAceptar() - " + e.getClass().getName() + " - " + e.getLocalizedMessage(), e);
			VentanaDialogoComponent.crearVentanaError(getStage(), I18N.getTexto("No se pudo cambiar el password"), e);
		}
	}

	protected boolean validaCambioPassword(String passAntigua, String passNueva, String passConfirmar) { // BRICO-326
		log.debug("validaCambioClave() - Validando cambio de clave...");

		try {
			validacionPasswordService.validaCambioPassword(passAntigua, passNueva, passConfirmar);
			log.debug("validaCambioClave() - Cambio clave validado con éxito");
			return true;
		}
		catch (Exception e) {
			Toolkit.getDefaultToolkit().beep();
			trataErrorCambioClave(e);
			return false;
		}
	}

	protected void trataErrorCambioClave(Exception e) { // BRICO-326
		log.debug("trataErrorCambioClave() - Tratando error en cambio de clave");

		if (e instanceof SamePasswordException) {
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto(ErroresValidacionPassword.MSG_ERROR_MISMA_CONTRASENA_ANTERIOR), getStage());
			pfNuevaPw.requestFocus();
		}
		else if(e instanceof NotMatchingException) {
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto(ErroresValidacionPassword.MSG_ERROR_NO_COINCIDEN), getStage());
			pfConfirmPw.requestFocus();
		}
		else if (e instanceof OldPasswordEmptyException) {
			pfAntiguaPw.requestFocus();
		}
		else if (e instanceof NewPasswordEmptyException) {
			pfNuevaPw.requestFocus();
		}
		else if (e instanceof NewPasswordConfirmEmptyException) {
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto(ErroresValidacionPassword.MSG_ERROR_NO_COINCIDEN), getStage());
			pfConfirmPw.requestFocus();
		}
		else if (e instanceof MinNumCharException) {
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto(ErroresValidacionPassword.MSG_ERROR_MIN_CHAR), getStage());
			pfNuevaPw.requestFocus();
		}
		else if (e instanceof LowerCaseException) {
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto(ErroresValidacionPassword.MSG_ERROR_MINUSCULA), getStage());
			pfNuevaPw.requestFocus();
		}
		else if (e instanceof UpperCaseException) {
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto(ErroresValidacionPassword.MSG_ERROR_MAYUSCULA), getStage());
			pfNuevaPw.requestFocus();
		}
		else if (e instanceof SpecialCharException) {
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto(ErroresValidacionPassword.MSG_ERROR_CHAR_ESPECIAL), getStage());
			pfNuevaPw.requestFocus();
		}

	}

}
