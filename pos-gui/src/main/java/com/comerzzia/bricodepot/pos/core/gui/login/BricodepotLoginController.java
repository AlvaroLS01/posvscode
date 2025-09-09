package com.comerzzia.bricodepot.pos.core.gui.login;

import java.net.NoRouteToHostException;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.api.rest.client.exceptions.RestException;
import com.comerzzia.api.rest.client.exceptions.RestHttpException;
import com.comerzzia.bricodepot.pos.core.gui.main.cabecera.BricodepotCambioPasswordController;
import com.comerzzia.bricodepot.pos.services.cajas.BricodepotCajasService;
import com.comerzzia.bricodepot.pos.services.core.sesion.BricodepotSesionAplicacion;
import com.comerzzia.bricodepot.pos.services.core.sesion.BricodepotSesionCaja;
import com.comerzzia.bricodepot.pos.services.core.sesion.exception.PasswordExpiradoLoginException;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.login.LoginController;
import com.comerzzia.pos.core.gui.main.MainViewController;
import com.comerzzia.pos.core.gui.main.cabecera.cambioPassword.CambioPasswordView;
import com.comerzzia.pos.core.gui.permisos.exception.SinPermisosException;
import com.comerzzia.pos.persistence.core.usuarios.UsuarioBean;
import com.comerzzia.pos.services.cajas.Caja;
import com.comerzzia.pos.services.cajas.CajaEstadoException;
import com.comerzzia.pos.services.cajas.CajasServiceException;
import com.comerzzia.pos.services.core.sesion.SesionInitException;
import com.comerzzia.pos.services.core.usuarios.UsuarioInvalidLoginException;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.services.ticket.copiaSeguridad.CopiaSeguridadTicketService;
import com.comerzzia.pos.util.config.AppConfig;
import com.comerzzia.pos.util.i18n.I18N;

import javafx.event.ActionEvent;

@Component
@Primary
public class BricodepotLoginController extends LoginController {

	private static final Logger log = Logger.getLogger(BricodepotLoginController.class.getName());

	@Autowired
	private CopiaSeguridadTicketService copiaSeguridadTicketService;

	@Autowired
	protected VariablesServices variablesServices;

	@Autowired
	private BricodepotCajasService cajasService;

	private boolean enProceso;

	private void actionBtAceptarEstandar(ActionEvent event) throws SesionInitException, UsuarioInvalidLoginException {
		log.debug("actionBtAceptar()");
		if (accionFrLoginSubmit()) {
			if (!esMismoUsuarioSesion()) {
				VentanaDialogoComponent.crearVentanaError(I18N.getTexto("El usuario es distinto al usuario de la sesión."), getStage());
				return;
			}
			// Comprobamos que el usuario tiene acceso a la pantalla activa
			if (!hasPermissionsCurrentScreen(tfUsuario.getText())) {
				VentanaDialogoComponent.crearVentanaError(I18N.getTexto("El usuario no tiene permisos para ejecutar la pantalla activa."), getStage());
				return;
			}
			sesion.initUsuarioSesion(tfUsuario.getText(), tfPassword.getText());
			sesion.getSesionUsuario().clearPermisos();
			getApplication().comprobarPermisosUI();

			((MainViewController) getApplication().getMainView().getController()).actualizarUsuario();
			if (isModoSeleccionDeCajero()) {
				getDatos().put(PARAMETRO_SALIDA_CAMBIO_USUARIO, "S");
				getStage().close();
			}
			else if (isModoBloqueo()) {
				getStage().close();
			}
			else {
				getApplication().showFullScreenView(mainView);
			}

		}
	}

	@Override
	public void actionBtAceptar(ActionEvent event) {
		log.debug("actionBtAceptar()");
		if (enProceso) {
			log.warn("actionBtAceptar() - Intentando login mientras está en proceso.");
			return;
		}

		try {
			enProceso = true;
			if (!((BricodepotSesionAplicacion) sesion.getAplicacion()).isCajaMasterActivada()) {
				actionBtAceptarEstandar(event);
				return;
			}
			log.debug("actionBtAceptar()");
			if (accionFrLoginSubmit()) {
				if (!esMismoUsuarioSesion()) {
					VentanaDialogoComponent.crearVentanaError(I18N.getTexto("El usuario es distinto al usuario de la sesión."), getStage());
					return;
				}
				// Comprobamos que el usuario tiene acceso a la pantalla activa
				if (!hasPermissionsCurrentScreen(tfUsuario.getText())) {
					VentanaDialogoComponent.crearVentanaError(I18N.getTexto("El usuario no tiene permisos para ejecutar la pantalla activa."), getStage());
					return;
				}
				sesion.initUsuarioSesion(tfUsuario.getText(), tfPassword.getText());
				iniciarSesion();
			}
		}
		catch (SesionInitException ex) {
			VentanaDialogoComponent.crearVentanaError(getStage(), ex.getMessageDefault(), ex);
			accionLimpiarFormulario();
			initializeFocus();
		}
		catch (UsuarioInvalidLoginException ex) {
			lbError.setText(I18N.getTexto(ex.getMessageI18N()));
			initializeFocus();
			if (ex instanceof PasswordExpiradoLoginException) { // BRICO-128
				muestraVentanaCambioClave();
				if (datos.containsKey(BricodepotCambioPasswordController.HA_CAMBIADO_PASSWORD)) {
					try {
						iniciarSesion();
					}
					catch (CajasServiceException | CajaEstadoException e) {
						log.error("actionBtAceptar() - Error no controlado -" + ex.getMessage(), ex);
						VentanaDialogoComponent.crearVentanaError(getStage(), ex);
					}
				}
			}
		}
		catch (Exception ex) {
			log.debug("actionBtAceptar() - Error no controlado -" + ex.getMessage(), ex);
			VentanaDialogoComponent.crearVentanaError(getStage(), ex);
		}
		finally {
			enProceso = false;
		}

	}

	protected void realizarAperturaCaja() throws SinPermisosException, CajasServiceException, CajaEstadoException {
		log.debug("realizarAperturaCaja()");
		UsuarioBean usuario = sesion.getSesionUsuario().getUsuario();
		Caja cajaAbierta = consultarCajaAbierta();

		boolean reintentar = true;
		boolean segundaConfirmacion = true;

		String textoAdic = "";
		if (cajaAbierta == null) {
			while (segundaConfirmacion) {
				while (reintentar) {
					try {
						cajaAbierta = cajasService.pedirYGuardarCajaAbierta(usuario);
						reintentar = false;
						segundaConfirmacion = false;
					}
					catch (RestException e) {
						log.error("pedirCajaEnMaster() - Ha habido un error al transferir la caja a la caja máster: " + e.getCause().getMessage(), e);

						String mensaje = I18N.getTexto("No se ha podido establecer la comunicación con la caja máster. ¿Desea intentarlo de nuevo?.");

						reintentar = VentanaDialogoComponent.crearVentanaConfirmacion(mensaje, getStage());
					}
					catch (RestHttpException e) {
						log.error("transferirCajaAMaster() - Ha habido un error al transferir la caja a la caja máster: " + e.getCause().getMessage(), e);

						String mensaje = I18N.getTexto("Ha habido un problema al llamar a la caja máster para iniciar sesión. Contacte con un administrador.");

						Short codigoCajaAbierta = 7001;
						if (codigoCajaAbierta.equals(e.getCodError())) {
							textoAdic = e.getMessage();
							reintentar = false;
						}
						else {
							reintentar = VentanaDialogoComponent.crearVentanaConfirmacion(mensaje, getStage());
						}
					}
					catch (Exception e) {
						String mensaje = e.getCause().getMessage();
						if (e.getCause().getCause() instanceof IllegalStateException || e.getCause() instanceof NoRouteToHostException || e.getCause().getCause() instanceof NoRouteToHostException) {
							mensaje = I18N.getTexto("No se ha podido conectar al servidor de la caja máster.");
						}
						mensaje = mensaje + System.lineSeparator() + System.lineSeparator() + I18N.getTexto("¿Desea intentarlo de nuevo?");
						reintentar = VentanaDialogoComponent.crearVentanaConfirmacion(mensaje, getStage());
					}

					if (cajaAbierta != null) {
						if (StringUtils.isNotBlank(cajaAbierta.getUidDiarioCaja())) {
							((BricodepotSesionCaja) sesion.getSesionCaja()).setCajaAbierta(cajaAbierta);
							VentanaDialogoComponent.crearVentanaInfo(I18N.getTexto("Se ha transferido su caja desde la caja máster."), getStage());
							reintentar = false;
							segundaConfirmacion = false;
						}
					}
				}
				if (cajaAbierta == null && segundaConfirmacion) {
					if (StringUtils.isNotBlank(textoAdic)) {
						VentanaDialogoComponent.crearVentanaAviso(textoAdic, getStage());
						throw new IllegalAccessError();
					}
					else {
						String textoSegundaConfirmacion = textoAdic + I18N.getTexto("Si continúa, se abrirá una caja no sincronizada con la caja máster, por lo que deberá") + System.lineSeparator()
						        + I18N.getTexto("realizar el cierre de caja en este terminal para poder cerrar la sesión en el futuro.") + System.lineSeparator() + System.lineSeparator()
						        + I18N.getTexto("¿Seguro que desea continuar?");
						if (VentanaDialogoComponent.crearVentanaConfirmacion(textoSegundaConfirmacion, getStage())) {
							reintentar = false;
							segundaConfirmacion = false;
						}
						else {
							throw new IllegalAccessError();
						}
					}
				}
			}
		}
		else {
			((BricodepotSesionCaja) sesion.getSesionCaja()).setCajaAbierta(cajaAbierta);
		}
	}
	
	protected Caja consultarCajaAbierta() {
		try {
			return cajasService.consultarCajaAbierta();
		}
		catch (Exception e) {
			return null;
		}
	}
	private void iniciarSesion() throws CajasServiceException, CajaEstadoException {
		// Si la caja no es maestra, se hace la petición a la misma por si tiene una caja abierta
//		if (((BricodepotSesionAplicacion) sesion.getAplicacion()).isCajaMasterActivada()) {
//			if (sesion.getAplicacion().getTiendaCaja().getIdTipoCaja() != 0L) {
				try {
					realizarAperturaCaja();
				}
				catch (IllegalAccessError e) {
					sesion.closeUsuarioSesion();
					return;
				}
				catch (SinPermisosException e) {
					VentanaDialogoComponent.crearVentanaError(I18N.getTexto("La caja abierta actual no coincide con el cajero logueado."), getStage());
				return;
				}
//			}
//		}
		sesion.getSesionUsuario().clearPermisos();
    	getApplication().comprobarPermisosUI();
    	
        ((MainViewController)getApplication().getMainView().getController()).actualizarUsuario();
        if (isModoSeleccionDeCajero()){
            getDatos().put(PARAMETRO_SALIDA_CAMBIO_USUARIO, "S");
            getStage().close();
        }
        else if(isModoBloqueo()){
        	getStage().close();
        }
        else{
            getApplication().showFullScreenView(mainView);
            copiaSeguridadTicketService.clearBackupReturns();
            getApplication().activarTimer();
            actionFilesManager.scheduleCancel();
        }
		
		
	}
	private boolean esMismoUsuarioSesion() { // BRICO-291
		log.debug("esMismoUsuarioSesion() - Comprobando si usuario que inicia es el mismo que abrió caja...");
		
		Caja cajaAbierta = sesion.getSesionCaja().getCajaAbierta();
    	if(AppConfig.showCashOpeningUser && cajaAbierta != null && StringUtils.isNotBlank(cajaAbierta.getUsuario()) && 
    			!cajaAbierta.getUsuario().equals(tfUsuario.getText().toUpperCase())) {
    		return false;
    	}
		
		return true;
	}
	
	public void muestraVentanaCambioClave() { // BRICO-128
		log.debug("muestraVentanaCambioClave() - Contraseña expirada, mostrando modal de cambio de contraseña");
		
		HashMap<String, Object> datos = new HashMap<String, Object>();
		datos.put(BricodepotCambioPasswordController.PARAM_PANTALLA_ORIGEN, BricodepotCambioPasswordController.PANTALLA_ORIGEN_LOGIN);
		datos.put(BricodepotCambioPasswordController.USUARIO_CAMBIO_PASSWORD, tfUsuario.getText());
	    btAceptar.requestFocus();
		getApplication().getMainView().showModalCentered(CambioPasswordView.class, datos, this.getStage());
		tfPassword.requestFocus();
		if (datos.containsKey(BricodepotCambioPasswordController.HA_CAMBIADO_PASSWORD)) {
			tfPassword.clear();
			lbError.setText("");
		}
	}

}
