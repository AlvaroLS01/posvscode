package com.comerzzia.bricodepot.pos.gui.mantenimientos;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.api.model.loyalty.ColectivosFidelizadoBean;
import com.comerzzia.api.model.loyalty.FidelizadoBean;
import com.comerzzia.api.model.loyalty.TiposContactoFidelizadoBean;
import com.comerzzia.api.rest.client.fidelizados.ConsultarFidelizadoRequestRest;
import com.comerzzia.api.rest.client.fidelizados.FidelizadoRequestRest;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.BricodepotVisorPantallaSecundaria;
import com.comerzzia.bricodepot.pos.gui.componentes.VentanaEspera;
import com.comerzzia.bricodepot.pos.gui.mantenimientos.fidelizados.resumen.BricodepotPaneResumenFidelizadoController;
import com.comerzzia.bricodepot.pos.services.fidelizado.BricodepotFidelizadosService;
import com.comerzzia.bricodepot.pos.services.fidelizado.ProcesoValidacionFidelizadoListener;
import com.comerzzia.bricodepot.pos.services.fidelizado.TicketFidelizadoCaptacion;
import com.comerzzia.bricodepot.pos.services.magento.MagentoValidacionService;
import com.comerzzia.bricodepot.pos.util.format.BricoEmailValidator;
import com.comerzzia.core.util.base.Estado;
import com.comerzzia.pos.core.dispositivos.Dispositivos;
import com.comerzzia.pos.core.dispositivos.dispositivo.visor.IVisor;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.RestBackgroundTask;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.permisos.exception.SinPermisosException;
import com.comerzzia.pos.core.gui.validation.ValidationUI;
import com.comerzzia.pos.dispositivo.comun.tarjeta.CodigoTarjetaController;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.ConsultarFidelizadoPorCodTask;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.ConsultarFidelizadoPorIdTask;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.CrearFidelizadoTask;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.EditarFidelizadoTask;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.EstadoCivilGui;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.FidelizadoController;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.FormularioFidelizadoBean;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.SexoGui;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.TipoIdentGui;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.colectivos.ColectivoAyudaGui;
import com.comerzzia.pos.gui.mantenimientos.fidelizados.tiendas.TiendaGui;
import com.comerzzia.pos.gui.ventas.tickets.articulos.FacturacionArticulosController;
import com.comerzzia.pos.services.core.paises.PaisNotFoundException;
import com.comerzzia.pos.services.core.paises.PaisService;
import com.comerzzia.pos.services.core.paises.PaisServiceException;
import com.comerzzia.pos.services.core.sesion.Sesion;
import com.comerzzia.pos.services.core.variables.VariablesServices;
import com.comerzzia.pos.services.devices.DeviceException;
import com.comerzzia.pos.servicios.impresion.ImpresionJasper;
import com.comerzzia.pos.servicios.impresion.ServicioImpresion;
import com.comerzzia.pos.util.config.SpringContext;
import com.comerzzia.pos.util.i18n.I18N;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

@Component
@Primary
public class BricodepotFidelizadoController extends FidelizadoController {

	public static final String EDICION_CAMPOS_OBLIGATORIOS_PENDIENTES = "EDICION_CAMPOS_OBLIGATORIOS_PENDIENTES";
	public static final String EMAIL_NO_REPETIDO = "EMAIL_NO_REPETIDO";
	public static final String DOCUMENTO_NO_REPETIDO = "DOCUMENTO_NO_REPETIDO";

	protected static final Logger log = Logger.getLogger(BricodepotFidelizadoController.class);

	protected IVisor visor;

	@FXML
	protected Button btEnviarVisor;

	@Autowired
	private Sesion sesion;

	@Autowired
	protected BricodepotFidelizadosService bricoFidelizadosService;

	@Autowired
	private PaisService paisService = SpringContext.getBean(PaisService.class);

	@Autowired
	private MagentoValidacionService magentoService;

	/*
	 * BRICO-287 este modo de edición necesita des/habilitar campos dependiendo de lógica y distinta función de botón
	 * cancelar
	 */
	private boolean modoEdicionCamposObligatoriosPendientes = false;
	
	private boolean datosValidadosVisor;

	@Override
	public void initializeComponents() throws InitializeGuiException {
		super.initializeComponents();

		visor = Dispositivos.getInstance().getVisor();
	}

	@Override
	public void initializeForm() throws InitializeGuiException {
		super.initializeForm();

		modoEdicionCamposObligatoriosPendientes = false;
		lbError.setText("");

		String newModo = (String) getDatos().get(CodigoTarjetaController.PARAMETRO_MODO);

		/* BRICO-287 modo edición especial para popup de campos incompletos */
		if (EDICION_CAMPOS_OBLIGATORIOS_PENDIENTES.equals(newModo)) {
			modoEdicionCamposObligatoriosPendientes = true;
			try {
				super.compruebaPermisos(PERMISO_EDITAR);
			}
			catch (SinPermisosException ex) {
				log.error(ex.getMessage(), ex);
				throw new InitializeGuiException(ex.getMessage());
			}

			setModo("EDICION");
			lblCampoObligatorios.setVisible(true);

			tabResumen.setDisable(true);
			tabColectivos.setDisable(false);
			tabMovimientosTarjetas.setDisable(true);
			tabUltimasVentas.setDisable(true);
			tabEtiquetas.setDisable(true);

			Long idFidelizado = (Long) getDatos().get(CodigoTarjetaController.PARAMETRO_ID_FIDELIZADO);
			String numeroTarjeta = (String) getDatos().get(FacturacionArticulosController.PARAMETRO_NUMERO_TARJETA);
			numeroTarjetaFidelizado = numeroTarjeta;
			cargarFidelizadoEdicionCamposObligatoriosPendientes(idFidelizado);

		}

		comportamientoBotonEnviarVisor();
	}

	private void comportamientoBotonEnviarVisor() {
		log.debug("comportamientoBotonEnviarVisor()");
		if (visor instanceof BricodepotVisorPantallaSecundaria) {
			btImprimir.setVisible(false);
			btImprimir.managedProperty().bind(btImprimir.visibleProperty());
			btImprimir.managedProperty().unbind();

			btAceptar.setVisible(false);
			btAceptar.managedProperty().bind(btAceptar.visibleProperty());
			btAceptar.managedProperty().unbind();

			if ("INSERCION".equals(getModo()) || "EDICION".equals(getModo())) {
				log.debug("comportamientoBotonEnviarVisor() - Boton enviarVisor visible");
				btEnviarVisor.setVisible(true);
				btEnviarVisor.managedProperty().bind(btEnviarVisor.visibleProperty());
				btEnviarVisor.managedProperty().unbind();
			}
			else {
				log.debug("comportamientoBotonEnviarVisor() - Boton enviarVisor NO visible");
				btEnviarVisor.setVisible(false);
				btEnviarVisor.managedProperty().bind(btEnviarVisor.visibleProperty());
				btEnviarVisor.managedProperty().unbind();
			}
		}
		else {
			log.debug("comportamientoBotonEnviarVisor() - Boton enviarVisor NO visible");
			btEnviarVisor.setVisible(false);
			btEnviarVisor.managedProperty().bind(btEnviarVisor.visibleProperty());
			btEnviarVisor.managedProperty().unbind();
		}
	}

	@Override
	public void accionEditar() { /* Brico 336 no aparecia tabColectivos en editar */
		log.debug("accionEditar()");

		setModo("EDICION");
		comportamientoBotonEnviarVisor();

		// btAceptar.setVisible(true);
		// btAceptar.setManaged(true);
		btCancelar.setVisible(true);
		btCancelar.setManaged(true);
		btEditar.setVisible(false);
		btEditar.setManaged(false);
		btCerrar.setVisible(false);
		btCerrar.setManaged(false);
		tabResumen.setDisable(true);
		tabColectivos.setDisable(false);
		tabMovimientosTarjetas.setDisable(true);
		tabUltimasVentas.setDisable(true);
		tabEtiquetas.setDisable(true);
		lblCampoObligatorios.setVisible(true);
		tpDatosFidelizado.getSelectionModel().select(tabDatosGenerales);
		paneObservacionesController.selected();
		paneDatosGeneralesController.selected();
	}

	public boolean isModoEdicionCamposObligatoriosPendientes() {
		return modoEdicionCamposObligatoriosPendientes;
	}

	public void setModoEdicionCamposObligatoriosPendientes(boolean modoEdicionCamposObligatoriosPendientes) {
		this.modoEdicionCamposObligatoriosPendientes = modoEdicionCamposObligatoriosPendientes;
	}

	@FXML
	public void accionEnviarVisor() {
		log.debug("accionEnviarVisor()");
		if (validarDatos()) {
			/* CREAMOS EL DIÁLOGO DE CARGA PERSONALIZADO, ESTO DEBERÍA HACERSE UNA SOLA VEZ EN EL POSAPPLICATION */
			VentanaEspera.crearVentanaCargando(getStage());
			VentanaEspera.setMensaje("Pendiente Validación del Cliente");
			VentanaEspera.mostrar();

			((BricodepotVisorPantallaSecundaria) visor).modoInfoFidelizado(paneDatosGeneralesController);

			((BricodepotVisorPantallaSecundaria) visor).setListenerFidelizadoOK(new ProcesoValidacionFidelizadoListener(){

				@Override
				public void procesoValidacionOK() {
					log.debug("procesoValidacionOK() - Se ha terminado el proceos de fidelizacion por la parte del visor. Se procede a realizar el alta del fidelizado.");
					VentanaEspera.cerrar();
					
					datosValidadosVisor = true;
					
					accionAceptar();
				}
			});
		}
	}

	@FXML
	public void accionImprimir() {
		log.debug("accionImprimir()");
		List<FidelizadoBean> fidelizados = new ArrayList<FidelizadoBean>();
		HashMap<String, Object> parametros = new HashMap<String, Object>();

		// Se edita temporalmente el domicilio y documento del fidelizado para la impresión por si fuera necesario
		// ocultarlo
		String domicilioCopia = fidelizado.getDomicilio();
		String documentoCopia = fidelizado.getDocumento();
		fidelizado.setDomicilio(imprimeDatoSensible(fidelizado.getDomicilio()));
		fidelizado.setDocumento(imprimeDatoSensible(fidelizado.getDocumento()));

		fidelizados.add(fidelizado);
		parametros.put(ImpresionJasper.LISTA, fidelizados);
		if (fidelizado != null) {
			if (fidelizado.getTipoContacto("MOVIL") != null) {
				parametros.put("MOVIL", imprimeDatoSensible(fidelizado.getTipoContacto("MOVIL").getValor()));
				parametros.put("MOVIL_NOTIF", fidelizado.getTipoContacto("MOVIL").getRecibeNotificaciones() ? I18N.getTexto("Si") : I18N.getTexto("No"));
			}
			else {
				parametros.put("MOVIL", "");
				parametros.put("MOVIL_NOTIF", "-");
			}
			if (fidelizado.getTipoContacto("EMAIL") != null) {
				parametros.put("EMAIL", imprimeDatoSensible(fidelizado.getTipoContacto("EMAIL").getValor()));
				parametros.put("EMAIL_NOTIF", fidelizado.getTipoContacto("EMAIL").getRecibeNotificaciones() ? I18N.getTexto("Si") : I18N.getTexto("No"));
			}
			else {
				parametros.put("EMAIL", "");
				parametros.put("EMAIL_NOTIF", "-");
			}

		}
		else { // Para que no aparezca 'null' en 'Permite notificaciones'
			parametros.put("MOVIL_NOTIF", "");
			parametros.put("EMAIL_NOTIF", "");
		}
		parametros.put("DESEMP", sesion.getAplicacion().getEmpresa().getDesEmpresa());
		parametros.put("DOMICILIO", sesion.getAplicacion().getEmpresa().getDomicilio());
		parametros.put("CP", sesion.getAplicacion().getEmpresa().getCp());
		parametros.put("PROVINCIA", sesion.getAplicacion().getEmpresa().getProvincia());

		if (sesion.getAplicacion().getEmpresa().getLogotipo() != null) {
			ByteArrayInputStream bis = new ByteArrayInputStream(sesion.getAplicacion().getEmpresa().getLogotipo());
			parametros.put("LOGO", bis);
		}

		/* Se pasa por parametro la imagen de la firma */
		if (BricodepotVisorPantallaSecundaria.getFirma() != null) {
			ByteArrayInputStream bis = new ByteArrayInputStream(BricodepotVisorPantallaSecundaria.getFirma());
			parametros.put("FIRMA", bis);
		}
		try {
			ServicioImpresion.imprimir("jasper/fidelizados/formulariofidelizado", parametros);
		}
		catch (DeviceException e) {
			log.error("Ha ocurrido un error al imprimir el informe ", e);
		}
		finally {
			fidelizado.setDomicilio(domicilioCopia);
			fidelizado.setDocumento(documentoCopia);
		}
	}

	public void crearFidelizado() {
		log.debug("crearFidelizado()");
		String apiKey = variablesService.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
		String uidActividad = sesion.getAplicacion().getUidActividad();
		FidelizadoRequestRest insertFidelizado = new FidelizadoRequestRest(apiKey, uidActividad, getDatosFidelizado("INSERCION"), sesion.getAplicacion().getEmpresa().getCodEmpresa(),
		        sesion.getAplicacion().getCodAlmacen());
		insertFidelizado.setTipoNotificacion("NUEVO_USUARIO_FIDELIZADO");

		log.debug("crearFidelizado() - Se realiza llamada a backoffice para hacer la insercion de fidelizado");
		CrearFidelizadoTask insertFidelizadoTask = SpringContext.getBean(CrearFidelizadoTask.class, insertFidelizado, new RestBackgroundTask.FailedCallback<FidelizadoBean>(){

			@Override
			public void succeeded(FidelizadoBean result) {
				log.debug("crearFidelizado()/succeeded()");

				fidelizado = result;
				tabResumen.setDisable(false);
				tabMovimientosTarjetas.setDisable(!verMovTarjetas);
				tabColectivos.setDisable(!verColectivos);
				tabUltimasVentas.setDisable(!verUltVentas);
				tabEtiquetas.setDisable(!verEtiquetas);
				paneResumenFidelizadoController.selected();
				tpDatosFidelizado.getSelectionModel().select(tabResumen);
				btAceptar.setVisible(false);
				btAceptar.setManaged(false);
				btCancelar.setVisible(false);
				btCancelar.setManaged(false);
				btEditar.setVisible(true);
				btEditar.setManaged(true);
				btImprimir.setVisible(true);
				btImprimir.setManaged(true);
				btCerrar.setVisible(true);
				btCerrar.setManaged(true);
				lblCampoObligatorios.setVisible(false);

				btEnviarVisor.setVisible(false);
				btEnviarVisor.managedProperty().bind(btEnviarVisor.visibleProperty());
				btEnviarVisor.managedProperty().unbind();
				setModo("CONSULTA");
				VentanaDialogoComponent.crearVentanaInfo(I18N.getTexto("Fidelizado creado correctamente"), getStage());

				asignarFidelizadoAVenta();

				TicketFidelizadoCaptacion ticketFidelizado = new TicketFidelizadoCaptacion();
				ticketFidelizado.setPdfFidelizado(BricodepotVisorPantallaSecundaria.getFirma());
				ticketFidelizado.setIdFidelizado(fidelizado.getIdFidelizado());
				bricoFidelizadosService.registrarTicketFidelizado(ticketFidelizado, fidelizado);
				
				
				accionImprimir();
				bricoFidelizadosService.registrarEnlacesColectivoFidelizado(fidelizado);
			}
			@Override
			public void failed(Throwable throwable) {
				getApplication().getMainView().close();
			}
		}, getStage());
		insertFidelizadoTask.start();
	}

	

	@Override
	public void editarFidelizado() {
		log.debug("editarFidelizado() - Inicio del proceso de edicion de fidelizado");

		String apiKey = variablesService.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
		String uidActividad = sesion.getAplicacion().getUidActividad();
		fidelizado = getDatosFidelizado("EDICION");
		FidelizadoRequestRest insertFidelizado = new FidelizadoRequestRest(apiKey, uidActividad, fidelizado);
		if (fidelizado.getContactos() != null && !fidelizado.getContactos().isEmpty()) {
			Map<String, Integer> mapaEstadosTiposContacto = new HashMap<String, Integer>();
			for (TiposContactoFidelizadoBean tipoContacto : fidelizado.getContactos()) {
				if (tipoContacto.getEstadoBean() != Estado.SIN_MODIFICAR) {
					mapaEstadosTiposContacto.put(tipoContacto.getCodTipoCon(), tipoContacto.getEstadoBean());
				}
			}
			insertFidelizado.setMapaEstadosTiposContacto(mapaEstadosTiposContacto);
		}
		if (fidelizado.getCodAlm() != null) {
			insertFidelizado.setCodAlmFav(fidelizado.getCodAlm());
		}
		EditarFidelizadoTask insertFidelizadoTask = SpringContext.getBean(EditarFidelizadoTask.class, insertFidelizado, new RestBackgroundTask.FailedCallback<FidelizadoBean>(){

			@Override
			public void succeeded(FidelizadoBean result) {
				fidelizado = result;
				actualizaFidelizado();
				tabResumen.setDisable(false);
				tabMovimientosTarjetas.setDisable(!verMovTarjetas);
				tabColectivos.setDisable(!verColectivos);
				tabUltimasVentas.setDisable(!verUltVentas);
				tabEtiquetas.setDisable(!verEtiquetas);
				paneResumenFidelizadoController.setFidelizado(null);
				paneResumenFidelizadoController.selected();
				tpDatosFidelizado.getSelectionModel().select(tabResumen);
				btAceptar.setVisible(false);
				btAceptar.setManaged(false);
				btCancelar.setVisible(false);
				btCancelar.setManaged(false);
				btEditar.setVisible(true);
				btEditar.setManaged(true);
				btCerrar.setVisible(true);
				btCerrar.setManaged(true);
				btEnviarVisor.setVisible(false); // BRICO-287
				btEnviarVisor.setManaged(false); // BRICO-287
				lblCampoObligatorios.setVisible(false);
				VentanaDialogoComponent.crearVentanaInfo(I18N.getTexto("Fidelizado editado correctamente"), getStage());

				/* BRICO-287 */

				TicketFidelizadoCaptacion ticketFidelizado = new TicketFidelizadoCaptacion();
				ticketFidelizado.setPdfFidelizado(BricodepotVisorPantallaSecundaria.getFirma());
				ticketFidelizado.setIdFidelizado(fidelizado.getIdFidelizado());
				bricoFidelizadosService.registrarTicketFidelizado(ticketFidelizado, fidelizado);

				accionImprimir();
				bricoFidelizadosService.registrarEnlacesColectivoFidelizado(fidelizado);
				setModo("CONSULTA");
				/* BRICO-287 */
			}

			@Override
			public void failed(Throwable throwable) {
				getApplication().getMainView().close();
			}
		}, getStage());
		insertFidelizadoTask.start();
	}
	
	@Override
	public boolean validarDatos() {
		boolean valido;

		// Limpiamos los errores que pudiese tener el formulario
		formFidelizado.clearErrorStyle();

		formFidelizado.setApellidos(paneDatosGeneralesController.getTfApellidos().getText());
		formFidelizado.setCodColectivo(paneDatosGeneralesController.getTfCodColectivo().getText());
		formFidelizado.setCodigo(paneDatosGeneralesController.getTfCodigo().getText());
		formFidelizado.setCp(paneDatosGeneralesController.getTfCodPostal().getText());
		formFidelizado.setDocumento(paneDatosGeneralesController.getTfDocumento().getText());
		formFidelizado.setDomicilio(paneDatosGeneralesController.getTfDomicilio().getText());
		formFidelizado.setEmail(paneDatosGeneralesController.getTfEmail().getText());
		formFidelizado.setFechaNacimiento(paneDatosGeneralesController.getDpFechaNacimiento().getSelectedDate());
		formFidelizado.setMovil(paneDatosGeneralesController.getTfMovil().getText());
		formFidelizado.setNombre(paneDatosGeneralesController.getTfNombre().getText());
		formFidelizado.setNumeroTarjeta(paneDatosGeneralesController.getTfNumeroTarjeta().getText());
		formFidelizado.setPoblacion(paneDatosGeneralesController.getTfPoblacion().getText());
		formFidelizado.setLocalidad(paneDatosGeneralesController.getTfLocalidad().getText());
		formFidelizado.setProvincia(paneDatosGeneralesController.getTfProvincia().getText());
		formFidelizado.setObservaciones(paneObservacionesController.getTaObservaciones().getText());
		TipoIdentGui tipoIdent = paneDatosGeneralesController.getCbTipoDocumento().getSelectionModel().getSelectedItem();
		if (tipoIdent == null) {
			formFidelizado.setTipoDocumento("");
		}
		else {
			formFidelizado.setTipoDocumento(tipoIdent.getCodigo());
		}

		SexoGui sexo = paneDatosGeneralesController.getCbSexo().getSelectionModel().getSelectedItem();
		if (sexo == null) {
			formFidelizado.setSexo("");
		}
		else {
			formFidelizado.setSexo(sexo.getCodigo());
		}

		EstadoCivilGui estCivil = paneDatosGeneralesController.getCbEstadoCivil().getSelectionModel().getSelectedItem();
		if (estCivil == null) {
			formFidelizado.setEstadoCivil("");
		}
		else {
			formFidelizado.setEstadoCivil(estCivil.getCodEstadoCivil());
		}

		try {
			paisService.consultarCodPais(paneDatosGeneralesController.getTfCodPais().getText());
		}
		catch (PaisNotFoundException e) {
			PathImpl path = PathImpl.createPathFromString("codPais");
			formFidelizado.setFocus(path);
			formFidelizado.setErrorStyle(path, true);
			return false;
		}
		catch (PaisServiceException e) {
			log.debug("validarDatos() - Ha habido un error al buscar el país con código " + paneDatosGeneralesController.getTfCodPais().getText() + ": " + e.getMessage());
			return false;
		}

		log.debug("validarDatos() - INICIO validacion de colectivos");
		String codcolectivo = paneDatosGeneralesController.getTfCodColectivo().getText();
		log.debug("validarDatos() - Codigo colectivo en pantalla" + codcolectivo);
		String codtiendafavorita = paneDatosGeneralesController.getTfCodTienda().getText();
		if (codcolectivo != null && !"".equals(codcolectivo)) {
			boolean colectivoValido = false;
			for (ColectivoAyudaGui colectivo : todosColectivos) {
				log.debug("validarDatos() - Codigo Colectivo en LISTA " + colectivo.getCodColectivo());
				if (codcolectivo.equals(colectivo.getCodColectivo())) {
					colectivoValido = true;
					break;
				}
			}
			if (!colectivoValido) {
				PathImpl path = PathImpl.createPathFromString("codColectivo");
				formFidelizado.setFocus(path);
				formFidelizado.setErrorStyle(path, true);
				return false;
			}
		}
		log.debug("validarDatos() - FIN validacion de colectivos");
		
		log.debug("validarDatos() - INICIO validacion de tiendas");
		if (codtiendafavorita != null && !"".equals(codtiendafavorita)) {
			boolean tiendaValido = false;
			for (TiendaGui tienda : todasTiendas) {
				if (codtiendafavorita.equals(tienda.getCodTienda())) {
					tiendaValido = true;
					break;
				}
			}
			if (!tiendaValido) {
				PathImpl path = PathImpl.createPathFromString("codAlmFav");
				formFidelizado.setFocus(path);
				formFidelizado.setErrorStyle(path, true);
				return false;
			}
		}
		log.debug("validarDatos() - FIN validacion de tiendas");
		
		// Limpiamos el posible error anterior
		lbError.setText("");

		Set<ConstraintViolation<FormularioFidelizadoBean>> constraintViolations = ValidationUI.getInstance().getValidator().validate(formFidelizado);
		constraintViolations.removeIf(v -> "email".equals(v.getPropertyPath().toString()));
		
		/*
		 * BRICO-146 Hacer obligatorios: CP y Colectivo Quitar obligatorios: provincia, poblacion, domicilio
		 */

		// Limpiar estilo error de nuevos campos obligatorios
		paneDatosGeneralesController.getTfCodPostal().setStyle("-fx-background-color: #f4f4f4;");
		paneDatosGeneralesController.getTfCodColectivo().setStyle("-fx-background-color: #f4f4f4;");
		paneDatosGeneralesController.getTfDesColectivo().setStyle("-fx-background-color: #f4f4f4;");

		// Quitar validaciones innecesarias (no se puede personalizar validador)
		if (!constraintViolations.isEmpty()) {
			List<String> camposQuitarObligatorio = Arrays.asList("provincia", "poblacion", "domicilio");
			for (Iterator<ConstraintViolation<FormularioFidelizadoBean>> i = constraintViolations.iterator(); i.hasNext();) {
				ConstraintViolation<FormularioFidelizadoBean> element = i.next();
				if (camposQuitarObligatorio.contains(element.getPropertyPath().toString())
				        && element.getConstraintDescriptor().getAnnotation() instanceof org.hibernate.validator.constraints.NotEmpty) {
					i.remove();
				}
			}

		}
		/* fin BRICO-146 */
		if (constraintViolations.size() >= 1) {
			ConstraintViolation<FormularioFidelizadoBean> next = constraintViolations.iterator().next();
			formFidelizado.setErrorStyle(next.getPropertyPath(), true);
			formFidelizado.setFocus(next.getPropertyPath());
			lbError.setText(next.getMessage());
			valido = false;
		}
		else {
			valido = true;
		}

		/* BRICO-146 */
		if (valido) {
			// Si el formulario es válido, se comprueban los nuevos campos que tienen que ser obligatorios
			log.debug("validarDatos() - INICIO validacion personalizada CP " + paneDatosGeneralesController.getTfCodPostal().getText());
			if (StringUtils.isBlank(paneDatosGeneralesController.getTfCodPostal().getText())) {
				paneDatosGeneralesController.getTfCodPostal().requestFocus();
				paneDatosGeneralesController.getTfCodPostal().setStyle("-fx-background-color: #ffbbbb;");
				lbError.setText("Debe rellenar el campo código postal");
				valido = false;
			}
			// BRICO-280 solo se pide colectivo en inserción (en edición no aparece el campo)
			else if (("INSERCION".equals(getModo()) || isModoEdicionCamposObligatoriosPendientes()) && StringUtils.isBlank(paneDatosGeneralesController.getTfCodColectivo().getText())) {
				log.debug("validarDatos() - INICIO validacion personalizada Colectivo " + paneDatosGeneralesController.getTfCodColectivo().getText());
				paneDatosGeneralesController.getTfCodColectivo().requestFocus();
				paneDatosGeneralesController.getTfCodColectivo().setStyle("-fx-background-color: #ffbbbb;");
				paneDatosGeneralesController.getTfDesColectivo().setStyle("-fx-background-color: #ffbbbb;");
				lbError.setText("Debe rellenar el campo colectivo");
				valido = false;
			}
		}
		/* fin BRICO-146 */

		// ini BRICO-266
		if (valido) {
			valido = validaDatosBrico();
		}

		return valido;
	}

	private boolean validaDatosBrico() {
		log.debug("validaDatosBrico() - Se validaran el email, documento y movil");
		boolean valido = false;
		
		valido = validaDocumento();
		if (valido) {
			valido = validaEmail();
		}
		if (valido) {
			valido = validaMovil();
		}
		return valido;
	}
	
	private boolean validaEmail() {
		log.debug("validaEmail() - Inicio de la validacion de Email");

		String emailIntroducido = paneDatosGeneralesController.getTfEmail().getText().trim();
		log.debug("validaEmail() - Validando Email: " + emailIntroducido);

		String errorMsg = BricoEmailValidator.getValidationErrorKey(emailIntroducido);
		if (errorMsg != null) {
			setErrorCampo(paneDatosGeneralesController.getTfEmail(), errorMsg);
			return false;
		}

		String msgResultado = bricoFidelizadosService.compruebaEmailNoRepetido(emailIntroducido, numeroTarjetaFidelizado);
		if (!EMAIL_NO_REPETIDO.equals(msgResultado)) {
			setErrorCampo(paneDatosGeneralesController.getTfEmail(), msgResultado);
			return false;
		}

		log.debug("validaEmail() - Email válido");
		paneDatosGeneralesController.getTfEmail().getStyleClass().remove("error-formulario");
		return true;
	}

	private boolean validaDocumento() {
		String documentoIntroducido = paneDatosGeneralesController.getTfDocumento().getText().trim();
		log.debug("validaDocumento() - Validando documento: " + documentoIntroducido);

		String msgResultado = bricoFidelizadosService.compruebaDocumentoNoRepetido(documentoIntroducido, numeroTarjetaFidelizado);
		if (!msgResultado.equals(DOCUMENTO_NO_REPETIDO)) {
			setErrorCampo(paneDatosGeneralesController.getTfDocumento(), msgResultado);
			return false;
		}

		log.debug("validaDocumento() - Documento válido");
		paneDatosGeneralesController.getTfDocumento().getStyleClass().remove("error-formulario");
		return true;

	}

	private boolean validaMovil() {
		log.debug("validaMovil()");
		String movilIntroducido = paneDatosGeneralesController.getTfMovil().getText().trim();
		if (StringUtils.isEmpty(movilIntroducido)) {
			return true; // Si el móvil no está relleno, no es necesario validar formato porque no es campo obligatorio.
		}

		boolean formatoMovilValido = magentoService.validaFormatoMovil(movilIntroducido);
		if (!formatoMovilValido) {
			setErrorCampo(paneDatosGeneralesController.getTfMovil(), I18N.getTexto("El formato de móvil no es correcto"));
			return false;
		}

		log.debug("validaMovil() - Formato móvil correcto");
		paneDatosGeneralesController.getTfMovil().getStyleClass().remove("error-formulario");
		return true;
	}

	private void setErrorCampo(TextField campo, String errorMsg) {
		campo.getStyleClass().add("error-formulario");
		campo.requestFocus();
		lbError.setText(errorMsg);
	}

	@Override
	protected FidelizadoBean getDatosFidelizado(String modo) { // BRICO-290
		log.debug("getDatosFidelizado()");
		FidelizadoBean datosFidelizado = super.getDatosFidelizado(modo);
		datosFidelizado.setDocumento(datosFidelizado.getDocumento().toUpperCase());

		if (datosFidelizado.getContactos() != null) {
			if (datosFidelizado.getTipoContacto("EMAIL") != null) {
				datosFidelizado.getTipoContacto("EMAIL").setRecibeNotificacionesCom(true);
				datosFidelizado.getTipoContacto("EMAIL").setEstadoBean(Estado.MODIFICADO);
				log.debug("getDatosFidelizado() - Marcando notificaciones comerciales para el email: " + datosFidelizado.getTipoContacto("EMAIL").getValor() + " del fidelizado con id: "
				        + datosFidelizado.getIdFidelizado());
			}
			if (datosFidelizado.getTipoContacto("MOVIL") != null) {
				datosFidelizado.getTipoContacto("MOVIL").setRecibeNotificacionesCom(true);
				log.debug("getDatosFidelizado() - Marcando notificaciones comerciales para el movil: " + datosFidelizado.getTipoContacto("MOVIL").getValor() + " del fidelizado con id: "
				        + datosFidelizado.getIdFidelizado());
			}
		}
		if ("EDICION".equals(modo)) { /* Modo EDICION */
			String codcolectivo = !"".equals(paneDatosGeneralesController.getTfCodColectivo().getText()) ? paneDatosGeneralesController.getTfCodColectivo().getText() : null;
			if (codcolectivo != null) {
				ColectivosFidelizadoBean colectivo = new ColectivosFidelizadoBean();
				colectivo.setCodColectivo(codcolectivo);
				if (datosFidelizado.getColectivos().isEmpty() || !datosFidelizado.getColectivos().contains(colectivo)) {
					datosFidelizado.getColectivos().add(colectivo);
				}
			}
		}
		return datosFidelizado;
	}

	/* BRICO-287 Método copiado de clase padre y adaptado por problemas de race condition */
	protected void cargarFidelizadoEdicionCamposObligatoriosPendientes(Long idFidelizado) {
		log.debug("cargarFidelizadoEdicionCamposObligatoriosPendientes()");
		String apiKey = variablesService.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
		String uidActividad = sesion.getAplicacion().getUidActividad();
		FidelizadoRequestRest consulta = new FidelizadoRequestRest(apiKey, uidActividad, idFidelizado);
		consulta.setLanguageCode(sesion.getAplicacion().getStoreLanguageCode());
		ConsultarFidelizadoPorIdTask consultarFidelizadoTask = SpringContext.getBean(ConsultarFidelizadoPorIdTask.class, consulta, new RestBackgroundTask.FailedCallback<FidelizadoBean>(){

			@Override
			public void succeeded(FidelizadoBean result) {
				fidelizado = result;

				tpDatosFidelizado.getSelectionModel().select(0);
				paneResumenFidelizadoController.setFidelizado(null);
				paneResumenFidelizadoController.selected();

				btCancelar.setVisible(false);
				btCancelar.setManaged(false);
				btCerrar.setVisible(true);
				btCerrar.setManaged(true);
				btEditar.setVisible(false);
				btEditar.setManaged(false);
				btImprimir.setVisible(true);
				btImprimir.setManaged(true);

				tpDatosFidelizado.getSelectionModel().select(tabDatosGenerales);
				paneObservacionesController.selected();
				paneDatosGeneralesController.selected();

			}

			@Override
			public void failed(Throwable throwable) {
				getApplication().getMainView().close();
			}
		}, getStage());
		consultarFidelizadoTask.start();
	}
	
	@FXML
	public void accionAceptar() {
		log.debug("accionAceptar()");
		if ("INSERCION".equals(getModo())) {
			if (datosValidadosVisor || validarDatos()) {
				String codigo = paneDatosGeneralesController.getTfCodigo().getText();
				if (StringUtils.isNotBlank(codigo)) {
					String apiKey = variablesService.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
					String uidActividad = sesion.getAplicacion().getUidActividad();
					ConsultarFidelizadoRequestRest consultarFidelizado = new ConsultarFidelizadoRequestRest(apiKey, uidActividad);
					consultarFidelizado.setCodFidelizado(codigo);

					ConsultarFidelizadoPorCodTask consultarFidelizadoTask = SpringContext.getBean(ConsultarFidelizadoPorCodTask.class, consultarFidelizado,
					        new RestBackgroundTask.FailedCallback<Boolean>(){

						        @Override
						        public void succeeded(Boolean result) {
							        if (result) {
								        VentanaDialogoComponent.crearVentanaError(I18N.getTexto("El código de fidelizado indicado ya existe"), getStage());
							        }
							        else {
								        String numeroTarjeta = paneDatosGeneralesController.getTfNumeroTarjeta().getText();
								        if (numeroTarjeta != null && !numeroTarjeta.isEmpty()) {
									        validarNumeroTarjeta(numeroTarjeta);
								        }
								        else {
									        crearFidelizado();
								        }
							        }

						        }

						        @Override
						        public void failed(Throwable throwable) {
							        getApplication().getMainView().close();
						        }
					        }, getStage());
					consultarFidelizadoTask.start();
				}
				else {
					String numeroTarjeta = paneDatosGeneralesController.getTfNumeroTarjeta().getText();
					if (numeroTarjeta != null && !numeroTarjeta.isEmpty()) {
						validarNumeroTarjeta(numeroTarjeta);
					}
					else {
						crearFidelizado();
					}
				}

			}
		}
		else if ("EDICION".equals(getModo())) {
			if (validarDatos()) {
				editarFidelizado();
			}
		}

	}
	
	@Override
	public void accionCancelar() {
		super.accionCancelar();
		
		btEnviarVisor.setVisible(false);
	}

	@Override
	public void accionCerrar() {
		((BricodepotPaneResumenFidelizadoController) paneResumenFidelizadoController).closeAllTasks();

		super.accionCerrar();
	}
}