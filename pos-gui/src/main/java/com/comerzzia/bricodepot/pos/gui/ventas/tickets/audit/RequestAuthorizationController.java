package com.comerzzia.bricodepot.pos.gui.ventas.tickets.audit;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.validation.ConstraintViolation;

import com.comerzzia.bricodepot.pos.services.ticket.BricodepotCabeceraTicket;
import com.comerzzia.bricodepot.pos.services.ticket.audit.TicketAuditEvent;
import com.comerzzia.bricodepot.pos.services.ticket.audit.TicketAuditEvent.Type;
import com.comerzzia.core.util.paginacion.PaginaResultados;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.controllers.WindowController;
import com.comerzzia.pos.core.gui.login.LoginFormularioBean;
import com.comerzzia.pos.core.gui.validation.ValidationUI;
import com.comerzzia.pos.gui.ventas.tickets.TicketManager;
import com.comerzzia.pos.gui.ventas.tickets.articulos.FacturacionArticulosController;
import com.comerzzia.pos.persistence.core.acciones.AccionBean;
import com.comerzzia.pos.persistence.core.perfiles.ParametrosBuscarPerfilesBean;
import com.comerzzia.pos.persistence.core.perfiles.PerfilBean;
import com.comerzzia.pos.persistence.core.permisos.PermisoBean;
import com.comerzzia.pos.persistence.core.usuarios.UsuarioBean;
import com.comerzzia.pos.services.core.acciones.AccionesService;
import com.comerzzia.pos.services.core.perfiles.ServicioPerfiles;
import com.comerzzia.pos.services.core.permisos.ServicioPermisos;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.sesion.SesionUsuario;
import com.comerzzia.pos.services.core.usuarios.UsuarioInvalidLoginException;
import com.comerzzia.pos.services.core.usuarios.UsuariosService;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.i18n.I18N;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

@Controller
public class RequestAuthorizationController extends WindowController {

	// log
	private static final Logger log = Logger.getLogger(RequestAuthorizationController.class.getName());

	public static final String PERMITIR_ACCION = "PERMITIR_ACCION";
	public static final String AUDIT_EVENT = "AUDIT_EVENT";
	public static final String VALIDACION_PERFIL = "VALIDACION_RAPIDA.PERFIL";
	public static final String PERMITIR_AUTORIZACION = "PERMITIR AUTORIZACION";
	
	UsuarioBean supervisor;

	List<TicketAuditEvent> auditEvent;

	TicketManager ticketManager;

	@FXML
	protected TextField tfUsuario;
	@FXML
	protected PasswordField tfPassword;
	@FXML
	protected Button btAceptar;
	@FXML
	protected Button btCancelar;
	@FXML
	protected Label lbError;
	@FXML
	protected Label lbInformacion;

	protected Runnable accionAceptar;

	// Formulario de logín -> funcion submit accionFormularioLoginSubmit()
	protected LoginFormularioBean formularioLogin;

	@Autowired
	protected ServicioPermisos servicioPermisos;
	@Autowired
	protected SesionUsuario sesionUsuario;
	@Autowired
	protected UsuariosService usuariosService;
	@Autowired
	protected VariablesServices variablesService;
	@Autowired
	protected ServicioPerfiles servicioPerfiles;
	@Autowired
	protected Sesion sesion;
	@Autowired
	protected AccionesService accionesService;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		log.debug("initialize()");
		// Inicializamos el formulario de login
		formularioLogin = SpringContext.getBean(LoginFormularioBean.class);
		// Asignamos un componente a cada elemento del formulario. (Para establecer foco
		// o estilos de error)
		formularioLogin.setFormField("usuario", tfUsuario);
		formularioLogin.setFormField("password", tfPassword);
	}

	@Override
	public void initializeComponents() throws InitializeGuiException {

		lbError.setText("");
		lbInformacion.setText("");
		btCancelar.setDisable(false);

		this.accionLimpiarFormulario();

	}

	/**
	 * Captura y consume evento de soltar tecla enter
	 * 
	 * @param e KeyEvent
	 */
	@FXML
	public void accionAceptarIntro(KeyEvent e) {
		log.debug("accionAceptarIntro()");

		if (e.getCode() == KeyCode.ENTER) {
			accionAceptar();
			e.consume();
		}
	}

	@FXML
	public void accionCancelarEsc(KeyEvent e) {
		log.debug("accionCancelarEsc()");

		if (e.getCode() == KeyCode.ESCAPE) {
			accionCancelar();
			e.consume();
		}
	}

	@Override
	public void initializeFocus() {
		tfUsuario.requestFocus();
	}

	@Override
	public void initializeForm() throws InitializeGuiException {
		this.auditEvent = null;
		this.ticketManager = null;
		this.supervisor = null;
		lbError.setText("");
		accionLimpiarFormulario();
		initializeFocus();
		this.auditEvent = castToList(getDatos().get("AUDIT_EVENT"));
		this.ticketManager = (TicketManager) getDatos().get(FacturacionArticulosController.TICKET_KEY);
		
		
		// si hay una parametro permitir accion quitarlo
		try {
			getDatos().remove(RequestAuthorizationController.PERMITIR_ACCION);
		} catch (Exception ignore) {

		}
		// asumir que ambos eventos son para la misma linea del ticket
		StringBuilder eventType = new StringBuilder();
		List<Type> eventsUsed = new ArrayList<TicketAuditEvent.Type>();
		if (auditEvent.size() > 1) {
			for (int i = 0; i < auditEvent.size(); i++) {
				if(!eventsUsed.contains(auditEvent.get(i).getType())) {
					eventsUsed.add(auditEvent.get(i).getType());
					if(i == 0) {
						eventType.append(auditEvent.get(i).getTypeAsString());
					}
					else {
						eventType.append(" & " + auditEvent.get(i).getTypeAsString());
					}
				}
			}
		} else {
			eventType.append(auditEvent.get(0).getTypeAsString());
		}
		log.debug("auditAction() - Captured auditable action of type " + eventType);
		if(!(auditEvent.size()>1) && auditEvent.get(0).getType().equals(Type.QR_CADUCADO)) {
			lbInformacion.setText(eventType + ", " + I18N.getTexto("se requiere autorización para su uso"));
		}
		else {
			lbInformacion.setText(I18N.getTexto("Se necesita autorización para") + " " + eventType
					+ (auditEvent.get(0).getDesArticulo() == null ? "" : (" de \n" + auditEvent.get(0).getDesArticulo())));
		}
		
	}

	@SuppressWarnings("unchecked")
	public static <T extends List<?>> T castToList(Object obj) {
		return (T) obj;
	}

	@Override
	@FXML
	public void accionCancelar() {
		StringBuilder eventType = new StringBuilder();
		if (auditEvent.size() > 1) {
			for (int i = 0; i < auditEvent.size(); i++) {
				eventType.append(auditEvent.get(i).getTypeAsString());
				if (i < auditEvent.size() - 1) {
					eventType.append(" & ");
				}
			}
		} else {
			eventType.append(auditEvent.get(0).getTypeAsString());
		}
		
		log.debug("actionBtCancelar() - Action(s) " + eventType + " cancelled or unauthorized :(");
		getDatos().put(PERMITIR_ACCION, Boolean.FALSE);
		getDatos().remove("AUDIT_EVENT");
		getDatos().put("cancela", "cancela");
		super.accionCancelar();
	}

	@FXML
	public void accionAceptar() {
		log.debug("accionAceptar()");
		if (accionFrLoginSubmit()) {
			try {
				UsuarioBean usuarioBean = usuariosService.login(formularioLogin.getUsuario(),
						formularioLogin.getPassword());
				this.supervisor = usuarioBean;
				
				// busca los perfiles del usuario supervisor
				ParametrosBuscarPerfilesBean params = new ParametrosBuscarPerfilesBean();
				params.setNumPagina(1);
				params.setIdUsuario(usuarioBean.getIdUsuario());
				PaginaResultados consultaPerfiles = this.servicioPerfiles.consultar(params);
				Boolean allow = false;
				AccionBean accion = new AccionBean();
				accion = getApplication().getMainView().getCurrentAccion();

				String perfiles = variablesService.getVariableAsString(VALIDACION_PERFIL);
				if(perfiles != null && !perfiles.isEmpty()){
					String[] perfil = perfiles.split(",");
					for (Object p : consultaPerfiles.getPagina()) {
						Long idPerfil = ((PerfilBean) p).getIdPerfil();
						com.comerzzia.pos.services.core.permisos.PermisosEfectivosAccionBean permisos = servicioPermisos.obtenerPermisosEfectivos(accion, usuarioBean.getIdUsuario());
						for(String perf:perfil){
							if (String.valueOf(idPerfil).equals(perf)) {
								allow = true;
							}							
						}
						if (allow && auditEvent.get(0).getType().equals(Type.CAMBIO_PRECIO)) {
							PermisoBean permisoPermitirAutorizacion = permisos.getPermisos().get(PERMITIR_AUTORIZACION);
							if(!permisoPermitirAutorizacion.isConcedido() && !permisoPermitirAutorizacion.isAdministrar()) {
								allow = false;
								break;
							}
						}
						if(allow){
							break;
						}
					}
				}

				if (allow) {
					for (TicketAuditEvent e : auditEvent) {
						e.logSupervisor(this.supervisor);

						log.info("accionAceptar() - Action " + e.getTypeAsString() + " authorized by "
								+ e.getDesUsuarioSupervisor());

						if (ticketManager != null) {
							((BricodepotCabeceraTicket) ticketManager.getTicket().getCabecera()).addAuditEvent(e);
						}
						getDatos().remove("AUDIT_EVENT");
						getDatos().put(PERMITIR_ACCION, Boolean.TRUE);

						getStage().close();
					}
				} else {
					lbError.setText(I18N.getTexto("Permisos insuficientes"));
					accionLimpiarFormulario();
					initializeFocus();
				}

			} catch (UsuarioInvalidLoginException ex) {
				lbError.setText(I18N.getTexto(ex.getMessageI18N()));
				accionLimpiarFormulario();
				initializeFocus();

			} catch (Exception ex) {
				log.debug("accionAceptar() - Error no controlado -" + ex.getMessage(), ex);
				VentanaDialogoComponent.crearVentanaError(getStage(), ex);
			}
		}
	}

	protected void accionLimpiarFormulario() {
		log.debug("accionLimpiarFormulario()");
		tfUsuario.setText("");
		tfPassword.setText("");
		formularioLogin.limpiarFormulario();
	}

	public boolean accionFrLoginSubmit() {
		formularioLogin.setUsuario(tfUsuario.getText());
		formularioLogin.setPassword(tfPassword.getText());
		return accionValidarFrLogin();
	}

	protected boolean accionValidarFrLogin() {
		// Limpiamos los errores que pudiese tener el formulario
		formularioLogin.clearErrorStyle();

		// Validamos el formulario de login
		Set<ConstraintViolation<LoginFormularioBean>> constraintViolations = ValidationUI.getInstance().getValidator()
				.validate(formularioLogin);
		if (constraintViolations.size() >= 1) {
			ConstraintViolation<LoginFormularioBean> next = constraintViolations.iterator().next();
			formularioLogin.setErrorStyle(next.getPropertyPath(), true);
			formularioLogin.setFocus(next.getPropertyPath());
			lbError.setText(next.getMessage());
			return false;
		}
		return true;
	}

}
