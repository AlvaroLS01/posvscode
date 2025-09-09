package com.comerzzia.bricodepot.pos.gui.mantenimientos.clientes;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.bricodepot.pos.gui.ventas.tickets.audit.RequestAuthorizationController;
import com.comerzzia.bricodepot.pos.gui.ventas.tickets.audit.RequestAuthorizationView;
import com.comerzzia.bricodepot.pos.services.cliente.BricodepotClientesService;
import com.comerzzia.bricodepot.pos.services.ticket.audit.TicketAuditEvent;
import com.comerzzia.core.util.base.Estado;
import com.comerzzia.pos.core.gui.BackgroundTask;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.botonaccion.BotonBotoneraComponent;
import com.comerzzia.pos.core.gui.componentes.botonaccion.simple.BotonBotoneraSimpleComponent;
import com.comerzzia.pos.core.gui.componentes.botonera.BotoneraComponent;
import com.comerzzia.pos.core.gui.componentes.botonera.ConfiguracionBotonBean;
import com.comerzzia.pos.core.gui.componentes.botonera.IContenedorBotonera;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.controllers.WindowController;
import com.comerzzia.pos.core.gui.exception.CargarPantallaException;
import com.comerzzia.pos.core.gui.tablas.celdas.CellFactoryBuilder;
import com.comerzzia.pos.core.gui.validation.ValidationUI;
import com.comerzzia.pos.gui.mantenimientos.clientes.MantenimientoClienteController;
import com.comerzzia.pos.gui.mantenimientos.clientes.MantenimientoClienteTasks;
import com.comerzzia.pos.gui.mantenimientos.clientes.MantenimientoClienteView;
import com.comerzzia.pos.gui.ventas.tickets.clientes.ClienteGui;
import com.comerzzia.pos.gui.ventas.tickets.clientes.FormularioConsultaClienteBean;
import com.comerzzia.pos.gui.ventas.tickets.factura.paises.PaisesController;
import com.comerzzia.pos.gui.ventas.tickets.factura.paises.PaisesView;
import com.comerzzia.pos.persistence.clientes.ClienteBean;
import com.comerzzia.pos.persistence.paises.PaisBean;
import com.comerzzia.pos.persistence.tiposIdent.TiposIdentBean;
import com.comerzzia.pos.services.core.paises.PaisNotFoundException;
import com.comerzzia.pos.services.core.paises.PaisService;
import com.comerzzia.pos.services.core.paises.PaisServiceException;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.tiposIdent.TiposIdentNotFoundException;
import com.comerzzia.pos.services.core.tiposIdent.TiposIdentService;
import com.comerzzia.pos.services.core.tiposIdent.TiposIdentServiceException;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.i18n.I18N;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import rest.bean.cliente.BricodepotClienteBean;
import rest.client.clientes.BricodepotClientesRest;
import rest.client.clientes.BricodepotConsultarClienteRequestRest;
import rest.client.clientes.BricodepotResponseGetClientesRest;

@Component
@Primary
public class BricodepotBuscarClienteController extends WindowController implements IContenedorBotonera {

	private static final Logger log = Logger.getLogger(BricodepotBuscarClienteController.class.getName());

	@Autowired
	private VariablesServices variablesServices;
	@Autowired
	private Sesion sesion;

	public static final String MODO_MODAL = "MODAL";
	
	 protected Boolean modoModal = false;
	
	@Autowired
	private PaisService paisService = SpringContext.getBean(PaisService.class);

	public static final String PARAMETRO_SALIDA_CLIENTE = "PARAMETRO_SALIDA_CLIENTE";

	// - lista de clientesBuscados
	protected ObservableList<ClienteGui> clientesBuscados;

	// - cliente seleccionado
	protected ClienteBean objetoSeleccionado;

	// - botonera de acciones de tabla
	protected BotoneraComponent botoneraAccionesTabla;

	// - acciones de pantalla al aceptar o accionCancelar
	@SuppressWarnings("rawtypes")
	protected EventHandler actionHandlerCancelar;

	protected String codPais;
	// Componentes
	@FXML
	protected TextField tfNumDocIdent, tfDescripcion, tfCodPais, tfDesPais;

	@FXML
	protected TableView<ClienteGui> tbClientes;
	@FXML
	protected TableColumn<ClienteGui, String> tcClientesDescripcion;
	@FXML
	protected TableColumn<ClienteGui, String> tcClientesCif;
	@FXML
	protected TableColumn<ClienteGui, String> tcClientesPoblacion;
	@FXML
	protected TableColumn<ClienteGui, String> tcClientesProvincia;

	@FXML
	protected AnchorPane panelMenuTabla;

	@FXML
	protected ComboBox<TiposIdentBean> cbTipoDocIdent;
	protected ObservableList<TiposIdentBean> tiposIdent;

	@FXML
	protected Label lbError;

	@FXML
	protected HBox footerHBox;

	protected FormularioConsultaClienteBean frConsultaCliente;

	@Autowired
	private TiposIdentService tiposIdentService;

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc="Creación e inicialización">
	/**
	 * Inicializa el componente tras su creación. No hay acceso al application desde
	 * este método.
	 *
	 * @param url
	 * @param rb
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		log.debug("initialize()");

		tbClientes.setPlaceholder(new Text(""));

		clientesBuscados = FXCollections.observableArrayList(new ArrayList<ClienteGui>());
		tbClientes.setItems(clientesBuscados);

		// Inicializamos la tabla
		tcClientesDescripcion.setCellFactory(CellFactoryBuilder.createCellRendererCelda("tbClientes",
				"tcClientesDescripcion", null, CellFactoryBuilder.ESTILO_ALINEACION_IZQ));
		tcClientesCif.setCellFactory(CellFactoryBuilder.createCellRendererCelda("tbClientes", "tcClientesCif", null,
				CellFactoryBuilder.ESTILO_ALINEACION_IZQ));
		tcClientesPoblacion.setCellFactory(CellFactoryBuilder.createCellRendererCelda("tbClientes",
				"tcClientesPoblacion", null, CellFactoryBuilder.ESTILO_ALINEACION_IZQ));
		tcClientesProvincia.setCellFactory(CellFactoryBuilder.createCellRendererCelda("tbClientes",
				"tcClientesProvincia", null, CellFactoryBuilder.ESTILO_ALINEACION_IZQ));

		tcClientesDescripcion.setCellValueFactory(new PropertyValueFactory<ClienteGui, String>("descripcion"));
		tcClientesCif.setCellValueFactory(new PropertyValueFactory<ClienteGui, String>("cif"));
		tcClientesPoblacion.setCellValueFactory(new PropertyValueFactory<ClienteGui, String>("poblacion"));
		tcClientesProvincia.setCellValueFactory(new PropertyValueFactory<ClienteGui, String>("provincia"));

		// Creamos el formulario que validará los parámetros para la búsqueda de
		// clientes
		frConsultaCliente = SpringContext.getBean(FormularioConsultaClienteBean.class);
		frConsultaCliente.setFormField("numDocIdent", tfNumDocIdent);
		frConsultaCliente.setFormField("descCliente", tfDescripcion);
	}

	@Override
	public void initializeFocus() {
		tfNumDocIdent.requestFocus();
	}

	/**
	 * Inicializa los componentes establecer la configuración de la ventana
	 */
	@Override
	public void initializeComponents() throws InitializeGuiException {
		
		
		tfCodPais.focusedProperty().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
				tfCodPais.setText(tfCodPais.getText().toUpperCase());
				if (!newValue ) {
					PaisBean pais = null;
					if(StringUtils.isNotBlank(tfCodPais.getText())) {
	                    try {
	                        pais = paisService.consultarCodPais(tfCodPais.getText());
	                    }
	                    catch (PaisNotFoundException e) {
	                    	log.debug("initializeComponents() - No se ha encontrado el país con código: " + tfCodPais.getText());
	                    }
	                    catch (PaisServiceException e) {
	                    	log.debug("initializeComponents() - Ha habido un error al buscar el país con código " + tfCodPais.getText() + ": " + e.getMessage());
	                    }
					}
                    
					if(pais != null) {
						tfDesPais.setText(pais.getDesPais());
					}
					else {
						tfDesPais.clear();
					}
					codPais = tfCodPais.getText();
					loadTiposIdentificacion();
				}
			}
		});
		
		
		try {
			List<ConfiguracionBotonBean> listaAccionesAccionesTabla = BotoneraComponent.cargarAccionesTablaSimple();
			listaAccionesAccionesTabla.add(0,
					new ConfiguracionBotonBean("iconos/view.png", null, null, "VER", "REALIZAR_ACCION"));
			listaAccionesAccionesTabla.add(1,
					new ConfiguracionBotonBean("iconos/add.png", null, null, "AÑADIR", "REALIZAR_ACCION"));
			listaAccionesAccionesTabla.add(2,
					new ConfiguracionBotonBean("iconos/edit.png", null, null, "EDITAR", "REALIZAR_ACCION"));
			listaAccionesAccionesTabla.add(3,
					new ConfiguracionBotonBean("iconos/delete.png", null, null, "ELIMINAR", "REALIZAR_ACCION"));

			log.debug("inicializarComponentes() - Configurando botonera");
			botoneraAccionesTabla = new BotoneraComponent(4, 1, this, listaAccionesAccionesTabla,
					panelMenuTabla.getPrefWidth(), panelMenuTabla.getPrefHeight(),
					BotonBotoneraSimpleComponent.class.getName());
			panelMenuTabla.getChildren().add(botoneraAccionesTabla);

			// Se registra el evento para salir de la pantalla pulsando la tecla escape.
			registrarAccionCerrarVentanaEscape();

		} catch (CargarPantallaException ex) {
			log.error("inicializarComponentes() - Error creando botonera para la consulta de clientes. error : "
					+ ex.getMessage(), ex);
			VentanaDialogoComponent.crearVentanaError(ex.getMessageI18N(), getStage());
		}
		/* BRICO-253 se inhabilita botón eliminar cliente */
		botoneraAccionesTabla.getBotonClave("ELIMINAR").setDisable(true);

	}

	@Override
	public void initializeForm() {
		log.debug("initializeform()");
		String codigoPais = sesion.getAplicacion().getTienda().getCliente().getCodpais();
		tfCodPais.setText(codigoPais);

		try {
			
			tfDescripcion.clear();
			tfNumDocIdent.clear();
			
			String descripcionPais = paisService.consultarCodPais(codigoPais).getDesPais();
			tfDesPais.setText(descripcionPais);
			
			tiposIdent = FXCollections.observableArrayList();
			loadTiposIdentificacion();
			cbTipoDocIdent.setItems(tiposIdent);
		} catch (PaisNotFoundException | PaisServiceException e) {
			log.error(I18N.getTexto("initializeForm()- No se ha encontrado el pais:" + codPais), e);
		}

		AnchorPane pane = (AnchorPane) getView().getViewNode().getChildrenUnmodifiable().get(0);
		pane.getStyleClass().remove("pantalla-modal");
		footerHBox.setPrefHeight(0.0);
		footerHBox.setVisible(false);

	}

	/**
	 * Busca un cliente en función de los campos de búsqueda de la pantalla.
	 */
	@FXML
	public void accionBuscar() {
		log.trace("accionBuscar()");

		clientesBuscados.clear();

		frConsultaCliente.setNumDocIdent(tfNumDocIdent.getText());
		frConsultaCliente.setDescCliente(tfDescripcion.getText());

		if (validarFormularioConsultaCliente()) {
			String descripcion = tfDescripcion.getText();
			String ident = tfNumDocIdent.getText().toUpperCase();
			String codTipoIdent = null;
			if (cbTipoDocIdent.getValue() != null) {
				codTipoIdent = cbTipoDocIdent.getValue().getCodTipoIden();
			} else {
				codTipoIdent = "";
			}
			/* BRICO-253 */
			String apiKey = variablesServices.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
			String uidActividad = sesion.getAplicacion().getUidActividad();
			String codigoPais = tfCodPais.getText();

			if (StringUtils.isBlank(ident) || StringUtils.isBlank(codTipoIdent)) {
				log.debug("accionBuscar() - No se ha informado tipo documento y/o documento");

				lbError.setText(
						I18N.getTexto("Debe informar Tipo Documento y Documento para poder realizar la búsqueda."));
			} else {
				log.debug(
						"accionBuscar() - Se ha informado tipo documento y documento y se procede a realizar la consulta");
				new BricodepotBuscarClientesTask(codTipoIdent, ident, descripcion, uidActividad, codigoPais, apiKey)
						.start();
				/* fin BRICO-253 */
			}
		}
	}

	/*
	 * BRICO-253 Task similar a BuscarClientesTask de ConsultaClienteController
	 * hacían falta parámetros distintos para consultar los clientes
	 */
	public class BricodepotBuscarClientesTask extends BackgroundTask<List<ClienteBean>> {

		private String codTipoIdent;
		private String cif;
		private String desCliente;
		private String uidActividad;
		private String codPais;
		private String apiKey;

		public BricodepotBuscarClientesTask(String codTipoIdent, String cif, String desCliente, String uidActividad,
				String codPais, String apiKey) {
			this.codTipoIdent = codTipoIdent;
			this.cif = cif;
			this.desCliente = desCliente;
			this.uidActividad = uidActividad;
			this.codPais = codPais;
			this.apiKey = apiKey;
		}

		// apiKey, uidActividad, descripcion, codTipoIdent, codPais, ident
		@Override
		protected List<ClienteBean> call() throws Exception {
			BricodepotConsultarClienteRequestRest consultaCliente = new BricodepotConsultarClienteRequestRest(apiKey, uidActividad, codTipoIdent, cif, desCliente, codPais);
			
			BricodepotResponseGetClientesRest response = BricodepotClientesRest.getClientePaisPOS(consultaCliente);
			List<ClienteBean> clientesPOS = new ArrayList<ClienteBean>();
			
			if (response != null && response.getClientes() != null && !response.getClientes().isEmpty()) {
				for (BricodepotClienteBean clienteBean : response.getClientes()) {
					clientesPOS.add(BricodepotClientesService.castClienteApiAPos(clienteBean));
				}
			}
			return clientesPOS;
			
		}

		@Override
		protected void failed() {
			log.error(getCMZException().getLocalizedMessage(), getCMZException());
			VentanaDialogoComponent.crearVentanaError(getStage(), getCMZException().getMessageI18N(),
					getCMZException());
			super.failed();
		}

		@Override
		protected void succeeded() {
			List<ClienteBean> clientesF = getValue();
			List<ClienteGui> clientesTabla = new ArrayList<ClienteGui>();

			for (ClienteBean c : clientesF) {
				clientesTabla.add(new ClienteGui(c));
			}

			if (clientesTabla.isEmpty()) {
				tbClientes.setPlaceholder(
						new Text(I18N.getTexto("No hay registros para los parámetros de búsqueda seleccionados")));
			} else {
				clientesBuscados.addAll(clientesTabla);
				tbClientes.getSelectionModel().selectFirst();
			}
			super.succeeded();
		}
	}

	@Override
	public void registrarAccionCerrarVentanaEscape() {
		// No registramos el evento
	}

	/**
	 * Método de control de acciones de página de clientesBuscados
	 *
	 * @param botonAccionado botón pulsado
	 */
	@Override
	public void realizarAccion(BotonBotoneraComponent botonAccionado) {
		log.debug("realizarAccion() - Realizando la acción : " + botonAccionado.getClave() + " de tipo : "
				+ botonAccionado.getTipo());
		switch (botonAccionado.getClave()) {

		case "ACCION_TABLA_PRIMER_REGISTRO":
			log.debug("Acción seleccionar primer registro de la tabla");
			accionTablaPrimerRegistro(tbClientes);
			break;
		case "ACCION_TABLA_ANTERIOR_REGISTRO":
			log.debug("Acción seleccionar registro anterior de la tabla");
			accionTablaIrAnteriorRegistro(tbClientes);
			break;
		case "ACCION_TABLA_SIGUIENTE_REGISTRO":
			log.debug("Acción seleccionar siguiente registro de la tabla");
			accionTablaIrSiguienteRegistro(tbClientes);
			break;
		case "ACCION_TABLA_ULTIMO_REGISTRO":
			log.debug("Acción seleccionar último registro de la tabla");
			accionTablaUltimoRegistro(tbClientes);
			break;
		}
		switch (botonAccionado.getClave()) {

		case "VER":
			verCliente(false);
			break;
		case "AÑADIR":
			crearCliente();
			break;
		case "EDITAR":
			verCliente(true);
			break;
		case "ELIMINAR":
			eliminarCliente();
			break;
		}
	}

	public void eliminarCliente() {
		if (tbClientes.getSelectionModel().getSelectedItem() != null) {
			if (VentanaDialogoComponent.crearVentanaConfirmacion(
					I18N.getTexto("Se borrará el cliente seleccionado. ¿Está seguro?"), getStage())) {
				MantenimientoClienteTasks.executeEliminarTask(
						tbClientes.getSelectionModel().getSelectedItem().getCliente(), clientesBuscados, getStage(),
						null);
				accionBuscar();
			}
		}
	}

	public void verCliente(boolean edicion) {
		log.debug("verCliente() - Edicion [" + edicion + "]");
		try {
			int indice = tbClientes.getSelectionModel().getSelectedIndex();
			if (indice > -1) {
				HashMap<String, Object> datos = new HashMap<>();
				datos.put(MantenimientoClienteController.INDICE_CLIENTE_SELECCIONADO, indice);
				datos.put(MantenimientoClienteController.LISTA_CLIENTES, clientesBuscados);
				datos.put(MantenimientoClienteController.MODO_EDICION, edicion);
				if (edicion) {
					TicketAuditEvent auditEvent = TicketAuditEvent.forEvent(TicketAuditEvent.Type.MODIFICACION_CLIENTE,
							sesion);
					HashMap<String, Object> datosSupervisor = new HashMap<>();
					abrirVentanaAutorizacion(auditEvent, datosSupervisor);
					if (datosSupervisor.get(RequestAuthorizationController.PERMITIR_ACCION).equals(true)) {
						datos.put(MantenimientoClienteController.ESTADO_CLIENTE, Estado.MODIFICADO);
					} else {
						return;
					}
				} else {
					datos.put(MantenimientoClienteController.ESTADO_CLIENTE, Estado.SIN_MODIFICAR);
				}
				getView().changeSubView(MantenimientoClienteView.class, datos);
			}
		} catch (InitializeGuiException e) {
			log.error("verCliente() - " + e.getMessage(), e);
			VentanaDialogoComponent.crearVentanaError(getStage(), e);
		}
	}

	public void crearCliente() {
		log.debug("crearCliente()");
		try {
			HashMap<String, Object> datos = new HashMap<>();
			datos.put(MantenimientoClienteController.MODO_EDICION, true);
			datos.put(MantenimientoClienteController.ESTADO_CLIENTE, Estado.NUEVO);
			getView().changeSubView(MantenimientoClienteView.class, datos);

		} catch (InitializeGuiException e) {
			log.error("verCliente() - " + e.getMessage(), e);
			VentanaDialogoComponent.crearVentanaError(getStage(), e);
		}
	}

	/**
	 * Establece la acción que será ejecutada tras accionCancelar en la ventana
	 *
	 * @param actionHandlerCancelar
	 */
	@SuppressWarnings("rawtypes")
	public void setActionHandlerCancelar(EventHandler actionHandlerCancelar) {
		this.actionHandlerCancelar = actionHandlerCancelar;
	}

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc="Funciones relacionadas con
	// interfaz GUI y manejo de pantalla">
	/**
	 * Función que refresca los totales en pantalla
	 */
	/**
	 * Acción aceptar
	 */
	@FXML
	public void accionBotonAceptarCliente(ActionEvent event) {
		log.debug("accionBotonAceptarCliente() - Acción aceptar");
		tratarClienteSeleccionado();
	}

	/**
	 * Accion para tratar el doble click en una de las filas de la pantalla
	 *
	 * @param event
	 */
	@FXML
	public void accionTablaAceptarCliente(MouseEvent event) {
		if (event.getButton().equals(MouseButton.PRIMARY)) {
			if (event.getClickCount() == 2) {
				tratarClienteSeleccionado();
			}
		}
	}

	/**
	 * Método para establecer el cliente seleccionado y notificarlo
	 */
	protected void tratarClienteSeleccionado() {
		try {
			int indice = tbClientes.getSelectionModel().getSelectedIndex();
			HashMap<String, Object> datos = new HashMap<>();
			datos.put(MantenimientoClienteController.INDICE_CLIENTE_SELECCIONADO, indice);
			datos.put(MantenimientoClienteController.LISTA_CLIENTES, clientesBuscados);
			datos.put(MantenimientoClienteController.MODO_EDICION, false);
			datos.put(MantenimientoClienteController.ESTADO_CLIENTE, Estado.SIN_MODIFICAR);
			getView().changeSubView(MantenimientoClienteView.class, datos);
		} catch (InitializeGuiException e) {
			log.error("tratarClienteSeleccionado() - " + e.getMessage(), e);
			VentanaDialogoComponent.crearVentanaError(getStage(), e);
		}
	}

	/**
	 * Valida los parámetros de búsqueda introducidos
	 *
	 * @return
	 */
	protected boolean validarFormularioConsultaCliente() {

		boolean valido;

		// Limpiamos los errores que pudiese tener el formulario
		frConsultaCliente.clearErrorStyle();
		// Limpiamos el posible error anterior
		lbError.setText("");

		// Validamos el formulario de login
		Set<ConstraintViolation<FormularioConsultaClienteBean>> constraintViolations = ValidationUI.getInstance()
				.getValidator().validate(frConsultaCliente);
		if (constraintViolations.size() >= 1) {
			ConstraintViolation<FormularioConsultaClienteBean> next = constraintViolations.iterator().next();
			frConsultaCliente.setErrorStyle(next.getPropertyPath(), true);
			frConsultaCliente.setFocus(next.getPropertyPath());
			lbError.setText(next.getMessage());
			valido = false;
		} else {
			valido = true;
		}

		return valido;
	}

	/**
	 * Acción accionCancelar
	 */
	@SuppressWarnings("unchecked")
	public void accionCancelar() {
		log.debug("accionCancelar() - Cancelar");

		// Limpiamos los errores que pudiese tener el formulario
		frConsultaCliente.clearErrorStyle();
		// Limpiamos el posible error anterior
		lbError.setText("");
		// getStage().close();
		cierraVentana();
		// Ejecutamos el código de aceptación de la ventana padre que abre esta
		if (actionHandlerCancelar != null) {
			actionHandlerCancelar.handle(null);
		}
	}

	@FXML
	public void accionBuscarTeclado(KeyEvent event) {
		log.trace("accionBuscarTeclado()");

		if (event.getCode() == KeyCode.ENTER && !event.isControlDown()) {
			accionBuscar();
		} else if (event.getCode() == KeyCode.DOWN && !event.isControlDown() && tbClientes.getItems().size() > 0) {
			tbClientes.requestFocus();
		}
	}

	/**
	 * Funcion auxiliar que llamaremos para establecer como cliente seleccionado de
	 * la pantalla, el cliente seleccionado en la tabla
	 */
	protected boolean establecerClienteSeleccionado() {
		int indice = tbClientes.getSelectionModel().getSelectedIndex();
		if (indice >= 0) {
			objetoSeleccionado = tbClientes.getItems().get(indice).getCliente();
			getDatos().put(PARAMETRO_SALIDA_CLIENTE, objetoSeleccionado);
			return true;
		} else {
			objetoSeleccionado = null;
			VentanaDialogoComponent.crearVentanaAviso("", I18N.getTexto("No ha seleccionado ningún cliente"),
					getStage());
			return false;
		}
	}

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc="AccionesMenu">

	// </editor-fold>
	// <editor-fold defaultstate="collapsed" desc=" Getters de objetos de la
	// pantalla">
	/**
	 * Devuelve el cliente seleccionado de la pantalla
	 *
	 * @return
	 */
	public ClienteBean getObjetoSeleccionado() {
		return objetoSeleccionado;
	}

	// </editor-fold>

	protected void cierraVentana() {
		clientesBuscados.clear();
		getStage().close();
	}

	public void aceptarClienteTeclado(KeyEvent event) {
		log.trace("aceptarClienteTeclado(KeyEvent event) - Aceptar");
		if (event.getCode() == KeyCode.ENTER) {
			tratarClienteSeleccionado();
		}
	}

	protected void abrirVentanaAutorizacion(TicketAuditEvent auditEvent, HashMap<String, Object> datos) {
		log.debug("abrirVentanaAutorizacion() - Inicio del proceso de auditoria");
		List<TicketAuditEvent> events = new ArrayList<>();
		events.add(auditEvent);
		datos.put(RequestAuthorizationController.AUDIT_EVENT, events);

		getApplication().getMainView().showModalCentered(RequestAuthorizationView.class, datos, this.getStage());
	}

	@FXML
	public void accionBuscarPais() {
		getApplication().getMainView().showModalCentered(PaisesView.class, getDatos(), this.getStage());

		if (getDatos() != null && getDatos().containsKey(PaisesController.PARAMETRO_SALIDA_PAIS)) {
			PaisBean pais = (PaisBean) getDatos().get(PaisesController.PARAMETRO_SALIDA_PAIS);
			tfDesPais.setText(pais.getDesPais());
			tfCodPais.setText(pais.getCodPais());
			codPais = pais.getCodPais().toUpperCase();
			loadTiposIdentificacion();
			cbTipoDocIdent.getSelectionModel().selectFirst();
		}
	}

	protected void loadTiposIdentificacion() {
		try {
			cbTipoDocIdent.getSelectionModel().clearSelection();
			tiposIdent.clear();
			String codPais = "";
			if (StringUtils.isEmpty(tfCodPais.getText())) {
				codPais = sesion.getAplicacion().getTienda().getCliente().getCodpais();
			} else {
				codPais = tfCodPais.getText();
			}
			List<TiposIdentBean> tiposIdentificacion = tiposIdentService.consultarTiposIdent(null, true, codPais);
			if (tiposIdentificacion.isEmpty()) {
				// Añadimos elemento vacío
				tiposIdent.add(new TiposIdentBean());
			} else {
				tiposIdent.addAll(tiposIdentificacion);
			}
		} catch (TiposIdentNotFoundException ex) {
		} catch (TiposIdentServiceException ex) {
			log.error("Error consultando los tipos de identificación.", ex);
			VentanaDialogoComponent.crearVentanaAviso(
					I18N.getTexto("Error consultando los documentos de identificación de la tienda."), this.getStage());
		} catch (Exception ex) {
			log.error("Se produjo un error en el tratamiento de los tipos de identificacion", ex);
		}
	}
}
