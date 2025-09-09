package com.comerzzia.bricodepot.pos.gui.ventas.articulos.autorizaracciones;

import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.gui.ventas.articulos.autorizaracciones.formulario.FormularioAutorizaDevolucionBean;
import com.comerzzia.bricodepot.pos.gui.ventas.devoluciones.articulos.BricodepotDevolucionesController;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.controllers.WindowController;
import com.comerzzia.pos.core.gui.validation.ValidationUI;
import com.comerzzia.pos.persistence.core.usuarios.UsuarioBean;
import com.comerzzia.pos.services.core.permisos.PermisosEfectivosAccionBean;
import com.comerzzia.pos.services.core.permisos.ServicioPermisos;
import com.comerzzia.pos.services.core.sesion.SesionInitException;
import com.comerzzia.pos.services.core.usuarios.UsuarioInvalidLoginException;
import com.comerzzia.pos.services.core.usuarios.UsuariosService;
import com.comerzzia.pos.services.core.usuarios.UsuariosServiceException;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.i18n.I18N;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/* [BRICO-78] - Devoluciones de tickets de Flexpoint */
@Component
public class AutorizarAccionesController extends WindowController{

	private static final Logger log = Logger.getLogger(AutorizarAccionesController.class.getName());
	
	@FXML
	private TextField tfUsuario, tfDocumento;
	@FXML
	private PasswordField tfPass;
	@FXML
	private Label lbError, lbTitulo, lbMensaje, lbTituloDatosDevolucion, lbDocumento;
	
	@Autowired
	private UsuariosService usuariosService;
	
	@Autowired
	private ServicioPermisos servicioPermisos;
	
	public static final String sUsuario = "usuario", sNombre = "nombre", sDocumento = "documento";
	private static final String OPERACION_DEVOLUCIONES = "EJECUCION";

	private String idDocumento;
	
	FormularioAutorizaDevolucionBean frAutorizaDevolucion;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		frAutorizaDevolucion = SpringContext.getBean(FormularioAutorizaDevolucionBean.class);
		frAutorizaDevolucion.setFormField("usuario", tfUsuario);
		frAutorizaDevolucion.setFormField("pass", tfPass);
		frAutorizaDevolucion.setFormField("documento", tfDocumento);
	}

	@Override
	public void initializeComponents() throws InitializeGuiException {
		registraEventoTeclado(new EventHandler<KeyEvent>(){

			@Override
			public void handle(KeyEvent arg0){
				if(arg0.getCode().equals(KeyCode.ENTER)){
					try{
						accionAceptar();
					}
					catch(Exception ex){
					}
				}else if(arg0.getCode().equals(KeyCode.ESCAPE)) {
					accionCancelar();
				}
			}
		}, KeyEvent.KEY_RELEASED);
	}

	@Override
	public void initializeFocus() {
		tfUsuario.setText("");
		tfPass.setText("");
		tfDocumento.setText(idDocumento);
		estableceFoco();
	}

	@Override
	public void initializeForm() throws InitializeGuiException {
		/* Damos a todo el valor por defecto */
		idDocumento = getDatos().get(BricodepotDevolucionesController.ID_DOCUMENTO_FLEXPOINT).toString();
		tfUsuario.setText("");
		tfPass.setText("");
		lbTitulo.setText(I18N.getTexto("Autorizar devolución"));
		lbMensaje.setText(I18N.getTexto("Indique el usuario que autoriza la devolución :"));
		lbTituloDatosDevolucion.setVisible(true);
		lbDocumento.setVisible(true);
		tfDocumento.setVisible(true);
		tfDocumento.setText("");
		/* Limpiamos el error */
		frAutorizaDevolucion.clearErrorStyle();
		lbError.setText("");
		/* Establecemos los parametros a vacío */
		getDatos().put(sUsuario, "");
		getDatos().put(sNombre, "");
		getDatos().put(sDocumento, "");
	}

	@FXML
	public void accionAceptar() {
		String sDesUsuario = "";
		frAutorizaDevolucion.setUsuario(tfUsuario.getText());
		frAutorizaDevolucion.setPass(tfPass.getText());
		frAutorizaDevolucion.setDocumento(tfDocumento.getText());
		try{
			if(validarFormulario()){
				try{
					/* Este es el usuario que ha introducido sus credenciales */
					UsuarioBean usuarioBean = usuariosService.login(tfUsuario.getText(), tfPass.getText());
					sDesUsuario = usuarioBean.getDesusuario();
					
					/* Comprobamos los permisos del usuario que ha introducido sus credenciales */
					PermisosEfectivosAccionBean permisos = servicioPermisos.obtenerPermisosEfectivos(getApplication().getMainView().getCurrentAccion(), usuarioBean.getIdUsuario());
					if(permisos.isPuede(OPERACION_DEVOLUCIONES)) {
						getDatos().put(sUsuario, tfUsuario.getText());
						getDatos().put(sNombre, sDesUsuario);
						getDatos().put(sDocumento, tfDocumento.getText());
						getStage().close();
					}else {
						VentanaDialogoComponent.crearVentanaError(I18N.getTexto("No tiene permisos para realizar devoluciones"), getStage());
						tfUsuario.requestFocus();
					}
				}
				catch(UsuarioInvalidLoginException ex){
					throw ex;
				}
				catch(UsuariosServiceException ex){
					log.error("init() -  Error iniciando sesión de usuario : " + tfUsuario.getText() + " - " + ex.getMessageI18N());
					throw new SesionInitException(ex.getMessageI18N(), ex);
				}
				catch(Exception ex){
					log.error("init() - Error inicializando sesión de usuario : " + tfUsuario.getText() + " - " + ex.getMessage(), ex);
					throw new SesionInitException(ex);
				}
			}
			else{
				log.debug("Datos autorización devolución incompletos");
				return;
			}
		}
		catch(Exception ex){
			log.error("actionBtAceptar() - Error no controlado - " + ex.getMessage(), ex);
			lbError.setText(I18N.getTexto("Usuario y/o contraseña no válidos"));
			tfUsuario.setText("");
			tfPass.setText("");
			tfUsuario.requestFocus();
		}
	}
	
	public boolean validarFormulario() {
		boolean valido = true;
		/* Inicializamos la etiqueta de error */
		frAutorizaDevolucion.clearErrorStyle();
		lbError.setText("");

		/* Validamos el formulario */
		Set<ConstraintViolation<FormularioAutorizaDevolucionBean>> constraintViolations = ValidationUI.getInstance()
				.getValidator().validate(frAutorizaDevolucion);
		Iterator<ConstraintViolation<FormularioAutorizaDevolucionBean>> iterator = constraintViolations.iterator();
		while(iterator.hasNext()) {
			ConstraintViolation<FormularioAutorizaDevolucionBean> next = iterator.next();
			frAutorizaDevolucion.setErrorStyle(next.getPropertyPath(), true);		
			lbError.setText(next.getMessage());
			valido = false;
		}
		if (!valido) {
			estableceFoco();
		}
		return valido;
	}

	public void estableceFoco() {
		if (tfUsuario.getText().trim().equals("")) {
			tfUsuario.requestFocus();
		} else if (tfPass.getText().trim().equals("")) {
			tfPass.requestFocus();
		} else if (tfDocumento.getText().trim().equals("")) {
			tfDocumento.requestFocus();
		}
	}

	@FXML
	public void accionCancelar() {
		getStage().close();
	}
	
}
