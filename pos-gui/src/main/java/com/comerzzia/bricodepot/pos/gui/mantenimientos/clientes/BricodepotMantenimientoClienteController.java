package com.comerzzia.bricodepot.pos.gui.mantenimientos.clientes;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.ws.rs.ClientErrorException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.comerzzia.api.rest.client.exceptions.RestException;
import com.comerzzia.api.rest.client.exceptions.RestHttpException;
import com.comerzzia.bricodepot.pos.devices.visor.pantallasecundaria.gui.BricodepotVisorPantallaSecundaria;
import com.comerzzia.bricodepot.pos.gui.componentes.VentanaEspera;
import com.comerzzia.bricodepot.pos.services.cliente.BricodepotClientesService;
import com.comerzzia.bricodepot.pos.services.cliente.ProcesoValidacionClienteListener;
import com.comerzzia.bricodepot.pos.services.cliente.TicketClienteCaptacion;
import com.comerzzia.bricodepot.pos.util.format.BricoEmailValidator;
import com.comerzzia.core.util.base.Estado;
import com.comerzzia.pos.core.dispositivos.Dispositivos;
import com.comerzzia.pos.core.dispositivos.dispositivo.visor.IVisor;
import com.comerzzia.pos.core.gui.InitializeGuiException;
import com.comerzzia.pos.core.gui.componentes.botonaccion.simple.BotonBotoneraSimpleComponent;
import com.comerzzia.pos.core.gui.componentes.botonera.BotoneraComponent;
import com.comerzzia.pos.core.gui.componentes.botonera.ConfiguracionBotonBean;
import com.comerzzia.pos.core.gui.componentes.dialogos.VentanaDialogoComponent;
import com.comerzzia.pos.core.gui.exception.CargarPantallaException;
import com.comerzzia.pos.core.gui.validation.ValidationUI;
import com.comerzzia.pos.core.gui.view.View;
import com.comerzzia.pos.dispositivo.visor.VisorPantallaSecundaria;
import com.comerzzia.pos.gui.mantenimientos.clientes.FormularioMantenimientoClientesBean;
import com.comerzzia.pos.gui.mantenimientos.clientes.MantenimientoClienteController;
import com.comerzzia.pos.gui.ventas.identificada.cliente.IdentificacionClienteView;
import com.comerzzia.pos.persistence.clientes.ClienteBean;
import com.comerzzia.pos.persistence.core.impuestos.tratamientos.TratamientoImpuestoBean;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import rest.bean.cliente.BricodepotClienteBean;
import rest.client.clientes.BricodepotClienteRequestRest;
import rest.client.clientes.BricodepotClientesRest;

@Component
@Primary
public class BricodepotMantenimientoClienteController extends MantenimientoClienteController {
	
	private static Logger log = Logger.getLogger(BricodepotMantenimientoClienteController.class);
	
	@Autowired
	protected BricodepotClientesService bricodepotClientesService;
	
	@FXML
	protected Button btCancelar, btEnviarVisor, btAceptar;
	
	@Autowired
	private VariablesServices variablesServices;
	@Autowired
	protected PaisService paisService = SpringContext.getBean(PaisService.class);
	
	@Autowired
	protected Sesion sesion;
	
	protected IVisor visor;
	
	public TextField getTfRazonSocial() {
		return tfRazonSocial;
	}

	public TextField getTfDomicilio() {
		return tfDomicilio;
	}

	public TextField getTfPoblacion() {
		return tfPoblacion;
	}

	public TextField getTfCP() {
		return tfCP;
	}

	public TextField getTfProvincia() {
		return tfProvincia;
	}

	public TextField getTfTelefono() {
		return tfTelefono;
	}

	public TextField getTfTelefono2() {
		return tfTelefono2;
	}

	public TextField getTfDesPais() {
		return tfDesPais;
	}

	public TextField getTfCodPais() {
		return tfCodPais;
	}

	public TextField getTfNumDocIdent() {
		return tfNumDocIdent;
	}

	public TextField getTfLocalidad() {
		return tfLocalidad;
	}

	public TextField getTfDescripcion() {
		return tfDescripcion;
	}

	public TextField getTfFax() {
		return tfFax;
	}

	public TextField getTfEmail() {
		return tfEmail;
	}

	public ComboBox<TratamientoImpuestoBean> getCbTratamientoImpuestos() {
		return cbTratamientoImpuestos;
	}

	public TextArea getTaObservaciones() {
		return taObservaciones;
	}
	
	@Override
	public void initializeComponents() throws InitializeGuiException {
		super.initializeComponents();

		try {
			panelMenuTabla.getChildren().clear();
			
			List<ConfiguracionBotonBean> listaAccionesAccionesTabla = BotoneraComponent.cargarAccionesTablaSimple();
			listaAccionesAccionesTabla.add(0, new ConfiguracionBotonBean("iconos/back.png", null, null, "VOLVER", "REALIZAR_ACCION"));
			listaAccionesAccionesTabla.add(1, new ConfiguracionBotonBean("iconos/add.png", null, null, "AÑADIR", "REALIZAR_ACCION"));
			listaAccionesAccionesTabla.add(2, new ConfiguracionBotonBean("iconos/edit.png", null, null, "EDITAR", "REALIZAR_ACCION"));
			listaAccionesAccionesTabla.add(3, new ConfiguracionBotonBean("iconos/delete.png", null, null, "ELIMINAR", "REALIZAR_ACCION"));

			log.debug("inicializarComponentes() - Configurando botonera");
			botoneraAccionesTabla = new BotoneraComponent(4, 1, this, listaAccionesAccionesTabla, panelMenuTabla.getPrefWidth(), panelMenuTabla.getPrefHeight(), BotonBotoneraSimpleComponent.class.getName());
			panelMenuTabla.getChildren().add(botoneraAccionesTabla);
			
			/* BRICO-253 se inhabilita botón eliminar cliente */
			botoneraAccionesTabla.getBotonClave("ELIMINAR").setDisable(true);
		}
		catch (CargarPantallaException ex) {
			log.error("inicializarComponentes() - Error creando botonera. Error : " + ex.getMessage(), ex);
			VentanaDialogoComponent.crearVentanaError(ex.getMessageI18N(), getStage());
		}
		
		visor = Dispositivos.getInstance().getVisor();
		comportamientoBotonEnviarVisor();
	}
	
	private void comportamientoBotonEnviarVisor() {
		log.debug("comportamientoBotonEnviarVisor()");
		if (visor instanceof BricodepotVisorPantallaSecundaria) {
			btAceptar.setVisible(false);
			btAceptar.managedProperty().bind(btAceptar.visibleProperty());
			btAceptar.managedProperty().unbind();

//			if ("INSERCION".equals(getModo()) || "EDICION".equals(getModo())) {
				log.debug("comportamientoBotonEnviarVisor() - Boton enviarVisor visible");
				btEnviarVisor.setVisible(true);
				btEnviarVisor.managedProperty().bind(btEnviarVisor.visibleProperty());
				btEnviarVisor.managedProperty().unbind();
//			}
//			else {
//				log.debug("comportamientoBotonEnviarVisor() - Boton enviarVisor NO visible");
//				btEnviarVisor.setVisible(false);
//				btEnviarVisor.managedProperty().bind(btEnviarVisor.visibleProperty());
//				btEnviarVisor.managedProperty().unbind();
//			}
		}
		else {
			log.debug("comportamientoBotonEnviarVisor() - Boton enviarVisor NO visible");
			btEnviarVisor.setVisible(false);
			btEnviarVisor.managedProperty().bind(btEnviarVisor.visibleProperty());
			btEnviarVisor.managedProperty().unbind();
		}
	}
	
	@Override
	protected void setModoEdicion(boolean modoEdicion){
		this.modoEdicion = modoEdicion;
		
		tfNumDocIdent.setEditable(modoEdicion);
		tfRazonSocial.setEditable(modoEdicion);
		tfProvincia.setEditable(modoEdicion);
		tfDomicilio.setEditable(modoEdicion);
		tfLocalidad.setEditable(modoEdicion);
		tfTelefono.setEditable(modoEdicion);
		tfTelefono2.setEditable(modoEdicion);
		tfFax.setEditable(modoEdicion);
		tfEmail.setEditable(modoEdicion);
		tfDescripcion.setEditable(modoEdicion);
		tfCodPais.setEditable(modoEdicion);
		tfDesPais.setEditable(modoEdicion);
		tfPoblacion.setEditable(modoEdicion);
		tfCP.setEditable(modoEdicion);
		cbTipoDocIdent.setDisable(!modoEdicion);
		cbActivo.setDisable(!modoEdicion);
		taObservaciones.setEditable(modoEdicion);
		cbTratamientoImpuestos.setDisable(!modoEdicion);
		btBuscarCentral.setDisable(!modoEdicion);
		btBuscarPais.setDisable(!modoEdicion);

		tfBanco.setEditable(modoEdicion);
		tfBancoDomicilio.setEditable(modoEdicion);
		tfBancoPoblacion.setEditable(modoEdicion);
		tfBancoCCC.setEditable(modoEdicion);

		botoneraAccionesTabla.getBotonClave("VOLVER").setDisable(modoEdicion);
		botoneraAccionesTabla.getBotonClave("AÑADIR").setDisable(modoEdicion);
		botoneraAccionesTabla.getBotonClave("EDITAR").setDisable(modoEdicion);
		botoneraAccionesTabla.getBotonClave("ACCION_TABLA_PRIMER_REGISTRO").setDisable(modoEdicion);
		botoneraAccionesTabla.getBotonClave("ACCION_TABLA_ANTERIOR_REGISTRO").setDisable(modoEdicion);
		botoneraAccionesTabla.getBotonClave("ACCION_TABLA_SIGUIENTE_REGISTRO").setDisable(modoEdicion);
		botoneraAccionesTabla.getBotonClave("ACCION_TABLA_ULTIMO_REGISTRO").setDisable(modoEdicion);
		
		btCancelar.setDisable(!modoEdicion);
		btEnviarVisor.setDisable(!modoEdicion);
		btAceptar.setDisable(!modoEdicion);
		
		/*BRICO-253 selecciona Nacional por defecto al editar */
		if(modoEdicion) {
 			for(TratamientoImpuestoBean trat : cbTratamientoImpuestos.getItems()) {
 				if(trat!=null && "Nacional".equals(trat.getDestratimp())) {
 					cbTratamientoImpuestos.getSelectionModel().select(trat);
 					break;
 				}
 				
 			}
			
		}
		
	}
	
	@Override
	public boolean validarDatos(){
		log.debug("validarFormularioDatosFactura()");

		boolean valido;
		
		// Limpiamos los errores que pudiese tener el formulario
		frBusqCentral.clearErrorStyle();
		frDatosCliente.clearErrorStyle();
		
		frDatosCliente.setDomicilio(tfDomicilio.getText());
		frDatosCliente.setcPostal(tfCP.getText());
		frDatosCliente.setNumDocIdent(tfNumDocIdent.getText());
		frDatosCliente.setProvincia(tfProvincia.getText());
		frDatosCliente.setRazonSocial(tfRazonSocial.getText());
		frDatosCliente.setPoblacion(tfPoblacion.getText());
		frDatosCliente.setLocalidad(tfLocalidad.getText());
		frDatosCliente.setPais(tfCodPais.getText());
		frDatosCliente.setTelefono(tfTelefono.getText());
		frDatosCliente.setTelefono2(tfTelefono2.getText());
		frDatosCliente.setDescripcion(tfDescripcion.getText());
		frDatosCliente.setFax(tfFax.getText());
		frDatosCliente.setEmail(tfEmail.getText());

		frDatosCliente.setBanco(tfBanco.getText());
		frDatosCliente.setBancoDomicilio(tfBancoDomicilio.getText());
		frDatosCliente.setBancoPoblacion(tfBancoPoblacion.getText());
		frDatosCliente.setBancoCCC(tfBancoCCC.getText());
		
		frDatosCliente.setObservaciones(taObservaciones.getText());
		
		try {
            paisService.consultarCodPais(tfCodPais.getText());
        }
        catch (PaisNotFoundException e) {
        	PathImpl path = PathImpl.createPathFromString("pais");
        	frDatosCliente.setFocus(path);
        	frDatosCliente.setErrorStyle(path, true);
        	return false;
        }
        catch (PaisServiceException e) {
        	log.debug("validarDatos() - Ha habido un error al buscar el país con código " + tfCodPais.getText() + ": " + e.getMessage());
        	return false;
        }
		
		TratamientoImpuestoBean tratImpuesto = cbTratamientoImpuestos.getSelectionModel().getSelectedItem();
		if(tratImpuesto == null){
			frDatosCliente.setIdTratImpuesto("");
		}
		else{
			frDatosCliente.setIdTratImpuesto(tratImpuesto.getCodtratimp());
		}
		
		//Limpiamos el posible error anterior
		lbError.setText("");

		// Validamos el formulario de login
		Set<ConstraintViolation<FormularioMantenimientoClientesBean>> constraintViolations = ValidationUI.getInstance().getValidator().validate(frDatosCliente);
		constraintViolations.removeIf(v -> "email".equals(v.getPropertyPath().toString()));

		/* BRICO-253 hacer obligatorios CP y Población */
		tfCP.setStyle("-fx-background-color: #f4f4f4;");
		tfPoblacion.setStyle("-fx-background-color: #f4f4f4;");
		/* fin BRICO-253 */
		
		if (constraintViolations.size() >= 1) {
			ConstraintViolation<FormularioMantenimientoClientesBean> next = constraintViolations.iterator().next();
			Path path = next.getPropertyPath();
			if (path.toString().toLowerCase().contains("banco")) {
				tabPane.getSelectionModel().select(tabBanco);
			} else {
				tabPane.getSelectionModel().select(tabGeneral);
			}
			frDatosCliente.setErrorStyle(path, true);
			frDatosCliente.setFocus(path);
			lbError.setText(next.getMessage());
			valido = false;
		}
		else {
			valido = true;
		}

		if (valido) {
			// Usamos getValidationErrorKey para comprobar local-part ≤64 y dominio ≤255
			if (valido) {
			    String email = tfEmail.getText();
			    String errorKey = BricoEmailValidator.getValidationErrorKey(email);
			    if (errorKey != null) {
			        PathImpl pathEmail = PathImpl.createPathFromString("email");
			        frDatosCliente.setErrorStyle(pathEmail, true);
			        frDatosCliente.setFocus(pathEmail);
			        lbError.setText(errorKey);
			        return false;
			    }
			}
		}

		
		/* BRICO-253 */
		if(valido) {
			//Si el formulario es válido, se comprueban los nuevos campos que tienen que ser obligatorios
			if(StringUtils.isBlank(tfCP.getText())) {
				tfCP.requestFocus();
				tfCP.setStyle("-fx-background-color: #ffbbbb;");
				lbError.setText(I18N.getTexto("Debe rellenar el campo código postal"));
				valido = false;
			}
			else if(StringUtils.isBlank(tfPoblacion.getText())) {
				tfPoblacion.requestFocus();
				tfPoblacion.setStyle("-fx-background-color: #ffbbbb;");
				lbError.setText(I18N.getTexto("Debe rellenar el campo población"));
				valido = false;
			}
		}
		/* fin BRICO-253 */

		/* Se añade como campo obligatorio el tipo de documento  de identificacion*/
		if (valido) {
			if (cbTipoDocIdent.getSelectionModel().getSelectedItem() == null) {
				cbTipoDocIdent.requestFocus();
				lbError.setText(I18N.getTexto("Debe seleccionar el tipo de documento"));
				valido = false;
			}
		}
		
		return valido;
	}
	
	@FXML
	public void accionEnviarVisor() {

		if(visor.getEstado() == VisorPantallaSecundaria.APAGADO) {
			log.error("No se puede completar proceso, visor apagado.");
			VentanaDialogoComponent.crearVentanaError(this.getStage(), "Lo sentimos, el visor no se encuentra disponible", new RuntimeException());
			return;
		}
		
		if (validarDatos()) {
			/* CREAMOS EL DIÁLOGO DE CARGA PERSONALIZADO, ESTO DEBERÍA HACERSE UNA SOLA VEZ EN EL POSAPPLICATION */
			VentanaEspera.crearVentanaCargando(getStage());
			VentanaEspera.setMensaje(I18N.getTexto("Pendiente Validación del Cliente"));
			VentanaEspera.mostrar();

			((BricodepotVisorPantallaSecundaria) visor).modoInfoCliente(this);

			((BricodepotVisorPantallaSecundaria) visor).setListenerClienteOK(new ProcesoValidacionClienteListener(){

				@Override
				public void procesoValidacionOK() {
					log.debug("procesoValidacionOK() - Se ha terminado el proceso de confirmación de cliente por la parte del visor. Se procede a realizar la inserción.");
					VentanaEspera.cerrar();
					accionAceptar();
				}
			});
		}
	}
	
	@FXML
	public void accionCancelar() {
		setModoEdicion(false);
		if(clientes == null){
			getApplication().getMainView().close();
		}
		else{
			if(cliente == null){
				cliente = clientes.get(indexCliente).getCliente();
			}
			cliente = clientes.get(indexCliente).getCliente();
			refrescarDatosPantalla();
			estadoCliente = Estado.SIN_MODIFICAR;
		}
	}
	
	/* BRICO-253 generación de informe jasper de facturación */
	public void accionImprimir() {
		log.debug("accionImprimir()");
		
		List<ClienteBean> clientes = new ArrayList<ClienteBean>();
		HashMap<String, Object> parametros = new HashMap<String, Object>();

		//Muestra check de alta o modificación dependiendo de lo que es
		parametros.put("ES_ALTA",Boolean.valueOf(cliente.getEstadoBean()==Estado.NUEVO));
		
		clientes.add(cliente);
		parametros.put(ImpresionJasper.LISTA, clientes);

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
			ServicioImpresion.imprimir("jasper/clientes/formulariocliente", parametros);
		}
		catch (DeviceException e) {
			log.error("Ha ocurrido un error al imprimir el informe ", e);
		}
	}

	@Override
	public void accionAceptar() {
		// Si el foco está en el input de código postal lo pasamos a otro sitio para que muestre la ventana de códigos postales si es necesario,
		// y evitar el bloqueo con otros mensajes que puedan salir
		if(tfCP.isFocused()) {
			tfDescripcion.requestFocus();
		}
		
		ClienteBean datosClienteSalvar = new ClienteBean();
		
		if(!(cliente == null)){
			BeanUtils.copyProperties(cliente, datosClienteSalvar);	
		}
			
		
		if(validarDatos()){
			if(estadoCliente == Estado.NUEVO){
				if(cliente == null){
					//cliente = new ClienteBean();
					datosClienteSalvar.setIdGrupoImpuestos(sesion.getAplicacion().getTienda().getCliente().getIdGrupoImpuestos());
					//El código de cliente no se puede editar, así que el que traiga el cliente se le queda, si no, el cif del cliente
					datosClienteSalvar.setCodCliente(tfNumDocIdent.getText().toUpperCase());
				
				}
			}
			
			//datosClienteSalvar.setEstadoBean(cliente.getEstadoBean());
			//datosClienteSalvar.setIdGrupoImpuestos(cliente.getIdGrupoImpuestos());
			//datosClienteSalvar.setCodCliente(cliente.getCodCliente());	
			datosClienteSalvar.setEstadoBean(estadoCliente);
			datosClienteSalvar.setCodpais(tfCodPais.getText());
			datosClienteSalvar.setCif(tfNumDocIdent.getText().toUpperCase());
			datosClienteSalvar.setCp(tfCP.getText());
			datosClienteSalvar.setDomicilio(tfDomicilio.getText());
			datosClienteSalvar.setPoblacion(tfPoblacion.getText());
			datosClienteSalvar.setLocalidad(tfLocalidad.getText());
			datosClienteSalvar.setTelefono1(tfTelefono.getText());
			datosClienteSalvar.setTelefono2(tfTelefono2.getText());
			datosClienteSalvar.setPais(tfDesPais.getText());
			if (cbTipoDocIdent.getSelectionModel().getSelectedItem() != null) {
				datosClienteSalvar.setTipoIdentificacion(cbTipoDocIdent.getSelectionModel().getSelectedItem().getCodTipoIden());
			}
			datosClienteSalvar.setNombreComercial(tfRazonSocial.getText());
			datosClienteSalvar.setProvincia(tfProvincia.getText());
			datosClienteSalvar.setActivo(cbActivo.isSelected());
			datosClienteSalvar.setObservaciones(taObservaciones.getText());
			datosClienteSalvar.setDesCliente(tfDescripcion.getText());
			datosClienteSalvar.setFax(tfFax.getText());
			datosClienteSalvar.setEmail(tfEmail.getText());
			datosClienteSalvar.setIdTratImpuestos(cbTratamientoImpuestos.getSelectionModel().getSelectedItem().getIdTratImpuestos());

			datosClienteSalvar.setBanco(tfBanco.getText());
			datosClienteSalvar.setBancoDomicilio(tfBancoDomicilio.getText());
			datosClienteSalvar.setBancoPoblacion(tfBancoPoblacion.getText());
			datosClienteSalvar.setCcc(tfBancoCCC.getText());
			
			String codTar = tfTarifa.getText();
			
			if(StringUtils.isBlank(codTar)){
				codTar = null;
			}
			
			datosClienteSalvar.setCodtar(codTar);
			
			if(checkClientTaxes(datosClienteSalvar)) {
				if(datosClienteSalvar.getEstadoBean() == Estado.NUEVO){
					try {
						boolean bSalvar = true;
						String sResultado = validarDocumento();
						if(sResultado != null) {
							bSalvar = VentanaDialogoComponent.crearVentanaConfirmacion(sResultado, getStage());
							if(!bSalvar) {
								tfNumDocIdent.requestFocus();
								return;
							}
						}

						/* BRICO-253 guardar cliente en central */
				        String apiKey = variablesServices.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
						String uidActividad = sesion.getAplicacion().getUidActividad();
						
						BricodepotClienteRequestRest clienteRequestRest = new BricodepotClienteRequestRest();
						clienteRequestRest.setCliente(BricodepotClientesService.castClientePosAApi(datosClienteSalvar));
						clienteRequestRest.setApiKey(apiKey);
						clienteRequestRest.setUidActividad(uidActividad);
						
						BricodepotClienteBean clienteAPI = BricodepotClientesRest.salvar(clienteRequestRest);
						if(clienteAPI.getResultado() == 0) {
							VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("No se pudo crear el cliente."), this.getStage());
						}
						/* fin BRICO-253 */
						
					}
					catch (RestException e) {
						log.error("Error actualizando el cliente.",e);
						VentanaDialogoComponent.crearVentanaError(this.getStage(), e);
						return;
					}
					catch (RestHttpException e) {
						log.error("Error actualizando el cliente.",e);
						VentanaDialogoComponent.crearVentanaError(this.getStage(), e);
						return;
					}catch (ClientErrorException e) {
						log.error("Error actualizando el cliente: "+e.getMessage(),e);
						VentanaDialogoComponent.crearVentanaError(this.getStage(), e);
						return;
					}
					
				}
				else{
					boolean bSalvar = true;
					String sResultado = validarDocumento();
					if(sResultado != null) {
						bSalvar = VentanaDialogoComponent.crearVentanaConfirmacion(sResultado, getStage());
						if(!bSalvar) {
							tfNumDocIdent.requestFocus();
							return;
						}
					}

					/* BRICO-253 guardar documento generado en almacenamiento digital */
					if(!guardarDatosCliente(datosClienteSalvar)) {
						return;
					}
					/* fin BRICO-253 */
				}

				if(cliente == null){
					cliente = new ClienteBean();
				}
				BeanUtils.copyProperties(datosClienteSalvar, cliente);
				
				/* BRICO-253 y BRICO-325 si el cliente es de ES, se registra ticket en central, si no, no se registra */
				if("ES".equals(sesion.getAplicacion().getTienda().getCliente().getCodpais())) {
					registrarTicketCliente();
				}
				else if(!Arrays.asList("ES","PT").contains(sesion.getAplicacion().getTienda().getCliente().getCodpais())) {
					log.warn("accionAceptar() - El cliente es de un país distinto a ES/PT");
					//Modificar este if si se añaden más países
				}
				
				for(View vista : getApplication().getMainView().getSubViews()) {
					if(vista instanceof IdentificacionClienteView) {
						HashMap<String, Object> mapDatos = new HashMap<String, Object>();
						mapDatos.put(CLIENTE_EDITADO, cliente);
						setDatos(mapDatos);
						vista.getController().setDatos(mapDatos);
					}
					else if(vista instanceof BricodepotBuscarClienteView) {
						((BricodepotBuscarClienteController) vista.getController()).accionBuscar();
					}
				}	
				
				setModoEdicion(false);
				getApplication().getMainView().close();
			}
		}
	}
	

	@Override
	protected boolean guardarDatosCliente(ClienteBean cliente){
		
		/* BRICO-253 guardar en central y guardar documento generado en almacenamiento digital*/
		
		try {
			String apiKey = variablesServices.getVariableAsString(VariablesServices.WEBSERVICES_APIKEY);
			String uidActividad = sesion.getAplicacion().getUidActividad();
			
			BricodepotClienteRequestRest clienteRequestRest = new BricodepotClienteRequestRest();
			clienteRequestRest.setCliente(BricodepotClientesService.castClientePosAApi(cliente));
			clienteRequestRest.setApiKey(apiKey);
			clienteRequestRest.setUidActividad(uidActividad);
			
			BricodepotClienteBean clienteAPI = BricodepotClientesRest.salvar(clienteRequestRest);
			if(clienteAPI.getResultado() == 0) {
				VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("No se encontró el cliente cuyos datos se desean modificar."), this.getStage());
			}
			
		} catch(RestHttpException e) {
			log.error("Error actualizando el cliente.",e);
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("No se pudo crear el cliente."), this.getStage());
			return false;
		}
		catch (RestException e) {
			log.error("Error actualizando el cliente.",e);
			VentanaDialogoComponent.crearVentanaAviso(I18N.getTexto("No se pudo crear el cliente."), this.getStage());
			return false;
		}
		return true;
		/* fin BRICO-253 */
	}
	
	/* BRICO-253 */
	/**
	 * Genera documento de facturación de cliente que se envía a BO -> procesador -> FTP.
	 * <br/><br/>
	 * También lo imprime en pantalla.
	 */
	private void registrarTicketCliente() {
		log.debug("registrarTicketCliente()");
		
		TicketClienteCaptacion ticketCliente = new TicketClienteCaptacion();
		ticketCliente.setPdfCliente(BricodepotVisorPantallaSecundaria.getFirma());
		ticketCliente.setCif(cliente.getCif());
		ticketCliente.setOperacion(cliente.getEstadoBean() == Estado.NUEVO ? "ALTA" : "MOD"); // ALTA o MOD(ificación)
		bricodepotClientesService.registrarTicketCliente(ticketCliente, cliente);

		accionImprimir();

	}
	
}