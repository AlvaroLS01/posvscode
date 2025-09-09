package com.comerzzia.bricodepot.pos.services.core.sesion;

import com.comerzzia.api.core.rest.client.usuarios.CambiarClaveUsuarioRequestRest;
import com.comerzzia.api.rest.client.exceptions.RestException;
import com.comerzzia.api.rest.client.exceptions.RestHttpException;
import com.comerzzia.api.rest.client.response.ResponsePostRest;
import com.comerzzia.bricodepot.pos.services.core.sesion.exception.MinHorasException;
import com.comerzzia.bricodepot.pos.services.core.sesion.exception.PasswordExpiradoLoginException;
import com.comerzzia.bricodepot.pos.services.core.sesion.exception.UsuarioBloqueadoException;
import com.comerzzia.bricodepot.pos.services.core.sesion.exception.UsuarioNoEncontradoException;
import com.comerzzia.bricodepot.pos.services.core.sesion.exception.VarHorasMinException;
import com.comerzzia.bricodepot.pos.services.core.sesion.exception.VariableNoConfiguradaException;
import com.comerzzia.core.util.criptografia.CriptoException;
import com.comerzzia.core.util.criptografia.CriptoUtil;
import com.comerzzia.pos.persistence.core.usuarios.UsuarioBean;
import com.comerzzia.pos.services.core.perfiles.PerfilException;
import com.comerzzia.pos.services.core.sesion.SesionInitException;
import com.comerzzia.pos.services.core.sesion.SesionUsuario;
import com.comerzzia.pos.services.core.usuarios.UsuarioInvalidLoginException;
import com.comerzzia.pos.services.core.usuarios.UsuarioNotFoundException;
import com.comerzzia.pos.services.core.usuarios.UsuariosService;
import com.comerzzia.pos.services.core.usuarios.UsuariosServiceException;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.util.i18n.I18N;
import com.comerzzia.validacion.passwords.constants.ErroresCambioPassword;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import rest.client.usuarios.ResponseUsuariosRest;
import rest.client.usuarios.RestUsuariosClient;
import rest.client.usuarios.UsuarioDTO;

@Component
@Primary
public class BricodepotSesionUsuario extends SesionUsuario {

	protected static final Logger log = Logger.getLogger(BricodepotSesionUsuario.class);

	@Autowired
	protected UsuariosService usuarioService;
	
	@Override
	public void init(String usuario, String password) throws SesionInitException, UsuarioInvalidLoginException {
		log.debug("init() - Iniciando sesión...");

		try {
			/* Si no existe el usuario en local, mandamos excepcion */
			usuarioService.consultarUsuario(usuario);
			
			iniciaSesionCentral(usuario, password); // BRICO-128
			return;
		}
		catch (UsuarioInvalidLoginException e) {
			// Excepción "controlada" para fallos de autenticación, se mostrará error en pantalla de login
			log.error("init() - Error iniciando sesión de usuario en central: " + e.getMessage(), e);
			throw e;
		}
		catch (RestException | RestHttpException e) {
			// Excepción no controlada, se pasa a login en tienda
			log.error("Error HTTP iniciando sesión en central: " + e.getMessage(), e);
		}
		catch (UsuarioNotFoundException | UsuariosServiceException e) {
			log.error("Error HTTP iniciando sesión en central: " + e.getMessage(), e);
			throw new UsuarioInvalidLoginException(I18N.getTexto("El usuario introducido no existe en la base de datos de la caja."));
		}
		catch (Exception e) {
			// Excepción no controlada, se pasa a login en tienda
			log.error("Error iniciando sesión en central: " + e.getMessage(), e);
		}

		log.debug("init() - Iniciando sesión en tienda ...");
		super.init(usuario, password);

	}

	public void iniciaSesionCentral(String usuario, String password) throws UsuarioInvalidLoginException, CriptoException, RestException, RestHttpException, PerfilException { // BRICO-128
		log.debug("iniciaSesionCentral() - Iniciando sesión en central para el usuario: " + usuario);

		String apiKey = variablesServices.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
		String uidActividad = sesion.getAplicacion().getUidActividad();
		String codEmpresa = sesion.getAplicacion().getEmpresa().getCodEmpresa();

		String passwordCifrado = CriptoUtil.cifrar(CriptoUtil.ALGORITMO_MD5, password.getBytes());

		ResponseUsuariosRest responseUsuarioRest = RestUsuariosClient.getLogin(apiKey, uidActividad, codEmpresa, usuario, passwordCifrado);

		if (responseUsuarioRest.getUsuarioDTO() != null) {
			UsuarioBean usuarioLoginCentral = creaUsuarioLoginCentral(responseUsuarioRest.getUsuarioDTO());
			this.usuario = usuarioLoginCentral;
			setIsSuperAdministrador();
			log.debug("iniciaSesionCentral() - Sesion iniciada desde central con éxito");
			return;
		}
		else {
			switch (responseUsuarioRest.getError()) {
				case ResponseUsuariosRest.USUARIO_NO_EXISTE:
					log.error("iniciaSesionCentral() - El usuario consultado no existe");
					throw new UsuarioNoEncontradoException();
				case ResponseUsuariosRest.CONTRASENA_INCORRECTA:
					log.error("iniciaSesionCentral() - Contraseña incorrecta");
					throw new UsuarioInvalidLoginException();
				case ResponseUsuariosRest.CONTRASENA_EXPIRADA:
					log.error("iniciaSesionCentral() - Contraseña expirada");
					throw new PasswordExpiradoLoginException();
				case ResponseUsuariosRest.VARIABLE_NO_CONFIGURADA:
					log.error("iniciaSesionCentral() - Variable no configurada");
					throw new VariableNoConfiguradaException();
				case ResponseUsuariosRest.USUARIO_BLOQUEADO:
					log.error("iniciaSesionCentral() - Usuario Bloqueado");
					throw new UsuarioBloqueadoException();
			}
		}
	}

	private UsuarioBean creaUsuarioLoginCentral(UsuarioDTO usuarioDTO) {
		log.debug("creaUsuarioLoginCentral() - Creando usuario a partir del usuario recibido desde central...");

		UsuarioBean usuarioBean = new UsuarioBean();
		usuarioBean.setUidInstancia(usuarioDTO.getUidInstancia());
		usuarioBean.setIdUsuario(usuarioDTO.getIdUsuario());
		usuarioBean.setUsuario(usuarioDTO.getUsuario());
		usuarioBean.setDesusuario(usuarioDTO.getDesUsuario());
		usuarioBean.setClave(usuarioDTO.getClave());
		usuarioBean.setActivo(usuarioDTO.getActivo());
		usuarioBean.setMenuPorDefecto(usuarioDTO.getUidMenuPorDefecto());
		usuarioBean.setPuedeCambiarMenu(usuarioDTO.getPuedeCambiarMenuAsString());

		return usuarioBean;
	}

	@Override
	public boolean cambiarPassword(String newPassword, String oldPassword, String usuario) throws RestException, RestHttpException {

		boolean result = false;
		CambiarClaveUsuarioRequestRest request = new CambiarClaveUsuarioRequestRest();

		request.setClave(oldPassword);
		request.setClaveNueva(newPassword);
		request.setUsuario(usuario.toUpperCase());
		request.setApiKey(variablesServices.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY));
		request.setUidActividad(sesion.getAplicacion().getUidActividad());

		/* BRICO-128 llamada a rest personalizado */
		ResponsePostRest response = RestUsuariosClient.setClaveUsuario(request);
		/* fin BRICO-128 */

		// BRICO-326
		if (response.getCodError() == ErroresCambioPassword.COD_ERROR_HORAS_MINIMAS) {
			throw new MinHorasException(I18N.getTexto(ErroresCambioPassword.MSG_ERROR_HORAS_MINIMAS));
		}
		if (response.getCodError() == ErroresCambioPassword.COD_ERROR_VAR_MIN_HORA_RENOVACION) {
			throw new VarHorasMinException(I18N.getTexto(ErroresCambioPassword.MSG_ERROR_VAR_MIN_HORA_RENOVACION));
		}
		// fin BRICO-326

		if (response.getNumeroUpdates() > 0) {
			result = true;
		}

		return result;
	}

}
